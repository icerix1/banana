package com.bananalab.lite;
import java.io.*;
public class ModelParityCheck{
 public static void main(String[] args)throws Exception{
  Semilla net=SemillaCheck.load(args[0]);float max=0;
  try(DataInputStream in=new DataInputStream(new FileInputStream(args[1]))){int count=in.readInt(),classes=in.readInt();for(int i=0;i<count;i++){int[] ids=new int[5];for(int j=0;j<5;j++)ids[j]=in.readInt();float[] actual=net.logits(ids);for(int j=0;j<classes;j++)max=Math.max(max,Math.abs(actual[j]-in.readFloat()));}}
  if(max>.0002f)throw new AssertionError("Parity error "+max);System.out.println("PASS paridad Java/NumPy: error maximo "+max);
  String html="<html><script>instruccion oculta</script><style>algo</style><p>El ajolote &amp; su h&#225;bitat.</p></html>";
  String clean=PageReader.clean(html);if(clean.contains("oculta")||clean.contains("algo")||!clean.contains("hábitat"))throw new AssertionError(clean);
  for(String address:new String[]{"http://example.com","https://localhost"}){try{PageReader.address(address);throw new AssertionError("URL aceptada");}catch(IOException expected){}}
  System.out.println("PASS extraccion HTML y rechazo de URL no publica/HTTP");
 }
}
