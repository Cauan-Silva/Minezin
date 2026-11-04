"""Offline approval of Subaru's G-Z (Minya, cast by Beatrice): geometry from MinyaBeatriceTest (core/vfx/MinyaBeatrice)
with the mod's GLSL on the Shamak preview scene. Beatrice raises her hands, a violet circle opens, seven faceted crystal
stakes form in an arc, tremble and fire one by one in a fan, stake into the ground, crystallise in spikes and shatter.
Subaru points (subaru_minya). Not Minecraft: neutral mannequin, no targets, no SFX."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
import preview_beatrice as BT
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

CRISTAL,CRISTAL_ESC,BRILHO,VIOLETA=0xB98CFF,0x5A2FA8,0xF2E8FF,0x9B5CFF
LUGAR=np.array([1.15,.55,.45])
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_minya']
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
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-minya-samples.json').read_text(encoding='utf-8'));BT.textura_unica(s.cena,scratch)
 def pose_beatrice(s,t):
  D,Fm=s.d['disparo'],s.d['forma']
  ergue=sv(t/(Fm*.7));solta=sv((t-D-8)/10);empurra=math.exp(-((t-D-1)/2.5)**2)
  bx=-110*ergue*(1-solta)+empurra*25
  return {'braco_e':[bx,0,-18*ergue*(1-solta)],'braco_d':[bx,0,18*ergue*(1-solta)],'cabeca':[-6*ergue*(1-solta),0,0]}
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
 def cristal(s,o,g,base,eixo,comp,raio,al,brilho=1.):
  """Bipiramide hexagonal alongada (ponta em base+eixo*comp*.75, cauda em base-eixo*comp*.25)."""
  eixo=eixo/np.linalg.norm(eixo);ref=np.array([0,1.,0]) if abs(eixo[1])<.9 else np.array([1.,0,0])
  u=np.cross(eixo,ref);u/=np.linalg.norm(u);v=np.cross(eixo,u)
  ponta=base+eixo*comp*.75;cauda=base-eixo*comp*.25;meio=base+eixo*comp*.18
  anel=[meio+raio*(math.cos(a)*u+math.sin(a)*v) for a in np.linspace(0,2*math.pi,7)[:-1]]
  for i in range(6):
   a,b=anel[i],anel[(i+1)%6]
   for tri in ((a,b,ponta),(b,a,cauda)):
    n=np.cross(tri[1]-tri[0],tri[2]-tri[0]);n/=max(1e-9,np.linalg.norm(n));vv=s.cam-tri[0];vv/=np.linalg.norm(vv)
    cor=sombra(CRISTAL if i%2 else CRISTAL_ESC,.45+.75*max(0,n@LUZ));rim=(1-abs(n@vv))**2
    for p in tri:o.append([*p,*rgb(cor),al*.85,0,0])
    for p in tri:g.append([*p,*rgb(BRILHO),al*(.15+.55*rim)*brilho,0,0])
 def efeitos(s,t,q):
  o=[];g=[];ac=q['alphaCirculo']
  if ac>.01:
   for a,l in enumerate(q['circulo']):
    P=[W(p) for p in l]
    for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.025,ac,VIOLETA if a else BRILHO)
    if a==1:
     for i in range(0,len(P),8):s.rib(g,P[i],P[(i+16)%len(P)],.015,ac*.7,VIOLETA)
  C=s.d['comprimento'];R=s.d['raio']
  for e in q['estacas']:
   es=e['escala']
   if es>.01:
    c=W(e['centro']);d=V(e['dir']);s.cristal(o,g,c,d,C*es,R*es,1)
    if e['rastro']>.01:
     for k in range(6):s.rib(g,c-d*(.3+.5*k),c-d*(.8+.5*k),.05*(1-k/6),e['rastro']*(1-k/6),VIOLETA)
   pa=W(e['acerto'])
   for sp,tam in e['espinhos']:
    if tam>.01:s.cristal(o,g,pa,V(sp),tam*1.4,tam*.22,1,.8)
   if e['impacto']>.01:s.esfera(g,pa+CIMA*.2,.5*(1.4-e['impacto']),BRILHO,e['impacto']*.4,4,8)
   for p,tam in e['cacos']:
    if tam>.005:c=W(p);s.cristal(o,g,c,np.array([math.sin(t+c[0]),1,math.cos(t*1.3+c[2])]),tam*2.2,tam*.7,1,.6)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim'];D=pv.d['disparo'];Fm=pv.d['forma']
 lado=(PARTIDA+np.array([6.5,3.0,-1.5]),PARTIDA+np.array([.6,.9,-3.2]));costas=(PARTIDA+np.array([-1.0,2.3,4.6]),PARTIDA+np.array([1.0,1.0,-5.]))
 def fase(t):return 'FORMACAO' if t<Fm else 'MIRA' if t<D else 'DISPAROS' if t<D+11 else 'CRISTALIZACAO' if t<F-10 else 'QUEBRA'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*costas,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU + BEATRICE / MINYA (G-Z) / {fase(t)} | tick {t:5.2f} (disparos a partir de {D}, fim {F}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: boneco neutro no lugar do Subaru, estacas cravando no chao (sem alvos), sem SFX',fill=(150,165,190));return im
 frames=[tela(n/2,'VELOCIDADE REAL (40 fps)') for n in range(0,F*2+1)]+[tela(n/4,'CAMERA LENTA 2x') for n in range(0,(D+14)*4+1)]
 with av.open(str(scratch/'subaru-minya.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=40);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([5,D-1,D+3,D+9,D+20,F-6]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-minya-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
