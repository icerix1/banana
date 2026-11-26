"""Own compositional creature-mask field. No external images, weights or teacher."""
import os
os.environ['OMP_NUM_THREADS']='2';os.environ['MKL_NUM_THREADS']='2'
import torch,torch.nn as nn,torch.nn.functional as F
import numpy as np,random,math,json,struct,time
from PIL import Image,ImageDraw
from pathlib import Path
r=Path(__file__).resolve().parents[1];out=r/'assets/custom23';out.mkdir(parents=True,exist_ok=True);(out/'bank').mkdir(exist_ok=True)
random.seed(23038);torch.manual_seed(23038);torch.set_num_threads(2);S=64
# Original masks:0 transparent,1 body,2 contour,3 isolated limbs.
def target(shape,ears,tail,width,height,view):
 im=Image.new('L',(S,S));d=ImageDraw.Draw(im);sx=(.72 if view==1 else 1)*width;sy=height;cx,cy=32,35
 def box(x1,y1,x2,y2):return [cx+(x1-cx)*sx,cy+(y1-cy)*sy,cx+(x2-cx)*sx,cy+(y2-cy)*sy]
 def poly(points):return [(cx+(x-cx)*sx,cy+(y-cy)*sy) for x,y in points]
 if tail:d.line(poly([(44,46),(53,45),(57,38 if tail==2 else 45)]),fill=2,width=3)
 for a in [(23,39,15,43),(43,39,50,43),(27,51,25,60),(38,51,40,60)]:d.line(a,fill=3,width=2)
 if ears==1:
  for x in [21,43]:d.ellipse(box(x-5,15,x+5,26),fill=1,outline=2,width=2)
 if ears==2:
  for x in [23,41]:d.ellipse(box(x-4,5,x+4,27),fill=1,outline=2,width=2)
 if ears==3:
  for x in [22,42]:d.polygon(poly([(x-6,25),(x,10),(x+6,25)]),fill=1);d.line(poly([(x-6,25),(x,10),(x+6,25)]),fill=2,width=2)
 if shape==0:d.ellipse(box(15,19,49,53),fill=1,outline=2,width=2)
 if shape==1:d.ellipse(box(18,15,46,54),fill=1,outline=2,width=2)
 if shape==2:d.rounded_rectangle(box(16,19,48,53),radius=4,fill=1,outline=2,width=2)
 if shape==3:
  points=[(32+((20 if i%2==0 else 13)*math.sin(i*math.pi/5)),35-((20 if i%2==0 else 13)*math.cos(i*math.pi/5))) for i in range(10)];points=poly(points);d.polygon(points,fill=1);d.line(points+[points[0]],fill=2,width=2)
 if shape==4:
  points=poly([(32,14),(46,34),(48,43),(43,51),(32,55),(21,51),(16,43),(18,34)]);d.polygon(points,fill=1);d.line(points+[points[0]],fill=2,width=2)
 if shape==5:d.rounded_rectangle(box(19,14,45,55),radius=12,fill=1,outline=2,width=2)
 return np.asarray(im).copy()
def condition(row):
 shape,ears,tail,w,h,view=row;c=np.zeros(18,dtype=np.float32);c[shape]=1;c[6+ears]=1;c[10+tail]=1;c[13]=(w-1)*3;c[14]=(h-1)*3;c[15+view]=1;return c
rows=[(s,e,t,w,h,v) for s in range(6) for e in range(4) for t in range(3) for w in [.85,1,1.15] for h in [.9,1.1] for v in range(3)];random.shuffle(rows);test=rows[:180];train=rows[180:]
def coords(xy):
 p=[xy[:,0],xy[:,1]]
 for f in [1,2,4]:p += [torch.sin(xy[:,0]*math.pi*f),torch.sin(xy[:,1]*math.pi*f),torch.cos(xy[:,0]*math.pi*f),torch.cos(xy[:,1]*math.pi*f)]
 return torch.stack(p,1)
C=torch.tensor(np.stack([condition(x) for x in train]));Y=torch.tensor(np.stack([target(*x) for x in train]),dtype=torch.long)
net=nn.Sequential(nn.Linear(32,64),nn.ReLU(),nn.Linear(64,64),nn.ReLU(),nn.Linear(64,64),nn.ReLU(),nn.Linear(64,4));opt=torch.optim.AdamW(net.parameters(),lr=.002)
start=time.time()
for step in range(1,8001):
 ix=torch.randint(len(train),(4,));xy=torch.rand(4,256,2)*2-1;pix=((xy+1)*.5*S).long().clamp(0,S-1);labels=Y[ix[:,None],pix[:,:,1],pix[:,:,0]].reshape(-1);x=torch.cat([coords(xy.reshape(-1,2)),C[ix].repeat_interleave(256,0)],1);loss=F.cross_entropy(net(x),labels,weight=torch.tensor([.45,1.,1.5,1.5]));opt.zero_grad();loss.backward();opt.step()
 if step==6000:opt.param_groups[0]['lr']=.0006
 if step%1000==0:print(step,round(loss.item(),4),round(time.time()-start),flush=True)
net.eval();grid=torch.tensor([[(x+.5)/S*2-1,(y+.5)/S*2-1] for y in range(S) for x in range(S)]);features=coords(grid)
@torch.no_grad()
def infer(row):return net(torch.cat([features,torch.tensor(condition(row))[None].repeat(S*S,1)],1)).argmax(1).reshape(S,S).numpy().astype('uint8')
ious=[]
for row in test:
 a=infer(row)>0;b=target(*row)>0;ious.append(float((a&b).sum()/max(1,(a|b).sum())))
with (out/'custom23.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42505831,32,64,4,64))
 for layer in [net[0],net[2],net[4],net[6]]:f.write(layer.weight.detach().T.numpy().astype('>f4').tobytes());f.write(layer.bias.detach().numpy().astype('>f4').tobytes())
for shape in range(6):
 for ears in range(4):
  for tail in range(3):
   for view in range(3):Image.fromarray(infer((shape,ears,tail,1,1,view))*85).save(out/'bank'/f's{shape}-e{ears}-t{tail}-v{view}.png')
with (r/'model-source/custom23-reference.bin').open('wb') as f:
 inputs=torch.cat([features[:3],C[:3]],1);outputs=torch.sigmoid(net(inputs)).detach();f.write(struct.pack('>2i',3,32))
 for x,y in zip(inputs,outputs):f.write(x.numpy().astype('>f4').tobytes());f.write(y.numpy().astype('>f4').tobytes())
card={'name':'Banana Pixel2.3','component':'compositional creature mask field','external_weights':False,'external_teacher':False,'parameters':sum(p.numel() for p in net.parameters()),'train_conditions':len(train),'heldout_conditions':len(test),'heldout_foreground_IoU':float(np.mean(ious)),'features':['6 body forms','4 ear styles','3 tail styles','width/height','3 views'],'limitations':'Finite own compositional style,64px masks; own existing learned faces/accessories composed after mask. Not universal text-to-image.'};(out/'model-card.json').write_text(json.dumps(card,indent=2));assert card['heldout_foreground_IoU']>.8,card
sheet=Image.new('RGB',(6*128,4*128),'#dae2e8')
for e in range(4):
 for sh in range(6):
  a=infer((sh,e,2,1,1,0));rgba=np.zeros((64,64,4),dtype='uint8');rgba[a==1]=[120,177,222,255];rgba[a==2]=[53,43,64,255];rgba[a==3]=[53,43,64,255];im=Image.fromarray(rgba).resize((128,128));sheet.paste(im,(sh*128,e*128),im)
sheet.save(r.parent/'custom23-inspection.png');print(json.dumps(card),flush=True)
