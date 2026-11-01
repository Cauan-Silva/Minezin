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
def mesh(style,pose_name="idle"):
 bosses={'toji':1,'aurora':2,'qual':3,'liebe':4}
 path=ASSETS/(f'geo/nemesis/{bosses[style]}.geo.json' if style in bosses else 'geo/asta/matadora.geo.json' if style=='matadora' else 'geo/asta/forma.geo.json' if style=='asta_forma' else 'geo/frieren/cajado.geo.json' if style=='cajado' else f'geo/armaduras/{style}.geo.json')
 d=json.loads(path.read_text(encoding='utf-8'))['minecraft:geometry'][0]
 bones={b['name']:b for b in d['bones']};cache={}
 materials={n:(d['description']['texture_width'],d['description']['texture_height'],2 if style=='asta_forma' else 1) for n in bones}
 if style=='asta_forma':
  armor=json.loads((ASSETS/'geo/armaduras/asta.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
  for b in armor['bones']:
   bones[b['name']]=b;materials[b['name']]=(armor['description']['texture_width'],armor['description']['texture_height'],1)
 if style=='frieren':
  staff=json.loads((ASSETS/'geo/frieren/cajado.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
  for b in staff['bones']:
   b=dict(b);b['name']='staff_'+b['name']
   if 'parent' in b:b['parent']='staff_'+b['parent']
   bones[b['name']]=b;materials[b['name']]=(staff['description']['texture_width'],staff['description']['texture_height'],2)
 def bone_matrix(name):
  if name not in cache:
   b=bones[name];base=bone_matrix(b['parent']) if 'parent'in b else translation([9,17,0]) if name=='staff_root' else translation([0,6,0]) if style=='matadora' else translation([0,18,0]) if style=='cajado' else np.eye(4)
   cache[name]=base@transform(b)
  return cache[name]
 faces={'north':([0,2,3,1],[0,0,-1]),'south':([4,5,7,6],[0,0,1]),'west':([0,4,6,2],[-1,0,0]),
        'east':([1,3,7,5],[1,0,0]),'down':([0,1,5,4],[0,-1,0]),'up':([2,6,7,3],[0,1,0])}
 # Neutral mannequin skin exposes where the real player's own face/hands remain visible.
 skin={'uv':[0,0],'uv_size':[1,1]}
 if style not in ['cajado','toji','aurora','qual','liebe','matadora']:
  for n,parent,o,size in [('skin_head','armorHead',[-4,24,-4],[8,8,8]),('skin_left','armorLeftArm',[4,12,-2],[4,12,4]),('skin_right','armorRightArm',[-8,12,-2],[4,12,4])]:
   bones[n]={'name':n,'parent':parent,'cubes':[{'origin':o,'size':size,'uv':{f:skin for f in faces}}]}
  if pose_name!='idle':
   for n,angle in [('armorLeftArm',-35 if pose_name=='walk' else -75),('armorRightArm',35 if pose_name=='walk' else -115),('armorLeftLeg',30 if pose_name=='walk' else 0),('armorRightLeg',-30 if pose_name=='walk' else 0),('armorLeftBoot',30 if pose_name=='walk' else 0),('armorRightBoot',-30 if pose_name=='walk' else 0)]:bones[n]['rotation']=[angle,0,0]
 out=[];tw,th=d['description']['texture_width'],d['description']['texture_height']
 for name,b in bones.items():
  tw,th,material=materials.get(name,(1,1,0))
  for cube in b.get('cubes',[]):
   m=bone_matrix(name)@transform(cube);inflate=cube.get('inflate',0);origin=np.array(cube['origin'])-inflate;size=np.array(cube['size'])+2*inflate
   corners=[origin+size*np.array([i&1,(i>>1)&1,(i>>2)&1]) for i in range(8)]
   for face,(indices,normal) in faces.items():
    uv=cube['uv'][face];u,v=uv['uv'];w,h=uv['uv_size'];tex=[(u/tw,v/th),((u+w)/tw,v/th),((u+w)/tw,(v+h)/th),(u/tw,(v+h)/th)]
    if face in ['north','east']:tex=[(u/tw,(v+h)/th),(u/tw,v/th),((u+w)/tw,v/th),((u+w)/tw,(v+h)/th)]
    elif face in ['south','west']:tex=[(u/tw,(v+h)/th),((u+w)/tw,(v+h)/th),((u+w)/tw,v/th),(u/tw,v/th)]
    n=m[:3,:3]@normal
    for k in [0,1,2,0,2,3]:out.append([*(m@np.r_[corners[indices[k]],1])[:3],*n,*tex[k],material])
 return np.array(out,dtype='f4')
def lookat(eye,target):
 f=(target-eye);f=f/np.linalg.norm(f);s=np.cross(f,[0,1,0]);s=s/np.linalg.norm(s);u=np.cross(s,f)
 m=np.eye(4);m[0,:3]=s;m[1,:3]=u;m[2,:3]=-f;return m@translation(-eye)
def render(output,backend=None,nemesis=False,asta=False,boss_scale=False,poses=False):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 program=ctx.program(vertex_shader='''#version 150
 in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;uniform mat4 ModelView;
 out vec2 uv;out vec3 normal;out float textured;void main(){textured=Textured;gl_Position=Matrix*vec4(Position,1);normal=mat3(ModelView)*Normal;uv=UV;}''',
 fragment_shader='''#version 150
 uniform sampler2D Atlas;uniform sampler2D Accessories;in vec2 uv;in vec3 normal;in float textured;out vec4 fragColor;
 void main(){vec4 c=textured<.5?vec4(.72,.55,.43,1):textured>1.5?texture(Accessories,uv):texture(Atlas,uv);if(c.a<.1)discard;float light=.38+.62*max(0.,dot(normalize(normal),normalize(vec3(-.4,.8,1))));fragColor=vec4(c.rgb*light,c.a);}''')
 image=Image.open(ASSETS/('textures/armor/anime_atlas.png' if nemesis else 'textures/armor/anime_reference_atlas.png')).convert('RGBA');texture=ctx.texture(image.size,4,image.tobytes());texture.filter=(moderngl.NEAREST,moderngl.NEAREST);texture.use();program['Atlas'].value=0;accessory_image=Image.open(ASSETS/'textures/armor/anime_reference_atlas.png').convert('RGBA');accessory_texture=ctx.texture(accessory_image.size,4,accessory_image.tobytes());accessory_texture.filter=(moderngl.NEAREST,moderngl.NEAREST);accessory_texture.use(1);program['Accessories'].value=1
 width,height=320,430;fbo=ctx.simple_framebuffer((width,height),components=4);fbo.use()
 ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
 projection=np.diag([2/34,2/45,-2/200,1]);projection[2,3]=-1
 view=lookat(np.array([0.,20.,-60.]),np.array([0.,18.,0.]))
 styles=['gojo','frieren','shadow','asta'] if poses else ['toji','qual','liebe'] if boss_scale else ['asta','matadora','asta_forma','liebe'] if asta else ['toji','aurora','qual'] if nemesis else ['gojo','frieren','shadow','cajado'];rows=1 if boss_scale else len(styles)
 sheet=Image.new('RGB',(width*3,height*rows+58),(11,15,24));draw=ImageDraw.Draw(sheet)
 views=[(style,0) for style in styles] if boss_scale else [(style,yaw) for style in styles for yaw in [0,180,28]]
 for index,(style,yaw)in enumerate(views):
  outfit='asta' if style=='asta_forma' else style
  image=Image.open(ASSETS/(f'textures/armor/trajes/{outfit}.png' if outfit in ['gojo','frieren','shadow','asta'] else f'textures/bosses/{style}_skin.png' if style in ['toji','aurora','qual','liebe'] else 'textures/armor/anime_atlas.png' if style in ['toji','aurora','qual','liebe'] else 'textures/armor/anime_reference_atlas.png')).convert('RGBA');texture.release();texture=ctx.texture(image.size,4,image.tobytes());texture.filter=(moderngl.NEAREST,moderngl.NEAREST);texture.use()
  fbo.clear(.14,.15,.18,1,depth=1);model=rotation([0,yaw,0]);mv=view@model
  framing=projection.copy();framing[0,0]=2/(84 if style=='asta_forma' else 48 if style=='liebe' else 34)
  if style=='asta_forma':
   framing[1,1]=2/64;mv=lookat(np.array([0.,26.,-60.]),np.array([0.,26.,0.]))@model
  if boss_scale:
   scale={'toji':1,'qual':2.5,'liebe':.85}[style];model=model@np.diag([scale,scale,scale,1]);framing[0,0]=2/84;framing[1,1]=2/110;mv=lookat(np.array([0.,52.,-60.]),np.array([0.,52.,0.]))@model
  program['Matrix'].write((framing@mv).T.astype('f4').tobytes());program['ModelView'].write(mv.T.astype('f4').tobytes())
  pose_name=['idle','walk','cast'][index%3] if poses else 'idle'
  buf=ctx.buffer(mesh(style,pose_name).tobytes());vao=ctx.vertex_array(program,[(buf,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);vao.render(moderngl.TRIANGLES)
  tile=Image.frombytes('RGBA',(width,height),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  data=np.asarray(tile);assert data[:,:,:3].std()>10,f'Blank model render: {style}, std={data[:,:,:3].std()}'
  x=index%3*width;y=index//3*height;sheet.paste(tile.convert('RGB'),(x,y))
  if boss_scale:
   for block in range(7):
    line=y+int(height*(.5+(52-block*16)/110));draw.line((x,line,x+width,line),fill=(90,94,104));draw.text((x+4,line-12),str(block)+' blocos',fill=(180,188,200))
  draw.text((x+12,y+12),style.upper()+(' - '+pose_name.upper() if poses else ' - FRENTE' if yaw==0 else ' - COSTAS' if yaw==180 else ' - PERSPECTIVA'),fill=(230,235,245));vao.release();buf.release()
 draw.text((12,height*rows+12),'MODELOS E TEXTURA REAIS EM OPENGL'+(' - CHEFES' if nemesis else ' - MANEQUIM NEUTRO'),fill=(230,235,245))
 draw.text((12,height*rows+32),'Sem animacao, iluminacao ou renderizador do Minecraft.',fill=(160,178,202))
 output=Path(output);output.parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 print('Armor GPU:',ctx.info['GL_RENDERER']);print('Rendered armor models:',output)
 texture.release();accessory_texture.release();program.release();fbo.release();ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');p.add_argument('--poses',action='store_true');p.add_argument('--boss-scale',action='store_true');p.add_argument('--nemesis',action='store_true');p.add_argument('--asta',action='store_true');a=p.parse_args();render(a.output,a.backend,a.nemesis,a.asta,a.boss_scale,a.poses)
