package com.bananalab.lite;
import android.app.*;import android.content.*;import android.os.*;import java.util.*;import java.io.*;import org.json.*;
public final class Native61Instrumentation extends Instrumentation{
 void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 BananaEngine.Result run(Context c,String s)throws Exception{return BananaEngine.run(c,s,new ArrayList<String[]>(),"",true,OwnBrain.load(c));}
 void note(String s){Bundle b=new Bundle();b.putString("stream",s+"\n");sendStatus(0,b);}
 @Override public void onStart(){Bundle out=new Bundle();try{Context c=getTargetContext();DurableCode.awaitReady();KnowledgeNotebook.prefs(c).edit().clear().commit();
 String text="La florimidina es una molécula imaginaria de las plantas azules. La florimidina se usa para almacenar energía durante la noche.";
 KnowledgeNotebook.remember(c,"Manual botánico",text,"https://example.com/botanica","web");
 check(run(c,"que es la florimidina").text.contains("molécula imaginaria"),"new word definition from web evidence");
 String purpose=run(c,"para que sirve la florimidina").text;check(purpose.contains("almacenar energía")&&purpose.contains("https://example.com/botanica"),"purpose answer and provenance through actual chat: "+purpose);
 String vocabulary=run(c,"que palabras aprendiste").text;check(vocabulary.contains("florimidina")&&vocabulary.contains("pesos del Transformer"),"derived glossary through natural chat");
 check(KnowledgeNotebook.retrieve(KnowledgeNotebook.data(c),"como construir motores de plasma")==null,"unrelated abstention");
 JSONArray portable=KnowledgeNotebook.data(c);KnowledgeNotebook.prefs(c).edit().clear().commit();KnowledgeNotebook.restore(c,portable);check(run(c,"que significa florimidina").text.contains("molécula imaginaria"),"rebuild derived vocabulary from restored sources");
 try{String actual=PageReader.read(c,"https://example.com/","example");check(actual.contains("Guardé en mi cuaderno"),"live UI reader joins notebook");check(KnowledgeNotebook.data(c).length()==2,"live public page retained");note("NATIVE_LIVE_HTTPS_PAGE_RETAINED_OK");}catch(Exception network){note("NATIVE_LIVE_HTTPS_UNAVAILABLE "+network.getClass().getSimpleName()+": "+network.getMessage());}
 out.putString("stream","PASS NATIVE61 novel-word definition/purpose answering through chat, attributed web fixture, glossary, unrelated abstention and vocabulary restored from portable sources.\n");finish(Activity.RESULT_OK,out);
 }catch(Throwable e){StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));out.putString("stream","FAIL "+s);finish(Activity.RESULT_CANCELED,out);}}
}
