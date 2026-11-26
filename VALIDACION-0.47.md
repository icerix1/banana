# Validación de Banana0.47

- 27 comprobaciones JVM aprobadas, incluidas regresiones de conversación, creación, persistencia, audio, módulos, Java y aprendizaje local.
- Prueba nueva con compilación real ECJ del método: errores observados, corrección por pasos, nueva compilación y ejecución del método reparado. Cadenas nuevas detectan la comparación por identidad aun cuando Java compila. Se verifican preservación de literales, retorno final, normalización de if simple y rechazo de APIs/bucles/cadenas incompletas.
- Android emulado API23: último intento de editor → diagnóstico real ECJ → reintentos → pruebas en proceso separado → candidato sin activar → activación explícita → respuesta real del chatbot.
- Android: casos registrados sólo tras aplicar; un intento no admitido invalida la acción de aplicar una reparación anterior; conservación del método activo y proyectos; error de comparación String== descubierto por pruebas; búsqueda de archivos reales con evidencia y advertencia de que no confirma causa.
- Suite Android interna:4/4 tareas sintéticas de regresión, sin modificar método activo ni borrador. No es una evaluación de tareas desconocidas o de programación general.
- Interfaz de producción: envío completo de un intento inválido, reparación del último cambio, revisión del antes/después, activación y espera de la burbuja exacta de Banana con la nueva respuesta. Sin FATAL EXCEPTION en esa prueba.
- Firma APK v1/v2/v3 verificada. Sin instrumentación ni claves en el APK.
- 22 archivos .bin base idénticos a0.46 por SHA-256. No se añadieron modelos externos ni se entrenó un programador neuronal nuevo.
- 127 fuentes instaladas cotejadas con los archivos de esta entrega.

## Límites

Las reglas son explícitas, finitas y dirigidas a un único método conectado por puente. Los fallos de la suite fueron diseñados para ese repertorio;4/4 no mide capacidad de programación general. La memoria prioriza reglas de casos comprobados y aplicados, no descubre por sí sola nuevos algoritmos de reparación. La búsqueda es una orientación lexical/de nombres, no una localización causal automática.

El servicio de pruebas comparte UID y permisos. No es un sandbox de Java libre. El lenguaje previo a ejecución está acotado. La compilación se efectúa en Android, aunque la construcción y firma de esta entrega de la app se realizó en el entorno de desarrollo.

Persistencia y recuperación se comprobaron en la infraestructura heredada y en las regresiones; no equivalen a reiniciar físicamente la tablet. No se probó en el dispositivo Android16 del usuario.

Registros: test/resources/release047.
