"""Original synthetic examples. Development and final phrases never enter training."""
from pathlib import Path
import json,re,hashlib
root=Path(__file__).resolve().parents[1]
source=(root/'src/com/bananalab/lite/JavaEditModel.java').read_text()
def field(name):
    start=source.index(name+'=')+len(name)+1
    raw=source[start:source.index(';',start)].replace('{','[').replace('}',']')
    return json.loads(raw)
ops=field('OPS');train=field('TRAIN');dev=field('HELDOUT');final=field('FINAL');contexts=field('CONTEXTS')
out=root/'assets/java-specialist';out.mkdir(exist_ok=True)
counts={}
for split,groups in [('train',train),('development',dev),('evaluation',final)]:
    rows=[]
    for label,phrases in enumerate(groups):
        for number,text in enumerate(phrases):
            for context in (contexts if split=='train' else ['']):
                name='Ejemplo'+str(label)+str(number)
                op=ops[label]
                expression='Math.'+op+'(a,b)' if op in ('min','max') else 'a'+op+'b'
                before='package com.bananalab.lite; final class '+name+' { static float valor(float a,float b){ return a+b; } }'
                after='package com.bananalab.lite; final class '+name+' { static float valor(float a,float b){ return '+expression+'; } }'
                rows.append({'instruction':(text+' '+context).strip(),'before':before,'after':after,'operator':op,'split':split,'source':'original synthetic example created for Banana'})
    raw=''.join(json.dumps(row,ensure_ascii=False)+'\n' for row in rows)
    (out/(split+'.jsonl')).write_text(raw)
    counts[split]={'examples':len(rows),'sha256':hashlib.sha256(raw.encode()).hexdigest()}
(out/'PROVENANCE.json').write_text(json.dumps({'origin':'Original synthetic code and Spanish instructions authored for this project; no third-party code corpus','scope':'operator selection; six arithmetic operations; not general Java code generation','splits':counts},indent=2))
