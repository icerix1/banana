package com.bananalab.lite;
import java.io.*;import java.util.*;
public class CinemaCheck{
 public static void main(String[] args)throws Exception{
  PoseField model=new PoseField(new FileInputStream(args[0]));float max=0;
  try(DataInputStream in=new DataInputStream(new FileInputStream(args[1]))){int rows=in.readInt(),n=in.readInt();for(int r=0;r<rows;r++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] out=model.forward(input);for(int j=0;j<2;j++)max=Math.max(max,Math.abs(out[j]-in.readFloat()));}}
  if(max>.0002f)throw new AssertionError("pose parity "+max);
  int[] source=new int[4096];try(DataInputStream in=new DataInputStream(new FileInputStream(args[2]))){for(int i=0;i<source.length;i++)source[i]=in.readInt();}
  try(DataOutputStream out=new DataOutputStream(new FileOutputStream(args[3]))){for(int pose=0;pose<5;pose++){int[] image=model.deform(source,1,pose);int changed=0;for(int i=0;i<image.length;i++){out.writeInt(image[i]);if(image[i]!=source[i])changed++;}if(pose==0&&!Arrays.equals(source,image))throw new AssertionError("neutral identity");if(pose>0&&changed<100)throw new AssertionError("pose without change");}}
  ShotPlan a=ShotPlan.at(0,33,67,true),b=ShotPlan.at(.4f,33,67,true),c=ShotPlan.at(.9f,33,67,true);if(a.shot!=0||b.shot!=1||c.shot!=2||b.zoom<1.4||b.reveal!=0||c.reveal<.5)throw new AssertionError("takes");if(ShotPlan.at(.4f,33,67,false).reveal!=1)throw new AssertionError("continuous");
  if(VideoExporter.y(0,0,0)!=16||VideoExporter.y(255,255,255)!=235||VideoExporter.u(128,128,128)!=128||VideoExporter.v(128,128,128)!=128)throw new AssertionError("YUV conversion");
  System.out.println("PASS poses neuronales (paridad "+max+"), imagen neutra conservada, 3 tomas y conversion YUV");
 }
}
