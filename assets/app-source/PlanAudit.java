package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
/** Checks supported entity/action coverage and preservation; not universal semantic grading. */
final class PlanAudit{
 static String verify(String request,SceneDraft before,SceneDraft after)throws Exception{
  String text=ScenePlanner.language(request);Set<String> asked=new LinkedHashSet<>();for(String kind:SceneDirector.TYPES.split("\\|"))if(SceneDirector.word(text,kind)){boolean present=false;for(SceneDraft.Actor a:after.cast)present|=a.kind.equals(kind);if(!present)throw new IllegalArgumentException("Falta el personaje pedido: "+kind);asked.add(kind);}
  Map<String,Integer> required=new HashMap<>(),actual=new HashMap<>();Matcher verbs=ScenePlanner.VERB.matcher(text);while(verbs.find()){String action=ScenePlanner.action(verbs.group(),text);required.put(action,required.containsKey(action)?required.get(action)+1:1);}for(String a:after.actions){String normalized=a.matches("entraizq|entrader")?"entrader":a;actual.put(normalized,actual.containsKey(normalized)?actual.get(normalized)+1:1);}for(Map.Entry<String,Integer> entry:required.entrySet()){String key=entry.getKey().matches("entraizq|entrader")?"entrader":entry.getKey();if(!actual.containsKey(key)||actual.get(key)<entry.getValue())throw new IllegalArgumentException("No pude cubrir todas las acciones: "+entry.getKey());}
  if(before!=null)for(SceneDraft.Actor old:before.cast){SceneDraft.Actor now=after.get(old.name);if(now==null||!now.designId.equals(old.designId)||!now.kind.equals(old.kind)||!now.label.equals(old.label)||!now.look.equals(old.look)||!now.reference.equals(old.reference)||now.size!=old.size||!now.view.equals(old.view))throw new IllegalArgumentException("El plan alteró el diseño de "+old.display());}
  SceneDraft.read(after.json().toString());int total=0;for(int n:required.values())total+=n;return "Cobertura comprobada: "+asked.size()+" tipos mencionados y "+total+" acciones expresadas. Diseños existentes conservados.";
 }
 static void verifyExtension(String request,SceneDraft before,SceneDraft after)throws Exception{
  verify(request,before,after);int n=before.actions.size();if(after.actions.size()<=n)throw new IllegalArgumentException("La continuación no agregó acciones.");for(int i=0;i<n;i++)if(!before.actions.get(i).equals(after.actions.get(i))||!before.actors.get(i).equals(after.actors.get(i))||before.slot(i)!=after.slot(i)||!before.target(i).equals(after.target(i))||!before.recipient(i).equals(after.recipient(i)))throw new IllegalArgumentException("La continuación alteró una acción anterior.");
  for(SceneDraft.Prop p:before.props){SceneDraft.Prop next=after.prop(p.id);if(next==null||!next.holder.equals(p.holder)||next.x!=p.x||next.y!=p.y||next.open!=p.open)throw new IllegalArgumentException("La continuación alteró un objeto anterior.");}
  int end=0;for(int i=0;i<n;i++)end=Math.max(end,before.slot(i));for(int i=n;i<after.actions.size();i++)if(n>0&&after.slot(i)<=end)throw new IllegalArgumentException("La continuación debe empezar después de la secuencia anterior.");SceneConsistency.require(after);
 }

}
