package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
/** Explicit, bounded edits over stable actor handles. No generative guessing. */
final class ProjectEdits{
 static CreatorSession.Result apply(String input,SceneDraft base)throws Exception{
  String q=CreationRequest.normalize(input).replaceAll("^(?:deja|dejame|mantene|conserva) todo igual(?:,)? (?:pero|y) ","").replaceAll("^pero ","").replaceAll("\\bal\\b","a el").replaceFirst("^hacelo ","hace este ").replaceFirst("^hacela ","hace esta ");
  Matcher swap=Pattern.compile("(?:cambia|reemplaza|sustitui)(?: solamente| solo)? (?:a |al )?(.+?) por (?:un |una |el |la )?("+SceneDirector.TYPES+")").matcher(q);
  Matcher trait=Pattern.compile("(?:hace|haceme|pone|deja)(?: solamente| solo)?(?: a)? (.+?) (?:con )?(ojos grandes|ojos pequenos|ojos normales|mas alto|mas alta|mas bajo|mas baja|mas delgado|mas delgada|mas ancho|mas ancha)").matcher(q);
  if(!swap.matches()&&!trait.matches())return null;if(base==null)return CreatorSession.say("Primero creá una escena para editar sus personajes.");
  boolean replacement=swap.matches();Matcher m=replacement?swap:trait;String who=m.group(1).replaceAll("^(?:el |la )(?!(?:que|otro|otra|protagonista)\\b)","");
  SceneDraft.Actor original=ScenePlanner.resolve(base,who,false);if(original==null)return CreatorSession.say("No pude identificar un único personaje para ese cambio. Nombralo; conservé la escena anterior.");
  SceneDraft d=base.copy();SceneDraft.Actor a=d.get(original.name);
  if(replacement){a.kind=m.group(2);a.designId=UUID.randomUUID().toString().replace("-","");a.reference="";}
  else{String value=m.group(2);String category=value.startsWith("ojos")?"ojos (?:grandes|pequenos|chicos|normales)":value.matches("mas alt[oa]|mas baj[oa]")?"alto|alta|bajo|baja":"delgado|delgada|gordo|gorda|ancho|ancha";String normalized=value.replace("mas alto","alto").replace("mas alta","alto").replace("mas bajo","bajo").replace("mas baja","bajo").replace("mas delgado","delgado").replace("mas delgada","delgado").replace("mas ancho","gordo").replace("mas ancha","gordo");a.look=(a.look.replaceAll("\\b(?:"+category+")\\b","")+" "+normalized).trim().replaceAll("\\s+"," ");}
  d.focused=a.name;SceneDraft.read(d.json().toString());return new CreatorSession.Result(true,replacement?"Reemplacé el tipo de "+original.display()+" por "+a.kind+"; conservé su lugar, nombre y acciones.":"Actualicé los rasgos de "+a.display()+" y conservé el resto.",d);
 }
}
