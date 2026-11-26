# Validación Banana0.70

58 suites host aprobadas: las 56 de0.69 más Stage79CrawlJobCheck y Stage80PdfCheck. Compilación completa con build.sh; APK700000 con el mismo certificado.

## Recorrido en segundo plano
Stage79, con un sitio simulado y la extracción real: sin recorrido no hace nada; si falla la primera página no queda nada en cola; el chat sólo pide la primera página; un segundo recorrido se rechaza mientras hay uno activo; las tandas respetan el orden por niveles y guardan el progreso; en pausa no avanza; al reanudar sin nada en memoria continúa sin volver a pedir páginas guardadas, sin pedir enlaces externos ni los vedados por robots.txt; el informe final cuenta páginas por nivel, omitidas y fallidas; el tope de páginas termina el recorrido e informa pendientes; cancelar conserva lo guardado; una página sin enlaces termina al instante; una tanda interrumpida no pierde la página; frases del chat; texto de tendencia del examen.

No comprobado: la ejecución dentro de LearningService y LearningJob en Android, el comportamiento con Doze o con el proceso detenido por el fabricante, la notificación, un recorrido real de cientos de páginas y el tamaño del estado guardado en ese caso. La descarga real de PageReader sigue sin verificarse en este entorno.

## PDF
Stage80, con seis PDF generados para la prueba (fuente estándar comprimida, ASCII85+Flate, TrueType con ToUnicode de dos bytes, flujos de objetos, cifrado y sin texto): todas las oraciones con acentos y signos fuera de Latin-1, orden entre páginas y salto de página; rechazo del cifrado, del que no tiene texto, de un archivo que no es PDF y de uno sin objetos; un archivo truncado y 200 copias con bytes alterados al azar terminan con texto o con un error claro, nunca con una excepción inesperada; filtros ASCII85 y hexadecimal; unión de renglones; documento guardado en UTF-8 y respuesta con su fuente.

Fuera de la suite, a mano: la Constitución española del BOE (330KB, 118767 caracteres extraídos) y un artículo de arXiv hecho con LaTeX (2,2MB, 38535 caracteres) dieron texto legible. No se probaron PDF de más de 3MB, con columnas o tablas complejas, con fuentes Type3 o codificaciones propias sin ToUnicode, ni la descarga del PDF por PageReader.fetch.

## Lector 2: no se adoptó
Continuación de 130 minutos desde Lector 1 (17993 pasos, 57,7 millones de tokens) con oraciones rotadas, título antepuesto y más negativas. Sobre las mismas 350 preguntas apartadas: oración exacta 121 (Lector 1: 137); con el filtro de la app mostró 108 y acertó 101 (Lector 1: 122 y 110); como segunda opción mostró 37 y acertó 34 (Lector 1: 45 y 39); en preguntas de relación 15 (Lector 1: 30). En diez fragmentos escritos a mano acertó 4 con 1 oración equivocada (Lector 1: 3 con 7). Es más prudente pero no mejor, y esas preguntas no llevan título ni rotación, que es para lo que se entrenó. La app conserva Lector 1 sin cambios; el registro está en model-source/lector/lector2.

## No incluido de la lista de PROXIMA-ETAPA.md
Respuestas cortas, preguntas encadenadas, tope mayor de la biblioteca, búsqueda por vectores, entrenador más rápido, preguntas generadas por el modelo, vocabulario que crece, respaldo del progreso, un solo modelo para conversar y leer, completar métodos Java y autoprueba antes de instalar. Sigue sin ejecutarse nada en emulador ni en dispositivo.
