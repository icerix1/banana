"""Owner-only signing capsule. Never included in an APK; do not publish this file's output."""
from pathlib import Path
import subprocess,tempfile,zipfile,sys
root=Path(__file__).resolve().parents[1]
destination=Path(sys.argv[1]).resolve()
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)
 def run(args):subprocess.run(args,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE,check=True)
 run(['openssl','pkcs12','-in',str(root/'keys/release.p12'),'-passin','file:'+str(root/'keys/release-password.txt'),'-nodes','-nocerts','-out',str(p/'key.pem')])
 run(['openssl','pkcs8','-topk8','-nocrypt','-in',str(p/'key.pem'),'-outform','DER','-out',str(p/'private-key.pk8')])
 run(['openssl','pkcs12','-in',str(root/'keys/release.p12'),'-passin','file:'+str(root/'keys/release-password.txt'),'-clcerts','-nokeys','-out',str(p/'cert.pem')])
 run(['openssl','x509','-in',str(p/'cert.pem'),'-outform','DER','-out',str(p/'certificate.der')])
 with zipfile.ZipFile(destination,'w',zipfile.ZIP_DEFLATED) as z:
  for n in ['private-key.pk8','certificate.der']:z.write(p/n,n)
print('Private owner capsule created; no key printed.')
