package com.bananalab.lite;
public class TenTurnCheck {
 public static void main(String[] args)throws Exception{
  Semilla model=SemillaCheck.load(args[0]);SceneDraft d=null;
  String[] messages={"dibuja una banana con sombrero y un pepino azul en una plaza","hace al pepino pequeno","ponelo a la izquierda","no el pepino era verde","cambia el fondo a bosque","hace al segundo grande","pone al ultimo a la derecha","el pepino salta","quita los movimientos","hace a la banana roja"};
  for(String m:messages){SceneConversation.Result r=SceneConversation.apply(model,m,d);if(r.draft==null)throw new AssertionError(m+": "+r.reply);d=SceneDraft.read(r.draft.json().toString());}
  if(!d.get("banana").look.contains("sombrero")||!d.get("banana").look.contains("roja")||!d.get("pepino").look.contains("verde")||d.get("pepino").x!=82||d.get("pepino").size<1.1||!d.actions.isEmpty()||!d.setting.equals("bosque"))throw new AssertionError(d.describe());
  System.out.println("PASS: diez turnos, persistencia y atributos independientes");
 }
}
