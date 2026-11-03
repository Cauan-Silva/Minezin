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
    # Anime reference: dark red shaft, golden crescent hugging a round ruby, red cord with hanging ribbons.
    cube(b['root'], [-.6,-17,-.6], [1.2,30.5,1.2], RUBY)
    cube(b['root'], [-.8,-17.4,-.8], [1.6,1.8,1.6], GOLD)
    cube(b['root'], [-.85,12.2,-.85], [1.7,1.2,1.7], GOLD)
    cube(b['root'], [-.75,10.2,-.75], [1.5,1.8,1.5], RUBY)
    cx,cy,r=0,17.6,3.6
    # Crescent: open towards the upper left, thicker at the bottom and tapering to the tips.
    for i in range(10):
        a0=math.radians(150+i*29);a1=math.radians(150+(i+1)*29);a=(a0+a1)/2
        x,y=cx+math.cos(a)*r,cy+math.sin(a)*r;comprimento=2*r*math.sin((a1-a0)/2)+.25
        espessura=.75+.85*max(0,-math.sin(a))
        # GeckoLib rotates Z clockwise for positive angles: negate to keep each segment tangent.
        cube(b['root'], [x-comprimento/2,y-espessura/2,-.6], [comprimento,espessura,1.2], GOLD, [x,y,0], [0,0,-(math.degrees(a)+90)])
    cube(b['root'], [-.6,12.8,-.6], [1.2,1.6,1.2], GOLD)
    b['gema'] = bone('gema', [cx,cy,0], 'root')
    cube(b['gema'], [cx-2.2,cy-2.2,.35], [4.4,4.4,.6], BRONZE)
    cube(b['gema'], [cx-1.6,cy-1.6,-1.0], [3.2,3.2,2.0], RUBY)
    cube(b['gema'], [cx-1.1,cy-2.0,-.9], [2.2,4.0,1.8], RUBY)
    cube(b['gema'], [cx-2.0,cy-1.1,-.9], [4.0,2.2,1.8], RUBY)
    cube(b['gema'], [cx-1.0,cy+.3,-1.06], [.6,.9,.1], WHITE)
    for side,name in [(-1,'fita_esq'),(1,'fita_dir')]:
        b[name] = bone(name, [side*.7,11.2,0], 'root')
        cube(b[name], [side*.7-.3,4.6,-.25], [.6,6.6,.5], RUBY, [side*.7,11.2,0], [0,0,-side*14])
    return b

def document(style, bones):
    clothing=style in ['gojo','frieren','shadow','asta','ichigo','ichigo_bankai','ichigo_mugetsu'] or any(style==n+'_'+slot for n in ['gojo','frieren','shadow','asta','ichigo'] for slot in ['helmet','chestplate','leggings','boots'])
    return {'format_version':'1.12.0','minecraft:geometry':[{'description':{
        'identifier':'geometry.'+style,'texture_width':128 if clothing else W,'texture_height':128 if clothing else H,
        'visible_bounds_width':4,'visible_bounds_height':5,'visible_bounds_offset':[0,1.5,0]},'bones':list(bones.values())}]}

def build(only=None):
    """only: optional set of styles; Gojo and Shadow have later cloth steps that a partial run must not overwrite."""
    subsets={'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},
             'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}
    for style,factory in [('gojo',gojo),('frieren',frieren),('shadow',shadow)]:
        if only is not None and style not in only:continue
        bones=factory()
        from clothing_uv import apply
        from polish_clothing_shapes import polish
        bones=apply(style,polish(style,bones))
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

    if only is None or 'shadow' in only:
        from build_shadow_trial import build as build_shadow_trial
        build_shadow_trial()

if __name__=='__main__': build()
