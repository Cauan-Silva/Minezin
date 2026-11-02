"""Offline Devil Union approval: the real GeckoLib model (geo/armaduras/asta_forma) and its texture, bones posed
with the deltas exported by DevilUnionTecidoTest (same conversion as the Frieren cloth preview), the red islands
full-bright like the glowmask. Light sky so the black reads. Not a Minecraft capture: neutral mannequin skin,
a simple limb swing only while running, no entrance bolts, orbs, Minecraft lighting or shader packs."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat

VS='''#version 330
in vec3 Position;in vec3 Normal;in vec2 UV;in float Textured;uniform mat4 Matrix;
out vec2 uv;out vec3 normal;out float textured;void main(){textured=Textured;gl_Position=Matrix*vec4(Position,1);normal=Normal;uv=UV;}'''
FS='''#version 330
uniform sampler2D Tex;uniform sampler2D Glow;uniform int Kind;in vec2 uv;in vec3 normal;in float textured;out vec4 o;
void main(){
 if(Kind==2){o=vec4(mix(vec3(.70,.80,.95),vec3(.47,.62,.90),uv.y),1);return;}
 if(Kind==3){o=vec4(uv.x>.5?vec3(.42,.58,.30):vec3(.39,.54,.28),1);return;}
 vec4 c=textured<.5?vec4(.72,.55,.43,1):texture(Tex,uv);if(c.a<.1)discard;
 float light=.45+.55*max(0.,dot(normalize(normal),normalize(vec3(-.4,.8,1))));
 vec4 g=texture(Glow,uv);o=vec4(textured>.5&&g.a>.5?g.rgb:c.rgb*light,1);}'''

class Cena:
 def __init__(s,w,h,textura='asta_forma'):
  s.ctx=moderngl.create_standalone_context(require=330);s.w,s.h=w,h;s.fbo=s.ctx.simple_framebuffer((w,h),components=4);s.fbo.use()
  s.prog=s.ctx.program(vertex_shader=VS,fragment_shader=FS)
  def tex(p,unit):
   im=Image.open(p).convert('RGBA');t=s.ctx.texture(im.size,4,im.tobytes());t.filter=(moderngl.NEAREST,moderngl.NEAREST);t.use(unit);return t
  s.tex=tex
  s.t0=tex(ASSETS/f'textures/armor/trajes/{textura}.png',0);s.t1=tex(ASSETS/f'textures/armor/trajes/{textura}_glowmask.png',1)
  s.prog['Tex'].value=0;s.prog['Glow'].value=1
  s.ceu=s.ctx.buffer(np.array([[x,y,.999,0,0,1,(x+1)/2,(y+1)/2,1] for x,y in [(-1,-1),(1,-1),(1,1),(-1,-1),(1,1),(-1,1)]],dtype='f4').tobytes())
  chao=[]
  for x0 in range(-8,8):
   for z0 in range(-8,8):
    for a,b in [(0,0),(1,0),(1,1),(0,0),(1,1),(0,1)]:chao.append([(x0+a)*16,0,(z0+b)*16,0,1,0,1. if (x0+z0)%2 else 0.,0,1])
  s.chao=s.ctx.buffer(np.array(chao,dtype='f4').tobytes())
 def lote(s,buf,kind,mat):
  s.prog['Kind'].value=kind;s.prog['Matrix'].write(mat.T.astype('f4').tobytes())
  v=s.ctx.vertex_array(s.prog,[(buf,'3f 3f 2f 1f','Position','Normal','UV','Textured')]);v.render();v.release()
 def desenhar(s,dados,cam,alvo,vp,fov=40):
  ctx=s.ctx;x,y,w,h=vp;ctx.viewport=vp;s.fbo.clear(depth=1,viewport=vp)
  ctx.disable(moderngl.DEPTH_TEST);s.lote(s.ceu,2,np.eye(4));ctx.enable(moderngl.DEPTH_TEST);ctx.disable(moderngl.CULL_FACE)
  f=1/math.tan(math.radians(fov)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  # Model units are pixels (16 per block); the model's front faces -z, so FRENTE looks from -z.
  mat=proj@lookat(np.asarray(cam,float)*16,np.asarray(alvo,float)*16);s.lote(s.chao,3,mat)
  b=s.ctx.buffer(dados.tobytes());s.lote(b,0,mat);b.release()
 def imagem(s):return Image.frombytes('RGBA',(s.w,s.h),s.fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM).convert('RGB')

def pose(dados,t):
 i=min(len(dados)-2,int(t));a=t-i;A,B=dados[i],dados[i+1]
 return {'fase':B['fase'] if a>.5 else A['fase'],'ossos':{n:[x+(y-x)*a for x,y in zip(A['ossos'][n],B['ossos'][n])] for n in A['ossos']}}

def modelo(p,balanco=0):
 # Same conversion as the Frieren cloth preview: GeckoLib setRot deltas -> geo degrees (-x, -y, z).
 rot={n:[-math.degrees(x),-math.degrees(y),math.degrees(z)] for n,(x,y,z) in p['ossos'].items()}
 membros={'right_arm':[math.degrees(balanco),0,0],'left_arm':[-math.degrees(balanco),0,0]} if balanco else None
 return mesh('asta_forma','du_pose',membros,rot)

VISTAS=[('FRENTE',(0,1.6,-7.0)),('COSTAS',(0,1.8,7.0)),('LADO',(7.0,1.6,0)),('3/4 COSTAS',(-5.0,2.4,5.0))]
def painel(dados,saida):
 w,h=520,620;cena=Cena(w,h);folha=Image.new('RGB',(w*4,h+64),(16,18,24));d=ImageDraw.Draw(folha);m=modelo(pose(dados,70))
 for i,(nome,cam) in enumerate(VISTAS):
  cena.desenhar(m,cam,(0,1.75,0),(0,0,w,h));folha.paste(cena.imagem(),(i*w,0));d.text((i*w+12,12),'DEVIL UNION v5 - '+nome,fill=(20,22,30))
 d.text((12,h+12),'Modelo GeckoLib real (geo/armaduras/asta_forma) com textura; vermelho em brilho pleno (glowmask). Repouso, asas abertas.',fill=(230,235,245))
 d.text((12,h+34),'Manequim neutro no lugar da skin; sem entrada em raios, sem orbes, sem iluminacao do Minecraft.',fill=(160,178,202))
 folha.save(saida);print('Painel:',saida)

def video(dados,saida,fps=30):
 w,h=520,580;cena=Cena(w*2,h);cont=av.open(str(saida),'w');st=cont.add_stream('libx264',rate=fps)
 st.width,st.height=w*2,h+36;st.pix_fmt='yuv420p';st.options={'crf':'19'};quadros=int((len(dados)-1)/20*fps)
 for q in range(quadros):
  t=q*20/fps;p=pose(dados,t);m=modelo(p,math.sin(t*.6662)*.6 if p['fase']=='CORRIDA' else 0)
  cena.desenhar(m,(4.8,2.0,-5.6),(0,1.8,0),(0,0,w,h));cena.desenhar(m,(-4.6,2.6,5.6),(0,1.9,0),(w,0,w,h))
  tela=Image.new('RGB',(w*2,h+36),(16,18,24));tela.paste(cena.imagem(),(0,0));d=ImageDraw.Draw(tela)
  d.text((10,h+10),f"DEVIL UNION v5 - asas de morcego batendo, membrana em tecido, cauda encadeada   fase: {p['fase']}   tick {t:5.1f}",fill=(235,238,245))
  d.text((10,10),'3/4 FRENTE',fill=(20,22,30));d.text((w+10,10),'3/4 COSTAS',fill=(20,22,30))
  for pk in st.encode(av.VideoFrame.from_image(tela)):cont.mux(pk)
 for pk in st.encode():cont.mux(pk)
 cont.close();print('Video:',saida,quadros,'quadros')

def espadas(saida):
 """Demon-Slayer Sword: normal (atlas) beside the Devil Union version, idle and during the Black Divider."""
 w,h=360,620;folha=Image.new('RGB',(w*6,h+64),(16,18,24));d=ImageDraw.Draw(folha)
 vazio=Image.new('RGBA',(1,1),(0,0,0,0))
 for col,(estilo,pose,titulo,cam) in enumerate([('matadora','idle','NORMAL',(0,2.0,-5.6)),('matadora_desperta','idle','DESPERTA',(0,2.0,-5.6)),('matadora_desperta','idle','DESPERTA 3/4',(3.8,2.6,-4.0)),
                                                  ('matadora_desperta','idle','DESPERTA COSTAS',(0,2.0,5.6)),('matadora','divider','DIVIDER NORMAL',(0,4.4,-13.0)),('matadora_desperta','divider','DIVIDER DESPERTA',(0,4.4,-13.0))]):
  cena=Cena(w,h,'matadora_desperta')
  if estilo=='matadora':
   im=Image.open(ASSETS/'textures/armor/anime_reference_atlas.png').convert('RGBA');t=cena.ctx.texture(im.size,4,im.tobytes());t.filter=(moderngl.NEAREST,moderngl.NEAREST);t.use(0)
   t1=cena.ctx.texture((1,1),4,vazio.tobytes());t1.use(1)
  # Sword stands on its pommel; the chao quad is skipped by framing high.
  m=mesh(estilo,pose);m[:,1]+=12
  cena.desenhar(m,cam,(0,2.0 if pose=='idle' else 4.4,0),(0,0,w,h),fov=46)
  folha.paste(cena.imagem(),(col*w,0));d.text((col*w+10,12),'MATADORA '+titulo,fill=(20,22,30))
 d.text((12,h+12),'Mesmo cabo e mesmos ossos (root/blade/energia_divider): a empunhadura e a escala do Black Divider nao mudam na troca.',fill=(230,235,245))
 d.text((12,h+34),'Troca quando o braco da espada fica preto na entrada da forma e desfaz no fade de saida. Sem iluminacao do Minecraft.',fill=(160,178,202))
 folha.save(saida);print('Espadas:',saida)

if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);o=Path(a.parse_args().output);o.mkdir(parents=True,exist_ok=True)
 dados=json.loads((ROOT/'build/asta-devil-union-samples.json').read_text(encoding='utf-8'))
 painel(dados,o/'devil_union_v5_painel.png');espadas(o/'matadora_desperta.png');video(dados,o/'devil_union_v5_movimento.mp4')
