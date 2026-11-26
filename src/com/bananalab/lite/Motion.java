package com.bananalab.lite;
import java.io.*;
public final class Motion {
 private final float[][][] weights;
 private final float[][] biases;
 public Motion(InputStream stream) throws IOException {
  DataInputStream in=new DataInputStream(stream);
  if(in.readInt()!=0x424c4d31)throw new IOException("Modelo incompatible");
  int count=in.readInt();if(count!=3)throw new IOException("Arquitectura incompatible");
  weights=new float[count][][];biases=new float[count][];
  int expected=4;
  for(int l=0;l<count;l++){
   int out=in.readInt(),input=in.readInt();
   if(input!=expected||out<1||out>32)throw new IOException("Modelo invalido");
   weights[l]=new float[out][input];biases[l]=new float[out];
   for(int r=0;r<out;r++)for(int c=0;c<input;c++)weights[l][r][c]=in.readFloat();
   for(int r=0;r<out;r++)biases[l][r]=in.readFloat();expected=out;
  }
  if(expected!=3)throw new IOException("Salida invalida");
 }
 public float[] predict(int action,float time){
  if(action<0||action>2||time<0||time>1)throw new IllegalArgumentException();
  float[] a=new float[]{action==0?1:0,action==1?1:0,action==2?1:0,time};
  for(int l=0;l<weights.length;l++){
   float[] next=new float[weights[l].length];
   for(int r=0;r<next.length;r++){
    float sum=biases[l][r];for(int c=0;c<a.length;c++)sum+=weights[l][r][c]*a[c];
    next[r]=(float)Math.tanh(sum);
   }
   a=next;
  }
  return a;
 }

 public Motion copy() throws IOException {
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();save(bytes);
  return new Motion(new ByteArrayInputStream(bytes.toByteArray()));
 }
 public void save(OutputStream output) throws IOException {
  DataOutputStream out=new DataOutputStream(output);out.writeInt(0x424c4d31);out.writeInt(weights.length);
  for(int l=0;l<weights.length;l++){
   out.writeInt(weights[l].length);out.writeInt(weights[l][0].length);
   for(float[] row:weights[l])for(float value:row)out.writeFloat(value);
   for(float value:biases[l])out.writeFloat(value);
  }
  out.flush();
 }
 public void teach(int action,float time,float[] target,float rate) {
  float[][] activations=new float[4][];
  activations[0]=new float[]{action==0?1:0,action==1?1:0,action==2?1:0,time};
  for(int l=0;l<3;l++){
   activations[l+1]=new float[weights[l].length];
   for(int r=0;r<weights[l].length;r++){
    float sum=biases[l][r];
    for(int c=0;c<weights[l][r].length;c++)sum+=weights[l][r][c]*activations[l][c];
    activations[l+1][r]=(float)Math.tanh(sum);
   }
  }
  float[] gradient=new float[3];
  for(int r=0;r<3;r++)gradient[r]=2*(activations[3][r]-target[r])/3;
  for(int l=2;l>=0;l--){
   float[] previous=new float[activations[l].length];
   for(int r=0;r<weights[l].length;r++){
    float value=activations[l+1][r];
    float delta=Math.max(-1,Math.min(1,gradient[r]*(1-value*value)));
    for(int c=0;c<previous.length;c++){
     previous[c]+=delta*weights[l][r][c];
     weights[l][r][c]-=rate*delta*activations[l][c];
    }
    biases[l][r]-=rate*delta;
   }
   gradient=previous;
  }
 }
}
