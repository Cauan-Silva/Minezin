"""Offline review: real armor geometry/atlas, fixed-tick Java cloth samples. Not Minecraft."""
import argparse,json
from pathlib import Path
import av,moderngl,numpy as np
from PIL import Image,ImageDraw
from preview_armor_models import mesh,rotation,lookat,ASSETS,ROOT

def render(output,poster):
 ctx=moderngl.create_standalone_context(require=320)
 p=ctx.program(vertex_shader='''#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;uniform mat4 Model;out vec3 n;out vec2 uv;out float tx;
 void main(){gl_Position=Matrix*vec4(Position,1);n=mat3(Model)*Normal;uv=UV;tx=Textured;}''',fragment_shader='''#version 150
 uniform sampler2D Atlas;in vec3 n;in vec2 uv;in float tx;out vec4 fragColor;
 void main(){vec4 c=tx<.5?vec4(.72,.55,.43,1):texture(Atlas,uv);if(c.a<.01)discard;float light=.65+.35*abs(dot(normalize(n),normalize(vec3(.4,.8,1.))));fragColor=vec4(c.rgb*light,c.a);}''')
 im=Image.open(ASSETS/'textures/armor/trajes/gojo.png').convert('RGBA');tex=ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);tex.use();p['Atlas'].value=0
 w,h=380,460;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
 container=av.open(str(output),'w');stream=container.add_stream('libx264',rate=40);stream.width=w*3;stream.height=h+54;stream.pix_fmt='yuv420p';stream.options={'crf':'18'}
 samples=json.loads((ROOT/'build/gojo-cloth-samples.json').read_text(encoding='utf-8'));marks=[35,120,220,300];sheet=Image.new('RGB',(w*3,(h+54)*4))
 for frame,sample in enumerate(samples):
  vertices=mesh('gojo','gojo_cloth_'+str(frame));buf=ctx.buffer(vertices.tobytes());vao=ctx.vertex_array(p,[(buf,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);screen=Image.new('RGB',(w*3,h+54),(16,20,29));d=ImageDraw.Draw(screen)
  for i,(yaw,label,center,span) in enumerate([(25,'FRENTE / BARRA',17.,42.),(155,'COSTAS / BARRA',17.,42.),(45,'GOLA - DETALHE',23.,15.)]):
   model=rotation([0,yaw,0]);view=lookat(np.array([0.,center+2,-65.]),np.array([0.,center,0.]));proj=np.diag([2/(span*w/h),2/span,-2/200,1]);proj[2,3]=-1
   p['Matrix'].write((proj@view@model).T.astype('f4').tobytes());p['Model'].write(model.T.astype('f4').tobytes());fbo.clear(.12,.14,.18,1,depth=1);vao.render()
   tile=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM);screen.paste(tile,(i*w,0));d.text((i*w+10,10),label,fill=(230,235,245))
  d.text((12,h+7),sample['fase']+' - movimento secundario curto, sem ampliar a deformacao',fill=(235,238,246));d.text((12,h+28),'PREVIA OFFLINE OPENGL - modelo/textura reais + fisica Java; manequim fixo',fill=(170,186,205))
  if frame in marks:sheet.paste(screen,(0,marks.index(frame)*(h+54)))
  for packet in stream.encode(av.VideoFrame.from_image(screen)):container.mux(packet)
  vao.release();buf.release()
 for packet in stream.encode():container.mux(packet)
 container.close();sheet.save(poster);print(ctx.info['GL_RENDERER'],output)
 tex.release();p.release();fbo.release();ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--poster',required=True);a=p.parse_args();render(Path(a.output),Path(a.poster))
