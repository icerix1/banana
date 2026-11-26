package com.bananalab.lite;
import java.util.*;
/** These whole scenarios were held out by the split rule before training. */
public class T3HeldoutCheck{
 public static void main(String[] args)throws Exception{
  BananaTransformer m=TransformerCheck.load(args[0]);String[] actors={"banana","pepino","gato","conejo","robot"},names={"tony","luna","milo","nina","leo","ana","kiari","tiziano"},colors={"rojo","azul","verde","amarillo"},places={"parque","bosque","playa","ciudad"};
  int passed=0,total=0;
  for(int a=0;a<5;a++)for(int n=0;n<8;n++)for(int c=0;c<4;c++)for(int p=0;p<4;p++)if((a+n*3+c*5+p*7)%11==0){
   List<String[]> hist=new ArrayList<>();hist.add(new String[]{"quiero una escena con "+actors[a],"podemos preparar la escena ."});hist.add(new String[]{"que se llame "+names[n],"el personaje se llama "+names[n]+" ."});hist.add(new String[]{"hace a "+names[n]+" "+colors[c]+" en "+places[p],"preparamos a "+names[n]+" "+colors[c]+" en "+places[p]+" ."});
   String[][] queries={{"como se llama el personaje",names[n]},{"de que color es "+names[n],colors[c]},{"donde esta "+names[n],places[p]}};
   for(String[] q:queries){String answer=m.generate(q[0],hist).text;boolean ok=answer.contains(q[1]);total++;if(ok)passed++;else System.out.println("FAIL "+q[0]+" expected="+q[1]+" actual="+answer);}
  }
  System.out.println("HELDOUT_CONTEXT "+passed+"/"+total);
  // Report performance, without hiding failures behind a success threshold.
 }
}
