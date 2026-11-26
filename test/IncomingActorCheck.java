package com.bananalab.lite;
public class IncomingActorCheck{
 public static void main(String[] args)throws Exception{
  Semilla m=SemillaCheck.load(args[0]);String[] types={"banana","pepino","gato","conejo","robot"};int cases=0;
  for(String first:types)for(String second:types)if(!first.equals(second)){
   for(String prefix:new String[]{"haceme una ","dibuja una ","una "}){
    String input=prefix+first+" que baila y va el "+second;
    CreatorSession.Result r=new CreatorSession().apply(m,input,null);if(r.commit==null)throw new AssertionError(input+" => "+r.text);
    SceneDraft d=r.commit;
    if(d.cast.size()!=2||d.get(first)==null||d.get(second)==null)throw new AssertionError("cast "+input+" "+d.describe());
    int incoming=d.cast.indexOf(d.get(second));boolean entrance=false;for(int i=0;i<d.actions.size();i++)if(d.actors.get(i)==incoming&&d.actions.get(i).equals("acerca"))entrance=true;
    if(!entrance)throw new AssertionError("wrong action owner "+d.describe());
    Scene scene=new Scene(d);scene.setSequence(true);boolean visible=false;for(int i=0;i<scene.frames();i++)if(scene.actorVisibility(second,i/(float)(scene.frames()-1))>.5)visible=true;
    if(!visible||scene.actorVisibility(second,1)<.99)throw new AssertionError("never visible "+input);
    scene.setSequence(false);if(scene.actorVisibility(second,.9f)<.99)throw new AssertionError("continuous");
    cases++;
   }
  }
  OwnPlan full=new OwnPlan(m,"una banana baila y va el pepino a hablarle");if(full.names.size()!=2||full.actors.get(1)!=1||full.actors.get(2)!=1)throw new AssertionError("full sentence");
  System.out.println("PASS "+cases+" pedidos: dos personajes, entrada asignada al segundo y visibilidad final en ambos modos");
 }
}
