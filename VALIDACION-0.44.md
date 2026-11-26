# Validación de Banana0.44

- 24 comprobaciones JVM aprobadas, incluidas las regresiones de conversación, modelos, creación, persistencia, módulos Java y compilación previa.
- Nueva comprobación de expresiones, tramos de subida/pausa/bajada, límites y rechazo de APIs, bucles, recursión, división variable, entradas ambiguas y pedidos mezclados.
- Integración en JVM: Scene.pose usa la función reemplazada; la parábola -100*t*(1-t) produce un desplazamiento distinto al seno anterior en el personaje real de una escena.
- Android emulado API23: descripción natural → fuente Java nueva → compilación ECJ/DX → prueba DEX en otro PID con257 muestras → activación → ejecución de la función → recarga persistida → expresión libre → recuperación de versión.
- Android: rechazo de APIs y candidatos desactualizados sin modificar la función activa ni los datos de escena.
- APK de producción instalado y abierto en el emulador tras la instrumentación; sin FATAL EXCEPTION durante esa prueba.
- Firma v1/v2/v3 verificada. El APK no incluye la instrumentación ni las claves de firma.
- 22 archivos de modelos .bin idénticos a0.43 por SHA-256. No se entrenó un modelo programador ni se añadieron pesos externos.
- 121 fuentes propias indexadas en el APK y comprobadas contra esta entrega.

## Límites

El proceso de prueba comparte UID y permisos: no es un sandbox de seguridad. Sólo se admite el AST matemático validado; estas pruebas no habilitan Java general. Las257 muestras son una prueba de contrato numérico, no una demostración exhaustiva de cualquier fórmula posible. El resultado se limita y el consumidor conserva una función de respaldo ante valores no finitos o fallos.

La prueba de persistencia recarga el paquete y el método guardados, no equivale a reiniciar el dispositivo entero. No se probó en la tablet física Android16. La prueba de producción comprueba instalación y apertura; los pedidos y cambios numéricos se verificaron mediante instrumentación del motor y pruebas de integración de Scene.pose, sin una evaluación visual completa de vídeos exportados.

Registros: test/resources/release044.
