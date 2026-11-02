"""Replace only the offline Maximum Purple preview audio; preserve encoded video packets."""
from pathlib import Path
from fractions import Fraction
import argparse,json,hashlib
import av,numpy as np,soundfile as sf
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'

def video_hash(path):
 h=hashlib.sha256()
 with av.open(str(path)) as c:
  for packet in c.demux(c.streams.video[0]):
   if packet.size:h.update(bytes(packet))
 return h.hexdigest()

def remux(source,target):
 source=Path(source);target=Path(target);payload=json.loads((ROOT/'build/gojo-roxo-maximo-samples.json').read_text(encoding='utf-8'))
 before=video_hash(source)
 with av.open(str(source)) as original,av.open(str(target),'w') as output:
  video=original.streams.video[0];copy=output.add_stream_from_template(video);duration=original.duration/1e6
  audio=output.add_stream('aac',rate=48000);audio.layout='mono';audio.bit_rate=128000
  for packet in original.demux(video):
   if packet.dts is None:continue
   packet.stream=copy;output.mux(packet)
  samples=np.zeros(round(duration*48000),dtype='f4')
  for cue in payload['audio']:
   sound,rate=sf.read(ASSETS/('sounds/anime/'+cue['nome']+'.ogg'),dtype='float32');assert rate==48000
   sound=sound[:cue['vida']*2400]*cue['volume']*.7;offset=round((cue['atraso']-2)/20*48000)
   if offset<0:sound=sound[-offset:];offset=0
   length=min(len(sound),len(samples)-offset)
   if length>0:samples[offset:offset+length]+=sound[:length]
  samples=np.clip(samples,-.98,.98)
  for start in range(0,len(samples),1024):
   frame=av.AudioFrame.from_ndarray(samples[start:start+1024][None,:].copy(),format='fltp',layout='mono');frame.sample_rate=48000;frame.pts=start;frame.time_base=Fraction(1,48000)
   for packet in audio.encode(frame):output.mux(packet)
  for packet in audio.encode():output.mux(packet)
 assert before==video_hash(target),'Approved video must remain byte-for-byte identical'
 print('Audio revised; encoded video unchanged:',target.name)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--source',required=True);p.add_argument('--output',required=True);a=p.parse_args();remux(a.source,a.output)
