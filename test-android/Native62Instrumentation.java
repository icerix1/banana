package com.bananalab.lite;
import android.app.*;import android.content.*;import android.os.*;import java.util.*;import java.io.*;
public final class Native62Instrumentation extends Instrumentation{
 void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 BananaEngine.Result run(Context c,String s)throws Exception{return BananaEngine.run(c,s,new ArrayList<String[]>(),"",true,OwnBrain.load(c));}
 void note(String s){Bundle b=new Bundle();b.putString("stream",s+"\n");sendStatus(0,b);}
 @Override public void onStart(){Bundle out=new Bundle();try{Context c=getTargetContext();DurableCode.awaitReady();KnowledgeNotebook.prefs(c).edit().clear().commit();final int[] calls={0};
 KnowledgeNotebook.PageLoader fixture=new KnowledgeNotebook.PageLoader(){public PageReader.Document load(String url)throws Exception{calls[0]++;check(url.equals("https://example.com/tema"),"exact link, not search: "+url);return PageReader.document(url,"<title>Manual</title><main>La fulgorina es un mineral ficticio que se usa para iluminar jardines de prueba.</main>",true);}};
 String receipt=KnowledgeNotebook.handle(c,"Qué dice esta página: [manual](https://example.com/tema).",fixture).text;check(receipt.contains("Guardé en mi cuaderno")&&calls[0]==1,"direct loader called once");check(run(c,"que es la fulgorina").text.contains("mineral ficticio"),"new page content usable through actual chat");
 String blocked=run(c,"que dice esta pagina https://localhost/prueba").text;check(blocked.contains("No pude leer directamente")&&!blocked.contains("No pude consultar el buscador general"),"real routing rejects local URL before network without general search: "+blocked);
 int before=KnowledgeNotebook.data(c).length();String denied=KnowledgeNotebook.handle(c,"investiga https://example.com/denied",new KnowledgeNotebook.PageLoader(){public PageReader.Document load(String url)throws Exception{throw new IOException("La pagina respondio 403");}}).text;check(denied.contains("HTTP 403")&&KnowledgeNotebook.data(c).length()==before,"403 accurately reported without changing knowledge");
 String multiple=KnowledgeNotebook.handle(c,"mira https://example.com/a y https://example.org/b",fixture).text;check(multiple.contains("varios enlaces")&&calls[0]==1,"no partial multi-link read");
 String actual=run(c,"podes aprender de esta pagina https://example.com/").text;if(actual.contains("Guardé en mi cuaderno"))note("NATIVE_LIVE_HTTPS_PAGE_RETAINED_OK");else{check(actual.contains("No pude leer directamente"),"public link did not fall into search: "+actual);note("NATIVE_LIVE_HTTPS_UNAVAILABLE "+actual);}
 out.putString("stream","PASS NATIVE62 provided links reach direct reader with natural wording/Markdown, extraction and learning, actual chat route, local URL refusal, HTTP403 diagnostics and no partial batch.\n");finish(Activity.RESULT_OK,out);
 }catch(Throwable e){StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));out.putString("stream","FAIL "+s);finish(Activity.RESULT_CANCELED,out);}}
}
