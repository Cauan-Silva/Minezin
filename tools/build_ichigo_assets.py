"""Original cuboid Zangetsu, shihakusho and 64x64 Minecraft-UV White Ichigo. No borrowed art."""
from pathlib import Path
import copy,json
from PIL import Image,ImageDraw
import build_armor_models as a
from cinematic_animations import gesture
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol';D=ROOT/'src/main/resources/data/arcanecontrol'

def sword(bankai=False):
 b={'root':a.bone('root',[0,0,0]),'cloth':a.bone('cloth',[0,-4.6,0],'root'),'chain':a.bone('chain',[0,-4.6,0],'root')};r=b['root'];c=a.cube
 c(r,[-.55,-4.6,-.55],[1.1,9.2,1.1],a.INK if bankai else a.IVORY)
 for y in [-4,-2,0,2,4]:c(r,[-.59,y,-.59],[1.18,.35,1.18],a.RUBY if bankai else a.LEATHER)
 if bankai:
  c(r,[-2.4,4.6,-.5],[4.8,.7,1],a.INK)
  for x in [-2.4,1.7]:c(r,[x,3.5,-.5],[.7,2.8,1],a.INK)
  c(r,[-.7,5.3,-.35],[1.4,33,.7],a.INK);c(r,[.55,5.4,-.4],[.18,32,.8],a.STEEL)
  c(r,[-.5,38.3,-.3],[1,2,.6],a.INK)
  b['chain']=a.bone('chain',[0,-4.6,0],'root')
  for i in range(5):c(b['chain'],[-.35,-5.3-i*.9,-.15],[.7,.8,.3],a.STEEL)
 else:
  c(r,[-3.4,4.6,-.6],[6.8,23,1.2],a.INK)
  c(r,[2.6,4.7,-.66],[.8,23,1.32],a.STEEL)
  for i in range(5):c(r,[-3.4+i*.6,27.6+i*1.1,-.55],[6.2-i*.9,1.1,1.1],a.INK)
  b['cloth']=a.bone('cloth',[0,-4.6,0],'root')
  c(b['cloth'],[-.6,-12,-.12],[1.2,7.4,.24],a.IVORY)
 return b

def uniform():
 b=a.skeleton();c=a.cube
 c(b['armorHead'],[-4.2,28,-4.2],[8.4,.7,8.4],a.INK)
 c(b['armorBody'],[-4.15,12,-2.15],[8.3,12.2,4.3],a.INK)
 # Crossed white collar and belt, broad clean color areas.
 for x,ang in [(-1.5,-20),(1.0,20)]:c(b['armorBody'],[x,18,-2.35],[.65,5,.25],a.IVORY,[x,21,-2.35],[0,0,ang])
 c(b['armorBody'],[-3.8,13.6,-2.24],[.35,5,.12],a.NAVY)
 c(b['armorBody'],[3.45,13.6,-2.24],[.35,5,.12],a.NAVY)
 c(b['armorBody'],[-4.25,12,-2.3],[8.5,1.4,4.6],a.IVORY)
 c(b['armorBody'],[-.9,10.8,-2.6],[1.8,2.3,.6],a.IVORY)
 for n,x in [('armorRightArm',-8.4),('armorLeftArm',3.75)]:
  c(b[n],[x,14,-2.3],[4.65,10.4,4.6],a.INK)
  c(b[n],[x,14,-2.33],[4.65,.6,4.66],a.IVORY)
 for n,x in [('armorRightLeg',-4.2),('armorLeftLeg',-.05)]:
  c(b[n],[x,1,-2.2],[4.25,11,4.4],a.INK)
  c(b[n],[x+.4,1,-2.27],[.38,10,.14],a.NAVY)
  c(b[n],[x+3.4,1,-2.27],[.38,10,.14],a.NAVY)
  c(b[n],[x,5,-2.25],[4.25,1,.1],a.NAVY)
 for n,x in [('armorRightBoot',-4.2),('armorLeftBoot',-.05)]:
  c(b[n],[x,-.1,-2.55],[4.25,1.4,4.9],a.LEATHER)
  c(b[n],[x,.7,-2.58],[4.25,.7,.35],a.IVORY)
 return b

def boss():
 def bone(n,p,parent='root'):return {'name':n,'pivot':p,'parent':parent,'cubes':[]}
 b={'root':{'name':'root','pivot':[0,0,0]}}
 for n,p in [('body',[0,24,0]),('head',[0,24,0]),('leftArm',[5,22,0]),('rightArm',[-5,22,0]),('leftLeg',[2,12,0]),('rightLeg',[-2,12,0])]:b[n]=bone(n,p)
 for n,o,size,uv in [('head',[-4,24,-4],[8,8,8],[0,0]),('body',[-4,12,-2],[8,12,4],[16,16]),('rightArm',[-8,12,-2],[4,12,4],[40,16]),('leftArm',[4,12,-2],[4,12,4],[32,48]),('rightLeg',[-4,0,-2],[4,12,4],[0,16]),('leftLeg',[0,0,-2],[4,12,4],[16,48])]:b[n]['cubes'].append({'origin':o,'size':size,'uv':uv})
 b['hair']=bone('hair',[0,24,0],'head')
 for x,y in [(-4.3,30.4),(-2,31),(1,30.5)]:b['hair']['cubes'].append({'origin':[x,y,-4.3],'size':[3.1,2,8.6],'uv':[32,0]})
 # Private boss sword, attached to the hand bone; coordinates align to the grip center.
 b['weapon']=bone('weapon',[-6,14,0],'rightArm')
 b['weapon']['cubes']=[{'origin':[-6.5,9,-.5],'size':[1,9,1],'uv':[40,32]}, {'origin':[-9,18,-.6],'size':[6,24,1.2],'uv':[0,32]}, {'origin':[-9,41,-.6],'size':[5,3,1.2],'uv':[0,32]}]
 # Explicit standard Minecraft cube nets, including the supplied 64x64 skin layout.
 for part in b.values():
  for cube in part.get('cubes',[]):
   u,v=cube['uv'];w,h,d=cube['size']
   rects={'west':(u,v+d,d,h),'north':(u+d,v+d,w,h),'east':(u+d+w,v+d,d,h),'south':(u+2*d+w,v+d,w,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
   cube['uv']={face:{'uv':[x,y],'uv_size':[rw,rh]} for face,(x,y,rw,rh) in rects.items()}
 a.save(A/'geo/nemesis/6.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.nemesis_6','texture_width':64,'texture_height':64,'visible_bounds_width':5,'visible_bounds_height':5,'visible_bounds_offset':[0,1.5,0]},'bones':list(b.values())}]})
 im=Image.new('RGBA',(64,64),(230,229,232,255));d=ImageDraw.Draw(im)
 d.rectangle((8,8,15,15),fill='#E4DDE7');d.rectangle((9,11,10,11),fill='#DBA622');d.rectangle((13,11,14,11),fill='#DBA622');d.rectangle((10,14,13,14),fill='#9E8B98')
 d.rectangle((32,0,63,7),fill='#FAF8F7');d.rectangle((20,20,27,31),fill='#EBE6EA');d.line((20,20,24,24,27,20),fill='#292331',width=1);d.rectangle((16,29,39,30),fill='#352C3C')
 d.rectangle((0,32,31,47),fill='#E6E2DF');d.rectangle((1,32,4,47),fill='#4C4354');d.rectangle((40,32,47,47),fill='#2A2430')
 # Readable Minecraft-size cloth shading, separate front/back/side regions.
 for x,y,w,h in [(20,20,8,12),(32,20,8,12),(44,20,4,12),(36,52,4,12),(4,20,4,12),(20,52,4,12)]:
  d.rectangle((x,y,x+w-1,y+h-1),fill='#E4E0E5');d.line((x,y,x,y+h-1),fill='#AAA2AF');d.line((x+w-1,y,x+w-1,y+h-1),fill='#F8F5F3')
  d.line((x+1,y+4,x+1,y+8),fill='#C7C0CB')
 d.line((20,20,23,24,27,20),fill='#2C2435',width=1);d.rectangle((20,29,27,30),fill='#3A2F42');d.rectangle((32,29,39,30),fill='#3A2F42')
 for x,y in [(4,20),(20,52)]:d.rectangle((x,y+10,x+3,y+11),fill='#49404D')
 d.rectangle((8,8,15,15),fill='#E4DDE7');d.line((8,9,15,9),fill='#F8F7F7');d.rectangle((9,11,10,11),fill='#DCAA31');d.rectangle((13,11,14,11),fill='#DCAA31');d.point((8,11),fill='#3F3549');d.point((15,11),fill='#3F3549');d.line((11,14,13,14),fill='#887A8A')
 im.save(A/'textures/bosses/zangetsu_skin.png')
 template=json.loads((A/'animations/nemesis/1.animation.json').read_text(encoding='utf-8'));a.save(A/'animations/nemesis/6.animation.json',template)

def animations():
 out={};z=[0,0,0]
 specs=[(200,1.2,.5,.6,[-25,-10,-18],[-65,20,-20]),(500,1,.3,.4,[-145,-30,-12],[0,10,-10]),(501,.8,0,.1,[-50,-20,10],[-50,20,-10]),(502,1.2,.5,.6,[-70,-35,35],[-70,35,-35]),(503,1.1,.35,.5,[25,-40,-20],[-35,10,-15]),(510,.65,0,.05,[-100,-50,-20],[-30,10,-10]),(511,.6,0,.1,[-40,-20,10],[-40,20,-10]),(512,1.1,.4,.5,[-150,-40,-20],[-80,20,-20]),(513,1.25,.5,.6,[-45,-10,-10],[-125,15,-18])]
 for tipo,length,prep,hit,right,left in specs:
  poses={}
  for n,wind in [('right_arm',right),('left_arm',left),('torso',[-6,-20,0]),('head',[6,10,0])]:
   release=[wind[0]*.3,-wind[1]*.7,wind[2]*.7];frames=[(0,z,'easeOutQuad')]
   if prep:frames.append((prep,wind,'easeInQuad'))
   frames.extend([(hit,release,'linear'),(hit+.1,release,'linear'),(length,z,'easeOutQuad')]);poses[n]=frames
  out[f'desperto_6_{tipo}']=gesture(length,poses)
 a.save(A/'player_animation/ichigo.json',{'format_version':'1.8.0','geckolib_format_version':2,'animations':out})

def main():
 for name,bankai in [('zangetsu',False),('tensa_zangetsu',True)]:a.save(A/f'geo/ichigo/{name}.geo.json',a.document(name,sword(bankai)))
 a.save(A/'animations/ichigo/zangetsu.animation.json',{'format_version':'1.8.0','animations':{'idle':{'loop':True,'animation_length':2,'bones':{'cloth':{'rotation':{'0':[0,0,-5],'1':[0,0,5],'2':[0,0,-5]}}}}}})
 a.save(A/'models/item/zangetsu.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[0,0,-35],'scale':[.35]*3},'thirdperson_righthand':{'rotation':[0,-90,10],'scale':[.8]*3},'thirdperson_lefthand':{'rotation':[0,90,-10],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,-45,25],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,45,-25],'scale':[.65]*3},'ground':{'scale':[.4]*3}}})
 b=uniform();a.save(A/'geo/armaduras/ichigo.geo.json',a.document('ichigo',b))
 (A/'textures/armor/trajes/ichigo.png').write_bytes((A/'textures/armor/anime_reference_atlas.png').read_bytes())
 for part,roots in {'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}.items():
  icon=copy.deepcopy(b)
  for n,v in icon.items():
   if n not in roots:v['cubes']=[]
  cubes=[c for v in icon.values() for c in v['cubes']];mid=(min(c['origin'][1] for c in cubes)+max(c['origin'][1]+c['size'][1] for c in cubes))/2
  for v in icon.values():
   v['pivot'][1]-=mid
   for c in v['cubes']:
    c['origin'][1]-=mid
    if 'pivot'in c:c['pivot'][1]-=mid
  a.save(A/f'geo/armaduras/ichigo_{part}.geo.json',a.document('ichigo_'+part,icon))
  suffix={'helmet':'cabeca','chestplate':'peito','leggings':'pernas','boots':'botas'}[part]
  a.save(A/f'models/item/ichigo_{suffix}.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[8,-28,0],'scale':[.8]*3},'ground':{'scale':[.5]*3}}})
  a.save(D/f'recipes/ichigo_{suffix}.json',{'type':'minecraft:smithing_transform','template':{'item':'minecraft:paper'},'base':{'item':'minecraft:netherite_'+part},'addition':{'item':'minecraft:lapis_lazuli'},'result':{'item':'arcanecontrol:ichigo_'+suffix}})
 a.save(A/'animations/armaduras/ichigo.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':2,'bones':{}}}})
 a.save(D/'recipes/zangetsu.json',{'type':'minecraft:crafting_shaped','pattern':[' II',' SI','S  '],'key':{'I':{'item':'minecraft:iron_block'},'S':{'item':'minecraft:stick'}},'result':{'item':'arcanecontrol:zangetsu'}})
 folder=A/'textures/ichigo';folder.mkdir(parents=True,exist_ok=True);im=Image.new('RGBA',(64,64),'#EEE9DF');d=ImageDraw.Draw(im);d.rectangle((0,16,11,31),fill='#161019');d.rectangle((12,16,31,31),fill='#BC2943');im.save(folder/'mascara.png')
 im=Image.new('RGBA',(64,64),'#15121C');d=ImageDraw.Draw(im);d.rectangle((0,40,63,63),fill='#E5DED6');d.rectangle((5,5,6,16),fill='#302A3A');im.save(folder/'bankai_coat.png')
 for lang,names in [('pt_br',['Zangetsu','Faixa Shihakusho','Shihakusho','Hakama','Sandálias espirituais']),('en_us',['Zangetsu','Shihakusho Headband','Shihakusho','Hakama','Spiritual Sandals'])]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for id,name in zip(['zangetsu','ichigo_cabeca','ichigo_peito','ichigo_pernas','ichigo_botas'],names):data['item.arcanecontrol.'+id]=name
  a.save(p,data)
 boss();animations();print('Ichigo assets authored: two weapons, uniform, mask, original boss and nine gestures.')
if __name__=='__main__':main()
