package com.bananalab.lite;
import java.io.*;import java.util.*;
/** Decoder causal propio. Inferencia incremental con cache K/V en Java. */
final class BananaTransformer {
 BytePairs bytePairs;
 final int vocabSize,d,heads,layers,context,ff;final List<String> vocab=new ArrayList<>();final Map<String,Integer> ids=new HashMap<>();
 final float[][] embedding,position,head;final float[] normWeight,normBias,headBias;final Block[] blocks;
 static final class Block{float[] n1w,n1b,qkvb,ob,n2w,n2b,f1b,f2b;float[][] qkv,o,f1,f2;}
 static final class Result{final String text;final int count;final boolean limited;Result(String t,int n,boolean l){text=t;count=n;limited=l;}}
 BananaTransformer(InputStream weights,InputStream words)throws IOException{
  try(BufferedReader r=new BufferedReader(new InputStreamReader(words,"UTF-8"))){String s;while((s=r.readLine())!=null){ids.put(s,vocab.size());vocab.add(s);}}
  try(DataInputStream in=new DataInputStream(new BufferedInputStream(weights))){
   if(in.readInt()!=0x42544631)throw new IOException("Transformer incompatible");vocabSize=in.readInt();d=in.readInt();heads=in.readInt();layers=in.readInt();context=in.readInt();ff=in.readInt();
   if(vocabSize!=vocab.size()||vocabSize<6||vocabSize>2048||!((d==48&&heads==4&&layers==2&&context==96&&ff==192)||(d==64&&heads==4&&layers==2&&(context==128||context==192)&&ff==256)||((d==96&&ff==384||d==128&&ff==512)&&heads==4&&layers==3&&context==384)))throw new IOException("Dimensiones del Transformer invalidas");
   embedding=matrix(in,vocabSize,d);position=matrix(in,context,d);blocks=new Block[layers];
   for(int l=0;l<layers;l++){Block b=new Block();b.n1w=vector(in,d);b.n1b=vector(in,d);b.qkv=matrix(in,d,3*d);b.qkvb=vector(in,3*d);b.o=matrix(in,d,d);b.ob=vector(in,d);b.n2w=vector(in,d);b.n2b=vector(in,d);b.f1=matrix(in,d,ff);b.f1b=vector(in,ff);b.f2=matrix(in,ff,d);b.f2b=vector(in,d);blocks[l]=b;}
   normWeight=vector(in,d);normBias=vector(in,d);head=matrix(in,d,vocabSize);headBias=vector(in,vocabSize);
  }
 }
 BananaTransformer(InputStream weights,InputStream vocab,InputStream tokenizer)throws Exception{this(weights,vocab);bytePairs=new BytePairs(tokenizer,this.vocab);}
 private static float[] vector(DataInputStream in,int n)throws IOException{float[] a=new float[n];for(int i=0;i<n;i++){a[i]=in.readFloat();if(Float.isNaN(a[i])||Float.isInfinite(a[i]))throw new IOException("Pesos invalidos");}return a;}
 private static float[][] matrix(DataInputStream in,int n,int m)throws IOException{float[][] a=new float[n][];for(int i=0;i<n;i++)a[i]=vector(in,m);return a;}
 private static float[] linear(float[] x,float[][] w,float[] b){float[] y=b.clone();for(int i=0;i<x.length;i++)for(int j=0;j<y.length;j++)y[j]+=x[i]*w[i][j];return y;}
 private static float[] norm(float[] x,float[] weight,float[] bias){float mean=0,var=0;for(float z:x)mean+=z;mean/=x.length;for(float z:x)var+=(z-mean)*(z-mean);float scale=1f/(float)Math.sqrt(var/x.length+1e-5f);float[] y=new float[x.length];for(int i=0;i<x.length;i++)y[i]=(x[i]-mean)*scale*weight[i]+bias[i];return y;}
 final class Session{
  final float[][][] keys=new float[layers][context][d],values=new float[layers][context][d];int used=0;float[] features;
  float[] step(int token){
   if(token<0||token>=vocabSize||used>=context)throw new IllegalArgumentException("Contexto excedido");
   float[] x=embedding[token].clone();for(int i=0;i<d;i++)x[i]+=position[used][i];
   for(int l=0;l<layers;l++){
    Block b=blocks[l];float[] qkv=linear(norm(x,b.n1w,b.n1b),b.qkv,b.qkvb);System.arraycopy(qkv,d,keys[l][used],0,d);System.arraycopy(qkv,2*d,values[l][used],0,d);float[] att=new float[d];int width=d/heads;
    for(int h=0;h<heads;h++){
     float[] scores=new float[used+1];float max=-Float.MAX_VALUE,sum=0;
     for(int p=0;p<=used;p++){float score=0;for(int j=0;j<width;j++)score+=qkv[h*width+j]*keys[l][p][h*width+j];scores[p]=score/(float)Math.sqrt(width);max=Math.max(max,scores[p]);}
     for(int p=0;p<=used;p++){scores[p]=(float)Math.exp(scores[p]-max);sum+=scores[p];}
     for(int p=0;p<=used;p++)for(int j=0;j<width;j++)att[h*width+j]+=scores[p]/sum*values[l][p][h*width+j];
    }
    float[] residual=linear(att,b.o,b.ob);for(int j=0;j<d;j++)x[j]+=residual[j];float[] hidden=linear(norm(x,b.n2w,b.n2b),b.f1,b.f1b);for(int j=0;j<ff;j++)hidden[j]=Math.max(0,hidden[j]);residual=linear(hidden,b.f2,b.f2b);for(int j=0;j<d;j++)x[j]+=residual[j];
   }
   used++;features=norm(x,normWeight,normBias);return linear(features,head,headBias);
  }
 }
 void save(OutputStream stream)throws IOException{DataOutputStream out=new DataOutputStream(new BufferedOutputStream(stream));out.writeInt(0x42544631);for(int n:new int[]{vocabSize,d,heads,layers,context,ff})out.writeInt(n);write(out,embedding);write(out,position);for(Block b:blocks){write(out,b.n1w);write(out,b.n1b);write(out,b.qkv);write(out,b.qkvb);write(out,b.o);write(out,b.ob);write(out,b.n2w);write(out,b.n2b);write(out,b.f1);write(out,b.f1b);write(out,b.f2);write(out,b.f2b);}write(out,normWeight);write(out,normBias);write(out,head);write(out,headBias);out.flush();}
 static void write(DataOutputStream out,float[] a)throws IOException{for(float v:a){if(Float.isNaN(v)||Float.isInfinite(v))throw new IOException("Peso inválido.");out.writeFloat(v);}}
 static void write(DataOutputStream out,float[][] a)throws IOException{for(float[] row:a)write(out,row);}
 Session session(){return new Session();}
 List<Integer> encode(String text,int maximum){if(bytePairs!=null)try{return bytePairs.encode(text.toLowerCase(java.util.Locale.ROOT),maximum);}catch(java.io.UnsupportedEncodingException e){throw new IllegalArgumentException(e);}List<String> words=new ArrayList<>();java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("[a-z]+|[0-9]+|[.,!?;:]").matcher(Knowledge.normalize(text));while(matcher.find()&&words.size()<160)words.add(matcher.group());List<Integer> tokens=new ArrayList<>();for(int i=0;i<Math.min(words.size(),maximum);i++){Integer n=ids.get(words.get(i));tokens.add(n==null?1:n);}return tokens;}
 Result generate(String request,String previousUser,String previousReply)throws IOException{
  List<String[]> history=new ArrayList<>();if(!previousUser.isEmpty()&&!previousReply.isEmpty())history.add(new String[]{previousUser,previousReply});return generate(request,history);
 }
 Result generate(String request,List<String[]> history)throws IOException{
  List<Integer> user=encode(request,bytePairs!=null?96:32);if(user.isEmpty())return new Result("Escribi un mensaje para generar texto.",0,false);int unknown=0;for(int n:user)if(n==1)unknown++;
  if(unknown>user.size()/2)return new Result("No conozco suficientes palabras de ese pedido. Proba una frase mas sencilla o usa el chat con fuentes.",0,false);
  List<Integer> prompt=new ArrayList<>();prompt.add(2);
  List<List<Integer>> pairs=new ArrayList<>();int budget=context-(bytePairs!=null?116:52)-user.size()-3;
  for(int i=history.size()-1;i>=0&&pairs.size()<3;i--){String[] pair=history.get(i);List<Integer> tokens=new ArrayList<>();tokens.add(3);tokens.addAll(encode(pair[0],bytePairs!=null?24:16));tokens.add(4);tokens.addAll(encode(pair[1],bytePairs!=null?40:20));tokens.add(5);if(tokens.size()>budget)break;pairs.add(0,tokens);budget-=tokens.size();}
  for(List<Integer> pair:pairs)prompt.addAll(pair);
  prompt.add(3);prompt.addAll(user);prompt.add(4);Session session=session();float[] logits=null;long deadline=System.nanoTime()+(bytePairs!=null?60L:30L)*1000*1000*1000;
  for(int n:prompt){if(Thread.currentThread().isInterrupted())throw new IOException("Generacion cancelada");logits=session.step(n);}
  StringBuilder answer=new StringBuilder();List<Integer> answerIds=new ArrayList<>();int count=0;boolean stopped=false;
  while(count<(bytePairs!=null?112:48)&&session.used<context){
   if(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline)throw new IOException("Generacion interrumpida o tiempo agotado");
   int best=5;for(int i=6;i<vocabSize;i++)if(logits[i]>logits[best])best=i;
   if(best==5){stopped=true;break;}
   answerIds.add(best);String word=vocab.get(best);if(answer.length()>0&&!word.matches("[.,!?;:]"))answer.append(' ');answer.append(word);count++;logits=session.step(best);
  }
  String text=(bytePairs!=null?bytePairs.decode(answerIds):answer.toString()).trim();if(text.isEmpty())text="El modelo no produjo una respuesta. Proba otra frase.";
  return new Result(text,count,!stopped);
 }
}
