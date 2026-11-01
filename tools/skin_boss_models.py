"""Minecraft 64x64 skin layout, with separate articulated costume accessories.

The supplied skin pixels are used unchanged. Geometry carries explicit vanilla UVs.
"""
import copy
import build_nemesis_models as m

def uv_box(u,v,w,h,d):
    return {n:{'uv':pos,'uv_size':size} for n,pos,size in [
        ('north',[u+d,v+d],[w,h]),('south',[u+d+w+d,v+d],[w,h]),
        ('east',[u,v+d],[d,h]),('west',[u+d+w,v+d],[d,h]),
        ('up',[u+d,v],[w,d]),('down',[u+d+w,v],[w,d])]}

def body():
    b=m.base()
    for name,o,size,uv,layer in [
        ('head',[-4,24,-4],[8,8,8],[0,0],[32,0]),
        ('body',[-4,12,-2],[8,12,4],[16,16],[16,32]),
        ('rightArm',[-8,12,-2],[4,12,4],[40,16],[40,32]),
        ('leftArm',[4,12,-2],[4,12,4],[32,48],[48,48]),
        ('rightLeg',[-4,0,-2],[4,12,4],[0,16],[0,32]),
        ('leftLeg',[0,0,-2],[4,12,4],[16,48],[0,48])]:
        w,h,d=size
        b[name]['cubes']=[{'origin':o,'size':size,'uv':uv_box(*uv,w,h,d)},
                          {'origin':o,'size':size,'inflate':.5 if name=='head' else .25,'uv':uv_box(*layer,w,h,d)}]
    return b

def patch(c,p):
    c['uv']={f:{'uv':[p[0]+.2,p[1]+.2],'uv_size':[.6,.6]} for f in ['north','south','east','west','up','down']}

def accessories(shape,names,palette):
    b=body()
    for n in names:
        bone=copy.deepcopy(shape[n])
        for i,c in enumerate(bone.get('cubes',[])):
            # UV refers to an existing material pixel, no replacement atlas or resampling.
            old=c['uv']['north']['uv'];tile=int(old[0]/m.W*4)+4*int(old[1]/m.H*4)
            patch(c,palette.get(tile,palette[m.BLACK]))
        b[n]=bone
    return b

def aurora(shape):
    # Skin already supplies face/hair cap, jewelry, blouse, gloves and shoes.
    names=[n for n in shape if n.startswith(('hair_','skirt_'))]
    b=accessories(shape,names,{m.BLACK:(4,16),m.PLUM:(9,2),m.RED:(9,2),m.GOLD:(10,1)})
    for n in names:
        if n.startswith('skirt_'):
            i=int(n.split('_')[1]);c=b[n]['cubes'][0]
            for f in ['north','south']:c['uv'][f]={'uv':[20+(i%4)*2,24],'uv_size':[2,8]}
    return b

def liebe(shape):
    names=[n for n in shape if n.startswith(('wing_','tail_'))]
    b=accessories(shape,names,{m.BLACK:(38,9),m.PLUM:(46,16)})
    # Keep only the curved horn cuboids; the supplied skin supplies all face/hair detail.
    # Explicit first eight segment cuboids are identified by the connected segment width.
    horns=[]
    for c in shape['head']['cubes']:
        if c.get('pivot') and abs(c['size'][0]-c['size'][2])<1e-6 and c['size'][0]<=1.71 and c['origin'][1]>=30:
            c=copy.deepcopy(c);patch(c,(38,9));horns.append(c)
    b['head']['cubes'].extend(horns)
    return b
