"""Redirect Java8 utility calls to small API23-compatible helpers, preserving Code offsets.
APK signing algorithms and upstream code flow remain unchanged. Only JDK utilities are adapted.
"""
import struct
U2=lambda b,p:struct.unpack_from('>H',b,p)[0]
U4=lambda b,p:struct.unpack_from('>I',b,p)[0]
def adapt(data):
 cp=[None];p=10;count=U2(data,8)
 while len(cp)<count:
  tag=data[p];p+=1
  if tag==1:
   n=U2(data,p);p+=2;value=data[p:p+n];p+=n
   for old,new in [(b'java/util/function/',b'com/bananalab/signcompat/function/'),(b'java/util/stream/',b'com/bananalab/signcompat/stream/'),(b'java/util/OptionalInt',b'com/bananalab/signcompat/OptionalInt'),(b'java/util/Base64',b'com/bananalab/signcompat/Base64')]:value=value.replace(old,new)
   cp.append((tag,value))
  else:
   size={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,17:4,18:4,19:2,20:2}[tag];cp.append((tag,data[p:p+size]));p+=size
   if tag in (5,6):cp.append(None)
 rest=bytearray(data[p:])
 def text(i):return cp[i][1].decode('ascii')
 def utf(s):
  row=(1,s.encode('ascii'))
  if row in cp:return cp.index(row)
  cp.append(row);return len(cp)-1
 def item(tag,raw):
  row=(tag,raw)
  if row in cp:return cp.index(row)
  cp.append(row);return len(cp)-1
 def method(name,desc):
  cls=item(7,struct.pack('>H',utf('com/bananalab/signcompat/Compat')));nt=item(12,struct.pack('>HH',utf(name),utf(desc)));return item(10,struct.pack('>HH',cls,nt))
 targets={}
 for i in range(1,count):
  row=cp[i]
  if not row or row[0] not in (10,11):continue
  cls,nt=struct.unpack('>HH',row[1]);owner=text(U2(cp[cls][1],0));nameidx,descidx=struct.unpack('>HH',cp[nt][1]);name,desc=text(nameidx),text(descidx)
  if name=='getTypeName' and owner in ('java/lang/reflect/Type','java/lang/Class','java/lang/reflect/ParameterizedType','java/lang/reflect/GenericArrayType','java/lang/reflect/TypeVariable','java/lang/reflect/WildcardType'):targets[i]=method(name,'(Ljava/lang/reflect/Type;)Ljava/lang/String;')
  elif name=='getDeclaredAnnotation' and owner in ('java/lang/Class','java/lang/reflect/Field','java/lang/reflect/Method','java/lang/reflect/Constructor','java/lang/reflect/AnnotatedElement'):targets[i]=method(name,'(Ljava/lang/reflect/AnnotatedElement;Ljava/lang/Class;)Ljava/lang/annotation/Annotation;')
  elif (owner,name)==('java/util/List','sort'):targets[i]=method('sort','(Ljava/util/List;Ljava/util/Comparator;)V')
  elif (owner,name)==('java/util/List','stream'):targets[i]=method('stream','(Ljava/util/List;)Lcom/bananalab/signcompat/stream/Stream;')
  elif (owner,name) in [('java/util/Arrays','stream'),('java/util/Comparator','comparing'),('java/lang/Math','toIntExact')]:targets[i]=method(name,desc)
 for i in range(1,count):
  row=cp[i]
  if row and row[0]==15:
   ref=U2(row[1],1)
   if ref in targets:cp[i]=(15,b"\x06"+struct.pack(">H",targets[ref]))
 def width(code,pc):
  op=code[pc]
  if op in (0xaa,0xab):
   pad=(4-(pc+1)%4)%4;at=pc+1+pad
   if op==0xaa:return 1+pad+12+4*(struct.unpack_from('>i',code,at+8)[0]-struct.unpack_from('>i',code,at+4)[0]+1)
   return 1+pad+8+8*U4(code,at+4)
  if op==0xc4:return 6 if code[pc+1]==0x84 else 4
  if op in (0xb9,0xba,0xc8,0xc9):return 5
  if op==0xc5:return 4
  if op in (0x11,0x13,0x14,0x84) or 0x99<=op<=0xa8 or 0xb2<=op<=0xb8 or op in (0xbb,0xbd,0xc0,0xc1,0xc6,0xc7):return 3
  if op==0x10 or op==0x12 or 0x15<=op<=0x19 or 0x36<=op<=0x3a or op in (0xa9,0xbc):return 2
  return 1
 at=6;interfaces=U2(rest,at);at+=2+interfaces*2
 for group in range(2):
  n=U2(rest,at);at+=2
  for _ in range(n):
   attrs=U2(rest,at+6);at+=8
   for _ in range(attrs):
    name=text(U2(rest,at));length=U4(rest,at+2);body=at+6
    if group==1 and name=='Code':
     size=U4(rest,body+4);offset=body+8;code=bytearray(rest[offset:offset+size]);pc=0
     while pc<len(code):
      op=code[pc];step=width(code,pc)
      if op in (0xb6,0xb7,0xb8,0xb9):
       ref=U2(code,pc+1)
       if ref in targets:
        code[pc]=0xb8;struct.pack_into('>H',code,pc+1,targets[ref])
        if step==5:code[pc+3:pc+5]=b'\0\0'
      pc+=step
     assert pc==len(code);rest[offset:offset+size]=code
    at=body+length
 assert len(cp)<65535
 out=bytearray(data[:8])+struct.pack('>H',len(cp))
 for row in cp[1:]:
  if row:
   out.append(row[0]);value=row[1]
   if row[0]==1:out+=struct.pack('>H',len(value))
   out+=value
 return bytes(out+rest)
