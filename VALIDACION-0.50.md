# Validación de Banana0.50

- 30 comprobaciones JVM aprobadas: las29 anteriores, nuevo módulo compilado de planificación, conservación de identidades/estado, controles reservados, abstención entrenada y cierre idempotente de la salida ZIP y alineación STORED en cuatro zonas horarias.
- Android emulado API23: compilación completa de los 142 Java propios, ECJ+DX de animación y planificador, pruebas del contrato en proceso separado y activación. Receta real deFito/Bani con tres pasos y conservación de designId.
- Pesos propios del selector entrenados automáticamente desde evaluaciones explícitas del usuario. Confirmación positiva, evaluación negativa con abstención y recuperación de pesos+datos anteriores verificadas. Las métricas son de retención sobre ejemplos conocidos; no existe evaluación independiente de comprensión general en esta entrega.
- Diario portable validado; respaldo con identidad de fuente alterada rechazado sin cambiar módulos activos.
- Firma privada importada desde archivo externo al APK de prueba, certificado contrastado con la app instalada y material protegido/recuperado con AndroidKeyStore y AES-GCM.
- Reconstrucción ejecutada realmente en Android: copia del núcleo/recursos, incorporación de DEX y Java canónico de módulos, manifiesto aumentado de500000 a500001, firma v1/v2/v3 y verificación de identidad. Verificación independiente en el host con apksigner.
- APK reconstruida instalada sobre la anterior: Android leyó500001 y conservó módulos/pesos. Con datos borrados, la misma APK recuperó el código y aprendizaje embebidos; no recuperó ninguna autoridad de firma privada.
- Actualización de núcleo de prueba a510000 sin bootstrap, con cambio compatible del generador de fuentes canónicas: los módulos guardados fueron recompilados y probados automáticamente. Se conservaron el salto de altura24, la receta y los pesos aprendidos. Esa APK de prueba no se distribuye como una versión real0.51.
- Interfaz de producción: ayuda de código permanente y aprendizaje, y apertura del selector de firma desde el chat; sin FATAL EXCEPTION observado.
- El APK de producción no contiene instrumentación ni archivos de firma privada. También se buscó el PKCS8 real dentro de todas sus entradas y en la APK reconstruida: ausente. Las fuentes del índice coinciden con el código entregado y el APK. 22 pesos .bin son idénticos a0.49; ningún modelo externo añadido.

## Correcciones durante la validación

La primera prueba detectó que el validador eliminaba los marcadores de plantilla al normalizar; se preservan ahora al comprobar el texto. La biblioteca upstream usaba utilidades Java8 ausentes en API23. Se incorporó una capa propia de compatibilidad de utilidades, sin cambiar los algoritmos criptográficos; se comprobaron512 conversiones Base64, rechazos de entradas inválidas, sort/stream/overflow y firma/verificación real en JVM y Android. La primera reconstrucción Android detectó un doble cierre de BufferedOutputStream en API23; la salida contada se hizo idempotente y se agregó una regresión. Se acotó la retención de APK temporales y se limpiaron las copias de prueba para evitar agotar el disco del emulador. La comprobación de reapertura espera la inicialización real de Application antes de consultar los estados. Las pruebas finales se repitieron después de esas correcciones.

## Límites

Se incorpora código DEX de contratos preparados dentro del APK, no cualquier cambio de la copia Java ni una recompilación completa de recursos. El nuevo planificador sustituye la política de recetas y conserva el ejecutor estable; no reemplaza todos los algoritmos de ScenePlanner. La instalación de una actualización sigue siendo una confirmación de Android por parte del propietario.

La prueba de núcleo verifica un cambio compatible concreto y no demuestra compatibilidad con cualquier versión futura. Los registros incompatibles/corruptos no se ejecutan como Java general. El proceso privado de pruebas comparte UID, por lo que no se presenta como sandbox. No se probaron actualizaciones a través de Play ni cambios de firmante, APK divididas, recursos nuevos, bibliotecas nativas nuevas o cambios arbitrarios del manifiesto. La autoridad importada necesita reimportación si se borran los datos; jamás viaja en el paquete.

No se probó en la tablet física Android16 del usuario. No se renderizó un MP4 nuevo ni se entrenó el modelo general de conversación/imagen en esta fase. El entrenamiento del selector usa hasta32 evaluaciones explícitas, no etiquetas inventadas por la propia app.

Referencias de firma: https://developer.android.com/tools/apksigner y https://developer.android.com/about/versions/11/behavior-changes-11 . Procedencia de la biblioteca: assets/java-tools/apksig-provenance.json; Apache2 y avisos de terceros incluidos.

Registros: test/resources/release050.
