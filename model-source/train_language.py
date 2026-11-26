"""BananaSemilla: modelo propio, inicializacion aleatoria, corpus sintetico propio.
Modelo de ventana: embeddings compartidos + MLP; cabeza de lenguaje causal y
cabeza contextual de categorias para planificar escenas. No usa pesos externos.
"""
import os
os.environ['OPENBLAS_NUM_THREADS']='1'
import numpy as np,random,json,struct,re,time
from pathlib import Path
random.seed(20261119);rng=np.random.default_rng(20261119)
root=Path(__file__).resolve().parents[1];out=root/'assets'/'language';out.mkdir(parents=True,exist_ok=True)
nouns='banana pepino tomate gato perro conejo leon tigre jirafa elefante zorro oso vaca caballo raton tortuga delfin tiburon pez pajaro loro aguila pinguino pato gallina mariposa abeja hormiga persona hombre mujer nino nina astronauta robot dinosaurio dragon monstruo fantasma planeta saturno marte luna sol argentina mexico brasil espana chile'.split()
looks='rojo roja azul verde amarillo amarilla negro negra blanco blanca gris rosa marron naranja violeta alto alta bajo baja pequeno pequena grande gordo gorda delgado delgada feliz triste rubio rubia'.split()
access='sombrero corona bufanda lentes anteojos gafas bigote'.split()
places='playa bosque parque ciudad montana espacio desierto jardin nieve mar'.split()
actions={
'camina':['camina','caminar','anda','avanza','pasea','camine','caminando'], 'salta':['salta','saltar','brinca','rebota','salte','saltando'],
'baila':['baila','bailar','danza','baile','bailando'], 'habla':['habla','hablar','hablarle','charla','conversa','hable','hablando'],
'saluda':['saluda','saludar','salude'], 'acerca':['acerca','acercarse','llega','viene','va'],
'gira':['gira','girar','rota'], 'duerme':['duerme','dormir','descansa'], 'vuela':['vuela','volar','planea','vuele','volando'],
'nada':['nada','nadar'], 'aplaude':['aplaude','aplaudir'], 'corre':['corre','correr','corra','corriendo'],
'canta':['canta','cantar','cante'], 'espera':['espera','quieto','quieta']}
labels=['OTHER','ENTITY','LOOK','PLACE']+list(actions)
verb={v:k for k,vs in actions.items() for v in vs if ' ' not in v}
def example(test=False):
 seq=[];tags=[]
 def add(words,tag='OTHER'):
  for w in words.split():seq.append(w);tags.append(labels.index(tag))
 if random.random()<.2:add(random.choice(['quiero que','hace que','necesito que','por favor','ahora']))
 count=random.choice([1,1,2,2,3]);chosen=random.sample(nouns,count)
 for i,n in enumerate(chosen):
  if i:add(random.choice(['y','luego','despues','y despues','mientras']))
  add(random.choice(['un','una','el','la']))
  add(n,'ENTITY')
  for _ in range(random.choice([0,1,1,2])):add(random.choice(looks),'LOOK')
  if random.random()<.35:add('con');add(random.choice(access),'LOOK')
  if test and random.random()<.5:add('ahora')
  for j in range(random.choice([1,1,2])):
   if j:add(random.choice(['y','despues','luego']))
   act=random.choice(list(actions));vs=[x for x in actions[act] if ' ' not in x];add(random.choice(vs),act)
  if random.random()<.25:add('en el');add(random.choice(places),'PLACE')
 if random.random()<.6:add('en la');add(random.choice(places),'PLACE')
 if random.random()<.25:add('de noche' if random.random()<.5 else 'de dia','PLACE')
 return seq,tags
train=[example() for _ in range(10000)];test=[example(True) for _ in range(600)]
extra=['a','al','le','se','que','mira','hacia','hasta','no','con','sin','ahora','primero','finalmente']
vocab=['<pad>','<unk>']+sorted(set(extra+[w for row,_ in train for w in row]));ids={w:i for i,w in enumerate(vocab)}
def arrays(data,noise=False):
 xs=[];ys=[];ls=[];nxt=[]
 for words,tags in data:
  tokens=[ids.get(w,1) for w in words]
  for i,tag in enumerate(tags):
   tok=tokens.copy()
   if noise and tag==1 and random.random()<.18:tok[i]=1
   xs.append([tok[j] if 0<=j<len(tok) else 0 for j in range(i-2,i+3)])
   ls.append([tok[j] if j>=0 else 0 for j in range(i-4,i+1)])
   ys.append(tag);nxt.append(tokens[i+1] if i+1<len(tokens) else 0)
 return np.array(xs),np.array(ys),np.array(ls),np.array(nxt)
X,Y,L,N=arrays(train,True);TX,TY,TL,TN=arrays(test)
v=len(vocab);d=16;h=48;c=len(labels)
E=rng.normal(0,.15,(v,d)).astype('f');W=rng.normal(0,.12,(5*d,h)).astype('f');B=np.zeros(h,'f');T=rng.normal(0,.12,(h,c)).astype('f');TB=np.zeros(c,'f');G=rng.normal(0,.1,(h,v)).astype('f');GB=np.zeros(v,'f')
params=[E,W,B,T,TB,G,GB];m=[np.zeros_like(p) for p in params];vv=[np.zeros_like(p) for p in params]
def soft(z):z=z-z.max(1,keepdims=True);p=np.exp(z);return p/p.sum(1,keepdims=True)
start=time.time()
for step in range(1,1801):
 idx=rng.integers(0,len(Y),512);x=X[idx];l=L[idx];target=Y[idx];nt=N[idx]
 a=E[x].reshape(-1,5*d);b=E[l].reshape(-1,5*d);ht=np.tanh(a@W+B);hl=np.tanh(b@W+B)
 p=soft(ht@T+TB);q=soft(hl@G+GB)
 dt=p.copy();dt[np.arange(len(idx)),target]-=1;dt/=len(idx)
 dg=q.copy();dg[np.arange(len(idx)),nt]-=1;dg*=.25/len(idx)
 dh=(dt@T.T)*(1-ht*ht);dl=(dg@G.T)*(1-hl*hl)
 de=np.zeros_like(E);np.add.at(de,x,(dh@W.T).reshape(-1,5,d));np.add.at(de,l,(dl@W.T).reshape(-1,5,d))
 grads=[de,a.T@dh+b.T@dl,dh.sum(0)+dl.sum(0),ht.T@dt,dt.sum(0),hl.T@dg,dg.sum(0)]
 for i,(par,grad) in enumerate(zip(params,grads)):
  grad=np.clip(grad,-1,1);m[i]=.9*m[i]+.1*grad;vv[i]=.999*vv[i]+.001*grad*grad;par-=.003*(m[i]/(1-.9**step))/(np.sqrt(vv[i]/(1-.999**step))+1e-8)
 if step%300==0:print(step,'tagloss',round(float(-np.log(p[np.arange(len(idx)),target]+1e-9).mean()),4),'secs',round(time.time()-start),flush=True)
def evaluate(x,y,l,n):
 h1=np.tanh(E[x].reshape(-1,5*d)@W+B);pr=soft(h1@T+TB);pred=pr.argmax(1)
 h2=np.tanh(E[l].reshape(-1,5*d)@W+B);lp=soft(h2@G+GB)
 return {'token_accuracy':float((pred==y).mean()),'entity_recall':float((pred[y==1]==1).mean()),'next_word_cross_entropy':float(-np.log(lp[np.arange(len(n)),n]+1e-9).mean())}
metrics=evaluate(TX,TY,TL,TN)
with (out/'semilla.bin').open('wb') as f:
 f.write(struct.pack('>6i',0x42534d31,v,d,h,c,5))
 for arr in params:f.write(arr.astype('>f4').tobytes())
(out/'vocab.txt').write_text('\n'.join(vocab)+'\n');(out/'labels.txt').write_text('\n'.join(labels)+'\n')
report={'name':'BananaSemilla 1','architecture':'shared embeddings + window MLP, contextual tagging and causal next-word head','external_weights':False,'seed':20261119,'parameters':sum(p.size for p in params),'train_sentences':len(train),'test_sentences':len(test),'metrics_synthetic_only':metrics,'limitations':'Synthetic evaluation only; not a general conversational LLM. Unknown words and complex syntax may fail.'}
(out/'model-card.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
(root/'model-source'/'heldout.json').write_text(json.dumps([{'text':' '.join(w),'labels':t} for w,t in test[:25]],ensure_ascii=False))
np.savez(root/'model-source'/'reference.npz',X=TX[:80],logits=np.tanh(E[TX[:80]].reshape(-1,5*d)@W+B)@T+TB)
