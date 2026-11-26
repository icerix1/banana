package com.bananalab.lite;
import android.content.Context;
/** Entrada unica de Banana Pixel: personajes, poses y escenario de la escena. */
final class BananaPixel {
 static String limitation(SceneDraft draft){return NeuralSprites.limitation(draft);}
 static boolean ready(SceneDraft draft,long seed){return NeuralSprites.ready(draft,seed);}
 static NeuralSprites.Batch cached(SceneDraft draft,long seed){return NeuralSprites.cached(draft,seed);}
 static NeuralSprites.Batch prepare(Context c,SceneDraft draft,long seed)throws Exception{return NeuralSprites.prepare(c,draft,seed);}
}
