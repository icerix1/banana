"""Banana Fondo1: fondos de terror neuronales propios, sin pesos externos."""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import numpy as np,random,struct,json,time
from PIL import Image,ImageDraw,ImageFilter
from pathlib import Path
random.seed(22022);np.random.seed(22022);torch.manual_seed(22022);torch.set_num_threads(2)
root=Path(__file__).resolve().parents[1];out=root/'assets/background';out.mkdir(parents=True,exist_ok=True)
S=128;RENDER=128

def draw(kind,night,z,phase,tree):
 yy,xx=np.meshgrid(np.arange(S)/S,np.arange(S)/S,indexing='ij');brightness=1-.55*night
 top=np.array([41,51,71])*brightness;bottom=np.array([86,106,106])*brightness
 arr=top[None,None,:]*(1-yy[:,:,None])+bottom[None,None,:]*yy[:,:,None];arr=np.repeat(arr,1,axis=1)
 glow=np.exp(-((xx-(.72+z[0]*.04))**2+(yy-.20)**2)/.012)[:,:,None];arr+=glow*np.array([29,40,35])[None,None,:]
 im=Image.fromarray(arr.clip(0,255).astype('uint8'),'RGB');p=ImageDraw.Draw(im);mask=Image.new('L',(S,S));m=ImageDraw.Draw(mask)
 def points(a):return [(int(x*S),int(y*S)) for x,y in a]
 def poly(a,c):p.polygon(points(a),fill=c)
 def rect(box,c):p.rectangle(tuple(int(v*S) for v in box),fill=c)
 if kind in [0,3]:
  for layer in range(2):
   line=[(0,.54+layer*.1)]+[(i/8,.48+layer*.09+.07*np.sin(i*1.9+z[0])) for i in range(9)]+[(1,1),(0,1)];poly(line,(int(29+layer*9),int(44+layer*8),int(45+layer*9)))
  if kind==3:
   poly([(0,.67),(.24,.25),(.48,.67),(.74,.30),(1,.70)],(46,57,66));poly([(.16,.39),(.24,.25),(.32,.40),(.25,.36)],(104,112,112))
  for i in range(8):
   x=(i*.147+.015+z[1]*.012)%1;rect((x,.30,x+.016,.83),(25,30,31));poly([(x-.09,.59),(x+.008,.21+(.03*(i%3))),(x+.105,.61)],(21,39,39))
  rect((0,.79,1,1),(35,46,45))
  poly([(.38,1),(.48,.72),(.57,.72),(.70,1)],(61,70,65))
 elif kind==1:
  rect((0,.50,1,.74),(39,68,78));rect((0,.74,1,1),(77,77,67));
  for i in range(7):
   y=.54+i*.025;p.line(points([(0,y),(1,y+.005)]),fill=(70,94,99),width=1)
  poly([(.30,.84),(.36,.57),(.40,.56),(.45,.86)],(37,42,45));rect((.36,.51,.40,.58),(31,37,41))
 elif kind==2:
  for i in range(8):
   x=i*.15-.04+float(z[0])*.018;top=.29+((i*7)%5)*.035+float(z[1])*.06;rect((x,top,x+.13,.86),(30+(i%3)*5,37+(i%3)*5,44+(i%3)*5))
   for row in range(5):
    for col in range(2):
     if (i+row+col)%4==0:rect((x+.025+col*.05,top+.04+row*.075,x+.04+col*.05,top+.065+row*.075),(111,113,91))
  poly([(0,.85),(.45,.72),(.58,.72),(1,.85),(1,1),(0,1)],(40,44,48));p.line(points([(.51,.82),(.54,1)]),fill=(106,103,90),width=1)
 else:
  poly([(0,0),(.40,.26),(.40,.71),(0,1)],(49,48,56));poly([(1,0),(.61,.26),(.61,.71),(1,1)],(37,42,50));poly([(0,1),(.40,.71),(.61,.71),(1,1)],(47,47,50));rect((.40,.26,.61,.72),(14,22,28))
  for i in range(3):
   x=.08+i*.11;poly([(x,.18+i*.04),(x+.075,.23+i*.04),(x+.075,.76-i*.02),(x,.81-i*.02)],(20,28,34))
  for i in range(3):
   y=.80+i*.08;p.line(points([(0,y),(1,y)]),fill=(64,64,62),width=1)
  rect((.45+float(z[0])*.03,.16,.55+float(z[0])*.03,.18),(int(137+z[1]*22),int(141+z[1]*20),120))
  rect((.43+float(z[0])*.025,.37,.57+float(z[0])*.025,.71),(int(17+z[1]*6),int(26+z[1]*5),31))
 if tree:
  rect((.15,.52,.21,.90),(25,27,29));p.ellipse((int(.02*S),int(.32*S),int(.34*S),int(.66*S)),fill=(24,39,38));m.rectangle((int(.15*S),int(.52*S),int(.21*S),int(.90*S)),fill=255);m.ellipse((int(.02*S),int(.32*S),int(.34*S),int(.66*S)),fill=255)

 if kind==4:
  # Side door and wall light add readable depth; variation remains conditional.
  doorx=.74+float(z[0])*.025;rect((doorx,.25,doorx+.10,.73),(26,35,46));rect((doorx+.015,.29,doorx+.085,.68),(65,75,82))
  rect((doorx+.07,.48,doorx+.079,.50),(191,169,105))
 if kind==2:
  rect((.78,.46,.79,.89),(49,53,59));rect((.74,.44,.82,.47),(151,155,130))
 a=np.asarray(im,dtype='f')/255;a=np.clip(a*(1.25-.18*night)+.025,0,1);
 fog=np.zeros_like(xx);a=a*(1-fog[:,:,None])+np.array([.45,.51,.51])[None,None,:]*fog[:,:,None]
 vignette=np.clip(1-.35*((xx-.5)**2+(yy-.5)**2),.6,1);a*=vignette[:,:,None]
 return np.dstack([a,np.asarray(mask,dtype='f')/255]).astype('f')
def condition(kind,night,z,phase,tree):return np.r_[np.eye(5,dtype='f')[kind],night,z,phase,tree].astype('f')
rows=[];test=[]
for kind in range(5):
 for night in [.25,.65,1.0]:
  for tree in [0,1]:
   for i in range(28):
    z=np.random.uniform(-1,1,2).astype('f');phase=float(np.random.uniform(-1,1));row=(condition(kind,night,z,phase,tree),draw(kind,night,z,phase,tree));(test if i>=26 else rows).append(row)
C=torch.tensor(np.array([c for c,a in rows]));IM=torch.tensor(np.array([a.reshape(-1,4) for c,a in rows]));
y,x=np.meshgrid((np.arange(S)+.5)/S*2-1,(np.arange(S)+.5)/S*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');coord=[xy]
for freq in [1,2,4,8,16]:coord += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(coord,1));INPUT=32;H=128
class Bg(nn.Module):
 def __init__(self):super().__init__();self.a=nn.Linear(INPUT,H);self.b=nn.Linear(H,H);self.c=nn.Linear(H,H);self.d=nn.Linear(H,4)
 def forward(self,x):return self.d(F.relu(self.c(F.relu(self.b(F.relu(self.a(x)))))))
net=Bg();opt=torch.optim.Adam(net.parameters(),lr=.0015);start=time.time()
for step in range(1,10001):
 if step==7500:opt.param_groups[0]['lr']=.0005
 ix=torch.randint(len(C),(4096,));pix=torch.randint(S*S,(4096,));target=IM[ix,pix];pred=net(torch.cat([P[pix],C[ix]],1));loss=3*F.mse_loss(torch.sigmoid(pred[:,:3]),target[:,:3])+F.binary_cross_entropy_with_logits(pred[:,3],target[:,3])*.3;opt.zero_grad();loss.backward();opt.step()
 if step%500==0:print(step,round(loss.item(),5),'seconds',round(time.time()-start),flush=True)
net.eval()
def render(c,side=S):
 y,x=np.meshgrid((np.arange(side)+.5)/side*2-1,(np.arange(side)+.5)/side*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');v=[xy]
 for freq in [1,2,4,8,16]:v += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
 p=torch.tensor(np.concatenate(v,1));cond=torch.tensor(c)
 with torch.no_grad():return torch.sigmoid(net(torch.cat([p,cond.expand(side*side,-1)],1))).numpy().reshape(side,side,4)
errs=[]
for c,a in test:errs.append(float(np.abs(render(c)[:,:,:3]-a[:,:,:3]).mean()))
with (out/'background22.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42424731,INPUT,H,4,RENDER))
 for layer in [net.a,net.b,net.c,net.d]:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
card={'name':'Banana Pixel 2.2','component':'escenarios','system_release':'0.34','external_weights':False,'parameters':sum(p.numel() for p in net.parameters()),'training_images':len(rows),'heldout_images':len(test),'synthetic_heldout_RGB_MAE':float(np.mean(errs)),'training_size':S,'render_size':RENDER,'types':['bosque/parque','playa','ciudad','montana','pasillo/interior'],'channels':'RGB + learned foreground mask','limitations':'Own synthetic horror backgrounds, not universal scenes or neural video. Camera/compositing and animation remain code.'};(out/'model-card22.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
with (root/'model-source/background22-reference.bin').open('wb') as f:
 cond=torch.tensor(condition(0,.65,[0,0],0,1));inp=torch.cat([P[[0,900,2048,5075,9000]],cond.expand(5,-1)],1)
 with torch.no_grad():expected=torch.sigmoid(net(inp))
 f.write(struct.pack('>2i',5,INPUT))
 for a,b in zip(inp,expected):f.write(a.numpy().astype('>f4').tobytes());f.write(b.numpy().astype('>f4').tobytes())
sheet=Image.new('RGB',(5*192,192))
for i in range(5):sheet.paste(Image.fromarray((render(condition(i,.8,[0,0],0,i==0),192)[:,:,:3]*255).clip(0,255).astype('uint8')),(i*192,0))
sheet.save(root.parent/'background22-inspection.png')
