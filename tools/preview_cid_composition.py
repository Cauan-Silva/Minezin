"""Exact Java-exported Cid compositions + cloth poses with runtime GLSL. Offline review, not Minecraft."""
from pathlib import Path
import argparse,json,math
import numpy as np
import moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import mesh,lookat,ASSETS,ROOT


def vertices(lote,camera=None):
 out=[]
 for q in lote['faces']:
  rgb=[(q['cor']>>n&255)/255 for n in [16,8,0]];color=[*rgb,q['alpha']]
  uv=[(q['u0'],q['v0']),(q['u1'],q['v0']),(q['u1'],q['v1']),(q['u0'],q['v1'])]
  points=np.array([[q[key][axis] for axis in ['x','y','z']] for key in ['a','b','c','d']])
  if camera is not None and q.get('billboard',False):
   a=(points[0]+points[3])*.5;b=(points[1]+points[2])*.5;width=np.linalg.norm(points[3]-points[0])*.5;side=np.cross(b-a,np.asarray(camera)-a);length=np.linalg.norm(side);side=(side/length if length>1e-8 else np.array([1.,0.,0.]))*width;points=np.array([a-side,b-side,b+side,a+side])
  for k in [0,1,2,0,2,3]:out.append([*points[k],*color,*uv[k]])
 return np.array(out,dtype='f4')


def render(output,video=None,backend=None,explosion=False,reference=False):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 body=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;
 out vec3 n;out vec2 uv;out float textured;void main(){gl_Position=Matrix*vec4(Position,1);n=Normal;uv=UV;textured=Textured;}""",fragment_shader="""#version 150
 uniform sampler2D Atlas;in vec3 n;in vec2 uv;in float textured;out vec4 fragColor;
 void main(){vec4 c=textured<.5?vec4(.55,.40,.29,1):texture(Atlas,uv);if(c.a<.01)discard;float l=.45+.55*max(0.,dot(normalize(n),normalize(vec3(.5,1.,-.6))));fragColor=vec4(c.rgb*l,1);}""")
 im=Image.open(ASSETS/'textures/armor/trajes/shadow.png').convert('RGBA');tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);body['Atlas'].value=0
 data=json.loads((ROOT/'build/cid-composition-samples.json').read_text())
 w,h=(960,600) if explosion or reference else (640,400);fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 camera=np.array([8.,5.,-10.]);view=lookat(camera,np.array([0.,1.,2.]));proj=np.diag([2/10.8,2/6.75,-2/80,1]);proj[2,3]=-1
 proj=proj.astype('f4');view=view.astype('f4')
 labels={11:'CORTE - SILHUETA E BORDA',12:'DASH - RASTRO VOLUMETRICO',17:'TRANSFORMACAO - MANTO / ENERGIA',14:'ATOMIC - ARCOS E LIBERACAO'}
 def visual_time(raw):
  import re
  clock=(ROOT/'src/main/java/com/cauansilva/arcanecontrol/core/vfx/RelogioAtomic.java').read_text(encoding='utf-8')
  real=[float(v) for v in re.search(r'REAL=\{([^}]+)',clock).group(1).split(',')];vis=[float(v) for v in re.search(r'VISUAL=\{([^}]+)',clock).group(1).split(',')]
  return float(np.interp(raw,real,vis))
 def draw(tipo,t,frame):
  fbo.depth_mask=True;fbo.clear(.055,.065,.09,1,depth=1)
  # Grid is only a spatial reference, not a recreation of a Minecraft map.
  grid=[]
  for i in range(-7,9):
   for a,b in [((-7,0,i),(8,0,i)),((i,0,-7),(i,0,8))]:
    for x,y,z in [a,b]:grid.append([x,y,z,.13,.15,.19,1,0,0])
  local_proj=proj.copy()
  if tipo==14:local_proj[0,0]=2/76;local_proj[1,1]=2/47.5
  local_view=view
  if tipo==14 and t>=90:
   # Colossal pillar (dome radius, far above the build limit): wider framing.
   local_proj[0,0]=2/300;local_proj[1,1]=2/188;local_proj[2,2]=-2/900
   local_view=lookat(np.array([160.,60.,-200.]),np.array([0.,75.,0.])).astype('f4')
  program=base;program['ModelViewMat'].write(local_view.T.tobytes());program['ProjMat'].write(local_proj.T.tobytes());program['EffectMode'].value=8;program['EffectTime'].value=0;program['Viewer'].value=tuple(camera)
  b=ctx.buffer(np.array(grid,dtype='f4').tobytes());v=ctx.vertex_array(program,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render(moderngl.LINES);v.release();b.release()
  verts=mesh('shadow','cid_frame'+str(frame%220));verts[:,:3]/=16
  body['Matrix'].write((local_proj@local_view).T.astype('f4').tobytes());tex.use();ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE_MINUS_SRC_ALPHA)
  b=ctx.buffer(verts.tobytes());v=ctx.vertex_array(body,[(b,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);v.render();v.release();b.release()
  fbo.depth_mask=False
  for lote in data[str(tipo)][int(t)]:
   if not lote['faces']:continue
   p=add if lote['aditivo'] else base;p['ModelViewMat'].write(local_view.T.tobytes());p['ProjMat'].write(local_proj.T.tobytes());p['EffectMode'].value=lote['material'];p['EffectTime'].value=t/20;p['Viewer'].value=tuple(camera)
   ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if lote['aditivo'] else moderngl.ONE_MINUS_SRC_ALPHA)
   verts=vertices(lote);b=ctx.buffer(verts.tobytes());v=ctx.vertex_array(p,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
  img=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(img);d.text((12,12),labels[tipo]+f' / tick visual {t:.1f}',fill=(235,237,245));return img
 snapshots=[(11,4),(11,8),(11,16),(12,2),(12,8),(12,16),(17,4),(17,12),(17,24),(14,18),(14,60),(14,98)]
 if reference:snapshots=[(14,t) for t in [0,15,30,50,70,79,87,90,110,170,235,239]]
 if explosion:snapshots=[(14,t) for t in [85,90,94,102,115,140,170,205,235]]
 rows=3 if explosion else 4
 sheet=Image.new('RGB',(w*3,h*rows+50),(10,14,20))
 for i,(tipo,t) in enumerate(snapshots):sheet.paste(draw(tipo,t,i*11),(i%3*w,i//3*h))
 ImageDraw.Draw(sheet).text((12,h*rows+14),'MALHAS EXPORTADAS DO JAVA + GLSL REAIS. CENA OFFLINE, SEM BLOOM, SEM MINECRAFT.',fill=(220,225,240));Path(output).parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 if video:
  import av
  container=av.open(str(video),'w');stream=container.add_stream('libx264',rate=20);stream.width=w if explosion or reference else w*2;stream.height=h+32 if explosion or reference else h*2+32;stream.pix_fmt='yuv420p';stream.options={'crf':'20'}
  for frame in range(540 if reference else 160 if explosion else 140):
   if reference:
    tick=visual_time(frame);screen=Image.new('RGB',(w,h+32),(10,14,20));screen.paste(draw(14,tick,0),(0,0));bottom=h
    title='I AM' if 78<=tick<86 else 'ATOMIC' if 86<=tick<88 else ''
    if 88<=tick<90:
     arr=np.asarray(screen).copy();arr[:h]=(arr[:h]*.04).astype('uint8');screen=Image.fromarray(arr)
    if title:
     from PIL import ImageFont
     font=ImageFont.load_default(size=int(h*(.22 if title=='ATOMIC' else .045)));d=ImageDraw.Draw(screen);d.text((w/2,h*.45),title,font=font,fill=(255,248,255),anchor='mm')
   elif explosion:
    screen=Image.new('RGB',(w,h+32),(10,14,20));screen.paste(draw(14,80+frame,frame),(0,0));bottom=h
   else:
    screen=Image.new('RGB',(w*2,h*2+32),(10,14,20));bottom=h*2
    for i,tipo in enumerate([11,12,17,14]):
     dur=len(data[str(tipo)]);screen.paste(draw(tipo,frame%dur,frame),(i%2*w,i//2*h))
   ImageDraw.Draw(screen).text((12,bottom+10),'PREVIA OFFLINE: GEOMETRIA / MATERIAIS REAIS. NAO E CAPTURA DO JOGO.',fill=(220,225,240))
   for packet in stream.encode(av.VideoFrame.from_image(screen)):container.mux(packet)
  for packet in stream.encode():container.mux(packet)
  container.close()
 for resource in [tex,body,base,add,fbo]:resource.release()
 print('Cid composition GPU:',ctx.info['GL_RENDERER'],output,video);ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--video');p.add_argument('--backend');p.add_argument('--explosion',action='store_true');p.add_argument('--reference',action='store_true');a=p.parse_args();render(a.output,a.video,a.backend,a.explosion,a.reference)
