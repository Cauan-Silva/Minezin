"""Offline approval of Asta's ultimate (V in the Devil Union): the real Devil Union model, the Matadora desperta in the
right hand and the Demon-Dweller drawn from the grimoire into the left, solid anti-magic slashes and the final X
exported by UltAstaTest, seen through the local cutscene camera and from outside. Not Minecraft: arms and torso follow the
asta_307 keyframes (torso twist applied to the whole body), no mobs, no aura on the held swords, no impact frame/SFX, no Minecraft lighting."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_gojo_vfx import load_program
from preview_armor_models import ROOT,ASSETS,mesh,lookat,rotation
from preview_asta_devil_union import Cena,pose,modelo
from preview_asta_black_meteorite import Previa,TINTA,VERMELHO,rgb,P

from build_asta_assets import ULT_BRACOS
def chave(osso,t):
 """Keyframe sample with easeInOutSine, like the PlayerAnimator file."""
 k=sorted(ULT_BRACOS[osso].items())
 if t<=k[0][0]:return k[0][1]
 for (t0,v0),(t1,v1) in zip(k,k[1:]):
  if t<=t1:u=(t-t0)/(t1-t0);u=-(math.cos(math.pi*u)-1)/2;return [a+(b-a)*u for a,b in zip(v0,v1)]
 return k[-1][1]
FRENTE=np.array([0,0,-1.]);LADO=np.array([1.,0,0]);CIMA=np.array([0,1.,0])
def W(q):q=np.asarray(q,float);return LADO*q[0]+CIMA*q[1]+FRENTE*q[2]
def Pw(q):return W([q['x'],q['y'],q['z']])
PAGINA,CAPA=0xC8283C,0x120E14

class Ult(Previa):
 def __init__(s,w,h):
  s.cena=Cena(w,h);s.ctx=s.cena.ctx;s.w,s.h=w,h
  s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.d=json.loads((ROOT/'build/asta-ult-samples.json').read_text(encoding='utf-8'));s.du=json.loads((ROOT/'build/asta-devil-union-samples.json').read_text(encoding='utf-8'))
  s.Q=s.d['quadros'];s.I=s.d['final'];s.F=s.d['duracao']
  s.texturas={}
  for nome in ('asta_forma','matadora_desperta','demon_dweller'):
   s.texturas[nome]=(s.cena.tex(ASSETS/f'textures/armor/trajes/{nome}.png',0),s.cena.tex(ASSETS/f'textures/armor/trajes/{nome}_glowmask.png',1))
  s.espadas={n:s._espada(n) for n in ('matadora_desperta','demon_dweller')}
  s.q=lambda t:s.Q[repr(round(t*2)/2)]
 @staticmethod
 def _espada(nome):
  m=mesh(nome,'idle').copy();m[:,1]-=6;return m # grip middle at the origin
 def _pose_espada(s,m,R,pos):
  m=m.copy();M=R[:3,:3];m[:,0:3]=m[:,0:3]@M.T;m[:,3:6]=m[:,3:6]@M.T;m[:,0:3]+=pos;return m
 def quadro(s,t,cam,alvo,vp,rotulo,fov=46):
  s.cam=np.asarray(cam,float);x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2)
  s.proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  q=s.q(t);dono=W([0,0,q['avanco']])*16
  # Arms and torso from the same keyframes the game plays (player_animation asta_307), swords fixed in the hands.
  ra,la,tronco=chave('right_arm',t),chave('left_arm',t),chave('body',t)
  p=pose(s.du,60+t);asas={n:[-math.degrees(x),-math.degrees(y),math.degrees(z)] for n,(x,y,z) in p['ossos'].items()}
  corpo=mesh('asta_forma','du_pose',{'right_arm':ra,'left_arm':la},asas).copy()
  giro=rotation([0,tronco[1],0])[:3,:3]
  corpo[:,0:3]=corpo[:,0:3]@giro.T+dono;corpo[:,3:6]=corpo[:,3:6]@giro.T
  t0,t1=s.texturas['asta_forma'];t0.use(0);t1.use(1)
  s.cena.desenhar(corpo,cam,alvo,vp,fov=fov)
  mat=s.proj@lookat(s.cam*16,np.asarray(alvo,float)*16)
  for nome,r,piv,vis in (('matadora_desperta',ra,np.array([-5.,22,0]),1),('demon_dweller',la,np.array([5.,22,0]),q['esquerda'])):
   if vis<.5:continue
   braco=rotation([-r[0],r[1],-r[2]])[:3,:3] # same gecko conversion as the arm bone
   mao=piv+braco@(np.array([piv[0]*1.2,12.4,0])-piv)
   R=giro@braco@rotation([-90,0,0])[:3,:3] # blade along the fist's forward axis, as a held item
   m=s._pose_espada(s.espadas[nome],np.vstack([np.hstack([R,np.zeros((3,1))]),[0,0,0,1]]),giro@mao+dono)
   a,b=s.texturas[nome];a.use(0);b.use(1);buf=s.ctx.buffer(m.tobytes());s.cena.lote(buf,0,mat);buf.release()
  if q['esquerda']<.5 and q['emergir']>.01:
   # Rising out of the open grimoire, hilt first.
   c=W(np.array([s.d['livro']['x'],s.d['livro']['y'],s.d['livro']['z']]))*16
   m=s._pose_espada(s.espadas['demon_dweller'],rotation([180,0,0]),c+np.array([0,-14+q['emergir']*20,0]))
   a,b=s.texturas['demon_dweller'];a.use(0);b.use(1);buf=s.ctx.buffer(m.tobytes());s.cena.lote(buf,0,mat);buf.release()
  s.mv=lookat(s.cam*16,np.asarray(alvo,float)*16)@np.diag([16.,16,16,1])
  s.ctx.enable(moderngl.BLEND);s.efeitos(t);s.ctx.disable(moderngl.BLEND)
 def efeitos(s,t):
  q=s.q(t);o=[];sol=[]
  lv=q['livro']
  if lv>.01:
   c=W([s.d['livro'][k] for k in 'xyz']);ab=math.radians(10+160*lv);alt=.36
   # Open book facing forward-left (where the camera opens), spine vertical, pages glowing red.
   eixo=np.array([0,1.,0]);n=W([-.6,0,1]);n/=np.linalg.norm(n);tg=np.cross(eixo,n)
   for sgn in (-1,1):
    d=tg*sgn*math.sin(ab/2)-n*math.cos(ab/2)
    p0,p1=c-eixo*alt,c+eixo*alt;q0,q1=p1+d*.5,p0+d*.5
    s.quad(o,[p0,p1,q0,q1],[(0,0),(1,0),(1,1),(0,1)],CAPA,1)
    f=n*.03;pg=d*.46+f;s.quad(sol,[p0+eixo*.03+f,p1-eixo*.03+f,p1-eixo*.03+pg,p0+eixo*.03+pg],[(0,0),(1,0),(1,1),(0,1)],PAGINA,1)
  for l in q['linguas']:
   pts=[Pw(p) for p in l]
   for i,(a,b) in enumerate(zip(pts,pts[1:])):s.rib(o,a,b,.12*(1-i/9),1,TINTA,.12*(1-(i+1)/9))
  for m in q['cortes']+q['x']:
   al=m['alpha']
   for face in m['faces']:
    for i in range(len(face)-1):
     for v in range(len(face[i])-1):
      p00,p10,p11,p01=Pw(face[i][v]),Pw(face[i+1][v]),Pw(face[i+1][v+1]),Pw(face[i][v+1])
      s.quad(o,[p00,p10,p11,p01],[(i*.4,v/4),((i+1)*.4,v/4),((i+1)*.4,(v+1)/4),(i*.4,(v+1)/4)],TINTA,al)
   # Glowing seam along the outer edge, visible from every side: the volume reads even edge-on.
   out=[Pw(m['faces'][0][i][4]) for i in range(len(m['faces'][0]))]
   for a,b in zip(out,out[1:]):s.rib(sol,a,b,.035,1,VERMELHO,.035)
  s.submit(s.base,22,o,t,False);s.submit(s.base,8,sol,t,False)
 def fase(s,t):return 'GRIMORIO' if t<18 else 'CRUZA AS ESPADAS' if t<30 else 'RAJADA DE CORTES' if t<57 else 'CORTE EM X' if t<64 else 'FINAL'

def render(saida):
 w,h=640,400;pv=Ult(w*2,h);out=Path(saida);out.mkdir(parents=True,exist_ok=True)
 def tela(t,titulo):
  q=pv.q(t);(c,a,fov)=q['camera'];cam=W([c['x'],c['y'],c['z']]);alvo=W([a['x'],a['y'],a['z']])
  pv.quadro(t,cam,alvo,(0,0,w,h),'',fov=70*fov)
  ext=W([7,5,q['avanco']+10]);pv.quadro(t,ext,W([0,1.6,q['avanco']+2.5]),(w,0,w,h),'',fov=55)
  im=Image.new('RGB',(w*2,h+40),(16,18,24));im.paste(pv.cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  d.text((10,10),'CAMERA DA CUTSCENE (so de quem usa)',fill=(20,22,30));d.text((w+10,10),'VISTA EXTERNA',fill=(20,22,30))
  d.text((10,h+8),f'ASTA / ULT (V NA FORMA) / {pv.fase(t)} | tick {t:5.1f} | cortes nos ticks 32-52, X no tick {pv.I} | {titulo}',fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: bracos e tronco com a animacao asta_307 do jogo, sem mobs, sem aura nas espadas, sem impact frame/SFX, sem luz do Minecraft',fill=(150,165,190));return im
 with av.open(str(out/'asta-ult.mp4'),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h+40,'yuv420p';st.options={'crf':'18'}
  frames=[tela(float(t),'VELOCIDADE REAL') for t in range(0,pv.F+1)]+[tela(n/2,'CAMERA LENTA 2x') for n in range(0,pv.F*2+1)]
  for im in frames:
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
 tw,th=int(w*.6),int(h*.6);folha=Image.new('RGB',(tw*2*2,(th+20)*4),(16,18,24));d=ImageDraw.Draw(folha)
 for i,t in enumerate([6,14,24,33,41,50,60,66]):
  im=tela(float(t),'').crop((0,0,w*2,h)).resize((tw*2,th),Image.Resampling.LANCZOS);x,y=i%2*tw*2,i//2*(th+20);folha.paste(im,(x,y))
  d.text((x+8,y+th+4),f'tick {t} - {pv.fase(t)}',fill=(235,238,245))
 folha.save(out/'asta-ult-fases.png');print('ok',len(frames),'quadros')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
