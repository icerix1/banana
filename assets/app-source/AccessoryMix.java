package com.bananalab.lite;
/** Regional composition of existing own neural variants, not universal inpainting. */
final class AccessoryMix{
 static int[] apply(int[] base,int[] overlay,int type,int view,boolean head){int[] result=base.clone();float[][] faces={{35,43},{32,32},{32,27},{32,29},{32,26},{32,27},{32,27},{32,27},{32,26},{32,33}};int[] hats={22,19,17,18,16,17,17,17,13,20};float fx=faces[type][0],fy=faces[type][1],rx=13;if(view==1||view==2){fx=32+(fx-32)*.78f;rx*=.78f;}if(view==2)fx=64-fx;for(int y=0;y<128;y++)for(int x=0;x<128;x++){float xx=(x+.5f)/2,yy=(y+.5f)/2,dx=(xx-fx)/rx,dy=(yy-fy)/10;boolean protectedFace=view!=3&&dx*dx+dy*dy<=1;boolean mask=head?yy<=hats[type]+4&&!protectedFace:view!=3&&Math.abs(xx-fx)<=14&&Math.abs(yy-fy)<=6;if(mask)result[y*128+x]=overlay[y*128+x];}return result;}
}
