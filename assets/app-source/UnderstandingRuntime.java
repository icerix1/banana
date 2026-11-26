package com.bananalab.lite;
import android.content.*;import java.io.*;import java.lang.reflect.*;import org.json.*;
final class UnderstandingRuntime{
 static volatile Method active;static volatile String error="";
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-understanding",0);}
 static UnderstandingSpec current(Context c)throws Exception{String raw=prefs(c).getString("active","");return UnderstandingSpec.read(raw.isEmpty()?new JSONObject(UnderstandingRules.spec()):new JSONObject(raw).getJSONObject("spec"));}
 static Method method(Context c,JSONObject row)throws Exception{UnderstandingSpec spec=UnderstandingSpec.read(row.getJSONObject("spec"));if(!row.getString("sourceHash").equals(JavaMethodEditor.hash(spec.candidate("UnderstandingCandidate"))))throw new IOException("La fuente de comprensión cambió.");File jar=new File(row.getString("jar"));if(!jar.getCanonicalPath().startsWith(OnDeviceJava.home(c).getCanonicalPath()+File.separator)||jar.canWrite()||!ProjectRuntime.digest(jar).equals(row.getString("jarHash")))throw new IOException("El candidato de comprensión cambió.");return OnDeviceJava.load(c,jar).getClassLoader().loadClass("com.bananalab.patch.UnderstandingCandidate").getMethod("rewrite",String.class);}
 static void restore(Context c){try{String raw=prefs(c).getString("active","");active=raw.isEmpty()?null:method(c,new JSONObject(raw));error="";}catch(Exception e){active=null;error="Conservé las reglas instaladas: "+AndroidRepair.summary(e);}}
 static String rewrite(Context c,String input){try{return active==null?UnderstandingRules.rewrite(input):(String)active.invoke(null,input);}catch(Exception e){error="Falló la mejora activa; conservé el pedido original.";return input;}}
}
