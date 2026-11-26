package com.bananalab.lite;
import java.io.*;
public class BackdropCheck{
 public static void main(String[] args)throws Exception{
  BackdropField net=new BackdropField(new FileInputStream(args[0]));float max=0;
  try(DataInputStream in=new DataInputStream(new FileInputStream(args[1]))){int rows=in.readInt(),n=in.readInt();for(int i=0;i<rows;i++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] actual=net.forward(input);for(int j=0;j<4;j++)max=Math.max(max,Math.abs(actual[j]-in.readFloat()));}}
  if(max>.0002f)throw new AssertionError("Parity "+max);
  float[] cond=new float[10];cond[0]=1;cond[5]=.8f;cond[9]=1;long start=System.nanoTime();int[] forest=net.render(cond);cond[0]=0;cond[4]=1;int[] inside=net.render(cond);int delta=0,mask=0;for(int i=0;i<forest.length;i++){if(forest[i]!=inside[i])delta++;if((forest[i]>>>24)>128)mask++;}
  if(delta<2000||mask<100||forest.length!=128*128)throw new AssertionError("No condition response");try(DataOutputStream out=new DataOutputStream(new FileOutputStream(args[2]))){for(int pixel:forest)out.writeInt(pixel|0xff000000);}
  if(Scene.ease(0)!=0||Scene.ease(1)!=1||Scene.ease(.25f)>=Scene.ease(.75f)||Scene.ease(.001f)>.00001f)throw new AssertionError("Easing");
  System.out.println("PASS fondo neuronal128, mascara, escenarios, suavizado y paridad Java/PyTorch max "+max+"; ms host "+(System.nanoTime()-start)/1000000);
 }
}
