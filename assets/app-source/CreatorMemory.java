package com.bananalab.lite;
import android.content.*;import org.json.*;
final class CreatorMemory {
 static CreatorSession load(Context c)throws Exception{
  return CreatorSession.read(c.getSharedPreferences("shared-scene",0).getString("creator",""));
 }
 static synchronized void save(Context c,CreatorSession state,SceneDraft commit)throws Exception{
  if(commit!=null)commit=SceneDraft.read(commit.json().toString());if(state.pending!=null)state.pending=SceneDraft.read(state.pending.json().toString());
  // Store creator state and scene in the same preference transaction.
  SharedPreferences p=c.getSharedPreferences("shared-scene",0);SharedPreferences.Editor editor=p.edit();
  if(commit!=null){
   String old=p.getString("current",""),data=commit.json().toString();
   if(!data.equals(old)){
    JSONArray past=SceneStore.stack(p,"undo");if(past.length()==0&&!p.getString("previous","").isEmpty())past.put(p.getString("previous",""));
    editor.putString("undo",SceneStore.push(past,old).toString()).putString("redo","[]").remove("previous").putString("current",data).putLong("revision",p.getLong("revision",0)+1);
   }
  }
  SceneDraft profile=commit!=null?commit:state.pending;if(profile!=null)editor.putString("characterArchive",CharacterArchive.updated(p.getString("characterArchive","[]"),profile).toString());
  if(!editor.putString("creator",state.json()).commit())throw new java.io.IOException("No se pudo guardar");
 }
 static void clear(Context c){c.getSharedPreferences("shared-scene",0).edit().remove("creator").apply();}
}
