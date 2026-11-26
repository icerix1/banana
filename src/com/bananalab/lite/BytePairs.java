package com.bananalab.lite;
import java.io.*;import java.util.*;import org.json.*;
/** UTF-8 BPE learned only from Banana's own training corpus. Mode "words" (reader) never merges across word boundaries. */
final class BytePairs {
 final int[][] merges;final byte[][] pieces;final boolean words;final int maxChunk;
 private final Map<Long,int[]> rank=new HashMap<>();
 private final Map<String,int[]> cache=new LinkedHashMap<String,int[]>(4096,.75f,true){protected boolean removeEldestEntry(Map.Entry<String,int[]> e){return size()>30000;}};
 BytePairs(InputStream source,List<String> vocab)throws Exception{this(text(source),vocab);}
 static String text(InputStream source)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;try(InputStream in=source){while((n=in.read(b))!=-1)out.write(b,0,n);}return new String(out.toByteArray(),"UTF-8");}
 BytePairs(String json,List<String> vocab)throws Exception{
  JSONObject root=new JSONObject(json);JSONArray rows=root.getJSONArray("merges");words=root.optString("mode").equals("words");maxChunk=Math.max(8,Math.min(256,root.optInt("max_chunk",48)));merges=new int[rows.length()][3];pieces=new byte[vocab.size()][];
  for(int i=0;i<rows.length();i++){JSONArray row=rows.getJSONArray(i);for(int j=0;j<3;j++)merges[i][j]=row.getInt(j);if(merges[i][2]!=262+i||merges[i][0]>=262+i||merges[i][1]>=262+i||merges[i][0]<6||merges[i][1]<6)throw new IOException("Tokenizer invalido");}
  if(words&&262+rows.length()!=vocab.size())throw new IOException("Tokenizer y vocabulario no coinciden");
  for(int i=6;i<vocab.size();i++){String s=vocab.get(i);if(s.length()%2!=0)throw new IOException("Token invalido");pieces[i]=new byte[s.length()/2];for(int j=0;j<pieces[i].length;j++)pieces[i][j]=(byte)Integer.parseInt(s.substring(j*2,j*2+2),16);}
  if(words)for(int i=0;i<merges.length;i++)rank.put(((long)merges[i][0]<<32)|merges[i][1],new int[]{i,merges[i][2]});
 }
 List<Integer> encode(String text,int max)throws UnsupportedEncodingException{
  if(words)return encodeWords(text,max);
  List<Integer> ids=new ArrayList<>();for(byte b:text.getBytes("UTF-8"))ids.add((b&255)+6);
  for(int[] m:merges){List<Integer> next=new ArrayList<>();for(int i=0;i<ids.size();i++){if(i+1<ids.size()&&ids.get(i)==m[0]&&ids.get(i+1)==m[1]){next.add(m[2]);i++;}else next.add(ids.get(i));}ids=next;}
  return new ArrayList<>(ids.subList(0,Math.min(max,ids.size())));
 }
 static boolean space(int cp){return cp<=0x20||cp==0xA0||cp>=0x2000&&cp<=0x200B||cp==0x2028||cp==0x2029||cp==0x202F||cp==0x3000||cp==0xFEFF;}
 static boolean word(int cp){return cp>='a'&&cp<='z'||cp>='0'&&cp<='9'||cp>=0xC0;}
 /** Same chunking as model-source/lector/lector_tok.py: a word or one sign, with one leading space when blanks preceded it. */
 List<Integer> encodeWords(String text,int max)throws UnsupportedEncodingException{List<Integer> out=new ArrayList<>();StringBuilder cur=new StringBuilder();boolean space=false,pending=false;
  for(int i=0;i<text.length()&&out.size()<max;){int cp=text.codePointAt(i);i+=Character.charCount(cp);if(cp>=0xD800&&cp<=0xDFFF)continue;
   if(space(cp)){if(cur.length()>0){chunk(out,pending?" "+cur:cur.toString());cur.setLength(0);pending=false;}space=true;}
   else if(word(cp)){if(cur.length()==0){pending=space;space=false;}cur.appendCodePoint(cp);}
   else{if(cur.length()>0){chunk(out,pending?" "+cur:cur.toString());cur.setLength(0);pending=false;}chunk(out,(space?" ":"")+new String(Character.toChars(cp)));space=false;}}
  if(cur.length()>0&&out.size()<max)chunk(out,pending?" "+cur:cur.toString());return out.size()>max?new ArrayList<>(out.subList(0,max)):out;
 }
 private void chunk(List<Integer> out,String piece)throws UnsupportedEncodingException{int[] ids;synchronized(cache){ids=cache.get(piece);}if(ids==null){byte[] data=piece.getBytes("UTF-8");if(data.length<=maxChunk)ids=merge(data,0,data.length);else{List<Integer> all=new ArrayList<>();for(int at=0;at<data.length;at+=maxChunk)for(int v:merge(data,at,Math.min(data.length,at+maxChunk)))all.add(v);ids=new int[all.size()];for(int k=0;k<ids.length;k++)ids[k]=all.get(k);}synchronized(cache){cache.put(piece,ids);}}for(int v:ids)out.add(v);}
 private int[] merge(byte[] data,int from,int to){int n=to-from;int[] ids=new int[n];for(int i=0;i<n;i++)ids[i]=(data[from+i]&255)+6;
  while(n>1){int best=Integer.MAX_VALUE,id=-1,a=0,b=0;for(int i=0;i+1<n;i++){int[] r=rank.get(((long)ids[i]<<32)|ids[i+1]);if(r!=null&&r[0]<best){best=r[0];id=r[1];a=ids[i];b=ids[i+1];}}if(id<0)break;int w=0;for(int i=0;i<n;i++){if(i+1<n&&ids[i]==a&&ids[i+1]==b){ids[w++]=id;i++;}else ids[w++]=ids[i];}n=w;}
  return Arrays.copyOf(ids,n);}
 String decode(List<Integer> ids)throws UnsupportedEncodingException{ByteArrayOutputStream out=new ByteArrayOutputStream();for(int id:ids)if(id>=6&&id<pieces.length)out.write(pieces[id],0,pieces[id].length);return new String(out.toByteArray(),"UTF-8");}
}
