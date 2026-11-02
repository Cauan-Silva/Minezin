"""Original procedural SFX for the Frieren wave and Qual. Tools only: numpy, soundfile. No sampled media.
Timings follow the approved VFX phases (ticks / 20). Updates only these entries in sounds.json, lang files and the manifest."""
from pathlib import Path
import argparse,hashlib,json
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'
RATE=48000
# name: (duration s, attenuation distance, pt_br subtitle, en_us subtitle)
SONS={
 'frieren_cast':(.9,32,'Zoltraak dispara','Zoltraak fires'),
 'frieren_barrier':(.8,32,'Barreira mágica se forma','Magic barrier forms'),
 'frieren_barrier_break':(.9,32,'Barreira mágica se estilhaça','Magic barrier shatters'),
 'frieren_impulso':(.7,24,'Rajada de vento','Gust of wind'),
 'frieren_buraco_carga':(3.5,64,'Buraco negro se forma na mão','Black hole forms in hand'),
 'frieren_buraco_atracao':(7.5,64,'Buraco negro atrai tudo','Black hole pulls everything'),
 'frieren_buraco_colapso':(3.0,64,'Buraco negro explode','Black hole bursts'),
 'frieren_mana':(3.0,48,'Mana é liberada','Mana is released'),
 'frieren_julgamento_carga':(1.25,48,'Relâmpagos se acumulam','Lightning gathers'),
 'frieren_julgamento_trovao':(2.0,64,'Relâmpago cai','Lightning strikes'),
 'frieren_escudo':(1.2,40,'Escudo celestial se ergue','Celestial shield rises'),
 'frieren_gaiola':(6.0,40,'Tornado uiva','Tornado howls'),
 'qual_zoltraak_carga':(1.5,48,'Qual concentra magia','Qual gathers magic'),
 'qual_zoltraak':(1.2,64,'Zoltraak original dispara','Original Zoltraak fires'),
}

def synth(name,duration):
    n=round(duration*RATE);t=np.arange(n)/RATE;u=t/duration
    rng=np.random.default_rng(4049+sum((i+1)*ord(c) for i,c in enumerate(name)))
    white=rng.standard_normal(n);freq=np.fft.rfftfreq(n,1/RATE)
    def noise(lo,hi,power=0.):
        w=(1+freq/80)**(-power)/(1+(freq/hi)**8)*(1-1/(1+(freq/max(lo,1))**4))
        x=np.fft.irfft(np.fft.rfft(white)*w,n=n);return x/max(1e-8,np.std(x))
    def sweep(f0,f1,curve=1.):
        f=f0+(f1-f0)*u**curve;return np.sin(2*np.pi*np.cumsum(f)/RATE)
    def at(s):return np.maximum(0,t-s)
    def env(a,d,start=0.):q=at(start);return (t>=start)*np.minimum(1,q/max(a,1e-4))*np.exp(-np.maximum(0,q-a)*d)
    def chime(f,start,decay,amp=.12):q=at(start);return (t>=start)*amp*(np.sin(2*np.pi*f*q)+.35*np.sin(2*np.pi*f*2.76*q))*np.exp(-q*decay)
    air=noise(2500,14000);low=noise(30,900,.6);mid=noise(400,4000)
    if name=='frieren_cast':
        x=sum(chime(f,.0,18,.08) for f in [1320,1980,2640])
        x+=.32*sweep(2400,520,.6)*env(.004,9,.04)+.30*air*env(.003,22,.04)+.14*low*env(.01,7,.04)
    elif name=='frieren_barrier':
        x=sum(chime(f,.04*i,7,.09) for i,f in enumerate([880,988,1175,1319,1480,1760,1976]))
        x+=.07*np.sin(2*np.pi*220*t)*env(.15,2.5)+.05*air*env(.01,30)
    elif name=='frieren_barrier_break':
        x=.38*air*env(.002,14)+.18*mid*env(.004,9)
        for i in range(26):
            s=rng.uniform(0,.45);f=rng.uniform(1800,6200);x+=chime(f,s,rng.uniform(14,30),rng.uniform(.03,.07))
    elif name=='frieren_impulso':
        sw=noise(300,3000)*(.5+.5*np.sin(2*np.pi*(2+6*u)*t));x=.42*sw*env(.05,4.5)+.12*low*env(.02,6)
    elif name=='frieren_buraco_carga':
        swell=np.clip(t/2.5,0,1)**1.5
        hum=.22*sweep(48,92)+.10*sweep(96,184)+.12*low
        crackle=(rng.random(n)<.0009*(.3+swell))*rng.uniform(-1,1,n);crackle=np.convolve(crackle,np.exp(-np.arange(240)/40),'same')*.5
        x=(hum*(.1+.9*swell)+crackle)
        # Launch at 2.5 s (tick 50): rising whoosh into the flight.
        q=np.clip((t-2.5)/1.0,0,1);x+=(t>=2.5)*.35*noise(200,6000)*q**1.3*(1-q*.4)
    elif name=='frieren_buraco_atracao':
        # Arrival at tick 70 (start), steady pull, tension in the last 1.2 s (ticks 196-220).
        boom=.55*np.sin(2*np.pi*(55*t-14*t*t))*np.exp(-t*3.5)+.25*air*np.exp(-t*18)
        tens=np.clip((t-6.3)/1.2,0,1)
        rumble=(.20*low+.06*np.sin(2*np.pi*36*t))*(.7+.3*np.sin(2*np.pi*.6*t))*np.minimum(1,t/.6)
        howl=.10*noise(250,1800)*(.6+.4*np.sin(2*np.pi*(.35+.6*tens)*t))
        whine=.12*tens**2*sweep(300,1400,3.)
        cracks=(rng.random(n)<.004*tens**2)*rng.uniform(-1,1,n);cracks=np.convolve(cracks,np.exp(-np.arange(400)/60),'same')*.9
        x=boom+rumble+howl+whine+cracks
        x*=1-np.clip((t-7.35)/.15,0,1)
    elif name=='frieren_buraco_colapso':
        suck=.30*noise(80,3000)*np.clip(t/.12,0,1)**2*(t<.14)
        burst=.62*np.sin(2*np.pi*(48*at(.14)-9*at(.14)**2))*env(.004,2.6,.14)+.40*air*env(.002,10,.14)
        zaps=np.zeros(n)
        for i in range(10):
            s=.34+rng.uniform(0,.75);zaps+=(t>=s)*.16*np.sign(np.sin(2*np.pi*rng.uniform(70,140)*at(s)))*mid*np.exp(-at(s)*28)
        sparkle=sum(chime(rng.uniform(2600,5200),rng.uniform(.6,2.4),rng.uniform(5,9),.035) for _ in range(18))
        x=suck+burst+zaps+sparkle+.16*low*env(.05,1.6,.14)
    elif name=='frieren_mana':
        swell=np.sin(np.pi*np.clip(t/2.6,0,1))**.8
        pad=sum(np.sin(2*np.pi*f*t+i)*a for i,(f,a) in enumerate([(392,.06),(587.3,.05),(784,.04),(1174.7,.03),(395,.04)]))
        x=(.26*noise(60,1600,.4)+pad+.05*air)*swell+.3*np.sin(2*np.pi*(40*t+8*t*t))*env(.4,1.4)
    elif name=='frieren_julgamento_carga':
        g=np.clip(t/1.2,0,1)
        sparks=(rng.random(n)<.002+.02*g**2)*rng.uniform(-1,1,n);sparks=np.convolve(sparks,np.exp(-np.arange(160)/20),'same')
        x=.6*sparks+.10*mid*g**2+.08*sweep(120,520,2.)*g
    elif name=='frieren_julgamento_trovao':
        crack=.55*noise(800,12000)*env(.001,26)+.35*mid*env(.002,12)
        q=at(.05);roll=(t>=.05)*(.30*low*(.6+.4*np.sin(2*np.pi*3.2*q)))*np.exp(-q*1.6)
        x=crack+roll+.4*np.sin(2*np.pi*(60*t-15*t*t))*env(.003,4)
    elif name=='frieren_escudo':
        x=sum(chime(f,.03*i,4.5,.07) for i,f in enumerate([2093,1760,1568,1319,1175,988,880,784,659,587]))
        x+=.06*air*env(.3,2)+.05*np.sin(2*np.pi*196*t)*env(.2,1.6)
    elif name=='frieren_gaiola':
        lfo=.6+.4*np.sin(2*np.pi*(.7*t+.3*np.sin(2*np.pi*.23*t)))
        howl=noise(250,2200)*lfo;whistle=.06*np.sin(2*np.pi*(640+120*np.sin(2*np.pi*.5*t))*t)
        x=(.34*howl+.14*low+whistle+.06*air)*np.minimum(1,t/.4)*(1-np.clip((t-5.25)/.75,0,1))
    elif name=='qual_zoltraak_carga':
        g=np.clip(t/1.45,0,1)
        grain=(rng.random(n)<.004+.01*g)*rng.uniform(-1,1,n);grain=np.convolve(grain,np.exp(-np.arange(120)/18),'same')
        x=(.24*sweep(42,88)+.12*np.sign(sweep(84,176))*.4+.12*low)*(.15+.85*g**1.4)+.35*grain
    elif name=='qual_zoltraak':
        harsh=np.tanh(3*(.5*sweep(160,55,.5)+.4*low))*.35
        grain=(rng.random(n)<.02)*rng.uniform(-1,1,n);grain=np.convolve(grain,np.exp(-np.arange(90)/14),'same')
        x=(harsh+.35*grain*np.exp(-t*2.2)+.25*mid*env(.002,14))*np.exp(-t*1.4)+.3*np.sin(2*np.pi*(70*t-20*t*t))*env(.003,5)
    else:raise ValueError(name)
    fade=np.minimum(1,t/.006)*np.minimum(1,(duration-t)/.06)
    # Sparse transients ring a little after Vorbis encoding: keep headroom below the decoded .99 bound.
    x=np.tanh(x*1.2)*fade;x*=min(1,.72/max(1e-8,np.max(np.abs(x))))
    return x.astype('float32')

def build(preview=None):
    folder=ASSETS/'sounds/anime';folder.mkdir(parents=True,exist_ok=True)
    sounds=json.loads((ASSETS/'sounds.json').read_text(encoding='utf-8'))
    mpath=ROOT/'art/audio/anime-sfx-manifest.json';manifest=json.loads(mpath.read_text(encoding='utf-8')) if mpath.exists() else {}
    mix=[]
    for name,(duration,distance,pt,en) in SONS.items():
        x=synth(name,duration);p=folder/(name+'.ogg')
        with sf.SoundFile(p,mode='w',samplerate=RATE,channels=1,format='OGG',subtype='VORBIS') as encoder:
            for start in range(0,len(x),4096):encoder.write(x[start:start+4096])
        decoded,sr=sf.read(p);assert sr==RATE and decoded.ndim==1 and np.isfinite(decoded).all() and np.max(np.abs(decoded))<.99,(name,float(np.max(np.abs(decoded))))
        sounds[name]={'subtitle':'subtitles.arcanecontrol.'+name,'sounds':[{'name':'arcanecontrol:anime/'+name,'attenuation_distance':distance}]}
        manifest[name]={'duration':round(len(decoded)/sr,4),'sample_rate':sr,'channels':1,'peak':round(float(np.max(np.abs(decoded))),5),'rms':round(float(np.sqrt(np.mean(decoded**2))),5),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
        mix.extend([x,np.zeros(int(RATE*.4),dtype='float32')])
    (ASSETS/'sounds.json').write_bytes((json.dumps(sounds,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
    for language,idx in [('pt_br',2),('en_us',3)]:
        p=ASSETS/f'lang/{language}.json';data=json.loads(p.read_text(encoding='utf-8'))
        data.update({'subtitles.arcanecontrol.'+n:v[idx] for n,v in SONS.items()})
        p.write_bytes((json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
    mpath.parent.mkdir(parents=True,exist_ok=True);mpath.write_bytes((json.dumps(manifest,indent=2)+'\n').encode('utf-8'))
    if preview:p=Path(preview);p.parent.mkdir(parents=True,exist_ok=True);sf.write(p,np.concatenate(mix),RATE,subtype='PCM_16')
    print('Frieren/Qual SFX:',len(SONS),'mono Vorbis files decoded with bounded peaks.')
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
