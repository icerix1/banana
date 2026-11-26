package com.bananalab.lite;
import java.util.*;import java.lang.reflect.*;
public class UnderstandingCheck{
 static void check(boolean x,String msg){if(!x)throw new AssertionError(msg);}
 public static void main(String[] args)throws Exception{
  String prompt="Un gato rojo camina y un perro azul salta en la playa";
  List<String> entities=Understanding.entities(prompt);
  check(entities.equals(Arrays.asList("gato","perro")),"entities: "+entities);
  Scene scene=new Scene(prompt);Field field=Scene.class.getDeclaredField("cast");field.setAccessible(true);
  Map cast=(Map)field.get(scene);check(cast.size()==2,"cast: "+cast);
  check(cast.get("gato").toString().contains("camina"),"cat action");
  check(cast.get("perro").toString().contains("salta"),"dog action");
  check(Appearance.color(Appearance.segment(Knowledge.normalize(prompt),"gato"),0)==0xffd66363,"cat color");
  check(Appearance.color(Appearance.segment(Knowledge.normalize(prompt),"perro"),0)==0xff629cdb,"dog color");
  check(Knowledge.classify("elefante","").equals("animal"),"elephant offline");
  check(Knowledge.classify("colibri","A bird native to America").equals("bird"),"English bird");
  check(Knowledge.classify("salmon","A fish").equals("fish"),"fish");
  check(Understanding.entities("Una mariposa vuela en el bosque").equals(Arrays.asList("mariposa")),"butterfly");
  System.out.println("PASS: entidades, acciones por personaje, colores independientes, perfiles ES/EN y fondos sin actores fantasma");
 }
}
