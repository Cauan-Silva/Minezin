"""Offline approval of Killua Hunter Exam outfit (geo/armaduras/killua.geo.json + textures/armor/trajes/killua.png, built by
build_killua_traje.py): a 360 turntable plus front, back and side views, standing still and then walking (simple limb
swing). Real geo + clothing_uv texture. Not Minecraft: no skin under the outfit (head and hands are bare), no lighting."""
from pathlib import Path
import math,argparse
import av
from PIL import Image,ImageDraw
from preview_armor_models import ASSETS,mesh
from preview_asta_devil_union import Cena

def render(saida):
 out=Path(saida);out.mkdir(parents=True,exist_ok=True);w,h=400,560
 cena=Cena(w*4,h);vz=out/'_g.png';Image.new('RGBA',(8,8)).save(vz);glow=cena.tex(vz,1);vz.unlink()
 tex=cena.tex(ASSETS/'textures/armor/trajes/killua.png',0)
 vistas=[('FRENTE',(0,1.3,-4.4)),('COSTAS',(0,1.5,4.4)),('LADO',(4.4,1.3,0))];frames=[]
 for i in range(160):
  anda=i>=60;bal=math.sin((i-60)*.35)*(30 if anda else 0)
  membros={'right_arm':[bal,0,0],'left_arm':[-bal,0,0],'right_leg':[-bal*1.1,0,0],'left_leg':[bal*1.1,0,0]}
  m=mesh('killua','idle',membros);a=math.radians(i*2.25)
  tex.use(0);glow.use(1);cena.desenhar(m,(math.sin(a)*4.3,1.5,-math.cos(a)*4.3),(0,1.0,0),(0,0,w,h),fov=40)
  for k,(n,cam) in enumerate(vistas):tex.use(0);glow.use(1);cena.desenhar(m,cam,(0,.9,0),((k+1)*w,0,w,h),fov=40)
  im=cena.imagem();d=ImageDraw.Draw(im)
  d.text((10,10),'KILLUA - EXAME HUNTER - giro 360',fill=(20,20,30))
  for k,(n,_) in enumerate(vistas):d.text(((k+1)*w+10,10),f'{n} - {"ANDANDO" if anda else "PARADO"} (quadro {i})',fill=(20,20,30))
  d.text((10,h-20),'PREVIA OFFLINE: geo e textura reais do jogo, sem pele por baixo (cabeca e maos nuas), sem luz do Minecraft',fill=(30,30,40))
  frames.append(im)
 with av.open(str(out/'killua-traje.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*4,h,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=w*4//2,h//2;sh=Image.new('RGB',(tw*2,th*2))
 for k,i in enumerate([0,40,75,120]):sh.paste(frames[i].resize((tw,th),Image.Resampling.LANCZOS),(k%2*tw,k//2*th))
 sh.save(out/'killua-traje-fases.png');print('ok',len(frames))
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
