package com.bananalab.lite;
/** Pure timeline state shared by drawing, ownership checks and export. */
final class ObjectTransfer{
 static final class State{String from="",to="";float blend;boolean moving;String owner(){return moving&&blend<.5f?from:to;}}
 static State at(SceneDraft d,String prop,float time,int beats){State out=new State();SceneDraft.Prop p=d.prop(prop);if(p==null)return out;out.from=out.to=p.holder;
  java.util.List<Integer> order=new java.util.ArrayList<>();for(int i=0;i<d.actions.size();i++)order.add(i);java.util.Collections.sort(order,(a,b)->Integer.compare(d.slot(a),d.slot(b)));for(int i:order){if(!d.target(i).equals(prop))continue;float phase=time*beats-d.slot(i);if(phase<0)continue;String action=d.actions.get(i),actor=d.cast.get(d.actors.get(i)).name;
   if(action.equals("sostiene")){out.from=out.to=actor;out.moving=false;}
   if(action.equals("entrega")){out.from=actor;out.to=d.recipient(i);out.blend=Scene.ease((phase-.2f)/.6f);out.moving=phase<1;}
  }return out;
 }
 static int beats(SceneDraft d){int n=1;for(int i=0;i<d.actions.size();i++)n=Math.max(n,d.slot(i)+1);return n;}
 static boolean holding(SceneDraft d,String actor,float time){for(SceneDraft.Prop p:d.props){State s=at(d,p.id,time,beats(d));if(s.owner().equals(actor)||(s.moving&&(s.from.equals(actor)||s.to.equals(actor))))return true;}return false;}
}
