package com.bananalab.lite;
/** Shared portrait anchors for object placement and articulated grip. */
final class ActorGeometry{
 static float scale(SceneDraft.Actor a,int count){return (count>2?.75f:.95f)*a.size;}
 static float handY(int type){return type==0?60:type==9?58:55;}
 static float shoulderY(int type){return type==0?54:type==9?46:40;}
 static float[] hand(SceneDraft.Actor a,int count,float actorX,int direction){float s=scale(a,count),x=direction>0?52:12,y=handY(PixelGenerator.type(a.kind));return new float[]{actorX+(-20+x/64f*40)*s*Appearance.width(a.look),80-14*s+a.y-67+(-30+y/64f*46)*s*Appearance.height(a.look)};}
 static float[] local(float x,float y,float worldX,float worldY,float scaleX,float scaleY,float rotation){float dx=(x-worldX)/scaleX,dy=(y-worldY)/scaleY,r=(float)Math.toRadians(rotation),u=dx*(float)Math.cos(r)+dy*(float)Math.sin(r),v=-dx*(float)Math.sin(r)+dy*(float)Math.cos(r);return new float[]{(u+20)/40*64,(v+30)/46*64};}
 static float[] world(float x,float y,float[] pose){float u=-20+x/64f*40,v=-30+y/64f*46,r=(float)Math.toRadians(pose[4]),rx=u*(float)Math.cos(r)-v*(float)Math.sin(r),ry=u*(float)Math.sin(r)+v*(float)Math.cos(r);return new float[]{pose[0]+rx*pose[2],pose[1]+ry*pose[3]};}

}
