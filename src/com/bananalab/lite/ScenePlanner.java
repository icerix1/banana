package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
/** Bounded compositional Spanish planner. Unsupported clauses never partially commit. */
final class ScenePlanner{
 static final String VERBS="bes(?:a|e|ar|an|en|ando|alo|ala|ale)|habl\\w*|convers\\w*|charl\\w*|ced\\w*|regal\\w*|bail\\w*|camin\\w*|salt\\w*|salud\\w*|entr\\w*|lleg\\w*|apare\\w*|entreg\\w*|pasa|pase|pasar|da|dar|recib\\w*|sorprend\\w*|reaccion\\w*|mir\\w*|acerc\\w*";
 static final Pattern VERB=Pattern.compile("\\b("+VERBS+")\\b");
 static String language(String input){return CreationRequest.normalize(input).replaceAll("\\b(?:le da|da|dar|dale) un beso (?:a|al)\\b","besa a").replaceAll("^(?:que )?(.+?) y (.+?) se bes(?:an|en)$","$1 besa a $2");}
 static CreatorSession.Result apply(String input,SceneDraft base)throws Exception{return plan(input,base,false);}
 static CreatorSession.Result plan(String input,SceneDraft base,boolean force)throws Exception{return plan(input,base,force,false);}
 static CreatorSession.Result plan(String input,SceneDraft base,boolean force,boolean append)throws Exception{
  CreatorSession.Result module=PlannerDispatch.plan(input,base,append);if(module!=null)return module;
  String text=language(input).replace("al mismo tiempo que","mientras").replace("al mismo tiempo","mientras").replaceAll("\\b(?:luego|mas tarde|a continuacion|entonces)\\b","despues");
  if(!force&&text.matches("(?:crea|creame|dibuja|dibujame|disena|haceme)(?: a)? [a-z]{2,20} (?:un|una) (?:"+SceneDirector.TYPES+").*"))return null;
  if(base!=null){Matcher relative=Pattern.compile("(?:hace que |pone a |deja que )?(.+?) despues de (?:que )?(.+)").matcher(text);if(relative.matches()&&!text.contains("mientras")){int moved=findStep(base,relative.group(1)),after=findStep(base,relative.group(2));if(moved<0||after<0||moved==after)return CreatorSession.say("Nombrá las dos acciones que ya están en la secuencia, por ejemplo: Tony baila después de que el pepino entra.");SceneDraft d=base.copy();int next=d.slot(after)+1;if(next>=SceneTiming.MAX_BEATS)return CreatorSession.say("No queda un tiempo posterior libre.");d.slots.set(moved,next);try{SceneDraft.read(d.json().toString());}catch(Exception e){return CreatorSession.say("Ese cambio superpone dos acciones del mismo personaje. Elegí otro tiempo.");}return new CreatorSession.Result(true,"Moví solo esa acción y conservé el resto.\n"+SceneAudit.report(d),d);}}
  if(base!=null&&text.matches("(?:mostrame|ver|revisa|comproba) (?:el plan|la escena|el resultado)|que vas a hacer|que entendiste"))return CreatorSession.say(SceneAudit.report(base));
  if(base!=null&&text.matches("hacelo despues|hace eso despues|primero eso|cambia el orden"))return CreatorSession.say("¿Qué acción querés mover y después de cuál? Puedo conservar el resto del plan.");
  if(base!=null){Matcher view=Pattern.compile("(?:mostra|pone|ponelo|hace)(?: a)? (.+?) (?:de |mirando de )?(frente|perfil|espalda|espaldas|hacia la izquierda|hacia la derecha)").matcher(text);
   if(view.matches()){SceneDraft d=base.copy();SceneDraft.Actor a=resolve(d,view.group(1),false);if(a==null)return CreatorSession.say("¿Qué personaje querés girar? Nombralo para conservar a los demás.");if(!a.reference.isEmpty())return CreatorSession.say("La referencia importada se conserva completa; no puedo inventar su espalda o perfil.");String v=view.group(2);a.view=v.startsWith("espald")?"back":v.equals("frente")?"front":v.contains("izquierda")?"left":"right";d.focused=a.name;return new CreatorSession.Result(true,"Conservé el diseño y cambié la vista de "+a.display()+".",d);}
  }
  // Leave the door planner and existing appearance edits to their own handlers.
  if(text.contains("puerta")||text.matches("(?:agrega|crea|dibuja|disena).*llamad[oa].*"))return null;
  boolean grounded=false;if(base!=null){Matcher ref=Pattern.compile("^((?:el|la) que (?:(?:entro|llego|aparecio)|(?:tiene|sostiene|recibio) la caja))\\s+(.+)$").matcher(text);if(ref.matches()){SceneDraft.Actor actor=SceneResolver.special(base,ref.group(1));if(actor==null)return CreatorSession.say("No pude identificar ese personaje; nombralo para conservar la escena.");text=actor.name+" "+ref.group(2);grounded=true;}}
  Matcher scan=VERB.matcher(text);int count=0;while(scan.find())count++;
  boolean interaction=Pattern.compile("\\bbes(?:a|e|ar|an|en|ando|alo|ala|ale)\\b").matcher(text).find()||text.matches("(?s).*\\b(?:entrega|entregue|entregar|entregandole|pasa|pase|pasar|cede|ceder|regala|regale|regalar|da|dar|recibe|reciba)\\b.*");
  if(!force&&!grounded&&count<2&&!(base!=null&&count==1&&text.matches("(?:el que|la que|el otro|la otra|ella|el protagonista|este|esta).*"))&&!interaction&&!text.matches("(?s).*(?:se miren|se miran|se acercan|se acerquen).*"))return null;
  String normalized=text.replaceAll("^(?:haceme|hace|crea|dibuja|arma|quiero|necesito)(?: una? (?:animacion|video|escena))?(?: donde| en la que| que)? ","").replaceAll("^(?:ahora |primero )","").replaceAll("(?:y )?(?:conserva|mantene|sin cambiar) .*$","").trim();
  SceneDraft d=base==null?new SceneDraft():base.copy();boolean replace=!append&&(count>=2&&!interaction||text.contains("mientras")||text.startsWith("primero")||text.startsWith("arma"));
  if(replace){d.dialogue.clear();d.actions.clear();d.actors.clear();d.slots.clear();d.targets.clear();d.recipients.clear();for(SceneDraft.Prop p:d.props)p.holder="";}
  Matcher separators=Pattern.compile("\\s*(?:,?\\s+y\\s+despues|,?\\s+despues|,?\\s+mientras|,?\\s+y|,)\\s+").matcher(normalized);
  List<String> clauses=new ArrayList<>(),links=new ArrayList<>();int start=0;String link="";while(separators.find()){clauses.add(normalized.substring(start,separators.start()).trim());links.add(link);link=separators.group().contains("mientras")?"parallel":"after";start=separators.end();}clauses.add(normalized.substring(start).trim());links.add(link);
  int slot=-1;for(int i=0;i<d.actions.size();i++)slot=Math.max(slot,d.slot(i));SceneDraft.Actor previous=d.get(d.focused);int recognized=0;
  for(int ci=0;ci<clauses.size();ci++){
   String clause=clauses.get(ci).replaceAll("^(?:que |ahora |primero )","").trim();if(clause.isEmpty())continue;Matcher vm=VERB.matcher(clause);if(!vm.find())return CreatorSession.say("No entendí esta parte: «"+clause+"». Decime el personaje y una acción; conservé la escena anterior.");String verb=vm.group(),prefix=clause.substring(0,vm.start()),tail=clause.substring(vm.end());if(vm.find())return CreatorSession.say("Separá esas acciones con ‘y después’ o ‘mientras’ para definir el orden. Conservé la escena anterior.");
   if(clause.matches("(?s).*(?:\\bno\\b|deja de|sin hacer).*"))return CreatorSession.say("¿Querés quitar una acción? Decime cuál; conservé el plan anterior.");
   boolean all=clause.matches("(?s).*\\b(?:todos|los tres|los cuatro)\\b.*");boolean both=clause.matches("(?s).*\\b(?:ambos|los dos|se miren|se miran|se acerquen|se acercan)\\b.*");
   slot=links.get(ci).equals("parallel")?Math.max(0,slot):slot+1;if(slot>=SceneTiming.MAX_BEATS)return CreatorSession.say("La secuencia admite treinta y dos tiempos; dividila en dos escenas.");
   String action=action(verb,clause);
   if((both||all)&&action.equals("besa")){if(d.cast.size()!=2)return CreatorSession.say("Nombrá los dos personajes del beso.");SceneDraft.Actor a=d.cast.get(0),b=d.cast.get(1);SceneDirector.step(d,a,"acercaactor",slot,"",b.name);slot++;if(slot>=SceneTiming.MAX_BEATS)return CreatorSession.say("No queda tiempo para el beso.");SceneDirector.step(d,a,"besa",slot,"",b.name);SceneDirector.step(d,b,"recibebeso",slot,"",a.name);recognized++;continue;}
   if(both||all){if((!all&&d.cast.size()!=2)||!action.matches("mira|sorprende|acercaactor|saluda|habla|besa")||(all&&action.matches("mira|acercaactor")))return CreatorSession.say("Para esa interacción nombrá a los dos personajes.");for(SceneDraft.Actor a:d.cast){SceneDraft.Actor other=d.cast.get(a==d.cast.get(0)?1:0);SceneDirector.step(d,a,action,slot,"",action.matches("sorprende|saluda|habla")?"":other.name);}recognized++;continue;}
   int castBefore=d.cast.size();SceneDraft.Actor a=resolve(d,prefix,true);if(a==null&&prefix.replaceAll("\\b(?:el|la|un|una|que|le|se)\\b","").trim().isEmpty()&&action.matches("entraizq|entrader"))a=resolve(d,tail,true);
   if(a==null&&prefix.matches("(?:el otro|la otra|el otro le|la otra le).*"))a=other(d,previous);
   if(a==null&&prefix.replaceAll("\\b(?:el|la|un|una|que|le|se|hacelo|hacela)\\b","").trim().isEmpty())a=previous;
   if(a==null)return CreatorSession.say("¿Qué personaje debe hacer «"+verb+"»? Usá su nombre; conservé la escena anterior.");
   if(d.cast.size()>castBefore&&action.matches("entraizq|entrader")){float destination=action.equals("entrader")?84:16;boolean free=true;for(SceneDraft.Actor other:d.cast)if(other!=a&&Math.abs(other.x-destination)<22)free=false;if(free)a.x=destination;}
   String target="",recipient="";
   if(action.equals("besa")){SceneDraft.Actor to=resolve(d,tail,true);if(to==null&&tail.trim().isEmpty())to=other(d,a);if(to==null||to==a)return CreatorSession.say("¿A quién debe besar "+a.display()+"? Nombrá otro personaje; conservé la escena.");SceneDirector.step(d,a,"acercaactor",slot,"",to.name);slot++;if(slot>=SceneTiming.MAX_BEATS)return CreatorSession.say("No queda tiempo para el beso; dividí la escena.");SceneDirector.step(d,a,"besa",slot,"",to.name);SceneDirector.step(d,to,"recibebeso",slot,"",a.name);
   }else if(action.matches("entrega|recibe")){
    Matcher obj=Pattern.compile("\\b(caja|lampara)\\b").matcher(tail);if(!obj.find())return CreatorSession.say("Puedo entregar una caja o una lámpara. Decime cuál y a quién.");SceneDraft.Prop existing=null;for(SceneDraft.Prop candidate:d.props)if(candidate.type.equals(obj.group(1)))existing=candidate;boolean newObject=existing==null;SceneDraft.Prop p=SceneDirector.object(d,obj.group(1));target=p.id;String destination=tail.substring(obj.end()).replaceAll("^\\s*(?:a|al|para)\\s+","");SceneDraft.Actor receiver=resolve(d,destination,false);if(receiver==null&&destination.trim().isEmpty())receiver=other(d,a);if(receiver==null||receiver==a)return CreatorSession.say("¿Quién recibe el objeto? Nombrá un personaje distinto de "+a.display()+".");
    if(action.equals("recibe"))return CreatorSession.say("Decime quién entrega el objeto a "+a.display()+" para preparar ambos movimientos.");
    recipient=receiver.name;int approachSlot=slot;
    // Add a physical approach only when a preceding approach is not already present.
    if(d.actions.isEmpty()||!d.actions.get(d.actions.size()-1).equals("acercaactor")){SceneDirector.step(d,a,"acercaactor",approachSlot,"",receiver.name);slot++;}
    if(slot>=SceneTiming.MAX_BEATS)return CreatorSession.say("No queda tiempo para la entrega; dividí la escena.");
    ObjectTransfer.State before=ObjectTransfer.at(d,p.id,Math.max(0,slot-.01f)/Math.max(1,slot+1),slot+1);if(before.owner().isEmpty()){if(!newObject)return CreatorSession.say("Ese objeto no tiene portador. Indicá quién lo sostiene antes de entregarlo; conservé la escena anterior.");p.holder=a.name;}else if(!before.owner().equals(a.name))return CreatorSession.say("Ese objeto lo sostiene otro personaje. Conservé la escena anterior.");
    SceneDirector.step(d,a,"entrega",slot,target,recipient);SceneDirector.step(d,receiver,"recibe",slot,target,a.name);
   }else{
    if(action.matches("mira|acercaactor|habla")){SceneDraft.Actor to=resolve(d,tail,action.equals("habla"));if(to==null)to=other(d,a);if(to==a)to=null;if(to==null&&!action.equals("habla"))return CreatorSession.say("¿A quién debe "+verb+" "+a.display()+"?");if(to!=null)recipient=to.name;}
    SceneDirector.step(d,a,action,slot,target,recipient);if(action.equals("habla")&&!recipient.isEmpty())SceneDirector.step(d,d.get(recipient),"habla",slot,"",a.name);
   }
   recognized++;previous=a;d.focused=a.name;
  }
  if(recognized==0)return null;if(d.actions.size()>SceneTiming.MAX_ACTIONS)return CreatorSession.say("El plan supera sesenta y cuatro acciones; dividilo en dos escenas. Conservé el anterior.");
  Matcher place=Pattern.compile("\\ben (?:el|la|un|una) (bosque|parque|playa|ciudad|montana|pasillo)\\b").matcher(text);String setting="";while(place.find()){if(!setting.isEmpty()&&!setting.equals(place.group(1)))return CreatorSession.say("Nombrá un solo fondo por escena; conservé la anterior.");setting=place.group(1);}if(!setting.isEmpty())d.setting=setting;
  String consistency=SceneConsistency.problem(d);if(consistency!=null)return CreatorSession.say("No apliqué el plan: "+consistency+" Conservé la escena anterior.");d.camera="wide";String coverage=PlanAudit.verify(input,base,d);return new CreatorSession.Result(true,"Preparé "+recognized+" partes del pedido.\n"+coverage+"\n"+SceneAudit.report(d),d);
 }
 static String action(String verb,String clause){return verb.startsWith("bes")?"besa":verb.matches("entreg.*|ced.*|regal.*|pasa|pase|pasar|da|dar")?"entrega":verb.matches("habl.*|convers.*|charl.*")?"habla":verb.startsWith("bail")?"baila":verb.startsWith("camin")?"camina":verb.startsWith("salt")?"salta":verb.startsWith("salud")?"saluda":verb.matches("entr.*|lleg.*|apare.*")?(clause.contains("izquierda")?"entraizq":"entrader"):verb.matches("sorprend.*|reaccion.*")?"sorprende":verb.startsWith("mir")?"mira":verb.startsWith("acerc")?"acercaactor":verb.startsWith("recib")?"recibe":"entrega";}
 static int findStep(SceneDraft d,String clause){Matcher v=VERB.matcher(clause);if(!v.find())return -1;SceneDraft.Actor actor=resolve(d,clause,false);if(actor==null)return -1;String word=v.group(),stem=word.substring(0,Math.min(4,word.length()));for(int i=d.actions.size()-1;i>=0;i--)if(d.cast.get(d.actors.get(i))==actor&&(word.startsWith("entreg")?d.actions.get(i).equals("entrega"):word.startsWith("entr")?d.actions.get(i).matches("entraizq|entrader"):d.actions.get(i).startsWith(stem)))return i;return -1;}
 static SceneDraft.Actor other(SceneDraft d,SceneDraft.Actor a){if(d.cast.size()!=2||a==null)return null;return d.cast.get(0)==a?d.cast.get(1):d.cast.get(0);}
 static SceneDraft.Actor resolve(SceneDraft d,String raw,boolean create){String text=raw.trim();if(text.isEmpty())return null;SceneDraft.Actor found=null;
  // Explicit labels take precedence over type names; duplicate types require a name.
  for(SceneDraft.Actor a:d.cast)if(SceneDirector.word(text,a.label)||(!a.name.equals(a.kind)&&SceneDirector.word(text,a.name))){if(found!=null&&found!=a)return null;found=a;}if(found!=null)return found;SceneDraft.Actor special=SceneResolver.special(d,text);if(special!=null)return special;
  for(String kind:SceneDirector.TYPES.split("\\|"))if(SceneDirector.word(text,kind)){SceneDraft.Actor a=d.reference(kind);if(a==null){int n=0;for(SceneDraft.Actor existing:d.cast)if(existing.kind.equals(kind))n++;if(n>0||!create||d.cast.size()>=4)return null;a=new SceneDraft.Actor(SceneDirector.unique(d,kind),SceneConversation.freePosition(d));a.kind=kind;d.cast.add(a);}if(found!=null&&found!=a)return null;found=a;}
  return found;
 }
}
