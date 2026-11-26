"""Banana Pixel1: campo neuronal condicional RGBA. Pesos y dibujos de origen propios.
No diffusion, pretrained weights, photos, or external drawing datasets.
"""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import numpy as np,random,struct,json,time,math
from PIL import Image,ImageDraw
from pathlib import Path
random.seed(21021);np.random.seed(21021);torch.manual_seed(21021);torch.set_num_threads(2)
root=Path(__file__).resolve().parents[1];out=root/'assets/pixel-v2';out.mkdir(parents=True,exist_ok=True)
S=128;TYPES=['banana','pepino','gato','conejo','robot','perro','oso','zorro','persona'];COLORS=[(214,99,99),(98,156,219),(114,183,131),(234,202,99),(66,73,90),(236,229,221),(232,163,191),(167,138,199)]
DARK=(55,48,63,255)
def draw(kind,rgb,access,expression,z,view=0):
 im=Image.new('RGBA',(128,128));p=ImageDraw.Draw(im);color=tuple(rgb)+(255,)
 def oval(box,fill,outline=DARK,width=1):p.ellipse(tuple(int(v*2) for v in box),fill=fill,outline=outline,width=width*2)
 def rect(box,fill,r=2):p.rounded_rectangle(tuple(int(v*2) for v in box),r*2,fill=fill,outline=DARK,width=2)
 def line(points,fill=DARK,width=2):p.line([(int(x*2),int(y*2)) for x,y in points],fill=fill,width=width*2)
 def poly(points,fill):p.polygon([(int(x*2),int(y*2)) for x,y in points],fill=fill,outline=DARK,width=2)
 light=tuple(min(255,int(c*.65+255*.35)) for c in rgb)+(255,)
 if kind==0:
  oval((12,16,55,57),color);oval((8,8,44,42),(0,0,0,0),outline=None,width=0);rect((48,16,52,22),(131,95,65,255),1);cx,ey=35,43;hx,hy=45,22
 elif kind==1:
  rect((22,18,43,54),color,10);rect((25,23,28,47),light,2);cx,ey=32,32;hx,hy=32,19
 elif kind==2:
  line([(43,45),(52,44),(54,36)],tuple(int(c*.8) for c in rgb)+(255,),4);oval((20,34,45,55),color);poly([(17,25),(18,9),(29,19)],color);poly([(35,18),(46,9),(47,28)],color);oval((16,17,48,43),color);oval((27,31,38,40),light);cx,ey=32,27;hx,hy=32,17
 elif kind==3:
  oval((21,3,28,27),color);oval((36,3,43,27),color);oval((23,6,26,23),(229,160,185,255),None,0);oval((38,6,41,23),(229,160,185,255),None,0);oval((22,37,43,55),color);oval((17,18,47,44),color);cx,ey=32,29;hx,hy=32,18
 elif kind==4:
  line([(32,13),(32,8)]);oval((29,5,35,11),(223,121,114,255));rect((23,37,42,55),color);rect((17,15,48,40),color,4);rect((26,41,39,46),light,1);cx,ey=32,26;hx,hy=32,16
 elif kind==5:
  oval((20,34,45,55),color);oval((13,18,24,39),tuple(max(0,int(c*.65)) for c in rgb)+(255,));oval((41,18,52,39),tuple(max(0,int(c*.65)) for c in rgb)+(255,));oval((18,16,47,43),color);oval((23,29,42,42),light);cx,ey=32,27;hx,hy=32,17
 elif kind==6:
  oval((16,10,28,23),color);oval((37,10,49,23),color);oval((20,35,45,55),color);oval((16,16,49,45),color);oval((24,29,42,43),light);cx,ey=32,27;hx,hy=32,17
 elif kind==7:
  oval((21,36,45,55),color);poly([(16,29),(17,8),(30,19),(39,18),(49,8),(48,31),(33,46)],color);poly([(21,30),(32,35),(44,29),(33,45)],light);cx,ey=32,27;hx,hy=32,17
 elif kind==9:
  oval((17,20,48,55),color);poly([(26,22),(20,15),(30,18),(33,10),(36,18),(46,15),(40,23)],(71,142,88,255));cx,ey=32,33;hx,hy=32,20
 else:
  rect((23,35,43,55),color,5);oval((19,12,46,39),(235,189,143,255));poly([(19,23),(21,13),(32,8),(44,14),(46,25),(37,18),(27,18)],(79,61,57,255));cx,ey=32,26;hx,hy=32,13
 if access in [3,4]:
  rect((21,37,45,55),(127,99,73,255),3);poly([(22,37),(31,42),(28,49)],(183,151,107,255));poly([(44,37),(34,42),(37,49)],(183,151,107,255))
  for yy in [46,51]:oval((32,yy,34,yy+2),DARK,None,0)
 line([(23,39),(15,43)]);line([(43,39),(50,43)]);line([(27,53),(25,60)]);line([(38,53),(40,60)])
 if view!=2:
  for x in ([cx+6] if view==1 else [cx-6,cx+6]):
   oval((x-3,ey-3,x+3,ey+4),(249,249,245,255),DARK,1);oval((x-.5,ey-1,x+1.5,ey+2),(51,55,68,255),None,0)
  if expression==0:p.arc(((cx-5)*2,(ey+4)*2,(cx+5)*2,(ey+12)*2),0,180,fill=DARK,width=2)
  elif expression==1:p.arc(((cx-5)*2,(ey+9)*2,(cx+5)*2,(ey+15)*2),180,360,fill=DARK,width=2)
  else:oval((cx-2,ey+7,cx+2,ey+12),(101,61,76,255),None,0)
  if kind==2 and view==0:
   for side in [-1,1]:
    for offset in [-2,1]:line([(cx+side*9,ey+8),(cx+side*18,ey+7+offset)],DARK,1)
  if access in [1,4]:
   rect((hx-10,hy-14,hx+10,hy),(143,104,77,255),3);rect((hx-16,hy-1,hx+16,hy+3),(97,73,81,255),1)
  elif access==2:
   poly([(hx-12,hy),(hx-13,hy-11),(hx-6,hy-6),(hx,hy-15),(hx+6,hy-6),(hx+13,hy-11),(hx+12,hy)],(235,193,80,255))
  if access==5:
   for xx in [cx-6,cx+6]:p.rectangle(((xx-5)*2,(ey-4)*2,(xx+5)*2,(ey+4)*2),outline=DARK,width=3)
   line([(cx-1,ey),(cx+1,ey)],DARK,1)
 if view==2:
  if access in [1,4]:
   rect((hx-10,hy-14,hx+10,hy),(143,104,77,255),3);rect((hx-16,hy-1,hx+16,hy+3),(97,73,81,255),1)
  elif access==2:
   poly([(hx-12,hy),(hx-13,hy-11),(hx-6,hy-6),(hx,hy-15),(hx+6,hy-6),(hx+13,hy-11),(hx+12,hy)],(235,193,80,255))

 if view==2:
  # Original back view: no face, with a small seam marking the rear of clothing.
  if access in [3,4]:line([(33,38),(33,54)],(97,77,63,255),1)
 if view==1:im=im.transform((128,128),Image.Transform.AFFINE,(1/.78,0,64-64/.78,0,1,0),Image.Resampling.BICUBIC)
 # Continuous width/height latent factors are learned conditioning, not runtime drawing.
 sx,sy=1+float(z[0])*.07,1+float(z[1])*.07
 im=im.transform((128,128),Image.Transform.AFFINE,(1/sx,0,64-64/sx,0,1/sy,68-68/sy),Image.Resampling.BICUBIC)
 im=im.resize((S,S),Image.Resampling.LANCZOS)
 a=np.asarray(im,dtype='f')/255
 # Modest vertical illumination in the targets.
 shade=np.ones((S,1,1),dtype='f');a[:,:,:3]=np.clip(a[:,:,:3]*shade,0,1)
 return a

def condition(kind,color,access,expr,z,view=0):return np.r_[np.eye(9,dtype='f')[kind],np.array(color,dtype='f')/255,np.eye(6,dtype='f')[access],np.eye(3,dtype='f')[expr],np.array(z,dtype='f'),np.eye(3,dtype='f')[view]]
# Dedicated new tomato field, random weights, own synthetic targets only.
COLORS=[(214,99,99),(98,156,219),(114,183,131),(234,202,99),(66,73,90),(236,229,221),(232,163,191),(167,138,199)]
def cond(color,acc,expr,view):return np.r_[np.array(color,dtype='f')/255,np.eye(6,dtype='f')[acc],np.eye(3,dtype='f')[expr],np.eye(3,dtype='f')[view]]
rows=[];test=[]
for ci,color in enumerate(COLORS):
 for acc in range(6):
  for expr in range(3):
   for view in range(3):
    row=(cond(color,acc,expr,view),draw(9,color,acc,expr,[0,0],view))
    (test if ci==1 and acc==1 else rows).append(row)
C=torch.tensor(np.array([c for c,a in rows]));IM=torch.tensor(np.array([a.reshape(-1,4) for c,a in rows]));TC=torch.tensor(np.array([c for c,a in test]));TIM=torch.tensor(np.array([a.reshape(-1,4) for c,a in test]))
y,x=np.meshgrid((np.arange(S)+.5)/S*2-1,(np.arange(S)+.5)/S*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');coords=[xy]
for freq in [1,2,4,8,16]:coords += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(coords,1));net=nn.Sequential(nn.Linear(37,160),nn.ReLU(),nn.Linear(160,160),nn.ReLU(),nn.Linear(160,160),nn.ReLU(),nn.Linear(160,4));opt=torch.optim.Adam(net.parameters(),lr=.0015);start=time.time()
for step in range(1,10001):
 if step==7500:opt.param_groups[0]['lr']=.0003
 image=random.randrange(len(C));target=IM[image];fg=torch.where(target[:,3]>.5)[0];white=torch.where((target[:,3]>.5)&(target[:,:3].mean(1)>.9))[0];white=white if len(white)>0 else fg;ix=torch.cat([torch.randint(S*S,(512,)),fg[torch.randint(len(fg),(768,))],white[torch.randint(len(white),(256,))]])
 logits=net(torch.cat([P[ix],C[image].expand(len(ix),-1)],1));truth=target[ix];loss=F.binary_cross_entropy_with_logits(logits[:,3],truth[:,3])+8*(torch.abs(torch.sigmoid(logits[:,:3])-truth[:,:3])*truth[:,3,None]).mean();opt.zero_grad();loss.backward();opt.step()
 if step%1000==0:print(step,float(loss),round(time.time()-start),flush=True)
layers=[net[0],net[2],net[4],net[6]]
with (out/'tomato22.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42505831,37,160,4,128))
 for layer in layers:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
with torch.no_grad():
 maes=[];ious=[]
 for c,target in zip(TC,TIM):
  pred=torch.sigmoid(net(torch.cat([P,c.expand(S*S,-1)],1)));mask=target[:,3]>.5;pm=pred[:,3]>.5;maes.append(float(torch.abs(pred[mask,:3]-target[mask,:3]).mean()));ious.append(float((mask&pm).sum()/((mask|pm).sum())))
 inp=torch.cat([P[[111,555,9000]],C[0].expand(3,-1)],1);expected=torch.sigmoid(net(inp))
with (root/'model-source/tomato22-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',3,37))
 for x,y in zip(inp,expected):f.write(x.numpy().astype('>f4').tobytes());f.write(y.numpy().astype('>f4').tobytes())
(out/'tomato22-card.json').write_text(json.dumps({'name':'Banana Pixel 2.2','component':'tomato field','external_weights':False,'parameters':sum(p.numel() for p in net.parameters()),'training_conditions':len(rows),'heldout_conditions':len(test),'heldout_alpha_IoU':np.mean(ious),'heldout_foreground_RGB_MAE':np.mean(maes),'limitations':'Own synthetic tomato, finite catalog only. Original 2.1 own fields retained for other bodies.'},indent=2));print('DONE_TOMATO',flush=True)
