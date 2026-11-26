package com.bananalab.lite;
import java.util.*;
final class OwnPlan {
 final List<String> names=new ArrayList<>(),looks=new ArrayList<>(),actions=new ArrayList<>();
 final List<Integer> actors=new ArrayList<>(),slots=new ArrayList<>();private boolean parallelNext=false;String setting="parque";int uncertain;
 OwnPlan(Semilla model,String input){
  input=CreationRequest.normalize(input);
  List<Semilla.Token> tokens=model.analyze(Timeline.language(input));int current=-1;boolean negative=false;
  List<String> pending=new ArrayList<>();StringBuilder place=new StringBuilder();
  for(int position=0;position<tokens.size();position++){Semilla.Token token=tokens.get(position);
   String word=token.word,label=isEntity(token)?"ENTITY":Timeline.action(token.word,token.label);
   if(word.equals("mientras")){parallelNext=true;continue;}if(word.matches("despues|luego"))parallelNext=false;
   if(word.matches("parque|plaza|bosque|playa|ciudad|montana|pasillo|casa|interior|habitacion|desierto")){place.append(word.equals("plaza")?"parque":word).append(' ');continue;}
   if(word.equals("no")){negative=true;continue;}if(word.matches("y|luego|despues|[.,;!?]"))negative=false;
   if(token.confidence<.55f)uncertain++;
   if(label.equals("ENTITY")){
    if(word.matches("un|una|el|la|con|en|de|que|quiero|hace|video|escena"))continue;
    int found=names.indexOf(word);
    if(found<0&&names.size()<4){found=names.size();names.add(word);looks.add("");Knowledge.generic(word);}
    current=found;if(current>=0){for(String a:pending)add(current,a);pending.clear();}
   }else if(label.equals("LOOK")&&current>=0&&!word.matches("a|al|de|del|el|la|con|por|se|le")){looks.set(current,(looks.get(current)+" "+word).trim());}
   else if(label.equals("PLACE")){place.append(word).append(' ');}
   else if(label.matches("camina|salta|baila|habla|saluda|acerca|gira|duerme|vuela|nada|aplaude|corre|canta|espera|entraizq|entrader|saleizq|saleder|mira")&&!negative){
    boolean inverted=position+2<tokens.size()&&tokens.get(position+1).word.matches("el|la|un|una")&&isEntity(tokens.get(position+2));
    if(current>=0&&!inverted)add(current,label);else pending.add(label);
   }
  }
  if(names.isEmpty()&&place.length()>0){setting=place.toString().trim();return;}
  if(names.isEmpty())throw new IllegalArgumentException("El modelo no identifico personajes. Proba: un gato rojo camina en la playa.");
  if(actions.isEmpty())add(0,"espera");if(place.length()>0)setting=place.toString().trim();
 }
 private static boolean isEntity(Semilla.Token token){return token.word.matches("banana|pepino|gato|conejo|robot|perro|oso|zorro|persona|tomate|criatura")||token.label.equals("ENTITY");}
 private void add(int actor,String action){if(actions.size()<SceneTiming.MAX_ACTIONS){int slot=slots.isEmpty()?0:slots.get(slots.size()-1)+(parallelNext?0:1);if(parallelNext)for(int i=0;i<slots.size();i++)if(slots.get(i)==slot&&actors.get(i)==actor)slot++;actors.add(actor);actions.add(action);slots.add(slot);parallelNext=false;}}
 String text(){StringBuilder b=new StringBuilder();for(int i=0;i<names.size();i++){if(i>0)b.append(" y un ");b.append(names.get(i)).append(' ').append(looks.get(i));}return b.append("; ").append(setting).toString();}
}
