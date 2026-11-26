package com.bananalab.lite;
public class CreatorCheck{
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Semilla m=SemillaCheck.load(args[0]);CreatorSession c=new CreatorSession();SceneDraft actual=null;
  String[] planning={"Quiero una serie de terror con una banana","Que se llame Tony","Agregá un pepino pero todavía no lo dibujes"};
  for(String q:planning){CreatorSession.Result r=c.apply(m,q,actual);System.out.println(q+" => "+r.text);check(r.handled&&r.commit==null&&c.pending!=null,"draft isolation");c=CreatorSession.read(c.json());}
  check(c.pending.cast.size()==2&&c.pending.get("banana").label.equals("tony"),"names retained");
  check(c.apply(m,"Como se llamaba el protagonista",actual).text.contains("tony"),"name query");
  CreatorSession.Result p=c.apply(m,"Ahora dibujalos en un pasillo",actual);
  check(p.commit!=null&&c.pending==null&&p.commit.setting.equals("pasillo"),"publish");
  actual=p.commit;String before=actual.json().toString();
  p=c.apply(m,"Hace a Tony rojo",actual);check(p.commit!=null&&p.commit.get("banana").look.contains("rojo"),"alias editing");check(actual.json().toString().equals(before),"transaction copy");actual=p.commit;
  p=c.apply(m,"Hace al segundo mas chico y conserva a Tony",actual);check(p.commit!=null&&p.commit.get("pepino").size<1&&p.commit.get("banana").label.equals("tony"),"ordinal preserves label");actual=p.commit;
  p=c.apply(m,"No el otro era verde",actual);check(p.commit!=null&&p.commit.get("banana").look.contains("verde")&&!p.commit.get("pepino").look.contains("verde"),"other focus correction");actual=p.commit;
  p=c.apply(m,"hace que Tony aparezca al final",actual);check(p.commit!=null&&p.commit.cast.get(1).name.equals("banana"),"reveal target");actual=p.commit;
  String original=actual.json().toString();
  p=c.apply(m,"agrega un gato sin dibujar",actual);check(p.commit==null&&c.pending!=null&&actual.json().toString().equals(original),"defer existing");
  c.apply(m,"que se llame Milo",actual);check(c.pending.get("gato").label.equals("milo"),"second named draft");
  c.apply(m,"deshacer",actual);check(c.pending.get("gato").label.isEmpty(),"draft undo");
  c.apply(m,"rehacer",actual);check(c.pending.get("gato").label.equals("milo"),"draft redo");
  String saved=c.json();p=c.apply(m,"agrega un dragon",actual);check(p.commit==null&&c.json().equals(saved),"unsupported unchanged");
  c.apply(m,"descarta el borrador",actual);check(c.pending==null&&actual.json().toString().equals(original),"discard");
  check(c.apply(m,"publica el borrador",actual).commit==null,"no invented draft");
  CreatorSession empty=new CreatorSession();empty.apply(m,"quiero una serie con un gato",null);empty.apply(m,"deshacer",null);check(empty.pending==null,"undo initial draft");empty.apply(m,"rehacer",null);check(empty.pending!=null,"redo initial draft");
  p=c.apply(m,"ordena los personajes",actual);check(p.commit!=null&&Math.abs(p.commit.cast.get(0).x-p.commit.cast.get(1).x)>30,"composition spacing");
  System.out.println("PASS borrador aislado, nombres, publicar, alias, ordinal, otro, revelar, persistir, deshacer/rehacer y rechazo");
 }
}
