package com.bananalab.lite;
import android.content.*;import org.json.*;
final class SceneStore {
 static SceneDraft load(Context c){try{String data=c.getSharedPreferences("shared-scene",0).getString("current","");return data.isEmpty()?null:SceneDraft.read(data);}catch(Exception e){return null;}}
 static long revision(Context c){return c.getSharedPreferences("shared-scene",0).getLong("revision",0);}
 static JSONArray stack(SharedPreferences p,String key)throws JSONException{return new JSONArray(p.getString(key,"[]"));}
 static JSONArray push(JSONArray a,String value)throws JSONException{a.put(value);JSONArray out=new JSONArray();for(int i=Math.max(0,a.length()-20);i<a.length();i++)out.put(a.getString(i));return out;}
 static synchronized void save(Context c,SceneDraft draft)throws Exception{draft=SceneDraft.read(draft.json().toString());
  SharedPreferences p=c.getSharedPreferences("shared-scene",0);String data=draft.json().toString(),old=p.getString("current","");if(data.equals(old))return;
  JSONArray past=stack(p,"undo");if(past.length()==0&&!p.getString("previous","").isEmpty())past.put(p.getString("previous",""));
  if(!p.edit().putString("undo",push(past,old).toString()).putString("redo","[]").remove("previous").putString("current",data).putLong("revision",revision(c)+1).commit())throw new java.io.IOException("No se pudo guardar la escena");
 }
 static synchronized boolean undo(Context c){return move(c,"undo","redo");}
 static synchronized boolean redo(Context c){return move(c,"redo","undo");}
 static boolean move(Context c,String from,String to){try{
  SharedPreferences p=c.getSharedPreferences("shared-scene",0);JSONArray a=stack(p,from);
  if(a.length()==0&&from.equals("undo")&&!p.getString("previous","").isEmpty())a.put(p.getString("previous",""));
  if(a.length()==0)return false;String value=a.getString(a.length()-1);if(!value.isEmpty())SceneDraft.read(value);
  JSONArray rest=new JSONArray();for(int i=0;i<a.length()-1;i++)rest.put(a.getString(i));
  return p.edit().putString(from,rest.toString()).putString(to,push(stack(p,to),p.getString("current","")).toString()).putString("current",value).remove("previous").putLong("revision",revision(c)+1).commit();
 }catch(Exception e){return false;}}
}
