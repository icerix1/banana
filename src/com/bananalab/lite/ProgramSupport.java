package com.bananalab.lite;
/** Fixed bounded runtime used by generated coordinated Java programs. */
final class ProgramSupport{
 static int[] numbers(String input){return MethodProgram.numbers(input);}
 static String csv(int[] a,int n){StringBuilder s=new StringBuilder();for(int i=0;i<n;i++){if(i>0)s.append(',');s.append(a[i]);}return s.toString();}
 static String text(String input){if(input.length()>128)throw new IllegalArgumentException("Hasta128 caracteres.");return input;}
}
