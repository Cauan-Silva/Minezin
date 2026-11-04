"""Offline approval of Subaru's C (Instrucao Tatica): geometry from InstrucaoSubaruTest (core/vfx/InstrucaoSubaru) drawn with
the mod's GLSL on the Shamak preview scene. Subaru points and gives the order (subaru_instrucao), a command pulse with a
radial grid runs over the ground and lights the 5 s buff on himself and on two allies at 3 and 5.5 blocks."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA

OURO,BRANCO,VENTO=0xFFC24A,0xFFF6DC,0xBFF2FF
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_instrucao']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def V(q):return LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-instrucao-samples.json').read_text(encoding='utf-8'))
  dirs=[np.zeros(3),np.array([.5,0,-.86]),np.array([-.6,0,-.8])]
  s.pos=[PARTIDA+dv*d for dv,d in zip(dirs,s.d['distancias'])]
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([0,tr[1],0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T;ms=[dono]
  for p in s.pos[1:]:m=mesh('ichigo','idle').copy();m[:,0:3]+=p*16;ms.append(m)
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(np.concatenate(ms),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t):
  q=s.d['quadros'][repr(round(t*2)/2)];g=[]
  ap=q['alphaPulso']
  if ap>.01:
   pts=[PARTIDA+V(p) for p in q['anel']]
   for i in range(len(pts)):s.rib(g,pts[i],pts[(i+1)%len(pts)],.09,ap,OURO);s.rib(g,pts[i],pts[(i+1)%len(pts)],.03,ap,BRANCO)
   for a,b in q['raios']:
    A,B=PARTIDA+V(a),PARTIDA+V(b);s.rib(g,A+(B-A)*.6,B,.03,ap*.8,OURO)
   for i in range(0,len(pts),8):s.rib(g,pts[i],pts[(i+8)%len(pts)]*0+pts[i]+CIMA*.35*ap,.025,ap*.6,OURO)
  for p0,a in zip(s.pos,q['aliados']):
   al=a['alpha']
   if al<.01:continue
   ac=a['acende'];pts=[p0+V(p) for p in a['pe']]
   for i in range(len(pts)):
    if i%4!=3:s.rib(g,pts[i],pts[(i+1)%len(pts)],.04,al*.9,OURO)
   for l,fr in a['espirais']:
    P=[p0+V(p) for p in l];n=len(P)
    for i,(A,B) in enumerate(zip(P,P[1:])):
     u=i/(n-1);d=min(abs(u-fr),1-abs(u-fr));k=math.exp(-(d/.12)**2)
     s.rib(g,A,B,.015+.03*k,al*(.15+.75*k),VENTO)
   for h,ad in a['divisas']:
    c=p0+CIMA*h;L=LADO*.22
    s.rib(g,c-L,c+CIMA*.14,.035,al*ad,OURO);s.rib(g,c+CIMA*.14,c+L,.035,al*ad,OURO)
   if ac>.01:s.esfera(g,p0+CIMA*1,.9,OURO,ac*.12,6,10)
  s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim'];O=pv.d['ordem']
 lado=(PARTIDA+np.array([8.,4.5,1.5]),PARTIDA+np.array([0,.8,-3.]));costas=(PARTIDA+np.array([1.6,2.4,3.4]),PARTIDA+np.array([0,1.,-4.]))
 def fase(t):return 'ORDEM' if t<O else 'PULSO DE COMANDO' if t<O+12 else 'BUFF ATIVO (5 s)'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*costas,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU / INSTRUCAO TATICA (C) / {fase(t)} | tick {t:5.1f} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: Subaru + aliados a 3 e 5,5 blocos; o video corta antes do fim dos 5 s do buff',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,(O+16)*2+1)]
 with av.open(str(scratch/'subaru-instrucao.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([3,O+3,O+6,O+10,30,60]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-instrucao-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
