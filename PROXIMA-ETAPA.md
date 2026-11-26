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
