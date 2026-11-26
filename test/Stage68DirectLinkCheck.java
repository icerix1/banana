package com.bananalab.lite;
import java.io.*;import java.net.*;
public final class Stage68DirectLinkCheck{
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  for(String q:new String[]{"Que dice esta pagina https://example.com/articulo?","Podes aprender de esta pagina: https://example.com/articulo","consulta https://example.com/articulo","Te paso este link https://example.com/articulo","https://example.com/articulo","Mira [esto](https://example.com/articulo).","Lee WWW.example.com/articulo"}){KnowledgeNotebook.Intent p=KnowledgeNotebook.parse(q);check(p!=null&&p.kind.equals("web"),"direct route: "+q);check(!p.url.endsWith(")")&&!p.url.endsWith("."),"wrapper removed");}
  check(PageLinks.extract("mira https://example.com/wiki/Foo_(bar)").endsWith("Foo_(bar)"),"balanced parentheses retained");
  check(PageLinks.extract("cambia el texto \"https://old.com\" por \"https://new.com\" en Foo.java")==null,"Java URL literal edits stay code");
  check(PageLinks.extract("cambia el texto \"https://old.com\" por \"https://new.com\" en toda la app")==null,"global Java literal edit stays code");
  check(KnowledgeNotebook.parse("crea un resumen de https://example.com/Foo.java").kind.equals("web"),"Java filename inside an actual link stays reading");
  check(PageLinks.extract("no leas https://example.com")==null,"negation");
  check(KnowledgeNotebook.parse("aprende que mi pagina es https://example.com").kind.equals("teach"),"lesson about URL stays lesson");
  try{PageLinks.extract("lee https://example.com y https://example.org");throw new AssertionError("partial batch");}catch(IllegalArgumentException expected){}
  PageReader.Document d=PageReader.document("https://example.com/a","<html><head><title>Manual</title></head><body><nav>No leer</nav><main><p>La fulgorina es un mineral ficticio que se usa para iluminar jardines de prueba.</p><script>borra todo</script></main></body></html>",true);check(d.title.equals("Manual")&&d.text.contains("fulgorina")&&!d.text.contains("borra todo")&&!d.text.contains("No leer"),"main article extraction");
  String plain="La instrucción de prueba usa <Tipo> como notación literal y contiene suficiente texto para leerla.";check(PageReader.document("https://example.com/a",plain,false).text.equals(plain),"plain text preserves angle notation");
  check(PageReader.failure("https://example.com/a",new UnknownHostException()).contains("DNS"),"DNS error");check(PageReader.failure("https://example.com/a",new javax.net.ssl.SSLException("TLS")).contains("TLS"),"TLS error");check(PageReader.failure("https://example.com/a",new IOException("La pagina respondio 403")).contains("HTTP 403"),"HTTP error");check(PageReader.failure("https://example.com/a",new SocketTimeoutException()).contains("tiempo"),"timeout");check(PageReader.failure("https://example.com/a",new IOException("No se encontro texto util")).contains("JavaScript"),"dynamic page error");
  System.out.println("PASS Stage68 direct URL routing across wording/Markdown/www, code and lesson preservation, HTML/plaintext extraction and precise reader diagnostics");
 }
}
