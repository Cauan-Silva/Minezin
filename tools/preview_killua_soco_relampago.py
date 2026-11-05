"""Offline approval of Killua's G-Z Piercing Lightning (Godspeed mode): geometry from KilluaRelampagoPerfuranteTest
(core/vfx/KilluaRelampagoPerfurante) with the approved cloth aura (AuraTecidoKillua) following the body. Crouch and charge,
body turns into a lightning spear that pierces the target at 6 blocks, radial spikes, shock rings, 1 s paralysis, fading
trail and scorch. Neutral mannequin with a provisional outfit; no SFX or Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,lookat,mesh,rotation
from preview_killua_base import chave
import preview_killua_aura_tecido as T
from preview_killua_base import W,AZUL,BRANCO,CIANO
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA,rgb
VIOLETA,LILAS2=0x6F6CFF,0xA9B8FF

class Previa(T.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.z=json.loads((ROOT/'build/killua-soco-relampago-samples.json').read_text(encoding='utf-8'))
 def pose(s,seg,pos,yaw=0):
  ossos={o:chave('killua_soco',o,seg) for o in ('right_arm','left_arm','right_leg','left_leg','head')}
  tr=chave('killua_soco','torso',seg);bd=chave('killua_soco','body',seg)
  g=rotation([bd[0],tr[1]+yaw,bd[2]])[:3,:3];m=mesh('ichigo','idle',ossos).copy()
  queda=-chave('killua_soco','body',seg,'position')[1]
  m[:,0:3]=m[:,0:3]@g.T+(pos-CIMA*queda)*16;m[:,3:6]=m[:,3:6]@g.T;return m
 def pos(s,t):
  a=s.z['quadros'][str(int(t))]['corpo'];b=s.z['quadros'][str(min(s.z['fim'],int(t)+1))]['corpo'];u=t-int(t)
  return W({k:a[k]+(b[k]-a[k])*u for k in 'xyz'})
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.z['quadros'][str(int(t))];c=W(q['corpo']);ac=q['alphaCorpo']
  alvo_m=s.boneco(None,0,W(q['atacante']),180)
  if q['alphaParalisia']>.05:alvo_m[:,0:3]+=np.array([math.sin(t*7)*.5*q['alphaParalisia'],0,0])
  ms=[alvo_m];fant=[]
  c=s.pos(t);k=s.pose(t/20,c,q['giro'])
  if ac>.97:ms.append(k)
  else:
   s.fantasma(fant,k,0xE6F4FF,ac*.7)
   for i in range(1,5):
    tt=t-i*.5
    if False:s.fantasma(fant,s.pose(tt/20,s.pos(tt),s.z['quadros'][str(int(tt))]['giro']),0x8FA8FF,.22*(1-i/5))
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(np.concatenate(ms),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  if fant:s.submit(s.add,8,fant,t,True)
  cr=.25+.75*ac
  qa={'corpo':q['corpo'],'linguas':q['linguas'],'envelope':q['envelope'],'explosivos':q['explosivos'],'bordas':q['bordas'],'pontes':[],'crescer':cr}
  qg={'alphaAura':1,'corpo':q['arcosCorpo'],'alphaDescarga':0,'descarga':[]}
  s.aura(qa,qg,t,c);s.golpe(q,t)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def golpe(s,q,t):
  g=[];o=[]
  acg=q['alphaCarga']
  if acg>.01:
   P0=W(q['punho']);s.esfera(g,P0,q['raioEsfera'],BRANCO,acg*.9,6,10);s.esfera(g,P0,q['raioEsfera']*2.2,CIANO,acg*.3,6,10)
   for l in q['arcosPunho']:s.raio(g,l,acg,.55)
   s.raio(g,q['espiralBraco'],acg,.5)
  ai=q['alphaImpacto']
  if ai>.01:
   for l in q['cone']:
    P=[W(p) for p in l]
    for A,B in zip(P,P[1:]):s.rib(g,A,B,.3*ai,ai*.35,VIOLETA);s.rib(g,A,B,.11,ai*.8,LILAS2);s.rib(g,A,B,.04,ai,BRANCO)
   s.esfera(g,W(q['pontoImpacto']),.35+.7*(1-ai),BRANCO,ai*.85,6,10)
  for l,a in zip(q['aneis'],q['alphaAneis']):
   if a>.01:s.linha(g,l,.12*a+.02,a*.85,BRANCO);s.linha(g,l,.32*a,a*.25,CIANO)
  arr=q['alphaRacha']
  if arr>.01:
   for l in q['rachas']:
    P=[W(p) for p in l]
    for A,B in zip(P,P[1:]):s.rib(o,A,B,.16,arr*.7,0x0A0B0E);s.rib(g,A,B,.07,arr*.6,CIANO);s.rib(g,A,B,.025,arr,BRANCO)
  apa=q['alphaParalisia']
  if apa>.01:
   for l in q['arcosAlvo']:s.raio(g,l,apa,.7)
  s.parts(g,q['faiscasImpacto'],CIANO,.035,1,1);s.nuvem(o,q['poeira'],0xB7A98E,.12,.6)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.z;F=d['fim']
 def fase(t):return 'CARGA NO PUNHO' if t<d['carga'] else 'AVANCO + SOCO' if t<d['impacto'] else 'IMPACTO: INTERROMPE (FORTE) + ARREMESSA' if t<d['impacto']+14 else 'RESIDUO'
 lado=(PARTIDA+np.array([7.5,2.6,-3.0]),PARTIDA+np.array([0,1.1,-3.0]));tras=(PARTIDA+np.array([-3.8,3.3,5.8]),PARTIDA+np.array([0,1.2,-3.5]))
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*tras,(w,0,w,h));cena=pv.cena.imagem()
  ai=pv.z['quadros'][str(int(t))]['alphaImpacto']
  if ai>.75:cena=Image.blend(cena,Image.new('RGB',cena.size,(210,220,255)),min(.4,(ai-.6)))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'KILLUA / G-C SOCO RELAMPAGO / {fase(t)} | tick {t:5.1f} de {F} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: alvo a 3,2 blocos canalizando; soco FORTE interrompe; aura aprovada; traje provisorio; sem SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[frames_ for frames_ in []]
 frames+=[tela(float(n)/4,'CAMERA LENTA 4x') for n in range(4*4,20*4+1)]
 with av.open(str(scratch/'killua-soco-relampago.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames+[frames[-1]]*10:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([4,8,10,12,15,22]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-soco-relampago-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
