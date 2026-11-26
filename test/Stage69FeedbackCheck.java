package com.bananalab.lite;
import org.json.*;
public final class Stage69FeedbackCheck{
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  for(String s:new String[]{"no me entendiste","eso esta mal","te estoy corrigiendo","no abras el editor del video","no, quiero que entiendas mi pregunta"})check(ConversationFeedback.parse(s).kind.equals("clarify"),s);
  check(ConversationFeedback.parse("no, la capital de Pruebalia es Limonia.").kind.equals("fact"),"natural factual correction");
  check(ConversationFeedback.parse("no gracias")==null,"courtesy remains social conversation");
  check(ConversationFeedback.parse("corrige paso 2: agrega un zorro")==null,"plan correction stays plan");check(ConversationFeedback.parse("cambia el boton enviar a rojo")==null,"app action stays action");
  check(ConversationFeedback.parse("no me respondas tan largo").text.equals("breve")&&ConversationFeedback.parse("explicame con mas detalle").text.equals("detallado"),"natural response preferences");
  StringBuilder text=new StringBuilder();for(int i=0;i<14000;i++)text.append('x');text.append(" La tardina es una sustancia ficticia que sirve para guardar el dato Zeta889.");JSONObject row=KnowledgeNotebook.entry("Documento largo",text.toString(),"https://example.com/largo","web",1);check(!row.getBoolean("truncated")&&row.getString("text").contains("Zeta889"),"beyond previous 12k retained");
  JSONArray rows=new JSONArray().put(row);check(KnowledgeNotebook.retrieve(rows,"que es la tardina").contains("Zeta889"),"late fact beyond old cap retrieved");
  JSONObject old=KnowledgeNotebook.entry("Versión web","La capital de Pruebalia es Naranjia.","https://example.com/pais","web",2),corrected=KnowledgeNotebook.entry("Corrección del usuario","La capital de Pruebalia es Limonia.","user","user",3);rows=new JSONArray().put(corrected).put(old);String answer=KnowledgeNotebook.retrieve(rows,"cual es la capital de Pruebalia");check(answer.indexOf("Limonia")<answer.indexOf("Naranjia"),"explicit user correction ranked first");
  String brief=ReadingKnowledge.style(answer,"breve",rows,"cual es la capital de Pruebalia");check(brief.length()<answer.length()&&brief.contains("Limonia")&&brief.contains("Fuente:"),"brief reply preserves source and corrected fact");
  check(KnowledgeNotebook.TEXT==262144&&KnowledgeNotebook.MAX==128&&KnowledgeNotebook.BUDGET==4000000&&DurableCode.PORTABLE_LIMIT>KnowledgeNotebook.BUDGET*3,"capacity and compatible backup budget");
  System.out.println("PASS Stage69 conversational correction priority, factual learning, style preferences, facts beyond12k, user correction ranking and matching backup limits");
 }
}
