# Mejoras propuestas después de0.69

Estado tras0.71: el lector cambió a un selector de oraciones que acierta mucho más (ver VALIDACION-0.71.md), con lo que los puntos 1, 2 y 4 quedan atendidos por otro camino; hechos además exploración de internet por tema, autoestudio y preguntas escritas por el modelo (punto 15, en versión simple). Siguen pendientes los puntos 3, 5, 6, 9, 12, 14, 16 a 22.

Estado tras0.70: hechos el 7 y el 8 (recorrido en segundo plano y reanudable), el 10 sólo para PDF por enlace, y la tendencia del examen para el 13. Los puntos 1, 2, 4 y 5 se intentaron con un Lector 2 que no resultó mejor y no se adoptó (ver VALIDACION-0.70.md). El resto sigue pendiente.

Lista de trabajo pendiente, ordenada por cuánto aportaría. Nada de esto está hecho. Los números de partida salen de VALIDACION-0.69.md.

## Primero: comprobar0.69 en la tablet
El entrenamiento del lector, el examen propio, el crecimiento, el banco de ejemplos y la autoprueba sólo se probaron en el equipo de desarrollo. Antes de sumar más conviene instalar la APK y mirar «hacé una autoprueba», «estado del lector» y «tomate un examen». Lo que falle ahí se corrige antes que cualquier mejora.

## Comprensión
1. **Entrenar más la base del lector.** Hoy acierta la oración exacta en 137 de 350 preguntas apartadas. Tuvo 100 minutos de CPU y 43 millones de tokens; seguía mejorando al cortar. Varias horas más, con más artículos, es la mejora más barata.
2. **Corregir su preferencia por la primera oración.** En fragmentos cortos elige casi siempre la primera. Hay que mezclar el orden de las oraciones al generar ejemplos y agregar más casos donde la respuesta está al medio o al final.
3. **Responder con la parte justa, no con la oración entera.** Entrenar un segundo formato donde la respuesta es el tramo que contesta («en 1802», «al sur de la provincia»), siempre literal.
4. **Que sepa decir que no.** La negativa («no encontré ese dato») acierta poco; subir su proporción en el entrenamiento para que calle en vez de elegir mal.
5. **Usar el título y la fuente como parte del fragmento**, para que «cuándo nació» encuentre al sujeto aunque la oración diga «nació en…».
6. **Preguntas encadenadas.** Pasarle la pregunta anterior para resolver «¿y dónde murió?».

## Leer más y mejor
7. **Recorrido en segundo plano.** Hoy un recorrido de subenlaces bloquea el chat hasta una hora. Moverlo al servicio de aprendizaje, con notificación, pausa y botón de cancelar.
8. **Reanudar recorridos.** Guardar la cola de enlaces pendientes para seguir otro día en vez de empezar de nuevo.
9. **Biblioteca más grande.** El tope de 1024 documentos y 512 MiB limita los recorridos; subirlo exige revisar índice, respaldos y APK reconstruida.
10. **Leer PDF y páginas con JavaScript simple.** Hoy sólo HTML y texto.
11. **Elegir qué subenlaces valen la pena**: priorizar por parecido con lo que preguntás seguido y saltear páginas casi repetidas.
12. **Búsqueda por significado.** Guardar un vector por bloque, calculado con el propio lector, para encontrar fragmentos que no comparten ninguna palabra con la pregunta. Hoy sin palabra en común no hay respuesta.

## Aprender en el dispositivo
13. **Medir si el examen propio sube con los días.** Es el dato que falta para saber si entrenar en la tablet sirve. Si no sube, hay que cambiar el ritmo de aprendizaje antes de agregar nada.
14. **Entrenamiento más eficiente.** El entrenador actual recorre los pesos con bucles simples; usar varios núcleos y un optimizador con memoria (Adam) puede multiplicar lo aprendido por hora.
15. **Preguntas hechas por el modelo.** El examen usa dos plantillas fijas. Entrenar al lector para generar la pregunta a partir de una oración daría exámenes y repasos más variados.
16. **Vocabulario que crece.** Agregar piezas nuevas para términos frecuentes de tus lecturas, con comprobación antes y después.
17. **Respaldo del progreso.** Exámenes, cursores y banco de ejemplos no viajan en respaldos ni en la APK reconstruida.

## Conversación
18. **Un solo modelo para conversar y leer.** Chat 3.4 quedó chico (783.518 parámetros, 542 piezas). Entrenar el lector también con el corpus de diálogo propio y pasarle la conversación reciente permitiría retirar Chat 3.4.
19. **Respuestas armadas con varias oraciones** de fragmentos distintos, cada una con su fuente.

## Autoprogramación
20. **Completar métodos con prueba.** Entrenar al lector con los ejemplos de Java que compilaron y pedirle que complete el cuerpo de un método; aceptar sólo lo que compile y pase las 64 pruebas generadas que ya existen para listas y texto.
21. **Aprender de los errores del compilador.** Guardar cada intento fallido con su mensaje como ejemplo de qué no hacer.
22. **Más comprobaciones en la autoprueba** y ejecutarla sobre la APK reconstruida antes de ofrecer instalarla, no después.

## Límite que no cambia
Con un modelo de pocos millones de parámetros entrenado en una tablet, el resultado alcanzable es un asistente que encuentra y cita bien lo que leyó y completa funciones pequeñas. Razonar en general o programar libremente requiere entrenar en una computadora durante mucho más tiempo, o un modelo externo, que el proyecto evita.

--- Etapas anteriores ---

# Camino hacia cambios completos de la app

Banana 0.43 entrega módulos Java preparados para interfaz, animación y contexto, compilación conjunta, pruebas de contrato, vista previa, historial, recuperación e índice del código instalado. Mantiene el modelo propio. Esto no completa la meta de programación general.

| Parte propuesta | Estado en 0.43 | Trabajo pendiente |
| --- | --- | --- |
| Mapa del código | Índice real con rutas, hashes y referencias entre archivos; lectura desde el chat | Navegación semántica, búsqueda por símbolo y cálculo fiable de impactos |
| Programador propio | Planificador local de ajustes concretos y generador de Java acotado | Modelo propio de edición de código, corpus autorizado, entrenamiento, evaluación de cambios fuera del repertorio |
| Varios archivos | Compilación conjunta de cuatro archivos preparados y activación conjunta de interfaz, animación y contexto | Cambios arbitrarios de firmas, imports y dependencias |
| Prueba separada | Directorio de compilación separado y módulos sin activar, pruebas reflectivas y límites estructurales | Proceso de prueba aislado, pruebas de UI generadas y vigilancia de fallos posteriores |
| Vista previa | Fuente y plan persistentes, probar y después aplicar/descartar | Comparación visual interactiva de pantallas y ejecución temporal de proyectos complejos |
| Recuperación | Once versiones previas, retorno por número y deshacer | Recuperación por fecha natural, protección ante arranque fallido y limpieza de caché compilada |
| Diagnóstico web | Búsqueda explícita del error mediante las fuentes existentes | Reparaciones justificadas y verificadas para problemas fuera del vocabulario preparado |
| APK completo | build.sh conserva la reconstrucción externa de las fuentes y firma existente | Toolchain Android completa que funcione en dispositivo, recursos/manifest, entrega firmada e instalación mediante Android |

## Criterios para habilitar edición Java más general

1. Entrenar y evaluar un editor propio con tareas Java nuevas, no sólo reconocer frases de un catálogo.
2. Probar cambios de varios archivos con renombres, dependencias, entradas desconocidas y proyectos preservados.
3. Ejecutar código candidato en un proceso separado con límites de tiempo y recursos; la validación acotada actual no es un sandbox para Java arbitrario.
4. Añadir pruebas de interfaz, persistencia, exportación y recuperación frente a fallos de arranque.
5. Incluir un proceso de construcción de APK reproducible y gestión de firma adecuada al canal de distribución. Respetar la instalación mediada por Android.

No eliminar las restricciones actuales para aparentar una capacidad de programación que el modelo todavía no tiene.

Documentación técnica de Android consultada para el diseño:
- https://developer.android.com/about/versions/14/behavior-changes-14 — código dinámico en archivos de sólo lectura.
- https://developer.android.com/reference/android/content/pm/PackageInstaller — instalación y estados que pueden requerir acción del usuario.


## Avance entregado en0.44

Se definió y compiló una función nueva fuera del catálogo de constantes: curva completa de salto con expresiones matemáticas y tramos. Hay pruebas en un proceso privado separado (no sandbox), evaluación de contrato, activación, persistencia y recuperación. No equivale a edición libre de Java ni a entrenamiento del programador general; esas metas de la tabla siguen pendientes.


## Avance entregado en0.45

Se editó una copia del método real ChatLanguage.socialReply, se conservó el archivo restante y se incorporaron comparación antes/después, compilación, pruebas de respuestas en proceso separado, activación por puente y recuperación de versiones. Admite modificaciones puntuales y reemplazo del cuerpo dentro de un subconjunto de condiciones de texto. No es un editor general de todos los métodos ni un modelo programador; esas capacidades continúan pendientes.


## Avance0.46

Paquetes atómicos de datos para interfaz, respuestas, secuencias y paneles; aprendizaje local de un recuperador a partir de correcciones, pesos persistidos, evaluación de variantes y recuperación. No se entrenó el generador general ni se habilitó Java arbitrario. Pendientes: evaluación semántica más amplia, editor de pantallas más flexible, expansión de datos y un programador propio con pruebas independientes.


## Avance0.47

Se incorporaron diagnóstico real ECJ, compilación/prueba/reintento en Android, detección por pruebas de un error que compila, staging seguro, memoria de casos aplicados, búsqueda de evidencia en las fuentes y regresión local. El repertorio de reparación es explícito y acotado: no constituye entrenamiento de un programador general ni habilita cambios entre clases arbitrarias.


## Avance0.48

Copia de todos los Java, modificaciones entre archivos y clases nuevas, compilación completa de Java en Android, versionado, corpus original separado y MLP propia para seleccionar operaciones en ediciones de un método fuera del chat. El modelo sólo aprende seis operaciones y no constituye un programador general. Pendientes: representación de tipos/contexto, generación de tokens/AST más amplia, corpus y tareas nuevas con licencias compatibles, pruebas generales en aislamiento adecuado, recursos Android y construcción/activación completa.
