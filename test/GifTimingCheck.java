package com.bananalab.lite;
import java.io.*;
public class GifTimingCheck{public static void main(String[] args)throws Exception{try(GifWriter out=new GifWriter(new FileOutputStream(args[0]),256)){int[] pixels=new int[256*256];for(int frame=0;frame<8;frame++){java.util.Arrays.fill(pixels,frame%2==0?0xff242e38:0xff34424c);out.frame(pixels,frame%2==0?6:7);}}}}
