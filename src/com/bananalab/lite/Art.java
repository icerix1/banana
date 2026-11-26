package com.bananalab.lite;
import android.graphics.*;

final class Art {
    static int mix(int color,int other,float amount){
        int r=(int)(Color.red(color)*(1-amount)+Color.red(other)*amount);
        int g=(int)(Color.green(color)*(1-amount)+Color.green(other)*amount);
        int b=(int)(Color.blue(color)*(1-amount)+Color.blue(other)*amount);
        return Color.rgb(r,g,b);
    }
    static void oval(Canvas c,Paint p,float left,float top,float right,float bottom,int color){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(left,top,right,bottom,mix(color,Color.WHITE,.24f),mix(color,0xff253647,.18f),Shader.TileMode.CLAMP));
        c.drawOval(left,top,right,bottom,p);p.setShader(null);
        p.setColor(mix(color,0xff283144,.55f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.5f);
        c.drawOval(left,top,right,bottom,p);p.setStyle(Paint.Style.FILL);
    }
    static void round(Canvas c,Paint p,float l,float t,float r,float b,float radius,int color){
        p.setShader(new LinearGradient(l,t,r,b,mix(color,Color.WHITE,.18f),mix(color,0xff26314a,.18f),Shader.TileMode.CLAMP));
        c.drawRoundRect(l,t,r,b,radius,radius,p);p.setShader(null);
        p.setColor(mix(color,0xff26314a,.55f));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.5f);
        c.drawRoundRect(l,t,r,b,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    static void eye(Canvas c,Paint p,float x,float y){
        p.setColor(0xfffcf9ee);c.drawOval(x-1.5f,y-1.8f,x+1.5f,y+1.8f,p);
        p.setColor(0xff344451);c.drawOval(x-.8f,y-1.2f,x+.8f,y+1.4f,p);
        p.setColor(Color.WHITE);c.drawCircle(x-.3f,y-.6f,.35f,p);
    }
}
