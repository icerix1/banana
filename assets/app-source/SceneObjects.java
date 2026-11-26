package com.bananalab.lite;
import android.content.*;import android.graphics.*;import java.util.*;
final class SceneObjects {
 private static final Map<String,Bitmap[]> cache=new HashMap<>();
 static int type(String name){String[] types={"puerta","mesa","silla","caja","lampara"};for(int i=0;i<types.length;i++)if(types[i].equals(name))return i;return -1;}
 static synchronized Map<String,Bitmap[]> cached(SceneDraft d){Map<String,Bitmap[]> out=new HashMap<>();for(SceneDraft.Prop p:d.props)if(cache.containsKey(p.type))out.put(p.id,cache.get(p.type));return out;}
 static Map<String,Bitmap[]> prepare(Context c,SceneDraft d)throws Exception{
  for(SceneDraft.Prop p:d.props){synchronized(SceneObjects.class){if(cache.containsKey(p.type))continue;}int kind=type(p.type);if(kind<0)throw new java.io.IOException("Objeto no admitido");Bitmap[] images=new Bitmap[2];
   for(int i=0;i<2;i++)try(java.io.InputStream in=c.getAssets().open("pixel-v2/props/"+kind+"-"+i+".png")){images[i]=BitmapFactory.decodeStream(in);if(images[i]==null)throw new java.io.IOException("Objeto no generado");}
   synchronized(SceneObjects.class){cache.put(p.type,images);}
  }return cached(d);
 }
 static float open(SceneDraft draft,String id,float time,int beats){SceneDraft.Prop object=draft.prop(id);float value=object!=null&&object.open?1:0;for(int i=0;i<draft.actions.size();i++)if(draft.actions.get(i).equals("abre")&&draft.target(i).equals(id))value=Math.max(value,Scene.ease(Math.max(0,Math.min(1,time*beats-draft.slot(i)))));return value;}
}
