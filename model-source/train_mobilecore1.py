"""Own compact topic router, random initialization. Inference is local Android Java."""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,numpy as np,random,json,struct,re,unicodedata
from pathlib import Path
from mobile_lessons import LESSONS
r=Path(__file__).resolve().parents[1];out=r/'assets/mobilecore1';out.mkdir(parents=True,exist_ok=True)
random.seed(39039);torch.manual_seed(39039);torch.set_num_threads(2)
D=1024
for a in LESSONS:
 if a['title'].lower() not in a['keywords']:a['keywords'].append(a['title'].lower())
labels=[a['key'] for a in LESSONS]+['unknown']
def features(q):
 text='^'+' '.join(re.sub('[^a-z0-9 ]',' ',''.join(c for c in unicodedata.normalize('NFD',q.lower()) if unicodedata.category(c)!='Mn')).split())[:240]+'$';v=np.zeros(D,np.float32)
 for n in [3,4,5]:
  for i in range(len(text)-n+1):
   h=2166136261
   for c in text[i:i+n]:h=((h^ord(c))*16777619)&0xffffffff
   at=(h&0x7fffffff)%D;v[at]=min(3,v[at]+1)
 v/=max(1e-6,np.linalg.norm(v));return v
train=[];held=[]
for i,a in enumerate(LESSONS):
 for q in a['queries']:
  for pre in ['','banana ','che ','por favor ','una pregunta ']:
   for post in ['',' porfa',' con un ejemplo']:train.append((pre+q+post,i))
 for q in ['me ayudas a comprender '+a['title'].lower(),'necesito una explicacion sencilla de '+a['title'].lower(),'podrias contarme para que sirve '+a['title'].lower()]:held.append((q,i))
unknown=['quien gano las elecciones','cuanto vale el dolar hoy','tengo dolor de cabeza','crea una banana','pone a Tony azul','hace que Milo salte','quiero hacer una animacion con Tony','no el que entra debe ser azul','que voz tiene Milo','necesito un video mas largo','me llamo Tiziano','hola como estas','que recordas de mi','cual es mi contrasena','como aprendo cocina','como hago una torta','que planeta es mas grande','cuando nacio esa persona','cual es la capital de noruega','dame recomendaciones de hoteles','es legal vender eso','quien tiene la caja','aplica el plan','borra la biblioteca web','aprende sobre dinosaurios','que es un dinosaurio','por que el cielo es azul','que pasa con las vacunas','como hacer una escena','cual es el mejor telefono']
for q in unknown:
 for pre in ['','banana ','che ','por favor ','una pregunta ']:
  for post in ['',' porfa',' ahora']:train.append((pre+q+post,len(LESSONS)))
for q in ['que tiempo va a hacer','quiero comprar una casa','necesito arreglar mi escena','cambia la cola de Nube','donde consigo un medicamento','quien gano ayer','contame un chiste nuevo','quiero un dibujo lindo']:held.append((q,len(LESSONS)))
assert not(set(q for q,_ in train)&set(q for q,_ in held))
X=torch.tensor(np.stack([features(q) for q,_ in train]));Y=torch.tensor([y for _,y in train]);TX=torch.tensor(np.stack([features(q) for q,_ in held]));TY=torch.tensor([y for _,y in held]);net=torch.nn.Sequential(torch.nn.Linear(D,64),torch.nn.ReLU(),torch.nn.Linear(64,len(labels)));opt=torch.optim.AdamW(net.parameters(),lr=.003)
for step in range(1800):
 ix=torch.randint(len(X),(96,));loss=torch.nn.functional.cross_entropy(net(X[ix]),Y[ix]);opt.zero_grad();loss.backward();opt.step()
with torch.no_grad():scores=net(TX).softmax(1);pred=scores.argmax(1);covered=scores.max(1).values>=.85;accuracy=float((pred==TY).float().mean());accepted=int(covered.sum());correct=int(((pred==TY)&covered).sum())
card={'name':'Banana Mobile Core topic router 1','role':'Own learned topic selection for authored offline lessons. Not a general language generator.','external_weights':False,'external_teacher':False,'train_examples':len(train),'heldout_examples':len(held),'heldout_accuracy_synthetic':accuracy,'confident_coverage':accepted/len(held),'confident_accuracy':correct/max(accepted,1),'threshold':.85,'promoted':accuracy>=.80 and correct/max(accepted,1)>=.90,'limitations':'Synthetic authored topic/paraphrase evaluation only; inference additionally requires a known topical keyword and excludes live/private/project commands. No claim of universal understanding.'}
card['heldout']=[dict(q=q,expected=labels[y],actual=labels[int(pred[i])],confidence=float(scores[i].max())) for i,(q,y) in enumerate(held)]
(out/'model-card.json').write_text(json.dumps(card,indent=2,ensure_ascii=False)+'\n');(out/'lessons.json').write_text(json.dumps(LESSONS,ensure_ascii=False,indent=2)+'\n');(out/'labels.txt').write_text('\n'.join(labels)+'\n');(r/'model-source/mobilecore1-corpus.json').write_text(json.dumps({'train':[{'q':q,'label':labels[y]} for q,y in train],'heldout':[{'q':q,'label':labels[y]} for q,y in held]},ensure_ascii=False,indent=2)+'\n')
with (out/'mobilecore1.bin').open('wb') as f:
 f.write(struct.pack('>4i',0x424e4c31,D,64,len(labels)))
 for layer in [net[0],net[2]]:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
fixtures=[dict(q=q,logits=net(torch.tensor(features(q))).detach().tolist()) for q,_ in held[:6]];(r/'test/resources/mobilecore1-parity.json').write_text(json.dumps(fixtures)+'\n')
print(json.dumps({k:v for k,v in card.items() if k!='heldout'},ensure_ascii=False),flush=True)
if not card['promoted']:raise SystemExit('Not promoted: topic gate failed')
