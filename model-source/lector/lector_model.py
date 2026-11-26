"""Transformer causal propio del lector; misma aritmética y formato binario que BananaTransformer.java."""
import math,struct,torch,torch.nn as nn,torch.nn.functional as F
class Lector(nn.Module):
    def __init__(self,vocab,d=192,heads=6,layers=6,context=512,ff=768):
        super().__init__();self.cfg=(vocab,d,heads,layers,context,ff)
        P=lambda *s:nn.Parameter(torch.randn(*s)*0.02);Z=lambda *s:nn.Parameter(torch.zeros(*s));O=lambda *s:nn.Parameter(torch.ones(*s))
        self.embedding=P(vocab,d);self.position=P(context,d);self.blocks=nn.ModuleList()
        for _ in range(layers):
            b=nn.Module();b.n1w=O(d);b.n1b=Z(d);b.qkv=P(d,3*d);b.qkvb=Z(3*d);b.o=nn.Parameter(torch.randn(d,d)*0.02/math.sqrt(2*layers));b.ob=Z(d);b.n2w=O(d);b.n2b=Z(d);b.f1=P(d,ff);b.f1b=Z(ff);b.f2=nn.Parameter(torch.randn(ff,d)*0.02/math.sqrt(2*layers));b.f2b=Z(d);self.blocks.append(b)
        self.normWeight=O(d);self.normBias=Z(d);self.headBias=Z(vocab)  # la salida comparte la matriz de embeddings durante este entrenamiento
    def forward(self,tokens):
        vocab,d,heads,layers,context,ff=self.cfg;B,T=tokens.shape
        x=self.embedding[tokens]+self.position[:T]
        for b in self.blocks:
            n=F.layer_norm(x,(d,),b.n1w,b.n1b,1e-5);q,k,v=(n@b.qkv+b.qkvb).split(d,dim=-1)
            sh=lambda t:t.view(B,T,heads,d//heads).transpose(1,2)
            a=F.scaled_dot_product_attention(sh(q),sh(k),sh(v),is_causal=True).transpose(1,2).reshape(B,T,d)
            x=x+a@b.o+b.ob;n=F.layer_norm(x,(d,),b.n2w,b.n2b,1e-5);x=x+F.relu(n@b.f1+b.f1b)@b.f2+b.f2b
        return F.layer_norm(x,(d,),self.normWeight,self.normBias,1e-5)@self.embedding.t()+self.headBias
    def tensors(self):
        out=[self.embedding,self.position]
        for b in self.blocks:out+=[b.n1w,b.n1b,b.qkv,b.qkvb,b.o,b.ob,b.n2w,b.n2b,b.f1,b.f1b,b.f2,b.f2b]
        return out+[self.normWeight,self.normBias,self.embedding.t().contiguous(),self.headBias]
    def weights(self):
        data=struct.pack('>7i',0x42544631,*self.cfg)
        for t in self.tensors():data+=t.detach().to(torch.float32).cpu().numpy().astype('>f4').tobytes()
        return data
def container(weights,vocab_text,tokenizer_text):
    """Un solo archivo inmutable: pesos + vocabulario + tokenizador (crecen juntos en el dispositivo)."""
    parts=[weights,vocab_text.encode('utf-8'),tokenizer_text.encode('utf-8')];out=struct.pack('>i',0x42524431)
    for p in parts:out+=struct.pack('>i',len(p))+p
    return out
