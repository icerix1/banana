package com.bananalab.lite;
import java.math.*;import java.util.regex.*;
/** Calculadora acotada: la aritmetica se calcula, no se adivina con el modelo. */
final class LocalMath {
 static String reply(String message){
  String q=Knowledge.normalize(message).trim().replace('×','*').replace('÷','/');
  Matcher m=Pattern.compile("(?:(?:cuanto es|calcula|cuanto da) )?(-?[0-9]{1,9})\\s*(mas|menos|por|dividido por|[+*/-])\\s*(-?[0-9]{1,9})\\??").matcher(q);
  if(!m.matches())return null;
  BigDecimal a=new BigDecimal(m.group(1)),b=new BigDecimal(m.group(3)),r;String op=m.group(2);
  if(op.equals("+")||op.equals("mas"))r=a.add(b);
  else if(op.equals("-")||op.equals("menos"))r=a.subtract(b);
  else if(op.equals("*")||op.equals("por"))r=a.multiply(b);
  else {if(b.signum()==0)return "No se puede dividir por cero.";r=a.divide(b,6,RoundingMode.HALF_UP);}
  return "Calculadora local: "+r.stripTrailingZeros().toPlainString()+".";
 }
}
