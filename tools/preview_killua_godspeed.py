"""Offline approval of Killua's G (Godspeed / Kanmuru): entrance (charge up the arms and over the ground, burst column)
and the continuous aura (body arcs, volumetric boiling shells, arcs inside the volume, orbiting rings, ground discharges). Geometry from
KilluaGodspeedTest (core/vfx/KilluaGodspeed) drawn with the mod's GLSL; pose killua_godspeed from killua.json. Neutral
mannequin with a provisional outfit; no SFX, impact frame (simulated tint only) or Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,lookat
import preview_killua_base as K
from preview_killua_base import W,AZUL,BRANCO,CIANO
import preview_subaru_shamak as SH
K.rgb=SH.rgb
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA

class Previa(K.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.g=json.loads((ROOT/'build/killua-godspeed-samples.json').read_text(encoding='utf-8'))
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.g['quadros'][repr(round(t*2)/2)]
  k=s.boneco('killua_godspeed',min(t/20,1.5),PARTIDA)
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(k,cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  br=q['brilho']
  if br>.01:
   fant=[];s.fantasma(fant,k,0xBFE6FF,min(.8,br*.85));s.submit(s.add,8,fant,t,True)
  s.fx(q,t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def fx(s,q,t):
  g=[];o=[]
  ac=q['alphaCarga']
  if ac>.01:
   for l in q['bracos']:s.raio(g,l,ac,.55)
   for l in q['chao']:s.raio(g,l,ac*.85,.6)
   s.parts(g,q['faiscasCarga'],CIANO,.03,ac,1)
  ae=q['alphaEstouro']
  if ae>.01:
   for l in q['coluna']:s.raio(g,l,ae,1.5)
   for a,b in q['estouro']:A,B=W(a),W(b);s.rib(g,A,B,.2*ae,ae*.35,AZUL);s.rib(g,A,B,.07*ae,ae*.9,BRANCO)
   for i,l in enumerate(q['ondas']):s.linha(g,l,(.14-.04*i)*ae+.02,ae*(.8-.2*i),BRANCO if i==0 else CIANO)
  s.parts(g,q['faiscasEstouro'],CIANO,.04,1,1)
  cl=q['clarao']
  if cl>.01:c=PARTIDA+CIMA*1.1;s.esfera(g,c,.6+.6*(1-cl),BRANCO,cl,6,10);s.esfera(g,c,1.8,CIANO,cl*.3,6,10)
  aa=q['alphaAura']
  if aa>.01:
   v=[];C=PARTIDA+CIMA*1.0
   for c,gr in enumerate(q['cascas']):
    G=[[W(p) for p in lin] for lin in gr];L=len(G);M=len(G[0])
    for i in range(L-1):
     for j in range(M):
      idx=[(i,j),(i+1,j),(i+1,(j+1)%M),(i,(j+1)%M)]
      for kq in [0,1,2,0,2,3]:
       ii,jj=idx[kq];P=G[ii][jj];n=P-C;n=n/max(1e-6,np.linalg.norm(n));vv=s.cam-P;vv=vv/np.linalg.norm(vv)
       rim=(1-abs(n@vv))**2.2;fl=.75+.25*math.sin(t*3.1+ii*1.7+jj*2.3+c)
       al=aa*fl*((.1+.75*rim) if c==0 else (.06+.55*rim) if c==1 else (.03+.38*rim))
       cor=BRANCO if c==0 else CIANO if c==1 else AZUL
       v.append([*P,*K.rgb(cor),al,0,0])
   s.submit(s.add,8,v,t,True)
   for l in q['volume']:s.raio(g,l,aa,1.0)
   for l in q['corpo']:s.raio(g,l,aa,.9)
   for l in q['aneis']:s.raio(g,l,aa,1.1)
   for l,al in q['tentaculos']:
    if al>.01:s.raio(g,l,aa*al,.7)
   if q['alphaDescarga']>.01:s.raio(g,q['descarga'],aa*q['alphaDescarga'],1.0)
   s.parts(g,q['faiscasAura'],CIANO,.03,aa,1)
  s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.g;F=d['fim'];A=d['ativa']
 frente=(PARTIDA+np.array([1.2,1.6,-4.6]),PARTIDA+np.array([0,1.5,0]));lado=(PARTIDA+np.array([6.,2.4,-1.5]),PARTIDA+np.array([0,1.8,0]))
 def fase(t):return 'CARGA' if t<A else 'ESTOURO (ATIVACAO)' if t<A+12 else 'AURA GODSPEED (CONTINUA)'
 def tela(t,tit):
  pv.quadro(t,*frente,(0,0,w,h));pv.quadro(t,*lado,(w,0,w,h));cena=pv.cena.imagem()
  cl=d['quadros'][repr(round(t*2)/2)]['clarao']
  if cl>.01:cena=Image.blend(cena,Image.new('RGB',cena.size,(200,230,255)),min(.5,cl*.5))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'KILLUA / G GODSPEED (KANMURU) / {fase(t)} | tick {t:5.1f} (ativa {A}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: boneco neutro com traje provisorio; aura mostrada por 3 s (no jogo dura a forma inteira); sem SFX/luz do Minecraft',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,(A+16)*2+1)]
 with av.open(str(scratch/'killua-godspeed.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([6,A-1,A+1,A+4,A+20,60]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-godspeed-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
