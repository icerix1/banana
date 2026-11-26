from pathlib import Path
import os,subprocess,time,shutil,xml.etree.ElementTree as ET,re,shlex
w=Path(__file__).resolve().parent;r=w.parent/'release067/Banana';c=Path('/workspace/scratch/f1fa469e81ab/banana/sdk');env=os.environ.copy();env.update(ANDROID_I_WANT_MY_TCG='yes',ANDROID_AVD_HOME='/workspace/scratch/f1fa469e81ab/banana/sdk/avd',ANDROID_TOOLS=str(c/'android-15'),ANDROID_JAR=str(c/'android-35-ext15/android.jar'),ECJ_JAR=str(c/'ecj.jar'))
subprocess.run([str(c/'platform-tools/adb'),'start-server'],env=env,capture_output=True,check=True);log=(w/'emulator.log').open('w');em=subprocess.Popen([str(c/'emulator/emulator'),'-avd','banana23','-read-only','-no-window','-no-audio','-no-snapshot','-no-boot-anim','-no-metrics','-gpu','swiftshader_indirect','-accel','off','-dns-server','168.63.129.16','-port','5554'],env=env,stdout=log,stderr=log);adb=[str(c/'platform-tools/adb'),'-s','emulator-5554']
def run(args,timeout=120):
 p=subprocess.run(adb+args,capture_output=True,text=True,timeout=timeout)
 if p.returncode:raise RuntimeError(str(args)+p.stderr[-400:]+p.stdout[-800:])
 return p.stdout
def ui():
 run(['shell','uiautomator','dump','/sdcard/ui.xml']);s=run(['shell','cat','/sdcard/ui.xml']);(w/'ui.xml').write_text(s);return ET.fromstring(s[s.index('<?xml'):].strip())
def tap(n):
 a=list(map(int,re.findall('[0-9]+',n.attrib['bounds'])));run(['shell','input','tap',str((a[0]+a[2])//2),str((a[1]+a[3])//2)])
def wait(s):
 end=time.time()+600
 while time.time()<end:
  try:
   if any(s in n.attrib.get('text','') for n in ui().iter('node')):return
  except Exception:pass
  time.sleep(2)
 raise RuntimeError('UI missing '+s)
def send(s):
 tap(next(n for n in ui().iter('node') if n.attrib.get('class')=='android.widget.EditText'));expected=''
 for at in range(0,len(s),24):
  chunk=s[at:at+24];expected+=chunk;run(['shell','input','text',shlex.quote(chunk.replace(' ','%s'))]);end=time.time()+30
  while time.time()<end:
   if next(n.attrib.get('text','') for n in ui().iter('node') if n.attrib.get('class')=='android.widget.EditText')==expected:break
  else:raise RuntimeError('incomplete input')
 run(['shell','input','keyevent','4']);tap(next(n for n in ui().iter('node') if n.attrib.get('text')=='Enviar'))
def seed():
 run(['shell','run-as','com.bananalab.lite','mkdir','-p','shared_prefs']);run(['push',str(w/'legacy.xml'),'/data/local/tmp/banana-legacy.xml']);run(['shell','run-as','com.bananalab.lite','cp','/data/local/tmp/banana-legacy.xml','shared_prefs/banana-knowledge-notebook.xml'])
def instrument(name,phase=None,timeout=2400):
 args=['shell','am','instrument','-w']
 if phase:args+=['-e','phase',phase]
 args+=['com.bananalab.lite/.'+name];path=w/('android-'+(phase or 'instrument')+'.log')
 with path.open('w') as f:
  process=subprocess.Popen(adb+args,stdout=f,stderr=subprocess.STDOUT,env=env)
  try:code=process.wait(timeout=timeout)
  except subprocess.TimeoutExpired:process.kill();raise
 result=path.read_text();print(result,flush=True)
 if code:raise RuntimeError('instrument command failed')
 return result
try:
 t=w/'instrumented';shutil.copytree(r,t,ignore=shutil.ignore_patterns('out','classes','__pycache__'),dirs_exist_ok=True);p=t/'AndroidManifest.xml';p.write_text(p.read_text().replace('android:allowBackup="false"','android:debuggable="true" android:allowBackup="false"').replace('</manifest>','<instrumentation android:name=".Native66Instrumentation" android:targetPackage="com.bananalab.lite"/><instrumentation android:name=".Native66ReopenInstrumentation" android:targetPackage="com.bananalab.lite"/></manifest>'))
 for name in ['Native66Instrumentation','Native66ReopenInstrumentation']:shutil.copy2(r/('test-android/'+name+'.java'),t/('src/com/bananalab/lite/'+name+'.java'))
 p=t/'tools/index_sources.py';p.write_text(p.read_text().replace("if p.name.endswith('Instrumentation.java'):continue",""))
 with (w/'instrument-build.log').open('w') as f:subprocess.run(['sh',str(t/'build.sh')],env=env,stdout=f,stderr=subprocess.STDOUT,check=True)
 end=time.time()+300
 while time.time()<end:
  try:
   if run(['shell','getprop','sys.boot_completed'],6).strip()=='1':break
  except Exception:pass
  time.sleep(2)
 else:raise RuntimeError('boot timeout')
 print('BOOT_READY',flush=True);print('LEGACY_INSTALL',run(['install','-r',str(w.parent/'work066/instrumented/out/BananaLab-Lite.apk')]),flush=True);run(['shell','pm','clear','com.bananalab.lite']);seed();print('NEW_INSTALL',run(['install','-r',str(t/'out/BananaLab-Lite.apk')]),flush=True);run(['shell','mkdir','-p','/sdcard/Android/data/com.bananalab.lite/files']);run(['push',str(w.parent/'work060/owner.zip'),'/sdcard/Android/data/com.bananalab.lite/files/owner.zip']);run(['logcat','-c'])
 result=instrument('Native66Instrumentation')
 while 'PASS NATIVE66 ' not in result:
  print('WAITING_FOR_FIX_RETRY',flush=True);end=time.time()+1200
  while time.time()<end and not (w/'retry').exists():time.sleep(2)
  if not (w/'retry').exists():raise RuntimeError('retry timeout')
  (w/'retry').unlink()
  for source in (r/'src/com/bananalab/lite').glob('*.java'):shutil.copy2(source,t/'src/com/bananalab/lite'/source.name)
  for name in ['Native66Instrumentation','Native66ReopenInstrumentation']:shutil.copy2(r/('test-android/'+name+'.java'),t/('src/com/bananalab/lite/'+name+'.java'))
  with (w/'instrument-build.log').open('w') as f:subprocess.run(['sh',str(t/'build.sh')],env=env,stdout=f,stderr=subprocess.STDOUT,check=True)
  run(['shell','pm','clear','com.bananalab.lite']);seed();print(run(['install','-r',str(t/'out/BananaLab-Lite.apk')]),flush=True);run(['shell','mkdir','-p','/sdcard/Android/data/com.bananalab.lite/files']);run(['push',str(w.parent/'work060/owner.zip'),'/sdcard/Android/data/com.bananalab.lite/files/owner.zip']);result=instrument('Native66Instrumentation')
 run(['pull','/sdcard/Android/data/com.bananalab.lite/files/Banana.apk',str(w/'Banana.apk')]);print('NORMAL_APK_PULLED',flush=True)
 with (w/'signature.log').open('w') as f:subprocess.run([str(c/'android-15/apksigner'),'verify','--verbose',str(w/'Banana.apk')],stdout=f,stderr=subprocess.STDOUT,check=True)
 print('UPDATE_INSTALL',run(['install','-r',str(w/'Banana.apk')]),flush=True)
 for phase in ['upgrade','fresh']:
  if phase=='fresh':run(['shell','pm','clear','com.bananalab.lite'])
  result=instrument('Native66ReopenInstrumentation',phase,300);assert 'PASS NATIVE66_REOPEN '+phase in result
 print('FULL_DOCUMENT_ARCHIVE_UPDATE_AND_FRESH_OK',flush=True)
 run(['uninstall','com.bananalab.lite']);print('PRODUCTION_INSTALL',run(['install',str(r/'out/BananaLab-Lite.apk')]),flush=True);run(['shell','am','start','-n','com.bananalab.lite/.StartActivity']);wait('BANANA');send('pausa el entrenamiento de lecturas');wait('Pausé el entrenamiento');send('aprende que la archivina es un registro ficticio con la clave Cedro517');wait('Guardé el documento completo');send('que es la archivina');wait('Cedro517');print('PRODUCTION_KNOWLEDGE_UI_OK',flush=True)
 send('aprende de un archivo');end=time.time()+60
 while time.time()<end:
  if any(n.attrib.get('package')=='com.android.documentsui' for n in ui().iter('node')):break
  time.sleep(1)
 else:raise RuntimeError('document picker not opened')
 print('PRODUCTION_DOCUMENT_PICKER_OPENED',flush=True);run(['shell','input','keyevent','4']);assert 'FATAL EXCEPTION' not in run(['logcat','-d','-s','AndroidRuntime:E']);print('NO_FATAL_EXCEPTION',flush=True)

finally:
 try:(w/'logcat.txt').write_text(run(['logcat','-d']))
 except Exception:pass
 em.terminate()
 try:em.wait(timeout=5)
 except subprocess.TimeoutExpired:em.kill()
