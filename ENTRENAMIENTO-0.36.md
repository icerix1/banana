# Entrenamiento propio de la segunda etapa

Se continuó el checkpoint propio Chat3.4, sin modelos externos ni profesor.
Se añadieron 100 ejemplos propios y se reprodujeron
9513 ejemplos del corpus anterior durante1400 pasos.
La arquitectura, el vocabulario byte-BPE y el contexto384 se conservaron.

La prueba nueva reservó 42 casos con frases e identidades
separadas del entrenamiento nuevo. No se usaron sus respuestas para entrenar.
Exact-match exploratorio: base 0/42;
candidato 5/42.
Las4 respuestas ancla se conservaron. Muchas salidas nuevas son ilegibles.

**Decisión: no activar el candidato.** La mejora exacta aislada no alcanza para
asegurar una mejor conversación. El requisito de calidad se endureció tras
inspeccionar este piloto; estos resultados no son una certificación ciega ni
un benchmark general. El modelo activo sigue siendo Chat3.4.

Se incluyen el corpus dividido, código de entrenamiento, pesos candidatos,
procedencia y las42 respuestas esperadas/reales en model-card.json. La paridad
Java/PyTorch del candidato fue menor a1e-5; esto comprueba la inferencia, no su
comprensión. La aplicación obtiene los datos de la escena mediante herramientas
verificables, no inventando resultados con este candidato.

Para otra iteración se necesitan más estructuras lingüísticas variadas y una
prueba nueva reservada: repetir estas frases no demuestra generalización.
