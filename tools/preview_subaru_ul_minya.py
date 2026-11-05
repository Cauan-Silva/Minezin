"""Offline approval of Subaru's G-V (Ul Minya, the contract form's ultimate, cast by Beatrice): geometry from
UlMinyaBeatriceTest (core/vfx/UlMinyaBeatrice) with the mod's GLSL on the Shamak preview scene, reusing the faceted
crystal of the Minya preview. Beatrice rises with her arms up, a huge violet seal opens in the sky over the area ahead
with a warning ring on the ground, 36 crystal stakes rain down, stake and crystallise, a giant crystal cluster grows in
the middle and traps everything, then she sweeps her arms down and every crystal shatters at once in a flash.
Subaru is posed by subaru_ul_minya. Not Minecraft: neutral mannequins, no targets, no SFX."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
import preview_subaru_minya as M
import preview_beatrice as BT
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

VIOLETA,LILAS,BRANCO=0x9B5CFF,0xD9C2FF,0xF2E8FF
LUGAR=np.array([1.15,.55,.45])
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_ul_minya']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
W,V,sv=M.W,M.V,M.sv

class Previa(M.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-ul-minya-samples.json').read_text(encoding='utf-8'))
  # chao maior (60x60 blocos) para caber a area de raio 12
  chao=[]
  for x0 in range(-30,30):
   for z0 in range(-36,24):
    for a,b in [(0,0),(1,0),(1,1),(0,0),(1,1),(0,1)]:chao.append([(x0+a)*16,0,(z0+b)*16,0,1,0,1. if (x0+z0)%2 else 0.,0,1])
  s.cena.chao.release();s.cena.chao=s.ctx.buffer(np.array(chao,dtype='f4').tobytes())
 def sobe_beatrice(s,t):return .9*sv(t/12)*(1-sv((t-s.d['quebra']-6)/14))
 def pose_beatrice(s,t):
  """Sobe erguendo os braços com passada, segura no alto com o idle por baixo, prende o fôlego na carga e varre os
  braços para baixo de uma vez na quebra; depois relaxa."""
  A,Q=s.d['abre'],s.d['quebra']
  def volta(u):u=min(1,max(0,u))-1;return 1+2.1*u**3+1.1*u**2
  alto=volta(t/A)*(1-sv((t-Q+1.5)/2.5));varre=math.exp(-((t-Q-1)/2.2)**2);tensao=sv((t-Q+20)/18)*(t<Q)
  i=lambda f:math.sin(t*.15+f)
  return {'braco_e':[-8+4*i(0)*(1-alto)-150*alto+30*varre,0,-6-26*alto+6*i(1)*alto-10*tensao],
          'braco_d':[-8+4*i(.5)*(1-alto)-150*alto+30*varre,0,6+26*alto-6*i(1.5)*alto+10*tensao],
          'corpo':[-6*alto+10*varre+2*math.sin(t*.12),0,1.5*math.sin(t*.1)],
          'cabeca':[3*math.sin(t*.09)-14*alto+8*varre,5*math.sin(t*.05)*(1-alto),0]}
 def quadro(s,t,cam,alvo,vp,fov=62):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.d['quadros'][repr(round(t*4)/4)]
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([0,tr[1],0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T;dono[:,6]*=.5
  b=BT.beatrice(t+60,[0,0,0],-12,s.pose_beatrice(t));b[:,0:3]+=(PARTIDA+LADO*LUGAR[0]+CIMA*(LUGAR[1]+s.sobe_beatrice(t))+FRENTE*LUGAR[2])*16
  s.cena.desenhar(np.concatenate([dono,b]),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos_ul(t,q);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos_ul(s,t,q):
  o=[];g=[];ac=q['alphaCirculo'];cg=q['carga'];cl=q['clarao']
  if ac>.01:
   for a,l in enumerate(q['circulo']):
    P=[W(p) for p in l]
    for i in range(len(P)):
     if a==1 and i%8>=6:continue
     s.rib(g,P[i],P[(i+1)%len(P)],.12+.04*(a==2),ac*(.8+.2*cg),BRILHO if a==2 else VIOLETA)
    if a==1:
     for i in range(0,len(P),6):s.rib(g,P[i],P[(i+30)%len(P)],.03,ac*.55,VIOLETA)
   for a,b in q['raios']:s.rib(g,W(a),W(b),.08,ac*.7,LILAS)
  az=q['alphaZona']
  if az>.01:
   P=[W(p) for p in q['zona']]
   for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.09,az,VIOLETA)
  C=s.d['comprimento'];R=s.d['raio'];br=1+1.5*cg
  for e in q['estacas']:
   es=e['escala']
   if es>.01:
    c=W(e['centro']);d=V(e['dir']);s.cristal(o,g,c,d,C*es,R*es,1,br)
    if e['rastro']>.01:
     for k in range(6):s.rib(g,c-d*(.4+.9*k),c-d*(1.3+.9*k),.08*(1-k/6),e['rastro']*(1-k/6),VIOLETA)
   pa=W(e['acerto'])
   for sp,tam in e['espinhos']:
    if tam>.01:s.cristal(o,g,pa,V(sp),tam*1.3,tam*.22,1,br*.8)
   if e['impacto']>.01:s.esfera(g,pa+CIMA*.2,.45*(1.4-e['impacto']),BRILHO,e['impacto']*.35,4,8)
   for p,tam in e['cacos']:
    if tam>.005:c=W(p);s.cristal(o,g,c,np.array([math.sin(t+c[0]),1,math.cos(t*1.3+c[2])]),tam*2.2,tam*.7,1,.6)
  ca=W(s.d['centroArea'])
  for gi in q['gigantes']:
   L=gi['comprimento']
   if L>.01:
    e=V(gi['eixo']);s.cristal(o,g,W(gi['base'])+e*L*.25,e,L,gi['raio']*min(1,L/2.5),1,br)
   for p,tam in gi['cacos']:
    if tam>.005:c=W(p);s.cristal(o,g,c,np.array([math.cos(t*.9+c[2]),1,math.sin(t+c[0])]),tam*2.4,tam*.7,1,.7)
  if cl>.01:
   s.esfera(g,ca+CIMA*2.5,5+7*(1-cl),BRILHO,cl*.4,8,14);s.esfera(g,ca+CIMA*2.5,2.4,BRANCO,cl*.6,6,10)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
BRILHO,BRANCO=M.BRILHO,0xFFFFFF

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.d;F=d['fim'];A,Pr,Q=d['abre'],d['prisao'],d['quebra']
 tras=(PARTIDA-LADO*7+CIMA*9-FRENTE*9,PARTIDA+CIMA*7+FRENTE*10);lado=(PARTIDA-LADO*31+CIMA*9+FRENTE*8,PARTIDA+CIMA*7.5+FRENTE*8)
 def fase(t):return 'SELO NO CEU' if t<A else 'CHUVA DE ESTACAS' if t<Pr else 'PRISAO DE CRISTAL' if t<Q else 'QUEBRA'
 def tela(t,tit):
  pv.quadro(t,*tras,(0,0,w,h));pv.quadro(t,*lado,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU + BEATRICE / UL MINYA (G-V) / {fase(t)} | tick {t:5.2f} (chuva {A}, prisao {Pr}, quebra {Q}, fim {F}) | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: area de raio 12 centrada 10 blocos a frente, sem alvos (no jogo: dano por estaca, presos ate a quebra, dano final)',fill=(150,165,190));return im
 frames=[tela(n/2,'VELOCIDADE REAL (40 fps)') for n in range(0,F*2+1)]+[tela(n/4,'CAMERA LENTA 2x') for n in list(range(A*4-8,(A+12)*4))+list(range((Q-6)*4,(Q+12)*4))]
 with av.open(str(scratch/'subaru-ul-minya.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=40);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([6,A+4,A+12,Pr-1,Pr+6,Q-2,Q+2,Q+9]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-ul-minya-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
