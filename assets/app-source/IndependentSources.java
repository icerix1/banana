package com.bananalab.lite;

import android.content.Context;
import org.json.*;
import java.net.URLEncoder;

final class IndependentSources {
    static JSONObject reference(Context context,String topic)throws Exception{
        if(topic.matches("luna|marte|tierra|planeta|espacio|estrella|nebulosa|astronauta")){
            String[] es={"luna","marte","tierra","planeta","espacio","estrella","nebulosa","astronauta"};
            String[] en={"moon","mars","earth","planet","space","star","nebula","astronaut"};
            String query=topic;
            for(int i=0;i<es.length;i++)if(topic.equals(es[i]))query=en[i];
            JSONObject result=new JSONObject(new String(VisualMemory.request("https://images-api.nasa.gov/search?q="+query+"&media_type=image&page_size=1",131072),"UTF-8"));
            JSONArray items=result.getJSONObject("collection").getJSONArray("items");
            if(items.length()==0)return null;
            JSONObject item=items.getJSONObject(0),data=item.getJSONArray("data").getJSONObject(0);
            JSONArray links=item.optJSONArray("links");
            if(links==null||links.length()==0)return null;
            String title=data.optString("title"),source="https://images.nasa.gov/details/"+data.optString("nasa_id");
            String description=data.optString("description");
            WebMemory.rememberExternal(context,topic,title,description,source);
            return pack(title,links.getJSONObject(0).optString("href"),source,"NASA image-use policy; revisar crédito individual",data.optString("photographer",data.optString("center","NASA")));
        }
        if(topic.matches("persona|hombre|mujer|paisaje|montana|ciudad|bosque|playa|robot|baile"))return null;
        String query=VisualMemory.translate(topic).split(" ")[0];
        String[] names={"zorro","leon","gato","perro","pinguino","caballo","pepino","tomate","banana"};
        String[] scientific={"Vulpes vulpes","Panthera leo","Felis catus","Canis familiaris","Spheniscidae","Equus caballus","Cucumis sativus","Solanum lycopersicum","Musa"};
        String exact="";
        for(int i=0;i<names.length;i++)if(topic.equals(names[i])){query=scientific[i];exact=query;}
        JSONObject result=new JSONObject(new String(VisualMemory.request("https://api.inaturalist.org/v1/taxa?q="+URLEncoder.encode(query,"UTF-8")+"&per_page=3&locale=es",131072),"UTF-8"));
        JSONArray taxa=result.getJSONArray("results");
        for(int i=0;i<taxa.length();i++){
            JSONObject taxon=taxa.getJSONObject(i),photo=taxon.optJSONObject("default_photo");
            if(photo==null)continue;
            if(!exact.isEmpty()&&!taxon.optString("name").equalsIgnoreCase(exact))continue;
            if(exact.isEmpty()&&!Knowledge.normalize(taxon.optString("preferred_common_name")).contains(topic)
                    &&!taxon.optString("name").equalsIgnoreCase(query))continue;
            String license=photo.optString("license_code");
            if(!license.matches("cc0|cc-by|cc-by-sa"))continue;
            String source="https://www.inaturalist.org/taxa/"+taxon.getLong("id");
            String title=taxon.optString("preferred_common_name",taxon.optString("name"));
            String description="Nombre científico: "+taxon.optString("name")+". Nombre común: "+title+". Grupo: "+taxon.optString("iconic_taxon_name")+".";
            WebMemory.rememberExternal(context,topic,title,description,source);
            return pack(title,photo.optString("square_url"),source,license,photo.optString("attribution"));
        }
        return null;
    }
    private static JSONObject pack(String title,String thumbnail,String source,String license,String author)throws JSONException{
        JSONObject info=new JSONObject();info.put("thumburl",thumbnail);info.put("descriptionurl",source);info.put("referenceTitle",title);
        JSONObject metadata=new JSONObject();
        metadata.put("LicenseShortName",new JSONObject().put("value",license));
        metadata.put("Artist",new JSONObject().put("value",author));
        info.put("extmetadata",metadata);return info;
    }
}
