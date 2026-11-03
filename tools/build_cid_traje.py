"""Cid / Shadow's outfit (style 'shadow') on the Ichigo outfit standard (docs/padrao-trajes.md), user's reference,
no hood and no mask: long black coat closed to the waist and open below, flaring into a tattered hem; high splayed
collar (black outside, purple inside, gold piping) with two black straps hanging from its tips; purple lapels with
gold edges; black inner vest with a zip, chevron seams and a purple gem at the neck; black belt with gold straps
crossing in an X; puffed upper sleeves with a buckled strap, wide purple cuffs with three gold bands, black gloves;
black trousers with dark diamond motifs, black boots with gold bands.

Cloth: the coat tails use the Ichigo cloth bones (bk_capa_* : back 6x6, sides, rounded corners, front flaps) and
the collar straps the bandage-end chains (mg_faixa_*), so RenderTrajeCid drives them with TecidoIchigo exactly like
the Bankai coat. Emissive only on the gem and the collar gold (cid_glow_*). No borrowed art.
"""
import copy,xml.etree.ElementTree as ET
from PIL import Image,ImageDraw
import build_armor_models as a
from armor_reference_shapes import garment as c
from build_ichigo_traje import BK_TOPO,BK_ALTURA,BK_SEGMENTOS,BK_PAINEIS,BK_LADOS,BK_QUINAS
A=a.ASSETS
# Collar straps (mg_faixa_<col>_<seg>): x of each strap at the collar tip, width; 6 short segments.
CD_ALCAS=[(-1.9,.55),(1.9,.55)];CD_ALCA_SEG,CD_ALCA_ALT=6,1.5

def _rasgo(nome,col,seg):
    """Torn hem like the reference: uneven teeth and notches per column, longest on the sides (deterministic)."""
    h=lambda k:((col*37+len(nome)*11+k*53)%10)/9
    base=2.2 if 'lado' in nome or 'quina' in nome else 1.4 if 'frente' in nome else 1.0
    return base+1.6*h(0),base*.5+1.2*h(1),.8+1.4*h(2)

def _caudas(b):
    """Coat tails as cloth (same bones as TecidoIchigo): back 6x6, front flaps, sides and rounded corners, all
    with the torn hem; purple shows only as a thin inner lining strip at the top of the front flaps."""
    def dente(bone,x0,w,top,z,dz,nome,col):
        l1,l2,l3=_rasgo(nome,col,0);y=top-BK_ALTURA
        c(b,bone,[x0,y-l1,z],[w*.34,l1,dz],a.BLACK);c(b,bone,[x0+w*.34,y-l3*.5,z],[w*.3,l3*.5,dz],a.BLACK)
        c(b,bone,[x0+w*.64,y-l2,z],[w*.36,l2,dz],a.BLACK)
    for nome,x0,z,cols,w in BK_PAINEIS:
        # Front flaps wide as in the reference (they flank the legs from the belt down); the bones stay where the
        # cloth expects them, only the panel grows inward.
        if nome!='costas':w,x0=2.9,(2.95 if x0>0 else -2.95)
        for col in range(cols):
            xc=x0+col*w*(1 if nome=='costas' else -1 if nome=='frente_esq' else 1)
            for seg in range(BK_SEGMENTOS):
                top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_{col}_{seg}'
                b[bone]=a.bone(bone,[xc,top,z],'armorBody' if seg==0 else f'bk_capa_{nome}_{col}_{seg-1}')
                zf=z-(0 if z>0 else .35)
                c(b,bone,[xc-w/2,top-BK_ALTURA,zf],[w,BK_ALTURA+.04,.35],a.BLACK)
                if seg in (1,3):c(b,bone,[xc-w/2+.35,top-BK_ALTURA+.3,zf+(.36 if z>0 else -.04)],[.12,BK_ALTURA-.6,.05],a.SHADOW)
                if nome!='costas' and seg<2:c(b,bone,[xc-w/2,top-BK_ALTURA,zf+.36],[w,BK_ALTURA,.06],a.PLUM)   # lining
                if nome!='costas':
                    xi=xc-w/2 if nome=='frente_esq' else xc+w/2-.22   # inner edge of the opening, a lighter fold
                    c(b,bone,[xi,top-BK_ALTURA,zf-.05],[.22,BK_ALTURA+.04,.06],a.SHADOW)
                if seg==BK_SEGMENTOS-1:dente(bone,xc-w/2,w,top,zf,.35,nome,col)
    for nome,x,zc,w in BK_LADOS:
        s_=1 if x>0 else -1
        for seg in range(BK_SEGMENTOS):
            top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_0_{seg}'
            b[bone]=a.bone(bone,[x,top,zc],'armorBody' if seg==0 else f'bk_capa_{nome}_0_{seg-1}')
            xf=x-(0 if s_>0 else .35)
            c(b,bone,[xf,top-BK_ALTURA,zc-w/2],[.35,BK_ALTURA+.04,w],a.BLACK)
            if seg==BK_SEGMENTOS-1:
                l1,l2,l3=_rasgo(nome,0,seg)
                c(b,bone,[xf,top-BK_ALTURA-l1,zc-w/2],[.35,l1,w*.36],a.BLACK);c(b,bone,[xf,top-BK_ALTURA-l3*.4,zc-w*.14],[.35,l3*.4,w*.3],a.BLACK)
                c(b,bone,[xf,top-BK_ALTURA-l2,zc+w*.16],[.35,l2,w*.34],a.BLACK)
    for nome,x,zc,w,ang in BK_QUINAS:
        for seg in range(BK_SEGMENTOS):
            top=BK_TOPO-seg*BK_ALTURA;bone=f'bk_capa_{nome}_0_{seg}'
            b[bone]=a.bone(bone,[x,top,zc],'armorBody' if seg==0 else f'bk_capa_{nome}_0_{seg-1}')
            c(b,bone,[x-w/2,top-BK_ALTURA,zc-.18],[w,BK_ALTURA+.04,.36],a.BLACK,[x,top,zc],[0,ang,0])
            if seg==BK_SEGMENTOS-1:
                l1,_,_=_rasgo(nome,0,seg);c(b,bone,[x-w/2,top-BK_ALTURA-l1,zc-.18],[w*.5,l1,.36],a.BLACK,[x,top,zc],[0,ang,0])

def _torso(b):
    B='armorBody'
    # Coat shell, closed down to the waist; slim (padrao-trajes: no "gordo").
    c(b,B,[-4.12,11.8,-2.12],[8.24,12.2,4.24],a.BLACK)
    # Inner vest: zip down the middle, chevron seams, a little lighter than the coat.
    c(b,B,[-1.9,12.2,-2.22],[3.8,11.0,.12],a.SHADOW)
    c(b,B,[-.07,12.2,-2.3],[.14,11.0,.08],a.INK)
    for y in [21.4,19.6,17.8,16.0,14.2]:
        for s in [-1,1]:c(b,B,[s*.9-.8,y,-2.32],[1.6,.12,.06],a.BLACK,[0,y,-2.3],[0,0,-s*28])
    # Lapels: purple triangles from the shoulders to the chest, gold edge.
    for s in [-1,1]:
        piv=[s*2.9,23.2,-2.3]
        c(b,B,[s*2.9-1.3,18.6,-2.42],[2.6,4.8,.16],a.PLUM,piv,[0,0,s*24])
        c(b,B,[s*2.9-(1.42 if s>0 else -1.16),18.4,-2.5],[.26,5.0,.1],a.GOLD,piv,[0,0,s*24])
        c(b,B,[s*2.9-1.3,23.2,-2.5],[2.6,.22,.1],a.GOLD,piv,[0,0,s*24])
    # Belt with gold straps crossing in an X (two crosses, as in the reference).
    c(b,B,[-4.22,11.4,-2.3],[8.44,2.6,4.6],a.BLACK)
    for xc in [-1.6,1.6]:
        for s in [-1,1]:
            c(b,B,[xc-1.9,12.58,-2.44],[3.8,.3,.1],a.GOLD,[xc,12.7,-2.4],[0,0,s*30])
            c(b,B,[xc-1.9,12.58,2.34],[3.8,.3,.1],a.GOLD,[xc,12.7,2.4],[0,0,s*30])
    for y in [11.4,13.85]:c(b,B,[-4.26,y,-2.36],[8.52,.18,4.72],a.GOLD)
    # Back: seam down the middle under the collar.
    c(b,B,[-.07,13.5,2.12],[.14,9.5,.06],a.SHADOW)

def _gola(b):
    """The collar itself is drawn by RenderTrajeCid as a smooth curved shell (SuperficieTecido.gola, user: no blocks,
    rounded, closed). This bone only places it and lends three palette texels: black, purple, gold (1x1x1 cubes,
    hidden in game). The straps hang from the collar tips by the chin (mg_faixa_*, cloth)."""
    b['cid_gola']=a.bone('cid_gola',[0,23.3,0],'armorBody');G='cid_gola'
    for k,tom in enumerate([a.BLACK,a.PLUM,a.GOLD]):c(b,G,[-.5+k*1.0,23.3,0],[.5,.5,.5],tom)
    # Purple gem on the vest at the throat.
    b['cid_glow_gema']=a.bone('cid_glow_gema',[0,22.6,-2.4],'armorBody')
    c(b,'cid_glow_gema',[-.4,22.2,-2.44],[.8,.8,.12],a.LILAC,[0,22.6,-2.4],[0,0,45])
    c(b,'armorBody',[-.55,22.05,-2.36],[1.1,1.1,.1],a.GOLD,[0,22.6,-2.35],[0,0,45])
    for ci,(x,w) in enumerate(CD_ALCAS):
        for seg in range(CD_ALCA_SEG):
            top=23.2-seg*CD_ALCA_ALT;nome=f'mg_faixa_{ci}_{seg}'
            b[nome]=a.bone(nome,[x,top,-3.7],G if seg==0 else f'mg_faixa_{ci}_{seg-1}')
            c(b,nome,[x-w/2,top-CD_ALCA_ALT,-3.9],[w,CD_ALCA_ALT+.04,.14],a.BLACK)
            if seg==CD_ALCA_SEG-1:c(b,nome,[x-w/2-.08,top-CD_ALCA_ALT-.5,-3.96],[w+.16,.5,.24],a.GOLD)   # buckle tip

def _mangas(b):
    for name,x,s in [('armorLeftArm',3.9,1),('armorRightArm',-8.1,-1)]:
        c(b,name,[x,12,-2.1],[4.2,12,4.2],a.BLACK)
        c(b,name,[x-.25,19.6,-2.35],[4.7,3.9,4.7],a.BLACK)                          # puffed upper sleeve
        c(b,name,[x-.3,19.4,-2.4],[4.8,.35,4.8],a.SHADOW)
        c(b,name,[x-.32,20.8,-2.42],[4.84,.5,4.84],a.INK)                           # strap
        c(b,name,[x+(4.4 if s>0 else -.4),20.6,-.45],[.4,.9,.9],a.GOLD)              # buckle
        c(b,name,[x-.3,13.2,-2.4],[4.8,3.6,4.8],a.PLUM)                             # wide cuff
        for y in [13.6,14.6,15.6]:c(b,name,[x-.34,y,-2.44],[4.88,.24,4.88],a.GOLD)
        c(b,name,[x+.05,11.9,-2.15],[4.1,1.4,4.3],a.BLACK)                           # glove

def _pernas(b):
    for leg,x in [('armorLeftLeg',0),('armorRightLeg',-4)]:
        c(b,leg,[x-.1,1.5,-2.2],[4.2,10.5,4.4],a.BLACK)
        # Diamond motifs on the inner side of each leg, where the open coat shows the trousers.
        dx=.3 if x==0 else 2.7
        for y in [8.6,5.6]:
            c(b,leg,[x+dx,y,-2.28],[1.0,1.0,.08],a.PLUM,[x+dx+.5,y+.5,-2.25],[0,0,45])
            c(b,leg,[x+dx+.28,y-1.1,-2.28],[.44,1.0,.08],a.PLUM)
    for foot,x in [('armorLeftBoot',0),('armorRightBoot',-4)]:
        c(b,foot,[x-.15,0,-2.3],[4.3,4.2,4.6],a.BLACK)
        for y in [2.6,3.6]:c(b,foot,[x-.2,y,-2.35],[4.4,.22,4.7],a.GOLD)
        c(b,foot,[x-.2,-.1,-2.9],[4.4,.5,5.3],a.INK)

def traje():
    b=a.skeleton()
    _torso(b);_gola(b);_mangas(b);_pernas(b);_caudas(b)
    return b

def _glow(b,svg):
    """Isolated texels for the emissive bones (gem and collar gold) so the base cloth never glows."""
    used=set()
    for bone in b.values():
        if bone['name'].startswith('cid_glow_'):continue
        for cube in bone.get('cubes',[]):
            for uv in cube['uv'].values():
                x,y=map(int,uv['uv']);w,h=map(int,uv['uv_size']);used.update((xx,yy) for xx in range(x,x+w) for yy in range(y,y+h))
    patches=[]
    for y in range(123,2,-1):
        for x in range(123,2,-1):
            cells={(xx,yy) for xx in range(x,x+3) for yy in range(y,y+3)}
            if not cells&used:patches.append((x,y));used|=cells
            if len(patches)==2:break
        if len(patches)==2:break
    text=svg.read_text(encoding='utf-8')
    for (x,y),cor in zip(patches,['a465e8','d4a95c']):text=text.replace('</svg>',f'<rect x="{x}" y="{y}" width="3" height="3" fill="#{cor}"/>\n</svg>')
    svg.write_bytes(text.encode('utf-8'))
    for n,bone in b.items():
        if not n.startswith('cid_glow_'):continue
        x,y=patches[1 if n.startswith('cid_glow_gold') else 0]
        for q in bone['cubes']:q['uv']={f:{'uv':[x+1,y+1],'uv_size':[1,1]} for f in ['north','south','east','west','up','down']}
    mask=Image.new('RGBA',(128,128),(0,0,0,0));d=ImageDraw.Draw(mask)
    for (x,y),cor in zip(patches,[(128,63,202,255),(150,110,40,255)]):d.rectangle((x,y,x+2,y+2),fill=cor)
    mask.save(A/'textures/armor/trajes/shadow_glowmask.png');return text

def main():
    from clothing_uv import apply
    from polish_clothing_shapes import polish
    # 'cid' skips polish's legacy shadow cape; the palette and textures stay the 'shadow' style.
    b=apply('shadow',polish('cid',traje()))
    svg=A/'textures/armor/trajes/shadow.svg';text=_glow(b,svg)
    im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
    for r in ET.fromstring(text):
        x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(A/'textures/armor/trajes/shadow.png')
    a.save(A/'geo/armaduras/shadow.geo.json',a.document('shadow',b))
    for part,roots in {'helmet':{'armorHead'},'chestplate':{'armorBody','armorLeftArm','armorRightArm'},'leggings':{'armorLeftLeg','armorRightLeg'},'boots':{'armorLeftBoot','armorRightBoot'}}.items():
        icon=copy.deepcopy(b)
        if part=='helmet':icon['armorHead']['cubes']=copy.deepcopy(icon['cid_gola']['cubes'])   # no hood: the collar is the icon
        def root(n):return root(icon[n]['parent']) if 'parent' in icon[n] else n
        for n,v in icon.items():
            if root(n) not in roots or part=='helmet' and n!='armorHead':v['cubes']=[]
        cubes=[q for v in icon.values() for q in v['cubes']];lo=[min(q['origin'][i] for q in cubes) for i in range(3)];hi=[max(q['origin'][i]+q['size'][i] for q in cubes) for i in range(3)];mid=(lo[1]+hi[1])/2
        for v in icon.values():
            v['pivot'][1]-=mid
            for q in v['cubes']:
                q['origin'][1]-=mid
                if 'pivot' in q:q['pivot'][1]-=mid
        a.save(A/f'geo/armaduras/shadow_{part}.geo.json',a.document('shadow_'+part,icon))
        suffix={'helmet':'capuz','chestplate':'manto','leggings':'calcas','boots':'botas'}[part];scale=round(min(1.3,14/max(hi[i]-lo[i] for i in range(3))),3)
        a.save(A/f'models/item/{suffix}_shadow.json',{'parent':'builtin/entity','display':{'gui':{'rotation':[8,-28,0],'scale':[scale]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3},'fixed':{'scale':[.8]*3}}})
    a.save(A/'animations/armaduras/shadow.animation.json',{'format_version':'1.8.0','animations':{'animation.traje.idle':{'loop':True,'animation_length':2,'bones':{}}}})
    print('shadow',sum(len(x['cubes']) for x in b.values()),'cubos,',len(b),'ossos')

if __name__=='__main__':main()
