package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
final class TimelineCommands{
 static CreatorSession.Result apply(Semilla model,String text,SceneDraft base)throws Exception{
  if(base==null)return null;
  if(text.matches("agrega ambiente y pasos|activa el sonido|agrega sonido|sin sonido|desactiva el sonido")){SceneDraft d=base.copy();d.sound=!text.matches("sin sonido|desactiva el sonido");return new CreatorSession.Result(true,d.sound?"Activé ambiente y pasos sintetizados para preview y MP4.":"Desactivé el sonido.",d);}

  if(text.matches("mostra a los dos|mostra a todos|plano general|camara general")){SceneDraft d=base.copy();d.camera="wide";return new CreatorSession.Result(true,"Cámara en plano general: todos los personajes.",d);}
  Matcher camera=Pattern.compile("(segui a|acercate a|primer plano de) (?:el |la )?([a-z]+)").matcher(text);
  if(camera.matches()){SceneDraft d=base.copy();if(d.get(camera.group(2))==null)return CreatorSession.say("¿A qué personaje querés enfocar?");d.camera=(camera.group(1).equals("segui a")?"follow:":"close:")+camera.group(2);return new CreatorSession.Result(true,"Actualicé la cámara.",d);}
  if(text.matches("mostrame la secuencia|ver secuencia|que entendiste|resumi la secuencia"))return CreatorSession.say(base.describe());
  Matcher change=Pattern.compile("(?:cambia|corregi) el paso ([1-9]|[1-5][0-9]|6[0-4]) a ([a-z]+)").matcher(text);
  if(change.matches()){SceneDraft d=base.copy();int i=Integer.parseInt(change.group(1))-1;String action=Timeline.action(change.group(2),change.group(2));if(i>=d.actions.size())return CreatorSession.say("Ese paso todavía no existe.");if(!Timeline.valid(action))return CreatorSession.say("No reconozco esa acción.");d.actions.set(i,action);return new CreatorSession.Result(true,"Corregí solo el paso "+(i+1)+".",d);}
  Matcher correction=Pattern.compile("(?:no )?(?:el |la )?([a-z]+) (?:tenia que|debe|deberia) ([a-z]+)").matcher(text);
  if(correction.matches()){SceneDraft d=base.copy();SceneDraft.Actor actor=d.get(correction.group(1));String action=Timeline.action(correction.group(2),correction.group(2));if(actor==null||!Timeline.valid(action))return CreatorSession.say("Decime el personaje y una acción que admita Banana.");for(int i=d.actions.size()-1;i>=0;i--)if(d.actors.get(i)==d.cast.indexOf(actor)){d.actions.set(i,action);return new CreatorSession.Result(true,"Corregí la acción de "+actor.name+" conservando los tiempos.",d);}return CreatorSession.say("Ese personaje todavía no tiene una acción para corregir.");}
  if(text.equals("hace que se miren")||text.equals("que se miren")){if(base.cast.size()!=2)return CreatorSession.say("Para esta interacción elegí una escena con dos personajes.");SceneDraft d=base.copy();if(d.actions.size()>SceneTiming.MAX_ACTIONS-2)return CreatorSession.say("No quedan pasos libres.");int next=0;for(int i=0;i<d.actions.size();i++)next=Math.max(next,d.slot(i)+1);if(next>=SceneTiming.MAX_BEATS)return CreatorSession.say("No quedan tiempos libres.");for(int i=0;i<2;i++){d.actions.add("mira");d.actors.add(i);d.slots.add(next);}return new CreatorSession.Result(true,"Ambos se miran en el mismo tiempo.",d);}
  Matcher scheduled=Pattern.compile("(?:el |la )?(banana|pepino|gato|conejo|robot|perro|oso|zorro|persona|tomate|criatura) (entraizq|entrader|saleizq|saleder)").matcher(Timeline.language(text));
  if(scheduled.matches()){SceneDraft d=base.copy();SceneDraft.Actor a=d.get(scheduled.group(1));if(a==null)return CreatorSession.say("Ese personaje no está en la escena.");int next=0;for(int i=0;i<d.actions.size();i++)next=Math.max(next,d.slot(i)+1);if(next>=SceneTiming.MAX_BEATS||d.actions.size()>=SceneTiming.MAX_ACTIONS)return CreatorSession.say("La secuencia ya alcanzó su límite de acciones o tiempos.");d.actors.add(d.cast.indexOf(a));d.actions.add(scheduled.group(2));d.slots.add(next);return new CreatorSession.Result(true,"Agregué la entrada o salida a la secuencia.",d);}
  return null;
 }
}
