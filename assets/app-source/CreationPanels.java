package com.bananalab.lite;
import android.app.*;import android.widget.*;import org.json.*;
final class CreationPanels{
 interface Changed{void run();}
 static void scenes(Activity c,Changed changed){
  try{JSONArray rows=SceneLibrary.list(c);String[] names=new String[rows.length()];for(int i=0;i<names.length;i++)names[i]=rows.getJSONObject(i).getString("name");
   UiTypeface.show(new AlertDialog.Builder(c).setTitle("Escenas de tu serie").setItems(names,(d,index)->{try{if(CreatorMemory.load(c).pending!=null)throw new Exception("Publicá o descartá el borrador antes de abrir otra escena.");SceneDraft draft=SceneDraft.read(rows.getJSONObject(index).getJSONObject("scene").toString());CreatorMemory.save(c,new CreatorSession(),draft);changed.run();}catch(Exception e){Toast.makeText(c,e.getMessage(),Toast.LENGTH_LONG).show();}})
    .setPositiveButton("Guardar actual",(d,w)->{EditText input=new EditText(c);input.setHint("Nombre de la escena");UiTypeface.show(new AlertDialog.Builder(c).setTitle("Guardar escena").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(dd,ww)->{try{if(BananaProject.isDraft(c))throw new Exception("Publicá el borrador antes de guardar la escena actual.");SceneLibrary.save(c,input.getText().toString(),BananaProject.current(c));Toast.makeText(c,"Escena guardada.",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(c,e.getMessage(),Toast.LENGTH_LONG).show();}}));}).setNegativeButton("Cerrar",null));
  }catch(Exception e){Toast.makeText(c,"No pude abrir las escenas.",Toast.LENGTH_LONG).show();}
 }
 static void timeline(Activity c,Changed changed){
  SceneDraft current;try{current=BananaProject.current(c);}catch(Exception e){Toast.makeText(c,e.getMessage(),Toast.LENGTH_LONG).show();return;}if(current==null){Toast.makeText(c,"Primero creá una escena.",Toast.LENGTH_SHORT).show();return;}
  String[] rows=new String[current.actions.size()];for(int i=0;i<rows.length;i++){SceneDraft.Actor a=current.cast.get(current.actors.get(i));rows[i]=(i+1)+". Tiempo "+(current.slot(i)+1)+" · "+(a.label.isEmpty()?a.name:a.label)+" "+Timeline.label(current.actions.get(i));}
  UiTypeface.show(new AlertDialog.Builder(c).setTitle("Editar secuencia").setItems(rows,(dialog,index)->{
   String[] values={"baila","camina","saluda","habla","salta","acerca","entraizq","entrader","saleizq","saleder","espera"};String[] choices={"Bailar","Caminar","Saludar","Hablar","Saltar","Acercarse","Entrar por izquierda","Entrar por derecha","Salir por izquierda","Salir por derecha","Esperar","Al mismo tiempo que el anterior","Después del anterior"};
   UiTypeface.show(new AlertDialog.Builder(c).setTitle(rows[index]).setItems(choices,(d,choice)->{try{
    SceneDraft draft=current.copy();if(choice<11)draft.actions.set(index,values[choice]);
    else{if(index==0)throw new Exception("El primer paso ya comienza al inicio.");draft.slots.set(index,draft.slot(index-1)+(choice==11?0:1));}
    draft=SceneDraft.read(draft.json().toString());BananaProject.edit(c,current,draft);changed.run();
   }catch(Exception e){Toast.makeText(c,"No pude aplicar ese cambio: "+e.getMessage(),Toast.LENGTH_LONG).show();}}));
  }).setNegativeButton("Cerrar",null));
 }
}
