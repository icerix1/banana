package com.bananalab.lite;
import android.content.*;import android.graphics.*;import java.io.*;
/** Own finite-catalog recognizer. It does not provide general photo descriptions. */
final class OwnVision{
 static final String[] TYPES={"banana","pepino","gato","conejo","robot","perro","oso","zorro","persona","tomate"};private static OwnVision cached;
 final float[] a,ab,b,bb,c,cb,d,db;
 OwnVision(InputStream source)throws Exception{try(DataInputStream in=new DataInputStream(source)){int magic=in.readInt();if((magic!=0x42505632&&magic!=0x42505633)||in.readInt()!=32||in.readInt()!=8||in.readInt()!=12||in.readInt()!=64)throw new IOException("Modelo de referencia invalido");int classes=magic==0x42505633?in.readInt():9;if(classes!=9&&classes!=10)throw new IOException("Catalogo invalido");a=read(in,8*4*9);ab=read(in,8);b=read(in,12*8*9);bb=read(in,12);c=read(in,64*768);cb=read(in,64);d=read(in,classes*64);db=read(in,classes);}}
 static float[] read(DataInputStream in,int n)throws Exception{float[] x=new float[n];for(int i=0;i<n;i++){x[i]=in.readFloat();if(Float.isNaN(x[i])||Float.isInfinite(x[i]))throw new IOException("Pesos invalidos");}return x;}
 static float[] conv(float[] x,int in,int out,int side,float[] w,float[] bias){float[] y=new float[out*side*side];for(int o=0;o<out;o++)for(int yy=0;yy<side;yy++)for(int xx=0;xx<side;xx++){float sum=bias[o];for(int k=0;k<in;k++)for(int dy=0;dy<3;dy++)for(int dx=0;dx<3;dx++){int py=yy+dy-1,px=xx+dx-1;if(py>=0&&py<side&&px>=0&&px<side)sum+=x[(k*side+py)*side+px]*w[((o*in+k)*3+dy)*3+dx];}y[(o*side+yy)*side+xx]=Math.max(0,sum);}return y;}
 static float[] pool(float[] x,int channels,int side){int n=side/2;float[] y=new float[channels*n*n];for(int c=0;c<channels;c++)for(int yy=0;yy<n;yy++)for(int xx=0;xx<n;xx++)for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++)y[(c*n+yy)*n+xx]+=x[(c*side+yy*2+dy)*side+xx*2+dx]/4;return y;}
 static float[] dense(float[] x,float[] w,float[] b,boolean relu){float[] y=b.clone();for(int o=0;o<y.length;o++){for(int i=0;i<x.length;i++)y[o]+=w[o*x.length+i]*x[i];if(relu)y[o]=Math.max(0,y[o]);}return y;}
 float[] forward(float[] x){return dense(dense(pool(conv(pool(conv(x,4,8,32,a,ab),8,32),8,12,16,b,bb),12,16),c,cb,true),d,db,false);}
 static synchronized String describe(Context c,Bitmap bitmap){
  try{if(cached==null)cached=new OwnVision(c.getAssets().open("pixel22/vision22.bin"));Bitmap small=Bitmap.createScaledBitmap(bitmap,32,32,true);float[] input=new float[4096];int visible=0;for(int y=0;y<32;y++)for(int x=0;x<32;x++){int color=small.getPixel(x,y),i=y*32+x;float alpha=(color>>>24)/255f;input[i]=((color>>16)&255)/255f*alpha;input[1024+i]=((color>>8)&255)/255f*alpha;input[2048+i]=(color&255)/255f*alpha;input[3072+i]=alpha;if(alpha>.5f)visible++;}if(small!=bitmap)small.recycle();float[] scores=cached.forward(input);int best=0;for(int i=1;i<scores.length;i++)if(scores[i]>scores[best])best=i;float sum=0;for(float s:scores)sum+=(float)Math.exp(s-scores[best]);float confidence=1/sum;if(confidence<.85||visible>900)return "No identifiqué un tipo del catálogo con suficiente confianza.";return "Coincidencia experimental con "+TYPES[best]+" dentro del catálogo propio; no es una identificación fiable de fotos generales.";
  }catch(Exception e){return "El reconocimiento del catálogo no está disponible.";}
 }
}
