package com.bananalab.lite;
import android.graphics.*;import android.media.*;import java.io.*;import java.nio.ByteBuffer;
/** AVC/MP4 local, un cuadro a la vez; Image planes respetan sus strides. */
final class VideoExporter{
 interface Monitor{boolean cancelled();void progress(int frame,int total);}
 static final int SIDE=384,FPS=16;
 static void write(File file,Scene scene,Motion motion,int frames,Monitor monitor)throws Exception{
  SceneDraft draft=scene.source();if(draft==null||!draft.sound){writeSilent(file,scene,motion,frames,monitor);return;}
  File video=File.createTempFile("banana-video-",".mp4",file.getParentFile()),audio=File.createTempFile("banana-audio-",".mp4",file.getParentFile());
  try{writeSilent(video,scene,motion,frames,monitor);if(monitor.cancelled())throw new IOException("Cancelado");Soundtrack.writeAac(audio,Soundtrack.pcm(draft,frames));if(monitor.cancelled())throw new IOException("Cancelado");Soundtrack.mux(video,audio,file);}finally{video.delete();audio.delete();}
 }
 interface Frames{void draw(Canvas canvas,int side,int index,int total);}
 static void writeSeries(File file,java.util.List<Scene> scenes,Motion motion,Monitor monitor)throws Exception{
  int total=0;boolean sound=false;for(Scene s:scenes){total+=s.frames();sound|=s.source()!=null&&s.source().sound;}final int count=total;if(count==0)throw new IOException("La serie esta vacia");
  Frames source=(canvas,side,index,unused)->{int offset=0;for(Scene s:scenes){if(index<offset+s.frames()){s.draw(canvas,side,motion,(index-offset)/(float)Math.max(1,s.frames()-1));return;}offset+=s.frames();}};
  if(!sound){writeFrames(file,source,count,monitor);return;}
  File v=File.createTempFile("series-video-",".mp4",file.getParentFile()),a=File.createTempFile("series-audio-",".mp4",file.getParentFile());
  try{writeFrames(v,source,count,monitor);if(monitor.cancelled())throw new IOException("Cancelado");short[] pcm=new short[count*Soundtrack.RATE/FPS];int at=0;
   for(Scene s:scenes){int n=s.frames()*Soundtrack.RATE/FPS;if(s.source()!=null&&s.source().sound){short[] part=Soundtrack.pcm(s.source(),s.frames());System.arraycopy(part,0,pcm,at,Math.min(n,part.length));}at+=n;}
   Soundtrack.writeAac(a,pcm);if(monitor.cancelled())throw new IOException("Cancelado");Soundtrack.mux(v,a,file);
  }finally{v.delete();a.delete();}
 }
 static void writeSilent(File file,Scene scene,Motion motion,int frames,Monitor monitor)throws Exception{
  writeFrames(file,(canvas,side,index,total)->scene.draw(canvas,side,motion,index/(float)Math.max(1,total-1)),frames,monitor);
 }
 static void writeFrames(File file,Frames source,int frames,Monitor monitor)throws Exception{
  MediaCodec codec=null;MediaMuxer muxer=null;Bitmap bitmap=null;boolean codecStarted=false,muxerStarted=false,success=false;int track=-1,written=0;long lastProgress=System.nanoTime();
  try{
   codec=MediaCodec.createEncoderByType("video/avc");MediaFormat format=MediaFormat.createVideoFormat("video/avc",SIDE,SIDE);format.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);format.setInteger(MediaFormat.KEY_BIT_RATE,1000000);format.setInteger(MediaFormat.KEY_FRAME_RATE,FPS);format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();codecStarted=true;muxer=new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);bitmap=Bitmap.createBitmap(SIDE,SIDE,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);int[] rgb=new int[SIDE*SIDE];MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();int next=0;boolean sentEnd=false,finished=false;
   while(!finished){
    if(monitor.cancelled()||Thread.currentThread().isInterrupted())throw new IOException("Exportacion cancelada");if(System.nanoTime()-lastProgress>60L*1000000000)throw new IOException("El codificador dejo de responder");
    if(!sentEnd){int index=codec.dequeueInputBuffer(10000);if(index>=0){if(next<frames){source.draw(canvas,SIDE,next,frames);bitmap.getPixels(rgb,0,SIDE,0,0,SIDE,SIDE);Image image=codec.getInputImage(index);if(image==null)throw new IOException("El codificador no ofrece entrada YUV compatible");fill(image,rgb,SIDE,SIDE);image.close();codec.queueInputBuffer(index,0,SIDE*SIDE*3/2,next*1000000L/FPS,0);next++;monitor.progress(next,frames);}else{codec.queueInputBuffer(index,0,0,frames*1000000L/FPS,MediaCodec.BUFFER_FLAG_END_OF_STREAM);sentEnd=true;}lastProgress=System.nanoTime();}}
    while(true){int output=codec.dequeueOutputBuffer(info,10000);if(output==MediaCodec.INFO_TRY_AGAIN_LATER)break;if(output==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){if(muxerStarted)throw new IOException("Formato de video cambio dos veces");track=muxer.addTrack(codec.getOutputFormat());muxer.start();muxerStarted=true;lastProgress=System.nanoTime();continue;}if(output<0)continue;ByteBuffer bytes=codec.getOutputBuffer(output);if((info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0)info.size=0;if(info.size>0){if(!muxerStarted||bytes==null)throw new IOException("Salida AVC incompleta");bytes.position(info.offset);bytes.limit(info.offset+info.size);muxer.writeSampleData(track,bytes,info);written++;lastProgress=System.nanoTime();}finished=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(output,false);if(finished)break;}
   }
   if(written<1)throw new IOException("El video no contiene cuadros");muxer.stop();muxerStarted=false;success=true;
  }finally{if(bitmap!=null)bitmap.recycle();if(codec!=null){if(codecStarted)try{codec.stop();}catch(Exception ignored){}codec.release();}if(muxer!=null){if(muxerStarted)try{muxer.stop();}catch(Exception ignored){}muxer.release();}if(!success)file.delete();}
 }
 static int clamp(int value){return Math.max(0,Math.min(255,value));}
 static int y(int r,int g,int b){return clamp(((66*r+129*g+25*b+128)>>8)+16);}
 static int u(int r,int g,int b){return clamp(((-38*r-74*g+112*b+128)>>8)+128);}
 static int v(int r,int g,int b){return clamp(((112*r-94*g-18*b+128)>>8)+128);}
 private static void put(Image.Plane plane,int x,int y,int value){ByteBuffer buffer=plane.getBuffer();int at=buffer.position()+y*plane.getRowStride()+x*plane.getPixelStride();if(at>=buffer.limit())throw new IllegalArgumentException("Plano YUV fuera de rango");buffer.put(at,(byte)value);}
 static void fill(Image image,int[] rgb,int width,int height){Image.Plane[] p=image.getPlanes();if(p.length<3)throw new IllegalArgumentException("Faltan planos YUV");Rect crop=image.getCropRect();for(int yy=0;yy<height;yy++)for(int xx=0;xx<width;xx++){int c=rgb[yy*width+xx];put(p[0],xx+crop.left,yy+crop.top,y((c>>16)&255,(c>>8)&255,c&255));}for(int yy=0;yy<height;yy+=2)for(int xx=0;xx<width;xx+=2){int r=0,g=0,b=0;for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++){int c=rgb[(yy+dy)*width+xx+dx];r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;}r/=4;g/=4;b/=4;put(p[1],(xx+crop.left)/2,(yy+crop.top)/2,u(r,g,b));put(p[2],(xx+crop.left)/2,(yy+crop.top)/2,v(r,g,b));}}
}
