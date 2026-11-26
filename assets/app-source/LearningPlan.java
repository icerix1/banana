package com.bananalab.lite;
/** Pure scheduling decisions shared by the service and the host checks. */
final class LearningPlan{
 static final long REVIEW_MS=15*60*1000L,DAY_MS=24*60*60*1000L;static final int ROUNDS=3;
 static String resources(int battery,boolean charging,boolean saving,boolean busy){if(busy)return "el chat o el editor están trabajando";if(!charging&&battery>=0&&battery<25)return "batería por debajo del25%";if(!charging&&saving)return "ahorro de batería activado";return "";}
 static boolean review(long now,long due,int rounds,long day){return now>=due&&(now/DAY_MS!=day||rounds<ROUNDS);}
 static long delay(String reason,int failures){if(reason.contains("chat")||reason.contains("editor"))return 5000;if(!reason.isEmpty())return 60000;return failures>0?Math.min(900000,60000L*(1L<<Math.min(3,failures-1))):2000;}
}
