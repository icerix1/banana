package com.bananalab.lite;
/** Memoria explicita, local y acotada para conversar sin inventar datos del usuario. */
final class DialogueState{
 String name="",interest="",last="";
 String reply(String message){
  String text=ChatLanguage.normalize(message);if(text.matches("como te llamas|quien sos|que sos"))return "Soy Banana. Puedo ayudarte a conversar, preparar escenas y corregir sus personajes y acciones.";java.util.regex.Matcher named=java.util.regex.Pattern.compile("(?:me llamo|mi nombre es) ([a-z]{2,20}(?: [a-z]{2,20})?)(?: y vos| y tu)?").matcher(text);
  if(named.matches()){name=named.group(1);last="name";return "¡Un gusto, "+name+"! Yo soy Banana. Lo voy a recordar en este chat. ¿Qué te gustaría hacer?";}
  if(text.matches("como me llamo|cual es mi nombre|te acordas de mi nombre"))return name.isEmpty()?"Todavía no me dijiste tu nombre. ¿Cómo te llamás?":"Me dijiste que te llamás "+name+".";
  if(text.startsWith("me gusta ")&&text.length()<75){interest=text.substring(9).trim();last="interest";return "Lo tengo en cuenta: te gusta "+interest+". ¿Qué es lo que más disfrutás de eso?";}
  if(text.matches("que me gusta|te acordas que me gusta"))return interest.isEmpty()?"Todavía no me contaste qué te gusta.":"Me contaste que te gusta "+interest+".";
  if(text.matches("(?:estoy|me siento) (?:triste|bajoneado|bajoneada|preocupado|preocupada|cansado|cansada)|tuve un mal dia|hoy me fue mal")){last="listen";return "Qué bajón. Si querés, contame qué pasó; te escucho.";}
  if(text.matches("estoy (?:feliz|contento|contenta)|hoy me fue bien|tuve un buen dia")){last="listen";return "¡Qué bueno! ¿Qué pasó hoy que te puso así?";}
  if(text.matches("no se de que hablar|no se que hacer|dame una idea|sugeri un tema")){last="choose";return interest.isEmpty()?"Podemos charlar de algo que te guste o inventar una escena divertida. ¿Preferís una historia, personajes o contarme tu día?":"Podemos seguir hablando de "+interest+" o usarlo para inventar una escena. ¿Qué preferís?";}
  if(text.matches("si|dale|bueno|ok|oka|de una")){if(last.equals("listen"))return "Te escucho. ¿Qué pasó?";if(last.equals("choose"))return "Dale. Elegí una: inventamos una historia o me contás algo de tu día.";return "Dale. Contame un poco más para seguirte.";}
  if(text.matches("no entendes|no me entendiste|eso no era|no era eso")){last="clarify";return "Gracias por decírmelo. ¿Qué parte entendí mal y cómo querías que quedara?";}
  if(text.startsWith("hoy ")||text.startsWith("ayer ")){last="listen";return "Te sigo. ¿Cómo te sentiste con eso?";}
  if(text.matches("necesito (?:hablar|charlar|conversar)(?: con vos)?")){last="listen";return "Dale, te escucho. ¿De qué te gustaría hablar?";}
  String social=ChatLanguage.socialReply(message);if(social!=null){last="social";if(!name.isEmpty()&&social.startsWith("¡Hola!"))return "¡Hola, "+name+"! ¿Cómo venís?";return social;}
  return null;
 }
}
