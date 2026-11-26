package com.bananalab.lite;
import java.util.*;
/** Tiempo y desplazamiento compartidos por preview y exportacion. */
final class Timeline{
 static String language(String input){
  return Knowledge.normalize(input).replace("al mismo tiempo que","mientras").replace("al mismo tiempo","mientras")
   .replaceAll("\\b(?:entra|entre|aparece|aparezca|llega|entrar|aparecer) (?:por|desde) (?:la )?izquierda\\b","entraizq")
   .replaceAll("\\b(?:entra|entre|aparece|aparezca|llega|entrar|aparecer) (?:por|desde) (?:la )?derecha\\b","entrader")
   .replaceAll("\\b(?:sale|salga|salir|se va) (?:por|hacia) la izquierda\\b","saleizq")
   .replaceAll("\\b(?:sale|salga|salir|se va) (?:por|hacia) la derecha\\b","saleder");
 }
 static String action(String word,String fallback){
  if(word.matches("besa|bese|besan|besen|besar|besando"))return "besa";
  if((word.equals("entraizq")||word.equals("entrader")||word.equals("saleizq")||word.equals("saleder")||word.equals("mira")||word.equals("abre")||word.equals("sorprende")||word.equals("sienta")||word.equals("sostiene")||word.equals("acercaactor")||word.equals("entrega")||word.equals("recibe")))return word;
  if((word.equals("viene")||word.equals("entra")||word.equals("llega")||word.equals("aparece")))return "acerca";
  if((word.equals("bailar")||word.equals("baile")||word.equals("bailando")))return "baila";
  if((word.equals("caminar")||word.equals("camine")||word.equals("caminando")))return "camina";
  if((word.equals("saludar")||word.equals("salude")||word.equals("saludando")))return "saluda";
  if((word.equals("hablar")||word.equals("hable")||word.equals("hablarle")||word.equals("hablando")))return "habla";
  if((word.equals("saltar")||word.equals("salte")||word.equals("saltando")))return "salta";
  return fallback;
 }
 static String label(String action){if(action.equals("besa"))return "da un beso";if(action.equals("recibebeso"))return "recibe un beso";if(action.equals("entrega"))return "entrega el objeto";if(action.equals("recibe"))return "recibe el objeto";if(action.equals("acercaactor"))return "se acerca al personaje";if(action.equals("abre"))return "abre la puerta";if(action.equals("sorprende"))return "se sorprende";if(action.equals("sienta"))return "se sienta";if(action.equals("sostiene"))return "sostiene el objeto";if(action.equals("entraizq"))return "entra por la izquierda";if(action.equals("entrader"))return "entra por la derecha";if(action.equals("saleizq"))return "sale por la izquierda";if(action.equals("saleder"))return "sale por la derecha";return action;}
 static boolean entry(String action){return (action.equals("acerca")||action.equals("entraizq")||action.equals("entrader"));}
 static boolean walk(String action){return (action.equals("camina")||action.equals("corre")||action.equals("acerca")||action.equals("acercaactor")||action.equals("entraizq")||action.equals("entrader")||action.equals("saleizq")||action.equals("saleder"));}
 static boolean valid(String action){return action.equals("besa")||action.equals("recibebeso")||(action.equals("camina")||action.equals("salta")||action.equals("baila")||action.equals("habla")||action.equals("saluda")||action.equals("acerca")||action.equals("gira")||action.equals("duerme")||action.equals("vuela")||action.equals("nada")||action.equals("aplaude")||action.equals("corre")||action.equals("canta")||action.equals("espera")||action.equals("entraizq")||action.equals("entrader")||action.equals("saleizq")||action.equals("saleder")||action.equals("mira")||action.equals("abre")||action.equals("sorprende")||action.equals("sienta")||action.equals("sostiene")||action.equals("acercaactor")||action.equals("entrega")||action.equals("recibe"));}
 static final class State{String action="espera";float progress=1,shift=0,alpha=1;}
 static State at(List<String> moves,float time,float x,float approach){
  State s=new State();float phase=Math.max(0,Math.min(1,time))*moves.size();int beat=Math.min(moves.size()-1,(int)phase);
  int first=0;while(first<moves.size()&&moves.get(first).equals("espera"))first++;
  if(first<moves.size()&&beat<first&&entry(moves.get(first))){s.alpha=0;return s;}
  for(int i=0;i<=beat;i++){
   String move=moves.get(i);float t=i==beat?(time>=1?1:phase-i):1,ease=Scene.ease(t);
   if(i==beat){s.action=move;s.progress=t;}
   if(move.equals("entraizq")){s.shift=(-22-x)*(1-ease);s.alpha=Math.min(1,t*6);}
   else if(move.equals("entrader")){s.shift=(122-x)*(1-ease);s.alpha=Math.min(1,t*6);}
   else if(move.equals("saleizq")){s.shift+=(-22-x)*ease;if(t>=1)s.alpha=0;}
   else if(move.equals("saleder")){s.shift+=(122-x)*ease;if(t>=1)s.alpha=0;}
   else if((move.equals("acerca")||move.equals("acercaactor"))){s.shift+=approach*ease;if(i==beat&&move.equals("acerca"))s.alpha=Math.min(1,t*5);}
   else if((move.equals("camina")||move.equals("corre")))s.shift+=Math.min(7,88-x-s.shift)*ease;
  }return s;
 }
}
