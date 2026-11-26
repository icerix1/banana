"""Ejemplos de lectura con fragmento: la respuesta es siempre una oración literal del fragmento (o la negativa fija).
Texto: Wikipedia en español (CC BY-SA 4.0), sin pesos ni profesores externos."""
import re,random,unicodedata
NEGATIVE='no encontre ese dato en el fragmento .'
def plain(s):return ''.join(c for c in unicodedata.normalize('NFD',s) if unicodedata.category(c)!='Mn')
SENT=re.compile(r'(?<=[.!?])\s+(?=[A-ZÁÉÍÓÚÑ¿¡"«(0-9])')
def sentences(par):return [s.strip() for s in SENT.split(par) if s.strip()]
DEFINE=re.compile(r' (es|son|fue|era|fueron|eran|significa|se define como|consiste en|se denomina|se conoce como) ')
DEF_Q=['que es {t}','qué es {t}','que significa {t}','quien es {t}','quien fue {t}','que son {t}','definime {t}','explicame que es {t}','que quiere decir {t}','a que se llama {t}','sabes que es {t}','me decis que es {t}']
MENTION_Q=['que dice sobre {k}','que sabes de {k}','hablame de {k}','contame de {k}','{k}','que pasa con {k}','informacion sobre {k}','que dice de {k}','que menciona sobre {k}','busca {k}','donde aparece {k}','que hay sobre {k}']
RELATIONS=[
 (r'\bnaci[oó]\b|\bnacid[oa]\b',['cuando nacio {t}','donde nacio {t}','en que año nacio {t}','fecha de nacimiento de {t}']),
 (r'\bmuri[oó]\b|\bfalleci[oó]\b',['cuando murio {t}','como murio {t}','cuando fallecio {t}','donde murio {t}']),
 (r'\bfundad[oa]s?\b|\bfund[oó]\b|\bfundaci[oó]n\b',['cuando se fundo {t}','quien fundo {t}','cuando fue fundada {t}']),
 (r'\bhabitantes\b|\bpoblaci[oó]n\b',['cuantos habitantes tiene {t}','cual es la poblacion de {t}','cuanta gente vive en {t}']),
 (r'\bcapital\b',['cual es la capital de {t}','cual es su capital','que capital tiene {t}']),
 (r'\bsuperficie\b|km²|\bkil[oó]metros cuadrados',['que superficie tiene {t}','que tamaño tiene {t}','cuanto ocupa {t}']),
 (r'\bubicad[oa]s?\b|\bsituad[oa]s?\b|\bse encuentra\b|\blocalizad[oa]s?\b',['donde queda {t}','donde esta {t}','donde se encuentra {t}','donde esta ubicado {t}']),
 (r'\binventad[oa]\b|\binvent[oó]\b|\bcread[oa] por\b|\bcre[oó]\b|\bdesarrollad[oa] por\b|\bdise[ñn]ad[oa] por\b',['quien invento {t}','quien creo {t}','quien lo desarrollo','quien hizo {t}','quien diseño {t}']),
 (r'\bescrit[oa] por\b|\bescribi[oó]\b|\bautora?\b',['quien escribio {t}','quien es el autor de {t}']),
 (r'\bdirigid[oa] por\b|\bdirector[a]?\b',['quien dirigio {t}','quien es el director de {t}']),
 (r'\bse utiliza[n]?\b|\bse usa[n]?\b|\bsirve[n]? para\b|\butilizad[oa]s? para\b|\bse emplea[n]?\b',['para que sirve {t}','para que se usa {t}','como se usa {t}','que uso tiene {t}']),
 (r'\bfunciona\b|\bfuncionamiento\b|\bmediante\b',['como funciona {t}','de que manera funciona {t}']),
 (r'\bgan[oó]\b|\bpremios?\b|\bgalardon',['que premios gano {t}','que gano {t}']),
 (r'\bse compone[n]?\b|\bcompuest[oa]s?\b|\bformad[oa]s? por\b|\bcontiene[n]?\b',['de que esta hecho {t}','de que se compone {t}','que contiene {t}']),
 (r'\bmide[n]?\b|\bmetros\b|\baltura\b|\blongitud\b',['cuanto mide {t}','que altura tiene {t}','que largo tiene {t}']),
 (r'\bidiomas?\b|\blenguas?\b|\bse habla\b',['que idioma se habla en {t}','que lengua hablan en {t}']),
 (r'\bmoneda\b',['cual es la moneda de {t}','que moneda usa {t}']),
 (r'\bclima\b|\btemperaturas?\b|\blluvias?\b',['como es el clima de {t}','que clima tiene {t}']),
 (r'\bcomenz[oó]\b|\binici[oó]\b|\bempez[oó]\b|\bcomienzo\b|\binicio\b',['cuando empezo {t}','cuando comenzo {t}','como empezo {t}']),
 (r'\btermin[oó]\b|\bfinaliz[oó]\b|\bconcluy[oó]\b|\bfinal\b',['cuando termino {t}','como termino {t}']),
 (r'\bdebido a\b|\bporque\b|\ba causa de\b|\bcausad[oa]\b',['por que paso eso con {t}','cual fue la causa','por que ocurrio']),
 (r'\bse alimenta[n]?\b|\bdieta\b|\bcome[n]?\b',['que come {t}','de que se alimenta {t}']),
 (r'\bvive[n]?\b|\bhabita[n]?\b|\bh[aá]bitat\b',['donde vive {t}','cual es el habitat de {t}']),
 (r'\bcolor(es)?\b',['de que color es {t}','que color tiene {t}']),
 (r'\bpertenece[n]?\b|\bfamilia\b|\bg[eé]nero\b|\bespecie\b',['a que familia pertenece {t}','que tipo de especie es {t}']),
]
RELATIONS=[(re.compile(p,re.I),q) for p,q in RELATIONS]
YEAR=re.compile(r'\b(1[0-9]{3}|20[0-2][0-9])\b');YEAR_Q=['que paso en {y}','que ocurrio en {y}','que sucedio en el año {y}','{y}']
WORD=re.compile(r"[A-Za-zÁÉÍÓÚÜÑáéíóúüñ][A-Za-zÁÉÍÓÚÜÑáéíóúüñ\-]{4,}")
def title(t):return re.sub(r'\s*\(.*?\)\s*','',t).strip()
def question(template,rng,**kw):
    q=template.format(**kw).lower()
    if rng.random()<.7:q=plain(q)
    r=rng.random()
    if r<.2:q+=' ?'
    elif r<.3:q='¿ '+q+' ?'
    return q
def key_term(sent,others,rng):
    """Palabra o par de palabras que sólo aparece en esta oración del fragmento."""
    rest=plain(' '.join(others)).lower();cands=[]
    words=WORD.findall(sent)
    for i,w in enumerate(words):
        if plain(w).lower() in rest:continue
        cands.append(w)
        if i+1<len(words) and w[0].isupper() and words[i+1][0].isupper() and (w+' '+words[i+1]) in sent:cands.append(w+' '+words[i+1])
    if not cands:return None
    caps=[c for c in cands if c[0].isupper() and not sent.startswith(c)]
    return rng.choice(caps) if caps and rng.random()<.6 else rng.choice(cands)
class Pool:
    """Artículos → párrafos → oraciones."""
    def __init__(self,rows):
        self.articles=[]
        for t,text in rows:
            pars=[sentences(p) for p in text.split('\n') if 160<=len(p)<=2400]
            pars=[[s for s in p if 25<=len(s)<=420] for p in pars];pars=[p for p in pars if len(p)>=2]
            if pars:self.articles.append((title(t),pars))
    def example(self,rng,kind=None):
        """Devuelve (contexto, pregunta, respuesta, tipo) o None."""
        t,pars=rng.choice(self.articles);kind=kind or rng.choices(['define','mention','relation','year','negative'],[20,28,28,6,18])[0]
        pi=0 if kind=='define' and rng.random()<.8 else rng.randrange(len(pars));par=pars[pi]
        n=rng.randint(2,min(7,len(par)));start=rng.randrange(len(par)-n+1) if not (kind=='define' and pi==0) else 0
        ctx=list(par[start:start+n]);own=set(ctx)
        if rng.random()<.4:
            _,op=rng.choice(self.articles);extra=rng.choice(op);extra=extra[:rng.randint(1,min(3,len(extra)))]
            ctx=extra+ctx if rng.random()<.5 else ctx+extra
        if len(ctx)>2 and rng.random()<.5:
            cut=rng.randrange(1,len(ctx));ctx=ctx[cut:]+ctx[:cut]  # la respuesta no debe estar siempre al principio
        head='titulo : '+t+' . ' if rng.random()<.5 else ''
        if kind=='negative':
            other=self.example(rng,rng.choice(['define','mention','relation']))
            if not other:return None
            term=re.sub(r'^(que es|quien es|que dice sobre) ','',plain(other[1]))
            body=plain(' '.join(ctx)).lower()
            words=[w for w in plain(other[1]).split() if len(w)>4 and w not in('sobre','donde','cuando','quien','cuanto','cuantos','tiene')]
            if not words or any(w in body for w in words):return None
            return head+' '.join(ctx),other[1],NEGATIVE,'negative'
        if kind=='define':
            for s in ctx:
                m=DEFINE.search(s)
                if m and m.start()<120 and (plain(t).lower().split()[0] in plain(s[:m.start()+1]).lower()):
                    if sum(1 for o in ctx if o is not s and DEFINE.search(o) and plain(t).lower() in plain(o).lower())>0 and rng.random()<.7:continue
                    return head+' '.join(ctx),question(rng.choice(DEF_Q),rng,t=t),s,'define'
            return None
        if kind=='mention':
            s=rng.choice(ctx);k=key_term(s,[o for o in ctx if o is not s],rng)
            return (head+' '.join(ctx),question(rng.choice(MENTION_Q),rng,k=k),s,'mention') if k else None
        if kind=='year':
            s=rng.choice(ctx);ys=YEAR.findall(s);rest=' '.join(o for o in ctx if o is not s)
            ys=[y for y in ys if y not in rest]
            return (head+' '.join(ctx),question(rng.choice(YEAR_Q),rng,y=rng.choice(ys)),s,'year') if ys else None
        order=list(range(len(RELATIONS)));rng.shuffle(order)
        for ri in order:
            pat,qs=RELATIONS[ri];hits=[s for s in ctx if pat.search(s)]
            if len(hits)==1:
                s=hits[0];subject=t if s in own and rng.random()<.6 else (key_term(s,[o for o in ctx if o is not s],rng) or t)
                return head+' '.join(ctx),question(rng.choice(qs),rng,t=subject),s,'relation'
        if rng.random()<.3:  # ninguna oración habla de eso: la respuesta correcta es la negativa
            for ri in order:
                pat,qs=RELATIONS[ri]
                if not any(pat.search(s) for s in ctx):return head+' '.join(ctx),question(rng.choice(qs),rng,t=t),NEGATIVE,'negative'
        return None
    def text(self,rng):
        t,pars=rng.choice(self.articles);p=rng.choice(pars);return ' '.join(p)
def encode_example(tok,ex,max_ctx=350,max_q=40,max_a=110):
    ctx,q,a,kind=ex;c=tok.encode(ctx);qq=tok.encode(q);aa=tok.encode(a)
    if len(c)>max_ctx or len(qq)>max_q or len(aa)>max_a or not aa:return None
    ids=[2,3]+c+[3]+qq+[4]+aa+[5];start=len(ids)-len(aa)-1
    return ids,start
def encode_text(tok,text,width=220):
    ids=tok.encode(text)[:width]
    return ([2,4]+ids+[5],len(ids)+3) if len(ids)>=16 else None
