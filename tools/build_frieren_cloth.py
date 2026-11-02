"""Regenerate only Frieren's outfit: capelet and A-line skirt as linked cloth bones simulated by TecidoFrieren.
Rasterizes the vector texture and refreshes the Blockbench project. Gojo/Shadow assets are untouched."""
from pathlib import Path
import json,sys,xml.etree.ElementTree as ET
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'tools'))
import build_armor_models as a

def build():
    a.build(only={'frieren'})
    svg=a.ASSETS/'textures/armor/trajes/frieren.svg';im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im)
    for r in ET.fromstring(svg.read_text(encoding='utf-8')):
        x,y,w,h=[int(float(r.attrib[k])) for k in ['x','y','width','height']];d.rectangle((x,y,x+w-1,y+h-1),fill=r.attrib['fill'])
    im.save(a.ASSETS/'textures/armor/trajes/frieren.png')
    from export_blockbench_projects import make_project,OUT
    (OUT/'frieren.bbmodel').write_text(json.dumps(make_project('frieren'),ensure_ascii=False,separators=(',',':'))+'\n',encoding='utf-8')
    geo=json.loads((a.ASSETS/'geo/armaduras/frieren.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]['bones']
    print('Frieren:',sum(1 for b in geo if b['name'].startswith('frieren_')),'cloth bones')
if __name__=='__main__':build()
