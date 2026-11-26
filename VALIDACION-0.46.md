# Validación de Banana0.46

- 26 comprobaciones JVM aprobadas, incluidas las regresiones de creación, conversación, persistencia, audio, modelos, módulos Java y edición de métodos.
- Nuevo paquete: aislamiento de candidato, activación conjunta de perfil/respuestas/comportamientos/paneles, rechazo de lote parcialmente inválido, recarga, recuperación y acciones de panel acotadas.
- Nuevo recuperador: pesos realmente entrenados, uso de aportes del usuario, recuperación de variantes, separación de una variante de evaluación, preguntas ajenas/negadas/números distintos, persistencia, pausa, recuperación de datos y modelo, fallos de publicación y de borrado. Las variantes duplicadas no se admiten como ejemplos apartados.
- Android emulado API23: paquete sin cambiar versión de APK durante las operaciones, respuesta real del chatbot, aplicación de una secuencia a la escena, configuración de paneles, recarga y deshacer conjunto sin borrar el proyecto.
- Android: entrenamiento local a partir de una respuesta explícita y de ‘anota esta correccion’, cambio de la siguiente respuesta real, evaluación de variante apartada, recarga de pesos, pausa, entrenamiento explícito y recuperación de la versión anterior.
- Interfaz de producción: preparación y activación de paquete, respuesta modular exacta, apertura del panel Estudio y su herramienta Personajes, enseñanza y recuperación de una respuesta desde el chat. Sin FATAL EXCEPTION en esa ejecución. El controlador verifica el contenido completo del campo antes de enviar; las primeras inyecciones de texto incompletas del entorno de prueba fueron descartadas.
- Firma v1/v2/v3 verificada. APK sin instrumentación ni claves.
- 22 archivos .bin de los modelos base idénticos a0.45 por SHA-256. Los nuevos parámetros se entrenan en los datos privados del dispositivo; no hay un modelo externo agregado ni un generador grande reentrenado.
- 125 fuentes instaladas cotejadas contra los archivos entregados.

## Límites

La última comprobación de variantes duplicadas y separación de secuencias numéricas fue en JVM; los flujos de interfaz y Android usaron ejemplos válidos del mismo motor. Las muestras prueban casos concretos, no inteligencia general ni comprensión semántica completa. La similitud léxica puede fallar en paráfrasis muy distintas y no determina la verdad de una respuesta aportada por el usuario. El entrenamiento no usa su propia salida como etiqueta verdadera.

La persistencia se probó recargando los estados guardados; no equivale a reiniciar por completo el dispositivo. No se probó en la tablet física Android16. Esta entrega requiere una actualización inicial; los cambios de módulos y ejemplos posteriores se aplican dentro de esa instalación. El núcleo, los permisos, nuevas bibliotecas y componentes Android quedan fuera de los paquetes.

Registros: test/resources/release046.
