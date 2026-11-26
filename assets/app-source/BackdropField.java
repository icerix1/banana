package com.bananalab.lite;
import java.io.*;
/** Campo neuronal condicional: todos los pixeles del personaje salen de esta red. */
final class BackdropField{
 final int inputs,hidden,outputs,side;final float[][][] weights=new float[4][][];final float[][] biases=new float[4][];
 BackdropField(InputStream stream)throws IOException{try(DataInputStream in=new DataInputStream(new BufferedInputStream(stream))){if(in.readInt()!=0x42424731)throw new IOException("Generador incompatible");inputs=in.readInt();hidden=in.readInt();outputs=in.readInt();side=in.readInt();if((inputs!=28&&inputs!=32)||(hidden!=64&&hidden!=96&&hidden!=128)||outputs!=4||side!=128)throw new IOException("Dimensiones de imagen invalidas");for(int l=0;l<4;l++){int n=l==0?inputs:hidden,m=l==3?outputs:hidden;weights[l]=new float[n][m];biases[l]=new float[m];for(int i=0;i<n;i++)for(int j=0;j<m;j++)weights[l][i][j]=value(in);for(int j=0;j<m;j++)biases[l][j]=value(in);}}}
 private static float value(DataInputStream in)throws IOException{float x=in.readFloat();if(Float.isNaN(x)||Float.isInfinite(x))throw new IOException("Peso invalido");return x;}
 private static float sigmoid(float x){return (float)(1/(1+Math.exp(-x)));}
 float[] forward(float[] input){float[] current=input;for(int l=0;l<4;l++){float[] next=biases[l].clone();for(int i=0;i<current.length;i++)for(int j=0;j<next.length;j++)next[j]+=current[i]*weights[l][i][j];for(int j=0;j<next.length;j++)next[j]=l==3?sigmoid(next[j]):Math.max(0,next[j]);current=next;}return current;}
 int[] render(float[] condition)throws IOException{
  if(condition.length!=10)throw new IOException("Condicion invalida");float[] base=biases[0].clone();for(int i=0;i<10;i++)for(int j=0;j<hidden;j++)base[j]+=condition[i]*weights[0][inputs-10+i][j];
  float[][] values={new float[hidden],new float[hidden],new float[hidden],new float[4]};int[] pixels=new int[side*side];float[] p=new float[inputs-10];long deadline=System.nanoTime()+90L*1000000000;
  for(int y=0;y<side;y++){
   if(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline)throw new IOException("Generacion neuronal interrumpida");
   for(int x=0;x<side;x++){
    p[0]=(x+.5f)/side*2-1;p[1]=(y+.5f)/side*2-1;int at=2;
    for(int freq=1;freq<=(inputs==32?16:8);freq*=2){p[at++]=(float)Math.sin(p[0]*Math.PI*freq);p[at++]=(float)Math.sin(p[1]*Math.PI*freq);p[at++]=(float)Math.cos(p[0]*Math.PI*freq);p[at++]=(float)Math.cos(p[1]*Math.PI*freq);}
    System.arraycopy(base,0,values[0],0,hidden);for(int i=0;i<p.length;i++)for(int j=0;j<hidden;j++)values[0][j]+=p[i]*weights[0][i][j];for(int j=0;j<hidden;j++)values[0][j]=Math.max(0,values[0][j]);
    for(int l=1;l<4;l++){int n=l==3?4:hidden;System.arraycopy(biases[l],0,values[l],0,n);for(int i=0;i<hidden;i++){float v=values[l-1][i];for(int j=0;j<n;j++)values[l][j]+=v*weights[l][i][j];}for(int j=0;j<n;j++)values[l][j]=l==3?sigmoid(values[l][j]):Math.max(0,values[l][j]);}
    float[] rgba=values[3];int alpha=rgba[3]<.1f?0:Math.round(rgba[3]*255);pixels[y*side+x]=(alpha<<24)|(Math.round(rgba[0]*255)<<16)|(Math.round(rgba[1]*255)<<8)|Math.round(rgba[2]*255);
   }
  }return pixels;
 }
}
