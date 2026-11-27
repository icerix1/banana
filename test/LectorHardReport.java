package com.bananalab.lite;
import org.json.*;import java.io.*;import java.nio.file.*;import java.util.*;
/** Host report: with about 22 candidate sentences of one article, how often the right one is among those the reader gets to judge,
 *  and how often it is finally chosen. Usage: LectorHardReport raiz ejemplos.json salida.json [peso ...] */
public final class LectorHardReport{
 public static void main(String[] a)throws Exception{BananaTransformer m;try(InputStream in=Files.newInputStream(Paths.get(a[0],"assets/lector3/lector32.bin"))){m=BananaTransformer.container(in);}JSONArray examples=new JSONArray(new String(Files.readAllBytes(Paths.get(a[1])),"UTF-8"));JSONObject out=new JSONObject();
  double[] weights=a.length>3?new double[a.length-3]:new double[]{Lector.MEANING_WEIGHT};for(int i=3;i<a.length;i++)weights[i-3]=Double.parseDouble(a[i]);
  for(double w:weights){Lector.MEANING_WEIGHT=w;int n=0,within=0,right=0,shown=0,shownRight=0;for(int e=0;e<examples.length();e++){JSONArray row=examples.getJSONArray(e);List<String> sentences=new ArrayList<>();JSONArray c=row.getJSONArray(1);for(int k=0;k<c.length();k++)sentences.add(c.getString(k));String q=row.getString(2);int goal=sentences.indexOf(row.getString(3));if(goal<0)continue;n++;
    int[] shared=new int[sentences.size()];List<Integer> order=Lector.order(m,sentences,q,shared);if(order.subList(0,Math.min(Lector.MAX_CANDIDATES,order.size())).contains(goal))within++;
    if(weights.length==1||w==weights[weights.length-1]){Lector.Pick pick=Lector.select(m,row.getString(0),sentences,q,Lector.MAX_CANDIDATES);if(pick!=null&&pick.index==goal)right++;if(pick!=null&&pick.confidence>=Lector.SELECT_CONFIDENCE){shown++;if(pick.index==goal)shownRight++;}}}
   out.put("weight"+w,new JSONObject().put("examples",n).put("answerAmongJudged",within).put("chosenRight",right).put("shownAtAppThreshold",shown).put("shownRight",shownRight));System.out.println("peso "+w+": entre las "+Lector.MAX_CANDIDATES+" leídas "+within+"/"+n+" · elegida "+right+" · mostradas "+shown+" bien "+shownRight);}
  Files.write(Paths.get(a[2]),out.put("note","Unas 22 oraciones candidatas del mismo artículo; preguntas de plantilla. La elección final sólo se mide con el último peso.").toString(1).getBytes("UTF-8"));}
}
