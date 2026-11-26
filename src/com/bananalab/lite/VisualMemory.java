package com.bananalab.lite;

import android.content.*;
import android.graphics.*;
import android.util.AtomicFile;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

final class VisualMemory {
    private static final Map<String,Bitmap> images=new HashMap<>();
    private static Context app;
    static synchronized Bitmap get(String name){return images.get(Knowledge.normalize(name));}
    static synchronized void restore(Context context){
        app=context.getApplicationContext();
        for(Map.Entry<String,?> entry:app.getSharedPreferences("visual-memory",0).getAll().entrySet()){
            try{
                JSONObject data=new JSONObject((String)entry.getValue());
                File file=new File(app.getFilesDir(),data.getString("file"));
                Bitmap image=BitmapFactory.decodeFile(file.getPath());
                if(image!=null&&images.size()<12){images.put(entry.getKey(),image);Knowledge.visual(entry.getKey());}
            }catch(Exception ignored){}
        }
    }
    static int palette(String name,int fallback) {
        Bitmap image=get(name);
        if(image==null)return fallback;
        long red=0,green=0,blue=0;int count=0;
        for(int y=image.getHeight()/2;y<image.getHeight();y+=4)
            for(int x=image.getWidth()/4;x<image.getWidth()*3/4;x+=4){
                int pixel=image.getPixel(x,y),r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
                if(r+g+b>690||r+g+b<90)continue;
                red+=r;green+=g;blue+=b;count++;
            }
        return count==0?fallback:0xff000000|((int)(red/count)<<16)|((int)(green/count)<<8)|(int)(blue/count);
    }

    static String translate(String topic){
        String[] es={"persona","hombre","mujer","paisaje","montana","ciudad","leon","pinguino","gato","perro","bosque","playa","caballo","zorro"};
        String[] en={"person portrait","man portrait","woman portrait","landscape","mountain","city skyline","lion animal","penguin animal","cat animal","dog animal","forest landscape","beach landscape","horse animal","fox animal"};
        for(int i=0;i<es.length;i++)if(es[i].equals(topic))return en[i];
        return topic;
    }
    static byte[] request(String address,int limit)throws Exception{
        URL url=new URL(address);
        if(!url.getProtocol().equals("https")||!(url.getHost().equals("commons.wikimedia.org")||url.getHost().equals("upload.wikimedia.org")||url.getHost().equals("en.wikipedia.org")||url.getHost().equals("es.wikipedia.org")||url.getHost().equals("api.inaturalist.org")||url.getHost().equals("inaturalist-open-data.s3.amazonaws.com")||url.getHost().equals("images-api.nasa.gov")||url.getHost().equals("images-assets.nasa.gov")||url.getHost().equals("openaccess-api.clevelandart.org")||url.getHost().equals("openaccess-cdn.clevelandart.org")||url.getHost().equals("images.clevelandart.org")||url.getHost().equals("collectionapi.metmuseum.org")||url.getHost().equals("images.metmuseum.org")))
            throw new IOException("Fuente visual no admitida");
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(5000);connection.setReadTimeout(5000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent","BananaLabLite/0.10 (Android educational animation app)");
        try{
            if(connection.getResponseCode()!=200)throw new IOException("Fuente visual no disponible");
            try(InputStream input=connection.getInputStream()){
                ByteArrayOutputStream output=new ByteArrayOutputStream();
                byte[] buffer=new byte[2048];int count;
                while((count=input.read(buffer))!=-1){
                    if(output.size()+count>limit)throw new IOException("Referencia demasiado grande");
                    output.write(buffer,0,count);
                }
                return output.toByteArray();
            }
        }finally{connection.disconnect();}
    }
    static void fetch(Context context,String topic)throws Exception{
        boolean cached;
        synchronized(VisualMemory.class){cached=images.containsKey(topic);if(!cached&&images.size()>=12)return;}
        boolean checked=context.getSharedPreferences("sources-v10",0).getBoolean(topic,false);
        if(cached&&checked)return;
        JSONObject info=null;
        try{info=IndependentSources.reference(context,topic);}catch(Exception ignored){}
        if(info==null)info=MuseumSources.reference(context,topic);
        context.getSharedPreferences("sources-v10",0).edit().putBoolean(topic,true).apply();
        String title;
        if(info!=null)title=info.optString("referenceTitle");
        else{
            if(cached)return;
            String query=URLEncoder.encode(translate(topic)+" filetype:bitmap","UTF-8");
            String api="https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrlimit=1&gsrsearch="+query+"&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=128&format=json";
            JSONObject data=new JSONObject(new String(request(api,131072),"UTF-8"));
            JSONObject pages=data.optJSONObject("query");
            if(pages==null||(pages=pages.optJSONObject("pages"))==null)return;
            JSONObject page=pages.getJSONObject(pages.keys().next());
            info=page.getJSONArray("imageinfo").getJSONObject(0);
            title=page.optString("title");
        }
        String thumb=info.optString("thumburl");
        if(thumb.isEmpty())return;
        byte[] bytes=request(thumb,1048576);
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);
        if(bounds.outWidth<=0||bounds.outHeight<=0)return;
        BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
        while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>128)options.inSampleSize*=2;
        Bitmap image=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        if(image==null)return;
        String file="visual-"+Integer.toHexString(topic.hashCode())+".png";
        AtomicFile storage=new AtomicFile(new File(context.getFilesDir(),file));
        FileOutputStream output=storage.startWrite();
        try{if(!image.compress(Bitmap.CompressFormat.PNG,100,output))throw new IOException("No se pudo guardar");storage.finishWrite(output);}
        catch(Exception error){storage.failWrite(output);image.recycle();throw error;}
        JSONObject saved=new JSONObject();
        saved.put("file",file);saved.put("source",info.optString("descriptionurl"));
        saved.put("title",title);saved.put("date",System.currentTimeMillis());
        JSONObject metadata=info.optJSONObject("extmetadata");
        if(metadata!=null){
            saved.put("license",metadata.optJSONObject("LicenseShortName"));
            saved.put("author",metadata.optJSONObject("Artist"));
        }
        if(!context.getSharedPreferences("visual-memory",0).edit().putString(topic,saved.toString()).commit()){image.recycle();return;}
        synchronized(VisualMemory.class){images.put(topic,image);Knowledge.visual(topic);}
    }
    static void show(final android.app.Activity activity){
        StringBuilder text=new StringBuilder();
        for(Map.Entry<String,?> item:activity.getSharedPreferences("visual-memory",0).getAll().entrySet())
            text.append(item.getKey()).append("\n").append(item.getValue()).append("\n\n");
        if(text.length()==0)text.append("Las referencias visuales aparecerán acá después de buscar.");
        TextViewWrapper.show(activity,text.toString());
    }
    static class TextViewWrapper {
        static void show(android.app.Activity activity,String text){
            android.widget.TextView view=new android.widget.TextView(activity);view.setText(text);view.setTextIsSelectable(true);view.setPadding(24,20,24,20);
            android.widget.ScrollView scroll=new android.widget.ScrollView(activity);scroll.addView(view);
            UiTypeface.show(new android.app.AlertDialog.Builder(activity).setTitle("Fuentes visuales y licencias").setView(scroll).setPositiveButton("Cerrar",null));
        }
    }
}
