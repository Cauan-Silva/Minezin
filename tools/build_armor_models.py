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
    b = skeleton(); head, body = b['armorHead'], b['armorBody']
    cube(head, [-4.25,27,-4.35], [8.5,2.55,.45], BLACK)
    for x in [-4.3,3.85]: cube(head, [x,27,-4], [.45,2.5,8.2], BLACK)
    cube(head, [-4.25,27,3.9], [8.5,2.5,.45], BLACK)
    for x in [-3.8,2.6]: cube(head, [x,26.55,-4.37], [1.2,.5,.42], BLACK)
    cube(body, [-4.2,12,-2.2], [8.4,11.8,4.4], INDIGO)
    cube(body, [-4.3,21.3,-2.45], [8.6,3,4.9], INDIGO)
    cube(body, [-3.4,21.7,-2.64], [6.8,1.6,.3], SHADOW)
    cube(body, [-3.8,12.1,-2.45], [3.6,9.6,.35], INDIGO)
    cube(body, [.05,12.1,-2.45], [3.75,9.6,.35], INDIGO)
    for y in [13.1,15.1,17.1,19.1,21.1]: cube(body, [-.25,y,-2.85], [.5,.65,.25], GOLD)
    cube(body, [-4.1,15.6,2.22], [8.2,.45,.18], SHADOW)
    for name, x in [('armorLeftArm',3.8), ('armorRightArm',-8.3)]:
        cube(b[name], [x,14.3,-2.2], [4.5,9.5,4.4], INDIGO)
        cube(b[name], [x-.05,14.0,-2.3], [4.6,.8,4.6], SHADOW)
        cube(b[name], [x-.05,15.1,-2.3], [4.6,.45,4.6], SHADOW)
        cube(b[name], [x+.5,17,-2.34], [.7,3.2,.16], SHADOW)
    legs(b, NAVY, BLACK, SHADOW)
    return b

def frieren():
    b = skeleton(); body = b['armorBody']
    for side, name in [(-1,'brinco_esq'), (1,'brinco_dir')]:
        b[name] = bone(name, [side*4.25,27,0], 'armorHead')
        cube(b[name], [side*4.25-.12,25.5,-.6], [.24,1.6,.3], GOLD)
        cube(b[name], [side*4.25-.22,25,-.72], [.44,.8,.38], RUBY)
    cube(body, [-4.25,12,-2.25], [8.5,12,4.5], IVORY)
    cube(body, [-2.3,13.7,-2.6], [4.6,7.5,.4], BLACK)
    for y in [14.5,17,19.5]: cube(body, [-2.1,y,-2.85], [4.2,.8,.2], WHITE)
    for x in [-2.8,2.2]: cube(body, [x,13.4,-2.95], [.65,8.9,.3], GOLD)
    for x in [-4.2,3.1]: cube(body, [x,13,-2.65], [1.15,9.5,.4], WHITE)
    cube(body, [-4.55,21.7,-2.5], [9.1,2.3,5], WHITE)
    for x in [-2.1,1.5]: cube(body, [x,22.8,-2.7], [.6,.75,.3], RUBY)
    cube(body, [-4.4,12,-2.65], [8.8,1.35,5.3], NAVY)
    cube(body, [-.5,12.1,-2.95], [1,1,.35], STEEL)
    for side, name in [(-1,'aba_esq'),(1,'aba_dir')]:
        b[name] = bone(name, [side*2.25,12.5,-2.2], 'armorBody')
        cube(b[name], [side*2.25-2.05,7.1,-2.68], [4.1,5.3,.55], WHITE)
        cube(b[name], [side*2.25-2.05,7.0,-2.75], [4.1,.55,.65], GOLD)
    for name,x,w in [('capa_esq',-4.35,2.8), ('capa_centro',-1.55,3.1), ('capa_dir',1.55,2.8)]:
        b[name] = bone(name, [x+w/2,22.5,2.5], 'armorBody')
        cube(b[name], [x,8,2.55], [w,14.5,.48], WHITE)
        cube(b[name], [x,7.9,2.52], [w,.55,.55], GOLD)
    for name,x in [('armorLeftArm',3.7), ('armorRightArm',-8.4)]:
        cube(b[name], [x,14,-2.3], [4.7,9.5,4.6], WHITE)
        cube(b[name], [x-.2,21.5,-2.5], [5.1,2.3,5], WHITE)
        cube(b[name], [x-.22,21.2,-2.52], [5.14,.65,5.04], GOLD)
        cube(b[name], [x+.9,22.05,-2.65], [.65,1.4,.3], RUBY)
        for y in [14.1,16.8]: cube(b[name], [x-.05,y,-2.4], [4.8,.75,4.8], GOLD)
    legs(b, NAVY, BROWN, BRONZE)
    return b

def shadow():
    b = skeleton(); head, body = b['armorHead'], b['armorBody']
    cube(head, [-4.8,24,3.3], [9.6,9.1,1.7], SHADOW)
    for x in [-5.1,3.7]: cube(head, [x,24,-4.6], [1.4,8.6,8.7], SHADOW)
    cube(head, [-4.8,31.4,-4.6], [9.6,1.7,9.6], SHADOW)
    cube(head, [-4.7,30.6,-4.8], [9.4,1,.5], INK)
    cube(head, [-4.9,29.1,4.9], [9.8,.55,.25], GOLD)
    for x in [-5.2,4.9]: cube(head, [x,29,-3.4], [.3,.55,8.4], GOLD)
    cube(body, [-4.3,12,-2.3], [8.6,12,4.6], SHADOW)
    cube(body, [-2.8,13.3,-2.65], [5.6,8.4,.45], INK)
    for y in [14.4,17,19.6]:
        for side in [-1,1]: cube(body, [side*1.25-1.4,y,-2.95], [2.8,.7,.35], STEEL, [side*1.25,y,-2.9], [0,0,side*23])
    for side in [-1,1]:
        cube(body, [side*3.55-.24,12,-2.75], [.48,10.4,.35], GOLD)
        cube(body, [side*4.2-1.3,21.7,-2.65], [2.6,3.8,.55], PLUM, [side*3.5,22,-2.5], [0,0,-side*20])
        cube(body, [side*4.2-1.3,21.7,-2.98], [.35,3.8,.25], GOLD, [side*3.5,22,-2.5], [0,0,-side*20])
    cube(body, [-4.4,11.8,-2.6], [8.8,1.5,5.2], BLACK)
    cube(body, [-1.2,11.9,-2.9], [2.4,1.2,.4], GOLD)
    for side,name in [(-1,'aba_esq'),(1,'aba_dir')]:
        b[name] = bone(name, [side*3,12,-2.1], 'armorBody')
        cube(b[name], [side*3-1.65,6,-2.55], [3.3,6,.6], SHADOW)
        cube(b[name], [side*3-1.65,5.9,-2.68], [3.3,.5,.8], GOLD)
    for name,x,w,y in [('capa_esq',-5.2,3.25,1.8),('capa_centro',-1.95,3.9,.8),('capa_dir',1.95,3.25,1.8)]:
        b[name] = bone(name, [x+w/2,22.7,2.7], 'armorBody')
        cube(b[name], [x,y,2.95], [w,22.7-y,.55], INK)
        cube(b[name], [x,y,3.5], [.55,20-y,.18], PLUM)
        for j in range(3):
            cube(b[name], [x+j*w/3,y+(j%2)*.7,3.53], [w/3,1.3,.2], LILAC if j%2==0 else PLUM)
            cube(b[name], [x+j*w/3,y+1.4+(j%2)*.7,3.53], [w/3,1.2,.2], PLUM)
    for name,x in [('armorLeftArm',3.7),('armorRightArm',-8.4)]:
        cube(b[name], [x,14,-2.35], [4.7,9.6,4.7], SHADOW)
        cube(b[name], [x-.35,21.7,-2.65], [5.4,2.1,5.3], PLUM)
        cube(b[name], [x-.4,21.3,-2.7], [5.5,.65,5.4], LILAC)
        cube(b[name], [x-.1,14.2,-2.45], [4.9,.6,4.9], GOLD)
        cube(b[name], [x-.12,16.9,-2.47], [4.94,.65,4.94], PLUM)
    legs(b, INK, LEATHER, GOLD)
    return b

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
            if name.startswith(('capa_','aba_','brinco_')):
                amount=1.2 if name.startswith('brinco_') else 2.5
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
