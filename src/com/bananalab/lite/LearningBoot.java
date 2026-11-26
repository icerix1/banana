package com.bananalab.lite;
import android.content.*;
public final class LearningBoot extends BroadcastReceiver{
 @Override public void onReceive(Context context,Intent intent){if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))LearningLoop.configure(context);}
}
