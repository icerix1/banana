package com.bananalab.lite;
import java.io.*;
public class SharedSceneCheck{
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Semilla model=SemillaCheck.load(args[0]);SceneConversation.Result first=SceneConversation.apply(model,"Dibuja una banana con sombrero y un pepino azul en una plaza",null);
  System.out.println(first.reply);check(first.draft!=null,"create");SceneDraft scene=first.draft;check(scene.cast.size()==2,"cast");check(scene.get("banana").look.contains("sombrero"),"hat");check(!scene.get("banana").look.contains("azul"),"color leak");check(scene.get("pepino").look.contains("azul"),"blue");
  String banana=scene.get("banana").look;SceneConversation.Result edit=SceneConversation.apply(model,"hace al pepino mas pequeno y ponelo a la izquierda",scene);System.out.println(edit.reply);check(edit.draft!=null,"edit");check(edit.draft.get("pepino").size<1&&edit.draft.get("pepino").x==18,"size and position");check(edit.draft.get("banana").look.equals(banana),"preserve other");check(scene.get("pepino").size==1,"transactional original");scene=edit.draft;
  edit=SceneConversation.apply(model,"no el pepino era verde",scene);check(edit.draft!=null&&edit.draft.get("pepino").look.contains("verde")&&!edit.draft.get("pepino").look.contains("azul"),"replace color");scene=edit.draft;
  edit=SceneConversation.apply(model,"ponelo junto a la banana",scene);check(edit.draft!=null&&edit.draft.get("pepino").relation.equals("junto a banana"),"relative");scene=edit.draft;
  edit=SceneConversation.apply(model,"ponelo detras del arbol",scene);check(edit.draft!=null&&edit.draft.get("pepino").behind,"occlusion");scene=edit.draft;
  edit=SceneConversation.apply(model,"el otro era rojo",scene);check(edit.handled&&edit.draft==null,"ask ambiguity");
  SceneDraft copy=SceneDraft.read(scene.json().toString());check(copy.get("pepino").behind&&copy.get("banana").look.equals(banana),"persist");new Scene(copy);
  edit=SceneConversation.apply(model,"el pepino salta y despues saluda",scene);System.out.println(edit.reply);if(edit.draft!=null)System.out.println(edit.draft.actions);check(edit.draft!=null&&edit.draft.actions.size()==2,"sequence");scene=edit.draft;
  edit=SceneConversation.apply(model,"quita los movimientos",scene);check(edit.draft!=null&&edit.draft.actions.isEmpty(),"clear actions");
  edit=SceneConversation.apply(model,"saca el sombrero de la banana",scene);check(edit.draft!=null&&!edit.draft.get("banana").look.contains("sombrero"),"remove accessory");
  edit=SceneConversation.apply(model,"Dibuja una banana y un pepino en la playa",scene);check(edit.draft!=null&&edit.draft.get("banana").look.equals(banana)&&edit.draft.get("pepino").look.contains("verde"),"carry appearance between scenes");
  edit=SceneConversation.apply(model,"cambia el fondo a bosque",scene);check(edit.draft!=null&&edit.draft.setting.equals("bosque")&&edit.draft.get("banana").look.equals(banana),"background only");
  check(Scene.bodyColor("pepino","azul")==0xff629cdb,"renderer honors cucumber blue");check(Scene.bodyColor("pepino","")==0xff63bc76,"default cucumber green");
  DialogueState dialogue=new DialogueState();check(dialogue.reply("me llamo ana").contains("ana"),"remember name");check(dialogue.reply("como me llamo").contains("ana"),"recall name");dialogue.reply("me gusta dibujar");check(dialogue.reply("que me gusta").contains("dibujar"),"interest");check(dialogue.reply("estoy triste").contains("contame"),"dialogue");check(dialogue.reply("dale").contains("escucho"),"context");
  System.out.println("PASS escena compartida: crear, editar, relaciones, conservar, aclarar, persistir, acciones y conversacion");
 }
}
