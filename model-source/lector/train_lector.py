"""Entrena desde cero el lector de Banana (inicio aleatorio; sin pesos ni profesor externos).
Uso: train_lector.py rows.pkl tokenizer.json salida/ minutos"""
import sys,os,time,math,random,pickle,json,collections,multiprocessing as mp,torch,torch.nn.functional as F
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
import lector_tok as T,lector_data as D
from lector_model import Lector,container
CFG=dict(d=192,heads=6,layers=6,context=512,ff=768);BATCH_TOKENS=3600;LR=1e-3;LM_SHARE=.15
def producer(rows,merges,queue,seed):
    tok=T.Tokenizer(merges);pool=D.Pool(rows);rng=random.Random(seed)
    while True:
        items=[]
        while len(items)<320:
            if rng.random()<LM_SHARE:e=D.encode_text(tok,pool.text(rng))
            else:
                ex=pool.example(rng);e=D.encode_example(tok,ex) if ex else None
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
        L=len(ids);x[i,:L]=torch.tensor(ids);y[i,:L-1]=torch.tensor(ids[1:]);w[i,:L-1]=.25;w[i,start-1:L-1]=1.0
    return x,y,w
def greedy(model,tok,ids,start,limit=110):
    cur=ids[:start]
    for _ in range(limit):
        if len(cur)>=CFG['context']:break
        nxt=int(model(torch.tensor([cur]))[0,-1].argmax())
        if nxt==5:break
        cur.append(nxt)
    return cur[start:]
def evaluate(model,tok,examples):
    model.eval();hit=collections.Counter();total=collections.Counter()
    with torch.no_grad():
        for ex,(ids,start) in examples:
            # Con decodificación voraz, acertar toda la respuesta equivale a acertar cada posición con el prefijo correcto.
            pred=model(torch.tensor([ids[:-1]]))[0,start-1:].argmax(-1).tolist();total[ex[3]]+=1;hit[ex[3]]+=int(pred==ids[start:])
    model.train();return {k:round(hit[k]/total[k],3) for k in total},round(sum(hit.values())/max(1,sum(total.values())),3)
if __name__=='__main__':
    rows=pickle.load(open(sys.argv[1],'rb'));merges=json.load(open(sys.argv[2]))['merges'];out=sys.argv[3];minutes=float(sys.argv[4]);os.makedirs(out,exist_ok=True)
    tok=T.Tokenizer(merges);train_rows=rows[:60000];held=rows[84000:86000]
    hp=D.Pool(held);rng=random.Random(99);examples=[]
    while len(examples)<120:
        ex=hp.example(rng);e=D.encode_example(tok,ex) if ex else None
        if e:examples.append((ex,e))
    json.dump([list(ex) for ex,_ in examples],open(os.path.join(out,'heldout-examples.json'),'w'),ensure_ascii=False)
    queue=mp.Queue(maxsize=64);mp.Process(target=producer,args=(train_rows,merges,queue,7),daemon=True).start()
    torch.manual_seed(1);torch.set_num_threads(3);model=Lector(tok.vocab_size,**CFG);opt=torch.optim.AdamW(model.parameters(),lr=LR,weight_decay=.01,betas=(.9,.98))
    params=sum(t.numel() for t in model.tensors());print('params exportados',params,'entrenados',sum(p.numel() for p in model.parameters()),flush=True)
    start=time.time();budget=minutes*60;step=0;seen=0;last=start;nextsave=600;log=open(os.path.join(out,'training-log.txt'),'a')
    def save(tag):
        w=model.weights();open(os.path.join(out,'lector1.bin'),'wb').write(container(w,'\n'.join(tok.vocab_lines())+'\n',tok.tokenizer_json()));torch.save(model.state_dict(),os.path.join(out,'state.pt'))
    while True:
        el=time.time()-start
        if el>budget:break
        frac=el/budget;lr=LR*min(1,(step+1)/600)*(0.08+0.92*0.5*(1+math.cos(math.pi*frac)))
        for g in opt.param_groups:g['lr']=lr
        x,y,w=collate(queue.get());logits=model(x);loss_all=F.cross_entropy(logits.view(-1,logits.size(-1)),y.view(-1),ignore_index=-100,reduction='none').view_as(w)
        loss=(loss_all*w).sum()/w.sum();opt.zero_grad();loss.backward();torch.nn.utils.clip_grad_norm_(model.parameters(),1.0);opt.step();step+=1;seen+=int((y!=-100).sum())
        if step%200==0:
            ans=(loss_all*(w==1)).sum()/(w==1).sum();line=f'step {step} min {el/60:.1f} loss {loss.item():.3f} answer {ans.item():.3f} lr {lr:.5f} tokens {seen} tok/s {seen/el:.0f}';print(line,flush=True);log.write(line+'\n');log.flush()
        if el>nextsave:
            nextsave+=900;save('ckpt');by,em=evaluate(model,tok,examples[:60]);line=f'EVAL min {el/60:.1f} exact {em} {by}';print(line,flush=True);log.write(line+'\n');log.flush()
    save('final');by,em=evaluate(model,tok,examples);line=f'FINAL steps {step} tokens {seen} exact {em} {by}';print(line,flush=True);log.write(line+'\n')
    json.dump({'name':'Banana Lector 1','external_weights':False,'initialization':'random','parameters':params,'layers':CFG['layers'],'width':CFG['d'],'heads':CFG['heads'],'context_tokens':CFG['context'],'ff':CFG['ff'],'vocab_size':tok.vocab_size,'tokenizer':'UTF-8 byte BPE por palabras entrenado sobre el mismo texto','training_text':'Wikipedia en español (dump 20231101, CC BY-SA 4.0), primeros 12000 caracteres de 60000 artículos','tasks':'respuesta = oración literal del fragmento o negativa fija; más continuación de texto','steps':step,'training_tokens':seen,'heldout_exact_match':em,'heldout_by_kind':by,'heldout_note':'120 ejemplos generados con las mismas plantillas sobre artículos no vistos; no es un benchmark externo','limitations':'Copia oraciones del fragmento; no razona ni responde sin fragmento. Sólo español.'},open(os.path.join(out,'model-card.json'),'w'),ensure_ascii=False,indent=1)
