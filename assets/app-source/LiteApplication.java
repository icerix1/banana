package com.bananalab.lite;
import android.app.Application;
public final class LiteApplication extends Application {
 @Override public void onCreate(){
  super.onCreate();try(java.io.InputStream process=new java.io.FileInputStream("/proc/self/cmdline")){if(new String(OnDeviceJava.bytes(process,512),"UTF-8").trim().endsWith(":banana_java_trial"))return;}catch(Exception ignored){}registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks(){public void onActivityResumed(android.app.Activity a){LearningLoop.opened(a);}public void onActivityPaused(android.app.Activity a){LearningLoop.closed();}public void onActivityCreated(android.app.Activity a,android.os.Bundle b){}public void onActivityStarted(android.app.Activity a){}public void onActivityStopped(android.app.Activity a){}public void onActivitySaveInstanceState(android.app.Activity a,android.os.Bundle b){}public void onActivityDestroyed(android.app.Activity a){}});PageReader.cache=getCacheDir();UiTypeface.install(this);ModularHub.restore(this);AdaptiveMemory.restore(this);JavaMethodEditor.restore(this);JavaFunction.restore(this);AppJavaWorkspace.restore(this);JavaLive.restore(this);ProjectRuntime.restore(this);PlannerExperience.restore(this);MethodLab.restore(this);UnderstandingRuntime.restore(this);DurableCode.start(this);ResearchScheduler.configure(this);final Thread.UncaughtExceptionHandler previous=Thread.getDefaultUncaughtExceptionHandler();
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){public void uncaughtException(Thread thread,Throwable error){
   Diagnostics.record(LiteApplication.this,error);
   if(previous!=null)previous.uncaughtException(thread,error);
   else{android.os.Process.killProcess(android.os.Process.myPid());System.exit(1);}
  }});
 }
}
