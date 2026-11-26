"""Original facial anchor neural field: random weights, no external images/models."""
from pathlib import Path
import torch,torch.nn as nn,torch.nn.functional as F,numpy as np,random,struct,json,time
from PIL import Image,ImageDraw
r=Path(__file__).resolve().parents[1];out=r/'assets/pixel22';out.mkdir(exist_ok=True);torch.set_num_threads(2);torch.manual_seed(2234);random.seed(2234);S=64

def draw(kind,expression,view,eyes,glasses):
 im=Image.new('RGBA',(128,128));p=ImageDraw.Draw(im);dark=(55,48,63,255);white=(249,249,245,255);scale=[1,.8,1.22][eyes];centers=[43] if view else [18+(kind%3)*.5,46-(kind%3)*.5]
 for x in centers:
  rx=7*scale;ry=(8+(kind%2)*.5)*scale;p.ellipse(((x-rx)*2,(20-ry)*2,(x+rx)*2,(20+ry)*2),fill=white,outline=dark,width=3);p.ellipse(((x-3)*2,16*2,(x+3)*2,24*2),fill=dark)
  if glasses:p.rectangle(((x-rx-2)*2,(20-ry-2)*2,(x+rx+2)*2,(20+ry+2)*2),outline=dark,width=3)
 if glasses and not view:p.line((32*2,20*2,35*2,20*2),fill=dark,width=3)
 cx=39 if view else 32
 if expression==0:p.arc(((cx-9)*2,34*2,(cx+9)*2,48*2),0,180,fill=dark,width=4)
 elif expression==1:p.arc(((cx-9)*2,42*2,(cx+9)*2,56*2),180,360,fill=dark,width=4)
 else:p.ellipse(((cx-5)*2,39*2,(cx+5)*2,55*2),fill=(101,61,76,255),outline=dark,width=3)
 return np.asarray(im.resize((64,64),Image.Resampling.LANCZOS),dtype='f')/255

def condition(k,e,v,eyes,g):return np.r_[np.eye(10)[k],np.eye(3)[e],np.eye(2)[v],np.eye(3)[eyes],[g]].astype('f')
rows=[];test=[]
for k in range(10):
 for e in range(3):
  for v in range(2):
   for eyes in range(3):
    for g in range(2):
     row=(condition(k,e,v,eyes,g),draw(k,e,v,eyes,g));(test if k in [3,9] and e==2 and v==1 and eyes==2 else rows).append(row)
C=torch.tensor(np.array([c for c,a in rows]));IM=torch.tensor(np.array([a.reshape(-1,4) for c,a in rows]));y,x=np.meshgrid((np.arange(S)+.5)/S*2-1,(np.arange(S)+.5)/S*2-1,indexing='ij');xy=np.stack([x.ravel(),y.ravel()],1).astype('f');coords=[xy]
for f in [1,2,4,8,16]:coords += [np.sin(xy*np.pi*f),np.cos(xy*np.pi*f)]
P=torch.tensor(np.concatenate(coords,1));net=nn.Sequential(nn.Linear(41,128),nn.ReLU(),nn.Linear(128,128),nn.ReLU(),nn.Linear(128,128),nn.ReLU(),nn.Linear(128,4));opt=torch.optim.Adam(net.parameters(),lr=.0015);fg=[torch.where(a[:,3]>.5)[0] for a in IM];start=time.time()
for step in range(1,10001):
 if step==7500:opt.param_groups[0]['lr']=.0003
 i=random.randrange(len(C));points=torch.cat([torch.randint(S*S,(512,)),fg[i][torch.randint(len(fg[i]),(1024,))]]);pred=net(torch.cat([P[points],C[i].expand(len(points),-1)],1));target=IM[i,points];loss=F.binary_cross_entropy_with_logits(pred[:,3],target[:,3])+8*(torch.abs(torch.sigmoid(pred[:,:3])-target[:,:3])*target[:,3,None]).mean();opt.zero_grad();loss.backward();opt.step()
 if step%1000==0:print(step,float(loss),round(time.time()-start),flush=True)
with (out/'faces22.bin').open('wb') as f:
 f.write(struct.pack('>5i',0x42505831,41,128,4,64))
 for l in [net[0],net[2],net[4],net[6]]:f.write(l.weight.detach().T.numpy().astype('>f4').tobytes());f.write(l.bias.detach().numpy().astype('>f4').tobytes())
bank=out/'faces';bank.mkdir(exist_ok=True);palette=np.array([[55,48,63],[249,249,245],[101,61,76]],dtype='f');sheet=Image.new('RGB',(640,192),(210,180,140))
with torch.no_grad():
 for k in range(10):
  for e in range(3):
   for v in range(2):
    for eyes in range(3):
     for g in range(2):
      a=torch.sigmoid(net(torch.cat([P,torch.tensor(condition(k,e,v,eyes,g)).expand(S*S,-1)],1))).numpy().reshape(S,S,4);rgb=palette[((a[:,:,:3,None]*255-palette.T[None,None,:,:])**2).sum(2).argmin(2)];alpha=np.where(a[:,:,3]>.5,255,0).astype('uint8');im=Image.fromarray(np.concatenate([rgb.astype('uint8'),alpha[:,:,None]],2),'RGBA');im.save(bank/f'{k}-{e}-{v}-{eyes}-{g}.png')
      if v==0 and eyes==0 and g==0:sheet.paste(im,(k*64,e*64),im)
 vals=[]
 for c,target in test:
  p=torch.sigmoid(net(torch.cat([P,torch.tensor(c).expand(S*S,-1)],1))).numpy().reshape(S,S,4);a=p[:,:,3]>.5;b=target[:,:,3]>.5;vals.append(float((a&b).sum()/max(1,(a|b).sum())))
 inp=torch.cat([P[[10,600,1450]],C[0].expand(3,-1)],1);expected=torch.sigmoid(net(inp))
with (r/'model-source/faces22-reference.bin').open('wb') as f:
 f.write(struct.pack('>2i',3,41))
 for x,y in zip(inp,expected):f.write(x.numpy().astype('>f4').tobytes());f.write(y.numpy().astype('>f4').tobytes())
sheet.save(r.parent/'faces22-inspection.png');(out/'faces22-card.json').write_text(json.dumps({'name':'Banana Pixel 2.2','component':'shared facial anchors','external_weights':False,'parameters':sum(p.numel() for p in net.parameters()),'training_conditions':len(rows),'heldout_conditions':len(test),'heldout_alpha_IoU_synthetic':np.mean(vals),'features':['10 kinds','3 expressions','front and profile','3 eye sizes','glasses'],'limitations':'Own synthetic facial patches composed with own body fields. Not general face recognition.'},indent=2));print('DONE_FACES',flush=True)
