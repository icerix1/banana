"""Only outputs from Banana's own serialized model, with explicit regional compositing."""
from pathlib import Path
import torch,numpy as np,struct,json,hashlib,time
from PIL import Image
torch.set_num_threads(2);r=Path(__file__).resolve().parents[1];path=r/'assets/pixel-v2/character.bin'
with path.open('rb') as f:
 magic,inputs,h,out,side=struct.unpack('>5i',f.read(20));layers=[]
 for i in range(4):
  n=inputs if i==0 else h;m=4 if i==3 else h
  layers.append((torch.tensor(np.frombuffer(f.read(n*m*4),dtype='>f4').astype('f').reshape(n,m)),torch.tensor(np.frombuffer(f.read(m*4),dtype='>f4').astype('f'))))
y,x=np.meshgrid((np.arange(side)+.5)/side*2-1,(np.arange(side)+.5)/side*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');ps=[xy]
for freq in [1,2,4,8,16]:ps += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(ps,1));bank=r/'assets/pixel-v2/bank';bank.mkdir(exist_ok=True)
palette=[0xffd66363,0xff629cdb,0xff72b783,0xffeaca63,0xff42495a,0xffece5dd,0xffe8a3bf,0xffa78ac7,0xffdd9b56,0xff9298a4,0xffa47858]
defaults=[0xfff8d061,0xff63bc76,0xffdb9b63,0xfff3dce8,0xff8cc8df,0xffb88770,0xffc49161,0xffe99a56,0xfff1c79f]
face=[(35,43),(32,32),(32,27),(32,29),(32,26),(32,27),(32,27),(32,27),(32,26)];haty=[22,19,17,18,16,17,17,17,13]
yy,xx=np.meshgrid((np.arange(side)+.5)/side*64,(np.arange(side)+.5)/side*64,indexing='ij')
def render(kind,color,acc,expr):
 c=np.zeros(23,dtype='f');c[kind]=1;c[9:12]=[(color>>16&255)/255,(color>>8&255)/255,(color&255)/255];c[12+acc]=1;c[18+expr]=1
 with torch.no_grad():
  a=torch.cat([P,torch.tensor(c).expand(side*side,-1)],1)
  for i,(w,b) in enumerate(layers):a=a@w+b;a=torch.relu(a) if i<3 else torch.sigmoid(a)
 a=a.numpy().reshape(side,side,4);a[:,:,3][a[:,:,3]<.1]=0
 return np.floor(a*255+.5).clip(0,255).astype('uint8')
count=0;start=time.time();sheet=Image.new('RGB',(9*128,3*128),(223,230,234))
for kind in range(9):
 fx,fy=face[kind];fm=((xx-fx)/13)**2+((yy-fy)/10)**2<=1
 for color in palette+[defaults[kind]]:
  base=render(kind,color,0,0);faces=[base,render(kind,color,0,1),render(kind,color,0,2)]
  for acc in range(6):
   variant=base if acc==0 else render(kind,color,acc,0);mask=np.zeros((side,side),bool)
   if acc in [1,2,4]:mask |= yy<=haty[kind]+4
   if acc in [3,4]:mask |= (yy>=36)&(yy<=57)&(xx>=18)&(xx<=48)
   mask &= ~fm
   if acc==5:mask |= (np.abs(xx-fx)<=14)&(np.abs(yy-fy)<=6)
   for expr in range(3):
    a=base.copy();a[mask]=variant[mask]
    if expr>0:a[fm]=faces[expr][fm]
    im=Image.fromarray(a,'RGBA');im.save(bank/f'k{kind}-{color:08x}-{acc}-{expr}.webp',lossless=True,method=3);count+=1
    if color==defaults[kind] and expr==0 and acc in [0,3,4]:sheet.paste(im,(kind*128,[0,3,4].index(acc)*128),im)
 print('type',kind,'presets',count,'seconds',round(time.time()-start),flush=True)
sheet.save(r.parent/'pixel-v2-stable-inspection.png')
(r/'assets/pixel-v2/preset-provenance.json').write_text(json.dumps({'external_weights':False,'model_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'presets':count,'local_edit':'Fixed face and body regions composite neural variants, preserving outside-region pixels. Not arbitrary inpainting.','encoding':'lossless WebP'},indent=2))
