package com.bananalab.lite;
import java.util.*;
/** Evidence from the resulting scene, not a claim of visual inspection. */
final class TaskReceipt{
 static String describe(SceneDraft before,SceneDraft after,String[] steps)throws Exception{
  SceneDraft.read(after.json().toString());StringBuilder b=new StringBuilder("Pedido aplicado: ").append(steps.length).append(steps.length==1?" paso.":" pasos.");for(int i=0;i<steps.length;i++)b.append("\n").append(i+1).append(". ").append(steps[i]);
  b.append("\nResultado comprobado: ").append(after.cast.size()).append(" personajes, ").append(after.actions.size()).append(" acciones.");
  if(before!=null){List<String> kept=new ArrayList<>(),changed=new ArrayList<>();for(SceneDraft.Actor a:before.cast){SceneDraft.Actor n=after.get(a.name);if(n!=null){if(a.designId.equals(n.designId)&&a.kind.equals(n.kind)&&a.look.equals(n.look)&&a.reference.equals(n.reference)&&a.view.equals(n.view)&&a.label.equals(n.label)&&a.size==n.size)kept.add(n.display());else changed.add(n.display());}}if(!kept.isEmpty())b.append("\nDiseños conservados: ").append(join(kept)).append('.');if(!changed.isEmpty())b.append("\nDiseños modificados: ").append(join(changed)).append('.');}
  return b.append("\nRevisá la vista previa para comprobar la apariencia y el movimiento.").toString();
 }
 static String join(List<String> rows){StringBuilder b=new StringBuilder();for(String s:rows){if(b.length()>0)b.append(", ");b.append(s);}return b.toString();}
}
