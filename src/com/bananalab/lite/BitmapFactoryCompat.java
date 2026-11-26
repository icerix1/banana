package com.bananalab.lite;
import android.content.*;import android.graphics.*;import java.io.*;
final class BitmapFactoryCompat{
 static Bitmap decode(Context c,String key){if(!key.matches("[a-z0-9_-]{1,60}"))return null;Bitmap b=BitmapFactory.decodeFile(new File(c.getFilesDir(),key+".png").getAbsolutePath());if(b!=null&&(b.getWidth()!=128||b.getHeight()!=128)){Bitmap scaled=Bitmap.createScaledBitmap(b,128,128,true);b.recycle();return scaled;}return b;}
}
