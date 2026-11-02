"""Original procedural SFX. Tools only: numpy 2.5.4, soundfile 0.14.0. No sampled media."""
from pathlib import Path
import argparse,hashlib,json
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/arcanecontrol'
RATE=48000
NAMES=['atomic_charge','atomic_release','divider_charge','divider_release','purple_charge','purple_release','frieren_cast','frieren_barrier','singularity_charge','singularity_collapse']
DURATIONS=[4.5,3.5,.9,.9,1.5,1.6,.55,.6,3.5,2.5]
PT=['Energia Atomic se concentra','Atomic detona','Lâmina de antimagia se estende','Black Divider corta','Azul e Vermelho convergem','Vazio Roxo dispara','Zoltraak dispara','Barreira mágica ressoa','Singularidade se forma','Singularidade colapsa']
EN=['Atomic energy gathers','Atomic detonates','Antimagic blade extends','Black Divider slashes','Blue and Red converge','Hollow Purple fires','Zoltraak fires','Magic barrier resonates','Singularity forms','Singularity collapses']

def synth(name,duration):
    t=np.arange(round(duration*RATE))/RATE;u=t/duration
    rng=np.random.default_rng(7719+sum((i+1)*ord(c)for i,c in enumerate(name)))
    white=rng.standard_normal(len(t));freq=np.fft.rfftfreq(len(t),1/RATE)
    def noise(cut,power):
        weight=(1+freq/80)**(-power)/(1+(freq/cut)**8)
        x=np.fft.irfft(np.fft.rfft(white)*weight,n=len(t));return x/max(1e-8,np.std(x))
    low=noise(1300,.75);air=noise(10000,.08)
    def tone(f0,f1):return np.sin(2*np.pi*(f0*t+(f1-f0)*t*t/(2*duration)))
    if name.endswith('charge'):
        f0,f1=(65,290)if name.startswith('atomic')else(90,540)if name.startswith('purple')else(48,120)if name.startswith('singularity')else(130,65)
        pulse=.7+.3*np.sin(2*np.pi*(3*t+u*u*7))**2
        x=(.24*tone(f0,f1)+.12*tone(f0*2.01,f1*2.01)+.14*low+.08*air*u)*(.15+.85*u**1.5)*pulse
    elif name=='atomic_release':
        shock=.52*tone(68,24)*np.exp(-t*4.8)+.25*air*np.exp(-t*35)
        delay=np.maximum(0,t-.18);after=(t>=.18)*(1-np.exp(-delay*30))*np.exp(-delay*1.4)
        x=shock+.24*low*after+.045*tone(600,95)*np.exp(-t*2.2)
    elif name=='frieren_barrier':
        x=sum(np.sin(2*np.pi*f*t)*np.exp(-t*(5+i))*.12 for i,f in enumerate([620,947,1530,2371]))+.10*air*np.exp(-t*25)
    elif name=='frieren_cast':
        x=.27*tone(1600,430)*np.exp(-t*12)+.22*air*np.exp(-t*19)+.12*low*np.exp(-t*9)
    else:
        punch=.4*tone(110 if name.startswith('divider')else 65,28)*np.exp(-t*8)
        crack=.25*air*np.exp(-t*20)
        tail=.22*low*np.exp(-t*(2.8 if name.startswith('divider')else 1.7))
        shimmer=.09*tone(900,180)*np.exp(-t*3)
        x=punch+crack+tail+shimmer
    if name=='atomic_charge':
        # Last 180 ms are deliberately quiet, making the tick-90 transient legible.
        q=np.clip((t-4.12)/.2,0,1);x*=1-q*q*(3-2*q)
    # Smooth edges and bounded peaks; mono preserves spatial attenuation in Minecraft/OpenAL.
    fade=np.minimum(1,t/.012)*np.minimum(1,(duration-t)/.07)
    x=np.tanh(x*1.2)*fade
    x*=min(1,.82/max(1e-8,np.max(np.abs(x))))
    return x.astype('float32')

def build(preview=None,atomic_only=False):
    folder=ASSETS/'sounds/anime';folder.mkdir(parents=True,exist_ok=True)
    definitions=json.loads((ASSETS/'sounds.json').read_text(encoding='utf-8')) if atomic_only else {}
    manifest=json.loads((ROOT/'art/audio/anime-sfx-manifest.json').read_text(encoding='utf-8')) if atomic_only else {};mix=[]
    for name,duration in zip(NAMES,DURATIONS):
        if atomic_only and not name.startswith('atomic_'):continue
        x=synth(name,duration);p=folder/(name+'.ogg');sf.write(p,x,RATE,format='OGG',subtype='VORBIS')
        decoded,sr=sf.read(p);assert sr==RATE and decoded.ndim==1 and np.isfinite(decoded).all()
        assert np.max(np.abs(decoded))<.99
        definitions[name]={'subtitle':'subtitles.arcanecontrol.'+name,'sounds':[{'name':'arcanecontrol:anime/'+name,'attenuation_distance':64 if name.startswith(('atomic','singularity'))else 32}]}
        manifest[name]={'duration':round(len(decoded)/sr,4),'sample_rate':sr,'channels':1,'peak':round(float(np.max(np.abs(decoded))),5),'rms':round(float(np.sqrt(np.mean(decoded**2))),5),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
        mix.extend([x] if atomic_only else [x,np.zeros(int(RATE*.35),dtype='float32')])
    (ASSETS/'sounds.json').write_bytes((json.dumps(definitions,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
    for language,labels in [('pt_br',PT),('en_us',EN)]:
        p=ASSETS/f'lang/{language}.json';data=json.loads(p.read_text(encoding='utf-8'))
        data.update({'subtitles.arcanecontrol.'+n:s for n,s in zip(NAMES,labels)})
        p.write_bytes((json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode('utf-8'))
    p=ROOT/'art/audio';p.mkdir(parents=True,exist_ok=True)
    (p/'anime-sfx-manifest.json').write_bytes((json.dumps(manifest,indent=2)+'\n').encode('utf-8'))
    if preview:
        p=Path(preview);p.parent.mkdir(parents=True,exist_ok=True);sf.write(p,np.concatenate(mix),RATE,subtype='PCM_16')
    print('Generated and decoded original mono Vorbis SFX; bounded peaks, subtitles and provenance manifest. Atomic-only:',atomic_only)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--preview');p.add_argument('--atomic-only',action='store_true');a=p.parse_args();build(a.preview,a.atomic_only)
