package com.bananalab.lite;
import android.content.Context;
/** Shared scene authority used by text commands, inline editor and restore actions. */
final class BananaProject{
 static SceneDraft current(Context c)throws Exception{CreatorSession state=CreatorMemory.load(c);return state.pending!=null?state.pending.copy():SceneStore.load(c);}
 static boolean same(SceneDraft a,SceneDraft b)throws Exception{if(a==null||b==null)return a==b;org.json.JSONObject x=a.json(),y=b.json();x.remove("focused");y.remove("focused");x.remove("previousFocus");y.remove("previousFocus");return x.toString().equals(y.toString());}
 static boolean isDraft(Context c)throws Exception{return CreatorMemory.load(c).pending!=null;}
 static synchronized void edit(Context c,SceneDraft expected,SceneDraft next)throws Exception{
  if(!same(current(c),expected))throw new java.io.IOException("El proyecto cambió. Abrí el resultado actual para editarlo.");
  SceneConsistency.require(next);SceneContext.focus(current(c),next);next=SceneDraft.read(next.json().toString());CreatorSession state=CreatorMemory.load(c);if(state.pending!=null){state.remember();state.pending=next;CreatorMemory.save(c,state,null);}else CreatorMemory.save(c,state,next);
 }
 static synchronized void restore(Context c,SceneDraft snapshot)throws Exception{CreatorSession state=CreatorMemory.load(c);if(state.pending!=null)throw new java.io.IOException("Publicá o descartá el borrador antes de retomar otra versión.");CreatorMemory.save(c,state,snapshot.copy());}
}
