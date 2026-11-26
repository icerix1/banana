"""Reproduce the signing-only Android helper from the owner's SDK Build Tools (no private keys)."""
from pathlib import Path
import os,zipfile,tempfile,subprocess,hashlib,json
from signcompat_patch import adapt
root=Path(__file__).resolve().parents[1];tools=Path(os.environ['ANDROID_TOOLS']);api=Path(os.environ['ANDROID_JAR']);upstream=tools/'lib/apksigner.jar'
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp);source=p/'apksig.jar'
 with zipfile.ZipFile(upstream) as z,zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED) as out:
  for name in sorted(z.namelist()):
   if name.startswith('com/android/apksig/') and name.endswith('.class'):out.writestr(name,adapt(z.read(name)))
 classes=p/'compat';classes.mkdir()
 subprocess.run(['java','-jar',os.environ['ECJ_JAR'],'-8','-encoding','UTF-8','-classpath',str(api),'-d',str(classes)]+[str(f) for f in (root/'tools/signcompat-src').rglob('*.java')],check=True)
 with zipfile.ZipFile(source,'a',zipfile.ZIP_DEFLATED) as z:
  for f in sorted(classes.rglob('*.class')):z.write(f,f.relative_to(classes).as_posix())
 if os.environ.get('SIGNER_COMPAT_JVM_OUT'):
  import shutil;shutil.copyfile(source,os.environ['SIGNER_COMPAT_JVM_OUT'])
 subprocess.run([str(tools/'d8'),'--min-api','23','--lib',str(api),'--output',str(p),str(source)],check=True)
 target=root/'assets/java-tools/apksig.jar'
 with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
  info=zipfile.ZipInfo('classes.dex',(1980,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,(p/'classes.dex').read_bytes())
 (root/'assets/java-tools/apksig-provenance.json').write_text(json.dumps({'source':'Android Build Tools35.0.0 apksigner.jar com/android/apksig classes, D8 min-api23','license':'Apache-2.0','adaptation':'Java8 utility calls redirected to own API23-compatible helpers; cryptographic algorithms unchanged; tools/signcompat_patch.py','upstream_sha256':hashlib.sha256(upstream.read_bytes()).hexdigest(),'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}))
