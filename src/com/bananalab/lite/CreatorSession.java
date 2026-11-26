package com.bananalab.lite;
import java.util.*;import java.util.regex.*;import org.json.*;
/** Planificacion local explicita. Un borrador no modifica la escena publicada. */
final class CreatorSession {
 SceneDraft pending;String tone="",lastTask="";boolean composing=false;SceneDraft activeBase;String clarifyTemplate="",clarifyScene="",proposal="",proposalScene="",takeBackup="";boolean takeMode=false;final List<String> shots=new ArrayList<>();final List<String> undo=new ArrayList<>(),redo=new ArrayList<>();
 static final class Result{
  final boolean handled;final String text;final SceneDraft commit;String studioId="";boolean importReference=false;
  Result(boolean h,String t,SceneDraft d){handled=h;text=t;commit=d;}
 }
 static Result say(String s){return new Result(true,s,null);}
 String json()throws Exception{
  JSONObject o=new JSONObject().put("clarifyTemplate",clarifyTemplate).put("clarifyScene",clarifyScene).put("proposal",proposal).put("proposalScene",proposalScene).put("takeBackup",takeBackup).put("takeMode",takeMode).put("shots",new JSONArray(shots)).put("lastTask",lastTask).put("tone",tone).put("undo",new JSONArray(undo)).put("redo",new JSONArray(redo));
  if(pending!=null)o.put("pending",pending.json());return o.toString();
 }
 static CreatorSession read(String data)throws Exception{
  CreatorSession c=new CreatorSession();if(data.isEmpty())return c;JSONObject o=new JSONObject(data);
  c.clarifyTemplate=o.optString("clarifyTemplate","");c.clarifyScene=o.optString("clarifyScene","");c.proposal=o.optString("proposal","");c.proposalScene=o.optString("proposalScene","");c.takeBackup=o.optString("takeBackup","");c.takeMode=o.optBoolean("takeMode",false);JSONArray takes=o.optJSONArray("shots");if(takes!=null){if(takes.length()>7)throw new Exception("Demasiadas tomas");for(int i=0;i<takes.length();i++){String take=takes.getString(i);SceneDraft.read(take);c.shots.add(take);}}c.lastTask=o.optString("lastTask","");c.tone=o.optString("tone");if(o.has("pending"))c.pending=SceneDraft.read(o.getJSONObject("pending").toString());
  for(String key:new String[]{"undo","redo"}){JSONArray a=o.optJSONArray(key);if(a!=null)for(int i=Math.max(0,a.length()-20);i<a.length();i++){String v=a.getString(i);if(!v.isEmpty())SceneDraft.read(v);(key.equals("undo")?c.undo:c.redo).add(v);}}
  return c;
 }
 void remember()throws Exception{undo.add(pending==null?"":pending.json().toString());while(undo.size()>20)undo.remove(0);redo.clear();}
 Result history(boolean forward)throws Exception{
  List<String> src=forward?redo:undo,dst=forward?undo:redo;if(src.isEmpty())return say("No hay cambios del borrador para "+(forward?"rehacer.":"deshacer."));
  dst.add(pending==null?"":pending.json().toString());String s=src.remove(src.size()-1);pending=s.isEmpty()?null:SceneDraft.read(s);
  return say("Borrador actualizado. "+(pending==null?"No queda un borrador pendiente.":pending.describe()));
 }
 static boolean contains(String text,String word){return Pattern.compile("\\b"+Pattern.quote(word)+"\\b").matcher(text).find();}
 Result apply(Semilla model,String input,SceneDraft actual)throws Exception{
  SceneDraft initial=pending!=null?pending:actual;String answer=ChatLanguage.normalize(input);
  if(answer.equals("aplica esa idea")||answer.equals("hace esa propuesta")){if(proposal.isEmpty())return say("Primero pedime una idea para el proyecto.");String now=initial==null?"":initial.json().toString();if(!now.equals(proposalScene)){proposal="";return say("El proyecto cambió desde esa propuesta. Pedime una idea nueva para conservar tus cambios.");}String command=proposal;proposal="";return apply(model,command,actual);}
  if(!clarifyTemplate.isEmpty()&&initial!=null){SceneDraft.Actor a=ScenePlanner.resolve(initial,answer,false);boolean exact=a!=null&&(answer.equals(a.label)||answer.equals(a.name)||answer.equals(a.kind)||answer.matches("el (?:primero|segundo|tercero|cuarto|anterior)"));if(exact){if(!initial.json().toString().equals(clarifyScene)){clarifyTemplate="";return say("La escena cambió desde la pregunta. Repetí el cambio para no editar una versión distinta.");}String retry=clarifyTemplate.replace("__actor__",a.name);clarifyTemplate="";clarifyScene="";return apply(model,retry,actual);}}
  String normalized=ConversationAssist.natural(input,initial,this);if(normalized.equals("__ask_actor__"))return say("¿Qué personaje querés que haga esa acción? Respondé con su nombre; conservé la escena.");input=normalized;
  String text=CreationRequest.normalize(input).replaceAll("\\b(?:poneme|ponele)\\b","pone").replaceAll("\\bdibjua\\b","dibuja"),q=text;
  if(q.matches("descarta el borrador|descartar borrador|cancela el borrador")){remember();pending=null;return say("Descarté el borrador. El dibujo publicado se conserva.");}
  if(q.matches("ver borrador|mostrame el borrador|resumi el borrador|que estamos preparando"))return say(pending==null?"No hay un borrador pendiente.":pending.describe()+"\nTono: "+tone+"\nTodavía no se publicó.");
  if((pending!=null||!undo.isEmpty()||!redo.isEmpty())&&q.matches("deshacer|deshace|volver atras|deshace el cambio|dejalo como antes"))return history(false);
  if((pending!=null||!undo.isEmpty()||!redo.isEmpty())&&q.matches("rehacer|rehace|rehace el cambio"))return history(true);
  boolean renderNow=CreationRequest.render(text);
  if(text.equals("dibuja"))return say("¿Qué querés animar? Por ejemplo: haceme una animación de una banana bailando.");
  boolean defer=q.matches("(?s).*(?:no (?:lo |los |la |las )?dibujes|sin dibujar|solo planear|primero planear|planeemos|planifiquemos).*");
  boolean idea=q.startsWith("quiero una serie")||q.startsWith("quiero una historia")||q.startsWith("planea una escena")||q.startsWith("planeemos una escena");
  SceneDraft base=pending!=null?pending:actual;activeBase=base;
  if(q.matches("que hiciste|que cambiaste|ultimo pedido|revisa el ultimo pedido"))return say(lastTask.isEmpty()?"Todavía no registré un pedido por pasos en este proyecto.":lastTask);
  Result take=SceneTakes.apply(input,base,this);if(take!=null)return take;
  Result custom=CustomCharacter.apply(input,base);if(custom!=null){if(custom.commit==null)return custom;return update(custom.commit,defer||pending!=null,custom.text);}
  Result speech=DialogueCommands.apply(input,base);if(speech!=null){if(speech.commit==null)return speech;return update(speech.commit,defer||pending!=null,speech.text);}
  Result flow=composing?null:TaskFlow.apply(model,input,base,this,defer);if(flow!=null)return flow;
  SceneContext.Result context=SceneContext.rewrite(input,base);if(context.error!=null){if(context.unresolved!=null){clarifyTemplate=context.text.replace(context.unresolved,"__actor__");clarifyScene=base.json().toString();}return say(context.error);}if(context.selection!=null)return update(context.selection,pending!=null||defer,"Seleccioné a "+context.selection.get(context.selection.focused).display()+".");String command=context.text;
  Result directed=SceneContinuation.apply(command,base);if(directed==null)directed=ScopedAppearance.apply(command,base);if(directed==null)directed=ProjectEdits.apply(command,base);if(directed==null)directed=SceneSettings.apply(command,base);if(directed==null)directed=ScenePlanner.apply(command,base);if(directed==null)directed=SceneDirector.apply(command,base);if(directed!=null){if(directed.commit==null)return directed;String problem=BananaPixel.limitation(directed.commit);if(problem!=null)return say(problem);String issue=SceneConsistency.problem(directed.commit);if(issue!=null)return say("No apliqué el cambio: "+issue+" La escena anterior se conserva.");if(!composing)lastTask=TaskReceipt.describe(base,directed.commit,new String[]{input});Result updated=update(directed.commit,defer||pending!=null,directed.text);updated.studioId=directed.studioId;updated.importReference=directed.importReference;return updated;}

  text=command;
  if(q.matches("como se llamaba el protagonista|como se llama el protagonista|quien es el protagonista|como se llamaba el personaje|como se llama")){
   if(base==null||base.cast.isEmpty())return say("Todavía no elegimos un protagonista.");
   SceneDraft.Actor a=q.equals("como se llama")&&base.get(base.focused)!=null?base.get(base.focused):base.cast.get(0);return say(a.label.isEmpty()?"El protagonista es "+a.name+"; todavía no tiene nombre.":"El protagonista se llama "+a.label+" y es "+a.name+".");
  }
  if(q.matches("quienes son los personajes|que personajes elegimos"))return say(base==null?"Todavía no elegimos personajes.":base.describe());
  Matcher named=Pattern.compile("(?:que se llame|se llama|llamalo|llamala|ponele de nombre) ([a-z]{2,20}(?: [a-z]{2,9})?)").matcher(q);
  if(base!=null&&named.matches()){
   SceneDraft d=base.copy();SceneDraft.Actor a=d.get(d.focused);if(a==null&&!d.cast.isEmpty())a=d.cast.get(0);
   if(a==null)return say("Primero agreguemos un personaje.");String label=named.group(1);if(PixelGenerator.type(label)>=0)return say("Elegí un nombre distinto del tipo de personaje, por ejemplo Tony.");
   for(SceneDraft.Actor other:d.cast)if(other!=a&&other.label.equals(label))return say("Ese nombre ya pertenece a otro personaje.");
   a.label=label;return update(d,pending!=null||defer,"Guardé el nombre "+label+".");
  }
  if(base!=null){
   // Resolve aliases through exact word boundaries. Rendering retains the original type.
   for(SceneDraft.Actor a:base.cast)if(!a.label.isEmpty())text=text.replaceAll("\\b"+Pattern.quote(a.label)+"\\b",Matcher.quoteReplacement(a.name));
   if(text.contains("el otro")||text.contains("la otra")){
    if(base.cast.size()!=2||base.focused.isEmpty())return say("¿A cuál personaje te referís? Nombralo para evitar cambiar otro.");
    String other=base.cast.get(0).name.equals(base.focused)?base.cast.get(1).name:base.cast.get(0).name;
    text=text.replace("el otro",other).replace("la otra",other);
    if(text.equals(other)||text.equals("no "+other)){SceneDraft selected=base.copy();selected.focused=other;return update(selected,pending!=null,"Seleccioné a "+other+". Decime qué cambio querés hacerle.");}
   }
  }
  if(base!=null){
   Matcher reveal=Pattern.compile("(?:hace que |que )?(?:el |la )?([a-z]+) aparezca al final").matcher(text);
   if(reveal.matches()){
    SceneDraft d=base.copy();SceneDraft.Actor actor=d.get(reveal.group(1));if(actor==null)return say("¿Qué personaje querés revelar al final?");
    List<String> actionNames=new ArrayList<>();for(int index:d.actors)actionNames.add(d.cast.get(index).name);
    d.cast.remove(actor);d.cast.add(actor);for(int i=0;i<d.actors.size();i++)d.actors.set(i,d.cast.indexOf(d.get(actionNames.get(i))));d.focused=actor.name;
    return update(d,pending!=null,"Preparé la revelación de "+(actor.label.isEmpty()?actor.name:actor.label)+". Activá ‘Tres tomas’ en Proyecto → Opciones para verla.");
   }
   if(text.matches("quita el ultimo movimiento|borra el ultimo movimiento")){
    if(base.actions.isEmpty())return say("No hay movimientos para quitar.");SceneDraft d=base.copy();d.actions.remove(d.actions.size()-1);d.actors.remove(d.actors.size()-1);if(!d.slots.isEmpty())d.slots.remove(d.slots.size()-1);if(d.targets.size()>d.actions.size())d.targets.remove(d.targets.size()-1);if(d.recipients.size()>d.actions.size())d.recipients.remove(d.recipients.size()-1);return update(d,pending!=null,"Quité el último movimiento.");
   }
  }
  text=text.replaceAll("(?:y )?(?:conserva|mantene|sin cambiar)(?: a)? .*$","").trim();
  if(base!=null&&text.matches("ordena (?:la escena|los personajes)|acomoda los personajes")){
   SceneDraft d=base.copy();for(int i=0;i<d.cast.size();i++){d.cast.get(i).x=(i+1)*100f/(d.cast.size()+1);d.cast.get(i).y=67;d.cast.get(i).relation="";d.cast.get(i).behind=false;}
   return update(d,pending!=null,"Ordené los personajes para separarlos en el encuadre.");
  }
  Result timing=TimelineCommands.apply(model,text,base);if(timing!=null){if(timing.commit==null)return timing;return update(timing.commit,pending!=null,timing.text);}
  boolean publish=q.matches("(?s)(?:ahora |dale )?(?:dibujalos|dibujalo|dibuja el borrador|publica el borrador|genera el borrador|crear el borrador)(?: .*)?");
  if(publish&&pending==null)return say("No hay un borrador para publicar. Podés preparar uno diciendo ‘quiero una serie con una banana’.");
  if(publish&&pending!=null){
   SceneDraft d=pending.copy();Matcher place=Pattern.compile("\\b(parque|plaza|bosque|playa|ciudad|montana|pasillo|casa|interior)\\b").matcher(text);
   if(place.find())d.setting=place.group(1).replace("plaza","parque");
   String limit=BananaPixel.limitation(d);if(limit!=null)return say(limit);
   String publishIssue=SceneConsistency.problem(d);if(publishIssue!=null)return say("No publiqué el borrador: "+publishIssue);pending=null;undo.clear();redo.clear();return new Result(true,"Publiqué el borrador.\n"+d.describe()+"\nEl resultado aparece en esta conversación.",d);
  }
  if(idea){
   Matcher start=Pattern.compile("\\bcon (.+)").matcher(text);
   if(!start.find())return say("¿Qué personajes querés para el borrador? Por ejemplo: quiero una serie de terror con una banana.");
   text="dibuja "+start.group(1);defer=true;
  }
  text=text.replaceAll("(?:pero )?(?:todavia )?no (?:lo |los |la |las )?dibujes(?: todavia)?","").replace("sin dibujar","").trim();
  if(defer&&text.isEmpty())return base==null?say("¿Qué personajes querés preparar?"):update(base.copy(),true,"Preparé el borrador.");
  if(text.matches("(?s).*\\b(?:besa|bese|besar|besan|besen|beso)\\b.*"))return say("Para el beso, nombrá a los dos personajes: ‘el zorro besa a la banana’. Conservé la escena anterior.");
  SceneConversation.Result edit=SceneConversation.apply(model,text,base);
  if(!edit.handled)return new Result(false,"",null);
  if(edit.draft==null)return say(edit.reply);
  String limit=BananaPixel.limitation(edit.draft);if(limit!=null)return say(limit+". Conservé el estado anterior.");
  if(idea&&q.contains("terror"))tone="terror";
  if(renderNow&&!defer&&!idea){String renderIssue=SceneConsistency.problem(edit.draft);if(renderIssue!=null)return say("No apliqué el plan: "+renderIssue);SceneContext.focus(base,edit.draft);pending=null;undo.clear();redo.clear();return new Result(true,"Preparé la escena. El dibujo y la animación aparecen en el resultado de este mensaje.",edit.draft);}
  return update(edit.draft,defer||pending!=null,edit.reply);
 }
 Result update(SceneDraft d,boolean draft,String answer)throws Exception{return update(d,draft,answer,true);}
 Result update(SceneDraft d,boolean draft,String answer,boolean trackFocus)throws Exception{
  try{SceneTiming.fit(d);}catch(Exception timing){return say(timing.getMessage()+" Conservé la escena anterior.");}String issue=SceneConsistency.problem(d);if(issue!=null)return say("No apliqué el cambio: "+issue+" La escena anterior se conserva.");if(trackFocus)SceneContext.focus(activeBase,d);
  if(draft){remember();pending=d;return say("Borrador guardado, sin publicar.\n"+d.describe()+"\nDecí ‘ahora dibujalos’ cuando esté listo.");}
  return new Result(true,answer,d);
 }
}
