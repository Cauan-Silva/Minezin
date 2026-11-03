"""Offline approval of Ichigo's Shikai kit X/C/V (Shunpo 501, heavy slam wave 525, lunge + rising slash 526): the Ichigo
outfit and Zangetsu posed by desperto_6_501/502/503 from player_animation/ichigo.json, with the 3D geometry exported by
KitBaseIchigoTest lit per vertex like EfeitosIchigo.malha. Not Minecraft: placeholder outfit, flat ground, no mobs,
no impact frame/SFX, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,mesh,lookat,rotation
from preview_ichigo_getsuga import Previa,chave,rgb,AZUL,BRANCO,TINTA,FRENTE,LADO,CIMA,PARTIDA

LUZ=np.array([.45,.8,-.4]);LUZ=LADO*LUZ[0]+CIMA*LUZ[1]+FRENTE*LUZ[2];LUZ/=np.linalg.norm(LUZ)
def Wp(q,o):return o+LADO*q['x']+CIMA*q['y']+FRENTE*q['z']
def sombra(c,l):l=min(1,l);return int((c>>16&255)*l)<<16|int((c>>8&255)*l)<<8|int((c&255)*l)

class Kit(Previa):
 def __init__(s,w,h,scratch):
  super().__init__(w,h,scratch);s.k=json.loads((ROOT/'build/ichigo-kit-base-samples.json').read_text(encoding='utf-8'))
 def corpo(s,clip,seg,pe,cam,alvo,vp,fov=50,espada="zangetsu",pulso=0,rolar=-1):
  ra,la,tr,hd=(chave(clip,o,seg) for o in ('right_arm','left_arm','torso','head'))
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  dono=pe*16;giro=rotation([0,tr[1],0])[:3,:3];s.giro=giro
  c=mesh('ichigo','pose',{'right_arm':ra,'left_arm':la,'head':hd}).copy();c[:,0:3]=c[:,0:3]@giro.T+dono;c[:,3:6]=c[:,3:6]@giro.T
  s.traje.use(0);s.glow.use(1);s.cena.desenhar(c,cam,alvo,vp,fov=fov)
  mat=s.proj@lookat(s.cam*16,np.asarray(alvo,float)*16)
  piv=np.array([-5.,22,0]);braco=rotation([-ra[0],ra[1],-ra[2]])[:3,:3];mao=piv+braco@(np.array([-6.,12.4,0])-piv)
  R=giro@braco@rotation([-90-pulso,0,0])[:3,:3]@rotation([0,rolar*pulso,0])[:3,:3];m=s.espadas[espada].copy();m[:,0:3]=m[:,0:3]@R.T+giro@mao+dono;m[:,3:6]=m[:,3:6]@R.T
  s.atlas.use(0);buf=s.ctx.buffer(m.tobytes());s.cena.lote(buf,0,mat);buf.release()
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
 def malha(s,g,modo,cor,rim,al,solido):
  n,m=len(g),len(g[0]);g=[[np.asarray(p) for p in l] for l in g];lum=np.zeros((n,m));fr=np.zeros((n,m))
  for i in range(n):
   for j in range(m):
    no=np.cross(g[min(n-1,i+1)][j]-g[max(0,i-1)][j],g[i][(j+1)%m]-g[i][(j-1)%m]);ln=np.linalg.norm(no);no=no/ln if ln>1e-9 else CIMA
    v=s.cam-g[i][j];v/=np.linalg.norm(v);f=(1-abs(no@v))**3;fr[i,j]=f;lum[i,j]=min(1,.28+.72*abs(no@LUZ)+.4*f)
  def tri(cf,af):
   o=[]
   for i in range(n-1):
    for j in range(m):
     q=[(i,j),(i+1,j),(i+1,(j+1)%m),(i,(j+1)%m)];uv=[(i*.4,0),((i+1)*.4,0),((i+1)*.4,1),(i*.4,1)]
     for k in [0,1,2,0,2,3]:a,b=q[k];o.append([*g[a][b],*rgb(cf(a,b)),af(a,b),*uv[k]])
   return o
  if solido and al>.95:s.ctx.depth_mask=True
  s.submit(s.base,modo,tri(lambda a,b:sombra(cor,lum[a,b]),lambda a,b:al),0,False);s.ctx.depth_mask=False
  if rim:s.submit(s.add,1,tri(lambda a,b:rim,lambda a,b:al*fr[a,b]),0,True)
 def caixas(s,cs,cor,modo=8,ad=False,al=1):
  o=[]
  for c,d in cs:
   if d<.005:continue
   v=[c+np.array([(-d,d)[i&1],(-d,d)[i>>1&1],(-d,d)[i>>2&1]]) for i in range(8)]
   for f in [(0,1,3,2),(4,6,7,5),(0,4,5,1),(2,3,7,6),(0,2,6,4),(1,5,7,3)]:s.quad(o,[v[k] for k in f],cor,al)
  s.submit(s.add if ad else s.base,modo,o,0,ad)

 def shunpo(s,t,cam,alvo,vp):
  fim=FRENTE*10;pe=PARTIDA+(fim if t>=0 else 0)
  s.corpo('desperto_6_501',max(0,t)/20,pe,cam,alvo,vp);s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  q=s.k['shunpo'].get(repr(round(t*2)/2))
  if q:
   for u,a in q['ecos']:
    if a<.02:continue
    o=PARTIDA+fim*u;cs=[(o+CIMA*1.55,.25),(o+CIMA*1.05,.28),(o+CIMA*1.05+LADO*.36,.11),(o+CIMA*1.05-LADO*.36,.11),(o+CIMA*.36+LADO*.13,.12),(o+CIMA*.36-LADO*.13,.12)]
    s.caixas(cs,0x2B5C86,al=a)
   g=[]
   for off,u0,u1 in q['riscos']:
    if u1>u0:o=Wp(off,PARTIDA);s.rib(g,o+fim*u0,o+fim*u1,.03,q['alphaRisco'],0x9FDCFF)
   s.submit(s.add,1,g,t,True)
   s.caixas([(Wp(a,PARTIDA),d) for a,b,d in q['poeira']]+[(Wp(b,PARTIDA+fim),d) for a,b,d in q['poeira']],0x8A7F70)
   for (gr,aa),o in zip(q['aneis'],(PARTIDA,PARTIDA+fim)):
    if aa>.02:s.malha([[Wp(p,o) for p in l] for l in gr],8,0xDDF3FF,AZUL,aa,False)
   g=[]
   for l in q['helices']:
    pts=[PARTIDA+fim*u+Wp(p,np.zeros(3)) for u,p in l]
    for i,(a,b) in enumerate(zip(pts,pts[1:])):s.rib(g,a,b,.06*math.sin(math.pi*i/len(pts)),q['alphaRisco']*.8,0xBFE8FF)
   s.submit(s.add,1,g,t,True)
   s.caixas([(PARTIDA+fim*u+Wp(p,np.zeros(3)),d) for u,p,d in q['flocos']],0x4F8FC4)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)

 def slam(s,t,cam,alvo,vp):
  imp=s.k['impactoC'];s.corpo('desperto_6_502',(t+imp)/20,PARTIDA,cam,alvo,vp);s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  q=s.k['onda'].get(repr(round(t*2)/2))
  if q:
   if q['alpha']>.02 and q['altura']>.05:s.malha([[Wp(p,PARTIDA) for p in l] for l in q['onda']],8,0xCFEFFF,AZUL,q['alpha'],True)
   ar=q['alphaRachadura']
   if ar>.02:
    o=[];g=[]
    for l in q['rachaduras']:
     for (a,w0),(b,w1) in zip(l,l[1:]):
      a,b=Wp(a,PARTIDA),Wp(b,PARTIDA);d=np.cross(b-a,CIMA);n=np.linalg.norm(d)
      if n<1e-8:continue
      d/=n;s.quad(o,[a+d*w0,b+d*w1,b-d*w1,a-d*w0],TINTA,ar);s.rib(g,a+CIMA*.02,b+CIMA*.02,w0*.45,ar*.8,AZUL)
    s.submit(s.base,22,o,t,False);s.submit(s.add,1,g,t,True)
   s.caixas([(Wp(p,PARTIDA),d) for p,d in q['detritos']],0x6E6458)
   for k,(g,af,rf) in enumerate(q['fumaca']):
    if af*.85>.02 and rf>.05:s.malha([[Wp(p,PARTIDA) for p in l] for l in g],8,0xB8C7D6 if k<6 else 0xA39A8C,AZUL if k<6 else 0,af*.85,False)
   if q['alphaClarao']>.02:
    o=[];r=q['clarao'];c=PARTIDA+CIMA*.3
    for i in range(10):
     for j in range(20):
      def P(a,b):return c+r*np.array([math.cos(b)*math.cos(a),math.sin(a),math.sin(b)*math.cos(a)])
      a0,a1=-math.pi/2+math.pi*i/10,-math.pi/2+math.pi*(i+1)/10;b0,b1=math.pi*2*j/20,math.pi*2*(j+1)/20
      s.quad(o,[P(a0,b0),P(a1,b0),P(a1,b1),P(a0,b1)],AZUL,q['alphaClarao']*.6)
    s.submit(s.add,15,o,t,True)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)

 def rising(s,t,cam,alvo,vp):
  imp=s.k['impactoV'];pe=PARTIDA+(FRENTE*s.k['investida'] if t>=0 else 0);s.corpo('desperto_6_503',(t+imp)/20,pe,cam,alvo,vp)
  s.ctx.enable(moderngl.BLEND);s.ctx.depth_mask=False
  q=s.k['corte'].get(repr(round(t*2)/2))
  if q and q['alpha']>.02:
   olho=pe+CIMA*1.62
   for k in range(len(q['volumes'])-1,-1,-1):
    v=q['volumes'][k]
    if not v:continue
    g=[[Wp(p,olho) for p in l] for l in v]
    if k==0:s.malha(g,8,BRANCO,AZUL,q['alpha'],True)
    else:s.malha(g,1,AZUL,0,q['alpha']*(.35 if k==1 else .15),False)
  if t>=0:
   # Dummy target 2.5 blocks ahead, launched with the server impulse (vanilla gravity/drag).
   y=0;vy=s.k['impulso']
   for _ in range(int(t)):y+=vy;vy=(vy-.08)*.98
   y=max(0,y);alvo=pe+FRENTE*2.5;s.caixas([(alvo+CIMA*(y+.9),.3)],0x7A3B3B)
   l=s.k['lancamento'].get(repr(round(t*2)/2))
   if l:
    if l['alpha']>.02:
     for e in l['espirais']:
      pts=[Wp(p,alvo) for p in e];g=[]
      for i,(a,b) in enumerate(zip(pts,pts[1:])):s.rib(g,a,b,.09*math.sin(math.pi*i/len(pts))+.01,l['alpha'],0xDDF3FF)
      s.submit(s.base,8,g,t,False);g=[]
      for i,(a,b) in enumerate(zip(pts,pts[1:])):s.rib(g,a,b,.16*math.sin(math.pi*i/len(pts)),l['alpha']*.5,AZUL)
      s.submit(s.add,1,g,t,True)
    s.caixas([(Wp(p,alvo),d) for p,d in l['estilhacos']],BRANCO)
  s.ctx.depth_mask=True;s.ctx.disable(moderngl.BLEND)

def render(saida):
 w,h=640,400;out=Path(saida);out.mkdir(parents=True,exist_ok=True);pv=Kit(w*2,h,out)
 cenas=[('X SHUNPO',pv.shunpo,-4,16,0,(PARTIDA+np.array([11,4,-5.]),PARTIDA+np.array([0,1,-5.])),(PARTIDA+np.array([2,2.6,4.]),PARTIDA+np.array([0,1.2,-6.]))),
        ('C GOLPE PESADO',pv.slam,-12,36,12,(PARTIDA+np.array([9,6,-9.]),PARTIDA+np.array([0,.5,0.])),(PARTIDA+np.array([2,2.6,4.]),PARTIDA+np.array([0,1.,-3.]))),
        ('V INVESTIDA + CORTE',pv.rising,-10,22,10,(PARTIDA+np.array([9,3.5,-4.]),PARTIDA+np.array([0,2.6,-5.])),(PARTIDA+np.array([2,2.6,4.]),PARTIDA+np.array([0,1.6,-4.])))]
 def tela(nome,f,t,imp,v1,v2,titulo):
  f(t,*v1,(0,0,w,h));f(t,*v2,(w,0,w,h))
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  d.text((10,h+8),f'ICHIGO SHIKAI / {nome} | tick {t+imp:5.1f} (golpe no tick {imp}) | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: poses desperto_6_501/502/503 do jogo, traje provisorio, sem mobs, sem impact frame/SFX',fill=(150,165,190));return im
 frames=[];folha=[]
 for nome,f,t0,t1,imp,v1,v2 in cenas:
  frames+=[tela(nome,f,float(t),imp,v1,v2,'VELOCIDADE REAL') for t in range(t0,t1+1)]+[tela(nome,f,n/2,imp,v1,v2,'CAMERA LENTA 2x') for n in range(t0*2,t1*2+1)]
  folha+=[tela(nome,f,float(t),imp,v1,v2,'') for t in ((-2,1,4) if nome[0]=='X' else (2,8,18) if nome[0]=='C' else (-2,1,8))]
 with av.open(str(out/'ichigo-kit-base.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):c.mux(p)
  for p in st.encode():c.mux(p)
 tw,th=int(w*.6),int((h+40)*.6);sh=Image.new('RGB',(tw*2*3,th*3),(16,18,24))
 for i,im in enumerate(folha):sh.paste(im.resize((tw*2,th),Image.Resampling.LANCZOS),(i%3*tw*2,i//3*th))
 sh.save(out/'ichigo-kit-base-fases.png');print('ok',len(frames))
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
