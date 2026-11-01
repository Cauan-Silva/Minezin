"""Original Asta assets: clean cuboids, shared palette, authored keyframes and native pixel glyph."""
from pathlib import Path
import copy,json,math,struct,zlib
import build_armor_models as armor
import build_nemesis_models as boss
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol';D=ROOT/'src/main/resources/data/arcanecontrol'
def save(p,j):armor.save(p,j)
def traje():
 b=armor.skeleton();c=armor.cube
 h=b['armorHead'];c(h,[-4.22,30,-4.3],[8.44,1.9,.35],armor.BLACK)
 for x in [-4.25,3.9]:c(h,[x,30,-4.1],[.35,1.9,8.2],armor.BLACK)
 c(h,[-4.22,30,3.9],[8.44,1.9,.35],armor.BLACK)
 c(h,[-3.9,30.3,-4.55],[1.3,1.2,.2],armor.GOLD)
 body=b['armorBody'];c(body,[-4.12,12,-2.12],[8.24,11.8,4.24],armor.IVORY)
 for x in [-4.4,1.7]:c(body,[x,12.5,-2.55],[2.7,9.7,.5],armor.INK)
 c(body,[-4.4,12.5,2.15],[8.8,10,.5],armor.INK)
 c(body,[-4.5,12,-2.55],[9,1.2,5.1],armor.LEATHER)
 c(body,[-.8,12,-2.8],[1.6,1.2,.4],armor.GOLD)
 for name,x in [('armorLeftArm',4),('armorRightArm',-8.1)]:
  c(b[name],[x,19,-2.15],[4.1,4.5,4.3],armor.IVORY)
  c(b[name],[x-.12,18.8,-2.25],[4.34,.7,4.5],armor.INK)
  c(b[name],[x,12.3,-2.12],[4.1,2,4.24],armor.LEATHER)
  c(b[name],[x-.1,12.3,-2.22],[4.3,.5,4.44],armor.GOLD)
 b['capa_touro']=armor.bone('capa_touro',[0,22.8,2.6],'armorBody')
 c(b['capa_touro'],[-4.7,17.2,2.6],[9.4,6,.55],armor.INK)
 c(b['capa_touro'],[-4.8,17.1,2.65],[9.6,.6,.65],armor.GOLD)
 c(body,[-4.75,21.8,-2.5],[9.5,1.7,5.4],armor.INK)
 for x in [-4.8,3.9]:c(body,[x,18.4,-2.6],[.9,3.4,.4],armor.GOLD)
 # Bull crest built from six clear strokes rather than a noisy texture.
 crest=b['capa_touro']
 c(crest,[-1.2,19,3.23],[2.4,1.6,.18],armor.GOLD)
 for side in [-1,1]:
  c(crest,[side*1.6-.35,20,3.23],[.7,1.5,.18],armor.GOLD,[side*1.6,20,3.23],[0,0,side*35])
 armor.legs(b,armor.NAVY,armor.LEATHER,armor.INK)
 b['grimoire']=armor.bone('grimoire',[-4.5,12,1],'armorBody');c(b['grimoire'],[-5.7,9.5,.1],[1.3,4.3,3.4],armor.BROWN)
 c(b['grimoire'],[-5.8,9.6,.2],[.2,4.1,3.2],armor.GOLD)
 return b
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
 pieces=[]
 def piece(o,s,color,pivot,rot=(0,0,0),wing=True):pieces.append({'origin':o,'size':s,'color':color,'pivot':pivot,'rotation':list(rot),'wing':wing})
 pivot=[-4,23,3]
 piece([-17,22,3],[14,2.6,1.4],'19131E',pivot,(0,0,-30))
 piece([-33,27,3],[18,2.6,1.3],'23131F',[-16,29,3],(0,0,8))
 piece([-18,26.5,2.8],[4,4,1.8],'19131E',pivot)
 for i in range(11):
  x=-10-i*2.3;y=25+math.sin(i*math.pi/12)*4;length=13+(i*.2 if i<6 else -(i-6)*1.3)
  local=[x+1.45,y,3.5];rot=(0,0,-10-i*2)
  piece([x,y-length,3.5],[2.9,length,1.0],'19131E' if i%2==0 else '28141F',local,rot)
  piece([x+.65,y-length-1.5,3.5],[1.6,2,1],'19131E',local,rot)
  if i%3==0:piece([x+.9,y-length+2,3.3],[.28,length-2,.18],'72162E',local,rot)
 # Demon horn is an awakening attachment; no hair or face replacement.
 for i in range(4):piece([-3.9-i*.55,28.4+i*1.8,-1.8],[1.9-i*.3,2.8-i*.3,1.7-i*.2],'26131F',[-3,28.4,-1],(0,0,-25-i*10),False)
 save(A/'models/effects/asta_forma.json',{'pieces':pieces})
 # Same source geometry exported with palette UVs for native GPU inspection.
 geo={'root':armor.bone('root',[0,0,0])}
 for i,p in enumerate(pieces):armor.cube(geo['root'],p['origin'],p['size'],armor.RUBY if p['color']=='72162E' else armor.INK,p['pivot'],p['rotation'])
 save(A/'geo/asta/forma.geo.json',armor.document('asta_forma',geo))
def liebe():
 b=boss.base();boss.human(b,boss.BLACK,boss.BLACK,boss.WHITE,boss.SILVER)
 for side in [-1,1]:
  for i in range(3):boss.cube(b['head'],[side*(3.2+i*.7)-.6,30+i*1.5,-1.2],[1.5-i*.3,2.6,1.4-i*.3],boss.RED,rotation=[0,0,-side*(25+i*10)])
  n='wing'+str(side);b[n]=boss.bone(n,[side*3.5,22,2.5],'body')
  boss.cube(b[n],[-17 if side<0 else 3,22,2.5],[14,2.6,1.4],boss.BLACK,rotation=[0,0,side*25],pivot=[side*4,23,3])
  for i in range(7):
   x=side*(6+i*1.7)-1.2;y=24+math.sin(i*math.pi/8)*3;length=12-i*.5
   boss.cube(b[n],[x,y-length,2.8],[2.7,length,1],boss.BLACK,rotation=[0,0,side*(10+i*2)],pivot=[x+1.35,y,2.8])
 for x in [-2.7,1.4]:boss.cube(b['head'],[x,27,-4.7],[1.4,.8,.3],boss.RED)
 boss.cube(b['body'],[-.4,14,-2.5],[.8,8,.3],boss.RED)
 for side in [-1,1]:boss.cube(b['body'],[side*2.5-.3,18,-2.6],[.6,3,.4],boss.RED,rotation=[0,0,side*25])
 save(A/'geo/nemesis/4.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.liebe','texture_width':boss.W,'texture_height':boss.H,'visible_bounds_width':5,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(b.values())}]})
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
 bones=traje();save(A/'geo/armaduras/asta.geo.json',armor.document('asta',bones))
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
 save(A/'animations/armaduras/asta.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':{'capa_touro':{'rotation':{'0':[0,0,0],'2':[3,0,0],'4':[0,0,0]}}}}}})
 save(A/'geo/asta/matadora.geo.json',armor.document('matadora',espada()));save(A/'animations/asta/matadora.animation.json',{'format_version':'1.8.0','animations':{'animation.matadora.idle':{'loop':True,'animation_length':1,'bones':{}}}})
 save(A/'models/item/matadora_demonios.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[0,0,-30],'translation':[0,0,0],'scale':[.38]*3},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.8]*3},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,1,0],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,-15,-12],'translation':[0,0,-1],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,15,12],'translation':[0,0,-1],'scale':[.65]*3},'ground':{'rotation':[0,0,90],'scale':[.5]*3},'fixed':{'rotation':[0,0,-25],'scale':[.4]*3}}})
 forma();liebe();animations();recipes();glyph();print('Authored Asta uniform, sword, awakening, Liebe, 9 gestures and recipes.')
if __name__=='__main__':main()
