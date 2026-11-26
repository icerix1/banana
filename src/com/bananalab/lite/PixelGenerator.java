package com.bananalab.lite;
import java.io.*;
/** Campo neuronal condicional: todos los pixeles del personaje salen de esta red. */
final class PixelGenerator{
 final int inputs,hidden,outputs,side;final float[][][] weights=new float[4][][];final float[][] biases=new float[4][];
 PixelGenerator(InputStream stream)throws IOException{try(DataInputStream in=new DataInputStream(new BufferedInputStream(stream))){if(in.readInt()!=0x42505831)throw new IOException("Generador incompatible");inputs=in.readInt();hidden=in.readInt();outputs=in.readInt();side=in.readInt();if(!((inputs==34&&hidden==96&&side==64)||(inputs==38&&hidden==160&&side==96)||((inputs==45&&hidden==192||inputs==48&&hidden==256)&&side==128)||(inputs==31&&hidden==96&&side==128)||(inputs==37&&hidden==160&&side==128)||(inputs==41&&hidden==128&&side==64)||(inputs==32&&hidden==64&&side==64))||outputs!=4)throw new IOException("Dimensiones de imagen invalidas");for(int l=0;l<4;l++){int n=l==0?inputs:hidden,m=l==3?outputs:hidden;weights[l]=new float[n][m];biases[l]=new float[m];for(int i=0;i<n;i++)for(int j=0;j<m;j++)weights[l][i][j]=value(in);for(int j=0;j<m;j++)biases[l][j]=value(in);}}}
 private static float value(DataInputStream in)throws IOException{float x=in.readFloat();if(Float.isNaN(x)||Float.isInfinite(x))throw new IOException("Peso invalido");return x;}
 static int type(String name){if(name.matches("banana|platano"))return 0;if(name.equals("pepino"))return 1;if(name.matches("gato|gata"))return 2;if(name.matches("conejo|coneja"))return 3;if(name.equals("robot"))return 4;if(name.matches("perro|perra"))return 5;if(name.equals("oso"))return 6;if(name.equals("zorro"))return 7;if(name.matches("persona|humano"))return 8;if(name.equals("tomate"))return 9;if(name.equals("criatura"))return 10;return -1;}
 static float[] condition(int type,int color,int accessory,int expression,long seed){if(type<0||type>=5||accessory<0||accessory>=3||expression<0||expression>=3)throw new IllegalArgumentException("Condicion no admitida");float[] c=new float[16];c[type]=1;c[5]=((color>>16)&255)/255f;c[6]=((color>>8)&255)/255f;c[7]=(color&255)/255f;c[8+accessory]=1;c[11+expression]=1;java.util.Random random=new java.util.Random(seed);c[14]=random.nextFloat()*2-1;c[15]=random.nextFloat()*2-1;return c;}
 private static float sigmoid(float x){return (float)(1/(1+Math.exp(-x)));}
 float[] features(float[] input){float[] current=input;for(int l=0;l<3;l++){float[] next=biases[l].clone();for(int i=0;i<current.length;i++)for(int j=0;j<next.length;j++)next[j]+=current[i]*weights[l][i][j];for(int j=0;j<next.length;j++)next[j]=Math.max(0,next[j]);current=next;}return current;}
 float[] coordinates(float[] condition,int x,int y){int coords=inputs-condition.length;float[] p=new float[inputs];p[0]=(x+.5f)/side*2-1;p[1]=(y+.5f)/side*2-1;int at=2;for(int f=1;f<=(coords==22?16:8);f*=2){p[at++]=(float)Math.sin(p[0]*Math.PI*f);p[at++]=(float)Math.sin(p[1]*Math.PI*f);p[at++]=(float)Math.cos(p[0]*Math.PI*f);p[at++]=(float)Math.cos(p[1]*Math.PI*f);}System.arraycopy(condition,0,p,coords,condition.length);return p;}
 void save(OutputStream stream)throws IOException{DataOutputStream out=new DataOutputStream(new BufferedOutputStream(stream));out.writeInt(0x42505831);for(int n:new int[]{inputs,hidden,outputs,side})out.writeInt(n);for(int l=0;l<4;l++){BananaTransformer.write(out,weights[l]);BananaTransformer.write(out,biases[l]);}out.flush();}
 float[] forward(float[] input){float[] current=input;for(int l=0;l<4;l++){float[] next=biases[l].clone();for(int i=0;i<current.length;i++)for(int j=0;j<next.length;j++)next[j]+=current[i]*weights[l][i][j];for(int j=0;j<next.length;j++)next[j]=l==3?sigmoid(next[j]):Math.max(0,next[j]);current=next;}return current;}
 float[] conditionFor(int type,int color,int accessory,int expression,long seed){
  if(inputs==37){float[] c=new float[15];c[0]=((color>>16)&255)/255f;c[1]=((color>>8)&255)/255f;c[2]=(color&255)/255f;c[3+accessory]=1;c[9+expression]=1;c[12]=1;return c;}if(inputs!=45&&inputs!=48)return condition(type,color,accessory,expression,seed);
  if(type<0||type>=9||accessory<0||accessory>=6||expression<0||expression>=3)throw new IllegalArgumentException("Condicion no admitida");
  float[] c=new float[inputs==48?26:23];c[type]=1;c[9]=((color>>16)&255)/255f;c[10]=((color>>8)&255)/255f;c[11]=(color&255)/255f;c[12+accessory]=1;c[18+expression]=1;if(inputs==48)c[23]=1;return c;
 }
 int[] render(float[] condition)throws IOException{
  int conditionSize=inputs==37?15:inputs==41?19:inputs==48?26:inputs==45?23:inputs==31?9:16;if(condition.length!=conditionSize)throw new IOException("Condicion invalida");if(inputs==38){condition=condition.clone();condition[14]=0;condition[15]=0;}int coords=inputs-conditionSize;float[] base=biases[0].clone();for(int i=0;i<conditionSize;i++)for(int j=0;j<hidden;j++)base[j]+=condition[i]*weights[0][coords+i][j];
  float[][] values={new float[hidden],new float[hidden],new float[hidden],new float[4]};int[] pixels=new int[side*side];float[] p=new float[coords];long deadline=System.nanoTime()+180L*1000000000;
  for(int y=0;y<side;y++){
   if(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline)throw new IOException("Generacion neuronal interrumpida");
   for(int x=0;x<side;x++){
    p[0]=(x+.5f)/side*2-1;p[1]=(y+.5f)/side*2-1;int at=2;
    for(int freq=1;freq<=(coords==22?16:8);freq*=2){p[at++]=(float)Math.sin(p[0]*Math.PI*freq);p[at++]=(float)Math.sin(p[1]*Math.PI*freq);p[at++]=(float)Math.cos(p[0]*Math.PI*freq);p[at++]=(float)Math.cos(p[1]*Math.PI*freq);}
    System.arraycopy(base,0,values[0],0,hidden);for(int i=0;i<coords;i++)for(int j=0;j<hidden;j++)values[0][j]+=p[i]*weights[0][i][j];for(int j=0;j<hidden;j++)values[0][j]=Math.max(0,values[0][j]);
    for(int l=1;l<4;l++){int n=l==3?4:hidden;System.arraycopy(biases[l],0,values[l],0,n);for(int i=0;i<hidden;i++){float v=values[l-1][i];for(int j=0;j<n;j++)values[l][j]+=v*weights[l][i][j];}for(int j=0;j<n;j++)values[l][j]=l==3?sigmoid(values[l][j]):Math.max(0,values[l][j]);}
    float[] rgba=values[3];int alpha=rgba[3]<.1f?0:Math.round(rgba[3]*255);pixels[y*side+x]=(alpha<<24)|(Math.round(rgba[0]*255)<<16)|(Math.round(rgba[1]*255)<<8)|Math.round(rgba[2]*255);
   }
  }return pixels;
 }
}
