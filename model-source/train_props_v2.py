"""Banana Pixel 2 object field, random initialization, original synthetic targets."""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
from pathlib import Path
import torch,torch.nn as nn,torch.nn.functional as F,numpy as np,struct,json,random,time
from PIL import Image,ImageDraw
torch.set_num_threads(2);torch.manual_seed(32032);random.seed(32032)
r=Path(__file__).resolve().parents[1];out=r/'assets/pixel-v2';out.mkdir(exist_ok=True)
S=128;H=96;DARK=(39,45,58,255)
def draw(kind,color,opened):
 im=Image.new('RGBA',(S,S));p=ImageDraw.Draw(im);c=tuple(color)+(255,);light=tuple(min(255,v+35) for v in color)+(255,)
 def rect(box,fill,width=3):p.rectangle(box,fill=fill,outline=DARK,width=width)
 if kind==0:
  rect((24,7,105,122),(91,99,108,255));rect((29,12,100,118),(27,35,48,255))
  if opened:p.polygon([(30,13),(54,28),(54,113),(30,118)],fill=c,outline=DARK,width=3);p.ellipse((44,64,49,69),fill=(235,200,90,255))
  else:
   rect((30,13,99,118),c);rect((37,23,92,58),light,2);rect((37,70,92,108),light,2);p.ellipse((84,62,91,69),fill=(236,198,79,255),outline=DARK)
 elif kind==1:
  rect((8,48,120,66),c);rect((17,65,28,120),light);rect((101,65,112,120),light)
 elif kind==2:
  rect((33,14,93,69),c);rect((29,70,98,84),light);rect((33,82,43,122),c);rect((84,82,94,122),c)
 elif kind==3:
  rect((18,29,112,116),c);rect((25,36,105,109),light);p.line((25,36,105,109),fill=DARK,width=5);p.line((105,36,25,109),fill=DARK,width=5)
 else:
  rect((59,43,69,115),c);p.ellipse((29,110,100,124),fill=light,outline=DARK,width=3);p.polygon([(44,13),(85,13),(106,56),(23,56)],fill=(246,217,126,255),outline=DARK,width=3)
 return np.asarray(im,dtype='f')/255
y,x=np.meshgrid((np.arange(S)+.5)/S*2-1,(np.arange(S)+.5)/S*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');p=[xy]
for freq in [1,2,4,8,16]:p += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(p,1));colors=[(143,104,75),(99,137,178),(172,119,87),(117,156,121),(160,159,151),(210,185,125)]
rows=[]
for kind in range(5):
 for opened in [0,1]:
  for color in colors:rows.append((np.r_[np.eye(5)[kind],np.array(color)/255,opened].astype('f'),draw(kind,color,opened)))
C=torch.tensor(np.array([c for c,a in rows]));IM=torch.tensor(np.array([a.reshape(-1,4) for c,a in rows]))
class Net(nn.Module):
 def __init__(self):super().__init__();self.layers=nn.ModuleList([nn.Linear(31,H),nn.Linear(H,H),nn.Linear(H,H),nn.Linear(H,4)])
 def forward(self,x):
  for i,l in enumerate(self.layers):x=l(x);x=F.relu(x) if i<3 else x
  return x
net=Net();opt=torch.optim.Adam(net.parameters(),lr=.0015);start=time.time()
for step in range(1,6001):
 if step==4000:opt.param_groups[0]['lr']=.0004
 ix=torch.randint(len(C),(2048,));pix=torch.randint(S*S,(2048,));target=IM[ix,pix];pred=net(torch.cat([P[pix],C[ix]],1))
 loss=F.binary_cross_entropy_with_logits(pred[:,3],target[:,3])+4*(((torch.sigmoid(pred[:,:3])-target[:,:3])**2)*target[:,3,None]).mean()
 opt.zero_grad();loss.backward();opt.step()
 if step%1000==0:print(step,float(loss),'seconds',round(time.time()-start),flush=True)
with (out/'props.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42505831,31,H,4,S))
 for l in net.layers:f.write(l.weight.detach().T.numpy().astype('>f4').tobytes());f.write(l.bias.detach().numpy().astype('>f4').tobytes())
bank=out/'props';bank.mkdir(exist_ok=True);sheet=Image.new('RGB',(5*128,2*128),(221,228,232))
with torch.no_grad():
 for kind in range(5):
  for opened in [0,1]:
   color=colors[0];c=torch.tensor(np.r_[np.eye(5)[kind],np.array(color)/255,opened].astype('f'));a=torch.sigmoid(net(torch.cat([P,c.expand(S*S,-1)],1))).numpy().reshape(S,S,4);a[:,:,3][a[:,:,3]<.1]=0;im=Image.fromarray(np.floor(a*255+.5).clip(0,255).astype('uint8'),'RGBA');im.save(bank/f'{kind}-{opened}.png');sheet.paste(im,(kind*128,opened*128),im)
sheet.save(r.parent/'props-v2-inspection.png')
(out/'props-card.json').write_text(json.dumps({'name':'Banana Pixel 2','component':'objects','external_weights':False,'parameters':sum(p.numel() for p in net.parameters()),'training_images':len(rows),'types':['puerta','mesa','silla','caja','lampara'],'size':S,'limitations':'Synthetic object shapes and open/closed door state. Not a universal image generator.'},indent=2))
