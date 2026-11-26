package com.bananalab.lite;
import android.content.*;import org.json.*;import java.util.regex.*;
final class SceneLibrary{
 static JSONArray list(Context c)throws Exception{return new JSONArray(c.getSharedPreferences("banana-scenes",0).getString("scenes","[]"));}
 static void save(Context c,String name,SceneDraft draft)throws Exception{
  if(draft==null)throw new java.io.IOException("Primero prepará una escena.");name=ChatLanguage.normalize(name);if(name.isEmpty()||name.length()>40)throw new java.io.IOException("Usá un nombre de hasta 40 letras.");
  JSONArray rows=list(c),out=new JSONArray();boolean found=false;for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);if(row.getString("name").equals(name)){out.put(new JSONObject().put("name",name).put("scene",draft.json()));found=true;}else out.put(row);}
  if(!found){if(rows.length()>=8)throw new java.io.IOException("Ya guardaste ocho escenas.");out.put(new JSONObject().put("name",name).put("scene",draft.json()));}
  if(!c.getSharedPreferences("banana-scenes",0).edit().putString("scenes",out.toString()).commit())throw new java.io.IOException("No se pudo guardar.");
 }
 static final class Action{boolean handled;String text="";SceneDraft scene;}
 static Action apply(Context c,String message,SceneDraft current)throws Exception{
  Action a=new Action();String q=ChatLanguage.normalize(message);Matcher save=Pattern.compile("guarda(?: esta| la)? escena como (.+)").matcher(q),open=Pattern.compile("(?:abri|abre|carga) (?:la )?escena (.+)").matcher(q);
  if(save.matches()){save(c,save.group(1),current);a.handled=true;a.text="Guardé la escena ‘"+save.group(1)+"’ en tu serie.";return a;}
  if(open.matches()){a.handled=true;JSONArray rows=list(c);for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("name").equals(open.group(1))){a.scene=SceneDraft.read(rows.getJSONObject(i).getJSONObject("scene").toString());a.text="Abrí la escena ‘"+open.group(1)+"’.";return a;}a.text="No hay una escena guardada con ese nombre.";}
  if(q.equals("mis escenas")||q.equals("mostrame la serie")){a.handled=true;JSONArray rows=list(c);a.text=rows.length()==0?"Todavía no guardaste escenas.":"Escenas de tu serie:";for(int i=0;i<rows.length();i++)a.text+="\n"+(i+1)+". "+rows.getJSONObject(i).getString("name");}
  return a;
 }
}
