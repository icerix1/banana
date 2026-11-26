package com.bananalab.lite;
public class ConversationCheck{
 public static void main(String[] args)throws Exception{
  String[] social={"holaa","holaaaa!!","buenas","buenass","Hola Banana 👋","cómo andás?","todo bien","bien y vos","quiero charlar","estoy aburrido","graciaaas","chauu"};
  for(String text:social){if(ChatLanguage.socialReply(text)==null)throw new AssertionError("No social: "+text);if(!BananaChat.topic(text,"gato").isEmpty())throw new AssertionError("Busca saludo con contexto: "+text);BananaChat.Reply reply=BananaChat.reply(null,text,"gato");if(reply.text.contains("http")||!reply.topic.equals("gato"))throw new AssertionError(text);System.out.println(text+" -> local, sin red");}
  if(!BananaChat.topic("holaa que es un ajolote","").equals("ajolote"))throw new AssertionError("saludo mixto");
  if(!BananaChat.topic("y como se ve","ajolote").equals("ajolote"))throw new AssertionError("seguimiento");
  for(String text:new String[]{"eso no era lo que dije","no entiendo","bla bla","me llamo ana"})if(!BananaChat.topic(text,"gato").isEmpty())throw new AssertionError("busqueda injustificada: "+text);
  if(ChatLanguage.socialReply("hola que es un gato")!=null)throw new AssertionError("oculto pregunta");
  System.out.println("PASS saludos sin contexto/red, preguntas mixtas y barrera de busqueda");
 }
}
