"""Offline review of Asta's Black Bulls robe as cloth (TecidoAsta, built like Cid's cape): the real outfit geometry and
texture, bones posed with the deltas exported by TecidoAstaTest (same conversion as the Frieren/Devil Union previews).
Neutral mannequin skin, a simple arm swing while running; not Minecraft (no lighting, no real walk cycle)."""
from pathlib import Path
import json,math,argparse
import av
from PIL import Image,ImageDraw
from preview_armor_models import ROOT,ASSETS,mesh
from preview_asta_devil_union import Cena

VISTAS=[('3/4 FRENTE',(2.4,1.9,-2.5)),('COSTAS 3/4',(-2.3,2.0,2.6)),('LADO',(3.3,1.7,0))]
def modelo(q,t):
 rot={n:[-math.degrees(x),-math.degrees(y),math.degrees(z)] for n,(x,y,z) in q['ossos'].items()}
 ba=math.sin(t*.6662)*.7 if q['fase']=='CORRIDA' else 0
 membros={'right_arm':[math.degrees(ba),0,0],'left_arm':[-math.degrees(ba),0,0]} if ba else None
 return mesh('asta','idle',membros,rot)
def render(saida):
 dados=json.loads((ROOT/'build/asta-capa-samples.json').read_text(encoding='utf-8'));w,h=400,520
 cena=Cena(w*3,h);cena.tex(ASSETS/'textures/armor/trajes/asta.png',0);cena.ctx.texture((1,1),4,bytes(4)).use(1)
 out=Path(saida);out.mkdir(parents=True,exist_ok=True)
 def tela(i):
  q=dados[i];m=modelo(q,i)
  for k,(n,cam) in enumerate(VISTAS):cena.desenhar(m,cam,(0,1.25,0),(k*w,0,w,h),fov=40)
  im=Image.new('RGB',(w*3,h+40),(16,18,24));im.paste(cena.imagem(),(0,0));d=ImageDraw.Draw(im)
  for k,(n,_) in enumerate(VISTAS):d.text((k*w+8,8),n,fill=(20,22,30))
  d.text((10,h+8),f"ASTA - CAPA DO TOURO NEGRO EM TECIDO (como a capa do Cid)   fase: {q['fase']}   tick {i}",fill=(235,238,245))
  d.text((10,h+24),'PREVIA OFFLINE: modelo e textura reais + fisica Java; manequim parado, so os bracos balancam na corrida; golpes/ataques/agachar entram como na fisica do jogo',fill=(150,165,190));return im
 with av.open(str(out/'asta-capa.mp4'),'w') as c:
  st=c.add_stream('libx264',rate=20);st.width,st.height,st.pix_fmt=w*3,h+40,'yuv420p';st.options={'crf':'18'}
  for i in range(len(dados)):
   for p in st.encode(av.VideoFrame.from_image(tela(i))):c.mux(p)
  for p in st.encode():c.mux(p)
 marcas=[(70,'CORRIDA'),(86,'PARADA BRUSCA'),(190,'QUEDA'),(222,'VENTO PARADO'),(236,'GOLPE Z'),(263,'GOLPE X'),(287,'INVESTIDA C'),(370,'AGACHADO')]
 folha=Image.new('RGB',(w*3,(h+40)//2*((len(marcas)+1)//2)),(16,18,24))
 for j,(i,_) in enumerate(marcas):folha.paste(tela(i).resize((w*3//2,(h+40)//2)),((j%2)*w*3//2,(j//2)*(h+40)//2))
 folha.save(out/'asta-capa-fases.png');print('ok')
if __name__=='__main__':
 a=argparse.ArgumentParser();a.add_argument('--output',required=True);render(a.parse_args().output)
