package com.bananalab.lite;
import android.content.*;import org.json.*;import java.util.regex.*;
final class CharacterArchive{
 static JSONArray updated(String old,SceneDraft draft)throws Exception{
  JSONArray rows=new JSONArray(old);for(SceneDraft.Actor actor:draft.cast)if(!actor.label.isEmpty()){
   SceneDraft one=new SceneDraft();one.cast.add(actor);JSONObject profile=new JSONObject().put("label",actor.label).put("actor",one.json().getJSONArray("cast").getJSONObject(0));JSONArray next=new JSONArray();boolean found=false;
   for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);if(row.getString("label").equals(actor.label)){next.put(profile);found=true;}else next.put(row);}
   if(!found&&next.length()<24)next.put(profile);rows=next;
  }return rows;
 }
 static JSONArray list(Context c)throws Exception{return new JSONArray(c.getSharedPreferences("shared-scene",0).getString("characterArchive","[]"));}
 static SceneLibrary.Action apply(Context c,String input,SceneDraft base)throws Exception{
  SceneLibrary.Action out=new SceneLibrary.Action();String q=ChatLanguage.normalize(input);
  if(base!=null&&q.matches("(?:deja|dejale|restaura|volve)(?: a .+?)? (?:la ropa|el aspecto|el diseno) de antes")){out.handled=true;SceneDraft.Actor actor=ScenePlanner.resolve(base,q,false);if(actor==null)actor=base.get(base.focused);if(actor==null){out.text="¿Qué personaje querés restaurar?";return out;}
   JSONArray history=SceneStore.stack(c.getSharedPreferences("shared-scene",0),"undo");for(int i=history.length()-1;i>=0;i--){String saved=history.getString(i);if(saved.isEmpty())continue;SceneDraft older=SceneDraft.read(saved);SceneDraft.Actor prior=older.get(actor.name);if(prior==null||!prior.kind.equals(actor.kind)||!prior.label.equals(actor.label)||prior.look.equals(actor.look))continue;SceneDraft d=base.copy();SceneDraft.Actor changed=d.get(actor.name);if(q.contains("ropa")){String category="detective|sobretodo|gabardina|abrigo|sombrero|corona|lentes|gafas|anteojos";StringBuilder clothes=new StringBuilder();for(String token:prior.look.split(" "))if(token.matches(category))clothes.append(token).append(' ');changed.look=SceneDirector.replace(changed.look,category,clothes.toString());}else{changed.look=prior.look;changed.view=prior.view;changed.reference=prior.reference;}out.scene=d;out.text="Restauré el diseño anterior de "+actor.display()+" conservando la secuencia actual.";return out;}out.text="No encontré un diseño anterior distinto para ese personaje.";return out;
  }
  if(q.equals("mis personajes")){out.handled=true;JSONArray rows=list(c);out.text=rows.length()==0?"Todavía no guardaste personajes con nombre.":"Personajes guardados:";for(int i=0;i<rows.length();i++)out.text+="\n"+rows.getJSONObject(i).getString("label");return out;}
  if(q.matches("que ves en la referencia|describi la referencia|describi esta imagen")){out.handled=true;SceneDraft.Actor a=base==null?null:base.get(base.focused);if(a==null||a.reference.isEmpty()){out.text="Primero importá una imagen desde Editar → Importar referencia en el resultado.";return out;}android.graphics.Bitmap image=BitmapFactoryCompat.decode(c,a.reference);out.text=image==null?"No pude abrir la referencia.":ReferenceImage.describe(c,image);return out;}
  Matcher m=Pattern.compile("(?:usa|agrega) (?:a )?([a-z][a-z ]{1,29})(?: de mis personajes)?").matcher(ChatLanguage.normalize(input));
  if(!m.matches())return out;JSONArray rows=list(c);for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);if(!row.getString("label").equals(m.group(1)))continue;out.handled=true;SceneDraft d=base==null?new SceneDraft():base.copy();
   for(SceneDraft.Actor a:d.cast)if(a.label.equals(m.group(1))){d.focused=a.name;out.scene=d;out.text="Seleccioné a "+a.label+".";return out;}
   if(d.cast.size()>=4){out.text="La escena admite cuatro personajes.";return out;}
   JSONObject data=new JSONObject().put("cast",new JSONArray().put(row.getJSONObject("actor"))).put("steps",new JSONArray());SceneDraft.Actor profile=SceneDraft.read(data.toString()).cast.get(0);
   profile.name=SceneDirector.unique(d,profile.kind);profile.x=SceneConversation.freePosition(d);d.cast.add(profile);d.focused=profile.name;out.scene=d;out.text="Agregué a "+profile.label+" conservando su diseño.";return out;
  }return out;
 }
}
