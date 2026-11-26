# Validación Banana0.71.1

60 suites host: las 59 de0.71 más Stage82ChainConflictCheck. Compilación completa con build.sh; APK711000 con el mismo certificado.

## Lector 3.1 y la prueba de agrandarlo
Dos continuaciones de 150 minutos desde Lector 3, en paralelo y con los mismos datos (negativos de otros párrafos del mismo artículo, negativos de otra relación, seis relaciones nuevas): una con 6 capas (13923 pasos, 51 millones de tokens) y otra agrandada a 8 capas con salida inicial cero (11489 pasos, 42 millones).

Prueba difícil (model-source/lector/eval_hard.py): 150 preguntas, la oración correcta entre unas 22 del mismo artículo. Lector 3 de0.71: 0,82. Continuación de 6 capas: 0,78. Continuación de 8 capas: 0,813. Con 150 preguntas la diferencia entre los tres está dentro del margen de azar (unos 3 puntos): ni más entrenamiento ni más capas mejoraron esta prueba. Se adoptó la de 6 capas por las relaciones nuevas y por callar mejor; la de 8 capas se descartó (un tercio más lenta, sin ganancia).

Informe de la app (test/resources/lector3-heldout-report.json, con la red de seguridad y la doble puntuación): 331 de 343 con la mejor oración correcta (Lector 3 de0.71: 334); buscador léxico 162. Con seguridad≥0,7 mostró 311 y acertó 303; en los 174 casos sin respuesta léxica mostró 160 y acertó 157; ante 50 preguntas sin respuesta mostró algo en 1 (Lector 3: 4). Por tipo: {"year": {"lexical": 5, "examples": 13, "reader": 12}, "define": {"lexical": 48, "examples": 57, "reader": 55}, "mention": {"lexical": 95, "examples": 194, "reader": 186}, "relation": {"lexical": 14, "examples": 79, "reader": 78}}. Preguntas de plantilla y fragmentos cortos: no es un benchmark externo.

Doble puntuación: con la oración anterior como contexto el modelo daba 0,18 a «Murió en 1870 en Rosario.» ante «¿dónde murió Lucio Maren?» y 0,97 sin ella. Lector.select toma el mayor de los dos puntajes; cuesta el doble de pasadas, por eso los candidatos bajaron de 16 a 12.

Stage76 agrega: detección del asunto de la pregunta, que nacimiento y muerte no se confunden en un fragmento con ambos, y que una fecha sobre otra cosa no se ofrece como fecha de nacimiento. Stage78 con este lector: 8 de 12 en el examen propio sobre texto inventado y 3 de 4 en bloques apartados.

## Dos pasos, contradicciones y biblioteca
Stage82, con el modelo real y una biblioteca real en memoria: forma de la pregunta y casos que no lo son; extracción del nombre tras «por», con «de la», y rechazo cuando no hay nombre o es la cosa misma; «¿cuándo nació quien construyó el faro Brumel?» une la oración del constructor con la del nacimiento, cada una con su fuente; «¿dónde murió…?» llega a la oración de la muerte; sin primer paso en la biblioteca no encadena nada. Contradicciones: sólo el término con cifras distintas se marca, con las dos versiones y fuentes; reglas de diferencia; el término en disputa no se estudia y el otro sí. Biblioteca: tope 4096, informe de uso, borrado de las páginas web más viejas primero, nunca las del usuario, y aceptación de documentos nuevos después.

## Sin comprobar
Nada se ejecutó en emulador ni en dispositivo. No se probó una biblioteca de miles de documentos (tiempos del índice, memoria, tamaño de la APK reconstruida). La cadena de dos pasos sólo se probó con dos documentos inventados. La detección de contradicciones no se probó con páginas reales. El retiro del checkpoint de0.71 al actualizar no se ejercitó en Android.

## No incluido
Biblioteca que suelte sola lo menos usado (se decidió que no borre sin pedido). Un modelo más grande que rinda más: se probó y no rindió con el cómputo disponible.
