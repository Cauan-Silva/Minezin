"""Cid-only trial: four linked cape segments per panel and sparse, original emissive seams."""
from pathlib import Path
import copy,xml.etree.ElementTree as ET
from PIL import Image,ImageDraw
import build_armor_models as a
from armor_reference_shapes import shadow
from polish_clothing_shapes import polish
from clothing_uv import apply
A=a.ASSETS

def build():
 b=polish('shadow',shadow())
 # Replace the separate front tips with a closed U collar, leaving the face/hair open.
 b['armorBody']['cubes']=[c for c in b['armorBody']['cubes'] if not ('rotation'in c and c['origin'][1]>=22 and c['origin'][2]<0)]
 b['cid_yoke']=a.bone('cid_yoke',[0,23.4,3.2],'armorBody')
 a.cube(b['cid_yoke'],[-5.1,22.85,2.8],[10.2,.9,2.5],a.BLACK)
 # Side bridges extend behind the two front wings and meet the rear collar.
 for side in [-1,1]:
  x=4.85 if side>0 else -5.4
  a.cube(b['cid_yoke'],[x,23.05,-2.45],[.55,1.1,7.75],a.PLUM)
 b['cid_collar_back']=a.bone('cid_collar_back',[0,23.35,4.85],'cid_yoke')
 a.cube(b['cid_collar_back'],[-5.15,23.35,4.85],[10.3,1.8,.45],a.PLUM)
 a.cube(b['cid_collar_back'],[-5.25,25.03,4.81],[10.5,.18,.55],a.GOLD)
 for side,label in [(-1,'left'),(1,'right')]:
  name='cid_collar_'+label;pivot=[side*3.4,23.2,-2.2]
  b[name]=a.bone(name,pivot,'cid_yoke');x=side*4.0-1.5
  a.cube(b[name],[x,22.85,-2.55],[3,2.75,.45],a.PLUM,pivot,[0,0,-side*24])
  a.cube(b[name],[x,25.42,-2.61],[3,.22,.55],a.GOLD,pivot,[0,0,-side*24])
  glow='cid_glow_gold_collar_'+label;b[glow]=a.bone(glow,pivot,name)
  a.cube(b[glow],[x+.3,25.48,-2.66],[2.4,.10,.05],a.GOLD,pivot,[0,0,-side*24])
 for n in list(b):
  if n.startswith('capa_'):del b[n]
 for panel,bottom in enumerate([2.7,.7,1.5,.2,2.2]):
  x=(panel-2)*2.4
  for segment in range(4):
   n=f'cid_cape_{panel}_{segment}';parent='armorBody' if segment==0 else f'cid_cape_{panel}_{segment-1}'
   top=23-segment*5.5;z=3.2+segment*.06;h=5.8 if segment<3 else top-bottom
   b[n]=a.bone(n,[x,top,z],parent)
   a.cube(b[n],[x-1.4,top-h,z],[2.8,h,.5],a.BLACK)
   if segment>=2:a.cube(b[n],[x-1.4,top-h,z-.12],[2.8,min(h,3.5),.12],a.PLUM)
   if segment==3:a.cube(b[n],[x-1.35,top-h-.7,z],[1.3,.8,.5],a.BLACK)
   if panel in [0,2,4]:
    glow=f'cid_glow_{panel}_{segment}';b[glow]=a.bone(glow,[x,top,z],n)
    a.cube(b[glow],[x+.82,top-h+.35,z+.51],[.10,max(.3,h-.7),.06],a.LILAC)
 b['cid_glow_gold']=a.bone('cid_glow_gold',[0,24,0],'armorBody')
 for side in [-1,1]:a.cube(b['cid_glow_gold'],[side*2.6-.06,17.3,-2.99],[.12,2.5,.08],a.GOLD)
 b=apply('shadow',b)
 # Allocate isolated texels for the glow-only geometry, so the base cloth never emits.
 used=set()
 for bone in b.values():
  if bone['name'].startswith('cid_glow_'):continue
  for cube in bone.get('cubes',[]):
   for uv in cube['uv'].values():
    x,y=map(int,uv['uv']);w,h=map(int,uv['uv_size']);used.update((xx,yy) for xx in range(x,x+w) for yy in range(y,y+h))
 patches=[]
 for y in range(123,2,-1):
  for x in range(123,2,-1):
   cells={(xx,yy) for xx in range(x,x+3) for yy in range(y,y+3)}
   if not cells&used:patches.append((x,y));used|=cells
   if len(patches)==2:break
  if len(patches)==2:break
 assert len(patches)==2
 svg=A/'textures/armor/trajes/shadow.svg';text=svg.read_text(encoding='utf-8')
 for patch,color in zip(patches,['a465e8','bc9658']):
  x,y=patch;text=text.replace('</svg>',f'<rect x="{x}" y="{y}" width="3" height="3" fill="#{color}"/>\n</svg>')
 svg.write_bytes(text.encode('utf-8'))
 for name,bone in b.items():
  if not name.startswith('cid_glow_'):continue
  x,y=patches[1 if name.startswith('cid_glow_gold') else 0]
  for c in bone['cubes']:c['uv']={f:{'uv':[x+1,y+1],'uv_size':[1,1]} for f in ['north','south','east','west','up','down']}
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
 for r in ET.fromstring(text):
  x,y,w,h=[int(r.attrib[k]) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
 im.save(A/'textures/armor/trajes/shadow.png')
 mask=Image.new('RGBA',(128,128),(0,0,0,0));d=ImageDraw.Draw(mask)
 for (x,y),color in zip(patches,[(128,63,202,255),(112,79,30,255)]):d.rectangle((x,y,x+2,y+2),fill=color)
 mask.save(A/'textures/armor/trajes/shadow_glowmask.png')
 # Worn helmet is visually absent; the inventory still displays its folded hood.
 original_head=copy.deepcopy(b['armorHead']['cubes']);b['armorHead']['cubes']=[]
 a.save(A/'geo/armaduras/shadow.geo.json',a.document('shadow',b))
 for part,roots in {'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}.items():
  icon=copy.deepcopy(b)
  if part=='helmet':icon['armorHead']['cubes']=copy.deepcopy(original_head)
  def root(n):return root(icon[n]['parent']) if 'parent'in icon[n] else n
  for n,v in icon.items():
   if root(n) not in roots:v['cubes']=[]
  cubes=[c for v in icon.values() for c in v['cubes']];assert cubes
  lo=[min(c['origin'][i] for c in cubes) for i in range(3)];hi=[max(c['origin'][i]+c['size'][i] for c in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
  for v in icon.values():
   v['pivot'][1]-=mid
   for c in v['cubes']:
    c['origin'][1]-=mid
    if 'pivot'in c:c['pivot'][1]-=mid
  a.save(A/f'geo/armaduras/shadow_{part}.geo.json',a.document('shadow_'+part,icon))
  suffix={'helmet':'capuz','chestplate':'manto','leggings':'calcas','boots':'botas'}[part];scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
  a.save(A/f'models/item/{suffix}_shadow.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[8,-28,0],'scale':[scale]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3},'fixed':{'scale':[.8]*3}}})
 a.save(A/'animations/armaduras/shadow.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':2,'bones':{}}}})
 print('Cid trial: 5 panels x 4 linked bones; sparse emissive mask; worn head/hair preserved.')
if __name__=='__main__':build()
