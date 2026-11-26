package com.bananalab.lite;
import android.content.Context;import java.io.*;import java.util.*;
/** Output-shape guard. It cannot verify facts or guarantee coherent reasoning. */
final class TextQuality{
 private static Set<String> words;
 static synchronized boolean acceptable(Context c,String output,String request,List<String[]> history)throws Exception{if(words==null){words=new HashSet<>();try(BufferedReader in=new BufferedReader(new InputStreamReader(c.getAssets().open("chat34/lexicon.txt"),"UTF-8"))){String line;while((line=in.readLine())!=null)words.add(line.trim());}}Set<String> allowed=new HashSet<>(words);for(String[] pair:history)for(String text:pair)allowed.addAll(Arrays.asList(ChatLanguage.normalize(text).split("\\W+")));allowed.addAll(Arrays.asList(ChatLanguage.normalize(request).split("\\W+")));return acceptable(output,allowed);}
 static boolean acceptable(String output,Set<String> allowed){if(output==null||output.trim().isEmpty()||output.contains("\ufffd"))return false;String[] tokens=ChatLanguage.normalize(output).split("\\W+");int n=0,known=0,run=0;String last="";Map<String,Integer> counts=new HashMap<>();for(String t:tokens){if(t.isEmpty())continue;n++;if(allowed.contains(t)||t.matches("[0-9]+"))known++;run=t.equals(last)?run+1:1;if(run>=4)return false;last=t;counts.put(t,counts.containsKey(t)?counts.get(t)+1:1);}if(n==0||known/(float)n<.72f)return false;for(int count:counts.values())if(n>12&&count>n*.4f)return false;return true;}
}
