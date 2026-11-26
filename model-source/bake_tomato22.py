"""Only outputs from Banana's own serialized model, with explicit regional compositing."""
from pathlib import Path
import torch,numpy as np,struct,json,hashlib,time
from PIL import Image,ImageFilter
torch.set_num_threads(2);r=Path(__file__).resolve().parents[1];path=r/'assets/pixel-v2/tomato22.bin'
with path.open('rb') as f:
 magic,inputs,h,out,side=struct.unpack('>5i',f.read(20));layers=[]
 for i in range(4):
  n=inputs if i==0 else h;m=4 if i==3 else h
  layers.append((torch.tensor(np.frombuffer(f.read(n*m*4),dtype='>f4').astype('f').reshape(n,m)),torch.tensor(np.frombuffer(f.read(m*4),dtype='>f4').astype('f'))))
y,x=np.meshgrid((np.arange(side)+.5)/side*2-1,(np.arange(side)+.5)/side*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');ps=[xy]
for freq in [1,2,4,8,16]:ps += [np.sin(xy*np.pi*freq),np.cos(xy*np.pi*freq)]
P=torch.tensor(np.concatenate(ps,1));bank=r/'assets/pixel-v2/bank';bank.mkdir(exist_ok=True)
palette=[0xffd66363,0xff629cdb,0xff72b783,0xffeaca63,0xff42495a,0xffece5dd,0xffe8a3bf,0xffa78ac7,0xffdd9b56,0xff9298a4,0xffa47858]
defaults=[0xfff8d061,0xff63bc76,0xffdb9b63,0xfff3dce8,0xff8cc8df,0xffb88770,0xffc49161,0xffe99a56,0xfff1c79f,0xffed6569]
face=[(35,43),(32,32),(32,27),(32,29),(32,26),(32,27),(32,27),(32,27),(32,26),(32,33)];haty=[22,19,17,18,16,17,17,17,13,20]
yy,xx=np.meshgrid((np.arange(side)+.5)/side*64,(np.arange(side)+.5)/side*64,indexing='ij')
def render(kind,color,acc,expr,view=0):
 c=np.zeros(15,dtype='f');c[:3]=[(color>>16&255)/255,(color>>8&255)/255,(color&255)/255];c[3+acc]=1;c[9+expr]=1;c[12+view]=1
 with torch.no_grad():
  a=torch.cat([P,torch.tensor(c).expand(side*side,-1)],1)
  for i,(w,b) in enumerate(layers):a=a@w+b;a=torch.relu(a) if i<3 else torch.sigmoid(a)
 a=a.numpy().reshape(side,side,4);a[:,:,3][a[:,:,3]<.1]=0
 raw=np.floor(a*255+.5).clip(0,255).astype('uint8')
 # Explicit palette cleanup of the network prediction, not a replacement drawing.
 rgb=np.asarray(Image.fromarray(raw[:,:,:3]).filter(ImageFilter.MedianFilter(3)),dtype='f')
 base=np.array([(color>>16)&255,(color>>8)&255,color&255],dtype='f');light=base*.65+255*.35
 colors=[base,light,[55,48,63],[249,249,245],[51,55,68],[101,61,76]]
 if kind==0:colors+=[[131,95,65]]
 if kind==9:colors+=[[71,142,88]]
 if kind==3:colors+=[[229,160,185]]
 if kind==4:colors+=[[223,121,114]]
 if kind==5:colors+=[base*.65]
 if kind==8:colors+=[[235,189,143],[79,61,57]]
 if acc in [1,4]:colors+=[[143,104,77],[97,73,81]]
 if acc in [3,4]:colors+=[[127,99,73],[183,151,107],[97,77,63]]
 if acc==2:colors+=[[235,193,80]]
 palette=np.array(colors,dtype='f');dist=((rgb[:,:,None,:]-palette[None,None,:,:])**2).sum(-1);clean=palette[dist.argmin(-1)].clip(0,255).astype('uint8')
 alpha=np.asarray(Image.fromarray(raw[:,:,3]).filter(ImageFilter.MedianFilter(3)))
 alpha=np.where(alpha<95,0,np.where(alpha>165,255,alpha)).astype('uint8')
 return np.concatenate([clean,alpha[:,:,None]],2)
count=0;start=time.time();sheet=Image.new('RGB',(128,3*128),(223,230,234));front=Image.new('RGB',(128,3*128),(223,230,234))
for kind in [9]:
 for view in range(3):
  fx,fy=face[kind];fx=32+(fx-32)*(.78 if view==1 else 1)
  fm=((xx-fx)/(13*(.78 if view==1 else 1)))**2+((yy-fy)/10)**2<=1
  if view==2:fm=np.zeros_like(fm)
  for color in palette+[defaults[kind]]:
   base=render(kind,color,0,0,view);faces=[base,render(kind,color,0,1,view),render(kind,color,0,2,view)] if view==0 else [base]*3
   for acc in range(6):
    variant=base if acc==0 else render(kind,color,acc,0,view);mask=np.zeros((side,side),bool)
    if acc in [1,2,4]:mask |= yy<=haty[kind]+4
    if acc in [3,4]:mask |= (yy>=36)&(yy<=57)&(xx>=18)&(xx<=48)
    mask &= ~fm
    if acc==5 and view!=2:mask |= (np.abs(xx-fx)<=14)&(np.abs(yy-fy)<=6)
    for expr in (range(3) if view==0 else [0]):
     a=base.copy();a[mask]=variant[mask]
     if expr>0:a[fm]=faces[expr][fm]
     im=Image.fromarray(a,'RGBA');suffix='' if view==0 else '-v'+str(view);im.save(bank/f'k{kind}-{color:08x}-{acc}-{expr}{suffix}.webp',lossless=True,method=3);count+=1
     if color==defaults[kind] and acc==4 and expr==0:sheet.paste(im,(0,view*128),im)
     if color==defaults[kind] and view==0 and expr==0 and acc in [0,3,4]:front.paste(im,(0,[0,3,4].index(acc)*128),im)
 print('type',kind,'presets',count,'seconds',round(time.time()-start),flush=True)
sheet.save(r.parent/'tomato22-views-inspection.png');front.save(r.parent/'tomato22-clean-inspection.png')
(r/'assets/pixel22/tomato-bank-provenance.json').write_text(json.dumps({'external_weights':False,'model_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'presets':count,'views':['front','profile','back'],'local_edit':'Neural variants composited by fixed regions. Median/palette cleanup of neural pixels, not universal inpainting.','encoding':'lossless WebP'},indent=2))
