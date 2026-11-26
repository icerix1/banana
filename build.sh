#!/bin/sh
# Codigo fuente privado: keys/ contiene la firma de esta app. No publicar.
set -eu
cd "$(dirname "$0")"
: "${ANDROID_TOOLS:?Ruta a Android Build Tools 35.0.0}"
: "${ANDROID_JAR:?Ruta a android.jar API 35}"
: "${ECJ_JAR:?Ruta a Eclipse ECJ 3.38.0}"
python3 tools/index_sources.py
python3 tools/project_api.py
python3 tools/java_corpus.py
rm -rf classes out/dex
mkdir -p classes out/dex
java -jar "$ECJ_JAR" -8 -encoding UTF-8 -classpath "$ANDROID_JAR" -d classes src/com/bananalab/lite/*.java
python3 tools/app_api.py
"$ANDROID_TOOLS/aapt2" compile --dir res -o out/resources.zip
"$ANDROID_TOOLS/aapt2" link -o out/base.apk -I "$ANDROID_JAR" --manifest AndroidManifest.xml -A assets out/resources.zip
"$ANDROID_TOOLS/d8" --release --min-api 23 --lib "$ANDROID_JAR" --output out/dex classes/com/bananalab/lite/*.class
python3 - <<'PY'
import shutil,zipfile
shutil.copy('out/base.apk','out/unsigned.apk')
with zipfile.ZipFile('out/unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:z.write('out/dex/classes.dex','classes.dex')
PY
"$ANDROID_TOOLS/zipalign" -f 4 out/unsigned.apk out/aligned.apk
IFS= read -r BANANA_LITE_KEYPASS < keys/release-password.txt || true
export BANANA_LITE_KEYPASS
"$ANDROID_TOOLS/apksigner" sign --ks keys/release.p12 --ks-key-alias bananalab-lite --ks-pass env:BANANA_LITE_KEYPASS --key-pass env:BANANA_LITE_KEYPASS --out out/BananaLab-Lite.apk out/aligned.apk
"$ANDROID_TOOLS/apksigner" verify --verbose out/BananaLab-Lite.apk
