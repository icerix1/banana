package com.bananalab.lite;
import android.content.*;import java.util.regex.*;
/** Clarification of bounded interface requests, never guesses arbitrary code. */
final class RequestDialogue{
 static final long TTL=600000;
 static final class Ask{String kind="",target="",question="";Ask(String k,String t,String q){kind=k;target=t;question=q;}}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-request-dialogue",0);}
 static Ask incomplete(String input){String q=NaturalCoding.normalize(input);if(q.startsWith("no "))return null;
  Matcher button=Pattern.compile("(?:cambia|cambiar|modifica|pone) (?:el )?(?:color (?:del |de |al ))?(?:boton)(?: (?:de )?([a-z0-9 ]+?))?(?: de color| a| en)?").matcher(q);
  if(button.matches()){String target=button.group(1)==null?"":button.group(1).trim();if(target.matches("(?:el )?color"))target="";if(target.isEmpty())return new Ask("buttonTarget","","¿Qué botón querés cambiar? Decime su nombre, por ejemplo Enviar.");if(target.split(" ").length<=4&&!target.matches(".*\\b(rojo|azul|verde|violeta|amarillo|original)\\b.*"))return new Ask("buttonColor",target,"¿Qué color querés para el botón «"+target+"»? Puedo usar rojo, azul, verde, violeta, amarillo u original.");}
  if(q.matches("(?:cambia|cambiar|modifica|pone|usa) (?:la )?(?:fuente|tipografia|tipo de letra)(?: de (?:la app|banana|todos los textos))?(?: a| por)?"))return new Ask("font","","¿Qué fuente preferís? Sans serif, serif, monospace, original, o decime «elegí vos».");
  if(q.matches("(?:cambia|cambiar|pone) (?:el )?(?:tema|fondo)(?: de (?:la app|banana|el chat))?(?: a)?"))return new Ask("theme","","¿Qué tema querés: oscuro, azul, verde o violeta?");
  return null;
 }
 static String complete(String kind,String target,String reply){String q=NaturalCoding.normalize(reply).replaceFirst("^(?:mejor |en |de color |el color )","");
  if(kind.equals("buttonColor")&&q.matches("rojo|azul|verde|violeta|amarillo|original"))return "pone el boton "+target+" "+q;
  if(kind.equals("theme")&&q.matches("oscuro|azul|verde|violeta"))return "pone el tema de la app "+q;
  if(kind.equals("font")){if(q.matches("elegi vos|elige vos|la que quieras|a tu gusto|vos elegi|elegila vos"))return "cambia la fuente de la app a tu gusto";if(q.matches("serif|sans serif|monospace|original|sans serif medium"))return "cambia la fuente de la app a "+q;}
  return null;
 }
 static boolean targetAnswer(String q){return (q.matches("enviar|herramientas|proyectos|crear video|guardar|generar|reproducir|cerrar")||q.startsWith("boton ")||q.startsWith("el boton "))&&q.matches("(?:el )?(?:boton (?:de )?)?[a-z0-9]+(?: [a-z0-9]+){0,3}")&&!q.matches("(?s).*\\b(?:cambia|aprende|crea|borra|olvida|hace|no|si|rojo|azul|verde|hola|gracias|cancela|salir|que|como)\\b.*");}
 static String buttonName(String raw){return NaturalCoding.normalize(raw).replaceFirst("^(?:el )?boton (?:de )?","").trim();}
 static boolean fresh(long saved,long now,int revision,int current){return now>=saved&&now-saved<=TTL&&revision==current;}
 static void clear(Context c){prefs(c).edit().clear().commit();}
 static BananaEngine.Result handle(Context c,String input)throws Exception{SharedPreferences p=prefs(c);String kind=p.getString("kind",""),q=NaturalCoding.normalize(input);long now=System.currentTimeMillis();int revision=AppJavaWorkspace.prefs(c).getInt("revision",0);
  if(!kind.isEmpty()){
   if(q.matches("cancela|cancelalo|olvidalo|dejalo|no importa")){clear(c);return MobileCore.say(c,"Cancelé el pedido pendiente. No cambié la interfaz.","app_aclaracion");}
   boolean valid=fresh(p.getLong("saved",0),now,p.getInt("revision",-1),revision);
   String command=complete(kind,p.getString("target",""),input);
   if(command!=null){clear(c);if(!valid)return MobileCore.say(c,"El pedido pendiente venció o la interfaz cambió. Decime otra vez qué querés cambiar; no apliqué ese ajuste.","app_aclaracion");return NaturalCoding.handle(c,command);}
   if(kind.equals("buttonTarget")&&targetAnswer(q)){if(!valid){clear(c);return MobileCore.say(c,"El pedido pendiente venció. Decime otra vez qué botón querés cambiar.","app_aclaracion");}String name=buttonName(input);Ask next=new Ask("buttonColor",name,"¿Qué color querés para el botón «"+name+"»? Rojo, azul, verde, violeta, amarillo u original.");return save(c,next,now,revision);}
   // Do not consume a new task as the answer to an older question.
   clear(c);
  }
  Ask ask=incomplete(input);return ask==null?null:save(c,ask,now,revision);
 }
 static BananaEngine.Result save(Context c,Ask ask,long now,int revision)throws Exception{if(!prefs(c).edit().putString("kind",ask.kind).putString("target",ask.target).putLong("saved",now).putInt("revision",revision).commit())throw new java.io.IOException("No pude guardar la pregunta pendiente");return MobileCore.say(c,ask.question+" No hice cambios todavía.","app_aclaracion");}
}
