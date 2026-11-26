"""Precompute actual trained network outputs, never teacher drawings, for common presets."""
from pathlib import Path
import numpy as np,torch,struct,json,hashlib
from PIL import Image
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
pixelpath=r/'assets/pixels2/pixel2.bin';layers,side,inputs=load(pixelpath);p=coords(side,[1,2,4,8,16]);out=r/'assets/pixels2/bank';out.mkdir(exist_ok=True)
palette=[0xffd66363,0xff629cdb,0xff72b783,0xffeaca63,0xff42495a,0xffece5dd,0xffe8a3bf,0xffa78ac7,0xffdd9b56,0xff9298a4,0xffa47858]
defaults=[0xfff8d061,0xff63bc76,0xffdb9b63,0xfff3dce8,0xff8cc8df];count=0
for kind in range(5):
 for color in palette+[defaults[kind]]:
  for access in range(3):
   for expr in range(3):
    c=np.zeros(16,dtype='f');c[kind]=1;c[5:8]=[(color>>16&255)/255,(color>>8&255)/255,(color&255)/255];c[8+access]=1;c[11+expr]=1
    a=render(layers,p,c,side);a[:,:,3][a[:,:,3]<.1]=0
    Image.fromarray(np.floor(a*255+.5).clip(0,255).astype('uint8'),'RGBA').save(out/f'k{kind}-{color:08x}-{access}-{expr}.png');count+=1
print('sprite presets',count,flush=True)
bgpath=r/'assets/background/background1.bin';layers,side,inputs=load(bgpath);p=coords(side,[1,2,4,8]);out=r/'assets/background/bank';out.mkdir(exist_ok=True)
# java.util.Random(0).nextFloat(), twice, mapped into [-1,1].
seed=(0^0x5deece66d)&((1<<48)-1);z=[]
for _ in range(2):
 seed=(seed*0x5deece66d+0xb)&((1<<48)-1);z.append((seed>>(48-24))/(1<<24)*2-1)
bc=0
for kind in range(5):
 for light,night in enumerate([.35,.25,.9]):
  for tree in range(2):
   for phase in range(2):
    c=np.r_[np.eye(5)[kind],night,z,-.7 if phase==0 else .7,tree].astype('f');a=render(layers,p,c,side)
    im=Image.fromarray(np.floor(a*255+.5).clip(0,255).astype('uint8'),'RGBA');im.save(out/f'k{kind}-{light}-{tree}-{phase}.png');im.convert('RGB').save(out/f'k{kind}-{light}-{tree}-{phase}-rgb.png');im.getchannel('A').convert('RGB').save(out/f'k{kind}-{light}-{tree}-{phase}-mask.png');bc+=1
meta={'source':'Serialized neural weights evaluated offline; not teacher drawings or a procedural fallback','sprite_presets':count,'background_presets_seed0':bc,'pixel2_sha256':hashlib.sha256(pixelpath.read_bytes()).hexdigest(),'background_sha256':hashlib.sha256(bgpath.read_bytes()).hexdigest()}
(r/'model-source/preset-provenance.json').write_text(json.dumps(meta,indent=2));print(meta,flush=True)
