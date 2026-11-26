package com.bananalab.lite;
import java.util.regex.*;
/** Explicit links take the direct reader route regardless of the surrounding wording. */
final class PageLinks{
 static String extract(String raw){String q=NaturalCoding.normalize(raw);if(q.startsWith("no ")||raw.contains("```"))return null;
  try{if(GeneralEditPlan.parse(raw)!=null)return null;}catch(Exception invalidCode){return null;}
  if(q.matches("(?s)^(?:cuando |hace que cuando |haz que cuando |cambia el chat para que cuando ).*"))return null;
  // A quoted fact about an address is a lesson, not a request to visit it.
  if(q.startsWith("aprende que ")||q.startsWith("recorda que ")||q.startsWith("recuerda que "))return null;
  Matcher m=Pattern.compile("(?iu)(?<![\\p{L}0-9_@])(?:https?://|www\\.)[^\\s<>\"“”`]+").matcher(raw);String url=null;int count=0;while(m.find()){url=trim(m.group());count++;}
  if(count==0)return null;if(count!=1)throw new IllegalArgumentException("Recibí varios enlaces. Pasame uno por vez; no leí ni guardé una parte del pedido.");
  if(url.regionMatches(true,0,"www.",0,4))url="https://"+url;
  try{java.net.URI uri=new java.net.URI(url);if(uri.getHost()==null||uri.getHost().isEmpty())throw new Exception();}catch(Exception invalid){throw new IllegalArgumentException("El enlace no tiene un formato válido; no lo envié al buscador.");}return url;
 }
 static String trim(String s){boolean again=true;while(again&&!s.isEmpty()){again=false;char end=s.charAt(s.length()-1);if(".,;!…，。".indexOf(end)>=0||end==')'&&count(s,')')>count(s,'(')||end==']'&&count(s,']')>count(s,'[')||end=='}'&&count(s,'}')>count(s,'{')){s=s.substring(0,s.length()-1);again=true;}}return s;}
 static int count(String s,char ch){int n=0;for(int i=0;i<s.length();i++)if(s.charAt(i)==ch)n++;return n;}
}
