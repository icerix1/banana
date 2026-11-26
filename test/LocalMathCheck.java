package com.bananalab.lite;
public class LocalMathCheck{
 public static void main(String[] args){
  if(!LocalMath.reply("3 mas 4").equals("Calculadora local: 7."))throw new AssertionError("sum");
  if(!LocalMath.reply("calcula 12 por 12").equals("Calculadora local: 144."))throw new AssertionError("product");
  if(!LocalMath.reply("cuanto es 5 menos 9").equals("Calculadora local: -4."))throw new AssertionError("negative");
  if(!LocalMath.reply("2 dividido por 0").contains("cero"))throw new AssertionError("zero");
  if(!LocalMath.reply("3 + 4").equals("Calculadora local: 7."))throw new AssertionError("symbol");
  if(LocalMath.reply("hola 2 amigos")!=null)throw new AssertionError("unrelated");
  System.out.println("PASS calculadora local: suma, producto, negativos, cero y aislamiento");
 }
}
