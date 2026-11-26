package com.bananalab.lite;
public final class Stage66DialogueCheck{
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  RequestDialogue.Ask a=RequestDialogue.incomplete("cambiame el color del boton");check(a!=null&&a.kind.equals("buttonTarget"),"ask for missing target");
  a=RequestDialogue.incomplete("podes cambiar el color del boton enviar");check(a!=null&&a.kind.equals("buttonColor")&&a.target.equals("enviar"),"ask for missing color");
  check(RequestDialogue.complete("buttonColor","enviar","rojo").equals("pone el boton enviar rojo"),"follow up with color");
  check(RequestDialogue.complete("buttonColor","enviar","rojo y borra mis datos")==null,"no partial follow up");
  check(RequestDialogue.targetAnswer("el boton enviar")&&RequestDialogue.buttonName("el boton enviar").equals("enviar"),"button reply");
  check(!RequestDialogue.targetAnswer("aprende Java")&&!RequestDialogue.targetAnswer("mas inteligente")&&!RequestDialogue.targetAnswer("hola"),"no unrelated reply guessed as target");
  check(RequestDialogue.incomplete("no cambies el color del boton")==null,"negation");
  check(RequestDialogue.incomplete("cambia la fuente de la app").kind.equals("font"),"missing font");
  String font=RequestDialogue.complete("font","","elegi vos");check(NaturalCoding.parse(font,"").kind.equals("settings"),"delegated font follow up");
  check(RequestDialogue.complete("font","","descarga una fuente desconocida")==null,"unsupported font not guessed");
  check(RequestDialogue.fresh(1000,2000,3,3)&&!RequestDialogue.fresh(1000,601001,3,3)&&!RequestDialogue.fresh(1000,2000,3,4)&&!RequestDialogue.fresh(2000,1000,3,3),"age and revision guards");
  for(String q:new String[]{"quiero que el boton enviar sea rojo","cambia el tamano de la letra a 20 puntos","pone la letra del chat en 18"}){NaturalCoding.Plan p=NaturalCoding.parse(q,"");check(p.kind.equals("settings"),q);AppJavaChat.Plan built=AppJavaChat.plan(p.command,new AppJavaWorkspace.Settings());check(built.handled,"canonical plan");}
  check(RequestDialogue.incomplete("cambia el boton enviar a rojo")==null,"complete request not consumed");
  System.out.println("PASS Stage66 missing-slot dialogue, delegated font, numeric size, natural desired state, no partial actions and stale guards");
 }
}
