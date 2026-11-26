package com.bananalab.lite;
public class SceneRevealCheck {
 public static void main(String[] args)throws Exception {
  SceneDraft draft=new SceneDraft();draft.cast.add(new SceneDraft.Actor("banana",33));draft.cast.add(new SceneDraft.Actor("pepino",67));draft.actions.add("camina");draft.actors.add(0);draft.actions.add("habla");draft.actors.add(1);Scene scene=new Scene(draft);
  java.lang.reflect.Field f=Scene.class.getDeclaredField("reveal");f.setAccessible(true);if(!"pepino".equals(f.get(scene)))throw new AssertionError("reveal identity");if(scene.frames()!=96)throw new AssertionError("frames");OwnPlan plan=new OwnPlan(SemillaCheck.load(args[0]),"una banana camina y un pepino habla en un pasillo");Scene fromPlan=new Scene(plan);if(!"pepino".equals(f.get(fromPlan)))throw new AssertionError("plan reveal identity");System.out.println("PASS ultimo personaje y 96 cuadros en ambas rutas");
 }
}
