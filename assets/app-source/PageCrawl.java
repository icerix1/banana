package com.bananalab.lite;
import android.content.Context;
import java.io.*;import java.net.*;import java.util.*;import java.util.regex.*;
/** Bounded, same-site, level-order reading of the links found inside a page the user sent. Text only; nothing fetched is executed. */
final class PageCrawl{
 static final int DEFAULT_DEPTH=4,MAX_DEPTH=20,DEFAULT_PAGES=100,MAX_PAGES=1000,MAX_QUEUE=50000,PAGE_LINKS=4000;
 static final long TIME_MS=3600000L;static volatile long pauseMs=400;
 static final Pattern SKIP_FILE=Pattern.compile("(?i).*\\.(?:pdf|jpe?g|png|gif|webp|svg|ico|bmp|tiff?|zip|rar|7z|gz|tgz|bz2|tar|mp[34]|m4[av]|avi|mov|mkv|webm|wav|ogg|flac|css|js|mjs|json|xml|rss|atom|woff2?|ttf|otf|eot|exe|msi|dmg|apk|iso|bin|docx?|xlsx?|pptx?|odt|ods|epub|csv)$");
 static final Pattern SKIP_ACTION=Pattern.compile("(?i)(?:^|[/?&=_.-])(?:login|logout|log-in|log-out|signin|signup|sign-in|sign-up|register|registro|carrito|cart|checkout)(?:$|[/?&=_.-])|[?&](?:action=(?:edit|history|raw|submit)|oldid=|diff=|printable=|share=|replytocom=|sessionid=|sid=)");
 static final Pattern WANTED=Pattern.compile("\\bsub ?(?:enlaces?|links?|paginas?|vinculos?)\\b|\\b(?:enlaces|links|vinculos|paginas) (?:internos|internas|que (?:tiene|contiene|incluye)|de adentro|relacionad[oa]s)\\b|\\btod[oa]s? (?:los|las|sus) (?:enlaces|links|vinculos|paginas)\\b|\\b(?:todo el|el) sitio (?:completo|entero)\\b|\\btodo el sitio\\b|\\btoda la (?:web|pagina web)\\b|\\b(?:[0-9]+|un|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|dieciocho|diecinueve|veinte|muchos|varios) nivel(?:es)?\\b|\\ben profundidad\\b|\\bprofundidad (?:de )?[0-9]+\\b|\\b(?:recorre|recorra|recorras|rastrea|rastrees|explora|explores) \\b|\\b(?:segui|sigue|sigas) (?:los|sus) (?:enlaces|links|vinculos)\\b|\\b(?:otros sitios|otros dominios|cualquier sitio|fuera del sitio|saliendo del sitio)\\b|\\btoda la (?:internet|red)\\b|\\btodo internet\\b|\\by (?:sus|los) (?:enlaces|links|vinculos)\\b");
 static final String[] NUMBERS={"cero","un","dos","tres","cuatro","cinco","seis","siete","ocho","nueve","diez","once","doce","trece","catorce","quince","dieciseis","diecisiete","dieciocho","diecinueve","veinte"};

 static final class Plan{String url="";int depth=DEFAULT_DEPTH,pages=DEFAULT_PAGES;String note="";}
 interface Saver{String save(PageReader.Document d)throws Exception;}
 static final class Report{String start="",site="",end="",receipt="";int saved,failed,robots,outside,pending,deepest;int[] levels=new int[MAX_DEPTH+1];List<String> titles=new ArrayList<>(),sources=new ArrayList<>(),errors=new ArrayList<>();}

 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("banana-page-crawl",0);}
 static boolean always(Context c){return prefs(c).getBoolean("always",false);}

 /** Words of the request with every address removed, so a URL that contains «niveles» cannot trigger a crawl. */
 static String wording(String raw){return ChatLanguage.normalize(raw.replaceAll("(?iu)(?:https?://|www\\.)[^\\s<>\"“”`]+"," "));}
 static boolean wanted(String raw){return WANTED.matcher(wording(raw)).find();}
 /** Returns "on", "off" or null. Checked before the generic «no …» exit of the notebook parser. */
 static String preference(String raw){if(raw.matches("(?isu).*(?:https?://|www\\.).*"))return null;String q=wording(raw);
  if(q.matches("(?:siempre )?(?:lee|lea|leas|leeme|segui|sigue|recorre) siempre (?:tambien )?(?:los |sus )?(?:sub ?enlaces|sub ?links|enlaces internos|enlaces)(?: de (?:las|mis) paginas)?(?: que te (?:mando|paso|envio))?|siempre (?:lee|lea|leas|segui|sigue|recorre) (?:tambien )?(?:los |sus )?(?:sub ?enlaces|sub ?links|enlaces internos|enlaces)(?: de (?:las|mis) paginas)?(?: que te (?:mando|paso|envio))?|activa (?:la lectura de |el recorrido de )?(?:los )?(?:sub ?enlaces|sub ?links)"))return "on";
  if(q.matches("(?:ya )?no (?:leas|lea|sigas|recorras)(?: mas| siempre)? (?:los |sus )?(?:sub ?enlaces|sub ?links|enlaces internos|enlaces)|deja de (?:leer|seguir|recorrer) (?:los |sus )?(?:sub ?enlaces|sub ?links|enlaces internos|enlaces)|desactiva (?:la lectura de |el recorrido de )?(?:los )?(?:sub ?enlaces|sub ?links)|lee solo la pagina(?: que te (?:mando|paso|envio))?"))return "off";return null;}
 /** A request to follow the links of the page that was read last, without repeating its address. */
 static boolean followUp(String raw){if(raw.matches("(?isu).*(?:https?://|www\\.).*"))return false;String q=wording(raw);return q.matches("(?:ahora |y |tambien |dale |si |bueno )*(?:lee|lea|leas|leeme|segui|sigue|sigas|recorre|recorra|rastrea|explora|aprende|aprendas|mira|abri|abre)(?: tambien| ahora)? (?:de )?(?:todos )?(?:los|sus|esos|estos) (?:sub ?enlaces|sub ?links|sub ?paginas|enlaces internos|enlaces|links|vinculos)(?: (?:de|en) (?:esa|esta|la|ese|este|el) (?:pagina|enlace|link|sitio|fuente|articulo))?(?: (?:con|hasta|en|a|de|maximo|max|y) .*)?");}
 static int number(String word){if(word.matches("[0-9]{1,6}"))return Integer.parseInt(word);for(int i=0;i<NUMBERS.length;i++)if(NUMBERS[i].equals(word))return i;return -1;}
 static Plan plan(String raw,String url){Plan p=new Plan();p.url=url;String q=wording(raw);StringBuilder note=new StringBuilder();
  Matcher depth=Pattern.compile("\\b([0-9]{1,6}|un|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|dieciocho|diecinueve|veinte) nivel(?:es)?\\b|\\bprofundidad (?:de )?([0-9]{1,6})\\b").matcher(q);
  if(depth.find()){int n=number(depth.group(1)!=null?depth.group(1):depth.group(2));if(n>MAX_DEPTH){note.append("Pediste ").append(n).append(" niveles; el máximo es ").append(MAX_DEPTH).append(".\n");n=MAX_DEPTH;}if(n>=1)p.depth=n;}
  else if(q.matches(".*\\b(?:muchos|varios) niveles\\b.*|.*\\ben profundidad\\b.*|.*\\b(?:todo el sitio|sitio completo|sitio entero|toda la web)\\b.*"))p.depth=8;
  Matcher pages=Pattern.compile("\\b(?:hasta|maximo|max|como mucho|no mas de|tope de|limite de) ([0-9]{1,6}) (?:sub ?)?(?:paginas|enlaces|links|vinculos|documentos)\\b|\\b([0-9]{1,6}) (?:sub ?)?paginas\\b").matcher(q);
  if(pages.find()){int n=number(pages.group(1)!=null?pages.group(1):pages.group(2));if(n>MAX_PAGES){note.append("Pediste ").append(n).append(" páginas; el máximo por recorrido es ").append(MAX_PAGES).append(".\n");n=MAX_PAGES;}if(n>=1)p.pages=n;}
  else if(p.depth>=8)p.pages=Math.max(p.pages,300);
  p.note=note.toString();return p;}

 static String site(String host){String h=host.toLowerCase(Locale.ROOT);return h.startsWith("www.")?h.substring(4):h;}
 /** Absolute HTTPS address without fragment, or null when the link is not a readable page. */
 static String resolve(URL base,String href){try{String h=href.trim().replace("&amp;","&").replace("&#38;","&").replace("&#x26;","&");if(h.isEmpty()||h.startsWith("#")||h.length()>2000||h.indexOf('\n')>=0||h.indexOf('\r')>=0)return null;String low=h.toLowerCase(Locale.ROOT);
   if(low.matches("^[a-z][a-z0-9+.-]*:.*")&&!low.startsWith("http://")&&!low.startsWith("https://"))return null;
   URL u=new URL(base,h.replace(" ","%20"));String protocol=u.getProtocol().toLowerCase(Locale.ROOT);if(!protocol.equals("https")&&!protocol.equals("http"))return null;if(u.getUserInfo()!=null||u.getHost().isEmpty())return null;if(u.getPort()!=-1&&u.getPort()!=443&&!(protocol.equals("http")&&u.getPort()==80))return null;
   String path=u.getPath()==null||u.getPath().isEmpty()?"/":u.getPath();if(SKIP_FILE.matcher(path).matches())return null;String query=u.getQuery();String rest=path+(query==null||query.isEmpty()?"":"?"+query);if(SKIP_ACTION.matcher(rest).find())return null;
   String out="https://"+u.getHost().toLowerCase(Locale.ROOT)+ascii(rest);return out.length()>2000?null:out;}catch(Exception invalid){return null;}}
 /** Percent-encodes non-ASCII characters so the stored address is the one that is requested. */
 static String ascii(String text)throws Exception{StringBuilder out=null;for(int i=0;i<text.length();){int cp=text.codePointAt(i),n=Character.charCount(cp);if(cp<128&&cp>32){if(out!=null)out.append((char)cp);}else{if(out==null)out=new StringBuilder(text.substring(0,i));for(byte x:new String(Character.toChars(cp)).getBytes("UTF-8"))out.append('%').append(Character.toUpperCase(Character.forDigit((x>>4)&15,16))).append(Character.toUpperCase(Character.forDigit(x&15,16)));}i+=n;}return out==null?text:out.toString();}
 static String key(String url){try{URL u=new URL(url);String path=u.getPath()==null||u.getPath().isEmpty()?"/":u.getPath();if(path.length()>1&&path.endsWith("/"))path=path.substring(0,path.length()-1);return site(u.getHost())+path+(u.getQuery()==null?"":"?"+u.getQuery());}catch(Exception e){return url;}}

 /** robots.txt rules for every agent («*») and, when present, the group that names this reader. Longest match wins; Allow wins a tie. */
 static final class Robots{final List<String[]> rules=new ArrayList<>();long delayMs=-1;
  static Robots parse(Reader in)throws IOException{List<String[]> general=new ArrayList<>(),own=new ArrayList<>();long[] delay={-1,-1};BufferedReader r=new BufferedReader(in);String line;boolean star=false,mine=false,body=false;int lines=0;
   while((line=r.readLine())!=null&&lines++<20000){int hash=line.indexOf('#');if(hash>=0)line=line.substring(0,hash);int colon=line.indexOf(':');if(colon<=0)continue;String field=line.substring(0,colon).trim().toLowerCase(Locale.ROOT),value=line.substring(colon+1).trim();
    if(field.equals("user-agent")){if(body){star=false;mine=false;body=false;}String agent=value.toLowerCase(Locale.ROOT);if(agent.equals("*"))star=true;else if(!agent.isEmpty()&&"bananalablite".contains(agent))mine=true;continue;}
    body=true;if(field.equals("allow")||field.equals("disallow")){if(value.isEmpty()||value.length()>2000)continue;String[] rule={field,value};if(mine&&own.size()<2000)own.add(rule);if(star&&general.size()<2000)general.add(rule);}
    else if(field.equals("crawl-delay"))try{long ms=(long)(Double.parseDouble(value)*1000);if(ms>=0){if(mine)delay[1]=ms;if(star)delay[0]=ms;}}catch(NumberFormatException ignored){}}
   Robots out=new Robots();if(!own.isEmpty()||delay[1]>=0){out.rules.addAll(own);out.delayMs=delay[1];}else{out.rules.addAll(general);out.delayMs=delay[0];}return out;}
  static boolean matches(String pattern,String path){boolean anchored=pattern.endsWith("$");String p=anchored?pattern.substring(0,pattern.length()-1):pattern;StringBuilder regex=new StringBuilder();for(String part:p.split("\\*",-1)){if(regex.length()>0)regex.append(".*");regex.append(Pattern.quote(part));}if(!anchored)regex.append(".*");try{return Pattern.compile(regex.toString(),Pattern.DOTALL).matcher(path).matches();}catch(Exception e){return false;}}
  boolean allowed(String url){String path;try{URL u=new URL(url);path=(u.getPath()==null||u.getPath().isEmpty()?"/":u.getPath())+(u.getQuery()==null?"":"?"+u.getQuery());}catch(Exception e){return false;}int best=-1;boolean allow=true;for(String[] rule:rules)if(matches(rule[1],path)){int size=rule[1].length();boolean a=rule[0].equals("allow");if(size>best||size==best&&a){best=size;allow=a;}}return allow;}
 }
 static Robots robots(String origin,KnowledgeNotebook.PageLoader pages){try(PageReader.Document d=pages.load(origin+"/robots.txt")){if(!key(d.url).equals(key(origin+"/robots.txt")))return new Robots();StringBuilder text=new StringBuilder();try(Reader in=d.reader()){char[] b=new char[8192];int n;while(text.length()<524288&&(n=in.read(b))!=-1)text.append(b,0,n);}return Robots.parse(new StringReader(text.toString()));}catch(InterruptedIOException stop){Thread.currentThread().interrupt();return new Robots();}catch(Exception none){return new Robots();}}

 static boolean full(Exception e){String m=e.getMessage();return m!=null&&(m.contains("biblioteca admite")||m.contains("Falta espacio"));}
 static void pause(long ms)throws InterruptedIOException{if(ms<=0)return;try{Thread.sleep(ms);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new InterruptedIOException("Lectura cancelada");}}
 static String brief(Exception e){String m=e.getMessage();if(e instanceof UnknownHostException)m="DNS sin resolver";else if(e instanceof SocketTimeoutException)m="tiempo de espera agotado";else if(m==null||m.isEmpty())m=e.getClass().getSimpleName();return m.length()>90?m.substring(0,90)+"…":m;}

 /** Reads the first page (an error there is thrown: nothing was saved) and then its links level by level, inside the same site. */
 static Report run(Plan plan,KnowledgeNotebook.PageLoader pages,Saver saver)throws Exception{Report r=new Report();r.start=plan.url;long deadline=System.nanoTime()+TIME_MS*1000000L;
  Set<String> seen=new HashSet<>();ArrayDeque<String[]> queue=new ArrayDeque<>();URL first;String site;Robots robots;long wait;
  JavaChat.phase("Leyendo la página inicial…");
  try(PageReader.Document d=pages.load(plan.url)){first=new URL(d.url);site=site(first.getHost());r.site=site;seen.add(key(plan.url));seen.add(key(d.url));r.receipt=saver.save(d);r.saved=1;r.levels[0]=1;r.titles.add(d.title);r.sources.add(d.url);collect(d,first,site,1,plan.depth,seen,queue);}
  robots=queue.isEmpty()?new Robots():robots("https://"+first.getHost(),pages);wait=Math.max(pauseMs,Math.min(robots.delayMs,5000));
  while(!queue.isEmpty()){if(r.saved>=plan.pages){r.end="Llegué al tope de "+plan.pages+" páginas.";break;}if(System.nanoTime()>deadline){r.end="Se agotó el tiempo total del recorrido ("+TIME_MS/60000+" minutos).";break;}if(Thread.currentThread().isInterrupted()){r.end="El recorrido fue cancelado.";break;}
   String[] next=queue.poll();String url=next[0];int level=Integer.parseInt(next[1]);if(!robots.allowed(url)){r.robots++;continue;}
   JavaChat.phase("Leyendo subenlaces: "+r.saved+" de hasta "+plan.pages+" páginas · nivel "+level+" de "+plan.depth+" · "+queue.size()+" en cola");
   try{pause(wait);}catch(InterruptedIOException stop){r.end="El recorrido fue cancelado.";queue.addFirst(next);break;}
   try(PageReader.Document d=pages.load(url)){URL real=new URL(d.url);if(!site(real.getHost()).equals(site)){r.outside++;continue;}String k=key(d.url);if(!k.equals(key(url))){if(!seen.add(k))continue;if(!robots.allowed(d.url)){r.robots++;continue;}}
    r.receipt=saver.save(d);r.saved++;r.levels[level]++;r.deepest=Math.max(r.deepest,level);if(r.titles.size()<8)r.titles.add(d.title);r.sources.add(d.url);collect(d,real,site,level+1,plan.depth,seen,queue);
   }catch(InterruptedIOException stop){if(Thread.currentThread().isInterrupted()||"Lectura cancelada".equals(stop.getMessage())){Thread.currentThread().interrupt();r.end="El recorrido fue cancelado.";queue.addFirst(next);break;}r.failed++;if(r.errors.size()<4)r.errors.add(url+" → "+brief(stop));
   }catch(Exception e){if(full(e)){r.end="La biblioteca alcanzó su límite: "+brief(e);queue.addFirst(next);break;}r.failed++;if(r.errors.size()<4)r.errors.add(url+" → "+brief(e));if(r.failed>=25&&r.failed>r.saved*3){r.end="Demasiados subenlaces fallaron; detuve el recorrido para no insistir contra el sitio.";break;}}
  }
  r.pending=queue.size();if(r.end.isEmpty())r.end=r.pending==0?"Recorrí todos los subenlaces encontrados dentro de "+plan.depth+(plan.depth==1?" nivel.":" niveles."):"Fin del recorrido.";return r;}
 static void collect(PageReader.Document d,URL base,String site,int level,int maxDepth,Set<String> seen,ArrayDeque<String[]> queue){if(level>maxDepth)return;for(String href:d.links){if(queue.size()>=MAX_QUEUE||seen.size()>=MAX_QUEUE*4)return;String url=resolve(base,href);if(url==null)continue;try{if(!site(new URL(url).getHost()).equals(site))continue;}catch(Exception e){continue;}if(seen.add(key(url)))queue.add(new String[]{url,Integer.toString(level)});}}

 /** Links of the same site that a single-page reading found, for the hint shown after a normal read. */
 static int sameSite(PageReader.Document d){try{URL base=new URL(d.url);String site=site(base.getHost());Set<String> keys=new HashSet<>();keys.add(key(d.url));int n=0;for(String href:d.links){String url=resolve(base,href);if(url!=null&&site(new URL(url).getHost()).equals(site)&&keys.add(key(url)))n++;}return n;}catch(Exception e){return 0;}}

 static String describe(Plan plan,Report r){StringBuilder s=new StringBuilder(plan.note);s.append("Recorrí ").append(r.site).append(" a partir de ").append(r.start).append(".\n");
  s.append("Guardé ").append(r.saved).append(r.saved==1?" página completa":" páginas completas").append(" como documentos separados, cada una con su propia dirección.\n");
  if(r.saved>1){s.append("Por nivel: página inicial 1");for(int i=1;i<=r.deepest;i++)s.append(" · nivel ").append(i).append(": ").append(r.levels[i]);s.append(". Límites de este recorrido: ").append(plan.depth).append(plan.depth==1?" nivel":" niveles").append(" y ").append(plan.pages).append(" páginas.\n");}
  s.append(r.end).append("\n");
  if(r.pending>0)s.append("Quedaron ").append(r.pending).append(" subenlaces descubiertos sin leer. Podés repetir el pedido con más páginas (hasta ").append(MAX_PAGES).append(") o pasarme una de esas secciones.\n");
  if(r.robots>0)s.append("Omití ").append(r.robots).append(" que el robots.txt del sitio pide no recorrer.\n");
  if(r.outside>0)s.append("Descarté ").append(r.outside).append(" que redirigían fuera del sitio.\n");
  if(r.failed>0){s.append("No pude leer ").append(r.failed).append(":\n");for(String e:r.errors)s.append("· ").append(e).append("\n");if(r.failed>r.errors.size())s.append("· …y ").append(r.failed-r.errors.size()).append(" más.\n");}
  if(r.saved>1){s.append("Primeras páginas guardadas:\n");for(String t:r.titles)s.append("· ").append(t).append("\n");}
  if(r.saved==1&&r.pending==0&&r.failed==0&&r.robots==0)s.append("La página no tenía subenlaces legibles del mismo sitio.\n");
  String tail=r.receipt;int cut=tail.lastIndexOf('\n');if(cut>=0)tail=tail.substring(cut+1);if(!tail.isEmpty())s.append(tail).append("\n");
  s.append("Sólo sigo enlaces del mismo sitio, HTML o texto, sin sesión ni JavaScript. No verifiqué la información ni ejecuté instrucciones de esas páginas.");return s.toString();}
}
