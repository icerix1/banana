package com.bananalab.lite;
import java.util.*;
/** Deterministic positions from previous slot state, shared by preview and MP4. */
final class ScenePositions{
 static Map<String,Float> at(SceneDraft d,float time){Map<String,Float> positions=new HashMap<>();for(SceneDraft.Actor a:d.cast)positions.put(a.name,a.x);int beats=ObjectTransfer.beats(d);float phase=Math.max(0,Math.min(1,time))*beats;int current=Math.min(beats-1,(int)phase);
  for(int slot=0;slot<=current;slot++){Map<String,Float> start=new HashMap<>(positions);float t=slot==current?(time>=1?1:phase-slot):1,e=Scene.ease(t);
   for(int i=0;i<d.actions.size();i++){if(d.slot(i)!=slot)continue;SceneDraft.Actor a=d.cast.get(d.actors.get(i));String action=d.actions.get(i);float from=start.get(a.name),to=from;
    if(action.equals("entrader")){from=122;to=a.x;}else if(action.equals("entraizq")){from=-22;to=a.x;}else if(action.equals("saleder"))to=122;else if(action.equals("saleizq"))to=-22;
    else if((action.equals("camina")||action.equals("corre")))to=Math.min(88,from+7);
    else if((action.equals("acerca")||action.equals("acercaactor"))){SceneDraft.Actor recipient=d.get(d.recipient(i));SceneDraft.Prop prop=d.prop(d.target(i));float target=prop!=null?prop.x-22:recipient!=null?start.get(recipient.name):d.cast.size()>1?start.get(d.cast.get(d.actors.get(i)==0?1:0).name):from+(from<50?12:-12);if(prop!=null)to=target;else if(d.cast.size()==1)to=target;else{float delta=target-from,distance=Math.max(0,Math.abs(delta)-22);if(recipient!=null)for(int j=0;j<d.actions.size();j++)if(d.slot(j)==slot&&d.actions.get(j).equals("acercaactor")&&d.cast.get(d.actors.get(j))==recipient&&d.recipient(j).equals(a.name))distance*=.5f;to=from+Math.signum(delta)*distance;}}
    positions.put(a.name,from+(to-from)*e);
   }
  }return positions;
 }
}
