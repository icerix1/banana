package com.bananalab.lite;
/** Small Java8 library backports for full source builds on API23. */
final class JavaCompat{
 static String join(CharSequence separator,Iterable<? extends CharSequence> items){if(separator==null||items==null)throw new NullPointerException();StringBuilder out=new StringBuilder();boolean first=true;for(CharSequence item:items){if(!first)out.append(separator);first=false;out.append(item);}return out.toString();}
 static String join(CharSequence separator,CharSequence...items){return join(separator,java.util.Arrays.asList(items));}
}
