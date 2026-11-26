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
