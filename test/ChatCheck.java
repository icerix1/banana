package com.bananalab.lite;
import java.io.*;
public class ChatCheck {
 public static void main(String[] args)throws Exception{
  ChatMind mind=new ChatMind(new FileInputStream(args[0]+"/chat.bin"),new FileInputStream(args[0]+"/chat-labels.txt"));
  String[][] cases={{"Hola Banana","HELLO"},{"quien sos","IDENTITY"},{"muchas gracias","THANKS"},{"que es un ajolote","FACT"},{"como se ve un elefante","APPEARANCE"},{"quiero hacer una animacion","ANIMATION"},{"que tenes guardado","MEMORY"},{"hasta luego","GOODBYE"},{"ayudame","HELP"}};
  for(String[] row:cases){ChatMind.Intent result=mind.classify(row[0]);System.out.println(row[0]+" -> "+result.name+" "+result.confidence);if(!result.name.equals(row[1])||result.confidence<.6)throw new AssertionError(row[0]);}
  if(!BananaChat.topic("que es un ajolote","").equals("ajolote"))throw new AssertionError("topic extraction");
  if(!BananaChat.topic("y como se ve","ajolote").equals("ajolote"))throw new AssertionError("followup");
  if(!BananaChat.topic("busca informacion sobre saturno","").equals("saturno"))throw new AssertionError("planet");
  String result=BananaChat.excerpt("El gato es un mamifero. Su pelaje puede ser negro.","pelaje");if(!result.contains("pelaje"))throw new AssertionError("retrieval");
  System.out.println("PASS chat propio: intenciones, tema, continuidad y seleccion de fragmento");
 }
}
