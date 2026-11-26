"""Precompute actual trained network outputs, never teacher drawings, for common presets."""
from pathlib import Path
import numpy as np,torch,struct,json,hashlib
from PIL import Image,ImageFilter
r=Path(__file__).resolve().parents[1];torch.set_num_threads(2)
def load(path):
 with path.open('rb') as f:
  magic,inputs,h,outputs,side=struct.unpack('>5i',f.read(20));layers=[]
  for i in range(4):
   n=inputs if i==0 else h;m=outputs if i==3 else h
   w=torch.tensor(np.frombuffer(f.read(n*m*4),dtype='>f4').astype('f').reshape(n,m));b=torch.tensor(np.frombuffer(f.read(m*4),dtype='>f4').astype('f'));layers.append((w,b))
 return layers,side,inputs
def coords(side,freqs):
 y,x=np.meshgrid((np.arange(side)+.5)/side*2-1,(np.arange(side)+.5)/side*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');p=[xy]
 for freq in freqs:p += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
 return torch.tensor(np.concatenate(p,1))
def render(layers,p,c,side):
 with torch.no_grad():
  x=torch.cat([p,torch.tensor(c,dtype=torch.float32).expand(side*side,-1)],1)
  for i,(w,b) in enumerate(layers):x=x@w+b;x=torch.relu(x) if i<3 else torch.sigmoid(x)
 return x.numpy().reshape(side,side,-1)
bgpath=r/'assets/background/background22.bin';layers,side,inputs=load(bgpath);p=coords(side,[1,2,4,8,16]);out=r/'assets/background/bank22';out.mkdir(exist_ok=True)
seed=(0^0x5deece66d)&((1<<48)-1);z=[]
for _ in range(2):
 seed=(seed*0x5deece66d+0xb)&((1<<48)-1);z.append((seed>>(48-24))/(1<<24)*2-1)
count=0
for kind in range(5):
 for light,night in enumerate([.35,.25,.9]):
  for tree in range(2):
   for phase in range(2):
    c=np.r_[np.eye(5)[kind],night,z,-.7 if phase==0 else .7,tree].astype('f');a=render(layers,p,c,side);rgb=Image.fromarray(np.floor(a[:,:,:3]*255+.5).clip(0,255).astype('uint8'),'RGB').filter(ImageFilter.UnsharpMask(radius=1.1,percent=130,threshold=2));alpha=Image.fromarray(np.where(a[:,:,3]>.5,255,0).astype('uint8'),'L');rgb.save(out/f'k{kind}-{light}-{tree}-{phase}-rgb.png');alpha.convert('RGB').save(out/f'k{kind}-{light}-{tree}-{phase}-mask.png');count+=1
(r/'assets/background/bank22-provenance.json').write_text(json.dumps({'external_weights':False,'model_sha256':hashlib.sha256(bgpath.read_bytes()).hexdigest(),'presets':count,'postprocessing':'Unsharp filter on own neural RGB; threshold of own neural foreground mask. Fog is explicit at runtime.'},indent=2));print('DONE_BACKGROUND_BANK',count,flush=True)
