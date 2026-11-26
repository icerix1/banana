package com.bananalab.lite;
import java.io.*;
import java.util.*;
import java.util.regex.*;
/** Red propia: embeddings + MLP compartido, categorias y prediccion de palabras. */
final class Semilla {
 final int v,d,h,c,k;final float[][] e,w,t,g;final float[] b,tb,gb;final List<String> vocab,labels;
 final Map<String,Integer> index=new HashMap<>();
 static final class Token{final String word,label;final float confidence;Token(String w,String l,float p){word=w;label=l;confidence=p;}}
 Semilla(InputStream weights,InputStream words,InputStream tags)throws IOException{
  vocab=lines(words);labels=lines(tags);for(int i=0;i<vocab.size();i++)index.put(vocab.get(i),i);
  DataInputStream in=new DataInputStream(new BufferedInputStream(weights));
  try{
   if(in.readInt()!=0x42534d31)throw new IOException("Modelo propio invalido");v=in.readInt();d=in.readInt();h=in.readInt();c=in.readInt();k=in.readInt();
   if(v!=vocab.size()||c!=labels.size()||v<2||v>4096||d!=16||h!=48||k!=5||c>64)throw new IOException("Dimensiones incompatibles");
   e=matrix(in,v,d);w=matrix(in,k*d,h);b=vector(in,h);t=matrix(in,h,c);tb=vector(in,c);g=matrix(in,h,v);gb=vector(in,v);
  }finally{in.close();}
 }
 private static List<String> lines(InputStream stream)throws IOException{List<String> a=new ArrayList<>();try(BufferedReader r=new BufferedReader(new InputStreamReader(stream,"UTF-8"))){String s;while((s=r.readLine())!=null)a.add(s);}return a;}
 private static float[] vector(DataInputStream in,int n)throws IOException{float[] a=new float[n];for(int i=0;i<n;i++){a[i]=in.readFloat();if(Float.isNaN(a[i])||Float.isInfinite(a[i]))throw new IOException("Pesos invalidos");}return a;}
 private static float[][] matrix(DataInputStream in,int n,int m)throws IOException{float[][] a=new float[n][];for(int i=0;i<n;i++)a[i]=vector(in,m);return a;}
 static List<String> words(String text){List<String> a=new ArrayList<>();Matcher m=Pattern.compile("[a-z]+|[.,;!?]").matcher(Knowledge.normalize(text));while(m.find()&&a.size()<160)a.add(m.group());return a;}
 int id(String word){Integer n=index.get(word);return n==null?1:n;}
 float[] hidden(int[] ids){float[] a=b.clone();for(int j=0;j<k;j++)for(int x=0;x<d;x++){float value=e[ids[j]][x];for(int z=0;z<h;z++)a[z]+=value*w[j*d+x][z];}for(int z=0;z<h;z++)a[z]=(float)Math.tanh(a[z]);return a;}
 float[] logits(int[] ids){return project(hidden(ids),t,tb);}
 private float[] project(float[] hidden,float[][] weights,float[] bias){float[] a=bias.clone();for(int j=0;j<h;j++)for(int z=0;z<a.length;z++)a[z]+=hidden[j]*weights[j][z];return a;}
 private static float[] probabilities(float[] a){float max=-Float.MAX_VALUE,sum=0;for(float x:a)max=Math.max(max,x);for(int i=0;i<a.length;i++){a[i]=(float)Math.exp(a[i]-max);sum+=a[i];}for(int i=0;i<a.length;i++)a[i]/=sum;return a;}
 synchronized List<Token> analyze(String text){
  List<String> words=words(text);List<Token> result=new ArrayList<>();
  for(int i=0;i<words.size();i++){int[] window=new int[k];for(int j=0;j<k;j++){int pos=i+j-2;window[j]=pos<0||pos>=words.size()?0:id(words.get(pos));}
   float[] p=probabilities(logits(window));int best=0;for(int j=1;j<c;j++)if(p[j]>p[best])best=j;
   String label=words.get(i).matches("[.,;!?]")?"OTHER":p[best]<.55f?"OTHER":labels.get(best);
   result.add(new Token(words.get(i),label,p[best]));
  }return result;
 }
 /** Ajuste limitado y autosupervisado de la cabeza de siguiente palabra. */
 synchronized int observe(String text){List<String> words=words(text);int count=0;
  for(int i=0;i<Math.min(words.size()-1,96);i++){
   if(Thread.currentThread().isInterrupted())break;
   int target=id(words.get(i+1));if(target<2)continue;
   int[] window=new int[k];for(int j=0;j<k;j++){int pos=i+j-4;window[j]=pos<0?0:id(words.get(pos));}
   float[] hidden=hidden(window),p=probabilities(project(hidden,g,gb));
   for(int z=0;z<v;z++){float error=(p[z]-(z==target?1:0))*.002f;gb[z]-=error;for(int j=0;j<h;j++)g[j][z]-=error*hidden[j];}count++;
  }return count;
 }
 synchronized void saveAdaptation(OutputStream stream)throws IOException{DataOutputStream out=new DataOutputStream(new BufferedOutputStream(stream));out.writeInt(0x42534131);out.writeInt(v);out.writeInt(h);for(float[] row:g)for(float x:row)out.writeFloat(x);for(float x:gb)out.writeFloat(x);out.flush();}
 synchronized void restoreAdaptation(InputStream stream)throws IOException{try(DataInputStream in=new DataInputStream(new BufferedInputStream(stream))){if(in.readInt()!=0x42534131||in.readInt()!=v||in.readInt()!=h)throw new IOException("Adaptacion incompatible");float[][] next=matrix(in,h,v);float[] bias=vector(in,v);for(int j=0;j<h;j++)System.arraycopy(next[j],0,g[j],0,v);System.arraycopy(bias,0,gb,0,v);}}
}
