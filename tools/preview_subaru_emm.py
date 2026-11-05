"""Offline approval of Subaru's G-X (E.M.M., cast by Beatrice): geometry from EmmBeatriceTest (core/vfx/EmmBeatrice)
with the mod's GLSL on the Shamak preview scene. Beatrice reaches out, a violet thread runs from her hand to Subaru's
chest and a dark Yin shell closes from head and feet to the waist. While it is closed the space around stops: motes
freeze, the ground circle stops turning, and two test bolts halt on the surface, hang there and crumble; the shell only
sinks a little, swallowing the blow. Then it peels from the top and returns to her hands as violet dust.
Subaru is posed by subaru_emm. Not Minecraft: neutral mannequins, no SFX."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
import preview_beatrice as BT
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

ESCURO,VIOLETA,LILAS,BRANCO=0x1C0730,0x8A4DFF,0xD9C2FF,0xFFFFFF
LUGAR=np.array([1.15,.55,.45])
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_emm']
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
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-emm-samples.json').read_text(encoding='utf-8'));BT.textura_unica(s.cena,scratch)
 def pose_beatrice(s,t):
  """Estende a mão para o Subaru com passada, segura enquanto a casca está fechada (com o idle por baixo) e recolhe
  a mão quando a poeira volta."""
  Fe,A,So=s.d['fecha'],s.d['abre'],s.d['solta']
  def volta(u):u=min(1,max(0,u))-1;return 1+2.1*u**3+1.1*u**2
  est=volta(t/6)*(1-sv((t-A-So*.6)/12));puxa=math.exp(-((t-A-So-4)/3)**2)
  i=lambda f:math.sin(t*.15+f)
  return {'braco_d':[-8+4*i(.5)-72*est+18*puxa,0,6+46*est-10*puxa],
          'braco_e':[-8+4*i(0)-18*est,0,-6-10*est],
          'corpo':[4*est+2*math.sin(t*.12),-14*est,-3*est+1.5*math.sin(t*.1)],
          'cabeca':[3*math.sin(t*.09)+6*est,-22*est+5*math.sin(t*.05)*(1-est),0]}
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.d['quadros'][repr(round(t*4)/4)]
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([0,tr[1],0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T;dono[:,6]*=.5
  b=BT.beatrice(t+60,[0,0,0],-12,s.pose_beatrice(t));b[:,0:3]+=(PARTIDA+LADO*LUGAR[0]+CIMA*LUGAR[1]+FRENTE*LUGAR[2])*16
  s.cena.desenhar(np.concatenate([dono,b]),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t,q);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t,q):
  o=[];g=[]
  ac=q['alphaCirculo']
  if ac>.01:
   for a,l in enumerate(q['circulo']):
    P=[W(p) for p in l]
    for i in range(len(P)):
     if a==1 and i%6>=4:continue
     s.rib(g,P[i],P[(i+1)%len(P)],.025,ac*.8,VIOLETA if a else LILAS)
    if a==1:
     for i in range(0,len(P),6):s.rib(g,P[i],P[(i+18)%len(P)],.012,ac*.5,VIOLETA)
  af=q['alphaFio']
  if af>.01:
   P=[W(p) for p in q['fio']];n=max(2,int(len(P)*q['alcanceFio']))
   for i,(a,b) in enumerate(zip(P[:n],P[1:n])):s.rib(g,a,b,.03,af*(.5+.5*i/n),LILAS)
   s.esfera(g,P[n-1],.06,BRANCO,af,4,6)
  ab=q['alphaCasca']
  if ab>.01:
   c=[[W(p) for p in l] for l in q['casca']];n=[[V(p) for p in l] for l in q['normais']];cob=q['cobertura'];M=len(c[0])
   ob=[];gb=[]
   for i in range(len(c)-1):
    for j in range(M):
     idx=[(i,j),(i+1,j),(i+1,(j+1)%M),(i,(j+1)%M)]
     for k in [0,1,2,0,2,3]:
      ii,jj=idx[k];cv=cob[ii]
      if cv<=.01:cv=0
      nn=n[ii][jj];vv=s.cam-c[ii][jj];vv=vv/np.linalg.norm(vv);rim=(1-abs(nn@vv))**2
      ob.append([*c[ii][jj],*rgb(ESCURO),ab*cv*(.92-.25*rim),0,0]);gb.append([*c[ii][jj],*rgb(VIOLETA),ab*cv*(.05+.9*rim),0,0])
   s.submit(s.base,8,ob,t,False);s.submit(s.add,1,gb,t,True)
   cobm=min(cob)
   for l in q['veias']:
    P=[W(p) for p,_ in l]
    for i,(a,b) in enumerate(zip(P,P[1:])):
     iv=min(len(cob)-1,int(round((i/(len(P)-1))*.84*(len(cob)-1)+.08*(len(cob)-1))));br=l[i][1]
     s.rib(g,a,b,.018+.01*br,ab*cob[iv]*br,LILAS if br>.7 else VIOLETA)
  aa=q['alphaAnel']
  if aa>.01:
   P=[W(p) for p in q['anel']]
   for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.02,aa*(.35+.35*math.sin(i*.6)),VIOLETA)
  acg=q['alphaCongelada']
  if acg>.01:
   for p in q['congeladas']:s.esfera(g,W(p),.035,LILAS,acg*.8,3,4)
  for pr in q['projeteis']:
   if pr['alpha']>.01:
    p=W(pr['pos']);ori=W(pr['origem']);d=ori-p;dl=max(1e-6,np.linalg.norm(d));d=d/dl
    rastro=min(.9,dl*.3) if dl>.3 else .25
    s.rib(g,p,p+d*rastro,.05,pr['alpha'],BRANCO);s.esfera(g,p,.07,BRANCO,pr['alpha'],4,6)
   for e,tam in pr['estilhacos']:
    if tam>.005:c=W(e);s.esfera(o,c,tam,0xEDEAF5,.8,3,4);s.esfera(g,c,tam*.7,LILAS,.5,3,4)
  for p,tam in q['poeira']:
   if tam>.005:s.esfera(g,W(p),tam,VIOLETA,.8,3,4)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.d;F=d['fim'];Fe,A,So=d['fecha'],d['abre'],d['solta'];C=d['chegada']
 lado=(PARTIDA+np.array([-6.0,2.4,-1.0]),PARTIDA+np.array([.4,1.,-.6]));frente=(PARTIDA+np.array([-1.6,1.9,-4.6]),PARTIDA+np.array([.3,1.1,0]))
 def fase(t):return 'CONTRATO / FIO' if t<Fe else 'CASCA FECHADA (INVULNERAVEL)' if t<A else 'DESCASCA' if t<A+So else 'POEIRA VOLTA'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*frente,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU + BEATRICE / E.M.M. (G-X) / {fase(t)} | tick {t:5.2f} (fecha {Fe}, abre {A}, fim {F}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: dois projeteis de teste nos ticks %d e %d (no jogo: qualquer dano/efeito anulado por 1,5 s)'%tuple(C),fill=(150,165,190));return im
 frames=[tela(n/2,'VELOCIDADE REAL (40 fps)') for n in range(0,F*2+1)]+[tela(n/4,'CAMERA LENTA 2x') for n in range(0,F*4+1)]
 with av.open(str(scratch/'subaru-emm.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=40);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([3,6,12,C[0]+2,C[1]+4,A+3,A+7,A+So+5]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-emm-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
