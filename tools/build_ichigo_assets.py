"""Original cuboid Zangetsu, shihakusho and 64x64 Minecraft-UV White Ichigo. No borrowed art."""
from pathlib import Path
import copy,json
from PIL import Image,ImageDraw
import build_armor_models as a
from cinematic_animations import gesture
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol';D=ROOT/'src/main/resources/data/arcanecontrol'

def sword(bankai=False):
 b={'root':a.bone('root',[0,0,0]),'cloth':a.bone('cloth',[0,-4.6,0],'root'),'chain':a.bone('chain',[0,-4.6,0],'root')};r=b['root'];c=a.cube
 c(r,[-.55,-4.6,-.55],[1.1,9.2,1.1],a.INK)
 if bankai:
  for y in [-4,-2,0,2,4]:c(r,[-.59,y,-.59],[1.18,.35,1.18],a.INK)
 else:
  # Shikai grip (user's reference): dark wrap laced with white diamonds.
  for y in [-3.9,-2.3,-.7,.9,2.5]:
   for side in [-1,1]:c(r,[side*.6-.07,y,-.32],[.14,1.1,.64],a.IVORY,[side*.6,y+.55,0],[0,45,0])
 if bankai:
  # Tensa Zangetsu (user's reference): square black guard with notched corners, black wrap with red diamonds, a
  # gently curved all-black blade (segments rotated progressively toward the edge side) and a short chain at the pommel.
  c(r,[-2.3,4.6,-2.3],[4.6,.6,4.6],a.INK)
  for x,z in [(-2.3,-2.3),(1.7,-2.3),(-2.3,1.7),(1.7,1.7)]:c(r,[x,4.55,z],[.6,.7,.6],a.SHADOW)
  c(r,[-.75,5.2,-.75],[1.5,.9,1.5],a.INK)
  for y in [-3.6,-1.9,-.2,1.5,3.2]:
   for side in [-1,1]:c(r,[side*.62-.08,y,-.28],[.16,.9,.56],a.RUBY,[side*.62,y+.45,0],[0,45,0])
  segs=8;ang=0;y0=6.1;x0=0
  for k in range(segs):
   L=4.1;b[f'lamina_{k}']=a.bone(f'lamina_{k}',[x0,y0,0],'root' if k==0 else f'lamina_{k-1}',[0,0,-2.2 if k else 0])
   w=1.35-.06*k;c(b[f'lamina_{k}'],[x0-w/2,y0,-.32],[w,L+.05,.64],a.INK)
   c(b[f'lamina_{k}'],[x0+w/2-.16,y0,-.36],[.16,L+.05,.72],a.SHADOW)
   y0+=L
  c(b[f'lamina_{segs-1}'],[-.45,y0,-.3],[.9,1.2,.6],a.INK,[0,y0,0],[0,0,-28])
  # Chain as a chain of 4 bones (2 links each) so TecidoEspadaIchigo can swing it like cloth.
  pai='root'
  for k in range(4):
   y=-4.6-k*1.7;nome='chain' if k==0 else f'chain_{k}';b[nome]=a.bone(nome,[0,y,0],pai)
   for j in range(2):yy=y-.8-j*.85;c(b[nome],[-.3+(.25 if j else 0),yy,-.15],[.6,.75,.3],a.STEEL,[0,yy,0],[0,90 if j else 0,0])
   pai=nome
 else:
  # Shikai Zangetsu (user's reference): long, slender cleaver. Straight dark spine on -x, the edge bellies out and runs
  # into a long sharp point; the blade fades from black at the spine to white at the edge (ink, steel, white bands
  # laid along the curve as thin rotated cubes, so the edge reads smooth). Long white cloth ribbon from the pommel.
  import math
  larg=[3.6,4.4,4.9,5.1,5.1,4.9,4.6,4.1,3.5,2.8,2.0,1.2,.5,0.0]
  pts=[(-2.6+w,4.6+2.6*i) for i,w in enumerate(larg)]
  for i,(w,(x,y)) in enumerate(zip(larg[:-1],pts[:-1])):
   esp=.9-.045*i;ww=max(.3,min(w,larg[i+1])-.2)
   c(r,[-2.6,y,-esp/2],[ww,2.6,esp],a.INK)
  for i in range(len(pts)-1):
   (x0,y0),(x1,y1)=pts[i],pts[i+1];L=math.hypot(x1-x0,y1-y0);ang=math.degrees(math.atan2(x1-x0,y1-y0));esp=.86-.045*i
   fundo=max(.05,min(2.8,min(x0,x1)+2.6))      # never past the spine (x=-2.6)
   for k,(f,tile) in enumerate([(1.0,a.INK),(.8,a.STEEL),(.55,a.WHITE)]):
    d=fundo*f;o=.04*k;c(r,[x0-d,y0,-esp/2-o],[d,L+.15,esp+2*o],tile,[x0,y0,0],[0,0,ang])
  # Small notch at the base of the spine.
  c(r,[-2.6,5.6,-.45],[.9,1.0,.9],a.INK)
  # Ribbon: a chain of bones, each bent a little more, so the cloth reads as one flowing strip.
  pai='root'
  for k in range(4):
   y=-4.6-k*3.4;nome='cloth' if k==0 else f'cloth_{k}'
   b[nome]=a.bone(nome,[0,y,0],pai,[0,0,10 if k else 6]);c(b[nome],[-.5,y-3.5,-.1],[1.0-k*.1,3.5,.2],a.IVORY);pai=nome
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
 # Getsuga Tensho: overhead wind-up with tension hold, diagonal release on the server's damage tick
 # (Shikai 0.40 s = tick 8 two-handed; Bankai 0.25 s = tick 5 one-handed), overshoot and follow-through.
 for tipo,length,keys in [
  (500,1.0,{'right_arm':[(0,z,'linear'),(.12,[-60,-25,-20],'easeOutQuad'),(.32,[-165,-35,-25],'easeOutQuad'),(.37,[-172,-36,-28],'linear'),(.40,[-40,30,10],'easeInQuad'),(.48,[-18,40,14],'easeOutQuad'),(.62,[-24,36,12],'linear'),(1.0,z,'easeInOutSine')],
             'left_arm':[(0,z,'linear'),(.12,[-45,20,-10],'easeOutQuad'),(.32,[-150,22,-10],'easeOutQuad'),(.37,[-156,22,-12],'linear'),(.40,[-55,-8,-6],'easeInQuad'),(.48,[-32,-18,-10],'easeOutQuad'),(.62,[-34,-16,-10],'linear'),(1.0,z,'easeInOutSine')],
             'torso':[(0,z,'linear'),(.32,[-8,-28,0],'easeOutQuad'),(.37,[-10,-30,0],'linear'),(.40,[10,22,0],'easeInQuad'),(.48,[14,28,0],'easeOutQuad'),(.62,[12,25,0],'linear'),(1.0,z,'easeInOutSine')],
             'head':[(0,z,'linear'),(.32,[4,14,0],'easeOutQuad'),(.40,[-2,-10,0],'easeInQuad'),(.62,[-2,-12,0],'linear'),(1.0,z,'easeInOutSine')]}),
  (510,.75,{'right_arm':[(0,z,'linear'),(.08,[-90,-30,-20],'easeOutQuad'),(.20,[-162,-40,-25],'easeOutQuad'),(.23,[-168,-41,-27],'linear'),(.25,[-35,35,12],'easeInQuad'),(.31,[-14,46,16],'easeOutQuad'),(.42,[-18,42,14],'linear'),(.75,z,'easeInOutSine')],
            'left_arm':[(0,z,'linear'),(.20,[25,-12,-28],'easeOutQuad'),(.25,[-30,12,-14],'easeInQuad'),(.31,[-40,18,-18],'easeOutQuad'),(.42,[-36,16,-16],'linear'),(.75,z,'easeInOutSine')],
            'torso':[(0,z,'linear'),(.20,[-10,-32,0],'easeOutQuad'),(.25,[12,26,0],'easeInQuad'),(.31,[16,32,0],'easeOutQuad'),(.42,[14,29,0],'linear'),(.75,z,'easeInOutSine')],
            'head':[(0,z,'linear'),(.20,[4,16,0],'easeOutQuad'),(.25,[-3,-12,0],'easeInQuad'),(.42,[-3,-14,0],'linear'),(.75,z,'easeInOutSine')]}),
  # G Bankai release (EntradaBankai): Zangetsu thrust forward with the left hand gripping the right wrist, the shout on
  # tick 14 (0.7 s), then a horizontal cut on tick 30 (1.5 s) that parts the dust; follow-through and rest by 2.2 s.
  (200,2.2,{'right_arm':[(0,z,'linear'),(.4,[-90,0,0],'easeOutQuad'),(.7,[-94,0,0],'linear'),(1.3,[-92,0,0],'linear'),(1.4,[-90,50,0],'easeOutQuad'),(1.5,[-90,-70,0],'easeInQuad'),(1.62,[-84,-82,0],'easeOutQuad'),(1.85,[-80,-78,0],'linear'),(2.2,z,'easeInOutSine')],
            'left_arm':[(0,z,'linear'),(.4,[-90,55,0],'easeOutQuad'),(1.3,[-92,55,0],'linear'),(1.5,[-30,-20,-25],'easeInQuad'),(1.85,[-25,-15,-22],'linear'),(2.2,z,'easeInOutSine')],
            'torso':[(0,z,'linear'),(.4,[0,-10,0],'easeOutQuad'),(.7,[-8,-10,0],'easeOutQuad'),(1.3,[-4,-8,0],'linear'),(1.4,[0,30,0],'easeOutQuad'),(1.5,[4,-30,0],'easeInQuad'),(1.62,[6,-36,0],'easeOutQuad'),(1.85,[5,-32,0],'linear'),(2.2,z,'easeInOutSine')],
            'head':[(0,z,'linear'),(.4,[0,10,0],'easeOutQuad'),(.7,[-15,10,0],'easeOutQuad'),(1.3,[-6,8,0],'linear'),(1.5,[0,25,0],'easeInQuad'),(1.85,[0,28,0],'linear'),(2.2,z,'easeInOutSine')]}),
  # C Mugetsu (MugetsuIchigo): hunched implosion until the cocoon bursts (tick 20), upright, right arm straight up while
  # the darkness blade grows (ticks 30-50), downward cut on tick 50 (2.5 s), held follow-through, rest by 4 s.
  (530,4.0,{'right_arm':[(0,z,'linear'),(.25,[-20,0,30],'easeOutQuad'),(.95,[-22,0,32],'linear'),(1.1,[-5,0,5],'easeOutQuad'),(1.5,[-178,0,4],'easeOutQuad'),(2.45,[-182,0,4],'linear'),(2.5,[-110,0,4],'easeInQuad'),(2.65,[-55,0,4],'easeOutQuad'),(3.5,[-58,0,4],'linear'),(4.0,z,'easeInOutSine')],
            'left_arm':[(0,z,'linear'),(.25,[-20,0,-30],'easeOutQuad'),(.95,[-22,0,-32],'linear'),(1.1,[-5,0,-5],'easeOutQuad'),(1.5,[-10,0,-12],'easeOutQuad'),(2.65,[15,0,-20],'easeOutQuad'),(3.5,[12,0,-18],'linear'),(4.0,z,'easeInOutSine')],
            'torso':[(0,z,'linear'),(.25,[14,0,0],'easeOutQuad'),(.95,[16,0,0],'linear'),(1.1,[-4,0,0],'easeOutQuad'),(1.5,[-6,0,0],'easeOutQuad'),(2.45,[-8,0,0],'linear'),(2.65,[18,0,0],'easeOutQuad'),(3.5,[15,0,0],'linear'),(4.0,z,'easeInOutSine')],
            'head':[(0,z,'linear'),(.25,[25,0,0],'easeOutQuad'),(.95,[25,0,0],'linear'),(1.1,[0,0,0],'easeOutQuad'),(1.5,[-12,0,0],'easeOutQuad'),(2.45,[-14,0,0],'linear'),(2.65,[8,0,0],'easeOutQuad'),(3.5,[6,0,0],'linear'),(4.0,z,'easeInOutSine')]}),
  # X Shunpo: crouched lean with the cleaver trailing behind, snapping upright on arrival.
  (501,.8,{'right_arm':[(0,z,'linear'),(.05,[35,-15,25],'easeOutQuad'),(.4,[38,-15,25],'linear'),(.55,[-20,-10,10],'easeOutQuad'),(.8,z,'easeInOutSine')],
           'left_arm':[(0,z,'linear'),(.05,[40,10,-25],'easeOutQuad'),(.4,[40,10,-25],'linear'),(.55,[-10,5,-10],'easeOutQuad'),(.8,z,'easeInOutSine')],
           'torso':[(0,z,'linear'),(.05,[22,0,0],'easeOutQuad'),(.4,[22,0,0],'linear'),(.55,[-4,0,0],'easeOutQuad'),(.8,z,'easeInOutSine')],
           'head':[(0,z,'linear'),(.05,[-18,0,0],'easeOutQuad'),(.4,[-18,0,0],'linear'),(.8,z,'easeInOutSine')]}),
  # C heavy slam (both hands together on the grip; angles solved so the fists meet): both hands lift the cleaver far overhead, hang, then smash straight down on tick 12 (0.6 s).
  (502,1.2,{'right_arm':[(0,z,'linear'),(.3,[-165,40,40],'easeOutQuad'),(.52,[-178,40,40],'easeOutQuad'),(.6,[-40,0,-38],'easeInQuad'),(.66,[-26,0,-38],'easeOutQuad'),(.85,[-28,0,-38],'linear'),(1.2,z,'easeInOutSine')],
            'left_arm':[(0,z,'linear'),(.3,[-165,-40,-40],'easeOutQuad'),(.52,[-178,-40,-40],'easeOutQuad'),(.6,[-40,0,38],'easeInQuad'),(.66,[-26,0,38],'easeOutQuad'),(.85,[-28,0,38],'linear'),(1.2,z,'easeInOutSine')],
            'torso':[(0,z,'linear'),(.52,[-14,0,0],'easeOutQuad'),(.6,[24,0,0],'easeInQuad'),(.66,[30,0,0],'easeOutQuad'),(.85,[26,0,0],'linear'),(1.2,z,'easeInOutSine')],
            'head':[(0,z,'linear'),(.52,[10,0,0],'easeOutQuad'),(.6,[-14,0,0],'easeInQuad'),(.85,[-14,0,0],'linear'),(1.2,z,'easeInOutSine')]}),
  # V rising slash: low guard with the blade down-back, lunge, then sweep up through the target on tick 10 (0.5 s).
  (503,1.1,{'right_arm':[(0,z,'linear'),(.3,[30,-25,30],'easeOutQuad'),(.45,[35,-25,32],'linear'),(.5,[-120,15,-10],'easeInQuad'),(.58,[-165,20,-15],'easeOutQuad'),(.75,[-160,18,-14],'linear'),(1.1,z,'easeInOutSine')],
            'left_arm':[(0,z,'linear'),(.3,[-35,25,-20],'easeOutQuad'),(.5,[20,-10,-30],'easeInQuad'),(.75,[15,-10,-28],'linear'),(1.1,z,'easeInOutSine')],
            'torso':[(0,z,'linear'),(.3,[18,-20,0],'easeOutQuad'),(.45,[20,-22,0],'linear'),(.5,[-14,18,0],'easeInQuad'),(.58,[-18,22,0],'easeOutQuad'),(.75,[-16,20,0],'linear'),(1.1,z,'easeInOutSine')],
            'head':[(0,z,'linear'),(.3,[-10,15,0],'easeOutQuad'),(.5,[-6,-10,0],'easeInQuad'),(.75,[-8,-12,0],'linear'),(1.1,z,'easeInOutSine')]})]:
  out[f'desperto_6_{tipo}']=gesture(length,keys)
 a.save(A/'player_animation/ichigo.json',{'format_version':'1.8.0','geckolib_format_version':2,'animations':out})

def main():
 for name,bankai in [('zangetsu',False),('tensa_zangetsu',True)]:a.save(A/f'geo/ichigo/{name}.geo.json',a.document(name,sword(bankai)))
 a.save(A/'animations/ichigo/zangetsu.animation.json',{'format_version':'1.8.0','animations':{'idle':{'loop':True,'animation_length':2,'bones':{}}}})
 a.save(A/'models/item/zangetsu.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[0,0,-35],'scale':[.35]*3},'thirdperson_righthand':{'rotation':[0,-90,10],'scale':[.8]*3},'thirdperson_lefthand':{'rotation':[0,90,-10],'scale':[.8]*3},'firstperson_righthand':{'rotation':[0,-45,25],'scale':[.65]*3},'firstperson_lefthand':{'rotation':[0,45,-25],'scale':[.65]*3},'ground':{'scale':[.4]*3}}})
 # Outfits (shihakusho armor set and Bankai coat) live in build_ichigo_traje.py.
 import build_ichigo_traje;build_ichigo_traje.main()
 a.save(D/'recipes/zangetsu.json',{'type':'minecraft:crafting_shaped','pattern':[' II',' SI','S  '],'key':{'I':{'item':'minecraft:iron_block'},'S':{'item':'minecraft:stick'}},'result':{'item':'arcanecontrol:zangetsu'}})
 folder=A/'textures/ichigo';folder.mkdir(parents=True,exist_ok=True);im=Image.new('RGBA',(64,64),'#EEE9DF');d=ImageDraw.Draw(im);d.rectangle((0,16,17,31),fill='#161019');d.rectangle((18,16,40,31),fill='#BC2943');im.save(folder/'mascara.png')
 for lang,names in [('pt_br',['Zangetsu','Faixa Shihakusho','Shihakusho','Hakama','Sandálias espirituais']),('en_us',['Zangetsu','Shihakusho Headband','Shihakusho','Hakama','Spiritual Sandals'])]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for id,name in zip(['zangetsu','ichigo_cabeca','ichigo_peito','ichigo_pernas','ichigo_botas'],names):data['item.arcanecontrol.'+id]=name
  a.save(p,data)
 boss();animations();print('Ichigo assets authored: two weapons, uniform, mask, original boss and nine gestures.')
if __name__=='__main__':main()
