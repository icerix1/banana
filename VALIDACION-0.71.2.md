# Validación Banana0.71.2

61 suites host aprobadas: las 60 de0.71.1 más Stage83TalkCheck. Compilación completa con build.sh; APK712000 con el mismo certificado.

## Lector 3.2
Continuación de 75 minutos desde Lector 3.1 (9211 pasos, 33 millones de tokens) con un 22 % de ejemplos de conversación. Lectura en 160 preguntas apartadas: 0,956 y calla ante dato ausente 0,957, igual que antes.

Informe de la app (test/resources/lector3-heldout-report.json): 330 de 343; buscador léxico 162; con seguridad≥0,7 mostró 312 y acertó 305; sin respuesta léxica mostró 157 y acertó 157; ante 50 preguntas sin dato mostró algo en 1. Prueba difícil (test/resources/lector3-hard-report.json, 200 preguntas con unas 22 oraciones del mismo artículo): eligió bien 153; con seguridad≥0,7 mostró 183 y acertó 147. Preguntas de plantilla; no es un benchmark externo.

Orden de candidatas en esa prueba: con sólo palabras compartidas la respuesta quedó entre las 12 primeras en 199 de 200; con peso 0,5 a 4 del parecido de significado, 196; sólo con el parecido, 178. Se dejó en 0,05.

Fallas encontradas y corregidas durante las pruebas: «dónde murió tomas rivel» (tildes mezcladas) puntuaba 0,29 la oración correcta contra 0,96 con «donde murio tomás rivel»; con el título «Doc» antepuesto la oración del nacimiento bajaba de 0,93 a 0,40.

## Charla por selección
30 respuestas redactadas, 6 formas de entrenamiento y 2 apartadas cada una (60 apartadas). El modelo solo, eligiendo entre las 30: 18 de 60. En la app (Stage83): la respuesta correcta está entre las 8 preseleccionadas en 54; el parecido de palabras solo acierta 30; el modelo eligiendo entre las 8, 22; la regla adoptada (el modelo confirma la primera del parecido con≥0,5) responde 12 y acierta 11. Ocho preguntas de conocimiento no recibieron respuesta de charla. Es una medida chica y las formas apartadas las escribí yo: mensajes reales pueden alejarse más.

## Resto de Stage83
Sujeto prestado sólo cuando falta, en su orden original, conservado entre seguimientos y olvidado con la biblioteca; la pregunta completada se responde con la oración correcta. Respuesta corta: fecha y lugar del asunto preguntado en una oración con nacimiento y muerte, cantidad con unidad, nombre tras «por», nada cuando no es claro, y siempre un pedazo literal. Dos faros con constructores distintos producen alternativa; una respuesta única no. Examen con preguntas del usuario: marcas, repetición, reemplazo de la misma pregunta y tope. Búsqueda de ejemplos por pedido y motivos del compilador en palabras simples. SelfCheck.pure pasa.

## Sin comprobar
Nada se ejecutó en emulador ni en dispositivo. En particular: SelfCheck.before (carga del DEX nuevo y de la APK instalada en cargadores aislados) sólo se compiló, y si no puede ejecutarse deja seguir la reconstrucción; CodeBank.autoVerify y el ChatBank dentro de BananaEngine tampoco se ejercitaron en Android. El tiempo por pregunta subió (hasta 30 pasadas del modelo) y no está medido en tablet. No se midió la charla con mensajes escritos por otra persona.

## No incluido
Entrenamiento con GPU (pedido expresamente que no). Búsqueda por vectores con un modelo entrenado para eso: la versión barata no ayudó. Elegir código con el lector: la búsqueda de ejemplos es por palabras.
