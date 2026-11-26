"""Palette cleanup of our own neural object outputs; no drawing replacement."""
from pathlib import Path
from PIL import Image,ImageFilter
import numpy as np,json,hashlib
r=Path(__file__).resolve().parents[1];bank=r/'assets/pixel-v2/props';source=r/'model-source/props20-original';source.mkdir(exist_ok=True)
colors=np.array([[143,104,75],[178,139,110],[39,45,58],[91,99,108],[27,35,48],[235,200,90],[236,198,79],[246,217,126]],dtype='f');sheet=Image.new('RGB',(640,256),(221,228,232))
for path in bank.glob('*.png'):
 saved=source/path.name
 if not saved.exists():saved.write_bytes(path.read_bytes())
 a=np.asarray(Image.open(saved).convert('RGBA')).copy();rgb=np.asarray(Image.fromarray(a[:,:,:3]).filter(ImageFilter.MedianFilter(3)),dtype='f');distance=((rgb[:,:,None,:]-colors[None,None,:,:])**2).sum(-1);a[:,:,:3]=colors[distance.argmin(-1)].astype('uint8');alpha=np.asarray(Image.fromarray(a[:,:,3]).filter(ImageFilter.MedianFilter(3)));a[:,:,3]=np.where(alpha<95,0,np.where(alpha>165,255,alpha));im=Image.fromarray(a,'RGBA');im.save(path);kind,opened=map(int,path.stem.split('-'));sheet.paste(im,(kind*128,opened*128),im)
sheet.save(r.parent/'props21-clean-inspection.png')
(bank/'cleanup-provenance.json').write_text(json.dumps({'source':'Original Banana own neural props outputs','source_model_sha256':hashlib.sha256((r/'assets/pixel-v2/props.bin').read_bytes()).hexdigest(),'postprocessing':'3x3 median and known own training palette quantization; geometry stays neural.'},indent=2))
