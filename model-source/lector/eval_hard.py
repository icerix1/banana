"""Prueba difícil del selector: la oración correcta entre ~25 del mismo artículo (no sólo las de un fragmento corto).
Uso: eval_hard.py rows.pkl tokenizer.json estado.pt [capas] [cantidad]"""
import sys,os,json,pickle,random,torch
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
import lector_tok as T,lector_data as D,lector_select as S
from lector_model import Lector
from train_select import CFG
def build(rows,tok,n,seed=31):
    pool=D.Pool(rows);by={t:pars for t,pars in pool.articles};rng=random.Random(seed);out=[]
    while len(out)<n:
        d=S.draw(pool,rng)
        if not d or d[3] is None:continue
        t,ctx,q,answer,kind=d;extra=[s for p in by.get(t,[]) for s in p if s not in ctx]
        rng.shuffle(extra);cands=list(ctx)+extra[:max(0,25-len(ctx))]
        if len(cands)<12 or any(len(tok.encode(s))>150 for s in cands):continue
        pos=cands.index(answer);order=list(range(len(cands)));rng.shuffle(order)
        # se conserva la oración anterior real de la respuesta; el resto va mezclado
        out.append((t,cands,q,answer,kind))
    return out
def top1(model,tok,examples):
    yes=tok.encode('si')[0];no=tok.encode('no')[0];hit=0;by={}
    with torch.no_grad():
        for t,cands,q,answer,kind in examples:
            best=-1;bp=-1
            for i,s in enumerate(cands):
                e=S.encode_pair(tok,None,cands[i-1] if i>0 else None,s,q,True,limits=(400,60));ids=e[0][:e[1]]
                l=model(torch.tensor([ids]))[0,-1];p=torch.softmax(l[[yes,no]],0)[0].item()
                if p>bp:bp=p;best=i
            ok=cands[best]==answer;hit+=ok;a=by.setdefault(kind,[0,0]);a[0]+=ok;a[1]+=1
    return hit/len(examples),{k:f'{v[0]}/{v[1]}' for k,v in by.items()}
if __name__=='__main__':
    rows=pickle.load(open(sys.argv[1],'rb'));tok=T.Tokenizer(json.load(open(sys.argv[2]))['merges']);layers=int(sys.argv[4]) if len(sys.argv)>4 else CFG['layers'];n=int(sys.argv[5]) if len(sys.argv)>5 else 150
    cfg=dict(CFG);cfg['layers']=layers;model=Lector(tok.vocab_size,**cfg);model.load_state_dict(torch.load(sys.argv[3]));model.eval()
    ex=build(rows[88000:90000],tok,n);acc,by=top1(model,tok,ex);print('hard top1',round(acc,3),by,'candidatos',sum(len(e[1]) for e in ex)/len(ex))
