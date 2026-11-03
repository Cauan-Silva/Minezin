"""Offline Black Meteorite approval: the real Devil Union model (as in preview_asta_devil_union) dashing like the
server moves it (0.9 block/tick up to the target), with the effect geometry exported by BlackMeteoriteAstaTest drawn
through the mod's GLSL (ink mode 22, additive 10/15). Light sky so the black reads. Not Minecraft: no sword in the
hand, no target mob, no generic 309 ring/impact frame, no terrain or Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,lookat
from preview_asta_devil_union import Cena,pose,modelo

TINTA,VERMELHO,BRANCO=0x140A12,0xE01E3A,0xFFF2EE
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def P(q):return np.array([q['x'],q['y'],q['z']],float)
# World: the model faces -z, so the strike goes toward -z and lateral = forward x up = +x.
FRENTE=np.array([0,0,-1.]);LADO=np.array([1.,0,0]);CIMA=np.array([0,1.,0]);PARTIDA=np.array([0,0,6.])
def W(q,o):return o+LADO*q[0]+CIMA*q[1]+FRENTE*q[2]

class Previa:
 def __init__(s,w,h):
  s.cena=Cena(w,h);s.ctx=s.cena.ctx;s.w,s.h=w,h
  s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.d=json.loads((ROOT/'build/asta-meteorite-samples.json').read_text(encoding='utf-8'));s.du=json.loads((ROOT/'build/asta-devil-union-samples.json').read_text(encoding='utf-8'))
  s.Q=s.d['quadros'];s.I=s.d['impacto'];s.F=s.d['fim']
 def q(s,t):return s.Q[repr(round(t*4)/4)]
 def submit(s,prog,mode,verts,t,additive):
  if not verts:return
  ctx=s.ctx;ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  prog['ModelViewMat'].write(s.mv.T.astype('f4').tobytes());prog['ProjMat'].write(s.proj.T.astype('f4').tobytes());prog['EffectTime'].value=t/20;prog['EffectMode'].value=mode;prog['Viewer'].value=(0.,0.,1.)
  b=ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=ctx.vertex_array(prog,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 @staticmethod
 def quad(o,pts,uv,c,a):
  for k in [0,1,2,0,2,3]:o.append([*pts[k],*rgb(c),a,*uv[k]])
 def rib(s,o,a,b,wa,al,c,wb=None,u0=0,u1=1):
  wb=wa if wb is None else wb;sd=np.cross(b-a,s.cam-a);n=np.linalg.norm(sd)
  if n<1e-8:return
  sd/=n;s.quad(o,[a-sd*wa,b-sd*wb,b+sd*wb,a+sd*wa],[(u0,0),(u1,0),(u1,1),(u0,1)],c,al)
 def dono(s,t):return PARTIDA+FRENTE*s.q(t)['avanco']
 def corpo(s,t):
  # Dash: CORRIDA wing pose and a forward lean; the overhead swing leans further, then the stance recovers.
  p=pose(s.du,100+min(t,14)*1.2) if t<16 else pose(s.du,60+t)
  m=modelo(p).copy()
  if not s.q(t)['visivel']:return m[:3]*0
  lean=math.radians(22*min(1,t/2)*(1 if t<12 else max(0,1-(t-12)/5))+14*max(0,1-abs(t-11)/3))
  c,sn=math.cos(lean),math.sin(lean)
  for k in (0,3):
   y,z=m[:,k+1].copy(),m[:,k+2].copy();m[:,k+1]=y*c+z*sn;m[:,k+2]=z*c-y*sn
  o=s.dono(t)*16;m[:,0]+=o[0];m[:,1]+=o[1];m[:,2]+=o[2];return m
 def efeitos(s,t):
  q=s.q(t);O=s.dono(t);I=s.dono(s.I);o=[];ad=[];gl=[];sol=[];sol2=[]
  if q['arranque']>.01:
   for e in q['espinhosArranque']:s.rib(o,PARTIDA+CIMA*.05,W(P(e),PARTIDA),.2*q['arranque'],1,TINTA,.02)
  if q['rastro']>.01 and q['avanco']>.05:
   R=s.d['rastro'];D=q['avanco'];ant=None
   for u,dv,wd in R:
    if u<q['cauda']:ant=None;continue
    pt=PARTIDA+FRENTE*(D*u)+CIMA*1.0+LADO*dv['x']+CIMA*dv['y']
    if ant is not None:s.rib(o,ant[0],pt,ant[1]*q['rastro'],1,TINTA,wd*q['rastro'],u*6,u*6+.15)
    ant=(pt,wd)
   for g,a in q['gotasRastro']:
    if a*q['rastro']>.05:pt=W(P(g),PARTIDA);s.rib(o,pt,pt+CIMA*.3,.11*min(1,a*1.6),1,TINTA,0)
  f=q['feixe']
  if f>.01:
   F=s.d['feixe']
   for i,((p0,w0),(p1,w1)) in enumerate(zip(F,F[1:])):
    a,b=W(P(p0),O),W(P(p1),O);s.rib(o,a,b,w0*f,1,TINTA,w1*f,i*.5,i*.5+.5)
    if i<len(F)*.6:s.rib(sol,a+(s.cam-a)/np.linalg.norm(s.cam-a)*.06,b+(s.cam-b)/np.linalg.norm(s.cam-b)*.06,.05*f*(1-i/len(F)),1,VERMELHO,.05*f*(1-(i+1)/len(F)))
   for l in q['espirais']:
    for i,(p0,p1) in enumerate(zip(l,l[1:])):s.rib(sol,W(P(p0),O),W(P(p1),O),.035*f*(1-i/len(l)),1,VERMELHO,.035*f*(1-(i+1)/len(l)))
  rp=q['reaparece']
  if rp>.01:
   for e in q['estilhacos']:
    pt=W(P(e),O);c=O+CIMA*1.0;d=pt-c;n=np.linalg.norm(d)
    if n>1e-6:s.rib(o,pt,pt+d/n*.45*rp,.12*rp,1,TINTA,0)
  if q['golpe']>.01 and q['varredura']>.01:
   fim=s.d['arco']*q['varredura'];ini=fim-s.d['janela'];n=18
   def L(u,a):r=.7+u*1.9;return W(np.array([0,1.5+math.cos(a)*r,math.sin(a)*r+.3]),O)
   def k(f):return 1-.65*math.sin(math.pi*f)
   for i in range(n):
    f0,f1=i/n,(i+1)/n;a0=ini+(fim-ini)*f0;a1=ini+(fim-ini)*f1
    if a1<=0:continue
    a0=max(0,a0);s.quad(o,[L(k(f0),a0),L(k(f1),a1),L(1,a1),L(1,a0)],[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)],TINTA,q['golpe'])
  if q['cratera']>.05 and q['alphaCratera']>.01:
   a=q['alphaCratera'];B=q['borda'];C=I+CIMA*.03
   for i in range(len(B)):
    b0,b1=W(P(B[i]),I),W(P(B[(i+1)%len(B)]),I)
    for p,uv in ((C,(i*.3,.5)),(b0,(i*.3,0)),(b1,((i+1)*.3,0))):o.append([*p,*rgb(TINTA),a,*uv])
   for r in q['rachaduras']:
    pts=[W(P(p),I)+CIMA*.03 for p in r]
    for p0,p1 in zip(pts,pts[1:]):s.rib(sol,p0,p1,.045,a,VERMELHO)
   for b,h in q['espinhos']:
    if h>.02:base=W(P(b),I);top=base+(base-I)*.18+CIMA*h;s.rib(o,base,top,.22,a,TINTA,.02)
   for g,ag in q['gotas']:
    if ag>.05:pt=W(P(g),I);s.rib(o,pt,pt+CIMA*.32,.13*min(1,ag*1.6),a,TINTA,0)
  e=q['estrela']
  if e>.01:
   c=I+CIMA*1.2;dirc=s.cam-c;dirc/=np.linalg.norm(dirc);hz=np.cross(CIMA,dirc);hz/=np.linalg.norm(hz)
   for v,L in ((CIMA,3.5),(hz,5)):
    for sgn in (1,-1):s.rib(sol,c,c+v*sgn*L*e,.2*e,1,VERMELHO,.0);s.rib(sol2,c+dirc*.05,c+dirc*.05+v*sgn*L*.85*e,.08*e,1,BRANCO,.0)
  s.submit(s.add,10,gl,t,True);s.submit(s.base,22,o,t,False);s.submit(s.base,8,sol,t,False);s.submit(s.base,8,sol2,t,False);s.submit(s.add,10,ad,t,True)
 def quadro(s,t,cam,alvo,vp,rotulo):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(46)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  s.cena.desenhar(s.corpo(t),cam,alvo,vp,fov=46)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.efeitos(t);s.ctx.disable(moderngl.BLEND)
 def fase(s,t):return 'FEIXE' if t<9 else 'GOLPE' if t<s.I else 'IMPACTO' if t<s.I+4 else 'CRATERA'

VISTAS=[('LADO',(12.5,3.0,-.6),(0,1.1,-.6)),('PERSEGUICAO',(-2.8,3.4,12.5),(0,1.1,-1.5))]
def render(saida):
 w,h=640,400;pv=Previa(w*2,h);out=Path(saida);out.mkdir(parents=True,exist_ok=True)
 def tela(t,titulo):
  for i,(nome,cam,alvo) in enumerate(VISTAS):pv.quadro(t,cam,alvo,(i*w,0,w,h),nome)
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  for i,(nome,_,_) in enumerate(VISTAS):d.text((i*w+10,10),nome,fill=(20,22,30))
  d.text((10,h+8),f'ASTA / BLACK METEORITE / {pv.fase(t)} | tick {t:5.2f} (dano no tick {pv.I}, raio 4) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: modelo real da Devil Union, sem espada na mao, sem alvo, sem o anel/impact frame 309 generico, sem luz do Minecraft',fill=(150,165,190));return im
 with av.open(str(out/'asta-black-meteorite.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,pv.F+1)]+[tela(n/4,'CAMERA LENTA 4x') for n in range(0,pv.F*4+1)]
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*3),(16,18,24));d=ImageDraw.Draw(folha)
 for i,t in enumerate([2,6,8.5,10.5,13,20]):
  pv.quadro(t,*VISTAS[0][1:],(0,0,w,h),'');pv.quadro(t,*VISTAS[1][1:],(w,0,w,h),'')
  im=pv.cena.imagem().resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  d.text((x+8,y+th+4),f'tick {t} - {pv.fase(t)}',fill=(235,238,245))
 folha.save(out/'asta-black-meteorite-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
