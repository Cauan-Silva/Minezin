"""Original articulated boss geometry; reuses the existing smooth palette, no raster edits."""
from pathlib import Path
import json,struct
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/arcanecontrol'
W,H=struct.unpack('>II',(A/'textures/armor/anime_atlas.png').read_bytes()[16:24])
BLACK,NAVY,IVORY,PLUM,WHITE,GOLD,SILVER,SKIN,CYAN,VIOLET,RED=range(11)
def save(path,obj):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes((json.dumps(obj,ensure_ascii=False,separators=(',',':'))+'\n').encode('utf-8'))
def bone(name,pivot,parent='root'):
 return {'name':name,'pivot':pivot,'parent':parent,'cubes':[]}
def cube(b,o,s,t,rotation=None,pivot=None):
 uv={f:{'uv':[(t%4+.2)*W/4,(t//4+.2)*H/4],'uv_size':[W*.15,H*.15]} for f in ['north','south','east','west','up','down']}
 c={'origin':o,'size':s,'uv':uv}
 if rotation:c.update(rotation=rotation,pivot=pivot or o)
 b['cubes'].append(c)
def base():
 b={'root':{'name':'root','pivot':[0,0,0]}}
 for n,p in [('body',[0,24,0]),('head',[0,24,0]),('leftArm',[5,22,0]),('rightArm',[-5,22,0]),('leftLeg',[2,12,0]),('rightLeg',[-2,12,0])]:b[n]=bone(n,p)
 return b
def human(b,shirt,pants,hair,skin=SKIN):
 cube(b['body'],[-4.1,12,-2.2],[8.2,12,4.4],shirt)
 cube(b['head'],[-4,24,-4],[8,8,8],skin)
 cube(b['head'],[-4.3,29.6,-4.3],[8.6,2.8,8.6],hair)
 for x in [-4.1,2.5]:cube(b['head'],[x,26.7,-4.35],[1.7,3,1],hair,rotation=[0,0,10 if x<0 else -10])
 for x in [-2.6,1.5]:
  cube(b['head'],[x,27.1,-4.4],[1.25,.7,.2],WHITE)
  cube(b['head'],[x+.35,27.1,-4.55],[.5,.65,.1],PLUM)
 for n,x in [('leftArm',4),('rightArm',-8)]:
  cube(b[n],[x,14,-2],[4,10,4],shirt);cube(b[n],[x,12,-2],[4,2,4],skin)
 for n,x in [('leftLeg',0),('rightLeg',-4)]:
  cube(b[n],[x,1,-2],[4,11,4],pants);cube(b[n],[x-.1,0,-2.5],[4.2,2.2,4.7],BLACK)
def toji():
 b=base();human(b,BLACK,IVORY,BLACK)
 cube(b['body'],[-4.2,13,-2.4],[8.4,1.2,4.8],NAVY)
 for x in [-3.7,.4]:cube(b['body'],[x,20,-2.4],[3.3,2.5,.3],BLACK)
 cube(b['head'],[2.8,25.4,-4.2],[.7,.17,.15],RED,rotation=[0,0,-32])
 b['weapon']=bone('weapon',[-6,15,0],'rightArm')
 cube(b['weapon'],[-6.3,2,-.3],[.6,18,.6],GOLD)
 cube(b['weapon'],[-6.6,18,-.5],[1.2,9,1],SILVER)
 for x in [-8,-5.3]:cube(b['weapon'],[x,20,-.4],[.55,5,.8],SILVER)
 # Inventory curse wrapped around the torso, original cuboid interpretation.
 b['curse']=bone('curse',[0,18,0],'body')
 for i in range(7):
  cube(b['curse'],[-5+i*1.35,15+i*.8,2.2+i*.06],[2.8,2.8,2.8],PLUM)
 cube(b['curse'],[-5.4,19.5,-1.4],[3,3,4],PLUM);cube(b['curse'],[-5,20.5,-1.65],[.55,.55,.3],RED)
 return b
def aurora():
 b=base();human(b,PLUM,PLUM,PLUM)
 b['hair']=bone('hair',[0,30,3],'head')
 for x in [-4,-1.2,1.6]:cube(b['hair'],[x,13,3.2],[2.5,18,2],PLUM,rotation=[-4,0,0],pivot=[x,30,3.2])
 for side in [-1,1]:
  n='skirt'+str(side);b[n]=bone(n,[side*2,14,0],'body');cube(b[n],[side*3-3,1,-3.1],[6,13,6.2],PLUM,rotation=[0,0,side*7],pivot=[side*2,14,0]);cube(b[n],[side*3-3,.8,-3.3],[6,.7,6.6],VIOLET)
  cube(b['body'],[side*3.8-.4,19,-2.6],[.8,4,.45],VIOLET)
  cube(b['head'],[side*4.1-.2,24.6,-.7],[.4,1.6,.6],RED)
 for n,x in [('leftArm',4),('rightArm',-8)]:cube(b[n],[x-.3,13.8,-2.3],[4.6,1,4.6],GOLD)
 cube(b['body'],[-.7,19,-2.7],[1.4,1.8,.55],RED);cube(b['body'],[-3.9,12,-2.4],[7.8,1,4.8],GOLD)
 for x in [-3.5,1.5]:cube(b['head'],[x,27.1,-4.4],[1.3,.65,.1],VIOLET)
 return b
def qual():
 b=base();human(b,NAVY,PLUM,SILVER,SILVER)
 cube(b['head'],[-5,25,-4.6],[10,8,9.2],SILVER)
 cube(b['head'],[-2,23.3,-5],[4,4,3],SILVER)
 for x in [-3.4,1.5]:
  cube(b['head'],[x,28,-4.9],[2,.75,.4],BLACK);cube(b['head'],[x+.6,28,-5.2],[.6,.7,.2],RED)
 for side in [-1,1]:
  n='horn'+str(side);b[n]=bone(n,[side*4,31,0],'head')
  for i in range(3):cube(b[n],[side*(4+i*1.1)-1,31+i*2,-.8],[2-i*.4,3,1.8-i*.3],IVORY,rotation=[0,0,-side*(25+i*15)])
  n='robe'+str(side);b[n]=bone(n,[side*3,18,0],'body');cube(b[n],[side*3-3,0,-3.2],[6,17,6.4],NAVY,rotation=[0,0,side*6],pivot=[side*3,18,0]);cube(b[n],[side*3-3,1,-3.5],[6,1,7],GOLD)
  cube(b['body'],[side*4-.5,17,-2.8],[1,7,.6],GOLD)
 cube(b['body'],[-1,14,-2.7],[2,9,.7],PLUM)
 b['staff']=bone('staff',[-6,16,0],'rightArm');cube(b['staff'],[-6.35,0,-.4],[.7,31,.8],GOLD)
 for i in range(6):cube(b['staff'],[-7.5+i*.5,29+abs(2.5-i)*.8,-.8],[.65,3,1.6],SILVER)
 cube(b['staff'],[-6.7,31,-.9],[1.5,2,1.8],RED)
 return b
def main():
 for i,fn in enumerate([toji,aurora,qual],1):
  b=fn();save(A/f'geo/nemesis/{i}.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':f'geometry.nemesis_{i}','texture_width':W,'texture_height':H,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(b.values())}]})
 animations={'idle':{'loop':True,'animation_length':2,'bones':{'body':{'rotation':['math.sin(query.anim_time*180)*1.2',0,0]},'head':{'rotation':[0,'math.sin(query.anim_time*90)*4',0]}}},
 'walk':{'loop':True,'animation_length':1,'bones':{n:{'rotation':[f'math.sin(query.anim_time*360)*{a}',0,0]} for n,a in [('leftArm',-22),('rightArm',22),('leftLeg',25),('rightLeg',-25)]}},
 'cast':{'loop':True,'animation_length':1.5,'bones':{'leftArm':{'rotation':[-75,0,-18]},'rightArm':{'rotation':[-105,0,12]},'head':{'rotation':[-8,0,0]},'body':{'rotation':[0,0,'math.sin(query.anim_time*240)*2']}}}}
 save(A/'animations/nemesis.animation.json',{'format_version':'1.8.0','animations':animations})
 print('Authored boss geometry:',[(i,len(fn()),sum(len(b.get('cubes',[])) for b in fn().values())) for i,fn in enumerate([toji,aurora,qual],1)])
if __name__=='__main__':main()
