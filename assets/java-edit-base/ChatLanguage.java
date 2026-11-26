package com.bananalab.lite;
/** Conversacion social antes de clasificar o consultar fuentes. */
final class ChatLanguage{
 static String normalize(String text){return Knowledge.normalize(text).replaceAll("[^a-z0-9 ]"," ").replaceAll("\\bhol+a+s*\\b","hola").replaceAll("\\bbue+n+a+s+\\b","buenas").replaceAll("\\bhe+y+\\b","hey").replaceAll("\\bgra+c+i+a+s+\\b","gracias").replaceAll("\\bcha+u+\\b","chau").replaceAll("\\s+"," ").trim();}
 static String withoutGreeting(String text){return normalize(text).replaceFirst("^(?:(?:hola|buenas|hey|buen dia|buenas tardes|buenas noches)(?: banana)?(?: che)?\\s+)","").trim();}
 static String socialReply(String message){
  String text=normalize(message).replaceFirst("^banana ","");
  if(text.matches("(?:hola|buenas|hey|buen dia|buenas tardes|buenas noches)(?: banana)?(?: como (?:estas|andas)| todo bien)?"))return "¡Hola! ¿Cómo andás? ¿De qué tenés ganas de hablar?";
  if(text.matches("(?:y )?(?:vos|tu)? ?(?:como (?:estas|andas)|todo bien|que onda|que haces)(?: banana)?"))return "Acá estoy, listo para charlar con vos. ¿Cómo venís?";
  if(text.matches("(?:yo )?(?:bien|todo bien|muy bien|joya|re bien|bien gracias)(?: y vos| y tu)?"))return "¡Me alegro! ¿Querés charlar de algo o pensar una idea para una animación?";
  if(text.matches("(?:quiero (?:charlar|hablar|conversar)(?: con vos)?|charlemos|hablemos|estoy aburrid[oa]|me aburro)"))return "Dale, charlemos. Contame qué estuviste haciendo o qué te gustaría crear.";
  if(text.matches("(?:muchas |mil )?gracias(?: banana| por todo)?|te agradezco"))return "¡De nada! ¿Seguimos con algo más?";
  if(text.matches("(?:jaja)+|(?:jeje)+|jajaja+"))return "Jajaja 😄 ¿Qué pasó?";
  if(text.matches("chau(?: banana)?|adios|hasta luego|nos vemos"))return "¡Nos vemos! Cuando quieras seguimos charlando.";
  return null;
 }
}
