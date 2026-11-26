# Validación Banana0.71

59 suites host: las 58 de0.70 más Stage81ExploreCheck; Stage76 y Stage78 reescritas para el selector. Compilación completa con build.sh; APK710000 con el mismo certificado.

## Lector 3
Continuación desde los pesos de Lector 1 con la tarea «si»/«no»: 30 minutos sin negativos vecinos y 120 minutos con ellos (15750 pasos y 56847796 tokens en la segunda etapa). En 160 preguntas apartadas durante el entrenamiento: mejor oración correcta 0.924, calla ante dato ausente 0.952.

Informe independiente (test/resources/lector3-heldout-report.json), mismo conjunto de artículos que el informe de0.69, 343 preguntas con respuesta: mejor oración correcta 334; buscador léxico 162. Con seguridad≥0,7 mostró 312 y acertó 307; con≥0,9, 270 y 268. En los 174 casos sin respuesta léxica, con≥0,7 mostró 162 y acertó 162. De 50 preguntas sin respuesta en el texto, mostró algo en 4 con≥0,7. Por tipo: {"year": {"lexical": 5, "examples": 13, "reader": 13}, "define": {"lexical": 48, "examples": 57, "reader": 57}, "mention": {"lexical": 95, "examples": 194, "reader": 189}, "relation": {"lexical": 14, "examples": 79, "reader": 75}}. 7 ejemplos se omitieron porque la app parte las oraciones distinto que el generador. Los umbrales se fijaron con otro conjunto de 345 preguntas (333 correctas).

Límites de esa medida: las preguntas salen de las mismas plantillas con que se entrenó; cada fragmento tiene pocas oraciones del mismo artículo o de dos; no mide la recuperación entre muchos documentos ni preguntas escritas por personas. No es un benchmark externo. Fuera del informe: 6 de 6 en fragmentos escritos a mano (Stage76) y, en un texto inventado y repetitivo, 7 de 12 en el examen propio y 1 de 4 en bloques apartados (Stage78), que muestra cuánto baja con texto difícil.

Stage76 comprueba además: paridad de tokens y logits con PyTorch, disposición del candidato igual a la del script, que la oración que define puntúa más que otra, que «¿cuándo nació?» elige la oración del nacimiento y no la vecina sobre la muerte, que un sujeto ausente no recibe respuesta, que el lector escribe una pregunta para una oración, que las preguntas falladas se estudian el doble (con un modelo dañado a propósito), capa agregada sin cambiar salidas, y ajuste con un bloque nuevo aceptado (pérdida 0,011→0,004) o restaurado exacto.

## Exploración de internet
Stage81, con sitios simulados y la extracción real: frases de exploración sin tocar la ruta vieja de «investigá»; semillas desde la página de resultados y, si el buscador falla, sólo Wikipedia; sin servicio en segundo plano no se inicia; páginas de tres sitios; descarte de la que no nombra el tema, de la repetida en otra dirección y de la corta; redes sociales nunca pedidas; robots.txt una vez por sitio; cupo de 15 por sitio; enlace más «otros sitios»; el recorrido normal sigue dentro del sitio. Corrigió en la marcha dos fallas: «volcán» no contaba como el tema «volcanes» y la huella de repetidos dependía del final del texto.

No comprobado: una exploración real contra internet (el entorno no resuelve DNS), la calidad de las semillas del buscador, el comportamiento con sitios que bloquean lectores, ni el servicio de Android.

## Sin comprobar en general
Nada se instaló ni ejecutó en emulador ni en dispositivo: entrenamiento, autoestudio, examen, crecimiento, retiro del checkpoint anterior al actualizar, exploración y recorrido en el servicio. No se midió si el autoestudio mejora el examen propio con los días. El tiempo que agrega el lector a cada pregunta en una tablet es estimado.

## No incluido
Biblioteca más grande o que rota, preguntas que conectan dos lecturas, detección de contradicciones, búsqueda por vectores, un modelo más grande, respaldo del progreso del lector, y el resto de PROXIMA-ETAPA.md.
