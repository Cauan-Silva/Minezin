"""Beatrice (G do Subaru, contrato): companion GeckoLib model + texture, made from scratch for Minezin.
Chibi proportions (big head), the face painted on the texture, thick straight drill twin-tails down to the waist
with striped ribbons (one bone per segment so they sway), crimson dress with bell sleeves, fluffy white cuffs and hem,
big striped bow on the chest, striped stockings and a small gold crown. Units are pixels (16 per block); front faces -z.
Writes geo/subaru/beatrice.geo.json and textures/entity/subaru/beatrice.png."""
from pathlib import Path
import json,math,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol'
T=16;N=8;W=H=T*N;ALT=2.6
(ROSTO,CARMIM,CARMIM_ESC,FELPA,CABELO,CABELO_SOMBRA,PELE,ROSA,LISTRA,MEIA,SAPATO,OURO,RUBI,PRETO,CABELO_TOPO,NUCA)=range(16)
COR={CARMIM:(196,30,78),CARMIM_ESC:(150,18,60),FELPA:(250,246,246),CABELO:(240,232,178),CABELO_SOMBRA:(214,200,140),PELE:(252,228,214),
     ROSA:(246,150,190),SAPATO:(110,40,96),OURO:(236,190,70),RUBI:(200,30,50),PRETO:(40,28,40)}
def tile(k):return k%N*T,k//N*T
def textura():
 rnd=random.Random(7);im=Image.new('RGBA',(W,H),(0,0,0,0));d=ImageDraw.Draw(im)
 for k,c in COR.items():
  x,y=tile(k);d.rectangle([x,y,x+T-1,y+T-1],fill=c+(255,))
  for i in range(T):
   for j in range(T):
    if rnd.random()<.12:im.putpixel((x+i,y+j),tuple(max(0,v-12) for v in c)+(255,))
 x,y=tile(FELPA)  # felpa: bolinhas cinza-claro
 for _ in range(40):i,j=rnd.randrange(T),rnd.randrange(T);im.putpixel((x+i,y+j),(222,214,222,255))
 for k,base,sombra in ((CABELO,(240,232,178),(214,200,140)),(CABELO_SOMBRA,(214,200,140),(186,170,112))):
  x,y=tile(k);d.rectangle([x,y,x+T-1,y+T-1],fill=base+(255,))
  for i in range(0,T,3):d.line([x+i,y,x+i,y+T-1],fill=sombra+(255,))  # mechas verticais
  for i in range(1,T,5):d.line([x+i,y,x+i,y+T-1],fill=(252,248,220,255))
 x,y=tile(CABELO_TOPO);d.rectangle([x,y,x+T-1,y+T-1],fill=(240,232,178,255))
 for i in range(T):d.line([x+T//2,y+T//2,x+i,y],fill=(214,200,140,255)) if i%3==0 else None
 x,y=tile(NUCA);d.rectangle([x,y,x+T-1,y+T-1],fill=(240,232,178,255))
 for i in range(0,T,2):d.line([x+i,y,x+i,y+T-1],fill=(222,210,150,255))
 x,y=tile(LISTRA)  # listras diagonais rosa e branco (fitas e laco)
 for i in range(T):
  for j in range(T):im.putpixel((x+i,y+j),(255,240,246,255) if ((i+j)//3)%2 else (236,120,170,255))
 x,y=tile(MEIA)
 for j in range(T):d.line([x,y+j,x+T-1,y+j],fill=((150,60,130) if (j//3)%2 else (236,170,206))+(255,))
 # rosto (16x16 para a face de 10x10 da cabeca)
 x,y=tile(ROSTO);d.rectangle([x,y,x+T-1,y+T-1],fill=COR[PELE]+(255,))
 for i in range(T):d.point((x+i,y),fill=(240,232,178,255));d.point((x+i,y+1),fill=(240,232,178,255))
 for ex in (2,9):
  d.rectangle([x+ex,y+7,x+ex+4,y+7],fill=(50,34,48,255))            # contorno de cima
  d.rectangle([x+ex,y+8,x+ex+4,y+11],fill=(70,140,220,255))         # iris
  d.rectangle([x+ex,y+10,x+ex+4,y+11],fill=(120,200,250,255))       # brilho de baixo
  d.rectangle([x+ex+1,y+8,x+ex+3,y+9],fill=(36,70,150,255))         # pupila em borboleta
  d.point((x+ex+2,y+10),fill=(36,70,150,255))
  d.point((x+ex+3 if ex==2 else x+ex+1,y+8),fill=(255,255,255,255)) # reflexo
 d.line([x+2,y+5,x+6,y+6],fill=(196,170,110,255));d.line([x+9,y+6,x+13,y+5],fill=(196,170,110,255))  # sobrancelhas franzidas
 d.line([x+7,y+13,x+8,y+13],fill=(200,90,110,255));d.point((x+6,y+14),fill=(200,90,110,255));d.point((x+9,y+14),fill=(200,90,110,255))  # boquinha emburrada
 for bx in (1,12):d.rectangle([x+bx,y+12,x+bx+2,y+12],fill=(250,190,196,255))  # bochechas
 out=A/'textures/entity/subaru/beatrice.png';out.parent.mkdir(parents=True,exist_ok=True);im.save(out)
FACES=['north','south','east','west','up','down']
def uv(t,frente=None,inteiro=False):
 r={}
 for f in FACES:
  k=frente if (f=='north' and frente is not None) else t;x,y=tile(k)
  r[f]={'uv':[x,y],'uv_size':[T,T]} if (inteiro or k==ROSTO) else {'uv':[x+2,y+2],'uv_size':[T-4,T-4]}
 return r
B={}
def osso(n,pivot,parent=None):
 b={'name':n,'pivot':pivot,'cubes':[]}
 if parent:b['parent']=parent
 B[n]=b;return b
def cubo(n,o,s,t,rot=None,pivot=None,frente=None,inteiro=False):
 c={'origin':o,'size':s,'uv':uv(t,frente,inteiro)}
 if rot:c.update(rotation=rot,pivot=pivot or [o[0]+s[0]/2,o[1]+s[1]/2,o[2]+s[2]/2])
 B[n]['cubes'].append(c)
def construir():
 osso('root',[0,0,0])
 for n,x in [('perna_e',.25),('perna_d',-2.75)]:
  osso(n,[x+1.25,5,0],'root');cubo(n,[x,1.2,-1.25],[2.5,4,2.5],MEIA,inteiro=True);cubo(n,[x-.2,0,-1.7],[2.9,1.5,3.2],SAPATO)
 osso('corpo',[0,11,0],'root')
 # saia larga em sino, camadas carmim e barra felpuda
 osso('saia',[0,11,0],'corpo')
 # saia em sino de tecido liso (SuperficieTecido.anel, como a da Frieren): paineis alinhados aos eixos em 3 linhas que
 # alargam; o anel suaviza as bordas e arredonda os cantos. Barra felpuda = segundo anel, uma linha, logo abaixo.
 def anel_saia(pref,linhas,cor):
  for i,(px,pz,ax) in enumerate([(-1,-1,'x'),(0,-1,'x'),(1,-1,'x'),(1,-.5,'z'),(1,.5,'z'),(1,1,'x'),(0,1,'x'),(-1,1,'x'),(-1,.5,'z'),(-1,-.5,'z')]):
   pai='saia'
   for k,(yt,hx,hz,alt) in enumerate(linhas):
    n=f'{pref}{i}_{k}'
    if ax=='x':w=2*hx/3;x0=px*w-w/2;o=[x0,yt-alt,pz*hz-.16];sz=[w,alt,.32];pv=[x0+w/2,yt,pz*hz]
    else:d=hz;z0=(0 if pz>0 else -d);o=[px*hx-.16,yt-alt,z0];sz=[.32,alt,d];pv=[px*hx,yt,z0+d/2]
    osso(n,pv,pai);cubo(n,o,sz,cor);pai=n
 anel_saia('beatrice_saia_',[(11,3.6,2.6,2.4),(8.6,5,3.7,2.5),(6.1,6.4,4.6,2.5)],CARMIM)
 anel_saia('beatrice_barra_',[(3.8,6.9,5.0,1.3)],FELPA)
 # corpete e laco grande listrado
 cubo('corpo',[-3,11,-2],[6,5,4],CARMIM)
 cubo('corpo',[-3.6,15.2,-2.5],[7.2,1.2,5],FELPA)
 cubo('corpo',[-1,12.6,-2.7],[2,1.8,.8],LISTRA,inteiro=True)
 for s in (-1,1):
  cubo('corpo',[(-.6 if s>0 else -3.9),12.2,-2.6],[4.5,2.8,.7],LISTRA,rot=[0,0,-12*s],pivot=[0,13.5,-2.2],inteiro=True)
  cubo('corpo',[(.3 if s>0 else -1.6),8.4,-2.9],[1.3,4.2,.4],LISTRA,rot=[0,0,10*s],pivot=[0,12.6,-2.6],inteiro=True)
 # mangas em sino com punho felpudo
 for n,x,s in [('braco_e',3,1),('braco_d',-3,-1)]:
  osso(n,[x+.9*s,15.6,0],'corpo');bx=x if s>0 else x-2.4
  cubo(n,[bx,12.6,-1.4],[2.4,3.4,2.8],CARMIM)
  cubo(n,[bx-.5 if s>0 else bx-.6,9.6,-2],[3.5,3.2,4],CARMIM)
  cubo(n,[bx-.8 if s>0 else bx-.9,8.6,-2.3],[4.1,1.4,4.6],FELPA)
  cubo(n,[bx+.5 if s>0 else bx+.4,7.7,-.8],[1.6,1,1.6],PELE)
 # cabeca grande (chibi) com o rosto pintado
 osso('cabeca',[0,16.2,0],'corpo')
 cubo('cabeca',[-5,16.2,-5],[10,10,10],PELE,frente=ROSTO)
 cubo('cabeca',[-5.4,23.4,-5.4],[10.8,3.2,10.8],CABELO_TOPO,inteiro=True)
 cubo('cabeca',[-5.4,15.4,-.2],[10.8,8.4,5.6],NUCA,inteiro=True)
 for i,x in enumerate([-5.3,-2.6,.1,2.8]):cubo('cabeca',[x,22.4,-5.6],[2.5,1.8+(.6 if i in (1,2) else 0),.8],CABELO,rot=[0,0,(-6,4,-4,6)[i]])
 for x in [-5.6,4.4]:cubo('cabeca',[x,16.8,-5.2],[1.2,7,5],CABELO,inteiro=True)
 # coroa
 cubo('cabeca',[-1.8,26.4,-1.8],[3.6,1.2,3.6],OURO)
 for dx,dz in [(-1.8,-1.8),(1.0,-1.8),(-1.8,1.0),(1.0,1.0),(-.4,-1.8)]:cubo('cabeca',[dx,27.6,dz],[.8,1,.8],OURO)
 cubo('cabeca',[-.6,27.6,-.6],[1.2,1.4,1.2],RUBI)
 # brocas grossas e retas ate a cintura, atras dos ombros, com fita listrada no topo
 for lado,x in [('e',6.2),('d',-6.2)]:
  s=1 if x>0 else -1
  cubo('cabeca',[x-2.4,22,.2],[4.8,2.2,4.8],LISTRA,inteiro=True)
  pai='cabeca';y=22.2
  for k in range(7):
   n=f'broca_{lado}_{k}';r=4.4-.28*k;osso(n,[x,y,2.6],pai)
   cubo(n,[x-r/2,y-2.6,2.6-r/2],[r,2.6,r],CABELO if k%2==0 else CABELO_SOMBRA,inteiro=True)
   cubo(n,[x-r/2-.25,y-.9,2.6-r/2-.25],[r+.5,.8,r+.5],CABELO_SOMBRA,inteiro=True)   # anel da espiral
   pai=n;y-=2.3
  n=f'broca_{lado}_7';osso(n,[x,y,2.6],pai);cubo(n,[x-.9,y-1.6,1.7],[1.8,1.6,1.8],CABELO)
 # pernas a mostra abaixo da saia: sobe tudo que nao e perna e estica as meias
 for n,b in B.items():
  if n.startswith('perna'):
   b['pivot'][1]+=ALT;c=b['cubes'][0];c['size'][1]+=ALT;continue
  if n=='root':continue
  b['pivot'][1]+=ALT
  for c in b['cubes']:
   c['origin'][1]+=ALT
   if 'pivot' in c:c['pivot'][1]+=ALT
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.beatrice','texture_width':W,'texture_height':H,
      'visible_bounds_width':3,'visible_bounds_height':3,'visible_bounds_offset':[0,1,0]},'bones':list(B.values())}]}
 out=A/'geo/subaru/beatrice.geo.json';out.parent.mkdir(parents=True,exist_ok=True)
 out.write_text(json.dumps(geo,ensure_ascii=False,indent=1)+'\n',encoding='utf-8')
if __name__=='__main__':
 textura();construir();print('ok',sum(len(b['cubes']) for b in B.values()),'cubos')
