"""Offline Getsuga Tensho approval (Shikai 520 and Bankai 524): the repository's Ichigo outfit and Zangetsu/Tensa Zangetsu
models, arms/torso/head following desperto_6_500/510 from player_animation/ichigo.json, and the geometry exported by
GetsugaIchigoTest drawn through the mod's GLSL (Shikai additive mode 1, Bankai ink mode 22 + red rim). Not Minecraft:
outfit is the current placeholder (its own wave comes later), neutral mannequin skin, flat ground, no mobs, no impact
frame/SFX, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
from preview_asta_devil_union import Cena

AZUL,BRANCO,TINTA,RUBRO=0x5FC4FF,0xEAF8FF,0x0E0A12,0xD32A50
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)
FRENTE=np.array([0,0,-1.]);LADO=np.array([1.,0,0]);CIMA=np.array([0,1.,0]);PARTIDA=np.array([0,0,6.])
OLHO=PARTIDA+CIMA*1.62
def W(q,o=OLHO):return o+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']

EASE={'linear':lambda u:u,'easeInQuad':lambda u:u*u,'easeOutQuad':lambda u:1-(1-u)**2,'easeInOutSine':lambda u:-(math.cos(math.pi*u)-1)/2}
ANIM=json.loads((ASSETS/'player_animation/ichigo.json').read_text(encoding='utf-8'))['animations']
def chave(clip,osso,seg):
 k=sorted((float(t),v['vector'],v['easing']) for t,v in ANIM[clip]['bones'][osso]['rotation'].items())
 if seg<=k[0][0]:return k[0][1]
 for (t0,v0,_),(t1,v1,e) in zip(k,k[1:]):
  if seg<=t1:u=EASE.get(e,EASE['linear'])((seg-t0)/(t1-t0));return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]

class Previa:
 def __init__(s,w,h,scratch):
  s.cena=Cena(w,h);s.ctx=s.cena.ctx
  s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.d=json.loads((ROOT/'build/ichigo-getsuga-samples.json').read_text(encoding='utf-8'))
  vazio=Path(scratch)/'glow_vazio.png';Image.new('RGBA',(64,64),(0,0,0,0)).save(vazio)
  s.traje=s.cena.tex(ASSETS/'textures/armor/trajes/ichigo.png',0);s.atlas=s.cena.tex(ASSETS/'textures/armor/anime_reference_atlas.png',0);s.glow=s.cena.tex(vazio,1)
  s.espadas={n:mesh(n,'idle').copy() for n in ('zangetsu','tensa_zangetsu')}
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
 def quadro(s,bk,t,cam,alvo,vp,fov=50):
  forma=s.d['bankai' if bk else 'shikai'];imp=forma['impacto'];clip='desperto_6_510' if bk else 'desperto_6_500'
  seg=(t+imp)/20;ra,la,tr,hd=(chave(clip,o,seg) for o in ('right_arm','left_arm','torso','head'))
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  dono=PARTIDA*16;giro=rotation([0,tr[1],0])[:3,:3]
  corpo=mesh('ichigo','pose',{'right_arm':ra,'left_arm':la,'head':hd}).copy()
  corpo[:,0:3]=corpo[:,0:3]@giro.T+dono;corpo[:,3:6]=corpo[:,3:6]@giro.T
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(corpo,cam,alvo,vp,fov=fov)
  mat=s.proj@lookat(s.cam*16,np.asarray(alvo,float)*16)
  piv=np.array([-5.,22,0]);braco=rotation([-ra[0],ra[1],-ra[2]])[:3,:3];mao=piv+braco@(np.array([-6.,12.4,0])-piv)
  R=giro@braco@rotation([-90,0,0])[:3,:3];m=s.espadas['tensa_zangetsu' if bk else 'zangetsu'].copy()
  m[:,0:3]=m[:,0:3]@R.T+giro@mao+dono;m[:,3:6]=m[:,3:6]@R.T
  s.atlas.use(0);buf=s.ctx.buffer(m.tobytes());s.cena.lote(buf,0,mat);buf.release()
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False;s.efeitos(bk,t);s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)
 def efeitos(s,bk,t):
  q=s.d['bankai' if bk else 'shikai']['quadros'][repr(round(t*2)/2)];corpo,brilho=(22,False) if bk else (1,True)
  if q['carga']>.01:
   o=[];g=[]
   for l in q['linguas']:
    pts=[W(p,PARTIDA) for p in l]
    for i,(a,b) in enumerate(zip(pts,pts[1:])):
     s.rib(o,a,b,.13*(1-i/7),q['carga']*(1-i/7)*(.9 if bk else .55),TINTA if bk else AZUL,i/6,(i+1)/6)
     if bk:s.rib(g,a,b,.04,q['carga']*.7,RUBRO)
   s.submit(s.base if bk else s.add,corpo,o,t,brilho);s.submit(s.add,1,g,t,True)
  o=[]
  for z,al,lg in q['sulco']:
   if al<.02:continue
   c=PARTIDA+FRENTE*z+CIMA*.02;s.quad(o,[c-LADO*lg,c+LADO*lg,c+FRENTE*.51+LADO*lg,c+FRENTE*.51-LADO*lg],TINTA if bk else AZUL,al)
  s.submit(s.base if bk else s.add,22 if bk else 1,o,t,not bk)
  a=q['alpha'];e=q['escala'];dis=q['dissolve']
  if a<.01:return
  c=OLHO+FRENTE*q['d'];P=lambda p:c+(LADO*p['x']+CIMA*p['y']+FRENTE*p['z'])*e
  fr=[P(p) for p in q['frente']];tr=[P(p) for p in q['tras']]
  if dis<.6:
   ac=a*(1-dis/.6);o=[];ch=[];g=[]
   V=[[P(p) for p,_ in l] for l in q['volume']];M=len(V[0]);LX,LY,LZ=s.d['luz']
   N=[[LADO*nn['x']+CIMA*nn['y']+FRENTE*nn['z'] for _,nn in l] for l in q['volume']];Lw=LADO*LX+CIMA*LY+FRENTE*LZ;Lw/=np.linalg.norm(Lw)
   def sombra(c,l):l=min(1,l);return int((c>>16&255)*l)<<16|int((c>>8&255)*l)<<8|int((c&255)*l)
   lum=[[0]*M for _ in V];rim=[[0]*M for _ in V]
   for i in range(len(V)):
    for j in range(M):
     vv=s.cam-V[i][j];vv/=np.linalg.norm(vv);n=N[i][j];fz=(1-abs(n@vv))**3;rim[i][j]=fz;lum[i][j]=min(1.25,.28+.72*abs(n@Lw)+.55*fz)
   def solido(o,off,cor,al,brilho=None):
    for i in range(len(V)-1):
     for j in range(M):
      idx=[(i,j),(i+1,j),(i+1,(j+1)%M),(i,(j+1)%M)];uv=[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)]
      for k in [0,1,2,0,2,3]:
       a_,b_=idx[k];c_=sombra(cor,lum[a_][b_]) if brilho is None else brilho;aa=al if brilho is None else al*rim[a_][b_]
       o.append([*(V[a_][b_]+off),*rgb(c_),aa,*uv[k]])
   for de,ae in q['ecos']:
    ae*=ac
    if ae>.02:solido(o,FRENTE*(de-q['d']),TINTA if bk else AZUL,ae)
   s.submit(s.base if bk else s.add,22 if bk else 1,o,t,not bk);o=[]
   if ac>.95:s.ctx.depth_mask=True
   solido(o,np.zeros(3),0x4A3446 if bk else BRANCO,ac);s.submit(s.base,22 if bk else 8,o,t,False);s.ctx.depth_mask=False;o=[]
   solido(o,np.zeros(3),0,ac,RUBRO if bk else AZUL);s.submit(s.add,1,o,t,True);o=[]
   for l in q['chamas']:
    pts=[P(p) for p in l]
    for i,(p0,p1) in enumerate(zip(pts,pts[1:])):s.rib(ch,p0,p1,(.2 if bk else .16)*(1-i/7),ac*(1-i/7),TINTA if bk else AZUL,i/6,(i+1)/6)
   for i in range(len(fr)-1):
    s.rib(g,fr[i],fr[i+1],.07 if bk else .1,ac,RUBRO if bk else BRANCO);s.rib(g,fr[i],fr[i+1],.35 if bk else .45,ac*.25,RUBRO if bk else AZUL)
    if bk:s.rib(g,tr[i],tr[i+1],.05,ac*.8,RUBRO)
   s.submit(s.base if bk else s.add,corpo,ch,t,brilho);s.submit(s.add,1,g,t,True)
  if dis>0:
   o=[];g=[]
   tam=.22*(1-dis*.6)
   def caixa(o,c,x,y,z,cor,al):
    v=[c+np.array([(-x,x)[i&1],(-y,y)[i>>1&1],(-z,z)[i>>2&1]]) for i in range(8)]
    for f in [(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]:s.quad(o,[v[k] for k in f],cor,al)
   for p in q['lascas']:
    pt=P(p);caixa(o,pt,tam,tam*.6,tam,0x2A1A24 if bk else BRANCO,a)
    if bk:caixa(g,pt,tam*1.3,tam*.8,tam*1.3,RUBRO,a*.35)
   s.submit(s.base,22 if bk else 8,o,t,False);s.submit(s.add,1,g,t,True)

def render(saida):
 w,h=640,400;scratch=Path(saida);scratch.mkdir(parents=True,exist_ok=True);pv=Previa(w*2,h,scratch);F=pv.d['fim']
 lado=(PARTIDA+np.array([9,3.5,-14.]),PARTIDA+np.array([0,2,-9.]));costas=(PARTIDA+np.array([2.2,2.6,4.2]),PARTIDA+np.array([0,1.4,-8.]))
 def fase(bk,t):return 'CARGA' if t<0 else 'LIBERA' if t<3 else 'VIAGEM' if t<pv.d['viagem'] else 'DISSIPA'
 def tela(bk,t,titulo):
  pv.quadro(bk,t,*lado,(0,0,w,h));pv.quadro(bk,t,*costas,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  imp=pv.d['bankai' if bk else 'shikai']['impacto']
  d.text((10,h+8),f'ICHIGO / GETSUGA TENSHO {"BANKAI" if bk else "SHIKAI"} / {fase(bk,t)} | tick {t+imp:5.1f} (corte no tick {imp}) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: pose desperto_6_500/510 do jogo, traje provisorio, sem mobs, sem impact frame/SFX, sem luz do Minecraft',fill=(150,165,190));return im
 frames=[]
 for bk in (False,True):
  imp=pv.d['bankai' if bk else 'shikai']['impacto']
  frames+=[tela(bk,float(t),'VELOCIDADE REAL') for t in range(-imp,F+1)]+[tela(bk,n/2,'CAMERA LENTA 2x') for n in range(-imp*2,F*2+1)]
 with av.open(str(scratch/'ichigo-getsuga.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));d=ImageDraw.Draw(folha)
 for i,(bk,t) in enumerate([(False,-3),(False,0),(False,2),(False,22),(True,-2),(True,0),(True,6),(True,23)]):
  im=tela(bk,float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  d.text((x+8,y+th+4),f'{"BANKAI" if bk else "SHIKAI"} - {fase(bk,t)} (t={t})',fill=(235,238,245))
 folha.save(scratch/'ichigo-getsuga-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
