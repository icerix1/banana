package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
final class SceneConversation{
 static final String COLORS="rojo|roja|azul|verde|amarillo|amarilla|negro|negra|blanco|blanca|gris|rosa|marron|naranja|violeta";
 static final String PROPS=COLORS+"|sombrero|corona|bufanda|lentes|anteojos|gafas|bigote|sobretodo|gabardina|abrigo|alto|alta|bajo|baja|delgado|delgada|gordo|gorda|feliz|triste|sorprendido|sorprendida|sobretodo|gabardina|abrigo|detective|rubio|rubia";
 static final class Result{final boolean handled;final String reply;final SceneDraft draft;Result(boolean h,String r,SceneDraft d){handled=h;reply=r;draft=d;}}
 static Result no(){return new Result(false,"",null);}static Result ask(String text){return new Result(true,text,null);}
 static Result apply(Semilla model,String input,SceneDraft saved)throws Exception{
  String text=ChatLanguage.withoutGreeting(input);
  boolean command=text.matches("(?s).*(?:\\b(?:dibuja|dibujame|crea|crear|agrega|anade|pone|ponelo|ponela|hacelo|hacela|hace|hacele|cambia|cambialo|cambiala|pinta|pintalo|pintala|quita|saca|elimina|borra|mueve|movelo|movela|escena|dibujo)\\b).*"),correction=(text.startsWith("no ")||text.matches("(?s).*\\b(?:era|es)\\b.*"))&&text.matches("(?s).*\\b(?:era|es|azul|rojo|verde|amarillo|pequeno|grande)\\b.*");
  if(text.matches("que (?:hay|tiene) (?:en )?(?:la |el )?(?:escena|dibujo)|como quedo(?: el dibujo| la escena)?|mostrame (?:la escena|el dibujo)"))return saved==null?ask("Todavía no hay un dibujo. Decime, por ejemplo: dibujá una banana con sombrero y un pepino azul en una plaza."):ask(saved.describe()+"\nPodés verlo y reproducirlo en el resultado de esta conversación.");
  if(text.matches("(?:que|quien|como|por que|cuando|donde) .*"))return no();
  text=text.replaceAll("\\bal (primero|segundo|tercero|cuarto|ultimo)\\b","a el $1");
  // Referencias ordinales resueltas contra el estado, sin cambiar identidades.
  if(saved!=null&&!saved.cast.isEmpty()){
   String[] refs={"el primero","el segundo","el tercero","el cuarto","el ultimo"};
   for(int i=0;i<refs.length;i++){int index=i==4?saved.cast.size()-1:i;if(index<saved.cast.size()&&text.contains(refs[i]))text=text.replace(refs[i],saved.cast.get(index).name);}
  }
  boolean namedAction=false;if(saved!=null)for(SceneDraft.Actor actor:saved.cast)if(text.contains(actor.name)&&text.matches("(?s).*\\b(?:salta|baila|camina|habla|saluda|vuela|nada|corre|canta|duerme)\\b.*"))namedAction=true;
  if(saved!=null&&text.startsWith("ahora en "))command=true;
  if(saved!=null&&text.matches("(?:ahora |mas )?(?:azul|rojo|verde|amarillo|grande|pequeno|a la izquierda|a la derecha)")){text="hacelo "+text;command=true;}
  if(!command&&!correction&&!namedAction)return no();
  boolean fresh=text.matches("(?s).*\\b(?:dibuja|dibujame|crea|crear|nueva escena|nuevo dibujo)\\b.*")&&!text.matches("(?s).*\\b(?:agrega|anade|cambia|ponelo|hacelo)\\b.*");
  if(fresh||saved==null){
   if(!fresh)return ask("Primero necesitamos una escena. Probá: dibujá una banana y un pepino.");
   String request=text.replaceFirst("^(?:(?:quiero|necesito) )?(?:que )?(?:dibuja|dibujame|crea|crear)(?: una escena con| un dibujo de| una escena de)?\\s*","").replace("plaza","parque");
   OwnPlan plan;try{plan=new OwnPlan(model,request);}catch(IllegalArgumentException e){return ask("No identifiqué los personajes. ¿Qué querés dibujar? Por ejemplo: una banana con sombrero y un pepino azul.");}
   SceneDraft d=SceneDraft.fromPlan(plan);if(d.actions.size()==1&&d.actions.get(0).equals("espera")&&!request.contains("espera")){d.dialogue.clear();d.actions.clear();d.actors.clear();d.slots.clear();d.targets.clear();d.recipients.clear();}if(d.cast.isEmpty())return new Result(true,"Preparé el escenario: "+d.setting+". Podés verlo en el resultado o agregar un personaje.",d);
   for(SceneDraft.Actor actor:d.cast){if(actor.name.matches("dibuja|dibujame|crea|crear|quiero|escena|dibujo|plaza"))return ask("No identifiqué bien los personajes. Nombralos después de ‘dibujá’.");String part=segment(request,actor.name,plan.names);SceneDraft.Actor prior=saved==null?null:saved.get(actor.name);if(prior!=null){actor.look=prior.look;actor.size=prior.size;actor.label=prior.label;actor.kind=prior.kind;actor.designId=prior.designId;actor.reference=prior.reference;actor.view=prior.view;}setProperties(actor,part);setPosition(actor,part);}
   return new Result(true,"Preparé el dibujo.\n"+d.describe()+"\nPodés editarlo y reproducirlo en el resultado de este mensaje.",d);
  }
  SceneDraft d=saved.copy();
  if(text.contains("quita los movimientos")||text.contains("borra los movimientos")){d.dialogue.clear();d.actions.clear();d.actors.clear();d.slots.clear();d.targets.clear();d.recipients.clear();return new Result(true,"Dejé los personajes quietos. Sus diseños y posiciones se conservan.",d);}
  if(text.contains("fondo")||text.contains("escenario")||text.startsWith("ahora en ")){
   Matcher background=Pattern.compile("\\b(parque|plaza|playa|bosque|ciudad|montana|desierto|pasillo|casa|interior|habitacion)\\b").matcher(text);if(background.find()){d.setting=background.group(1).replace("plaza","parque");return new Result(true,"Cambié el escenario a "+d.setting+" y conservé los personajes.",d);}
  }
  if(text.contains("de noche")||text.contains("de dia")){d.setting=d.setting.replace("noche","").replace("dia","").trim()+ (text.contains("de noche")?" noche":" dia");return new Result(true,"Cambié la iluminación y conservé la escena.",d);}
  if(text.matches("(?s).*\\b(?:agrega|anade)\\b.*")){
   String request=text.replaceFirst("^.*?\\b(?:agrega|anade)\\b\\s*","");OwnPlan addition;try{addition=new OwnPlan(model,request);}catch(Exception e){return ask("¿Qué personaje querés agregar?");}
   for(int i=0;i<addition.names.size();i++){String name=addition.names.get(i);if(d.get(name)!=null)return ask("Ya hay un "+name+". Por ahora puedo distinguir uno por tipo; decime qué querés cambiarle.");if(d.cast.size()>=4)return ask("Esta escena admite hasta cuatro personajes. Podés quitar uno antes de agregar otro.");SceneDraft.Actor actor=new SceneDraft.Actor(name,freePosition(d));actor.look=addition.looks.get(i);setProperties(actor,request);d.cast.add(actor);d.focused=name;}
   return new Result(true,"Agregué el personaje y conservé el resto del dibujo.\n"+d.describe(),d);
  }
  if(d.cast.isEmpty())return ask("El escenario todavía no tiene personajes. Podés decir ‘agrega un gato’.");
  boolean pronoun=text.matches("(?s).*\\b(?:ponelo|ponela|hacelo|hacela|pintalo|pintala|movelo|movela|cambialo|cambiala)\\b.*");
  String subject=text.split("\\b(?:junto a|al lado de|detras de|delante de)\\b",2)[0];List<SceneDraft.Actor> mentioned=new ArrayList<>();for(SceneDraft.Actor a:d.cast)if(Pattern.compile("\\b"+Pattern.quote(a.name)+"\\b").matcher(subject).find())mentioned.add(a);
  SceneDraft.Actor actor=mentioned.size()==1?mentioned.get(0):pronoun?d.get(d.focused):d.cast.size()==1?d.cast.get(0):null;
  if(actor==null)return ask("¿A cuál personaje te referís? En el dibujo están: "+names(d)+". Nombralo para que cambie solo ese.");
  if(text.matches("(?s).*\\b(?:quita|saca|elimina|borra)\\b.*")&&text.matches("(?s).*\\b(?:sombrero|corona|bufanda|lentes|anteojos|gafas|bigote|sobretodo|gabardina|abrigo)\\b.*")){for(String prop:new String[]{"sombrero","corona","bufanda","lentes","anteojos","gafas","bigote","sobretodo","gabardina","abrigo"})if(text.contains(prop))actor.look=actor.look.replaceAll("\\b"+prop+"\\b","").trim();if(text.matches("(?s).*(sobretodo|gabardina|abrigo).*"))actor.look=actor.look.replaceAll("\\b(?:detective|sobretodo|gabardina|abrigo)\\b","").trim();if(text.contains("sombrero"))actor.look=actor.look.replaceAll("\\b(?:detective|sombrero)\\b","").trim();d.focused=actor.name;return new Result(true,"Le quité el accesorio a "+actor.name+". El resto se conserva.",d);}
  if(text.matches("(?s).*\\b(?:quita|saca|elimina|borra)\\b.*")){int removed=d.cast.indexOf(actor);d.cast.remove(removed);for(int i=d.actors.size()-1;i>=0;i--){int id=d.actors.get(i);if(id==removed||d.recipient(i).equals(actor.name)){d.actors.remove(i);d.actions.remove(i);if(i<d.slots.size())d.slots.remove(i);if(i<d.targets.size())d.targets.remove(i);if(i<d.recipients.size())d.recipients.remove(i);}else if(id>removed)d.actors.set(i,id-1);}for(SceneDraft.Prop prop:d.props)if(prop.holder.equals(actor.name))prop.holder="";d.focused=d.cast.isEmpty()?"":d.cast.get(d.cast.size()-1).name;return new Result(true,"Quité a "+actor.name+". Los otros personajes siguen igual.",d);}
  String before=actor.look;float oldX=actor.x,oldY=actor.y,oldSize=actor.size;String oldRelation=actor.relation;
  setProperties(actor,subject);setPosition(actor,text);
  Matcher target=Pattern.compile("(?:junto a|al lado de) (?:el |la |un |una )?([a-z]+)").matcher(text);
  if(target.find()){SceneDraft.Actor neighbor=d.get(target.group(1));if(neighbor==null||neighbor==actor)return ask("¿Junto a qué otro personaje? Podés elegir: "+names(d));actor.x=neighbor.x<=60?Math.min(88,neighbor.x+24):Math.max(12,neighbor.x-24);actor.y=neighbor.y;actor.behind=false;actor.relation="junto a "+neighbor.name;}
  if(text.matches("(?s).*\\b(?:detras del arbol|detras de un arbol|detras de el arbol)\\b.*")){actor.x=24;actor.y=65;actor.behind=true;actor.relation="detras del arbol";}
  if(text.matches("(?s).*\\b(?:delante del arbol|delante de un arbol|delante de el arbol)\\b.*")){actor.x=18;actor.y=71;actor.behind=false;actor.relation="delante del arbol";}
  boolean actionAdded=false;for(Semilla.Token token:model.analyze(text))if(!text.startsWith("no ")&&token.word.matches(Understanding.VERBS)&&token.label.matches("camina|salta|baila|habla|saluda|acerca|gira|duerme|vuela|nada|aplaude|corre|canta|espera")){if(d.actions.size()>=SceneTiming.MAX_ACTIONS)return ask("Ya hay sesenta y cuatro acciones. Para cambiar la secuencia, decime ‘quita los movimientos’ y luego agregamos otros.");int next=0;for(int k=0;k<d.actions.size();k++)next=Math.max(next,d.slot(k)+1);if(next>=SceneTiming.MAX_BEATS)return ask("La secuencia admite treinta y dos tiempos.");d.slots.add(next);d.actors.add(d.cast.indexOf(actor));d.actions.add(token.label);d.targets.add("");actionAdded=true;}
  if(text.contains("quita los movimientos")||text.contains("borra los movimientos")){d.dialogue.clear();d.actions.clear();d.actors.clear();d.slots.clear();d.targets.clear();d.recipients.clear();return new Result(true,"Dejé los personajes quietos. Sus diseños y posiciones se conservan.",d);}
  if(before.equals(actor.look)&&oldX==actor.x&&oldY==actor.y&&oldSize==actor.size&&oldRelation.equals(actor.relation)&&!actionAdded)return ask("¿Qué querés cambiarle a "+actor.name+"? Puedo ajustar color, tamaño, posición, accesorios o agregar una acción.");
  d.focused=actor.name;return new Result(true,"Listo, cambié solo a "+actor.name+".\n"+d.describe(),d);
 }
 static String segment(String text,String name,List<String> names){int start=text.indexOf(name)+name.length(),end=text.length();for(String other:names)if(!other.equals(name)){int next=text.indexOf(other,start);if(next>=0)end=Math.min(end,next);}String part=text.substring(start,end);return part.split("\\ben (?:el|la|un|una)\\b",2)[0];}
 static float freePosition(SceneDraft d){float best=18,space=-1;for(float x:new float[]{18,40,62,84}){float closest=100;for(SceneDraft.Actor a:d.cast)closest=Math.min(closest,Math.abs(a.x-x));if(closest>space){space=closest;best=x;}}return best;}
 static String names(SceneDraft d){StringBuilder b=new StringBuilder();for(SceneDraft.Actor a:d.cast){if(b.length()>0)b.append(", ");b.append(a.name);}return b.toString();}
 private static void setProperties(SceneDraft.Actor actor,String text){
  Matcher color=Pattern.compile("\\b("+COLORS+")\\b").matcher(text);String selected=null;while(color.find())selected=color.group(1);
  if(selected!=null)actor.look=actor.look.replaceAll("\\b(?:"+COLORS+")\\b","").trim()+" "+selected;
  for(String category:new String[]{"alto|alta|bajo|baja","delgado|delgada|gordo|gorda","feliz|triste|sorprendido|sorprendida"})if(Pattern.compile("\\b(?:"+category+")\\b").matcher(text).find())actor.look=actor.look.replaceAll("\\b(?:"+category+")\\b","");
  Matcher props=Pattern.compile("\\b("+PROPS+")\\b").matcher(text);while(props.find()){String prop=props.group(1);if(Pattern.compile("\\bsin (?:el |la |un |una )?"+prop+"\\b").matcher(text).find()){actor.look=actor.look.replaceAll("\\b"+prop+"\\b","");continue;}if(!prop.matches(COLORS)&&!Pattern.compile("\\b"+prop+"\\b").matcher(actor.look).find())actor.look+=" "+prop;}
  actor.look=actor.look.trim().replaceAll("\\s+"," ");
 }

 private static void setPosition(SceneDraft.Actor actor,String text){if(text.contains("izquierda")){actor.x=18;actor.behind=false;actor.relation="";}else if(text.contains("derecha")){actor.x=82;actor.behind=false;actor.relation="";}else if(text.contains("centro")){actor.x=50;actor.behind=false;actor.relation="";}if(text.matches("(?s).*\\b(?:pequeno|pequena|chico|chica)\\b.*"))actor.size=.65f;else if(text.matches("(?s).*\\b(?:grande|enorme)\\b.*"))actor.size=1.2f;}
}
