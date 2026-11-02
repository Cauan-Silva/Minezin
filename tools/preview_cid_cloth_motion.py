"""Animated review of the real Java cloth sample poses and real Cid atlas. No Minecraft UI."""
import argparse
from pathlib import Path
import av,moderngl,numpy as np
from PIL import Image,ImageDraw
from preview_armor_models import mesh,rotation,lookat,ASSETS

def render(output,poster):
 ctx=moderngl.create_standalone_context(require=320)
 p=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;uniform mat4 Model;out vec3 n;out vec2 uv;out float tx;
 void main(){gl_Position=Matrix*vec4(Position,1);n=mat3(Model)*Normal;uv=UV;tx=Textured;}""",fragment_shader="""#version 150
 uniform sampler2D Atlas;uniform float Glow;in vec3 n;in vec2 uv;in float tx;out vec4 fragColor;
 void main(){vec4 c=tx<.5?vec4(.65,.48,.35,1):texture(Atlas,uv);if(Glow>.5&&tx<.5)discard;if(c.a<.01)discard;float light=Glow>.5?1.:.5+.5*abs(dot(normalize(n),normalize(vec3(.4,.8,1.))));fragColor=vec4(c.rgb*light,c.a);}""")
 textures=[]
 for name in ['shadow.png','shadow_glowmask.png']:
  im=Image.open(ASSETS/('textures/armor/trajes/'+name)).convert('RGBA');t=ctx.texture(im.size,4,im.tobytes());t.filter=(moderngl.NEAREST,moderngl.NEAREST);textures.append(t)
 p['Atlas'].value=0;w,h=360,430;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
 view=lookat(np.array([0.,20.,-65.]),np.array([0.,17.,0.]));proj=np.diag([2/37,2/45,-2/200,1]);proj[2,3]=-1
 container=av.open(str(output),'w');stream=container.add_stream('libx264',rate=20);stream.width=w*3;stream.height=h+38;stream.pix_fmt='yuv420p';stream.options={'crf':'20'}
 sheet=Image.new('RGB',(w*3,(h+38)*4));marks=[20,155,175,210]
 for frame in range(220):
  vertices=mesh('shadow','cid_frame'+str(frame));b=ctx.buffer(vertices.tobytes());vao=ctx.vertex_array(p,[(b,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);screen=Image.new('RGB',(w*3,h+38),(12,15,23));d=ImageDraw.Draw(screen)
  phase='FRENTE' if frame<35 else 'LATERAL DIREITA' if frame<70 else 'LATERAL ESQUERDA' if frame<100 else 'GIRO' if frame<120 else 'FRENAGEM / TRANSFORMACAO' if frame<140 else 'QUEDA VERTICAL - FLUXO DE AR ASCENDENTE' if frame<180 else 'POUSO - RETORNO AMORTECIDO'
  for i,(yaw,label) in enumerate([(180,'COSTAS'),(125,'PERFIL'),(28,'FRENTE')]):
   model=rotation([0,yaw,0]);p['Matrix'].write((proj@view@model).T.astype('f4').tobytes());p['Model'].write(model.T.astype('f4').tobytes());fbo.depth_mask=True;fbo.clear(.075,.085,.11,1,depth=1)
   p['Glow'].value=0;textures[0].use();ctx.disable(moderngl.BLEND);vao.render()
   p['Glow'].value=1;textures[1].use();fbo.depth_mask=False;ctx.depth_func='<=';ctx.enable(moderngl.BLEND);ctx.blend_func=(moderngl.ONE,moderngl.ONE);vao.render()
   im=Image.frombytes('RGBA',(w,h),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM);screen.paste(im,(i*w,0));d.text((i*w+10,10),label,fill=(235,237,245))
  d.text((10,h+8),phase+' - SIMULACAO JAVA / PREVIA OFFLINE, NAO E MINECRAFT',fill=(220,225,240))
  if frame in marks:sheet.paste(screen,(0,marks.index(frame)*(h+38)))
  for packet in stream.encode(av.VideoFrame.from_image(screen)):container.mux(packet)
  vao.release();b.release()
 for packet in stream.encode():container.mux(packet)
 container.close();sheet.save(poster);print('Cloth motion GPU:',ctx.info['GL_RENDERER'],output)
 for r in [*textures,p,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--poster',required=True);a=p.parse_args();render(a.output,a.poster)
