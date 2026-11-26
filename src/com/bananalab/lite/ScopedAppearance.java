package com.bananalab.lite;
import java.util.*;import java.util.regex.*;
final class ScopedAppearance{
 static final String PARTS="sombrero|corona|lentes|sobretodo";
 static CreatorSession.Result apply(String input,SceneDraft base)throws Exception{
  String q=SceneContext.clean(CreationRequest.normalize(input)).replaceAll("\\b(?:anteojos|gafas)\\b","lentes").replaceAll("\\b(?:abrigo|gabardina)\\b","sobretodo").replaceFirst("^cambiale el ","cambia su ").replaceAll("\\b(?:solo|solamente)\\b\\s*","");
  Matcher change=Pattern.compile("(?:cambia|reemplaza|sustitui) (?:el |la |los |su )?("+PARTS+")(?: de (.+?))? por (?:un |una |unos |el |la |los )?("+PARTS+")").matcher(q),remove=Pattern.compile("(?:quita|saca|elimina) (?:el |la |los |su )?("+PARTS+")(?: de (.+))?").matcher(q);
  boolean changing=change.matches(),removing=remove.matches();boolean targeted=q.matches("(?:cambia|reemplaza|sustitui|quita|saca|elimina) (?:el |la |los |su )?(?:"+PARTS+").*");if(!changing&&!removing&&!targeted)return null;if(base==null)return CreatorSession.say("Primero creá una escena para editar un accesorio.");if(!changing&&!removing)return CreatorSession.say("¿Qué accesorio querés cambiar y por cuál? Puedo cambiar sombrero, corona, lentes o sobretodo. No cambio el color del cuerpo para simular un color de sombrero.");
  Matcher m=changing?change:remove;String who=m.group(2);SceneDraft.Actor original=who==null?base.get(base.focused):ScenePlanner.resolve(base,who,false);if(original==null)return CreatorSession.say("Nombrá al personaje para cambiar solamente su accesorio.");if(!original.reference.isEmpty())return CreatorSession.say("No puedo separar ese accesorio de una referencia importada; conservé la imagen.");
  SceneDraft d=base.copy();SceneDraft.Actor a=d.get(original.name);String old=m.group(1),next=changing?m.group(3):"";String look=a.look;if(SceneDirector.word(look,"detective")){look=look.replaceAll("\\bdetective\\b","");if(!SceneDirector.word(look,"sombrero"))look+=" sombrero";if(!SceneDirector.word(look,"sobretodo"))look+=" sobretodo";}
  if(!SceneDirector.word(look,old))return CreatorSession.say(original.display()+" no tiene ese accesorio. Conservé su diseño.");look=look.replaceAll("\\b"+old+"\\b","");if(next.matches("sombrero|corona"))look=look.replaceAll("\\b(?:sombrero|corona)\\b","");if(!next.isEmpty()&&!SceneDirector.word(look,next))look+=" "+next;a.look=look.trim().replaceAll("\\s+"," ");d.focused=a.name;
  return new CreatorSession.Result(true,"Cambié solamente el accesorio de "+a.display()+". Conservé color, tamaño, acciones, escenario y los demás personajes.",d);
 }
}
