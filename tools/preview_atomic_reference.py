"""Long Atomic: Java camera/geometry + runtime post GLSL; offline test scene, not Minecraft."""
from pathlib import Path
import argparse,json,math
import numpy as np,moderngl
from PIL import Image,ImageDraw,ImageFont
from preview_cid_composition import vertices
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,ASSETS,mesh,lookat


def render(output,video=None,backend=None):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 post=ctx.program(vertex_shader=(ASSETS/'shaders/program/atomic_cinema.vsh').read_text(),fragment_shader=(ASSETS/'shaders/program/atomic_cinema.fsh').read_text())
 material=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;out vec2 uv;out float skin;out vec3 normal;
 void main(){gl_Position=Matrix*vec4(Position/16.,1);uv=UV;skin=Textured;normal=Normal;}""",fragment_shader="""#version 150
 uniform sampler2D Atlas;in vec2 uv;in float skin;in vec3 normal;out vec4 fragColor;
 void main(){vec4 c=skin<.5?vec4(.65,.45,.33,1):texture(Atlas,uv);if(c.a<.1)discard;fragColor=vec4(c.rgb*(.5+.5*abs(dot(normalize(normal),normalize(vec3(.4,.8,1.))))),1);}""")
 data=json.loads((ROOT/'build/cid-composition-samples.json').read_text())['14'];frames=json.loads((ROOT/'build/cid-reference-camera.json').read_text())
 w,h=960,600;color=ctx.texture((w,h),4);depth=ctx.depth_texture((w,h));depth.compare_func='';scene=ctx.framebuffer([color],depth);target=ctx.simple_framebuffer((w,h),components=4)
 im=Image.open(ASSETS/'textures/armor/trajes/shadow.png').convert('RGBA');atlas=ctx.texture(im.size,4,im.tobytes());atlas.filter=(moderngl.NEAREST,moderngl.NEAREST);material['Atlas'].value=0
 bodybuf=ctx.buffer(mesh('shadow','cid_parado').tobytes());bodyvao=ctx.vertex_array(material,[(bodybuf,'3f 3f 2f 1f','Position','Normal','UV','Textured')])
 grid=[]
 for i in range(-40,41):
  for a,b in [((-40,0,i),(40,0,i)),((i,0,-40),(i,0,40))]:
   for p in [a,b]:grid.append([*p,.18,.13,.21,1,0,0])
 gridbuf=ctx.buffer(np.array(grid,dtype='f4').tobytes());gridvao=ctx.vertex_array(base,[(gridbuf,'3f 4f 2f','Position','Color','UV0')])
 full=ctx.buffer(np.array([[0,0,0],[w,0,0],[w,h,0],[0,0,0],[w,h,0],[0,h,0]],dtype='f4').tobytes());quad=ctx.vertex_array(post,[(full,'3f','Position')])
 ortho=np.eye(4,dtype='f4');ortho[0,0]=2/w;ortho[1,1]=2/h;ortho[0,3]=-1;ortho[1,3]=-1;post['ProjMat'].write(ortho.T.tobytes());post['OutSize'].value=(w,h);post['InSize'].value=(w,h);post['DiffuseSampler'].value=0;post['DepthSampler'].value=1;post['Frames'].value=1
 def draw(raw):
  frame=frames[raw];q=frame['camera'];tick=frame['visual'];camera=np.array([-q['x'],1.62+q['y'],q['z']]);view=lookat(camera,np.array([0.,1.62+q['alvoAltura'],q['alvoFrente']])).astype('f4')
  f=1/math.tan(math.radians(70*q['fov'])/2);near,far=.1,256;projection=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-(far+near)/(far-near),-2*far*near/(far-near)],[0,0,-1,0]],dtype='f4');matrix=projection@view
  scene.use();scene.depth_mask=True;scene.clear(.12,.07,.16,1,depth=1);ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE|moderngl.BLEND)
  base['ModelViewMat'].write(view.T.tobytes());base['ProjMat'].write(projection.T.tobytes());base['EffectMode'].value=8;base['EffectTime'].value=0;base['Viewer'].value=tuple(camera);gridvao.render(moderngl.LINES)
  material['Matrix'].write(matrix.T.astype('f4').tobytes());atlas.use(0);bodyvao.render()
  scene.depth_mask=False;ctx.enable(moderngl.BLEND)
  for lote in data[min(len(data)-1,int(tick))]:
   if not lote['faces']:continue
   p=add if lote['aditivo'] else base;p['ModelViewMat'].write(view.T.tobytes());p['ProjMat'].write(projection.T.tobytes());p['EffectMode'].value=lote['material'];p['EffectTime'].value=tick/20;p['Viewer'].value=tuple(camera);ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if lote['aditivo'] else moderngl.ONE_MINUS_SRC_ALPHA)
   buf=ctx.buffer(vertices(lote,camera).tobytes());vao=ctx.vertex_array(p,[(buf,'3f 4f 2f','Position','Color','UV0')]);vao.render();vao.release();buf.release()
  target.use();ctx.disable(moderngl.DEPTH_TEST|moderngl.BLEND);color.use(0);depth.use(1);post['AtomicTick'].value=tick;post['Strength'].value=frame['peso'];center=matrix@np.array([0.,1.2,0.,1.]);center=center[:2]/center[3]*.5+.5;post['ScreenCenter'].value=tuple(np.clip(center,.04,.96));quad.render()
  image=Image.frombytes('RGBA',(w,h),target.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(image);title=frame['titulo']
  if title:
   font=ImageFont.load_default(size=int(h*(.22 if title=='ATOMIC' else .045)));d.text((w/2,h*(.43 if title=='ATOMIC' else .57)),title,font=font,fill=(255,248,255),anchor='mm')
  d.rectangle((0,h-27,w,h),fill=(10,13,21));d.text((10,h-20),f'PREVIA OFFLINE | {raw/20:.2f}s | NAO E MINECRAFT. AVATAR ESTATICO / CENA DE TESTE.',fill=(232,235,245));return image
 # Moments picked on the old 39 s clock (780 ticks), scaled to the current length (3.11: 27 s).
 marks=[round(m*(len(frames)-1)/779) for m in [40,120,200,320,373,400,420,470,600,730,770,779]];sheet=Image.new('RGB',(w*3,h*4));
 for i,t in enumerate(marks):sheet.paste(draw(t),(i%3*w,i//3*h))
 Path(output).parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 if video:
  import av
  with av.open(str(video),'w') as container:
   stream=container.add_stream('libx264',rate=20);stream.width=w;stream.height=h;stream.pix_fmt='yuv420p';stream.options={'crf':'20'}
   for raw in range(len(frames)):
    for packet in stream.encode(av.VideoFrame.from_image(draw(raw))):container.mux(packet)
   for packet in stream.encode():container.mux(packet)
 for resource in [base,add,post,material,bodyvao,bodybuf,gridvao,gridbuf,quad,full,atlas,scene,target,color,depth]:resource.release()
 print('Atomic reference GPU:',ctx.info['GL_RENDERER'],output,video);ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--video');p.add_argument('--backend');a=p.parse_args();render(a.output,a.video,a.backend)
