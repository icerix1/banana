package com.bananalab.lite;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class Stage75SubLinksCheck{
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static String page(String title,String fact,String... links){StringBuilder s=new StringBuilder("<html><head><title>"+title+"</title><script>var x='<a href=\"/trampa-script\">';</script></head><body><nav><a href='/'>Inicio</a></nav><main><p>"+fact+" Este párrafo agrega texto suficiente para superar el mínimo de contenido útil del lector.</p>");for(String l:links)s.append("<p><a class=\"x\" href=\"").append(l).append("\">enlace</a></p>");return s.append("</main></body></html>").toString();}
 static final class Site implements KnowledgeNotebook.PageLoader{final Map<String,String> pages=new HashMap<>(),redirects=new HashMap<>();final List<String> requested=new ArrayList<>();final File dir;Site(File d){dir=d;}
  public PageReader.Document load(String url)throws Exception{requested.add(url);String real=redirects.containsKey(url)?redirects.get(url):url;String body=pages.get(real);if(body==null)throw new IOException("La pagina respondio 404");return PageStream.extract(dir,real,new StringReader(body),!real.endsWith("robots.txt"),new PageStream.Budget());}}
 public static void main(String[] args)throws Exception{PageCrawl.pauseMs=0;File dir=Files.createTempDirectory("crawl67").toFile();
  // Wording and limits
  check(PageCrawl.wanted("lee https://example.com/a y sus subenlaces")&&PageCrawl.wanted("recorre todo el sitio https://example.com")&&PageCrawl.wanted("https://example.com con 4 niveles")&&PageCrawl.wanted("lee los sub enlaces de www.example.com"),"crawl wording");
  check(!PageCrawl.wanted("https://example.com/niveles/subenlaces")&&!PageCrawl.wanted("que dice esta pagina https://example.com/recorre "),"words inside the address do not trigger a crawl");
  PageCrawl.Plan p=PageCrawl.plan("lee https://example.com/3-niveles con cinco niveles y hasta 12 paginas","https://example.com/3-niveles");check(p.depth==5&&p.pages==12&&p.note.isEmpty(),"depth and page numbers from words");check(PageCrawl.plan("lee https://example.com con 1 nivel","x").depth==1&&PageCrawl.plan("https://example.com un nivel","x").depth==1&&PageCrawl.wanted("https://example.com 1 nivel"),"singular «nivel» is understood");
  p=PageCrawl.plan("lee los subenlaces de https://example.com","https://example.com");check(p.depth==PageCrawl.DEFAULT_DEPTH&&p.pages==PageCrawl.DEFAULT_PAGES,"defaults");
  p=PageCrawl.plan("https://example.com con 50 niveles y hasta 9000 paginas","https://example.com");check(p.depth==PageCrawl.MAX_DEPTH&&p.pages==PageCrawl.MAX_PAGES&&p.note.contains("máximo"),"clamped limits are reported");
  p=PageCrawl.plan("recorre https://example.com con muchos niveles","https://example.com");check(p.depth==8&&p.pages==300,"many levels");p=PageCrawl.plan("https://example.com con quince niveles y hasta 800 paginas","https://example.com");check(p.depth==15&&p.pages==800&&p.note.isEmpty()&&PageCrawl.MAX_PAGES<=NotebookDisk.MAX_DOCUMENTS,"raised limits stay inside the library capacity");
  check("on".equals(PageCrawl.preference("Siempre leé los subenlaces"))&&"on".equals(PageCrawl.preference("lee siempre los subenlaces de las paginas que te mando"))&&"off".equals(PageCrawl.preference("no leas los subenlaces"))&&"off".equals(PageCrawl.preference("Dejá de leer los subenlaces"))&&PageCrawl.preference("no leas https://example.com")==null&&PageCrawl.preference("lee los subenlaces")==null,"preference phrases");
  check(PageCrawl.followUp("Leé los subenlaces")&&PageCrawl.followUp("ahora segui los enlaces de esa pagina con 2 niveles")&&!PageCrawl.followUp("que son los subenlaces")&&!PageCrawl.followUp("lee los subenlaces de https://example.com"),"follow-up phrases");
  check(KnowledgeNotebook.parse("lee https://example.com/a y sus subenlaces").crawl&&!KnowledgeNotebook.parse("https://example.com/a").crawl&&KnowledgeNotebook.parse("lee los subenlaces").kind.equals("crawl_last")&&KnowledgeNotebook.parse("no leas los subenlaces").kind.equals("crawl_off")&&KnowledgeNotebook.parse("no leas https://example.com")==null,"notebook routing");
  // Link resolution
  java.net.URL base=new java.net.URL("https://www.example.com/docs/a.html");
  check("https://www.example.com/docs/b?x=1&y=2".equals(PageCrawl.resolve(base,"b?x=1&amp;y=2#parte"))&&"https://www.example.com/c".equals(PageCrawl.resolve(base,"/c"))&&"https://example.com/d".equals(PageCrawl.resolve(base,"http://EXAMPLE.com/d")),"relative, entity, fragment and http upgrade");check("https://www.example.com/wiki/Pl%C3%A1tano_(fruta)".equals(PageCrawl.resolve(base,"/wiki/Plátano_(fruta)"))&&"https://www.example.com/wiki/Pl%C3%A1tano".equals(PageCrawl.resolve(base,"/wiki/Pl%C3%A1tano")),"non-ASCII paths are percent-encoded once");
  for(String bad:new String[]{"#x","mailto:a@example.com","javascript:void(0)","tel:123","/a.pdf","/img/x.PNG","/login","/wiki?action=edit","https://user:pw@example.com/a","https://example.com:8080/a","ftp://example.com/a"})check(PageCrawl.resolve(base,bad)==null,"skipped link "+bad);
  check(PageCrawl.key("https://www.example.com/a/").equals(PageCrawl.key("https://example.com/a"))&&!PageCrawl.key("https://example.com/a?p=1").equals(PageCrawl.key("https://example.com/a?p=2")),"deduplication key");
  check("/x y".equals(PageStream.href("a class=q HREF = '/x y' id=z"))&&"/u".equals(PageStream.href("a href=/u"))&&PageStream.href("a data-href=\"/no\" name=x")==null&&"/v".equals(PageStream.href("a\nhref=\"/v\"")),"href attribute parsing");
  // robots.txt
  PageCrawl.Robots robots=PageCrawl.Robots.parse(new StringReader("User-agent: otro\nDisallow: /\n\nUser-agent: *\nDisallow: /privado\nAllow: /privado/publico\nDisallow: /*.php$\nCrawl-delay: 2\n"));
  check(robots.allowed("https://example.com/a")&&!robots.allowed("https://example.com/privado/x")&&robots.allowed("https://example.com/privado/publico/y")&&!robots.allowed("https://example.com/x/y.php")&&robots.allowed("https://example.com/x/y.php?z")&&robots.delayMs==2000,"robots general group, longest match, wildcard and delay");
  check(!PageCrawl.Robots.parse(new StringReader("User-agent: *\nAllow: /\nUser-agent: BananaLabLite\nDisallow: /a\n")).allowed("https://example.com/a"),"own robots group overrides the general one");
  // Site: a -> b,c,(external),(pdf),(privado) ; b -> d,a ; c -> d ; d -> e ; e -> f ; g redirects off-site
  Site site=new Site(dir);String h="https://example.com";
  site.pages.put(h+"/a",page("Inicio A","La alfarina es una roca ficticia de nivel cero.","/b","c","https://otro.example.org/x","/manual.pdf","/privado/secreto","/a#arriba","/g","/roto"));
  site.pages.put(h+"/b",page("Pagina B","La betarina es un metal ficticio de nivel uno.","/d","/a"));
  site.pages.put(h+"/c",page("Pagina C","La cetarina es un gas ficticio de nivel uno.","/d","/b/"));
  site.pages.put(h+"/d",page("Pagina D","La deltarina es una sal ficticia de nivel dos.","/e"));
  site.pages.put(h+"/e",page("Pagina E","La epsilina es una fibra ficticia de nivel tres.","/f"));
  site.pages.put(h+"/f",page("Pagina F","La efarina es una resina ficticia de nivel cuatro."));
  site.pages.put(h+"/privado/secreto",page("Secreto","No debe leerse por robots."));
  site.pages.put("https://otro.example.org/x",page("Externa","No debe leerse por ser otro sitio."));
  site.pages.put("https://otro.example.org/g",page("Redirigida","No debe guardarse por redirigir afuera."));site.redirects.put(h+"/g","https://otro.example.org/g");
  site.pages.put(h+"/robots.txt","User-agent: *\nDisallow: /privado\n# comentario para superar el mínimo de texto útil del lector\n");
  try(PageReader.Document d=site.load(h+"/a")){check(d.links.contains("/b")&&d.links.contains("c")&&d.links.contains("/")&&!d.links.contains("/trampa-script"),"stream extraction collects body and nav links, not script text");check(d.links.indexOf("/b")<d.links.indexOf("/")&&d.links.get(d.links.size()-1).equals("/"),"main content links come before menu links");check(PageCrawl.sameSite(d)==6,"same-site link count for the hint: "+PageCrawl.sameSite(d));}
  site.requested.clear();final List<String> saved=new ArrayList<>();PageCrawl.Saver saver=new PageCrawl.Saver(){public String save(PageReader.Document d)throws Exception{StringBuilder t=new StringBuilder();try(Reader in=d.reader()){char[] b=new char[4096];int n;while((n=in.read(b))!=-1)t.append(b,0,n);}check(t.length()>40,"text available while saving");saved.add(d.url);return "Guardé\nEncolado";}};
  PageCrawl.Plan plan=PageCrawl.plan("lee "+h+"/a con 2 niveles",h+"/a");PageCrawl.Report r=PageCrawl.run(plan,site,saver);
  check(saved.equals(Arrays.asList(h+"/a",h+"/b",h+"/c",h+"/d")),"level order within two levels: "+saved);check(r.saved==4&&r.levels[1]==2&&r.levels[2]==1&&r.deepest==2&&r.robots==1&&r.outside==1&&r.failed==2&&r.pending==0,"report counters "+r.robots+" "+r.outside+" "+r.failed+" "+r.pending);
  check(!site.requested.contains("https://otro.example.org/x")&&!site.requested.contains(h+"/privado/secreto")&&!site.requested.contains(h+"/manual.pdf")&&!site.requested.contains(h+"/e"),"external, robots, binary and too-deep links never requested");check(Collections.frequency(site.requested,h+"/d")==1&&Collections.frequency(site.requested,h+"/a")==1,"each page requested once");
  String text=PageCrawl.describe(plan,r);check(text.contains("Guardé 4 páginas")&&text.contains("nivel 2: 1")&&text.contains("robots.txt")&&text.contains("fuera del sitio")&&text.contains("/roto")&&text.endsWith("esas páginas.")&&text.contains("Encolado"),"report text: "+text);
  saved.clear();r=PageCrawl.run(PageCrawl.plan("lee "+h+"/a con 6 niveles",h+"/a"),site,saver);check(saved.size()==6&&saved.get(5).equals(h+"/f")&&r.deepest==4,"many levels reach the deepest page");
  saved.clear();plan=PageCrawl.plan(h+"/a con 6 niveles y hasta 3 paginas",h+"/a");r=PageCrawl.run(plan,site,saver);check(saved.size()==3&&r.pending>0&&r.end.contains("tope de 3"),"page cap stops and reports pending");
  site.pages.put(h+"/solo","<html><body><p>La solarina es una página ficticia sin ningún enlace y con texto suficiente para leerse.</p></body></html>");saved.clear();r=PageCrawl.run(PageCrawl.plan(h+"/solo con 3 niveles",h+"/solo"),site,saver);check(saved.size()==1&&PageCrawl.describe(new PageCrawl.Plan(),r).contains("no tenía subenlaces"),"page without links");
  try{PageCrawl.run(PageCrawl.plan(h+"/nada con 3 niveles",h+"/nada"),site,saver);throw new AssertionError("missing start page accepted");}catch(IOException expected){}
  saved.clear();r=PageCrawl.run(PageCrawl.plan(h+"/a con 3 niveles",h+"/a"),site,new PageCrawl.Saver(){public String save(PageReader.Document d){saved.add(d.url);Thread.currentThread().interrupt();return "";}});check(Thread.interrupted()&&saved.size()==1&&r.end.contains("cancelado"),"cancellation keeps what was saved");
  // Whole route through the notebook with real storage
  CreatorPersistenceCheck.Fake c=new CreatorPersistenceCheck.Fake();NotebookDisk disk=NotebookDisk.of(c);
  String one=KnowledgeNotebook.handle(c,h+"/a",site).text;check(disk.index.count()==1&&one.contains("6 subenlaces")&&one.contains("leé los subenlaces"),"single read keeps one document and offers the crawl: "+one);
  String follow=KnowledgeNotebook.handle(c,"Leé los subenlaces con 2 niveles",site).text;check(disk.index.count()==4&&follow.contains("Guardé 4 páginas"),"follow-up crawls the last page: "+follow);
  check(ReadingKnowledge.answer(disk.selected("que es la deltarina"),"que es la deltarina").contains("sal ficticia"),"second-level page is answerable with its own source");
  check(KnowledgeNotebook.handle(c,"siempre lee los subenlaces",site).text.contains("Desde ahora")&&PageCrawl.always(c),"preference on");
  String all=KnowledgeNotebook.handle(c,h+"/a",site).text;check(disk.index.count()==6&&all.contains("Guardé 6 páginas"),"preference crawls a bare link with default depth: "+all);
  check(KnowledgeNotebook.handle(c,"no leas los subenlaces",site).text.contains("sólo la página")&&!PageCrawl.always(c),"preference off");
  String direct=KnowledgeNotebook.handle(c,"lee "+h+"/a y todos sus enlaces, 6 niveles",site).text;check(disk.index.count()==6&&direct.contains("nivel 4: 1"),"explicit request with depth: "+direct);
  String missing=KnowledgeNotebook.handle(c,"lee los subenlaces de "+h+"/nada",site).text;check(missing.contains("No pude leer directamente")&&missing.contains("404"),"failure of the first page keeps the reader diagnosis");
  String[] left=dir.list();check(left==null||left.length==0,"temporary files cleaned");
  System.out.println("PASS Stage75 sub-link reading: wording, limits, link resolution, robots, level order, caps, cancellation, notebook route, preference and follow-up");
 }
}
