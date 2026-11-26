package com.bananalab.lite;
import java.io.*;
/** GIF limitado: paleta RGB332; LZW con reinicio por pixel para RAM acotada. */
public final class GifWriter implements Closeable {
 private final OutputStream out;private final int size;private boolean ended=false;
 public GifWriter(OutputStream output,int size) throws IOException {
  if(size!=32&&size!=64&&size!=96&&size!=128&&size!=256)throw new IllegalArgumentException();this.out=output;this.size=size;
  out.write("GIF89a".getBytes("US-ASCII"));word(size);word(size);out.write(0xf7);out.write(0);out.write(0);
  for(int i=0;i<256;i++){out.write(((i>>5)&7)*255/7);out.write(((i>>2)&7)*255/7);out.write((i&3)*255/3);}
  out.write(new byte[]{0x21,(byte)0xff,11});out.write("NETSCAPE2.0".getBytes("US-ASCII"));out.write(new byte[]{3,1,0,0,0});
 }
 private void word(int v)throws IOException{out.write(v&255);out.write((v>>8)&255);}
 public void frame(int[] pixels,int delay)throws IOException{
  if(ended||pixels.length!=size*size)throw new IllegalArgumentException();
  out.write(new byte[]{0x21,(byte)0xf9,4,4});word(delay);out.write(0);out.write(0);
  out.write(0x2c);word(0);word(0);word(size);word(size);out.write(0);out.write(8);
  ByteArrayOutputStream codes=new ByteArrayOutputStream(size*size*3);
  long bits=0;int length=0;
  for(int color:pixels){
   int index=(((color>>16)&255)>>5)*32+(((color>>8)&255)>>5)*4+((color&255)>>6);
   // Clear + pixel. Asi el ancho permanece en 9 bits, sin diccionario creciente.
   bits|=256L<<length;length+=9;bits|=((long)index)<<length;length+=9;while(length>=8){codes.write((int)(bits&255));bits>>>=8;length-=8;}
  }
  bits|=257L<<length;length+=9;while(length>0){codes.write((int)(bits&255));bits>>>=8;length-=8;}
  byte[] data=codes.toByteArray();for(int at=0;at<data.length;at+=255){int count=Math.min(255,data.length-at);out.write(count);out.write(data,at,count);}out.write(0);
 }
 public void close()throws IOException{if(!ended){out.write(0x3b);out.flush();ended=true;}}
}
