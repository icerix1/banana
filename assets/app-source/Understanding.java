package com.bananalab.lite;

import java.util.*;
import java.util.regex.*;

final class Understanding {
    static final String VERBS="besa|bese|besar|besan|besen|nad\\w*|and\\w*|convers\\w*|salt\\w*|gir\\w*|bail\\w*|salud\\w*|corr\\w*|camin\\w*|avanz\\w*|rebot\\w*|flot\\w*|duerm\\w*|dorm\\w*|acerc\\w*|habl\\w*|charl\\w*|aplaud\\w*|celebr\\w*|agach\\w*|vol\\w*|vuel\\w*|cant\\w*|rie|reir|llor\\w*|comer|come|beb\\w*|piens\\w*|sienta|mir\\w*|pele\\w*|abraz\\w*";
    private static final Set<String> SKIP=new HashSet<>(Arrays.asList("izquierda","derecha","mano","vez","escena","video","cosa","algo","que","luego","despues","playa","bosque","parque","ciudad","montana","noche","dia","camisa","ropa","sombrero","corona","bufanda","lentes","anteojos","gafas","bigote"));
    static List<String> entities(String prompt){
        String text=Knowledge.normalize(prompt);
        LinkedHashSet<String> result=new LinkedHashSet<>();
        Matcher subjects=Pattern.compile("(?:^|[.,;!?]|\\by\\b)\\s*(?:(?:un|una|el|la)\\s+)?([a-z]+(?:\\s+[a-z]+){0,2}?)\\s+(?=(?:"+VERBS+")\\b)").matcher(text);
        while(subjects.find())add(result,subjects.group(1));
        Matcher articles=Pattern.compile("\\b(?:un|una|el|la|al)\\s+([a-z]{3,})").matcher(text);
        while(articles.find())add(result,articles.group(1));
        Matcher known=Pattern.compile("\\b(argentina|mexico|espana|brasil|japon|chile|peru|uruguay|estados unidos|saturno|marte|jupiter|tierra|luna|sol|paisaje|montana|ciudad|bosque|playa)\\b").matcher(text);
        while(known.find())add(result,known.group());
        if(result.isEmpty()&&text.trim().matches("[a-z]+(?: [a-z]+){0,2}"))add(result,text.trim());
        return new ArrayList<>(result);
    }
    private static void add(Set<String> result,String value){
        String cleaned=value.replaceAll("\\s+con\\s+.*$", "").replaceAll("\\s+(?:se|le)$","").replaceAll("\\b(rojo|roja|verde|azul|grande|pequeno|pequena|amarillo|amarilla|alto|alta|bajo|baja|gordo|gorda|delgado|delgada|triste|feliz)\\b","").trim().replaceAll("\\s+"," ");
        cleaned=cleaned.replaceAll("\\b(negro|negra|blanco|blanca|gris|rosa|marron|naranja|violeta|rojo|roja|verde|azul)\\b", "").trim();
        if(!cleaned.isEmpty()&&!SKIP.contains(cleaned)&&!cleaned.matches(VERBS)&&result.size()<4)result.add(cleaned);
    }
    static void prepare(String text){for(String name:entities(text))Knowledge.generic(name);}
}
