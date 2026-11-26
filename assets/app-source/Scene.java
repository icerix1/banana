package com.bananalab.lite;

import android.graphics.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

final class Scene {
    private static final String ACTOR = "banana|platano|pepino|tomate|gato|gata|perro|perra|conejo|coneja|robot|persona|oso|zorro|criatura|hombre|mujer|nino|nina|astronauta";
    private static final Pattern TOKENS = Pattern.compile("\\b("+ACTOR+"|salt\\w*|gir\\w*|bail\\w*|salud\\w*|corr\\w*|camin\\w*|avanz\\w*|rebot\\w*|flot\\w*|izquierda|derecha|duerm\\w*|dorm\\w*|acerc\\w*|habl\\w*|charl\\w*|aplaud\\w*|celebr\\w*|agach\\w*)\\b");
    private final LinkedHashMap<String,List<String>> cast = new LinkedHashMap<>();
    private final boolean night, beach, park;
    private Map<String,Bitmap[]> mouths=new HashMap<>();private Map<String,Bitmap[]> views=new HashMap<>();private float cachedPositionTime=Float.NaN;private Map<String,Float> cachedPositions;private float drawX,drawY,drawScaleX,drawScaleY,drawRotation;private float frameTime;private Motion frameMotion;private SceneDraft source;SceneDraft source(){return source;}private String cameraMode="auto";private boolean sequence=true;private String lead="",reveal="";private ShotPlan camera;private Map<String,Bitmap[]> poses=new HashMap<>();
    void setSequence(boolean value){sequence=value;}
    String shotLabel(float time){if(!cameraMode.equals("auto"))return cameraMode.equals("wide")?"Plano general":"Cámara: "+cameraMode.substring(cameraMode.indexOf(':')+1);return !sequence?"Plano continuo":time<.28f?"Toma 1/3 · ambiente":time<.65f?"Toma 2/3 · primer plano":"Toma 3/3 · revelación";}
    private Map<String,Bitmap> neural=new HashMap<>(),alternate=new HashMap<>();private NeuralBackdrop.Frame[] backdrop;private float actorOpacity=1;
    int neuralCount(){return neural.size();}
    void setNeural(NeuralSprites.Batch batch){neural=new HashMap<>(batch.images);alternate=new HashMap<>(batch.alternate);poses=new HashMap<>(batch.poses);backdrop=batch.backdrop;rigs=batch.rigs;views=batch.views;mouths=batch.mouths;objects=batch.objects;}
    private Map<String,RigRenderer> rigs=new HashMap<>();private Map<String,Bitmap[]> objects=new HashMap<>();private final Map<String,SceneDraft.Actor> specs=new HashMap<>();private boolean tree=false;
    private int beats = 1;private String actorWords="";private final String wordsForBackground;

    Scene(String prompt) {
        String words = Normalizer.normalize(prompt.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        Understanding.prepare(prompt);
        wordsForBackground=words;
        night = words.contains("noche") || words.contains("luna");
        beach = words.contains("playa");
        park = words.contains("parque") || words.contains("bosque");
        java.util.List<String> targets=new ArrayList<>();
        Matcher references=Pattern.compile("\\b(?:hacia|hasta|a|al)\\s+(?:(?:el|la|un|una)\\s+)?("+ACTOR+")\\b").matcher(words);
        while(references.find())targets.add(references.group(1));
        words=words.replaceAll("\\band(?:a|ar)\\b","camina")
            .replaceAll("\\bconvers(?:a|ar)\\b","habla")
            .replaceAll("\\bfestej(?:a|ar)\\b","celebra")
            .replaceAll("\\bda vueltas\\b","gira")
            .replaceAll("\\blevanta (?:la|una) mano\\b","saluda")
            .replaceAll("\\b(?:va|camina) (?:hacia|hasta|a) (?:el|la|un|una) ("+ACTOR+")\\b","acerca");
        // Spanish also puts the subject after the verb: "va el pepino a hablarle".
        words = words.replaceAll("\\bva\\s+(?:el|la|un|una)\\s+("+ACTOR+")\\s+a\\s+hablar(?:le)?\\b", "$1 acerca habla");
        words = words.replaceAll("\\bva\\s+a\\s+hablar(?:le)?\\b", "acerca habla");
        words = words.replaceAll("\\bse\\s+acerca\\s+(?:a|al)\\s+(?:la\\s+|el\\s+)?("+ACTOR+")", "acerca");
        String actorPattern=ACTOR+Knowledge.names();
        Pattern tokens=Pattern.compile("\\b("+actorPattern+"|"+Understanding.VERBS+"|izquierda|derecha)\\b");
        Matcher matcher = tokens.matcher(words);
        List<String> current = null;
        while (matcher.find()) {
            String word = matcher.group();
            if (word.matches(actorPattern)) {
                if (word.equals("platano")) word = "banana";
                if ((word.equals("gata")||word.equals("perra")||word.equals("coneja"))) word = word.substring(0,word.length()-1)+"o";
                if (!cast.containsKey(word) && cast.size() >= 4) { current = null; continue; }
                if (!cast.containsKey(word)) cast.put(word,new ArrayList<String>());
                current = cast.get(word);
            } else {
                if (current == null) {
                    if (!cast.isEmpty()) continue;
                    current = new ArrayList<>();
                    cast.put("banana",current);
                }
                if ((word.equals("izquierda") || word.equals("derecha")) && !current.isEmpty()
                        && current.get(current.size()-1).matches("camin.*|corr.*|avanz.*")) {
                    current.set(current.size()-1,current.get(current.size()-1)+":"+word);
                } else if (current.size()<4) current.add(word);
                beats = Math.max(beats,current.size());
            }
        }
        for(String target:targets)if(!cast.containsKey(target)&&cast.size()<4)cast.put(target,new ArrayList<String>());
        if (cast.isEmpty()&&!words.matches("(?s).*\\b(paisaje|montana|ciudad|bosque|playa)\\b.*")) throw new IllegalArgumentException("Escribí un personaje o un movimiento. Ej.: una banana salta y un gato saluda en la playa.");
    }

    Scene(OwnPlan plan){this(SceneDraft.fromPlan(plan));}

    Scene(SceneDraft draft){
        this(draft.cast.isEmpty()?"paisaje "+draft.setting:draft.text());if(!draft.cast.isEmpty()){lead=draft.cast.get(0).name;if(draft.cast.size()>1)reveal=draft.cast.get(draft.cast.size()-1).name;}source=draft;tree=draft.sceneryTree;cast.clear();cameraMode=draft.camera;beats=1;for(int i=0;i<draft.actions.size();i++)beats=Math.max(beats,draft.slot(i)+1);
        java.util.List<SceneDraft.Actor> ordered=new ArrayList<>(draft.cast);java.util.Collections.sort(ordered,(a,b)->Boolean.compare(b.behind,a.behind));
        for(SceneDraft.Actor actor:ordered){specs.put(actor.name,actor);tree|=actor.relation.contains("arbol");List<String> moves=new ArrayList<>();for(int i=0;i<beats;i++)moves.add("espera");cast.put(actor.name,moves);}
        for(int i=0;i<draft.actions.size();i++)cast.get(draft.cast.get(draft.actors.get(i)).name).set(draft.slot(i),draft.actions.get(i));
    }

    String describe() {
        StringBuilder result=new StringBuilder("Escena interpretada");
        for(Map.Entry<String,List<String>> actor:cast.entrySet())
            result.append("\n").append(actor.getKey()).append(" — ")
                .append(actor.getValue().isEmpty()?"espera":android.text.TextUtils.join(", ",actor.getValue()));
        result.append("\nDibujo local · hasta 4 personajes");
        return result.toString();
    }

    private Timeline.State state(String name,float time){
        List<String> moves=cast.get(name);SceneDraft.Actor spec=specs.get(name);float x=spec==null?50:spec.x;int index=new ArrayList<>(cast.keySet()).indexOf(name);
        Timeline.State result=Timeline.at(moves,time,x,approachDistance(index,x));if(source!=null){if(cachedPositions==null||cachedPositionTime!=time){cachedPositions=ScenePositions.at(source,time);cachedPositionTime=time;}result.shift=cachedPositions.get(name)-x;}return result;
    }
    float actorVisibility(String name,float time){if(!cast.containsKey(name))return 0;float alpha=state(name,time).alpha;if(sequence&&cameraMode.equals("auto")&&name.equals(reveal))alpha*=ShotPlan.at(time,50,67,true).reveal;return alpha;}
    float actorX(String name,float time){SceneDraft.Actor spec=specs.get(name);return (spec==null?50:spec.x)+state(name,time).shift;}
    private ShotPlan cameraAt(float time){
        if(cameraMode.equals("wide"))return ShotPlan.view(1,50,50);
        if(cameraMode.startsWith("follow:")||cameraMode.startsWith("close:")){String target=cameraMode.substring(cameraMode.indexOf(':')+1);SceneDraft.Actor actor=specs.get(target);if(actor!=null)return ShotPlan.view(cameraMode.startsWith("close:")?1.6f:1.25f,Math.max(24,Math.min(76,actorX(target,time))),actor.y-8);}
        SceneDraft.Actor primary=specs.get(lead);return ShotPlan.at(time,primary==null?50:primary.x,primary==null?67:primary.y,sequence);
    }

    int frames() { return source==null?Math.max(sequence?96:48,beats*24):SceneTiming.frames(source); }
    static float ease(float t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}

    private static void oval(Canvas canvas, Paint paint, float x, float y, float w, float h, int color) {
        if(w>5&&h>5&&Color.alpha(color)==255)Art.oval(canvas,paint,x,y,x+w,y+h,color);
        else{paint.setColor(color);canvas.drawOval(x,y,x+w,y+h,paint);}
    }

    synchronized void draw(Canvas c, int size, Motion motion, float time) {frameTime=time;frameMotion=motion;
        c.save();c.scale(size/100f,size/100f);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        if(backdrop==null||neural.size()!=cast.size()){c.drawColor(0xff11171e);p.setColor(0xffb7c1c5);p.setTextSize(6);c.drawText("Generá tu escena neuronal",9,50,p);c.restore();return;}
        camera=cameraAt(time);c.translate(50,50);c.scale(camera.zoom,camera.zoom);c.translate(-camera.x,-camera.y);
        c.scale(1.014f,1.014f,50,50);c.translate((float)Math.sin(time*Math.PI*2)*.55f,(float)Math.cos(time*Math.PI*2)*.3f);drawBackground(c,p,time,false);drawObjects(c,p,time,false);
        int index=0;boolean treePainted=false;
        for(Map.Entry<String,List<String>> entry:cast.entrySet()) {
            SceneDraft.Actor spec=specs.get(entry.getKey());
            if(tree&&!treePainted&&(spec==null||!spec.behind)){drawBackground(c,p,time,true);treePainted=true;}
            float x=spec==null?(index+1)*100f/(cast.size()+1):spec.x;
            actorWords=spec==null?Appearance.segment(wordsForBackground,entry.getKey()):spec.look;
            float scale=(cast.size()>2 ? .75f : .95f)*(spec==null?1:spec.size)*(spec==null&&actorWords.matches("(?s).*\\b(pequeno|pequena|chico|chica)\\b.*")?.75f:1f);
            Timeline.State timed=state(entry.getKey(),time);String move=timed.action;float t=timed.progress;
            float progress=ease(t);float wave=(float)Math.sin(t*Math.PI*2);
            float lift=0,spin=0,shift=timed.shift,arm=0;
            if(move.startsWith("salt")||move.startsWith("rebot")) lift=motion.predict(1,t)[1]*20;
            if((move.startsWith("vol")||move.startsWith("vuel"))){lift=-10*(float)Math.sin(Math.PI*t);arm=60*wave;}
            if(move.startsWith("nad")){shift=9*t;spin=8*wave;arm=40*wave;}
            if(move.startsWith("flot")) lift=-7*(1-(float)Math.cos(t*Math.PI*2));
            if(move.startsWith("gir")) spin=motion.predict(2,t)[2]*65;
            if(move.startsWith("bail")) {spin=16*wave;arm=55*wave;lift=-4*Math.abs(wave);}
            if(move.startsWith("salud")) arm=65+25*wave;
            if(Timeline.walk(move)) {
                
                lift=-.6f*(1-(float)Math.cos(t*Math.PI*4));arm=20*wave;
            }
            if(Timeline.entry(move)) {lift=-.6f*(1-(float)Math.cos(t*Math.PI*4));arm=35*wave;}
            if(move.startsWith("aplaud")) arm=-40+35*wave;
            if(move.startsWith("celebr")) {arm=100;lift=-7*Math.abs(wave);}
            if(move.startsWith("sienta")) lift=5*(float)Math.sin(Math.PI*t);
            if(move.startsWith("rie")){lift=-.6f*(1-(float)Math.cos(t*Math.PI*4));arm=20*wave;}
            if(move.startsWith("agach")) lift=6*(float)Math.sin(Math.PI*t);
            if(move.equals("sienta"))lift=-6;
            if(move.startsWith("duerm")||move.startsWith("dorm")) spin=70*(float)Math.sin(Math.PI*t);
            spin+=1.5f*(float)Math.sin(time*Math.PI*2+index);lift+=.3f*(float)Math.sin(time*Math.PI*4+index);if(Timeline.walk(move)){spin=0;lift=0;}actorOpacity=actorVisibility(entry.getKey(),time);if(actorOpacity<=.001f){index++;continue;}
            oval(c,p,actorX(entry.getKey(),time)-9*scale,spec==null?81:81+spec.y-67,18*scale,4,Color.argb(Math.round(64*actorOpacity),81,46,40));
            c.save();
            float worldX=(move.equals("entraizq")||move.equals("entrader")||move.equals("saleizq")||move.equals("saleder"))?x+shift:Math.max(12,Math.min(88,x+shift));
            float[] transform=pose(entry.getKey(),time,motion);drawX=transform[0];drawY=transform[1];drawScaleX=transform[2];drawScaleY=transform[3];drawRotation=transform[4];c.translate(drawX,drawY);c.scale(drawScaleX,drawScaleY);c.rotate(drawRotation);
            drawActor(c,p,entry.getKey(),arm,move,t);drawKissMouth(c,p,entry.getKey(),time);
            c.restore();
            index++;
        }
        drawObjects(c,p,time,true);drawKissHearts(c,p,time);if(tree&&!treePainted)drawBackground(c,p,time,true);
        c.restore();
    }

    static boolean approaches(String move) { return move.startsWith("acerc"); }

    private float approachDistance(int index, float x) {
        if(source!=null&&index>=0&&index<cast.size()){String name=new ArrayList<>(cast.keySet()).get(index);for(int j=0;j<source.actions.size();j++)if(source.cast.get(source.actors.get(j)).name.equals(name)&&source.actions.get(j).equals("acercaactor")){SceneDraft.Actor to=source.get(source.recipient(j));if(to!=null){float delta=to.x-x;return Math.signum(delta)*Math.max(0,Math.abs(delta)-22);}}for(int i=0;i<source.actions.size();i++)if(source.cast.get(source.actors.get(i)).name.equals(name)&&source.actions.get(i).equals("acerca")&&!source.target(i).isEmpty()){SceneDraft.Prop prop=source.prop(source.target(i));if(prop!=null)return prop.x-22-x;}}
        if (cast.size() < 2) return x < 50 ? 12 : -12;
        int target = index == 0 ? 1 : index-1;
        String targetName=new ArrayList<>(cast.keySet()).get(target);SceneDraft.Actor targetSpec=specs.get(targetName);float destination = targetSpec==null?(target+1)*100f/(cast.size()+1):targetSpec.x;
        float delta = destination-x;
        return Math.signum(delta)*Math.max(0,Math.abs(delta)-22);
    }

    static int bodyColor(String type,String look){
        int fallback=type.equals("criatura")?0xff78b1de:type.equals("oso")?0xffc49161:type.equals("zorro")?0xffe99a56:type.equals("persona")?0xfff1c79f:type.equals("tomate")?0xffed6569:type.equals("pepino")?0xff63bc76:type.equals("gato")?0xffdb9b63:type.equals("perro")?0xffb88770:type.equals("conejo")?0xfff3dce8:type.equals("robot")?0xff8cc8df:0xfff8d061;
        return Appearance.color(look,fallback);
    }

    private void drawBackground(Canvas c,Paint p,float time,boolean foreground){
        float blend=.5f-.5f*(float)Math.cos(time*Math.PI*2);RectF bounds=new RectF(-2,-2,102,102);p.setShader(null);p.setFilterBitmap(true);p.setAlpha(255);if(source!=null&&source.lighting.equals("warm"))p.setColorFilter(new ColorMatrixColorFilter(new float[]{1.08f,0,0,0,7,0,1.02f,0,0,3,0,0,.88f,0,0,0,0,0,1,0}));c.drawBitmap(foreground?backdrop[0].foreground:backdrop[0].background,null,bounds,p);p.setAlpha(Math.round(blend*255));c.drawBitmap(foreground?backdrop[1].foreground:backdrop[1].background,null,bounds,p);p.setAlpha(255);p.setColorFilter(null);if(!foreground&&source!=null&&source.fog>0){p.setColor(Color.argb(Math.round(source.fog*255),173,188,191));c.drawRect(bounds,p);}
    }
    private void drawObjects(Canvas c,Paint p,float time,boolean front){
        if(source==null)return;for(SceneDraft.Prop prop:source.props){Bitmap[] images=objects.get(prop.id);if(images==null)continue;boolean inFront=false;SceneDraft.Actor holder=null;for(SceneDraft.Actor a:source.cast)if(a.relation.equals("detras "+prop.id))inFront=true;
        ObjectTransfer.State transfer=ObjectTransfer.at(source,prop.id,time,beats);holder=source.get(transfer.owner());if(holder!=null)inFront=true;
        if(inFront!=front)continue;float height=prop.type.equals("puerta")?48:prop.type.equals("mesa")?30:36,width=prop.type.equals("mesa")?36:28;RectF box=new RectF(prop.x-width/2,83+prop.y-67-height,prop.x+width/2,83+prop.y-67);
        if(holder!=null){float[] point=objectPoint(prop.id,time);box.set(point[0]-5,point[1]-4,point[0]+5,point[1]+4);}float open=SceneObjects.open(source,prop.id,time,beats);p.setAlpha(holder==null?255:Math.round(actorVisibility(holder.name,time)*255));c.drawBitmap(images[0],null,box,p);if(open>0){p.setAlpha(Math.round(open*255));c.drawBitmap(images[1],null,box,p);}p.setAlpha(255);}
    }
    float[] pose(String name,float time,Motion motion){float[] contact=KissMotion.pose(source,name,time);if(contact!=null)return contact;SceneDraft.Actor a=specs.get(name);Timeline.State st=state(name,time);String move=st.action;float t=st.progress,wave=(float)Math.sin(t*Math.PI*2),scale=ActorGeometry.scale(a,cast.size()),lift=0,spin=0;if(move.startsWith("salt")||move.startsWith("rebot"))lift=motion==null?AnimationProfile.jump(t):motion.predict(1,t)[1]*20;if(move.startsWith("salt")||move.startsWith("rebot"))lift=ProjectRuntime.jump(t,AppJavaWorkspace.jumpLift(JavaFunction.lift(t,lift)));if(move.startsWith("vol")||move.startsWith("vuel"))lift=-10*(float)Math.sin(t*Math.PI);if(move.startsWith("nad"))spin=8*wave;if(move.startsWith("flot"))lift=-7*(1-(float)Math.cos(t*Math.PI*2));if(move.startsWith("gir"))spin=motion==null?360*t:motion.predict(2,t)[2]*65;if(move.startsWith("bail")){spin=ProjectRuntime.sway(t,AnimationProfile.sway(t));lift=-4*Math.abs(wave);}if(move.startsWith("celebr"))lift=-7*Math.abs(wave);if(move.equals("sienta"))lift=-6;if(move.startsWith("agach"))lift=6*(float)Math.sin(Math.PI*t);if(move.startsWith("duerm")||move.startsWith("dorm"))spin=70*(float)Math.sin(Math.PI*t);int index=new ArrayList<>(cast.keySet()).indexOf(name);spin+=1.5f*(float)Math.sin(time*Math.PI*2+index);lift+=.3f*(float)Math.sin(time*Math.PI*4+index);if(Timeline.walk(move)){spin=0;lift=0;}float x=actorX(name,time);if(!(move.equals("entraizq")||move.equals("entrader")||move.equals("saleizq")||move.equals("saleder")))x=Math.max(12,Math.min(88,x));return new float[]{x,80-14*scale+a.y-67+lift,scale*Appearance.width(a.look),scale*Appearance.height(a.look),spin};}
    private void drawKissMouth(Canvas c,Paint p,String name,float time){int i=KissMotion.active(source,name,time);if(i<0||!specs.get(name).reference.isEmpty())return;float t=time*beats-source.slot(i);if(KissMotion.envelope(t)<.5f)return;float[] m=KissMotion.mouth(specs.get(name),KissMotion.direction(source,name,i));float x=-20+m[0]/64*40,y=-30+m[1]/64*46;int dir=KissMotion.direction(source,name,i);p.setColor(0xffb84a65);p.setAlpha(255);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.75f);c.drawLine(x-dir*1.3f,y-1,x+dir*.4f,y,p);c.drawLine(x+dir*.4f,y,x-dir*1.3f,y+1,p);p.setStyle(Paint.Style.FILL);}
    private void drawKissHearts(Canvas c,Paint p,float time){if(source==null)return;for(int i=0;i<source.actions.size();i++)if(source.actions.get(i).equals("besa")){String a=source.cast.get(source.actors.get(i)).name,b=source.recipient(i);if(KissMotion.active(source,a,time)!=i)continue;float t=time*beats-source.slot(i),e=KissMotion.envelope(t);if(e<.9f)continue;float[] m=KissMotion.mouth(source.get(a),KissMotion.direction(source,a,i)),point=ActorGeometry.world(m[0],m[1],pose(a,time,frameMotion));for(int n=0;n<3;n++){float x=point[0]+(n-1)*4,y=point[1]-8-n*3-(t-.3f)*10,size=1.5f+n*.3f;p.setColor(0xffef6986);p.setAlpha(Math.round(e*230));Path heart=new Path();heart.moveTo(x,y+size);heart.cubicTo(x-size*2,y-size,x-size*.4f,y-size*2,x,y-size*.6f);heart.cubicTo(x+size*.4f,y-size*2,x+size*2,y-size,x,y+size);c.drawPath(heart,p);}p.setAlpha(255);}}
    private int handDirection(String name,float time){int v=viewAt(name,time);SceneDraft.Actor a=specs.get(name);int direction=v==1?1:v==2?-1:a!=null&&a.x<50?1:-1;if(source!=null)for(int i=0;i<source.actions.size();i++)if(source.cast.get(source.actors.get(i)).name.equals(name)&&time*beats>=source.slot(i)&&(source.actions.get(i).equals("mira")||source.actions.get(i).equals("acercaactor")||source.actions.get(i).equals("entrega")||source.actions.get(i).equals("recibe")||source.actions.get(i).equals("habla"))){SceneDraft.Actor other=source.get(source.recipient(i));if(other!=null)direction=actorX(other.name,time)>=actorX(name,time)?1:-1;}return direction;}
    float[] hand(String name,float time){SceneDraft.Actor a=specs.get(name);return ActorGeometry.world(handDirection(name,time)>0?52:12,ActorGeometry.handY(PixelGenerator.type(a.kind)),pose(name,time,frameMotion));}
    float handX(String name,float time){return hand(name,time)[0];}
    float[] objectPoint(String id,float time){ObjectTransfer.State state=ObjectTransfer.at(source,id,time,beats);SceneDraft.Actor owner=source.get(state.owner());if(owner==null){SceneDraft.Prop p=source.prop(id);return new float[]{p.x,p.y};}float[] point=hand(owner.name,time);if(state.moving&&source.get(state.from)!=null&&source.get(state.to)!=null){float[] from=hand(state.from,time),to=hand(state.to,time);point=new float[]{from[0]*(1-state.blend)+to[0]*state.blend,from[1]*(1-state.blend)+to[1]*state.blend};}return point;}
    private float[] grip(String name,float time){if(source==null)return null;for(SceneDraft.Prop prop:source.props){ObjectTransfer.State s=ObjectTransfer.at(source,prop.id,time,beats);if(s.owner().equals(name)||s.moving&&(s.from.equals(name)||s.to.equals(name))){float[] world=objectPoint(prop.id,time);return ActorGeometry.local(world[0],world[1],drawX,drawY,drawScaleX,drawScaleY,drawRotation);}}return null;}
    private boolean holding(String name){return source!=null&&ObjectTransfer.holding(source,name,frameTime);}
    int viewAt(String name,float time){int kissing=KissMotion.active(source,name,time);if(kissing>=0&&specs.get(name).reference.isEmpty())return KissMotion.direction(source,name,kissing)>0?1:2;SceneDraft.Actor actor=specs.get(name);int v=actor==null?0:NeuralSprites.viewIndex(actor.view);if(source==null||actor==null||!actor.reference.isEmpty())return v;for(int i=0;i<source.actions.size();i++)if(source.cast.get(source.actors.get(i)).name.equals(name)&&time*beats>=source.slot(i)&&(source.actions.get(i).equals("mira")||source.actions.get(i).equals("acercaactor")||source.actions.get(i).equals("entrega")||source.actions.get(i).equals("recibe")||source.actions.get(i).equals("habla"))){SceneDraft.Actor other=source.get(source.recipient(i));if(other!=null)v=actorX(other.name,time)>=actorX(name,time)?1:2;}if(actor.view.equals("front"))for(int i=0;i<source.actions.size();i++)if(source.cast.get(source.actors.get(i)).name.equals(name)&&source.actions.get(i).equals("sorprende")&&Math.min(beats-1,(int)(time*beats))==source.slot(i))v=0;return v;}
    private Bitmap expression(String name,int view,String move,float progress){if(view==3)return null;Bitmap[] bank=mouths.get(name);Bitmap image=bank!=null&&bank[view]!=null?bank[view]:alternate.get(name);if(source!=null&&DialogueCommands.hasLine(source,name,Math.min(beats-1,(int)(frameTime*beats))))return DialogueCommands.mouth(source,name,frameTime)>.3f?image:null;return move.equals("sorprende")||(move.equals("habla")||move.equals("canta"))&&Math.sin(progress*Math.PI*6)>0?image:null;}
    private RectF faceBounds(String name,int view){SceneDraft.Actor actor=specs.get(name);int kind=actor==null?0:Math.max(0,PixelGenerator.type(actor.kind));float fx=FaceAnchor.CENTERS[kind][0],fy=FaceAnchor.CENTERS[kind][1],width=26;if(view==1||view==2){fx=32+(fx-32)*.78f;width*=.78f;}if(view==2)fx=64-fx;return new RectF(-20+(fx-width/2)/64*40,-30+(fy-8)/64*46,-20+(fx+width/2)/64*40,-30+(fy+16)/64*46);}
    private void drawActor(Canvas c,Paint p,String type,float arm,String move,float time){
        int view=viewAt(type,frameTime);Bitmap[] variants=views.get(type);Bitmap generated=variants!=null&&variants[view]!=null?variants[view]:neural.get(type);if(generated==null)return;
        RigRenderer rig=rigs.get(type+"|v:"+view);if(rig==null)rig=rigs.get(type);if(rig!=null&&(holding(type)||Timeline.walk(move)||(move.equals("saluda")||move.equals("aplaude")||move.equals("abre")||move.equals("sorprende")||move.equals("sienta")||move.equals("sostiene")||move.equals("habla")||move.equals("canta")))){
         c.save();int layer=c.saveLayerAlpha(new RectF(-24,-34,24,20),Math.round(actorOpacity*255));rig.draw(c,new RectF(-20,-30,20,16),move,time,expression(type,view,move,time),holding(type),grip(type,frameTime),view);c.restoreToCount(layer);c.restore();return;
        }
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setFilterBitmap(true);c.save();float breath=(float)Math.sin(time*Math.PI*2);c.scale(1+.009f*breath,1+.014f*breath,0,14);
        if(move.startsWith("salud")||move.startsWith("aplaud"))c.rotate(4*(float)Math.sin(time*Math.PI*4));
        RectF bounds=new RectF(-20,-30,20,16);Bitmap a=generated,b=generated;float poseBlend=0;Bitmap[] bank=poses.get(type);
        if(bank!=null&&view==0){if(Timeline.walk(move)&&bank[1]!=null&&bank[2]!=null){a=bank[1];b=bank[2];poseBlend=.5f-.5f*(float)Math.cos(time*Math.PI*4);}else if(move.matches("salud.*|aplaud.*")&&bank[3]!=null){b=bank[3];poseBlend=.5f-.5f*(float)Math.cos(time*Math.PI*2);}else if(camera!=null&&camera.shot==1&&bank[4]!=null){b=bank[4];poseBlend=.65f;}}
        int actorLayer=c.saveLayerAlpha(bounds,Math.round(actorOpacity*255));int layer=c.saveLayer(bounds,null);p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));p.setAlpha(Math.round((1-poseBlend)*255));c.drawBitmap(a,null,bounds,p);p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));p.setAlpha(Math.round(poseBlend*255));c.drawBitmap(b,null,bounds,p);p.setXfermode(null);c.restoreToCount(layer);if(Timeline.walk(move)){float edge=Math.min(1,Math.min(time*6,(1-time)*6));p.setAlpha(Math.round((1-edge)*255));c.drawBitmap(generated,null,bounds,p);}Bitmap[] expressionViews=mouths.get(type);Bitmap mouth=expressionViews!=null&&expressionViews[view]!=null?expressionViews[view]:alternate.get(type);
        if(view!=3&&mouth!=null&&(move.startsWith("habl")||move.startsWith("cant"))){float blend=.5f-.5f*(float)Math.cos(time*Math.PI*6);p.setAlpha(Math.round(blend*220));c.save();c.clipRect(faceBounds(type,view));c.drawBitmap(mouth,null,bounds,p);c.restore();}
        p.setAlpha(255);c.restoreToCount(actorLayer);c.restore();
    }
}
