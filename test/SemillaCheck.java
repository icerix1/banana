package com.bananalab.lite;
import java.io.*;import java.util.*;
public final class SemillaCheck {
 static Semilla load(String root)throws Exception{return new Semilla(new FileInputStream(root+"/semilla.bin"),new FileInputStream(root+"/vocab.txt"),new FileInputStream(root+"/labels.txt"));}
 public static void main(String[] args)throws Exception{
  Semilla net=load(args[0]);String[] inputs={"La banana baila y va el pepino a hablarle","Un gato rojo alto con sombrero camina y un perro azul salta en la playa","Una mariposa vuela en el bosque","Un elefante pequeno camina y despues saluda","Quiero que un dragon verde vuele por la ciudad"};
  for(String text:inputs){System.out.println(text);for(Semilla.Token token:net.analyze(text))System.out.print(token.word+":"+token.label+" ");System.out.println();OwnPlan plan=new OwnPlan(net,text);System.out.println(plan.names+" "+plan.looks+" "+plan.actors+" "+plan.actions+" "+plan.setting);}
  OwnPlan cats=new OwnPlan(net,inputs[1]);if(!cats.names.equals(Arrays.asList("gato","perro"))||!cats.actions.equals(Arrays.asList("camina","salta"))||!cats.setting.contains("playa"))throw new AssertionError("Escena de gato/perro");
  OwnPlan bananas=new OwnPlan(net,inputs[0]);if(!bananas.names.equals(Arrays.asList("banana","pepino"))||!bananas.actions.equals(Arrays.asList("baila","acerca","habla"))||!bananas.actors.equals(Arrays.asList(0,1,1)))throw new AssertionError("Escena banana/pepino");
  List<Semilla.Token> before=net.analyze(inputs[1]);int count=net.observe("El gato es un mamifero. Un gato camina en el jardin. El perro es un animal.");ByteArrayOutputStream bytes=new ByteArrayOutputStream();net.saveAdaptation(bytes);Semilla copy=load(args[0]);copy.restoreAdaptation(new ByteArrayInputStream(bytes.toByteArray()));List<Semilla.Token> after=copy.analyze(inputs[1]);for(int i=0;i<before.size();i++)if(!before.get(i).label.equals(after.get(i).label))throw new AssertionError("Adaptacion altero categorias");
  if(count==0)throw new AssertionError("Sin ajuste local");
  System.out.println("PASS: modelo propio, plan, ajuste local y persistencia sin alterar categorias");
 }
}
