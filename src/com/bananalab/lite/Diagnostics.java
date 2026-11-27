package com.bananalab.lite;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.graphics.Color;
import java.io.*;

final class Diagnostics {
 static String phase="Inicio";
 static void record(Context context,Throwable error){
  try{
   StringWriter stack=new StringWriter();error.printStackTrace(new PrintWriter(stack));
   String report="Banana 0.73 / Chat3.4 Pixel2.3\nAndroid "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nDispositivo: "+Build.MANUFACTURER+" "+Build.MODEL+"\nMódulos: revisión "+context.getSharedPreferences("banana-runtime-modules",0).getInt("revision",0)+"\nEtapa: "+phase+"\n\n"+stack.toString();
   if(report.length()>16000)report=report.substring(0,16000);
   try(FileOutputStream out=context.openFileOutput("last-crash.txt",Context.MODE_PRIVATE)){out.write(report.getBytes("UTF-8"));}
  }catch(Throwable ignored){}
 }
 static String read(Context context){
  try(FileInputStream in=context.openFileInput("last-crash.txt")){
   ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;
   while((n=in.read(b))>0&&data.size()<16000)data.write(b,0,n);
   return data.toString("UTF-8");
  }catch(Exception e){return "";}
 }
 static void show(final Activity activity,Throwable error){record(activity,error);show(activity);}
 static void show(final Activity activity){
  final String report=read(activity);
  android.util.Log.e("BananaLab",report);
  ScrollView scroll=new ScrollView(activity);LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(24,42,24,24);body.setBackgroundColor(Color.WHITE);scroll.addView(body);
  TextView title=new TextView(activity);title.setText("No se pudo iniciar Banana Lab");title.setTextSize(23);title.setTextColor(Color.BLACK);body.addView(title);
  TextView help=new TextView(activity);help.setText("Copiá este error y pegalo en el chat para corregir la causa. No hace falta reinstalar Python ni descargar modelos.");help.setTextSize(16);help.setTextColor(Color.BLACK);body.addView(help);
  Button copy=new Button(activity);copy.setText("Copiar error");body.addView(copy);copy.setOnClickListener(new android.view.View.OnClickListener(){public void onClick(android.view.View v){android.content.ClipboardManager cb=(android.content.ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);cb.setPrimaryClip(ClipData.newPlainText("Error Banana Lab",report));Toast.makeText(activity,"Error copiado",Toast.LENGTH_SHORT).show();}});
  Button retry=new Button(activity);retry.setText("Reintentar con configuración limpia");body.addView(retry);retry.setOnClickListener(new android.view.View.OnClickListener(){public void onClick(android.view.View v){activity.deleteFile("last-crash.txt");activity.getSharedPreferences("MainActivity",0).edit().remove("image").apply();Intent i=new Intent(activity,MainActivity.class);activity.startActivity(i);activity.finish();}});
  TextView details=new TextView(activity);details.setText(report.isEmpty()?"No hay un informe guardado.":report);details.setTextColor(Color.BLACK);details.setTextSize(12);details.setTextIsSelectable(true);body.addView(details);
  activity.setContentView(scroll);
 }
}
