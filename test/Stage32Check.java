package com.bananalab.lite;
import java.io.*;import java.util.*;import org.json.*;
public class Stage32Check{
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  String root=args[0];Semilla model=SemillaCheck.load(root+"/assets/language");CreatorSession creator=new CreatorSession();
  CreatorSession.Result design=creator.apply(model,"crea a Tony una banana detective con sobretodo",null);check(design.commit!=null,"named design "+design.text);
  SceneDraft d=design.commit;check(d.cast.get(0).label.equals("tony")&&d.cast.get(0).kind.equals("banana")&&NeuralSprites.accessory(d.cast.get(0).look)==4,"identity and clothing");
  String look=d.cast.get(0).look;CreatorSession.Result plan=creator.apply(model,"ahora hacelo caminar hasta una puerta abrirla y sorprenderse al encontrar al pepino conserva su cara y su ropa",d);
  check(plan.commit!=null,"story plan "+plan.text);d=plan.commit;check(d.cast.size()==2&&d.props.size()==1,"actors and door");check(d.get("banana").look.equals(look)&&d.get("banana").label.equals("tony"),"unchanged design");
  check(d.actions.equals(Arrays.asList("acerca","abre","entrader","sorprende")),"action plan "+d.actions);
  check(d.slot(1)==d.slot(2)&&d.target(1).equals("puerta"),"target and timing");
  Scene scene=new Scene(d);scene.setSequence(false);check(scene.actorX("banana",.32f)>50,"walk reaches door");check(SceneObjects.open(d,"puerta",0,3)==0&&SceneObjects.open(d,"puerta",1,3)==1,"door state");
  SceneDraft copied=d.copy();check(copied.props.size()==1&&copied.cast.get(0).kind.equals("banana"),"JSON new fields");
  CreatorSession.Result second=creator.apply(model,"agrega otra banana llamada Tina con lentes",copied);check(second.commit!=null&&second.commit.cast.size()==3,"duplicate type with identity "+second.text);check(second.commit.reference("tina")!=null&&!second.commit.reference("tina").name.equals("banana"),"distinct id");
  CreatorSession.Result undress=creator.apply(model,"saca el sobretodo de Tony",second.commit);check(undress.commit!=null&&undress.commit.cast.size()==3&&NeuralSprites.accessory(undress.commit.get("banana").look)==1,"localized clothing removal");
  CreatorPersistenceCheck.Fake fake=new CreatorPersistenceCheck.Fake();CreatorMemory.save(fake,new CreatorSession(),copied);SceneLibrary.Action reused=CharacterArchive.apply(fake,"usa a tony",new SceneDraft());check(reused.scene!=null&&reused.scene.cast.get(0).look.equals(look),"persistent profile");
  for(String kind:new String[]{"banana","pepino","gato","conejo","robot","perro","oso","zorro","persona"}){SceneDraft one=SceneDraft.fromPlan(new OwnPlan(model,"un "+kind+" camina"));check(one.cast.size()==1&&PixelGenerator.type(one.cast.get(0).kind)>=0,"type "+kind);}
  PixelGenerator visual=new PixelGenerator(new FileInputStream(root+"/assets/pixel-v2/character.bin"));check(visual.side==128&&visual.inputs==45,"new own visual architecture");
  float max=0;try(DataInputStream in=new DataInputStream(new FileInputStream(root+"/model-source/pixel-v2-reference.bin"))){int rows=in.readInt(),n=in.readInt();for(int i=0;i<rows;i++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] actual=visual.forward(input);for(int j=0;j<4;j++)max=Math.max(max,Math.abs(actual[j]-in.readFloat()));}}check(max<.0003,"pixel parity "+max);
  BananaTransformer chat=new BananaTransformer(new FileInputStream(root+"/assets/chat32/chat32.bin"),new FileInputStream(root+"/assets/chat32/vocab.txt"),new FileInputStream(root+"/assets/chat32/tokenizer.json"));check(chat.d==96&&chat.layers==3&&chat.context==384,"own chat architecture");
  for(String text:new String[]{"Raquel inventó a Ñandú 🌈","Tony_42: hola!","nombre nunca visto zyxabc","日本語"})check(chat.bytePairs.decode(chat.bytePairs.encode(text,2048)).equals(text),"UTF8 byte roundtrip "+text);
  float chatMax=0;try(DataInputStream in=new DataInputStream(new FileInputStream(root+"/model-source/chat32-reference.bin"))){int cases=in.readInt(),vocab=in.readInt();for(int i=0;i<cases;i++){int count=in.readInt();BananaTransformer.Session session=chat.session();float[] actual=null;for(int j=0;j<count;j++)actual=session.step(in.readInt());for(int j=0;j<vocab;j++)chatMax=Math.max(chatMax,Math.abs(actual[j]-in.readFloat()));}}check(chatMax<.0004,"chat parity "+chatMax);
  OwnVision vision=new OwnVision(new FileInputStream(root+"/assets/pixel-v2/vision.bin"));float visionMax=0;try(DataInputStream in=new DataInputStream(new FileInputStream(root+"/model-source/vision32-reference.bin"))){int rows=in.readInt(),n=in.readInt();for(int i=0;i<rows;i++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] actual=vision.forward(input);for(int j=0;j<9;j++)visionMax=Math.max(visionMax,Math.abs(actual[j]-in.readFloat()));}}check(visionMax<.0004,"vision parity "+visionMax);System.out.println("PASS own vision Java/PyTorch parity "+visionMax);
  System.out.println("PASS Tony, coat/face identity, door, reveal/reaction, duplicate types, profile reuse, 9 visual types, Pixel128 "+max+", own Chat32 "+chatMax+" and UTF8 BPE");
  for(String q:new String[]{"hola","sos independiente","que significa mientras","quiero una idea de detectives"})System.out.println("CHAT "+q+" => "+chat.generate(q,"","").text);
 }
}
