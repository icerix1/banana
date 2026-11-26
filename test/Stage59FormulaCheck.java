package com.bananalab.lite;
import java.net.*;import java.lang.reflect.*;import org.json.*;
public final class Stage59FormulaCheck{
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static String wrong(String x){return "0";}
 public static void main(String[] args)throws Exception{
 String[] specs={"transforma cada numero en su doble mas tres","transforma cada numero en si x es mayor que 0 entonces x por 2 sino 0","conserva cada numero si x%3==0; transforma cada numero en abs(x)+1; suma","transforma cada numero en (x+7)*(x-1)/3","conserva cada numero si x!=0 && 10/x>1","transforma cada numero en x==0?0:10/x","transforma cada numero en x*x*x*x","conserva cada numero si !(x<0) || x==-3"};
 int count=0;for(String spec:specs){ProgramPlan p=ProgramPlan.request(spec);try(URLClassLoader loader=Stage55EvolutionCheck.compile(p.candidate())){Method m=loader.loadClass("com.bananalab.patch.ProgramKernel").getMethod("apply",String.class);for(long seed:new long[]{2,89,1001}){check(ProgramJudge.check(m,p,seed).getInt("passed")==64,spec);count+=64;}if(spec.equals(specs[0]))check(m.invoke(null,"2,-1,0").equals("7,1,3"),"independent arithmetic expectation");if(spec.equals(specs[1]))check(m.invoke(null,"2,-1,0").equals("4,0,0"),"conditional expectation");if(spec.equals(specs[4]))check(m.invoke(null,"0,1,2,-1").equals("1,2"),"short circuit avoids division by zero");}}
 for(String bad:new String[]{"transforma cada numero en x>0","conserva cada numero si x+1","transforma cada numero en System.exit(0)","transforma cada numero en x+1 basura","transforma cada numero en x+1001","transforma cada numero","transforma cada numero en x>0?1:x<0","transforma cada numero en x; texto minusculas"}){try{ProgramPlan.request(bad);throw new AssertionError("accepted "+bad);}catch(IllegalArgumentException good){}}
 try{ProgramJudge.check(Stage59FormulaCheck.class.getMethod("wrong",String.class),ProgramPlan.request(specs[0]),42);throw new AssertionError("mutant accepted");}catch(IllegalArgumentException good){}
 check(ImprovementFlow.plan("crea una funcion que transforme cada numero en su doble mas tres").isEmpty(),"program not stolen by goal router");
 System.out.println("PASS Stage59 typed formulas, branches, short-circuit, limits, independent expected results and "+count+" compiled generated cases");}
}
