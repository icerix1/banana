package com.bananalab.lite;
import android.content.Context;import java.util.*;
/** Single orchestration point for conversation, tools and creation. No UI navigation. */
final class BananaEngine{
 static final class Result{String text,topic,mode,studioId="";boolean importReference,changed,draft;SceneDraft scene;}
 static Result run(Context c,String message,List<String[]> history,String topic,boolean useTransformer,Semilla planner)throws Exception{Result out=MobileCore.run(c,message,history,topic,useTransformer,planner);if(out!=null&&out.text!=null)ConversationFeedback.observe(c,message,out);return out;}
 static Result raw(Context c,String message,List<String[]> history,String topic,boolean useTransformer,Semilla planner)throws Exception{
  BananaChat.Reply reply;String chosen=null;String mode="conversacion",studioId="";boolean showAnimation=false,importReference=false;
    String normalized=ChatLanguage.normalize(message);
    CreatorSession creator=CreatorMemory.load(c);
    SceneLibrary.Action saved=SceneLibrary.apply(c,message,creator.pending!=null?creator.pending:SceneStore.load(c));
    if(!saved.handled)saved=CharacterArchive.apply(c,message,creator.pending!=null?creator.pending:SceneStore.load(c));
    String utility=saved.handled?saved.text:LocalMath.reply(message);
    if(saved.scene!=null){if(creator.pending!=null)utility="Publicá o descartá el borrador antes de abrir otra escena.";else{CreatorMemory.save(c,creator,saved.scene);showAnimation=true;}}
if(utility==null)utility=ProjectMemory.reply(c,message);
    if(normalized.equals("resumi mi proyecto")){SceneDraft current=creator.pending!=null?creator.pending:SceneStore.load(c);utility=(current==null?"Todavía no hay una escena guardada.":(creator.pending!=null?"Borrador sin publicar.\n":"")+SceneGuide.summary(current))+"\n\n"+(utility==null?"":utility);}
    if(utility==null)utility=SceneGuide.reply(message,creator.pending!=null?creator.pending:SceneStore.load(c));
    if(utility==null)utility=ResearchConversation.reply(c,message,topic);
    CreatorSession.Result creation=utility==null?creator.apply((planner==null?OwnBrain.load(c):planner),message,SceneStore.load(c)):new CreatorSession.Result(false,"",null);
    if(utility!=null){reply=new BananaChat.Reply(utility,topic);mode="herramienta";}
    else if(creation.handled){
     if(Thread.currentThread().isInterrupted())throw new java.io.IOException("Cancelado");
     CreatorMemory.save(c,creator,creation.commit);
     studioId=creation.studioId;importReference=creation.importReference;showAnimation=creation.commit!=null;reply=new BananaChat.Reply(creation.text,topic);mode=creator.pending!=null?"borrador":"escena";
    }else if(normalized.matches("rehacer|rehace|rehace el cambio")){
     boolean restored=SceneStore.redo(c);showAnimation=restored;reply=new BananaChat.Reply(restored?"Rehice el cambio del dibujo.":"No hay cambios para rehacer.",topic);mode="escena";
    }else if(normalized.matches("deshacer|deshace|volver atras|deshace el cambio|dejalo como antes")){
     boolean restored=SceneStore.undo(c);showAnimation=restored;reply=new BananaChat.Reply(restored?"Deshice el último cambio del dibujo.":"No hay cambios anteriores.",topic);mode="escena";
    }else{
     String dialogue=DialogueMemory.reply(c,message);if(dialogue==null)dialogue=ConversationAssist.reply(c,message,creator.pending!=null?creator.pending:SceneStore.load(c));
     if(dialogue!=null)reply=new BananaChat.Reply(dialogue,topic);
     else if(normalized.matches("(?s)^(?:busca|investiga|que es|quien es|que sabes sobre|busca informacion).*")){
      reply=BananaChat.reply(c,message,topic);mode="fuentes";
     }else if(useTransformer&&(chosen=ChatBank.reply(c,message))!=null){
      reply=new BananaChat.Reply(chosen,topic);mode="conversacion";
     }else if(useTransformer){
      String notes=ProjectMemory.related(c,message);
      java.util.List<String[]> turns=new java.util.ArrayList<>(history);
      if(!notes.isEmpty()){if(turns.size()>=3)turns.remove(0);turns.add(0,new String[]{"recorda que "+notes,"entendido ."});}
      BananaTransformer.Result generated=TransformerBrain.reply(c,message,turns);
      if(!TextQuality.acceptable(c,generated.text,message,turns)){reply=new BananaChat.Reply("No pude formar una respuesta clara. Podemos reformular la pregunta o trabajar sobre los personajes, el fondo y las acciones del proyecto.",topic);mode="clarificacion";}else reply=new BananaChat.Reply("[Texto experimental · "+generated.count+" tokens"+(generated.limited?" · limite alcanzado":"")+"]\n"+generated.text+(notes.isEmpty()?"":"\n\nTus notas relacionadas: "+notes),topic);if(!mode.equals("clarificacion"))mode="transformer";
     }else{reply=BananaChat.reply(c,message,topic);mode="fuentes";}
    }
  Result out=new Result();out.text=reply.text;out.topic=reply.topic;out.mode=mode;out.studioId=studioId;out.importReference=importReference;out.changed=showAnimation;CreatorSession state=CreatorMemory.load(c);out.draft=state.pending!=null;out.scene=out.draft?state.pending.copy():SceneStore.load(c);return out;
 }
}
