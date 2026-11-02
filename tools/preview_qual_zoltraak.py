"""Offline Qual Zoltraak approval: mirrors RenderBossVfx.zoltraakQual (telegraph 403 and beam 413), runtime GLSL. Not Minecraft."""
from pathlib import Path
import math,argparse
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import lookat

VIOLETA,PONTO=0x7A3FB8,0xE6DAFF
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def unit(v):n=np.linalg.norm(v);return v/n if n>1e-9 else v
def basis(n):
 u=np.cross(n,[0,1,0])
 if np.linalg.norm(u)<1e-4:u=np.array([1.,0,0])
 u=unit(u);return u,unit(np.cross(n,u))

def render(output):
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 w,h=960,540;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 f=1/math.tan(math.radians(70)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tint;out vec4 fragColor;void main(){fragColor=vec4(Tint,1);}")
 cam=np.array([11.,4.5,4.]);view=lookat(cam,np.array([0,2.8,6.5]))
 def submit(prog,mode,verts,t,additive=True):
  if not verts:return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  prog['ModelViewMat'].write(view.T.astype('f4').tobytes());prog['ProjMat'].write(proj.T.tobytes());prog['EffectTime'].value=t/20;prog['EffectMode'].value=mode;prog['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(prog,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def quad(out,pts,uvs,c,a):
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*rgb(c),a,*uvs[k]])
 def ribbon(out,a,b,wd,al,c,u1=1):
  side=np.cross(b-a,cam-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=wd/n;quad(out,[a-side,b-side,b+side,a+side],[(0,0),(u1,0),(u1,1),(0,1)],c,al)
 def disc(out,c,r,col,al,lados=8):
  u,v=basis(unit(cam-c))
  for i in range(lados):
   a0,a1=i*math.tau/lados,(i+1)*math.tau/lados;quad(out,[c,c,c+(u*math.cos(a1)+v*math.sin(a1))*r,c+(u*math.cos(a0)+v*math.sin(a0))*r],[(i*.3,0),((i+1)*.3,0),((i+1)*.3,1),(i*.3,1)],col,al)
 def ring(out,c,n,r0,r1,col,al,lados=48):
  u,v=basis(n)
  for i in range(lados):
   a0,a1=i*math.tau/lados,(i+1)*math.tau/lados;p=u*math.cos(a0)+v*math.sin(a0);q=u*math.cos(a1)+v*math.sin(a1)
   quad(out,[c+p*r0,c+q*r0,c+q*r1,c+p*r1],[(i*.3,0),((i+1)*.3,0),((i+1)*.3,1),(i*.3,1)],col,al)
 qual=np.zeros(3);alvo=np.array([0,0,14.]);olho=qual+[0,4.4,0]
 def frame(t,beam):
  fbo.clear(.11,.12,.16,1,depth=1);g=[]
  for i in range(-20,21,2):g.extend([[-20,0,i],[20,0,i+14],[i,0,-6],[i,0,34]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes());line['Tint'].value=(.2,.22,.28)
  bx=[];x,z=0,0
  for dx,dz in [(-1.05,-1.05),(1.05,-1.05),(1.05,1.05),(-1.05,1.05)]:bx.extend([[x+dx,0,z+dz],[x+dx,5,z+dz]])
  for yy in (0,5):bx.extend([[-1.05,yy,-1.05],[1.05,yy,-1.05],[1.05,yy,-1.05],[1.05,yy,1.05],[1.05,yy,1.05],[-1.05,yy,1.05],[-1.05,yy,1.05],[-1.05,yy,-1.05]])
  b=ctx.buffer(np.asarray(g+bx,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  out=[]
  if not beam:
   carga=min(1,t/30);d=unit(np.array([alvo[0],0,alvo[2]]));foco=qual+[0,4.4,0];u,vv=basis(d)
   for anel,(r,n) in enumerate([(1.0,12),(1.6,18)]):
    r*=.5+.5*carga
    for i in range(n):a=i*math.tau/n+(t*.05 if anel==0 else -t*.035);disc(out,foco+(u*math.cos(a)+vv*math.sin(a))*r,.09,PONTO,.4+.6*carga)
   r=1.8*(1-t/30);ring(out,alvo+[0,.08,0],np.array([0,1.,0]),r,r+.06,0xFFFFFF,1);ring(out,alvo+[0,.06,0],np.array([0,1.,0]),1.74,1.8,VIOLETA,1)
   submit(add,15,out,t);label=f'QUAL / CARGA DO ZOLTRAAK | tick {t}'
  else:
   fim=alvo+[0,1,0];lw=max(0,1-t/20);d=unit(fim-olho)
   o=[];ribbon(o,olho,fim,1.1*lw,.45,VIOLETA,8);submit(add,1,o,t)
   o=[];ribbon(o,olho,fim,.6*lw,1,0x2A1640,8);submit(base,21,o,t,False)
   u,vv=basis(d)
   for r,n in [(.9,12),(1.4,18)]:
    for i in range(n):a=i*math.tau/n+t*.08;disc(out,olho+(u*math.cos(a)+vv*math.sin(a))*r,.08,PONTO,lw)
   submit(add,15,out,t);label=f'QUAL / ZOLTRAAK ORIGINAL (PRETO) | tick {t}'
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');dr=ImageDraw.Draw(im)
  dr.text((14,12),label,fill=(230,222,255));dr.text((14,h-22),'PREVIA OFFLINE / GLSL DO MOD / CAIXA = HITBOX DA QUAL (2,1 x 5 BLOCOS)',fill=(160,172,196));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 tw,th=w//2,h//2;sheet=Image.new('RGB',(tw*2,th*2))
 for i,im in enumerate([frame(8,False),frame(26,False),frame(2,True),frame(10,True)]):sheet.paste(im.resize((tw,th),Image.Resampling.LANCZOS),(i%2*tw,i//2*th))
 sheet.save(output/'qual-zoltraak-fases.png');print('Qual Zoltraak GPU:',ctx.info['GL_RENDERER'])
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);render(p.parse_args().output)
