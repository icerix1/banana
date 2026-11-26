"""Own intent encoder for contextual help, not a general language model."""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F,numpy as np,random,unicodedata,re,json,struct
from pathlib import Path
r=Path(__file__).resolve().parents[1];out=r/'assets/context1';out.mkdir(parents=True,exist_ok=True);random.seed(38038);torch.manual_seed(38038);torch.set_num_threads(2)
groups={
'continue':['como sigo la escena','quiero continuar la historia','como agrego otra accion','seguir sin perder lo anterior','continuar con los mismos personajes','quiero agregar algo despues','como continuo mi video','que hago para seguir la secuencia'],
'recap':['me perdi en el proyecto','resumime lo que hicimos','como va mi escena','que estabamos preparando','recordame donde quedamos','quiero revisar el proyecto actual','mostrame un resumen del trabajo','no recuerdo lo que hicimos'],
'voice':['como hago para que hablen','quiero ponerles voces','como escribo un dialogo','quiero que digan una frase','agregar voz a mis personajes','hacer hablar a los personajes','poner una frase hablada','quiero crear una conversacion hablada'],
'voice_limits':['las voces son naturales','de donde salen las voces','que calidad tienen las voces','son voces neuronales','la voz suena robotica','usa otra ia para hablar','como sintetizas las voces','que limitaciones tiene la voz'],
'custom':['quiero inventar un personaje','como dibujo una criatura nueva','quiero cambiar la forma del cuerpo','puedo combinar orejas y cola','como creo un monstruo propio','quiero un personaje diferente del catalogo','como diseno una criatura combinable','cuales son las formas de dibujo'],
'duration':['como hago un video mas largo','quiero aumentar la duracion','como elijo cuantos segundos dura','puedo crear videos de un minuto','cual es el limite de tiempo','necesito que el video dure mas','como unir varias tomas','quiero una escena mas larga'],
'objects':['como entrego una caja','como se quien tiene un objeto','no puede entregar esa caja','como paso un objeto a otro','quien puede sostener una lampara','como funciona el intercambio de objetos','que pasa si no tiene el objeto','por que rechaza una entrega'],
'correction':['no me entendiste bien','quiero corregir solo una parte','como cambio algo sin borrar todo','quiero deshacer el ultimo cambio','como modifico solo el sombrero','no era lo que queria hacer','me equivoque de personaje','como reparo mi escena'],
'ideas':['dame una idea para seguir','que podemos hacer ahora','proponeme un dialogo','ayudame a pensar lo siguiente','que escena podria hacer','no se que hacer despues','sugeri algo para el proyecto','inventemos una idea con mis personajes'],
'capability':['que podes hacer en banana','cuales son tus funciones','podes dibujar cualquier cosa','que herramientas tiene este proyecto','para que sirve banana','que cosas podes crear','cuales son tus limites','como empiezo a usar esto'],
'memory':['como guardo un recuerdo','quiero recordar un dato del proyecto','como borro un recuerdo','como corrijo un dato guardado','donde veo mis recuerdos','puedo conservar datos entre conversaciones','como funciona la memoria del proyecto','como actualizo un recuerdo'],
'audio':['el video tiene sonido','quiero escuchar el audio','los gif tienen audio','como activo el sonido del mp4','por que no escucho los pasos','que sonido se guarda al exportar','como desactivo el audio','que incluye la pista de sonido'],
'unknown':['cuanto vale el dolar hoy','quien gano el partido','como se cura una enfermedad','cual es mi direccion','que esta pasando en las noticias','cual es la capital de alemania','quien es el presidente','cual es la cotizacion del bitcoin','como resuelvo una ecuacion','quiero comprar una computadora','que tiempo hace manana','por que gira la tierra','que es una proteina','dame una receta para comer','busca hoteles en noruega','contame de una celebridad']}
held={
'continue':['de que manera puedo seguir con la misma historia','podemos sumar lo que pasa despues sin reemplazar'],
'recap':['podrias recordarme como quedo el trabajo','en que punto dejamos el proyecto'],
'voice':['se les puede poner dialogos a los personajes','quiero escuchar al personaje decir hola'],
'voice_limits':['que tan humana es la voz de banana','la voz hablada es un modelo neuronal'],
'custom':['me gustaria disenar un personaje nuevo','puedo mezclar diferentes cuerpos orejas y colas'],
'duration':['necesito alargar el video que estoy haciendo','se pueden juntar escenas para tener mas tiempo'],
'objects':['como hago que uno le pase la caja al otro','si no tiene la lampara puede entregarla'],
'correction':['podemos arreglar una parte sin perder el resto','el cambio no era para ese personaje'],
'ideas':['me propones alguna idea con este elenco','no se como seguir podrias sugerir algo'],
'capability':['explicame que se puede hacer con banana','que sabes crear dentro del proyecto'],
'memory':['como hago para que no olvides ese dato','quiero editar uno de mis recuerdos guardados'],
'audio':['el mp4 sale con una pista de audio','el gif exportado tambien tiene voces'],
'unknown':['quien gano la eleccion de ayer','cual es el tratamiento para la gripe','como hago una pizza','cuanto cuesta una casa','hay vida en otros planetas','cual es mi numero de telefono']}
labels=list(groups);D=1024
norm=lambda x:re.sub('[^a-z0-9 ]',' ',''.join(c for c in unicodedata.normalize('NFD',x.lower()) if unicodedata.category(c)!='Mn'))
def features(q):
 text='^'+' '.join(norm(q).split())[:240]+'$';v=np.zeros(D,np.float32)
 for n in [3,4,5]:
  for i in range(len(text)-n+1):
   h=2166136261
   for c in text[i:i+n]:h=((h^ord(c))*16777619)&0xffffffff
   v[(h&0x7fffffff)%D]+=1
 v=np.minimum(v,3);v/=max(1e-6,np.linalg.norm(v));return v
train=[]
for label,queries in groups.items():
 for q in queries:
  for pre in ['', 'banana ', 'che ', 'por favor ', 'me explicas ', 'una pregunta ']:
   for post in ['', ' porfa',' en este proyecto']:train.append((pre+q+post,labels.index(label)))
test=[(q,labels.index(k)) for k,qs in held.items() for q in qs];assert not(set(q for q,_ in train)&set(q for q,_ in test))
X=torch.tensor(np.stack([features(q) for q,_ in train]));Y=torch.tensor([y for _,y in train]);TX=torch.tensor(np.stack([features(q) for q,_ in test]));TY=torch.tensor([y for _,y in test]);net=nn.Sequential(nn.Linear(D,64),nn.ReLU(),nn.Linear(64,len(labels)));opt=torch.optim.AdamW(net.parameters(),lr=.004)
for step in range(1800):
 ix=torch.randint(len(X),(64,));loss=F.cross_entropy(net(X[ix]),Y[ix]);opt.zero_grad();loss.backward();opt.step()
with torch.no_grad():scores=net(TX).softmax(1);pred=scores.argmax(1);accuracy=float((pred==TY).float().mean());covered=(scores.max(1).values>=.85);accepted=int(covered.sum());correct=int(((pred==TY)&covered).sum())
rows=[{'q':q,'expected':labels[y],'actual':labels[int(pred[i])],'confidence':float(scores[i].max())} for i,(q,y) in enumerate(test)]
card={'name':'Banana contextual intent encoder1','external_weights':False,'external_teacher':False,'train_examples':len(train),'heldout_examples':len(test),'heldout_accuracy_synthetic':accuracy,'confident_coverage':accepted/len(test),'confident_accuracy':correct/max(1,accepted),'threshold':.85,'limitations':'Small authored project-help intent set, not a general conversational LLM; predicts help labels, never executes arbitrary generated commands.','heldout':rows}
card['promoted']=accuracy>=.75 and correct/max(1,accepted)>=.85
with (out/'context1.bin').open('wb') as f:
 f.write(struct.pack('>4i',0x424e4c31,D,64,len(labels)))
 for layer in [net[0],net[2]]:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
(out/'labels.txt').write_text('\n'.join(labels)+'\n');(out/'model-card.json').write_text(json.dumps(card,indent=2));(r/'model-source/context1-corpus.json').write_text(json.dumps({'train':train,'heldout':test,'labels':labels},ensure_ascii=False,indent=2))
with (r/'model-source/context1-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',3,D))
 for x in TX[:3]:f.write(x.numpy().astype('>f4').tobytes());f.write(net(x[None])[0].detach().numpy().astype('>f4').tobytes())
print(json.dumps(card,indent=2),flush=True)
