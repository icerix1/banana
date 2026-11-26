package com.bananalab.lite;
import java.util.regex.*;
final class SceneContinuation{
 static CreatorSession.Result apply(String input,SceneDraft base)throws Exception{
  String q=CreationRequest.normalize(input);Matcher replace=Pattern.compile("(?:reemplaza|reinicia|rehace) (?:la secuencia|las acciones)(?: por| con)? (.+)").matcher(q);if(replace.matches()){if(base==null)return CreatorSession.say("Primero creá los personajes de la escena.");SceneDraft empty=base.copy();empty.dialogue.clear();empty.actions.clear();empty.actors.clear();empty.slots.clear();empty.targets.clear();empty.recipients.clear();CreatorSession.Result planned=ScenePlanner.plan(replace.group(1),empty,true,true);if(planned.commit==null)return planned;return new CreatorSession.Result(true,"Reemplacé solamente la secuencia. Conservé los diseños y objetos.\n"+planned.commit.describe(),planned.commit);}
  Matcher m=Pattern.compile("(?:(?:continua|segui|sigue)(?: la escena| la historia| la secuencia)?(?: con| haciendo que)?|agrega al final|y despues|despues) (.+)").matcher(q);if(!m.matches()||q.startsWith("despues de "))return null;if(base==null)return CreatorSession.say("Primero creá una escena para continuarla.");String body=m.group(1).replaceFirst("^(?:que |hace que )","");if(!ScenePlanner.VERB.matcher(body).find())return CreatorSession.say("Decime qué personaje y acción agregar al final; conservé la secuencia anterior.");
  CreatorSession.Result result=ScenePlanner.plan(body,base,true,true);if(result.commit==null)return result;PlanAudit.verifyExtension(body,base,result.commit);return new CreatorSession.Result(true,"Continué la escena conservando todos los pasos anteriores.\n"+result.commit.describe(),result.commit);
 }
}
