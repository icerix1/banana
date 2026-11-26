package com.bananalab.lite;
import android.content.*;
final class DialogueMemory{
 static synchronized String reply(Context c,String text){SharedPreferences p=c.getSharedPreferences("banana-dialogue",0);DialogueState state=new DialogueState();state.name=p.getString("name","");state.interest=p.getString("interest","");state.last=p.getString("last","");String reply=state.reply(text);if(reply!=null)p.edit().putString("name",state.name).putString("interest",state.interest).putString("last",state.last).apply();return reply;}
 static void resetConversation(Context c){c.getSharedPreferences("banana-dialogue",0).edit().remove("last").apply();}
 static void clear(Context c){c.getSharedPreferences("banana-dialogue",0).edit().clear().apply();}
}
