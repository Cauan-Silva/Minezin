"""Original one-shot Maximum Purple SFX. No repeated cues or rhythmic modulation."""
from pathlib import Path
import numpy as np,soundfile as sf
root=Path(__file__).resolve().parents[1];rate=48000

def noise(rng,n,cut,power):
 f=np.fft.rfftfreq(n,1/rate);white=rng.standard_normal(n)
 x=np.fft.irfft(np.fft.rfft(white)*(1+f/90)**(-power)/(1+(f/cut)**8),n=n)
 return x/max(np.std(x),1e-8)

def write(name,x):
 x=(x/max(np.max(np.abs(x)),1e-8)*.85).astype('float32');assert np.isfinite(x).all()
 out=root/('src/main/resources/assets/arcanecontrol/sounds/anime/'+name+'.ogg')
 with sf.SoundFile(out,'w',samplerate=rate,channels=1,format='OGG',subtype='VORBIS') as writer:
  for i in range(0,len(x),4096):writer.write(x[i:i+4096])
 decoded,sr=sf.read(out,dtype='float32');assert sr==rate and np.isfinite(decoded).all() and np.max(np.abs(decoded))<1
 print(name,round(len(x)/rate,2),'seconds / 48 kHz mono / decoded peak',round(float(np.max(np.abs(decoded))),3))

rng=np.random.default_rng(20261103);t=np.arange(round(4.5*rate))/rate;u=t/4.5
low=noise(rng,len(t),650,.5);air=noise(rng,len(t),8000,.05)
# A single evolving swell: no repeating amplitude oscillator.
x=(low*(.22-.1*u)+air*(.025+.17*u*u))*(.08+.92*u**1.6)
x+=.025*np.sin(2*np.pi*(38*t+15*t*t))*(u**2)
x*=np.minimum(1,t/.12)*np.clip((4.25-t)/.35,0,1)
write('purple_maximum_charge',x)

rng=np.random.default_rng(20261104);t=np.arange(round(2.4*rate))/rate
low=noise(rng,len(t),450,.45);air=noise(rng,len(t),9000,.02);rumble=noise(rng,len(t),100,.6)
# The pitched sub-transient ends quickly; the tail is diffuse and aperiodic.
phase=2*np.pi*(25*t+55*(1-np.exp(-t*14))/14)
x=.65*np.sin(phase)*np.exp(-t*16)+.22*air*np.exp(-t*24)
x+=.38*low*np.exp(-t*2.7)+.25*rumble*np.exp(-t*3.2)
x*=(1-np.exp(-t*2000))*np.clip((2.4-t)/.25,0,1)
write('purple_maximum_release',x)
