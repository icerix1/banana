package com.bananalab.lite;
import android.content.Context;import android.graphics.Bitmap;import java.util.*;
final class NeuralBackdrop{
 static final class Frame{final Bitmap background,foreground;Frame(Bitmap b,Bitmap f){background=b;foreground=f;}}
 private static BackdropField model;private static final LinkedHashMap<String,Frame[]> cache=new LinkedHashMap<>(4,.75f,true);
 private static String key(SceneDraft d,long seed){return d.setting+"|"+d.lighting+"|"+tree(d)+"|"+seed;}
 private static boolean tree(SceneDraft d){if(d.sceneryTree)return true;for(SceneDraft.Actor a:d.cast)if(a.relation.contains("arbol"))return true;return false;}
 static float[] condition(String words,boolean tree,long seed,float phase){String text=Knowledge.normalize(words);int kind=text.contains("playa")?1:text.contains("ciudad")?2:text.contains("montana")?3:text.matches("(?s).*(pasillo|interior|casa|habitacion).*" )?4:0;float[] c=new float[10];c[kind]=1;c[5]=text.contains("dia")?.25f:text.contains("noche")?.9f:.35f;Random r=new Random(seed);c[6]=r.nextFloat()*2-1;c[7]=r.nextFloat()*2-1;c[8]=phase;c[9]=tree?1:0;return c;}
 static synchronized Frame[] cached(SceneDraft draft,long seed){return cache.get(key(draft,seed));}
 static Frame[] prepare(Context context,SceneDraft draft,long seed)throws Exception{
  synchronized(NeuralBackdrop.class){Frame[] hit=cache.get(key(draft,seed));if(hit!=null)return hit;if(model==null)model=new BackdropField(context.getAssets().open("background/background22.bin"));}
  Frame[] frames=new Frame[2];
  for(int i=0;i<2;i++){float[] cond=condition(draft.setting,tree(draft),seed,i==0?-.7f:.7f);if(draft.lighting.equals("day")||draft.lighting.equals("warm"))cond[5]=.25f;else if(draft.lighting.equals("night"))cond[5]=.9f;int[] rgba=null;
   if(seed==0){int kind=0;for(int k=0;k<5;k++)if(cond[k]>.5f)kind=k;int light=Math.abs(cond[5]-.25f)<.001?1:cond[5]>.8f?2:0;String path="background/bank22/k"+kind+"-"+light+"-"+(tree(draft)?1:0)+"-"+i;
    try(java.io.InputStream rgb=context.getAssets().open(path+"-rgb.png");java.io.InputStream mask=context.getAssets().open(path+"-mask.png")){
     Bitmap color=android.graphics.BitmapFactory.decodeStream(rgb),alpha=android.graphics.BitmapFactory.decodeStream(mask);if(color!=null&&alpha!=null){rgba=new int[color.getWidth()*color.getHeight()];int[] ma=new int[rgba.length];color.getPixels(rgba,0,color.getWidth(),0,0,color.getWidth(),color.getHeight());alpha.getPixels(ma,0,alpha.getWidth(),0,0,alpha.getWidth(),alpha.getHeight());for(int k=0;k<rgba.length;k++)rgba[k]=(rgba[k]&0xffffff)|((ma[k]&255)<<24);color.recycle();alpha.recycle();}
    }catch(Exception ignored){}}
   if(rgba==null)rgba=model.render(cond);int[] opaque=new int[rgba.length];for(int j=0;j<rgba.length;j++)opaque[j]=rgba[j]|0xff000000;frames[i]=new Frame(Bitmap.createBitmap(opaque,128,128,Bitmap.Config.ARGB_8888),Bitmap.createBitmap(rgba,128,128,Bitmap.Config.ARGB_8888));}
  synchronized(NeuralBackdrop.class){cache.put(key(draft,seed),frames);while(cache.size()>3)cache.remove(cache.keySet().iterator().next());}return frames;
 }
}
