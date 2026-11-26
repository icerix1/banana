package com.bananalab.lite;
import android.app.job.*;import android.content.*;
final class ResearchScheduler{
 static final int ID=3801;
 static void configure(Context c){try{JobScheduler jobs=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);if(jobs==null)return;if(!ResearchStore.prefs(c).getBoolean("enabled",true)){jobs.cancel(ID);return;}boolean wifi=ResearchStore.prefs(c).getBoolean("wifi",true);JobInfo existing=null;for(JobInfo pending:jobs.getAllPendingJobs())if(pending.getId()==ID)existing=pending;int network=wifi?JobInfo.NETWORK_TYPE_UNMETERED:JobInfo.NETWORK_TYPE_ANY;if(existing!=null&&existing.getNetworkType()==network)return;JobInfo job=new JobInfo.Builder(ID,new ComponentName(c,ResearchJob.class)).setPeriodic(60*60*1000L).setRequiredNetworkType(network).setRequiresCharging(false).setPersisted(true).build();int result=jobs.schedule(job);ResearchStore.prefs(c).edit().putString("schedule",result==JobScheduler.RESULT_SUCCESS?"Programado":"Android rechazó la tarea").apply();}catch(Exception e){ResearchStore.prefs(c).edit().putString("schedule","No se pudo programar: "+e.getClass().getSimpleName()).apply();}}
}
