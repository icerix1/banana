package com.bananalab.lite;
import java.io.*;import java.util.*;import java.util.regex.*;
/** Modulo propio de intenciones, entrenado desde inicializacion aleatoria. */
final class ChatMind {
 final int f,h,c;final float[][] w,u;final float[] b,bias;final List<String> labels=new ArrayList<>();
 static final class Intent{final String name;final float confidence;Intent(String n,float p){name=n;confidence=p;}}
 ChatMind(InputStream weights,InputStream names)throws IOException{
  try(BufferedReader r=new BufferedReader(new InputStreamReader(names,"UTF-8"))){String s;while((s=r.readLine())!=null)labels.add(s);}
  try(DataInputStream in=new DataInputStream(new BufferedInputStream(weights))){if(in.readInt()!=0x42434831)throw new IOException("Modulo de chat invalido");f=in.readInt();h=in.readInt();c=in.readInt();if(f!=384||h!=32||c!=labels.size()||c>32)throw new IOException("Dimensiones de chat invalidas");w=matrix(in,f,h);b=vector(in,h);u=matrix(in,h,c);bias=vector(in,c);}
 }
 private static float[] vector(DataInputStream in,int n)throws IOException{float[] a=new float[n];for(int i=0;i<n;i++){a[i]=in.readFloat();if(Float.isNaN(a[i])||Float.isInfinite(a[i]))throw new IOException("Peso invalido");}return a;}
 private static float[][] matrix(DataInputStream in,int n,int m)throws IOException{float[][] a=new float[n][];for(int i=0;i<n;i++)a[i]=vector(in,m);return a;}
 private int hash(String word){int hash=0x811c9dc5;for(int i=0;i<word.length();i++)hash=(hash^word.charAt(i))*16777619;return (int)((hash&0xffffffffL)%f);}
 float[] features(String text){float[] x=new float[f];List<String> words=new ArrayList<>();Matcher m=Pattern.compile("[a-z]+").matcher(Knowledge.normalize(text));while(m.find())words.add(m.group());for(String word:words){x[hash("w:"+word)]+=2;for(int i=0;i<word.length()-2;i++)x[hash(word.substring(i,i+3))]+=.5f;}for(int i=1;i<words.size();i++)x[hash(words.get(i-1)+"_"+words.get(i))]+=1;float length=0;for(float v:x)length+=v*v;length=Math.max(1,(float)Math.sqrt(length));for(int i=0;i<f;i++)x[i]/=length;return x;}
 Intent classify(String text){float[] x=features(text),hidden=b.clone(),p=bias.clone();for(int j=0;j<h;j++){for(int i=0;i<f;i++)hidden[j]+=x[i]*w[i][j];hidden[j]=(float)Math.tanh(hidden[j]);}for(int j=0;j<c;j++)for(int i=0;i<h;i++)p[j]+=hidden[i]*u[i][j];int best=0;for(int i=1;i<c;i++)if(p[i]>p[best])best=i;float sum=0;for(float v:p)sum+=(float)Math.exp(v-p[best]);return new Intent(labels.get(best),1/sum);}
}
