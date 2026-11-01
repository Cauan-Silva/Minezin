"""Costume silhouette refinements, independent of material authoring."""
import build_armor_models as a

def polish(style,b):
    # Cover the entire shoulder end-cap, including the outer skin sleeve layer.
    # A cap below y=24 exposes the whole 4x4 top face when the arm rotates.
    for name,x in [('armorLeftArm',3.65),('armorRightArm',-8.35)]:
        sleeve=max(b[name]['cubes'],key=lambda c:c['size'][1])
        sleeve['size'][1]=24.4-sleeve['origin'][1]
        sleeve['origin'][0]=x;sleeve['size'][0]=4.7
        sleeve['origin'][2]=-2.35;sleeve['size'][2]=4.7
    if style=='gojo':
        # Replace paper-thin fold geometry with pixel-art seams; retain one brass button.
        for name in ['armorBody','armorLeftArm','armorRightArm']:
            b[name]['cubes']=[c for c in b[name]['cubes'] if c['size'][2]>=.2]
        a.cube(b['armorBody'],[-4.27,12,-2.24],[8.54,1.15,4.48],a.NAVY)
        # Blindfold sits outside the outer skin layer (8px head + 0.5px hat inflation).
        for c in b['armorHead']['cubes']:
            o,s=c['origin'],c['size']
            if s[0]>8:o[0]= -4.8;s[0]=9.6
            if o[2]<-4.1:o[2]-=.5
            elif o[2]>3.9:o[2]=4.55
            if s[2]>8:s[2]=9.1;o[2]=-4.5
            if s[0]<.4:o[0]= -4.8 if o[0]<0 else 4.45
    elif style=='frieren':
        for name in ['brinco_esq','brinco_dir']:
            delta=-.6 if name.endswith('esq') else .6
            b[name]['pivot'][0]+=delta
            for c in b[name]['cubes']:c['origin'][0]+=delta
        # A slightly longer front hem with separate leg parents, preserving walking.
        for name in ['saia_esq','saia_dir']:
            for c in b[name]['cubes']:c['origin'][1]-=.6;c['size'][1]+=.6
    elif style=='shadow':
        for name in list(b):
            if name.startswith('capa_'):del b[name]
        # Broad, overlapping panels keep the upper cape continuous and the hem torn.
        for i,bottom in enumerate([2.7,.7,1.5,.2,2.2]):
            x=(i-2)*2.4;name=f'capa_{i}'
            b[name]=a.bone(name,[x,23,3.1],'armorBody');pivot=[x,23,3.1];angle=(i-2)*2.5
            a.cube(b[name],[x-1.35,bottom+1.6,3.15],[2.7,21.4-bottom,.55],a.BLACK,pivot,[0,0,angle])
            a.cube(b[name],[x-1.35,bottom+1.6,3.0],[2.7,10,.14],a.PLUM,pivot,[0,0,angle])
            a.cube(b[name],[x-1.35,bottom,3.15],[1.3,2,.55],a.BLACK,pivot,[0,0,angle])
        # The gold back V must remain in front of the new cape surface.
        for c in b['armorBody']['cubes']:
            if c['origin'][2]>=3.5:c['origin'][2]+=.3
    elif style=='asta':
        # Thin draped surfaces replace the thick, tilted shoulder boxes.
        for n in list(b):
            if n.startswith(('manto_','capa_touro')):del b[n]
        for side,name in [(-1,'manto_frente_esq'),(1,'manto_frente_dir')]:
            x=-4.5 if side<0 else -.1;bottom=19.1 if side<0 else 19.6
            b[name]=a.bone(name,[side*2.2,23.8,-2.6],'armorBody')
            a.cube(b[name],[x,bottom,-2.8],[4.6,24-bottom,.35],a.BLACK,[side*2.2,23.8,-2.6],[0,0,side*7])
            a.cube(b[name],[x,bottom-.05,-2.86],[4.6,.28,.42],a.GOLD,[side*2.2,23.8,-2.6],[0,0,side*7])
        for side,name in [(-1,'capa_touro_esq'),(1,'capa_touro_dir')]:
            x=-4.5 if side<0 else -.1
            b[name]=a.bone(name,[side*2.2,23.8,2.6],'armorBody')
            a.cube(b[name],[x,19.2,2.5],[4.6,4.8,.35],a.BLACK)
            a.cube(b[name],[x,19.15,2.49],[4.6,.28,.42],a.GOLD)
        for side,name,parent in [(-1,'manto_ombro_esq','armorRightArm'),(1,'manto_ombro_dir','armorLeftArm')]:
            x=-8.5 if side<0 else 3.6
            b[name]=a.bone(name,[side*5,22,0],parent)
            # Top and thin front/back drops make a mantle, rather than a shoulder pad.
            a.cube(b[name],[x,24.4,-2.6],[4.9,.35,5.2],a.BLACK)
            for z in [-2.62,2.35]:
                a.cube(b[name],[x,22.55,z],[4.9,2.1,.28],a.BLACK)
                a.cube(b[name],[x,22.5,z-.03],[4.9,.25,.34],a.GOLD)
            outer=x if side<0 else x+4.6
            a.cube(b[name],[outer,22.85,-2.5],[.3,1.8,5],a.BLACK)
        for c in b['emblema_touro']['cubes']:
            c['origin'][2]-=.05

        for c in b['armorHead']['cubes']:
            o,s=c['origin'],c['size']
            if s[0]>8:o[0]=-4.75;s[0]=9.5
            if o[2]<-4.1:o[2]-=.6
            elif o[2]>3.9:o[2]=4.55
            if s[2]>8:s[2]=9.1;o[2]=-4.55
            if s[0]<.4:o[0]=-4.75 if o[0]<0 else 4.45
        a.cube(b['grimoire'],[-6.1,12.6,.15],[.25,1.5,3.6],a.BROWN,[-5.95,13.8,1.9],[0,0,-12])
    return b
