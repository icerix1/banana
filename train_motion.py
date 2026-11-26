"""Modelo propio de movimiento: entrenamiento NumPy desde cero y exportacion JSON."""
import os
os.environ.setdefault('OPENBLAS_NUM_THREADS','1')
import json
from pathlib import Path
import numpy as np
rng=np.random.default_rng(41)
shape=[4,24,24,3]
weights=[rng.normal(0,np.sqrt(1/a),(b,a)).astype('float32') for a,b in zip(shape[:-1],shape[1:])]
bias=[np.zeros(b,dtype='float32') for b in shape[1:]]
params=weights+bias;m=[np.zeros_like(p) for p in params];v=[np.zeros_like(p) for p in params]
for step in range(1,2401):
 a=rng.integers(0,3,128);t=rng.random(128).astype('float32')
 x=np.column_stack([np.eye(3,dtype='float32')[a],t]);y=np.zeros((128,3),dtype='float32')
 y[:,0]=(a==0)*t*.8;y[:,1]=(a==1)*(-np.sin(np.pi*t)*.8);y[:,2]=(a==2)*np.sin(np.pi*t)*.8
 activations=[x]
 for w,b in zip(weights,bias):activations.append(np.tanh(activations[-1]@w.T+b))
 delta=2*(activations[-1]-y)/y.size;dw=[];db=[]
 for i in range(2,-1,-1):
  dz=delta*(1-activations[i+1]**2);dw.insert(0,dz.T@activations[i]);db.insert(0,dz.sum(0));delta=dz@weights[i]
 for i,(p,g) in enumerate(zip(params,dw+db)):
  m[i]=.9*m[i]+.1*g;v[i]=.999*v[i]+.001*g*g
  p-=.003*(m[i]/(1-.9**step))/(np.sqrt(v[i]/(1-.999**step))+1e-8)
a=np.repeat(np.arange(3),101);t=np.tile(np.linspace(0,1,101,dtype='float32'),3)
x=np.column_stack([np.eye(3,dtype='float32')[a],t]);y=np.zeros((303,3),dtype='float32');y[:,0]=(a==0)*t*.8;y[:,1]=(a==1)*(-np.sin(np.pi*t)*.8);y[:,2]=(a==2)*np.sin(np.pi*t)*.8
pred=x
for w,b in zip(weights,bias):pred=np.tanh(pred@w.T+b)
model=dict(format='banana-motion-0.1',shape=shape,activation='tanh',steps=2400,validation_mse=float(np.mean((pred-y)**2)),layers=[dict(weight=w.round(7).tolist(),bias=b.round(7).tolist()) for w,b in zip(weights,bias)])
path=Path(__file__).resolve().parent/'assets/motion.json';path.write_text(json.dumps(model,separators=(',',':')),encoding='utf-8')
print('Modelo entrenado:',model['validation_mse'],'MSE,',path.stat().st_size,'bytes')
# Formato binario utilizado por la app Java. No necesita NumPy en Android.
import struct
with path.with_suffix('.bin').open('wb') as f:
 f.write(b'BLM1');f.write(struct.pack('>i',len(model['layers'])))
 for layer in model['layers']:
  w,b=layer['weight'],layer['bias'];f.write(struct.pack('>ii',len(w),len(w[0])))
  for row in w:f.write(struct.pack('>'+str(len(row))+'f',*row))
  f.write(struct.pack('>'+str(len(b))+'f',*b))
