package com.bananalab.lite;
import android.content.*;import android.graphics.*;import android.net.Uri;import java.io.*;import java.util.*;
final class ReferenceImage{
 static Bitmap importBitmap(Context c,Uri uri,boolean remove)throws Exception{
  BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;try(InputStream in=c.getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,o);}if(o.outWidth<1||o.outHeight<1)throw new IOException("Imagen no admitida");
  o.inJustDecodeBounds=false;o.inSampleSize=1;while(o.outWidth/o.inSampleSize>512||o.outHeight/o.inSampleSize>512)o.inSampleSize*=2;Bitmap original;try(InputStream in=c.getContentResolver().openInputStream(uri)){original=BitmapFactory.decodeStream(in,null,o);}if(original==null)throw new IOException("No pude leer la imagen.");
  Bitmap work=original.copy(Bitmap.Config.ARGB_8888,true);original.recycle();
  if(remove){int w=work.getWidth(),h=work.getHeight(),border=work.getPixel(0,0),r=(border>>16)&255,g=(border>>8)&255,b=border&255;int[] pixels=new int[w*h];work.getPixels(pixels,0,w,0,0,w,h);boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];int head=0,tail=0;
   for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(x==0||y==0||x==w-1||y==h-1){int i=y*w+x;if(!seen[i]){seen[i]=true;queue[tail++]=i;}}
   while(head<tail){int i=queue[head++],color=pixels[i];int dr=((color>>16)&255)-r,dg=((color>>8)&255)-g,db=(color&255)-b;if(dr*dr+dg*dg+db*db>1800)continue;pixels[i]=0;int x=i%w,y=i/w;int[] ns={x>0?i-1:-1,x+1<w?i+1:-1,y>0?i-w:-1,y+1<h?i+w:-1};for(int j:ns)if(j>=0&&!seen[j]){seen[j]=true;queue[tail++]=j;}}
   work.setPixels(pixels,0,w,0,0,w,h);
  }
  Bitmap result=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(result);float scale=Math.min(112f/work.getWidth(),120f/work.getHeight()),width=work.getWidth()*scale,height=work.getHeight()*scale;canvas.drawBitmap(work,null,new RectF((128-width)/2,124-height,(128+width)/2,124),new Paint(Paint.FILTER_BITMAP_FLAG));work.recycle();return result;
 }
 static String describe(Context c,Bitmap image){return describe(image)+" "+OwnVision.describe(c,image);}
 static String describe(Bitmap image){Map<Integer,Integer> bins=new HashMap<>();int visible=0;for(int y=0;y<image.getHeight();y+=2)for(int x=0;x<image.getWidth();x+=2){int color=image.getPixel(x,y);if((color>>>24)<100)continue;int key=(((color>>16)&255)/32<<6)|(((color>>8)&255)/32<<3)|((color&255)/32);bins.put(key,bins.containsKey(key)?bins.get(key)+1:1);visible++;}
  List<Map.Entry<Integer,Integer>> values=new ArrayList<>(bins.entrySet());Collections.sort(values,(a,b)->Integer.compare(b.getValue(),a.getValue()));StringBuilder s=new StringBuilder("Referencia visual conservada. Colores dominantes: ");for(int i=0;i<Math.min(3,values.size());i++){int k=values.get(i).getKey();if(i>0)s.append(", ");s.append(String.format("#%02X%02X%02X",((k>>6)&7)*32+16,((k>>3)&7)*32+16,(k&7)*32+16));}return visible==0?"La imagen quedó transparente; importá otra referencia.":s+". Este análisis mide color y transparencia; no identifica el contenido de cualquier foto.";}
}
