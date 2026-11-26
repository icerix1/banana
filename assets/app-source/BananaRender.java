package com.bananalab.lite;
import android.content.Context;import java.io.IOException;
/** Identical scene preparation for inline preview and all export formats. */
final class BananaRender{
 static Scene prepare(Context c,SceneDraft value,boolean three)throws Exception{SceneDraft snapshot=value.copy();SceneConsistency.require(snapshot);SceneAudit.report(snapshot);NeuralSprites.Batch batch=BananaPixel.prepare(c,snapshot,0);for(SceneDraft.Actor actor:snapshot.cast)if(!batch.images.containsKey(actor.name))throw new IOException("Falta el personaje "+actor.display());for(SceneDraft.Prop prop:snapshot.props)if(!batch.objects.containsKey(prop.id))throw new IOException("Falta el objeto "+prop.type);Scene scene=new Scene(snapshot);scene.setSequence(three);scene.setNeural(batch);return scene;}
}
