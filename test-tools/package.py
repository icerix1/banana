from pathlib import Path
import json,zipfile,hashlib,shutil,io,struct
w=Path(__file__).resolve().parent;r=w.parent/'release067/Banana';d=w.parent/'delivery066';d.mkdir(exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
log=(w/'android-result.log').read_text();pipeline=(w/'pipeline.log').read_text()
for marker in ['PASS NATIVE66 ','PASS NATIVE66_REOPEN upgrade','PASS NATIVE66_REOPEN fresh','NATIVE_CONTINUOUS_WEIGHTS_EXERCISES_OK','NATIVE_SERVICE_NOTIFICATION_PAUSE_AND_RESUME_OK','PRODUCTION_KNOWLEDGE_UI_OK','PRODUCTION_DOCUMENT_PICKER_OPENED','NO_FATAL_EXCEPTION']:assert marker in log,marker
assert 'FAIL ' not in (w/'android-instrument.log').read_text() and 'Traceback' not in log
assert pipeline.count('PASS')==52
assert '6bd2a846ed14f8385f4a4a91ede2415d43de207de5187d47951a0b03aceb65d5' in (w/'distribution-signature.log').read_text()
apk=r/'out/BananaLab-Lite.apk'
with zipfile.ZipFile(apk) as a,zipfile.ZipFile(w.parent/'delivery065/Banana-0.65-Android.apk') as old:
 idx=json.loads(a.read('assets/code-map.json'));assert idx['release']=='0.66'
 for f in idx['files']:
  data=(r/f['path']).read_bytes();assert sha(data)==f['sha256'] and a.read('assets/app-source/'+f['name'])==data
  assert (w/'instrumented'/f['path']).read_bytes()==data
 assert not any(n.startswith('keys/') or 'Instrumentation' in n for n in a.namelist())
 assert a.read('classes.dex')[:8]==b'dex\n035\0'
 bins=[n for n in old.namelist() if n.startswith('assets/') and n.endswith('.bin')]
 for n in bins:assert a.read(n)==old.read(n),n
 with zipfile.ZipFile(w/'Banana.apk') as built:
  for f in idx['files']:assert built.read('assets/app-source/'+f['name'])==(r/f['path']).read_bytes(),f['name']
  original=a.read('assets/chat34/chat34.bin');updated=built.read('assets/chat34/chat34.bin');header=struct.unpack('>7i',original[:28]);body=len(original)-4*(header[1]*header[2]+header[1]);assert len(updated)==len(original) and updated[:body]!=original[:body]
  baked=json.loads(built.read('assets/training/baked-models.json'))['models']['chat'];assert sha(built.read('assets/chat34/chat34.bin'))==baked['sha256']
  assert baked['report']['readingWeights'] and baked['report']['accepted']
  code=json.loads(built.read('assets/banana-code/manifest.json'));assert 'knowledge' not in code and len(code['readingTraining']['done'])==3
  assert not code['continuousLearning']['enabled'] and len(code['continuousLearning']['history'])==1
  with zipfile.ZipFile(io.BytesIO(built.read('assets/banana-notebook/library.zip'))) as archive:
   manifest=json.loads(archive.read('manifest.json'));assert len(manifest['documents'])==3
   h=code['continuousLearning']['history'][0];row=next(d for d in manifest['documents'] if d['id']==h['sourceId']);assert row['sha256']==h['sourceSha256'];source=archive.read('documents/'+row['id']+'.txt').decode('utf-8');assert h['answer'] in source and h['evidence'] in source and h['labelOrigin']=='literal_source_not_model'
   for row in manifest['documents']:
    data=archive.read('documents/'+row['id']+'.txt');assert sha(data)==row['sha256'] and len(data)==row['bytes']
report='# Validación Banana0.66\n\n52 suites host aprobadas. Stage73 cubre activación/pausa de ambas rutas automáticas, prioridad de datos privados, condiciones de batería/ahorro/trabajo del chat, repasos espaciados, reintentos limitados y rutas de lenguaje natural. Stage74 genera ejercicios de definición/completar/inverso con evidencia literal e identidad de revisión; el modelo produce un intento independiente limitado12 tokens que nunca se convierte en etiqueta. Entrena realmente el Transformer completo con microtanda de una época, pasa controles locales y conserva12 registros recientes. El informe continuous66-report.json incluye pérdida, retención e intento real; ese intento fue incorrecto aunque la pérdida local bajó. No se afirma mejora de respuestas libres.\n\nSe conservan las50 suites de0.65, incluida lectura HTML9MiB, frontera de copia64MiB, Unicode, entrenamiento completo, checkpoints y journal de cursor, biblioteca en archivos/índice doble sólo para host y contratos anteriores. Los modelos base de distribución no fueron reemplazados; el cambio es el aprendizaje real posterior en el dispositivo.\n\nAndroid6/API23 emulado: actualización desde0.65 con migración de preferencias, fuente1.3millones de caracteres y FTS real, archivos de conocimiento íntegros, fallo de Reader sin reemplazo y borrado/restauración controlados. Se ejecuta LearningService real, comprueba que un chat ocupado aplaza el entrenamiento, observa notificación y ejecuta su PendingIntent de pausa, verifica estado persistido y salida del servicio. Luego reanuda y deja trabajar sin actividad visible: se publica un checkpoint micro real con ejercicio basado en fuente, pérdida menor, SHA verificable e historial conservado. Las otras fuentes del ensayo se marcan ya recorridas para aislar esa prueba; no se afirma haberlas entrenado completas en segundo plano.\n\nSe reconstruye todo Java en Android y se instala la APK normal660001. Tanto como actualización como con datos vacíos recupera fuentes, checkpoint entrenado/reportes, cursores, pausa continua e historial del ejercicio. La distribución660000 se instala aparte y verifica enseñanza/consulta de conocimiento y apertura del selector de documentos, sin excepción fatal observada. El panel nuevo se compiló; la automatización funcional específica del panel y Android16 físico quedan pendientes. La notificación y su acción sí se ejecutaron en API23.\n\nIntegridad: fuentes de producción iguales a índice/APK/copia nativa y fuentes del reconstruido; DEX035, misma firma original, sin instrumentación ni claves privadas en distribución. El reconstruido del ensayo lleva instrumentación y no se entrega como distribución. Pesos activos distintos de los originales, metadatos/SHA de modelo coherentes, manifiesto de código separado y ZIP completo de tres fuentes verificados. Archivo privado de fuentes contiene la firma original del propietario: no publicar ese ZIP.\n\nLímites: la API35 se usa para compilar los permisos/tipo specialUse/callback de timeout y la implementación respeta inicio desde actividad visible; no se probó en Android14/15/16 ni se garantiza comportamiento de fabricantes, cuotas, Doze o publicación en Play. El fallback JobScheduler se compila y programa, pero no se forzó una ejecución completa independiente ni un reinicio físico del dispositivo. Los controles térmicos y de batería se prueban por lógica, sin calentar una tablet. No hay garantía24/7, aprendizaje universal ni entrenamiento de todo un documento en una tanda. Tres recorridos por día UTC con repasos mínimos15min al agotar cola, esperando fuentes nuevas después. Las evaluaciones locales de las mismas definiciones con otras formulaciones no son un benchmark externo de comprensión. El texto web mantiene64MiB, documento16millones de unidades UTF-16/64MiB, biblioteca1024/512MiB; no se probó biblioteca llena ni descarga HTTPS pública nueva y no se declara resuelto el fallo DNS del entorno. Borrar documentos no desentrena pesos; ZIP de conocimiento no respalda modelos, APK reconstruida sí incorpora activos. No se simula audio para sostener procesos.\n'
(r/'VALIDACION-0.66.md').write_text(report)
resources=r/'test/resources/release066';resources.mkdir(exist_ok=True)
for p in w.glob('*.log'):
 if not p.name.startswith(('checkpoint-','final-upload-')):shutil.copy2(p,resources/p.name)
for name in ['validate.py','android.py','package.py']:shutil.copy2(w/name,r/'test-tools'/name)
for p in r.glob('ETAPA-*-EN-VALIDACION.md'):p.unlink()
for suffix,src in [('Android.apk',apk),('Guia.txt',r/'LEEME.txt'),('Mapa-del-proyecto.md',r/'ARQUITECTURA.md')]:shutil.copy2(src,d/('Banana-0.66-'+suffix))
with zipfile.ZipFile(d/'Banana-0.66-Fuentes-privadas.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(r.rglob('*')):
  rel=p.relative_to(r)
  if p.is_file() and not any(x in ['out','classes','__pycache__','.git'] for x in rel.parts):z.write(p,'Banana/'+str(rel))
items=[]
for suffix,id,version in [('Android.apk','libfile_319c929c243c819192dd1b5def76f792',66),('Fuentes-privadas.zip','libfile_4152164178288191a7fbeb9321ecfb0b',66),('Guia.txt','libfile_0de81d7cf038819184ad41bc0adfc8d2',28),('Mapa-del-proyecto.md','libfile_7d6c833899608191b0496195f2d1639a',33)]:
 items.append({'local_path':str(d/('Banana-0.66-'+suffix)),'purpose':'replace_library_file','library_file_id':id,'expected_current_version':version,'version_reason':'Banana0.66: entrenamiento continuo visible, ejercicios con respuestas documentadas y52 suites; servicio real y restauración de pesos/pausa/historial comprobados'})
(w/'final-upload.json').write_text(json.dumps({'uploads':items}))
print('Packaged0.66 with source, model, archive and native integrity checked')
