package com.bananalab.lite;
import android.content.Context;
import org.json.*;
import java.util.*;import java.util.regex.*;
final class BananaChat {
 static final class Reply{final String text,topic;Reply(String t,String p){text=t;topic=p;}}
 private static ChatMind mind;
 private static synchronized ChatMind load(Context context)throws Exception{OwnBrain.load(context);if(mind==null)mind=new ChatMind(context.getAssets().open("language/chat.bin"),context.getAssets().open("language/chat-labels.txt"));return mind;}
 static String topic(String message,String last){
  if(ChatLanguage.socialReply(message)!=null)return "";
  String text=ChatLanguage.withoutGreeting(message);
  if(!last.isEmpty()&&text.matches("(?:y )?(?:como es|como se ve|donde vive|que come|contame mas|decime mas|hablame mas|que aspecto tiene|como vive|cuanto mide|y eso)"))return last;
  java.util.regex.Matcher question=java.util.regex.Pattern.compile("^(?:que sabes sobre|busca informacion (?:de|sobre)|busca|investiga|contame (?:sobre|de)|hablame (?:del|de)|explicame (?:que es|sobre)|quiero saber (?:de|sobre)|me explicas|decime algo sobre|sabes que es|que es|como es|como se ve|de que color es|que aspecto tiene|que forma tiene|como se dibuja|describi visualmente|donde vive|que come)\\s+(.+)$").matcher(text);
  if(!question.matches())return "";
  String value=question.group(1).replaceFirst("^(?:un|una|el|la|los|las)\\s+", "").replaceAll("\\s+(?:en internet|por favor)$", "").trim();
  return value.matches("[a-z]+(?: [a-z]+){0,4}")?value:"";
 }
 static Reply reply(Context context,String message,String previous)throws Exception{
  String social=ChatLanguage.socialReply(message);if(social!=null)return new Reply(social,previous);
  ChatMind.Intent intent=load(context).classify(ChatLanguage.withoutGreeting(message));String topic=topic(message,previous);
  String text=Knowledge.normalize(message);
  // These boundaries validate intent before network use; answers remain sourced data.
  if(intent.name.equals("HELLO")&&intent.confidence>.6f&&topic.isEmpty())return new Reply("¡Hola! Soy Banana. Podemos hablar de animales, personas, lugares y otros temas. ¿Que queres saber?",previous);
  if(intent.name.equals("THANKS")&&intent.confidence>.6f)return new Reply("¡De nada! Si queres, seguimos con ese tema.",previous);
  if(intent.name.equals("GOODBYE")&&intent.confidence>.6f)return new Reply("¡Nos vemos! La conversacion queda guardada en este dispositivo.",previous);
  if(intent.name.equals("IDENTITY")&&intent.confidence>.6f)return new Reply("Soy Banana, nuestro modelo experimental propio. Mis pesos se entrenaron desde cero. Uso modulos neuronales pequenos, memoria y respuestas programadas; todavia no soy un asistente general ni puedo responder cualquier pregunta.",previous);
  if(intent.name.equals("HELP")&&intent.confidence>.6f)return new Reply("Podes preguntarme ‘¿que es un ajolote?’ o ‘¿como se ve un elefante?’. Consulto fuentes cuando hace falta y recuerdo el ultimo tema. Para crear una escena, describila en esta misma conversación. Todavia no puedo razonar ni conversar sobre cualquier asunto.",previous);
  if(intent.name.equals("MEMORY")&&intent.confidence>.6f){int count=WebMemory.count(context),examples=context.getSharedPreferences("semilla-stats",0).getInt("examples",0);return new Reply("Tengo "+count+" entradas de referencias guardadas y "+examples+" ejemplos de ajuste local de palabras. Podes ver o borrar las referencias en Memoria. Leer una pagina no significa que todo su contenido sea correcto ni que lo haya aprendido por completo.",previous);}
  if(intent.name.equals("ANIMATION")&&intent.confidence>.6f)return new Reply("Para dibujar y animar, escribí en esta conversación: ‘una banana baila mientras entra el pepino’. El resultado aparece acá y podés guardarlo como MP4.",previous);
  if(intent.name.equals("FEELING")&&intent.confidence>.6f)return new Reply("Te escucho. ¿Querés contarme un poco más de cómo te sentís o qué pasó?",previous);
  if(intent.name.equals("PERSONAL")&&intent.confidence>.6f)return new Reply("Contame un poco más. Puedo recordar tu nombre y algo que te guste dentro de este chat.",previous);
  if(intent.name.equals("SCENE_EDIT")&&intent.confidence>.6f)return new Reply("Podemos editar el dibujo conversando. Decime qué personaje querés cambiar y cómo: por ejemplo, ‘el pepino era azul’.",previous);
  if(topic.isEmpty())return new Reply("Quiero entenderte mejor. ¿Me contás un poco más o me decís qué querés saber? Si querés buscar un tema, podés preguntarme ‘¿qué es una mariposa?’.",previous);
  JSONObject entry=WebMemory.forTopic(context,topic);boolean cached=entry!=null;
  if(!cached){try{WebMemory.lookup(context,topic);}catch(Exception error){return new Reply("No pude consultar Internet. No tengo una referencia guardada sobre ‘"+topic+"’. Podés reintentar cuando haya conexión.",topic);}entry=WebMemory.forTopic(context,topic);}
  if(entry==null)return new Reply("No encontre una referencia suficiente sobre ‘"+topic+"’. No voy a inventar una respuesta. Probá con otro nombre o una pregunta más concreta.",topic);
  String body=entry.optString("text").replaceAll("\\s+"," ").trim();String snippet=excerpt(body,text);
  return new Reply((cached?"De mi memoria":"Encontre esta referencia")+" sobre "+topic+":\n\n"+snippet+"\n\nFuente: "+entry.optString("source")+"\nGuardado: "+new Date(entry.optLong("saved"))+"\n\nEs un fragmento de la fuente; puede no responder toda tu pregunta.",topic);
 }
 static String excerpt(String body,String query){String[] sentences=body.split("(?<=[.!?])\\s+");String best=sentences.length==0?body:sentences[0];int bestScore=-1;for(String sentence:sentences){int score=0;for(String term:Knowledge.normalize(query).split("[^a-z]+"))if(term.length()>3&&!term.matches("sobre|como|queres|quiero|contame|banana|puedes|podes")&&Knowledge.normalize(sentence).contains(term))score++;if(score>bestScore){bestScore=score;best=sentence;}}return best.length()>500?best.substring(0,500)+"…":best;}
}
