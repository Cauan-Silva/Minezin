"""Authored, reproducible gestures. Values are degrees; times derive from server ticks."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/arcanecontrol/player_animation'

def channel(frames):
    return {'rotation':{str(t):{'vector':v,'easing':ease} for t,v,ease in frames}}

def gesture(length,poses):
    return {'animation_length':length,'loop':False,'bones':{name:channel(frames) for name,frames in poses.items()}}

def refine(animations,kit):
    z=[0,0,0]
    if kit=='gojo':
        a=animations['gojo_roxo']
        a['bones']['right_arm']['rotation']['1.50']['vector']=[-95,-8,8]
        a['bones']['right_arm']['rotation']['1.60']={'vector':[-95,-8,8],'easing':'linear'}
        a['bones']['left_arm']['rotation']['1.35']['vector']=[-85,30,-25]
        a['bones']['left_arm']['rotation']['1.50']['vector']=[-85,25,-20]
        a['bones']['left_arm']['rotation']['1.60']={'vector':[-85,25,-20],'easing':'linear'}
    if kit=='cid':
        animations['cid_4']=gesture(5,{
            'right_arm':[(0,z,'easeInOutSine'),(.65,[-8,-12,6],'easeOutQuad'),(1.4,[-12,-8,8],'easeInOutSine'),(3.85,[-12,-8,8],'linear'),(4.4,[-14,-12,6],'easeInQuad'),(4.5,[-20,16,-8],'linear'),(4.65,[-20,16,-8],'linear'),(4.82,[-12,8,-4],'easeOutQuad'),(5,z,'easeInOutSine')],
            'left_arm':[(0,z,'easeOutQuad'),(1.4,[-38,24,-22],'easeOutQuad'),(4.4,[-38,24,-22],'linear'),(4.5,[-80,-24,-18],'linear'),(4.65,[-80,-24,-18],'linear'),(5,z,'easeInOutSine')],
            'torso':[(0,z,'easeOutQuad'),(1.4,[-5,-10,-2],'easeInOutSine'),(4.4,[-7,-12,-2],'easeInQuad'),(4.5,[7,14,2],'linear'),(4.65,[7,14,2],'linear'),(5,z,'easeOutQuad')],
            'head':[(0,z,'easeOutQuad'),(1.4,[9,8,0],'easeOutQuad'),(4.4,[9,8,0],'linear'),(4.5,[-7,-8,0],'linear'),(4.65,[-7,-8,0],'linear'),(5,z,'easeOutQuad')]})
    if kit=='frieren':
        a=animations['frieren_4']
        for name,frames in {
            'right_arm':[(0,z,'easeOutQuad'),(1.1,[-104,-18,8],'easeOutQuad'),(3.35,[-108,-18,8],'easeInQuad'),(3.5,[-83,8,5],'linear'),(3.65,[-83,8,5],'linear'),(10.55,[-67,4,5],'easeInOutSine'),(10.9,[-110,-15,6],'easeInQuad'),(11,[-76,12,8],'linear'),(11.15,[-76,12,8],'linear'),(11.4,[-60,8,8],'easeOutQuad'),(14,z,'easeInOutSine')],
            'left_arm':[(0,z,'easeOutQuad'),(1.1,[-92,30,-18],'easeOutQuad'),(3.35,[-94,34,-18],'easeInQuad'),(3.5,[-84,-24,-10],'linear'),(3.65,[-84,-24,-10],'linear'),(10.55,[-60,-10,-8],'easeInOutSine'),(10.9,[-104,28,-15],'easeInQuad'),(11,[-78,-22,-8],'linear'),(11.15,[-78,-22,-8],'linear'),(14,z,'easeInOutSine')],
            'torso':[(0,z,'easeOutQuad'),(1.1,[-4,0,0],'easeOutQuad'),(3.35,[-5,0,0],'easeInQuad'),(3.5,[4,0,0],'linear'),(3.65,[4,0,0],'linear'),(10.55,z,'easeInOutSine'),(10.9,[-5,0,0],'easeInQuad'),(11,[6,0,0],'linear'),(11.15,[6,0,0],'linear'),(14,z,'easeOutQuad')]
        }.items():a['bones'][name]=channel(frames)
    if kit=='asta':
        animations['asta_306']=gesture(1.6,{
            'right_arm':[(0,[-45,-15,-12],'easeOutQuad'),(.3,[-110,-12,-14],'easeOutQuad'),(.8,[-172,-8,-12],'easeInQuad'),(.9,[0,10,18],'linear'),(1.0,[0,10,18],'linear'),(1.2,[30,14,20],'easeOutQuad'),(1.6,z,'easeInOutSine')],
            'left_arm':[(0,[-30,15,-10],'easeOutQuad'),(.3,[-90,8,-25],'easeOutQuad'),(.8,[-135,5,-20],'easeInQuad'),(.9,[-35,-15,-32],'linear'),(1.0,[-35,-15,-32],'linear'),(1.6,z,'easeOutQuad')],
            'torso':[(0,z,'easeOutQuad'),(.8,[-8,-15,-2],'easeInQuad'),(.9,[14,18,3],'linear'),(1.0,[14,18,3],'linear'),(1.6,z,'easeOutQuad')]
        })
        for tipo,hit,length in [(304,.6,1.4),(305,.5,1.3)]:
            animations[f'asta_{tipo}']=gesture(length,{
                'right_arm':[(0,[-35,-20,-12],'linear'),(hit-.15,[-140,-35,-15],'easeInQuad'),(hit,[-20,45,24],'linear'),(hit+.1,[-20,45,24],'linear'),(length,z,'easeOutQuad')],
                'left_arm':[(0,[-35,10,-12],'linear'),(hit-.15,[-100,15,-25],'easeInQuad'),(hit,[-35,-30,-25],'linear'),(hit+.1,[-35,-30,-25],'linear'),(length,z,'easeOutQuad')],
                'torso':[(0,z,'linear'),(hit-.15,[-6,-20,-3],'easeInQuad'),(hit,[10,22,3],'linear'),(hit+.1,[10,22,3],'linear'),(length,z,'easeOutQuad')]})
        animations['asta_200']=gesture(2.75,{
            'right_arm':[(0,[-15,-15,-20],'easeInQuad'),(.7,[-100,8,-14],'easeOutQuad'),(1.65,[-42,20,-38],'easeInOutSine'),(2.25,[-50,22,-42],'easeInQuad'),(2.35,[-68,18,-22],'linear'),(2.5,[-68,18,-22],'linear'),(2.75,z,'easeOutQuad')],
            'left_arm':[(0,z,'easeOutQuad'),(.7,[-30,-20,-18],'easeOutQuad'),(1.65,[-55,-28,-28],'easeInOutSine'),(2.25,[-60,-30,-30],'easeInQuad'),(2.35,[-25,-18,-38],'linear'),(2.5,[-25,-18,-38],'linear'),(2.75,z,'easeOutQuad')],
            'torso':[(0,z,'easeOutQuad'),(.7,[7,-8,-2],'easeOutQuad'),(1.65,[10,18,-4],'easeInOutSine'),(2.25,[12,22,-5],'easeInQuad'),(2.35,[-4,-8,3],'linear'),(2.5,[-4,-8,3],'linear'),(2.75,z,'easeOutQuad')],
            'head':[(0,z,'easeOutQuad'),(1.65,[12,-10,-4],'easeInOutSine'),(2.25,[14,-12,-4],'easeInQuad'),(2.35,[-8,6,0],'linear'),(2.5,[-8,6,0],'linear'),(2.75,z,'easeOutQuad')]})

def awakened():
    out={};z=[0,0,0]
    # (kit, effect, duration, preparation, release, right arm, left arm, torso)
    specs=[
        (1,32,5.5,0,.1,[-92,-12,8],[-92,12,-8],[-3,0,0]),
        (1,40,3.5,0,.15,[-102,-18,10],[-55,30,-20],[-2,10,0]),
        (1,41,.9,0,.1,[-45,-20,15],[-45,20,-15],[0,12,0]),
        (2,34,3.2,1.05,1.2,[-160,-10,10],[-80,25,-20],[-5,-8,0]),
        (2,35,5,0,.25,[-102,-20,8],[-102,20,-8],[-6,0,0]),
        (2,36,2.25,0,.15,[-35,-20,25],[-35,20,-25],[-4,0,0]),
        (3,30,6,0,.2,[-90,-12,10],[-85,25,-12],[-2,0,0]),
        (3,37,2,0,.1,[-92,-12,8],[-85,12,-10],[-3,0,0]),
        (3,38,2,1.1,1.25,[-160,-10,8],[-90,25,-15],[-5,0,0]),
        (3,39,6,0,.2,[-85,-30,15],[-85,30,-15],[-4,0,0]),
        (3,200,3,.7,1.1,[-95,-18,12],[-95,18,-12],[-4,0,0])]
    for kit,effect,length,prep,hit,right,left,torso in specs:
        poses={}
        for name,pose in [('right_arm',right),('left_arm',left),('torso',torso)]:
            wind=[pose[0]*1.08,pose[1],pose[2]]
            frames=[(0,z,'easeOutQuad')]
            if prep>0:frames.append((prep,wind,'easeInQuad'))
            frames.extend([(hit,pose,'linear'),(round(min(length-.2,hit+.1),3),pose,'linear'),(round(length-.15,3),pose,'easeInOutSine'),(length,z,'easeOutQuad')])
            poses[name]=frames
        out[f'desperto_{kit}_{effect}']=gesture(length,poses)
    # Four discrete cuts, matching the server's t=0/3/6/9 sequence.
    poses={n:[] for n in ['right_arm','left_arm','torso']}
    for i,t in enumerate([0,.15,.3,.45]):
        for name in poses:
            pose=([-95,(-1)**i*65,(-1)**i*25] if name=='right_arm' else [-40,(-1)**i*-25,-15] if name=='left_arm' else [3,(-1)**i*15,0])
            poses[name].extend([(t,pose,'linear'),(round(t+.05,3),pose,'linear')])
    for frames in poses.values():frames.append((.6,z,'easeOutQuad'))
    out['desperto_2_33']=gesture(.6,poses)
    # Individually staged transformed finishers. Damage ticks remain server-owned.
    out['desperto_1_32']=gesture(5.5,{
        'right_arm':[(0,[-90,-15,8],'linear'),(.15,[-100,-12,10],'easeOutQuad'),(3.9,[-103,-12,10],'easeInOutSine'),(4.35,[-125,-22,12],'easeInQuad'),(4.5,[-70,16,6],'linear'),(4.65,[-70,16,6],'linear'),(5.5,z,'easeOutQuad')],
        'left_arm':[(0,[-85,30,-20],'linear'),(.15,[-92,26,-22],'easeOutQuad'),(3.9,[-92,26,-22],'linear'),(4.35,[-115,34,-25],'easeInQuad'),(4.5,[-76,-18,-12],'linear'),(4.65,[-76,-18,-12],'linear'),(5.5,z,'easeOutQuad')],
        'torso':[(0,[-3,-8,0],'linear'),(4.35,[-7,-12,0],'easeInQuad'),(4.5,[9,12,0],'linear'),(4.65,[9,12,0],'linear'),(5.5,z,'easeOutQuad')]})
    out['desperto_2_35']=gesture(5,{
        'right_arm':[(0,[-65,-28,20],'linear'),(.25,[-105,-35,28],'easeOutQuad'),(1.8,[-98,-24,18],'easeInOutSine'),(3.7,[-82,-18,15],'easeInOutSine'),(4.25,[-65,-10,12],'easeInQuad'),(5,z,'easeOutQuad')],
        'left_arm':[(0,[-60,28,-20],'linear'),(.25,[-105,35,-28],'easeOutQuad'),(1.8,[-98,24,-18],'easeInOutSine'),(3.7,[-82,18,-15],'easeInOutSine'),(4.25,[-65,10,-12],'easeInQuad'),(5,z,'easeOutQuad')],
        'torso':[(0,[3,0,0],'linear'),(.25,[-6,0,0],'easeOutQuad'),(3.7,[-8,0,0],'easeInOutSine'),(5,z,'easeOutQuad')]})
    out['desperto_3_38']=gesture(2,{
        'right_arm':[(0,z,'linear'),(.45,[-90,-20,8],'easeOutQuad'),(1.1,[-155,-15,10],'easeInQuad'),(1.25,[-65,12,8],'linear'),(1.35,[-65,12,8],'linear'),(2,z,'easeOutQuad')],
        'left_arm':[(0,z,'linear'),(.45,[-70,25,-15],'easeOutQuad'),(1.1,[-100,35,-20],'easeInQuad'),(1.25,[-80,-20,-15],'linear'),(1.35,[-80,-20,-15],'linear'),(2,z,'easeOutQuad')],
        'torso':[(0,z,'linear'),(1.1,[-6,-8,0],'easeInQuad'),(1.25,[8,12,0],'linear'),(1.35,[8,12,0],'linear'),(2,z,'easeOutQuad')]})
    return out

def save(path,data):path.write_bytes((json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))

def main():
    for kit in ['gojo','cid','frieren','asta']:
        p=ROOT/f'{kit}.json';data=json.loads(p.read_text(encoding='utf-8'));refine(data['animations'],kit);save(p,data)
    save(ROOT/'despertos.json',{'format_version':'1.8.0','geckolib_format_version':2,'animations':awakened()})
    print('Authored 12 awakening gestures and three ultimate sequences.')
if __name__=='__main__':main()
