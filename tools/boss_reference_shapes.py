"""Authored cuboid interpretations of the supplied four boss turnaround sheets."""
import build_nemesis_models as m
import math

def add(b,n,o,s,t,angle=0,pivot=None):
    m.cube(b[n],o,s,t,rotation=[0,0,-angle] if angle else None,pivot=pivot)

def segment(b,n,start,end,width,t):
    dx,dy,dz=[end[i]-start[i] for i in range(3)]
    assert dz==0
    add(b,n,[start[0]-width/2,start[1],start[2]-width/2],[width,math.hypot(dx,dy),width],t,-math.degrees(math.atan2(dx,dy)),start)

def horn(b,side,points,t):
    for i in range(len(points)-1):segment(b,'head',[side*points[i][0],points[i][1],.8],[side*points[i+1][0],points[i+1][1],.8],1.7-i*.3,t)

def group(b,n,p,parent='body'):
    b[n]=m.bone(n,p,parent);return n

def face(b,hair,skin,eyes=m.RED):
    add(b,'head',[-4,24,-4],[8,8,8],skin)
    add(b,'head',[-4.25,30,-4.25],[8.5,2.5,8.5],hair)
    for side in [-1,1]:
        add(b,'head',[side*2.1-.8,27,-4.2],[1.6,.65,.25],m.WHITE)
        add(b,'head',[side*2.1-.3,27,-4.4],[.6,.65,.25],eyes)
        add(b,'head',[side*2.1-1,28,-4.3],[2,.5,.25],hair,side*22.5,[side*2.1,28,-4.3])

def toji():
    b=m.base();face(b,m.BLACK,m.SKIN,m.BLACK)
    add(b,'body',[-4.3,12,-2.3],[8.6,12,4.6],m.BLACK)
    for n,x in [('leftArm',4),('rightArm',-8)]:
        add(b,n,[x-.15,20,-2.15],[4.3,4,4.3],m.BLACK)
        add(b,n,[x,12,-2],[4,8,4],m.SKIN)
        add(b,n,[x-.1,16,-2.12],[4.2,1.2,4.24],m.SKIN)
    for n,x in [('leftLeg',0),('rightLeg',-4)]:
        add(b,n,[x-.3,4,-2.35],[4.6,8,4.7],m.IVORY)
        add(b,n,[x-.15,1.8,-2.1],[4.3,3,4.2],m.IVORY)
        add(b,n,[x+.1,0,-2.7],[3.8,1.8,5],m.BLACK)
        add(b,n,[x+.25,1.5,-2.2],[3.5,.65,4.4],m.SKIN)
        add(b,n,[x+.4,5,-2.53],[3.2,.5,.35],m.SILVER,22.5,[x+2,5,-2.5])
    add(b,'body',[-4.4,12,-2.5],[8.8,1,5],m.BLACK)
    for side in [-1,1]:add(b,'body',[side*1.1-.45,8.2,-2.75],[.9,4.5,.5],m.BLACK,side*22.5,[side*1.1,12,-2.7])
    # Black hair belongs to this NPC, never to the player's armor.
    for i in range(5):add(b,'head',[-4+i*1.65,28.8,-4.45],[1.9,2.8,1],m.BLACK,(-1 if i%2 else 1)*22.5,[-3+i*1.65,31,-4.1])
    add(b,'head',[2.9,25.5,-4.25],[.7,.25,.25],m.PLUM,22.5,[3.2,25.5,-4.2])
    group(b,'curse',[0,20,0])
    for i in range(9):
        x=-4.5+i*.9;y=23-i*.8
        add(b,'curse',[x,y,-3.1],[2.3,2.4,2],m.PLUM,-22.5,[x+1,y+1,-2.1])
        add(b,'curse',[x,y,2],[2.3,2.4,1.9],m.PLUM,22.5,[x+1,y+1,3])
    add(b,'curse',[-6.4,22,-2.5],[3.4,3.3,4],m.PLUM)
    for x in [-5.9,-4.4]:add(b,'curse',[x,23.6,-2.7],[.55,.5,.25],m.BLACK)
    add(b,'curse',[-5.7,22.5,-2.75],[1.7,.35,.25],m.BLACK)
    # Boss-only inverted spear: grip at the hand, guard/blade below the palm.
    group(b,'weapon',[-6,13,-2.2],'rightArm')
    add(b,'weapon',[-6.5,11.8,-2.75],[1,2.6,1.1],m.BLACK)
    add(b,'weapon',[-7.7,11.2,-2.85],[3.4,.6,1.3],m.SILVER)
    add(b,'weapon',[-6.6,3.5,-2.7],[1.2,7.7,.95],m.SILVER)
    add(b,'weapon',[-6.3,2.4,-2.6],[.6,1.2,.75],m.SILVER)
    for side in [-1,1]:
        add(b,'weapon',[-6+side*1.3-.3,6.4,-2.7],[.6,4.8,.95],m.SILVER)
    add(b,'weapon',[-6.6,14.3,-2.8],[1.2,.5,1.2],m.GOLD)
    group(b,'chain',[-6,14.7,-2.2],'weapon')
    for i in range(6):
        add(b,'chain',[-7.1-(i%2)*.2,13.8-i*.9,-2.4],[.5,1.1,.5],m.SILVER)
    return b

def aurora():
    b=m.base();face(b,m.BLACK,m.SKIN,m.RED)
    add(b,'body',[-3.8,12,-2.1],[7.6,11,4.2],m.PLUM)
    for side in [-1,1]:
        add(b,'body',[side*2-.9,18,-2.35],[1.8,5,.4],m.RED,side*22.5,[side*2,20,-2.3])
        add(b,'body',[side*2.5-.3,14,-2.55],[.6,8,.5],m.GOLD,side*22.5,[side*2.5,18,-2.5])
    add(b,'body',[-3.9,12,-2.3],[7.8,.7,4.6],m.GOLD)
    add(b,'body',[-.6,20,-2.65],[1.2,1.6,.45],m.RED)
    for n,x in [('leftArm',4),('rightArm',-8)]:
        add(b,n,[x+.25,19,-1.8],[3.5,4.5,3.6],m.SKIN)
        add(b,n,[x,12,-2],[4,7,4],m.PLUM)
        add(b,n,[x-.1,13.4,-2.1],[4.2,.65,4.2],m.GOLD)
        add(b,n,[x+.1,12,-2.1],[3.8,1,4.2],m.SKIN)
    # Six independently articulated panels make a wide burgundy dress.
    for i in range(6):
        x=(i-2.5)*1.7;n=group(b,'skirt_'+str(i),[x,13,0])
        add(b,n,[x-.95,.8,-3.0],[1.9,12.2,6],m.RED if i in [2,3] else m.PLUM,(i-2.5)*4.5,[x,13,0])
        add(b,n,[x-.95,.7,-3.18],[1.9,.65,6.36],m.GOLD,(i-2.5)*4.5,[x,13,0])
    for i in range(3):
        x=(i-1)*2.75;n=group(b,'hair_'+str(i),[x,30,3.5],'head')
        add(b,n,[x-1.45,10+abs(i-1),3.5],[2.9,20-abs(i-1),1.4],m.BLACK)
    for side in [-1,1]:
        add(b,'head',[side*3.6-.35,28,-4.5],[.7,4,1],m.BLACK,side*22.5,[side*3.6,31,-4])
        add(b,'head',[side*4.1-.2,24.5,-.6],[.4,1.5,.5],m.GOLD)
    for n,x in [('leftLeg',0),('rightLeg',-4)]:add(b,n,[x+.2,0,-2.3],[3.6,3,4.6],m.BLACK)
    return b

def qual():
    b=m.base();face(b,m.PLUM,m.SILVER,m.RED)
    add(b,'body',[-5,12,-2.7],[10,12,5.4],m.SILVER)
    for side in [-1,1]:
        add(b,'body',[side*2.5-1.8,18,-3],[3.6,3.5,.6],m.PLUM,side*22.5,[side*2.5,20,-2.8])
        for y in [13,15,17]:add(b,'body',[side*2.2-1.3,y,-3.1],[2.6,1,.6],m.SILVER,side*22.5,[side*2.2,y,-2.9])
        for layer in range(2):
            n=('leftArm' if side>0 else 'rightArm') if layer==0 else group(b,'lower_arm_'+str(side),[side*5,16,0])
            x=4.8 if side>0 else -8.8;y=12 if layer==0 else 6
            add(b,n,[x,y,-2],[4,10,4],m.SILVER)
            if layer==1:b[n]['rotation']=[0,0,-side*35]

            for k in range(3):add(b,n,[x+k*1.1,y-.8,-2.5],[.65,2,1],m.BLACK,0)
        horn(b,side,[(4,30),(6,31),(7.2,33.5),(7,36),(5.5,38)],m.PLUM)
        add(b,'head',[side*4.1-.6,26,-3],[1.2,4,2],m.SILVER,side*22.5,[side*4,28,-2])
    add(b,'head',[-2.3,24,-4.5],[4.6,2,.6],m.BLACK)
    for i in range(5):add(b,'head',[-1.9+i*.8,25.2,-4.8],[.5,.65,.4],m.IVORY)
    for n,x in [('leftLeg',0),('rightLeg',-4)]:
        add(b,n,[x-.3,1,-2.1],[4.6,11,4.2],m.SILVER)
        add(b,n,[x-.4,0,-3],[4.8,2,5.2],m.SILVER)
        for i in range(3):add(b,n,[x+i*1.45-.15,-.05,-3.8],[.9,1,1.8],m.BLACK)
    for i in range(7):
        x=(i-3)*1.9;n=group(b,'mantle_'+str(i),[x,23,3])
        bottom=[3,1.5,4,0,3,2,4][i]
        add(b,n,[x-1,bottom+2,3.2],[2,21-bottom,.7],m.NAVY,(i-3)*4.5,[x,23,3])
        add(b,n,[x-1,bottom,3.2],[1,3,.7],m.NAVY,(i-3)*4.5,[x,23,3])
    for side in [-1,1]:add(b,'body',[side*4-2.4,22,-.5],[4.8,2.3,6],m.NAVY,side*22.5,[side*4,23,1])
    return b

def liebe():
    b=m.base();face(b,m.WHITE,m.IVORY,m.RED)
    add(b,'body',[-3.7,12,-2],[7.4,12,4],m.IVORY)
    for side in [-1,1]:
        for y in [14,16.5,19]:add(b,'body',[side*1.7-1.2,y,-2.2],[2.4,.6,.4],m.SILVER,side*22.5,[side*1.7,y,-2])
        horn(b,side,[(4,30),(5.8,32),(6.5,35),(5.8,37),(4.5,38)],m.BLACK)
        for i in range(3):add(b,'head',[side*(1+i)-.7,31+i*.6,-3],[1.7,3,2.2],m.WHITE,-side*22.5,[side*2,31,0])
        add(b,'head',[side*2-.25,25.5,-4.35],[.5,1.5,.3],m.BLACK,side*22.5,[side*2,26,-4.3])
    for n,x in [('leftArm',4),('rightArm',-8)]:
        add(b,n,[x,13,-2],[4,11,4],m.IVORY)
        add(b,n,[x-.1,11.8,-2.1],[4.2,5,4.2],m.BLACK)
        for i in range(3):add(b,n,[x+.3+i*1.1,10.5,-2.25],[.7,1.8,.7],m.BLACK)
    for n,x in [('leftLeg',0),('rightLeg',-4)]:
        add(b,n,[x-.2,7,-2.2],[4.4,5,4.4],m.BLACK)
        add(b,n,[x+.3,1,-1.8],[3.4,7,3.6],m.IVORY)
        add(b,n,[x,0,-2.9],[4,2,5.2],m.IVORY)
        for i in range(2):add(b,n,[x+.6+i*1.5,-.1,-3.5],[.8,.8,1.2],m.BLACK)
    for side in [-1,1]:
        n=group(b,'wing_'+str(side),[side*3,22,2.5])
        for i in range(6):
            x=side*(5+i*2.6);top=28+min(i,2)*1.1;bottom=19+[0,-1,-2,0,2,5][i]
            add(b,n,[x-1.5,bottom,3],[3,top-bottom,.65],m.BLACK,0)
            add(b,n,[x-1.5,top,2.85],[3.3,.6,1],m.PLUM,side*22.5,[x,top,3])
            if i%2==0:add(b,n,[x-.2,bottom-.8,2.9],[.5,top-bottom+1.3,.8],m.PLUM,0)
        for i in range(3):add(b,n,[side*(4+i*6)-.6,25+i*2,2.7],[1.2,6,1.2],m.BLACK,-side*45,[side*(4+i*6),25+i*2,3])
    # Connected tail joints: each child starts exactly at its parent's segment end.
    points=[(0,12),(3,10),(6,10),(8,12),(9,14)]
    parent='body'
    for i,(start,end) in enumerate(zip(points,points[1:])):
        n=group(b,'tail_'+str(i),[start[0],start[1],3],parent)
        segment(b,n,[start[0],start[1],3],[end[0],end[1],3],.8-i*.1,m.BLACK);parent=n
    segment(b,parent,[9,14,3],[10,15.5,3],1.8,m.BLACK)
    return b
