package com.bananalab.lite;
import java.util.*;
/** Chronological object/actor invariants, independent of generated text. */
final class SceneConsistency{
 static String problem(SceneDraft d){if(d==null)return null;try{
  SceneDraft.read(d.json().toString());String kiss=KissMotion.problem(d);if(kiss!=null)return kiss;Map<String,String> owners=new HashMap<>();for(SceneDraft.Prop p:d.props)owners.put(p.id,p.holder);
  for(int slot=0;slot<SceneTiming.MAX_BEATS;slot++){Set<String> touched=new HashSet<>();Map<String,String> updates=new HashMap<>();
   for(int i=0;i<d.actions.size();i++){if(d.slot(i)!=slot)continue;String action=d.actions.get(i),target=d.target(i),actor=d.cast.get(d.actors.get(i)).name;
    if(action.equals("sostiene")){if(!owners.containsKey(target))return "Sostener requiere un objeto existente.";String owner=owners.get(target);if(!owner.isEmpty()&&!owner.equals(actor))return "En el tiempo "+(slot+1)+", otro personaje ya sostiene ese objeto.";if(!touched.add(target))return "Dos operaciones usan el mismo objeto al mismo tiempo.";updates.put(target,actor);}
    if(action.equals("entrega")){if(!touched.add(target))return "Dos entregas usan el mismo objeto al mismo tiempo. Separalas en tiempos distintos.";if(!actor.equals(owners.get(target)))return "En el tiempo "+(slot+1)+", "+d.get(actor).display()+" no tiene el objeto que intenta entregar.";String receiver=d.recipient(i);int pairs=0;for(int j=0;j<d.actions.size();j++)if(d.slot(j)==slot&&d.actions.get(j).equals("recibe")&&d.target(j).equals(target)&&d.cast.get(d.actors.get(j)).name.equals(receiver)&&d.recipient(j).equals(actor))pairs++;if(pairs!=1)return "La entrega necesita una recepción coincidente en el mismo tiempo.";updates.put(target,receiver);}
    if(action.equals("recibe")){int pairs=0;for(int j=0;j<d.actions.size();j++)if(d.slot(j)==slot&&d.actions.get(j).equals("entrega")&&d.target(j).equals(target)&&d.recipient(j).equals(actor)&&d.cast.get(d.actors.get(j)).name.equals(d.recipient(i)))pairs++;if(pairs!=1)return "Hay una recepción sin una entrega coincidente.";}
   }owners.putAll(updates);
  }return null;
 }catch(Exception e){return e.getMessage()==null?"La secuencia no es válida.":e.getMessage();}}
 static void require(SceneDraft d)throws Exception{String issue=problem(d);if(issue!=null)throw new IllegalArgumentException(issue);}
}
