package com.bananalab.lite;
import android.content.*;
import org.json.*;import java.util.*;
/** The app adjusts one of its own numbers: how sure the reader must be before it answers. It tries each allowed value against
 *  the user's judged questions and keeps the best. Only this bounded number changes, never code; «valores de fábrica» undoes it. */
final class SelfTune{
 static final double[] OPTIONS={.5,.6,.7,.8,.9};static final int MIN_JUDGED=8;
 interface Judge{Lector.Found best(String question)throws Exception;}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-self-tune",0);}
 /** Stored as a whole percentage so that the factory value is exactly the factory value. */
 static void apply(Context c){try{int percent=prefs(c).getInt("percent",-1);Lector.selectConfidence=percent<50||percent>90?Lector.SELECT_CONFIDENCE:percent/100.0;}catch(Throwable ignored){}}
 /** One judged question as seen now: the reader's confidence, and whether its sentence is the one the user judged. */
 static final class Case{final boolean good,same;final double confidence;Case(boolean g,boolean s,double c){good=g;same=s;confidence=c;}}
 /** +1 for a good answer still given, −1 for a good question now answered with another sentence, −1 for a bad answer still given. */
 static int value(List<Case> cases,double threshold){int v=0;for(Case k:cases){boolean shown=k.confidence>=threshold;if(!shown)continue;if(k.good)v+=k.same?1:-1;else if(k.same)v--;}return v;}
 static double choose(List<Case> cases){double best=Lector.SELECT_CONFIDENCE;int top=value(cases,best);for(double t:OPTIONS){int v=value(cases,t);if(v>top||v==top&&Math.abs(t-Lector.SELECT_CONFIDENCE)<Math.abs(best-Lector.SELECT_CONFIDENCE)){top=v;best=t;}}return best;}
 static List<Case> cases(JSONArray rows,Judge judge)throws Exception{List<Case> out=new ArrayList<>();for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);String verdict=r.getString("verdict");if(verdict.isEmpty())continue;Lector.Found f=judge.best(r.getString("question"));boolean same=f!=null&&new Lector.Flat(f.sentence,true).text.toString().equals(new Lector.Flat(r.getString("sentence"),true).text.toString());out.add(new Case(verdict.equals("bien"),same,f==null?0:f.confidence));}return out;}
 /** Tries every allowed threshold on the judged questions and adopts the best. */
 static String tune(Context c,Judge judge)throws Exception{List<Case> cases=cases(RealExam.rows(c),judge);if(cases.size()<MIN_JUDGED)return "Para ajustarme sola necesito al menos "+MIN_JUDGED+" respuestas calificadas por vos; tengo "+cases.size()+". Seguí diciéndome «esa respuesta estuvo bien» o «estuvo mal».";double before=Lector.selectConfidence,chosen=choose(cases);int was=value(cases,before),now=value(cases,chosen);if(!prefs(c).edit().putInt("percent",(int)Math.round(chosen*100)).putLong("time",System.currentTimeMillis()).putInt("judged",cases.size()).commit())throw new java.io.IOException("No pude guardar el ajuste");Lector.selectConfidence=chosen;
  return chosen==before?"Probé "+OPTIONS.length+" niveles de seguridad contra tus "+cases.size()+" respuestas calificadas y el actual ("+before+") sigue siendo el mejor.":"Me ajusté sola: ahora respondo cuando mi seguridad es de "+chosen+" o más (antes "+before+"). Con tus "+cases.size()+" respuestas calificadas eso da un balance de "+now+" contra "+was+" (aciertos conservados menos errores repetidos). Decí «volvé a los valores de fábrica» para deshacerlo.";}
 static String reset(Context c)throws Exception{if(!prefs(c).edit().clear().commit())throw new java.io.IOException("No pude volver a los valores de fábrica");Lector.selectConfidence=Lector.SELECT_CONFIDENCE;return "Volví al nivel de seguridad de fábrica ("+Lector.SELECT_CONFIDENCE+").";}
 static String summary(Context c){try{int percent=prefs(c).getInt("percent",-1);return percent<0?"Nivel de seguridad para responder: "+Lector.SELECT_CONFIDENCE+" (de fábrica).":"Nivel de seguridad para responder: "+percent/100.0+", elegido por la app con "+prefs(c).getInt("judged",0)+" respuestas tuyas (de fábrica "+Lector.SELECT_CONFIDENCE+").";}catch(Throwable e){return "";}}
}
