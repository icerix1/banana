"""Own Chat3.5 continuation from own Chat3.4 weights. No external model/teacher.
Replays original authored data and adds held-out wording families for project work.
"""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import random,re,json,struct,math,time,unicodedata,hashlib
import numpy as np
from pathlib import Path
root=Path(__file__).resolve().parents[1];out=root/'model-source/chat35-candidate';out.mkdir(parents=True,exist_ok=True)
# Recover the authored prior corpus, without invoking its tokenizer or training.
source=(root/'model-source/train_chat34.py').read_text();exec(compile(source[:source.index('# A byte vocabulary')],str(root/'model-source/train_chat34.py'),'exec'))
out=root/'model-source/chat35-candidate';random.seed(35037);torch.manual_seed(35037);torch.set_num_threads(2)
old_train=list(train)
concepts=[
(['como continuo la escena','quiero agregar lo que sigue','continua sin borrar lo anterior'],'como sigo con otra accion sin perder la secuencia','continuar agrega acciones despues de las existentes y conserva los diseños .'),
(['que significa el anterior','a quien se refiere el anterior','como elegis al anterior'],'si digo el de antes a quien elegis','el anterior es el personaje enfocado antes del actual . si no esta claro necesito su nombre .'),
(['el que recibio es quien tiene la caja','recibir y tener es lo mismo','que diferencia hay entre recibir y sostener'],'puede recibir la caja y luego pasarla a otro','recibir describe un evento . quien tiene la caja depende de las entregas posteriores .'),
(['cambia solo su sombrero','modifica un accesorio y nada mas','como cambio solamente el sombrero'],'quiero otro sombrero sin cambiar su cuerpo','un cambio de accesorio conserva el color del cuerpo , la identidad , las acciones y los demas personajes .'),
(['dos acciones juntas para el mismo personaje','puede bailar y saltar al mismo tiempo','hay acciones incompatibles'],'que pasa si el mismo actor hace dos cosas en un tiempo','cada personaje admite una accion por tiempo . necesito separar las acciones incompatibles .'),
(['no tiene la caja y quiere entregarla','puede entregar una caja ajena','como verificas una entrega'],'si otro sostiene el objeto puede pasarlo este personaje','antes de entregar verifico quien sostiene el objeto . si pertenece a otro personaje necesito corregir el plan .'),
(['quien es ese','a cual le decis ese','que significa esa referencia'],'como sabes de quien hablo cuando digo ese','uso el personaje enfocado y el estado del proyecto . si hay varias opciones necesito una aclaracion .'),
(['que haces si el pedido es ambiguo','no sabes cual personaje quiero','si hay varios posibles personajes'],'elegis cualquier personaje cuando hay dudas','si no hay una referencia unica necesito que nombres al personaje . conservo la escena anterior .'),
(['como compruebo lo que cambiaste','que muestra el registro de pasos','para que sirve que hiciste'],'como reviso las modificaciones del ultimo pedido','el registro describe pasos aplicados y cambios comprobados en el estado . la vista previa permite revisar la apariencia .'),
(['como corrijo un recuerdo','quiero cambiar una memoria','como edito un dato guardado'],'puedo actualizar un dato sin borrar los otros','pedi mis recuerdos y usa su numero para corregir uno . los demas recuerdos se conservan .'),
(['como borro un recuerdo','quiero olvidar solo un dato','como saco un dato guardado'],'puedo eliminar una memoria individual','pedi mis recuerdos y usa su numero para borrar solamente ese recuerdo .'),
(['los videos tienen audio','que sonido genera banana','el mp4 tiene sonido'],'que escucho al exportar un video','el mp4 puede incluir ambiente y pasos sintetizados . todavia no genera voces ni dialogos hablados .'),
(['ya sos como chatgpt','sos una inteligencia general','entendes cualquier pedido'],'podes resolver todo como una ia grande','soy un modelo propio pequeño y experimental . las herramientas resuelven operaciones limitadas del proyecto .'),
(['que pasa si un paso falla','puede quedar un cambio a medias','si no entendes una parte del pedido'],'un paso desconocido modifica el resto de la escena','si un paso no se puede aplicar conservo la escena anterior y explico que parte necesita aclaracion .'),
(['que se conserva al continuar','continuar cambia los diseños','que guarda una continuacion'],'seguir la historia reemplaza los personajes anteriores','conservo los diseños y las acciones anteriores . agrego los nuevos pasos al final de la secuencia .'),
(['reemplazar toda la secuencia','quiero una secuencia nueva','como borro el orden anterior'],'como diferencio empezar de nuevo y seguir','continuar agrega al final . reemplazar la secuencia cambia las acciones anteriores de forma explicita .')]
new=[];held=[]
for ci,(phrases,probe,answer) in enumerate(concepts):
 for q in phrases:new.append((tuple(),q,answer))
 held.append((tuple(),probe,answer))
# Authored multi-turn references; held-out identities are excluded from all new training.
train_people=['tony','milo','nora','ana','kiari','lucas','sofia','bruno']
held_people=['celia','ramiro','olivia','gael']
for name in train_people+held_people:
 for other in ['pepino','tomate','robot']:
  history=(('selecciona a '+name,'el foco es '+name+' .'),('ahora selecciona al '+other,'el foco es '+other+' y el anterior es '+name+' .'))
  rows=[(history,'quien era el anterior','el anterior es '+name+' .'),(history,'quien esta seleccionado','esta seleccionado el '+other+' .')]
  (held if name in held_people else new).extend(rows)
for a,b,c in [('tony','milo','nora'),('ana','kiari','lucas'),('celia','gael','olivia')]:
 h=((a+' entrega la caja a '+b,b+' recibe la caja .'),(b+' entrega la caja a '+c,c+' tiene la caja .'))
 rows=[(h,'quien tiene la caja','la tiene '+c+' .'),(h,'quien recibio primero','primero la recibio '+b+' .')]
 (held if a=='celia' else new).extend(rows)
# One split manifest, with exact no-overlap verification. Old tests also remain held out.
key=lambda row:json.dumps(row,ensure_ascii=False)
assert not(set(map(key,new))&set(map(key,held)))
(root/'model-source/chat35-corpus.json').write_text(json.dumps({'origin':'authored own templates; no teacher','train_new':new,'heldout_new':held,'previous_corpus':'train_chat34.py before BPE','train_identities':train_people,'heldout_identities':held_people},ensure_ascii=False,indent=2))
prior=root/'assets/chat34';tokenizer=json.loads((prior/'tokenizer.json').read_text());merges=tokenizer['merges'];vocab=(prior/'vocab.txt').read_text().splitlines();pieces={i:bytes.fromhex(vocab[i]) for i in range(6,len(vocab))}
def encode(text):
 seq=[b+6 for b in text.encode('utf8')]
 for left,right,idx in merges:
  n=[];i=0
  while i<len(seq):
   if i+1<len(seq) and seq[i]==left and seq[i+1]==right:n.append(idx);i+=2
   else:n.append(seq[i]);i+=1
  seq=n
 return seq
CTX=384;D=128;HEADS=4;LAYERS=3;FF=512
exec(compile(source[source.index('class Block(nn.Module):'):source.index('net=Banana();opt=')],str(root/'model-source/train_chat34.py'),'exec'))
net=Banana()
params=[(net.e.weight,False),(net.pos.weight,False)]
for b in net.blocks:params += [(b.n1.weight,False),(b.n1.bias,False),(b.qkv.weight,True),(b.qkv.bias,False),(b.proj.weight,True),(b.proj.bias,False),(b.n2.weight,False),(b.n2.bias,False),(b.f1.weight,True),(b.f1.bias,False),(b.f2.weight,True),(b.f2.bias,False)]
params += [(net.norm.weight,False),(net.norm.bias,False),(net.head.weight,True),(net.head.bias,False)]
with (prior/'chat34.bin').open('rb') as f:
 header=struct.unpack('>7i',f.read(28));assert header==(0x42544631,len(vocab),D,HEADS,LAYERS,CTX,FF)
 with torch.no_grad():
  for param,transpose in params:
   a=np.frombuffer(f.read(param.numel()*4),dtype='>f4').astype('float32');shape=tuple(reversed(param.shape)) if transpose else tuple(param.shape);a=a.reshape(shape);a=a.T if transpose else a;param.copy_(torch.from_numpy(a.copy()))
 assert f.read()==b''
def pack(row):
 hist,q,a=row;tokens=[2]
 for u,reply in hist:tokens += [3]+encode(u)+[4]+encode(reply)+[5]
 tokens += [3]+encode(q)+[4];start=len(tokens);tokens += encode(a)+[5]
 if len(tokens)>CTX:return None
 x=tokens[:-1];y=[v if i>=start-1 else -100 for i,v in enumerate(tokens[1:])]
 return x+[0]*(CTX-1-len(x)),y+[-100]*(CTX-1-len(y)),len(x)
def dataset(rows):
 entries=[p for p in map(pack,rows) if p];x,y,n=zip(*entries);return torch.tensor(x),torch.tensor(y),torch.tensor(n)
RX,RY,RN=dataset(old_train);NX,NY,NN=dataset(new);HX,HY,HN=dataset(held)
@torch.no_grad()
def generate(q,hist=()):
 tokens=[2]
 for u,a in hist:tokens += [3]+encode(u)+[4]+encode(a)+[5]
 tokens += [3]+encode(q)+[4];result=[]
 for _ in range(min(100,CTX-len(tokens))):
  logits=net(torch.tensor([tokens]))[0,-1];logits[:5]=-1e9;idx=int(logits.argmax())
  if idx==5:break
  tokens.append(idx);result.append(idx)
 return b''.join(pieces[i] for i in result if i>=6).decode('utf8',errors='replace')
@torch.no_grad()
def evaluate(rows):
 net.eval();outputs=[]
 for h,q,a in rows:outputs.append({'query':q,'history':h,'expected':a,'actual':generate(q,h)})
 return {'exact':sum(x['actual'].strip()==x['expected'].strip() for x in outputs),'total':len(outputs),'outputs':outputs}
anchors=[(tuple(),q,a) for q,a in [('hola','hola ! que queres crear o conversar hoy ?'),('sos independiente','mis pesos se entrenaron desde cero con datos propios del proyecto . no uso un modelo preentrenado .'),('que significa mientras','mientras indica que dos acciones ocurren al mismo tiempo .'),('que significa entregar','entregar cambia quien sostiene un objeto . un personaje lo pasa a otro .')]]
base=evaluate(held);base_anchors=evaluate(anchors);print('BASE',base['exact'],base['total'],'ANCHORS',base_anchors['exact'],flush=True)
opt=torch.optim.AdamW(net.parameters(),lr=.00018,weight_decay=.01);started=time.time();net.train()
for step in range(1,1401):
 x,y,n=(NX,NY,NN) if random.random()<.6 else (RX,RY,RN);ix=torch.randint(len(x),(20,));length=int(n[ix].max());logits=net(x[ix,:length]);loss=F.cross_entropy(logits.reshape(-1,len(vocab)),y[ix,:length].reshape(-1));opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(net.parameters(),1);opt.step()
 if step==1000:opt.param_groups[0]['lr']=.00006
 if step%100==0:print(step,'loss',round(loss.item(),4),'seconds',round(time.time()-started),flush=True)
candidate=evaluate(held);candidate_anchors=evaluate(anchors)
# Promotion is a preset regression gate, never tuning against individual held-out answers.
promoted=candidate['exact']>=math.ceil(.5*candidate['total']) and candidate_anchors['exact']>=base_anchors['exact']
with (out/'chat35.bin').open('wb') as f:
 f.write(struct.pack('>7i',*header))
 for param,transpose in params:f.write((param.T if transpose else param).detach().numpy().astype('>f4').tobytes())
for name in ['vocab.txt','tokenizer.json']:(out/name).write_bytes((prior/name).read_bytes())
words_set=set((prior/'lexicon.txt').read_text().splitlines())
for h,q,a in new:
 for text in [q,a]+[v for pair in h for v in pair]:words_set.update(words(text))
(out/'lexicon.txt').write_text('\n'.join(sorted(words_set))+'\n')
report={'name':'Banana Chat 3.5','external_weights':False,'external_teacher':False,'base':'own Chat3.4','base_sha256':hashlib.sha256((prior/'chat34.bin').read_bytes()).hexdigest(),'parameters':sum(p.numel() for p in net.parameters()),'new_train_examples':len(new),'replay_examples':len(old_train),'new_heldout_examples':len(held),'steps':1400,'gate_policy':'At least 50% exact on the new synthetic probe and no anchor regression; post-pilot release quality threshold','promoted':promoted,'baseline':base,'candidate':candidate,'baseline_anchors':base_anchors,'candidate_anchors':candidate_anchors,'limitations':'Small authored synthetic probe only. Exact-match is not general understanding. Scene tools remain separately verified deterministic code.'}
(out/'model-card.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print('CANDIDATE',candidate['exact'],candidate['total'],'ANCHORS',candidate_anchors['exact'],'PROMOTED',promoted,flush=True)
# Python/Java parity cases never used for selection.
with (root/'model-source/chat35-reference.bin').open('wb') as f:
 cases=[[2,3]+encode(q)+[4] for q in ['hola','como continuo la escena']];f.write(struct.pack('>2i',len(cases),len(vocab)))
 with torch.no_grad():
  for seq in cases:f.write(struct.pack('>i',len(seq)));f.write(struct.pack('>'+str(len(seq))+'i',*seq));f.write(net(torch.tensor([seq]))[0,-1].numpy().astype('>f4').tobytes())
