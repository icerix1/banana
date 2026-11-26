package com.bananalab.lite;
import android.app.Activity;import android.os.Bundle;import android.content.Intent;
/** Compatibility entry point. All functionality lives in the unified workspace. */
public final class ChatActivity extends Activity{@Override public void onCreate(Bundle b){super.onCreate(b);Intent i=new Intent(this,BananaActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);finish();}}
