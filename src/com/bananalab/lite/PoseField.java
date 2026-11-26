package com.bananalab.lite;
import java.io.*;
/** Flujo inverso neuronal; conserva la textura de la imagen original. */
final class PoseField{
 final float[][][] w=new float[3][][];final float[][] b=new float[3][];
 PoseField(InputStream stream)throws IOException{try(DataInputStream in=new DataInputStream(new BufferedInputStream(stream))){if(in.readInt()!=0x42504f31||in.readInt()!=28||in.readInt()!=48)throw new IOException("Modelo de poses incompatible");for(int l=0;l<3;l++){int n=l==0?28:48,m=l==2?2:48;w[l]=new float[n][m];b[l]=new float[m];for(int i=0;i<n;i++)for(int j=0;j<m;j++)w[l][i][j]=value(in);for(int j=0;j<m;j++)b[l][j]=value(in);}}}
 private static float value(DataInputStream in)throws IOException{float x=in.readFloat();if(Float.isNaN(x)||Float.isInfinite(x))throw new IOException("Peso invalido");return x;}
 float[] forward(float[] x){for(int l=0;l<3;l++){float[] y=b[l].clone();for(int i=0;i<x.length;i++)for(int j=0;j<y.length;j++)y[j]+=x[i]*w[l][i][j];if(l<2)for(int j=0;j<y.length;j++)y[j]=Math.max(0,y[j]);x=y;}return x;}
 int[] deform(int[] original,int type,int pose)throws IOException{
  int side=(int)Math.sqrt(original.length);if((side!=64&&side!=96&&side!=128)||side*side!=original.length||type<0||type>=9||pose<0||pose>=5)throw new IOException("Pose no admitida");if(pose==0)return original.clone();if(type>=5)type=type==8?4:2;int[] out=new int[original.length];float[] f=new float[28];f[18+type]=1;f[23+pose]=1;
  for(int y=0;y<side;y++){if(Thread.currentThread().isInterrupted())throw new IOException("Pose cancelada");for(int x=0;x<side;x++){f[0]=(x+.5f)/side*2-1;f[1]=(y+.5f)/side*2-1;int at=2;for(int freq=1;freq<=8;freq*=2){f[at++]=(float)Math.sin(f[0]*Math.PI*freq);f[at++]=(float)Math.sin(f[1]*Math.PI*freq);f[at++]=(float)Math.cos(f[0]*Math.PI*freq);f[at++]=(float)Math.cos(f[1]*Math.PI*freq);}float[] uv=forward(f);out[y*side+x]=sample(original,(uv[0]+1)*side/2-.5f,(uv[1]+1)*side/2-.5f);}}
  return out;
 }
 static int sample(int[] pixels,float x,float y){int side=(int)Math.sqrt(pixels.length);if(x<0||y<0||x>side-1||y>side-1)return 0;int x0=(int)x,y0=(int)y,x1=Math.min(side-1,x0+1),y1=Math.min(side-1,y0+1);float dx=x-x0,dy=y-y0;int[] p={pixels[y0*side+x0],pixels[y0*side+x1],pixels[y1*side+x0],pixels[y1*side+x1]};float[] weights={(1-dx)*(1-dy),dx*(1-dy),(1-dx)*dy,dx*dy};float alpha=0,r=0,g=0,b=0;for(int i=0;i<4;i++){float a=(p[i]>>>24)*weights[i];alpha+=a;r+=((p[i]>>16)&255)*a;g+=((p[i]>>8)&255)*a;b+=(p[i]&255)*a;}if(alpha<1)return 0;return (Math.round(alpha)<<24)|(Math.round(r/alpha)<<16)|(Math.round(g/alpha)<<8)|Math.round(b/alpha);}
}
