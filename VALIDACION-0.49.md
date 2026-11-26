# Validación de Banana0.49

- 29 comprobaciones JVM aprobadas, incluidas las28 regresiones anteriores y un nuevo conjunto de extracción, fórmulas, rangos sin clamp previo, extremos de fase, despacho real del renderer/chat/secuencias y conservación del estado activo si falla guardar.
- Android emulado API23: edición de AnimationProfile.jump/sway, ChatLanguage.socialReply y ActionProfile.celebration dentro de la copia completa; compilación de los 134 Java propios con ECJ local; emisión del subconjunto validado y conversión aDEX conDX.
- Activación de animación:2050 muestras en otro PID; sin efecto antes de aplicar; Scene.pose pasa del salto original de altura12 a24 y el baile cambia8 grados en su máximo. Se recarga el módulo persistido y se comprueba nuevamente la pose.
- Los módulos compilados del chat y de acciones coexisten con animación. La entrada social real devuelve la respuesta nueva y respeta null. La secuencia compilada devuelve salta,aplaude y se ejecuta a través de JavaLive sobre un plan real deFito, conservando su identidad. Se rechaza una identidad DEX alterada antes de publicar el estado activo.
- Candidato desactualizado por una edición posterior rechazado; fórmula fuera de rango rechazada conservando el módulo activo. Tres deshacer recuperan sucesivamente los destinos anteriores y al final la pose original. Las escenas guardadas permanecen iguales durante estas activaciones.
- Actualización de base Java conserva overrides, guarda respaldo e invalida la evidencia de compilación.
- Interfaz de producción: edición del salto, compilación/preparación, activación y deshacer desde el chat; sin FATAL EXCEPTION observado durante esa prueba.
- Firma v1/v2/v3 verificada. APK sin instrumentación ni claves. Las fuentes del índice coinciden por hash con las del APK y esta entrega. 22 archivos .bin idénticos a0.48; no se añadió ni descargó un modelo externo.

## Alcance de la evidencia

Scene.draw utiliza Scene.pose para la transformación del personaje y el exportador utiliza Scene.draw. Se comprobó la pose real; no se renderizó ni comparó un MP4 completo en esta entrega. La prueba de acciones usa el puente y el planificador real; no se exportó esa secuencia aMP4.

El proceso privado de prueba comparte UID y no es un sandbox para Java arbitrario. Los cambios generales se compilan sin ejecutarse. Sólo se activan las cuatro funciones del contrato. La comprobación de rango muestrea1025 fases por curva y se aplica clamp al ejecutar; no demuestra continuidad matemática para toda fórmula condicional. El cambio de fuente Java usa una representación float canónica en este contrato, no semántica Java arbitraria.

No se probó en la tablet física Android16. Persistencia comprobada recargando estados, sin reiniciar el dispositivo. No se aumentó ni reentrenó el modelo neuronal general. Métricas anteriores de la MLP propia:36/36 base,11/12 desarrollo,11/12 evaluación; selección entre seis operadores aritméticos.

Registros: test/resources/release049.
