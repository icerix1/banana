package com.bananalab.lite;
import android.content.*;
import java.util.*;
/** «¿Y dónde murió?»: a question without a subject of its own borrows the subject of the previous answered question. */
final class FollowUp{
 static final long TTL=600000L;
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-follow-up",0);}
 /** Drops a leading «y», and turns «en qué año…» and «en qué lugar…» into the «cuándo» and «dónde» the library understands. */
 static String strip(String question){return question.replaceFirst("(?iu)^[\\s¿¡]*(?:y|pero|entonces|ahora|bueno)[, ]+(?=\\S)","").replaceFirst("(?iu)^[\\s¿¡]*en qu[eé] (?:año|fecha|momento)\\b","cuándo").replaceFirst("(?iu)^[\\s¿¡]*en qu[eé] (?:lugar|ciudad|pa[ií]s|parte)\\b","dónde").trim();}
 /** Content words of a question that are not about the matter asked (born, died, founded…): what the question is about. */
 static List<String> subject(String question){List<String> out=new ArrayList<>();for(String w:ChatLanguage.normalize(question).split(" "))if(!ReadingKnowledge.words(w).isEmpty()&&Lector.matter(w)<0&&!w.matches("ano|fecha|lugar|ciudad|pais|el|ella|ellos|ellas|eso|esa|ese|alli|ahi|aqui|su|sus|lo|le")&&!out.contains(w))out.add(w);return out;}
 static boolean dangling(String question){return ReadingKnowledge.informative(strip(question))&&subject(strip(question)).isEmpty();}
 static String complete(String question,String subject){String q=strip(question);if(subject==null||subject.isEmpty()||!dangling(question))return q;return q.replaceAll("[?¿!¡.]+\\s*$","").trim()+" "+subject;}
 static String resolve(Context c,String question){try{long age=System.currentTimeMillis()-prefs(c).getLong("time",0);return complete(question,age>=0&&age<TTL?prefs(c).getString("subject",""):"");}catch(Throwable e){return strip(question);}}
 /** Called after a question was answered. A follow-up keeps the subject it borrowed. */
 static void remember(Context c,String asked,String name){try{String subject=name!=null&&!name.isEmpty()?name.toLowerCase(Locale.ROOT):join(subject(asked));if(subject.isEmpty())return;prefs(c).edit().putString("subject",subject).putLong("time",System.currentTimeMillis()).apply();}catch(Throwable ignored){}}
 static String join(List<String> words){StringBuilder b=new StringBuilder();for(int i=0;i<Math.min(4,words.size());i++){if(b.length()>0)b.append(' ');b.append(words.get(i));}return b.toString();}
 static void forget(Context c){try{prefs(c).edit().clear().apply();}catch(Throwable ignored){}}
}
