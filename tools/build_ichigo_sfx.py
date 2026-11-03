"""Original procedural SFX for the Ichigo wave. Tools only: numpy, soundfile. No sampled media.
Timings follow the VFX phases (ticks / 20). Reuses build_frieren_sfx's writer contract: updates only these entries in
sounds.json, lang files and the manifest."""
from pathlib import Path
import argparse,hashlib,json
import numpy as np
import soundfile as sf
import build_frieren_sfx as base

ROOT=base.ROOT;ASSETS=base.ASSETS;RATE=base.RATE
SONS={
 'ichigo_getsuga':(1.1,48,'Getsuga Tenshou corta o ar','Getsuga Tenshou cuts the air'),
 'ichigo_getsuga_bankai':(1.2,56,'Getsuga negro ruge','Black Getsuga roars'),
 'ichigo_shunpo':(.35,20,'Passo flash','Flash step'),
 'ichigo_golpe_pesado':(1.6,40,'Golpe pesado racha o chão','Heavy blow cracks the ground'),
 'ichigo_corte_ascendente':(.8,32,'Corte ascendente','Rising slash'),
 'ichigo_bankai':(2.4,64,'Bankai é liberado','Bankai is released'),
 'ichigo_mascara':(.9,32,'Máscara Hollow se forma','Hollow mask forms'),
 'ichigo_mugetsu':(4.2,64,'Mugetsu: as trevas engolem tudo','Mugetsu: darkness swallows everything'),
}

def synth(name,duration):
    n=round(duration*RATE);t=np.arange(n)/RATE;u=t/duration
    rng=np.random.default_rng(6060+sum((i+1)*ord(c) for i,c in enumerate(name)))
    white=rng.standard_normal(n);freq=np.fft.rfftfreq(n,1/RATE)
    def noise(lo,hi,power=0.):
        w=(1+freq/80)**(-power)/(1+(freq/hi)**8)*(1-1/(1+(freq/max(lo,1))**4))
        x=np.fft.irfft(np.fft.rfft(white)*w,n=n);return x/max(1e-8,np.std(x))
    def sweep(f0,f1,curve=1.,start=0.):
        q=np.clip((t-start)/max(1e-6,duration-start),0,1);f=f0+(f1-f0)*q**curve;return np.sin(2*np.pi*np.cumsum(f)/RATE)
    def at(s):return np.maximum(0,t-s)
    def env(a,d,start=0.):q=at(start);return (t>=start)*np.minimum(1,q/max(a,1e-4))*np.exp(-np.maximum(0,q-a)*d)
    def boom(start,f0=60,decay=3.):q=at(start);return (t>=start)*np.sin(2*np.pi*(f0*q-f0*.22*q*q))*np.exp(-q*decay)
    air=noise(2500,14000);low=noise(30,900,.6);mid=noise(400,4000);hiss=noise(4000,16000)
    if name=='ichigo_getsuga':
        # Shikai: swish of the swing, then a bright humming wave that travels away (doppler down).
        x=.38*noise(600,7000)*env(.01,9)+.22*sweep(900,380,.7)*env(.02,2.4,.05)+.16*sweep(1800,760,.7)*env(.02,2.8,.05)+.18*low*env(.02,3,.05)
    elif name=='ichigo_getsuga_bankai':
        grit=np.tanh(3*(.5*sweep(220,70,.6)+.4*low))
        x=.30*noise(500,6000)*env(.006,10)+.34*grit*env(.02,2.2,.03)+.25*boom(.03,55,3.4)+.10*hiss*env(.005,12,.03)
    elif name=='ichigo_shunpo':
        x=.55*noise(1500,12000)*env(.002,30)+.25*sweep(3000,600,.5)*env(.002,22)
    elif name=='ichigo_golpe_pesado':
        # Swing whoosh, slam on 0.6 s (tick 12 from the cast is the impact; this cue plays on the impact), rocks.
        x=.55*boom(0,48,2.4)+.40*mid*env(.002,16)+.25*low*env(.02,1.6)
        for i in range(22):
            s=rng.uniform(.08,1.1);x+=(t>=s)*.10*noise(800,5000)*np.exp(-at(s)*rng.uniform(18,40))
    elif name=='ichigo_corte_ascendente':
        x=.42*noise(700,9000)*env(.04,6)*(.5+.5*np.sin(2*np.pi*3*t))+.22*sweep(400,1400,.8)*env(.03,4)
    elif name=='ichigo_bankai':
        # Pressure building until the shout on tick 14 (0.7 s), pillar roar, wind of the dust, cut at tick 30 (1.5 s).
        g=np.clip(t/.7,0,1)
        x=(.24*low+.12*sweep(40,90))*g**2*(t<.72)
        x+=.62*boom(.7,42,1.6)+.40*noise(80,5000)*env(.01,2.2,.7)+.16*np.tanh(2*sweep(110,60))*env(.05,1.2,.7)
        x+=.22*noise(300,3000)*env(.1,1.8,.9)
        x+=.35*noise(1200,12000)*env(.003,14,1.5)+.2*sweep(2600,900,.5,1.5)*env(.003,8,1.5)
    elif name=='ichigo_mascara':
        cr=(rng.random(n)<.006)*rng.uniform(-1,1,n);cr=np.convolve(cr,np.exp(-np.arange(200)/30),'same')
        x=.5*cr*np.clip(t/.6,0,1)+.18*np.tanh(3*sweep(70,140))*env(.3,3)+.35*noise(800,8000)*env(.002,14,.62)
    elif name=='ichigo_mugetsu':
        # Low drone while the darkness gathers (0-2.5 s), a hair-thin high "shing" for the cut on tick 50,
        # one beat of near silence, then the colossal boom and a long rumble of falling darkness.
        g=np.clip(t/2.4,0,1)
        x=(.22*low+.10*sweep(32,55)+.05*np.sin(2*np.pi*27*t))*g**1.4*(t<2.5)
        x+=(t>=2.5)*.30*(np.sin(2*np.pi*5200*at(2.5))+.5*np.sin(2*np.pi*7800*at(2.5)))*np.exp(-at(2.5)*9)
        x+=.75*boom(2.75,34,1.1)+.45*noise(40,2500,.6)*env(.05,.9,2.75)+.12*hiss*env(.01,3,2.75)
    else:raise ValueError(name)
    fade=np.minimum(1,t/.006)*np.minimum(1,(duration-t)/.06)
    x=np.tanh(x*1.2)*fade;x*=min(1,.6/max(1e-8,np.max(np.abs(x))))
    return x.astype('float32')

def build(preview=None):
    # base.build prints its own (Frieren) label; the files written are the Ichigo ones listed in SONS.
    base.SONS=SONS;base.synth=synth;base.build(preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
