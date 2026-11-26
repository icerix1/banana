# Validación Banana 0.51

31 suites de escritorio aprobadas. Stage53 compila y ejecuta las 46 variantes de la gramática, comprueba las salidas visibles y reservadas, el aprendizaje del orden de búsqueda, las restricciones ABI y la normalización del DEX legado. Algunas variantes coinciden en ejemplos visibles: se comprobó que el filtro reservado rechaza esos candidatos ambiguos; no se garantiza que toda petición de la gramática encuentre solución con el orden actual.

Prueba integrada en emulador Android 6/API23: síntesis, ejecución de Java compilado en otro proceso, corrupción y reparación de método, ranking aprendido, compilación completa, referencias simbólicas de bytecode, sustitución del DEX principal y reconstrucción firmada de Banana.apk. Actualización instalada con éxito; ejecución del método nuevo y alternancia real de cuatro turnos conservando identidad de personajes. Instalación limpia del APK reconstruido: método incorporado y experiencias restauradas sin autoridad privada de firma.

APK de distribución instalada por separado: comandos reales en la interfaz para generar y ejecutar un candidato, sin excepción fatal observada. No se probó en una tablet física ni en Android16.

Verificación del APK reconstruido mediante apksigner: esquemas v1/v2/v3 válidos. El merger antiguo emitía DEX036 no reconocido en API23; se corrigió exclusivamente para entradas035 y se validaron mapa y límites antes de normalizar cabecera y recalcular firmaSHA1 y Adler32. Reinstalación y arranque confirmados después de la corrección.

Integridad: 149 fuentes coinciden con su índice y con los assets del APK de distribución; jar de base y herramientas coinciden con sus hashes de procedencia. 22 archivos binarios de modelos permanecen idénticos a0.50. APK de distribución sin instrumentation ni claves privadas. Las clases nativas probadas son idénticas a las de distribución salvo instrumentation añadida en el paquete de prueba.

Los ejemplos reservados son fixtures de desarrollo conocidos, no un benchmark externo. Durante el desarrollo se refinó el banco visible tras detectar una ambigüedad. Compilar y conservar ABI no prueba corrección semántica de Java arbitrario. Los límites completos están en LEEME.txt y ARQUITECTURA.md.
