"""Offline Domain approval: runtime GLSL and Java-exported geometry, no Minecraft."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_armor_models import ROOT,lookat,translation

def render(output):
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'gojo_energy');add=load_program(ctx,'gojo_energy_additive')
 data=json.loads((ROOT/'build/gojo-dominio-samples.json').read_text(encoding='utf-8'))
 w,h=720,480;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 center=np.array([0.,0.,0.]);cam=np.array([0.,1.7,-15.]);view=lookat(cam,np.array([0.,7.,0.]))
 f=1/math.tan(math.radians(46)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nuniform vec3 Tone;out vec4 fragColor;void main(){fragColor=vec4(Tone,1);}")
 def submit(program,mode,verts,t,additive=False,local=False,position=None):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  program['ModelViewMat'].write((view@translation(center if position is None else position) if local else view).T.astype('f4').tobytes());program['ProjMat'].write(proj.T.tobytes());program['EffectTime'].value=t/20;program['EffectMode'].value=mode
  viewer=cam-(position if local and position is not None else center);length=np.linalg.norm(viewer);viewer=viewer/length if length>1e-8 else np.array([0.,0.,1.]);program['Viewer'].value=tuple(viewer)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def ribbon(out,a,b,width,alpha,color,u0,u1):
  side=np.cross(b-a,cam-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;pts=[a-side,b-side,b+side,a+side];uv=[(u0,0),(u1,0),(u1,1),(u0,1)]
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*color,alpha,*uv[k]])
 def point(p):return center+np.array([p['x'],p['y'],p['z']])
 def orb(position,r,mode,alpha,t,color=(1.,1.,1.),glow=False):
  if r<.01 or alpha<.004:return
  dense=mode==15 and alpha>.95
  fbo.depth_mask=dense
  if dense:ctx.enable(moderngl.CULL_FACE)
  submit(add if glow else base,mode,mesh(r,32,(*color,alpha)),t,glow,True,position)
  fbo.depth_mask=False;ctx.disable(moderngl.CULL_FACE)
 def draw(t,exterior=False):
  nonlocal cam,view
  cam=np.array([44.,18.,-48.]) if exterior else np.array([0.,1.7,-15.]);target=np.array([0.,4.,0.]) if exterior else np.array([0.,7.,0.]);view=lookat(cam,target)
  info=data[str(t)];alpha=info['alpha'];radius=info['raio'];presence=0 if exterior else info['presenca'];interior=np.linalg.norm(cam)<radius-.5
  fbo.depth_mask=True;fbo.clear(.12,.18,.25,1,depth=1)
  if presence>.001:orb(cam,100,14,presence,t)
  fbo.depth_mask=True;grid=[]
  for i in range(-60,61,4):grid.extend([[-60,0,i],[60,0,i],[i,0,-60],[i,0,60]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes())
  line['Tone'].value=(.07,.11,.14) if exterior else (.006,.009,.018)
  plane=np.array([[-100,-.04,-100],[100,-.04,-100],[100,-.04,100],[-100,-.04,-100],[100,-.04,100],[-100,-.04,100]],dtype='f4');b=ctx.buffer(plane.tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.TRIANGLES);v.release();b.release()
  line['Tone'].value=(.13,.16,.23)
  b=ctx.buffer(np.asarray(grid,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release();fbo.depth_mask=False
  orb(center,radius,14 if interior else 15,alpha,t)
  if interior and presence>.01:
   out=[]
   for j in range(8):
    for i in range(64):
     points=[]
     for r,a in [(j/8,i*math.tau/64),((j+1)/8,i*math.tau/64),((j+1)/8,(i+1)*math.tau/64),(j/8,(i+1)*math.tau/64)]:
      x,z=math.cos(a)*r,math.sin(a)*r;u=max(0,min(1,(r-.7)/.3));fade=1-u*u*(3-2*u);points.append([x*radius,.035,z*radius,1.,1.,1.,presence*.95*fade,x*.5+.5,z*.5+.5])
     out.extend([points[k] for k in [0,1,2,0,2,3]])
   submit(base,18,out,t)
   if t>=20:
    strength=alpha*presence
    out=[]
    for j,points in enumerate(info['fluxos']):
     for i in range(1,len(points)):
      steps=info['passos'];u=i/steps;rgb=info['cores'][j][i];color=tuple(((rgb>>shift)&255)/255 for shift in [16,8,0]);ribbon(out,point(points[i-1]),point(points[i]),.28,strength*info['pulsos'][j][i]*.38,color,(i-1)/steps,u);ribbon(out,point(points[i-1]),point(points[i]),.1,strength*info['pulsos'][j][i],color,(i-1)/steps,u)
    submit(add,17,out,t,True)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im);d.text((12,12),'GOJO / VAZIO INFINITO / '+('EXTERIOR' if exterior else 'INTERIOR')+' | tick '+str(t),fill=(235,240,255));d.text((12,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD',fill=(165,182,207));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 frames=[draw(t) for t in range(24,145,2)]
 frames[0].save(output/'gojo-dominio-linhas-intensas.gif',save_all=True,append_images=frames[1:],duration=100,loop=0)
 sheet=Image.new('RGB',(w*2,h),(9,12,20));sheet.paste(draw(80,True),(0,0));sheet.paste(draw(80),(w,0));sheet.save(output/'gojo-dominio-intenso-exterior-interior.png')
 sheet=Image.new('RGB',(w*2,h*2),(9,12,20))
 for i,t in enumerate([24,40,120,392]):sheet.paste(draw(t),(i%2*w,i//2*h))
 sheet.save(output/'gojo-dominio-intenso-fases.png')
 first=draw(80);second=draw(120);delta=np.abs(np.asarray(first,dtype='f4')[40:h-30]-np.asarray(second,dtype='f4')[40:h-30]).mean();print('Domain motion: mean RGB change over 2 seconds:',round(float(delta),2));assert delta>4,'Fog motion is too subtle'
 motion=Image.new('RGB',(w*2,h),(9,12,20));motion.paste(first,(0,0));motion.paste(second,(w,0));motion.save(output/'gojo-dominio-intenso-deslocamento.png');print('Domain GPU:',ctx.info['GL_RENDERER'],'frames:',len(frames))
 for r in [base,add,line,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
