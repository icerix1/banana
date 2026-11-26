package com.bananalab.lite;
import android.graphics.*;
/** Small 2D cutout rig made from the model's own pixels. No generated 3D views. */
final class RigRenderer {
 final Bitmap body;final Bitmap[] limbs=new Bitmap[4];final int side,type;
 RigRenderer(Bitmap source,int type){
  this.type=type;side=source.getWidth();int[] original=new int[side*side],base;source.getPixels(original,0,side,0,0,side,side);base=original.clone();int[][] parts=new int[4][original.length];
  for(int y=0;y<side;y++)for(int x=0;x<side;x++){float xx=(x+.5f)/side*64,yy=(y+.5f)/side*64;int part=-1;
   float[][] lines={{23,39,15,43},{43,39,50,43},{27,53,25,60},{38,53,40,60}};
   for(int j=0;j<4;j++){float[] line=lines[j];float dx=line[2]-line[0],dy=line[3]-line[1],t=Math.max(0,Math.min(1,((xx-line[0])*dx+(yy-line[1])*dy)/(dx*dx+dy*dy)));float ex=xx-line[0]-t*dx,ey=yy-line[1]-t*dy;if(ex*ex+ey*ey<3.5f){part=j;break;}}
   // Only detach isolated dark strokes. Colored fill and its nearby contour
   // stay intact, including the banana crescent and the face under its arm.
   if(part>=0){boolean protect=false;for(int oy=-3;oy<=3&&!protect;oy++)for(int ox=-3;ox<=3;ox++){int px=x+ox,py=y+oy;if(px<0||py<0||px>=side||py>=side)continue;int color=original[py*side+px];if((color>>>24)>120&&Math.max((color>>16)&255,Math.max((color>>8)&255,color&255))>75){protect=true;break;}}if(protect)part=-1;}

   if(part>=0){int i=y*side+x;parts[part][i]=original[i];base[i]=0;}
  }
  body=Bitmap.createBitmap(base,side,side,Bitmap.Config.ARGB_8888);for(int i=0;i<4;i++)limbs[i]=Bitmap.createBitmap(parts[i],side,side,Bitmap.Config.ARGB_8888);
 }
 void draw(Canvas canvas,RectF bounds,String action,float time,Bitmap expression,boolean holding,float[] grip,int view){
  canvas.save();canvas.translate(bounds.left,bounds.top);canvas.scale(bounds.width()/64f,bounds.height()/64f);android.graphics.Paint p=new android.graphics.Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);RectF normalized=new RectF(0,0,64,64);
  canvas.drawBitmap(body,null,normalized,p);
  if(expression!=null){float[][] centers={{35,43},{32,32},{32,27},{32,29},{32,26},{32,27},{32,27},{32,27},{32,26},{32,33},{32,31}};float[] f=centers[Math.min(type,10)];float fx=f[0],radius=14;if(view==1||view==2){fx=32+(fx-32)*.78f;radius*=.78f;}if(view==2)fx=64-fx;canvas.save();canvas.clipRect(fx-radius,f[1]-8,fx+radius,f[1]+16);canvas.drawBitmap(expression,null,normalized,p);canvas.restore();}
  float gait=(float)Math.sin(time*Math.PI*4),blend=Math.min(1,Math.min(time*7,(1-time)*7));float left=0,right=0,leg=0;
  if(Timeline.walk(action)){left=-gait*17*blend;right=gait*17*blend;leg=gait*19*blend;}
  if((action.equals("saluda")||action.equals("aplaude")))right=-85-25*(float)Math.sin(time*Math.PI*4);
  if(action.equals("abre"))right=-55*(float)Math.sin(time*Math.PI);
  if(action.equals("sorprende")){left=65;right=-65;}
  if((action.equals("sostiene")||action.equals("entrega")||action.equals("recibe"))||holding){left=30;right=-30;}
  left*=ModularHub.view().gesture;right*=ModularHub.view().gesture;leg*=ModularHub.view().gesture;
  if(grip!=null){float sy=ActorGeometry.shoulderY(type);if(action.equals("saluda")){boolean rightHold=grip[0]>=32;drawArm(canvas,p,rightHold?43:23,sy,grip[0],grip[1],rightHold?-1:1);float free=rightHold?23:43,sign=rightHold?-1:1;drawArm(canvas,p,free,sy,free+sign*(11+3*(float)Math.sin(time*Math.PI*6)),sy-18+3*(float)Math.cos(time*Math.PI*6),rightHold?1:-1);}else{drawArm(canvas,p,23,sy,grip[0]-2,grip[1],1);drawArm(canvas,p,43,sy,grip[0]+2,grip[1],-1);}}else{drawPart(canvas,p,0,23,39,left,0);drawPart(canvas,p,1,43,39,right,0);}
  float seated=action.equals("sienta")?-60:0;
  drawPart(canvas,p,2,27,53,leg+seated,Timeline.walk(action)?plant(27,53,25,60,leg)-Math.max(0,gait)*1.2f:0);drawPart(canvas,p,3,38,53,-leg+seated,Timeline.walk(action)?plant(38,53,40,60,-leg)-Math.max(0,-gait)*1.2f:0);
  canvas.restore();
 }
 private float plant(float x,float y,float tx,float ty,float angle){float r=(float)Math.toRadians(angle);return ty-(y+(tx-x)*(float)Math.sin(r)+(ty-y)*(float)Math.cos(r));}
 private void drawArm(Canvas c,Paint p,float x,float y,float tx,float ty,int bend){float[] joint=JointMath.solve(x,y,tx,ty,13,14,bend);p.setColor(0xff37303f);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,joint[0],joint[1],p);c.drawLine(joint[0],joint[1],joint[2],joint[3],p);c.drawCircle(joint[2],joint[3],1.25f,p);}
 private void drawPart(Canvas c,Paint p,int i,float x,float y,float angle,float lift){c.save();c.translate(0,lift);c.rotate(angle,x,y);c.drawBitmap(limbs[i],null,new RectF(0,0,64,64),p);c.restore();}
}
