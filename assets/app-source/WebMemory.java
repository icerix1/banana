package com.bananalab.lite;

import android.app.*;
import android.content.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.Normalizer;

final class WebMemory {
    private static final int MAX_BYTES = 65536;
    private static final long WEEK = 7L*24*60*60*1000;

    static List<String> topics(String prompt) {
        List<String> names=Understanding.entities(prompt);
        return names.size()>3?new ArrayList<>(names.subList(0,3)):names;
    }

    private static JSONArray stored(Context context) {
        try{return new JSONArray(context.getSharedPreferences("web-memory",0).getString("entries","[]"));}
        catch(JSONException e){return new JSONArray();}
    }

    static void rememberExternal(Context context,String topic,String title,String text,String source)throws Exception{
        JSONArray entries=stored(context),next=new JSONArray();
        JSONObject entry=new JSONObject().put("topic",topic).put("title",title).put("text",text.substring(0,Math.min(4000,text.length()))).put("source",source).put("saved",System.currentTimeMillis());
        next.put(entry);
        for(int i=0;i<entries.length()&&next.length()<32;i++){
            JSONObject old=entries.getJSONObject(i);
            if(!topic.equals(old.optString("topic")))next.put(old);
        }
        if(!context.getSharedPreferences("web-memory",0).edit().putString("entries",next.toString()).commit())throw new java.io.IOException("No se pudo guardar");
    }

    static void restore(Context context) {
        JSONArray entries=stored(context);
        for(int i=0;i<entries.length();i++){
            JSONObject entry=entries.optJSONObject(i);
            if(entry!=null)Knowledge.learn(entry.optString("topic"),entry.optString("text"));
        }
    }

    static String lookup(Context context, String topic) throws Exception {
        JSONArray memory=stored(context);
        for(int i=0;i<memory.length();i++){
            JSONObject entry=memory.getJSONObject(i);
            if(topic.equals(entry.optString("topic"))&&System.currentTimeMillis()-entry.optLong("saved")<WEEK)
                {Knowledge.learn(topic,entry.optString("text"));return "Recuerdo guardado sobre "+topic;}
        }
        JSONObject page=searchPage("es",topic);
        boolean englishSource=false;
        String extract=page.optString("extract").trim();
        if(extract.isEmpty()){
            page=searchPage("en",VisualMemory.translate(topic).replace(" animal","").replace(" portrait",""));
            extract=page.optString("extract").trim();englishSource=true;
        }
        if(extract.isEmpty())return "Sin resumen sobre "+topic;
        JSONObject entry=new JSONObject();
        entry.put("topic",topic);entry.put("title",page.optString("title"));
        entry.put("text",extract.length()>1200?extract.substring(0,1200):extract);
        entry.put("source",(englishSource?"https://en.wikipedia.org/?curid=":"https://es.wikipedia.org/?curid=")+page.getLong("pageid"));
        entry.put("saved",System.currentTimeMillis());
        JSONArray next=new JSONArray();next.put(entry);
        for(int i=0;i<memory.length()&&next.length()<32;i++){
            JSONObject old=memory.getJSONObject(i);
            if(!topic.equals(old.optString("topic")))next.put(old);
        }
        if(!context.getSharedPreferences("web-memory",0).edit().putString("entries",next.toString()).commit())
            throw new IOException("No se pudo guardar el recuerdo");
        Knowledge.learn(topic,extract);
        // Web evidence is not a supervised training example. Keep weights unchanged.
        return "Información incorporada sobre "+topic;
    }

    static JSONObject searchPage(String language,String topic)throws Exception{
        String url="https://"+language+".wikipedia.org/w/api.php?action=query&generator=search&gsrnamespace=0&gsrlimit=3&gsrsearch="+
            URLEncoder.encode(topic,"UTF-8")+"&prop=extracts&exintro=1&explaintext=1&exchars=1800&format=json";
        JSONObject data=new JSONObject(new String(VisualMemory.request(url,MAX_BYTES),"UTF-8"));
        JSONObject query=data.optJSONObject("query"),pages=query==null?null:query.optJSONObject("pages");
        JSONObject best=new JSONObject();int rank=Integer.MAX_VALUE;
        if(pages!=null){Iterator<String> keys=pages.keys();while(keys.hasNext()){
            JSONObject page=pages.getJSONObject(keys.next());
            if(!page.optString("extract").isEmpty()&&page.optInt("index",99)<rank){best=page;rank=page.optInt("index",99);}
        }}
        return best;
    }

    static int count(Context context){return stored(context).length();}
    static JSONObject forTopic(Context context,String topic){JSONArray entries=stored(context);for(int i=0;i<entries.length();i++){JSONObject entry=entries.optJSONObject(i);if(entry!=null&&Knowledge.normalize(topic).equals(Knowledge.normalize(entry.optString("topic"))))return entry;}return null;}

    static void show(final Activity activity) {
        StringBuilder text=new StringBuilder();
        JSONArray entries=stored(activity);
        for(int i=0;i<entries.length();i++) {
            JSONObject entry=entries.optJSONObject(i);if(entry==null)continue;
            text.append(entry.optString("title")).append("\n")
                .append(entry.optString("text")).append("\n")
                .append(entry.optString("source")).append("\n")
                .append(new Date(entry.optLong("saved"))).append("\n\n");
        }
        if(text.length()==0)text.append("Todavía no hay información guardada. Al generar una escena, buscará sus temas automáticamente.");
        final JSONArray indexed=entries;
        LinearLayout layout=new LinearLayout(activity);layout.setOrientation(1);
        EditText query=new EditText(activity);query.setHint("Buscar en el índice local");layout.addView(query);
        Button search=new Button(activity);search.setText("Buscar");layout.addView(search);
        TextView content=new TextView(activity);content.setText(text.toString());content.setTextIsSelectable(true);content.setPadding(24,16,24,16);
        ScrollView scroll=new ScrollView(activity);scroll.addView(content);layout.addView(scroll,new LinearLayout.LayoutParams(-1,420));
        search.setOnClickListener(v->{
            java.util.List<JSONObject> hits=new ArrayList<>();
            for(int i=0;i<indexed.length();i++){
                JSONObject entry=indexed.optJSONObject(i);
                if(entry!=null&&Knowledge.score(query.getText().toString(),entry.optString("title"),entry.optString("text"))>0)hits.add(entry);
            }
            java.util.Collections.sort(hits,(a,b)->Integer.compare(Knowledge.score(query.getText().toString(),b.optString("title"),b.optString("text")),Knowledge.score(query.getText().toString(),a.optString("title"),a.optString("text"))));
            StringBuilder result=new StringBuilder();
            for(JSONObject hit:hits)result.append(hit.optString("title")).append("\n").append(hit.optString("text")).append("\n").append(hit.optString("source")).append("\n\n");
            content.setText(result.length()==0?"No hay resultados guardados para esa búsqueda.":result.toString());
        });
        UiTypeface.show(new AlertDialog.Builder(activity).setTitle("Buscador y memoria local").setView(layout)
            .setPositiveButton("Cerrar",null)
            .setNeutralButton("Borrar memoria",(d,w)->{activity.getSharedPreferences("web-memory",0).edit().clear().apply();Knowledge.clear();}));
    }
}
