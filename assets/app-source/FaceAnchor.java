package com.bananalab.lite;
import android.content.Context;import android.graphics.*;import java.util.*;
/** Own neural face patches over own face-free body views, never applied to references. */
final class FaceAnchor{
 static final float[][] CENTERS={{35,43},{32,32},{32,27},{32,29},{32,26},{32,27},{32,27},{32,27},{32,26},{32,33},{32,31}};
 private static final LinkedHashMap<String,Bitmap> faces=new LinkedHashMap<>(32,.75f,true);
 static Bitmap apply(Context c,int kind,int color,int acc,int expr,int view,String look,Bitmap base)throws Exception{
  if(view==2)return base;int eyes=Appearance.has(look,"ojos pequenos|ojos chicos")?1:Appearance.has(look,"ojos grandes")?2:0,g=acc==5||Appearance.has(look,"lentes|anteojos|gafas")?1:0;String key=kind+"-"+expr+"-"+view+"-"+eyes+"-"+g;Bitmap face;
  synchronized(FaceAnchor.class){face=faces.get(key);}if(face==null){try(java.io.InputStream in=c.getAssets().open("pixel22/faces/"+key+".png")){face=BitmapFactory.decodeStream(in);}if(face==null)throw new java.io.IOException("Falta ancla facial");synchronized(FaceAnchor.class){faces.put(key,face);while(faces.size()>40)faces.remove(faces.keySet().iterator().next());}}
  Bitmap back;try(java.io.InputStream in=c.getAssets().open("pixel-v2/bank/k"+kind+"-"+Integer.toHexString(color)+"-0-0-v2.webp")){back=BitmapFactory.decodeStream(in);}catch(Exception e){back=null;}if(back==null)return base;
  if(view==1){Matrix m=new Matrix();m.setScale(.78f,1,64,64);back=Bitmap.createBitmap(back,0,0,128,128,m,true);Bitmap fit=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);new Canvas(fit).drawBitmap(back,(128-back.getWidth())/2f,0,new Paint(Paint.FILTER_BITMAP_FLAG));back=fit;}
  int[] pixels=new int[16384],fill=new int[16384];base.getPixels(pixels,0,128,0,0,128,128);back.getPixels(fill,0,128,0,0,128,128);float fx=CENTERS[kind][0],fy=CENTERS[kind][1],width=view==1?20.3f:26;if(view==1)fx=32+(fx-32)*.78f;
  for(int y=0;y<128;y++)for(int x=0;x<128;x++){float xx=(x+.5f)/2,yy=(y+.5f)/2;int i=y*128+x;if(Math.abs(xx-fx)<=width/2&&yy>=fy-5&&yy<=fy+14&&(pixels[i]>>>24)>100&&(fill[i]>>>24)>100)pixels[i]=(pixels[i]&0xff000000)|(fill[i]&0xffffff);}
  Bitmap out=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);out.setPixels(pixels,0,128,0,0,128,128);Canvas canvas=new Canvas(out);canvas.drawBitmap(face,null,new RectF((fx-width/2)*2,(fy-8)*2,(fx+width/2)*2,(fy+16)*2),new Paint(Paint.FILTER_BITMAP_FLAG));return out;
 }
}
