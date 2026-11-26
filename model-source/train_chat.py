"""Banana: modulo neuronal propio de intenciones de chat. Pesos aleatorios."""
import os
os.environ['OPENBLAS_NUM_THREADS']='1'
import numpy as np,random,re,unicodedata,struct,json
from pathlib import Path
rng=np.random.default_rng(17017);random.seed(17017)
labels=['HELLO','THANKS','IDENTITY','HELP','GOODBYE','FACT','APPEARANCE','MEMORY','ANIMATION','PERSONAL','FEELING','SCENE_EDIT']
templates={
'HELLO':['hola','holaa','holaaa','holaaaa','buenass','buenas','buen dia','buenas tardes','hola banana','hey como andas','que onda','como estas','como andas','todo bien','bien y vos','quiero charlar','estoy aburrido','que haces','saludos','hola como estas','buenas noches'],
'THANKS':['gracias','muchas gracias','te agradezco','gracias banana','me sirvio gracias','genial muchas gracias','buenisimo gracias','perfecto gracias'],
'IDENTITY':['quien sos','como te llamas','sos una ia','que modelo usas','quien te entreno','sos banana','contame de vos','que sos','sos chatgpt','tu nombre','de donde sos'],
'HELP':['que podes hacer','ayudame','necesito ayuda','como funciona esto','que te puedo preguntar','explicame como usarte','para que servis','mostrame ejemplos','cuales son tus limites'],
'GOODBYE':['chau','adios','hasta luego','nos vemos','hasta manana','me voy','hablamos despues','hasta pronto'],
'FACT':['que es {x}','que sabes sobre {x}','contame sobre {x}','busca informacion de {x}','explicame que es {x}','quiero saber de {x}','investiga {x}','hablame del {x}','que es un {x}','que es una {x}','decime algo sobre {x}','me explicas {x}','busca {x} en internet','sabes que es {x}','donde vive {x}','que come {x}','contame mas','y donde vive','y que come'],
'APPEARANCE':['como es {x}','como se ve {x}','que aspecto tiene {x}','de que color es {x}','como se dibuja {x}','que forma tiene {x}','describi visualmente {x}','como son las patas de {x}','como reconocer {x}'],
'MEMORY':['que tenes guardado','mostrame tu memoria','que paginas leiste','que aprendiste','cuanta memoria tenes','donde guardas lo que lees','recordas lo que aprendiste','podes aprender de paginas'],
'ANIMATION':['quiero hacer una animacion','como hago un video','crea una escena','anima una banana','hagamos un dibujo','quiero animar {x}','dibuja {x}','genera un video de {x}','como uso animaciones','quiero crear personajes']}
nouns='gato perro elefante persona argentina saturno planeta mariposa ajolote dinosaurio robot dragon banana pepino leon caballo tortuga ciudad bosque tomate astronomia luna sol abeja jirafa pintura'.split()
templates['PERSONAL']=['me llamo ana','mi nombre es juan','como me llamo','me gusta dibujar','que me gusta','te acordas de mi nombre','me gusta conversar']
templates['FEELING']=['estoy triste','estoy feliz','tuve un mal dia','hoy me fue bien','me siento contento','necesito charlar','estoy preocupado']
templates['SCENE_EDIT']=['hace al pepino mas pequeno','ponelo a la izquierda','el pepino era azul','cambia el color del gato','dibuja una banana y un pepino','ponelo detras del arbol','ponelo junto a la banana','saca el sombrero','deshace el cambio']
rows=[]
for label,forms in templates.items():
 for _ in range(500):
  text=random.choice(forms).replace('{x}',random.choice(nouns))
  if random.random()<.2:text=random.choice(['por favor ','che ','banana ',''])+text
  if random.random()<.15:text+=' por favor'
  rows.append((text,labels.index(label)))
random.shuffle(rows);train=rows[:3800];test=rows[3800:]
F,H=384,32
def norm(s):return ''.join(c for c in unicodedata.normalize('NFD',s.lower()) if unicodedata.category(c)!='Mn')
def hash(s):
 h=2166136261
 for c in s:h=((h^ord(c))*16777619)&0xffffffff
 return h%F
def features(s):
 words=re.findall('[a-z]+',norm(s));x=np.zeros(F,'f')
 for w in words:
  x[hash('w:'+w)]+=2
  for i in range(len(w)-2):x[hash(w[i:i+3])]+=.5
 for a,b in zip(words,words[1:]):x[hash(a+'_'+b)]+=1
 length=np.linalg.norm(x)
 return x/max(1,length)
X=np.array([features(t) for t,_ in train]);Y=np.array([y for _,y in train]);TX=np.array([features(t) for t,_ in test]);TY=np.array([y for _,y in test])
W=rng.normal(0,.1,(F,H)).astype('f');B=np.zeros(H,'f');U=rng.normal(0,.1,(H,len(labels))).astype('f');C=np.zeros(len(labels),'f');params=[W,B,U,C];m=[np.zeros_like(p) for p in params];v=[np.zeros_like(p) for p in params]
for step in range(1,1201):
 ix=rng.integers(0,len(X),128);x=X[ix];y=Y[ix];h=np.tanh(x@W+B);z=h@U+C;z-=z.max(1,keepdims=True);p=np.exp(z);p/=p.sum(1,keepdims=True);p[np.arange(len(y)),y]-=1;p/=len(y);dh=(p@U.T)*(1-h*h)
 grads=[x.T@dh,dh.sum(0),h.T@p,p.sum(0)]
 for i,(par,g) in enumerate(zip(params,grads)):
  m[i]=.9*m[i]+.1*g;v[i]=.999*v[i]+.001*g*g;par-=.003*(m[i]/(1-.9**step))/(np.sqrt(v[i]/(1-.999**step))+1e-8)
acc=float(((np.tanh(TX@W+B)@U+C).argmax(1)==TY).mean())
root=Path(__file__).resolve().parents[1];out=root/'assets/language';
with (out/'chat.bin').open('wb') as f:
 f.write(struct.pack('>4i',0x42434831,F,H,len(labels)))
 for p in params:f.write(p.astype('>f4').tobytes())
(out/'chat-labels.txt').write_text('\n'.join(labels)+'\n')
card={'name':'Banana chat 1','external_weights':False,'parameters':sum(p.size for p in params),'training_examples':len(train),'heldout_examples':len(test),'heldout_accuracy_synthetic_only':acc,'limitations':'Synthetic templates, not a general LLM. Neural intent selection plus sourced retrieval and programmed responses.'}
(out/'chat-card.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
