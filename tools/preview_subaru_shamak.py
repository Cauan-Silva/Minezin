"""Offline Shamak approval (Subaru, Z): the geometry exported by SamakSubaruTest (core/vfx/SamakSubaru) drawn through the
mod's GLSL (dark violet solids in flat-colour mode 8 with a violet additive rim, mode 1 ribbons). The caster is a neutral
mannequin posed by subaru_shamak from player_animation/subaru.json (right arm, left hand on the wrist, torso and head), and
the target is a second mannequin at 8 blocks, face to face. Not Minecraft: no Subaru outfit yet (its own step comes later), no impact frame/SFX, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
from preview_asta_devil_union import Cena

ESCURO,VIOLETA,CLARO,NEVOA=0x1A0B2E,0x9B5CFF,0xD9C2FF,0x2A1444
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
def sombra(c,l):l=max(0,min(1.3,l));return min(255,int((c>>16&255)*l))<<16|min(255,int((c>>8&255)*l))<<8|min(255,int((c&255)*l))
FRENTE=np.array([0,0,-1.]);LADO=np.array([1.,0,0]);CIMA=np.array([0,1.,0]);PARTIDA=np.array([0,0,6.]);OLHO=PARTIDA+CIMA*1.62
def W(q):return OLHO+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
def V(q):return LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
LUZ=np.array([-.4,.8,.6]);LUZ=LUZ/np.linalg.norm(LUZ)
EASE={'linear':lambda u:u,'easeInQuad':lambda u:u*u,'easeOutQuad':lambda u:1-(1-u)**2,'easeInOutSine':lambda u:-(math.cos(math.pi*u)-1)/2}
ANIM=json.loads((ASSETS/'player_animation/subaru.json').read_text(encoding='utf-8'))['animations']['subaru_shamak']
def chave(osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=EASE.get(e,EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]

class Previa:
 def __init__(s,w,h,scratch):
  s.cena=Cena(w,h);s.ctx=s.cena.ctx
  s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.d=json.loads((ROOT/'build/subaru-shamak-samples.json').read_text(encoding='utf-8'))
  vazio=Path(scratch)/'glow_vazio.png';Image.new('RGBA',(64,64),(0,0,0,0)).save(vazio)
  s.traje=s.cena.tex(ASSETS/'textures/armor/trajes/ichigo.png',0);s.glow=s.cena.tex(vazio,1)
  s.alvo=PARTIDA+FRENTE*s.d['alcance']
 def quad(s,o,pts,c,a,u=(0,1)):
  uv=[(u[0],0),(u[1],0),(u[1],1),(u[0],1)]
  for k in [0,1,2,0,2,3]:o.append([*pts[k],*rgb(c),a,*uv[k]])
 def rib(s,o,a,b,wd,al,c,u0=0,u1=1):
  sd=np.cross(b-a,s.cam-a);n=np.linalg.norm(sd)
  if n<1e-8:return
  sd*=wd/n;s.quad(o,[a-sd,b-sd,b+sd,a+sd],c,al,(u0,u1))
 def submit(s,prog,mode,verts,t,additive):
  if not verts:return
  s.ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE if additive else moderngl.ONE_MINUS_SRC_ALPHA)
  prog['ModelViewMat'].write(s.mv.T.astype('f4').tobytes());prog['ProjMat'].write(s.proj.T.astype('f4').tobytes());prog['EffectTime'].value=t/20;prog['EffectMode'].value=mode;prog['Viewer'].value=(0.,0.,1.)
  b=s.ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=s.ctx.vertex_array(prog,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
 def esfera(s,o,c,r,cor,al,lat=6,lon=8):
  P=lambda i,j:c+r*np.array([math.sin(math.pi*i/lat)*math.cos(2*math.pi*j/lon),math.cos(math.pi*i/lat),math.sin(math.pi*i/lat)*math.sin(2*math.pi*j/lon)])
  for i in range(lat):
   for j in range(lon):
    p=[P(i,j),P(i+1,j),P(i+1,j+1),P(i,j+1)];n=(p[0]+p[2])/2-c;n=n/max(1e-9,np.linalg.norm(n))
    s.quad(o,p,sombra(cor,.55+.55*max(0,n@LUZ)),al)
 def quadro(s,t,cam,alvo,vp,fov=48):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  seg=t/20;ra,la,tr,hd=(chave(o,seg) for o in ('right_arm','left_arm','torso','head'))
  gt=rotation([0,tr[1],0])[:3,:3];dono=mesh('ichigo','idle',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  dono[:,0:3]=dono[:,0:3]@gt.T+PARTIDA*16;dono[:,3:6]=dono[:,3:6]@gt.T
  giro=rotation([0,180,0])[:3,:3];ale=mesh('ichigo','idle').copy();ale[:,0:3]=ale[:,0:3]@giro.T+s.alvo*16;ale[:,3:6]=ale[:,3:6]@giro.T
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(np.concatenate([dono,ale]),cam,alvo,vp,fov=fov)
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t):
  q=s.d['quadros'][repr(round(t*2)/2)]
  if q['alphaFios']>.01:
   o=[];g=[]
   for l in q['fios']:
    pts=[W(p) for p in l]
    for i,(a,b) in enumerate(zip(pts,pts[1:])):
     k=(i+1)/len(pts);s.rib(o,a,b,.07*k+.015,q['alphaFios']*.85,ESCURO,i/8,(i+1)/8);s.rib(g,a,b,.025,q['alphaFios']*k,VIOLETA)
   s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
  if q['alphaRastro']>.01:
   o=[];g=[]
   for l in q['rastros']:
    pts=[W(p) for p in l]
    for i,(a,b) in enumerate(zip(pts,pts[1:])):
     k=1-(i+1)/len(pts);s.rib(o,a,b,.11*k+.01,q['alphaRastro']*.8,ESCURO,i/8,(i+1)/8);s.rib(g,a,b,.03*k,q['alphaRastro']*.7,VIOLETA)
   s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
  pt=[];pg=[]
  for p,tam in q['particulas']:
   if tam>.004:c=W(p);s.esfera(pt,c,tam,ESCURO,.9,4,6);s.esfera(pg,c,tam*1.6,VIOLETA,.25,4,6)
  s.submit(s.base,8,pt,t,False);s.submit(s.add,1,pg,t,True)
  a=q['alphaOrbe']
  if a>.01:
   n=[[V(p) for p in l] for l in q['normais']]
   def malha(pts,al,glow,escuro=.35):
    c=[[W(p) for p in l] for l in pts];M=len(c[0]);o=[];g=[]
    for i in range(len(c)-1):
     for j in range(M):
      idx=[(i,j),(i+1,j),(i+1,(j+1)%M),(i,(j+1)%M)]
      for k in [0,1,2,0,2,3]:
       ii,jj=idx[k];nn=n[ii][jj];vv=s.cam-c[ii][jj];vv=vv/np.linalg.norm(vv);rim=(1-abs(nn@vv))**3
       o.append([*c[ii][jj],*rgb(sombra(ESCURO,escuro+.8*max(0,nn@LUZ))),al,0,0]);g.append([*c[ii][jj],*rgb(VIOLETA),glow*rim,0,0])
    s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
   malha(q['orbe'],a,a,.2)
   if q['alphaTentaculos']>.01:
    o=[];g=[];at=q['alphaTentaculos']*a
    for l in q['tentaculos']:
     pts=[W(p) for p in l]
     for i,(p0,p1) in enumerate(zip(pts,pts[1:])):
      k=1-i/len(pts);s.rib(o,p0,p1,.07*k+.008,at*.85,ESCURO);s.rib(g,p0,p1,.02*k+.004,at*.6,VIOLETA)
    s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
   malha(q['casca'],a*.32,a*.55,.6)
  ao=q['alphaOnda']
  if ao>.01:
   r=q['raioOnda'];o=[];pts=[s.alvo+OLHO*0+CIMA*1.62+LADO*math.cos(2*math.pi*i/40)*r+CIMA*math.sin(2*math.pi*i/40)*r for i in range(41)]
   for a_,b_ in zip(pts,pts[1:]):s.rib(o,a_,b_,.07*(1-ao*.4),ao,CLARO);
   s.submit(s.add,1,o,t,True)
  ac=q['alphaChao']
  if ac>.01:
   o=[];pe=s.alvo+np.array([0,.05,0]);r=q['raioChao']
   for k in range(18):
    ang=k*2.399+t*.02;rr=r*math.sqrt((k+.5)/18);s.esfera(o,pe+LADO*math.cos(ang)*rr+FRENTE*math.sin(ang)*rr,.28+.1*(k%3),NEVOA,ac*.5,4,8)
   s.submit(s.base,8,o,t,False)
  ae=q['alphaEnlaces']
  if ae>.01:
   o=[];g=[]
   for l in q['enlaces']:
    pts=[W(p) for p in l]
    for i,(p0,p1) in enumerate(zip(pts,pts[1:])):
     k=math.sin(math.pi*(i+.5)/len(pts));s.rib(o,p0,p1,.09*k+.01,ae*.8,ESCURO);s.rib(g,p0,p1,.025*k,ae*.5,VIOLETA)
   s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
  an=q['alphaNevoa']
  if an>.01:
   o=[];g=[]
   for p,tam in q['bolhas']:
    c=W(p);s.esfera(o,c,tam,NEVOA,an*.55);s.esfera(g,c,tam*1.25,VIOLETA,an*.07)
   s.submit(s.base,8,o,t,False);s.submit(s.add,1,g,t,True)
  aa=q['alphaAneis']
  if aa>.01:
   o=[]
   for l in q['aneis']:
    pts=[W(p) for p in l]
    for i in range(len(pts)-1):
     if i%4!=3:s.rib(o,pts[i],pts[i+1],.035,aa,VIOLETA)
   s.submit(s.add,1,o,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim'];C=pv.d['carga'];I=pv.d['impacto']
 lado=(PARTIDA+np.array([8.5,2.4,-3.0]),PARTIDA+np.array([0,1.5,-4.]));ombro=(PARTIDA+np.array([1.9,2.2,3.0]),PARTIDA+np.array([0,1.4,-8.]))
 def fase(t):return 'CARGA' if t<C else 'VIAGEM' if t<I else 'ACERTO' if t<I+4 else 'NEVOA E TONTURA'
 def tela(t,titulo):
  pv.quadro(t,*lado,(0,0,w,h));pv.quadro(t,*ombro,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  d.text((10,h+8),f'SUBARU / SHAMAK (Z) / {fase(t)} | tick {t:5.1f} (disparo {C}, acerto {I}, fim {F}) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: boneco neutro com traje provisorio, alvo parado a 8 blocos, sem impact frame/SFX, sem luz do Minecraft',fill=(150,165,190));return im
 frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,(I+10)*2+1)]
 with av.open(str(scratch/'subaru-shamak.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));d=ImageDraw.Draw(folha)
 for i,t in enumerate([2,C-1,C+2,I-2,I,I+3,I+14,I+45]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  d.text((x+8,y+th+4),f'{fase(t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'subaru-shamak-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
