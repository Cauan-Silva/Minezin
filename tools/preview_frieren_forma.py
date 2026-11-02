"""Offline Frieren awakened-form approval: runtime GLSL and Java-exported geometry, no Minecraft.
Mirrors RenderDespertarFrieren; references: mana release (EP10), Judradjim (EP25), defensive panels (EP3)."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,lookat

MANA,MANA_CLARO,VIOLETA,FAVO,VIDRO,TRACO=0xBFD9FF,0xE4F0FF,0xC56BFF,0x7FF3FF,0x5FD8FF,0xE8FBFF
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def vec(p):return np.array([p['x'],p['y'],p['z']],dtype=float)
def unit(v):n=np.linalg.norm(v);return v/n if n>1e-9 else v
def basis(normal):
 u=np.cross(normal,[0,1,0])
 if np.linalg.norm(u)<1e-4:u=np.array([1.,0,0])
 u=unit(u);return u,unit(np.cross(normal,u))
def smooth(x):x=max(0,min(1,x));return x*x*(3-2*x)

def render(output,width=960,height=540):
 data=json.loads((ROOT/'build/frieren-forma-samples.json').read_text(encoding='utf-8'))
 flor=json.loads((ROOT/'build/frieren-base-samples.json').read_text(encoding='utf-8'))
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 w,h=width,height;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 st={}
 def submit(mode,verts,t):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE)
  add['ModelViewMat'].write(st['view'].T.astype('f4').tobytes());add['ProjMat'].write(proj.T.tobytes());add['EffectTime'].value=t/20;add['EffectMode'].value=mode;add['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(add,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(out,pts,uvs,color,alpha):
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*rgb(color),alpha,*uvs[k]])
 def ribbon(out,a,b,width,alpha,color):
  side=np.cross(b-a,st['cam']-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;quad(out,[a-side,b-side,b+side,a+side],[(0,0),(1,0),(1,1),(0,1)],color,alpha)
 def anel(out,c,normal,i_,e_,color,alpha,lados):
  u,v=basis(normal)
  for i in range(lados):
   a,b=i*math.tau/lados,(i+1)*math.tau/lados;p=u*math.cos(a)+v*math.sin(a);q=u*math.cos(b)+v*math.sin(b)
   quad(out,[c+p*i_,c+q*i_,c+q*e_,c+p*e_],[(i*.3,0),((i+1)*.3,0),((i+1)*.3,1),(i*.3,1)],color,alpha)
 def circulo(out,c,normal,raio,giro,alpha):
  b=basis(normal);tr=max(.008,raio*.014)
  def pl(x,y):cs,sn=math.cos(giro),math.sin(giro);return c+b[0]*(x*cs-y*sn)*raio+b[1]*(x*sn+y*cs)*raio
  anel(out,c,normal,raio*.97,raio,TRACO,alpha,64);anel(out,c,normal,raio*.86,raio*.885,TRACO,alpha*.9,64);anel(out,c,normal,raio*.8,raio*.815,TRACO,alpha*.8,64)
  for k in range(6):
   pts=[pl(q['x'],q['y']) for q in flor['petalas'][k]]
   for i in range(1,len(pts)):ribbon(out,pts[i-1],pts[i],tr,alpha,TRACO)
   o=pl(flor['olhos'][k]['x'],flor['olhos'][k]['y']);anel(out,o,normal,raio*.085,raio*.1,TRACO,alpha,24)
 def lines(points,tint):
  line['Matrix'].write((proj@st['view']).T.astype('f4').tobytes());line['Tint'].value=tint
  b=ctx.buffer(np.asarray(points,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
 def scene(cam,look,casters=((0,0,0),)):
  st['cam']=np.asarray(cam,float);st['view']=lookat(st['cam'],np.asarray(look,float))
  fbo.depth_mask=True;fbo.clear(.035,.045,.075,1,depth=1);g=[]
  for i in range(-30,31,2):g.extend([[-30,0,i],[30,0,i],[i,0,-30],[i,0,30]])
  lines(g,(.12,.14,.2))
  for x,y,z in casters:
   bx=[]
   for dx,dz in [(-.3,-.3),(.3,-.3),(.3,.3),(-.3,.3)]:bx.extend([[x+dx,y,z+dz],[x+dx,y+1.8,z+dz]])
   for yy in (y,y+1.8):bx.extend([[x-.3,yy,z-.3],[x+.3,yy,z-.3],[x+.3,yy,z-.3],[x+.3,yy,z+.3],[x+.3,yy,z+.3],[x-.3,yy,z+.3],[x-.3,yy,z+.3],[x-.3,yy,z-.3]])
   lines(bx,(.6,.66,.8))
 def finish(title):
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im)
  d.text((14,12),title,fill=(220,236,255));d.text((14,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD / SEM TERRENO, SKIN NEM ANIMACAO',fill=(160,172,196));return im
 corpo=np.array([0,1.6,0])
 def mana(idade):
  info=data['mana'][str(idade)];scene((7,4.5,-11),(0,6,0),((0,1.6,0),));r,alt,a=info['raio'],info['altura'],info['alpha']
  out=[];ribbon(out,corpo+[0,-.2,0],corpo+[0,alt,0],r,a*.8,MANA);ribbon(out,corpo+[0,-.2,0],corpo+[0,alt*.8,0],r*.5,a*.6,MANA_CLARO);submit(19,out,idade)
  out=[]
  for ang,ea in info['estrias']:b0=corpo+[math.cos(ang)*r*.9,0,math.sin(ang)*r*.9];ribbon(out,b0,b0+[0,alt*.85,0],.05,a*ea*.6,MANA_CLARO)
  for p,pa in info['particulas']:
   if a*pa>.01:q=corpo+vec(p);anel(out,q,unit(st['cam']-q),0,.09,MANA_CLARO,a*pa,8)
  submit(15,out,idade);return finish(f'FRIEREN / LIBERAR MANA | idade {idade}')
 alvo=np.array([0,1.6,15.])
 def vinte(t):
  scene((-7,4,-5),(0,2,7),((0,1.6,0),(0,0,15)))
  out1=[];out15=[]
  for k in range(0,t+1,2):
   idade=t-k
   if idade>=12:continue
   o=np.array([0,1.6+1.62,0])+[math.cos(k*1.3)*1.4,math.sin(k*.8)*.7+.5,math.sin(k*1.3)*1.4];lw=1-smooth(idade/12);d=unit(alvo+[0,1.62,0]-o);fim=alvo+[0,1.62,0]
   ribbon(out1,o,fim,.6*lw,.5*lw,0xC9D4FF);ribbon(out1,o,fim,.2*lw,min(1,lw*1.5),0xFFFFFF);circulo(out15,o+d*.3,d,.5,idade*.03,lw)
  submit(1,out1,t);submit(15,out15,t);return finish(f'FRIEREN / VINTE ZOLTRAAK | tick {t}')
 def julg(t):
  info=data['julgamento'][str(t)];scene((14,9,-18),(0,10,0),((0,0,0),(-2,0,-10)));q=data['queda']
  out=[]
  if info['aviso']>.01:
   a=info['aviso'];circulo(out,np.array([0,.08,0]),np.array([0,1.,0]),4,t*.02,a*.85);anel(out,np.array([0,.1,0]),np.array([0,1.,0]),4*info['fecha'],4*info['fecha']+.12,0xFFFFFF,a,64)
   circulo(out,np.array([0,30.,0]),np.array([0,1.,0]),3*(.6+.4*info['carga']),-t*.04,a*info['carga'])
  submit(15,out,t)
  ar=info['alphaRaio']
  if ar>.01:
   for largura,cor,g in [(.75,VIOLETA,.45),(.16,0xFFFFFF,1.)]:
    out=[]
    for j,pts in enumerate(info['raios']):
     for i in range(1,len(pts)):ribbon(out,vec(pts[i-1]),vec(pts[i]),largura*(1.4 if j==0 else 1),ar*g,cor)
    alc=info['alcance']
    for pts in info['ramos']:
     for i in range(1,len(pts)):
      if i>math.ceil(alc):break
      p0=vec(pts[i-1]);p1=vec(pts[i]);p1=p0+(p1-p0)*min(1,alc-(i-1));ribbon(out,p0,p1,largura*.6,ar*g,cor)
    submit(15,out,t)
   if info['clarao']>.01:out=[];anel(out,np.array([0,.6,0]),unit(st['cam']),0,5*(1.2-info['clarao']),0xF2DDFF,info['clarao'],32);submit(15,out,t)
  return finish(f'FRIEREN / JULGAMENTO (JUDRADJIM) | tick {t}')
 def escudo(t):
  info=data['escudo'][str(t)];scene((11,6,-14),(0,3,0),((0,0,0),(2,0,1),(-2,0,-1)));r0=data['painel']
  for borda in (False,True):
   out=[]
   for painel,(esc,luz) in zip(data['paineis'],info):
    if esc<.01:continue
    c=vec(painel['centro']);n=vec(painel['normal']);b=basis(n);rr=r0*esc;l=luz*esc
    for k in range(6):
     a0,a1=k*math.pi/3,(k+1)*math.pi/3;p0=c+b[0]*math.cos(a0)*rr+b[1]*math.sin(a0)*rr;p1=c+b[0]*math.cos(a1)*rr+b[1]*math.sin(a1)*rr
     if borda:ribbon(out,p0,p1,.045,l,FAVO)
     else:quad(out,[c,p0,p1,p1],[(0,0),(1,0),(1,1),(0,1)],VIDRO,.1*l)
   submit(15 if borda else 8,out,t)
  return finish(f'FRIEREN / ESCUDO CELESTIAL | tick {t}')
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 frames=[mana(i) for i in range(0,81)]+[vinte(t) for t in range(0,40)]+[julg(t) for t in range(0,41)]+[escudo(t) for t in range(0,121,2)]
 video=output/'frieren-forma-desperta.mp4'
 with av.open(str(video),'w') as dest:
  try:vs=dest.add_stream('libx264',rate=20);vs.options={'crf':'18'}
  except Exception:vs=dest.add_stream('mpeg4',rate=20);vs.bit_rate=8_000_000
  vs.width,vs.height,vs.pix_fmt=w,h,'yuv420p'
  for im in frames:
   for p in vs.encode(av.VideoFrame.from_image(im)):dest.mux(p)
  for p in vs.encode():dest.mux(p)
 tw,th=w//2,h//2;sheet=Image.new('RGB',(tw*3,th*2),(8,10,16))
 for i,im in enumerate([mana(8),mana(70),vinte(20),julg(14),julg(27),escudo(40)]):sheet.paste(im.resize((tw,th),Image.Resampling.LANCZOS),(i%3*tw,i//3*th))
 sheet.save(output/'frieren-forma-desperta-fases.png');print('Form GPU:',ctx.info['GL_RENDERER'],'video:',video)
 for r in [base,add,line,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
