package com.bananalab.lite;
import android.content.Context;
import org.json.*;import java.io.*;import java.util.*;
/** Conversation by choosing, not writing: the reader judges which authored reply, if any, a message is asking for.
 *  A reply is used only above a confidence threshold; otherwise the earlier conversation routes continue. */
final class ChatBank{
 static final String ASSET="lector3/respuestas.json";static final int CANDIDATES=8;
 /** Measured on 60 wordings never seen: likeness of words alone picks the right reply in 30, the model alone in 22; when the model
  *  confirms the reply that likeness ranks first, 11 of 12 are right. Only that agreement is answered. */
 static final double CONFIDENCE=.5;
 static final class Entry{final String reply;final List<String> forms=new ArrayList<>();float[] meaning;Set<String> words;Entry(String r){reply=r;}}
 private static List<Entry> cached;
 static List<Entry> parse(String json)throws Exception{JSONArray rows=new JSONArray(json);List<Entry> out=new ArrayList<>();for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);Entry e=new Entry(r.getString("reply"));JSONArray f=r.getJSONArray("forms");for(int k=0;k<f.length();k++)e.forms.add(f.getString(k));out.add(e);}return out;}
 static synchronized List<Entry> load(Context c)throws Exception{if(cached==null)cached=parse(AppJavaChat.asset(c,ASSET,400000));return cached;}
 static int[] prompt(BananaTransformer m,String reply,String message){List<Integer> r=m.encode("respuesta : "+reply,Lector.CANDIDATE_TOKENS+1),q=m.encode(message,Lector.QUESTION_TOKENS+1);if(r.isEmpty()||q.isEmpty()||r.size()>Lector.CANDIDATE_TOKENS||q.size()>Lector.QUESTION_TOKENS)return null;int[] out=new int[r.size()+q.size()+4];int n=0;out[n++]=2;out[n++]=3;for(int v:r)out[n++]=v;out[n++]=3;for(int v:q)out[n++]=v;out[n++]=4;return out;}
 static String plain(String message){return ChatLanguage.normalize(message);}
 /** The replies worth judging: those whose known wordings share words with the message, then those closest in meaning. */
 static List<Entry> shortlist(BananaTransformer m,List<Entry> bank,String message){final Set<String> terms=new HashSet<>(Arrays.asList(plain(message).split(" ")));final float[] q=Lector.vector(m,plain(message));final Map<Entry,Double> rank=new HashMap<>();for(Entry e:bank){if(e.meaning==null){StringBuilder all=new StringBuilder();e.words=new HashSet<>();for(String f:e.forms){all.append(f).append(' ');e.words.addAll(Arrays.asList(f.split(" ")));}e.meaning=Lector.vector(m,all.toString());}int shared=0;for(String t:terms)if(t.length()>2&&e.words.contains(t))shared++;rank.put(e,shared+Lector.cosine(q,e.meaning));}List<Entry> order=new ArrayList<>(bank);Collections.sort(order,new Comparator<Entry>(){public int compare(Entry a,Entry b){return Double.compare(rank.get(b),rank.get(a));}});return order.subList(0,Math.min(CANDIDATES,order.size()));}
 static final int LEARNED=300;
 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-chat-bank",0);}
 /** Wordings the user confirmed (message → reply) and pairs the user rejected. */
 static JSONObject learned(Context c)throws Exception{return new JSONObject(prefs(c).getString("learned","{}"));}
 static JSONArray blocked(Context c)throws Exception{return new JSONArray(prefs(c).getString("blocked","[]"));}
 static String key(String message,String reply){return plain(message)+"\u0001"+reply;}
 static boolean isBlocked(Context c,String message,String reply)throws Exception{JSONArray b=blocked(c);String k=key(message,reply);for(int i=0;i<b.length();i++)if(b.getString(i).equals(k))return true;return false;}
 /** Called when the user says the last chat reply was right or wrong. A confirmed wording is answered directly from then on. */
 static String mark(Context c,boolean good)throws Exception{String raw=prefs(c).getString("last","");if(raw.isEmpty())return null;JSONObject last=new JSONObject(raw);String message=plain(last.getString("message")),reply=last.getString("reply");JSONObject learned=learned(c);JSONArray blocked=blocked(c);
  if(good){if(learned.length()>=LEARNED&&!learned.has(message))return "Ya guardé "+LEARNED+" formas tuyas de pedir respuestas; no sumé ésta.";learned.put(message,reply);JSONArray keep=new JSONArray();String k=key(message,reply);for(int i=0;i<blocked.length();i++)if(!blocked.getString(i).equals(k))keep.put(blocked.getString(i));prefs(c).edit().putString("learned",learned.toString()).putString("blocked",keep.toString()).apply();return "Anotado: cuando me digas «"+last.getString("message")+"» te respondo eso. Ya aprendí "+learned.length()+(learned.length()==1?" forma tuya":" formas tuyas")+" de pedirme cosas.";}
  learned.remove(message);if(blocked.length()<LEARNED)blocked.put(key(message,reply));prefs(c).edit().putString("learned",learned.toString()).putString("blocked",blocked.toString()).apply();return "Anotado: esa respuesta no correspondía a «"+last.getString("message")+"». No la vuelvo a dar para ese mensaje. Si querés enseñarme qué responder, decime: cuando te diga \""+last.getString("message")+"\" respondas \"…\".";}
 static long lastTime(Context c){try{return prefs(c).getLong("lastTime",0);}catch(Throwable e){return 0;}}
 static String summary(Context c){try{return "Charla: 30 respuestas redactadas · "+learned(c).length()+" formas tuyas aprendidas · "+blocked(c).length()+" respuestas descartadas por vos.";}catch(Throwable e){return "";}}
 static final class Choice{final String reply;final double confidence;Choice(String r,double c){reply=r;confidence=c;}}
 static Choice choose(BananaTransformer m,List<Entry> bank,String message)throws Exception{String text=plain(message);if(text.isEmpty()||text.split(" ").length>14)return null;List<Entry> list=shortlist(m,bank,message);if(list.isEmpty())return null;int[] prompt=prompt(m,list.get(0).reply,text);return prompt==null?null:new Choice(list.get(0).reply,Lector.yes(m,prompt));}
 /** The chosen reply, or null when the reader is unavailable or not sure enough. Never throws. */
 static String reply(Context c,String message){try{if(!Lector.SELECTOR)return null;String known=learned(c).optString(plain(message),"");String reply=known;if(reply.isEmpty()){Choice choice=choose(Lector.load(c),load(c),message);reply=choice!=null&&choice.confidence>=CONFIDENCE?choice.reply:"";}if(reply.isEmpty()||isBlocked(c,message,reply))return null;prefs(c).edit().putString("last",new JSONObject().put("message",message).put("reply",reply).toString()).putLong("lastTime",System.currentTimeMillis()).apply();return reply;}catch(Throwable unavailable){return null;}}
}
