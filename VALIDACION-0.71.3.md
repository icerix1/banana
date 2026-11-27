# Validación Banana0.71.3

61 suites host aprobadas (las mismas de0.71.2, con Stage76, Stage78 y Stage83 ampliadas). Compilación completa con build.sh; APK713000 con el mismo certificado. El modelo no cambió: los números de lectura y de charla son los de VALIDACION-0.71.2.md.

## Ajuste propio
Stage83: con casos armados, el nivel que conserva las respuestas buenas y descarta las malas gana (0,8 frente a 0,7 y 0,5); sin diferencia queda el de fábrica; con menos de 8 veredictos no cambia nada; con ocho preguntas reales sobre una biblioteca de prueba el resultado queda dentro de 0,5–0,9; el valor guardado se aplica exacto y el de fábrica vuelve exacto. Durante la prueba apareció y se corrigió un error: guardado como decimal, 0,7 volvía como 0,69999999.

No hay ninguna medida de que el ajuste mejore las respuestas en uso real: necesita veredictos del usuario, que todavía no existen. Con pocos veredictos puede elegir un nivel por azar.

## Estudio con veredictos
Stage83: una respuesta «bien» produce un ejemplo «sí» y uno «no»; una «mal», un «no»; rota entre preguntas y no hace nada sin historial; los documentos preguntados van primero. No se midió si entrenar con estos ejemplos mejora el examen con preguntas del usuario.

## Entrenamiento de las capas superiores
Stage76: con ruido agregado a propósito en la capa de salida, la pérdida de las preguntas apartadas de un bloque baja de 0,310 a 0,026 ajustando sólo las dos últimas capas, el candidato se acepta y los embeddings no cambian. Con ruido menor (0,027→0,006) y mayor (4,50→1,64) también baja. Con el lector tal como se entrega, ese bloque ya se responde casi sin error (0,002) y el candidato se rechaza: no hay nada que aprender ahí. El ahorro de tiempo es una estimación por operaciones, no una medida en tablet.

## Charla, sugerencia y autoprueba
Stage83: una forma confirmada se guarda y una rechazada se bloquea sólo para ese par; sin respuesta de charla previa no hay nada que marcar. Una pregunta propia con un nombre que no está en el texto se descarta («que estudio antonio de lara» ante «Estudió en la escuela de puentes de Belorado.»); con el sujeto de la conversación se acepta. Stage78: la autoprueba tiene 12 pasos, 9 pasan en el host y los 3 que necesitan los modelos instalados se informan.

## Sin comprobar
Nada se ejecutó en emulador ni en dispositivo. No se ejercitaron en Android: el ajuste dentro del chat, el envío del veredicto a la charla o al lector, la sugerencia dentro de una respuesta, el entrenamiento con veredictos en el servicio, ni la autoprueba previa (carga de DEX aislada).
