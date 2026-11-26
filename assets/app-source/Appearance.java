package com.bananalab.lite;
import java.util.regex.*;
final class Appearance {
 static String segment(String prompt,String name){
  int start=prompt.indexOf(name);if(start<0)return "";
  String tail=prompt.substring(start+name.length());
  Matcher end=Pattern.compile("[,;.]|\\b(?:y|mientras|luego|despues)\\s+(?:un|una|el|la)\\b").matcher(tail);
  return end.find()?tail.substring(0,end.start()):tail;
 }
 static boolean has(String text,String words){return Pattern.compile("\\b(?:"+words+")\\b").matcher(text).find();}
 static float width(String text){return has(text,"gordo|gorda|redondo|redonda|ancho|ancha")?1.25f:has(text,"delgado|delgada|fino|fina")?.78f:1f;}
 static float height(String text){return has(text,"alto|alta|alargado|alargada")?1.2f:has(text,"bajo|baja|corto|corta")?.85f:1f;}
 static int color(String text,int fallback){
  String[] names={"rojo|roja","azul","verde","amarillo|amarilla","negro|negra","blanco|blanca","rosa","violeta|morado|morada","naranja","gris","marron"};
  int[] colors={0xffd66363,0xff629cdb,0xff72b783,0xffeaca63,0xff42495a,0xffece5dd,0xffe8a3bf,0xffa78ac7,0xffdd9b56,0xff9298a4,0xffa47858};
  for(int i=0;i<names.length;i++)if(Pattern.compile("\\b(?:"+names[i]+")\\b").matcher(text).find())return colors[i];return fallback;
 }
}
