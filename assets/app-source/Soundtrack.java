package com.bananalab.lite;
import android.media.*;import java.io.*;import java.nio.*;import java.util.*;
/** Ambiente y pasos sintetizados localmente; sin voces ni grabaciones externas. */
final class Soundtrack{
 static final int RATE=16000;
 static short[] pcm(SceneDraft draft,int frames){
  int count=Math.max(1,frames)*RATE/VideoExporter.FPS;short[] data=new short[count];int slots=1;for(int i=0;i<draft.actions.size();i++)slots=Math.max(slots,draft.slot(i)+1);
  int[] footsteps=new int[slots];for(int i=0;i<draft.actions.size();i++)if(Timeline.walk(draft.actions.get(i)))footsteps[draft.slot(i)]++;
  Random random=new Random(31031);
  for(int n=0;n<count;n++){double t=n/(double)RATE,phase=n/(double)count*slots;double fade=Math.min(1,Math.min(t*3,(count-n)/(double)RATE*3));double value=180*Math.sin(2*Math.PI*110*t)+100*Math.sin(2*Math.PI*165*t);
   int slot=Math.min(slots-1,(int)phase);double local=phase-slot,step=local*3-Math.floor(local*3),envelope=1700*Math.exp(-step*28);for(int i=0;i<footsteps[slot];i++)value+=(random.nextDouble()*2-1)*envelope;
   data[n]=(short)Math.max(-5000,Math.min(5000,value*fade));
  }for(SceneDraft.Line line:draft.dialogue){SceneDraft.Actor a=draft.get(line.actor);if(a==null)continue;short[] voice=VoiceSynth.render(line.text,a.voice,a.name.hashCode());int at=Math.round((line.slot*draft.beatSeconds+.05f)*RATE);int overlapping=0;for(SceneDraft.Line other:draft.dialogue)if(other.slot==line.slot)overlapping++;float volume=.8f/Math.max(1,overlapping);for(int i=0;i<voice.length&&at+i<data.length;i++)if(at+i>=0)data[at+i]=(short)Math.max(-30000,Math.min(30000,data[at+i]+Math.round(voice[i]*volume)));}return data;
 }
 static void writeAac(File file,short[] samples)throws Exception{
  MediaCodec codec=null;MediaMuxer mux=null;boolean started=false,ready=false,ok=false;int track=-1;
  try{
   codec=MediaCodec.createEncoderByType("audio/mp4a-latm");MediaFormat f=MediaFormat.createAudioFormat("audio/mp4a-latm",RATE,1);f.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);f.setInteger(MediaFormat.KEY_BIT_RATE,48000);f.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,4096);codec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();started=true;mux=new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
   int at=0;boolean sent=false,end=false;MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();long deadline=System.nanoTime()+60L*1000000000;
   while(!end){if(Thread.currentThread().isInterrupted()||System.nanoTime()>deadline)throw new IOException("Audio cancelado o sin respuesta");
    if(!sent){int i=codec.dequeueInputBuffer(10000);if(i>=0){ByteBuffer b=codec.getInputBuffer(i);b.clear();b.order(ByteOrder.LITTLE_ENDIAN);int n=Math.min(b.remaining()/2,samples.length-at);for(int j=0;j<n;j++)b.putShort(samples[at+j]);codec.queueInputBuffer(i,0,n*2,at*1000000L/RATE,n==0?MediaCodec.BUFFER_FLAG_END_OF_STREAM:0);at+=n;if(n==0)sent=true;}}
    while(true){int i=codec.dequeueOutputBuffer(info,10000);if(i==MediaCodec.INFO_TRY_AGAIN_LATER)break;if(i==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){track=mux.addTrack(codec.getOutputFormat());mux.start();ready=true;continue;}if(i<0)continue;ByteBuffer b=codec.getOutputBuffer(i);if((info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0)info.size=0;if(info.size>0){if(!ready)throw new IOException("Audio sin formato");info.presentationTimeUs=Math.max(0,info.presentationTimeUs);b.position(info.offset);b.limit(info.offset+info.size);mux.writeSampleData(track,b,info);}end=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(i,false);if(end)break;}
   }if(!ready)throw new IOException("Audio vacio");mux.stop();ready=false;ok=true;
  }finally{if(codec!=null){if(started)try{codec.stop();}catch(Exception ignored){}codec.release();}if(mux!=null){if(ready)try{mux.stop();}catch(Exception ignored){}mux.release();}if(!ok)file.delete();}
 }
 static void mux(File video,File audio,File output)throws Exception{
  MediaMuxer mux=new MediaMuxer(output.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);MediaExtractor v=new MediaExtractor(),a=new MediaExtractor();boolean started=false,ok=false;
  try{v.setDataSource(video.getAbsolutePath());a.setDataSource(audio.getAbsolutePath());if(v.getTrackCount()<1||a.getTrackCount()<1)throw new IOException("Falta una pista");int vt=mux.addTrack(v.getTrackFormat(0)),at=mux.addTrack(a.getTrackFormat(0));mux.start();started=true;copy(v,mux,vt);copy(a,mux,at);mux.stop();started=false;ok=true;}
  finally{v.release();a.release();if(started)try{mux.stop();}catch(Exception ignored){}mux.release();if(!ok)output.delete();}
 }
 private static void copy(MediaExtractor e,MediaMuxer mux,int track)throws Exception{
  e.selectTrack(0);ByteBuffer b=ByteBuffer.allocate(2*1024*1024);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
  while(true){if(Thread.currentThread().isInterrupted())throw new IOException("Exportacion cancelada");b.clear();int n=e.readSampleData(b,0);if(n<0)break;info.set(0,n,Math.max(0,e.getSampleTime()),e.getSampleFlags());mux.writeSampleData(track,b,info);e.advance();}
 }
}
