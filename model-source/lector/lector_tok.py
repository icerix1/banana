"""Tokenizador BPE de bytes por palabras del lector de Banana. Debe coincidir con BytePairs.java (modo "words")."""
import json,collections
SPACES=set(range(0,0x21))|{0xA0,0x2028,0x2029,0x202F,0x3000,0xFEFF}|set(range(0x2000,0x200C))
MAX_CHUNK=48
def is_word(cp):return 0x61<=cp<=0x7a or 0x30<=cp<=0x39 or cp>=0xC0
def chunks(text):
    """Trozos de bytes: palabra (letras/dígitos) o un signo, con un espacio inicial si había blancos antes."""
    out=[];cur=[];space=False;pending=False
    def flush():
        nonlocal cur,pending
        if cur:
            data=((' ' if pending else '')+''.join(cur)).encode('utf-8')
            for i in range(0,len(data),MAX_CHUNK):out.append(data[i:i+MAX_CHUNK])
            cur=[];pending=False
    for ch in text:
        cp=ord(ch)
        if 0xD800<=cp<=0xDFFF:continue
        if cp in SPACES:
            flush();space=True
        elif is_word(cp):
            if not cur:pending=space;space=False
            cur.append(ch)
        else:
            flush();out.append(((' ' if space else '')+ch).encode('utf-8'));space=False
    flush();return out
def learn(counts,vocab_size,log=None):
    """counts: {bytes: frecuencia}. Devuelve merges [[a,b,id],...] con ids desde 262."""
    words=[list(b+6 for b in w) for w in counts];freq=[counts[w] for w in counts]
    pairs=collections.Counter();where=collections.defaultdict(set)
    for i,w in enumerate(words):
        for a,b in zip(w,w[1:]):pairs[(a,b)]+=freq[i];where[(a,b)].add(i)
    merges=[];next_id=262
    while next_id<vocab_size and pairs:
        best,count=max(pairs.items(),key=lambda kv:(kv[1],-kv[0][0],-kv[0][1]))
        if count<2:break
        merges.append([best[0],best[1],next_id])
        for i in list(where[best]):
            w=words[i];f=freq[i]
            for a,b in zip(w,w[1:]):
                pairs[(a,b)]-=f
                if pairs[(a,b)]<=0:del pairs[(a,b)]
                where[(a,b)].discard(i)
            new=[];j=0
            while j<len(w):
                if j+1<len(w) and w[j]==best[0] and w[j+1]==best[1]:new.append(next_id);j+=2
                else:new.append(w[j]);j+=1
            words[i]=new
            for a,b in zip(new,new[1:]):pairs[(a,b)]+=f;where[(a,b)].add(i)
        where.pop(best,None);next_id+=1
        if log and len(merges)%500==0:log(len(merges))
    return merges
class Tokenizer:
    def __init__(self,merges):
        self.merges=merges;self.rank={(a,b):(i,c) for i,(a,b,c) in enumerate(merges)};self.cache={}
        self.pieces={i+6:bytes([i]) for i in range(256)}
        for a,b,c in merges:self.pieces[c]=self.pieces[a]+self.pieces[b]
        self.vocab_size=262+len(merges)
    def chunk(self,data):
        hit=self.cache.get(data)
        if hit is not None:return hit
        ids=[b+6 for b in data]
        while len(ids)>1:
            best=None;at=-1
            for i in range(len(ids)-1):
                r=self.rank.get((ids[i],ids[i+1]))
                if r is not None and (best is None or r[0]<best[0]):best=r;at=i
            if best is None:break
            a,b=ids[at],ids[at+1];new=[];j=0
            while j<len(ids):
                if j+1<len(ids) and ids[j]==a and ids[j+1]==b:new.append(best[1]);j+=2
                else:new.append(ids[j]);j+=1
            ids=new
        if len(self.cache)<400000:self.cache[data]=ids
        return ids
    def encode(self,text):
        out=[]
        for c in chunks(text.lower()):out.extend(self.chunk(c))
        return out
    def decode(self,ids):return b''.join(self.pieces.get(i,b'') for i in ids).decode('utf-8','replace')
    def vocab_lines(self):return ['<pad>','<unk>','<bos>','<user>','<assistant>','<end>']+[self.pieces[i].hex() for i in range(6,self.vocab_size)]
    def tokenizer_json(self):return json.dumps({'name':'Banana Lector byte BPE','mode':'words','base_bytes':256,'offset':6,'max_chunk':MAX_CHUNK,'merges':self.merges},separators=(',',':'))
