package com.bananalab.lite;
import java.util.*;
public final class Stage58GoalsCheck{
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void plan(String q,String kinds){List<ImprovementFlow.Step> p=ImprovementFlow.plan(q);String s="";for(ImprovementFlow.Step x:p)s+=x.kind+",";check(s.equals(kinds),q+" => "+s);}
 public static void main(String[] args)throws Exception{
 plan("podes mejorar los pesos de chat y pixel y actualizar la app","both,build,");
 plan("quiero que mejores banana chat y prepares la app con las mejoras","chat,build,");
 plan("agranda la letra y mejora los pesos de chat y pixel y actualiza la app con todo","settings,both,build,");
 plan("mejora banana pixel con la referencia actual","pixel,");plan("quiero que mejores pixel mas a fondo","pixelDeep,");plan("recupera los pesos anteriores de chat","undoChat,");plan("mostrame los entrenamientos","status,");plan("que falta mejorar","scope,");
 for(String s:new String[]{"no mejores los pesos de chat","como entreno chat","hace que la banana baile","crea una funcion que ordene la lista y quite duplicados y sume","mejora el dibujo del zorro","cuando diga \"entrena chat\" responde \"no\"","mejora la app","hola"})plan(s,"");
 for(String s:new String[]{"mejora chat y borra mis escenas","mejora chat; borra mis escenas","actualiza la app y mejora pixel","mejora chat y entrena chat","mejora pixel mas a fondo y mejora pixel","actualiza la app y mejora pixel mas a fondo"}){try{ImprovementFlow.plan(s);throw new AssertionError("accepted "+s);}catch(IllegalArgumentException good){}}
 ProgramPlan p=ProgramPlan.request("ordena; quita duplicados; suma cada elemento 1; multiplica por 2; mayores -100; menores 100; pares; positivos; ordena descendente; valor absoluto; ordena; suma");check(p.steps.length()==12,"12 steps");check(p.oracle("3,1,3,-2").equals("12"),"oracle result");check(p.kernel().contains("step11"),"full kernel");try(java.net.URLClassLoader loader=Stage55EvolutionCheck.compile(p.candidate())){java.lang.reflect.Method m=loader.loadClass("com.bananalab.patch.ProgramKernel").getMethod("apply",String.class);check(m.invoke(null,"3,1,3,-2").equals("12"),"compiled12-step execution");check(ProgramJudge.check(m,p,42).getInt("passed")==64,"generated12-step cases");}try{ProgramPlan.request("ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena;ordena");throw new AssertionError("13 steps");}catch(IllegalArgumentException good){}
 System.out.println("PASS Stage58 natural goals, full-clause rejection before mutations, scene separation, ordered rebuild, duplicate training prevention and12-step programs");}
}
