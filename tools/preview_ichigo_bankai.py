"""Offline approval of Ichigo's Bankai release (G, effect 200): pose desperto_6_200, Zangetsu swapping to Tensa Zangetsu and
the Bankai coat appearing on EntradaBankai.REVELA (the in-game coat layer approximated by boxes here), with the 3D
geometry exported by EntradaBankaiTest. Not Minecraft: placeholder outfit, flat ground, no impact frame/SFX."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT
from preview_ichigo_getsuga import rgb,TINTA,RUBRO,FRENTE,LADO,CIMA,PARTIDA
from preview_ichigo_kit_base import Kit,Wp

class Bankai(Kit):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.e=json.loads((ROOT/'build/ichigo-bankai-entrada-samples.json').read_text(encoding='utf-8'))
 def chamas(s,linhas,a):
  o=[];g=[]
  for j,l in enumerate(linhas):
   pts=[Wp(p,PARTIDA) for p in l]
   for i,(p0,p1) in enumerate(zip(pts,pts[1:])):
    s.rib(o,p0,p1,.14*(1-i/7),a*(1-i/7),TINTA,i/6,(i+1)/6)
    if j%2==0:s.rib(g,p0,p1,.04,a*.7,RUBRO)
  s.submit(s.base,22,o,0,False);s.submit(s.add,1,g,0,True)
 def quadro(s,t,cam,alvo,vp):
  q=s.e['quadros'][repr(round(t*2)/2)];traje=q['traje']
  s.corpo('desperto_6_200',t/20,PARTIDA,cam,alvo,vp,espada='tensa_zangetsu' if traje else 'zangetsu',pulso=q['pulso'])
  s.ctx.enable(moderngl.BLEND)
  if traje:
   c=PARTIDA;s.ctx.depth_mask=True
   o=[]
   def box(ctr,hx,hy,hz,cor):
    v=[ctr+np.array([(-hx,hx)[i&1],(-hy,hy)[i>>1&1],(-hz,hz)[i>>2&1]]) for i in range(8)]
    for f in [(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]:s.quad(o,[s.giro@(v[k]-c)+c for k in f],cor,1)
   box(c+CIMA*1.02,.285,.5,.17,0x15121C);box(c+CIMA*.5+LADO*.13,.15,.3,.165,0x15121C);box(c+CIMA*.5-LADO*.13,.15,.3,.165,0x15121C)
   box(c+CIMA*.22+LADO*.13,.155,.04,.17,0xE5DED6);box(c+CIMA*.22-LADO*.13,.155,.04,.17,0xE5DED6)
   s.submit(s.base,8,o,t,False)
  s.ctx.depth_mask=False
  if q['vazamento']>.02:s.chamas(q['linguas'],q['vazamento']*.85)
  if q['alphaPilar']>.02 and q['altura']>.1:s.malha([[Wp(p,PARTIDA) for p in l] for l in q['pilar']],22,0x2A1A24,RUBRO,q['alphaPilar'],True)
  if q['alphaChoque']>.02 and q['raioChoque']>.1:s.malha([[Wp(p,PARTIDA) for p in l] for l in q['choque']],22,0x2A1A24,RUBRO,q['alphaChoque'],True)
  for g,an,rn in q['nuvens']:
   if an*.9>.02 and rn>.05:s.malha([[Wp(p,PARTIDA) for p in l] for l in g],8,0x7D7670,0,an*.9,False)
  if q['alphaCorte']>.02:s.malha([[Wp(p,PARTIDA) for p in l] for l in q['corte']],22,0x2A1A24,RUBRO,q['alphaCorte'],True)
  if q['aura']>.02:s.chamas(q['chamas'],q['aura']*.8)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)

def render(saida):
 w,h=640,400;out=Path(saida);out.mkdir(parents=True,exist_ok=True);pv=Bankai(w*2,h,out);F=pv.e['fim']
 frente=(PARTIDA+np.array([5,2.8,-9.5]),PARTIDA+np.array([0,1.6,0.]));longe=(PARTIDA+np.array([14,8,-16.]),PARTIDA+np.array([0,6,0.]))
 def tela(t,titulo):
  q=pv.e['quadros'][repr(round(t*2)/2)]
  pv.quadro(t,*frente,(0,0,w,h));pv.quadro(t,*longe,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  d.text((10,h+8),f'ICHIGO / BANKAI (G) / {q["fase"]} | tick {t:5.1f} (grito {pv.e["grito"]}, revelacao {pv.e["revela"]}) | {"TRAJE BANKAI" if q["traje"] else "TRAJE BASE"} | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: pose desperto_6_200, casaco aproximado por caixas (onda do traje vem depois), sem impact frame/SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,F*2+1)]
 with av.open(str(out/'ichigo-bankai.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=int(w*.6),int((h+40)*.6);sh=Image.new('RGB',(tw*2*2,th*4),(16,18,24))
 for i,t in enumerate([10,15,18,26,30,32,36,43]):sh.paste(tela(float(t),'').resize((tw*2,th),Image.Resampling.LANCZOS),(i%2*tw*2,i//2*th))
 sh.save(out/'ichigo-bankai-fases.png');print('ok',len(frames))
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
