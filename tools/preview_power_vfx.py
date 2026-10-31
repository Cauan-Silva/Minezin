"""GPU review of the actual Shadow/Frieren/Atomic shaders, not a Minecraft screenshot."""
from pathlib import Path
import argparse,math
import numpy as np
import moderngl
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program,mesh

def quad(width,height,color,u=4):
 points=[(-width,-height,0),(width,-height,0),(width,height,0),(-width,height,0)]
 uv=[(0,0),(u,0),(u,1),(0,1)]
 return np.array([[*points[k],*color,*uv[k]] for k in [0,1,2,0,2,3]],dtype='f4')
def ring(inner,outer,normal,color):
 normal=np.asarray(normal,dtype=float);normal/=np.linalg.norm(normal)
 side=np.cross(normal,[0,1,0]);side=side/np.linalg.norm(side) if np.linalg.norm(side)>1e-8 else np.array([1.,0,0])
 up=np.cross(normal,side);out=[]
 for i in range(96):
  a,b=i*math.tau/96,(i+1)*math.tau/96
  p=side*math.cos(a)+up*math.sin(a);q=side*math.cos(b)+up*math.sin(b)
  vertices=[p*inner,q*inner,q*outer,p*outer];uv=[(i*.3,0),((i+1)*.3,0),((i+1)*.3,1),(i*.3,1)]
  for k in [0,1,2,0,2,3]:out.append([*vertices[k],*color,*uv[k]])
 return np.array(out,dtype='f4')
def lightning(color):
 out=[]
 for j in range(8):
  angle=j*math.tau/8;previous=None
  for i in range(9):
   r=1.15+i*.18;p=np.array([math.cos(angle)*r-math.sin(angle)*math.sin(i*2.1+j)*.12,
                         math.sin(angle)*r+math.cos(angle)*math.sin(i*2.1+j)*.12,0.])
   if previous is not None:
    d=p-previous;side=np.cross(d,[0,0,1]);side=side/np.linalg.norm(side)*.08
    points=[previous-side,p-side,p+side,previous+side];uv=[(i*.25,0),((i+1)*.25,0),((i+1)*.25,1),(i*.25,1)]
    for k in [0,1,2,0,2,3]:out.append([*points[k],*color,*uv[k]])
   previous=p
 return np.array(out,dtype='f4')
def render(output,backend=None):
 ctx=moderngl.create_standalone_context(require=320,**({'backend':backend}if backend else{}))
 base=load_program(ctx,'anime_energy');add=load_program(ctx,'anime_energy_additive')
 atomic=load_program(ctx,'atomic_energy','one');gojo=load_program(ctx,'gojo_energy_additive')
 load_roxo=load_program(ctx,'gojo_energy')
 width,height=320,260;fbo=ctx.simple_framebuffer((width,height),components=4);fbo.use()
 proj=np.diag([2/6,2/4.875,-2/100,1.]).astype('f4');proj[2,3]=-1
 view=np.eye(4,dtype='f4');view[2,3]=-8
 violet=(.72,.42,1,1);gold=(.92,.81,.58,1);blue=(.84,.92,1,1)
 def draw(program,mode,vertices,additive=False,time=3.5):
  program['ProjMat'].write(proj.T.tobytes());program['ModelViewMat'].write(view.T.tobytes())
  program['EffectTime'].value=time;program['EffectMode'].value=mode
  if 'Viewer' in program:program['Viewer'].value=(0,0,1)
  ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  vbo=ctx.buffer(vertices.tobytes());vao=ctx.vertex_array(program,[(vbo,'3f 4f 2f','Position','Color','UV0')]);vao.render(moderngl.TRIANGLES);vao.release();vbo.release()
 labels=['CORTE DE SLIME','ZOLTRAAK','BARREIRA HEXAGONAL','BURACO NEGRO','DISCO DE ACRECAO','SOMBRA / MIRAGEM','SELO ARCANO','ATOMIC - NUCLEO','RAIOS DO VAZIO ROXO']
 sheet=Image.new('RGB',(width*3,height*3+65),(10,14,23));text=ImageDraw.Draw(sheet)
 ctx.enable(moderngl.DEPTH_TEST|moderngl.BLEND);ctx.disable(moderngl.CULL_FACE)
 for index,label in enumerate(labels):
  fbo.clear(.028,.036,.055,1,depth=1);ctx.depth_mask=False
  if index==0:
   draw(base,0,quad(2.4,.55,violet));draw(add,1,quad(2.4,.09,violet),True)
  elif index==1:draw(add,1,quad(2.4,.18,blue),True)
  elif index==2:draw(add,2,mesh(1.6,color=blue),True)
  elif index==3:
   ctx.depth_mask=True;draw(base,3,mesh(1,color=blue));ctx.depth_mask=False
   draw(add,4,ring(1.3,2.8,[.3,1,.2],gold),True)
   draw(add,4,ring(1.03,1.11,[0,0,1],blue),True)
  elif index==4:draw(add,4,ring(.8,2.3,[0,0,1],gold),True)
  elif index==5:draw(base,5,quad(.55,1.6,violet,u=1))
  elif index==6:draw(add,6,ring(1.25,1.38,[0,0,1],gold),True)
  elif index==7:draw(atomic,1,mesh(1.5,color=(1,1,1,.7)),True,time=5)
  else:
   draw(load_roxo,2,mesh(1.1,color=(.7,.3,1,1)))
   draw(gojo,8,lightning((.9,.83,1,1)),True)
  tile=Image.frombytes('RGBA',(width,height),fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
  data=np.asarray(tile);assert data[:,:,:3].std()>1.5,f'Blank VFX: {label}'
  x=index%3*width;y=index//3*height;sheet.paste(tile.convert('RGB'),(x,y));text.text((x+12,y+12),label,fill=(230,234,245))
 text.text((12,height*3+12),'MATERIAIS REAIS EM OPENGL - NAO E CAPTURA DO MINECRAFT',fill=(230,234,245))
 text.text((12,height*3+33),'Sem bloom Photon, personagens ou composicao completa de cada habilidade.',fill=(160,178,202))
 output=Path(output);output.parent.mkdir(parents=True,exist_ok=True);sheet.save(output)
 print('Power VFX GPU:',ctx.info['GL_RENDERER']);print('Rendered 9 power materials:',output)
 for p in [base,add,atomic,gojo,load_roxo]:p.release()
 fbo.release();ctx.release()
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--output',required=True);p.add_argument('--backend');a=p.parse_args();render(a.output,a.backend)
