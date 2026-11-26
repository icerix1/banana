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
root=Path(__file__).resolve().parents[1];out=root/'assets/pixels2';out.mkdir(parents=True,exist_ok=True)
S=96;TYPES=['banana','pepino','gato','conejo','robot'];COLORS=[(214,99,99),(98,156,219),(114,183,131),(234,202,99),(66,73,90),(236,229,221),(232,163,191),(167,138,199)]
DARK=(55,48,63,255)
def draw(kind,rgb,access,expression,z):
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
 else:
  line([(32,13),(32,8)]);oval((29,5,35,11),(223,121,114,255));rect((23,37,42,55),color);rect((17,15,48,40),color,4);rect((26,41,39,46),light,1);cx,ey=32,26;hx,hy=32,16
 line([(23,39),(15,43)]);line([(43,39),(50,43)]);line([(27,53),(25,60)]);line([(38,53),(40,60)])
 for x in [cx-6,cx+6]:
  oval((x-3,ey-3,x+3,ey+4),(249,249,245,255),DARK,1);oval((x-.5,ey-1,x+1.5,ey+2),(51,55,68,255),None,0)
 if expression==0:p.arc(((cx-5)*2,(ey+4)*2,(cx+5)*2,(ey+12)*2),0,180,fill=DARK,width=2)
 elif expression==1:p.arc(((cx-5)*2,(ey+9)*2,(cx+5)*2,(ey+15)*2),180,360,fill=DARK,width=2)
 else:oval((cx-2,ey+7,cx+2,ey+12),(101,61,76,255),None,0)
 if kind==2:
  for side in [-1,1]:
   for offset in [-2,1]:line([(cx+side*9,ey+8),(cx+side*18,ey+7+offset)],DARK,1)
 if access==1:
  rect((hx-10,hy-14,hx+10,hy),(143,104,77,255),3);rect((hx-16,hy-1,hx+16,hy+3),(97,73,81,255),1)
 elif access==2:
  poly([(hx-12,hy),(hx-13,hy-11),(hx-6,hy-6),(hx,hy-15),(hx+6,hy-6),(hx+13,hy-11),(hx+12,hy)],(235,193,80,255))
 # Continuous width/height latent factors are learned conditioning, not runtime drawing.
 sx,sy=1+float(z[0])*.07,1+float(z[1])*.07
 im=im.transform((128,128),Image.Transform.AFFINE,(1/sx,0,64-64/sx,0,1/sy,68-68/sy),Image.Resampling.BICUBIC)
 im=im.resize((S,S),Image.Resampling.LANCZOS)
 a=np.asarray(im,dtype='f')/255
 # Modest vertical illumination in the targets.
 shade=np.linspace(1.12,.85,S,dtype='f')[:,None,None];a[:,:,:3]=np.clip(a[:,:,:3]*shade,0,1)
 return a

def condition(kind,color,access,expr,z):return np.r_[np.eye(5,dtype='f')[kind],np.array(color,dtype='f')/255,np.eye(3,dtype='f')[access],np.eye(3,dtype='f')[expr],np.array(z,dtype='f')]
rows=[];test=[]
for kind in range(5):
 for ci,color in enumerate(COLORS):
  for access in range(3):
   for expr in range(3):
    for variant in range(2):
     z=np.zeros(2,dtype='f');row=(condition(kind,color,access,expr,z),draw(kind,color,access,expr,z))
     # Held-out composition: blue cucumber with hat, all expressions/variants.
     (test if kind==1 and ci==1 and access==1 else rows).append(row)
C=torch.tensor(np.array([c for c,a in rows]));IM=torch.tensor(np.array([a.reshape(-1,4) for c,a in rows]));TC=torch.tensor(np.array([c for c,a in test]));TIM=torch.tensor(np.array([a.reshape(-1,4) for c,a in test]));
y,x=np.meshgrid((np.arange(S)+.5)/S*2-1,(np.arange(S)+.5)/S*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');coord=[xy]
for freq in [1,2,4,8,16]:coord += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(coord,axis=1));INPUT=P.shape[1]+C.shape[1]
class Pixel(nn.Module):
 def __init__(self):super().__init__();self.a=nn.Linear(INPUT,160);self.b=nn.Linear(160,160);self.c=nn.Linear(160,160);self.d=nn.Linear(160,4)
 def forward(self,x):return self.d(F.relu(self.c(F.relu(self.b(F.relu(self.a(x)))))))
net=Pixel();opt=torch.optim.Adam(net.parameters(),lr=.0015)
fg=[torch.where(a[:,3]>.3)[0] for a in IM];dark=[torch.where((a[:,3]>.7)&(a[:,:3].mean(1)<.35))[0] for a in IM]
start=time.time()
for step in range(1,12001):
 if step==7000:opt.param_groups[0]["lr"]=.0005
 if step==10000:opt.param_groups[0]["lr"]=.0002
 image=int(torch.randint(len(C),(1,)));n=2048
 a=torch.randint(S*S,(n//2,));f=fg[image];a=torch.cat([a,f[torch.randint(len(f),(n//4,))]])
 k=dark[image];k=k if len(k)>0 else f;a=torch.cat([a,k[torch.randint(len(k),(n//4,))]])
 pred=net(torch.cat([P[a],C[image].expand(n,-1)],1));target=IM[image,a];rgb=torch.sigmoid(pred[:,:3]);alpha=target[:,3]
 loss=F.binary_cross_entropy_with_logits(pred[:,3],alpha)+3*((rgb-target[:,:3])**2*alpha[:,None]).sum()/torch.clamp(alpha.sum()*3,min=1)
 opt.zero_grad();loss.backward();opt.step()
 if step%500==0:print(step,'loss',round(loss.item(),4),'seconds',round(time.time()-start),flush=True)
net.eval()
def render(c):
 with torch.no_grad():return torch.sigmoid(net(torch.cat([P,c.expand(S*S,-1)],1))).numpy().reshape(S,S,4)
ious=[];maes=[]
for cond,target in test:
 pred=render(torch.tensor(cond));mask=target[:,:,3]>.5;pm=pred[:,:,3]>.5;ious.append(float((mask&pm).sum()/max(1,(mask|pm).sum())));maes.append(float(np.abs(pred[:,:,:3]-target[:,:,:3])[mask].mean()))
with (out/'pixel2.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42505831,INPUT,160,4,S))
 for layer in [net.a,net.b,net.c,net.d]:
  f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
card={'name':'Banana Pixel','component':'personajes','system_release':'0.30','architecture':'conditional coordinate MLP with Fourier coordinates, 3 hidden layers, RGBA output','parameters':sum(p.numel() for p in net.parameters()),'external_weights':False,'training_images':len(rows),'heldout_images':len(test),'unique_training_conditions':len(set(tuple(c.tolist()) for c,a in rows)),'unique_heldout_conditions':len(set(tuple(c.tolist()) for c,a in test)),'duplicate_note':'Two identical zero-latent variants per condition; synthetic evaluation only.','heldout_combination':'blue cucumber + hat','heldout_alpha_IoU':float(np.mean(ious)),'heldout_foreground_RGB_MAE':float(np.mean(maes)),'size':S,'hidden':160,'coordinate_frequencies':[1,2,4,8,16],'types':TYPES,'accessories':['none','hat','crown'],'expressions':['happy','sad','surprised'],'limitations':'Neural renderer learned from our own synthetic cartoons. Not diffusion or universal text-to-image. Background and motion remain procedural.'}
(out/'model-card.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
# Independent reference vectors for Java parity, including all coordinate features.
with (root/'model-source/pixel2-reference.bin').open('wb') as f:
 cond=torch.tensor(condition(1,COLORS[1],1,0,[0,0]));inp=torch.cat([P[[0,555,2048,5075,9000]],cond.expand(5,-1)],1)
 with torch.no_grad():expected=torch.sigmoid(net(inp))
 f.write(struct.pack('>2i',5,INPUT));
 for a,b in zip(inp,expected):f.write(a.numpy().astype('>f4').tobytes());f.write(b.numpy().astype('>f4').tobytes())
# Inspection sheet; retained only as a local test image.
sheet=Image.new('RGB',(6*128,3*128),(220,227,230))
for j in range(3):
 for i in range(6):
  kind=i%5;access=1 if i==5 else j;expr=j;cond=condition(kind,COLORS[1] if i in [1,5] else COLORS[2 if kind==1 else 3],access,expr,[0,0]);a=render(torch.tensor(cond));a[:,:,3][a[:,:,3]<.1]=0;im=Image.fromarray((a*255).clip(0,255).astype('uint8'),'RGBA').resize((128,128));sheet.paste(im,(i*128,j*128),im)
sheet.save(root.parent/'pixel2-inspection.png')
