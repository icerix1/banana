"""Lector 3 (selector): en vez de copiar, decide si UNA oración responde la pregunta («si»/«no»).
La app puntúa cada oración candidata y muestra la mejor, que es literal por construcción.
Segunda tarea menor: dada una oración, escribir una pregunta que esa oración responde."""
import re
import lector_data as D
def candidate_text(title,prev,sentence):
    return ('titulo : '+title+' . ' if title else '')+('antes : '+prev+' ' if prev else '')+'oracion : '+sentence
def encode_pair(tok,title,prev,sentence,question,label,limits=(170,40)):
    c=tok.encode(candidate_text(title,prev,sentence));q=tok.encode(question);a=tok.encode('si' if label else 'no')
    if len(c)>limits[0] or len(q)>limits[1]:return None
    ids=[2,3]+c+[3]+q+[4]+a+[5];return ids,len(ids)-len(a)-1
def encode_question(tok,sentence,question,limits=(150,40)):
    c=tok.encode('oracion : '+sentence);q=tok.encode(question);g=tok.encode('pregunta')
    if len(c)>limits[0] or len(q)>limits[1]:return None
    ids=[2,3]+c+[3]+g+[4]+q+[5];return ids,len(ids)-len(q)-1
def draw(pool,rng):
    """(titulo, oraciones, pregunta, oración correcta o None, tipo) a partir del generador de fragmentos."""
    ex=pool.example(rng)
    if not ex:return None
    t,ctx=pool.last;ctx=[s for s in ctx]
    answer=None if ex[2]==D.NEGATIVE else ex[2]
    if answer is not None and answer not in ctx:return None
    return t,ctx,ex[1],answer,ex[3]
def encode_reply(tok,reply,message,label):
    """¿Este mensaje pide esta respuesta del banco de conversación?"""
    c=tok.encode('respuesta : '+reply);q=tok.encode(message);a=tok.encode('si' if label else 'no')
    if len(c)>170 or len(q)>40:return None
    ids=[2,3]+c+[3]+q+[4]+a+[5];return ids,len(ids)-len(a)-1
def chat_item(pool,tok,rng):
    """Positivo, otra respuesta del banco, una pregunta de lectura ante una respuesta, o un saludo ante una oración leída."""
    import chat_intents as C
    train,_=C.split();message,reply=rng.choice(train);r=rng.random()
    if r<.36:return encode_reply(tok,reply,message,True)
    if r<.78:
        other=rng.choice(C.INTENTS)[0]
        return encode_reply(tok,other,message,False) if other!=reply else None
    d=draw(pool,rng)
    if not d:return None
    if r<.9:return encode_reply(tok,reply,d[2],False)
    return encode_pair(tok,None,None,rng.choice(d[1]),message,False)
def training_item(pool,tok,rng,question_share=.15,chat_share=.22):
    if rng.random()<chat_share:return chat_item(pool,tok,rng)
    d=draw(pool,rng)
    if not d:return None
    t,ctx,q,answer,kind=d
    if answer is not None and rng.random()<question_share and kind in('define','mention','relation'):return encode_question(tok,answer,q)
    positive=answer is not None and rng.random()<.42
    if positive:i=ctx.index(answer)
    else:
        others=[k for k,s in enumerate(ctx) if s!=answer]
        if not others:return None
        i=rng.choice(others);near=None
        if answer is not None:
            # negativos difíciles: la oración vecina de la respuesta, con la respuesta como «antes»
            k=ctx.index(answer);r=rng.random()
            if r<.35 and k+1<len(ctx) and ctx[k+1]!=answer:near=k+1
            elif r<.5 and k>0 and ctx[k-1]!=answer:near=k-1
        if near is None and kind=='relation' and answer is not None and rng.random()<.4:
            # negativo de OTRA relación (p. ej. «murió» ante «¿cuándo nació?»): el modelo debe distinguir de qué habla la oración
            by=getattr(pool,'by',None)
            if by is None:by=pool.by={title:pars for title,pars in pool.articles}
            mine=[i for i,(pat,_) in enumerate(D.RELATIONS) if pat.search(answer)]
            rival=[x for par in by.get(t,[]) for x in par if x!=answer and any(pat.search(x) for pat,_ in D.RELATIONS) and not any(D.RELATIONS[i][0].search(x) for i in mine)]
            if rival:
                x=rng.choice(rival);return encode_pair(tok,t if rng.random()<.5 else None,None,x,q,False)
        if near is None and rng.random()<.3:
            # negativo del mismo artículo pero de otro párrafo: como cuando la app trae muchas oraciones del documento
            by=getattr(pool,'by',None)
            if by is None:by=pool.by={title:pars for title,pars in pool.articles}
            far=[x for par in by.get(t,[]) for x in par if x not in ctx]
            if far:
                x=rng.choice(far);return encode_pair(tok,t if rng.random()<.5 else None,None,x,q,False)
        if near is not None:
            title=t if rng.random()<.5 else None
            return encode_pair(tok,title,ctx[near-1] if near>0 else None,ctx[near],q,False)
    prev=ctx[i-1] if i>0 and rng.random()<.7 else None;title=t if rng.random()<.5 else None
    return encode_pair(tok,title,prev,ctx[i],q,positive)
