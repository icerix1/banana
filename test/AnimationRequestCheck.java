package com.bananalab.lite;
public class AnimationRequestCheck{
 public static void main(String[] args)throws Exception{
  Semilla model=SemillaCheck.load(args[0]);
  String[] requests={"haceme una animacion de una banana bailando","podes hacer una animacion de un pepino caminando","quiero una animacion de un gato saltando","quiero que hagas una animacion de un robot saludando","anima una banana bailando","genera un video de un conejo caminando","me haces una animacion de una banana bailando","podes hacer que un gato salte"};
  for(String text:requests){CreatorSession c=new CreatorSession();CreatorSession.Result r=c.apply(model,text,null);if(!r.handled||r.commit==null||r.commit.actions.isEmpty())throw new AssertionError(text+" => "+r.text);System.out.println("PASS "+text+" => "+r.commit.describe());}
  CreatorSession c=new CreatorSession();CreatorSession.Result r=c.apply(model,"haceme una animacion de una banana bailando pero no lo dibujes todavia",null);if(r.commit!=null||c.pending==null)throw new AssertionError("defer");
  r=c.apply(model,"haceme una animacion",null);if(r.commit!=null||!r.handled)throw new AssertionError("missing subject");
  r=c.apply(model,"podes hacer una animacion de un dragon",null);if(r.commit!=null)throw new AssertionError("unsupported");
  System.out.println("PASS animacion ejecutable, acciones, borrador, aclaracion y limites");
 }
}
