# Lector 1: entrenamiento reproducible

Modelo propio de lectura con fragmento. Inicio aleatorio; sin pesos, profesores ni APIs de otros modelos.

- `lector_tok.py`: tokenizador BPE de bytes por palabras (4096 piezas). Debe coincidir con `BytePairs.java` en modo `words`.
- `lector_model.py`: mismo Transformer causal que `BananaTransformer.java`, con el mismo formato binario. Durante este entrenamiento la capa de salida comparte la matriz de embeddings; al exportar se escriben las dos copias y en el dispositivo se ajustan por separado.
- `lector_data.py`: genera los ejemplos. La respuesta es siempre una oración literal del fragmento, o la negativa fija.
- `train_lector.py`: entrenamiento con AdamW en CPU. `make_examples.py` y `export_fixture.py`: ejemplos apartados y fixture de paridad para `Stage76LectorCheck`.

Texto de entrenamiento: Wikipedia en español, volcado 20231101 (conjunto `wikimedia/wikipedia`, archivo `20231101.es/train-00000-of-00013.parquet`), licencia CC BY-SA 4.0. Se usan los primeros 12000 caracteres de 60000 artículos de más de 1500 caracteres; otros artículos del mismo archivo quedan apartados para medir. El texto no se redistribuye en este repositorio.

```sh
python -m venv venv && venv/bin/pip install torch numpy pyarrow
# rows.pkl: lista (título, texto[:12000]) mezclada con semilla 1; tokenizer.json: lector_tok.learn sobre 25000 artículos
venv/bin/python train_lector.py rows.pkl tokenizer.json salida/ 100
```

`training-log.txt` y `model-card.json` son los de la corrida que produjo `assets/lector1/lector1.bin`. Una primera corrida sin compartir embeddings no aprendió a copiar en 23 minutos y se descartó.

## Lector 2: intento descartado (0.70)

`lector2/` guarda el registro, la ficha y el informe de una continuación de 130 minutos desde los pesos de Lector 1 (17993 pasos, 57,7 millones de tokens, 80000 artículos). Cambios en `lector_data.py` para ese intento: oraciones del fragmento rotadas para que la respuesta no quede siempre primera, título del artículo antepuesto en la mitad de los ejemplos, más ejemplos con la negativa (18 % más negativas por relación ausente) y contexto de hasta 350 tokens. El `lector_data.py` actual es el de ese intento; el que entrenó a Lector 1 está en el commit a790664.

Sobre las mismas 350 preguntas apartadas (sin título, sin rotación):

| Medida | Lector 1 | Lector 2 |
| --- | --- | --- |
| readerExactSentence | 137 | 121 |
| readerShownButOtherSentence | 148 | 130 |
| appGateShown | 122 | 108 |
| appGateRight | 110 | 101 |
| fallbackShownWhenLexicalSilent | 45 | 37 |
| fallbackRightWhenLexicalSilent | 39 | 34 |
| aciertos en «year» | 1 | 3 |
| aciertos en «define» | 43 | 43 |
| aciertos en «mention» | 63 | 60 |
| aciertos en «relation» | 30 | 15 |

En diez fragmentos cortos escritos a mano: Lector 1 acertó 3 y mostró 7 oraciones equivocadas; Lector 2 acertó 4 y mostró 1. Lector 2 se equivoca menos porque calla más, pero responde menos y perdió la mitad de los aciertos en preguntas de relación. No hay evidencia de que sea mejor para la app, así que **la app sigue con Lector 1** y `Lector.TITLES` queda en falso. Sus pesos no se incluyen en el repositorio.

## Lector 3: selector de oraciones (0.71, es el que usa la app)

`lector_select.py` y `train_select.py`. El modelo no copia: decide «si»/«no» para una oración candidata, con el título y la oración anterior como contexto opcional; tarea menor (15 %), escribir una pregunta para una oración. Parte de los pesos de Lector 1. Dos etapas: 30 minutos sin negativos vecinos y 120 minutos agregando como negativos difíciles las oraciones contiguas a la respuesta (la primera etapa confundía «murió» con «nació» cuando la oración anterior traía la respuesta). `lector3/` guarda registro y ficha; el informe está en `test/resources/lector3-heldout-report.json`: 334 de 343 con la mejor oración correcta, contra 162 del buscador léxico.

```sh
venv/bin/python train_select.py rows.pkl tokenizer.json etapa1/ 30 lector1-state.pt   # sin el bloque «near» de lector_select.py
venv/bin/python train_select.py rows.pkl tokenizer.json etapa2/ 120 etapa1/state.pt
```
