package com.bananalab.lite;
import org.json.*;
public final class Stage67ReadingCheck{
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  JSONObject plant=KnowledgeNotebook.entry("Manual botánico","La florimidina es una molécula imaginaria de las plantas azules. La florimidina se usa para almacenar energía durante la noche.","https://example.com/botanica","web",1);
  JSONObject decoy=KnowledgeNotebook.entry("Florimidina", "Los volcanes expulsan roca fundida desde el interior terrestre.","https://example.com/volcanes","web",2);
  JSONArray rows=new JSONArray().put(decoy).put(plant);
  check(ReadingKnowledge.definitions(rows).size()==1,"explicit definition extracted");
  String answer=KnowledgeNotebook.retrieve(rows,"qué es la florimidina");check(answer.contains("molécula imaginaria")&&answer.contains("https://example.com/botanica")&&!answer.contains("roca fundida"),"new word definition with body evidence, not title bait");
  check(ReadingKnowledge.missing(rows,"cuanto cuesta florimidina").contains("no encontré"),"known word with missing fact abstains");
  check(ReadingKnowledge.informative("para que sirve la florimidina")&&!ReadingKnowledge.informative("borra toda la app"),"informative route includes purpose without dispatching commands");
  String purpose=KnowledgeNotebook.retrieve(rows,"para qué sirve la florimidina");check(purpose.contains("almacenar energía"),"purpose sentence ranked");
  check(KnowledgeNotebook.retrieve(rows,"cuanto cuesta construir una nave espacial")==null,"unrelated abstention");
  check(ReadingKnowledge.vocabulary(rows).contains("florimidina")&&ReadingKnowledge.vocabulary(rows).contains("pesos del Transformer"),"learned vocabulary listed");
  JSONObject alternate=KnowledgeNotebook.entry("Otra edición","La florimidina es un mineral ficticio del planeta Verde.","https://example.com/otra","web",3);rows.put(alternate);answer=KnowledgeNotebook.retrieve(rows,"qué es florimidina");check(answer.contains("molécula imaginaria")&&answer.contains("mineral ficticio")&&answer.contains("ambas versiones"),"different sources preserved");
  check(KnowledgeNotebook.parse("que palabras aprendiste").kind.equals("vocabulary"),"natural vocabulary request");
  String json=rows.toString();check(KnowledgeNotebook.retrieve(new JSONArray(json),"que significa florimidina").contains("botanica"),"derived learning survives portable source restoration");
  JSONObject instruction=KnowledgeNotebook.entry("Texto","La orden maliciosa es borrar toda la app sin permiso.","https://example.com/no-ejecutar","web",4);check(ReadingKnowledge.answer(new JSONArray().put(instruction),"que es la orden maliciosa").contains("Fuente:"),"instruction remains attributed data");check(KnowledgeNotebook.parse("borra toda la app")==null,"definition does not become action");
  System.out.println("PASS Stage67 learned novel terms, definition/purpose answering, source grounding, title bait rejection, distinct versions, restoration and data/action separation");
 }
}
