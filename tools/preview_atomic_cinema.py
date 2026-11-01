"""Real Atomic post GLSL on an offline scene using the mod's Shadow mesh and real depth.
This is not a Minecraft capture, performance certification or shader-pack compatibility test.
"""
from pathlib import Path
import argparse,json,math
import moderngl
import numpy as np
from PIL import Image,ImageDraw
from preview_armor_models import mesh,lookat,ASSETS

def box(center,size,color):
 c=np.asarray(center);s=np.asarray(size);out=[]
 for axis in range(3):
  for side in [-1,1]:
   u=(axis+1)%3;v=(axis+2)%3;points=[]
   for i,j in [(-1,-1),(1,-1),(1,1),(-1,1)]:
    p=c.copy();p[axis]+=s[axis]*side;p[u]+=s[u]*i;p[v]+=s[v]*j;points.append(p)
   for k in [0,1,2,0,2,3]:out.append([*points[k],*(np.asarray(color)*(.72 if side<0 else .9))])
 return np.asarray(out,dtype='f4')

def render(output,backend=None,video=None):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 w,h=480,300
 color=ctx.texture((w,h),4);color.filter=(moderngl.NEAREST,moderngl.NEAREST)
 depth=ctx.depth_texture((w,h));depth.compare_func='';depth.filter=(moderngl.NEAREST,moderngl.NEAREST)
 scene=ctx.framebuffer(color_attachments=[color],depth_attachment=depth)
 processed=ctx.simple_framebuffer((w,h),components=4)
 post=ctx.program(vertex_shader=(ASSETS/'shaders/program/atomic_cinema.vsh').read_text(),fragment_shader=(ASSETS/'shaders/program/atomic_cinema.fsh').read_text())
 copy=ctx.program(vertex_shader=(ASSETS/'shaders/core/atomic_copy.vsh').read_text(),fragment_shader=(ASSETS/'shaders/core/atomic_copy.fsh').read_text())
 copy.release()
 material=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;out vec2 uv;out float skin;
 void main(){gl_Position=Matrix*vec4(Position/16.,1);uv=UV;skin=Textured;}""",fragment_shader="""#version 150
 uniform sampler2D Atlas;in vec2 uv;in float skin;out vec4 fragColor;
 void main(){vec4 c=skin<.5?vec4(.65,.45,.33,1):texture(Atlas,uv);if(c.a<.1)discard;fragColor=c;}""")
 terrain=ctx.program(vertex_shader="""#version 150
 in vec3 Position;in vec3 Color;uniform mat4 Matrix;out vec3 color;
 void main(){gl_Position=Matrix*vec4(Position,1);color=Color;}""",fragment_shader="""#version 150
 in vec3 color;out vec4 fragColor;void main(){fragColor=vec4(color,1);}""")
 texture_image=Image.open(ASSETS/'textures/armor/trajes/shadow.png').convert('RGBA');atlas=ctx.texture(texture_image.size,4,texture_image.tobytes());atlas.filter=(moderngl.NEAREST,moderngl.NEAREST);atlas.use(0);material['Atlas'].value=0
 view=lookat(np.array([3.,2.8,-7.]),np.array([0.,1.,0.]))
 f=1/math.tan(math.radians(48)/2);near,far=.1,100
 projection=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-(far+near)/(far-near),-2*far*near/(far-near)],[0,0,-1,0]],dtype='f4')
 matrix=projection@view
 material['Matrix'].write(matrix.T.astype('f4').tobytes());terrain['Matrix'].write(matrix.T.astype('f4').tobytes())
 vertices=mesh('shadow');buf=ctx.buffer(vertices.tobytes());model=ctx.vertex_array(material,[(buf,'3f 12x 2f 1f','Position','UV','Textured')])
 blocks=np.concatenate([box([0.,-.1,0.],[20.,.1,20.],[.36,.42,.27]),box([-2.,.6,2.],[.6,.6,.6],[.48,.44,.39]),box([3.,1.1,4.],[.7,1.1,.7],[.42,.4,.36]),box([-4.,1.6,5.],[.6,1.6,.6],[.46,.45,.42])])
 ground_buf=ctx.buffer(blocks.tobytes());ground=ctx.vertex_array(terrain,[(ground_buf,'3f 3f','Position','Color')])
 scene.use();scene.depth_mask=True;scene.clear(.46,.61,.78,1,depth=1);ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.BLEND|moderngl.CULL_FACE);atlas.use(0);ground.render();model.render()
 before_depth=depth.read();original=np.frombuffer(color.read(),dtype=np.uint8).reshape(h,w,4).copy()
 assert original[:,:,:3].std()>15
 color.use(0);depth.use(1);post['DiffuseSampler'].value=0;post['DepthSampler'].value=1
 ortho=np.eye(4,dtype='f4');ortho[0,0]=2/w;ortho[1,1]=2/h;ortho[0,3]=-1;ortho[1,3]=-1
 post['ProjMat'].write(ortho.T.tobytes());post['OutSize'].value=(w,h);post['InSize'].value=(w,h)
 center=matrix@np.array([0.,1.2,0.,1.]);post['ScreenCenter'].value=tuple(center[:2]/center[3]*.5+.5)
 quad=np.array([[0,0,0],[w,0,0],[w,h,0],[0,0,0],[w,h,0],[0,h,0]],dtype='f4');qbuf=ctx.buffer(quad.tobytes());vao=ctx.vertex_array(post,[(qbuf,'3f','Position')])
 ctx.disable(moderngl.DEPTH_TEST|moderngl.BLEND);processed.use()
 def frame(t,strength=1,frames=1):
  post['AtomicTick'].value=t;post['Strength'].value=strength;post['Frames'].value=frames;processed.clear(0,0,0,1);vao.render(moderngl.TRIANGLES)
  return np.frombuffer(processed.read(components=4),dtype=np.uint8).reshape(h,w,4).copy()
 assert np.abs(frame(91,0).astype('i2')-original.astype('i2')).max()<=1,'Zero strength must be identity'
 ticks=[0,60,86,88.6,90.6,91.8,93,94.8,99,110,150,220]
 labels=['BASE','COMPRESSAO','CONCENTRACAO','NEGATIVO','PAPEL / SILHUETA','INVERSAO','DUOTOM VIOLETA','TRACO / CORTE','DESCARGA','ONDA','RESCALDO','DISSIPACAO']
 sheet=Image.new('RGB',(w*3,h*4+70),(10,13,21));draw=ImageDraw.Draw(sheet);signatures=[]
 for i,(t,label)in enumerate(zip(ticks,labels)):
  data=frame(t,0 if t==0 else 1);assert data[:,:,:3].std()>3,(t,'blank')
  signatures.append(data[:,:,:3].mean(axis=(0,1)).tolist())
  tile=Image.fromarray(data).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');x=i%3*w;y=i//3*h;sheet.paste(tile,(x,y));draw.text((x+10,y+10),f'{label} | tick {t}',fill=(255,180,90),stroke_width=1,stroke_fill=(0,0,0))
 no_frames=frame(90.6,1,0);paper=frame(90.6)
 assert abs(paper[:,:,:3].mean()-no_frames[:,:,:3].mean())>12,'Frames-off must suppress paper exposure'
 assert np.abs(frame(90.6,1).astype('i2')-frame(91.8,1).astype('i2')).mean()>35,'Adjacent authored frames must be distinct'
 assert np.abs(frame(239.9,.00001).astype('i2')-original.astype('i2')).max()<=1
 assert before_depth==depth.read(),'Post pass must not alter source depth'
 draw.text((12,h*4+14),'GLSL REAL + MODELO SHADOW + PROFUNDIDADE REAL EM CENA DE TESTE OFFLINE',fill=(235,239,248))
 draw.text((12,h*4+36),'Nao e captura do Minecraft. Sem Photon, shader pack ou composicao completa da explosao.',fill=(164,181,207))
 out=Path(output);out.parent.mkdir(parents=True,exist_ok=True);sheet.save(out)
 out.with_suffix('.json').write_text(json.dumps({'renderer':ctx.info['GL_RENDERER'],'ticks':ticks,'mean_rgb':signatures,'checks':['identity_at_zero','distinct_impact_stages','frames_off','fade_to_identity','source_depth_unchanged']},indent=2))
 print('Atomic post GPU:',ctx.info['GL_RENDERER']);print('12 temporal frames and 5 GPU checks passed:',out)
 if video:
  import av
  video=Path(video);video.parent.mkdir(parents=True,exist_ok=True)
  with av.open(str(video),'w') as container:
   stream=container.add_stream('libx264',rate=60);stream.width=w;stream.height=h;stream.pix_fmt='yuv420p';stream.options={'crf':'18','preset':'fast'}
   for i in range(720):
    t=i/3.;u=np.clip(t/12,0,1);u=u*u*(3-2*u);v=np.clip((t-170)/70,0,1);v=v*v*(3-2*v)
    data=frame(t,float(u*(1-v)))
    im=Image.fromarray(data).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB');d=ImageDraw.Draw(im)
    d.rectangle((0,h-32,w,h),fill=(10,13,21));d.text((8,h-26),f'PREVIA GLSL OFFLINE | tick {t:.1f} | NAO E MINECRAFT',fill=(235,238,246))
    f=av.VideoFrame.from_image(im);f.pts=i
    for packet in stream.encode(f):container.mux(packet)
   for packet in stream.encode():container.mux(packet)
  print('Offline temporal preview (silent):',video)
 for item in [vao,qbuf,ground,ground_buf,model,buf,atlas,post,material,terrain,processed,scene,color,depth]:item.release()
 ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');p.add_argument('--video');a=p.parse_args();render(a.output,a.backend,a.video)
