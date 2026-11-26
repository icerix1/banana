package com.bananalab.lite;
import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
final class OwnBrain {
 private static Semilla model;
 static synchronized Semilla load(Context context)throws IOException{
  if(model==null){
   model=new Semilla(context.getAssets().open("language/semilla.bin"),context.getAssets().open("language/vocab.txt"),context.getAssets().open("language/labels.txt"));
   try{model.restoreAdaptation(new AtomicFile(new File(context.getFilesDir(),"semilla-adaptation.bin")).openRead());}catch(IOException ignored){}
  }return model;
 }
 static OwnPlan plan(Context context,String text)throws IOException{
  if(text.trim().isEmpty()||text.length()>600)throw new IllegalArgumentException("Escribi entre 1 y 600 caracteres");return new OwnPlan(load(context),text);
 }
 static synchronized void reset(Context context){model=null;new AtomicFile(new File(context.getFilesDir(),"semilla-adaptation.bin")).delete();context.getSharedPreferences("semilla-stats",0).edit().clear().apply();context.getSharedPreferences("semilla-seen",0).edit().clear().apply();}
 static synchronized void observe(Context context,String text)throws IOException{
  android.content.SharedPreferences seen=context.getSharedPreferences("semilla-seen",0);String key=Integer.toHexString(text.hashCode());if(seen.contains(key))return;
  Semilla network=load(context);int count=network.observe(text);if(count==0)return;
  AtomicFile file=new AtomicFile(new File(context.getFilesDir(),"semilla-adaptation.bin"));FileOutputStream stream=file.startWrite();
  try{network.saveAdaptation(stream);file.finishWrite(stream);}catch(IOException error){file.failWrite(stream);throw error;}
  if(seen.getAll().size()>=64)seen.edit().clear().commit();seen.edit().putBoolean(key,true).commit();
  android.content.SharedPreferences prefs=context.getSharedPreferences("semilla-stats",0);prefs.edit().putInt("examples",Math.min(1000000,prefs.getInt("examples",0)+count)).apply();
 }
}
