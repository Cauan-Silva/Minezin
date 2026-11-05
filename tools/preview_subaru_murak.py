"""Offline approval of Subaru's G-C (Murak, cast by Beatrice): geometry from MurakBeatriceTest (core/vfx/MurakBeatrice)
with the mod's GLSL on the Shamak preview scene. Beatrice snaps her fingers, a violet drop flies to Subaru's chest and
three rune rings open around his ankles, waist and chest; pebbles around him float up. A test jump shows the effect:
he leaps ~4.5 blocks forward, hangs at the top and drifts down slowly with an afterimage trail, landing softly with a
ground ripple. Beatrice follows with a small lag. Subaru is posed by subaru_murak. Not Minecraft: no SFX."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
import preview_beatrice as BT
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

VIOLETA,LILAS,BRANCO,PEDRA=0x8A4DFF,0xD9C2FF,0xFFFFFF,0x8C8478
LUGAR=np.array([1.15,.55,.45])
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_murak']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def W(q):return PARTIDA+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
def V(q):return LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
def sv(a):return 0 if a<=0 else 1 if a>=1 else a*a*(3-2*a)

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-murak-samples.json').read_text(encoding='utf-8'));BT.textura_unica(s.cena,scratch)
 def q(s,t):t=min(max(t,0),s.d['fim']);return s.d['quadros'][repr(round(t*4)/4)]
 def pose_beatrice(s,t):
  """Estalo: ergue a mão, estala com um tranco curto e volta com atraso; depois acompanha o salto com a cabeça."""
  sobe=sv(t/2.5)*(1-sv((t-6)/6));estalo=math.exp(-((t-3)/.9)**2);Sa,To=s.d['salto'],s.d['topo']
  olha=sv((t-Sa)/6)*(1-sv((t-To-14)/16))
  i=lambda f:math.sin(t*.15+f)
  return {'braco_d':[-8+4*i(.5)-70*sobe-14*estalo,0,6+18*sobe],
          'braco_e':[-8+4*i(0),0,-6],
          'corpo':[2*math.sin(t*.12)-4*estalo,0,1.5*math.sin(t*.1)],
          'cabeca':[3*math.sin(t*.09)-24*olha+4*estalo,-10*olha+5*math.sin(t*.05),0]}
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.q(t);c=W(q['corpo'])
  seg=t/20;P={o:chave(o,seg) for o in ('right_arm','left_arm','torso','head','right_leg','left_leg')}
  gt=rotation([0,P['torso'][1],0])[:3,:3];dono=mesh('ichigo','idle',{k:P[k] for k in ('right_arm','left_arm','head','right_leg','left_leg','torso')}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+c*16;dono[:,3:6]=dono[:,3:6]@gt.T;dono[:,6]*=.5
  cb=W(s.q(t-3)['corpo'])-PARTIDA  # ela segue com atraso
  b=BT.beatrice(t+60,[0,0,0],-12,s.pose_beatrice(t));b[:,0:3]+=(PARTIDA+cb+LADO*LUGAR[0]+CIMA*LUGAR[1]+FRENTE*LUGAR[2])*16
  s.cena.desenhar(np.concatenate([dono,b]),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t,q);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t,q):
  o=[];g=[]
  if q['alphaGota']>.01:
   P=[W(p) for p in q['gota']]
   for i,(a,b) in enumerate(zip(P,P[1:])):k=(i+1)/len(P);s.rib(g,a,b,.05*k,k,VIOLETA)
   s.esfera(g,P[-1],.09,LILAS,1,4,6)
  for an in q['aneis']:
   al=an['alpha']
   if al>.01:
    P=[W(p) for p in an['pontos']]
    for i in range(len(P)):
     s.rib(g,P[i],P[(i+1)%len(P)],.04,al*(.55+.35*math.sin(i*1.7+t*.4)),LILAS if i%9==0 else VIOLETA)
     if i%6==0:s.esfera(g,P[i],.06,LILAS,al,3,4)
  for p,tam in q['pedras']:
   if tam>.005:c=W(p);s.esfera(o,c,tam,PEDRA,.95,3,4);s.esfera(g,c,tam*1.3,VIOLETA,.18,3,4)
  for p,al in q['rastro']:
   if al>.01:
    c=W(p)
    for hy,r in ((.15,.5),(.9,.55),(1.35,.48)):
     P=[c+CIMA*hy+LADO*math.cos(a)*r+FRENTE*math.sin(a)*r for a in np.linspace(0,2*math.pi,25)]
     for a,b in zip(P,P[1:]):s.rib(g,a,b,.018,al*.55,VIOLETA)
  ao=q['alphaOnda']
  if ao>.01:
   P=[W(p) for p in q['onda']]
   for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.04,ao,LILAS)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.d;F=d['fim'];Sa,Po,To=d['salto'],d['pouso'],d['topo']
 lado=(PARTIDA+np.array([-11.5,3.4,1.4]),PARTIDA+np.array([0,3.0,1.2]));frente=(PARTIDA+np.array([3.0,3.0,-9.0]),PARTIDA+np.array([.3,3.4,1.2]))
 def fase(t):return 'ESTALO / GOTA' if t<d['gota'] else 'ANEIS E PEDRAS' if t<Sa else 'SALTO' if t<To else 'TOPO / QUEDA LENTA' if t<Po else 'POUSO MACIO'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*frente,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU + BEATRICE / MURAK (G-C) / {fase(t)} | tick {t:5.2f} (salto {Sa}, pouso {Po}, fim {F}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: um salto de teste (no jogo o efeito dura 8 s: salto alto + queda lenta, aneis seguem o jogador)',fill=(150,165,190));return im
 frames=[tela(n/2,'VELOCIDADE REAL (40 fps)') for n in range(0,F*2+1)]+[tela(n/4,'CAMERA LENTA 2x') for n in range(Sa*4-8,Po*4+12)]
 with av.open(str(scratch/'subaru-murak.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=40);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([3,11,Sa-2,Sa+5,To+2,To+14,Po-4,Po+3]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-murak-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
