"""App JVM class baseline for ABI validation. Not executable code for ART."""
from pathlib import Path
import zipfile,json,hashlib
r=Path(__file__).resolve().parents[1];names={Path(x['name']).stem for x in json.loads((r/'assets/code-map.json').read_text())['files']};target=r/'assets/java-tools/app-api.jar'
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted((r/'classes/com/bananalab/lite').glob('*.class')):
  if p.stem.split('$')[0] in names:
   e=zipfile.ZipInfo('com/bananalab/lite/'+p.name,(1980,1,2,0,0,0));e.compress_type=zipfile.ZIP_DEFLATED;z.writestr(e,p.read_bytes())
(r/'assets/java-tools/app-api-provenance.json').write_text(json.dumps({'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'sourceMapHash':hashlib.sha256((r/'assets/code-map.json').read_bytes()).hexdigest(),'purpose':'App-owned class baseline, ABI validation only'}))
