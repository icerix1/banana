package com.bananalab.lite;
import java.io.*;
public class TransformerCheck{
 static BananaTransformer load(String root)throws Exception{return new BananaTransformer(new FileInputStream(root+"/banana-t1.bin"),new FileInputStream(root+"/vocab.txt"));}
 public static void main(String[] args)throws Exception{
  BananaTransformer model=load(args[0]);float max=0;
  try(DataInputStream in=new DataInputStream(new FileInputStream(args[1]))){int cases=in.readInt(),vocab=in.readInt();for(int i=0;i<cases;i++){int count=in.readInt();BananaTransformer.Session session=model.session();float[] actual=null;for(int j=0;j<count;j++)actual=session.step(in.readInt());for(int j=0;j<vocab;j++)max=Math.max(max,Math.abs(actual[j]-in.readFloat()));}}
  if(max>.0003f)throw new AssertionError("Paridad Transformer "+max);System.out.println("PASS PyTorch completo vs Java incremental: max "+max);
  for(String text:new String[]{"hola banana","hola","quien sos","que podes hacer","gracias","dame una frase con gato","como me llamo"}){BananaTransformer.Result result=model.generate(text,"","");System.out.println(text+" => "+result.text+" ["+result.count+"]");if(result.count<1||result.count>48)throw new AssertionError("No genero tokens");}
  BananaTransformer.Result greeting=model.generate("hola","","");if(!greeting.text.contains("hola"))throw new AssertionError("saludo");
  BananaTransformer.Result context=model.generate("como me llamo","me llamo ana","hola ana , un gusto conversar con vos .");System.out.println("CONTEXT => "+context.text);
  BananaTransformer.Result unknown=model.generate("xyzzzzz qqqqqq zzzzzzzz","","");if(unknown.count!=0)throw new AssertionError("Guardia vocabulario");
  Thread.currentThread().interrupt();try{model.generate("hola","","");throw new AssertionError("No cancelado");}catch(IOException expected){}finally{Thread.interrupted();}
  System.out.println("PASS generacion acotada, contexto experimental, OOV y cancelacion");
 }
}
