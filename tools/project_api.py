"""Compilation-only Android framework stubs, without framework resources or executables."""
from pathlib import Path
import os,zipfile,hashlib,json,subprocess
root=Path(__file__).resolve().parents[1]
source=Path(os.environ['ANDROID_JAR'])
target=root/'assets/java-tools/android-api.jar'
stubs=root/'out/java8-stubs'
stubs.mkdir(parents=True,exist_ok=True)
subprocess.run(['java','-jar',os.environ['ECJ_JAR'],'-8','-encoding','UTF-8','-classpath',str(source),'-d',str(stubs)]+[str(p) for p in (root/'tools/java8-stubs').rglob('*.java')],check=True)
with zipfile.ZipFile(source) as old,zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as out:
    for entry in old.infolist():
        if entry.filename.endswith('.class') or entry.filename.startswith('NOTICES/'):
            info=zipfile.ZipInfo(entry.filename,(1980,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED
            out.writestr(info,old.read(entry.filename))
    for stub in stubs.rglob('*.class'):
        info=zipfile.ZipInfo(stub.relative_to(stubs).as_posix(),(1980,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED
        out.writestr(info,stub.read_bytes())
(root/'assets/java-tools/android-api-provenance.json').write_text(json.dumps({'source':'Android SDK API35 ext15 android.jar','content':'class stubs and upstream notices only; no resources or runtime implementation','sha256':hashlib.sha256(target.read_bytes()).hexdigest()}))
