package com.bananalab.lite;
import org.json.*;import java.io.*;import java.nio.file.*;import java.util.*;
public final class Stage79CrawlJobCheck{
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{PageCrawl.pauseMs=0;File dir=Files.createTempDirectory("job70").toFile();Stage75SubLinksCheck.Site site=new Stage75SubLinksCheck.Site(dir);String h="https://example.com";
  site.pages.put(h+"/a",Stage75SubLinksCheck.page("Inicio A","La alfarina es una roca ficticia de nivel cero.","/b","/c","https://otro.example.org/x","/privado/secreto","/roto"));
  site.pages.put(h+"/b",Stage75SubLinksCheck.page("Pagina B","La betarina es un metal ficticio de nivel uno.","/d","/a"));site.pages.put(h+"/c",Stage75SubLinksCheck.page("Pagina C","La cetarina es un gas ficticio de nivel uno.","/d","/e"));
  site.pages.put(h+"/d",Stage75SubLinksCheck.page("Pagina D","La deltarina es una sal ficticia de nivel dos.","/f"));site.pages.put(h+"/e",Stage75SubLinksCheck.page("Pagina E","La epsilina es una fibra ficticia de nivel dos.","/g"));
  site.pages.put(h+"/f",Stage75SubLinksCheck.page("Pagina F","La efarina es una resina ficticia de nivel tres."));site.pages.put(h+"/g",Stage75SubLinksCheck.page("Pagina G","La gamarina es una arcilla ficticia de nivel tres."));
  site.pages.put(h+"/privado/secreto",Stage75SubLinksCheck.page("Secreto","No debe leerse por robots."));site.pages.put(h+"/robots.txt","User-agent: *\nDisallow: /privado\n# comentario para superar el mínimo de texto útil del lector\n");
  site.pages.put(h+"/solo","<html><body><p>La solarina es una página ficticia sin ningún enlace y con texto suficiente para leerse.</p></body></html>");
  CreatorPersistenceCheck.Fake c=new CreatorPersistenceCheck.Fake();final List<String> saved=new ArrayList<>();PageCrawl.Saver saver=new PageCrawl.Saver(){public String save(PageReader.Document d){saved.add(d.url);return "Guardé "+d.title;}};
  check(CrawlJob.status(c).contains("No hay ningún recorrido")&&!CrawlJob.pending(c)&&!CrawlJob.step(c,site,saver,10,60000),"nothing to do without a job");
  try{CrawlJob.start(c,PageCrawl.plan(h+"/nada con 3 niveles",h+"/nada"),site,saver);throw new AssertionError("missing first page");}catch(IOException expected){}check(CrawlJob.state(c)==null,"a failed first page queues nothing");site.requested.clear();
  String started=CrawlJob.start(c,PageCrawl.plan("lee "+h+"/a con 3 niveles",h+"/a"),site,saver);check(saved.equals(Arrays.asList(h+"/a"))&&started.contains("segundo plano")&&started.contains("3 niveles")&&CrawlJob.pending(c),"only the first page is read in the chat: "+started);check(site.requested.size()==1,"no sub-link requested yet");
  try{CrawlJob.start(c,PageCrawl.plan(h+"/solo y sus subenlaces",h+"/solo"),site,saver);throw new AssertionError("second job accepted");}catch(IllegalStateException expected){check(expected.getMessage().contains("cancelá el recorrido"),"busy message");}
  check(CrawlJob.step(c,site,saver,2,60000)&&saved.equals(Arrays.asList(h+"/a",h+"/b",h+"/c")),"first slice reads two pages in level order: "+saved);JSONObject mid=CrawlJob.state(c);check(mid.getInt("saved")==3&&!mid.getBoolean("done")&&mid.getInt("pending")>0,"progress is saved between slices: "+mid);
  check(CrawlJob.status(c).contains("Recorrido en curso")&&CrawlJob.status(c).contains("Páginas guardadas: 3"),"status while running");
  check(CrawlJob.pause(c).contains("Pausé")&&!CrawlJob.pending(c)&&!CrawlJob.step(c,site,saver,10,60000)&&saved.size()==3&&CrawlJob.status(c).contains("en pausa"),"a paused job does not advance");
  // «Another day»: nothing in memory, only what was saved
  CrawlJob.ROBOTS.clear();int before=site.requested.size();check(CrawlJob.resume(c).contains("Sigo con el recorrido")&&CrawlJob.pending(c),"resume");check(!CrawlJob.step(c,site,saver,50,60000),"job finishes");
  check(saved.equals(Arrays.asList(h+"/a",h+"/b",h+"/c",h+"/d",h+"/e",h+"/f",h+"/g")),"resumed job continues where it stopped: "+saved);List<String> later=site.requested.subList(before,site.requested.size());check(!later.contains(h+"/a")&&!later.contains(h+"/b")&&!later.contains(h+"/c")&&!site.requested.contains(h+"/privado/secreto")&&!site.requested.contains("https://otro.example.org/x")&&Collections.frequency(site.requested,h+"/d")==1,"saved pages are not requested again and limits still hold");
  String done=CrawlJob.status(c);check(done.contains("Recorrido terminado")&&done.contains("Páginas guardadas: 7")&&done.contains("1: 2 2: 2 3: 2")&&done.contains("robots.txt")&&done.contains("/roto")&&!CrawlJob.pending(c),"final report: "+done);
  check(CrawlJob.cancel(c).contains("No hay un recorrido en curso")&&CrawlJob.pause(c).contains("No hay"),"finished job cannot be paused or cancelled");
  // Page cap and cancel
  saved.clear();CrawlJob.start(c,PageCrawl.plan(h+"/a con 3 niveles y hasta 3 paginas",h+"/a"),site,saver);CrawlJob.step(c,site,saver,50,60000);check(saved.size()==3&&CrawlJob.state(c).getBoolean("done")&&CrawlJob.status(c).contains("tope de 3")&&CrawlJob.status(c).contains("sin leer"),"page cap ends the job with pending links reported");
  saved.clear();CrawlJob.start(c,PageCrawl.plan(h+"/a con 3 niveles",h+"/a"),site,saver);CrawlJob.step(c,site,saver,1,60000);String cancelled=CrawlJob.cancel(c);check(cancelled.contains("Cancelé")&&cancelled.contains("2 páginas")&&!CrawlJob.pending(c)&&!CrawlJob.step(c,site,saver,10,60000)&&saved.size()==2,"cancel keeps what was saved and stops: "+cancelled);
  String single=CrawlJob.start(c,PageCrawl.plan(h+"/solo y sus subenlaces",h+"/solo"),site,saver);check(single.contains("no tenía subenlaces")&&!CrawlJob.pending(c),"page without links finishes at once");
  // Interruption inside a slice keeps the page for later
  saved.clear();CrawlJob.start(c,PageCrawl.plan(h+"/a con 2 niveles",h+"/a"),site,saver);Thread.currentThread().interrupt();check(CrawlJob.step(c,site,saver,10,60000)&&Thread.interrupted()&&saved.size()==1,"an interrupted slice reads nothing and stays pending");check(!CrawlJob.step(c,site,saver,10,60000)&&saved.size()==5,"and continues later: "+saved);
  check("status".equals(CrawlJob.parse("¿Cómo va el recorrido?"))&&"pause".equals(CrawlJob.parse("pausá el recorrido"))&&"resume".equals(CrawlJob.parse("Seguí el recorrido"))&&"cancel".equals(CrawlJob.parse("cancelá el recorrido"))&&"cancel".equals(CrawlJob.parse("detené la lectura de subenlaces"))&&CrawlJob.parse("recorré https://example.com")==null&&CrawlJob.parse("cancelá la escena")==null,"chat phrases");
  check(!KnowledgeNotebook.background(c),"host context keeps the synchronous route used by the other suites");String[] left=dir.list();check(left==null||left.length==0,"temporary files cleaned");
  // Exam trend text
  JSONObject state=new JSONObject();JSONArray exams=new JSONArray();int[] right={2,3,2,6,7,8};for(int r:right)exams.put(new JSONObject().put("time",1000L).put("items",12).put("right",r));state.put("exams",exams);check(LectorExam.history(state).contains("Tendencia: últimos tres exámenes 58 % · tres anteriores 19 % (subiendo)"),"trend: "+LectorExam.history(state));
  System.out.println("PASS Stage79 background sub-link job: first page in chat, saved queue, slices, pause/resume/cancel, restart without re-reading, robots and caps kept, interruption, phrases and exam trend");
 }
}
