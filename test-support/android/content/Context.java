package android.content;
/** Host-only test double; never included by build.sh. */
public abstract class Context {
 public android.content.res.AssetManager getAssets(){return null;}
 public abstract SharedPreferences getSharedPreferences(String name,int mode);
}
