# Validación de Banana 0.43

- 23 comprobaciones de regresión en JVM aprobadas, incluyendo los modelos propios, conversación, persistencia, escenas, besos, módulos y Java anterior.
- Pruebas nuevas: planificación de cambios conjuntos, rechazo completo de cláusulas desconocidas, límites numéricos, negativa a fuentes no canónicas, preservación de solicitudes normales de escena, contexto utilizado, multiplicador del salto y deshacer genérico según el último subsistema modificado.
- Android emulado API 23: compilación real de cuatro fuentes Java mediante ECJ y DX; carga de los métodos DEX; prueba sin activar; activación conjunta; recarga del paquete persistido; historial y recuperación; rechazo de candidato desactualizado; lectura del mapa y código instalado.
- APK de producción: envío de pedidos por la interfaz; cambio de texto visible; activación y apertura del panel de proyectos; lote preparado/aplicado/deshecho. Sin FATAL EXCEPTION durante esa prueba.
- Firma APK v1/v2/v3 verificada por build.sh. Instrumentación excluida de classes.dex de producción; claves excluidas del APK.
- 22 archivos .bin idénticos por SHA-256 a Banana 0.42. No se añadieron modelos externos.
- Mapa de 117 fuentes con hashes y referencias; el código incluido en el APK coincide con los archivos de esta entrega.

## Límites de la evidencia

No es una prueba en la tablet física Android 16 del usuario. El restablecimiento se comprobó reinicializando el estado de carga y releyendo el paquete persistido; no constituye una prueba de actualización del sistema operativo ni de reinicio completo del dispositivo. Los cambios finales de selección del deshacer genérico y el rechazo de pedidos mezclados tienen además pruebas JVM específicas. Las pruebas Android comprueban módulos de contrato acotado; no demuestran programación general, sandbox de Java arbitrario, compilación de APK en el dispositivo ni mejora de la inteligencia general del chatbot.

Los registros pertinentes se incluyen en test/resources/release043.
