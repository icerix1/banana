package com.bananalab.lite;
import java.util.*;
/** All-or-nothing local scene tasks. Memory/network/history are outside this transaction. */
final class TaskFlow{
 static CreatorSession.Result apply(Semilla model,String input,SceneDraft base,CreatorSession owner,boolean defer)throws Exception{
  String q=SceneContext.clean(CreationRequest.normalize(input.replaceAll(";|\\.\\s+"," taskseparator "))).replaceAll("^(?:deja|dejame|mantene|conserva) todo igual(?:,)? (?:pero|y) ","");
  // Keep action-only timing sentences with the established simultaneous planner.
  boolean explicit=q.contains("taskseparator");if(!explicit&&q.matches("(?s)^(?:continua|segui|sigue|agrega al final|y despues|despues|reemplaza la secuencia|reinicia la secuencia|rehace la secuencia) .*"))return null;if(explicit&&!q.matches("(?s)^(?:crea|creame|dibuja|dibujame|agrega|cambia|hace|haceme|pone|reemplaza|deja|quiero|necesito|continua|segui|sigue)\\b.*"))return null;
  boolean edits=q.matches("(?s).*(?:cambia|reemplaza|sustitui|ojos grandes|ojos pequenos|mas alto|mas alta|pone el fondo).*\\b(?:y|despues|luego)\\b.*")||q.matches("(?s).*\\by (?:cambia|reemplaza|sustitui)\\b.*");
  if(!explicit&&!edits&&!q.matches("(?s).*(?:por un|por una) (?:"+SceneDirector.TYPES+") y hacel[oa] .*"))return null;
  q=q.replace("taskseparator"," ; ");q=q.replaceAll("(?:,? y despues|,? y luego|;|\\.)\\s*"," ; ");if(edits||q.contains(" por un ")||q.contains(" por una "))q=q.replaceAll(" y (?=(?:hace|hacelo|hacela|cambia|reemplaza|pone|quita|saca|agrega|sin niebla|mucha niebla)\\b)"," ; ");
  String[] raw=q.split("\\s*;\\s*");List<String> steps=new ArrayList<>();for(String s:raw){s=s.replaceAll("^(?:despues |luego |ahora |que )","").replaceAll("(?:sin dibujar|solo planear)$","").trim();if(!s.isEmpty())steps.add(s);}if(steps.size()<2)return edits?CreatorSession.say("Separá esos cambios con ‘y después’ para que pueda comprobar cada paso. Conservé la escena anterior."):null;if(steps.size()>8)return CreatorSession.say("Dividí el pedido en hasta ocho pasos. Conservé la escena anterior.");
  CreatorSession sandbox=new CreatorSession();sandbox.composing=true;SceneDraft current=base==null?null:base.copy();
  for(int i=0;i<steps.size();i++){String step=steps.get(i);if(step.matches("(?s).*(?:borrador|deshacer|rehacer|recorda|recuerdo|internet|referencia|publica|descarta).*"))return CreatorSession.say("Ese pedido combina operaciones que no puedo aplicar juntas. Separá memoria, borradores o referencias de los cambios de escena. Conservé la escena anterior.");
   if(step.contains(" y ")&&!step.startsWith("reemplaza la secuencia")&&step.matches("(?s)^(?:cambia|pone|hace|quita|saca|reemplaza).*"))return CreatorSession.say("No pude separar todos los cambios del paso "+(i+1)+". Usá ‘y después’ entre ellos. Conservé la escena anterior.");
   CreatorSession.Result result=sandbox.apply(model,step,current);if(!result.handled||result.commit==null)return CreatorSession.say("No apliqué el pedido. Paso "+(i+1)+": «"+step+"». "+(result.handled?result.text:"No entendí esa parte; decime personaje y cambio.")+"\nLa escena anterior se conserva completa.");
   String problem=BananaPixel.limitation(result.commit);if(problem!=null)return CreatorSession.say("No apliqué el pedido: "+problem+". La escena anterior se conserva completa.");current=SceneDraft.read(result.commit.json().toString());
  }
  String receipt=TaskReceipt.describe(base,current,steps.toArray(new String[0]));owner.lastTask=receipt;return owner.update(current,defer||owner.pending!=null,receipt,false);
 }
}
