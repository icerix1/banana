package com.bananalab.lite;
import java.io.*;
public class CapabilityCheck {
 public static void main(String[] args)throws Exception {
  BananaTransformer model=TransformerCheck.load(args[0]);
  String[][] cases={
   {"hola","","","hola"},
   {"como me llamo","me llamo ana","hola ana .","ana"},
   {"de que color es pepino","el color de pepino es azul","el pepino es azul .","azul"},
   {"de que color es gato","el color de gato es verde","el gato es verde .","verde"},
   {"3 mas 4","","","7"},
   {"8 mas 6","","","14"},
   {"escribi una frase donde gato salta en bosque","","","gato salta en el bosque"},
   {"dibuja un dragon","","","todavia"},
   {"que paso hoy en el mundo","","","fuentes"}
  };int passed=0;
  for(String[] row:cases){String text=model.generate(row[0],row[1],row[2]).text;boolean pass=text.contains(row[3]);if(pass)passed++;System.out.println((pass?"PASS":"FAIL")+" | "+row[0]+" | "+text);}
  System.out.println("CONTROLLED_SMOKE_SCORE "+passed+"/"+cases.length);
 }
}
