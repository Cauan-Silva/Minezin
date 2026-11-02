"""Mux original procedural SFX into an offline rendered video. No sampled reference audio."""
import argparse
import av

def mux(video,audio,output):
 with av.open(video) as source,av.open(audio) as sound,av.open(output,'w',options={'movflags':'+faststart'}) as dest:
  v=dest.add_stream_from_template(source.streams.video[0]);a=dest.add_stream('aac',rate=48000);a.layout='mono';a.bit_rate=96000
  packets=[]
  for packet in source.demux(video=0):
   if packet.dts is not None:packet.stream=v;packets.append((float(packet.dts*packet.time_base),packet))
  resampler=av.AudioResampler(format='fltp',layout='mono',rate=48000)
  for frame in sound.decode(audio=0):
   for f in resampler.resample(frame):
    for packet in a.encode(f):packets.append((float(packet.dts*packet.time_base),packet))
  for f in resampler.resample(None):
   for packet in a.encode(f):packets.append((float(packet.dts*packet.time_base),packet))
  for packet in a.encode():packets.append((float(packet.dts*packet.time_base),packet))
  for _,packet in sorted(packets,key=lambda item:item[0]):dest.mux(packet)
 print('Muxed procedural SFX:',output)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--video',required=True);p.add_argument('--audio',required=True);p.add_argument('--output',required=True);a=p.parse_args();mux(a.video,a.audio,a.output)
