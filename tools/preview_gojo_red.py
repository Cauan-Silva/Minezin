"""Offline Red approval: runtime GLSL and Java-exported geometry, no Minecraft."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh
from preview_armor_models import ROOT,lookat,translation

def render(output):
 ctx=moderngl.create_standalone_context(require=320);base=load_program(ctx,'gojo_energy');add=load_program(ctx,'gojo_energy_additive')
 data=json.loads((ROOT/'build/gojo-vermelho-samples.json').read_text(encoding='utf-8'))
 w,h=720,480;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 center=np.array([0.,1.65,1.8]);cam=center+np.array([15.,8.,-14.]);view=lookat(cam,center+np.array([0.,0.,8.]))
 f=1/math.tan(math.radians(46)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-.2002],[0,0,-1,0]],dtype='f4')
 line=ctx.program(vertex_shader="#version 150\nin vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}",fragment_shader="#version 150\nout vec4 fragColor;void main(){fragColor=vec4(.13,.16,.23,1);}")
 def submit(program,mode,verts,t,additive=False,local=False):
  if not len(verts):return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  program['ModelViewMat'].write((view@translation(center) if local else view).T.astype('f4').tobytes());program['ProjMat'].write(proj.T.tobytes());program['EffectTime'].value=t/20;program['EffectMode'].value=mode
  viewer=cam-center;viewer/=np.linalg.norm(viewer);program['Viewer'].value=tuple(viewer)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def ribbon(out,a,b,width,alpha,color,u0,u1):
  side=np.cross(b-a,cam-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side*=width/n;pts=[a-side,b-side,b+side,a+side];uv=[(u0,0),(u1,0),(u1,1),(u0,1)]
  for k in [0,1,2,0,2,3]:out.append([*pts[k],*color,alpha,*uv[k]])
 def point(p):return center+np.array([-p['lateral'],p['vertical'],p['frente']])
 def draw(t,close=False):
  nonlocal cam,view
  cam=center+np.array([1.5,.7,-3.]) if close else center+np.array([15.,8.,-14.]);view=lookat(cam,center if close else center+np.array([0.,0.,8.]))
  info=data[str(t)];alpha=info['alpha'];radius=info['raio'];fbo.depth_mask=True;fbo.clear(.028,.035,.06,1,depth=1)
  grid=[]
  for i in range(-12,33,2):grid.extend([[-12,0,i],[32,0,i],[i,0,-12],[i,0,32]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes());b=ctx.buffer(np.asarray(grid,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release()
  dense=alpha>.95
  if dense:ctx.enable(moderngl.CULL_FACE)
  fbo.depth_mask=dense;submit(base,12,mesh(radius,22,(1.,.145,.239,alpha)),t,local=True);ctx.disable(moderngl.CULL_FACE);fbo.depth_mask=False
  submit(add,6,mesh(radius*2,22,(1.,.145,.239,alpha*.5)),t,True,True)
  out=[]
  if t<13:
   for points in info['carga']:
    for i in range(1,len(points)):
     u=i/32;ribbon(out,point(points[i-1]),point(points[i]),.035,alpha*.8*math.sin(u*math.pi),(1.,.145,.239),(i-1)/32,u)
  else:
   impacto=info['impacto']
   for points in info['descarga']:
    for i in range(1,len(points)):
     u=i/24;luz=alpha*(.3+.7*impacto)*(1-u*.5)
     ribbon(out,point(points[i-1]),point(points[i]),.28,luz*.22,(1.,.145,.239),0,u)
     ribbon(out,point(points[i-1]),point(points[i]),.085+impacto*.065,luz,(1.,.318,.38),0,u)
   for points in info['ondas']:
    for i in range(1,len(points)):
     u=i/64;ribbon(out,point(points[i-1]),point(points[i]),1.+impacto*.35,alpha*.18,(1.,.145,.239),(i-1)/64,u)
     ribbon(out,point(points[i-1]),point(points[i]),.16+impacto*.2,alpha*(.4+.6*impacto),(1.,.475,.506),(i-1)/64,u)
  submit(add,13,out,t,True)
  im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im);d.text((12,12),'GOJO / VERMELHO / REPULSAO | tick '+str(t),fill=(235,240,255));d.text((12,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD',fill=(165,182,207));return im
 output=Path(output);output.mkdir(parents=True,exist_ok=True)
 def composition(t):
  image=draw(t);inset=draw(t,True).resize((240,160),Image.Resampling.LANCZOS);image.paste(inset,(w-246,32));d=ImageDraw.Draw(image);d.rectangle((w-247,31,w-6,192),outline=(120,80,90));d.text((w-242,199),'DETALHE DO FOCO',fill=(220,205,215));return image
 frames=[composition(t) for t in range(1,28)]
 frames[0].save(output/'gojo-vermelho-repulsao.gif',save_all=True,append_images=frames[1:],duration=50,loop=0)
 sheet=Image.new('RGB',(w*2,h*2),(9,12,20))
 for i,t in enumerate([8,12,13,20]):sheet.paste(composition(t),(i%2*w,i//2*h))
 sheet.save(output/'gojo-vermelho-fases.png');print('Red GPU:',ctx.info['GL_RENDERER'],'frames:',len(frames))
 for r in [base,add,line,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);a=p.parse_args();render(a.output)
