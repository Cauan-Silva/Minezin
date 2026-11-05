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
  super().__init__(w,h,scratch);s.z=json.loads((ROOT/'build/killua-whirlpool-samples.json').read_text(encoding='utf-8'))
 def pose(s,seg,pos,yaw=0):
  ossos={o:chave('killua_whirlpool',o,seg) for o in ('right_arm','left_arm','right_leg','left_leg','head')}
  tr=chave('killua_whirlpool','torso',seg);bd=chave('killua_whirlpool','body',seg)
  g=rotation([bd[0],tr[1]+yaw,0])[:3,:3];m=mesh('ichigo','idle',ossos).copy()
  queda=.15*max(0,min(1,bd[0]/16)) if seg<.4 else 0
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
    if tt>=s.z['acerto']:s.fantasma(fant,s.pose(tt/20,s.pos(tt),s.z['quadros'][str(int(tt))]['giro']),0x8FA8FF,.22*(1-i/5))
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
  ag=q['alphaGuarda']
  if ag>.01:
   for l in q['cupula']:s.raio(g,l,ag,.55)
   for i,l in enumerate(q['aneis']):s.linha(g,l,.05*ag+.01,ag*(.8-.3*i),BRANCO if i==0 else CIANO)
  asu=q['alphaSumico']
  if asu>.01:
   for l in q['sumico']:
    P=[W(p) for p in l]
    for A,B in zip(P,P[1:]):s.rib(g,A,B,.35*asu,asu*.35,VIOLETA);s.rib(g,A,B,.12,asu*.8,LILAS2);s.rib(g,A,B,.045,asu,BRANCO)
  s.parts(g,q['faiscasSumico'],CIANO,.03,1,1)
  ar=q['alphaRedemoinho']
  if ar>.01:
   for l in q['espirais']:
    P=[W(p) for p in l];n=len(P)
    for i,(A,B) in enumerate(zip(P,P[1:])):k=.5+.5*i/n;s.rib(g,A,B,.26*ar*k,ar*.3*k,VIOLETA);s.rib(g,A,B,.09*k,ar*.75*k,LILAS2);s.rib(g,A,B,.03,ar*k,BRANCO)
   P=[W(p) for p in q['arcoChute']];n=len(P)
   for i,(A,B) in enumerate(zip(P,P[1:])):k=1-i/max(1,n);s.rib(g,A,B,.3*k,ar*.5*k,CIANO);s.rib(g,A,B,.08*k,ar*k,BRANCO)
  ai=q['alphaImpacto']
  if ai>.01:
   for l in q['raiosImpacto']:
    P=[W(p) for p in l]
    for A,B in zip(P,P[1:]):s.rib(g,A,B,.2*ai,ai*.35,VIOLETA);s.rib(g,A,B,.07,ai*.85,LILAS2);s.rib(g,A,B,.025,ai,BRANCO)
  s.parts(g,q['faiscasImpacto'],CIANO,.035,1,1)
  apa=q['alphaParalisia']
  if apa>.01:
   for l in q['arcosAlvo']:s.raio(g,l,apa,.7)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.z;F=d['fim']
 def fase(t):return 'GUARDA (JANELA 10 t)' if t<d['acerto'] else 'ATINGIDO: SOME NUM RAIO' if t<d['reaparece'] else 'CHUTE EM REDEMOINHO' if t<d['fimChute'] else 'ATACANTE INTERROMPIDO + ARREMESSADO'
 lado=(PARTIDA+np.array([7.5,2.8,-1.6]),PARTIDA+np.array([0,1.1,-1.6]));tras=(PARTIDA+np.array([-7.0,3.4,-1.0]),PARTIDA+np.array([0,1.2,-1.4]))
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*tras,(w,0,w,h));cena=pv.cena.imagem()
  ai=pv.z['quadros'][str(int(t))]['alphaImpacto']
  if ai>.75:cena=Image.blend(cena,Image.new('RGB',cena.size,(210,220,255)),min(.4,(ai-.6)))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'KILLUA / G-X WHIRLPOOL (CONTRA-ATAQUE) / {fase(t)} | tick {t:5.1f} de {F} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: atacante golpeia no tick 7 dentro da guarda; sistema de interrupcao; aura aprovada; sem SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[frames_ for frames_ in []]
 frames+=[tela(float(n)/4,'CAMERA LENTA 4x') for n in range(0,26*4+1)]
 with av.open(str(scratch/'killua-whirlpool.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames+[frames[-1]]*10:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([5,8,10,12,15,24]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-whirlpool-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
