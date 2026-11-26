package com.bananalab.lite;
final class ShotPlan{
 final int shot;final float zoom,x,y,reveal;
 private ShotPlan(int s,float z,float x,float y,float r){shot=s;zoom=z;this.x=x;this.y=y;reveal=r;}
 static ShotPlan view(float zoom,float x,float y){return new ShotPlan(0,zoom,x,y,1);}
 static ShotPlan at(float time,float leadX,float leadY,boolean sequence){float t=Math.max(0,Math.min(1,time));if(!sequence)return new ShotPlan(0,1.014f,50,50,1);if(t<.28f)return new ShotPlan(0,1.015f+t*.12f,50,50,0);if(t<.65f){float p=Scene.ease((t-.28f)/.37f);return new ShotPlan(1,1.45f+.22f*p,leadX,leadY-7,0);}float p=Scene.ease((t-.65f)/.35f);return new ShotPlan(2,1.13f-.08f*p,50,54,Math.min(1,p*3));}
}
