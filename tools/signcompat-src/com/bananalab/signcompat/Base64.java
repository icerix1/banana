package com.bananalab.signcompat;
/** Basic RFC4648 codec matching the used JDK Base64 APIs. */
public final class Base64{
 static final String ALPHABET="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
 public static Encoder getEncoder(){return new Encoder();}public static Decoder getDecoder(){return new Decoder();}
 public static final class Encoder{public String encodeToString(byte[] bytes){StringBuilder out=new StringBuilder((bytes.length+2)/3*4);for(int i=0;i<bytes.length;i+=3){int a=bytes[i]&255,b=i+1<bytes.length?bytes[i+1]&255:0,c=i+2<bytes.length?bytes[i+2]&255:0;out.append(ALPHABET.charAt(a>>>2)).append(ALPHABET.charAt(((a&3)<<4)|(b>>>4))).append(i+1<bytes.length?ALPHABET.charAt(((b&15)<<2)|(c>>>6)):'=').append(i+2<bytes.length?ALPHABET.charAt(c&63):'=');}return out.toString();}}
 public static final class Decoder{public byte[] decode(String text){if(text==null)throw new NullPointerException();int n=text.length(),data=n;while(data>0&&text.charAt(data-1)=='=')data--;int padding=n-data;if(padding>2||data%4==1||(padding>0&&(n%4!=0||padding!=(4-data%4)%4)))throw new IllegalArgumentException("Invalid base64 padding");byte[] out=new byte[data*6/8];int bits=0,value=0,at=0;for(int i=0;i<data;i++){int next=ALPHABET.indexOf(text.charAt(i));if(next<0)throw new IllegalArgumentException("Invalid base64 character");value=(value<<6)|next;bits+=6;if(bits>=8){bits-=8;out[at++]=(byte)(value>>>bits);}}return out;}}
}
