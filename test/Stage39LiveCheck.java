package com.bananalab.lite;
/** Optional integration probe. Availability depends on the public providers, not the fixtures. */
public final class Stage39LiveCheck{
 public static void main(String[] args)throws Exception{String query=args.length==0?"energia solar":args[0];long started=System.currentTimeMillis();ResearchWeb.Batch b=ResearchWeb.search(query,ResearchWeb.NETWORK);System.out.println("LIVE query="+query+" millis="+(System.currentTimeMillis()-started)+" docs="+b.docs.size()+" warning="+b.warning);for(ResearchWeb.Doc d:b.docs)System.out.println(d.title+"\n"+d.url+"\n"+ResearchAnswer.excerpt(query,d.text));if(b.docs.isEmpty())throw new AssertionError("No live readable source");}
}
