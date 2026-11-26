package com.bananalab.lite;
/** Portable planning policy. Edit the whole rule set; execution uses a stable scene contract. */
final class PlannerProfile { static String rules(){ return "{\"version\":1,\"rules\":[{\"id\":\"fiesta\",\"phrase\":\"{actor} arma una fiesta con {otro}\",\"steps\":[{\"who\":\"actor\",\"move\":\"baila\"},{\"who\":\"otro\",\"move\":\"salta\"},{\"who\":\"actor\",\"move\":\"aplaude\"}]},{\"id\":\"entrada\",\"phrase\":\"{actor} hace una entrada triunfal\",\"steps\":[{\"who\":\"actor\",\"move\":\"saluda\"},{\"who\":\"actor\",\"move\":\"salta\"}]}]}"; } }
