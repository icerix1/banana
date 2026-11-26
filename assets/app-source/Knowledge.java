package com.bananalab.lite;

import java.text.Normalizer;
import java.util.*;

final class Knowledge {
    static final class Profile {
        final String shape;
        final int color;
        String traits="";
        Profile(String shape,int color){this.shape=shape;this.color=color;}
    }
    private static final Map<String,Profile> profiles=new HashMap<>();
    static String normalize(String text){
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}","");
    }
    static String classify(String topic,String description){
        String name=normalize(topic),facts=normalize(description);
        if(name.matches("pais|argentina|mexico|espana|brasil|japon|chile|peru|uruguay|estados unidos"))return "country";
        if(name.matches("planeta|saturno|marte|jupiter|tierra|luna|sol|neptuno|urano|venus|mercurio"))return "planet";
        String intro=facts.substring(0,Math.min(300,facts.length()));
        if(intro.matches("(?s).*\\b(pais soberano|es un pais|estado soberano|republica|nacion)\\b.*"))return "country";
        if(intro.matches("(?s).*\\b(planeta|satelite natural|estrella)\\b.*"))return "planet";
        if(name.matches("persona|hombre|mujer|nino|nina|astronauta")||intro.matches("(?s).*\\b(persona|humano|humanos|hombre|mujer|actor|actriz|actress|artist|pintor|presidente|politico|futbolista)\\b.*"))return "human";
        if(name.matches("pajaro|loro|aguila|pinguino|pato|gallina")||facts.matches("(?s).*\\b(bird|birds|ave|aves|pajaro|pajaros|aves de)\\b.*"))return "bird";
        if(name.matches("pez|tiburon|salmon")||facts.matches("(?s).*\\b(fish|pez|peces|actinopterygii)\\b.*"))return "fish";
        if(name.matches("gato|perro|conejo|leon|tigre|cebra|jirafa|elefante|caballo|zorro|oso|vaca|dinosaurio|tortuga")||facts.contains("mammal")||facts.contains("mamifero")||facts.contains("mammalia")||facts.contains("reptil"))return "animal";
        if(facts.contains("insect")||name.matches("insecto|mariposa|hormiga|abeja"))return "insect";
        return "object";
    }
    static synchronized void generic(String name){
        if(name.matches("paisaje|montana|ciudad|bosque|playa|banana|platano|pepino|tomate|gato|gata|perro|perra|conejo|coneja|robot"))return;
        String normalized=normalize(name);
        if(!profiles.containsKey(normalized))profiles.put(normalized,new Profile(classify(name,""),0xff8baacb));
    }

    static synchronized void learn(String topic,String text){
        String facts=normalize(text);
        String shape=classify(topic,text);
        int color=facts.contains("negro")?0xff394458:facts.contains("rojo")?0xffde7270:
            facts.contains("verde")?0xff74b584:facts.contains("azul")?0xff74a9d6:
            facts.contains("blanco")?0xffe3e8ed:0xffc7a580;
        Profile profile=new Profile(shape,color);
        if(facts.contains("trompa")||facts.contains("trunk"))profile.traits="elefante";
        if(facts.contains("cuello largo")||facts.contains("long neck"))profile.traits="jirafa";
        if(facts.contains("rayas")||facts.contains("stripes"))profile.traits=normalize(topic).contains("cebra")?"cebra":"tigre";
        profiles.put(normalize(topic),profile);
    }
    static synchronized void visual(String name){if(name.matches("paisaje|montana|ciudad|bosque|playa"))return;if(!profiles.containsKey(normalize(name)))profiles.put(normalize(name),new Profile(name.matches("persona|hombre|mujer|nino|nina|astronauta")?"human":"object",0xff82b2c7));}
    static synchronized Profile get(String name){return profiles.get(normalize(name));}
    static synchronized String names(){
        StringBuilder pattern=new StringBuilder();
        for(String name:profiles.keySet())pattern.append("|").append(java.util.regex.Pattern.quote(name));
        return pattern.toString();
    }
    static synchronized void clear(){profiles.clear();}
    static int score(String query,String title,String body){
        String normalizedTitle=normalize(title),normalizedBody=normalize(body);
        int score=0;
        for(String term:normalize(query).split("[^a-z0-9]+")){
            if(term.length()<3)continue;
            if(normalizedTitle.contains(term))score+=5;
            if(normalizedBody.contains(term))score++;
        }
        return score;
    }
}
