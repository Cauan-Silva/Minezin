"""Offline approval of Ichigo's Mugetsu (C in the Bankai, effect 530): pose desperto_6_530, Bankai coat until the cocoon
bursts, then the Mugetsu form (long hair, bandages; CamadaHollow parts approximated by boxes), Tensa Zangetsu hidden once
the blade turns to darkness, and the 3D geometry exported by MugetsuIchigoTest. Not Minecraft: no mobs, no impact
frame/SFX, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT
from preview_ichigo_getsuga import FRENTE,LADO,CIMA,PARTIDA
from preview_ichigo_kit_base import Kit,Wp

TREVA=0x141018
class Mugetsu(Kit):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.mg=json.loads((ROOT/'build/ichigo-mugetsu-samples.json').read_text(encoding='utf-8'))
 def box(s,o,ctr,hx,hy,hz,cor):
  c=PARTIDA;v=[ctr+np.array([(-hx,hx)[i&1],(-hy,hy)[i>>1&1],(-hz,hz)[i>>2&1]]) for i in range(8)]
  for f in [(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]:s.quad(o,[s.giro@(v[k]-c)+c for k in f],cor,1)
 def quadro(s,t,cam,alvo,vp):
  q=s.mg['quadros'][repr(round(t*2)/2)];c=PARTIDA
  s.corpo('desperto_6_530',t/20,PARTIDA,cam,alvo,vp,espada='tensa_zangetsu',pulso=q['pulso'])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=True;o=[]
  if q['forma']:
   s.box(o,c+CIMA*1.25-FRENTE*.25,.27,.85,.06,0x0B080E)            # hair down to the knees
   s.box(o,c+CIMA*1.98,.275,.08,.275,0x0B080E)                    # hair top
   s.box(o,c+CIMA*1.6+FRENTE*.02,.27,.12,.28,0x8A949E)            # bandage over the mouth
   s.box(o,c+CIMA*1.18,.27,.3,.145,0x8A949E)                      # chest bandages
  else:
   s.box(o,c+CIMA*1.02,.285,.5,.17,0x15121C);s.box(o,c+CIMA*.5+LADO*.13,.15,.3,.165,0x15121C);s.box(o,c+CIMA*.5-LADO*.13,.15,.3,.165,0x15121C)
  s.submit(s.base,8,o,t,False);s.ctx.depth_mask=False
  if q['aura']>.02:
   for g,e in q['labaredas']:
    G=[[Wp(p,c) for p in l] for l in g];E=[Wp(p,c) for p in e]
    s.malha(G,8,TREVA,0,q['aura'],True)
    s.malha([[E[i]+(G[i][j]-E[i])*1.7 for j in range(len(G[i]))] for i in range(len(G))],8,TREVA,0,q['aura']*.28,False)
   s.caixas([(Wp(p,c),d) for p,d in q['fragmentos']],TREVA)
  if q['alphaLinha']>.02 and q['espessura']>.001:
   e=q['espessura'];H=q['topoLinha'];z0=s.mg['linhaZ'];z1=q['frenteLinha']
   for f,al in ((1,q['alphaLinha']),(q['halo'],q['alphaLinha']*.3)):
    if f<.05:continue
    g=[[c+LADO*x*e*f+CIMA*y+FRENTE*z for x,y in ((-1,0),(1,0),(1,H),(-1,H))] for z in (z0,z1)]
    s.malha(g,8,0x000000,0,al,f==1)
  if q['alphaLamina']>.02 and q['comprimento']>.1:s.malha([[Wp(p,c) for p in l] for l in q['lamina']],8,TREVA,0,q['alphaLamina']*.7,False)
  if q['alphaLamina']>.02:
   g=[]
   for l in q['linguas']:
    pts=[Wp(p,c) for p in l]
    for i,(p0,p1) in enumerate(zip(pts,pts[1:])):s.rib(g,p0,p1,.09*(1-i/6),q['alphaLamina'],TREVA)
   s.submit(s.base,8,g,t,False)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)

def render(saida):
 w,h=640,400;out=Path(saida);out.mkdir(parents=True,exist_ok=True);pv=Mugetsu(w*2,h,out);F=pv.mg['fim']
 perto=(PARTIDA+np.array([4,2.4,-6.]),PARTIDA+np.array([0,1.8,0.]));longe=(PARTIDA+np.array([18,6,4.]),PARTIDA+np.array([0,14,-8.]))
 def tela(t,titulo):
  q=pv.mg['quadros'][repr(round(t*2)/2)]
  pv.quadro(t,*perto,(0,0,w,h));pv.quadro(t,*longe,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  d.text((10,h+8),f'ICHIGO / MUGETSU (C NO BANKAI) / {q["fase"]} | tick {t:5.1f} (forma {pv.mg["forma"]}, golpe {pv.mg["golpe"]}, fim {F} -> Bankai acaba e 60 s sem poderes) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: pose desperto_6_530, forma Mugetsu aproximada por caixas, sem mobs, sem impact frame/SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,F*2+1)]
 with av.open(str(out/'ichigo-mugetsu.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=int(w*.6),int((h+40)*.6);sh=Image.new('RGB',(tw*2*2,th*4),(16,18,24))
 for i,t in enumerate([24,40,50,51,52,54,57,66]):sh.paste(tela(float(t),'').resize((tw*2,th),Image.Resampling.LANCZOS),(i%2*tw*2,i//2*th))
 sh.save(out/'ichigo-mugetsu-fases.png');(out/'glow_vazio.png').unlink(missing_ok=True);print('ok',len(frames))
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
