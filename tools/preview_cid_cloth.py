"""Real Cid mesh, glowmask and spring samples exported by Java tests. Offline, not Minecraft."""
from pathlib import Path
import argparse,math
import numpy as np
import moderngl
from PIL import Image,ImageDraw
from preview_armor_models import mesh,lookat,rotation,ASSETS

def render(output,backend=None,collar=False,directions=False,combat=False):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 p=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;uniform mat4 Model;
 out vec2 uv;out vec3 normal;out float textured;void main(){gl_Position=Matrix*vec4(Position,1);normal=mat3(Model)*Normal;uv=UV;textured=Textured;}""",fragment_shader="""#version 150
 uniform sampler2D Atlas;uniform float Light;uniform float Glow;in vec2 uv;in vec3 normal;in float textured;out vec4 fragColor;
 void main(){vec4 c=textured<.5?vec4(.72,.55,.43,1):texture(Atlas,uv);if(Glow>.5&&textured<.5)discard;if(c.a<.01)discard;
 float l=Glow>.5?1.:Light*(.4+.6*max(0.,dot(normalize(normal),normalize(vec3(-.4,.8,-1)))));fragColor=vec4(c.rgb*l,c.a);} """)
 tex=[]
 for path in ['shadow.png','shadow_glowmask.png']:
  im=Image.open(ASSETS/('textures/armor/trajes/'+path)).convert('RGBA');t=ctx.texture(im.size,4,im.tobytes());t.filter=(moderngl.NEAREST,moderngl.NEAREST);tex.append(t)
 p['Atlas'].value=0;w,h=360,430;fbo=ctx.simple_framebuffer((w,h),components=4);fbo.use();ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
 cases=[('PARADO - FRENTE','parado',0,1,True),('PARADO - COSTAS','parado',180,1,True),('PARADO - 3/4','parado',28,1,True),('CORRENDO - PERFIL','correndo',125,1,True),('VIRANDO','virando',125,1,True),('CAINDO','caindo',125,1,True),('ESCURO - SEM EMISSAO','parado',0,.06,False),('ESCURO - EMISSAO','parado',0,.06,True),('ESCURO - COSTAS','parado',180,.06,True)]
 if directions:
  cases=[('FRENTE - PERFIL','correndo',125,1,True),('DIREITA - COSTAS','direita',180,1,True),('ESQUERDA - COSTAS','esquerda',180,1,True),('RE - PERFIL','re',125,1,True),('GIRO','virando',125,1,True),('FRENAGEM','freando',125,1,True),('QUEDA','caindo',125,1,True),('GOLA REDUZIDA','parado',28,1,True),('COSTAS EM REPOUSO','parado',180,1,True)]
 if combat:
  cases=[('REPOUSO','parado',28,1,True),('CORTE - COSTAS','acao11',180,1,True),('DASH - PERFIL','acao12',125,1,True),('FIOS - FRENTE','acao13',28,1,True),('ATOMIC - IMPACTO','acao14',125,1,True),('TRANSFORMACAO','acao17',180,1,True),('CORTE - ABAS','acao11',28,1,True),('TRANSFORMACAO - ABAS','acao17',28,1,True),('ATOMIC - COSTAS','acao14',180,1,True)]
 sheet=Image.new('RGB' ,(w*3,h*3+60),(12,15,23));draw=ImageDraw.Draw(sheet);night=[]
 for i,(label,pose,yaw,light,glow) in enumerate(cases):
  vertices=mesh('shadow','cid_'+pose);assert np.isfinite(vertices).all();model=rotation([0,yaw,0]);view=lookat(np.array([0.,48.,-30.]),np.array([0.,24.,1.])) if collar else lookat(np.array([0.,20.,-65.]),np.array([0.,17.,0.]));projection=np.diag([2/22,2/26.3,-2/200,1]) if collar else np.diag([2/37,2/45,-2/200,1]);projection[2,3]=-1
  p['Matrix'].write((projection@view@model).T.astype('f4').tobytes());p['Model'].write(model.T.astype('f4').tobytes())
  b=ctx.buffer(vertices.tobytes());vao=ctx.vertex_array(p,[(b,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);fbo.depth_mask=True;fbo.clear(.075,.085,.11,1,depth=1)
  ctx.disable(moderngl.BLEND);p['Glow'].value=0;p['Light'].value=light;tex[0].use();vao.render()
  if glow:
   fbo.depth_mask=False;ctx.enable(moderngl.BLEND);ctx.blend_func=(moderngl.ONE,moderngl.ONE);ctx.depth_func='<=';p['Glow'].value=1;tex[1].use();vao.render();ctx.disable(moderngl.BLEND)
  data=np.frombuffer(fbo.read(components=4),dtype=np.uint8).reshape(h,w,4).copy();assert data[:,:,:3].std()>3
  if not directions and not combat and i in [6,7]:night.append(data.astype('i2'))
  tile=Image.fromarray(data).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');x=i%3*w;y=i//3*h;sheet.paste(tile,(x,y));draw.text((x+10,y+10),label,fill=(230,235,246));vao.release();b.release()
 if not directions and not combat:
  diff=np.abs(night[1][:,:,:3]-night[0][:,:,:3]).max(axis=2);assert (diff>10).sum()>15;assert (diff>0).sum()<w*h*.04,'Only sparse seams should emit'
 draw.text((12,h*3+12),'GEOMETRIA + MASCARA REAIS; POSES DA SIMULACAO JAVA. NAO E CAPTURA DO MINECRAFT.',fill=(232,238,246));draw.text((12,h*3+34),'Sem shader packs. Movimento nao tem colisao fisica com o mundo.',fill=(161,179,203))
 out=Path(output);out.parent.mkdir(parents=True,exist_ok=True);sheet.save(out);print('Cid cloth GPU:',ctx.info['GL_RENDERER'],out)
 for r in [*tex,p,fbo]:r.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');p.add_argument('--collar',action='store_true');p.add_argument('--directions',action='store_true');p.add_argument('--combat',action='store_true');a=p.parse_args();render(a.output,a.backend,a.collar,a.directions,a.combat)
