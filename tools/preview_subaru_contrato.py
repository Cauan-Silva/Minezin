"""Offline approval of Subaru's G (Contrato com a Beatrice): geometry from ContratoSubaruTest (core/vfx/ContratoSubaru)
with the mod's GLSL on the Shamak preview scene. The contract circle lights under Subaru (subaru_contrato), the door of the
Forbidden Library rises beside him and opens with light, Beatrice (geo/subaru/beatrice) floats out to his shoulder, a mana
thread links them and the circle flares. Not Minecraft: neutral mannequin, door drawn as flat-shaded boxes."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
import preview_beatrice as BT
from preview_subaru_shamak import rgb,sombra,LUZ,LADO,CIMA,FRENTE,PARTIDA

VIOLETA,CLARO,OURO,MADEIRA,MADEIRA_ESC,LUZ_PORTA=0x9B5CFF,0xE6D6FF,0xFFD27A,0x6B3E26,0x3E2316,0xFFF1C9
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_contrato']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def W(q):return PARTIDA+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/subaru-contrato-samples.json').read_text(encoding='utf-8'));BT.textura_unica(s.cena,scratch)
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.d['quadros'][repr(round(t*4)/4)]
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([0,tr[1],0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T;dono[:,6]*=.5;ms=[dono]
  if q['escala']>.01:
   b=BT.beatrice(t,[0,0,0],q['giro']);ri=rotation([q['inclina'],0,0])[:3,:3];b[:,0:3]=(b[:,0:3]-np.array([0,12.,0]))@ri.T+np.array([0,12.,0]);b[:,3:6]=b[:,3:6]@ri.T;b[:,0:3]=b[:,0:3]*q['escala']+W(q['beatrice'])*16;ms.append(b)
  s.cena.desenhar(np.concatenate(ms),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t,q);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def caixa(s,o,centro,meia,R,cor,al):
  c=np.asarray(centro);hx,hy,hz=meia;cs=[c+R@np.array([sx*hx,sy*hy,sz*hz]) for sx in(-1,1) for sy in(-1,1) for sz in(-1,1)]
  for idx,n in [([0,1,3,2],[-1,0,0]),([4,6,7,5],[1,0,0]),([0,4,5,1],[0,-1,0]),([2,3,7,6],[0,1,0]),([0,2,6,4],[0,0,-1]),([1,5,7,3],[0,0,1])]:
   nn=R@np.array(n,float);s.quad(o,[cs[i] for i in idx],sombra(cor,.5+.6*max(0,nn@LUZ)),al)
 def efeitos(s,t,q):
  o=[];g=[];ac=q['alphaCirculo'];cl=q['clarao']
  if ac>.01:
   for a,l in enumerate(q['circulo']):
    P=[W(p) for p in l]
    for i in range(len(P)):s.rib(g,P[i],P[(i+1)%len(P)],.035+.01*a+.04*cl,ac,VIOLETA if a!=1 else CLARO)
   E=[W(p) for p in q['estrela']]
   for k in range(len(E)):s.rib(g,E[k],E[(k+2)%len(E)],.025+.03*cl,ac*.8,VIOLETA)
   for p,acende in q['runas']:
    if acende>.01:c=W(p);s.rib(g,c-LADO*.1,c+LADO*.1,.05,ac*acende,CLARO);s.rib(g,c-FRENTE*.1,c+FRENTE*.1,.05,ac*acende,CLARO)
   if cl>.01:s.esfera(g,PARTIDA+CIMA*.1,1.9*cl+.2,VIOLETA,cl*.12,4,12)
  ap=q['alphaPorta']
  if ap>.01:
   er=q['ergue'];ab=q['abertura'];base=W(s.d['porta']);hgt=2.6*er;Rg=rotation([0,-35,0])[:3,:3]
   L=Rg@np.array([1.,0,0]);topo=base+CIMA*hgt
   for sx in(-1,1):s.caixa(o,base+L*sx*.72+CIMA*hgt/2,(.09,hgt/2,.12),Rg,MADEIRA_ESC,ap)
   s.caixa(o,topo+CIMA*.1,(.85,.12,.14),Rg,MADEIRA_ESC,ap);s.caixa(o,topo+CIMA*.3,(.35,.1,.1),Rg,OURO,ap)
   if hgt>.1:
    for sx in(-1,1):
     ang=sx*100*ab;Rf=Rg@rotation([0,ang,0])[:3,:3];dob=base+L*sx*.63
     s.caixa(o,dob+Rf@np.array([-sx*.31,0,0])+CIMA*hgt/2,(.31,hgt/2-.04,.05),Rf,MADEIRA,ap)
     s.caixa(o,dob+Rf@np.array([-sx*.5,0,-.06])+CIMA*hgt*.5,(.03,.03,.03),Rf,OURO,ap)
    lz=q['luzPorta']
    if lz>.01:
     n=Rg@np.array([0,0,-1.]);c=base+CIMA*hgt/2+n*-.02
     s.quad(g,[c-L*.6-CIMA*hgt/2,c+L*.6-CIMA*hgt/2,c+L*.6+CIMA*hgt/2,c-L*.6+CIMA*hgt/2],LUZ_PORTA,lz*ap*.9)
     for k in range(6):r=.25+.2*k;s.esfera(g,c+n*-.3,r*lz,LUZ_PORTA,lz*ap*.05,4,8)
  af=q['alphaFio']
  if af>.01:
   P=[W(p) for p in q['fio']]
   for A,B in zip(P,P[1:]):s.rib(g,A,B,.03,af,VIOLETA);s.rib(g,A,B,.01,af,CLARO)
  au=q['aura']
  if au>.01:
   for k in range(14):
    a=2*math.pi*k/14+t*.08;h=((t*.04+k*.37)%1);c=PARTIDA+LADO*math.cos(a)*.55+FRENTE*math.sin(a)*.55+CIMA*(.1+1.8*h)
    s.rib(g,c,c+CIMA*.35,.04,au*math.sin(math.pi*h)*.7,VIOLETA)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim'];PO,AB,SA,FE=pv.d['fases']
 lado=(PARTIDA+np.array([1.8,2.3,-6.0]),PARTIDA+np.array([1.2,1.1,0]));costas=(PARTIDA+np.array([-1.4,2.2,4.2]),PARTIDA+np.array([1.0,1.0,-.5]))
 def fase(t):return 'CIRCULO E PORTA' if t<PO else 'PORTA ABRE' if t<AB else 'BEATRICE SAI' if t<SA else 'CONTRATO' if t<FE+8 else 'FORMA ATIVA'
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*costas,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'SUBARU / CONTRATO COM A BEATRICE (G) / {fase(t)} | tick {t:5.1f} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: boneco neutro no lugar do Subaru, porta em caixas simples, sem SFX',fill=(150,165,190));return im
 frames=[tela(n/2,'VELOCIDADE REAL (40 fps)') for n in range(0,F*2+1)]+[tela(n/4,'CAMERA LENTA 2x') for n in range(AB*4-16,(FE+8)*4+1)]
 with av.open(str(scratch/'subaru-contrato.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=40);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([5,PO,AB-2,AB+4,(AB+SA)//2,SA,FE+4,F-2]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-contrato-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
