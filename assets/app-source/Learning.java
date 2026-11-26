package com.bananalab.lite;
import android.app.*;
import android.widget.*;
import android.content.*;
import android.util.AtomicFile;
import java.io.*;
import java.text.Normalizer;
import java.util.Locale;

final class Learning {
 static String key(String phrase) {
  return Normalizer.normalize(phrase.trim().toLowerCase(Locale.ROOT),Normalizer.Form.NFD)
   .replaceAll("\\p{M}","").replaceAll("\\s+"," ");
 }
 static String resolve(Activity app,String phrase) {
  return app.getSharedPreferences("lessons",0).getString(key(phrase),phrase);
 }
 static void phrase(final Activity app,final EditText prompt) {
  final String original=prompt.getText().toString().trim();
  if(original.isEmpty()){Toast.makeText(app,"Primero escribí la frase que querés enseñarle.",Toast.LENGTH_LONG).show();return;}
  final EditText meaning=new EditText(app);
  meaning.setHint("Ej.: un pepino se acerca y habla");
  meaning.setText(resolve(app,original));
  final AlertDialog dialog=new AlertDialog.Builder(app).setTitle("Enseñar esta frase")
   .setMessage("Escribí la escena equivalente con personajes y acciones que ya conoce.")
   .setView(meaning).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   String value=meaning.getText().toString().trim();
   try{new Scene(value);}catch(IllegalArgumentException error){meaning.setError(error.getMessage());return;}
   app.getSharedPreferences("lessons",0).edit().putString(key(original),value).apply();
   Toast.makeText(app,"Frase aprendida en este dispositivo.",Toast.LENGTH_LONG).show();dialog.dismiss();
  }));
  UiTypeface.show(dialog);
 }
 static void movement(final Activity app,final Motion current) {
  LinearLayout body=new LinearLayout(app);body.setOrientation(1);body.setPadding(25,15,25,10);
  TextView help=new TextView(app);help.setText("Ajustá cuánto salta y gira. Aprenderá estas preferencias con entrenamiento local.");body.addView(help);
  TextView jump=new TextView(app);jump.setText("Altura del salto");body.addView(jump);
  final SeekBar height=new SeekBar(app);height.setMax(100);height.setProgress(app.getSharedPreferences("training",0).getInt("height",80));body.addView(height);
  TextView spin=new TextView(app);spin.setText("Amplitud del giro");body.addView(spin);
  final SeekBar angle=new SeekBar(app);angle.setMax(100);angle.setProgress(app.getSharedPreferences("training",0).getInt("angle",80));body.addView(angle);
  UiTypeface.show(new AlertDialog.Builder(app).setTitle("Enseñar movimientos").setView(body).setNegativeButton("Cancelar",null)
   .setPositiveButton("Entrenar acá",(d,w)->train(app,current,height.getProgress(),angle.getProgress())));
 }
 static void train(Activity app,Motion current,int height,int angle){new Thread(()->{try{Motion next=current.copy();for(int step=0;step<2400;step++){int action=step%3;float time=((step*37)%101)/100f,wave=(float)Math.sin(Math.PI*time);next.teach(action,time,new float[]{action==0?time*.8f:0,action==1?-wave*Math.min(.95f,height/100f):0,action==2?wave*Math.min(.95f,angle/100f):0},.025f);}save(app,next);app.getSharedPreferences("training",0).edit().putInt("height",height).putInt("angle",angle).apply();app.runOnUiThread(()->Toast.makeText(app,"Movimiento guardado.",Toast.LENGTH_SHORT).show());}catch(Exception e){app.runOnUiThread(()->Toast.makeText(app,"No se pudo guardar el movimiento.",Toast.LENGTH_LONG).show());}},"banana-motion-training").start();}
 static Motion load(Context app)throws IOException {
  File file=new File(app.getFilesDir(),"learned-motion.bin");
  if(file.exists())try(InputStream in=new AtomicFile(file).openRead()){return new Motion(in);}catch(IOException error){file.delete();}
  try(InputStream in=app.getAssets().open("motion.bin")){return new Motion(in);}
 }
 static void save(Context app,Motion model)throws IOException {
  AtomicFile file=new AtomicFile(new File(app.getFilesDir(),"learned-motion.bin"));
  FileOutputStream out=file.startWrite();
  try{model.save(out);file.finishWrite(out);}catch(IOException e){file.failWrite(out);throw e;}
 }
}
