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

def espada():
 b={'root':armor.bone('root',[0,0,0])};c=armor.cube;r=b['root']
 c(r,[-.6,-10,-.6],[1.2,10,1.2],armor.LEATHER)
 for y in [-9,-7,-5,-3]:c(r,[-.68,y,-.68],[1.36,.5,1.36],armor.INK)
 c(r,[-1,-10.8,-.8],[2,1.2,1.6],armor.INK)
 c(r,[-4.8,-.2,-.8],[9.6,1.4,1.6],armor.GOLD)
 c(r,[-4.4,1.2,-.65],[8.8,24,1.3],armor.STEEL)
 c(r,[-3.9,1.3,-.75],[7.8,23.5,1.5],armor.INK)
 for side in [-1,1]:c(r,[side*3.9-.22,2,-.8],[.44,22,.18],armor.NAVY)
 c(r,[-3.5,25.2,-.6],[7,3,1.2],armor.STEEL)
 c(r,[-2.8,25.2,-.68],[5.6,2.5,1.36],armor.INK)
 c(r,[-2.2,28.2,-.55],[4.4,2.2,1.1],armor.STEEL)
 c(r,[-1.5,28.2,-.64],[3,1.5,1.28],armor.INK)
 c(r,[-.6,30.4,-.5],[1.2,1.6,1],armor.STEEL)
 return b
def forma():
 from armor_reference_shapes import asta_forma
 pieces=asta_forma();save(A/'models/effects/asta_forma.json',{'pieces':pieces})
 geo={name:armor.bone(name,pivot) for name,pivot in [('body',[0,24,0]),('head',[0,24,0]),('right_arm',[-5,22,0])]}
 for p in pieces:
  armor.cube(geo[p['attachment']],p['origin'],p['size'],armor.RUBY if p['color'] in ['621526','76182D'] else armor.BLACK,p['pivot'],p['rotation'])
 doc=armor.document('asta_forma',geo);doc['minecraft:geometry'][0]['description'].update(visible_bounds_width=8,visible_bounds_height=7)
 save(A/'geo/asta/forma.geo.json',doc)

def liebe():
 from build_nemesis_models import liebe as shape
 save(A/'geo/nemesis/4.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.nemesis_4','texture_width':64,'texture_height':64,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(shape().values())}]})
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
def main():
 # Reuse the established per-slot exporter without rewriting older authored pieces.
 # Export Asta through a dedicated subset identical to the existing renderer's contract.
 from clothing_uv import apply
 from polish_clothing_shapes import polish
 bones=apply('asta',polish('asta',traje()));save(A/'geo/armaduras/asta.geo.json',armor.document('asta',bones))
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
  save(A/f'models/item/asta_{suffix}.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[8,-28,0],'scale':[scale]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3},'fixed':{'scale':[.8]*3}}})
 anim={name:{'rotation':{'0':[0,0,0],'2':[1.2 if name.startswith('capa_') else .6,0,0],'4':[0,0,0]}} for name in bones if name.startswith(('manto_','capa_'))}
 save(A/'animations/armaduras/asta.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':anim}}})
 save(A/'geo/asta/matadora.geo.json',armor.document('matadora',espada()));save(A/'animations/asta/matadora.animation.json',{'format_version':'1.8.0','animations':{'animation.matadora.idle':{'loop':True,'animation_length':1,'bones':{}}}})
 save(A/'models/item/matadora_demonios.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[0,0,-30],'translation':[0,0,0],'scale':[.38]*3},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.8]*3},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,-15,-12],'translation':[0,0,-1],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,15,12],'translation':[0,0,-1],'scale':[.65]*3},'ground':{'rotation':[0,0,90],'scale':[.5]*3},'fixed':{'rotation':[0,0,-25],'scale':[.4]*3}}})
 forma();liebe();animations();recipes();glyph();print('Authored Asta uniform, sword, awakening, Liebe, 9 gestures and recipes.')
if __name__=='__main__':main()
