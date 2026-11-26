package com.bananalab.lite;
import android.content.*;import java.util.regex.*;
/** Corrections of the conversation take priority over scene/editor intent detection. */
final class ConversationFeedback{
 static final class Plan{String kind,text;Plan(String k,String t){kind=k;text=t;}}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-conversation-feedback",0);}
 static Plan parse(String raw){String q=ChatLanguage.normalize(NaturalCoding.polite(raw));
  if(q.matches("no gracias|no muchas gracias|no hace falta|no te preocupes"))return null;
  if(q.matches("no (?:el|la) que (?:entra|entro|baila|salta|habla) (?:tiene que ser|deberia ser|debe ser) (?:azul|rojo|roja|verde|amarillo|amarilla|violeta|rosa|blanco|blanca|negro|negra)"))return null;
  if(q.matches("(?:respondeme|responde|hablame) mas (?:corto|breve)|no (?:me respondas tan largo|escribas tanto|tan largo)|respuestas mas breves"))return new Plan("style","breve");
  if(q.matches("(?:respondeme|responde) mas (?:detallado|largo)|explicame con mas detalle|no tan breve|desarrolla mas"))return new Plan("style","detallado");
  if(q.matches("no era eso|no me entendiste|me entendiste mal|eso esta mal|eso no es asi|eso no responde(?: mi pregunta)?|no respondiste mi pregunta|te estoy corrigiendo|estoy corrigiendote|no abras el editor(?: del video)?|no quiero editar el video|estoy hablando del chat"))return new Plan("clarify","");
  Matcher m=Pattern.compile("^(?:no[ ,:]+|te corrijo[ ,:]+|la correcci[oó]n es[ :]+|eso est[aá] mal[ ,:]+)(.+)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.DOTALL).matcher(raw.trim());if(!m.matches())return null;String body=m.group(1).trim(),n=ChatLanguage.normalize(body);
  if(body.length()>8000)return new Plan("clarify","");if(n.matches("(?s).*(?:\\bes\\b|\\bson\\b|se llama|significa|\\btiene\\b|esta en|queda en|se usa).*" )&&!n.matches("(?s)^(?:quiero|necesito|hace|haz|crea|cambia|borra|reconstruye|no ) .*"))return new Plan("fact",body);
  return new Plan("clarify",body);
 }
 static void observe(Context c,String input,BananaEngine.Result out){if(parse(input)!=null||ChatLanguage.socialReply(input)!=null)return;prefs(c).edit().putString("question",input.substring(0,Math.min(8000,input.length()))).putString("answer",out.text.substring(0,Math.min(4000,out.text.length()))).apply();}
 static BananaEngine.Result handle(Context c,String input)throws Exception{Plan p=parse(input);if(p==null)return null;RequestDialogue.clear(c);
  if(p.kind.equals("style")){MobileCore.prefs(c).edit().putString("style",p.text).commit();return MobileCore.say(c,p.text.equals("breve")?"Voy a responder más breve en las consultas al conocimiento guardado y las lecciones locales.":"Voy a desarrollar más las consultas al conocimiento guardado y las lecciones locales.","correccion_chat");}
  if(p.kind.equals("fact")){String receipt=KnowledgeNotebook.remember(c,"Corrección del usuario",p.text,"user","user");String question=prefs(c).getString("question","");String answer=KnowledgeNotebook.reply(c,question);return MobileCore.say(c,"Tomé tu corrección como aporte del usuario: «"+p.text+"».\n"+(answer==null?receipt:"Revisando tu pregunta anterior:\n"+answer)+"\nLa conservé en el cuaderno; no cambié el video ni abrí su editor.","correccion_chat");}
  MobileCore.prefs(c).edit().putBoolean("repair",true).commit();String last=prefs(c).getString("question","");return MobileCore.say(c,"Entendí que estás corrigiendo la conversación."+(last.isEmpty()?"":" Tu último pedido fue: «"+last.substring(0,Math.min(400,last.length()))+"».")+"\n"+(p.text.isEmpty()?"Decime qué dato o qué interpretación debería corregir.":"Recibí esta aclaración: «"+p.text+"». Decime qué parte de mi respuesta querés reemplazar.")+" No abrí el editor ni cambié la escena.","correccion_chat");
 }
}
