package com.bananalab.lite;
import android.content.Context;
import org.json.*;import java.io.*;import java.util.*;
/** Own reader model: receives a saved fragment plus the question and answers by copying one sentence of that fragment.
 *  The app only shows an answer that is found literally in the fragment; anything else is discarded. */
final class Lector{
 static final int CTX_TOKENS=330,QUESTION_TOKENS=40,ANSWER_TOKENS=110,MAX_WINDOWS=2;
 /** Minimum confidence to show a sentence. Calibrated on set-aside examples: a chosen sentence that shares no content word with the
  *  question was right in 4 of 162 cases, so it is never shown (a value above 1 disables that route). */
 static final double SHARED_CONFIDENCE=.3,FREE_CONFIDENCE=2;
 static final String NEGATIVE="no encontre ese dato en el fragmento .";
 private static BananaTransformer model;private static String loaded="";
 static synchronized BananaTransformer load(Context c)throws Exception{String revision=ModelDepot.revision(c,"reader");if(model==null||!loaded.equals(revision)){try(InputStream in=ModelDepot.open(c,"reader")){model=BananaTransformer.container(in);}loaded=revision;}return model;}
 /** A private, modifiable copy for training or growth. */
 static BananaTransformer copy(Context c)throws Exception{try(InputStream in=ModelDepot.open(c,"reader")){return BananaTransformer.container(in);}}
 static synchronized void forget(){model=null;loaded="";}

 /** [bos][user] fragment [user] question [assistant]; null when a part does not fit. */
 static int[] prompt(BananaTransformer m,String fragment,String question){List<Integer> ctx=m.encode(fragment,CTX_TOKENS+1),q=m.encode(question,QUESTION_TOKENS+1);if(ctx.isEmpty()||q.isEmpty()||ctx.size()>CTX_TOKENS||q.size()>QUESTION_TOKENS)return null;int[] out=new int[ctx.size()+q.size()+4];int n=0;out[n++]=2;out[n++]=3;for(int v:ctx)out[n++]=v;out[n++]=3;for(int v:q)out[n++]=v;out[n++]=4;return out;}
 /** Greedy answer in the tokenizer's lower case, or null if the model did not finish inside the limits. */
 static String generate(BananaTransformer m,String fragment,String question)throws Exception{Out o=answer(m,fragment,question);return o==null?null:o.text;}
 static final class Out{final String text;final double confidence;Out(String t,double c){text=t;confidence=c;}}
 /** Answer plus the probability the model gave to its first three tokens: where it decides which sentence to copy. */
 static Out answer(BananaTransformer m,String fragment,String question)throws Exception{int[] prompt=prompt(m,fragment,question);if(prompt==null||prompt.length+2>m.context)return null;double confidence=1;BananaTransformer.Session s=m.session();long deadline=System.nanoTime()+90000000000L;
  for(int i=0;i+1<prompt.length;i++){if((i&15)==0&&(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline))throw new IOException("Lectura neuronal interrumpida");s.advance(prompt[i]);}
  float[] logits=s.step(prompt[prompt.length-1]);List<Integer> ids=new ArrayList<>();
  while(ids.size()<ANSWER_TOKENS&&s.used<m.context){if(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline)throw new IOException("Lectura neuronal interrumpida");int best=5;for(int i=6;i<m.vocabSize;i++)if(logits[i]>logits[best])best=i;if(ids.size()<3)confidence*=HeadTrainer.probabilities(logits)[best];if(best==5)return new Out(m.bytePairs.decode(ids).trim(),confidence);ids.add(best);logits=s.step(best);}
  return null;}

 /** Lower case and single spaces, remembering where each character came from. */
 static final class Flat{final StringBuilder text=new StringBuilder();final List<Integer> at=new ArrayList<>();
  Flat(String source,boolean accents){boolean space=false;for(int i=0;i<source.length();i++){char ch=source.charAt(i);if(BytePairs.space(ch)){space=text.length()>0;continue;}if(space){text.append(' ');at.add(i);space=false;}char low=Character.toLowerCase(ch);if(!accents)low=bare(low);text.append(low);at.add(i);}}}
 static char bare(char c){switch(c){case 'á':case 'à':case 'ä':case 'â':return 'a';case 'é':case 'è':case 'ë':case 'ê':return 'e';case 'í':case 'ì':case 'ï':case 'î':return 'i';case 'ó':case 'ò':case 'ö':case 'ô':return 'o';case 'ú':case 'ù':case 'ü':case 'û':return 'u';default:return c;}}
 /** The original text of the fragment that the answer copies, or null when the answer is not literally there. */
 static String ground(String fragment,String answer){if(answer==null)return null;Flat f=new Flat(fragment,true),a=new Flat(answer,true);String needle=a.text.toString();if(needle.length()<20||needle.equals(NEGATIVE))return null;int hit=f.text.indexOf(needle);if(hit<0)return null;
  int start=f.at.get(hit),end=f.at.get(hit+needle.length()-1)+1;return fragment.substring(start,end).trim();}

 /** Consecutive sentences packed into pieces that fit the reader, best lexical overlap first. */
 static List<String> windows(BananaTransformer m,String text,Set<String> terms,int limit){List<String> all=new ArrayList<>();List<Integer> score=new ArrayList<>();StringBuilder cur=new StringBuilder();int used=0;
  for(String sentence:KnowledgeNotebook.chunks(text)){int size=m.encode(sentence,CTX_TOKENS+1).size();if(size>CTX_TOKENS)continue;if(used+size+1>CTX_TOKENS&&cur.length()>0){all.add(cur.toString());cur.setLength(0);used=0;}if(cur.length()>0)cur.append(' ');cur.append(sentence);used+=size+1;}
  if(cur.length()>0)all.add(cur.toString());for(String w:all)score.add(ReadingKnowledge.common(terms,ReadingKnowledge.words(w)));
  List<String> out=new ArrayList<>();while(out.size()<limit){int best=-1;for(int i=0;i<all.size();i++)if(all.get(i)!=null&&(best<0||score.get(i)>score.get(best)))best=i;if(best<0||score.get(best)<=0&&!terms.isEmpty())break;out.add(all.get(best));all.set(best,null);}return out;}

 static final class Found{final String sentence;final JSONObject row;Found(String s,JSONObject r){sentence=s;row=r;}}
 /** Reads the best windows of the retrieved documents until one yields a grounded sentence. */
 static Found read(BananaTransformer m,JSONArray rows,String question)throws Exception{Set<String> terms=ReadingKnowledge.words(question);String q=question.toLowerCase(Locale.ROOT).replaceAll("[¿?¡!]"," ").replaceAll("\\s+"," ").trim();int tried=0;
  for(int i=0;i<rows.length()&&i<3&&tried<MAX_WINDOWS;i++){JSONObject row=rows.getJSONObject(i);for(String window:windows(m,row.getString("text"),terms,MAX_WINDOWS-tried)){tried++;JavaChat.phase("Leyendo el fragmento con el lector neuronal…");Out o=answer(m,window,q);String grounded=o==null?null:ground(window,o.text);if(grounded!=null&&o.confidence>=(ReadingKnowledge.common(terms,ReadingKnowledge.words(grounded))>0?SHARED_CONFIDENCE:FREE_CONFIDENCE))return new Found(grounded,row);if(tried>=MAX_WINDOWS)break;}}
  return null;}
 /** Attributed answer text, or null when the reader is unavailable or found nothing literal. Never throws: the extractive route remains. */
 static String reply(Context c,JSONArray rows,String question){try{if(rows==null||rows.length()==0)return null;Found f=read(load(c),rows,question);if(f==null)return null;return "Mi lector neuronal leyó el fragmento guardado y señala esta oración:\n\n«"+f.sentence+"»\nFuente: "+ReadingKnowledge.source(f.row)+"\n\nEs una oración literal de la fuente, elegida por el modelo propio; puede no responder exactamente la pregunta y no es una verificación nueva.";}catch(Throwable unavailable){return null;}}
 static String card(Context c){try{BananaTransformer m=load(c);return "Lector neuronal: "+m.parameters()+" parámetros · "+m.layers+" capas · ancho "+m.d+" · vocabulario "+m.vocabSize+" · contexto "+m.context+" tokens"+(ModelDepot.active(c,"reader")?" · con aprendizaje de tus lecturas":" · modelo base");}catch(Throwable e){return "Lector neuronal no disponible en este contexto.";}}
}
