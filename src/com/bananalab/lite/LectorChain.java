package com.bananalab.lite;
import android.content.Context;
import org.json.*;import java.util.*;import java.util.regex.*;
/** Questions that need two readings: «¿cuándo nació quien fundó X?». The first step finds who; the second asks about that name.
 *  Only this fixed family of questions is recognised, and both steps must be found in the saved text with the reader's usual confidence. */
final class LectorChain{
 interface Retriever{JSONArray rows(String query)throws Exception;}
 static final Pattern SHAPE=Pattern.compile("(cuando|donde|en que ano|en que lugar|como) (nacio|murio|fallecio|vivio|estudio|trabajo) (?:quien|el que|la que|la persona que|el hombre que|la mujer que) (fundo|creo|invento|escribio|dirigio|descubrio|diseno|desarrollo|construyo|pinto|compuso|inauguro) (?:a |al |el |la |los |las )?(.{2,80})");
 /** {first question, second question with %s for the name, the thing asked about}, or null. */
 static String[] split(String question){Matcher m=SHAPE.matcher(ChatLanguage.normalize(question));if(!m.matches())return null;return new String[]{"quien "+m.group(3)+" "+m.group(4),m.group(1)+" "+m.group(2)+" %s",m.group(4)};}
 static final String NAME="[A-ZÁÉÍÓÚÑ][\\p{L}.'-]+(?: (?:(?:de|del|de la|van|von|di|da) )?[A-ZÁÉÍÓÚÑ][\\p{L}.'-]+){0,3}";
 /** The person named in a sentence: after «por» when present, otherwise the first capitalised name that is not the thing itself. */
 static String name(String sentence,String thing){String avoid=ChatLanguage.normalize(thing);Matcher by=Pattern.compile("\\bpor (?:el |la |los |las )?(?:[\\p{Ll}]+ ){0,3}("+NAME+")").matcher(sentence);while(by.find()){String n=clean(by.group(1));if(usable(n,avoid))return n;}Matcher any=Pattern.compile("(?<![\\p{L}])("+NAME+")").matcher(sentence);while(any.find()){String n=clean(any.group(1));if(any.start()>0&&usable(n,avoid))return n;}return null;}
 static String clean(String n){return n.replaceAll("[.,;:]+$","").trim();}
 static boolean usable(String name,String avoid){String n=ChatLanguage.normalize(name);if(n.length()<4||n.split(" ").length<2)return false;for(String w:n.split(" "))if(w.length()>3&&avoid.contains(w))return false;return true;}
 static final class Chain{final Lector.Found first,second;final String name;Chain(Lector.Found a,String n,Lector.Found b){first=a;name=n;second=b;}}
 static Chain follow(BananaTransformer m,Retriever library,String question)throws Exception{String[] parts=split(question);if(parts==null)return null;Lector.Found first=Lector.read(m,library.rows(parts[0]),parts[0]);if(first==null)return null;String who=name(first.sentence,parts[2]);if(who==null)return null;String second=String.format(Locale.ROOT,parts[1],who.toLowerCase(Locale.ROOT));Lector.Found answer=Lector.read(m,library.rows(second),second);return answer==null||answer.sentence.equals(first.sentence)?null:new Chain(first,who,answer);}
 static String reply(final Context c,String question){try{if(!Lector.SELECTOR||split(question)==null)return null;Chain chain=follow(Lector.load(c),new Retriever(){public JSONArray rows(String query)throws Exception{return NotebookDisk.of(c).selected(query);}},question);if(chain==null)return null;return "Para responder junté dos lecturas con mi lector neuronal:\n\n1. «"+chain.first.sentence+"»\nFuente: "+ReadingKnowledge.source(chain.first.row)+"\n\n2. Sobre "+chain.name+": «"+chain.second.sentence+"»\nFuente: "+ReadingKnowledge.source(chain.second.row)+"\n\nSon dos oraciones literales unidas por el nombre que aparece en la primera. Si ese nombre corresponde a otra persona con el mismo nombre, la respuesta es incorrecta; no lo verifiqué.";}catch(Throwable unavailable){return null;}}
}
