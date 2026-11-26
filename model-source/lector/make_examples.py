"""Genera ejemplos de artículos apartados para calibrar o informar. Uso: make_examples.py rows.pkl desde hasta cantidad semilla salida.json"""
import sys,os,json,pickle,random
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
import lector_tok as T,lector_data as D
rows=pickle.load(open(sys.argv[1],'rb'));a,b,n,seed=map(int,sys.argv[2:6]);tok=T.Tokenizer(json.load(open(os.path.join(os.path.dirname(sys.argv[1]),'tokenizer.json')))['merges'])
pool=D.Pool(rows[a:b]);rng=random.Random(seed);out=[]
while len(out)<n:
    ex=pool.example(rng)
    if ex and D.encode_example(tok,ex):out.append(list(ex))
json.dump(out,open(sys.argv[6],'w'),ensure_ascii=False);print(len(out))
