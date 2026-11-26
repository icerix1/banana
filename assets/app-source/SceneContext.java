package com.bananalab.lite;
import java.util.regex.*;
/** Resolves explicit discourse selectors before action verbs are scanned. */
final class SceneContext{
 static final String REF="(?:el|la) que (?:recibio (?:primero )?(?:la |el )?(?:caja|lampara)(?: de [a-z]{2,20})?|(?:tiene|sostiene) (?:la |el )?(?:caja|lampara)|entro|entra|llego|aparecio|habla|baila|salta)|(?:el|la) (?:de antes|anterior|otro|otra|primero|segundo|tercero|cuarto|ultimo|protagonista|seleccionado|seleccionada)|ese personaje|esa personaje|ese|esa|este|esta|ella";
 static String clean(String q){return q.replaceAll("(?: y |,? )(?:deja|dejame|mantene|conserva) (?:todo (?:lo )?demas|el resto|todo lo otro) igual$","").replaceAll("(?: y )?(?:sin cambiar|sin modificar) (?:todo (?:lo )?demas|el resto)$","").trim();}
 static final class Result{String text,error,unresolved;SceneDraft selection;Result(String q){text=q;}}
 static Result rewrite(String input,SceneDraft base){String q=clean(CreationRequest.normalize(input)).replaceAll("\\bdel (?=(?:anterior|protagonista|primero|segundo|tercero|cuarto|ultimo|otro)\\b)","de el ").replaceAll("\\bal (?=(?:que|anterior|de antes|otro|primero|segundo|tercero|cuarto|ultimo|protagonista|seleccionado)\\b)","a el ");Result r=new Result(q);if(base==null)return r;
  Matcher select=Pattern.compile("(?:ahora )?(?:selecciona|elegi|enfoca)(?: a| al)? (.+)").matcher(q);if(select.matches()){SceneDraft.Actor a=ScenePlanner.resolve(base,select.group(1),false);if(a==null){r.error="No pude identificar un único personaje para seleccionarlo. Nombralo.";return r;}try{r.selection=base.copy();r.selection.focused=a.name;}catch(Exception e){r.error="No pude recuperar la escena.";}return r;}
  if(q.matches("(?s).*\\bel (?=(?:"+ScenePlanner.VERBS+")\\b).*")){SceneDraft.Actor a=base.get(base.focused);if(a==null){r.error="Nombrá al personaje para esa acción.";return r;}q=q.replaceAll("\\bel (?=(?:"+ScenePlanner.VERBS+")\\b)",Matcher.quoteReplacement(a.name+" "));}
  Matcher refs=Pattern.compile("\\b(?:"+REF+")\\b").matcher(q);StringBuffer b=new StringBuffer();while(refs.find()){SceneDraft.Actor a=SceneResolver.special(base,refs.group());if(a==null){r.unresolved=refs.group();r.error="No pude resolver «"+refs.group()+"» de forma única. Nombrá al personaje; conservé la escena anterior.";return r;}refs.appendReplacement(b,Matcher.quoteReplacement(a.name));}refs.appendTail(b);r.text=b.toString();return r;
 }
 static void focus(SceneDraft before,SceneDraft after){if(before!=null){after.previousFocus=after.focused.equals(before.focused)?before.previousFocus:before.focused;}if(after.get(after.previousFocus)==null||after.previousFocus.equals(after.focused))after.previousFocus="";}
}
