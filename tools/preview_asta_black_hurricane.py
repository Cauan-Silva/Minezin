"""Offline Black Hurricane approval: the real Devil Union model standing still, with the geometry exported by
BlackHurricaneAstaTest through the mod's GLSL (ink mode 22, solid red mode 8). Light sky so the black reads.
Not Minecraft: no sword in the hand, no spin animation on the body, no mobs, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT
from preview_asta_devil_union import Cena,pose,modelo
from preview_asta_black_meteorite import Previa,TINTA,VERMELHO,BRANCO,rgb,P

class Furacao(Previa):
 def __init__(s,w,h):
  s.cena=Cena(w,h);s.ctx=s.cena.ctx;s.w,s.h=w,h
  s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.d=json.loads((ROOT/'build/asta-hurricane-samples.json').read_text(encoding='utf-8'));s.du=json.loads((ROOT/'build/asta-devil-union-samples.json').read_text(encoding='utf-8'))
  s.Q=s.d['quadros'];s.I=s.d['estouro'];s.F=s.d['fim']
 def corpo(s,t):return modelo(pose(s.du,60+t)).copy()
 def efeitos(s,t):
  q=s.q(t);o=[];sol=[];V=lambda p:P(p)
  if q['succao']>.01:
   for l in q['fios']:
    for (p0,a0),(p1,a1) in zip(l,l[1:]):
     if max(a0,a1)>.03:s.rib(o,V(p0),V(p1),.09*min(1,a0*1.8),1,TINTA,.09*min(1,a1*1.8))
  af=q['alphaFunil']*min(1,q['funil']*2)
  if af>.01:
   for b in q['bandas']:
    for i,((p0,w0),(p1,w1)) in enumerate(zip(b,b[1:])):s.rib(o,V(p0),V(p1),w0*af,1,TINTA,w1*af,i*.4,i*.4+.4)
  al=q['alphaLaminas']
  if al>.01 and q['fronteira']>.1:
   for lam in q['laminas']:
    for i,((a0,b0),(a1,b1)) in enumerate(zip(lam,lam[1:])):s.quad(o,[V(a0),V(a1),V(b1),V(b0)],[(i*.5,0),((i+1)*.5,0),((i+1)*.5,1),(i*.5,1)],TINTA,al)
  an=q['anel']
  if an>.01:
   R=s.d['raio'];n=64
   for i in range(n):
    a0,a1=i*2*math.pi/n,(i+1)*2*math.pi/n;r0,r1=R-.35*an,R
    pt=lambda a,r:np.array([math.cos(a)*r,.04,math.sin(a)*r])
    s.quad(o,[pt(a0,r0),pt(a1,r0),pt(a1,r1),pt(a0,r1)],[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)],TINTA,1)
  for i,(p,a) in enumerate(q['fragmentos']):
   if a>.05:
    c=V(p);d=-c*np.array([1,0,1]);n=np.linalg.norm(d);d=d/n if n>1e-6 else np.array([1.,0,0])
    s.rib(sol,c,c+d*.5+np.array([0,.15,0]),.12*a,1,BRANCO if i%3==0 else VERMELHO,0)
  s.submit(s.base,22,o,t,False);s.submit(s.base,8,sol,t,False)
 def fase(s,t):return 'SUCCAO' if t<5 else 'FUNIL' if t<6 else 'VARREDURA' if t<s.I else 'ESTOURO' if t<s.I+3 else 'DISSIPA'

VISTAS=[('3/4 ALTO',(11.0,8.5,-12.0),(0,.8,0)),('RENTE AO CHAO',(-2.0,2.0,-14.5),(0,1.4,0))]
def render(saida):
 w,h=640,400;pv=Furacao(w*2,h);out=Path(saida);out.mkdir(parents=True,exist_ok=True)
 def tela(t,titulo):
  for i,(nome,cam,alvo) in enumerate(VISTAS):pv.quadro(t,cam,alvo,(i*w,0,w,h),nome)
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  for i,(nome,_,_) in enumerate(VISTAS):d.text((i*w+10,10),nome,fill=(20,22,30))
  d.text((10,h+8),f'ASTA / BLACK HURRICANE / {pv.fase(t)} | tick {t:5.2f} (dano no tick {pv.I}, raio {pv.d["raio"]}, 360 graus) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: modelo real da Devil Union, sem espada na mao, sem giro do corpo, sem mobs, sem luz do Minecraft. Grade = 1 bloco',fill=(150,165,190));return im
 with av.open(str(out/'asta-black-hurricane.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,pv.F+1)]+[tela(n/4,'CAMERA LENTA 4x') for n in range(0,pv.F*4+1)]
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));d=ImageDraw.Draw(folha)
 for i,t in enumerate([3,6,8,10,12,20]):
  pv.quadro(t,*VISTAS[0][1:],(0,0,w,h),'');pv.quadro(t,*VISTAS[1][1:],(w,0,w,h),'')
  im=pv.cena.imagem().resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  d.text((x+8,y+th+4),f'tick {t} - {pv.fase(t)}',fill=(235,238,245))
 folha.save(out/'asta-black-hurricane-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
