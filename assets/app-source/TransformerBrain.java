package com.bananalab.lite;
import android.content.Context;
final class TransformerBrain{
 private static BananaTransformer model;private static String loaded="";
 static synchronized BananaTransformer load(Context context)throws Exception{String revision=ModelDepot.revision(context,"chat");if(model==null||!loaded.equals(revision)){model=new BananaTransformer(ModelDepot.open(context,"chat"),context.getAssets().open("chat34/vocab.txt"),context.getAssets().open("chat34/tokenizer.json"));loaded=revision;}return model;}
 static BananaTransformer.Result reply(Context context,String text,String user,String answer)throws Exception{return load(context).generate(text,user,answer);}
 static BananaTransformer.Result reply(Context c,String text,java.util.List<String[]> history)throws Exception{return load(c).generate(text,history);}
}
