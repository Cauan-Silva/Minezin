"""Offline Frieren Zoltraak and Barrier approval: runtime GLSL and Java-exported geometry, no Minecraft.
Mirrors RenderMagiasFrieren.zoltraak/barreira/estilhacos; references: Frieren wiki stills (six-petal circle, EP21 barrier)."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,lookat

TRACO,HALO,FAVO,VIDRO=0xE8FBFF,0xC9D4FF,0x7FF3FF,0x5FD8FF
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def p2(q):return q['x'],q['y']
def unit(v):n=np.linalg.norm(v);return v/n if n>1e-9 else v
def basis(normal):
 u=np.cross(normal,[0,1,0])
 if np.linalg.norm(u)<1e-4:u=np.array([1.,0,0])
 u=unit(u);return u,unit(np.cross(normal,u))

def render(output,width=960,height=540):
 data=json.loads((ROOT/'build/frieren-base-samples.json').read_text(encoding='utf-8'))
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 w,h=width,height;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 st={}
 def submit(program,mode,verts,t,additive=True):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  program['ModelViewMat'].write(st['view'].T.astype('f4').tobytes());program['ProjMat'].write(proj.T.tobytes());program['EffectTime'].value=t/20;program['EffectMode'].value=mode;program['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(out,pts,uvs,color,alpha):
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*rgb(color),alpha,*uvs[k]])
 def ribbon(out,a,b,width,alpha,color,u0=0,u1=1):
  side=np.cross(b-a,st['cam']-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;quad(out,[a-side,b-side,b+side,a+side],[(u0,0),(u1,0),(u1,1),(u0,1)],color,alpha)
 def anel(out,c,normal,i_,e_,color,alpha,lados):
  u,v=basis(normal)
  for i in range(lados):
   a,b=i*math.tau/lados,(i+1)*math.tau/lados;p=u*math.cos(a)+v*math.sin(a);q=u*math.cos(b)+v*math.sin(b)
   quad(out,[c+p*i_,c+q*i_,c+q*e_,c+p*e_],[(i*.3,0),((i+1)*.3,0),((i+1)*.3,1),(i*.3,1)],color,alpha)
 def plano(c,b,x,y):return c+b[0]*x+b[1]*y
 def plano_r(c,b,q,esc,giro):
  x,y=q;cs,sn=math.cos(giro),math.sin(giro);return plano(c,b,(x*cs-y*sn)*esc,(x*sn+y*cs)*esc)
 def lines(points,tint):
  line['Matrix'].write((proj@st['view']).T.astype('f4').tobytes());line['Tint'].value=tint
  b=ctx.buffer(np.asarray(points,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
 def scene(cam,look,caster=np.zeros(3)):
  st['cam']=np.asarray(cam,float);st['view']=lookat(st['cam'],np.asarray(look,float))
  fbo.depth_mask=True;fbo.clear(.035,.045,.075,1,depth=1)
  g=[]
  for i in range(-20,41,2):g.extend([[-20,0,i],[20,0,i]])
  for i in range(-20,21,2):g.extend([[i,0,-20],[i,0,40]])
  lines(g,(.12,.14,.2))
  x,z=caster[0],caster[2];bx=[]
  for dx,dz in [(-.3,-.3),(.3,-.3),(.3,.3),(-.3,.3)]:bx.extend([[x+dx,0,z+dz],[x+dx,1.8,z+dz]])
  for y in (0,1.8):bx.extend([[x-.3,y,z-.3],[x+.3,y,z-.3],[x+.3,y,z-.3],[x+.3,y,z+.3],[x+.3,y,z+.3],[x-.3,y,z+.3],[x-.3,y,z+.3],[x-.3,y,z-.3]])
  lines(bx,(.6,.66,.8))
 def circulo(out,c,normal,raio,giro,alpha):
  b=basis(normal);tr=max(.008,raio*.014)
  anel(out,c,normal,raio*.97,raio,TRACO,alpha,64);anel(out,c,normal,raio*.86,raio*.885,TRACO,alpha*.9,64);anel(out,c,normal,raio*.8,raio*.815,TRACO,alpha*.8,64)
  for m in range(36):
   a=giro+m*math.pi/18
   if m%3==2:anel(out,plano(c,b,math.cos(a)*raio*.927,math.sin(a)*raio*.927),normal,raio*.012,raio*.026,TRACO,alpha*.85,10);continue
   ribbon(out,plano(c,b,math.cos(a-.035)*raio*.927,math.sin(a-.035)*raio*.927),plano(c,b,math.cos(a+.035)*raio*.927,math.sin(a+.035)*raio*.927),tr,alpha*.85,TRACO)
  for k in range(6):
   pts=[plano_r(c,b,p2(q),raio,giro) for q in data['petalas'][k]]
   for i in range(1,len(pts)):ribbon(out,pts[i-1],pts[i],tr,alpha,TRACO)
   o=plano_r(c,b,p2(data['olhos'][k]),raio,giro);anel(out,o,normal,raio*.085,raio*.1,TRACO,alpha,24);anel(out,o,normal,raio*.04,raio*.052,TRACO,alpha*.8,16)
   tri=[plano_r(c,b,p2(q),raio,giro) for q in data['triangulos'][k]]
   for v in range(3):ribbon(out,tri[v],tri[(v+1)%3],tr,alpha*.8,TRACO)
  anel(out,c,normal,raio*.1,raio*.115,TRACO,alpha,24);anel(out,c,normal,raio*.04,raio*.05,TRACO,alpha,16)
 origem=np.array([.35,1.4,.6]);alvo=origem+np.array([0,0,18.]);dirz=unit(alvo-origem)
 def zoltraak(t,cam,look,label):
  scene(cam,look);info=data['zoltraak'][str(t)];lw=info['largura'];b=basis(dirz);comp=np.linalg.norm(alvo-origem)
  if lw>.01:
   out=[];ribbon(out,origem,alvo,.95*lw,.55*lw,HALO,0,8);ribbon(out,origem,alvo,.32*lw,min(1,lw*1.6),0xFFFFFF,0,8);submit(add,1,out,t)
   out=[]
   for j in range(3):
    a=j*math.tau/3+t*.25;off=b[0]*math.cos(a)*.24*lw+b[1]*math.sin(a)*.24*lw;ribbon(out,origem+off,alvo+off,.02,.5*lw,TRACO)
   submit(add,15,out,t)
  out=[]
  for frac,off,ang,a in info['faiscas']:
   if a>.01:q=origem+dirz*comp*frac+b[0]*math.cos(ang)*off+b[1]*math.sin(ang)*off;ribbon(out,q,q+dirz*.28,.035,a,0xFFFFFF)
  imp=info['impacto']
  if imp>.01:r=.2+(1-imp)*1.6;anel(out,alvo,unit(st['cam']-alvo),r*.75,r,TRACO,imp,32)
  submit(add,15,out,t)
  if info['alpha']>.01:out=[];circulo(out,origem+dirz*.6,dirz,.85*info['escala'],info['giro'],info['alpha']);submit(add,15,out,t)
  return finish(f'FRIEREN / ZOLTRAAK / {label} | tick {t}')
 peito=np.array([0,1.25,0]);olhar=np.array([0,0,1.])
 def barreira(t,cam,look):
  scene(cam,look);info=data['barreira'][str(t)];a=info['alpha'];b=basis(olhar);c0=peito+olhar*1.4
  if a>.01:
   out=[]
   for cel in info['celulas']:
    if cel['escala']<.01:continue
    o=plano(c0,b,*p2(cel['centro']));vs=[plano(c0,b,*p2(v)) for v in cel['vertices']]
    for v in range(6):quad(out,[o,vs[v],vs[(v+1)%6],vs[(v+1)%6]],[(0,0),(1,0),(1,1),(0,1)],VIDRO,a*.14)
   submit(add,8,out,t)
   for largura,ganho in [(.09,.3),(.028,1.)]:
    out=[]
    for cel in info['celulas']:
     if cel['escala']<.01:continue
     vs=[plano(c0,b,*p2(v)) for v in cel['vertices']];luz=a*ganho*cel['pulso']*cel['escala']
     for v in range(6):ribbon(out,vs[v],vs[(v+1)%6],largura,luz,FAVO)
    submit(add,15,out,t)
  return finish(f'FRIEREN / BARREIRA ARCANA | tick {t}')
 def cacos(t,cam,look):
  scene(cam,look);info=data['cacos'][str(t)];a=info['alpha'];b=basis(olhar);c0=peito+olhar*1.4
  for borda in (False,True):
   out=[]
   for c,v,d,frente,g in info['cacos']:
    fav=data['favo'][c];o=p2(fav['centro']);p0=p2(fav['vertices'][v]);p1=p2(fav['vertices'][(v+1)%6])
    cx=(o[0]+p0[0]+p1[0])/3;cy=(o[1]+p0[1]+p1[1])/3;q=[]
    for x,y in (o,p0,p1):
     x-=cx;y-=cy;rx=x*math.cos(g)-y*math.sin(g);ry=x*math.sin(g)+y*math.cos(g);q.append(plano(c0,b,cx+d['x']+rx*.92,cy+d['y']+ry*.92)+olhar*frente)
    if borda:
     for k in range(3):ribbon(out,q[k],q[(k+1)%3],.02,a,FAVO)
    else:quad(out,[q[0],q[1],q[2],q[2]],[(0,0),(1,0),(1,1),(0,1)],VIDRO,a*.3)
   submit(add,15 if borda else 8,out,t)
  return finish(f'FRIEREN / BARREIRA ESTILHACANDO | tick {t}')
 def impulso(t,cam,look):
  # Mirror of RenderMagiasFrieren.impulso; the mannequin rises on the server's launch arc.
  info=data['impulso'][str(t)];alt=3.2*(1-math.exp(-t/9))
  pos=np.array([0,alt,0]);scene(cam,look,pos);out=[]
  if info['rajada']>.01:
   for a in info['angulos']:d=np.array([math.cos(a),0,math.sin(a)]);ribbon(out,d*info['raio']*.55+[0,.08,0],d*info['raio']+[0,.15,0],.06,info['rajada']*.8,0xEAF6FF)
  if info['subida']>.01:
   for j in range(6):a=j*math.pi/3+.4;b0=pos+[math.cos(a)*.55,-.3,math.sin(a)*.55];ribbon(out,b0,b0+[0,1.6+j%2*.6,0],.04,info['subida']*.7,0xEAF6FF)
  submit(add,14,out,t)
  out=[]
  for i,a in enumerate(info['rastro']):
   if a>.01:q=pos+[math.sin(i*2.1)*.45,-.4-(t-i*3)*.03,math.cos(i*2.1)*.45];anel(out,q,unit(st['cam']-q),0,.08,0xE4F0FF,a,8)
  submit(add,15,out,t);return finish(f'FRIEREN / IMPULSO ARCANO | tick {t}')
 def gaiola(t,cam,look):
  info=data['gaiola'][str(t)];scene(cam,look,np.array([0,0,-14.]));a=info['alpha'];esp=info['espalha']
  if a>.01:
   out=[]
   for faixa in info['faixas']:
    pts=[np.array([math.cos(ang)*r*esp,y,math.sin(ang)*r*esp]) for ang,y,r in faixa]
    for i in range(1,len(pts)):ribbon(out,pts[i-1],pts[i],.6+pts[i][1]*.06,a*.85,0xD8CDB8,(i-1)*.25,i*.25)
   anel(out,np.array([0,.1,0]),np.array([0,1.,0]),1.6*esp,5.5*esp,0xD8CDB8,a*.5,48)
   submit(base,20,out,t,False)
   out=[]
   for i,(ang,y,r) in enumerate(info['detritos']):
    c=np.array([math.cos(ang)*r*esp*1.08,y,math.sin(ang)*r*esp*1.08]);s_=.08+(i%3)*.04
    for fc in [((-1,-1),(1,-1),(1,1),(-1,1))]:quad(out,[c+[x*s_,yy*.06,0] for x,yy in fc],[(0,0),(1,0),(1,1),(0,1)],0x6E665A,a)
   submit(base,8,out,t,False)
  return finish(f'FRIEREN / GAIOLA DE VENTO (WALDGOSE) | tick {t}')
 def finish(title):
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im)
  d.text((14,12),title,fill=(220,240,255));d.text((14,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD / SEM TERRENO, SKIN NEM ANIMACAO',fill=(160,172,196));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 lado=(np.array([5.5,2.6,-2.5]),np.array([0,1.4,6.]))
 frente_circ=(origem+dirz*2.4+np.array([2.,.5,0]),origem+dirz*.6)
 cam_bar=(np.array([2.6,1.9,4.2]),peito+olhar*1.4)
 frames=[]
 for t in range(0,31):frames.append(zoltraak(t,*lado,'DISPARO'))
 for t in range(0,31):frames.append(zoltraak(t,*frente_circ,'CIRCULO DE PERTO'))
 for t in range(0,41):frames.append(barreira(t,*cam_bar))
 for t in range(0,21):frames.append(cacos(t,*cam_bar))
 cam_imp=(np.array([4.5,2.5,-5.5]),np.array([0,1.8,0]));cam_gai=(np.array([9.,5.,-15.]),np.array([0,4.,0]))
 for t in range(0,51):frames.append(impulso(t,*cam_imp))
 for t in range(0,121):frames.append(gaiola(t,*cam_gai))
 video=output/'frieren-zoltraak-barreira.mp4'
 with av.open(str(video),'w') as dest:
  try:vs=dest.add_stream('libx264',rate=20);vs.options={'crf':'18'}
  except Exception:vs=dest.add_stream('mpeg4',rate=20);vs.bit_rate=8_000_000
  vs.width,vs.height,vs.pix_fmt=w,h,'yuv420p'
  for im in frames:
   for p in vs.encode(av.VideoFrame.from_image(im)):dest.mux(p)
  for p in vs.encode():dest.mux(p)
 tw,th=w//2,h//2;sheet=Image.new('RGB',(tw*3,th*2),(8,10,16))
 shots=[zoltraak(2,*lado,'DISPARO'),zoltraak(10,*lado,'DISPARO'),zoltraak(6,*frente_circ,'CIRCULO DE PERTO'),barreira(3,*cam_bar),barreira(30,*cam_bar),cacos(8,*cam_bar)]
 for i,im in enumerate(shots):sheet.paste(im.resize((tw,th),Image.Resampling.LANCZOS),(i%3*tw,i//3*th))
 sheet.save(output/'frieren-zoltraak-barreira-fases.png')
 sheet2=Image.new('RGB',(tw*3,th*2),(8,10,16))
 for i,im in enumerate([impulso(3,*cam_imp),impulso(10,*cam_imp),impulso(25,*cam_imp),gaiola(6,*cam_gai),gaiola(60,*cam_gai),gaiola(112,*cam_gai)]):sheet2.paste(im.resize((tw,th),Image.Resampling.LANCZOS),(i%3*tw,i//3*th))
 sheet2.save(output/'frieren-impulso-gaiola-fases.png');print('Base spells GPU:',ctx.info['GL_RENDERER'],'video:',video)
 for r in [base,add,line,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
