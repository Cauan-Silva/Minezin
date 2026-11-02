"""Offline Black Divider approval: mirrors EfeitosAsta.blackDivider with Java samples and the mod's GLSL. Not Minecraft.
Light background on purpose: the anime's anti-magic is flat black ink with a thin red rim."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,lookat

TINTA,VERMELHO=0x140A12,0xE01E3A
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def P(q):return np.array([q['x'],q['y'],q['z']],float)

def render(output):
 d=json.loads((ROOT/'build/asta-divider-samples.json').read_text(encoding='utf-8'));Q=d['quadros']
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 w,h=960,540;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 st={}
 # World: player at origin facing +z (so lateral = forward x up = -x).
 frente=np.array([0,0,1.]);lado=np.cross(frente,[0,1,0]);lado/=np.linalg.norm(lado)
 def W(q):return lado*q[0]+np.array([0,q[1],0])+frente*q[2]
 mao=lado*.55+np.array([0,1.1,0])+frente*.6
 def submit(prog,mode,verts,t,additive):
  if not verts:return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  prog['ModelViewMat'].write(st['v'].T.astype('f4').tobytes());prog['ProjMat'].write(proj.T.tobytes());prog['EffectTime'].value=t/20;prog['EffectMode'].value=mode;prog['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(prog,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(o,pts,uv,c,a):
  for k in [0,1,2,0,2,3]:o.append([*pts[k],*rgb(c),a,*uv[k]])
 def rib(o,a,b,wd,al,c,u0=0,u1=1):
  sd=np.cross(b-a,st['c']-a);n=np.linalg.norm(sd)
  if n<1e-8:return
  sd*=wd/n;quad(o,[a-sd,b-sd,b+sd,a+sd],[(u0,0),(u1,0),(u1,1),(u0,1)],c,al)
 def frame(t,cam,look,label):
  q=Q[str(t)];st['c']=np.array(cam,float);st['v']=lookat(st['c'],np.array(look,float))
  fbo.clear(.70,.74,.78,1,depth=1);g=[]
  for i in range(-12,13,2):g.extend([[i,0,-6],[i,0,24]])
  for i in range(-6,25,2):g.extend([[-12,0,i],[12,0,i]])
  line['Matrix'].write((proj@st['v']).T.astype('f4').tobytes());line['Tint'].value=(.52,.56,.6)
  bx=[]
  for dx,dz in [(-.3,-.3),(.3,-.3),(.3,.3),(-.3,.3)]:bx.extend([[dx,0,dz],[dx,1.8,dz]])
  for yy in (0,1.8):bx.extend([[-.3,yy,-.3],[.3,yy,-.3],[.3,yy,-.3],[.3,yy,.3],[.3,yy,.3],[-.3,yy,.3],[-.3,yy,.3],[-.3,yy,-.3]])
  b=ctx.buffer(np.asarray(g+bx,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  carga,cond=q['carga'],q['condensa']
  if carga>.01:
   o=[]
   for fio in q['fios']:
    ant=None
    for i,(p,a) in enumerate(fio):
     pt=mao+W(P(p));u=i/16
     a=min(1,a*1.8)
     if ant is not None and a>.02:rib(o,ant,pt,.09,a,TINTA,u*3,u*3+.2)
     ant=pt
   if cond>.01:
    for k in range(4):off=(k-1.5)*.12;rib(o,mao+lado*off,mao+frente*3.6+lado*off*1.6+np.array([0,math.sin(t*1.7+k)*.15,0]),.3*cond,cond,TINTA,0,4)
   submit(base,22,o,t,False)
   o=[]
   for fa in q['faiscas']:
    ant=mao
    for p in fa:pt=mao+W(P(p));rib(o,ant,pt,.035,carga*.8,VERMELHO);ant=pt
   submit(add,15,o,t,True)
  vr,ml=q['varredura'],q['meia']
  if ml>.01 and vr>.01:
   o=[];fim=1.95*vr;ini=max(0,fim-1.2);n=18
   lam=d['lamina']
   def L(u,a):
    r=1.2+u*7.5;return W(np.array([0,1.3+math.cos(a)*r,math.sin(a)*r+.6]))
   for i in range(n):
    a0=ini+(fim-ini)*i/n;a1=ini+(fim-ini)*(i+1)/n;al=ml
    quad(o,[L(.55,a0),L(.55,a1),L(1,a1),L(1,a0)],[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)],TINTA,al)
   submit(base,22,o,t,False)
  ab,af=q['fenda'],q['alphaFenda']
  if af>.01 and ab>.05:
   o=[];bd=d['borda']
   for i in range(len(bd)-1):
    (b0,w0),(b1,w1)=bd[i],bd[i+1];z0,z1=b0['z'],min(b1['z'],ab)
    if z0>ab:break
    k=(z1-z0)/max(1e-6,b1['z']-z0);x1=b0['x']+(b1['x']-b0['x'])*k
    c0=W(np.array([b0['x'],.04,z0]));c1=W(np.array([x1,.04,z1]))
    quad(o,[c0-lado*w0,c1-lado*w1,c1+lado*w1,c0+lado*w0],[(z0*.5,0),(z1*.5,0),(z1*.5,1),(z0*.5,1)],TINTA,af)
   for p,a in q['gotas']:
    a=min(1,a*1.6)
    if af*a>.05 and p['z']<=ab:pt=W(P(p));rib(o,pt,pt+np.array([0,.28,0]),.13,af*a,TINTA)
   submit(base,22,o,t,False)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');dr=ImageDraw.Draw(im)
  dr.text((14,12),f'ASTA / BLACK DIVIDER / {label} | tick {t} (dano no tick {d["corte"]})',fill=(20,16,24));dr.text((14,h-22),'PREVIA OFFLINE / GLSL DO MOD / SEM ESPADA, SKIN NEM TERRENO',fill=(40,44,52));return im
 lado_cam=(np.array([8.,4.,-3.]),np.array([0,1.5,7.]));costas=(np.array([1.5,3.,-5.]),np.array([0,1.2,6.]))
 out=Path(output);out.mkdir(parents=True,exist_ok=True);fase=lambda t:'CARGA' if t<14 else 'CONDENSA' if t<18 else 'CORTE' if t<22 else 'RASTRO'
 frames=[frame(t,*lado_cam,fase(t)) for t in range(0,d['fim']+1)]+[frame(t,*costas,fase(t)) for t in range(0,d['fim']+1)]
 with av.open(str(out/'asta-black-divider.mp4'),'w') as c:
  s=c.add_stream('libx264',rate=20);s.width,s.height,s.pix_fmt=w,h,'yuv420p';s.options={'crf':'18'}
  for im in frames:
   for p in s.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in s.encode():c.mux(p)
 tw,th=w//2,h//2;sh=Image.new('RGB',(tw*3,th*2))
 for i,(t,cv) in enumerate([(8,lado_cam),(16,lado_cam),(19,lado_cam),(21,lado_cam),(26,costas),(19,costas)]):sh.paste(frame(t,*cv,fase(t)).resize((tw,th),Image.Resampling.LANCZOS),(i%3*tw,i//3*th))
 sh.save(out/'asta-black-divider-fases.png');print('ok')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
