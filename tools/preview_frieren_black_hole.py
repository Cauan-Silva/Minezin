"""Offline Frieren Black Hole approval: runtime GLSL and Java-exported geometry, no Minecraft.
Mirrors RenderMagiasFrieren.buracoNegro; reference: the Frieren clone black hole scene supplied by the user."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_armor_models import ROOT,lookat,translation

BRASA,BORDA,RACHA,VIOLETA,CIANO,VENTO=0xFF9A3C,0xFF7A2A,0xFF5C84,0xB36BFF,0xA8E8FF,0xCFEFFF
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def vec(p):return np.array([p['x'],p['y'],p['z']],dtype=float)
def unit(v):n=np.linalg.norm(v);return v/n if n>1e-9 else v
def basis(normal):
 u=np.cross(normal,[0,1,0])
 if np.linalg.norm(u)<1e-4:u=np.array([1.,0,0])
 u=unit(u);return u,unit(np.cross(normal,u))
def smooth(x):x=max(0,min(1,x));return x*x*(3-2*x)

def render(output,width=960,height=540,audio=True):
 data=json.loads((ROOT/'build/frieren-buraco-samples.json').read_text(encoding='utf-8'))
 A,C,F,L,frames=data['abertura'],data['colapso'],data['fim'],data['lancamento'],data['quadros']
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 w,h=width,height;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 centro=np.array([0.,4.,0.]);caster=np.array([-.8,0.,-11.95]);mao=centro+vec(data['mao'])
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 state={}
 def submit(program,mode,verts,t,additive=False,model=None):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  mv=state['view']@model if model is not None else state['view']
  program['ModelViewMat'].write(mv.T.astype('f4').tobytes());program['ProjMat'].write(proj.T.tobytes());program['EffectTime'].value=t/20;program['EffectMode'].value=mode
  program['Viewer'].value=tuple(unit(state['cam']-state['pos']))
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(out,pts,uvs,color,alpha):
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*rgb(color),alpha,*uvs[k]])
 def ribbon(out,a,b,width,alpha,color,u0=0,u1=1):
  side=np.cross(b-a,state['cam']-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;quad(out,[a-side,b-side,b+side,a+side],[(u0,0),(u1,0),(u1,1),(u0,1)],color,alpha)
 def anel(out,c,normal,interno,externo,color,alpha,lados,angular=False):
  u,v=basis(normal)
  for i in range(lados):
   a,b=i*math.tau/lados,(i+1)*math.tau/lados;p=u*math.cos(a)+v*math.sin(a);q=u*math.cos(b)+v*math.sin(b)
   u0,u1=(i*8/lados,(i+1)*8/lados) if angular else (i*.3,(i+1)*.3)
   quad(out,[c+p*interno,c+q*interno,c+q*externo,c+p*externo],[(u0,0),(u1,0),(u1,1),(u0,1)],color,alpha)
 def sphere(radius,c,color,alpha,scale_y=1.):
  return [[x+c[0],y*scale_y+c[1],z+c[2],*rest] for x,y,z,*rest in mesh(radius,24,(*rgb(color),alpha))]
 def box(out,c,sx,sy,sz,rot,color,alpha):
  ax,az=rot;cx,sxn,cz,szn=math.cos(ax),math.sin(ax),math.cos(az),math.sin(az)
  def tr(p):x,y,z=p;y,z=y*cx-z*sxn,y*sxn+z*cx;x,y=x*cz-y*szn,x*szn+y*cz;return c+np.array([x,y,z])
  v=[tr(((-sx if i&1==0 else sx),(-sy if i&2==0 else sy),(-sz if i&4==0 else sz))) for i in range(8)]
  for fc in [(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]:quad(out,[v[k] for k in fc],[(0,0),(1,0),(1,1),(0,1)],color,alpha)
 def lines(points,tint):
  line['Matrix'].write((proj@state['view']).T.astype('f4').tobytes());line['Tint'].value=tint
  b=ctx.buffer(np.asarray(points,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
 def fase(t):
  return 'FORMACAO NA MAO' if t<L else 'LANCAMENTO' if t<A else 'ATRACAO' if t<C-24 else 'TENSAO / CASCA' if t<C else 'EXPLOSAO' if t<C+30 else 'DISSIPACAO'
 def draw(t,cam,alvo):
  info=frames[str(t)];fade=info['alpha'];r=info['nucleo'];casca=info['casca'];tensao=info['tensao']
  tremor=np.array([math.sin(t*2.3),math.sin(t*3.1+1)*.6,math.cos(t*2.7)])*tensao*.1 if t<C else np.zeros(3)
  pos=centro+vec(info['posicao'])+tremor;state['pos']=pos
  state['cam']=np.asarray(cam,dtype=float);state['view']=lookat(state['cam'],np.asarray(alvo,dtype=float))
  fbo.depth_mask=True;fbo.clear(.035,.045,.075,1,depth=1)
  grid=[]
  for i in range(-30,31,2):grid.extend([[-30,0,i],[30,0,i],[i,0,-30],[i,0,30]])
  lines(grid,(.12,.14,.2))
  x,z=caster[0],caster[2];bx=[]
  for y0,y1 in [(0,0),(1.8,1.8)]:bx.extend([[x-.3,y0,z-.3],[x+.3,y1,z-.3],[x+.3,y0,z-.3],[x+.3,y1,z+.3],[x+.3,y0,z+.3],[x-.3,y1,z+.3],[x-.3,y0,z+.3],[x-.3,y1,z-.3]])
  for dx,dz in [(-.3,-.3),(.3,-.3),(.3,.3),(-.3,.3)]:bx.extend([[x+dx,0,z+dz],[x+dx,1.8,z+dz]])
  lines(bx,(.6,.66,.8))
  vista=unit(state['cam']-pos);lateral=np.cross(vista,[0,1,0]);lateral=unit(lateral) if np.linalg.norm(lateral)>1e-6 else np.array([1.,0,0])
  if r>.02 and t<C+4:
   fbo.depth_mask=fade>.95;submit(base,3,sphere(r,pos,BORDA,fade),t);fbo.depth_mask=False
   out=[];anel(out,pos,vista,r*.98,info['halo'],0xFFFFFF,fade*(1-casca),64,True);submit(add,16,out,t,True)
   meia=info['estria']
   if meia>.05:out=[];ribbon(out,pos-lateral*meia,pos+lateral*meia,info['larguraEstria'],fade*(1-casca),0xFFFFFF);submit(add,17,out,t,True)
   out=[]
   for rd,a in info['distorcao']:
    if fade*a>.01:anel(out,pos,vista,rd,rd+max(.08,r*.12),0xFF7A4A,fade*a,48,True)
   submit(add,9,out,t,True)
  if info['emVoo']:
   out=[];anterior=pos
   for k in range(1,9):
    q=centro+vec(info['rastro'][k]);ribbon(out,anterior,q,.38*(1-k/9),fade*(1-k/9),BRASA,k-1,k);anterior=q
   submit(add,1,out,t,True)
  if A-6<t<C:
   out=[]
   for j,pts in enumerate(info['ventos']):
    for i in range(1,len(pts)):
     a=fade*info['ventosAlpha'][j][i];s=i/24
     if a>.01:ribbon(out,pos+vec(pts[i-1]),pos+vec(pts[i]),.12,a,VENTO,s-1/24,s)
   submit(add,14,out,t,True)
  if casca>.01:
   rc=data['raioCasca'];dense=fade*casca>.95;fbo.depth_mask=dense;submit(base,3,sphere(rc,pos,RACHA,fade*casca,1.2),t);fbo.depth_mask=False
   out=[];anel(out,pos,vista,rc*.9,rc*1.4,RACHA,fade*casca*.55,48);submit(add,15,out,t,True)
   abertas=info['rachaduras']
   for largura,ganho in [(.5,.55),(.14,1.)]:
    out=[]
    for cam_ in data['caminhosRachadura']:
     for i in range(1,len(cam_)):
      if i>math.ceil(abertas):break
      a=pos+vec(cam_[i-1])*rc*1.01;b=pos+vec(cam_[i])*rc*1.01;b=a+(b-a)*min(1,abertas-(i-1))
      ribbon(out,a,b,largura,fade*casca*ganho,RACHA)
    submit(add,15,out,t,True)
  if t>=C:
   esp=fade*info['alphaEspinho'];rel=fade*info['alphaRelampago']
   if esp>.01:
    out=[]
    for d,comp in zip(data['direcoesEspinho'],info['espinhos']):ribbon(out,pos,pos+unit(vec(d))*comp,1.8,esp,VIOLETA)
    submit(add,18,out,t,True)
    out=[];anel(out,pos,vista,0,min(10,(t-C)*1.4+1),0xF4E8FF,esp,32);submit(add,15,out,t,True)
   if rel>.01:
    alc=info['relampago']
    for largura,ganho in [(.6,.4),(.18,1.)]:
     out=[]
     for cam_ in data['caminhosRelampago']:
      for i in range(1,len(cam_)):
       if i>math.ceil(alc):break
       a=pos+vec(cam_[i-1]);b=pos+vec(cam_[i]);b=a+(b-a)*min(1,alc-(i-1));ribbon(out,a,b,largura,rel*ganho,CIANO)
     submit(add,15,out,t,True)
   out=[]
   for p,a in info['faiscas']:
    if fade*a>.01:anel(out,pos+vec(p),vista,0,.26,0xEAF8FF,fade*a,10)
   submit(add,15,out,t,True)
   out=[]
   for i,(p,a) in enumerate(info['estilhacos']):
    if fade*a>.01:tam=.12+(i%4)*.05;box(out,pos+vec(p),tam,tam*.25,tam*.7,(math.radians(t*7+i*41),math.radians(t*5+i*29)),0xE6F2FF,fade*a)
   submit(base,8,out,t)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im)
  d.text((14,12),f'FRIEREN / BURACO NEGRO / {fase(t)} | tick {t} ({t/20:.2f}s)',fill=(255,226,196))
  d.text((14,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD / SEM TERRENO, IMPACT FRAMES NEM PEDRAS ORBITANDO',fill=(160,172,196))
  return im
 def shot(t):
  # Over-shoulder on the hand while it forms, then follows the flight to a wide view (same beats as the cutscene).
  close_cam=caster+np.array([2.2,2.4,-3.4]);close_look=mao
  wide_cam=centro+np.array([13.,5.,-22.]);wide_look=centro+np.array([0,-.5,0])
  k=smooth((t-L)/24)
  return close_cam*(1-k)+wide_cam*k,close_look*(1-k)+wide_look*k
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 video=output/'frieren-buraco-negro.mp4'
 with av.open(str(video),'w') as dest:
  try:vs=dest.add_stream('libx264',rate=20);vs.options={'crf':'18'}
  except Exception:vs=dest.add_stream('mpeg4',rate=20);vs.bit_rate=8_000_000
  vs.width,vs.height,vs.pix_fmt=w,h,'yuv420p'
  as_=None
  if audio:as_=dest.add_stream('aac',rate=48000);as_.layout='mono';as_.bit_rate=128000
  for t in range(0,F+20):
   frame=av.VideoFrame.from_image(draw(min(t,F),*shot(min(t,F))))
   for p in vs.encode(frame):dest.mux(p)
  for p in vs.encode():dest.mux(p)
  if as_ is not None:
   total=np.zeros(int((F+20)/20*48000),dtype=np.float32)
   sons=ROOT/'src/main/resources/assets/arcanecontrol/sounds/anime'
   for nome,tick,ganho in [('frieren_buraco_carga',0,.7),('frieren_buraco_atracao',A,.75),('frieren_buraco_colapso',C,.9)]:
    res=av.AudioResampler(format='flt',layout='mono',rate=48000);pcm=[]
    with av.open(str(sons/f'{nome}.ogg')) as src:
     for fr in src.decode(audio=0):
      for g in res.resample(fr):pcm.append(g.to_ndarray().reshape(-1))
    pcm=np.concatenate(pcm)*ganho;ini=int(tick/20*48000);fim=min(len(total),ini+len(pcm));total[ini:fim]+=pcm[:fim-ini]
   total=np.clip(total,-1,1);pts=0
   for k in range(0,len(total),1024):
    chunk=total[k:k+1024].reshape(1,-1);fr=av.AudioFrame.from_ndarray(chunk,format='flt',layout='mono');fr.sample_rate=48000;fr.pts=pts;pts+=chunk.shape[1]
    for p in as_.encode(fr):dest.mux(p)
   for p in as_.encode():dest.mux(p)
 cols,rows=4,2;tw,th=w//2,h//2;sheet=Image.new('RGB',(tw*cols,th*rows),(8,10,16))
 for i,t in enumerate([30,58,66,120,212,222,236,256]):sheet.paste(draw(t,*shot(t)).resize((tw,th),Image.Resampling.LANCZOS),(i%cols*tw,i//cols*th))
 sheet.save(output/'frieren-buraco-negro-fases.png')
 print('Black Hole GPU:',ctx.info['GL_RENDERER'],'video:',video)
 for res_ in [base,add,line,fbo]:res_.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--sem-audio',action='store_true');a=p.parse_args();render(a.output,audio=not a.sem_audio)
