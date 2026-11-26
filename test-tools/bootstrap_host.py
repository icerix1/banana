"""Restore host-only lowering test dependencies from the included ECJ sources."""
from pathlib import Path
import sys,zipfile,subprocess
r=Path(__file__).resolve().parent.parent
w=Path(sys.argv[1]).resolve();cache=Path(sys.argv[2]).resolve();w.mkdir(parents=True,exist_ok=True)
src=w/'ecj-src';zipfile.ZipFile(r/'model-source/ecj-4.6.1-sources.jar').extractall(src)
files=[str(p) for p in src.rglob('*.java') if p.name!='JDTCompilerAdapter.java' and '/compiler/tool/' not in str(p)]
(w/'ecj-sources.txt').write_text('\n'.join(files))
with (w/'ecj-build.log').open('w') as f:subprocess.run(['java','-jar',str(cache/'ecj.jar'),'-8','-encoding','UTF-8','-d',str(w/'ecj-classes'),'@'+str(w/'ecj-sources.txt')],stdout=f,stderr=subprocess.STDOUT,check=True)
with zipfile.ZipFile(w/'ecj-old.jar','w') as z:
 for p in (w/'ecj-classes').rglob('*.class'):z.write(p,p.relative_to(w/'ecj-classes').as_posix())
 for p in src.rglob('*'):
  if p.is_file() and not p.name.endswith('.java'):z.write(p,p.relative_to(src).as_posix())
subprocess.run(['java','-jar',str(cache/'ecj.jar'),'-8','-classpath',str(w/'ecj-old.jar'),'-d',str(w/'lower-classes'),str(r/'tools/lowering/SourceLowerer.java')],check=True)
