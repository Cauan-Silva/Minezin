"""Offline approval of the cloth-energy aura prototype (Killua's Godspeed): entrance from KilluaGodspeedTest, then the
aura made of MalhaAura tongues simulated in AuraTecidoKilluaTest while Killua stands, runs, jumps while running and
stops. Tongues are drawn as smooth glowing surfaces (halo + body + white core, fading to the tips) with lightning running
on their edges. No hair changes. Neutral mannequin with a provisional outfit; no SFX or Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,lookat
import preview_killua_godspeed as G
from preview_killua_base import W,AZUL,BRANCO,CIANO
from preview_subaru_shamak import LADO,CIMA,FRENTE,PARTIDA,rgb

class Previa(G.Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.a=json.loads((ROOT/'build/killua-aura-tecido-samples.json').read_text(encoding='utf-8'))
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  qa=s.a['quadros'][str(int(t))];qg=s.g['quadros'][repr(float(min(150,round(t*2)/2)))]
  c=W(qa['corpo']);k=s.boneco('killua_godspeed',min(t/20,1.5),c)
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(k,cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  br=qg['brilho']
  if br>.01:fant=[];s.fantasma(fant,k,0xBFE6FF,min(.8,br*.85));s.submit(s.add,8,fant,t,True)
  s.entrada(qg,t);s.aura(qa,qg,t,c)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def entrada(s,q,t):
  g=[]
  ac=q['alphaCarga']
  if ac>.01:
   for l in q['bracos']:s.raio(g,l,ac,.55)
   for l in q['chao']:s.raio(g,l,ac*.85,.6)
   s.parts(g,q['faiscasCarga'],CIANO,.03,ac,1)
  ae=q['alphaEstouro']
  if ae>.01:
   for l in q['coluna']:s.raio(g,l,ae,1.5)
   for a,b in q['estouro']:A,B=W(a),W(b);s.rib(g,A,B,.2*ae,ae*.35,AZUL);s.rib(g,A,B,.07*ae,ae*.9,BRANCO)
   for i,l in enumerate(q['ondas']):s.linha(g,l,(.14-.04*i)*ae+.02,ae*(.8-.2*i),BRANCO if i==0 else CIANO)
  s.parts(g,q['faiscasEstouro'],CIANO,.04,1,1)
  cl=q['clarao']
  if cl>.01:c=PARTIDA+CIMA*1.1;s.esfera(g,c,.6+.6*(1-cl),BRANCO,cl,6,10);s.esfera(g,c,1.8,CIANO,cl*.3,6,10)
  s.submit(s.add,1,g,t,True)
 def aura(s,qa,qg,t,c):
  if 'linguas' not in qa:return
  cr=qa['crescer'];v=[];g=[]
  def mais(P,i,j,cor,al):v.append([*P,*rgb(cor),al,0,0])
  for lin in qa['linguas']:
   G_=[[W(p) for p in row] for row in lin];L=len(G_)
   for camada,(esc,cores,amax) in enumerate(((2.2,(AZUL,AZUL,AZUL),.1),(1.4,(CIANO,CIANO,CIANO),.17),(.8,(CIANO,BRANCO,CIANO),.3))):
    R=[]
    for row in G_:
     m=row[1];R.append([m+(p-m)*esc for p in row])
    for i in range(L-1):
     u0,u1=i/(L-1),(i+1)/(L-1)
     for j in range(2):
      quad=[(i,j),(i+1,j),(i+1,j+1),(i,j+1)]
      for kq in [0,1,2,0,2,3]:
       ii,jj=quad[kq];u=ii/(L-1);fl=.8+.2*math.sin(t*2.7+ii*1.3+jj)
       al=cr*amax*fl*(1-u**1.4)*(1 if jj==1 else .55)
       mais(R[ii][jj],ii,jj,cores[jj],al)
  s.submit(s.add,8,v,t,True)
  # brilho interno: envelope eliptico arredondado (mesma forma que a fisica usa para o topo)
  e=[];ev=qa['envelope'];RH,RV=ev[3],ev[4];C0=W({'x':ev[0],'y':ev[1],'z':ev[2]});lat,lon=10,20
  def E(i,j):
   th=math.pi*i/lat;ph=2*math.pi*j/lon
   dx,dy,dz=math.sin(th)*math.cos(ph),-math.cos(th),math.sin(th)*math.sin(ph);tm=(t-14)/20;az=math.atan2(dz,dx)
   lb=.92*(1+.32*(.55*math.sin(az*3+tm*1.3)+.3*math.sin(az*5-tm*2.1+dy*3)+.35*math.sin(dy*4.5-tm*1.7+az*2)))
   return C0+CIMA*(dy*RV*lb)+LADO*dx*RH*lb+FRENTE*-dz*RH*lb
  for i in range(lat):
   for j in range(lon):
    quad=[E(i,j),E(i+1,j),E(i+1,j+1),E(i,j+1)]
    for kq in [0,1,2,0,2,3]:
     P=quad[kq];n=P-C0;n=n/max(1e-6,np.linalg.norm(n));vv=s.cam-P;vv=vv/np.linalg.norm(vv);rim=(1-abs(n@vv))**2.5
     baixo=max(0,min(1,(P[1]-c[1])/.5))
     e.append([*P,*rgb(CIANO),cr*baixo*(.035+.025*rim)*max(0,1-(RH-1.3)*8)*(.85+.15*math.sin(t*1.9+i+j)),0,0])
  s.submit(s.add,8,e,t,True)
  # pulsos de energia subindo pelo centro de cada lingua e brasas soltando das pontas
  for n_,lin in enumerate(qa['linguas']):
   M=[W(row[1]) for row in lin];L=len(M);fase=((t*.09+n_*.37)%1)
   for i,(A,B) in enumerate(zip(M,M[1:])):
    u=i/(L-1);k=math.exp(-((u-fase)/.09)**2)
    if k>.05:s.rib(g,A,B,.09*k,cr*k*.9*(1-u*.5),BRANCO)
   if L>3:
    d=M[-1]-M[-3];nd=np.linalg.norm(d)
    if nd>1e-4:
     d=d/nd
     for b in range(2):
      ph=((t*.21+n_*.53+b*.5)%1);P0=M[-1]+d*(.1+ph*.6)+LADO*math.sin(n_*3.1+b)*.08*ph
      s.rib(g,P0,P0+d*.12,.025,cr*(1-ph)*.9,BRANCO if b==0 else CIANO)
  # chao: anel que pulsa e arcos curtos que estalam em volta dos pes
  if qa['corpo']['y']<.3:
   pe=c+CIMA*.03
   anel=[pe+(LADO*math.cos(2*math.pi*i/48)+FRENTE*math.sin(2*math.pi*i/48))*(1.05+.05*math.sin(t*1.3)) for i in range(49)]
   for A,B in zip(anel,anel[1:]):s.rib(g,A,B,.06,cr*.35,AZUL);s.rib(g,A,B,.018,cr*.6,BRANCO)
   q_=int(t)
   for kk in range(6):
    a0=2*math.pi*((kk*.618+q_*.137)%1);r0,r1=.35,.9+.3*((kk*7+q_*3)%5)/5
    pts=[]
    for i in range(6):
     u=i/5;r=r0+(r1-r0)*u;jit=math.sin(kk*13.1+i*7.7+q_*3.3)*.12*math.sin(math.pi*u)
     pts.append({'x':c[0]+math.cos(a0+jit)*r,'y':.03,'z':-(c[2]-PARTIDA[2])+math.sin(a0+jit)*r})
    s.raio(g,pts,cr*.7,.45)
  aa=qg['alphaAura'] if t<150 else 1
  VIOLETA=0x6F6CFF
  for l in qa['explosivos']:
   P=[W(p) for p in l]
   for A,B in zip(P,P[1:]):s.rib(g,A,B,.2*cr,cr*.35,VIOLETA);s.rib(g,A,B,.09*cr,cr*.8,0xA9B8FF);s.rib(g,A,B,.035*cr,cr,BRANCO)
  for l in qa['bordas']:s.raio(g,l,cr*.8,.5)
  for l in qa['pontes']:s.raio(g,l,cr*.8,.6)
  off={'x':qa['corpo']['x'],'y':qa['corpo']['y'],'z':qa['corpo']['z']}
  def mov(l):return [{'x':p['x']+off['x'],'y':p['y']+off['y'],'z':p['z']+off['z']} for p in l]
  for l in qg['corpo']:s.raio(g,mov(l),cr*.9,.7)
  if qg['alphaDescarga']>.01 and qa['corpo']['y']<.05:s.raio(g,mov(qg['descarga']),cr*qg['alphaDescarga'],1.0)
  s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);d=pv.a;F=d['fim'];A=d['ativa']
 def fase(t):return 'CARGA' if t<A else 'ESTOURO' if t<A+12 else 'AURA PARADO' if t<d['corre'] else 'CORRENDO' if t<d['salto'] else 'SALTO CORRENDO' if t<d['salto']+16 else 'PARANDO' if t<d['para']+8 else 'AURA PARADO'
 def cams(t):
  c=W(pv.a['quadros'][str(int(t))]['corpo'])
  return (c+np.array([7.5,2.0,-1.6]),c+CIMA*1.6),(c+np.array([-3.0,2.4,4.8]),c+CIMA*1.5+FRENTE*-.5)
 def tela(t,tit):
  a,b=cams(t);pv.quadro(t,*a,(0,0,w,h));pv.quadro(t,*b,(w,0,w,h));cena=pv.cena.imagem()
  cl=pv.g['quadros'][repr(float(min(150,round(t*2)/2)))]['clarao']
  if cl>.01:cena=Image.blend(cena,Image.new('RGB',cena.size,(200,230,255)),min(.5,cl*.5))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(cena,(0,0));dr=ImageDraw.Draw(im)
  dr.text((10,h+8),f'KILLUA / G GODSPEED / AURA DE TECIDO (PROTOTIPO) / {fase(t)} | tick {t:5.1f} | {tit}',fill=(235,238,245))
  dr.text((10,h+24),'PREVIA OFFLINE: 14 linguas de tecido de energia (MalhaAura) presas ao corpo; sem mudar cabelo; traje provisorio; sem SFX',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,(A+16)*2+1)]
 with av.open(str(scratch/'killua-aura-tecido.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));dr=ImageDraw.Draw(folha)
 for i,t in enumerate([A+3,40,60,90,108,140]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  dr.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'killua-aura-tecido-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
