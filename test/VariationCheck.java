package com.bananalab.lite;
import java.util.*;
public class VariationCheck {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  String prompt="un gato rojo alto con sombrero camina y un perro azul bajo con bufanda salta";
  String cat=Appearance.segment(prompt,"gato"),dog=Appearance.segment(prompt,"perro");
  check(Appearance.has(cat,"sombrero"),"cat hat");check(!Appearance.has(cat,"bufanda"),"no leaked scarf");
  check(Appearance.has(dog,"bufanda"),"dog scarf");check(!Appearance.has(dog,"sombrero"),"no leaked hat");
  check(Appearance.height(cat)>1&&Appearance.height(dog)<1,"separate heights");
  check(Appearance.color(cat,0)==0xffd66363&&Appearance.color(dog,0)==0xff629cdb,"separate colors");
  check(!Appearance.has("sincronizado","corona"),"word boundaries");
  List<String> names=Understanding.entities("un gato con sombrero camina y un perro salta");
  check(names.equals(Arrays.asList("gato","perro")),"accessory is not actor: "+names);
  check(Appearance.width("delgada")<1&&Appearance.width("gordo")>1,"width variants");
  new Scene(prompt);
  System.out.println("PASS variaciones: atributos separados, proporciones, colores, accesorios y limites de palabra");
 }
}
