"""Offline approval of Ichigo's outfits: the shihakusho (armor set, turntable) and the Bankai coat whose tails take the
TecidoIchigo deltas exported by TecidoIchigoTest (idle, running, stopping, turning). Real geo + clothing_uv textures.
Not Minecraft: neutral mannequin skin, simple limb swing while running, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh
from preview_asta_devil_union import Cena

def render(saida,estilo='ichigo_bankai'):
 out=Path(saida);out.mkdir(parents=True,exist_ok=True);w,h=400,560
 cena=Cena(w*4,h);vz=out/'_g.png';Image.new('RGBA',(8,8)).save(vz);glow=cena.tex(vz,1);vz.unlink()
 tex={s:cena.tex(ASSETS/f'textures/armor/trajes/{s}.png',0) for s in ('ichigo',estilo)}
 pano=json.loads((ROOT/'build/ichigo-bankai-cloth-samples.json').read_text(encoding='utf-8'))
 frames=[];vistas=[('FRENTE',(0,1.3,-4.4)),('COSTAS',(0,1.5,4.4)),('LADO',(4.4,1.3,0))]
 for i,q in enumerate(pano):
  corre=q['fase']=='CORRENDO';bal=math.sin(i*.55)*(32 if corre else 0)
  membros={'right_arm':[bal,0,0],'left_arm':[-bal,0,0]}
  ossos={n:[-math.degrees(x),-math.degrees(y),math.degrees(z)] for n,(x,y,z) in q['ossos'].items()}
  a=math.radians(i*2.25)
  tex['ichigo'].use(0);glow.use(1);cena.desenhar(mesh('ichigo','idle',membros),(math.sin(a)*4.3,1.5,-math.cos(a)*4.3),(0,1.0,0),(0,0,w,h),fov=40)
  m=mesh(estilo,'idle',membros,ossos)
  for k,(n,cam) in enumerate(vistas):tex[estilo].use(0);glow.use(1);cena.desenhar(m,cam,(0,.9,0),((k+1)*w,0,w,h),fov=40)
  im=cena.imagem();d=ImageDraw.Draw(im)
  d.text((10,10),'SHIHAKUSHO (BASE) - giro 360',fill=(20,20,30))
  for k,(n,_) in enumerate(vistas):d.text(((k+1)*w+10,10),f'{"MUGETSU" if "mugetsu" in estilo else "BANKAI"} {n} - {q["fase"]} (tick {i})',fill=(20,20,30))
  d.text((10,h-20),'PREVIA OFFLINE: geo e texturas reais do jogo, tecido do TecidoIchigoTest, pele neutra, sem luz do Minecraft',fill=(30,30,40))
  frames.append(im)
 nome='ichigo-mugetsu-forma' if 'mugetsu' in estilo else 'ichigo-trajes'
 with av.open(str(out/(nome+'.mp4')),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*4,h,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=w*4//2,h//2;sh=Image.new('RGB',(tw*2,th*3))
 for k,i in enumerate([3,10,40,60,106,150]):sh.paste(frames[i].resize((tw,th),Image.Resampling.LANCZOS),(k%2*tw,k//2*th))
 sh.save(out/(nome+'-fases.png'));print('ok',len(frames))
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);a.add_argument('--estilo',default='ichigo_bankai');x=a.parse_args();render(x.output,x.estilo)
