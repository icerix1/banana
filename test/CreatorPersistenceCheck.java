package com.bananalab.lite;
import android.content.*;import java.lang.reflect.*;import java.util.*;
public class CreatorPersistenceCheck{
 static final class Fake extends Context implements NotebookDisk.Provider {
  NotebookDisk store;public NotebookDisk notebook()throws Exception{if(store==null){store=new NotebookDisk(java.nio.file.Files.createTempDirectory("banana-host-notebook").toFile(),new NotebookMemoryIndex());store.migrate(this);}return store;}
  final Map<String,Map<String,Object>> files=new HashMap<>();boolean fail;
  public SharedPreferences getSharedPreferences(String name,int mode){
   final Map<String,Object> values=files.computeIfAbsent(name,k->new HashMap<>());
   return (SharedPreferences)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.class},(proxy,method,args)->{
    String n=method.getName();
    if(n.equals("edit")){
     Map<String,Object> changes=new HashMap<>();boolean[] clear={false};
     return Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{SharedPreferences.Editor.class},(editor,m,a)->{
      String op=m.getName();
      if(op.startsWith("put")){changes.put((String)a[0],a[1]);return editor;}
      if(op.equals("remove")){changes.put((String)a[0],null);return editor;}
      if(op.equals("clear")){clear[0]=true;return editor;}
      if(op.equals("commit")||op.equals("apply")){if(fail){fail=false;return op.equals("commit")?false:null;}if(clear[0])values.clear();for(String k:changes.keySet())if(changes.get(k)==null)values.remove(k);else values.put(k,changes.get(k));return op.equals("commit")?true:null;}
      return null;
     });
    }
    if(n.equals("contains"))return values.containsKey(args[0]);
    if(n.equals("getAll"))return new HashMap<>(values);
    if(n.startsWith("get"))return values.getOrDefault(args[0],args[1]);
    return null;
   });
  }
 }
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  Fake c=new Fake();Semilla m=SemillaCheck.load(args[0]);CreatorSession session=CreatorMemory.load(c);
  CreatorSession.Result result=session.apply(m,"quiero una serie con una banana",null);CreatorMemory.save(c,session,result.commit);
  check(SceneStore.load(c)==null&&CreatorMemory.load(c).pending!=null,"draft isolation persisted");
  session=CreatorMemory.load(c);result=session.apply(m,"que se llame Tony",null);CreatorMemory.save(c,session,result.commit);
  session=CreatorMemory.load(c);result=session.apply(m,"ahora dibujalos en un pasillo",null);
  c.fail=true;try{CreatorMemory.save(c,session,result.commit);throw new AssertionError("failure not reported");}catch(java.io.IOException expected){}
  check(SceneStore.load(c)==null&&CreatorMemory.load(c).pending!=null,"atomic rollback");
  CreatorMemory.save(c,session,result.commit);check(SceneStore.load(c).get("banana").label.equals("tony")&&CreatorMemory.load(c).pending==null,"atomic publish");
  check(SceneStore.undo(c)&&SceneStore.load(c)==null,"undo first scene");check(SceneStore.redo(c)&&SceneStore.load(c)!=null,"redo");
  String answer=ProjectMemory.reply(c,"recorda que mi serie es de terror");check(answer.contains("terror"),"store memory");
  check(ProjectMemory.related(c,"una idea para mi serie").contains("terror"),"relevant retrieval");
  CreatorMemory.clear(c);ProjectMemory.clear(c);check(SceneStore.load(c)!=null&&CreatorMemory.load(c).pending==null&&ProjectMemory.related(c,"serie").isEmpty(),"clear preserves scene");
  System.out.println("PASS persisted draft, atomic publish/failure, undo/redo, related memory and clear");
 }
}
