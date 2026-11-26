"""Fixture de paridad Python↔Java: tokens y logits de referencia del lector entrenado.
Uso: export_fixture.py salida_entrenamiento/ fixture.json tokenizer.json"""
import sys,os,json,torch
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
import lector_tok as T
from lector_model import Lector
run,out,tokenizer=sys.argv[1],sys.argv[2],sys.argv[3];merges=json.load(open(tokenizer))['merges'];tok=T.Tokenizer(merges)
from train_lector import CFG
model=Lector(tok.vocab_size,**CFG);model.load_state_dict(torch.load(os.path.join(run,'state.pt')));model.eval()
texts=['La fotosíntesis es un proceso químico. ¿Qué pasó en 1987, niño?','  Hola,   mundo!\nSegunda línea\tcon tab… y «comillas» — fin.','email@ejemplo.com 3,14 x²+y² 北京 ÑANDÚ über','a'*120+' palabra-compuesta e.g. 100%','']
cases=[{'text':t,'ids':tok.encode(t)} for t in texts]
fragment='La brumelia es una planta ficticia de hojas azules. Crece en valles húmedos del norte. Sus semillas se usan para teñir telas.'
ids=[2,3]+tok.encode(fragment)+[3]+tok.encode('que es la brumelia')+[4]
with torch.no_grad():logits=model(torch.tensor([ids]))[0,-1]
top=torch.topk(logits,8)
json.dump({'tokenizer':cases,'prompt':ids,'top_ids':top.indices.tolist(),'top_logits':[round(float(v),5) for v in top.values]},open(out,'w'),ensure_ascii=False)
print('fixture',len(ids),top.indices.tolist()[:3])
