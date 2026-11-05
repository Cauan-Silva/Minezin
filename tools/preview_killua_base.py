"""Offline approval of Killua's whole base kit (Z Narukami, X Rhythm Echo, C Yo-yo, V Assassin Claws): geometry exported
by KilluaBaseTest (core/vfx/KilluaBase) drawn with the mod's GLSL on the Subaru preview scene, poses from
player_animation/killua.json. Neutral mannequin with a provisional outfit (Killua's own outfit is a later step); target
mannequin at 6 blocks. Not Minecraft: no terrain, SFX, impact frames or Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
import preview_subaru_shamak as S
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA,rgb

AZUL,BRANCO,CIANO=0x4FA8FF,0xF2FAFF,0x9FE6FF
ACO,ACO_CLARO,FIO=0x6E7680,0xC9D2DC,0xE8E8E8
FUMACA,SANGUE,OURO,LILAS=0x1C1E26,0xB0101A,0xFFD24A,0xB9C8FF
ANIMS=json.loads((ASSETS/'player_animation/killua.json').read_text(encoding='utf-8'))['animations']
def chave(anim,osso,seg,canal='rotation'):
 b=ANIMS[anim]['bones'][osso]
 if canal not in b:return [0,0,0]
 k=sorted((float(t),v['vector'],v['easing']) for t,v in b[canal].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=S.EASE.get(e,S.EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
def W(q):return PARTIDA+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

TECNICAS={'narukami':('Z','NARUKAMI (PALMA RELAMPAGO)','killua_narukami'),'echo':('X','RHYTHM ECHO (PASSO BORRAO)','killua_echo'),
 'yoyo':('C','YO-YO DE ACO','killua_ioio'),'garras':('V','GARRAS DE ASSASSINO','killua_garras')}

class Previa(S.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.d=json.loads((ROOT/'build/killua-base-samples.json').read_text(encoding='utf-8'))
  s.alvo=PARTIDA+FRENTE*s.d['alvo']
 def boneco(s,anim,seg,pos,giro=0,escala=1.):
  ossos={}
  if anim:ra,la,tr,hd=(chave(anim,o,seg) for o in ('right_arm','left_arm','torso','head'));ossos={'right_arm':ra,'left_arm':la,'head':hd};giro+=tr[1]
  g=rotation([0,giro,0])[:3,:3];m=mesh('ichigo','idle',ossos).copy()
  m[:,0:3]=(m[:,0:3]*escala)@g.T+pos*16;m[:,3:6]=m[:,3:6]@g.T;return m
 def fantasma(s,o,m,cor,al):
  for v in m:o.append([*(v[0:3]/16),*rgb(cor),al,0,0])
 def raio(s,g,pts,al,larg=1.):
  P=[W(p) for p in pts]
  for a,b in zip(P,P[1:]):s.rib(g,a,b,.13*larg,al*.45,AZUL);s.rib(g,a,b,.05*larg,al*.8,CIANO);s.rib(g,a,b,.018*larg,al,BRANCO)
 def quadro(s,tec,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.d[tec]['quadros'][repr(round(t*2)/2)];anim=TECNICAS[tec][2];seg=t/20
  ms=[];fant=[]
  corpo=W(q['corpo'])-CIMA*0 if 'corpo' in q else PARTIDA
  if tec=='yoyo':alvo_m=s.boneco(None,0,PARTIDA+FRENTE*min(3.5,q['mob']),180,.6)
  else:alvo_m=s.boneco(None,0,s.alvo,180)
  if tec=='narukami' and q['alphaParalisia']>.05:alvo_m[:,0:3]+=np.array([math.sin(t*7)*.6,0,0])
  ms.append(alvo_m)
  k=s.boneco(anim,seg,corpo)
  ac=q.get('alphaCorpo',1)
  if ac>.97:ms.append(k)
  elif ac>.01:s.fantasma(fant,k,LILAS if tec=='echo' else 0x5A6478,ac*.5)
  if tec=='echo':
   for p,a in q['imagens']:
    if a>.01:s.fantasma(fant,s.boneco(anim,seg,W(p)),LILAS,a*.35)
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(np.concatenate(ms),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  s.submit(s.base,8,fant,t,False);s.submit(s.add,8,[[*v[:3],*v[3:6],v[6]*.35,0,0] for v in fant],t,True);getattr(s,'fx_'+tec)(q,t)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def parts(s,o,lista,cor,larg,al=1.,cabeca=0,corCabeca=None):
  for q in lista:
   a=W({'x':q[0],'y':q[1],'z':q[2]});b=W({'x':q[3],'y':q[4],'z':q[5]});k=q[6]*al
   if np.linalg.norm(a-b)<1e-4:b=a+CIMA*.02
   s.rib(o,b,a,larg,k,cor)
   if cabeca:s.rib(o,b+(a-b)*.6,a,larg*.45,k,corCabeca or BRANCO)
 def nuvem(s,o,lista,cor,tam,al):
  for q in lista:
   c=W({'x':q[0],'y':q[1],'z':q[2]});s.esfera(o,c,tam*(1.6-q[6]*.6),cor,al*q[6]*.5,4,6)
 def linha(s,o,pts,larg,al,cor,fecha=False):
  P=[W(p) for p in pts]
  for a,b in zip(P,P[1:]):s.rib(o,a,b,larg,al,cor)
 def fx_narukami(s,q,t):
  g=[]
  if q['alphaAura']>.01:
   for l in q['aura']:s.raio(g,l,q['alphaAura']*.85,.55)
  ae=q['alphaEstrela']
  if ae>.01:
   c=W(q['mao']);re=q['raioEstrela']
   s.esfera(g,c,re*.8,BRANCO,ae,5,8);s.esfera(g,c,re*1.6,CIANO,ae*.3,5,8);s.esfera(g,c,re*3,AZUL,ae*.1,5,8)
   for i,(x,y) in enumerate(q['flares']):
    A,B=W(x),W(y);s.rib(g,A,B,.14*ae,ae*.5,CIANO);s.rib(g,A,B,.05*ae,ae,BRANCO)
   for l in q['raiosEstrela']:s.raio(g,l,ae*.9,.8)
   for l in q['faiscas']:s.raio(g,l,ae*.8,.45)
   s.linha(g,q['anelEstrela'],.04,ae*.8,BRANCO)
  ac=q['alphaColuna']
  if ac>.01:
   for (x,y),wd in q['coluna']:
    A,B=W(x),W(y)
    if np.linalg.norm(A-B)>.05:s.rib(g,A,B,wd*2.2,ac*.35,AZUL);s.rib(g,A,B,wd,ac*.75,CIANO if wd<.08 else BRANCO)
   for l in q['ceu']:s.raio(g,l,ac*.9,1.1)
  ar=q['alphaRaio']
  if ar>.01:
   for i,l in enumerate(q['trancados']):s.raio(g,l,ar*(1 if i==0 else .75),2.4 if i==0 else 1.0)
   s.raio(g,q['raio'],ar,1.6)
   for l in q['galhos']:s.raio(g,l,ar*.8,.6)
  ax=q['alphaExplosao']
  if ax>.01:
   for x,y in q['explosao']:
    A,B=W(x),W(y);s.rib(g,A,B,.2*ax,ax*.35,AZUL);s.rib(g,A,B,.07*ax,ax*.9,BRANCO)
  cl=q['clarao']
  if cl>.01:
   c=s.alvo+CIMA*1.6;s.esfera(g,c,.5+.5*(1-cl),BRANCO,cl,6,10);s.esfera(g,c,1.4,CIANO,cl*.35,6,10);s.esfera(g,c,2.6,AZUL,cl*.15,6,10)
  if q['alphaOnda']>.01:
   ao=q['alphaOnda'];s.linha(g,q['ondaV'],.12*ao+.02,ao*.6,CIANO);s.linha(g,q['ondaV'],.035,ao,BRANCO)
   s.linha(g,q['ondaC'],.16*ao+.02,ao*.5,AZUL);s.linha(g,q['ondaC'],.04,ao*.9,BRANCO)
  s.parts(g,q['voo'],CIANO,.04,1,1)
  if q['alphaParalisia']>.01:
   ap=q['alphaParalisia']
   for l in q['paralisia']:s.raio(g,l,ap,.8)
   for l in q['gaiola']:s.raio(g,l,ap*.7,.6)
   s.raio(g,q['anelPar'],ap*.8,.6);s.esfera(g,s.alvo+CIMA*1,1.0,0x7F6BFF,ap*.08,5,8)
  s.submit(s.add,1,g,t,True)
  o=[];s.nuvem(o,q['pouso'],0xB7A98E,.14,1);s.submit(s.base,8,o,t,False)
 def fx_echo(s,q,t):
  g=[];o=[]
  for anel,a in q['passos']:
   if a>.01:s.linha(g,anel,.05*a+.01,a*.7,LILAS);s.linha(g,anel,.015,a,BRANCO)
  s.nuvem(o,q['poeira'],0xB7A98E,.12,1)
  if q['alphaBorrao']>.01:
   for l in q['borrao']:
    P=[W(p) for p in l];n=len(P)
    for i,(a,b) in enumerate(zip(P,P[1:])):k=(i+1)/max(1,n);s.rib(g,a,b,.28*k,q['alphaBorrao']*.35*k,LILAS);s.rib(g,a,b,.06*k,q['alphaBorrao']*.5*k,BRANCO)
  if q['alphaEstouro']>.01:
   ae=q['alphaEstouro']
   for l in q['estouros']:s.linha(g,l,.09*ae+.015,ae*.7,LILAS);s.linha(g,l,.02,ae,BRANCO)
  if q['alphaRiscos']>.01:
   for a,b in q['riscos']:s.rib(g,W(a),W(b),.05,q['alphaRiscos']*.7,LILAS);s.rib(g,W(a),W(b),.015,q['alphaRiscos'],BRANCO)
   for a,b in q['vento']:s.rib(g,W(a),W(b),.025,q['alphaRiscos']*.8,BRANCO)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
 def disco(s,o,g,c,eixo,giro):
  e=np.array([eixo['x'],eixo['y'],eixo['z']]);e=LADO*e[0]+CIMA*e[1]+FRENTE*e[2];u=CIMA;v=np.cross(e,u);v=v/np.linalg.norm(v)
  for lado in (-1,1):
   cc=c+v*lado*.07
   P=[cc+ (u*math.cos(2*math.pi*i/14+giro)+e*math.sin(2*math.pi*i/14+giro))*.22 for i in range(15)]
   for i in range(14):
    for a,b,cor in ((cc,P[i],ACO if i%3 else ACO_CLARO),):s.quad(o,[a,P[i],P[i+1],a],S.sombra(cor,.8+.3*math.cos(i)),1)
    s.quad(o,[P[i],P[i+1],P[i+1]-v*lado*.06,P[i]-v*lado*.06],0x4A5058,1)
   s.esfera(o,cc+v*lado*.01,.06,0xD8DEE6,1,3,6)
  for k in range(3):
   a=giro*1.3+k*2.09;d=u*math.cos(a)+e*math.sin(a);s.rib(g,c+d*.24,c+d*.42,.035,.5,ACO_CLARO)
 def fx_yoyo(s,q,t):
  o=[];g=[]
  for y in q['yoyos']:
   P=[W(p) for p in y['fio']]
   for a,b in zip(P,P[1:]):s.rib(o,a,b,.014,.95,FIO);s.rib(g,a,b,.03,.15,BRANCO)
   c=W(y['pos']);s.disco(o,g,c,y['eixo'],q['giro'])
   ar=q['alphaRastro']
   if ar>.01:
    B=[W(p) for p in y['faixaB']];C=[W(p) for p in y['faixaC']];n=len(B)
    for i in range(n-1):kk=1-i/n;s.quad(g,[B[i],B[i+1],C[i+1],C[i]],ACO_CLARO,ar*.35*kk)
    R=[W(p) for p in y['rastro']]
    for i,(a,b) in enumerate(zip(R,R[1:])):kk=1-i/len(R);s.rib(g,a,b,.05*kk+.01,ar*.9*kk,BRANCO)
   s.nuvem(o,y['poeira'],0xB7A98E,.16,1)
  if q['alphaOnda']>.01:
   ao=q['alphaOnda']
   for i,l in enumerate(q['onda']):s.linha(g,l,.06*ao+.01,ao*(.8 if i==1 else .4),BRANCO if i==1 else ACO_CLARO)
  if q['alphaPuxao']>.01:
   for a,b in q['puxao']:s.rib(g,W(a),W(b),.03,q['alphaPuxao']*.6,BRANCO)
  s.parts(g,q['faiscas'],0xFFB040,.03,1,1,0xFFF2C0)
  if q['acerto']>.05:
   c=PARTIDA+FRENTE*min(3.5,q['mob'])+CIMA*.7;ac=q['acerto']
   for k in range(10):a=k*math.pi/5;d=LADO*math.cos(a)+CIMA*math.sin(a);s.rib(g,c+d*.1,c+d*(.4+.3*(k%2)),.04*ac,ac,BRANCO)
  s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
 def fx_garras(s,q,t):
  o=[];g=[];sombra_=[]
  if q['alphaFumaca']>.01:
   for i,p in enumerate(q['fumaca']):s.esfera(o,W(p),.3+.12*(i%3),FUMACA,q['alphaFumaca']*.55,5,8)
  s.parts(g,q['fragmentos'],0x7A84A0,.04,1)
  if q['alphaAura']>.01:
   aa=q['alphaAura']
   for l in q['fiosAura']:
    P=[W(p) for p in l]
    for i,(a,b) in enumerate(zip(P,P[1:])):k=math.sin(math.pi*(i+.5)/len(P));s.rib(o,a,b,.06*k+.01,aa*.35*k,0x120A1E);s.rib(g,a,b,.015*k,aa*.4*k,0x7A4FB0)
  for anel,a in q['passos']:
   if a>.01:s.linha(g,anel,.025,a*.5,0x7A4FB0)
  if q['unha']>.01:
   m=W(q['mao'])
   for k in range(5):
    d=(FRENTE*.9+CIMA*.3+LADO*(k-2)*.18);d=d/np.linalg.norm(d);a=m+LADO*(k-2)*.06
    s.rib(o,a,a+d*q['unha']*1.8,.035,1,0xE8E4DA);s.rib(g,a,a+d*q['unha']*1.8,.06,.35,0xFF5060)
  if q['alphaBraco']>.01:
   P=[W(p) for p in q['braco']];n=len(P)
   for i,(a,b) in enumerate(zip(P,P[1:])):k=(i+1)/max(1,n);s.rib(g,a,b,.3*k,q['alphaBraco']*.3*k,0xFF8090);s.rib(g,a,b,.08*k,q['alphaBraco']*.7*k,BRANCO)
  if q['alphaCorte']>.01:
   ac=q['alphaCorte']
   for l in q['cortes']:
    P=[W(p) for p in l]
    for i,(a,b) in enumerate(zip(P,P[1:])):kk=math.sin(math.pi*(i+.5)/len(P));s.rib(sombra_,a,b,.16*kk+.02,ac*.75,0x200006);s.rib(g,a,b,.12*kk+.02,ac*.8,0xFF2035);s.rib(g,a,b,.04*kk+.005,ac,BRANCO)
  if q['alphaMeiaLua']>.01:
   am=q['alphaMeiaLua'];P=[W(p) for p in q['meiaLua']]
   for i,(a,b) in enumerate(zip(P,P[1:])):kk=math.sin(math.pi*(i+.5)/len(P));s.rib(g,a,b,.18*kk+.01,am*.45,0xFF3040);s.rib(g,a,b,.05*kk,am*.9,BRANCO)
  if q['alphaImpacto']>.01:
   c=s.alvo+CIMA*1.3+FRENTE*-.5;ai=q['alphaImpacto']
   for k in range(14):
    a=k*math.pi/7;d=LADO*math.cos(a)+CIMA*math.sin(a);r=.8+.6*(k%2)
    s.rib(g,c+d*.2,c+d*r,.05*ai,ai,BRANCO if k%2 else 0xFF3040)
  s.parts(o,q['jorro'],SANGUE,.045,1)
  if q['alphaSangue']>.01:
   for p in q['gotas']:s.esfera(o,W(p),.07,SANGUE,q['alphaSangue'],3,5)
  if q['alphaMancha']>.01:
   for x,y,z,r in q['manchas']:
    c=W({'x':x,'y':y,'z':z});P=[c+(LADO*math.cos(2*math.pi*i/10)+FRENTE*math.sin(2*math.pi*i/10))*r*(1+.25*math.sin(i*2.3)) for i in range(11)]
    for i in range(10):s.quad(o,[c,P[i],P[i+1],c],0x6A0810,q['alphaMancha']*.9)
  if q['critico']>.01:
   c=s.alvo+CIMA*2.4;r=.5*q['critico']
   for d in (LADO,CIMA,(LADO+CIMA)*.7,(LADO-CIMA)*.7):s.rib(g,c-d*r,c+d*r,.05,q['critico'],OURO)
   s.linha(g,[{'x':math.cos(2*math.pi*i/24)*r*1.2,'y':2.4,'z':s.d['alvo']+math.sin(2*math.pi*i/24)*r*1.2} for i in range(25)],.03,q['critico'],OURO)
  s.submit(s.base,8,sombra_,t,False);s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.d
 lado=(PARTIDA+np.array([8.5,2.6,-3.0]),PARTIDA+np.array([0,1.2,-3.]));ombro=(PARTIDA+np.array([2.2,2.4,3.4]),PARTIDA+np.array([0,1.2,-6.]))
 cima=(PARTIDA+np.array([0.01,9.,-.5]),PARTIDA+np.array([0,0,-1.]))
 naru_lado=(PARTIDA+np.array([9.,4.2,-3.4]),PARTIDA+np.array([0,3.6,-4.6]));naru_baixo=(PARTIDA+np.array([4.2,.7,-11.5]),PARTIDA+np.array([0,3.,-4.6]))
 cams={'narukami':(naru_lado,naru_baixo),'echo':(lado,ombro),'yoyo':(lado,cima),'garras':(lado,ombro)}
 def fase(tec,t):
  x=d[tec]
  if tec=='narukami':return 'SALTO' if t<x['salto'] else 'ESTRELA NA MAO' if t<x['descarga'] else 'DESCARGA (COLUNA)' if t<x['impacto']+6 else 'PARALISIA (1 s) + QUEDA' if t<x['pouso'] else 'PARALISIA (1 s)'
  if tec=='echo':return 'PREPARO' if t<4 else 'RITMO + POS-IMAGENS' if t<x['teleporte'] else 'TELEPORTE (4 blocos)'
  if tec=='yoyo':return 'LANCA' if t<x['lanca'] else 'GIRO 360 + PUXAO' if t<x['lanca']+x['giro'] else 'RECOLHE'
  return 'SOME' if t<x['some'] else 'FURTIVO (2 s)' if t<x['golpe']-2 else 'GOLPE CRITICO' if t<x['golpe']+8 else 'SANGRAMENTO'
 def tela(tec,t,tit):
  a,b=cams[tec];pv.quadro(tec,t,*a,(0,0,w,h));pv.quadro(tec,t,*b,(w,0,w,h))
  cena=pv.cena.imagem()
  if tec=='narukami':
   cl=pv.d['narukami']['quadros'][repr(round(t*2)/2)]['clarao']
   if cl>.01:cena=Image.blend(cena,Image.new('RGB',cena.size,(200,230,255)),min(.55,cl*.55))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im);tk,nome,_=TECNICAS[tec]
  dr.text((10,h+8),f'KILLUA / {tk} {nome} / {fase(tec,t)} | tick {t:5.1f} de {d[tec]["fim"]} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: boneco neutro com traje provisorio, alvo a 6 blocos, sem SFX/impact frame/luz do Minecraft',fill=(150,165,190));return im
 lentos={'narukami':(8,30),'echo':(2,20),'yoyo':(0,30),'garras':(42,62)}
 frames=[]
 for tec in TECNICAS:
  F=d[tec]['fim'];frames+=[tela(tec,float(t),'VELOCIDADE REAL') for t in range(0,F+1)]
  a,b=lentos[tec];frames+=[tela(tec,n/2,'CAMERA LENTA 2x') for n in range(a*2,b*2+1)]
  frames+=[frames[-1]]*10
 with av.open(str(scratch/'killua-base.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 marcos={'narukami':[7,13,17,28],'echo':[6,10,14.5,20],'yoyo':[4,12,18,30],'garras':[5,30,49,56]}
 tw,th=int(w*.5),int(h*.5);folha=Image.new('RGB',(tw*2*4,(th+20)*4),(16,18,24));dr=ImageDraw.Draw(folha)
 for r,tec in enumerate(TECNICAS):
  for c,t in enumerate(marcos[tec]):
   im=tela(tec,float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=c*tw*2,r*(th+20);folha.paste(im,(x,y))
   dr.text((x+8,y+th+4),f'{TECNICAS[tec][0]} {fase(tec,t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-base-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
