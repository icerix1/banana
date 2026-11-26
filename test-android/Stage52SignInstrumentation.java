package com.bananalab.lite;
import android.app.*;import android.os.*;import android.content.*;import java.io.*;
public final class Stage52SignInstrumentation extends Instrumentation{
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle out=new Bundle();try{Context c=getTargetContext();DurableCode.awaitReady();ApkSigning.importKey(c,new FileInputStream(new File(c.getExternalFilesDir(null),"owner.zip")));DeviceApk.build(c);out.putString("stream","PASS STAGE52_SIGN actual Android v1/v2/v3 APK signing and verification.\n");finish(Activity.RESULT_OK,out);}catch(Throwable e){StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));out.putString("stream","FAIL "+s);finish(Activity.RESULT_CANCELED,out);}}
}
