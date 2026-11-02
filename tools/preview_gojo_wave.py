"""Gojo approval images: runtime sphere shaders and Java-exported arc/lightning positions. Offline, not Minecraft."""
from pathlib import Path
import argparse,json,math,subprocess
import numpy as np,moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh as sphere
from preview_armor_models import ROOT,ASSETS,lookat,translation


def render(output,comparison,backend=None):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}));base=load_program(ctx,'gojo_energy');add=load_program(ctx,'gojo_energy_additive')
 before=subprocess.run(['git','show','origin/main:src/main/resources/assets/arcanecontrol/shaders/core/gojo_energy.fsh'],cwd=ROOT,check=True,stdout=subprocess.PIPE).stdout.decode('utf-8')
 vs=(ASSETS/'shaders/core/gojo_energy.vsh').read_text(encoding='utf-8');old=ctx.program(vertex_shader=vs,fragment_shader=before)
 line=ctx.program(vertex_shader="""#version 150
 in vec3 Position;uniform mat4 Matrix;void main(){gl_Position=Matrix*vec4(Position,1);}""",fragment_shader="""#version 150
 out vec4 fragColor;void main(){fragColor=vec4(.13,.16,.23,1);}""")
 samples=json.loads((ROOT/'build/gojo-roxo-samples.json').read_text());w,h=640,440;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 colors={'blue':(.145,.616,1.),'red':(1.,.145,.239),'purple':(.702,.349,1.),'white':(.949,.87,1.)}
 def submit(program,mode,verts,view,proj,time,viewer,additive):
  if len(verts)==0:return
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  program['ModelViewMat'].write(view.T.astype('f4').tobytes());program['ProjMat'].write(proj.T.astype('f4').tobytes());program['EffectTime'].value=time/20;program['EffectMode'].value=mode;program['Viewer'].value=tuple(viewer)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def ribbon(out,a,b,width,color,alpha,u0,u1,cam):
  a=np.asarray(a);b=np.asarray(b);side=np.cross(b-a,cam-a);n=np.linalg.norm(side)
  if n<1e-8:return
  side=side/n*width;points=[a-side,b-side,b+side,a+side];uv=[(u0,0),(u1,0),(u1,1),(u0,1)]
  for k in [0,1,2,0,2,3]:out.append([*points[k],*color,alpha,*uv[k]])
 def point(p,center):return center+np.array([-p['lateral'],p['vertical'],p['frente']])
 def draw(t,new=True,close=False):
  info=samples[str(t)];alpha=info['alpha'];center=np.array([0.,1.62,3.2 if t<=30 else info['distancia']]);cam=center+np.array([0.,.5,-14.]) if close else center+np.array([10.,5.,-18.]);view=lookat(cam,center);f=1/math.tan(math.radians(46)/2);near,far=.1,180;proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-(far+near)/(far-near),-2*far*near/(far-near)],[0,0,-1,0]],dtype='f4')
  fbo.depth_mask=True;fbo.clear(.028,.035,.06,1,depth=1);ctx.disable(moderngl.BLEND)
  grid=[]
  for i in range(-18,53,2):grid.extend([[-18,0,i],[52,0,i],[i,0,-18],[i,0,52]])
  line['Matrix'].write((proj@view).T.astype('f4').tobytes());b=ctx.buffer(np.array(grid,dtype='f4').tobytes());v=ctx.vertex_array(line,[(b,'3f','Position')]);v.render(moderngl.LINES);v.release();b.release();ctx.enable(moderngl.BLEND);fbo.depth_mask=False
  material=base if new else old
  def orb(c,r,mode,color,a,glow=False):
   if r<.01:return
   dense=new and mode==2 and a>.95 and not glow
   if dense:fbo.depth_mask=True;ctx.enable(moderngl.CULL_FACE)
   localcam=cam-c;localcam=localcam/np.linalg.norm(localcam);submit(add if glow and new else old if not new else material,mode,sphere(r,detail=22,color=(*color,a)),view@translation(c),proj,t,localcam,glow)
   if dense:fbo.depth_mask=False;ctx.disable(moderngl.CULL_FACE)
  if t<=30:
   d=info['separacao'];blue=center+np.array([d,0,0]);red=center-np.array([d,0,0]);orb(blue,.6,0,colors['blue'],alpha);orb(red,.48,1,colors['red'],alpha);orb(blue,1,6,colors['blue'],alpha*.4,True);orb(red,.85,6,colors['red'],alpha*.4,True)
   out=[]
   for j,points in enumerate(info['pontes']):
    for i in range(1,len(points)):ribbon(out,point(points[i-1],center),point(points[i],center),.06,colors['blue' if j%2==0 else 'red'],alpha*.7,i/20*3,(i/20+.05)*3,cam)
   submit(add if new else old,5,out,view,proj,t,(0,0,1),True)
   if t>21:orb(center,info['raio'],2,colors['purple'],alpha*info['raio']/1.6)
  else:
   orb(center,3.6,2,colors['purple'],alpha);orb(center,4.5,6,colors['purple'],alpha*.5,True)
   out=[]
   for points in info['rastros']:
    for i in range(1,len(points)):
     u=i/24;ribbon(out,point(points[i-1],center),point(points[i],center),.14*(1-u*.7),colors['purple'],alpha*(1-u),u*5,(u+1/24)*5,cam)
   submit(add if new else old,5,out,view,proj,t,(0,0,1),True)
  if new and info['raio']>.05:
   out=[]
   for j,points in enumerate(info['coroas']):
    for i in range(1,len(points)):ribbon(out,point(points[i-1],center),point(points[i],center),info['raio']*.055,colors['white'] if j==1 else colors['purple'],alpha*.75*(info['raio']/1.6 if t<=30 else 1),(i-1)/48,i/48,cam)
   submit(add,9,out,view,proj,t,(0,0,1),True)
  if t>12:
   out=[]
   for j,points in enumerate(info['raios']):
    if not new:
     points=[]
     for k in range(9):
      u=k/8;a=j*2.399963+math.sin(j*1.71)*.28+t*.008;r=3.9+u*(2.7+(math.sin(j*3)*.5+.5)*1.5);dev=math.sin(j*3+k*2.1+math.floor(t/3))*.6*math.sin(u*math.pi)
      points.append({'lateral':math.cos(a)*r-math.sin(a)*dev,'vertical':math.sin(a)*r+math.cos(a)*dev,'frente':math.sin(j*1.7+k*.9)*u*1.2})
    pulse=(.65+.35*max(0,math.sin(t*.22+j*1.7))**4)*alpha
    for i in range(1,len(points)):
     a=point(points[i-1],center);b=point(points[i],center);ribbon(out,a,b,.24,colors['purple'],pulse*.55,0,1,cam);ribbon(out,a,b,.075,colors['white'],pulse,0,1,cam)
    for i in [3,5]:
     a=point(points[i],center);b=point(info['ramos'][j][0 if i==3 else 1],center) if new else a+np.array([-math.sin(j*2)*1.7,1.3,0]);ribbon(out,a,b,.065,(.92,.824,1),pulse,0,1,cam)
   submit(add if new else old,8,out,view,proj,t,(0,0,1),True)
  image=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');return image
 labels=['AZUL + VERMELHO','CONVERGENCIA / TENSAO','FUSAO','ROXO FORMADO','DISPARO','RASTRO / DISSIPACAO']
 sheet=Image.new('RGB',(w*3,h*2+58),(9,12,20));d=ImageDraw.Draw(sheet)
 for i,(t,label) in enumerate(zip([8,18,26,29,34,48],labels)):
  tile=draw(t);sheet.paste(tile,(i%3*w,i//3*h));d.text((i%3*w+12,i//3*h+12),label+f' | tick {t}',fill=(238,239,250))
 d.text((12,h*2+12),'GOJO / PRIMEIRA DIRECAO VISUAL PARA APROVACAO. GEOMETRIA E GLSL DO MOD EM CENA OFFLINE.',fill=(230,233,245));d.text((12,h*2+33),'Sem Minecraft, Photon, shader pack ou animacao corporal. Timings, alcance e dano preservados.',fill=(165,182,207));Path(output).parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 sheet=Image.new('RGB',(w*2,h+58),(9,12,20));d=ImageDraw.Draw(sheet)
 for i,(new,label) in enumerate([(False,'ANTES / MAIN'),(True,'PROPOSTA / ONDA GOJO')]):sheet.paste(draw(34,new,True),(i*w,0));d.text((i*w+12,12),label,fill=(238,239,250))
 d.text((12,h+15),'MESMA CAMERA, TEMPO, ESCALA E ILUMINACAO. COMPARACAO OFFLINE DOS MATERIAIS / CAMADAS.',fill=(230,233,245));sheet.save(comparison)
 frames=[]
 for t in range(34,75,2):
  frame=draw(t);label=ImageDraw.Draw(frame);label.text((12,12),'HOLLOW PURPLE / RAIOS MULTIDIRECIONAIS',fill=(238,239,250));label.text((12,h-22),'PREVIA OFFLINE / GLSL E GEOMETRIA DO MOD',fill=(165,182,207));frames.append(frame)
 animation=Path(output).with_name('gojo-roxo-raios-movimento.gif');frames[0].save(animation,save_all=True,append_images=frames[1:],duration=100,loop=0)
 for r in [base,add,old,line,fbo]:r.release()
 print('Gojo approval GPU:',ctx.info['GL_RENDERER'],output,comparison);ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--comparison',required=True);p.add_argument('--backend');a=p.parse_args();render(a.output,a.comparison,a.backend)
