package com.bananalab.lite;
/** Bounded integer semantics shared by generated programs. */
final class FormulaMath{
 static int checked(long n){if(n < -1000000 || n > 1000000)throw new IllegalArgumentException("Intermedio fuera de rango");return (int)n;}
 static int add(int a,int b){return checked((long)a+b);}static int sub(int a,int b){return checked((long)a-b);}static int mul(int a,int b){return checked((long)a*b);}
 static int div(int a,int b){if(b==0)throw new IllegalArgumentException("Division por cero");return checked((long)a/b);}static int mod(int a,int b){if(b==0)throw new IllegalArgumentException("Division por cero");return a%b;}
 static int neg(int a){return checked(-(long)a);}static int abs(int a){return checked(Math.abs((long)a));}
}
