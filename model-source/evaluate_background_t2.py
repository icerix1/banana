"""Same independent synthetic inputs for both versions; not a general image benchmark."""
from pathlib import Path
import ast,json,struct
import numpy as np
import torch
from PIL import Image,ImageDraw,ImageFilter
r=Path(__file__).resolve().parents[1]
scope=dict(np=np,Image=Image,ImageDraw=ImageDraw,ImageFilter=ImageFilter,S=96)
tree=ast.parse((r/'model-source/train_background_t2.py').read_text())
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name in ['draw','condition']],type_ignores=[]),'<targets>','exec'),scope)
def load(path):
 with open(path,'rb') as f:
  magic,inputs,h,outputs,side=struct.unpack('>5i',f.read(20));layers=[]
  for i in range(4):
   n=inputs if i==0 else h;m=4 if i==3 else h
   w=torch.tensor(np.frombuffer(f.read(n*m*4),dtype='>f4').astype('f').reshape(n,m))
   b=torch.tensor(np.frombuffer(f.read(m*4),dtype='>f4').astype('f'))
   layers.append((w,b))
 return layers
yy,xx=np.meshgrid((np.arange(96)+.5)/96*2-1,(np.arange(96)+.5)/96*2-1,indexing='ij')
xy=np.stack([xx.ravel(),yy.ravel()],1).astype('f');p=[xy]
for freq in [1,2,4,8]:p += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
p=torch.tensor(np.concatenate(p,1))
torch.set_num_threads(2);rng=np.random.default_rng(251125)
models=[load(r/'model-source/fondo1-baseline/background1.bin'),load(r/'assets/background/background1.bin')]
errors=[[],[]]
with torch.no_grad():
 for k in range(30):
  kind=k%5;z=rng.uniform(-1,1,2);night=float(rng.uniform(.25,1));phase=float(rng.uniform(-1,1));tree=k%2
  c=scope['condition'](kind,night,z,phase,tree);target=scope['draw'](kind,night,z,phase,tree)
  for i,layers in enumerate(models):
   x=torch.cat([p,torch.tensor(c).expand(96*96,-1)],1)
   for j,(w,b) in enumerate(layers):x=x@w+b;x=torch.relu(x) if j<3 else torch.sigmoid(x)
   pred=x.numpy().reshape(96,96,4)
   errors[i].append(float(np.abs(pred[:,:,:3]-target[:,:,:3]).mean()))
report={'evaluation':'30 independent synthetic backgrounds, same target generator and inputs for both versions; not perceptual quality','fondo1_rgb_mae':float(np.mean(errors[0])),'fondo2_rgb_mae':float(np.mean(errors[1]))}
(r/'model-source/background-comparison.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
