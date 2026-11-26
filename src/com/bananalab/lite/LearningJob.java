package com.bananalab.lite;
import android.app.job.*;
/** OS-scheduled bounded fallback. Never launches a foreground service from a job. */
public final class LearningJob extends JobService{
 Thread task;volatile int generation;
 @Override public boolean onStartJob(final JobParameters params){if(!LearningLoop.enabled(this)||LearningLoop.serviceRunning)return false;final int ticket=++generation;task=new Thread(new Runnable(){public void run(){try{android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);LearningLoop.cycle(LearningJob.this);}catch(Exception e){if(ticket==generation)LearningLoop.status(LearningJob.this,"Android reintentará cuando sea posible: "+AndroidRepair.summary(e));}finally{if(ticket==generation)jobFinished(params,false);}}},"banana-learning-job");task.start();return true;}
 @Override public boolean onStopJob(JobParameters p){generation++;if(task!=null)task.interrupt();return LearningLoop.enabled(this);}
 @Override public void onDestroy(){generation++;if(task!=null)task.interrupt();super.onDestroy();}
}
