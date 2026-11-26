package com.bananalab.lite;
import java.io.*;import java.nio.charset.*;import java.util.*;
/** Bounded, disk-backed HTML/text extraction. No page-sized String or DOM. */
final class PageStream{
 static final long WIRE_LIMIT=67108864L,TIME_MS=240000L;
 static final Set<String> HIDDEN=new HashSet<>(Arrays.asList("head","script","style","noscript","svg","nav","footer","header","aside","form","template"));
 static final Set<String> BLOCK=new HashSet<>(Arrays.asList("p","div","li","br","h1","h2","h3","h4","h5","h6","tr","section","main","article","blockquote"));
 static final class Budget{long deadline=System.nanoTime()+TIME_MS*1000000L;long counter;void check()throws IOException{if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Lectura cancelada");if(System.nanoTime()>deadline)throw new IOException("Se agotó el tiempo total de lectura (240 segundos)");}}
 static long copy(InputStream in,OutputStream out,long limit,Budget budget)throws Exception{byte[] b=new byte[65536];long total=0;int n;while((n=in.read(b))!=-1){budget.check();if(total+n>limit)throw new IOException("La página supera64 MiB; no guardé una parte");out.write(b,0,n);total+=n;}return total;}
 static final class Limited extends FilterInputStream{long bytes,limit;Limited(InputStream in,long max){super(in);limit=max;}public int read()throws IOException{int b=super.read();if(b>=0&&++bytes>limit)throw new IOException("La página supera64 MiB");return b;}public int read(byte[] b,int o,int n)throws IOException{int got=in.read(b,o,n);if(got>0&&(bytes+=got)>limit)throw new IOException("La página supera64 MiB");return got;}}
 static final class Sink implements Closeable{
  final File file;final Writer writer;long chars;boolean space,line;long checked;Sink(File f)throws Exception{file=f;writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),Charset.forName("UTF-8").newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)),16384);}
  void raw(int c)throws Exception{if(++chars>WIRE_LIMIT)throw new IOException("El HTML decodificado supera el límite de texto temporal");if(chars-checked>=1048576){checked=chars;if(file.getParentFile().getUsableSpace()<33554432L)throw new IOException("Falta espacio temporal para procesar la página");}writer.write(c);}
  void boundary(){line=true;space=false;}
  void unit(char c)throws Exception{if(c=='\ufeff')return;if(Character.isWhitespace(c)||c=='\u00a0'){space=true;return;}if(c<32)return;if(chars>0){if(line)raw('\n');else if(space)raw(' ');}line=false;space=false;raw(c);}
  void emit(String text)throws Exception{for(int i=0;i<text.length();i++)unit(text.charAt(i));}
  public void close()throws IOException{writer.close();}
 }
 static String entity(String value){switch(value){case "amp":return "&";case "lt":return "<";case "gt":return ">";case "quot":return "\"";case "apos":return "'";case "nbsp":case "ensp":case "emsp":return " ";case "mdash":return "—";case "ndash":return "–";case "hellip":return "…";case "rsquo":case "lsquo":return "'";case "ldquo":case "rdquo":return "\"";}if(value.startsWith("#"))try{int n=Integer.parseInt(value.startsWith("#x")||value.startsWith("#X")?value.substring(2):value.substring(1),value.length()>1&&(value.charAt(1)=='x'||value.charAt(1)=='X')?16:10);if(n>0&&Character.isValidCodePoint(n)&&(n<0xd800||n>0xdfff))return new String(Character.toChars(n));return " ";}catch(Exception ignored){}return "&"+value+";";}
 static PageReader.Document extract(File cache,String url,Reader source,boolean html,Budget budget)throws Exception{
  if(!cache.isDirectory()&&!cache.mkdirs())throw new IOException("No pude preparar el espacio temporal");File all=File.createTempFile("banana-page-",".txt",cache),main=File.createTempFile("banana-main-",".txt",cache);boolean complete=false;StringBuilder title=new StringBuilder();long selectedChars=0;File selected=all;
  try{try(PushbackReader input=new PushbackReader(new BufferedReader(source,16384),2);Sink body=new Sink(all);Sink semantic=new Sink(main)){
   if(!html){char[] buffer=new char[8192];int n;while((n=input.read(buffer))!=-1){budget.check();if(body.chars+n>NotebookDisk.DOCUMENT_CHARS)throw new IOException("El texto útil supera16000000 caracteres por documento");body.writer.write(buffer,0,n);body.chars+=n;}selectedChars=body.chars;
   }else{List<String> hidden=new ArrayList<>();int semanticDepth=0;boolean inTitle=false;int c;long checked=0;while((c=input.read())!=-1){if(++checked%4096==0)budget.check();if(c=='<'){
     StringBuilder tag=new StringBuilder();char quote=0;boolean done=false,comment=false;int prev=0,prev2=0,total=0;
     while((c=input.read())!=-1){if(++checked%4096==0)budget.check();total++;if(tag.length()<8192)tag.append((char)c);if(total==3&&tag.toString().equals("!--"))comment=true;
      if(comment){if(prev2=='-'&&prev=='-'&&c=='>'){done=true;break;}prev2=prev;prev=c;continue;}
      if(quote!=0){if(c==quote)quote=0;}else if(c=='\"'||c=='\'')quote=(char)c;else if(c=='>'){done=true;break;}
     }
     if(!done){if(comment)break;throw new IOException("El HTML terminó dentro de una etiqueta incompleta");}if(comment)continue;String raw=tag.toString().trim();boolean close=raw.startsWith("/");int at=close?1:0;while(at<raw.length()&&Character.isWhitespace(raw.charAt(at)))at++;int end=at;while(end<raw.length()&&(Character.isLetterOrDigit(raw.charAt(end))||raw.charAt(end)=='-'))end++;if(end==at)continue;String name=raw.substring(at,end).toLowerCase(Locale.ROOT);
     if(name.equals("title"))inTitle=!close;if(HIDDEN.contains(name)){if(close){int hit=hidden.lastIndexOf(name);if(hit>=0)while(hidden.size()>hit)hidden.remove(hidden.size()-1);}else if(!raw.endsWith("/>")){if(hidden.size()>=64)throw new IOException("HTML con anidación excesiva");hidden.add(name);}}
     if(hidden.isEmpty()){if(name.equals("main")||name.equals("article")){if(close)semanticDepth=Math.max(0,semanticDepth-1);else if(!raw.endsWith("/>"))semanticDepth++;}if(BLOCK.contains(name)){body.boundary();if(semanticDepth>0||close&&(name.equals("main")||name.equals("article")))semantic.boundary();}else{body.space=true;if(semanticDepth>0)semantic.space=true;}}continue;
    }
    if(c!='&'&&!inTitle){if(hidden.isEmpty()){body.unit((char)c);if(semanticDepth>0)semantic.unit((char)c);}continue;}
    String text;if(c=='&'){StringBuilder e=new StringBuilder();boolean ended=false;while(e.length()<40){int v=input.read();if(v==-1)break;checked++;if(v==';'){ended=true;break;}if(v=='<'||Character.isWhitespace(v)){input.unread(v);break;}e.append((char)v);}text=ended?entity(e.toString()):"&"+e;}else text=Character.toString((char)c);
    if(inTitle&&title.length()<512)title.append(text.substring(0,Math.min(text.length(),512-title.length())));if(hidden.isEmpty()){body.emit(text);if(semanticDepth>0)semantic.emit(text);}
   }if(semantic.chars>=40){selected=main;selectedChars=semantic.chars;}else selectedChars=body.chars;}
  }
  budget.check();if(selectedChars<40)throw new IOException("No se encontro texto util; puede requerir JavaScript o acceso privado");if(selectedChars>NotebookDisk.DOCUMENT_CHARS||selected.length()>NotebookDisk.DOCUMENT_BYTES)throw new IOException("El texto útil supera el límite por documento; no lo recorté");String heading=title.toString().replaceAll("\\s+"," ").trim();if(heading.isEmpty())heading=new java.net.URL(url).getHost();if(heading.length()>160)heading=heading.substring(0,160);File discarded=selected.equals(all)?main:all;discarded.delete();PageReader.Document doc=new PageReader.Document(heading,selected,url);complete=true;return doc;
  }finally{if(!complete){all.delete();main.delete();}}
 }
}
