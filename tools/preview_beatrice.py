"""Offline approval of the Beatrice companion model (geo/subaru/beatrice.geo.json, built by build_beatrice.py): turntable
sheet (front, side, back, 3/4) and a clip of her floating beside Subaru with an idle bob, skirt sway and the drill
twin-tails swinging segment by segment. Not Minecraft: the caster is the neutral mannequin, no lighting, no contract VFX."""
from pathlib import Path
import math,argparse
import numpy as np,av
from PIL import Image,ImageDraw
from preview_armor_models import ASSETS,mesh,rotation
from preview_asta_devil_union import Cena

def textura_unica(cena,scratch):
 a=Image.open(ASSETS/'textures/armor/trajes/ichigo.png').convert('RGBA');b=Image.open(ASSETS/'textures/entity/subaru/beatrice.png').convert('RGBA').resize(a.size,Image.Resampling.NEAREST)
 im=Image.new('RGBA',(a.width*2,a.height));im.paste(a,(0,0));im.paste(b,(a.width,0));p=Path(scratch)/'tex_beatrice.png';im.save(p)
 vazio=Path(scratch)/'glow_vazio.png';Image.new('RGBA',(8,8),(0,0,0,0)).save(vazio);cena.tex(p,0);cena.tex(vazio,1)
def beatrice(t,pos,giro=0,pose=None):
 rot={}
 for lado,f in (('e',0),('d',1.3)):
  for k in range(8):rot[f'broca_{lado}_{k}']=[3*math.sin(t*.18+f-k*.35)*(1+k*.1),0,(2 if lado=="e" else -2)*math.sin(t*.13+f-k*.3)]
 for i in range(10):rot[f'beatrice_saia_{i}_0']=[-(3+2*math.sin(t*.16+i*.63))*(1 if i<3 else -1 if 5<=i<8 else 0),0,(3+2*math.sin(t*.16+i*.63))*(1 if 3<=i<5 else -1 if i>=8 else 0)]
 rot['saia']=[2.5*math.sin(t*.16),0,2*math.sin(t*.11)]
 rot['braco_e']=[-8+4*math.sin(t*.15),0,-6];rot['braco_d']=[-8+4*math.sin(t*.15+.5),0,6]
 rot['perna_e']=[8+5*math.sin(t*.14),0,0];rot['perna_d']=[14+5*math.sin(t*.14+1),0,0];rot['cabeca']=[3*math.sin(t*.09),8*math.sin(t*.05),0]
 rot.update(pose or {})
 m=mesh('beatrice','idle',None,rot).copy();g=rotation([0,giro,0])[:3,:3]
 m[:,0:3]=m[:,0:3]@g.T+np.asarray(pos)*16+np.array([0,16*(.08*math.sin(t*.2)),0]);m[:,3:6]=m[:,3:6]@g.T
 m[:,6]=.5+m[:,6]*.5;return m
def dono():
 m=mesh('ichigo','idle').copy();m[:,6]*=.5;return m

def render(saida):
 scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True)
 w,h=480,560;c=Cena(w*4,h);textura_unica(c,scratch);folha=Image.new('RGB',(w*4,h+30),(16,18,24));d=ImageDraw.Draw(folha)
 for i,(nome,cam) in enumerate([('FRENTE',(0,1.0,-3.4)),('LADO',(3.4,1.0,0)),('COSTAS',(0,1.1,3.4)),('3/4',(-2.5,1.6,-2.5))]):
  c.desenhar(beatrice(0,[0,0,0]),cam,(0,.8,0),(i*w,0,w,h),fov=40)
 folha.paste(c.imagem(),(0,0))
 for i,n in enumerate(['FRENTE','LADO','COSTAS','3/4']):d.text((i*w+10,h+8),f'BEATRICE / {n}',fill=(235,238,245))
 folha.save(scratch/'beatrice-modelo.png')
 W2,H2=1280,560;c2=Cena(W2,H2);textura_unica(c2,scratch);frames=[]
 for t in range(0,120):
  ang=t*3;cam=(math.sin(math.radians(ang))*-4.8,1.8,-math.cos(math.radians(ang))*4.8)
  c2.desenhar(np.concatenate([dono(),beatrice(t,[1.15,.55,.45],-12)]),cam,(.5,1.1,0),(0,0,W2,H2),fov=45)
  im=Image.new('RGB',(W2,H2+30),(16,18,24));im.paste(c2.imagem(),(0,0));ImageDraw.Draw(im).text((10,H2+8),f'BEATRICE FLUTUANDO AO LADO DO SUBARU | tick {t} | PREVIA OFFLINE (boneco neutro no lugar do Subaru)',fill=(235,238,245));frames.append(im)
 with av.open(str(scratch/'beatrice-flutuando.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=W2,H2+30,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 print('ok')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
