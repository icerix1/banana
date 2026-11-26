package com.bananalab.lite;

import android.content.Context;
import java.net.URLEncoder;
import org.json.*;

final class MuseumSources {
    static JSONObject reference(Context context,String topic) {
        String query=VisualMemory.translate(topic);
        try {
            JSONObject result=new JSONObject(new String(VisualMemory.request(
                "https://openaccess-api.clevelandart.org/api/artworks/?q="+URLEncoder.encode(query,"UTF-8")+
                "&has_image=1&cc0=1&limit=3",262144),"UTF-8"));
            JSONObject info=cleveland(result);
            if(info!=null){remember(context,topic,info);return info;}
        }catch(Exception ignored){}
        try {
            JSONObject result=new JSONObject(new String(VisualMemory.request(
                "https://collectionapi.metmuseum.org/public/collection/v1.1/search?hasImages=true&q="+
                URLEncoder.encode(query,"UTF-8")+"&limit=3",65536),"UTF-8"));
            JSONArray ids=result.optJSONArray("objectIDs");
            if(ids==null)return null;
            for(int i=0;i<Math.min(ids.length(),3);i++){
                long id=ids.getLong(i);
                JSONObject object=new JSONObject(new String(VisualMemory.request(
                    "https://collectionapi.metmuseum.org/public/collection/v1/objects/"+id,131072),"UTF-8"));
                JSONObject info=met(object);
                if(info!=null){remember(context,topic,info);return info;}
            }
        }catch(Exception ignored){}
        return null;
    }

    static JSONObject cleveland(JSONObject result)throws JSONException{
        JSONArray records=result.optJSONArray("data");
        if(records==null)return null;
        for(int i=0;i<records.length();i++){
            JSONObject object=records.getJSONObject(i);
            if(!"CC0".equals(object.optString("share_license_status")))continue;
            JSONObject images=object.optJSONObject("images");
            if(images==null)continue;
            JSONObject web=images.optJSONObject("web");
            if(web==null||web.optString("url").isEmpty()||web.optLong("filesize",0)>1048576)continue;
            String title=object.optString("title");
            String source=object.optString("url","https://www.clevelandart.org/art/"+object.optString("accession_number"));
            return pack(title,web.optString("url"),source,object.optString("tombstone"),"Cleveland Museum of Art · CC0");
        }
        return null;
    }

    static JSONObject met(JSONObject object)throws JSONException{
        if(!object.optBoolean("isPublicDomain")||object.optString("primaryImageSmall").isEmpty())return null;
        return pack(object.optString("title"),object.optString("primaryImageSmall"),object.optString("objectURL"),
            object.optString("artistDisplayName")+" · "+object.optString("objectDate")+" · "+object.optString("medium"),
            "The Metropolitan Museum of Art · Open Access / CC0");
    }

    private static JSONObject pack(String title,String image,String source,String description,String credit)throws JSONException{
        JSONObject info=new JSONObject();
        info.put("referenceTitle",title);info.put("thumburl",image);
        info.put("descriptionurl",source);info.put("description",description);
        JSONObject metadata=new JSONObject();
        metadata.put("LicenseShortName",new JSONObject().put("value","CC0"));
        metadata.put("Artist",new JSONObject().put("value",credit+" · "+description));
        info.put("extmetadata",metadata);
        return info;
    }

    private static void remember(Context context,String topic,JSONObject info)throws Exception{
        WebMemory.rememberExternal(context,topic,info.optString("referenceTitle"),info.optString("description"),info.optString("descriptionurl"));
    }
}
