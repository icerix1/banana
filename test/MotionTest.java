import com.bananalab.lite.*;
import java.io.*;
public class MotionTest {
 public static void main(String[] args)throws Exception{
  Motion model;try(InputStream in=new FileInputStream("banana-lite/assets/motion.bin")){model=new Motion(in);}
  for(int a=0;a<3;a++)for(int i=0;i<=100;i++){
   float t=i/100f;float[] got=model.predict(a,t);float[] expected=new float[]{a==0?t*.8f:0,a==1?-(float)Math.sin(Math.PI*t)*.8f:0,a==2?(float)Math.sin(Math.PI*t)*.8f:0};
   for(int k=0;k<3;k++)if(Float.isNaN(got[k])||Math.abs(got[k]-expected[k])>.06)throw new AssertionError("Trayectoria incorrecta");
  }
  for(int size:new int[]{32,64}){
   int frames=size==32?8:16;try(OutputStream out=new FileOutputStream("/tmp/banana-lite-test-"+size+".gif");GifWriter writer=new GifWriter(out,size)){
    int[] pixels=new int[size*size];
    for(int f=0;f<frames;f++){
     float[] move=model.predict(1,f/(float)(frames-1));int y=(int)(size*.6+move[1]*size*.2);
     for(int at=0;at<pixels.length;at++){int x=at%size,z=at/size;pixels[at]=(x>size/4&&x<size*3/4&&z>y-4&&z<y+4)?0xffffcc00:0xffeeeeff;}
     writer.frame(pixels,f%2==0?12:13);
    }
   }
  }
  try{new Motion(new ByteArrayInputStream(new byte[10]));throw new AssertionError("Acepto modelo invalido");}catch(IOException expected){}
  System.out.println("PASS: 303 predicciones Java contra datos de validacion, GIF 32/64 y rechazo de modelo invalido.");
 }
}
