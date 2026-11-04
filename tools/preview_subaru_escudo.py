"""Offline approval of Subaru's X (Chamado Espiritual de Emergencia): geometry exported by EscudoSubaruTest
(core/vfx/EscudoSubaru) drawn with the mod's GLSL, reusing the Shamak preview scene. Spirits of light spiral in and fuse
into a mana bubble with a rune circle at the feet; a frontal blow (a white bolt from an attacker) lands, a ripple runs over
the shell and it shatters into fragments. The caster is posed by subaru_escudo (player_animation/subaru.json)."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

LUZ_ESP,CIANO,BRANCO,MANA=0xE8FFF6,0x8FE8FF,0xFFFFFF,0x5AA8FF
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_escudo']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def W(q):return PARTIDA+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
def V(q):return LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-escudo-samples.json').read_text(encoding='utf-8'))
  s.atacante=PARTIDA+FRENTE*5
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy();dono[:,0:3]+=PARTIDA*16
  giro=rotation([0,180,0])[:3,:3];ata=mesh('ichigo','idle',{'right_arm':[-90,0,0]} if t>s.d['toque']-6 else {}).copy()
  ata[:,0:3]=ata[:,0:3]@giro.T+s.atacante*16;ata[:,3:6]=ata[:,3:6]@giro.T
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(np.concatenate([dono,ata]),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t):
  d=s.d;q=d['quadros'][repr(round(t*2)/2)]
  ar=q['alphaRunas']
  if ar>.01:
   g=[]
   for a,l in enumerate(q['runas']):
    pts=[W(p) for p in l]
    for i in range(len(pts)):
     if a==1 and i%6>=4:continue
     s.rib(g,pts[i],pts[(i+1)%len(pts)],.03+.01*a,ar*.8,CIANO if a!=1 else LUZ_ESP)
    if a==2:
     for i in range(0,len(pts),8):s.rib(g,pts[i],pts[(i+16)%len(pts)],.015,ar*.6,CIANO)
   s.submit(s.add,1,g,t,True)
  if q['alphaEspiritos']>.01:
   o=[]
   for l in q['espiritos']:
    pts=[W(p) for p in l]
    for i,(a,b) in enumerate(zip(pts,pts[1:])):k=(i+1)/len(pts);s.rib(o,a,b,.05*k,q['alphaEspiritos']*k,CIANO)
    s.esfera(o,pts[-1],.07,LUZ_ESP,q['alphaEspiritos'],4,6)
   s.submit(s.add,1,o,t,True)
  ab=q['alphaBolha']
  if ab>.01:
   c=[[W(p) for p in l] for l in q['bolha']];n=[[V(p) for p in l] for l in q['normais']];M=len(c[0]);o=[];g=[];cl=q['clarao']
   for i in range(len(c)-1):
    for j in range(M):
     idx=[(i,j),(i+1,j),(i+1,(j+1)%M),(i,(j+1)%M)]
     for k in [0,1,2,0,2,3]:
      ii,jj=idx[k];nn=n[ii][jj];vv=s.cam-c[ii][jj];vv=vv/np.linalg.norm(vv);rim=(1-abs(nn@vv))**2.5
      hexa=.5+.5*math.cos(ii*1.7)*math.cos(jj*1.3)
      o.append([*c[ii][jj],*rgb(MANA),ab*.18,0,0]);g.append([*c[ii][jj],*rgb(CIANO if cl<.3 else BRANCO),ab*(.2+.8*rim+.25*hexa*rim)+cl*.35,0,0])
   s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
  ao=q['alphaOnda']
  if ao>.01:
   gp=np.array([d['golpe'][k] for k in 'xyz']);cc=np.array([d['centro'][k] for k in 'xyz']);dirg=gp-cc;dirg/=np.linalg.norm(dirg)
   u=np.cross(dirg,[0,1,0]);u/=np.linalg.norm(u);v=np.cross(dirg,u);ang=q['anguloOnda'];R=1.35;o=[]
   pts=[W(dict(zip('xyz',cc+R*(math.cos(ang)*dirg+math.sin(ang)*(math.cos(b)*u+math.sin(b)*v))))) for b in np.linspace(0,2*math.pi,41)]
   for a,b in zip(pts,pts[1:]):s.rib(o,a,b,.06,ao,BRANCO)
   s.esfera(o,W(dict(zip('xyz',cc+dirg*R))),.25*ao,BRANCO,ao,4,8);s.submit(s.add,1,o,t,True)
  to=d['toque']
  if to-5<=t<=to:
   o=[];a=W(dict(zip('xyz',np.array([d['golpe'][k] for k in 'xyz']))));ini=s.atacante+CIMA*1.3;u=(t-(to-5))/5
   s.rib(o,ini+(a-ini)*max(0,u-.4),ini+(a-ini)*u,.06,1,BRANCO);s.submit(s.add,1,o,t,True)
  o=[];g=[]
  for p,tam in q['fragmentos']:
   if tam>.005:c=W(p);s.esfera(o,c,tam,MANA,.5,3,4);s.esfera(g,c,tam*.8,CIANO,.7,3,4)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=int(pv.d['fim']);I=pv.d['invoca'];Q=int(pv.d['quebra']);T=pv.d['toque']
 lado=(PARTIDA+np.array([6.5,2.4,-2.5]),PARTIDA+np.array([0,1.,-2.]));frente=(PARTIDA+np.array([1.2,2.0,-4.5]),PARTIDA+np.array([0,1.1,0]))
 def fase(t):return 'INVOCACAO' if t<I else 'ESCUDO ATIVO' if t<T else 'ABSORCAO' if t<Q else 'ESTOURO'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*frente,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU / CHAMADO ESPIRITUAL (X) / {fase(t)} | tick {t:5.1f} (escudo {I}, golpe {T:.0f}, estouro {Q}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: golpe simulado aos 3 s (no jogo o escudo dura ate 5 s ou ate absorver 2 coracoes)',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(int(T-8)*2,F*2+1)]
 with av.open(str(scratch/'subaru-escudo.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([2,6,I+2,30,T-1,T+2,Q+2,Q+7]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-escudo-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
