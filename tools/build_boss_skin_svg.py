"""Editable vector pixel art on the exact Minecraft 64x64 UV grid.

SVG is the authored source. PNGs are lossless raster exports for Minecraft.
No gradients, filtering, external art or random noise.
"""
from pathlib import Path
from skin_boss_models import uv_box

OUT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/arcanecontrol/textures/bosses'

def build(name):
    if name!='qual':raise ValueError('Toji uses the credited external skin, not this generator')
    qual=True;rects=[]
    colors={'k':'14151c','h':'242331','s':'cba78e','d':'aa826f','p':'c7cac5','f':'a9b2ad',
            'g':'776b7a','l':'a69ba8','r':'38283e','e':'d6cbd4','w':'e9e8dc','v':'5d465f'}
    def rect(x,y,w,h,c):rects.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="#{colors.get(c,c)}"/>')
    def box(u,v,w,h,d,c):
        for f in uv_box(u,v,w,h,d).values():rect(*f['uv'],*f['uv_size'],c)
    box(0,0,8,8,8,'g' if qual else 's')
    # Authored 8x8 face pixels, eyebrows, eyes, mouth and hair line.
    pattern=(['gggggggg','gggrrggg','gkkggkkg','glkggklg','gggllggg','gkkkkkkg','gwewewgg','gggggggg'] if qual else
             ['hhhhhhhh','khhkhkhk','kskhskhk','sdsssdss','sks ssks'.replace(' ','s'),'ssssssss','ssddssss','dssssssd'])
    for y,row in enumerate(pattern):
        assert len(row)==8
        for x,c in enumerate(row):rect(8+x,8+y,1,1,c)
    if not qual:
        for pos in [(0,8),(16,8),(24,8)]:
            rect(*pos,8,3,'h')
            for x in range(0,8,2):rect(pos[0]+x,pos[1]+3,1,1,'h')
        rect(8,0,8,8,'h');rect(24,8,8,8,'h')
        # Hat layer only at the hair line; all other second-layer pixels stay transparent.
        rect(40,8,8,1,'h');rect(40,9,2,1,'h');rect(46,9,2,1,'h')
    box(16,16,8,12,4,'g' if qual else 'k')
    for row in range(12):
        for x in range(8):
            if qual:
                c='r' if row<4 and x not in [3,4] else 'l' if row in [5,8,10] and x in [1,2,5,6] else 'g'
            else:c='h' if (row in [1,2] and x in [1,2,5,6]) or (row==10 and x in [2,3]) else 'k'
            rect(20+x,20+row,1,1,c)
    for u,v in [(40,16),(32,48)]:
        box(u,v,4,12,4,'g' if qual else 's')
        if not qual:
            rect(u+4,v+4,4,4,'k');rect(u,v+4,4,4,'k');rect(u+8,v+4,4,4,'k');rect(u+12,v+4,4,4,'k')
        rect(u+4,v+9,1,6,'l' if qual else 'd')
    for u,v in [(0,16),(16,48)]:
        box(u,v,4,12,4,'g' if qual else 'p')
        rect(u+4,v+5,1,7,'l' if qual else 'f');rect(u+7,v+9,1,3,'r' if qual else 'f')
        rect(u+4,v+14,4,2,'k');rect(u+12,v+14,4,2,'k')
        if not qual:rect(u+4,v+13,4,1,'s')
    # Hand-authored carved-stone planes: no gradients and no random pixel noise.
    colors.update({'a':'514655','b':'655869','c':'807184','t':'a293a4','q':'382e41','z':'211d2a','n':'b4a79f'})
    face=['aabbbbaa','abqzzqba','bztbbtzb','bqcaacqb','acbttbca','bzqqqqzb','bcntntcb','aabbcbaa']
    chest=['aabqqbaa','abqqqqba','bqtcctqb','bttbbttb','atcbbcta','bbttttbb','abqccqba','actbbtca','abtcc tba'.replace(' ',''),'abqbbqba','atcbbcta','aabbbbaa']
    arm=['aabb','abcb','bcct','bcct','abcb','aabb','abqq','abcb','bcct','bcct','abbb','azaz']
    leg=['aabb','abcb','bcct','bcct','abcb','abqq','abcb','bcct','bcct','abbb','zqzq','nznz']
    def paint(x,y,rows):
        for j,row in enumerate(rows):
            for i,c in enumerate(row):rect(x+i,y+j,1,1,c)
    paint(8,8,face)
    for side,x in enumerate([0,16,24]):
        paint(x,8,['abcc bbaa'.replace(' ','') if row%3==0 else 'aabccbba' for row in range(8)])
    paint(20,20,chest)
    paint(32,20,[row[::-1] for row in chest])
    for x in [16,28]:paint(x,20,['bcba' if i%3 else 'atca' for i in range(12)])
    for u,v in [(40,16),(32,48)]:
        for x in [u,u+4,u+8,u+12]:paint(x,v+4,arm)
    for u,v in [(0,16),(16,48)]:
        for x in [u,u+4,u+8,u+12]:paint(x,v+4,leg)
    # 8x16 cloth island outside all vanilla skin faces, used by the seven mantle strips.
    cloth=['zzqaaqzz','zqabbaqz',*['zqabcbqz']*8,'zqabbcqz','zqabc bqz'.replace(' ',''),'zqabbaqz','zzqbbqzz','zzqbaqzz','zzqqqqzz']
    for row in cloth:assert len(row)==8,row
    paint(56,16,cloth)
    # Material swatches in an unused area of the vanilla skin layout, for accessories.
    for x,c in enumerate(['k','g','r','w','bfa272','s','h','v']):rect(x,0,1,1,c)
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/f'{name}_skin.svg').write_bytes(('<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64" shape-rendering="crispEdges">\n'+'\n'.join(rects)+'\n</svg>\n').encode('utf-8'))

if __name__=='__main__':
    build('qual')
