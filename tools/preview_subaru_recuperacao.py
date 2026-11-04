"""Offline approval of Subaru's V (Recuperacao Psicologica): geometry from RecuperacaoSubaruTest (core/vfx/RecuperacaoSubaru)
drawn with the mod's GLSL on the Shamak preview scene. Hand on the chest and a deep breath (subaru_recuperacao); poison and
slowness strands are pulled out and dissolve, a light pulse leaves the chest and Regeneration I (2 s) shows a beating heart
of light, rings rising from the feet and green motes."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA

VENENO,CINZA,VERDE_VENENO,CURA,BRANCO=0x1F3A14,0x5A5F66,0x7FBF2A,0x7CFFB0,0xF2FFF6
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_recuperacao']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def W(q):return PARTIDA+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-recuperacao-samples.json').read_text(encoding='utf-8'))
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([tr[0],0,0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(dono,cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t):
  d=s.d;q=d['quadros'][repr(round(t*2)/2)];o=[];g=[]
  at=q['alphaToxinas']
  if at>.01:
   for k,l in enumerate(q['toxinas']):
    P=[W(p) for p in l];brilho=VERDE_VENENO if k%3 else CINZA
    for i,(A,B) in enumerate(zip(P,P[1:])):
     u=i/(len(P)-1);s.rib(g,A,B,.012*(1-u),at*.35*(1-u),brilho)
   for p,tam,den,cinza in sorted(q['fumacas'],key=lambda f:-np.linalg.norm(W(f[0])-s.cam)):
    if tam>.01:
     c=W(p);cor=CINZA if cinza else VENENO
     s.esfera(o,c,tam,cor,at*.55*den,5,8);s.esfera(o,c,tam*1.5,cor,at*.15*den,5,8);s.esfera(g,c,tam*.5,CINZA if cinza else VERDE_VENENO,at*.25*den,4,6)
  peito=W(d['peito'])
  ap=q['alphaPulso']
  if ap>.01:
   r=q['raioPulso'];pts=[peito+LADO*math.cos(a)*r+FRENTE*math.sin(a)*r for a in np.linspace(0,2*math.pi,49)]
   for A,B in zip(pts,pts[1:]):s.rib(g,A,B,.07,ap,BRANCO)
   s.esfera(g,peito,r*.25,CURA,ap*.3,6,10)
  ac=q['alphaCura']
  if ac>.01:
   c=q['coracao'];s.esfera(g,peito,c,CURA,ac*.55,6,10);s.esfera(g,peito,c*.55,BRANCO,ac*.8,5,8);s.esfera(g,peito,c*2.2,CURA,ac*.12,6,10)
   for l,aa in q['aneis']:
    P=[W(p) for p in l]
    for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.035,aa*.85,CURA)
   for p,tam in q['particulas']:
    if tam>.004:c=W(p);s.esfera(g,c,tam,CURA,.9,3,5);s.rib(g,c,c-CIMA*tam*4,tam*.6,.5,CURA)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim'];I=pv.d['inspira'];L=pv.d['limpa']
 lado=(PARTIDA+np.array([4.2,2.0,-2.0]),PARTIDA+np.array([0,1.1,0]));frente=(PARTIDA+np.array([.8,1.8,-4.0]),PARTIDA+np.array([0,1.1,0]))
 def fase(t):return 'RESPIRACAO' if t<I else 'LIMPEZA' if t<L else 'REGENERACAO I (2 s)'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*frente,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU / RECUPERACAO PSICOLOGICA (V) / {fase(t)} | tick {t:5.1f} (limpeza {I}-{L}, fim {F}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: verde-escuro = veneno, cinza = lentidao (no jogo so aparecem os efeitos que ele tiver)',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,(L+6)*2+1)]
 with av.open(str(scratch/'subaru-recuperacao.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([3,I+4,L-4,L,L+12,L+28]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-recuperacao-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
