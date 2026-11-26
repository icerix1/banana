# Banana0.66: entrenamiento continuo Android y ejercicios documentados

LearningLoop coordina preferencias, métricas, historial12 ejercicios, condiciones, repasos y trabajo periódico6601. LearningService es un foreground service specialUse con notificación silenciosa y pausa; sólo se inicia desde actividad visible. LearningJob ejecuta una microtanda como fallback dentro de la cuota de JobScheduler sin lanzar un foreground service. LearningBoot programa al reiniciar/actualizar. LiteApplication registra visibilidad y BananaActivity señala trabajo de chat/editor, además del panel Proyecto → Aprendizaje continuo.

ReadingTraining usa TRAIN.tryLock para serializar candidatos, con LOCK sólo para pequeñas operaciones de estado y publicación. Así las fuentes nuevas y la pausa no esperan todo el SGD. collect reúne metadatos en una sola pasada. Antes de publicar revalida cursor/fuente/revisión y avanza sobre el estado fresco para no perder altas concurrentes. El journal de0.65 recupera el cursor si falla su confirmación. El entrenamiento de fondo no cambia JavaChat.owner/progress del compilador de interfaz.

SourceExercises genera preguntas por plantillas desde definiciones o frases cortas. Target literal con ID/SHA/evidencia, sin usar la respuesta del modelo como label. Intento propio greedy limitado12 tokens y verificación por coincidencia normalizada; no equivale a exactitud semántica ni verdad externa. ReadingWeights.fit micro usa una época con controles locales y pesos completos; conservamos el entrenamiento manual anterior y el formato binario.

Al terminar una revisión se permiten hasta tres recorridos por día UTC con separación mínima15min; no se fabrican fuentes. ReadingBudget limita cada tanda y cede por actividad/recursos. El servicio sólo mantiene WakeLock temporal por tanda. ModelDepot sigue publicando archivos inmutables y copia activa a la APK. DurableCode agrega continuousLearning opcional, valida y restaura preferencias/historial, manteniendo compatibilidad con manifiestos antiguos. NotebookArchive continúa separado y completo.

Referencias Android consultadas: https://developer.android.com/develop/background-work/services/fgs/service-types y https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start . specialUse describe entrenamiento local prolongado solicitado por el usuario; no implica aceptación automática de una publicación en Google Play. La tarea de fallback y sus cuotas siguen bajo Android. No se solicita excluir la app de optimizaciones ni se fuerza reinicio después de una parada del usuario.

--- Arquitectura previa ---

# Banana0.65: lectura y adaptación conversacional Android

PageReader → PageStream: descarga/comprimido/descomprimido máximo64MiB y extracción en archivos, sin DOM completo. NotebookDisk/NotebookSQLite conservan texto e índice. KnowledgeNotebook.rememberReader confirma primero el documento y luego llama ReadingTraining.afterLearning; una excepción posterior no revierte ni oculta ese guardado.

ReadingTraining mantiene cola por ID/SHA/ordinal/tokenOffset; una tanda por alta, hasta tres a pedido. ReadingWeights aplica gradientes a todo BananaTransformer en un candidato independiente. Entrena definiciones pequeñas completas, continuaciones de texto y diálogo propio, valida paráfrasis de los mismos hechos/segmentos separados y retención. No hay modelo externo, respuestas sintéticas usadas como verdad ni autoejecución de instrucciones web. El tokenizador no cambia.

ModelDepot publica checkpoints inmutables con control de revisión y respaldo previo. ReadingTraining registra un journal previo a publicar para recuperar el cursor si falla su confirmación. ReadingBudget limita tiempo/memoria/calor. DurableCode incorpora progreso y ModelDepot.baked los pesos activos; NotebookArchive transporta documentos por separado. La consulta léxica continúa usando las fuentes incluso si el candidato se rechaza.

La pérdida local no mide inteligencia general. Sólo una fracción del documento se entrena por tanda; puede rechazarse. Borrar documentos no borra información de pesos. No hay reentrenamiento masivo ni servicio autónomo permanente.

--- Arquitectura previa ---

# Banana: mapa del proyecto

Una sola experiencia para conversar, diseñar, animar y guardar. Chat 3.4 y Pixel 2.3 son motores internos propios.

| Parte | Archivo principal | Responsabilidad |
| --- | --- | --- |
| Pantalla única | BananaActivity.java | Conversación, resultados y ciclo de vida Android |
| Coordinación | BananaEngine.java | Un único recorrido para texto, herramientas y creación |
| Estado del proyecto | BananaProject.java | Editor y texto modifican el mismo borrador o escena |
| Conversación | BananaConversation.java | Historial compatible y versiones visuales inmutables |
| Resultado visual | BananaSceneCard.java | Vista previa, reproducción y controles por resultado |
| Edición | BananaEditor.java | Personajes y referencias dentro de la tarjeta |
| Preparación visual | BananaRender.java | Mismo estado y preparación en pantalla y al guardar |
| Guardado de archivos | BananaExporter.java | PNG, GIF, MP4 y series desde copias del resultado |
| Fuentes públicas | BananaReferences.java | Referencias opcionales con origen y créditos |
| Planificación | CreatorSession / ScenePlanner / SceneDirector | Pedidos, borradores, acciones y correcciones |
| Modelo de escena | SceneDraft / ScenePositions / ObjectTransfer | Identidades, tiempos, objetos y posiciones |
| Texto neuronal | TransformerBrain / BananaTransformer | Modelo propio Chat 3.4 |
| Imagen neuronal | BananaPixel / NeuralSprites / PixelGenerator | Modelos y banco propio Pixel 2.3 |
| Render y sonido | Scene / RigRenderer / PreviewSound / Soundtrack | Animación 2D y audio sintetizado |

MainActivity, ChatActivity y CharacterStudio son entradas de compatibilidad: redirigen al espacio único y no contienen otra implementación del producto.

## Reglas del estado

- CreatorMemory y SceneStore siguen siendo la autoridad para borradores y escena publicada.
- Cada resultado en la conversación guarda su propia copia JSON; editar el proyecto no altera versiones anteriores.
- Seleccionar un personaje actualiza el foco compartido, sin convertirlo en otro diseño.
- El editor rechaza cambios si la versión cambió mientras tanto.
- Retomar una versión requiere resolver antes un borrador pendiente.
- Un guardado captura la versión elegida antes de abrir el selector de archivos.
- Borrar conversación conserva proyecto, borrador, personajes, escenas y recuerdos del proyecto.
- Los modelos siguen siendo propios, locales y limitados; unificar la interfaz no aumenta su capacidad neuronal.

El historial visible mantiene hasta40 mensajes. La escena actual y las bibliotecas siguen guardadas por separado aunque sus mensajes salgan de ese límite.

## Componentes de0.34

- SceneResolver: referencias apoyadas en acciones y propietarios reales.
- PlanAudit: cobertura acotada del pedido y conservación de designId/aspecto.
- SceneSettings: ambiente independiente de personajes y acciones.
- TextQuality: filtro léxico y de repetición, sin verificación factual.
- FaceAnchor: parches faciales neuronales propios sobre cuerpos propios.
- ActorGeometry / JointMath: anclas compartidas, transformación inversa y brazos2D.
- chat34/: texto; pixel22/: caras y clasificador; pixel-v2/tomato22: tomate;
  background/background22 y bank22: escenarios. Los demás cuerpos siguen en character21.

## Etapa0.35: comprensión operativa, memoria y ejecución

| Componente | Función |
| --- | --- |
| TaskFlow | Hasta8 pasos sobre copias; publicación conjunta o ningún cambio |
| ProjectEdits | Reemplazo por tipo soportado y rasgos con referencias al estado |
| TaskReceipt | Registro persistido de pasos y diferencias de diseño comprobadas |
| SceneGuide | Respuestas sobre aspecto, acciones, relaciones y resumen guardados |
| ProjectMemory | Recuerdos explícitos con IDs estables, corrección y borrado individual |
| CreatorSession | Aísla borradores y conserva un único paso de deshacer por pedido |

Los pesos activos continúan siendo Chat3.4 y Pixel2.2. Esta etapa no cambia sus
arquitecturas ni capacidades generativas generales. Las reglas y herramientas
resuelven operaciones acotadas sobre el estado persistido, separadas del texto neuronal.
Los recuerdos no ejecutan herramientas. Una transacción por pasos solamente
modifica escenas; no promete transacciones con memoria, red o archivos externos.

## Etapa0.36: contexto, continuidad y coherencia

| Componente | Función |
| --- | --- |
| SceneContext | Resuelve referencias antes de analizar verbos y registra el foco anterior |
| SceneResolver | Distingue eventos de recepción de la propiedad final; resuelve nombres y selectores |
| ScopedAppearance | Cambia una categoría de accesorio sin aproximarla mediante un cambio de cuerpo |
| SceneContinuation | Añade al final o reemplaza acciones solamente mediante pedido explícito |
| SceneConsistency | Simula propiedad por tiempos y exige entregas/recepciones coincidentes |
| PlanAudit.verifyExtension | Comprueba que la continuación conserva el prefijo de acciones |
| SceneGuide | Contesta consultas de identidad, eventos, poseedores y contradicciones del estado |

SceneDraft añade previousFocus con valor vacío en proyectos anteriores. Los
cambios de foco se excluyen de la comparación de versiones del editor. Los pedidos
compuestos resuelven cada referencia contra su copia intermedia, no contra el
estado inicial de todos los pasos. Un fallo no publica cambios parciales.
La preparación visual también rechaza secuencias contradictorias. Esto no mide
física ni calidad visual. Las herramientas no dependen del candidato neuronal.

El experimento propio Chat3.5 está en model-source/chat35-candidate; no en assets.
Los archivos activos de modelos/bancos continúan iguales a0.35.

## Entrega 0.37: recorrido compartido

| Parte nueva | Responsabilidad |
| --- | --- |
| ConversationAssist / ContextEncoder | Aclaraciones persistentes, ayuda contextual, propuesta revisable y clasificación neuronal propia |
| CustomCharacter / custom23 | Rasgos de criaturas, inferencia propia y banco de máscaras generado por el modelo |
| DialogueCommands / VoiceSynth | Frases, voces y síntesis original por formantes |
| SceneTiming | Duración compartida, límites y ajuste a las frases |
| SceneTakes / SeriesExporter | Continuidad, recuperación de serie y preparación progresiva de tomas |
| Scene.pose / ActorGeometry | Transformación compartida de cuerpos, manos y objetos |

Los límites actuales y las instrucciones están en LEEME.txt. Cualquier sección histórica anterior describe su versión, no los límites vigentes. Chat3.4 permanece activo; el nuevo encoder contextual no reemplaza su generador. Voz DSP experimental, no TTS neuronal.

## Entrega 0.38: investigación sin cambiar los pesos

| Componente | Función |
| --- | --- |
| ResearchConversation | Preguntas, repreguntas, comparaciones, seguimiento y comandos de control |
| ResearchWeb | Buscador, lectura HTTPS pública acotada, alternativa Wikipedia y separación de datos/órdenes |
| ResearchAnswer | Recuperación de fragmentos pertinentes y atribución, sin síntesis generativa |
| ResearchStore | Biblioteca de 80 fuentes, ocho temas, fechas, estado y protección contra repoblación tras borrar |
| ResearchScheduler / ResearchJob | JobScheduler persistente, una ronda horaria orientativa y control de red/pausa |
| BananaEngine | Herramientas y escena primero; investigación antes del generador experimental para preguntas pertinentes |

El chat conserva Chat3.4 + Context1; esta fase no cambia pesos ni promete comprensión universal. La consulta incluye solo la pregunta reformulada, no el historial ni la escena. Las fuentes no alimentan el entrenador. La memoria de referencias visuales anterior sigue separada. La documentación vigente de uso y restricciones está en LEEME.txt.

## Entrega 0.39: núcleo móvil

| Componente | Responsabilidad |
| --- | --- |
| MobileCore | Memoria del turno, control de corrección, estilo, frases enseñadas, registro de efectos y coordinación |
| MobilePlan | Separación respetando comillas, plan de doce pasos, sandbox de escena, reparación y comprobación de versión |
| MobileKnowledge / mobilecore1 | Selector neuronal propio, filtro de alcance y recuperación de 24 lecciones originales |
| SceneResolver / SceneContext | Referencias a acciones reales del personaje, sin inferir eventos futuros |
| BananaEngine.raw | Herramientas y generador anteriores, conservados detrás del núcleo nuevo |

MobileCore es una capa funcional y un nuevo clasificador de temas, no un reemplazo de Chat3.4 por un modelo general. La inferencia del núcleo y las lecciones ocurre en Android; investigar páginas sigue requiriendo Internet. Los planes guardan una versión de escena y borrador, se ejecutan sobre copias y publican una vez al final. Los planes y frases son persistentes, acotados y controlados por el usuario. Borrar conversación limpia el contexto del turno y conserva proyectos y aprendizaje explícito.

## Corrección 0.39.1: beso entre dos personajes

ScenePlanner y PlanAudit comparten la normalización de besar/da un beso. SceneDraft y Timeline conservan besa/recibebeso con destinatario. SceneConsistency exige una pareja coincidente. KissMotion calcula acercamiento de anclas faciales con tamaños distintos y una envolvente que vuelve a la postura. Scene.pose aplica esa misma geometría a cuerpos y manos; la orientación, el gesto y los corazones se dibujan desde el tiempo de la escena, compartido con MP4. No hay modelos nuevos ni cambios de pesos.

La propuesta de autodiagnóstico/actualización de código se explica en LEEME.txt como posibilidad futura. Esta versión no incluye ejecución dinámica de código, generación de parches ni autoinstalación.

## Entrega 0.40: programas modificables en Android

| Parte | Función |
| --- | --- |
| RuntimeProgram | Lenguaje JSON acotado, parámetros, interpretación de pasos y pruebas en fixtures separados |
| RuntimeModules | Borrador, pruebas, activación atómica, revisiones, recuperación y reparadores por plantillas |
| RuntimeWorkshop | Editor local, inspección, pruebas, activación y recuperación desde pantalla |
| MobileCore | Despacha módulos antes del generador, preservando controles del chat |
| MobilePlan | Integra módulos en transacciones de escena; incluye la revisión de módulos en el snapshot |

El código de módulo vive en SharedPreferences propio; el Java y los pesos del APK permanecen estables. El estado activo se relee por pedido. Las fuentes web nunca se ejecutan como código de módulo. Los parámetros se vinculan a nombres/tipos exactos o selectores explícitos; no se interpretan instrucciones del chat como fragmentos de actor. El programa de módulo puede inspeccionarse en el editor y con ver codigo de modulos. No se implementa recompilación del APK ni modificación arbitraria de Java.

## Entrega 0.41: compilación Java real en Android

| Componente | Papel |
| --- | --- |
| JavaPatchPolicy | Subconjunto de fuente permitido para una clase preparada y límites de resultados |
| OnDeviceJava | ECJ → class → DX → DEX; almacenamiento privado de solo lectura y DexClassLoader |
| JavaLive | Fuente, borrador, compilación, pruebas, activación de clase, recuperación y ganchos de escena |
| JavaWorkshop | Editor Java y controles de compilación en pantalla |
| BananaBehavior.java | Fuente de la clase preparada en assets/java-source |

MobileCore llama a la reescritura Java activa y permite ejecutar celebration sobre la escena. KissMotion usa la separación cargada desde kissGap. El APK no contiene una clase BananaBehavior precompilada en su cargador principal: la clase compilada se carga con un cargador nuevo por revisión. La fuente Java se reescribe de verdad y se compila dentro del dispositivo; no se modifica cualquier clase ya cargada ni se firma un APK nuevo en este recorrido. ECJ/DX son dependencias de compilación externas, no pesos de IA.

## Entrega 0.42: Java mediante conversación

JavaChat interpreta ajustes admitidos de celebración/separación, valida que no se pierdan instrucciones y llama al reescritor y compilador reales de JavaLive. Sus respuestas principales y la barra de progreso usan lenguaje sencillo. Las pruebas de celebración se ejecutan por JavaLive.handle sobre el personaje seleccionado o nombrado. El retroceso está vinculado a la revisión del último ajuste conversacional. No se modifica el subconjunto de Java permitido ni se reemplaza un modelo generativo.


## 0.43: módulos compilados para la app

AppJavaChat planifica ajustes locales definidos en AppJavaWorkspace.Settings. Genera cuatro archivos Java, los compila juntos mediante OnDeviceJava.compileSources, carga los métodos y comprueba que coincidan exactamente con el plan. No acepta fuentes arbitrarias: el paquete debe coincidir con las fuentes canónicas del plan validado. El Java de celebración conserva su motor y versiones separados.

Los consumidores reales son BananaActivity (texto, botones, márgenes, tema y panel), Scene y RigRenderer (saltos/gestos), y BananaConversation (contexto). Un snapshot de Settings volatile publica los módulos probados en conjunto. SharedPreferences guarda el paquete activo completo y un historial acotado; prepare no publica, apply comprueba la base y las fuentes antes de confirmar, recover comprueba la versión objetivo antes de publicar. Los datos del proyecto no se escriben en estas rutas.

El build genera assets/code-map.json y assets/app-source con sólo los Java propios instalados, incluyendo referencias por nombre y hashes. No incluye claves ni permite modificar las clases del APK. PROXIMA-ETAPA.md distingue lo entregado del programador general y la reconstrucción en dispositivo aún pendientes.


## 0.44: función definida fuera del catálogo de constantes

JavaCurve analiza expresiones aritméticas finitas y genera el método jump(float t) de BananaFunction.java. Los parámetros libres permiten funciones completas por tramos y composiciones matemáticas; no habilitan sentencias Java arbitrarias. JavaFunction gestiona candidato, compilación real, activación y ocho estados anteriores. Scene.pose consume la función antes del multiplicador global de salto y se comparte entre la vista previa y la exportación.

JavaTrial envía el paquete compilado y su expresión a JavaTrialService en :banana_java_trial. El servicio compara257 resultados DEX con el AST de referencia; el cliente exige PID distinto, respuesta positiva y límite de20segundos. Es un proceso separado privado con los mismos permisos, no un sandbox de seguridad. Las fuentes se regeneran desde el AST permitido; no se acepta código de propósito general.

Persistencia de funciones: banana-java-function; independiente de banana-app-java y banana-live-java. El deshacer genérico compara las fechas de los tres subsistemas. Una prueba pendiente no modifica escenas ni la función activa. La recuperación del estado guardado comprueba fuentes y resultados. No se implementó un programador general ni construcción del APK en el dispositivo.


##0.45: edición de un método de la aplicación

JavaTextPlan representa un subconjunto explícito de condiciones y respuestas Java. Edita únicamente el cuerpo socialReply de assets/java-edit-base/ChatLanguage.java, una copia del archivo real de0.44. El resto del archivo se conserva. La clase de ejecución se genera con un nombre diferente y el normalizador se vuelve autocontenido usando la misma operación JDK; esto evita dependencias de acceso entre paquetes de distintos classloaders.

JavaMethodEditor escribe el archivo ChatLanguage.java en una copia de trabajo versionada privada, guarda el plan y el antes/después, compila, prueba por IPC y activa una referencia a Method. ChatLanguage.socialReply tiene un puente con respaldo en socialReplyBase. DialogueState, BananaChat y ResearchConversation siguen consumiendo ese punto de entrada; por eso las respuestas nuevas aparecen en el chat real. No se parchea una clase ART ya cargada ni se reconstruye el APK.

JavaTrial/JavaTrialService admiten ahora el contrato numérico anterior y un contrato textual con muestras derivadas de las reglas y controles de creación. El proceso separado no es un sandbox de seguridad. El plan canónico no acepta Java arbitrario.

Persistencia: banana-java-method. Las versiones guardan un hash de la base y fuentes planas; no anidan el JSON de las versiones anteriores dentro de cada candidato. Se conservan ocho estados previos recuperables por número, con validación de fuentes y resultados antes de publicar. El deshacer genérico compara este subsistema con los tres anteriores.


##0.46: paquetes modulares y recuperador entrenable

ModularHub almacena un documento canónico con profile, replies, behaviors y panels. Valida completo y prepara sin ejecutar; activa mediante un único commit y una referencia Bundle volatile. Conserva cinco paquetes anteriores y restaura todas las partes juntas. Los consumidores reales son BananaActivity, BananaConversation, RigRenderer, AppJavaWorkspace.jumpLift y ChatLanguage.socialReply; los comportamientos usan RuntimeProgram con sus invariantes de escena. Los paneles se construyen desde datos y ejecutan comandos admitidos al pulsar un botón. Los valores explícitos del perfil modular prevalecen sobre el perfil Java; los campos restantes usan los ajustes previos.

AdaptiveMemory es un recuperador propio con128 características, términos/trigramas hash y clasificadores logísticos por respuesta. Entrena24 épocas a partir de correcciones del usuario y negativos fijos, aplica un filtro léxico conservador y mantiene coincidencias exactas. Evalúa por separado la última variante; rechaza regresiones observadas con el mismo tamaño de evaluación y exige retener los ejemplos exactos. Los datos pendientes y el modelo activo están diferenciados. Cada versión activa guarda dataset y pesos sin anidar versiones anteriores; hasta tres estados previos. Las recuperaciones restablecen ambos.

El aprendizaje es local y explícito: no se etiqueta su propia salida como verdad ni se modifican los modelos generativos .bin. Las respuestas muestran procedencia del usuario. Se limita a32 respuestas y cuatro variantes; no es una mejora universal del razonamiento. La búsqueda web existente no alimenta automáticamente el entrenamiento.

Los paquetes y el recuperador persisten y cambian dentro de la instalación. La primera actualización a0.46 incorpora estos nuevos puntos de extensión; cambios del núcleo, permisos y bibliotecas siguen requiriendo un APK.


##0.47: ciclo de reparación Android

RepairSyntax limita los tokens a condiciones de texto y retornos, conserva los literales y propone cinco clases de corrección. AndroidRepair compila el cuerpo candidato mediante OnDeviceJava, observa los errores, contrasta resultados por JavaTrial.text con un plan de referencia y prueba arreglos aplicables hasta seis intentos. Un cuerpo que compila no se considera aprobado sólo por compilar. La prueba del método utiliza cadenas nuevas para detectar identidad String==.

El diagnóstico no altera borradores del editor. Una reparación aprobada genera un candidato canónico por JavaMethodEditor, ligado a su hash. Aplicar vuelve a comprobar la revisión y el hash de fuente. Un intento nuevo invalida la acción de aplicar el candidato anterior del reparador, sin destruir el borrador anterior del editor si no hubo una nueva solución.

JavaMethodEditor.apply notifica a AndroidRepair.applied una vez activado. Sólo un candidato con hash coincidente pasa al registro de casos compilados/probados/aplicados; los errores del registro no revierten ni anuncian como fallida una activación ya realizada. Los hashes y reglas se conservan en hasta dieciséis casos y se usan para ordenar arreglos conocidos. El punto y coma conserva prioridad estructural y el retorno final se considera al final. No se entrenó un modelo de código general.

La búsqueda recorre assets/code-map.json y las fuentes reales instaladas. Presenta evidencia textual acotada y una clasificación por palabras/nombres; no confirma automáticamente causas. La suite interna de cuatro fallos es una regresión reproducible y no una evaluación general de programación. Todo este ciclo se ejecuta en Android y mantiene los límites del puente del método y del proceso de prueba compartido.


## 0.48: entorno de fuentes completo y selector neuronal

JavaProject construye una vista de todas las fuentes instaladas de assets/app-source y un conjunto versionado de overrides. Admite edición de un método no ambiguo y archivos completos, varios cambios antes de compilar y checkpoints ligados al hash de la copia/base. Usa ECJ1.8 en Android contra un jar de stubs de API35 con los tipos originales de compilación Java8 que faltan en el SDK público. No convierte a DEX ni carga los resultados generales. Sólo el compilador confiable se carga por DexClassLoader. No hay ejecución de código general del workspace ni construcción de recursos/APK.

JavaSourceEdit ubica métodos con un escáner que respeta cadenas y comentarios; las sobrecargas ambiguas se rechazan. La generación neuronal se limita a un return binario simple. JavaEditModel es una MLP96→24→6,2454 parámetros, entrenada desde cero con instrucciones originales y contextos balanceados. Ignora vocabulario ajeno al entrenamiento y usa un margen de selección, sin garantía semántica. La salida es un operador y un emisor conserva operandos/archivo; no es un generador autoregresivo de código.

El corpus original está en assets/java-specialist:216 ejemplos de entrenamiento con before/after,12 de desarrollo y12 de evaluación. La red sólo consume características de instrucción y etiqueta de operador. Los conjuntos no comparten frases exactas de instrucciones; siguen siendo las mismas seis tareas. Los pesos se entrenan en Android y persisten con métricas. La validación de desarrollo guió ajustes, mientras la evaluación separada se observó después de fijar esa versión del modelo.

El jar de API es de compilación, sin recursos ni implementación ejecutable. Incluye los NOTICES originales y el stub propio LambdaMetafactory (sólo para tipos de compilación de lambdas), cuyo fuente está en tools/java8-stubs. El contenido ZIP tiene timestamps deterministas y su hash identifica el caché privado. No se incluye ese stub como clase DEX del runtime de la app.


## 0.49: conexiones desde la copia Java

JavaProject conserva la edición/compilación completa. ProjectReplacementSpec extrae sólo cuerpos de cuatro métodos y emite Java canónico: AnimationProfile.jump/sway, ChatLanguage.socialReply y ActionProfile.celebration. Ninguna clase general del workspace se carga para ejecutar.

ProjectRuntime administra tres destinos independientes, una revisión, un candidato y ocho mapas anteriores planos. Preparar comprueba el proyecto completo y compila sólo el subconjunto acotado con ECJ+DX. JavaTrialService valida en otro proceso. Activar exige la misma huella del workspace, evidencia de compilación vigente y revisión activa. Los DEX son privados, de sólo lectura y tienen hash. La fuente canónica se compara al cargar. El proceso de prueba comparte UID: no es un sandbox para Java general.

| Función de la copia | Conexión activa | Comprobación |
| --- | --- | --- |
| AnimationProfile.jump | Scene.pose, saltos y rebotes |1025 muestras, rango -40..0 y extremos0 |
| AnimationProfile.sway | Scene.pose, rotación del baile |1025 muestras, rango -30..30 y extremos0 |
| ChatLanguage.socialReply | Entrada social del chat | Casos de reglas y controles existentes |
| ActionProfile.celebration | JavaLive, secuencia con planificador normal | Literal validado1..8 movimientos |

La sustitución del proyecto tiene prioridad sobre los puentes antiguos en esos puntos. Deshacer o desactivar vuelve al estado anterior. Se reutiliza Scene.pose en vista previa/exportación; los modelos y las escenas guardadas no se reentrenan ni se reescriben al activar un módulo. Actualizar la base Java conserva overrides y respaldo, e invalida la evidencia previa de compilación.


## 0.50: código portable y APK con módulos incorporados

PlannerContract valida una política completa de recetas (hasta8 plantillas y8 pasos por receta), emite una clase Java pura y prueba coincidencias, salida y abstención. ProjectRuntime incorpora el cuarto destino planificador. PlannerDispatch usa su selección compilada para construir una escena mediante el ejecutor estable, conserva identidades y valida consistencia. Es un módulo de recetas: no permite reemplazar cualquier algoritmo de ScenePlanner.

PlannerExperience implementa un selector lineal propio de64 características por receta, entrenado desde pesos cero con evaluaciones explícitas. Nunca autoetiqueta un resultado generado como correcto. Publica sólo si retiene sus ejemplos compatibles y permite recuperar pesos y datos anteriores. Las métricas son de retención, no generalización independiente. La identidad del modelo depende de la política, para no reutilizar pesos sobre reglas distintas.

DurableCode serializa especificaciones aprobadas, fuentes canónicas y aprendizaje sin rutas de archivo ni claves. Los datos privados tienen prioridad sobre el bootstrap incluido en el APK. La restauración y recompilación compatible ocurren en un trabajador; los pedidos esperan su finalización. El proceso privado de pruebas no inicia otro bootstrap. Un registro incompatible o alterado queda pendiente y el núcleo sigue disponible.

DeviceApk conserva núcleo/recursos, modifica únicamente versionCode en el AXML e incorpora los módulos DEX y Java canónico en assets/banana-code. Mantiene entradas STORED alineadas a4 bytes y elimina las firmas anteriores antes de volver a firmar. La versión0.50 empieza en500000, con9999 incrementos locales; los siguientes núcleos deben reservar bloques crecientes de10000. No recompila cambios arbitrarios de classes.dex ni recursos nuevos.

ApkSigning carga apksig (Android Build Tools35, D8, Apache2) con hash verificado. Una cápsula del propietario suministra PKCS8+certificado, se contrasta con el firmante instalado y se protege mediante AndroidKeyStore/AES-GCM. La APK distribuida y las reconstruidas no contienen la clave privada. El propietario guarda la APK por SAF y confirma la instalación estándar de Android. Sin importar la firma correcta se conserva la app y se informa del paso pendiente.

| Persistencia | Contenido | Recuperación |
| --- | --- | --- |
| Datos privados | Módulos activos, Java de trabajo, pesos y ejemplos | Se conservan al actualizar con igual paquete/firma |
| APK reconstruida | DEX aprobados, Java canónico, política y aprendizaje | Bootstrap sólo si no hay activación local previa |
| Respaldo JSON | Especificaciones, fuentes y aprendizaje | Recompilar/probar antes de activar; datos locales prioritarios |
| Firma importada | Autoridad protegida por AndroidKeyStore | No viaja en APK ni respaldo; reimportación por el propietario |


## 0.51: síntesis comprobada y reemplazo de clases del núcleo

MethodProgram define46 programas tipados combinatorios de listas, texto y turnos. La búsqueda usa ejemplos visibles; el programa seleccionado se compila y se ejecuta contra casos visibles/reservados por JavaTrialService. MethodRanker es un perceptrón propio de64 características que aprende el orden de búsqueda a partir de soluciones verificadas, sin usar valores reservados en el entrenamiento. MethodLab conserva candidatos, pruebas, reparación por re-síntesis y experiencias portables v2; acepta respaldos v1 anteriores.

JavaAlgorithm es un archivo generado: su clase completa se incorpora al classes.dex normal. SceneTurns utiliza el algoritmo instalado para construir turnos reales sin alterar los diseños. JvmClass analiza ABI y referencias del constant pool sin ejecutar código. Esas referencias no equivalen a un grafo dinámico completo de llamadas.

CoreBuild compila todo el proyecto, selecciona las unidades modificadas y sus clases anidadas, comprueba el ABI contra app-api.jar y convierte sólo clases sin características que necesiten desugaring. La adaptación de versión JVM se permite tras ese control; las clases con lambdas se rechazan. DX convierte las sustituciones y DexMerger KEEP_FIRST las fusiona con el DEX del APK instalado. Las clases originales no se ejecutan durante la construcción. app_api.py crea la base JVM sólo para análisis.

DeviceApk sustituye classes.dex, las fuentes cambiadas, el índice y la base JVM dentro del paquete. Conserva los recursos existentes y el mismo paquete/firmante. El resultado se llama Banana.apk. CoreBuild.sync sincroniza una copia que coincide exactamente con la reconstruida y conserva respaldo/checkpoints; no pisa ediciones posteriores. La firma privada continúa fuera del paquete.

Límites: ABI incompatible, constantes en línea cambiadas, clases que requieren desugaring, eliminación de clases, multidex y recursos nuevos necesitan otro backend. Compilación/ABI no prueban comportamiento de Java general. La ejecución automática se limita a fuentes canónicas de la gramática; el proceso privado comparte UID y no se presenta como sandbox.


## 0.52: intención normal del chat → programación comprobada

NaturalCoding se ejecuta al principio de MobileCore después de DurableCode.awaitReady. Analiza el pedido completo sin mutación y conserva la ruta anterior para escenas y comandos avanzados. Reconoce variantes acotadas con prefijos de cortesía, ajustes de interfaz, métodos de la gramática, respuestas entre comillas, reconstrucción y continuaciones con contexto. No es un LLM general ni un parser de Java libre.

La ruta settings crea un plan completo AppJavaChat, compila los módulos mediante AppJavaWorkspace, compara sus getters y activa el candidato. El nuevo campo accent es opcional al leer datos previos; su valor cero genera exactamente las fuentes antiguas sin getter adicional. Para colores nuevos se genera BananaInterface.buttonAccent y se comprueba el getter compilado. BananaActivity aplica el tinte y guarda los colores originales por botón en un WeakHashMap para restaurarlos. ModularHub admite accent y su mezcla mantiene la precedencia de perfiles anteriores.

La ruta reply construye JavaTextPlan, compila la copia de socialReply, ejecuta sus casos en JavaTrialService y activa la respuesta. La ruta method usa MethodLab sin exigir un comando; conserva el método anterior de la copia para deshacer con comprobación de hash/base. Los atajos de prueba ejecutan el candidato; los de ejecución consultan el núcleo instalado. La incorporación conserva el modo code_export_apk del flujo normal de guardado Android.

Antes de generar o reconstruir, una base de proyecto anterior se migra con JavaProject.handle conservando sus ediciones, metadatos y copia de base anterior; la compilación completa posterior vuelve a comprobarlas. El recibo y el último objetivo se guardan en banana-natural-coding. Los ajustes relativos posteriores sólo se resuelven para texto/botones; falta de contexto pide concretar. La reversión de módulos/respuestas comprueba la revisión; la de fuentes compara hash y base. No hay reversión automática de una APK ya instalada. La búsqueda diagnóstica reutiliza AndroidRepair.search y nunca confunde coincidencias de texto con una causa comprobada.

ChatEdits extiende DurableCode a portable v3 (lectura de v1/v2 conservada): incluye configuración canónica y planes de respuesta activos, tras comprobar fuentes y código cargado. Valida los valores/esquemas antes de importarlos. Al primer arranque sin datos recompila y prueba esos planes; las preferencias existentes tienen prioridad. Si una restauración falla conserva lo ya aplicado y permite reintentar al arrancar; la restauración conjunta no es una transacción atómica. DeviceApk verifica que el manifiesto portable y el workspace sigan sin cambios durante la reconstrucción.

Los planes de UI/respuesta viajan en assets/banana-code/manifest.json y se conectan por sus puentes; no se anuncian como nuevas clases del DEX principal. Los métodos compatibles de JavaAlgorithm mantienen el reemplazo del classes.dex y su base de fuentes. Base de versión520000, próximo núcleo530000 o superior. Sin nuevos modelos externos, pesos propios de Chat/Pixel sin cambios.


## 0.53: programación coordinada y autoedición acotada comprobada

CodeAtlas construye el índice de referencias de las fuentes actuales de JavaProject y recorre dependencias inversas hasta dos saltos, con detección de ciclos. Las responsabilidades generales son conocidas y la cuenta de métodos es una estimación por sintaxis; no es un LSP ni un grafo completo de ejecución.

ProgramPlan es una IR tipada de1 a6 operaciones del mismo dominio, con generación canónica de JavaAlgorithm y ProgramKernel. ProgramSupport aporta límites/parseo. Las seis firmas step0..step5 se conservan para no romper ABI al cambiar la longitud del programa. JavaProject.stageBatch guarda el lote con un único commit, conservando archivos ajenos. ProgramLab compila tres clases de candidato y solicita64 casos de ProgramJudge en JavaTrialService. La semilla se crea después de compilar; el oráculo usa colecciones y una interpretación separada. La IR limita bucles, tamaños y rangos; no ejecuta Java libre. Guarda hash de fuente, JAR inmutable, semilla y copia previa. La prueba posterior requiere que ambos archivos sigan coincidiendo con el candidato canónico.

LearningJournal separa reportes del usuario, evidencias de programas, mejoras de comprensión y rechazos. Máximo48 registros con origen y número de pruebas. Los reportes no se convierten automáticamente en etiquetas correctas ni en pesos generales.

UnderstandingSpec genera código para UnderstandingRules: tres transformaciones locales combinables (máscara0..7) y hasta8 equivalencias exactas aportadas por el usuario. Conserva negaciones, preguntas de control y diálogos (también con el verbo dice sin comillas). SelfLearning reutiliza un conjunto de12 fixtures visibles para buscar sin repetir su construcción por candidato. Compila una clase baseline y otra candidata, y usa CapabilityBench en JavaTrialService para comparar efectos reales de ScenePlanner sobre copias. PlannerDispatch consulta un ThreadLocal sólo durante la evaluación para aislar extensiones del planificador. El evaluador comprueba consistencia, acciones/slots/destinatarios, identidad de diseños existentes y controles negativos; no usa sólo coincidencia de cadenas reescritas. El juego base tiene48 casos con nombres/tipos variados y añade los casos aportados por el usuario. Su procedencia es de desarrollo local, no externa.

Sólo publica con ganancia estricta y cero regresiones por caso. La recuperación de un respaldo admite igualdad sin regresiones. Comprueba base, fuentes y versión antes de publicar. UnderstandingRuntime conserva un método compilado en datos privados, validado por hash/fuente canónica; sin él usa UnderstandingRules del DEX instalado. La copia de UnderstandingRules se prepara después de pasar pruebas y se incorpora al DEX mediante CoreBuild. Guardar fuentes y publicar el puntero activo son commits separados: un fallo intermedio puede dejar una copia preparada sin activación; no se anuncia como éxito. La reversión protege base/hash y no revierte una APK ya instalada.

MobileCore encamina registros, autoedición, mapa y programas antes de los comandos previos; después aplica UnderstandingRuntime.rewrite al pedido. La mejora modifica interpretación de escenas, no los modelos neuronales de conversación o dibujo. No produce inteligencia general ni habilidades fuera de la gramática/transformaciones disponibles.

DurableCode portablev4 añade evolution (spec, casos de significado y journal), conservando lectura de v1..v3. Valida escenas y contratos antes de recuperar. Si un respaldo trae reglas distintas a las instaladas y no hay un estado local activo, recompila y compara antes de activarlas. Los datos locales existentes conservan prioridad. Las fuentes del programa y UnderstandingRules viajan en assets y clases DEX reales al reconstruir; las autoridades de firma siguen excluidas. Base530000, siguiente núcleo540000 o superior. Nuevos recursos, permisos, bibliotecas, multidex y cambios de ABI no admitidos siguen fuera de este backend.


## 0.54: entrenamiento de capas de salida y DEX completo desde fuentes

BananaTransformer.Session expone el vector normalizado antes de la proyección de vocabulario. HeadTrainer calcula gradientes de entropía cruzada y SGD sobre head/headBias (cuerpo congelado), con replay de distribuciones previas y proyección de la actualización a una bola L2 de radio0.01. NeuralTraining precomputa características con teacher forcing, separa preguntas de train/eval y rechaza truncamiento. Son ocho épocas y una sola evaluación final de publicación: baja de NLL y KL<=0.003 en controles. Los datos incluidos son mínimos, propios y de plantillas; el descenso no es un benchmark de lenguaje abierto.

PixelGenerator expone características del MLP y serialización del modelo BPX1. HeadTrainer actualiza sólo su cuarta capa mediante derivadas de sigmoid/MSE RGBA ponderada por alfa. Hay128 puntos de entrenamiento,128 separados de evaluación de la misma imagen y64 controles. La referencia proviene del usuario o del dibujante procedural propio de train_pixel21.py. No se entrenan fondos/caras/poses/objetos/otros modelos. La prueba es espacial dentro de una imagen, no de generalización a imágenes nuevas.

ModelDepot guarda checkpoints completos en datos privados de sólo lectura, verifica hashes, mantiene punteros y copias previas reales, y permite rollback. Se recargan los pesos de TransformerBrain por identidad; NeuralSprites.resetModels invalida pesos/imágenes. Un monitor separado de render protege preparación, variantes y reset para que publicar no vacíe un modelo en uso; las consultas rápidas de caché conservan su monitor corto. La vista de Pixel usa PixelRetouch sobre el banco propio para interpolar una malla de deltas de logits16×16 de la capa entrenada, calculada respecto al modelo original del banco. Es aproximación de vista rápida, no render neuronal exacto de128×128 por actualización. El modelo completo sigue disponible para inferencia exacta. Se valida el tamaño y finitud de esa malla antes de publicar.

DeviceApk reemplaza los dos assets de pesos cuando hay checkpoints activos y añade training/baked-models.json con hashes, informes y malla. Se conserva el modelo original del banco como training/pixel-bank-base.bin. Un banco adicional de la banana de color predeterminado procede de la inferencia de esos pesos originales, no de un modelo externo. En una instalación limpia los metadatos mantienen la vista entrenada y el Transformer lee los assets actualizados. Los modelos propios originales de la distribución se conservan hasta que el usuario solicita entrenar.

FullBuild sustituye el ensamblado por parches de CoreBuild en el flujo normal. Escribe todo el workspace, usa source-lowerer.jar (código propio basado en las vinculaciones del ECJ4.6.1 ya incluido) para convertir lambdas en clases anónimas, compila el resultado con ECJ y genera todo classes.dex con DX. Las clases JVM resultantes se validan y adaptan a versión50, y se reconstruyen el índice y app-api.jar. No se mezclan clases del DEX previo. Los cambios internos de ABI pueden resolverse con todas las fuentes presentes. JavaCompat implementa el join usado por la app para no depender de la implementación API26 de String.join.

SourceLowerer resuelve tipos, firma SAM, parámetros inferidos/genéricos y llamadas/capturas de this léxico. Reescribe lambdas anidadas de adentro hacia afuera. Se prueba explícitamente el caso de Runnable.run que llama al método run del objeto externo para evitar recursión accidental. No es un desugaring general de cualquier Java8+ ni reemplaza D8 para cualquier proyecto. Este backend usa recursos/manifiesto empaquetados, conserva la identidad de actualización, y no añade recursos/permisos/bibliotecas libremente. El generador del chat sigue acotado aunque el constructor pueda recompilar todo el Java actual.

No hay una transacción global entre todos los archivos de fuentes, checkpoints y publicación. Se protegen identidades/guardas de modelos y fuentes antes de firmar. Un fallo conserva la app instalada y puede dejar material de trabajo para diagnosticar. Base de versión540000, siguiente núcleo550000 o superior. Sin modelos externos ni Qwen.


## 0.55: objetivos naturales y pedidos coordinados

ImprovementFlow se ejecuta antes de las rutas precisas. Interpreta objetivos acotados de entrenamiento de Chat, Pixel, ambos, recuperación de pesos, estado, alcance y reconstrucción, combinables con ajustes de interfaz. Valida todas las cláusulas antes de ejecutar; rechaza cláusulas desconocidas, entrenamientos repetidos y operaciones posteriores a la reconstrucción. Protege preguntas, negaciones y respuestas entre comillas del enrutamiento de objetivos. No analiza Java arbitrario ni inventa correcciones.

La ejecución usa los entrenadores, controles y constructor reales existentes. Separa Chat/Pixel en etapas para conservar un recibo si falla una posterior. El resultado de reconstrucción conserva code_export_apk, que activa la entrega normal de Banana.apk. El fallo devuelto por el constructor detiene el pedido. La firma sigue siendo interna de la misma app; la distribución no incluye claves.

ProgramPlan amplía el límite de6 a12 operaciones y el texto de especificación de350 a900 caracteres; conserva rangos, tipado y comprobación de cada operación. ProgramKernel genera12 métodos paso; esto puede cambiar la superficie interna al compilar todo el proyecto. Chat admite24 ejemplos con límites de tokens sin truncar; no se amplían capas entrenables ni promesas de calidad.

La capacidad faltante principal continúa siendo la síntesis general de cambios coordinados de Java con pruebas específicas, análisis de dependencias y tratamiento de recursos y bibliotecas. El compilador de todo el Java actual ya existe. La actualización se instala con confirmación de Android. No hay aprendizaje continuo autónomo ni mejora garantizada del razonamiento.


## 0.56: compilador tipado de fórmulas y retropropagación completa de Pixel

Formula tokeniza y analiza expresiones con precedencia, límites de profundidad/nodos y tipos int/bool. No inserta texto Java libre. Emite operaciones aritméticas a FormulaMath con intermedios acotados; las comparaciones, cortocircuito y ramas se generan como Java. El intérprete del árbol evalúa con long y límites de forma separada, sin invocar FormulaMath. ProgramPlan añade map_expr/filter_expr, admite combinarlos con operaciones anteriores y conserva su esquema1 con claves específicas verificadas. Los candidatos incluyen una copia canónica del helper en el paquete de prueba; el núcleo recompilado usa FormulaMath de producción. ProgramLab preserva puntuación al extraer tareas de fórmulas. La ruta de objetivos deja estas solicitudes al generador correspondiente.

ProgramJudge conserva64 casos generados con semilla posterior a la compilación. Para fórmulas usa tanto magnitudes pequeñas como entradas más grandes y casos inválidos. Esto evita que fórmulas de varias multiplicaciones tengan sólo casos de desbordamiento; conserva el requisito de cobertura válida e inválida. No se afirma prueba exhaustiva: el generador y referencia comparten la representación y pueden compartir errores semánticos. Stage59 añade resultados esperados manuales, mutante defectuoso y1.536 casos compilados.

DeepPixelTrainer implementa propagación directa y gradientes de MSE RGBA ponderada por alfa a través de cuatro capas (ReLU, ReLU, ReLU, sigmoid). Calcula el delta anterior antes de actualizar cada matriz. Dos épocas SGD con replay y proyección global L2<=0.03 sobre pesos y sesgos. NeuralTraining comparte preparación de referencias pero, en modo profundo, usa coordenadas sin congelar características. Verifica pérdida, retención y recarga del checkpoint antes de publicarlo. El modo de salida anterior sigue disponible. La vista interpolada usa diferencias de logits calculadas con características separadas de cada modelo, necesarias al modificar capas internas. No cambia redes secundarias ni el cuerpo de Chat.

FullBuild genera ahora el índice con release0.56; corrige la etiqueta heredada0.54 al reconstruir fuentes de versiones posteriores. Se mantiene un DEX nuevo, mismo paquete y certificado, sin nuevas claves distribuidas.

Pruebas actuales y límites: VALIDACION-0.56.md. Las redes mantienen el formato binario y sus pesos propios originales en la distribución; el usuario produce los checkpoints al solicitar entrenamiento. La mejora local no se usa como evidencia de inteligencia general o calidad perceptual universal.


## 0.57: controles identificados por nombre y Transformer completo

NaturalCoding normaliza cambiame/cambiale/pintame/poneme y variantes antes de seleccionar el ajuste. El patrón de botón singular conserva el nombre y lo traduce a AppJavaChat. AppJavaWorkspace.Settings añade buttonColors opcional; se omite si está vacío para conservar fuentes y JSON canónicos anteriores. Valida hasta16 nombres normalizados y valores de paleta0..5. Un getter opcional de BananaInterface devuelve su JSON canónico, que se comprueba al cargar el módulo. ChatEdits y ModularHub aceptan el nuevo campo; la mezcla de perfiles conserva su precedencia anterior. BananaActivity consulta el nombre normalizado de cada Button antes del acento global y restaura su estilo original para valor0. No es una búsqueda semántica libre de controles ni un cambio en XML.

El worker de BananaActivity rechaza resultados nulos/vacíos y captura Throwable para devolver un error visible en más casos. No se afirma recuperación universal de errores de UI, cierres del sistema o memoria agotada.

TransformerTraining contiene un tape de diferenciación inversa con tensores, lineales, LayerNorm, ReLU, residuales y atención causal multihead. Calcula gradientes de Q/K/V y del softmax de atención con máscara causal. La representación por secuencia reproduce la inferencia incremental del mismo modelo. Se entrena con etiquetas en posiciones de respuesta y EOS; la pérdida por pregunta se promedia por tokens etiquetados y después por ejemplos. El replay completo usa etiquetas propias de control, mientras la retención se verifica contra distribuciones previas sobre esos controles. Gradient clipping global a1 y región global de cambio0.01, SGD0.0002 por muestra, dos épocas. Máximo6 filas de entrenamiento; no se selecciona silenciosamente un subconjunto de una colección mayor.

DeepChatTraining valida separación de preguntas, compara NLL media por secuencia y KL de control usando características recalculadas del Transformer entrenado, verifica checkpoint y publica por ModelDepot. La revisión invalida la inferencia almacenada y permite recuperación exacta. El formato BTF1 y la arquitectura propios se conservan. El entrenamiento de salida sigue disponible. Los pesos originales distribuidos no se sustituyen por los fixtures de desarrollo.

Stage61 comprueba paridad de logits causal/incremental y diferencias finitas en30 grupos: embeddings, posiciones, atención/salida/FF de cada bloque, normalizaciones y salida. Evalúa entrenamiento completo, cambio de embeddings y atención, confianza acotada y recarga. Stage62 cubre la frase real, variantes, aislamiento de color por nombre, fuentes canónicas, respaldo y negación. Los cambios Android arbitrarios y comprensión general continúan sin estar resueltos. Pruebas actuales: VALIDACION-0.57.md.


## 0.58: edición general de fuentes con comprobación previa

GeneralEditPlan representa operaciones file/method/literal/rename y prepara un mapa de cambios sin mutar las fuentes actuales. SourceLex separa comentarios, cadenas y caracteres preservando offsets; JavaSourceEdit.method utiliza la máscara para evitar declaraciones falsas en comentarios o cadenas. No es un parser semántico completo de Java. Las transformaciones literales usan decodificación de escapes compatible con JSON y pueden rechazar escapes específicos de Java. La renombración es global y léxica, no resuelve sobrecargas ni reflexión arbitraria. Los reemplazos de método rechazan escapes Unicode y declaraciones ambiguas; los archivos completos no usan esa selección.

JavaProject.compile incorpora un modo de candidato sin compromiso: lee overrides proporcionados y guarda candidateReport sin alterar working ni compiledHash. El camino anterior conserva su comportamiento. GeneralSourceChat compara base, revisión y copia previa antes de stageBatch. Persiste pending con before/after y promueve la transacción después de guardar las fuentes. Si la siguiente edición o undo encuentra pending, contrasta los snapshots y completa o descarta sólo el registro apropiado; un conflicto se conserva. No hay transacción global entre SharedPreferences.

La ruta se ejecuta antes de los ajustes y generación acotada. Reconoce pedidos concretos de reemplazo/renombración y bloques Java para métodos o archivos, sin activación adicional. La UI admite bloques de código en mensajes largos dentro de su límite. La adición de una clase se incorpora al DEX completo en la reconstrucción siguiente. La compilación general no ejecuta el candidato ni afirma corrección funcional. El entrenamiento propio y la interpretación de ajustes de0.57 se conservan.

Stage63 compila un método con bucle fuera de la gramática anterior, comprueba renombraciones en llamadores, literales, comentarios, nuevas clases, ambigüedad y paquete. También recorre fuentes reales de la app para cambiar un literal UI. Las pruebas Android verifican rechazo sin compromiso, deshacer exacto, adición de GeneralProof, cambio posterior de su literal y su ejecución en la APK instalada, con datos previos y vacíos. Detalles: VALIDACION-0.58.md.

Tras un reinicio del entorno se recuperó la entrega persistente0.57 y se reconstruyó esta etapa; hay un checkpoint0.58 marcado en validación. Para pruebas de host se recompiló el ECJ4.6.1 de sus fuentes incluidas, omitiendo adaptadores JSR199/Ant incompatibles con el JDK del host. El compilador y lowerer dentro de la app no cambiaron; se validan en Android. No se afirma síntesis libre de cualquier modificación Android desde lenguaje natural.


## 0.59: tipografía delegada y cuaderno de conocimiento atribuido

UiTypeface.command interpreta pedidos de fuente y delegación de elección. AppJavaWorkspace.Settings añade font0..4 como campo opcional omitido en0, con getter compilado opcional de BananaInterface. Se conserva la fuente canónica previa cuando no se personaliza. ChatEdits/ModularHub admiten el campo. UiTypeface aplica Typeface a TextView/derivados, conserva estilo y una referencia débil a la fuente original; escucha layouts y callbacks de Activity sin retener Activities estáticamente. Los cuadros de diálogo de la app pasan por overloads de UiTypeface.show; no se intercepta la UI de Android ni canvas/video.

KnowledgeNotebook guarda32 documentos, hasta12000 caracteres por documento, con presupuesto JSON160000. Se valida origen user/web, título, ID/hash, fuente, fecha y marca de recorte. Las páginas del mismo URL se actualizan; los aportes distintos conservan IDs derivados de contenido. La recuperación usa términos normalizados y fragmentos, exige coincidencia suficiente y devuelve citas atribuidas; no genera una narración sin respaldo ni ejecuta datos recuperados. Las órdenes se despachan antes de la recuperación y no se toman ejemplos web como etiquetas supervisadas. Las correcciones de AdaptiveMemory conservan prioridad. Recorda que conserva ProjectMemory para no duplicar memorias ni alterar sus ediciones/borrado.

PageReader separa fetch de read(Context,...). El fetch devuelve Document con título, texto y URL final; valida HTTPS público sin credenciales/redirecciones privadas, mantiene timeout/redirecciones, límite1MB y HTML/texto. Extrae main/article si existe y limpia navegación, head/scripts/styles; no es un navegador con JavaScript. El chat reconoce un enlace por pedido. Aprender un tema sin contenido explícito utiliza WebMemory/Wikipedia existente como fuente enciclopédica; no afirma haber aprendido una habilidad. El mensaje vago de auto-mejora es una explicación de vías concretas, no una actualización falsa.

DurableCode v4 acepta knowledge opcional para lectura de versiones anteriores y lo incluye en la escritura actual. Se valida antes de restaurar; el estado privado existente tiene prioridad, incluido un cuaderno vacío explícito. Se limita la serialización UTF8 total a480KB frente al lector de500KB. La memoria se incluye en backups/APK solicitados, no sólo en la sesión. El entrenamiento neuronal anterior permanece separado y sin cambios de parámetros en distribución.

Stage64 prueba enseñanza, URL, temas, rechazo de un esto sin contenido, recuperación atribuida, abstención, duplicados, recorte y separación datos/acciones. Stage65 cubre la frase real de tipografía, familia elegida, fuente compilada, respaldo, negación y rechazo de partes desconocidas. Native valida aprendizaje y consultas, checkpoint portable, fuente de botón y mensaje de diálogo tras actualización y datos limpios. El intento HTTPS real registra éxito o indisponibilidad explícitamente, sin declarar que una fixture probó Internet. Detalles actuales: VALIDACION-0.59.md.


## 0.60 — Preguntas de interfaz con estado transitorio
RequestDialogue se ejecuta antes de las rutas de aprendizaje/editor en MobileCore. Separa nombre y color faltantes, fuente y tema. La respuesta corta se transforma sólo en una orden existente comprobable por NaturalCoding/AppJavaChat. Las preguntas guardan tipo, objetivo, fecha y revisión; plazo10min. Un cambio de revisión, fecha futura o vencimiento impide aplicar la respuesta. Una tarea nueva descarta el pendiente antes de continuar por el enrutador normal. resetConversation limpia el estado. La persistencia del ajuste final sigue siendo la de módulos/ChatEdits de versiones anteriores; la pregunta no se exporta.

NaturalCoding.setting añade estado deseado del botón («que… sea rojo») y tamaño numérico del texto con «puntos». No se añaden aproximaciones difusas ni generación universal de código. Stage66DialogueCheck evalúa resolución de datos faltantes, negación, respuesta compuesta, entradas ajenas y guardas de tiempo/revisión. Native60Instrumentation comprueba conversación real, ausencia de cambios mientras faltan datos, compilación/activación, cancelar, vencer, aprender otro tema y tipografía/color en una Activity real.


## 0.61 — Vocabulario derivado y preguntas sobre lectura
ReadingKnowledge deriva un léxico y definiciones explícitas de KnowledgeNotebook, sin alterar su esquema portable. Recorre frases retenidas, detecta sujetos con reglas de español, conserva su fila de origen y limita el glosario a256 definiciones. El índice de términos con stopwords pondera coincidencias mediante frecuencia documental de fragmentos y normalización de longitud. Exige evidencia en el cuerpo de una frase y cobertura de consulta, no sólo un título. Para preguntas definitorias favorece el sujeto de una definición; para utilidad favorece marcadores explícitos. Cada fuente aporta como máximo su mejor fragmento y se muestran hasta dos con fecha. No es un modelo semántico ni entrenamiento neuronal.

KnowledgeNotebook.retrieve delega en ReadingKnowledge.answer. Si no hay respuesta pero el sujeto de una definición coincide con la pregunta, reply devuelve una abstención específica. Se agrega la intención de vocabulario y el recibo de lectura aclara el índice creado. El lector de diálogo antiguo incorpora también al cuaderno: si falla la memoria antigua después de guardar el cuaderno, informa ese resultado parcial. No usa respuestas generadas como etiquetas ni cambia automáticamente los pesos. La restauración y el borrado derivan su efecto del cuaderno original; no se necesita migración.

Stage67ReadingCheck usa un término ficticio ausente del modelo, definiciones, utilidad, título engañoso, fuentes distintas, restauración y contenido que parece una instrucción. Native61Instrumentation comprueba respuestas y vocabulario por el chat real y restauración del cuaderno. La validación web real puede fallar por red y se informa aparte; un fixture web no se presenta como una descarga real.


## 0.62 — Enlaces explícitos y diagnóstico del lector
PageLinks extrae un enlace dentro de frases generales, Markdown, www y mayúsculas. Retira cierres desequilibrados y puntuación final. No usa coincidencia difusa ni busca un dominio en un buscador. Rechaza lotes antes de leer una parte y conserva instrucciones de Java, bloques de código, reglas y lecciones explícitas sobre direcciones. KnowledgeNotebook.parse delega aquí antes de temas; la ruta sigue ejecutándose al inicio de MobileCore.

KnowledgeNotebook.PageLoader es una frontera de lectura inyectable para pruebas; el camino normal usa PageReader.fetch. Permite comprobar el flujo de reconocimiento/lectura/retención sin fingir que una fuente controlada vino de Internet. El fallo de web se presenta mediante PageReader.failure con categorías DNS/TLS/conexión/HTTP/contenido y sin recurrir a búsqueda. PageReader.document separa la extracción pura de HTML/texto de la descarga; el texto plano no se trata como HTML y el MIME no depende de mayúsculas. No se modifica la validación de HTTPS/destinos ni se permite omitir certificados.

Stage68DirectLinkCheck evalúa redacción, Markdown, www, paréntesis válidos, negación, código/lecciones, HTML/texto plano y categorías de error. Native62Instrumentation comprueba lector directo inyectado, retención y preguntas posteriores; usa además la ruta real del chat para rechazar localhost sin buscarlo y prueba errores403/lotes. La descarga real se intenta por separado y su fallo nunca se convierte en éxito del fixture.


## 0.63 — Feedback prioritario, estilo y capacidad
ConversationFeedback se ejecuta antes de RequestDialogue y de cualquier herramienta en MobileCore. Clasifica quejas explícitas, correcciones factuales y preferencias de brevedad/detalle; conserva excepciones de corrección visual por rol, plan/código y cortesía. BananaEngine.run registra el último pedido/respuesta para este seguimiento, incluso cuando la ruta salió temprano; excluye feedback y saludos. Se guarda contexto abreviado en preferencias privadas y los hechos corregidos en KnowledgeNotebook con título/origen específicos. ReadingKnowledge ordena primero correcciones explícitas y, entre ellas, por fecha; no borra las fuentes anteriores. El marcador repair anterior se conserva para compatibilidad.

ReadingKnowledge.style conserva citas/fuentes; breve acorta el primer fragmento, detallado añade como máximo dos fragmentos pertinentes. KnowledgeNotebook.reply lo aplica usando la preferencia MobileCore existente. El campo opcional experience.style se incluye y valida en el portable4, aceptando respaldos anteriores sin ese campo y conservando la preferencia privada cuando existe. No hay entrenamiento automático ni sintetizador semántico nuevo.

Se amplía el cuaderno a128 fuentes,262144 caracteres por fuente y4000000 caracteres serializados totales. La descarga permite8MiB y el portable32MiB; bootstrap e importación de Activity leen el mismo límite. El historial abrevia grandes lecciones a8000 caracteres sin recortar el contenido admitido. La entrada normal sube a8000, con límites propios de herramientas Java conservados. ReadingKnowledge.related prefiltra por términos del cuerpo antes de escanear fragmentos. Sigue siendo almacenamiento JSON, no un índice de documentos ilimitado; puede haber latencia en bibliotecas grandes.

Stage69FeedbackCheck prueba corrección versus acción/cortesía, estilo, recuperación de dato posterior al límite anterior, prioridad de corrección y presupuesto de respaldo. Native63Instrumentation comprueba escena/editor sin cambios, corrección que influye en preguntas posteriores, brevedad real, dato tardío en fuentes grandes y exportación/importación mayor a500KB con estilo y fuentes restaurados. Se reinstala luego producción y se comprueba el chat sin navegación al editor.


## 0.64 — Documentos UTF-8 e índice SQLite/FTS4
NotebookDisk separa archivos inmutables UTF-8 de Doc/metadatos y fragmentos. El Index de producción es NotebookSQLite: tablas docs/chunks/meta y tabla virtual FTS4 lex; los textos completos nunca forman una fila de CursorWindow. Se escriben fragmentos de unos2048 caracteres, evitando cortar pares sustitutos. La consulta normaliza términos, busca coincidencias AND y una cobertura OR acotada, diversifica hasta12 documentos, trae hasta8 coincidencias por documento con sus vecinos y pasa sólo esa selección a ReadingKnowledge. Se conservan sus umbrales/citas y prioridad de correcciones; no es recuperación semántica universal ni contexto neuronal ilimitado.

La ingestión usa Reader, búferes pequeños, codificación UTF-8 estricta, conteos y SHA-256. Los datos se escriben primero en un archivo .part, se sincronizan y renombran; luego se modifica el índice transaccionalmente. Los archivos reemplazados se borran después de confirmar el índice. Una interrupción deja como máximo archivos sin referencia, limpiables en una reapertura correcta. Una confirmación incierta conserva evidencia antes de borrar archivos. Los límites son16millones de unidades UTF-16/64MiB por documento,1024 documentos y512MiB de texto total, además del espacio reservado para el índice. No se carga toda la biblioteca para responder.

La migración valida el JSON antiguo, guarda sus fuentes con los IDs originales y sólo retira preferencias después de confirmar toda la migración. Si se interrumpe, reintenta documentos faltantes conservando los ya confirmados. Los marcadores initialized/legacy_migrated impiden resucitar un borrado. El JSON antiguo queda como formato de compatibilidad explícitamente limitado; data/portable(c,true) rechazan bibliotecas que no caben, en lugar de recortarlas u omitirlas. Las rutas operativas dejaron de llamar a ese exportador.

NotebookArchive escribe un ZIP por streaming con manifiesto limitado y archivos UTF-8 verificados. Al restaurar se validan nombres, IDs, límites, duplicados, tamaños, número de caracteres y SHA; todos los nuevos archivos se preparan antes de activar el índice en una transacción. Los IDs privados existentes tienen prioridad. La restauración automática sólo ocurre en una biblioteca no inicializada; la manual permite recuperar tras un borrado. Este archivo respalda conocimiento, no todos los subsistemas de Banana. No ofrece una transacción global junto con módulos/modelos.

DeviceApk usa portable(c,false), añade assets/banana-notebook/library.zip por streaming y comprueba su generación antes y después de empaquetar. Elimina un archivo de conocimiento incorporado anterior al reemplazarlo. Bootstrap restaura el ZIP antes del manifiesto portable cuando corresponde. Se mantiene el exportador JSON de código con límite anterior y error explícito para bibliotecas grandes.

La Activity añade modos knowledge_import_text/export/import y ACTION_OPEN/CREATE_DOCUMENT para TXT/Markdown UTF-8 y ZIP. Los permisos son los del archivo seleccionado, sin acceso general al almacenamiento. El contenido pasa a archivos privados; exportación valida la ruta privada y copia por bloques. La lectura web mantiene su límite8MiB y la extracción de una página existente, que todavía utiliza un buffer de página. El chat pegado mantiene su cap anterior; los documentos extensos se ingresan por archivo.

Stage70DiskKnowledgeCheck usa archivos reales y un índice doble sólo en host: fuente de1.2millones generada por Reader, recuperación del dato final, selección acotada, migración, respaldo completo idéntico, huella errónea sin importación parcial, fallo de Reader, precedencia privada, borrado y Unicode. Las pruebas nativas usan SQLite/FTS4 real y actualización desde0.63, una fuente de1.3millones, reconstrucción íntegra de la APK y reapertura con datos conservados/vacíos. El resultado definitivo está en VALIDACION-0.64.md.
