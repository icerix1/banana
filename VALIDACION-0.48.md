# Validación de Banana0.48

- 28 comprobaciones JVM aprobadas, incluidas todas las regresiones anteriores y pruebas de entrenamiento real de la MLP, persistencia de pesos y edición del cuerpo de métodos respetando cadenas/comentarios.
- Android emulado API23: compilación de los 130 Java propios de la app completa; entrenamiento desde pesos aleatorios en el dispositivo; edición manual e inferida de SceneTiming.seconds, fuera del método del chat; recompilación de todo el proyecto.
- Android: incorporación de dos clases nuevas dependientes entre sí, comprobación conjunta, error real de símbolo inexistente, rechazo de checkpoint con evidencia vieja, descarte y recuperación de versión, conservación del proyecto visual y del método activo del chat. No se ejecutan las clases generales compiladas ni se genera un APK.
- MLP propia:216 ejemplos de instrucciones originales de entrenamiento; comprobación de36 frases base36/36; validación de desarrollo11/12; evaluación separada11/12. Las mismas métricas se observaron en JVM y Android. Es selección de operador entre seis tareas aritméticas, no resolución de12 programas Java generales.
- La validación de desarrollo guió ajustes de características y balanceo de contextos. La evaluación separada se observó al fijar esa versión. No se utilizaron las frases de evaluación en el vocabulario/entrenamiento; el conjunto evalúa variantes de tareas conocidas, no tipos de problemas nuevos.
- Primer intento de compilación completa detectó el tipo LambdaMetafactory ausente en los stubs públicos. Se incorporó un stub propio sólo de compilación y se volvió a probar el proyecto completo. Ese stub no se incluye como DEX ejecutable de la app.
- Interfaz de producción: estado del modelo guardado, preparación de la copia y compilación completa desde el chat. Sin FATAL EXCEPTION durante esa prueba. La inyección de texto espera contenido completo antes de enviar.
- Firma v1/v2/v3 verificada, sin claves ni instrumentación en el APK. La API de compilación se comprueba por hash.
- 22 archivos .bin base idénticos a0.47 por SHA-256. Modelo nuevo propio, pequeño, entrenado localmente; ningún modelo externo añadido.
- Las 130 fuentes incluidas en el APK coinciden con esta entrega. Corpus con origen y particiones explícitos en assets/java-specialist; stubs/licencias de compilación en assets/java-tools y tools/java8-stubs.

## Límites

No se entrenó un modelo generador de Java libre. La MLP aprende instrucción→operador; el código se produce por un emisor restringido. La comprobación general sólo demuestra sintaxis y tipos contra API35, no el comportamiento de todas las clases. No hay sandbox ni ejecución de Java general, reconstrucción de recursos/APK o activación general de los cambios copiados. Los puentes acotados anteriores son independientes y conservan su alcance.

No se probó en la tablet física Android16 ni con proyectos Java externos. El entrenamiento usa un corpus fijo original; no se importan automáticamente las ediciones del usuario a la MLP. La persistencia y recuperación se probaron recargando estados, sin reinicio completo del dispositivo.

Registros: test/resources/release048.
