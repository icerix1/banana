package com.bananalab.lite;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.media.*;import java.io.*;import java.util.*;
/** Test-only APK instrumentation. Not included in the production APK. */
public class Stage32Instrumentation extends Instrumentation{
 private void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 private void note(String s){Bundle b=new Bundle();b.putString("stream",s+"\n");sendStatus(0,b);}
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){
  Bundle result=new Bundle();
  try{Context c=getTargetContext();Semilla model=OwnBrain.load(c);Motion motion=new Motion(c.getAssets().open("motion.bin"));
   CreatorSession session=new CreatorSession();SceneDraft design=session.apply(model,"crea a Tony una banana detective con sobretodo",null).commit;
   check(design!=null,"Tony design");SceneDraft draft=session.apply(model,"ahora hacelo caminar hasta una puerta abrirla y sorprenderse al encontrar al pepino conserva su cara y su ropa",design).commit;
   check(draft!=null&&draft.props.size()==1&&draft.cast.size()==2,"door story");draft.sound=true;draft.camera="wide";
   check(draft.slot(1)==draft.slot(2),"parallel reveal");
   long start=System.nanoTime();NeuralSprites.Batch batch=NeuralSprites.prepare(c,draft,0);note("PREPARE_MS "+(System.nanoTime()-start)/1000000);
   check(batch.objects.size()==1&&batch.rigs.size()==2,"objects and cutout rigs");check(batch.images.size()==2&&batch.images.get("banana").getWidth()==128,"clear sprites");int[] bg=new int[128*128];batch.backdrop[0].background.getPixels(bg,0,128,0,0,128,128);long light=0;for(int color:bg)light+=(color>>16)&255;check(light/bg.length>20,"background premultiplication");
   Scene scene=new Scene(draft);scene.setSequence(false);scene.setNeural(batch);File dir=c.getExternalFilesDir(null);check(dir!=null,"output directory");
   Bitmap bmp=Bitmap.createBitmap(384,384,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bmp);scene.draw(canvas,384,motion,.72f);try(FileOutputStream out=new FileOutputStream(new File(dir,"stage32-frame.png"))){bmp.compress(Bitmap.CompressFormat.PNG,100,out);}bmp.recycle();
   note("RENDER_OK sprites128 background_readable");
   File video=new File(dir,"stage32-audio.mp4");VideoExporter.Monitor monitor=new VideoExporter.Monitor(){public boolean cancelled(){return false;}public void progress(int frame,int total){}};
   VideoExporter.write(video,scene,motion,scene.frames(),monitor);verify(video,2,scene.frames());note("SINGLE_MP4_AV_OK");
   SceneDraft second=draft.copy();second.setting="ciudad dia";second.sound=false;Scene next=new Scene(second);next.setSequence(false);next.setNeural(NeuralSprites.prepare(c,second,0));
   File series=new File(dir,"stage32-series.mp4");VideoExporter.writeSeries(series,Arrays.asList(scene,next),motion,monitor);verify(series,2,scene.frames()+next.frames());note("SERIES_MP4_AV_OK");
   SceneLibrary.save(c,"inicio",draft);SceneLibrary.save(c,"segunda",second);check(SceneLibrary.list(c).length()>=2,"library");
   SceneLibrary.Action opened=SceneLibrary.apply(c,"abri escena inicio",null);check(opened.scene.get("banana").label.equals("tony")&&opened.scene.slot(1)==opened.scene.slot(2),"continuity");
   OwnVision vision=new OwnVision(c.getAssets().open("pixel-v2/vision.bin"));float visionMax=0;try(DataInputStream in=new DataInputStream(new FileInputStream(new File(dir,"vision-reference.bin")))){int rows=in.readInt(),n=in.readInt();for(int i=0;i<rows;i++){float[] input=new float[n];for(int j=0;j<n;j++)input[j]=in.readFloat();float[] actual=vision.forward(input);for(int j=0;j<9;j++)visionMax=Math.max(visionMax,Math.abs(actual[j]-in.readFloat()));}}check(visionMax<.0004,"vision parity "+visionMax);note("OWN_VISION_PARITY "+visionMax);
   File reference=new File(dir,"reference.png");try(FileOutputStream out=new FileOutputStream(reference)){batch.images.get("banana").compress(Bitmap.CompressFormat.PNG,100,out);}
   Bitmap imported=ReferenceImage.importBitmap(c,android.net.Uri.fromFile(reference),false);check(imported.getWidth()==128,"import dimension");note("REFERENCE_DESCRIPTION "+ReferenceImage.describe(c,imported));
   String refKey="ref_stage32";try(FileOutputStream out=c.openFileOutput(refKey+".png",0)){imported.compress(Bitmap.CompressFormat.PNG,100,out);}SceneDraft locked=draft.copy();locked.get("banana").reference=refKey;
   NeuralSprites.Batch refBatch=NeuralSprites.prepare(c,locked,0);check(!refBatch.rigs.containsKey("banana")&&refBatch.images.get("banana").sameAs(refBatch.alternate.get("banana")),"reference face stays locked on reaction");
   CreatorMemory.save(c,new CreatorSession(),locked);SceneLibrary.Action reused=CharacterArchive.apply(c,"usa a tony",new SceneDraft());check(reused.scene.cast.get(0).reference.equals(refKey),"profile reference persistence");
   BananaTransformer chat=new BananaTransformer(c.getAssets().open("chat32/chat32.bin"),c.getAssets().open("chat32/vocab.txt"),c.getAssets().open("chat32/tokenizer.json"));String reply=chat.generate("sos independiente","","").text;note("OWN_CHAT32 "+reply);check(reply.contains("propios")&&reply.contains("preentrenado"),"own chat inference");
   note("SCENE_CONTINUITY_OK");result.putString("stream","PASS STAGE32 Android render, own Chat32, own vision, references, rigs, door interactions, AAC/H264 mux and series.\n");finish(Activity.RESULT_OK,result);
  }catch(Throwable t){StringWriter sw=new StringWriter();t.printStackTrace(new PrintWriter(sw));result.putString("stream","FAIL "+sw.toString());finish(Activity.RESULT_CANCELED,result);}
 }
 private void verify(File file,int tracks,int frames)throws Exception{
  MediaExtractor e=new MediaExtractor();try{e.setDataSource(file.getAbsolutePath());check(e.getTrackCount()==tracks,"track count");boolean video=false,audio=false;
   for(int i=0;i<e.getTrackCount();i++){String mime=e.getTrackFormat(i).getString(MediaFormat.KEY_MIME);e.selectTrack(i);e.seekTo(0,MediaExtractor.SEEK_TO_CLOSEST_SYNC);int count=0;long previous=-1;while(e.getSampleTime()>=0){long pts=e.getSampleTime();check(pts>=previous,"PTS order");previous=pts;count++;e.advance();}e.unselectTrack(i);if(mime.startsWith("video")){video=true;check(count==frames,"video frame count "+count+" vs "+frames);}if(mime.startsWith("audio")){audio=true;check(count>1,"audio samples");}}
   check(video&&audio,"AV streams");
  }finally{e.release();}
 }
}
