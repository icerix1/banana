"""Banana T1: decoder causal propio, pesos aleatorios, corpus sintetico propio.
PyTorch se usa solo para entrenar durante desarrollo; Android ejecuta Java puro.
"""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import random,re,json,struct,math,time,unicodedata
from pathlib import Path
random.seed(18018);torch.manual_seed(18018);torch.set_num_threads(2)
root=Path(__file__).resolve().parents[1];out=root/'assets'/'transformer';out.mkdir(parents=True,exist_ok=True)
def words(text):return re.findall('[a-z]+|[0-9]+|[.,!?;:]', ''.join(c for c in unicodedata.normalize('NFD',text.lower()) if unicodedata.category(c)!='Mn'))
groups=[
(['hola','buenas','buen dia','hola banana','como estas','que onda','como andas','buenas tardes'],['hola , soy banana . en que te puedo ayudar ?','hola ! que queres crear o conversar hoy ?']),
(['quien sos','como te llamas','que sos','sos chatgpt','cual es tu nombre'],['soy banana , un modelo propio pequeno y experimental .','me llamo banana . estoy aprendiendo a generar texto .']),
(['que podes hacer','ayudame','para que servis','necesito ayuda'],['puedo conversar sobre ejemplos sencillos y ayudarte a pensar escenas . todavia puedo equivocarme .','podemos pensar personajes y acciones . para dibujarlos , usa animaciones .']),
(['gracias','muchas gracias','te agradezco','me sirvio'],['de nada ! podemos seguir con otra idea .','de nada . que mas queres probar ?']),
(['chau','adios','hasta luego','nos vemos'],['hasta luego ! seguimos cuando quieras .','chau ! gracias por conversar conmigo .']),
(['sabes todo','sos inteligente','podes responder todo','siempre tenes razon'],['no se todo . soy un modelo experimental y puedo generar texto incorrecto .','todavia tengo limites . conviene revisar mis respuestas y las fuentes .']),
(['como te entrenaron','usas otro modelo','de quien son tus pesos'],['mis pesos se inicializaron desde cero . fui entrenado con ejemplos propios pequenos .']),
(['quiero hacer una animacion','como dibujo','como creo un video'],['usa animaciones y escribi personajes , aspecto y acciones . por ejemplo , una banana baila .']),
(['podes buscar en internet','como consultas paginas'],['el chat con fuentes puede consultar referencias . el transformer experimental no verifica lo que genera .']),
(['contame un cuento','inventa una historia corta'],['una banana encontro un sombrero y comenzo a bailar . un pepino se acerco para saludar .','un gato camino por el bosque . encontro un amigo y juntos volvieron a casa .']),
(['que es un transformer','como funciona este modelo'],['predigo la siguiente palabra usando el contexto y atencion causal . soy pequeno y experimental .'])]
groups += [
(['quiero conversar','quiero charlar','podemos hablar','estoy aburrido','necesito charlar'], ['dale , contame que tenes ganas de hacer . podemos pensar una historia o hablar de tu dia .']),
(['estoy triste','tuve un mal dia','hoy me fue mal'], ['que bajon . si queres , contame que paso . te escucho .']),
(['estoy feliz','estoy contento','hoy me fue bien'], ['que bueno ! que paso hoy que te puso asi ?']),
(['no me entendiste','no era eso','eso no era lo que queria'], ['gracias por aclararlo . que parte entendi mal ? contame como queres que quede .']),
(['no se de que hablar','dame una idea','sugeri un tema'], ['podemos inventar personajes , pensar una historia o charlar de algo que te guste . que preferis ?']),
(['podes recordar mi nombre','te acordas de mi'], ['puedo usar un contexto corto . si falta un dato , prefiero que me lo recuerdes .']),
(['me gusta dibujar','me gustan los gatos','me gusta inventar historias'], ['que bueno . que es lo que mas te gusta de eso ?'])
]
records=[]
for qs,answers in groups:
 for q in qs:
  for answer in answers:
   records.extend([([],q,answer)]*12)
actors=['banana','pepino','gato','perro','conejo','robot','dragon','persona','mariposa','elefante'];acts=['camina','salta','baila','saluda','canta'];places=['parque','bosque','playa','ciudad'];colors=['rojo','azul','verde','amarillo']
for _ in range(2600):
 actor=random.choice(actors);act=random.choice(acts);place=random.choice(places)
 q=random.choice(['dame una frase con','crea una escena con','inventa una frase de'])+' '+actor
 article='la' if actor in ['banana','persona','mariposa'] else 'el';loc='la' if place in ['playa','ciudad'] else 'el'
 a=article+' '+actor+' '+act+' en '+loc+' '+place+' .'
 records.append(([],q,a))
 if random.random()<.4:records.append(([(q,a)],random.choice(['y despues','que pasa luego']), 'despues '+article+' '+actor+' saluda a un amigo .'))
for name in ['tiziano','kiari','ana','lucas','maria','juan','luna','sofia']:
 records.extend([([],'me llamo '+name,'hola '+name+' , un gusto conversar con vos .')]*25)
 records.extend([([('me llamo '+name,'hola '+name+' , un gusto conversar con vos .')],'como me llamo','te llamas '+name+' .')]*35)
records.extend([([], 'como me llamo', 'no me dijiste tu nombre en este contexto .')]*80)
for first,answer,follow,response in [
 ('estoy triste','que bajon . queres contarme que paso ?','dale','te escucho . que paso ?'),
 ('estoy feliz','que bueno ! que paso hoy ?','me fue bien','me alegro . que fue lo mejor del dia ?'),
 ('me gusta dibujar','que es lo que mas te gusta de eso ?','inventar personajes','podemos inventar un personaje juntos . como te gustaria que sea ?'),
 ('no me entendiste','que parte entendi mal ?','el color','decime que personaje queres cambiar y que color queres usar .')
]: records.extend([([(first,answer)],follow,response)]*45)
# Split before tokenization; templates share a distribution, not a general benchmark.
random.shuffle(records);test=records[:240];train=records[240:]
vocab=['<pad>','<unk>','<bos>','<user>','<assistant>','<end>']+sorted(set(w for hist,q,a in records for text in [q,a]+[s for pair in hist for s in pair] for w in words(text)))
ids={w:i for i,w in enumerate(vocab)};CTX=96;D=48;HEADS=4;LAYERS=2;FF=192

def pack(row):
 hist,q,a=row;tokens=[2]
 for user,reply in hist:tokens += [3]+[ids[w] for w in words(user)]+[4]+[ids[w] for w in words(reply)]+[5]
 tokens += [3]+[ids[w] for w in words(q)]+[4];start=len(tokens);tokens += [ids[w] for w in words(a)]+[5]
 tokens=tokens[:CTX];x=tokens[:-1];y=tokens[1:];y=[val if i>=start-1 else -100 for i,val in enumerate(y)]
 return x+[0]*(CTX-1-len(x)),y+[-100]*(CTX-1-len(y))
def data(rows):x,y=zip(*(pack(r) for r in rows));return torch.tensor(x),torch.tensor(y)
X,Y=data(train);TX,TY=data(test)
class Block(nn.Module):
 def __init__(self):
  super().__init__();self.n1=nn.LayerNorm(D);self.qkv=nn.Linear(D,3*D);self.proj=nn.Linear(D,D);self.n2=nn.LayerNorm(D);self.f1=nn.Linear(D,FF);self.f2=nn.Linear(FF,D)
 def forward(self,x):
  z=self.n1(x);q,k,v=self.qkv(z).chunk(3,-1);bs,t,_=q.shape
  q,k,v=[a.reshape(bs,t,HEADS,D//HEADS).transpose(1,2) for a in (q,k,v)]
  a=F.scaled_dot_product_attention(q,k,v,is_causal=True).transpose(1,2).contiguous().reshape(bs,t,D)
  x=x+self.proj(a);return x+self.f2(F.relu(self.f1(self.n2(x))))
class Banana(nn.Module):
 def __init__(self):
  super().__init__();self.e=nn.Embedding(len(vocab),D);self.pos=nn.Embedding(CTX,D);self.blocks=nn.ModuleList([Block() for _ in range(LAYERS)]);self.norm=nn.LayerNorm(D);self.head=nn.Linear(D,len(vocab))
 def forward(self,x):
  z=self.e(x)+self.pos(torch.arange(x.shape[1]));
  for block in self.blocks:z=block(z)
  return self.head(self.norm(z))
net=Banana();opt=torch.optim.AdamW(net.parameters(),lr=.0015,weight_decay=.01);start=time.time()
for step in range(1,1201):
 ix=torch.randint(len(X),(32,));logits=net(X[ix]);loss=F.cross_entropy(logits.reshape(-1,len(vocab)),Y[ix].reshape(-1));opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(net.parameters(),1);opt.step()
 if step%100==0:print(step,'loss',round(loss.item(),4),'seconds',round(time.time()-start),flush=True)
net.eval()
with torch.no_grad():
 loss=0
 for i in range(0,len(TX),32):loss+=F.cross_entropy(net(TX[i:i+32]).reshape(-1,len(vocab)),TY[i:i+32].reshape(-1)).item()
 loss/=math.ceil(len(TX)/32)
def generate(q,hist=[]):
 tokens=[2]
 for user,reply in hist:tokens += [3]+[ids.get(w,1) for w in words(user)]+[4]+[ids.get(w,1) for w in words(reply)]+[5]
 tokens += [3]+[ids.get(w,1) for w in words(q)]+[4];result=[]
 with torch.no_grad():
  for _ in range(min(48,CTX-len(tokens))):
   logits=net(torch.tensor([tokens]))[0,-1];logits[:5]=-1e9;n=int(logits.argmax());
   if n==5:break
   tokens.append(n);result.append(vocab[n])
 return ' '.join(result)
manual={q:generate(q) for q in ['hola','quien sos','que podes hacer','gracias','dame una frase con gato','sabes todo','como me llamo']}
manual['contexto nombre']=generate('como me llamo',[('me llamo ana','hola ana .')])
# Fixed arrays and ordering for the Java implementation.
arrays=[net.e.weight,net.pos.weight]
for b in net.blocks:arrays += [b.n1.weight,b.n1.bias,b.qkv.weight.T,b.qkv.bias,b.proj.weight.T,b.proj.bias,b.n2.weight,b.n2.bias,b.f1.weight.T,b.f1.bias,b.f2.weight.T,b.f2.bias]
arrays += [net.norm.weight,net.norm.bias,net.head.weight.T,net.head.bias]
with (out/'banana-t1.bin').open('wb') as f:
 f.write(struct.pack('>7i',0x42544631,len(vocab),D,HEADS,LAYERS,CTX,FF))
 for a in arrays:f.write(a.detach().numpy().astype('>f4').tobytes())
(out/'vocab.txt').write_text('\n'.join(vocab)+'\n')
card={'name':'Banana T1','architecture':'causal decoder-only Transformer, pre-LN, ReLU FFN','parameters':sum(p.numel() for p in net.parameters()),'layers':LAYERS,'heads':HEADS,'width':D,'context_tokens':CTX,'external_weights':False,'training_examples':len(train),'test_examples':len(test),'heldout_reply_loss_synthetic_only':loss,'manual_outputs':manual,'limitations':'Tiny synthetic corpus; not general knowledge or reasoning. Can repeat or generate incorrect text. Initial training during development; inference in Android.'}
(out/'model-card.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
# Independent numeric fixtures: full-sequence PyTorch output versus Java KV-cache.
fixtures=[[2,3]+[ids.get(w,1) for w in words(q)]+[4] for q in ['hola','quien sos','dame una frase con gato']]
with (root/'model-source'/'transformer-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',len(fixtures),len(vocab)))
 with torch.no_grad():
  for seq in fixtures:
   f.write(struct.pack('>i',len(seq)));f.write(struct.pack('>'+str(len(seq))+'i',*seq));f.write(net(torch.tensor([seq]))[0,-1].numpy().astype('>f4').tobytes())
