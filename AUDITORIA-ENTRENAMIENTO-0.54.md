# Auditoría del entrenamiento propio —0.54

La ruta de inferencia activa usa BananaTransformer (chat34/chat34.bin) y PixelGenerator (pixel-v2/character21.bin para el modelo principal). Los entrenadores PyTorch del desarrollo no estaban disponibles como entrenamiento Android. Pixel suele usar imágenes de un banco neuronal precalculado; cambiar sólo un archivo de pesos no garantizaba que la vista leyera el resultado.

Chat: vocabulario542, dimensión128,4 cabezas,3 bloques, contexto384 y FF512. Archivo original3.134.100 bytes,783.518 parámetros float32. La capa de salida tiene69.918 parámetros. Se habilitó SGD real sobre esa proyección y su sesgo; no hay backprop por atención, normalizaciones, embeddings ni FF.

Pixel principal: entradas48, tres capas ocultas de256 y salidaRGBA4 a128×128. Archivo original580.644 bytes,145.156 parámetros. Se habilitó SGD real sobre los1.028 parámetros de su capa final; sus características quedan congeladas. Los modelos de fondos, poses, caras, objetos, tomate y criaturas no se reentrenan en esta fase.

La versión inicial de SGD de Chat mejoró la pérdida local pero excedió el umbral de KL en controles y fue rechazada durante el desarrollo. Se añadió una restricción L2 al tamaño de la actualización sin relajar el criterio de publicación. La configuración final tiene8 épocas y radio0.01. La métrica final de escritorio pasó de2.2502500020 a2.2380375169 nats/token, con KL0.000002132 en los controles. Es un descenso pequeño sobre dos preguntas de evaluación propias, no una medida de calidad general.

Pixel pasó de0.0029188072 a0.0028752995 en error RGBA ponderado por alfa sobre posiciones distintas de una misma imagen propia, con cambio de error0.000003296 en controles de otra condición. Los datos propios de referencia vienen del dibujante procedural original de train_pixel21.py. No se presenta como evaluación sobre imágenes nuevas.

Se comprobaron la derivada por diferencia finita de la pérdida de Chat, la modificación real de pesos, la conservación byte a byte del cuerpo congelado y la carga del checkpoint con su formato original. El informe de entrenamiento permite rechazar candidatos sin publicar. Los pesos base distribuidos siguen siendo los originales hasta que el usuario solicita entrenamiento.

La vista rápida de Pixel aplica un campo16×16 de diferencia de logits generado con la capa entrenada sobre el banco propio original; no es inferencia exacta completa en cada píxel. El nuevo banco del color predeterminado se calculó con los pesos propios originales mediante la misma red, no mediante un modelo externo. Con un banco faltante se conserva la ruta de inferencia completa más costosa.

No se usaron Qwen, pesos externos ni maestros externos. Se utilizó la versión del compilador Eclipse ya incluida para desarrollar la conversión de lambdas, no como modelo de IA. La reconstrucción total se refiere a las fuentes Java actuales; los recursos/manifiesto empaquetados conservan sus límites.
