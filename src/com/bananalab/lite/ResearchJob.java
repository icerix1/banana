package com.bananalab.lite;
import android.app.job.*;
/** One bounded topic per invocation; Android owns network and battery scheduling. */
public final class ResearchJob extends JobService{
 private Thread worker;private volatile int generation;
 @Override public boolean onStartJob(final JobParameters parameters){final int ticket=++generation;worker=new Thread(()->{try{ResearchConversation.updateOne(getApplicationContext(),ResearchWeb.NETWORK);}catch(Exception e){if(generation==ticket)ResearchStore.prefs(this).edit().putString("lastError","No pude actualizar: "+e.getClass().getSimpleName()).apply();}finally{if(generation==ticket)jobFinished(parameters,false);}},"banana-research-background");worker.start();return true;}
 @Override public boolean onStopJob(JobParameters parameters){generation++;if(worker!=null)worker.interrupt();return true;}
}
