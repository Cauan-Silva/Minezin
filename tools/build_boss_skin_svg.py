"""Editable vector pixel art on the exact Minecraft 64x64 UV grid.

SVG is the authored source. PNGs are lossless raster exports for Minecraft.
No gradients, filtering, external art or random noise.

Qual follows the anime profile: ashy-brown skin with dark tattoo bands and staples, a mask-like face with
light-brown markings, stapled-shut eyes and jagged teeth, long blonde hair, fur cuffs and a dark grey
floor-length loincloth over the legs. Accessories (fur cape, beard, horns, claws, belt) sample the swatch row.
"""
from pathlib import Path
import xml.etree.ElementTree as ET
from skin_boss_models import uv_box

OUT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/arcanecontrol/textures/bosses'

def build(name):
    if name!='qual':raise ValueError('Toji uses the credited external skin, not this generator')
    rects=[]
    colors={'S':'6e625c','s':'5c514c','T':'3a2f2b','p':'a9a9a6','L':'b89a77','H':'4a3a30','k':'15131a',
            'w':'e8e2cc','B':'e6d8a6','b':'c9b984','F':'efe6d2','f':'cfc3a8','C':'4a4a50','c':'393940',
            'Y':'d8c070','G':'8a8c90','R':'7a1e24'}
    def rect(x,y,w,h,c):rects.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="#{colors.get(c,c)}"/>')
    def box(u,v,w,h,d,c):
        for f in uv_box(u,v,w,h,d).values():rect(*f['uv'],*f['uv_size'],c)
    def paint(x,y,rows):
        for j,row in enumerate(rows):
            for i,c in enumerate(row):
                if c!='.':rect(x+i,y+j,1,1,c)
    # Head: skin box, blonde hair over top/sides/back, mask face in front.
    box(0,0,8,8,8,'S')
    paint(8,0,['BBBBBBBB']*8)
    for x in [0,16,24]:paint(x,8,['BBBBBBBB','BBBBBBBB','BBBBBbBB','BbBBBBBB','BBBBBBbB','BBBbBBBB','BBBBBBBB','BbBBBBBB'] if x==24 else
                             ['BBBBBBBB','BBBBBBBB','BBBBSSSS','BBBSSSSS','BBBSSSSS','BBBSSSSs','BBBBSSSs','BBBBBSSs'] if x==0 else
                             ['BBBBBBBB','BBBBBBBB','SSSSBBBB','SSSSSBBB','SSSSSBBB','sSSSSBBB','sSSSBBBB','sSSBBBBB'])
    paint(8,8,['BHHHHHHB',
               'BSHSSHSB',
               'LLLSSLLL',
               'LkkLLkkL',
               'SLLSSLLS',
               'SkkkkkkS',
               'SwkwkwkS',
               'BwkwkwkB'])
    # Body: tattooed chest with staples, dark cloth at the waist; long hair down the back.
    box(16,16,8,12,4,'S')
    paint(20,20,['SSSSSSSS','SSSSSSSS','STTSSTTS','SSSSSSSS','SpSSSSpS','STTSSTTS',
                 'SSSSSSSS','SSSssSSS','SSSSSSSS','CCCCCCCC','CcCCCCcC','CCCCCCCC'])
    paint(32,20,['BBBBBBBB','BBBbBBBB','BBBBBBBB','BBBBBbBB','BBBBBBBB','BbBBBBBB',
                 'BBBBBBBB','BBBBBBbB','SBBBBBBS','CCCCCCCC','CcCCCCcC','CCCCCCCC'])
    for x in [16,28]:paint(x,20,['SSSS','SSSS','STTS','SSSS','SpSS','STTS','SSSS','SSSS','SSSS','CCCC','CcCC','CCCC'])
    # Arms: tattoo bands and staples, fur cuff near the wrist, ashy hand.
    arm=['SSSS','STTS','STTS','SSSS','SpSS','SSSS','STTS','SSSS','SSpS','FFFF','fFFf','SSSS']
    for u,v in [(40,16),(32,48)]:
        box(u,v,4,12,4,'S')
        for x in [u,u+4,u+8,u+12]:paint(x,v+4,arm)
        paint(u+8,v,['SSSS','SSSS','SSSS','SSSS'])
    # Legs: hidden by the loincloth, painted as the same cloth so gaps never show bare skin.
    leg=['CCCC','CcCC','CCCC','CCcC','CCCC','CcCC','CCCC','CCcC','CCCC','CcCC','cccc','kkkk']
    for u,v in [(0,16),(16,48)]:
        box(u,v,4,12,4,'C')
        for x in [u,u+4,u+8,u+12]:paint(x,v+4,leg)
    # Swatch row for accessories (unused corner of the vanilla layout), order matches skin_boss_models.qual.
    for x,c in enumerate(['C','c','F','f','B','Y','G','R']):rect(x,0,1,1,c)
    OUT.mkdir(parents=True,exist_ok=True)
    text='<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64" shape-rendering="crispEdges">\n'+'\n'.join(rects)+'\n</svg>\n'
    (OUT/f'{name}_skin.svg').write_bytes(text.encode('utf-8'))
    from PIL import Image,ImageDraw
    im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
    for r in ET.fromstring(text):
        x,y,w,h=[int(r.attrib[k]) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(OUT/f'{name}_skin.png')

if __name__=='__main__':
    build('qual')
