package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
/** Compositional scene planning over the supported ontology; no external model. */
final class SceneDirector{
 static final String TYPES="banana|pepino|gato|conejo|robot|perro|oso|zorro|persona|tomate|criatura";
 static boolean word(String text,String value){return !value.isEmpty()&&Pattern.compile("\\b"+Pattern.quote(value)+"\\b").matcher(text).find();}
 static SceneDraft.Actor subject(SceneDraft d,String text){
  SceneDraft.Actor found=null;for(SceneDraft.Actor a:d.cast)if(word(text,a.name)||word(text,a.label)){if(found!=null&&found!=a)return null;found=a;}if(found!=null)return found;
  for(SceneDraft.Actor a:d.cast)if(word(text,a.kind)){if(found!=null&&found!=a)return null;found=a;}
  return found!=null?found:d.get(d.focused)!=null?d.get(d.focused):d.cast.size()==1?d.cast.get(0):null;
 }
 static String unique(SceneDraft d,String kind){if(d.get(kind)==null)return kind;for(int i=2;i<=4;i++)if(d.get(kind+"_"+i)==null)return kind+"_"+i;return kind+"_4";}
 static void look(SceneDraft.Actor a,String text){
  if(word(text,"detective"))a.look+=" detective sobretodo sombrero";
  for(String color:new String[]{"rojo","roja","azul","verde","amarillo","amarilla","rosa","violeta","blanco","negro","gris","marron","naranja"})if(word(text,color))a.look=replace(a.look,"rojo|roja|azul|verde|amarillo|amarilla|rosa|violeta|blanco|negro|gris|marron|naranja",color);
  for(String accessory:new String[]{"sobretodo","gabardina","abrigo","sombrero","corona","lentes","gafas","anteojos"})if(word(text,accessory))a.look+=" "+accessory;
  a.look=a.look.trim().replaceAll("\\s+"," ");
 }
 static String replace(String look,String category,String value){return (look.replaceAll("\\b(?:"+category+")\\b","")+" "+value).trim().replaceAll("\\s+"," ");}
 static SceneDraft.Prop object(SceneDraft d,String type){for(SceneDraft.Prop p:d.props)if(p.type.equals(type))return p;SceneDraft.Prop p=new SceneDraft.Prop(type,type,76);d.props.add(p);return p;}
 static void step(SceneDraft d,SceneDraft.Actor a,String action,int slot,String target){step(d,a,action,slot,target,"");}
 static void step(SceneDraft d,SceneDraft.Actor a,String action,int slot,String target,String recipient){while(d.targets.size()<d.actions.size())d.targets.add("");while(d.recipients.size()<d.actions.size())d.recipients.add("");d.actors.add(d.cast.indexOf(a));d.actions.add(action);d.slots.add(slot);d.targets.add(target);d.recipients.add(recipient);}
 static CreatorSession.Result apply(String input,SceneDraft base)throws Exception{
  String text=ChatLanguage.withoutGreeting(input);
  Matcher named=Pattern.compile("(?:crea|creame|dibuja|dibujame|disena|haceme)(?: a)? ([a-z]{2,20})(?: una?| es una?) ("+TYPES+")(.*)").matcher(text);
  Matcher called=Pattern.compile("(?:crea|creame|dibuja|dibujame|disena|haceme) una? ("+TYPES+")(?: llamado| llamada| que se llame) ([a-z]{2,20})(.*)").matcher(text);
  if(named.matches()||called.matches()){
   boolean first=named.matches();String label=first?named.group(1):called.group(2),kind=first?named.group(2):called.group(1),extra=first?named.group(3):called.group(3);
   SceneDraft d=new SceneDraft();SceneDraft.Actor a=new SceneDraft.Actor(kind,50);a.label=label;look(a,extra);d.cast.add(a);d.focused=a.name;
   if(ScenePlanner.VERB.matcher(extra).find())return ScenePlanner.plan(label+" "+extra,d,true);
   return new CreatorSession.Result(true,"Diseñé a "+label+" como "+kind+". Podés editarlo por partes desde el resultado de este mensaje.",d);
  }
  if(base==null&&text.matches("usa una imagen como referencia|importa una referencia")){SceneDraft d=new SceneDraft();SceneDraft.Actor a=new SceneDraft.Actor("persona",50);a.label="referencia";d.cast.add(a);d.focused=a.name;CreatorSession.Result result=new CreatorSession.Result(true,"Preparé un personaje para tu referencia.",d);result.studioId=a.name;result.importReference=true;return result;}
  if(base==null)return null;
  if(text.matches("mostrame su diseno|mostrame el diseno|ver diseno|editar personaje|usa una imagen como referencia|importa una referencia")){
   SceneDraft.Actor a=subject(base,text);if(a==null)return CreatorSession.say("¿Qué personaje querés abrir? Nombralo primero.");
   CreatorSession.Result r=CreatorSession.say("Abriendo el diseño de "+a.display()+".");r.studioId=a.name;r.importReference=text.contains("referencia");return r;
  }
  Matcher add=Pattern.compile("(?:agrega|anade) (?:otro |otra |un |una )?("+TYPES+")(?: llamado| llamada| que se llame) ([a-z]{2,20})(.*)").matcher(text);
  if(add.matches()){SceneDraft d=base.copy();if(d.cast.size()>=4)return CreatorSession.say("La escena admite cuatro personajes.");if(d.reference(add.group(2))!=null)return CreatorSession.say("Ese nombre ya está en la escena.");SceneDraft.Actor a=new SceneDraft.Actor(unique(d,add.group(1)),SceneConversation.freePosition(d));a.kind=add.group(1);a.label=add.group(2);look(a,add.group(3));d.cast.add(a);d.focused=a.name;return new CreatorSession.Result(true,"Agregué a "+a.label+" con identidad propia.",d);}
  Matcher prop=Pattern.compile("(?:agrega|pone) (?:un |una )?(puerta|mesa|silla|caja|lampara)").matcher(text);
  if(prop.matches()){SceneDraft d=base.copy();if(d.props.size()>=6)return CreatorSession.say("La escena admite seis objetos.");object(d,prop.group(1));return new CreatorSession.Result(true,"Agregué "+prop.group(1)+" al escenario.",d);}
  if(text.matches("deja la puerta abierta|deja la puerta cerrada|cierra la puerta")){SceneDraft d=base.copy();object(d,"puerta").open=text.contains("abierta");return new CreatorSession.Result(true,"Actualicé el estado inicial de la puerta.",d);}
  // Plan a multi-clause interaction while preserving every existing design.
  if(text.contains("puerta")&&text.matches("(?s).*\\b(?:abre|abrir|abrirla|abrila)\\b.*")){
   SceneDraft d=base.copy();SceneDraft.Actor a=subject(d,text);if(a==null)return CreatorSession.say("¿Quién debe abrir la puerta?");
   SceneDraft.Prop door=object(d,"puerta");d.actions.clear();d.actors.clear();d.slots.clear();d.targets.clear();d.recipients.clear();int slot=0;
   if(text.matches("(?s).*\\b(?:caminar|camina|camine|ir|anda|acerca|hasta)\\b.*"))step(d,a,"acerca",slot++,door.id);
   step(d,a,"abre",slot++,door.id);
   if(text.matches("(?s).*\\b(?:encontrar|encuentra|encontrarse)\\b.*")){
    Matcher target=Pattern.compile("(?:encontrar|encuentra|encontrarse)(?: con)? (?:a |al )?(?:el |la |un |una )?("+TYPES+")").matcher(text);
    if(target.find()){String kind=target.group(1);SceneDraft.Actor other=d.reference(kind);if(other==a)other=null;if(other==null){if(d.cast.size()>=4)return CreatorSession.say("No queda lugar para el personaje que encuentra.");other=new SceneDraft.Actor(unique(d,kind),84);other.kind=kind;d.cast.add(other);}step(d,other,"entrader",Math.max(0,slot-1),"");}
   }
   if(text.matches("(?s).*sorprend.*"))step(d,a,"sorprende",slot++,"");
   d.focused=a.name;d.camera="wide";return new CreatorSession.Result(true,"Preparé el recorrido, la puerta y la reacción. Conservé cara, ropa y nombres.\n"+d.describe(),d);
  }
  if(text.matches("(?s).*sost(?:enga|iene|ener).*(?:caja|lampara).*")){SceneDraft d=base.copy();SceneDraft.Actor a=subject(d,text);if(a==null)return CreatorSession.say("¿Quién debe sostener el objeto?");String kind=text.contains("caja")?"caja":"lampara";SceneDraft.Prop p=object(d,kind);int next=0;for(int i=0;i<d.actions.size();i++)next=Math.max(next,d.slot(i)+1);if(next>=SceneTiming.MAX_BEATS||d.actions.size()>=SceneTiming.MAX_ACTIONS)return CreatorSession.say("No quedan pasos libres.");step(d,a,"sostiene",next,p.id);return new CreatorSession.Result(true,"Preparé al personaje sosteniendo "+kind+".",d);}
  Matcher spatial=Pattern.compile("(?:pone|ponelo|coloca)(?: a)? (.+?) (detras|delante) de (?:la |el )?(puerta|mesa|silla|caja|lampara)").matcher(text);
  if(spatial.matches()){SceneDraft d=base.copy();SceneDraft.Actor a=subject(d,spatial.group(1));if(a==null)return CreatorSession.say("¿Qué personaje querés ubicar?");SceneDraft.Prop p=object(d,spatial.group(3));a.x=p.x;a.relation=(spatial.group(2).equals("detras")?"detras ":"delante ")+p.id;return new CreatorSession.Result(true,"Actualicé la posición respecto del objeto.",d);}
  if(text.matches("(?s).*(?:se siente|sentate|sienta|sentar|sentado).*(?:silla).*")){SceneDraft d=base.copy();SceneDraft.Actor a=subject(d,text);if(a==null)return CreatorSession.say("¿Quién debe sentarse?");SceneDraft.Prop p=object(d,"silla");a.x=p.x;int next=0;for(int i=0;i<d.actions.size();i++)next=Math.max(next,d.slot(i)+1);if(next>=SceneTiming.MAX_BEATS||d.actions.size()>=SceneTiming.MAX_ACTIONS)return CreatorSession.say("No quedan pasos disponibles.");step(d,a,"sienta",next,p.id);return new CreatorSession.Result(true,"Preparé al personaje para sentarse en la silla.",d);}
  return null;
 }
}
