"""Own small image recognizer trained on Banana Pixel 2 outputs only."""
from pathlib import Path
import torch,torch.nn as nn,torch.nn.functional as F,numpy as np,random,struct,json
from PIL import Image
r=Path(__file__).resolve().parents[1];torch.set_num_threads(2);torch.manual_seed(3232);random.seed(3232)
train=[];test=[]
for p in sorted((r/'assets/pixel-v2/bank').glob('*.webp')):
 parts=p.stem.split('-');kind=int(parts[0][1:]);acc=int(parts[2]);expr=int(parts[3])
 im=np.asarray(Image.open(p).convert('RGBA').resize((32,32),Image.Resampling.BILINEAR),dtype='f')/255;im[:,:,:3]*=im[:,:,3,None];row=(im.transpose(2,0,1),kind)
 # Hold out one entire color/accessory composition for every type.
 (test if parts[1]=='ff629cdb' and acc in [1,3,4] else train).append(row)
X=torch.tensor(np.array([x for x,y in train]));Y=torch.tensor([y for x,y in train]);TX=torch.tensor(np.array([x for x,y in test]));TY=torch.tensor([y for x,y in test])
class Vision(nn.Module):
 def __init__(self):super().__init__();self.a=nn.Conv2d(4,8,3,padding=1);self.b=nn.Conv2d(8,12,3,padding=1);self.c=nn.Linear(12*8*8,64);self.d=nn.Linear(64,9)
 def forward(self,x):
  x=F.avg_pool2d(F.relu(self.a(x)),2);x=F.avg_pool2d(F.relu(self.b(x)),2);return self.d(F.relu(self.c(x.flatten(1))))
net=Vision();opt=torch.optim.Adam(net.parameters(),lr=.001)
for step in range(1,1001):
 ix=torch.randint(len(X),(48,));batch=X[ix].clone();batch[:,:3]*=torch.rand(len(ix),1,1,1)*.25+.85
 logits=net(batch);loss=F.cross_entropy(logits,Y[ix]);opt.zero_grad();loss.backward();opt.step()
 if step%200==0:print(step,float(loss),flush=True)
net.eval()
with torch.no_grad():accuracy=float((net(TX).argmax(1)==TY).float().mean())
out=r/'assets/pixel-v2/vision.bin'
with out.open('wb') as f:
 f.write(struct.pack('>5i',0x42505632,32,8,12,64))
 for layer in [net.a,net.b,net.c,net.d]:f.write(layer.weight.detach().numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
with (r/'model-source/vision32-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',3,4*32*32))
 with torch.no_grad():
  for x in TX[:3]:f.write(x.numpy().astype('>f4').tobytes());f.write(net(x[None])[0].numpy().astype('>f4').tobytes())
(r/'assets/pixel-v2/vision-card.json').write_text(json.dumps({'name':'Banana Pixel 2','component':'reference recognizer','external_weights':False,'training_images':len(X),'heldout_images':len(TX),'heldout_accuracy_synthetic_only':accuracy,'classes':['banana','pepino','gato','conejo','robot','perro','oso','zorro','persona'],'limitations':'Recognizes the own synthetic catalog only; not general image understanding or reliable classification of real photos.'},indent=2));print('synthetic accuracy',accuracy,flush=True)
