"""Offline Asta base Z/X/C/V approval: mirrors EfeitosAsta.golpeBase with Java samples and the mod's GLSL (ink mode 22). Not Minecraft."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,lookat

TINTA=0x140A12
VERMELHO=0xE01E3A
def hsh(n):x=math.sin(n*127.1+311.7)*43758.5453;return x-math.floor(x)
RAIOS,PONTOS=5,14
def relampago(cam_fn,j,i,t,amp,bolts=RAIOS):
 u=i/PONTOS;v=(j+.5)/RAIOS;b=np.array(cam_fn(u,v),float)
 if i==0 or i==PONTOS:return b
 q=math.floor(t/2)*13+j*31+i*7;return b+np.array([(hsh(q)-.5)*amp,(hsh(q+1)-.5)*amp,(hsh(q+2)-.5)*amp])
def ramo(o,j,i,t,tam):
 q=math.floor(t/2)*17+j*29+i*11;return o+np.array([(hsh(q)-.5)*tam,(hsh(q+1)-.3)*tam*.8,(hsh(q+2)-.5)*tam])
def leque(u,v):a=u*math.pi/3;r=3.0+v*1.9;return (math.sin(a)*r,1.1+(1-abs(u))*.15,math.cos(a)*r)
def vertical(u,v):a=u*2.3;r=2.3+v*1.9;return (0,1.25+math.cos(a)*r,math.sin(a)*r+.4)
def guarda(a,v,t):ang=a*1.9;r=1.35+v*.35+.05*math.sin(t*.4);return (math.sin(ang)*r,1.1+math.cos(ang)*r*.9,.95)
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def P(q):return np.array([q['x'],q['y'],q['z']],float)

def render(output):
 d=json.loads((ROOT/'build/asta-base-samples.json').read_text(encoding='utf-8'));Q=d['quadros']
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy')
 w,h=960,540;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 st={};frente=np.array([0,0,1.]);lado=np.cross(frente,[0,1,0]);lado/=np.linalg.norm(lado)
 def W(q,o):return o+lado*q[0]+np.array([0,q[1],0])+frente*q[2]
 def submit(verts,t):
  if not verts:return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE_MINUS_SRC_ALPHA)
  base['ModelViewMat'].write(st['v'].T.astype('f4').tobytes());base['ProjMat'].write(proj.T.tobytes());base['EffectTime'].value=t/20;base['EffectMode'].value=22;base['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(base,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 add=load_program(ctx,'anime_energy_additive')
 def submit2(verts,t):
  if not verts:return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE)
  add['ModelViewMat'].write(st['v'].T.astype('f4').tobytes());add['ProjMat'].write(proj.T.tobytes());add['EffectTime'].value=t/20;add['EffectMode'].value=15;add['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(add,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(o,pts,uv,a):
  for k in [0,1,2,0,2,3]:o.append([*pts[k],*rgb(TINTA),a,*uv[k]])
 def rib(o,a,b,wd,al,c=TINTA):
  sd=np.cross(b-a,st['c']-a);n=np.linalg.norm(sd)
  if n<1e-8:return
  sd*=wd/n
  for k,(pp,uv) in enumerate(zip([a-sd,b-sd,b+sd,a+sd],[(0,0),(1,0),(1,1),(0,1)])):pass
  for k in [0,1,2,0,2,3]:o.append([*[a-sd,b-sd,b+sd,a+sd][k],*rgb(c),al,*[(0,0),(1,0),(1,1),(0,1)][k]])
 def strip(o,pairs,i0,i1,a,org):
  for i in range(i0,i1):
   (p0,q0),(p1,q1)=pairs[i],pairs[i+1];quad(o,[W(P(p0),org),W(P(p1),org),W(P(q1),org),W(P(q0),org)],[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)],a)
 def frame(slot,t):
  q0=Q[str(t)];org=np.zeros(3)
  if slot==2:org=frente*min(6.5,.65*min(t,10))
  cam={0:(np.array([5.,6.5,-4.]),np.array([0,1,2.5])),1:(np.array([7.,3.,1.]),np.array([0,1.8,3.])),2:(np.array([7.,4.,-1.]),org+np.array([0,1,0])),3:(np.array([2.5,2.2,4.5]),np.array([0,1.2,.5]))}[slot]
  st['c'],st['v']=cam[0],lookat(cam[0],cam[1]);fbo.clear(.70,.74,.78,1,depth=1);g=[]
  for i in range(-10,11,2):g.extend([[i,0,-6],[i,0,14],[-10,0,i+4],[10,0,i+4]])
  bx=[]
  for dx,dz in [(-.3,-.3),(.3,-.3),(.3,.3),(-.3,.3)]:bx.extend([org+[dx,0,dz],org+[dx,1.8,dz]])
  line['Matrix'].write((proj@st['v']).T.astype('f4').tobytes());line['Tint'].value=(.52,.56,.6)
  b=ctx.buffer(np.asarray(g+[list(x) for x in bx],dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  layers=[]
  def raios(fn,amp,larg,al,bolts):
   for j in range(bolts):
    ant=None
    for i in range(PONTOS+1):
     q=relampago(fn,j,i,t,amp);pt=W(q,org);af=math.sin(math.pi*i/PONTOS)*.6+.4
     if ant is not None:layers.append((ant,pt,larg*.5*af,al,False));layers.append((ant,pt,larg*.4,al*.8,True))
     if i%3==1:r=W(ramo(q,j,i,t,amp*2.2),org);layers.append((pt,r,larg*.28,al,False));layers.append((pt,r,larg*.25,al*.6,True))
     ant=pt
  if slot==0 and q0['alpha0']>.01 and q0['varre0']>.01:vr=q0['varre0'];raios(lambda u,v:leque(-1+2*vr*u,v),.45,.2,q0['alpha0'],5)
  if slot==1:
   vr=q0['varre1'];lo=max(0,vr-.8)
   if q0['alpha1']>.01 and vr>.01:raios(lambda u,v:vertical(lo+(vr-lo)*u,v),.5,.22,q0['alpha1'],4)
   r=q0['rasgo'];bd=d['rasgo']
   if r>.01:raios(lambda u,v:(bd[int(round(u*11*r))][0]['x']+(v-.5)*.6,.06,bd[int(round(u*11*r))][0]['z']),.3,.16,q0['alpha1'],2)
  if slot==2:
   L=q0['rastro']
   if L>.05:raios(lambda u,v:((v-.5)*.9,.5+v*.9,-L*(1-u)),.3,.16,1,3)
   e=q0['alphaEspinho']
   if e>.01:
    for k in range(0,10,2):
     tp=q0['espinhos'][k];tt=t;
     for j in range(1):
      ant=None
      for i in range(PONTOS+1):
       u_=i/PONTOS;b=np.array([tp['x']*u_,.1+(tp['y']-.1)*u_,tp['z']*u_])
       if 0<i<PONTOS:qq=math.floor((t+k)/2)*13+j*31+i*7;b=b+np.array([(hsh(qq)-.5)*.35,(hsh(qq+1)-.5)*.35,(hsh(qq+2)-.5)*.35])
       pt=W(b,org);af=math.sin(math.pi*i/PONTOS)*.6+.4
       if ant is not None:layers.append((ant,pt,.09*af,e,False));layers.append((ant,pt,.072,e*.8,True))
       ant=pt
  if slot==3 and q0['guarda']>.01:raios(lambda u,v:guarda(-1+2*u,v,t),.28,.14,q0['guarda'],3)
  o=[];g2=[]
  for a_,b_,wd,al,br in layers:rib(g2 if br else o,a_,b_,wd,al,VERMELHO if br else TINTA)
  submit(o,t);submit2(g2,t)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');dr=ImageDraw.Draw(im)
  nome=['Z LEQUE DO LADO PLANO (dano t6)','X CORTE VERTICAL (dano t8)','C INVESTIDA TOURO NEGRO (dano t10)','V GUARDA ANTIMAGIA'][slot]
  dr.text((14,12),f'ASTA / {nome} | tick {t}',fill=(20,16,24));dr.text((14,h-22),'PREVIA OFFLINE / GLSL DO MOD / SEM ESPADA, SKIN NEM ANIMACAO',fill=(40,44,52));return im
 out=Path(output);out.mkdir(parents=True,exist_ok=True)
 frames=[frame(s,t) for s in range(4) for t in range(0,33)]
 with av.open(str(out/'asta-golpes-base.mp4'),'w') as c:
  s=c.add_stream('libx264',rate=20);s.width,s.height,s.pix_fmt=w,h,'yuv420p';s.options={'crf':'18'}
  for im in frames:
   for p in s.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in s.encode():c.mux(p)
 tw,th=w//2,h//2;sh=Image.new('RGB',(tw*2,th*2))
 for i,(s_,t) in enumerate([(0,6),(1,10),(2,12),(3,20)]):sh.paste(frame(s_,t).resize((tw,th),Image.Resampling.LANCZOS),(i%2*tw,i//2*th))
 sh.save(out/'asta-golpes-base-fases.png');print('ok')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
