package com.bananalab.lite;
import java.util.*;import java.io.*;import org.json.*;
public class Stage31Check{
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  String root=args[0];Semilla model=SemillaCheck.load(root+"/assets/language");
  SceneDraft parallel=SceneDraft.fromPlan(new OwnPlan(model,"una banana baila mientras el pepino camina"));
  check(parallel.cast.size()==2&&parallel.actions.size()==2&&parallel.slot(0)==0&&parallel.slot(1)==0,"simultaneous slots");
  SceneDraft seq=SceneDraft.fromPlan(new OwnPlan(model,"una banana baila y despues el pepino camina"));
  check(seq.slot(0)==0&&seq.slot(1)==1,"sequential slots");
  SceneDraft entry=SceneDraft.fromPlan(new OwnPlan(model,"una banana baila mientras el pepino entra por la derecha y despues el pepino sale por la izquierda"));
  check(entry.actions.equals(Arrays.asList("baila","entrader","saleizq"))&&entry.slots.equals(Arrays.asList(0,0,1)),"directions "+entry.describe());
  Scene movie=new Scene(entry);movie.setSequence(false);
  check(movie.actorX("pepino",.01f)>100&&movie.actorX("pepino",.49f)<90&&movie.actorVisibility("pepino",1)==0,"entry/exit movement");
  CreatorSession c=new CreatorSession();CreatorSession.Result changed=c.apply(model,"cambia el paso 2 a saludar",parallel);check(changed.commit!=null&&changed.commit.actions.get(1).equals("saluda")&&changed.commit.slot(1)==0&&parallel.actions.get(1).equals("camina"),"correct one action");
  changed=c.apply(model,"no el pepino tenia que saludar",parallel);check(changed.commit!=null&&changed.commit.actions.get(1).equals("saluda")&&changed.commit.slot(1)==0,"natural correction");
  changed=c.apply(model,"segui a la banana",parallel);check(changed.commit!=null&&changed.commit.camera.equals("follow:banana"),"follow camera");
  changed=c.apply(model,"mostra a los dos",entry);check(changed.commit!=null&&changed.commit.camera.equals("wide"),"wide camera");
  changed=c.apply(model,"hace que se miren",parallel);check(changed.commit!=null&&changed.commit.actions.size()==4&&changed.commit.slot(2)==changed.commit.slot(3),"interaction");
  JSONObject old=seq.json();for(int i=0;i<old.getJSONArray("steps").length();i++)old.getJSONArray("steps").getJSONObject(i).remove("slot");old.remove("camera");old.remove("sound");
  SceneDraft migrated=SceneDraft.read(old.toString());check(migrated.slot(1)==1&&!migrated.sound&&migrated.camera.equals("auto"),"legacy migration");
  SceneDraft clone=parallel.copy();clone.actors.set(1,0);try{SceneDraft.read(clone.json().toString());throw new AssertionError("overlap accepted");}catch(JSONException expected){}
  PixelGenerator clear=new PixelGenerator(new FileInputStream(root+"/assets/pixels2/pixel2.bin"));check(clear.side==96&&clear.hidden==160,"clear model");
  float max=0;try(DataInputStream in=new DataInputStream(new FileInputStream(root+"/model-source/pixel2-reference.bin"))){int rows=in.readInt(),n=in.readInt();for(int i=0;i<rows;i++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] actual=clear.forward(input);for(int j=0;j<4;j++)max=Math.max(max,Math.abs(actual[j]-in.readFloat()));}}
  check(max<.0003,"pixel2 parity "+max);
  int[] pixels=clear.render(PixelGenerator.condition(1,0xff629cdb,1,0,0));check(pixels.length==96*96,"native pixels");
  PoseField pose=new PoseField(new FileInputStream(root+"/assets/pose/pose1.bin"));check(Arrays.equals(pixels,pose.deform(pixels,1,0)),"neutral clear pose");check(!Arrays.equals(pixels,pose.deform(pixels,1,1)),"walk clear pose");
  short[] sound=Soundtrack.pcm(parallel,48);check(sound.length==48000,"audio duration");long energy=0;for(short s:sound)energy+=(long)s*s;check(energy>0&&Arrays.equals(sound,Soundtrack.pcm(parallel,48)),"deterministic audio");
  CreatorPersistenceCheck.Fake fake=new CreatorPersistenceCheck.Fake();parallel.get("banana").label="tony";SceneLibrary.save(fake,"inicio",parallel);SceneLibrary.save(fake,"segunda",entry);
  SceneLibrary.Action loaded=SceneLibrary.apply(fake,"abri escena inicio",null);check(loaded.scene!=null&&loaded.scene.get("banana").label.equals("tony")&&loaded.scene.slot(1)==0,"story identity/timing");check(SceneLibrary.list(fake).length()==2,"two scenes");
  System.out.println("PASS simultaneidad, orden, entrada/salida, correccion, camara, interaccion, migracion, Pixel2 "+max+", poses96, sonido y serie");
 }
}
