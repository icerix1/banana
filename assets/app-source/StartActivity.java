package com.bananalab.lite;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.widget.TextView;
import android.graphics.Color;
public final class StartActivity extends Activity {
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  if(!Diagnostics.read(this).isEmpty()){Diagnostics.show(this);return;}
  TextView opening=new TextView(this);opening.setText("Banana Lab Lite\nIniciando…");opening.setTextColor(Color.BLACK);opening.setBackgroundColor(Color.WHITE);opening.setTextSize(24);opening.setPadding(30,70,30,30);setContentView(opening);
  new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){public void run(){if(isFinishing())return;try{startActivity(new Intent(StartActivity.this,BananaActivity.class));finish();}catch(Throwable error){Diagnostics.show(StartActivity.this,error);}}},100);
 }
}
