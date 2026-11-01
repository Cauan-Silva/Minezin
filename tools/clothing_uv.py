"""Per-outfit 128px UVs and editable vector pixel art. No gradients or raster edits.

One texel per model unit, rounded only for fractional cuboids. Islands have a
one-pixel gutter; material islands are reused when their size and purpose match.
"""
import math
from pathlib import Path
import build_armor_models as a

SIZE=128
OUT=a.ASSETS/'textures/armor/trajes'
BASE=['161520','27243d','e6dfcf','2c2038','757c89','bc9658','202b45','795b40',
      '54aade','a385b8','9e283c','f0ece2','342a29','201d30','493050','947146']

def shade(hexcode,factor):
    return ''.join(f'{max(0,min(255,round(int(hexcode[i:i+2],16)*factor))):02x}' for i in [0,2,4])

def apply(style,bones):
    palette=BASE.copy()
    if style=='gojo':palette[a.NAVY]='202b45';palette[a.BLACK]='13151d'
    if style=='shadow':palette[a.BLACK]='191622';palette[a.PLUM]='3b2949'
    if style=='asta':palette[a.BLACK]='1b2029';palette[a.NAVY]='26364a'
    islands={};rects=[];cursor_x=cursor_y=row_height=0
    def rect(x,y,w,h,color):
        rects.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="#{color}"/>')
    def island(tile,w,h,face,kind):
        nonlocal cursor_x,cursor_y,row_height
        key=(tile,w,h,face,kind)
        if key in islands:return islands[key]
        if cursor_x+w+2>SIZE:cursor_x=0;cursor_y+=row_height;row_height=0
        if cursor_y+h+2>SIZE:raise ValueError(f'{style}: UV budget exceeded at {key}')
        x,y=cursor_x+1,cursor_y+1;cursor_x+=w+2;row_height=max(row_height,h+2)
        color=palette[tile];light=1.08 if face=='up' else .80 if face=='down' else .94 if face in ['east','west'] else 1
        base=shade(color,light);white=tile in [a.IVORY,a.WHITE]
        dark=shade(base,.90 if white else .82);mid=shade(base,.965 if white else .93);bright=shade(base,1.025 if white else 1.12)
        rect(x-1,y-1,w+2,h+2,base)
        # Broad clusters rather than random noise or a repeated checker pattern.
        if w>=3:
            rect(x,y,1,h,mid);rect(x+w-1,y,1,h,dark)
        if h>=4:rect(x,y+h-1,w,1,mid)
        cloth=tile in [a.BLACK,a.NAVY,a.IVORY,a.WHITE,a.PLUM,a.SHADOW,a.INK]
        if cloth and h>=6 and w>=3:
            rect(x+1,y+1,max(1,w-3),1,bright)
            for j in range(min(3,h//4)):
                px=x+max(1,min(w-2,w//2+j-1));py=y+h//2+j
                rect(px,py,max(1,min(w-1-(px-x),w//2)),1,mid)
        if kind=='torso' and face=='north':
            if style=='gojo':
                for j in range(h):rect(x+w//2,y+j,1,1,dark)
                for j in range(min(3,w//2)):rect(x+w//2-j,y+2+j,1,1,bright)
            elif style=='asta' and tile==a.IVORY:
                for j in range(min(3,h)):rect(x+w//2-j-1,y+j,2,1,shade(palette[a.NAVY],.8))
            elif style=='shadow' and tile==a.PLUM:
                for j in range(2,min(h-1,8),3):
                    rect(x+1,y+j,max(1,w-2),1,shade(palette[a.STEEL],.63))
        if kind=='boots' and h>=3:
            rect(x,y+h-2,w,1,dark)
            if face=='north' and w>=3:rect(x+1,y+1,w-2,1,bright)
        if kind=='cape' and w>=2 and h>=5:
            rect(x,y,1,h,bright);rect(x+w-1,y,1,h,dark)
            if h>10:rect(x,y+h-4,1,2,mid)
        if tile in [a.GOLD,a.STEEL,a.BROWN,a.LEATHER] and h>=2:
            rect(x,y,w,1,bright)
        islands[key]=[x,y];return [x,y]
    faces=['north','south','east','west','up','down']
    for name,b in bones.items():
        for c in b.get('cubes',[]):
            old=c['uv']['north']['uv'];tile=int(old[0]/a.W*4)+4*int(old[1]/a.H*4)
            w,h,d=[max(1,int(math.ceil(v-.05))) for v in c['size']]
            kind='boots' if 'Boot' in name else 'cape' if name.startswith(('capa_','aba_','manto_','saia_')) else 'torso' if name=='armorBody' and c['size'][1]>5 else 'cloth'
            c['uv']={}
            for f in faces:
                fw,fh=(w,h) if f in ['north','south'] else (d,h) if f in ['east','west'] else (w,d)
                c['uv'][f]={'uv':island(tile,fw,fh,f,kind),'uv_size':[fw,fh]}
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/f'{style}.svg').write_bytes(('<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128" shape-rendering="crispEdges">\n'+'\n'.join(rects)+'\n</svg>\n').encode('utf-8'))
    return bones
