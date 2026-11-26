"""Banana Pose1: campo neuronal de deformacion inversa. No cambia pesos Pixel1.
Aprende flujo de poses sinteticas propias. No reconstruye superficies 3D ocultas.
"""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import numpy as np,struct,json,time
from pathlib import Path
root=Path(__file__).resolve().parents[1];out=root/'assets/pose';out.mkdir(parents=True,exist_ok=True)
torch.manual_seed(23023);torch.set_num_threads(2)
D,H=28,48
class Pose(nn.Module):
 def __init__(self):super().__init__();self.a=nn.Linear(D,H);self.b=nn.Linear(H,H);self.c=nn.Linear(H,2)
 def forward(self,x):return self.c(F.relu(self.b(F.relu(self.a(x)))))
def features(xy,kind,pose):
 pieces=[xy]
 for f in [1,2,4,8]:pieces += [torch.sin(xy*torch.pi*f),torch.cos(xy*torch.pi*f)]
 return torch.cat(pieces+[F.one_hot(kind,5).float(),F.one_hot(pose,5).float()],1)
def target(xy,kind,pose):
 x,y=xy[:,0],xy[:,1];u=x.clone();v=y.clone();leg=torch.clamp((y-.48)/.46,0,1);side=torch.tanh(x*10)
 direction=torch.where(pose==1,1.,torch.where(pose==2,-1.,0.));u+=direction*side*leg*.13;v+=direction*side*leg*.08
 # The raised arm is a learned inverse rotational flow in a localized region.
 angle=.8;dx=x-.32;dy=y-.22;ru=dx*torch.cos(torch.tensor(angle))-dy*torch.sin(torch.tensor(angle))+.32;rv=dx*torch.sin(torch.tensor(angle))+dy*torch.cos(torch.tensor(angle))+.22
 mask=torch.exp(-((x-.48)**2/.13+(y-.04)**2/.20))*(pose==3);u=u*(1-mask)+ru*mask;v=v*(1-mask)+rv*mask
 head=torch.exp(-((y+.15)**2/.45));turn=(pose==4).float();u+=turn*(x*.25+head*.065)
 return torch.stack([u,v],1)
net=Pose();opt=torch.optim.Adam(net.parameters(),lr=.0015);start=time.time()
for step in range(1,2201):
 xy=torch.rand(4096,2)*2-1;kind=torch.randint(5,(4096,));pose=torch.randint(5,(4096,));loss=F.mse_loss(net(features(xy,kind,pose)),target(xy,kind,pose));opt.zero_grad();loss.backward();opt.step()
 if step%400==0:print(step,round(loss.item(),6),'seconds',round(time.time()-start),flush=True)
net.eval();xy=torch.rand(10000,2)*2-1;kind=torch.randint(5,(10000,));pose=torch.randint(5,(10000,));
with torch.no_grad():mae=float((net(features(xy,kind,pose))-target(xy,kind,pose)).abs().mean())
with (out/'pose1.bin').open('wb') as f:
 f.write(struct.pack('>3i',0x42504f31,D,H))
 for layer in [net.a,net.b,net.c]:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
card={'name':'Banana Pose1','parameters':sum(p.numel() for p in net.parameters()),'external_weights':False,'architecture':'coordinate MLP predicting inverse UV flow','poses':['quieto','paso A','paso B','saludo','giro lateral aproximado'],'heldout_coordinate_MAE':mae,'limitations':'Learns deformations, resamples the original neural character; not full pose synthesis or a reconstructed 3D side view. Preserves the original blur and texture.'};(out/'model-card.json').write_text(json.dumps(card,indent=2));print(json.dumps(card,indent=2))
with (root/'model-source/pose-reference.bin').open('wb') as f:
 inp=features(torch.tensor([[-.6,.8],[.5,.2],[0.,0.],[.2,.8]]),torch.tensor([1,1,2,0]),torch.tensor([1,3,4,2]));
 with torch.no_grad():val=net(inp)
 f.write(struct.pack('>2i',4,D))
 for a,b in zip(inp,val):f.write(a.numpy().astype('>f4').tobytes());f.write(b.numpy().astype('>f4').tobytes())
