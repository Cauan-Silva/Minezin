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
 from boss_reference_shapes import toji as shape
 from skin_boss_models import toji
 return toji(shape())
def aurora():
 from boss_reference_shapes import aurora as shape
 from skin_boss_models import aurora
 return aurora(shape())
def qual():
 from boss_reference_shapes import qual as shape
 from skin_boss_models import qual
 return qual(shape())

def liebe():
 from boss_reference_shapes import liebe as shape
 from skin_boss_models import liebe
 return liebe(shape())

def animations_for(kit,bones):
 duration=[0,.9,1.3,1.5,1.1][kit]
 def track(values):return {'rotation':{str(t):v for t,v in values}}
 idle={'head':track([(0,[0,0,0]),(2,[0,3,0]),(4,[0,0,0])])}
 walk={n:track([(0,[0,0,0]),(.25,[v,0,0]),(.5,[0,0,0]),(.75,[-v,0,0]),(1,[0,0,0])]) for n,v in [('leftArm',-22),('rightArm',22),('leftLeg',25),('rightLeg',-25)]}
 cast={};release={}
 poses={1:([-35,0,-22.5],[-110,-22.5,-22.5],[18,-22.5,0]),2:([-70,0,-45],[-70,0,45],[-8,0,0]),3:([-95,0,-45],[-95,0,45],[-12,0,0]),4:([-110,-22.5,-45],[-110,22.5,45],[22.5,0,0])}
 for n,pose in zip(['rightArm','leftArm','body'],poses[kit]):
  cast[n]=track([(0,[0,0,0]),(round(duration*.65,3),pose),(duration,pose)])
  end=[-15,45,15] if n=='rightArm' and kit==1 else [v*.25 for v in pose]
  release[n]=track([(0,pose),(.1,end),(.25,end),(.4,[0,0,0])])
 for i,n in enumerate(bones):
  if n.startswith(('hair_','skirt_','mantle_','wing_','tail_')) or n=='chain':
   amp=2 if n.startswith(('hair_','skirt_','mantle_')) else 5
   axis=1 if n.startswith(('wing_','tail_')) else 0
   v=[0,0,0];v[axis]=amp*(-1 if i%2 else 1)
   idle[n]=track([(0,[0,0,0]),(2,v),(4,[0,0,0])]);walk[n]=track([(0,v),(.5,[-x for x in v]),(1,v)])
  if n.startswith('lower_arm_'):
   cast[n]=track([(0,[0,0,0]),(duration,[-45,0,0])]);release[n]=track([(0,[-45,0,0]),(.1,[-85,0,0]),(.25,[-85,0,0]),(.4,[0,0,0])])
 return {'idle':{'loop':True,'animation_length':4,'bones':idle},'walk':{'loop':True,'animation_length':1,'bones':walk},'cast':{'loop':False,'animation_length':duration,'bones':cast},'release':{'loop':False,'animation_length':.4,'bones':release}}

def main():
 for i,fn in enumerate([toji,aurora,qual,liebe],1):
  b=fn();save(A/f'animations/nemesis/{i}.animation.json',{'format_version':'1.8.0','animations':animations_for(i,b)});save(A/f'geo/nemesis/{i}.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':f'geometry.nemesis_{i}','texture_width':64,'texture_height':64,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(b.values())}]})
 animations={'idle':{'loop':True,'animation_length':2,'bones':{'body':{'rotation':['math.sin(query.anim_time*180)*1.2',0,0]},'head':{'rotation':[0,'math.sin(query.anim_time*90)*4',0]}}},
 'walk':{'loop':True,'animation_length':1,'bones':{n:{'rotation':[f'math.sin(query.anim_time*360)*{a}',0,0]} for n,a in [('leftArm',-22),('rightArm',22),('leftLeg',25),('rightLeg',-25)]}},
 'cast':{'loop':True,'animation_length':1.5,'bones':{'leftArm':{'rotation':[-75,0,-18]},'rightArm':{'rotation':[-105,0,12]},'head':{'rotation':[-8,0,0]},'body':{'rotation':[0,0,'math.sin(query.anim_time*240)*2']}}}}
 save(A/'animations/nemesis.animation.json',{'format_version':'1.8.0','animations':animations})
 print('Authored boss geometry:',[(i,len(fn()),sum(len(b.get('cubes',[])) for b in fn().values())) for i,fn in enumerate([toji,aurora,qual,liebe],1)])
if __name__=='__main__':main()
