package com.bananalab.lite;
import java.util.regex.*;
/** Convierte pedidos cotidianos de animacion a una orden del editor. */
final class CreationRequest{
 static String normalize(String input){
  String t=ChatLanguage.withoutGreeting(input);
  String[][] typos={{"animasion","animacion"},{"animacionn","animacion"},{"bananna","banana"},{"pepno","pepino"},{"derehca","derecha"},{"izqueirda","izquierda"}};
  for(String[] pair:typos)t=t.replaceAll("\\b"+pair[0]+"\\b",pair[1]);

  t=t.replaceFirst("^(?:por favor )?(?:podes |podrias |puedes |podes por favor )?(?:hacerme|hacer|crearme|crear|generarme|generar) (?:un |una )?(?:video|animacion|dibujo)(?: animado)?(?: de| con| donde)?\\s*","dibuja ");
  t=t.replaceFirst("^(?:quiero|necesito) (?:que )?(?:me )?(?:hagas|crees|generes) (?:un |una )?(?:video|animacion|dibujo)(?: de| con| donde)?\\s*","dibuja ");
  t=t.replaceFirst("^(?:quiero|necesito) (?:un |una )?(?:video|animacion|dibujo)(?: de| con| donde)?\\s*","dibuja ");
  t=t.replaceFirst("^(?:hace|haceme|me haces|me harias|crea|creame|genera|generame) (?:un |una )?(?:video|animacion|dibujo)(?: de| con| donde)?\\s*","dibuja ");
  if(!t.contains("aparezca al final")&&!t.contains("se miren"))t=t.replaceFirst("^(?:podes |podrias )?(?:hacer|hace|haceme) que\\s+","dibuja ");
  t=t.replaceFirst("^(?:anima|animame)\\s+","dibuja ");
  t=t.replaceAll("\\bbailando\\b","baila").replaceAll("\\bcaminando\\b","camina").replaceAll("\\bsaltando\\b","salta").replaceAll("\\bhablando\\b","habla").replaceAll("\\bcorriendo\\b","corre").replaceAll("\\bsaludando\\b","saluda");
  t=t.replaceAll("\\bbaile\\b","baila").replaceAll("\\bcamine\\b","camina").replaceAll("\\bsalte\\b","salta").replaceAll("\\bhable\\b","habla").replaceAll("\\bcorra\\b","corre").replaceAll("\\bsalude\\b","saluda");
  t=t.replaceFirst("^(?:haceme|hace|me haces|dibujame) ((?:un|una) (?:banana|pepino|gato|conejo|robot|perro|oso|zorro|persona|tomate|criatura)\\b.*)","dibuja $1");
  if(t.matches("^(?:un|una|el|la) (?:banana|pepino|gato|conejo|robot|perro|oso|zorro|persona|tomate|criatura)\\b.*")&&t.matches("(?s).*\\b(?:baila|camina|salta|va|habla|corre|saluda)\\b.*"))t="dibuja "+t;
  return t.replaceAll("\\bgata\\b","gato").replaceAll("\\bperra\\b","perro").replaceAll("\\bconeja\\b","conejo").replaceAll("\\bplatano\\b","banana").trim();
 }
 static boolean render(String t){return t.matches("^(?:dibuja|dibujame|crea|crear)\\b.*");}
}
