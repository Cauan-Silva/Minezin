"""Ichigo's outfits as editable cuboids on the clothing_uv pipeline (one texel per unit, vector pixel art).

Base shihakusho (style 'ichigo', the armor set, user's reference): black kosode with wide white lapels in a V, Zangetsu's
red-brown strap with a buckle, white obi with a front knot, wide rectangular sleeves with a white hem, wide hakama.
Bankai coat (style 'ichigo_bankai', drawn by CamadaHollow/RenderBankaiIchigo while the Bankai is visible): long open
black coat with a white edge down the front, wide sleeves with a torn hem, and coat tails as cloth (TecidoIchigo): a back panel of BK_COLUNAS x BK_SEGMENTOS bones and two open front panels, the hem
ragged like the anime's (each column ends in uneven tatters). No borrowed art.
"""
from pathlib import Path
import copy,json
import build_armor_models as a
from armor_reference_shapes import garment as c
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/arcanecontrol'

# Coat tails shared with TecidoIchigo (Java): panel, x of column 0, hanging plane z, columns, column width.
BK_TOPO,BK_ALTURA,BK_SEGMENTOS=12.2,1.85,6
# Front: open coat, only one narrow strip per side (user: 'na frente ele nao tem uma saia').
BK_PAINEIS=[('costas',-3.65,2.45,6,1.46),('frente_esq',3.6,-2.5,1,1.5),('frente_dir',-3.6,-2.5,1,1.5)]
# Side panels close the coat at the hips (user: 'faltando na lateral'): panel, x of the hanging plane, z centre, width.
BK_LADOS=[('lado_esq',4.35,-.45,4.4),('lado_dir',-4.35,-.45,4.4)]
# Rounded hip corner between back and side (user: 'arredondar'): a 45-degree strip that follows the back cloth too.
BK_QUINAS=[('quina_esq',3.95,2.05,1.5,45),('quina_dir',-3.95,2.05,1.5,-45),
           ('quinaf_esq',4.15,-2.3,1.1,-45),('quinaf_dir',-4.15,-2.3,1.1,45)]  # front corners: front strip <-> side
# Tatter lengths per (panel, column): two teeth of uneven length each (the anime's ragged hem), deterministic.
RASGOS={(n,c):(1.0+1.8*((c*37+len(n)*11)%10)/9,1.0+1.8*((c*53+len(n)*7+4)%10)/9) for n,_,_,cols,_ in BK_PAINEIS for c in range(cols)}
# Bankai sleeves as cloth (TecidoIchigo): chains of BK_MANGA bones hanging from the shoulder seam.
BK_MANGA,BK_MANGA_TOPO,BK_MANGA_ALTURA=3,23.4,3.55

def _kosode(b):
    """Black kosode with wide white lapels crossing in a V down to the obi, a few creases (user's reference)."""
    c(b,'armorBody',[-4.12,11.6,-2.12],[8.24,12.4,4.24],a.BLACK)
    for side in [-1,1]:
        # Lapel: white band from the side of the neck to the obi, the right one crossing over the left.
        c(b,'armorBody',[side*2.0-.6,12.9,-2.27],[1.2,11.1,.15],a.IVORY,[side*2.0,24,-2.25],[0,0,side*-8])
        c(b,'armorBody',[side*1.05-.6,23.6,2.1],[1.2,.6,.15],a.IVORY)
    for x,y,h,r in [(-3.3,14,6,6),(2.6,15,5,-5),(-1.2,18.5,3.5,40),(3.1,19,3,-35)]:
        c(b,'armorBody',[x,y,-2.2],[.22,h,.1],a.SHADOW,[x,y+h,-2.18],[0,0,r])
    c(b,'armorBody',[-1.4,23.7,2.13],[2.8,.4,.08],a.SHADOW)

def _obi(b):
    c(b,'armorBody',[-4.28,10.9,-2.3],[8.56,2.0,4.6],a.IVORY)
    b['obi_no']=a.bone('obi_no',[0,11.9,-2.6],'armorBody')
    c(b,'obi_no',[-.7,10.95,-2.7],[1.4,1.8,.42],a.IVORY)                                  # knot
    for side in [-1,1]:
        c(b,'obi_no',[side*1.4-.8,11.2,-2.62],[1.6,1.3,.3],a.IVORY,[side*.7,11.9,-2.6],[0,0,side*-20])   # loops
        c(b,'obi_no',[side*.5-.35,7.8,-2.58],[.7,3.2,.2],a.IVORY,[side*.5,10.9,-2.55],[0,0,side*8])       # hanging ends

def _correia(b):
    """Zangetsu's red-brown strap from the right shoulder to the left hip, with a square buckle on the chest."""
    b['correia']=a.bone('correia',[0,18,0],'armorBody')
    c(b,'correia',[-.7,11.6,-2.45],[1.4,14.2,.25],a.LEATHER,[0,18,-2.35],[0,0,-29])
    c(b,'correia',[-.7,11.6,2.2],[1.4,14.2,.25],a.LEATHER,[0,18,2.35],[0,0,29])
    c(b,'correia',[-1.35,18.4,-2.62],[1.7,1.7,.25],a.STEEL,[-.5,19.25,-2.55],[0,0,-29])
    c(b,'correia',[-.95,18.8,-2.68],[.9,.9,.12],a.LEATHER,[-.5,19.25,-2.55],[0,0,-29])

def _mangas(b,barra):
    """Kimono sleeves: the arm is covered to the shoulder, the wide rectangular sleeve hangs from it (manto_ bones
    sway in the idle loop) and stops above the hand; white hem on the base outfit, torn hem on the Bankai."""
    for name,x,side in [('armorLeftArm',3.9,1),('armorRightArm',-8.3,-1)]:
        c(b,name,[x,13.2,-2.15],[4.3,11,4.3],a.BLACK)
        m=f'manto_manga_{"esq" if side>0 else "dir"}';b[m]=a.bone(m,[side*6,22,0],name)
        xs=x-.3
        c(b,m,[xs,12.8,-2.5],[5.0,10.6,5.0],a.BLACK)
        for k,zz in enumerate([-1.3,.7]):c(b,m,[xs+(4.9 if side>0 else -.02),14+k*1.5,zz],[.12,7,.1],a.SHADOW)
        c(b,m,[xs+1.5,15.2,-2.55],[.1,6.5,.1],a.SHADOW)
        if barra:c(b,m,[xs-.03,12.5,-2.53],[5.06,.55,5.06],a.IVORY)
        else:
            for k in range(3):c(b,m,[xs+k*1.67,11.8-k%2*.5,-2.5],[1.67,1.1+k%2*.5,5.0],a.BLACK)

def _mangas_tecido(b):
    """Bankai sleeves: arm covered to the shoulder, the wide sleeve split into BK_MANGA chained bones (bk_manga_*)
    so it swings with the arm; torn hem on the last segment."""
    for name,x,side in [('armorLeftArm',3.9,1),('armorRightArm',-8.3,-1)]:
        c(b,name,[x,13.2,-2.15],[4.3,11,4.3],a.BLACK)
        lado='esq' if side>0 else 'dir';xs=x-.3;pai=name
        for k in range(BK_MANGA):
            top=BK_MANGA_TOPO-k*BK_MANGA_ALTURA;nome=f'bk_manga_{lado}_{k}';b[nome]=a.bone(nome,[x+2.15,top,0],pai);pai=nome
            c(b,nome,[xs,top-BK_MANGA_ALTURA,-2.5],[5.0,BK_MANGA_ALTURA+.05,5.0],a.BLACK)
            if k==1:c(b,nome,[xs+(4.9 if side>0 else -.02),top-BK_MANGA_ALTURA+.3,-1.2],[.12,BK_MANGA_ALTURA-.6,.1],a.SHADOW)
            if k==BK_MANGA-1:
                for j in range(3):c(b,nome,[xs+j*1.67,top-BK_MANGA_ALTURA-1.1-j%2*.5,-2.5],[1.67,1.1+j%2*.5,5.0],a.BLACK)

def _hakama(b):
    for leg,x in [('armorLeftLeg',0),('armorRightLeg',-4)]:
        c(b,leg,[x-.3,2.2,-2.35],[4.6,10,4.7],a.BLACK)
        for k in range(3):c(b,leg,[x+.5+k*1.2,2.6,-2.42],[.22,9,.08],a.SHADOW)

def _sandalias(b):
    for foot,x in [('armorLeftBoot',0),('armorRightBoot',-4)]:
        c(b,foot,[x-.1,.3,-2.25],[4.2,2.0,4.5],a.IVORY)          # tabi
        c(b,foot,[x-.3,-.25,-2.85],[4.6,.55,5.6],a.BROWN)         # straw sole
        for side in [-1,1]:c(b,foot,[x+1.85+side*1.05-.15,.3,-2.6],[.3,1.4,.25],a.BLACK,[x+2,1.7,-2.5],[0,0,side*-30])

def shihakusho():
    b=a.skeleton()
    c(b,'armorHead',[-2.2,24,-2.4],[4.4,.6,4.8],a.BLACK)
    _kosode(b);_obi(b);_correia(b);_mangas(b,True);_hakama(b);_sandalias(b)
    return b

def _caudas(b,borda_branca):
    """Coat tails / skirt as cloth (TecidoIchigo bones bk_capa_*): back 6x6, front strips, sides and rounded corners."""
    for nome,x0,z,cols,w in BK_PAINEIS:
        for col in range(cols):
            xc=x0+col*w*(1 if nome=='costas' else -1 if nome=='frente_esq' else 1)
            for seg in range(BK_SEGMENTOS):
                top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_{col}_{seg}'
                b[bone]=a.bone(bone,[xc,top,z],'armorBody' if seg==0 else f'bk_capa_{nome}_{col}_{seg-1}')
                zf=z-(0 if z>0 else .35)
                c(b,bone,[xc-w/2,top-BK_ALTURA,zf],[w,BK_ALTURA,.35],a.BLACK)
                if seg==1:c(b,bone,[xc-w/2+.3,top-BK_ALTURA+.4,zf+(.46 if z>0 else -.04)],[.15,BK_ALTURA-.8,.06],a.SHADOW)
                if seg==BK_SEGMENTOS-1:
                    l1,l2=RASGOS[(nome,col)]
                    c(b,bone,[xc-w/2,top-BK_ALTURA-l1,zf],[w*.48,l1,.35],a.BLACK)
                    c(b,bone,[xc,top-BK_ALTURA-l2,zf],[w*.5,l2,.35],a.BLACK)
    for nome,x,zc,w in BK_LADOS:
        s_=1 if x>0 else -1
        for seg in range(BK_SEGMENTOS):
            top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_0_{seg}'
            b[bone]=a.bone(bone,[x,top,zc],'armorBody' if seg==0 else f'bk_capa_{nome}_0_{seg-1}')
            xf=x-(0 if s_>0 else .35)
            c(b,bone,[xf,top-BK_ALTURA,zc-w/2],[.35,BK_ALTURA,w],a.BLACK)
            if seg==BK_SEGMENTOS-1:
                c(b,bone,[xf,top-BK_ALTURA-2.0,zc-w/2],[.35,2.0,w*.5],a.BLACK);c(b,bone,[xf,top-BK_ALTURA-1.0,zc],[.35,1.0,w*.5],a.BLACK)
    for nome,x,zc,w,ang in BK_QUINAS:
        for seg in range(BK_SEGMENTOS):
            top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_0_{seg}'
            b[bone]=a.bone(bone,[x,top,zc],'armorBody' if seg==0 else f'bk_capa_{nome}_0_{seg-1}')
            c(b,bone,[x-w/2,top-BK_ALTURA,zc-.18],[w,BK_ALTURA,.36],a.BLACK,[x,top,zc],[0,ang,0])
            if seg==BK_SEGMENTOS-1:c(b,bone,[x-w/2,top-BK_ALTURA-1.6,zc-.18],[w*.55,1.6,.36],a.BLACK,[x,top,zc],[0,ang,0])
    if not borda_branca:return
    for nome,x0,z,cols,w in BK_PAINEIS:
        for col in range(cols):
            xc=x0+col*w*(1 if nome=='costas' else -1 if nome=='frente_esq' else 1)
            if nome!='costas' and col==0:
                for seg in range(BK_SEGMENTOS):
                    top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_0_{seg}';xi=xc+(w/2 if nome=='frente_dir' else -w/2)
                    c(b,bone,[xi-.15,top-BK_ALTURA,z-.42],[.3,BK_ALTURA,.15],a.WHITE)

def bankai():
    """Long open Bankai coat over the kosode: white edge down the front opening, wide sleeves with a torn hem,
    tails as cloth with the anime's torn hem (user's reference: no red lining, no high collar)."""
    b=a.skeleton()
    c(b,'armorHead',[-2.2,24,-2.4],[4.4,.6,4.8],a.BLACK)
    _kosode(b);_obi(b);_mangas_tecido(b);_hakama(b);_sandalias(b)
    # Coat shell: back and sides; the front stays open over the kosode, a white edge runs down each side.
    c(b,'armorBody',[-4.35,11.6,2.0],[8.7,12.6,.4],a.BLACK)
    for x in [-4.35,3.95]:c(b,'armorBody',[x,11.6,-2.35],[.4,12.6,4.4],a.BLACK)
    for side in [-1,1]:
        x0=-4.35 if side<0 else 2.55
        c(b,'armorBody',[x0,11.6,-2.45],[1.8,12.6,.35],a.BLACK)
        xe=-2.6 if side<0 else 2.3;c(b,'armorBody',[xe,11.6,-2.52],[.3,12.6,.15],a.WHITE)
    c(b,'armorBody',[-4.3,23.6,2.0],[8.6,1.2,.4],a.BLACK)
    _caudas(b,True)
    return b

# Mugetsu form (user's reference, no hair by request): grey bandage over nose and mouth knotted at the back of the head
# with two loose ends as cloth (mg_faixa_*), bare grey torso wrapped in layered dark bandages of varying width crossing
# front and back, the arm on the viewer's right in skin with black bands, the other wrapped grey, long wide black
# hakama with a torn hem (coat tail cloth bones + closed front), feet wrapped criss-cross with bare toes.
MG_FAIXAS=[(-.7,.9),(.6,.8)];MG_SEGMENTOS,MG_ALTURA=6,1.9

def _faixa(b,osso,y,r,z0,z1,x0,x1,larg,tom,pivx=0):
    """One bandage turn around a box part: front and back bands rotated by r, plain bands on the sides, raised a hair."""
    for z,sg in [(z0,1),(z1,-1)]:c(b,osso,[x0,y,z],[x1-x0,larg,.12],tom,[pivx,y+larg/2,z],[0,0,r*sg])
    for x in [x0-.06,x1-.06]:c(b,osso,[x,y-.1,z0],[.12,larg,z1-z0+.12],tom)

def mugetsu():
    b=a.skeleton();h='armorHead'
    # Face bandage in three overlapping turns, knot at the back, two loose ends hanging as cloth.
    for k,(y,hh,tom) in enumerate([(24.2,1.2,a.STEEL),(25.2,1.1,a.WHITE),(26.1,1.0,a.STEEL)]):
        c(b,h,[-4.25,y,-4.42-k*.03],[8.5,hh,.3],tom);c(b,h,[-4.25,y,3.95],[8.5,hh,.3],tom)
        for x in [-4.38,4.08]:c(b,h,[x,y,-4.3],[.3,hh,8.4],tom)
        c(b,h,[-4.25,y+hh-.08,-4.5-k*.03],[8.5,.1,.08],a.INK)
    c(b,h,[-.8,25.0,4.2],[1.6,1.4,.6],a.STEEL);c(b,h,[-.5,25.3,4.75],[1.0,.8,.25],a.INK)          # knot
    for ci,(x,w) in enumerate(MG_FAIXAS):
        for seg in range(MG_SEGMENTOS):
            top=25.2-seg*MG_ALTURA;nome=f'mg_faixa_{ci}_{seg}'
            b[nome]=a.bone(nome,[x,top,4.5],h if seg==0 else f'mg_faixa_{ci}_{seg-1}')
            c(b,nome,[x-w/2,top-MG_ALTURA,4.4],[w*(1-.05*seg),MG_ALTURA+.05,.15],a.STEEL if seg%2==0 else a.WHITE)
            if seg==MG_SEGMENTOS-1:c(b,nome,[x-w/4,top-MG_ALTURA-.7,4.4],[w/2,.7,.15],a.STEEL)
    # Bare grey torso, bandages layered: wide dark turns, thin light ones overlapping, a loose end over the hip.
    c(b,'armorBody',[-4.06,12,-2.06],[8.12,12,4.12],a.STEEL)
    for y,r,larg,tom in [(22.6,-18,.55,a.INK),(21.7,8,.35,a.SHADOW),(20.6,14,.6,a.INK),(19.6,-6,.3,a.SHADOW),(18.4,-12,.55,a.INK),
                         (17.4,10,.3,a.SHADOW),(16.2,16,.6,a.INK),(15.0,-8,.35,a.SHADOW),(13.8,-14,.55,a.INK),(12.6,6,.4,a.SHADOW)]:
        _faixa(b,'armorBody',y,r,-2.16,2.04,-4.12,4.12,larg,tom)
    c(b,'armorBody',[2.6,11.0,-2.24],[.6,2.4,.1],a.INK,[2.9,13.4,-2.2],[0,0,-12])               # loose end
    # Arms: viewer's right in skin with black bands of varying width; the other grey with layered wraps.
    for name,x,pele in [('armorRightArm',-8.08,True),('armorLeftArm',3.92,False)]:
        c(b,name,[x,12,-2.08],[4.16,12,4.16],a.LEATHER if pele else a.STEEL)
        faixas=[(22.8,-16,.5),(21.4,12,.3),(20.0,-10,.55),(18.4,18,.35),(16.8,-14,.5),(15.2,10,.3),(13.6,-12,.5),(12.4,8,.35)]
        for i,(y,r,larg) in enumerate(faixas):
            tom=a.BLACK if pele else (a.INK if i%2==0 else a.SHADOW)
            # polish() widens the arm to 4.7 (shoulder cap): bands sit just outside it so they stay visible.
            xa=-8.4 if name=='armorRightArm' else 3.6;_faixa(b,name,y,r,-2.47,2.35,xa,xa+4.8,larg,tom,pivx=xa+2.4)
    # Long wide hakama: waist wrap, legs plus the cloth skirt (coat tail bones, closed front) with a torn hem.
    for leg,x in [('armorLeftLeg',0),('armorRightLeg',-4)]:c(b,leg,[x-.35,1.2,-2.4],[4.7,10.8,4.8],a.BLACK)
    c(b,'armorBody',[-4.3,11.0,-2.4],[8.6,1.6,4.8],a.BLACK);c(b,'armorBody',[-4.35,12.2,-2.45],[8.7,.4,4.9],a.SHADOW)
    _caudas(b,False)
    for nome,x0 in [('frente_meio',0.0)]:
        for seg in range(BK_SEGMENTOS):
            top=BK_TOPO-seg*BK_ALTURA;bone=f'mg_saia_{nome}_{seg}';b[bone]=a.bone(bone,[x0,top,-2.5],'armorBody' if seg==0 else f'mg_saia_{nome}_{seg-1}')
            # Wide enough to overlap the front strips (x +-2.85..4.35): no gap between front and sides.
            c(b,bone,[x0-3.0,top-BK_ALTURA,-2.85],[6.0,BK_ALTURA,.35],a.BLACK)
            if seg==BK_SEGMENTOS-1:c(b,bone,[x0-3.0,top-BK_ALTURA-1.8,-2.85],[2.9,1.8,.35],a.BLACK);c(b,bone,[x0+.1,top-BK_ALTURA-1.0,-2.85],[2.8,1.0,.35],a.BLACK)
    # Feet: grey wraps crossing in an X over the instep, bare toes.
    for foot,x in [('armorLeftBoot',0),('armorRightBoot',-4)]:
        c(b,foot,[x-.1,.6,-2.2],[4.2,2.6,4.4],a.STEEL)
        for y in [.9,2.7]:c(b,foot,[x-.14,y,-2.24],[4.28,.3,4.48],a.INK)
        for sg in [-1,1]:c(b,foot,[x+.3,1.2,-2.3],[3.6,.35,.1],a.SHADOW,[x+2.1,1.4,-2.3],[0,0,sg*28])
        c(b,foot,[x+.2,0,-2.7],[3.6,.7,1.2],a.LEATHER)
    return b

def _exportar(style,bones):
    from clothing_uv import apply
    import xml.etree.ElementTree as ET
    from PIL import Image,ImageDraw
    from polish_clothing_shapes import polish
    bones=apply(style,polish(style,bones));a.save(A/f'geo/armaduras/{style}.geo.json',a.document(style,bones))
    svg=A/f'textures/armor/trajes/{style}.svg';im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
    for r in ET.fromstring(svg.read_text(encoding='utf-8')):
        x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(A/f'textures/armor/trajes/{style}.png');return bones

def main():
    bones=_exportar('ichigo',shihakusho())
    subsets={'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}
    for part,roots in subsets.items():
        icon=copy.deepcopy(bones)
        def root(x):return root(icon[x['parent']]) if x.get('parent') else x['name']
        for x in icon.values():
            if root(x) not in roots:x['cubes']=[]
        cubes=[q for x in icon.values() for q in x['cubes']];lo=[min(q['origin'][i] for q in cubes) for i in range(3)];hi=[max(q['origin'][i]+q['size'][i] for q in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
        for x in icon.values():
            x['pivot'][1]-=mid
            for q in x['cubes']:
                q['origin'][1]-=mid
                if 'pivot' in q:q['pivot'][1]-=mid
        a.save(A/f'geo/armaduras/ichigo_{part}.geo.json',a.document('ichigo_'+part,icon))
        suffix={'helmet':'cabeca','chestplate':'peito','leggings':'pernas','boots':'botas'}[part];scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
        a.save(A/f'models/item/ichigo_{suffix}.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[20,-35,0],'translation':[0,0,0],'scale':[.72]*3},'ground':{'translation':[0,3,0],'scale':[.35]*3},'fixed':{'scale':[.7]*3},'thirdperson_righthand':{'translation':[0,2,1],'scale':[.35]*3},'firstperson_righthand':{'translation':[0,3,0],'scale':[.4]*3}}})
    anim={n:{'rotation':{'0':[0,0,0],'2':[3,0,1.5],'4':[0,0,0]}} for n in bones if n.startswith('manto_')}
    a.save(A/'animations/armaduras/ichigo.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':anim}}})
    bk=_exportar('ichigo_bankai',bankai())
    mg=_exportar('ichigo_mugetsu',mugetsu())
    a.save(A/'animations/armaduras/ichigo_mugetsu.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':{}}}})
    print('ichigo_mugetsu',sum(len(x['cubes']) for x in mg.values()),'cubos,',len(mg),'ossos')
    a.save(A/'animations/armaduras/ichigo_bankai.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':4,'bones':{}}}})
    print('ichigo',sum(len(x['cubes']) for x in bones.values()),'cubos; ichigo_bankai',sum(len(x['cubes']) for x in bk.values()),'cubos,',len(bk),'ossos')

if __name__=='__main__':main()
