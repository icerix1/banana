# Banana

App Android (`com.bananalab.lite`) en Java puro: chat con Transformer propio, creación de escenas animadas, biblioteca de conocimiento y entrenamiento en el dispositivo. Versión de estas fuentes: **0.71.2** (charla por selección de respuestas, seguimiento de la conversación, respuestas cortas, examen con preguntas del usuario, autoprueba antes de instalar; preguntas que unen dos lecturas, contradicciones entre fuentes, biblioteca de 4096 documentos; lector neuronal que compara oraciones, exploración de internet por tema en segundo plano y autoestudio; conserva recorridos reanudables, PDF, examen propio, ejemplos de Java y autoprueba).

- `LEEME.txt`: guía de uso. `ARQUITECTURA.md`: mapa técnico. `VALIDACION-*.md`: pruebas por versión.
- `src/`: código Java. `assets/`: modelos y recursos. `model-source/`: scripts y registros de entrenamiento (`model-source/lector/` para el lector).
- `dist/Banana-0.71.2-Android.apk`: APK de esta versión. `dist/Banana-0.66-Android.apk`: APK original 0.66.

## Compilar

Requiere Android Build Tools 35.0.0, `android.jar` de API 35, Eclipse ECJ 3.38.0, Java y Python 3:

```sh
ANDROID_TOOLS=/ruta/build-tools ANDROID_JAR=/ruta/android.jar ECJ_JAR=/ruta/ecj-3.38.0.jar sh build.sh
```

La carpeta `keys/` (clave de firma `release.p12` y su contraseña) **no está en el repositorio** a propósito. Hay que copiarla localmente antes de compilar para que la APK conserve la firma original.
