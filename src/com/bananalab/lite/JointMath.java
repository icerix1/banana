package com.bananalab.lite;
/** Two-link analytic arm solution; distances stay within reach. */
final class JointMath{
 static float[] solve(float sx,float sy,float tx,float ty,float upper,float lower,int bend){float dx=tx-sx,dy=ty-sy,d=(float)Math.sqrt(dx*dx+dy*dy);if(d<.001f){dx=.001f;d=.001f;}float reach=Math.max(Math.abs(upper-lower)+.001f,Math.min(upper+lower-.001f,d)),ux=dx/d,uy=dy/d,a=(upper*upper-lower*lower+reach*reach)/(2*reach),h=(float)Math.sqrt(Math.max(0,upper*upper-a*a));return new float[]{sx+ux*a-uy*h*bend,sy+uy*a+ux*h*bend,sx+ux*reach,sy+uy*reach};}
}
