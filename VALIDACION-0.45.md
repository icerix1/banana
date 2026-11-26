# Validación de Banana0.45

- 25 comprobaciones JVM aprobadas. La nueva prueba modifica sólo el cuerpo del método, compila la copia con ECJ y verifica que ChatLanguage.socialReply use el método compilado. Incluye conservación de otras ramas, sustitución del cuerpo, condiciones de texto, protección de controles y rechazo de APIs/bucles/recursión.
- Android emulado API23: instrucción por chat → copia de ChatLanguage.java guardada y comparada con la fuente → compilación ECJ/DX → pruebas de contrato en otro PID → revisión sin activar → activación → respuesta real del motor de chat → recarga del paquete persistido → sustitución del cuerpo admitido → recuperación de versión.
- Rechazo de fuentes no admitidas, condiciones que interceptan controles y candidatos desactualizados. Los cambios de método conservan los datos de escena.
- Comprobación del protocolo numérico de la versión anterior: la función de salto sigue compilando y probándose por el servicio compartido.
- APK de producción: preparar una respuesta, revisar antes/después, aplicar y enviar la frase elegida desde la interfaz. Se esperó la burbuja exacta de Banana con la respuesta nueva. Sin FATAL EXCEPTION en esa prueba.
- Firma APK v1/v2/v3 verificada por build.sh. Instrumentación y claves excluidas del APK.
- 22 archivos de modelos .bin idénticos a0.44 por SHA-256. No se entrenó un programador general ni se añadieron modelos externos.
- 123 fuentes instaladas cotejadas contra los archivos entregados.

## Límites de la evidencia

Las pruebas cubren el subconjunto de condiciones y respuestas y el único método conectado por puente. No habilitan la edición de cualquier método/clase, compilación del APK en Android ni programación general. El proceso separado comparte permisos y no es un sandbox para Java arbitrario. Las muestras de controles no constituyen una prueba exhaustiva de todas las frases posibles.

La persistencia se comprobó recargando fuentes y métodos guardados; no equivale a un reinicio completo del dispositivo. No se probó en la tablet física Android16. Las fuentes incorporan una copia base del archivo real de0.44; se compila una clase con nombre distinto y el punto de entrada del APK la invoca mediante un puente. No se reemplaza una clase ART ya cargada.

Registros de esta ejecución: test/resources/release045.
