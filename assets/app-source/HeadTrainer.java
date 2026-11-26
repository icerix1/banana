package com.bananalab.lite;
import java.util.*;
/** Actual gradient descent on the existing output projection. Frozen feature extractor. */
final class HeadTrainer{
 static final class Token{final float[] x;final int label;Token(float[]x,int label){this.x=x.clone();this.label=label;}}
 static final class Pixel{final float[] x,y;Pixel(float[]x,float[]y){this.x=x.clone();this.y=y.clone();}}
 static float[] logits(float[] x,float[][] w,float[] b){float[] y=b.clone();for(int i=0;i<x.length;i++)for(int j=0;j<b.length;j++)y[j]+=x[i]*w[i][j];return y;}
 static float[] probabilities(float[] x){float max=-Float.MAX_VALUE;for(float n:x)max=Math.max(max,n);float total=0;float[] p=new float[x.length];for(int j=0;j<x.length;j++){p[j]=(float)Math.exp(x[j]-max);total+=p[j];}for(int j=0;j<p.length;j++)p[j]/=total;return p;}
 static double nll(List<Token> rows,float[][] w,float[] b){double loss=0;for(Token row:rows){float[] p=probabilities(logits(row.x,w,b));loss-=Math.log(Math.max(1e-12,p[row.label]));}return loss/Math.max(1,rows.size());}
 static float[][] distributions(List<Token> rows,float[][] w,float[] b){float[][] p=new float[rows.size()][];for(int i=0;i<p.length;i++)p[i]=probabilities(logits(rows.get(i).x,w,b));return p;}
 static double divergence(List<Token> rows,float[][] expected,float[][] w,float[] b){double kl=0;for(int i=0;i<rows.size();i++){float[] got=probabilities(logits(rows.get(i).x,w,b));for(int j=0;j<got.length;j++)if(expected[i][j]>0)kl+=expected[i][j]*Math.log(expected[i][j]/Math.max(1e-12,got[j]));}return Math.max(0,kl/Math.max(1,rows.size()));}
 static void tokenStep(Token row,float[] target,float[][] w,float[] b,float lr){float[] p=probabilities(logits(row.x,w,b));for(int j=0;j<b.length;j++){float gradient=p[j]-(target==null?(j==row.label?1:0):target[j]);b[j]-=lr*gradient;for(int i=0;i<w.length;i++)w[i][j]-=lr*row.x[i]*gradient;}}
 static void chat(List<Token> train,List<Token> replay,float[][] targets,float[][] w,float[] b){float[][] start=copy(w);float[] startBias=b.clone();for(int epoch=0;epoch<8;epoch++){for(int i=0;i<train.size();i++){if(Thread.currentThread().isInterrupted())throw new IllegalStateException("Entrenamiento cancelado.");tokenStep(train.get(i),null,w,b,.001f);if(!replay.isEmpty()){int at=i%replay.size();tokenStep(replay.get(at),targets[at],w,b,.001f);}}}project(w,b,start,startBias,.01f);}
 static void project(float[][] w,float[] b,float[][] original,float[] originalBias,float radius){double norm=0;for(int i=0;i<w.length;i++)for(int j=0;j<b.length;j++){double d=w[i][j]-original[i][j];norm+=d*d;}for(int j=0;j<b.length;j++){double d=b[j]-originalBias[j];norm+=d*d;}norm=Math.sqrt(norm);if(norm>radius){float scale=(float)(radius/norm);for(int i=0;i<w.length;i++)for(int j=0;j<b.length;j++)w[i][j]=original[i][j]+scale*(w[i][j]-original[i][j]);for(int j=0;j<b.length;j++)b[j]=originalBias[j]+scale*(b[j]-originalBias[j]);}}
 static float sigmoid(float z){return (float)(1/(1+Math.exp(-z)));}
 static double mse(List<Pixel> rows,float[][] w,float[] b){double loss=0;for(Pixel row:rows){float[] a=logits(row.x,w,b);for(int j=0;j<4;j++){double e=sigmoid(a[j])-row.y[j];loss+=e*e*(j==3?1:row.y[3]);}}return loss/Math.max(1,rows.size()*4);}
 static void pixelStep(Pixel row,float[][] w,float[] b,float lr){float[] z=logits(row.x,w,b);for(int j=0;j<4;j++){float p=sigmoid(z[j]),g=(p-row.y[j])*p*(1-p)*.5f*(j==3?1:row.y[3]);b[j]-=lr*g;for(int i=0;i<w.length;i++)w[i][j]-=lr*row.x[i]*g;}}
 static void pixel(List<Pixel> train,List<Pixel> replay,float[][] w,float[] b){for(int epoch=0;epoch<12;epoch++)for(int i=0;i<train.size();i++){if(Thread.currentThread().isInterrupted())throw new IllegalStateException("Entrenamiento cancelado.");pixelStep(train.get(i),w,b,.015f);if(!replay.isEmpty())pixelStep(replay.get(i%replay.size()),w,b,.015f);}}
 static float[][] copy(float[][] w){float[][] copy=new float[w.length][];for(int i=0;i<w.length;i++)copy[i]=w[i].clone();return copy;}
 static double distance(float[][] a,float[][] b){double sum=0;for(int i=0;i<a.length;i++)for(int j=0;j<a[i].length;j++){double d=a[i][j]-b[i][j];sum+=d*d;}return Math.sqrt(sum);}
}
