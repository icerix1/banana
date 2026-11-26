package com.bananalab.lite;
import java.io.*;import java.util.*;import org.json.*;
/** UTF-8 BPE learned only from Banana's own training corpus. */
final class BytePairs {
 final int[][] merges;final byte[][] pieces;
 BytePairs(InputStream source,List<String> vocab)throws Exception{
  ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;try(InputStream in=source){while((n=in.read(b))!=-1)out.write(b,0,n);}
  JSONArray rows=new JSONObject(new String(out.toByteArray(),"UTF-8")).getJSONArray("merges");merges=new int[rows.length()][3];pieces=new byte[vocab.size()][];
  for(int i=0;i<rows.length();i++){JSONArray row=rows.getJSONArray(i);for(int j=0;j<3;j++)merges[i][j]=row.getInt(j);if(merges[i][2]!=262+i||merges[i][0]>=262+i||merges[i][1]>=262+i)throw new IOException("Tokenizer invalido");}
  for(int i=6;i<vocab.size();i++){String s=vocab.get(i);if(s.length()%2!=0)throw new IOException("Token invalido");pieces[i]=new byte[s.length()/2];for(int j=0;j<pieces[i].length;j++)pieces[i][j]=(byte)Integer.parseInt(s.substring(j*2,j*2+2),16);}
 }
 List<Integer> encode(String text,int max)throws UnsupportedEncodingException{
  List<Integer> ids=new ArrayList<>();for(byte b:text.getBytes("UTF-8"))ids.add((b&255)+6);
  for(int[] m:merges){List<Integer> next=new ArrayList<>();for(int i=0;i<ids.size();i++){if(i+1<ids.size()&&ids.get(i)==m[0]&&ids.get(i+1)==m[1]){next.add(m[2]);i++;}else next.add(ids.get(i));}ids=next;}
  return new ArrayList<>(ids.subList(0,Math.min(max,ids.size())));
 }
 String decode(List<Integer> ids)throws UnsupportedEncodingException{ByteArrayOutputStream out=new ByteArrayOutputStream();for(int id:ids)if(id>=6&&id<pieces.length)out.write(pieces[id],0,pieces[id].length);return new String(out.toByteArray(),"UTF-8");}
}
