package com.bananalab.lite;
import java.io.*;
public class PixelCheck{
 public static void main(String[] args)throws Exception{
  PixelGenerator net=new PixelGenerator(new FileInputStream(args[0]));float max=0;
  try(DataInputStream in=new DataInputStream(new FileInputStream(args[1]))){int rows=in.readInt(),n=in.readInt();for(int r=0;r<rows;r++){float[] input=new float[n];for(int i=0;i<n;i++)input[i]=in.readFloat();float[] actual=net.forward(input);for(int i=0;i<4;i++)max=Math.max(max,Math.abs(actual[i]-in.readFloat()));}}
  if(max>.0002f)throw new AssertionError("Paridad "+max);System.out.println("PASS paridad neuronal Java/PyTorch: "+max);
  long start=System.nanoTime();int[] blue=net.render(PixelGenerator.condition(1,0xff629cdb,1,0,0)),red=net.render(PixelGenerator.condition(1,0xffd66363,0,0,0));int opaque=0,delta=0;
  for(int i=0;i<blue.length;i++){if((blue[i]>>>24)>128)opaque++;if(blue[i]!=red[i])delta++;}
  if(opaque<100||opaque>2200||delta<200)throw new AssertionError("Imagen o condicion no valida");
  int[] noHat=net.render(PixelGenerator.condition(1,0xff629cdb,0,0,0));int hatTop=0,plainTop=0;long bR=0,bB=0,rR=0,rB=0;for(int y=0;y<64;y++)for(int x=0;x<64;x++){int i=y*64+x;if(y<17){if((blue[i]>>>24)>128)hatTop++;if((noHat[i]>>>24)>128)plainTop++;}if(x>=25&&x<40&&y>=25&&y<46){bR+=(blue[i]>>16)&255;bB+=blue[i]&255;rR+=(red[i]>>16)&255;rB+=red[i]&255;}}if(hatTop<plainTop+30||bB<=bR||rR<=rB)throw new AssertionError("Color o sombrero no responden a la condicion");
  try(DataOutputStream out=new DataOutputStream(new FileOutputStream(args[2]))){for(int pixel:blue)out.writeInt(pixel);}
  Thread.currentThread().interrupt();try{net.render(PixelGenerator.condition(1,0xff629cdb,1,0,0));throw new AssertionError("Sin cancelacion");}catch(IOException expected){}finally{Thread.interrupted();}
  System.out.println("PASS RGBA64, condicion color/accesorio y cancelacion; pixeles opacos "+opaque+", diferencias "+delta+", ms host "+(System.nanoTime()-start)/1000000);
 }
}
