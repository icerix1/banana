package com.bananalab.lite;
/** Explains the actual executable plan; does not claim a render was already inspected. */
final class SceneAudit{
 static String report(SceneDraft d)throws Exception{SceneDraft.read(d.json().toString());int timed=0;for(int i=0;i<d.actions.size();i++)timed=Math.max(timed,d.slot(i)+1);StringBuilder s=new StringBuilder("Plan comprobado: ").append(d.cast.size()).append(" personajes, ").append(d.actions.size()).append(" acciones, ").append(timed).append(" tiempos.\n").append(d.describe());for(SceneDraft.Actor a:d.cast){boolean acts=false;for(int i:d.actors)acts|=d.cast.get(i)==a;if(!acts)s.append("\n").append(a.display()).append(" permanece visible sin una acción asignada.");}return s.append("\nEl plan está validado; revisá la animación para comprobar su apariencia.").toString();}
}
