"""Reference-led clothing. All parts remain editable cuboids; existing clean atlas is unchanged."""
import math
import build_armor_models as a

def garment(b,name,o,s,t,pivot=None,rotation=None):
    a.cube(b[name],o,s,t,pivot,rotation)

def limb_legs(b,pants,boot,height=4,hem=None):
    for leg,foot,x in [('armorLeftLeg','armorLeftBoot',0),('armorRightLeg','armorRightBoot',-4)]:
        garment(b,leg,[x-.12,height,-2.14],[4.24,12-height,4.28],pants)
        garment(b,foot,[x-.18,.35,-2.3],[4.36,height,4.6],boot)
        garment(b,foot,[x-.22,-.12,-2.85],[4.44,1.25,5.45],boot)
        garment(b,foot,[x-.25,-.24,-2.89],[4.5,.35,5.5],a.BLACK)
        if hem is not None:garment(b,foot,[x-.24,height-.1,-2.4],[4.48,.65,4.8],hem)

def gojo():
    b=a.skeleton();c=garment
    # Blindfold only: the player's own face and hair remain intact.
    c(b,'armorHead',[-4.24,27,-4.31],[8.48,2.4,.32],a.BLACK)
    for x in [-4.28,3.96]:c(b,'armorHead',[x,27,-4],[.32,2.3,8.2],a.BLACK)
    c(b,'armorHead',[-4.24,27,3.95],[8.48,2.3,.3],a.BLACK)
    c(b,'armorHead',[-3.7,26.55,-4.33],[1,.5,.3],a.BLACK,[0,28,-4],[0,0,-8])
    c(b,'armorBody',[-4.18,12,-2.16],[8.36,11.7,4.32],a.NAVY)
    # Standing collar, lower at the front to leave the chin readable.
    c(b,'armorBody',[-4.3,22.4,1.92],[8.6,3.4,.55],a.NAVY)
    for x in [-4.3,3.8]:c(b,'armorBody',[x,22.4,-2.3],[.5,3.0,4.5],a.NAVY)
    c(b,'armorBody',[-4.05,22.4,-2.43],[8.1,2.1,.45],a.NAVY)
    c(b,'armorBody',[-.1,12,-2.36],[.17,12.5,.12],a.NAVY)
    c(b,'armorBody',[-3.0,20.45,-2.42],[3.05,.14,.14],a.NAVY,[-.05,22,-2.4],[0,0,-20])
    # One discreet brass button on the asymmetric lapel, not a row of buttons.
    b['botao_uniforme']=a.bone('botao_uniforme',[-2.8,21,-2.5],'armorBody')
    c(b,'botao_uniforme',[-3.13,20.68,-2.58],[.66,.66,.23],a.GOLD,[-2.8,21,-2.5],[0,0,22.5])
    c(b,'armorBody',[-3.7,16.7,2.18],[4.7,.16,.12],a.NAVY,[-1.3,16.8,2.2],[0,0,-10])
    for name,x in [('armorLeftArm',3.9),('armorRightArm',-8.25)]:
        c(b,name,[x,13,-2.17],[4.35,10.8,4.34],a.NAVY)
        c(b,name,[x-.04,12.85,-2.22],[4.43,.45,4.44],a.NAVY)
        c(b,name,[x+.4,18.1,-2.25],[2.8,.14,.12],a.NAVY,[x+1.8,18.1,-2.25],[0,0,-12])
    limb_legs(b,a.NAVY,a.BLACK,2.6)
    return b

def frieren():
    b=a.skeleton();c=garment
    for side,name in [(-1,'brinco_esq'),(1,'brinco_dir')]:
        b[name]=a.bone(name,[side*4.22,27,0],'armorHead')
        c(b,name,[side*4.22-.12,25.4,-.5],[.24,1.7,.28],a.GOLD)
        c(b,name,[side*4.22-.22,24.8,-.63],[.44,.8,.4],a.RUBY)
    # Anime reference: white dress, striped shirt only at the neckline, short gold-trimmed capelet,
    # dark belt, A-line skirt with gold hem, wide sleeves with gold cuffs, dark tights and brown boots.
    c(b,'armorBody',[-4.15,12,-2.13],[8.3,11.8,4.26],a.WHITE)
    c(b,'armorBody',[-1.5,19.4,-2.37],[3.0,3.4,.3],a.INK)
    for y in [19.9,21.3]:c(b,'armorBody',[-1.4,y,-2.56],[2.8,.65,.16],a.IVORY)
    c(b,'armorBody',[-4.36,11.6,-2.36],[8.72,1.2,4.72],a.NAVY)
    c(b,'armorBody',[-2.7,23.0,-2.7],[5.4,1.6,5.4],a.WHITE)
    c(b,'armorBody',[-2.75,24.3,-2.75],[5.5,.35,5.5],a.GOLD)
    # Capelet yoke over the shoulders; its lower edge is cloth (frieren_capa_*), simulated in Java.
    c(b,'armorBody',[-6.4,21.6,-2.75],[12.8,2.4,5.5],a.WHITE)
    capelet_and_skirt(b)
    for name,x in [('armorLeftArm',3.95),('armorRightArm',-8.25)]:
        c(b,name,[x,15.6,-2.2],[4.3,8.0,4.4],a.WHITE)
        # Wide bell cuff with the gold band at the opening.
        c(b,name,[x-.35,13.0,-2.55],[5.0,2.8,5.1],a.WHITE)
        c(b,name,[x-.42,12.75,-2.62],[5.14,.65,5.24],a.GOLD)
    limb_legs(b,a.NAVY,a.BROWN,5.2,a.LEATHER)
    return b

# Cloth panels for Frieren. Names, rows and columns must match TecidoFrieren.
CAPA={'front':4,'back':4,'left':2,'right':2};SAIA={'front':3,'back':3,'left':2,'right':2}
CAPA_LINHAS,SAIA_LINHAS=2,3
def capelet_and_skirt(b):
    def panel(prefix,face,col,row,top,height,half_w,half_d,cols,flare,gold):
        name=f'frieren_{prefix}_{face}_{col}_{row}'
        parent='armorBody' if row==0 else f'frieren_{prefix}_{face}_{col}_{row-1}'
        w=(half_w+flare*row)*2;d=(half_d+flare*row)*2;bottom=top-height
        if face in ['front','back']:
            span=w/cols;x=-w/2+col*span
            if prefix=='capa':
                # Front of the capelet stays open over the striped shirt.
                if face=='front':
                    side=-1 if col<2 else 1;span=(w/2-1.25)/2;x=-w/2+col*span if col<2 else 1.25+(col-2)*span
            z=-(half_d+flare*row)-.32 if face=='front' else half_d+flare*row
            origin=[x,bottom,z];size=[span,height,.32];pivot=[x+span/2,top,z+.16]
        else:
            span=d/cols;z=-d/2+col*span
            x=-(half_w+flare*row)-.32 if face=='left' else half_w+flare*row
            origin=[x,bottom,z];size=[.32,height,span];pivot=[x+.16,top,z+span/2]
        b[name]=a.bone(name,pivot,parent)
        garment(b,name,origin,size,a.WHITE)
        if gold:
            g=list(origin);gs=list(size);gs[1]=.55;g[1]=bottom-.05
            if face in ['front','back']:g[2]-=.04 if face=='front' else -.04;gs[2]+=.04
            else:g[0]-=.04 if face=='left' else -.04;gs[0]+=.04
            garment(b,name,g,gs,a.GOLD)
    for face,cols in CAPA.items():
        for col in range(cols):
            for row in range(CAPA_LINHAS):panel('capa',face,col,row,21.6-row*1.6,1.65,6.4,2.75,cols,.15,row==CAPA_LINHAS-1)
    # Skirt ends above the knee so the dark tights show between hem and boots, as in the anime.
    for face,cols in SAIA.items():
        for col in range(cols):
            for row in range(SAIA_LINHAS):panel('saia',face,col,row,12.0-row*1.5,1.55,4.6,2.5,cols,.4,row==SAIA_LINHAS-1)

def shadow():
    b=a.skeleton();c=garment
    c(b,'armorHead',[-4.7,24,3.35],[9.4,8.8,1.4],a.BLACK)
    for x in [-5.0,3.75]:c(b,'armorHead',[x,24,-4.4],[1.25,8.5,8.3],a.BLACK)
    c(b,'armorHead',[-4.6,31.6,-4.45],[9.2,1.2,9.1],a.BLACK)
    for side in [-1,1]:c(b,'armorHead',[side*2.25-2.5,32,-3.9],[5,1.5,7.8],a.BLACK,[side*2.2,32,-.1],[0,0,-side*12])
    c(b,'armorBody',[-4.22,12,-2.2],[8.44,12,4.4],a.BLACK)
    c(b,'armorBody',[-2.25,13,-2.43],[4.5,8.8,.28],a.PLUM)
    for side in [-1,1]:
        c(b,'armorBody',[side*1.45-.15,15.3,-2.69],[.3,6.5,.22],a.SHADOW,[side*1.45,19,-2.65],[0,0,-side*12])
        # High, splayed collar, with the V-shaped gold edge repeated on the back.
        c(b,'armorBody',[side*4.2-2.15,22,-2.5],[4.3,5.2,.6],a.PLUM,[side*3.3,22,-2.3],[0,0,-side*34])
        c(b,'armorBody',[side*4.2-2.15,26.65,-2.66],[4.3,.48,.3],a.GOLD,[side*3.3,22,-2.3],[0,0,-side*34])
        c(b,'armorBody',[side*2.1-.22,19.5,3.6],[.44,5.2,.3],a.GOLD,[side*2.1,22.2,3.6],[0,0,-side*32])
        for y in [13.5,16.1]:
            c(b,'armorBody',[side*2.65-1.55,y,-2.65],[3.1,.43,.32],a.GOLD,[side*2.6,y,-2.6],[0,0,side*5])
            c(b,'armorBody',[side*1.7-.35,y-.17,-2.82],[.7,.78,.15],a.GOLD)
    for side,name in [(-1,'aba_esq'),(1,'aba_dir')]:
        b[name]=a.bone(name,[side*3,12,-2.2],'armorBody')
        c(b,name,[side*3-1.6,5.9,-2.6],[3.2,6.1,.55],a.BLACK,[side*3,12,-2.2],[0,0,side*6])
        c(b,name,[side*3-1.6,5.8,-2.74],[.4,6.2,.2],a.GOLD,[side*3,12,-2.2],[0,0,side*6])
    # Seven individually articulated strips create a torn, tapered cape silhouette.
    for i in range(7):
        x=(i-3)*1.75;bottom=[2.8,.7,2,0.6,3,1.1,2.1][i];name=f'capa_{i}'
        b[name]=a.bone(name,[x,22.5,2.8],'armorBody')
        angle=(i-3)*3
        c(b,name,[x-.93,bottom+2,2.9],[1.86,20.5-bottom,.5],a.BLACK,[x,22.5,2.8],[0,0,angle])
        c(b,name,[x-.93,bottom+2,2.75],[1.86,9,.12],a.PLUM,[x,22.5,2.8],[0,0,angle])
        c(b,name,[x-.93,bottom,2.9],[.95,2.6,.5],a.BLACK,[x,22.5,2.8],[0,0,angle])
        c(b,name,[x+.02,bottom+1,2.9],[.91,1.6,.5],a.PLUM,[x,22.5,2.8],[0,0,angle])
    for name,x in [('armorLeftArm',3.8),('armorRightArm',-8.3)]:
        c(b,name,[x,13,-2.25],[4.5,10.6,4.5],a.BLACK)
        c(b,name,[x-.2,21,-2.45],[4.9,2.3,4.9],a.PLUM)
        c(b,name,[x-.22,20.9,-2.5],[4.94,.45,5],a.GOLD)
        c(b,name,[x-.15,12.8,-2.4],[4.8,3.4,4.8],a.BLACK)
        c(b,name,[x-.2,15.9,-2.5],[4.9,.5,5],a.GOLD)
    limb_legs(b,a.BLACK,a.BLACK,6,a.BLACK)
    for leg,x in [('armorLeftLeg',2),('armorRightLeg',-2)]:
        c(b,leg,[x-.82,6.2,-2.48],[1.64,1.64,.25],a.STEEL,[x,6.95,-2.45],[0,0,45])
        c(b,leg,[x-.59,6.45,-2.65],[1.18,1.18,.18],a.BLACK,[x,6.95,-2.65],[0,0,45])
    return b

def asta():
    b=a.skeleton();c=garment
    c(b,'armorHead',[-4.2,30,-4.25],[8.4,1.7,.3],a.BLACK)
    for x in [-4.25,3.95]:c(b,'armorHead',[x,30,-4],[.3,1.7,8.2],a.BLACK)
    c(b,'armorHead',[-4.2,30,3.96],[8.4,1.7,.3],a.BLACK)
    c(b,'armorHead',[-3.8,30.25,-4.48],[1.3,1.1,.18],a.GOLD)
    c(b,'armorBody',[-4.12,11.4,-2.13],[8.24,12.4,4.26],a.IVORY)
    c(b,'armorBody',[-4.3,11.2,-2.32],[8.6,1,4.64],a.IVORY)
    c(b,'armorBody',[-4.4,12.5,-2.5],[8.8,1.2,5],a.BROWN)
    c(b,'armorBody',[-.85,12.45,-2.75],[1.7,1.3,.3],a.GOLD)
    c(b,'armorBody',[-.45,12.7,-2.93],[.9,.8,.1],a.LEATHER)
    # Blue rolled sleeves and exposed forearms under a short black mantle.
    for name,x in [('armorLeftArm',3.95),('armorRightArm',-8.2)]:
        c(b,name,[x,18.1,-2.15],[4.25,5.6,4.3],a.NAVY)
        c(b,name,[x-.12,17.5,-2.29],[4.49,1.15,4.58],a.IVORY)
    for side,name in [(-1,'manto_frente_esq'),(1,'manto_frente_dir')]:
        b[name]=a.bone(name,[side*2,23,0],'armorBody')
        c(b,name,[side*2.35-2.4,18,-2.65],[4.8,5.5,.5],a.BLACK,[side*2,23,0],[0,0,-side*7])
        c(b,name,[side*2.35-2.4,17.85,-2.73],[4.8,.52,.6],a.GOLD,[side*2,23,0],[0,0,-side*7])
    for side,name,x in [(-1,'manto_ombro_esq',-6.35),(1,'manto_ombro_dir',3.05)]:
        b[name]=a.bone(name,[side*4,23,0],'armorBody')
        c(b,name,[x,19.1,-2.75],[3.3,4.4,5.5],a.BLACK,[side*4,23,0],[0,0,side*11])
        c(b,name,[x,19,-2.81],[3.3,.45,5.62],a.GOLD,[side*4,23,0],[0,0,side*11])
    for side,name in [(-1,'capa_touro_esq'),(1,'capa_touro_dir')]:
        b[name]=a.bone(name,[side*2,23,2.65],'armorBody')
        c(b,name,[side*2.2-2.35,18,2.65],[4.7,5.4,.5],a.BLACK,[side*2,23,2.65],[0,0,side*5])
        c(b,name,[side*2.2-2.35,17.85,2.65],[4.7,.5,.58],a.GOLD,[side*2,23,2.65],[0,0,side*5])
    # Front bull crest, readable at Minecraft scale without a noisy decal.
    b['emblema_touro']=a.bone('emblema_touro',[-2.4,21.2,-3],'armorBody')
    c(b,'emblema_touro',[-3.1,20.3,-3.05],[1.4,1.25,.2],a.GOLD)
    for side in [-1,1]:c(b,'emblema_touro',[-2.4+side*.9-.2,21.05,-3.05],[.4,1,.2],a.GOLD,[-2.4+side*.9,21.05,-3],[0,0,side*30])
    limb_legs(b,a.NAVY,a.BROWN,4.5,a.LEATHER)
    for leg,x in [('armorLeftLeg',0),('armorRightLeg',-4)]:c(b,leg,[x-.22,5.1,-2.25],[4.44,.8,4.5],a.NAVY)
    b['grimoire']=a.bone('grimoire',[-4.5,12,1],'armorBody')
    c(b,'grimoire',[-5.9,9.5,.1],[1.5,4.6,3.7],a.BROWN)
    c(b,'grimoire',[-6.05,9.9,.3],[.2,3.8,3.3],a.LEATHER)
    c(b,'grimoire',[-6.15,11.4,1.6],[.15,.7,.65],a.GOLD)
    return b

# Devil Union layout shared with DevilUnionTecido (Java). Bat wing in the back plane (x outward, y up, pixels):
# shoulder root, wrist (claw) and outer tip on the leading edge; finger tips on the scalloped lower edge; the
# membrane is ASA_FAIXAS hanging strips of ASA_SEGMENTOS linked bones (cloth), fingers ride on the strips.
ASA_RAIZ=(2.0,23.0,2.45);ASA_PULSO=(9.0,37.0);ASA_PONTA=(27.0,22.0);ASA_DEDOS=[(4.5,14.0),(11.0,9.5),(17.5,8.5),(23.5,13.0)]
ASA_FAIXAS=8;ASA_SEGMENTOS=3;ASA_X0=4.0;ASA_PASSO=3.0
# Tail links: (angle back from straight down, sideways angle; negative = toward the player's right), deg.
CAUDA_DIRECOES=[(15,0),(25,-10),(28,-25),(24,-45),(15,-65),(6,-80),(-4,-95),(-8,-105)]
CAUDA_COMPRIMENTO=[2.4,2.3,2.2,2.1,2.0,1.9,1.8,1.6];CAUDA_RAIZ=(0,12.6,2.3)
def _borda_superior(x):
    (rx,ry),(px,py),(tx,ty)=(ASA_X0,ASA_RAIZ[1]),ASA_PULSO,ASA_PONTA
    return ry+(py-ry)*(x-rx)/(px-rx) if x<=px else py+(ty-py)*(x-px)/(tx-px)
def _borda_inferior(x):
    # Scallops: straight between finger tips, cut upward in the middle of each span.
    pts=ASA_DEDOS+[ASA_PONTA]
    for (ax,ay),(bx,by) in zip(pts,pts[1:]):
        if ax<=x<=bx:u=(x-ax)/(bx-ax);return ay+(by-ay)*u+math.sin(math.pi*u)*2.6
    return pts[0][1] if x<pts[0][0] else pts[-1][1]
def asta_forma():
    """Devil Union after the user's Minecraft reference video (and concept art for the tail), built like the outfits
    (GeckoLib bones, UV texture, glowmask): a slim black body hugging the bare player (+0.25 px; RenderTrajeAnime
    hides the outfit meanwhile) whose glowing red sigils are painted by build_asta_assets (chest star, belly cross,
    shoulder X, arm strokes, leg ribs, spine), boxy shoulder pads, a glowing crown with teeth and a diamond, two
    horns on top of the head (no hair), bat wings with glowing fingers and hem whose membrane is linked cloth
    strips, and a linked tail curling to the side. Rest pose is baked here; DevilUnionTecido only adds deltas."""
    b=a.skeleton();c=garment
    def bar(bone,p,q,width,z,tile,thick=.3,parent_pivot=None):
        dx,dy=q[0]-p[0],q[1]-p[1]
        c(b,bone,[p[0]-width/2,p[1],z],[width,math.hypot(dx,dy),thick],tile,[p[0],p[1],z],[0,0,-math.degrees(math.atan2(dx,dy))])
    c(b,'armorBody',[-4.25,11.75,-2.25],[8.5,12.5,4.5],a.BLACK)
    # Crown on the forehead: glowing band, light teeth on top, diamond in front (eyes stay the player's skin).
    c(b,'armorHead',[-4.6,29.5,-4.6],[9.2,1.5,9.2],a.RUBY)
    for x in [-3.6,-2.2,2.2,3.6]:c(b,'armorHead',[x-.45,31.0,-4.7],[.9,1.0,.45],a.STEEL)
    c(b,'armorHead',[-.5,31.2,-4.7],[1.0,1.4,.45],a.STEEL)
    for side in [-1,1]:
        for z in [-3.0,-1.0,1.0,3.0]:c(b,'armorHead',[side*4.6-.22,31.0,z-.45],[.45,1.0,.9],a.STEEL)
    c(b,'armorHead',[-.8,29.3,-4.95],[1.6,1.6,.4],a.RUBY,[0,30.1,-4.8],[0,0,45])
    for side in [-1,1]:
        s='e' if side<0 else 'd'
        # Horns on top of the head, behind the crown: up, slightly out, tips leaning back out.
        hx,hy=side*2.4,32.0
        for i in range(6):
            phi=math.radians(8+6*i);w=1.6-i*.2;L=1.15
            c(b,'armorHead',[hx-w/2,hy-.3,.2-w/2],[w,L+.3,w],a.INK,[hx,hy,.2],[-10-i*4,0,-side*math.degrees(phi)])
            hx+=side*math.sin(phi)*L;hy+=math.cos(phi)*L
        arm='armorRightArm' if side<0 else 'armorLeftArm';x0=-8.25 if side<0 else 3.75
        c(b,arm,[x0,11.75,-2.25],[4.5,12.75,4.5],a.BLACK)
        # Boxy shoulder pad, painted with the glowing X.
        c(b,arm,[x0-.5,20.4,-2.75],[5.5,4.4,5.5],a.SHADOW)
        leg='armorRightLeg' if side<0 else 'armorLeftLeg';lx=-4.25 if side<0 else -.25
        # PLUM is painted the same black for the form: it only keeps the legs' UV islands apart from the arms'.
        c(b,leg,[lx,-.25,-2.25],[4.5,12.5,4.5],a.PLUM)
        # Bat wing: leading-edge arm (rigid on the root), wrist claw, membrane strips and the fingers on them.
        raiz=f'du_asa_{s}';rx,ry,rz=ASA_RAIZ;b[raiz]=a.bone(raiz,[side*rx,ry,rz],'armorBody');px,py=ASA_PULSO;tx,ty=ASA_PONTA
        bar(raiz,(side*rx,ry),(side*px,py),2.2,rz-.35,a.INK,.9)
        bar(raiz,(side*px,py),(side*tx,ty),1.6,rz-.3,a.INK,.8)
        c(b,raiz,[side*px-.6,py,rz-.3],[1.2,2.6,.8],a.INK,[side*px,py,rz],[0,0,-side*30])
        faixas=[]
        for i in range(ASA_FAIXAS):
            x=ASA_X0+ASA_PASSO*(i+.5);top=_borda_superior(x)-.6;bottom=_borda_inferior(x);seg=(top-bottom)/ASA_SEGMENTOS;parent=raiz
            for j in range(ASA_SEGMENTOS):
                name=f'du_asa_{s}_{i}_{j}';y0=top-j*seg;b[name]=a.bone(name,[side*x,y0,rz],parent);faixas.append((x,y0-seg,y0,name))
                # The hem segment gets its own material so its glowing edge is not shared with the upper links.
                c(b,name,[side*x-ASA_PASSO*.55,y0-seg-(0 if j==ASA_SEGMENTOS-1 else .4),rz],[ASA_PASSO*1.1,seg+(0 if j==ASA_SEGMENTOS-1 else .4),.3],a.SHADOW if j==ASA_SEGMENTOS-1 else a.BLACK)
                parent=name
        # Finger ridges from the wrist to each scallop tip, cut into short pieces carried by the strip segment under
        # each piece, so the fingers bend with the membrane instead of floating over it.
        for fx,fy in ASA_DEDOS[1:]:
            n=max(2,int(math.hypot(fx-px,fy-py)/1.4));ang=math.degrees(math.atan2(side*(fx-px),fy-py))
            for k in range(n):
                u=(k+.5)/n;x,y=px+(fx-px)*u,py+(fy-py)*u
                dono=next((nm for fx0,y1,y2,nm in faixas if abs(x-fx0)<=ASA_PASSO*.55 and y1-.2<=y<=y2+.2),None)
                if dono:c(b,dono,[side*x-.35,y-.9,rz-.3],[.7,1.8,.4],a.RUBY,[side*x,y,rz],[0,0,-ang])
    # Long thin tail from the lower back, curling out to the player's right, linked bone by bone.
    px,py,pz=CAUDA_RAIZ;parent='armorBody';rot=(0,0)
    for i,((back,lado),length) in enumerate(zip(CAUDA_DIRECOES,CAUDA_COMPRIMENTO)):
        name=f'du_cauda_{i}';b[name]=a.bone(name,[px,py,pz],parent);w=.9-i*.05;ra,rg=math.radians(back),math.radians(lado)
        extra=0 if i==len(CAUDA_DIRECOES)-1 else .4
        c(b,name,[px-w/2,py-length-extra,pz-w/2],[w,length+extra,w],a.BLACK,[px,py,pz],[-back,0,lado])
        px+=math.cos(ra)*math.sin(rg)*length;py-=math.cos(ra)*math.cos(rg)*length;pz+=math.sin(ra)*length;parent=name;rot=(back,lado)
    # Arrowhead: a stepped triangle continuing the last link, flat toward the back so it reads from behind.
    back,lado=rot;ra,rg=math.radians(back),math.radians(lado);d=(math.cos(ra)*math.sin(rg),-math.cos(ra)*math.cos(rg),math.sin(ra))
    for k,(w,l) in enumerate([(2.8,.9),(1.9,.9),(1.0,.9)]):
        o=[px+d[0]*k*.85,py+d[1]*k*.85,pz+d[2]*k*.85]
        c(b,parent,[o[0]-w/2,o[1]-l,o[2]-.2],[w,l,.4],a.BLACK,o,[-back,0,lado])
    return b
