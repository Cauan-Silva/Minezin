"""Author the Gojo/Frieren GeckoLib models from named garment parts. No raster edits."""
from pathlib import Path
import copy
import json
import math
import struct

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'
W,H=struct.unpack('>II',(ASSETS/'textures/armor/anime_atlas.png').read_bytes()[16:24])

def save(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text((json.dumps(data,ensure_ascii=False,separators=(',',':')) if path.name.endswith('.geo.json') else json.dumps(data,ensure_ascii=False,indent=2))+'\n',encoding='utf-8')

def uv(tile):
    x=(tile%4+.09)*W/4;y=(tile//4+.09)*H/4
    return {f:{'uv':[round(x,3),round(y,3)],'uv_size':[round(W*.2,3),round(H*.2,3)]}
            for f in ['north','south','east','west','up','down']}

def bone(name,pivot,parent=None,rotation=None):
    b={'name':name,'pivot':pivot,'cubes':[]}
    if parent:b['parent']=parent
    if rotation:b['rotation']=rotation
    return b

def cube(b,origin,size,tile,pivot=None,rotation=None):
    c={'origin':origin,'size':size,'uv':uv(tile)}
    if pivot:c['pivot']=pivot
    if rotation:c['rotation']=rotation
    b['cubes'].append(c)

def skeleton():
    return {name:bone(name,pivot) for name,pivot in [
        ('armorHead',[0,24,0]),('armorBody',[0,24,0]),('armorLeftArm',[5,22,0]),
        ('armorRightArm',[-5,22,0]),('armorLeftLeg',[2,12,0]),('armorRightLeg',[-2,12,0]),
        ('armorLeftBoot',[2,12,0]),('armorRightBoot',[-2,12,0])]}

def gojo():
    b=skeleton();head=b['armorHead'];body=b['armorBody']
    cube(head,[-4.3,30.2,-4.15],[8.6,2.5,8.3],4)
    for row,z in enumerate([-2.4,.1,2.4]):
        for i,x in enumerate([-3.5,-2.1,-.7,.7,2.1,3.5]):
            pivot=[x,31.6,z];rot=[row*8-7,(i-2.5)*5,(i-2.5)*-10]
            cube(head,[x-.65,31.6,z-.75],[1.3,1.8,1.5],4,pivot,rot)
            cube(head,[x-.45,33.2,z-.5],[.9,.95,1.0],4,pivot,rot)
            cube(head,[x-.24,34.0,z-.3],[.48,.55+(i%3)*.18,.6],4,pivot,rot)
    for side in [-1,1]:
        cube(head,[side*4.0-(1 if side<0 else 0),28.0,-1.5],[1.0,4.1,4.7],4,[side*4,30,0],[0,0,-side*12])
    cube(head,[-4.35,27.0,-4.45],[8.7,2.3,.55],0)
    cube(head,[-4.4,27.0,-4.1],[.55,2.3,8.5],0);cube(head,[3.85,27.0,-4.1],[.55,2.3,8.5],0)
    cube(head,[-4.15,27.0,3.9],[8.3,2.3,.5],0)
    b['faixa_pontas']=bone('faixa_pontas',[2.2,28,4.4],'armorHead')
    cube(b['faixa_pontas'],[1.4,25.2,4.3],[1.3,3,.25],0,[2.2,28,4.4],[12,0,-12])
    cube(b['faixa_pontas'],[2.3,25.8,4.4],[1.1,2.4,.25],1,[2.2,28,4.4],[18,0,14])
    cube(body,[-4.25,12,-2.25],[8.5,12,4.5],0)
    cube(body,[-4.45,21.4,-2.65],[8.9,3.0,.55],0)
    cube(body,[-4.45,21.4,2.1],[8.9,3.0,.55],0)
    for side in [-1,1]:
        cube(body,[side*4.2-(.3 if side<0 else 0),21.4,-2.2],[.3,3,4.4],1)
        cube(body,[side*2.1-1.7,12,-2.52],[3.4,9.1,.3],0)
    cube(body,[-.2,12.4,-2.74],[.4,10,.25],1)
    for y in [14.0,16.4,18.8,21.2]:cube(body,[-.15,y,-2.96],[.3,.3,.25],6)
    cube(body,[-4.4,11.8,-2.48],[8.8,1.15,4.96],14)
    for side,name in [(-1,'aba_esq'),(1,'aba_dir')]:
        b[name]=bone(name,[side*2,12,1.9],'armorBody')
        cube(b[name],[side*2-2,9.2,1.95],[4,2.9,.45],0)
        cube(b[name],[side*2-2,9.15,1.9],[4,.25,.55],1)
    for side,arm in [(-1,'armorRightArm'),(1,'armorLeftArm')]:
        x=-8.35 if side<0 else 3.85
        cube(b[arm],[x,15.0,-2.3],[4.5,9.2,4.6],0)
        cube(b[arm],[x-.1,21.8,-2.4],[4.7,1.3,4.8],1)
        cube(b[arm],[x-.1,14.6,-2.4],[4.7,1.4,4.8],14)
        cube(b[arm],[x+1.8,15.1,-2.7],[.6,.6,.35],6)
    for leg,x in [('armorLeftLeg',-.1),('armorRightLeg',-4.25)]:
        cube(b[leg],[x,-.1,-2.2],[4.35,12.2,4.4],0)
        cube(b[leg],[x+.6,4.2,-2.4],[3.1,2.8,.3],1)
    for boot,x in [('armorLeftBoot',-.2),('armorRightBoot',-4.35)]:
        cube(b[boot],[x,-.2,-2.9],[4.55,2.5,5.35],12)
        cube(b[boot],[x,1.7,-2.3],[4.55,3.0,4.65],12)
        cube(b[boot],[x,-.3,-3.0],[4.55,.45,5.5],6)
        cube(b[boot],[x+1.5,2.5,-2.65],[1.5,.45,.4],6)
    return b

def frieren():
    b=skeleton();head=b['armorHead'];body=b['armorBody']
    cube(head,[-4.3,30.2,-4.15],[8.6,2.4,8.3],4)
    cube(head,[-4.4,23.0,3.2],[8.8,8.8,1.3],4)
    for i in range(6):cube(head,[-4.1+i*1.35,29.0,-4.5],[1.35,2.8,.9],4,[-3.4+i*1.35,31.8,-4],[0,0,(i-2.5)*4])
    for side,brinco in [(-1,'brinco_esq'),(1,'brinco_dir')]:
        x=-6.1 if side<0 else 4.0
        cube(head,[x,20.6,-3.4],[2.1,10.0,2.6],4,[side*5.0,30,1.5],[0,0,-side*5])
        cube(head,[x+.1,20.1,-3.2],[1.9,1.2,2.2],4)
        ear_x=-7.0 if side<0 else 4.0
        cube(head,[ear_x,27.0,-.6],[3.0,1.2,1.5],7,[side*4,27.5,0],[0,0,side*16])
        cube(head,[-7.7 if side<0 else 6.6,27.5,-.3],[1.0,.6,.9],7,[side*6.7,27.7,0],[0,0,side*20])
        b[brinco]=bone(brinco,[side*5.9,27,-.8],'armorHead')
        cube(b[brinco],[side*5.9-.12,25.4,-.95],[.24,1.8,.25],5)
        cube(b[brinco],[side*5.9-.34,24.9,-1.05],[.68,.8,.35],5)
        cube(b[brinco],[side*5.9-.2,24.98,-1.2],[.4,.56,.22],10)
    cube(body,[-4.4,12,-2.4],[8.8,12.2,4.8],2)
    cube(body,[-5.2,20.7,-2.65],[10.4,3.1,5.3],2)
    for x in [-4.4,.4]:
        cube(body,[x,12,-2.9],[4,10.2,.55],11)
        cube(body,[x,12,-3.0],[.35,10.1,.15],5)
    cube(body,[-4.5,11.8,-2.7],[9,1.0,5.4],15)
    cube(body,[-.65,21,-3.15],[1.3,1.2,.45],5)
    cube(body,[-.4,21.2,-3.4],[.8,.8,.3],10)
    for name,x,w,rot in [('capa_centro',-3,6,0),('capa_esq',-6,3.1,-12),('capa_dir',2.9,3.1,12)]:
        b[name]=bone(name,[x+w/2,22.5,2.6],'armorBody',[4,rot,0])
        cube(b[name],[x,4.5,2.7],[w,18,.6],2)
        cube(b[name],[x,4.35,2.65],[w,.85,.7],15)
        cube(b[name],[x,5.2,3.3],[.3,17.0,.12],5)
    for arm,x in [('armorLeftArm',3.6),('armorRightArm',-8.4)]:
        cube(b[arm],[x,15.0,-2.4],[4.8,9.1,4.8],2)
        cube(b[arm],[x-.25,14.5,-2.65],[5.3,2.0,5.3],15)
        cube(b[arm],[x-.25,14.35,-2.7],[5.3,.4,5.4],5)
        cube(b[arm],[x-.1,21.6,-2.5],[5,1.4,5],15)
    for leg,x in [('armorLeftLeg',-.15),('armorRightLeg',-4.25)]:
        cube(b[leg],[x,-.1,-2.15],[4.4,12.1,4.3],0)
        cube(b[leg],[x-.1,4.7,-2.7],[4.6,7.5,.55],2)
        cube(b[leg],[x-.1,4.7,-2.85],[.35,7.5,.2],5)
        cube(b[leg],[x-.1,4.5,-2.8],[4.6,.5,.7],15)
    for boot,x in [('armorLeftBoot',-.2),('armorRightBoot',-4.35)]:
        cube(b[boot],[x,-.2,-2.9],[4.55,2.6,5.35],12)
        cube(b[boot],[x,1.7,-2.25],[4.55,3.1,4.5],12)
        cube(b[boot],[x,-.3,-3.0],[4.55,.45,5.5],6)
        cube(b[boot],[x+1.4,2.6,-2.7],[1.6,.6,.4],5)
    return b

def document(style,bones):
    return {'format_version':'1.12.0','minecraft:geometry':[{'description':{
        'identifier':'geometry.'+style,'texture_width':W,'texture_height':H,
        'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':list(bones.values())}]}

def build():
    subsets={'helmet':({'armorHead'},28),'chestplate':({'armorBody','armorLeftArm','armorRightArm'},18),
             'leggings':({'armorLeftLeg','armorRightLeg'},6),'boots':({'armorLeftBoot','armorRightBoot'},2)}
    for style,bones in [('gojo',gojo()),('frieren',frieren())]:
        save(ASSETS/f'geo/armaduras/{style}.geo.json',document(style,bones))
        scales={}
        for part,(roots,center) in subsets.items():
            icon=copy.deepcopy(bones)
            def root(b):return root(icon[b['parent']]) if b.get('parent') else b['name']
            for b in icon.values():
                if root(b) not in roots:b['cubes']=[]
            cubes=[c for b in icon.values() for c in b['cubes']]
            lower=[min(c['origin'][axis] for c in cubes) for axis in range(3)]
            upper=[max(c['origin'][axis]+c['size'][axis] for c in cubes) for axis in range(3)]
            center=(lower[1]+upper[1])/2
            scales[part]=round(min(1.3,14/max(upper[i]-lower[i] for i in range(3))),3)
            for b in icon.values():
                b['pivot'][1]-=center
                for c in b['cubes']:
                    c['origin'][1]-=center
                    if 'pivot' in c:c['pivot'][1]-=center
            save(ASSETS/f'geo/armaduras/{style}_{part}.geo.json',document(style+'_'+part,icon))
        anim={}
        for name in bones:
            if name.startswith(('capa_','aba_','faixa_','brinco_')):
                amount=2 if name.startswith('brinco_') else 4
                anim[name]={'rotation':{'0':[0,0,0],'1.5':[amount,0,1],'3':[0,0,0],'4.5':[-amount*.5,0,-1],'6':[0,0,0]}}
        save(ASSETS/f'animations/armaduras/{style}.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':6,'bones':anim}}})
        for part,suffix in [('helmet','cabeca'),('chestplate','peito'),('leggings','pernas'),('boots','botas')]:
            item=f'{style}_{suffix}'
            save(ASSETS/f'models/item/{item}.json',{'parent':'builtin/entity','display':{
                'gui':{'rotation':[8,-28,0],'translation':[0,0,0],'scale':[scales[part]]*3},
                'ground':{'translation':[0,3,0],'scale':[.5,.5,.5]},'fixed':{'scale':[.8,.8,.8]}}})
            save(ROOT/f'src/main/resources/data/arcanecontrol/recipes/{item}.json',{
                'type':'minecraft:smithing_transform','template':{'item':'minecraft:paper'},
                'base':{'item':'minecraft:'+('netherite_' if style=='gojo' else 'diamond_')+part},
                'addition':{'item':'minecraft:amethyst_shard' if style=='gojo' else 'minecraft:gold_ingot'},
                'result':{'item':'arcanecontrol:'+item}})
        print(style, 'bones',len(bones),'cubes',sum(len(b['cubes']) for b in bones.values()))

if __name__=='__main__':build()
