"""Render the repository's actual GeckoLib cubes and atlas in OpenGL. Not a Minecraft capture."""
from pathlib import Path
import argparse,math,json
import numpy as np
import moderngl
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'

def translation(p):
 m=np.eye(4);m[:3,3]=p;return m
def rotation(degrees):
 x,y,z=np.radians(degrees);cx,sx,cy,sy,cz,sz=math.cos(x),math.sin(x),math.cos(y),math.sin(y),math.cos(z),math.sin(z)
 rx=np.array([[1,0,0,0],[0,cx,-sx,0],[0,sx,cx,0],[0,0,0,1]])
 ry=np.array([[cy,0,sy,0],[0,1,0,0],[-sy,0,cy,0],[0,0,0,1]])
 rz=np.array([[cz,-sz,0,0],[sz,cz,0,0],[0,0,1,0],[0,0,0,1]])
 return rz@ry@rx
def transform(obj):
 pivot=np.array(obj.get('pivot',[0,0,0]));return translation(pivot)@rotation(obj.get('rotation',[0,0,0]))@translation(-pivot)
def mesh(style):
 path=ASSETS/('geo/frieren/cajado.geo.json' if style=='cajado' else f'geo/armaduras/{style}.geo.json')
 d=json.loads(path.read_text(encoding='utf-8'))['minecraft:geometry'][0]
 bones={b['name']:b for b in d['bones']};cache={}
 if style=='frieren':
  staff=json.loads((ASSETS/'geo/frieren/cajado.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
  for b in staff['bones']:
   b=dict(b);b['name']='staff_'+b['name']
   if 'parent' in b:b['parent']='staff_'+b['parent']
   bones[b['name']]=b
 def bone_matrix(name):
  if name not in cache:
   b=bones[name];base=bone_matrix(b['parent']) if 'parent'in b else translation([9,17,0]) if name=='staff_root' else translation([0,18,0]) if style=='cajado' else np.eye(4)
   cache[name]=base@transform(b)
  return cache[name]
 faces={'north':([0,2,3,1],[0,0,-1]),'south':([4,5,7,6],[0,0,1]),'west':([0,4,6,2],[-1,0,0]),
        'east':([1,3,7,5],[1,0,0]),'down':([0,1,5,4],[0,-1,0]),'up':([2,6,7,3],[0,1,0])}
 # Neutral mannequin skin exposes where the real player's own face/hands remain visible.
 skin={'uv':[0,0],'uv_size':[1,1]}
 if style!='cajado':bones['mannequin']={'name':'mannequin','cubes':[
  {'origin':[-4,24,-4],'size':[8,8,8],'uv':{f:skin for f in faces}},
  {'origin':[4,12,-2],'size':[4,3,4],'uv':{f:skin for f in faces}},
  {'origin':[-8,12,-2],'size':[4,3,4],'uv':{f:skin for f in faces}}]}
 out=[];tw,th=d['description']['texture_width'],d['description']['texture_height']
 for name,b in bones.items():
  for cube in b.get('cubes',[]):
   m=bone_matrix(name)@transform(cube);origin=np.array(cube['origin']);size=np.array(cube['size'])
   corners=[origin+size*np.array([i&1,(i>>1)&1,(i>>2)&1]) for i in range(8)]
   for face,(indices,normal) in faces.items():
    uv=cube['uv'][face];u,v=uv['uv'];w,h=uv['uv_size'];tex=[(u/tw,v/th),((u+w)/tw,v/th),((u+w)/tw,(v+h)/th),(u/tw,(v+h)/th)]
    n=m[:3,:3]@normal
    for k in [0,1,2,0,2,3]:out.append([*(m@np.r_[corners[indices[k]],1])[:3],*n,*tex[k],0 if name=='mannequin' else 1])
 return np.array(out,dtype='f4')
def lookat(eye,target):
 f=(target-eye);f=f/np.linalg.norm(f);s=np.cross(f,[0,1,0]);s=s/np.linalg.norm(s);u=np.cross(s,f)
 m=np.eye(4);m[0,:3]=s;m[1,:3]=u;m[2,:3]=-f;return m@translation(-eye)
def render(output,backend=None):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 program=ctx.program(vertex_shader='''#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;uniform mat4 ModelView;
 out vec2 uv;out vec3 normal;out float textured;void main(){textured=Textured;gl_Position=Matrix*vec4(Position,1);normal=mat3(ModelView)*Normal;uv=UV;}''',
 fragment_shader='''#version 150
 uniform sampler2D Atlas;in vec2 uv;in vec3 normal;in float textured;out vec4 fragColor;
 void main(){vec4 c=mix(vec4(.72,.55,.43,1),texture(Atlas,uv),textured);float light=.38+.62*max(0.,dot(normalize(normal),normalize(vec3(-.4,.8,1))));fragColor=vec4(c.rgb*light,c.a);}''')
 image=Image.open(ASSETS/'textures/armor/anime_reference_atlas.png').convert('RGBA');texture=ctx.texture(image.size,4,image.tobytes());texture.filter=(moderngl.LINEAR,moderngl.LINEAR);texture.use();program['Atlas'].value=0
 width,height=320,430;fbo=ctx.simple_framebuffer((width,height),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
 projection=np.diag([2/34,2/45,-2/200,1]);projection[2,3]=-1
 view=lookat(np.array([0.,20.,-60.]),np.array([0.,18.,0.]))
 sheet=Image.new('RGB',(width*3,height*4+58),(11,15,24));draw=ImageDraw.Draw(sheet)
 for index,(style,yaw)in enumerate([(style,yaw) for style in ['gojo','frieren','shadow','cajado'] for yaw in [0,180,28]]):
  fbo.clear(.035,.045,.067,1,depth=1);model=rotation([0,yaw,0]);mv=view@model
  program['Matrix'].write((projection@mv).T.astype('f4').tobytes());program['ModelView'].write(mv.T.astype('f4').tobytes())
  buf=ctx.buffer(mesh(style).tobytes());vao=ctx.vertex_array(program,[(buf,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);vao.render(moderngl.TRIANGLES)
  tile=Image.frombytes('RGBA',(width,height),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  data=np.asarray(tile);assert data[:,:,:3].std()>10,'Blank armor render'
  x=index%3*width;y=index//3*height;sheet.paste(tile.convert('RGB'),(x,y))
  draw.text((x+12,y+12),style.upper()+(' - FRENTE' if yaw==0 else ' - COSTAS' if yaw==180 else ' - PERSPECTIVA'),fill=(230,235,245));vao.release();buf.release()
 draw.text((12,height*4+12),'MODELOS E TEXTURA REAIS EM OPENGL - MANEQUIM NEUTRO',fill=(230,235,245))
 draw.text((12,height*4+32),'Sem animacao, iluminacao ou renderizador do Minecraft.',fill=(160,178,202))
 output=Path(output);output.parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 print('Armor GPU:',ctx.info['GL_RENDERER']);print('Rendered armor models:',output)
 texture.release();program.release();fbo.release();ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');a=p.parse_args();render(a.output,a.backend)
