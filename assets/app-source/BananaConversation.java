package com.bananalab.lite;
import android.content.*;import org.json.*;import java.util.*;
/** Existing chat history plus immutable visual snapshots; same storage for migration. */
final class BananaConversation{
 static JSONArray load(Context c){try{return new JSONArray(c.getSharedPreferences("banana-chat",0).getString("history","[]"));}catch(Exception e){return new JSONArray();}}
 static String topic(Context c){return c.getSharedPreferences("banana-chat",0).getString("topic","");}
 static void save(Context c,JSONArray rows,String topic)throws Exception{if(!c.getSharedPreferences("banana-chat",0).edit().putString("history",rows.toString()).putString("topic",topic).commit())throw new java.io.IOException("No se pudo guardar la conversación; el proyecto se conserva.");}
 static JSONArray append(JSONArray old,String who,String text,SceneDraft scene,boolean draft)throws Exception{JSONArray rows=new JSONArray();for(int i=Math.max(0,old.length()-39);i<old.length();i++)rows.put(old.getJSONObject(i));JSONObject row=new JSONObject().put("who",who).put("text",text);if(scene!=null)row.put("scene",scene.json()).put("draft",draft);rows.put(row);return rows;}
 static List<String[]> context(JSONArray rows)throws Exception{List<String[]> out=new ArrayList<>();for(int i=0;i<rows.length()-1;i++){JSONObject u=rows.getJSONObject(i),a=rows.getJSONObject(i+1);if(u.optString("who").equals("Vos")&&a.optString("who").equals("Banana")){String text=a.optString("text");if(text.startsWith("[Banana Chat")||text.startsWith("[Transformer")||text.startsWith("[Texto experimental"))text=text.substring(text.indexOf(']')+1).trim();out.add(new String[]{u.optString("text"),text});}}while(out.size()>ModularHub.view().context)out.remove(0);return out;}
 static SceneDraft snapshot(JSONObject row)throws Exception{return row.has("scene")?SceneDraft.read(row.getJSONObject("scene").toString()):null;}
}
