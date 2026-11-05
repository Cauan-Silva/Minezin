"""Subaru Natsuki's tracksuit (style 'subaru'), from the user's reference (Re:Zero): white jacket front and back with
black sleeves, black shoulder yoke and black ribbed hem; a yellow-orange band under the yoke, a yellow stripe down each
sleeve and yellow cuffs; tall black stand collar, yellow inside; silver zip closed to the collar; black "N" on the left
chest. Black track pants with a yellow stripe on the outer leg; dark sneakers with white soles. Fitted, no cloth
(docs/padrao-trajes.md). No borrowed art.
"""
import copy
import xml.etree.ElementTree as ET
from PIL import Image,ImageDraw
import build_armor_models as a
from armor_reference_shapes import garment as c
A=a.ASSETS
PRETO,CANELADO,AMARELO,BRANCO,METAL,SOLA,ESCURO=a.BLACK,a.SHADOW,a.GOLD,a.WHITE,a.STEEL,a.IVORY,a.INK

def _torso(b):
    B='armorBody'
    c(b,B,[-4.12,13.9,-2.12],[8.24,10.1,4.24],BRANCO)                         # white jacket body (front face z=-2.12)
    c(b,B,[-4.2,21.7,-2.2],[8.4,2.3,4.4],PRETO)                               # black shoulder yoke, front and back
    c(b,B,[-4.24,21.0,-2.24],[8.48,.7,4.48],AMARELO)                          # yellow band under the yoke
    c(b,B,[-4.25,12.0,-2.25],[8.5,1.9,4.5],PRETO)                             # black ribbed hem
    c(b,B,[-4.28,12.85,-2.28],[8.56,.25,4.56],CANELADO)                       # rib line
    c(b,B,[-.25,12.0,-2.32],[.5,12.0,.1],METAL)                               # zip closed up to the collar
    c(b,B,[-.45,22.4,-2.44],[.9,.9,.14],METAL)                                # zip pull at the collar
    # collar: black band around the neck, yellow inside showing at the front opening
    c(b,B,[-4.3,22.4,-2.3],[8.6,1.6,4.6],PRETO)
    for s in [-1,1]:c(b,B,[s*.85-.55,22.4,-2.38],[1.1,1.6,.1],AMARELO)
    # black "N" on the wearer's left chest (the viewer's right in the front view; -x in this geo, checked in the preview)
    nx=-2.1   # chest centre of the letter
    c(b,B,[nx-.85,17.6,-2.24],[.45,2.4,.1],PRETO);c(b,B,[nx+.4,17.6,-2.24],[.45,2.4,.1],PRETO)
    c(b,B,[nx-.25,17.5,-2.26],[.5,2.6,.1],PRETO,[nx,18.8,-2.25],[0,0,-24])
    c(b,B,[-.2,14.2,2.12],[.4,7.2,.12],CANELADO)                               # back seam

def _mangas(b):
    # polish() widens the main sleeve to 4.7 (x 3.65..8.35 on the left arm, -8.35..-3.65 on the right): details go outside it
    for name,x,s in [('armorLeftArm',3.9,1),('armorRightArm',-8.1,-1)]:
        c(b,name,[x,14.0,-2.1],[4.2,10.0,4.2],PRETO)
        cx=3.6 if s>0 else -8.4
        c(b,name,[cx,12.9,-2.4],[4.8,1.5,4.8],AMARELO)                        # yellow cuff (hand stays bare below)
        c(b,name,[cx-.02,13.9,-2.42],[4.84,.25,4.84],CANELADO)                # rib line
        fora=8.35 if s>0 else -8.47
        c(b,name,[fora,14.6,-.4],[.12,9.6,.8],AMARELO)                        # stripe down the outside of the sleeve

def _pernas(b):
    for leg,x,s in [('armorLeftLeg',0,1),('armorRightLeg',-4,-1)]:
        c(b,leg,[x-.1,2.4,-2.2],[4.2,9.7,4.4],PRETO)
        fora=x+4.1 if s>0 else x-.22
        c(b,leg,[fora,2.9,-.4],[.12,9.0,.8],AMARELO)                          # yellow stripe on the outer leg
        c(b,leg,[x-.15,2.4,-2.25],[4.3,.6,4.5],CANELADO)                     # ankle band
    for foot,x in [('armorLeftBoot',0),('armorRightBoot',-4)]:
        c(b,foot,[x-.15,.6,-2.3],[4.3,2.2,4.6],ESCURO)                        # dark sneaker upper
        c(b,foot,[x-.2,0,-2.6],[4.4,.7,4.95],SOLA)                            # white sole
        c(b,foot,[x-.18,.6,-2.62],[4.36,1.0,.45],SOLA)                        # toe cap
        c(b,foot,[x+.9,1.6,-2.42],[2.2,1.1,.14],METAL)                        # laces

def traje():
    b=a.skeleton()
    _torso(b);_mangas(b);_pernas(b)
    return b

def main():
    from clothing_uv import apply
    from polish_clothing_shapes import polish
    b=apply('subaru',polish('subaru',traje()))
    svg=A/'textures/armor/trajes/subaru.svg';text=svg.read_text(encoding='utf-8')
    im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
    for r in ET.fromstring(text):
        x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(A/'textures/armor/trajes/subaru.png')
    a.save(A/'geo/armaduras/subaru.geo.json',a.document('subaru',b))
    for part,roots in {'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}.items():
        icon=copy.deepcopy(b)
        def root(n):return root(icon[n]['parent']) if 'parent' in icon[n] else n
        if part=='helmet':   # no hat: the collar is the icon of the first piece
            icon['armorHead']['cubes']=[q for q in copy.deepcopy(icon['armorBody']['cubes']) if q['origin'][1]>=22.3]
        for n,v in icon.items():
            if root(n) not in roots or part=='helmet' and n!='armorHead':v['cubes']=[]
        cubes=[q for v in icon.values() for q in v['cubes']];lo=[min(q['origin'][i] for q in cubes) for i in range(3)];hi=[max(q['origin'][i]+q['size'][i] for q in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
        for v in icon.values():
            v['pivot'][1]-=mid
            for q in v['cubes']:
                q['origin'][1]-=mid
                if 'pivot' in q:q['pivot'][1]-=mid
        a.save(A/f'geo/armaduras/subaru_{part}.geo.json',a.document('subaru_'+part,icon))
        scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
        a.save(A/f'models/item/subaru_{ {"helmet":"cabeca","chestplate":"peito","leggings":"pernas","boots":"botas"}[part] }.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[8,-28,0],'scale':[scale]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3},'fixed':{'scale':[.8]*3}}})
    a.save(A/'animations/armaduras/subaru.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':2,'bones':{}}}})
    print('subaru',sum(len(x['cubes']) for x in b.values()),'cubos,',len(b),'ossos')

if __name__=='__main__':main()
