"""Index app-owned Java sources only. Never includes keys or build inputs."""
from pathlib import Path
import json,hashlib,re
root=Path(__file__).resolve().parents[1]
src=root/'src/com/bananalab/lite'
out=root/'assets/app-source'
out.mkdir(exist_ok=True)
for old in out.glob('*.java'):old.unlink()
names={p.stem for p in src.glob('*.java')}
files=[]
for p in sorted(src.glob('*.java')):
    if p.name.endswith('Instrumentation.java'):continue
    text=p.read_text()
    (out/p.name).write_text(text)
    deps=sorted((set(re.findall(r'\b[A-Z][A-Za-z0-9]+\b',text)) & names)-{p.stem})
    files.append({'name':p.name,'path':'src/com/bananalab/lite/'+p.name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'references':deps})
(root/'assets/code-map.json').write_text(json.dumps({'release':'0.73','files':files},ensure_ascii=False))
