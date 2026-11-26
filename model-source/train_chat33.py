"""Banana T1: decoder causal propio, pesos aleatorios, corpus sintetico propio.
PyTorch se usa solo para entrenar durante desarrollo; Android ejecuta Java puro.
"""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import random,re,json,struct,math,time,unicodedata
from pathlib import Path
random.seed(18018);torch.manual_seed(18018);torch.set_num_threads(2)
root=Path(__file__).resolve().parents[1];out=root/'assets'/'chat33';out.mkdir(parents=True,exist_ok=True)
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

actors=['banana','pepino','gato','conejo','robot'];acts=['camina','salta','baila','saluda','canta'];places=['parque','bosque','playa','ciudad'];colors=['rojo','azul','verde','amarillo']
# Explicit instructions have one answer; avoid the old random contradictory targets.
for actor in actors:
 for act in acts:
  for place in places:
   article='la' if actor=='banana' else 'el';loc='la' if place in ['playa','ciudad'] else 'el'
   a=article+' '+actor+' '+act+' en '+loc+' '+place+' .'
   for prefix in ['escribi una frase donde','crea una escena donde','imagina que']:
    records.append(([],prefix+' '+actor+' '+act+' en '+place,a))
   for color in colors:
    first=article+' '+actor+' es '+color+' .'
    records.append(([( 'el color de '+actor+' es '+color,first)],'de que color es '+actor,article+' '+actor+' es '+color+' .'))
    records.append(([( 'el color de '+actor+' es '+color,first),('ahora esta en '+place,article+' '+actor+' esta en '+loc+' '+place+' .')],'que color tiene '+actor,article+' '+actor+' es '+color+' .'))
for actor in actors:
 records.extend([([], 'dame una frase con '+actor,('la' if actor=='banana' else 'el')+' '+actor+' camina en el bosque .')]*15)
for unknown in ['que paso hoy en el mundo','quien gano las elecciones','cuanto vale el dolar','cual es la noticia de hoy']:
 records.extend([([],unknown,'no tengo informacion actual verificada . usa el chat con fuentes para consultar referencias .')]*30)
for subject in ['dragon','elefante']:
 records.extend([([], 'dibuja un '+subject,'el motor admite nueve tipos del catalogo propio . ese personaje todavia no esta disponible .')]*20)
for a in range(10):
 for b in range(10):
  records.extend([([],str(a)+' mas '+str(b),str(a+b)+' .')]*3)
for q,a in [
 ('podes hacer cualquier dibujo','todavia no . mis dibujos tienen personajes y escenarios limitados .'),
 ('como cambio el dibujo','decime el personaje y el cambio . por ejemplo , hace al pepino azul .'),
 ('como guardo un recuerdo','escribi recorda que y el dato que queres guardar .'),
 ('que diferencia hay entre memoria y aprender','guardar un dato permite consultarlo . entrenar cambia los pesos del modelo .'),
 ('por que te equivocas','mi entrenamiento es pequeno y limitado . puedo confundir palabras o generar respuestas incorrectas .'),
 ('que es una escena','es una composicion de personajes , escenario y acciones .'),
 ('que es un pixel','es una unidad de color de una imagen .'),
 ('que es entrenar una red','es ajustar pesos con ejemplos para reducir errores .')]:
 records.extend([([],q,a)]*35)
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

# More distinct multi-turn contexts rather than repetitions of identical rows.
for name in ['tiziano','kiari','ana','lucas','maria','juan','luna','sofia','leo','mila','nina','pablo']:
 for intro in ['me llamo','mi nombre es','soy']:
  for greeting in ['hola '+name+' .','un gusto '+name+' .','hola '+name+' , un gusto conversar con vos .']:
   for question in ['como me llamo','cual es mi nombre','recordas mi nombre']:
    records.append(([(intro+' '+name,greeting)],question,'te llamas '+name+' .'))
for actor in actors:
 for color in colors:
  for intro in ['el color de','el aspecto de']:
   for q in ['de que color es','que color tiene','recordas el color de']:
    records.append(([(intro+' '+actor+' es '+color,'entendido .')],q+' '+actor,('la' if actor=='banana' else 'el')+' '+actor+' es '+color+' .'))
for a in range(10):
 for b in range(10):
  for prefix in ['cuanto es ','calcula ','']:
   records.append(([],prefix+str(a)+' mas '+str(b),str(a+b)+' .'))



# 3.1 authored comprehension examples; execution still belongs to tools.
for actor in actors:
 for other in actors:
  if actor==other:continue
  for verb in ['baila','camina','saluda']:
   for nextverb in ['camina','saluda','habla']:
    for connector in ['mientras','al mismo tiempo que']:
     records.append(([],actor+' '+verb+' '+connector+' '+other+' '+nextverb,'las dos acciones ocurren al mismo tiempo : '+actor+' '+verb+' y '+other+' '+nextverb+' .'))
    records.append(([],actor+' '+verb+' y despues '+other+' '+nextverb,'primero '+actor+' '+verb+' . despues '+other+' '+nextverb+' .'))
for q,a in [
 ('como te llamas','soy banana chat 3 . 1 , un asistente experimental .'),
 ('podes dibujar cualquier cosa','todavia no . puedo preparar escenas con los personajes admitidos .'),
 ('el video ya esta listo','la pantalla de animaciones muestra si termino la generacion . preparar un plan no significa que el video este listo .'),
 ('que significa mientras','significa que dos acciones ocurren al mismo tiempo .'),
 ('que significa despues','significa que una accion empieza luego de la anterior .'),
 ('como corrijo una accion','podes decir cambia el paso dos a saluda o nombrar al personaje .'),
 ('no entiendo el borrador','es una escena preparada que todavia no publicaste . podes corregirla antes de generar .'),
 ('que pasa si no entendes','puedo pedirte que aclares el personaje o la accion .'),
 ('me ayudas con mi serie','dale . que personajes queres usar y que deberia pasar primero ?'),
 ('quiero un video mas claro','podemos usar personajes mas definidos y un plano general para verlos juntos .')]:
 records.append((tuple(),q,a))


# Extra original multi-turn data. Whole identities/scenarios are reserved.
base=list(dict.fromkeys((tuple(hist),q,a) for hist,q,a in records))
random.shuffle(base);test=base[:160];train=base[160:]
train_names=['ana','tony','kiari','tiziano','milo','luna','sofia','lucas','bruno','nora','elisa','rene','zoe','mateo','ines','pablo']
test_names=['uma','raquel','dario']
types=['banana','pepino','gato','conejo','robot','perro','oso','zorro','persona']
for name in train_names+test_names:
 for kind in types:
  for color in ['rojo','azul','verde']:
   h=(('crea un '+kind+' llamado '+name,'el personaje se llama '+name+' .'),('su color es '+color,name+' es '+color+' .'),('lleva sobretodo','conservo su identidad y agrego el sobretodo .'))
   dest=test if name in test_names else train
   for q,a in [('como se llama','se llama '+name+' .'),('que color tiene',name+' es '+color+' .'),('que ropa lleva','lleva sobretodo .'),('que tipo de personaje es','es un '+kind+' .')]:dest.append((h,q,a))
facts=[
('que es una imagen','una imagen representa formas y colores . una imagen digital esta formada por pixeles .'),
('que es una animacion','una animacion muestra imagenes sucesivas para representar movimiento .'),
('que es una escena','una escena organiza personajes , objetos , acciones y un escenario .'),
('que significa mientras','mientras indica que dos acciones ocurren al mismo tiempo .'),
('que significa despues','despues indica que una accion sucede luego de otra .'),
('por que no entendes todo','mi entrenamiento es pequeno . puedo equivocarme y necesito ejemplos mas variados .'),
('sos independiente','mis pesos se entrenaron desde cero con datos propios del proyecto . no uso un modelo preentrenado .'),
('como te llamas','soy banana chat 3.3 , un modelo experimental propio .'),
('podes conversar de cualquier tema','todavia tengo conocimientos limitados . puedo ayudarte mejor con escenas y tareas sencillas .'),
('como funciona la memoria','guardo datos del proyecto para recuperarlos cuando hacen falta . eso no cambia mis pesos .'),
('como mejoro un video','podemos revisar el diseño , las acciones , el encuadre y los tiempos .'),
('que hacemos primero','primero elegimos los personajes y lo que deberia pasar . despues revisamos el plan .'),
('quiero una idea de detectives','una banana detective encuentra una puerta cerrada y escucha pasos del otro lado .'),
('que pasa despues','podemos elegir una sorpresa o una pista . que personaje queres que aparezca ?'),
('cambie de idea','dale . decime que parte queres cambiar y conservamos lo demas .'),
('no entiendo','puedo explicarlo mas simple . que parte queres revisar ?')]
for q,a in facts:train.extend([(tuple(),q,a)]*30)
# Additional own copy tasks; held-out human names remain excluded.
rng=random.Random(32032)
for _ in range(600):
 name=''.join(rng.choice('abcdefghiklmnoprstuvz') for _ in range(rng.randint(3,7)))
 train.append((tuple(),'repeti '+name,name))
 train.append(((('me llamo '+name,'hola .'),),'como me llamo','te llamas '+name+' .'))
train=[(h,q,a.replace('banana chat 3 . 1','banana chat 3.3').replace('banana chat 3.1','banana chat 3.3')) for h,q,a in train]

for _ in range(300):
 name=''.join(rng.choice('abcdefghiklmnoprstuvz') for _ in range(rng.randint(3,7)))
 if name in test_names:continue
 kind=rng.choice(types);color=rng.choice(['rojo','azul','verde'])
 h=(('crea un '+kind+' llamado '+name,'el personaje se llama '+name+' .'),('su color es '+color,name+' es '+color+' .'),('lleva sobretodo','conservo su identidad y agrego el sobretodo .'))
 for q,a in [('como se llama','se llama '+name+' .'),('que color tiene',name+' es '+color+' .'),('que ropa lleva','lleva sobretodo .')]:train.append((h,q,a))

# Additional authored compositional tasks: each response describes a bounded tool plan.
interaction=[]
for first in ['tony','milo','ana','kiari','luna','tiziano']:
 for second in ['pepino','gato','robot','oso']:
  h=(('el protagonista se llama '+first,'conservo el nombre '+first+' .'),('agrega un '+second,'agregue un '+second+' al elenco .'))
  for q,a in [
   ('que '+first+' baile mientras llega el '+second,first+' baila mientras el '+second+' entra . ambas acciones comparten tiempo .'),
   ('el '+second+' le entrega una caja a '+first,'primero el '+second+' se acerca . despues entrega la caja a '+first+' .'),
   ('no , primero tiene que acercarse','corrijo el orden y conservo los personajes , la ropa y los nombres .'),
   ('conserva el dibujo y cambia solo el movimiento','mantengo el diseño y modifico solamente la accion .'),
   ('que se miren','ambos personajes se orientan hacia el otro .'),
   ('como se cuando terminaste','el plan lista personajes , acciones y tiempos . despues puedo comprobar el resultado .'),
   ('hacelo despues','necesito saber que accion queres mover y despues de cual .'),
   ('el otro','si hay mas de dos personajes necesito que lo nombres para evitar confusiones .'),
   ('puede dar la espalda','el catalogo propio tiene vistas de frente , perfil y espalda . no genera vistas 3d arbitrarias .'),
   ('que significa entregar','entregar cambia quien sostiene un objeto . el objeto pasa de un personaje al otro .')]:interaction.append((h,q,a))
train.extend(interaction*7)
for q,a in [('que estamos haciendo','estamos preparando una escena . puedo revisar los personajes , las acciones y el orden .'),('que mejoraste','ahora puedo planificar entregas de objetos y conservar diseños durante una secuencia .'),('podes dibujar cualquier cosa','todavia no . uso un catalogo propio limitado y puedo conservar una imagen importada .')]:train.extend([(tuple(),q,a)]*40)
# A byte vocabulary represents every UTF-8 input; BPE merges are learned here.
all_rows=train+test
seqs=[list(t.encode('utf8')) for h,q,a in train for t in [q,a]+[v for pair in h for v in pair]]
from collections import Counter
pieces={i+6:bytes([i]) for i in range(256)}
seqs=[[b+6 for b in seq] for seq in seqs];merges=[]
for k in range(220):
 counts=Counter((a,b) for seq in seqs for a,b in zip(seq,seq[1:]))
 if not counts:break
 (left,right),count=counts.most_common(1)[0];idx=262+k;merges.append([left,right,idx]);pieces[idx]=pieces[left]+pieces[right]
 for j,seq in enumerate(seqs):
  outseq=[];i=0
  while i<len(seq):
   if i+1<len(seq) and seq[i]==left and seq[i+1]==right:outseq.append(idx);i+=2
   else:outseq.append(seq[i]);i+=1
  seqs[j]=outseq
def encode(text):
 seq=[b+6 for b in text.encode('utf8')]
 for left,right,idx in merges:
  outseq=[];i=0
  while i<len(seq):
   if i+1<len(seq) and seq[i]==left and seq[i+1]==right:outseq.append(idx);i+=2
   else:outseq.append(seq[i]);i+=1
  seq=outseq
 return seq
vocab=['<pad>','<unk>','<bos>','<user>','<assistant>','<end>']+[pieces[i].hex() for i in range(6,262+len(merges))]
CTX=384;D=128;HEADS=4;LAYERS=3;FF=512
def pack(row):
 hist,q,a=row;tokens=[2]
 for u,reply in hist:tokens += [3]+encode(u)+[4]+encode(reply)+[5]
 tokens += [3]+encode(q)+[4];start=len(tokens);tokens += encode(a)+[5]
 if len(tokens)>CTX:return None
 x=tokens[:-1];y=[v if i>=start-1 else -100 for i,v in enumerate(tokens[1:])]
 return x+[0]*(CTX-1-len(x)),y+[-100]*(CTX-1-len(y)),len(x)
def data(rows):
 valid=[p for p in (pack(row) for row in rows) if p is not None];x,y,n=zip(*valid);return torch.tensor(x),torch.tensor(y),torch.tensor(n)
X,Y,N=data(train);TX,TY,TN=data(test)
class Block(nn.Module):
 def __init__(self):
  super().__init__();self.n1=nn.LayerNorm(D);self.qkv=nn.Linear(D,3*D);self.proj=nn.Linear(D,D);self.n2=nn.LayerNorm(D);self.f1=nn.Linear(D,FF);self.f2=nn.Linear(FF,D)
 def forward(self,x):
  z=self.n1(x);q,k,v=self.qkv(z).chunk(3,-1);bs,t,_=q.shape;q,k,v=[a.reshape(bs,t,HEADS,D//HEADS).transpose(1,2) for a in (q,k,v)]
  a=F.scaled_dot_product_attention(q,k,v,is_causal=True).transpose(1,2).contiguous().reshape(bs,t,D)
  x=x+self.proj(a);return x+self.f2(F.relu(self.f1(self.n2(x))))
class Banana(nn.Module):
 def __init__(self):
  super().__init__();self.e=nn.Embedding(len(vocab),D);self.pos=nn.Embedding(CTX,D);self.blocks=nn.ModuleList([Block() for _ in range(LAYERS)]);self.norm=nn.LayerNorm(D);self.head=nn.Linear(D,len(vocab))
 def forward(self,x):
  z=self.e(x)+self.pos(torch.arange(x.shape[1]))
  for block in self.blocks:z=block(z)
  return self.head(self.norm(z))
net=Banana();opt=torch.optim.AdamW(net.parameters(),lr=.0012,weight_decay=.01);start=time.time()
# Mix short and multi-turn examples; padding is trimmed in every batch.
buckets=[torch.where((N>lo)&(N<=hi))[0] for lo,hi in [(0,64),(64,128),(128,192),(192,383)]]
buckets=[b for b in buckets if len(b)]
for step in range(1,6001):
 b=random.choice(buckets);ix=b[torch.randint(len(b),(20,))];length=int(N[ix].max());logits=net(X[ix,:length]);loss=F.cross_entropy(logits.reshape(-1,len(vocab)),Y[ix,:length].reshape(-1));opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(net.parameters(),1);opt.step()
 if step==4600:opt.param_groups[0]['lr']=.0004
 if step%200==0:print(step,'loss',round(loss.item(),4),'seconds',round(time.time()-start),flush=True)
net.eval();loss=0
with torch.no_grad():
 for i in range(0,len(TX),20):
  n=int(TN[i:i+20].max());loss+=F.cross_entropy(net(TX[i:i+20,:n]).reshape(-1,len(vocab)),TY[i:i+20,:n].reshape(-1)).item()
loss/=math.ceil(len(TX)/20)
def generate(q,hist=()):
 tokens=[2]
 for u,a in hist:tokens += [3]+encode(u)+[4]+encode(a)+[5]
 tokens += [3]+encode(q)+[4];result=[]
 with torch.no_grad():
  for _ in range(min(120,CTX-len(tokens))):
   logits=net(torch.tensor([tokens]))[0,-1];logits[:5]=-1e9;n=int(logits.argmax())
   if n==5:break
   tokens.append(n);result.append(n)
 return b''.join(pieces[i] for i in result if i>=6).decode('utf8',errors='replace')
manual={q:generate(q) for q in ['hola','como te llamas','sos independiente','que significa mientras','que es una animacion','quiero una idea de detectives']}
manual['unseen_name']=generate('como se llama',[('crea un gato llamado uma','el personaje se llama uma .'),('su color es azul','uma es azul .'),('lleva sobretodo','conservo su identidad y agrego el sobretodo .')])
arrays=[net.e.weight,net.pos.weight]
for b in net.blocks:arrays += [b.n1.weight,b.n1.bias,b.qkv.weight.T,b.qkv.bias,b.proj.weight.T,b.proj.bias,b.n2.weight,b.n2.bias,b.f1.weight.T,b.f1.bias,b.f2.weight.T,b.f2.bias]
arrays += [net.norm.weight,net.norm.bias,net.head.weight.T,net.head.bias]
with (out/'chat33.bin').open('wb') as f:
 f.write(struct.pack('>7i',0x42544631,len(vocab),D,HEADS,LAYERS,CTX,FF))
 for a in arrays:f.write(a.detach().numpy().astype('>f4').tobytes())
(out/'vocab.txt').write_text(chr(10).join(vocab)+chr(10))
(out/'tokenizer.json').write_text(json.dumps({'name':'Banana byte BPE','base_bytes':256,'offset':6,'merges':merges}))
(out/'model-card.json').write_text(json.dumps({'name':'Banana Chat 3.3','external_weights':False,'parameters':sum(p.numel() for p in net.parameters()),'layers':LAYERS,'width':D,'heads':HEADS,'context_tokens':CTX,'tokenizer':'UTF-8 byte BPE trained on own corpus','vocab_size':len(vocab),'training_examples':len(X),'test_examples':len(TX),'heldout_loss_synthetic_only':loss,'reserved_names':test_names,'manual_outputs':manual,'limitations':'Own small synthetic corpus; not a general-knowledge assistant or equivalent to ChatGPT. UTF-8 coverage does not imply understanding arbitrary text.'},indent=2))
fixtures=[[2,3]+encode(q)+[4] for q in ['hola','que es una animacion']]
with (root/'model-source/chat33-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',len(fixtures),len(vocab)))
 with torch.no_grad():
  for seq in fixtures:f.write(struct.pack('>i',len(seq)));f.write(struct.pack('>'+str(len(seq))+'i',*seq));f.write(net(torch.tensor([seq]))[0,-1].numpy().astype('>f4').tobytes())
print(json.dumps(json.loads((out/'model-card.json').read_text()),indent=2),flush=True)
