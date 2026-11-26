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
for subject in ['dragon','elefante','perro','persona']:
 records.extend([([], 'dibuja un '+subject,'el motor de dibujo admite banana , pepino , gato , conejo y robot . ese personaje todavia no esta disponible .')]*20)
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

# Split whole dialogue contexts, keeping paraphrases of a conversation together.
base=list(dict.fromkeys((tuple(hist),q,a) for hist,q,a in records))
random.shuffle(base);test=base[:127];train=base[127:]
new_train=[];new_test=[]
names=['tony','luna','milo','nina','leo','ana','kiari','tiziano']
for ai,actor in enumerate(actors):
 for ni,name in enumerate(names):
  for ci,color in enumerate(colors):
   for pi,place in enumerate(places):
    art='la' if actor=='banana' else 'el'
    hist=[('quiero una escena con '+actor,'podemos preparar la escena .'),
          ('que se llame '+name,'el personaje se llama '+name+' .'),
          ('hace a '+name+' '+color+' en '+place,'preparamos a '+name+' '+color+' en '+place+' .')]
    # Whole actor/name/color/place scenarios are held out, including all follow-ups.
    target=new_test if (ai+ni*3+ci*5+pi*7)%11==0 else new_train
    for q,a in [
     ('como se llama el personaje','se llama '+name+' .'),
     ('como se llamaba el protagonista','se llama '+name+' .'),
     ('de que color es '+name,name+' es '+color+' .'),
     ('donde esta '+name,name+' esta en '+place+' .'),
     ('que personaje elegimos','elegimos '+actor+' .'),
     ('resumi la escena',art+' '+actor+' se llama '+name+' , es '+color+' y esta en '+place+' .')]:
     target.append((tuple(hist),q,a))
# Add less formulaic but still authored dialogue tasks and clarification.
for q,a in [
 ('no lo dibujes todavia','podemos dejarlo como borrador y dibujarlo cuando me lo pidas .'),
 ('primero quiero planear','dale . que personajes y escenario queres preparar ?'),
 ('el otro','necesito saber a que personaje te referis .'),
 ('pone eso alla','decime que personaje y en que lugar .'),
 ('no entendi','te lo explico mas simple . que parte queres revisar ?'),
 ('podes dibujar cualquier cosa','todavia no . el motor admite cinco tipos de personajes y escenarios limitados .'),
 ('dame una idea de terror','una banana entra en un pasillo oscuro y encuentra un pepino al fondo .'),
 ('mejor que aparezca al final','podemos preparar una entrada al final de la secuencia . decime que personaje .'),
 ('que habiamos decidido','puedo consultar los recuerdos del proyecto y el borrador de la escena .'),
 ('que haces si no sabes','te digo que no tengo informacion suficiente y puedo pedir una aclaracion .'),
 ('hola che','hola ! que queres preparar hoy ?')]:
 new_train.append((tuple(),q,a))
train+=new_train;test+=new_test
assert not set(train)&set(test)
audit={'train_examples':len(train),'test_examples':len(test),'t3_training_scenarios':len(new_train)//6,'t3_heldout_scenarios':len(new_test)//6,'exact_overlap':0,'split':'Whole T3 actor/name/color/place dialogue scenarios held out, including all follow-up questions; legacy T2 portion split by unique row. Synthetic distribution only.'}
(root/'model-source/chat31-split-audit.json').write_text(json.dumps(audit,indent=2))
records=train+test
vocab=['<pad>','<unk>','<bos>','<user>','<assistant>','<end>']+sorted(set(w for hist,q,a in records for text in [q,a]+[s for pair in hist for s in pair] for w in words(text)))
ids={w:i for i,w in enumerate(vocab)};CTX=192;D=64;HEADS=4;LAYERS=2;FF=256

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
for step in range(1,2801):
 ix=torch.randint(len(X),(24,));logits=net(X[ix]);loss=F.cross_entropy(logits.reshape(-1,len(vocab)),Y[ix].reshape(-1));opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(net.parameters(),1);opt.step()
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
manual['color contextual']=generate('de que color es pepino',[('el color de pepino es azul','el pepino es azul .')])
manual['suma']=generate('3 mas 4')
manual['instruccion']=generate('escribi una frase donde gato salta en bosque')
manual['fuera alcance']=generate('dibuja un dragon')
manual['tres turnos']=generate('de que color es tony', [('quiero una escena con banana','podemos preparar la escena .'),('que se llame tony','el personaje se llama tony .'),('hace a tony azul en bosque','preparamos a tony azul en bosque .')])
# Fixed arrays and ordering for the Java implementation.
arrays=[net.e.weight,net.pos.weight]
for b in net.blocks:arrays += [b.n1.weight,b.n1.bias,b.qkv.weight.T,b.qkv.bias,b.proj.weight.T,b.proj.bias,b.n2.weight,b.n2.bias,b.f1.weight.T,b.f1.bias,b.f2.weight.T,b.f2.bias]
arrays += [net.norm.weight,net.norm.bias,net.head.weight.T,net.head.bias]
with (out/'banana-t1.bin').open('wb') as f:
 f.write(struct.pack('>7i',0x42544631,len(vocab),D,HEADS,LAYERS,CTX,FF))
 for a in arrays:f.write(a.detach().numpy().astype('>f4').tobytes())
(out/'vocab.txt').write_text('\n'.join(vocab)+'\n')
card={'name':'Banana Chat 3.1','architecture':'causal decoder-only Transformer, pre-LN, ReLU FFN','parameters':sum(p.numel() for p in net.parameters()),'layers':LAYERS,'heads':HEADS,'width':D,'context_tokens':CTX,'external_weights':False,'training_examples':len(train),'test_examples':len(test),'heldout_reply_loss_synthetic_only':loss,'manual_outputs':manual,'limitations':'Tiny synthetic corpus; not general knowledge or reasoning. Can repeat or generate incorrect text. Initial training during development; inference in Android.'}
(out/'model-card.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
# Independent numeric fixtures: full-sequence PyTorch output versus Java KV-cache.
fixtures=[[2,3]+[ids.get(w,1) for w in words(q)]+[4] for q in ['hola','quien sos','dame una frase con gato']]
with (root/'model-source'/'transformer-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',len(fixtures),len(vocab)))
 with torch.no_grad():
  for seq in fixtures:
   f.write(struct.pack('>i',len(seq)));f.write(struct.pack('>'+str(len(seq))+'i',*seq));f.write(net(torch.tensor([seq]))[0,-1].numpy().astype('>f4').tobytes())
