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
  super().__init__(w,h,scratch);s.z=json.loads((ROOT/'build/killua-tempestade-samples.json').read_text(encoding='utf-8'))
 def pose(s,seg,pos,yaw=0):
  ossos={o:chave('killua_tempestade',o,seg) for o in ('right_arm','left_arm','right_leg','left_leg','head')}
  tr=chave('killua_tempestade','torso',seg);bd=chave('killua_tempestade','body',seg)
  g=rotation([bd[0],tr[1]+yaw,bd[2]])[:3,:3];m=mesh('ichigo','idle',ossos).copy()
  queda=-chave('killua_tempestade','body',seg,'position')[1]
  m[:,0:3]=m[:,0:3]@g.T+(pos-CIMA*queda)*16;m[:,3:6]=m[:,3:6]@g.T;return m
 def pos(s,t):
  a=s.z['quadros'][str(int(t))]['corpo'];b=s.z['quadros'][str(min(s.z['fim'],int(t)+1))]['corpo'];u=t-int(t)
  return W({k:a[k]+(b[k]-a[k])*u for k in 'xyz'})
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.z['quadros'][str(int(t))];c=W(q['corpo']);ac=q['alphaCorpo']
  ms=[];fant=[]
  al=q['alvo'];m_=s.boneco(None,0,W({'x':al['x'],'y':al['y']-1.1,'z':al['z']}),180)
  if q['alphaParalisia']>.05:m_[:,0:3]+=np.array([math.sin(t*7)*.3*q['alphaParalisia'],0,0])
  ms.append(m_)
  for f in q['fantasmas']:s.fantasma(fant,s.pose(f['tick']/20+.02,W(f['pos']),f['yaw']),0x9FB4FF,f['alpha'])
  c=s.pos(t);k=s.pose(t/20,c,q['giro'])
  if ac>.97:ms.append(k)
  else:
   s.fantasma(fant,k,0xE6F4FF,ac*.7)
   for i in []:
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
 def plano(s,o,A,B,larg,al,cor):
  d=B-A;d[1]=0;n=np.linalg.norm(d)
  if n<1e-5:return
  p=np.array([-d[2],0,d[0]])/n*larg/2;c=rgb(cor);v=[A+p,A-p,B-p,B+p]
  for k in [0,1,2,0,2,3]:o.append([*v[k],*c,al,0,0])
 def forte(s,g,l,a,w=1.):
  P=[W(p) for p in l]
  for A,B in zip(P,P[1:]):s.rib(g,A,B,.3*w*a,a*.33,VIOLETA);s.rib(g,A,B,.11*w,a*.8,LILAS2);s.rib(g,A,B,.04*w,a,BRANCO)
 def golpe(s,q,t):
  g=[];o=[]
  if q['alphaRaioPrender']>.01:s.forte(g,q['raioPrender'],q['alphaRaioPrender'],.8)
  ap=q['alphaPrisao']
  if ap>.01:
   for l in q['aneisPrisao']:s.linha(g,l,.05,ap*.8,CIANO);s.linha(g,l,.015,ap,BRANCO)
  for l in q['gaiola']:s.forte(g,l,q['alphaGaiola'],.8)
  for x in q['acertos']:s.forte(g,x['raio'],x['alpha'],.5)
  if q['alphaSubida']>.01 and q['rastroSubida']:s.forte(g,q['rastroSubida'],q['alphaSubida'],1.2)
  ac=q['alphaCeu']
  if ac>.01:C=W(q['ceu']);s.esfera(g,C,2.2,0x8FA0FF,ac*.25,8,14);s.esfera(g,C,.9,BRANCO,ac*.7,6,10)
  aco=q['alphaColuna']
  if aco>.01:
   for i,l in enumerate(q['coluna']):s.forte(g,l,aco,3. if i==0 else 1.2)
  ai=q['alphaImpacto']
  if ai>.01:
   C=W(q['chao']);R=q['raioExplosao'];s.esfera(g,C,R,LILAS2,ai*.12,10,18);s.esfera(g,C,R*.5,BRANCO,ai*.4,8,14)
   for l in q['estouro']:s.forte(g,l,ai,1.)
  for l,a in zip(q['aneis'],q['alphaAneis']):
   if a>.01:s.linha(g,l,.18*a+.03,a*.85,BRANCO);s.linha(g,l,.45*a,a*.25,CIANO)
  if q['alphaCratera']>.01:
   ca=q['calor'];P=[W(p) for p in q['borda']];C=W(q['chao'])
   for A,B in zip(P,P[1:]):
    for k in range(5):
     u0,u1=k/5,(k+1)/5;s.plano(o,C+(A-C)*u0+(B-A)*0,C+(A-C)*u1,np.linalg.norm(B-A)*u1+.05,.55-.08*k,0x0C0D10)
    s.plano(o,A,B,.35,.7,0x1A1B20);s.plano(g,A,B,.08,ca*.6,CIANO)
   for l in q['rachas']:
    P=[W(p) for p in l]
    for A,B in zip(P,P[1:]):s.plano(o,A,B,.22,.75,0x08090B);s.plano(g,A,B,.09,ca*.7,CIANO);s.plano(g,A,B,.03,ca,BRANCO)
  apa=q['alphaParalisia']
  if apa>.01:
   for l in q['arcosAlvo']:s.raio(g,l,apa,.7)
  s.parts(g,q['faiscas'],CIANO,.035,1,1);s.nuvem(o,q['poeira'],0x8A8476,.1,.25)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.z;F=d['fim']
 def fase(t):return 'PRENDE O ALVO' if t<d['prender'] else 'TEMPESTADE: 12 TELEPORTES' if t<d['fimGolpes'] else 'SOBE AO CEU' if t<d['fimSubida'] else 'MERGULHO (RELAMPAGO GIGANTE)' if t<d['impacto'] else 'IMPACTO + CRATERA' if t<d['impacto']+20 else 'FIM DO GODSPEED'
 lado=(PARTIDA+np.array([13.,5.5,-4.0]),PARTIDA+np.array([0,4.0,-4.0]));tras=(PARTIDA+np.array([5.5,1.8,2.5]),PARTIDA+np.array([0,2.4,-4.0]))
 def tela(t,tit):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*tras,(w,0,w,h));cena=pv.cena.imagem()
  ai=pv.z['quadros'][str(int(t))]['clarao']*1.2
  if ai>.75:cena=Image.blend(cena,Image.new('RGB',cena.size,(210,220,255)),min(.4,(ai-.6)))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'KILLUA / ULTIMATE: TEMPESTADE DIVINA (G NO GODSPEED) / {fase(t)} | tick {t:5.1f} de {F} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: alvo a 4 blocos; ABSOLUTA; encerra o Godspeed; aura aprovada; traje provisorio; sem SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[frames_ for frames_ in []]
 frames+=[tela(float(n)/2,'CAMERA LENTA 2x') for n in range(8*2,66*2+1)]
 with av.open(str(scratch/'killua-tempestade-divina.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames+[frames[-1]]*10:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([6,20,40,52,60,80]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-tempestade-divina-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
