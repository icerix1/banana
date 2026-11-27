"""Entrena el selector (Lector 3) continuando desde los pesos de Lector 1.
Uso: train_select.py rows.pkl tokenizer.json salida/ minutos estado_inicial.pt [capas hilos]
Con más capas que el estado inicial, las nuevas empiezan con salida cero: el modelo calcula lo mismo hasta entrenarlas."""
import sys,os,time,math,random,pickle,json,collections,multiprocessing as mp,torch,torch.nn.functional as F
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
import lector_tok as T,lector_data as D,lector_select as S
from lector_model import Lector,container
CFG=dict(d=192,heads=6,layers=6,context=512,ff=768);BATCH_TOKENS=4200;LR=6e-4
def producer(rows,merges,queue,seed):
    tok=T.Tokenizer(merges);pool=D.Pool(rows);rng=random.Random(seed)
    while True:
        items=[]
        while len(items)<512:
            e=S.training_item(pool,tok,rng)
            if e:items.append(e)
        items.sort(key=lambda e:len(e[0]));batches=[];cur=[]
        for e in items:
            if cur and (len(cur)+1)*len(e[0])>BATCH_TOKENS:batches.append(cur);cur=[]
            cur.append(e)
        if cur:batches.append(cur)
        rng.shuffle(batches)
        for b in batches:queue.put(b)
def collate(batch):
    n=max(len(e[0]) for e in batch);x=torch.zeros(len(batch),n,dtype=torch.long);y=torch.full((len(batch),n),-100,dtype=torch.long);w=torch.zeros(len(batch),n)
    for i,(ids,start) in enumerate(batch):
        L=len(ids);x[i,:L]=torch.tensor(ids);y[i,:L-1]=torch.tensor(ids[1:]);w[i,:L-1]=.1;w[i,start-1:L-1]=1.0
    return x,y,w
def scores(model,tok,title,ctx,q,yes,no):
    seqs=[]
    for i,s in enumerate(ctx):
        e=S.encode_pair(tok,title,ctx[i-1] if i>0 else None,s,q,True,limits=(400,60));seqs.append(e[0][:e[1]] if e else None)
    out=[]
    for ids in seqs:
        if ids is None or len(ids)>500:out.append(0.0);continue
        l=model(torch.tensor([ids]))[0,-1];p=torch.softmax(l[[yes,no]],0)[0].item();out.append(p)
    return out
def evaluate(model,tok,examples):
    model.eval();yes=tok.encode('si')[0];no=tok.encode('no')[0];hit=collections.Counter();total=collections.Counter();best_neg=[];best_pos=[]
    with torch.no_grad():
        for t,ctx,q,answer,kind in examples:
            p=scores(model,tok,t,ctx,q,yes,no);top=max(range(len(p)),key=lambda i:p[i])
            if answer is None:best_neg.append(p[top]);continue
            total[kind]+=1;ok=ctx[top]==answer;hit[kind]+=int(ok);best_pos.append((p[top],ok))
    import chat_intents as C
    _,held=C.split();chat=0;stray=0
    with torch.no_grad():
        def ask(reply,message):
            ids=S.encode_reply(tok,reply,message,True);ids=ids[0][:ids[1]];l=model(torch.tensor([ids]))[0,-1];return torch.softmax(l[[yes,no]],0)[0].item()
        for message,reply in held:
            ps=[ask(r,message) for r,_ in C.INTENTS];chat+=int(C.INTENTS[max(range(len(ps)),key=lambda i:ps[i])][0]==reply)
        for t,ctx,q,answer,kind in examples[:30]:stray+=int(max(ask(r,q) for r,_ in C.INTENTS[::3])>=.8)
    model.train();acc=sum(hit.values())/max(1,sum(total.values()));refuse=sum(1 for v in best_neg if v<.5)/max(1,len(best_neg));evaluate.chat=(chat,len(held),stray)
    return {k:round(hit[k]/total[k],3) for k in total},round(acc,3),round(refuse,3)
if __name__=='__main__':
    rows=pickle.load(open(sys.argv[1],'rb'));merges=json.load(open(sys.argv[2]))['merges'];out=sys.argv[3];minutes=float(sys.argv[4]);init=sys.argv[5];os.makedirs(out,exist_ok=True)
    tok=T.Tokenizer(merges);hp=D.Pool(rows[84000:86000]);rng=random.Random(99);examples=[]
    while len(examples)<160:
        d=S.draw(hp,rng)
        if d and all(len(tok.encode(s))<150 for s in d[1]):examples.append(d)
    json.dump([list(e) for e in examples],open(os.path.join(out,'heldout-select.json'),'w'),ensure_ascii=False)
    queue=mp.Queue(maxsize=64);mp.Process(target=producer,args=(rows[:80000],merges,queue,11),daemon=True).start()
    layers=int(sys.argv[6]) if len(sys.argv)>6 else CFG['layers'];CFG['layers']=layers;torch.manual_seed(3);torch.set_num_threads(int(sys.argv[7]) if len(sys.argv)>7 else 3);model=Lector(tok.vocab_size,**CFG);old=torch.load(init);state=model.state_dict()
    for k,v in old.items():state[k]=v
    had=1+max(int(k.split('.')[1]) for k in old if k.startswith('blocks.'))
    for i in range(had,layers):
        for name in ('o','ob','f2','f2b'):state[f'blocks.{i}.{name}']=torch.zeros_like(state[f'blocks.{i}.{name}'])
    model.load_state_dict(state);opt=torch.optim.AdamW(model.parameters(),lr=LR,weight_decay=.01,betas=(.9,.98))
    start=time.time();budget=minutes*60;step=0;seen=0;nextsave=600;log=open(os.path.join(out,'training-log.txt'),'a')
    def save():
        open(os.path.join(out,'lector3.bin'),'wb').write(container(model.weights(),'\n'.join(tok.vocab_lines())+'\n',tok.tokenizer_json()));torch.save(model.state_dict(),os.path.join(out,'state.pt'))
    while True:
        el=time.time()-start
        if el>budget:break
        lr=LR*min(1,(step+1)/300)*(0.06+0.94*0.5*(1+math.cos(math.pi*el/budget)))
        for g in opt.param_groups:g['lr']=lr
        x,y,w=collate(queue.get());logits=model(x);la=F.cross_entropy(logits.view(-1,logits.size(-1)),y.view(-1),ignore_index=-100,reduction='none').view_as(w)
        loss=(la*w).sum()/w.sum();opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(model.parameters(),1.0);opt.step();step+=1;seen+=int((y!=-100).sum())
        if step%200==0:
            ans=(la*(w==1)).sum()/(w==1).sum();line=f'step {step} min {el/60:.1f} loss {loss.item():.3f} label {ans.item():.3f} lr {lr:.5f} tokens {seen} seq/s {step*x.size(0)/el:.0f}';print(line,flush=True);log.write(line+'\n');log.flush()
        if el>nextsave:
            nextsave+=1200;save();by,acc,refuse=evaluate(model,tok,examples[:80]);line=f'EVAL min {el/60:.1f} top1 {acc} refuse_absent {refuse} chat {evaluate.chat[0]}/{evaluate.chat[1]} lectura-como-charla {evaluate.chat[2]}/30 {by}';print(line,flush=True);log.write(line+'\n');log.flush()
    save();by,acc,refuse=evaluate(model,tok,examples);line=f'FINAL steps {step} tokens {seen} top1 {acc} refuse_absent {refuse} chat {evaluate.chat[0]}/{evaluate.chat[1]} lectura-como-charla {evaluate.chat[2]}/30 {by}';print(line,flush=True);log.write(line+'\n')
    json.dump({'name':'Banana Lector 3 (selector)','external_weights':False,'continues_from':'Lector 1','parameters':sum(t.numel() for t in model.tensors()),'layers':CFG['layers'],'width':CFG['d'],'heads':CFG['heads'],'context_tokens':CFG['context'],'vocab_size':tok.vocab_size,'task':'si/no: ¿esta oración responde la pregunta?; tarea menor: escribir una pregunta para una oración','training_text':'Wikipedia en español (dump 20231101, CC BY-SA 4.0), 80000 artículos','steps':step,'training_tokens':seen,'heldout_top1':acc,'heldout_refuses_absent':refuse,'heldout_by_kind':by,'heldout_note':'160 preguntas de plantilla sobre artículos no vistos; acierto = la oración mejor puntuada es la correcta','limitations':'Elige entre oraciones del fragmento; no redacta respuestas ni razona. Sólo español.'},open(os.path.join(out,'model-card.json'),'w'),ensure_ascii=False,indent=1)
