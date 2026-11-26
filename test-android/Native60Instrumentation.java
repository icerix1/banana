package com.bananalab.lite;
import android.app.*;import android.content.*;import android.os.*;import android.graphics.Typeface;import android.widget.*;import android.view.*;import java.util.*;import java.io.*;
public final class Native60Instrumentation extends Instrumentation{
 void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 BananaEngine.Result run(Context c,String s)throws Exception{return BananaEngine.run(c,s,new ArrayList<String[]>(),"",true,OwnBrain.load(c));}
 static Button send(View v){if(v instanceof Button&&((Button)v).getText().toString().equals("Enviar"))return (Button)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Button b=send(g.getChildAt(i));if(b!=null)return b;}}return null;}
 @Override public void onStart(){Bundle out=new Bundle();try{Context c=getTargetContext();DurableCode.awaitReady();int revision=AppJavaWorkspace.prefs(c).getInt("revision",0);
 check(run(c,"cambiame el color del boton").text.contains("Qué botón"),"asks target");check(run(c,"Enviar").text.contains("Qué color"),"remembers target");check(AppJavaWorkspace.prefs(c).getInt("revision",0)==revision,"questions leave code untouched");
 check(run(c,"rojo").text.contains("Color del botón"),"color response compiles and activates");check(AppJavaWorkspace.live.buttonColors.getInt("enviar")==4,"targeted color active");
 check(run(c,"cambia la fuente de la app").text.contains("Qué fuente"),"asks font");check(run(c,"elegi vos").text.contains("Fuente de la interfaz"),"font follow up compiles");check(AppJavaWorkspace.live.font==4,"font chosen");
 run(c,"cambia el tema de la app");check(run(c,"cancelalo").text.contains("Cancelé"),"cancel");check(RequestDialogue.prefs(c).getString("kind","").isEmpty(),"cancel clears");
 run(c,"cambia el tema de la app");run(c,"aprende que el proyecto Limon usa el taller Violeta.");check(RequestDialogue.prefs(c).getString("kind","").isEmpty(),"new learning task clears unrelated question");check(KnowledgeNotebook.retrieve(KnowledgeNotebook.data(c),"que taller usa el proyecto Limon").contains("Violeta"),"learning remains routed");
 run(c,"cambia el tema de la app");RequestDialogue.prefs(c).edit().putLong("saved",0).commit();check(run(c,"azul").text.contains("venció"),"expired reply not applied");
 final Activity a=startActivitySync(new Intent(c,BananaActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));runOnMainSync(new Runnable(){public void run(){Button b=send(a.getWindow().getDecorView());check(b!=null&&b.getTypeface().equals(Typeface.create("sans-serif-medium",b.getTypeface().getStyle())),"real font");check(b.getBackgroundTintList().getDefaultColor()==0xff962a32,"real color");a.finish();}});
 out.putString("stream","PASS NATIVE60 chained clarification, no changes before completion, actual compiled font/color, cancellation, expiry, unrelated learning route and real UI.\n");finish(Activity.RESULT_OK,out);
 }catch(Throwable e){StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));out.putString("stream","FAIL "+s);finish(Activity.RESULT_CANCELED,out);}}
}
