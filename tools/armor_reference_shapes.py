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
    c(b,'armorBody',[-4.15,12,-2.13],[8.3,11.8,4.26],a.WHITE)
    c(b,'armorBody',[-1.85,14,-2.37],[3.7,8.6,.3],a.INK)
    for y in [14.6,16.7,18.8,20.9]:c(b,'armorBody',[-1.7,y,-2.58],[3.4,.7,.18],a.IVORY)
    for x in [-2.4,1.85]:c(b,'armorBody',[x,13.8,-2.66],[.55,9.15,.24],a.GOLD)
    c(b,'armorBody',[-4.36,11.8,-2.86],[8.72,1.25,5.72],a.NAVY)
    c(b,'armorBody',[-.55,11.95,-3.08],[1.1,.95,.25],a.GOLD)
    c(b,'armorBody',[-4.25,22.2,-2.4],[8.5,1.8,4.8],a.WHITE)
    c(b,'armorBody',[-3.6,23,-2.55],[7.2,.45,4.8],a.GOLD)
    # Short shoulder mantle forms the distinct bell-shaped upper silhouette.
    for side,name,x in [(-1,'manto_esq',-5.9),(1,'manto_dir',.3)]:
        b[name]=a.bone(name,[side*3.8,23,0],'armorBody')
        c(b,name,[x,20.2,-2.62],[5.6,3.4,5.24],a.WHITE,[side*3.8,23,0],[0,0,side*9])
        c(b,name,[x,20.05,-2.68],[5.6,.6,5.36],a.GOLD,[side*3.8,23,0],[0,0,side*9])
    # The two front skirts follow their legs rather than cutting through them.
    for side,name,leg,x in [(-1,'saia_esq','armorRightLeg',-4.75),(1,'saia_dir','armorLeftLeg',0)]:
        b[name]=a.bone(name,[side*2.2,12,0],leg)
        c(b,name,[x,8.4,-2.62],[4.75,4.6,5.24],a.WHITE,[side*2.2,12,0],[0,0,side*5])
        c(b,name,[x-.03,8.3,-2.7],[4.81,.58,5.4],a.GOLD,[side*2.2,12,0],[0,0,side*5])
    for name,x in [('armorLeftArm',3.95),('armorRightArm',-8.25)]:
        c(b,name,[x,13,-2.2],[4.3,9.6,4.4],a.WHITE)
        for y in [13.05,15.9]:c(b,name,[x-.07,y,-2.27],[4.44,.65,4.54],a.GOLD)
    limb_legs(b,a.NAVY,a.BROWN,4.8,a.LEATHER)
    return b

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

def asta_forma():
    pieces=[]
    def piece(o,s,color,pivot,rot=(0,0,0),attachment='body'):
        pieces.append({'origin':o,'size':s,'color':color,'pivot':pivot,'rotation':list(rot),
                       'wing':attachment=='body','attachment':attachment})
    def spar(start,end,width,color):
        dx,dy=end[0]-start[0],end[1]-start[1]
        piece([start[0]-width/2,start[1],3.5],[width,math.hypot(dx,dy),1.2],color,
              [start[0],start[1],4],(0,0,-math.degrees(math.atan2(dx,dy))))
    # A high, torn membrane wing, following the supplied Black Asta silhouette.
    spar([-4,23],[-15,39],2.3,'12131E')
    spar([-15,39],[-35,48],1.7,'17121D')
    for i in range(8):
        x=-10-i*3.15;top=[30,34,38,41,43,45,46.5,48][i]
        bottom=[17,15,18,20,24,29,33,39][i]
        piece([x,bottom+2,4],[3.7,top-bottom-2,.65],'12121B',[-4,23,4])
        piece([x,bottom,4],[1.5,3,.65],'17121D',[-4,23,4])
        if i%3==0:piece([x+1.4,bottom+4,3.85],[.18,top-bottom-5,.16],'621526',[-4,23,4])
    # Hook on the upper joint and thin bony tips, kept separate from the skin.
    for i in range(3):
        piece([-15.7-i*.65,38.3-i*.8,2.8],[1.2-i*.2,2,1.1-i*.2],'77727B',[-15,39,3],(0,0,35+i*20))
    # Curved demon horn attached to the head, not a hair replacement.
    for i in range(6):
        angle=i*.34
        piece([-4.8-math.sin(angle)*2.2,31.0+i*.7,-2.8-i*.15],
              [1.8-i*.22,1.7-i*.13,1.5-i*.18],'20151F',[-4.3,31.0,-2.6],
              (0,0,-20-i*15),'head')
    # The right arm uses the player's animated arm transform in the renderer.
    piece([-8.18,12,-2.2],[4.36,10.5,4.4],'12121A',[-5,22,0],attachment='right_arm')
    piece([-8.3,11.55,-2.3],[4.6,3.5,4.6],'17121E',[-5,22,0],attachment='right_arm')
    for i in range(3):
        piece([-7.75+i*1.05,10.6,-2.35],[.7,1.4,.8],'21121D',[-5,22,0],
              (0,0,-6+i*5),'right_arm')
    for x,y,length in [(-7.6,16,5.2),(-5.4,13.5,4.6)]:
        piece([x,y,-2.25],[.18,length,.16],'76182D',[-5,22,0],(0,0,-8),'right_arm')
    return pieces
