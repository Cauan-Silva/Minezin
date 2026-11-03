"""Offline approval of Asta's swords from the user's reference images: Demon-Slayer (normal and Devil Union, with the
animated black-and-red flame aura exported by AuraMatadoraTest) and the Demon-Dweller pulled out for the ultimate.
Real geo + texture + glowmask, standing on the pommel; aura through the mod's GLSL (ink 22 + additive 10). Not Minecraft."""
from pathlib import Path
import json,math,argparse
import numpy as np,moderngl,av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,mesh,lookat
from preview_asta_devil_union import Cena
from preview_gojo_vfx import load_program

ESPADAS=[('matadora','MATADORA (DEMON-SLAYER)'),('matadora_desperta','MATADORA DESPERTA + AURA'),('demon_dweller','DEMON-DWELLER')]
VISTAS=[('FRENTE',(0,2.3,-5.4)),('3/4',(3.6,2.9,-4.0)),('COSTAS',(0,2.3,5.4))]
ALTURA=18 # geo y -> preview mesh y: the sword root sits 6 px up and the sheet stands it 12 px higher.
TINTA,AURA=0x140A12,0x8C1238
def rgb(c):return ((c>>16&255)/255,(c>>8&255)/255,(c&255)/255)

class Aura:
 def __init__(s,cena):
  s.cena=cena;s.ctx=cena.ctx;s.base=load_program(s.ctx,'anime_energy');s.add=load_program(s.ctx,'anime_energy_additive')
  s.Q=json.loads((ROOT/'build/asta-aura-matadora-samples.json').read_text(encoding='utf-8'))['quadros']
 def desenhar(s,t,cam,alvo,vp,fov=46):
  x,y,w,h=vp;f=1/math.tan(math.radians(fov)/2);proj=np.array([[f/(w/h),0,0,0],[0,f,0,0],[0,0,-1.002,-2.0],[0,0,-1,0]])
  c=np.asarray(cam,float)*16;mv=lookat(c,np.asarray(alvo,float)*16);ink=[];glow=[]
  def rib(o,a,b,wa,wb,cor,al,u0=0,u1=1):
   sd=np.cross(b-a,c-a);n=np.linalg.norm(sd)
   if n<1e-8:return
   sd/=n;pts=[a-sd*wa,b-sd*wb,b+sd*wb,a+sd*wa];uv=[(u0,0),(u1,0),(u1,1),(u0,1)]
   for k in [0,1,2,0,2,3]:o.append([*pts[k],*rgb(cor),al,*uv[k]])
  for l in s.Q[repr(round(t*2)/2%52)]:
   pts=[(np.array([p['x'],p['y']+ALTURA,p['z']]),wd) for p,wd in l]
   for i,((a,wa),(b,wb)) in enumerate(zip(pts,pts[1:])):
    rib(ink,a,b,wa,wb,TINTA,1,i*.5,i*.5+.5)
   # One halo strip per flame (base to tip): overlapping translucent segments would band.
   (a,wa),(m,_),(b,_)=pts[0],pts[len(pts)//2],pts[-1];rib(glow,a,m,wa*2.4+.8,wa*1.6+.6,AURA,.25);rib(glow,m,b,wa*1.6+.6,.3,AURA,.25)
  s.ctx.enable(moderngl.BLEND)
  # Soft crimson halo first without depth writes (it must not hide the flames), then the solid ink flames.
  for prog,mode,verts,halo in ((s.base,8,glow,True),(s.base,22,ink,False)):
   if not verts:continue
   s.cena.fbo.depth_mask=not halo;s.ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE_MINUS_SRC_ALPHA)
   prog['ModelViewMat'].write(mv.T.astype('f4').tobytes());prog['ProjMat'].write(proj.T.astype('f4').tobytes());prog['EffectTime'].value=t/20;prog['EffectMode'].value=mode;prog['Viewer'].value=(0.,0.,1.)
   b=s.ctx.buffer(np.asarray(verts,dtype='f4').tobytes());v=s.ctx.vertex_array(prog,[(b,'3f 4f 2f','Position','Color','UV0')]);v.render();v.release();b.release()
  s.cena.fbo.depth_mask=True;s.ctx.disable(moderngl.BLEND)

def render(saida):
 w,h=300,620;folha=Image.new('RGB',(w*len(ESPADAS)*len(VISTAS),h+60),(16,18,24));d=ImageDraw.Draw(folha);col=0
 for estilo,titulo in ESPADAS:
  cena=Cena(w,h,estilo);m=mesh(estilo,'idle');m[:,1]+=12;aura=Aura(cena) if estilo=='matadora_desperta' else None
  for nome,cam in VISTAS:
   cena.desenhar(m,cam,(0,2.3,0),(0,0,w,h),fov=46)
   if aura:aura.desenhar(6,cam,(0,2.3,0),(0,0,w,h))
   folha.paste(cena.imagem(),(col*w,0));d.text((col*w+8,10),titulo,fill=(20,22,30));d.text((col*w+8,24),nome,fill=(40,44,52));col+=1
 d.text((12,h+10),'Modelos GeckoLib reais com textura (vermelho em brilho pleno = glowmask). Referencias: imagens das espadas enviadas pelo usuario.',fill=(230,235,245))
 d.text((12,h+32),'Desperta: lamina carmesim escura com manchas, sem linhas vermelhas; aura de chamas pretas com borda vermelha (efeito animado). Sem luz do Minecraft.',fill=(160,178,202))
 Path(saida).parent.mkdir(parents=True,exist_ok=True);folha.save(saida);print('ok',saida)
 # Looping aura video: front and 3/4 of the awakened sword, two cycles.
 cena=Cena(w*2,h,'matadora_desperta');m=mesh('matadora_desperta','idle');m[:,1]+=12;aura=Aura(cena)
 with av.open(str(Path(saida).with_name('asta-matadora-aura.mp4')),'w') as cont:
  st=cont.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*2,h,'yuv420p';st.options={'crf':'18'}
  for n in range(104):
   t=n/2
   for i,(nome,cam) in enumerate(VISTAS[:2]):cena.desenhar(m,cam,(0,2.3,0),(i*w,0,w,h),fov=46);aura.desenhar(t,cam,(0,2.3,0),(i*w,0,w,h))
   im=cena.imagem();ImageDraw.Draw(im).text((8,10),f'MATADORA DESPERTA - aura em loop (camera lenta 2x)  tick {t:4.1f}',fill=(20,22,30))
   for p in st.encode(av.VideoFrame.from_image(im)):cont.mux(p)
  for p in st.encode():cont.mux(p)
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
