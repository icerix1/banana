package com.bananalab.lite;
final class SceneTiming{
 static final int MAX_BEATS=32,MAX_ACTIONS=64,MAX_SECONDS=120,MAX_SERIES_SECONDS=300;
 static int frames(SceneDraft d){return Math.max(48,Math.round(ObjectTransfer.beats(d)*d.beatSeconds*VideoExporter.FPS));}
 static float seconds(SceneDraft d){return frames(d)/(float)VideoExporter.FPS;}
 static void fit(SceneDraft d)throws Exception{int beats=ObjectTransfer.beats(d);d.beatSeconds=Math.max(d.beatSeconds,3f/beats);float needed=0;for(SceneDraft.Line l:d.dialogue){SceneDraft.Actor a=d.get(l.actor);if(a!=null)needed=Math.max(needed,VoiceSynth.duration(l.text,a.voice)+.15f);}if(needed>d.beatSeconds+.001f){if(d.fixedDuration)throw new IllegalArgumentException("El diálogo no entra en la duración elegida. Aumentá el tiempo o acortá la frase.");d.beatSeconds=needed;}if(seconds(d)>MAX_SECONDS+.001f)throw new IllegalArgumentException("La escena supera120 segundos. Dividila en tomas.");}
}
