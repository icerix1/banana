"""Own small image recognizer trained on Banana Pixel 2 outputs only."""
from pathlib import Path
import torch,torch.nn as nn,torch.nn.functional as F,numpy as np,random,struct,json
from PIL import Image
r=Path(__file__).resolve().parents[1];torch.set_num_threads(2);torch.manual_seed(3232);random.seed(3232)
centers=[(35,43),(32,32),(32,27),(32,29),(32,26),(32,27),(32,27),(32,27),(32,26),(32,33)]
def anchored(path,eyes=0):
 parts=path.stem.split('-');kind=int(parts[0][1:]);acc=int(parts[2]);expr=int(parts[3]);view=int(parts[4][1:]) if len(parts)>4 else 0;base=Image.open(path).convert('RGBA')
 if view==2:return base
 back=Image.open(path.with_name(f'k{kind}-{parts[1]}-0-0-v2.webp')).convert('RGBA')
 if view==1:back=back.resize((100,128),Image.Resampling.BILINEAR);fit=Image.new('RGBA',(128,128));fit.paste(back,(14,0));back=fit
 a=np.array(base);fill=np.array(back);fx,fy=centers[kind];width=26
 if view==1:fx=32+(fx-32)*.78;width*=.78
 yy,xx=np.meshgrid((np.arange(128)+.5)/2,(np.arange(128)+.5)/2,indexing='ij');mask=(np.abs(xx-fx)<=width/2)&(yy>=fy-5)&(yy<=fy+14)&(a[:,:,3]>100)&(fill[:,:,3]>100);a[:,:,:3][mask]=fill[:,:,:3][mask];base=Image.fromarray(a,'RGBA');face=Image.open(r/f'assets/pixel22/faces/{kind}-{expr}-{view}-{eyes}-{1 if acc==5 else 0}.png').convert('RGBA');face=face.resize((round(width*2),48),Image.Resampling.BILINEAR);base.alpha_composite(face,(round((fx-width/2)*2),round((fy-8)*2)));return base
train=[];test=[]
for p in sorted((r/'assets/pixel-v2/bank').glob('*.webp')):
 parts=p.stem.split('-');kind=int(parts[0][1:]);acc=int(parts[2]);expr=int(parts[3])
 im=np.asarray(anchored(p,random.randrange(3)).resize((32,32),Image.Resampling.BILINEAR),dtype='f')/255;im[:,:,:3]*=im[:,:,3,None];row=(im.transpose(2,0,1),kind)
 # Hold out one entire color/accessory composition for every type.
 (test if parts[1]=='ff629cdb' and acc in [1,3,4] else train).append(row)
X=torch.tensor(np.array([x for x,y in train]));Y=torch.tensor([y for x,y in train]);TX=torch.tensor(np.array([x for x,y in test]));TY=torch.tensor([y for x,y in test])
class Vision(nn.Module):
 def __init__(self):super().__init__();self.a=nn.Conv2d(4,8,3,padding=1);self.b=nn.Conv2d(8,12,3,padding=1);self.c=nn.Linear(12*8*8,64);self.d=nn.Linear(64,10)
 def forward(self,x):
  x=F.avg_pool2d(F.relu(self.a(x)),2);x=F.avg_pool2d(F.relu(self.b(x)),2);return self.d(F.relu(self.c(x.flatten(1))))
net=Vision();opt=torch.optim.Adam(net.parameters(),lr=.001)
for step in range(1,1001):
 ix=torch.randint(len(X),(48,));batch=X[ix].clone();batch[:,:3]*=torch.rand(len(ix),1,1,1)*.25+.85
 logits=net(batch);loss=F.cross_entropy(logits,Y[ix]);opt.zero_grad();loss.backward();opt.step()
 if step%200==0:print(step,float(loss),flush=True)
net.eval()
with torch.no_grad():accuracy=float((net(TX).argmax(1)==TY).float().mean())
out=r/'assets/pixel22/vision22.bin'
with out.open('wb') as f:
 f.write(struct.pack('>6i',0x42505633,32,8,12,64,10))
 for layer in [net.a,net.b,net.c,net.d]:f.write(layer.weight.detach().numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
with (r/'model-source/vision22-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',3,4*32*32))
 with torch.no_grad():
  for x in TX[:3]:f.write(x.numpy().astype('>f4').tobytes());f.write(net(x[None])[0].numpy().astype('>f4').tobytes())
(r/'assets/pixel22/vision22-card.json').write_text(json.dumps({'name':'Banana Pixel 2.2','component':'reference recognizer','external_weights':False,'training_images':len(X),'heldout_images':len(TX),'heldout_accuracy_synthetic_only':accuracy,'classes':['banana','pepino','gato','conejo','robot','perro','oso','zorro','persona','tomate'],'limitations':'Recognizes the own synthetic catalog only; not general image understanding or reliable classification of real photos.'},indent=2));print('synthetic accuracy',accuracy,flush=True)

defaults=[0xfff8d061,0xff63bc76,0xffdb9b63,0xfff3dce8,0xff8cc8df,0xffb88770,0xffc49161,0xffe99a56,0xfff1c79f,0xffed6569];sheet=Image.new('RGB',(1280,384),(223,230,234))
for k,color in enumerate(defaults):
 for view in range(3):
  suffix='' if view==0 else '-v'+str(view);im=anchored(r/f'assets/pixel-v2/bank/k{k}-{color:08x}-4-0{suffix}.webp');sheet.paste(im,(k*128,view*128),im)
sheet.save(r.parent/'pixel22-catalog-inspection.png')
