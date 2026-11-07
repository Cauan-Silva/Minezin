"""Original Asta assets: clean cuboids, shared palette, authored keyframes and native pixel glyph."""
from pathlib import Path
import copy,json,math,struct,zlib
import build_armor_models as armor
import build_nemesis_models as boss
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol';D=ROOT/'src/main/resources/data/arcanecontrol'
def save(p,j):armor.save(p,j)
def traje():
 from armor_reference_shapes import asta
 return asta()

def _alinhar(b):
 """Align the middle of the ten-unit grip with the held-item origin (all Asta swords share this)."""
 for part in b.values():
  part['pivot'][1]+=5
  for cb in part.get('cubes',[]):
   cb['origin'][1]+=5
   if 'pivot' in cb:cb['pivot'][1]+=5
 return b

def _octogono(r,cx,cy,z0,lado,prof,tile):
 """Round-looking disc in the xy plane: two squares, one turned 45 degrees."""
 armor.cube(r,[cx-lado/2,cy-lado/2,z0],[lado,lado,prof],tile)
 armor.cube(r,[cx-lado/2,cy-lado/2,z0+.02],[lado,lado,prof-.04],tile,[cx,cy,0],[0,0,45])

# Demon-Slayer blade outline (user's reference): broad straight blade, then a stepped chisel tip.
MATADORA_LAMINA=(9.0,1.2,24.0)
MATADORA_PONTA=[(7.8,25.2,2.4),(6.2,27.6,2.2),(4.4,29.8,2.0),(2.6,31.8,1.8),(1.0,33.6,1.4)]
def _empunhadura_matadora(r,cabo,metal):
 """Grip, round pommel, cross guard with flared ends and the round boss, as in the user's reference."""
 c=armor.cube
 c(r,[-.6,-10,-.6],[1.2,10,1.2],cabo)
 for y in [-9.2,-6.6,-4.0,-1.4]:c(r,[-.7,y,-.7],[1.4,.5,1.4],metal)
 _octogono(r,0,-11.2,-.9,2.4,1.8,metal)
 c(r,[-5.4,-.1,-.75],[10.8,1.3,1.5],metal)
 for side in [-1,1]:
  c(r,[side*5.6-.7,-.5,-.8],[1.4,2.1,1.6],metal,[side*5.6,.55,0],[0,0,side*12])
 _octogono(r,0,.55,-1.15,2.6,2.3,metal)

def espada():
 """Demon-Slayer Sword as in the user's reference: grey broad blade with black ink blotches (painted by
 _manchas), black cross guard with flared ends, round boss and pommel, long ringed grip."""
 b={'root':armor.bone('root',[0,0,0])};r=b['root'];c=armor.cube
 _empunhadura_matadora(r,armor.INK,armor.SHADOW)
 b['blade']=armor.bone('blade',[0,1.2,0],'root');bl=b['blade']
 w,y,h=MATADORA_LAMINA;c(bl,[-w/2,y,-.65],[w,h,1.3],armor.STEEL)
 for w,y,h in MATADORA_PONTA:c(bl,[-w/2,y,-.6],[w,h,1.2],armor.STEEL)
 b['energia_divider']=armor.bone('energia_divider',[0,1.2,0],'blade')
 for side in [-1,1]:
  c(b['energia_divider'],[side*4.32-.1,2,-.87],[.2,22.6,.14],armor.RUBY)
  c(b['energia_divider'],[side*3.0-.1,25.6,-.8],[.2,2.2,.12],armor.RUBY)
 return _alinhar(b)

def _manchas(im,glow,bones,k):
 """Black ink blotches over the grey Demon-Slayer blade (front and back), denser toward the edges."""
 import random
 from PIL import ImageDraw
 di=ImageDraw.Draw(im);rnd=random.Random(7);TINTA=(21,19,24,255)
 for idx,cb in enumerate(bones['blade']['cubes']):
  for f in ('north','south'):
   uv=cb['uv'][f];u,v,w,h=uv['uv'][0],uv['uv'][1],uv['uv_size'][0],uv['uv_size'][1]
   for _ in range(int(w*h/(k*k)*.07)+1):
    lado=rnd.choice((0,1));x=u+(rnd.uniform(-.05,.22) if lado==0 else rnd.uniform(.78,1.05))*w;y=v+rnd.uniform(0,1)*h
    for _ in range(rnd.randint(4,7)):
     rx,ry=rnd.uniform(.6,1.5)*k,rnd.uniform(.7,1.9)*k;ox,oy=rnd.uniform(-1.4,1.4)*k,rnd.uniform(-1.6,1.6)*k
     x0,y0,x1,y1=max(u,x+ox-rx),max(v,y+oy-ry),min(u+w-1,x+ox+rx),min(v+h-1,y+oy+ry)
     if x1>x0 and y1>y0:di.ellipse([x0,y0,x1,y1],fill=TINTA)

def matadora():
 bones=_textura('matadora',espada(),{},_manchas)
 save(A/'geo/asta/matadora.geo.json',_documento('matadora',bones,visible_bounds_width=3,visible_bounds_height=4))
 print('Matadora:',sum(len(x['cubes']) for x in bones.values()),'cubos')

def sword_display():
 return {'parent':'builtin/entity','display':{
  'gui':{'rotation':[0,0,-30],'translation':[0,-1.9,0],'scale':[.38]*3},
  'thirdperson_righthand':{'rotation':[0,-90,10],'translation':[0,0,0],'scale':[.8]*3},
  'thirdperson_lefthand':{'rotation':[0,90,-10],'translation':[0,0,0],'scale':[.8]*3},
  'firstperson_righthand':{'rotation':[0,-45,25],'translation':[0,0,0],'scale':[.65]*3},
  'firstperson_lefthand':{'rotation':[0,45,-25],'translation':[0,0,0],'scale':[.65]*3},
  'ground':{'rotation':[0,0,90],'scale':[.5]*3},'fixed':{'rotation':[0,0,-25],'scale':[.4]*3}}}

ESCALA_TEXTURA=4
def _rachaduras(im,rects,k):
 """Glowing lava cracks over the dark islands (concept art look). Deterministic per island; the hot red pixels
 go to the glowmask, the dark-red rims around them stay matte."""
 import random
 CORE,HOT,RIM=(255,42,61,255),(255,79,36,255),(58,10,16,255)
 for (u,v,w,h),densidade in rects.items():
  if w*h<6:continue
  rng=random.Random(u*7919+v*104729+w*31+h);pintados=set()
  def andar(x,y,ang,passos,ramo):
   for _ in range(passos):
    ang+=rng.uniform(-.7,.7);x+=math.cos(ang);y+=math.sin(ang)
    ix,iy=int(round(x)),int(round(y))
    if not(u<=ix<u+w and v<=iy<v+h):return
    pintados.add((ix,iy))
    if ramo and rng.random()<.12:andar(x,y,ang+rng.choice([-1.1,1.1]),max(2,passos//3),False)
  # Density is per model texel; at k texels per model unit the strokes stay one texel thin.
  for _ in range(max(1,int(w*h/(densidade*k*k)*1.6))):
   andar(rng.uniform(u,u+w-1),rng.uniform(v,v+h-1),rng.uniform(0,2*math.pi),int(max(w,h)*rng.uniform(.5,.9)),True)
  for x,y in pintados:
   for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
    q=(x+dx,y+dy)
    if q not in pintados and u<=q[0]<u+w and v<=q[1]<v+h and rng.random()<.6:im.putpixel(q,RIM)
  for x,y in pintados:im.putpixel((x,y),HOT if rng.random()<.18 else CORE)

def _textura(style,bones,densidade,marcas=None):
 """Outfit-style UV islands (clothing_uv) at ESCALA_TEXTURA texels per model unit, lava cracks over the materials
 in `densidade` (texels per crack), optional painted sigils (`marcas(im,glow,bones,k)`) and a glowmask with only
 the glowing red. Writes textures/armor/trajes/<style>.png
 and <style>_glowmask.png; returns the bones with their final UVs."""
 from clothing_uv import apply
 import xml.etree.ElementTree as ET
 from PIL import Image,ImageDraw
 # Remember each cube's material before apply() replaces the atlas UVs with the outfit islands.
 tiles=[int(c['uv']['north']['uv'][0]/armor.W*4)+4*int(c['uv']['north']['uv'][1]/armor.H*4) for b in bones.values() for c in b['cubes']]
 bones=apply(style,bones);k=ESCALA_TEXTURA
 # Outfit islands are laid out at 1 texel per model unit; k times that keeps the cracks thin.
 for b in bones.values():
  for c in b['cubes']:
   for f in c['uv'].values():f['uv']=[f['uv'][0]*k,f['uv'][1]*k];f['uv_size']=[f['uv_size'][0]*k,f['uv_size'][1]*k]
 tex=A/'textures/armor/trajes';im=Image.new('RGBA',(128*k,128*k));dr=ImageDraw.Draw(im)
 for r in ET.fromstring((tex/f'{style}.svg').read_text(encoding='utf-8')):
  x,y,w,h=[int(float(r.attrib[n]))*k for n in ['x','y','width','height']];dr.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
 rects={}
 for tile,c in zip(tiles,[c for b in bones.values() for c in b['cubes']]):
  if tile in densidade:
   for f in c['uv'].values():rects[(int(f['uv'][0]),int(f['uv'][1]),int(f['uv_size'][0]),int(f['uv_size'][1]))]=densidade[tile]
 _rachaduras(im,rects,k)
 # Glowmask: only the red, drawn full-bright by the emissive layer (Cid's shadow_glowmask pattern).
 glow=Image.new('RGBA',im.size)
 for y in range(im.size[1]):
  for x in range(im.size[0]):
   r,g,bl,al=im.getpixel((x,y))
   if al and r>150 and g<90 and bl<110:glow.putpixel((x,y),(r,g,bl,255))
 if marcas:marcas(im,glow,bones,k)
 im.save(tex/f'{style}.png');glow.save(tex/f'{style}_glowmask.png')
 return bones

def _documento(style,bones,**limites):
 doc=armor.document(style,bones);d=doc['minecraft:geometry'][0]['description']
 d.update(texture_width=128*ESCALA_TEXTURA,texture_height=128*ESCALA_TEXTURA,**limites);return doc

def _sigilos(im,glow,bones,k):
 """Glowing red sigils of the Devil Union (user's reference video), drawn per cube face in model units:
 chest star with a line down to the belly, belly cross in a square, spine, shoulder X, arm strokes, leg chevron
 ribs and the wing hem. Halo stays matte; red and the light core also go to the glowmask (bloom look)."""
 from PIL import ImageDraw
 di,dg=ImageDraw.Draw(im),ImageDraw.Draw(glow)
 HALO,RED,CORE=(96,10,20,255),(255,42,52,255),(255,196,176,255)
 def face(bone,idx,f):
  uv=bones[bone]['cubes'][idx]['uv'][f];return uv['uv'][0],uv['uv'][1],uv['uv_size'][0],uv['uv_size'][1]
 def traco(rect,pts,larg=2.6):
  u,v,w,h=rect;q=[(u+min(w-1,max(0,x*k)),v+min(h-1,max(0,y*k))) for x,y in pts]
  di.line(q,fill=HALO,width=int(larg*k*.5+4),joint='curve')
  for d in (di,dg):d.line(q,fill=RED,width=int(larg*k*.5),joint='curve')
  for d in (di,dg):d.line(q,fill=CORE,width=max(1,int(larg*k*.18)),joint='curve')
 def losango(rect,cx,cy,r):
  u,v,w,h=rect;q=[(u+cx*k,v+(cy-r)*k),(u+(cx+r)*k,v+cy*k),(u+cx*k,v+(cy+r)*k),(u+(cx-r)*k,v+cy*k)]
  di.polygon(q,fill=RED);dg.polygon(q,fill=RED)
  m=[(u+cx*k,v+(cy-r*.45)*k),(u+(cx+r*.45)*k,v+cy*k),(u+cx*k,v+(cy+r*.45)*k),(u+(cx-r*.45)*k,v+cy*k)];di.polygon(m,fill=CORE);dg.polygon(m,fill=CORE)
 # Chest: star sigil whose long ray runs down to the belly cross; spine on the back.
 r=face('armorBody',0,'north');W,H=r[2]/k,r[3]/k;cx=W/2
 traco(r,[(cx,1.0),(cx,9.4)],3.2);traco(r,[(1.0,3.6),(W-1.0,3.6)],3.0)
 traco(r,[(1.1,.8),(cx,3.6),(W-1.1,.8)],2.4);traco(r,[(2.3,6.5),(cx,3.6),(W-2.3,6.5)],2.2);losango(r,cx,3.6,1.2)
 traco(r,[(cx-1.3,9.6),(cx+1.3,9.6),(cx+1.3,12.2),(cx-1.3,12.2),(cx-1.3,9.6)],1.6);traco(r,[(cx,9.9),(cx,11.9)],1.8);traco(r,[(cx-1.0,10.9),(cx+1.0,10.9)],1.8)
 r=face('armorBody',0,'south');W,H=r[2]/k,r[3]/k
 traco(r,[(W/2,.8),(W/2,H-1.2)],2.2)
 for y in [2.2,4.0,5.8,7.6,9.4]:traco(r,[(W/2-1.3,y-.6),(W/2,y),(W/2+1.3,y-.6)],1.4)
 for arm in ['armorRightArm','armorLeftArm']:
  # Arm strokes like runes, and the glowing X on every side of the shoulder pad.
  for f in ['north','south','east','west']:
   r=face(arm,0,f);W,H=r[2]/k,r[3]/k
   traco(r,[(W*.3,5.4),(W*.62,6.8),(W*.36,8.2)],1.6);traco(r,[(W*.6,10.0),(W*.4,11.6)],1.6)
  for f in ['north','south','east','west','up']:
   r=face(arm,1,f);W,H=r[2]/k,r[3]/k;m=.7
   traco(r,[(m,m),(W-m,H-m)],2.2);traco(r,[(W-m,m),(m,H-m)],2.2)
 for leg in ['armorRightLeg','armorLeftLeg']:
  # Legs: chevron ribs on a centre line (front), lighter strokes on the other sides.
  r=face(leg,0,'north');W,H=r[2]/k,r[3]/k
  traco(r,[(W/2,1.0),(W/2,H-1.0)],1.8)
  for y in [2.0,4.2,6.4,8.6,10.8]:traco(r,[(.8,y),(W/2,y+1.3),(W-.8,y)],1.8)
  for f in ['east','west','south']:
   r=face(leg,0,f);W,H=r[2]/k,r[3]/k
   for y in [3.0,7.0,10.6]:traco(r,[(W*.25,y),(W*.5,y+1.0),(W*.75,y)],1.4)
 # Wing hem: the bottom of every lower membrane strip glows, like the burning edge in the reference.
 for nome in bones:
  if nome.startswith('du_asa_') and nome.endswith('_2') and nome.count('_')==4:
   for f in ['north','south']:
    r=face(nome,0,f);W,H=r[2]/k,r[3]/k;traco(r,[(0,H-.35),(W,H-.35)],1.6)

def forma():
 """Devil Union as an outfit-style GeckoLib model: geo + texture with painted glowing sigils + glowmask (red only),
 rendered by RenderFormaAsta with all bones visible. Wings/tail bones are posed by DevilUnionTecido."""
 from armor_reference_shapes import asta_forma
 bones=_textura('asta_forma',asta_forma(),{},_sigilos)
 save(A/'geo/armaduras/asta_forma.geo.json',_documento('asta_forma',bones,visible_bounds_width=8,visible_bounds_height=7))
 save(A/'animations/armaduras/asta_forma.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':6,'bones':{}}}})
 print('Devil Union:',len(bones),'ossos',sum(len(x['cubes']) for x in bones.values()),'cubos')

def espada_desperta():
 """Demon-Slayer Sword in the Devil Union (user's screenshot): the same silhouette as espada(), the blade turned
 dark crimson under the same black ink blotches, no red lines; the black-and-red flame aura around it is the
 animated AuraMatadora effect, not geometry. Same grip and bones, so the swap and the Black Divider do not jump."""
 return espada()

def matadora_desperta():
 bones=_textura('matadora_desperta',espada_desperta(),{},_manchas)
 save(A/'geo/asta/matadora_desperta.geo.json',_documento('matadora_desperta',bones,visible_bounds_width=3,visible_bounds_height=4))
 print('Matadora desperta:',sum(len(x['cubes']) for x in bones.values()),'cubos')

def demon_dweller():
 """Demon-Dweller Sword (user's reference), pulled from the grimoire in the ultimate: slimmer straight blade with a
 lighter central fuller, rounded lobed guard, segmented grip. The four-point stars near the guard and the tip are
 painted by _estrelas in glowing red, the Devil Union style. Same grip convention as espada()."""
 b={'root':armor.bone('root',[0,0,0])};r=b['root'];c=armor.cube
 c(r,[-.55,-10,-.55],[1.1,10,1.1],armor.INK)
 for y in [-9.4,-7.0,-4.6,-2.2]:c(r,[-.75,y,-.75],[1.5,1.5,1.5],armor.SHADOW)
 c(r,[-.7,-11.2,-.7],[1.4,1.2,1.4],armor.SHADOW)
 # Lobed guard: a centre plate and two round lobes, like the reference's flower-shaped guard.
 c(r,[-2.0,-.3,-.9],[4.0,1.6,1.8],armor.SHADOW)
 for side in [-1,1]:_octogono(r,side*2.5,.7,-.8,2.6,1.6,armor.SHADOW)
 b['blade']=armor.bone('blade',[0,1.2,0],'root');bl=b['blade']
 c(bl,[-2.6,1.2,-.55],[5.2,27,1.1],armor.BLACK)
 for w,y,h in [(4.2,28.2,2.2),(2.8,30.4,2.0),(1.4,32.4,1.6)]:c(bl,[-w/2,y,-.5],[w,h,1.0],armor.BLACK)
 c(bl,[-.45,2.0,-.62],[.9,26,1.24],armor.STEEL)
 return _alinhar(b)

def _estrelas(im,glow,bones,k):
 """Glowing four-point stars on the Demon-Dweller: three on each side of the fuller near the guard, one near the tip."""
 from PIL import ImageDraw
 di,dg=ImageDraw.Draw(im),ImageDraw.Draw(glow);RED,CORE=(255,42,52,255),(255,196,176,255)
 def estrela(u,v,cx,cy,r):
  pts=[]
  for i in range(8):
   a=i*math.pi/4;rr=r if i%2==0 else r*.28;pts.append((u+(cx+math.sin(a)*rr)*k,v+(cy-math.cos(a)*rr)*k))
  for d in (di,dg):d.polygon(pts,fill=RED)
  m=[(u+(cx+math.sin(i*math.pi/4)*(r*.45 if i%2==0 else r*.14))*k,v+(cy-math.cos(i*math.pi/4)*(r*.45 if i%2==0 else r*.14))*k) for i in range(8)]
  for d in (di,dg):d.polygon(m,fill=CORE)
 lam=bones['blade']['cubes'][0]
 for f in ('north','south'):
  uv=lam['uv'][f];u,v,w,h=uv['uv'][0],uv['uv'][1],uv['uv_size'][0],uv['uv_size'][1];W,H=w/k,h/k
  # v grows from the tip end of the face (top of the cube) toward the guard.
  for lado in (-1,1):
   for j,(dx,dy) in enumerate([(1.35,H-2.2),(1.6,H-4.4),(1.35,H-6.6)]):estrela(u,v,W/2+lado*dx,dy,.9)
  estrela(u,v,W/2,1.6,.8)

def demon_dweller_espada():
 bones=_textura('demon_dweller',demon_dweller(),{},_estrelas)
 save(A/'geo/asta/demon_dweller.geo.json',_documento('demon_dweller',bones,visible_bounds_width=3,visible_bounds_height=4))
 print('Demon-Dweller:',sum(len(x['cubes']) for x in bones.values()),'cubos')

def liebe():
 from build_nemesis_models import liebe as shape
 save(A/'geo/nemesis/4.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.nemesis_4','texture_width':64,'texture_height':64,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(shape().values())}]})
# Ultimate (V in the Devil Union, UltAsta): tick -> rotation in degrees, PlayerAnimator convention (negative x =
# arm forward/up). Left hand pulls the Demon-Dweller out of the grimoire, both blades cross, the arms alternate one
# cut per sword ending exactly on each cut's hit tick (32..52), both swords come down crossed on tick 60 (the X),
# and the left hand returns the sword to the book.
ULT_BRACOS={
 'right_arm':{0:[-30,0,10],18:[-40,0,15],24:[-80,-25,35],30:[-80,-25,35],
  29.5:[-160,20,10],32:[-45,-40,0],37:[-160,20,10],40:[-45,-40,0],45:[-160,20,10],48:[-45,-40,0],
  56:[-170,10,10],60:[-50,-45,0],62:[-50,-45,0],70:[-40,-20,0],80:[0,0,0]},
 'left_arm':{0:[0,0,0],6:[-70,40,-20],12:[-75,45,-25],18:[-110,30,-10],24:[-80,25,-35],30:[-80,25,-35],
  33:[-160,-20,-10],36:[-45,40,0],41:[-160,-20,-10],44:[-45,40,0],49:[-160,-20,-10],52:[-45,40,0],
  56:[-170,-10,-10],60:[-50,45,0],66:[-70,40,-20],74:[-60,30,-15],80:[0,0,0]},
 'body':{0:[0,0,0],10:[0,25,0],24:[5,0,0],32:[10,-20,0],36:[10,20,0],40:[10,-20,0],44:[10,20,0],48:[10,-20,0],
  52:[10,20,0],56:[-10,0,0],60:[20,0,0],70:[5,15,0],80:[0,0,0]},
}
def ult_animacao():
 return {'animation_length':4.0,'loop':False,'bones':{osso:{'rotation':{str(round(t/20,3)):{'vector':v,'easing':'easeInOutSine'} for t,v in sorted(chaves.items())}} for osso,chaves in ULT_BRACOS.items()}}

def animations():
 all={}
 for tipo in [200,*range(300,308)]:
  length=2.75 if tipo==200 else 1.3
  if tipo==200:poses=[[-15,-15,-20],[-95,10,-10],[-40,20,-35],[0,0,0]]
  elif tipo in [302,304]:poses=[[-35,0,-20],[-115,15,-20],[-100,10,-10],[0,0,0]]
  elif tipo in [301,306]:poses=[[-150,-20,-15],[-165,5,-10],[10,10,20],[0,0,0]]
  elif tipo==303:poses=[[-90,-20,-30],[-90,-20,-30],[-90,-20,-30],[0,0,0]]
  else:poses=[[-60,-50,-25],[-100,-65,-15],[-65,75,30],[0,0,0]]
  frames={str(round(t*length,3)):{'vector':v,'easing':'easeInOutSine'} for t,v in zip([0,.3,.6,1],poses)}
  all[f'asta_{tipo}']={'animation_length':length,'loop':False,'bones':{'right_arm':{'rotation':frames},'body':{'rotation':{'0':{'vector':[0,0,0]},str(round(length*.5,3)):{'vector':[8,25 if tipo%2==0 else -20,0]},str(length):{'vector':[0,0,0]}}}}}
 from refine_player_timing import refine_asta
 refine_asta(all)
 from cinematic_animations import refine
 refine(all,"asta")
 all['asta_307']=ult_animacao() # the ultimate replaces the old generic V gesture, after the timing refiners
 save(A/'player_animation/asta.json',{'format_version':'1.8.0','geckolib_format_version':2,'animations':all})
def recipes():
 for suffix,base in [('cabeca','helmet'),('peito','chestplate'),('pernas','leggings'),('botas','boots'),('matadora_demonios','sword')]:
  item=suffix if suffix=='matadora_demonios' else 'asta_'+suffix
  save(D/f'recipes/{item}.json',{'type':'minecraft:smithing_transform','template':{'item':'minecraft:paper'},'base':{'item':'minecraft:diamond_sword' if base=='sword' else 'minecraft:netherite_'+base},'addition':{'item':'minecraft:obsidian'},'result':{'item':'arcanecontrol:'+item}})
  save(D/f'advancements/recipes/combat/{item}.json',{'parent':'minecraft:recipes/root','criteria':{'has_obsidian':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['minecraft:obsidian']}]}}},'rewards':{'recipes':['arcanecontrol:'+item]}})
def glyph():
 # A native 16px HUD glyph authored as a pixel grid, not an edited external bitmap.
 rows=['................','.....rrrrrr.....','...rr......rr...','..r....rr....r..','.r....rrrr....r.','.r....r..r....r.','r..rr.r..r.rr..r','r...rrrrrrrr...r','r...rrrrrrrr...r','r..rr.r..r.rr..r','.r....r..r....r.','.r....rrrr....r.','..r....rr....r..','...rr......rr...','.....rrrrrr.....','................']
 pixels=b''.join(b'\x00'+b''.join(bytes((218,54,83,255)) if c=='r' else bytes((0,0,0,0)) for c in row) for row in rows)
 def chunk(t,d):return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
 png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
 p=A/'textures/mob_effect/silencio.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(png)
def uniforme():
 """Asta's base outfit (geo, per-slot icons, idle sway of the rigid shoulder mantles)."""
 # Reuse the established per-slot exporter without rewriting older authored pieces.
 # Export Asta through a dedicated subset identical to the existing renderer's contract.
 from clothing_uv import apply
 from polish_clothing_shapes import polish
 bones=apply('asta',polish('asta',traje()));save(A/'geo/armaduras/asta.geo.json',armor.document('asta',bones))
 # The outfit texture is exactly the clothing_uv vector islands, rasterized 1:1.
 import xml.etree.ElementTree as ET
 from PIL import Image,ImageDraw
 svg=A/'textures/armor/trajes/asta.svg';im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
 for r in ET.fromstring(svg.read_text(encoding='utf-8')):
  x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
 im.save(A/'textures/armor/trajes/asta.png')
 subsets={'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}
 for part,roots in subsets.items():
  icon=copy.deepcopy(bones)
  def root(b):return root(icon[b['parent']]) if b.get('parent') else b['name']
  for b in icon.values():
   if root(b) not in roots:b['cubes']=[]
  cubes=[c for b in icon.values() for c in b['cubes']];lo=[min(c['origin'][i] for c in cubes) for i in range(3)];hi=[max(c['origin'][i]+c['size'][i] for c in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
  for b in icon.values():
   b['pivot'][1]-=mid
   for c in b['cubes']:
    c['origin'][1]-=mid
    if 'pivot'in c:c['pivot'][1]-=mid
  save(A/f'geo/armaduras/asta_{part}.geo.json',armor.document('asta_'+part,icon))
  suffix={'helmet':'cabeca','chestplate':'peito','leggings':'pernas','boots':'botas'}[part];scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
  save(A/f'models/item/asta_{suffix}.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[20,-35,0],'translation':[0,0,0],'scale':[.72]*3},'ground':{'translation':[0,3,0],'scale':[.35]*3},'fixed':{'scale':[.7]*3},'thirdperson_righthand':{'translation':[0,2,1],'scale':[.35]*3},'firstperson_righthand':{'translation':[0,3,0],'scale':[.4]*3}}})
 anim={name:{'rotation':{'0':[0,0,0],'2':[1.2 if name.startswith('capa_') else .6,0,0],'4':[0,0,0]}} for name in bones if name.startswith(('manto_','capa_'))}
 save(A/'animations/armaduras/asta.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':anim}}})
 from export_blockbench_projects import make_project,OUT
 (OUT/'asta.bbmodel').write_text(json.dumps(make_project('asta'),ensure_ascii=False,separators=(',',':'))+chr(10),encoding='utf-8')

def main():
 uniforme()
 matadora();save(A/'animations/asta/matadora.animation.json',{'format_version':'1.8.0','animations':{'animation.matadora.idle':{'loop':True,'animation_length':1,'bones':{}}}})
 save(A/'models/item/matadora_demonios.json',sword_display())
 forma();matadora_desperta();demon_dweller_espada();liebe();animations();recipes();glyph();print('Authored Asta uniform, sword, awakening, Liebe, 9 gestures and recipes.')
if __name__=='__main__':main()
