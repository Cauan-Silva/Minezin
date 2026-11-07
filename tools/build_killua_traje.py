"""Killua Zoldyck's Hunter Exam outfit (style 'killua', HxH 2011, user's labeled reference): loose lavender-white V-neck
tee over a navy long-sleeve turtleneck (navy collar, V and sleeves to the wrists); slate-blue shorts to the knee (shins
bare: the skin shows); purple high-top boots with white fur cuff, white toe cap and sole. No hair: the
player's skin keeps its own (Padrao de Auras / padrao-trajes). Fitted, no cloth. No borrowed art.
"""
import copy
from PIL import Image,ImageDraw
import xml.etree.ElementTree as ET
import build_armor_models as a
from armor_reference_shapes import garment as c
A=a.ASSETS
CAMISA,CAMISA_SOM,MARINHO,MARINHO_ESC,SHORT,SHORT_ESC,BOTA,BOTA_ESC,PELO,SOLA=a.WHITE,a.LILAC,a.NAVY,a.INK,a.BROWN,a.LEATHER,a.PLUM,a.INDIGO,a.IVORY,a.STEEL

def _torso(b):
    B='armorBody'
    c(b,B,[-4.12,12.4,-2.12],[8.24,11.6,4.24],CAMISA)                         # loose lavender-white V-neck tee to the hips
    c(b,B,[-4.2,12.4,-2.2],[8.4,.4,4.4],CAMISA_SOM)                           # tee hem
    c(b,B,[-1.2,20.6,-2.2],[2.4,1.8,.1],MARINHO)                              # navy V of the undershirt at the chest
    c(b,B,[-.7,19.8,-2.2],[1.4,.9,.1],MARINHO)                                # bottom tip of the V
    c(b,B,[-4.3,22.2,-2.3],[8.6,1.9,4.6],MARINHO)                             # navy turtleneck collar
    c(b,B,[-4.32,22.2,-2.32],[8.64,.3,4.64],MARINHO_ESC)                      # collar fold
    for s in [-1,1]:c(b,B,[s*2.9-.15,13.0,-2.18],[.3,6.5,.08],CAMISA_SOM)     # loose folds of the tee

def _mangas(b):
    # polish() widens the main sleeve to 4.7: the navy long sleeve is the main cube, the tee's short sleeve sits outside
    for name,x,s in [('armorLeftArm',3.9,1),('armorRightArm',-8.1,-1)]:
        c(b,name,[x,13.0,-2.1],[4.2,11.0,4.2],MARINHO)                        # navy long sleeve to the wrist
        cx=3.6 if s>0 else -8.4
        c(b,name,[cx,19.4,-2.4],[4.8,5.0,4.8],CAMISA)                         # tee short sleeve
        c(b,name,[cx-.02,19.4,-2.42],[4.84,.35,4.84],CAMISA_SOM)              # short-sleeve hem
        c(b,name,[cx+.1,12.9,-2.36],[4.6,.6,4.72],MARINHO_ESC)                # cuff

def _pernas(b):
    for leg,x,s in [('armorLeftLeg',0,1),('armorRightLeg',-4,-1)]:
        c(b,leg,[x-.1,7.4,-2.2],[4.2,4.7,4.4],SHORT)                         # slate-blue shorts above the knee
        c(b,leg,[x-.15,7.4,-2.25],[4.3,.5,4.5],SHORT_ESC)                    # hem
        fora=x+4.1 if s>0 else x-.22
        c(b,leg,[fora,8.0,-.6],[.12,3.6,1.2],SHORT_ESC)                      # pocket seam
    for foot,x in [('armorLeftBoot',0),('armorRightBoot',-4)]:
        c(b,foot,[x-.15,.6,-2.3],[4.3,3.4,4.6],BOTA)                          # purple high-top boot
        c(b,foot,[x-.25,3.6,-2.4],[4.5,1.0,4.8],PELO)                         # white fur cuff at the top
        c(b,foot,[x-.2,0,-2.6],[4.4,.7,4.95],SOLA)                            # white sole
        c(b,foot,[x-.18,.6,-2.62],[4.36,1.2,.45],SOLA)                        # white toe cap
        c(b,foot,[x+.6,1.9,-2.42],[2.8,1.2,.14],BOTA_ESC)                     # darker toe panel

def traje():
    b=a.skeleton()
    _torso(b);_mangas(b);_pernas(b)
    return b

def main():
    from clothing_uv import apply
    from polish_clothing_shapes import polish
    b=apply('killua',polish('killua',traje()))
    svg=A/'textures/armor/trajes/killua.svg';text=svg.read_text(encoding='utf-8')
    im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
    for r in ET.fromstring(text):
        x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(A/'textures/armor/trajes/killua.png')
    a.save(A/'geo/armaduras/killua.geo.json',a.document('killua',b))
    for part,roots in {'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}.items():
        icon=copy.deepcopy(b)
        def root(n):return root(icon[n]['parent']) if 'parent' in icon[n] else n
        if part=='helmet':   # no hat: the rolled collar is the icon of the first piece
            icon['armorHead']['cubes']=[q for q in copy.deepcopy(icon['armorBody']['cubes']) if q['origin'][1]>=22.2]
        for n,v in icon.items():
            if root(n) not in roots or part=='helmet' and n!='armorHead':v['cubes']=[]
        cubes=[q for v in icon.values() for q in v['cubes']];lo=[min(q['origin'][i] for q in cubes) for i in range(3)];hi=[max(q['origin'][i]+q['size'][i] for q in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
        for v in icon.values():
            v['pivot'][1]-=mid
            for q in v['cubes']:
                q['origin'][1]-=mid
                if 'pivot' in q:q['pivot'][1]-=mid
        a.save(A/f'geo/armaduras/killua_{part}.geo.json',a.document('killua_'+part,icon))
        scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
        a.save(A/f'models/item/killua_{ {"helmet":"cabeca","chestplate":"peito","leggings":"pernas","boots":"botas"}[part] }.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[20,-35,0],'translation':[0,0,0],'scale':[.72]*3},'ground':{'translation':[0,3,0],'scale':[.35]*3},'fixed':{'scale':[.7]*3},'thirdperson_righthand':{'translation':[0,2,1],'scale':[.35]*3},'firstperson_righthand':{'translation':[0,3,0],'scale':[.4]*3}}})
    a.save(A/'animations/armaduras/killua.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':2,'bones':{}}}})
    print('killua',sum(len(x['cubes']) for x in b.values()),'cubos,',len(b),'ossos')

if __name__=='__main__':main()
