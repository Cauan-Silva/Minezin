"""Author reference-based GeckoLib clothing and Frieren staff. No raster edits."""
from pathlib import Path
import copy
import json
import math
import struct

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/arcanecontrol'
ATLAS = ASSETS / 'textures/armor/anime_reference_atlas.png'
W, H = struct.unpack('>II', ATLAS.read_bytes()[16:24])
BLACK, INDIGO, IVORY, SHADOW, STEEL, GOLD, NAVY, BROWN, CYAN, LILAC, RUBY, WHITE, LEATHER, INK, PLUM, BRONZE = range(16)

def save(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    compact = path.name.endswith('.geo.json')
    text = json.dumps(data, ensure_ascii=False, separators=(',', ':')) if compact else json.dumps(data, ensure_ascii=False, indent=2)
    path.write_bytes((text + '\n').encode('utf-8'))

def uv(tile):
    x, y = (tile % 4 + .15) * W / 4, (tile // 4 + .15) * H / 4
    return {f: {'uv': [round(x, 3), round(y, 3)], 'uv_size': [round(W * .175, 3), round(H * .175, 3)]}
            for f in ['north', 'south', 'east', 'west', 'up', 'down']}

def bone(name, pivot, parent=None, rotation=None):
    b = {'name': name, 'pivot': pivot, 'cubes': []}
    if parent: b['parent'] = parent
    if rotation: b['rotation'] = rotation
    return b

def cube(b, origin, size, tile, pivot=None, rotation=None):
    c = {'origin': origin, 'size': size, 'uv': uv(tile)}
    if pivot: c['pivot'] = pivot
    if rotation: c['rotation'] = rotation
    b['cubes'].append(c)

def skeleton():
    return {name: bone(name, pivot) for name, pivot in [
        ('armorHead', [0,24,0]), ('armorBody', [0,24,0]), ('armorLeftArm', [5,22,0]),
        ('armorRightArm', [-5,22,0]), ('armorLeftLeg', [2,12,0]), ('armorRightLeg', [-2,12,0]),
        ('armorLeftBoot', [2,12,0]), ('armorRightBoot', [-2,12,0])]}

def legs(b, material, boots, cuff):
    for name, x in [('armorLeftLeg', -.1), ('armorRightLeg', -4.2)]:
        cube(b[name], [x,0,-2.12], [4.3,12,4.24], material)
        cube(b[name], [x+.45,4.8,-2.23], [3.2,2.2,.15], INDIGO if material == NAVY else PLUM)
    for name, x in [('armorLeftBoot', -.15), ('armorRightBoot', -4.25)]:
        cube(b[name], [x,-.15,-2.65], [4.4,2.2,4.95], boots)
        cube(b[name], [x,1.8,-2.25], [4.4,3.0,4.5], boots)
        cube(b[name], [x,-.22,-2.7], [4.4,.45,5.05], LEATHER)
        cube(b[name], [x-.03,4.3,-2.3], [4.46,.7,4.6], cuff)

def gojo():
    from armor_reference_shapes import gojo as model
    return model()

def frieren():
    from armor_reference_shapes import frieren as model
    return model()

def shadow():
    from armor_reference_shapes import shadow as model
    return model()

def staff():
    b = {'root': bone('root', [0,0,0])}
    cube(b['root'], [-.65,-17,-.65], [1.3,31,1.3], RUBY)
    cube(b['root'], [-.8,-17.2,-.8], [1.6,2.2,1.6], GOLD)
    cube(b['root'], [-.8,9,-.8], [1.6,2.2,1.6], GOLD)
    cube(b['root'], [-.77,-3,-.77], [1.54,4.7,1.54], LEATHER)
    # Octagonal open crest around the ruby. Intentionally no noisy surface texture.
    for i in range(7):
        a=math.radians(-135+i*45);x,y=math.cos(a)*3.8,16.7+math.sin(a)*3.8
        cube(b['root'], [x-1.55,y-.55,-.55], [3.1,1.1,1.1], GOLD, [x,y,0], [0,0,math.degrees(a)+90])
    cube(b['root'], [-2.4,13.8,-.75], [4.8,5.6,1.5], BRONZE)
    b['gema'] = bone('gema', [0,16.6,-.9], 'root')
    cube(b['gema'], [-1.75,14.55,-1.2], [3.5,4.1,2.6], RUBY)
    cube(b['gema'], [-1.1,15,-1.26], [.55,2.7,.1], WHITE)
    for side,name in [(-1,'fita_esq'),(1,'fita_dir')]:
        b[name] = bone(name, [side*2.8,12.5,0], 'root')
        cube(b[name], [side*2.8-.45,6.5,-.45], [.9,6,.5], RUBY, [side*2.8,12.5,0], [0,0,-side*12])
    return b

def document(style, bones):
    return {'format_version':'1.12.0','minecraft:geometry':[{'description':{
        'identifier':'geometry.'+style,'texture_width':W,'texture_height':H,
        'visible_bounds_width':4,'visible_bounds_height':5,'visible_bounds_offset':[0,1.5,0]},'bones':list(bones.values())}]}

def build():
    subsets={'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},
             'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}
    for style,bones in [('gojo',gojo()),('frieren',frieren()),('shadow',shadow())]:
        save(ASSETS/f'geo/armaduras/{style}.geo.json', document(style,bones));scales={}
        for part,roots in subsets.items():
            icon=copy.deepcopy(bones)
            def root(b): return root(icon[b['parent']]) if b.get('parent') else b['name']
            for b in icon.values():
                if root(b) not in roots: b['cubes']=[]
            cubes=[c for b in icon.values() for c in b['cubes']]
            lower=[min(c['origin'][axis] for c in cubes) for axis in range(3)]
            upper=[max(c['origin'][axis]+c['size'][axis] for c in cubes) for axis in range(3)]
            center=(lower[1]+upper[1])/2;scales[part]=round(min(1.3,14/max(upper[i]-lower[i] for i in range(3))),3)
            for b in icon.values():
                b['pivot'][1]-=center
                for c in b['cubes']:
                    c['origin'][1]-=center
                    if 'pivot' in c: c['pivot'][1]-=center
            save(ASSETS/f'geo/armaduras/{style}_{part}.geo.json',document(style+'_'+part,icon))
        anim={}
        for name in bones:
            if name.startswith(('capa_','aba_','brinco_','manto_')):
                amount=.65 if name.startswith('manto_') else 1.2 if name.startswith('brinco_') else 2.5
                amount*=1+(sum(map(ord,name))%5-2)*.12
                anim[name]={'rotation':{'0':[0,0,0],'1.5':[amount,0,1],'3':[0,0,0],'4.5':[-amount*.5,0,-1],'6':[0,0,0]}}
        save(ASSETS/f'animations/armaduras/{style}.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':6,'bones':anim}}})
        for part,suffix,cid in [('helmet','cabeca','capuz'),('chestplate','peito','manto'),('leggings','pernas','calcas'),('boots','botas','botas')]:
            item=cid+'_shadow' if style=='shadow' else style+'_'+suffix
            save(ASSETS/f'models/item/{item}.json',{'parent':'builtin/entity','display':{
                'gui':{'rotation':[8,-28,0],'translation':[0,0,0],'scale':[scales[part]]*3},
                'ground':{'translation':[0,3,0],'scale':[.5]*3},'fixed':{'scale':[.8]*3}}})
        print(style,'bones',len(bones),'cubes',sum(len(b['cubes']) for b in bones.values()))
    save(ASSETS/'geo/frieren/cajado.geo.json',document('cajado_frieren',staff()))
    save(ASSETS/'animations/frieren/cajado.animation.json',{'format_version':'1.8.0','animations':{'animation.cajado.idle':{
        'loop':True,'animation_length':4,'bones':{
            'fita_esq':{'rotation':{'0':[0,0,-2],'2':[4,0,3],'4':[0,0,-2]}},
            'fita_dir':{'rotation':{'0':[0,0,2],'2':[-4,0,-3],'4':[0,0,2]}},
            'gema':{'scale':{'0':[1,1,1],'2':[1.04,1.04,1.04],'4':[1,1,1]}}}}}})
    save(ASSETS/'models/item/cajado_frieren.json',{'parent':'builtin/entity','display':{
        'gui':{'rotation':[0,0,-25],'translation':[0,0,0],'scale':[.36]*3},
        'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.8]*3},
        'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.8]*3},
        'firstperson_righthand':{'rotation':[0,-15,-8],'translation':[2,0,-2],'scale':[.65]*3},
        'firstperson_lefthand':{'rotation':[0,15,8],'translation':[-2,0,-2],'scale':[.65]*3},
        'ground':{'rotation':[0,0,85],'translation':[0,2,0],'scale':[.5]*3},
        'fixed':{'rotation':[0,0,-25],'translation':[0,0,0],'scale':[.45]*3}}})

if __name__=='__main__': build()
