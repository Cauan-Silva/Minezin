"""Killua's SFX on the layered engine (tools/sfx_motor.py): CC0 recordings (art/audio/cc0) for thunder, metal, air and
whooshes; procedural synthesis for the electricity itself (dense crackle, a detuned mains buzz, bright transients).
Nen electricity is dry and sharp, blue-white: crackle on top, thunder underneath only on the big hits. Every internal
hit sits on the tick of its VFX phase in the pure classes (core/vfx/Killua*); the client schedules them in
EfeitosKillua on the packet of each moment.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK
# --- ticks copied from the pure classes ---
NK_SALTO,NK_DESCARGA,NK_IMPACTO,NK_POUSO,NK_FIM=10,15,16,32,46           # KilluaBase.Narukami
EC_PREPARO,EC_TELEPORTE,EC_FIM=4,14,32                                    # KilluaBase.Echo
YO_LANCA,YO_GIRO,YO_RECOLHE=6,20,8                                        # KilluaBase.Yoyo
GA_SOME,GA_GOLPE,GA_SANGRA=8,48,30                                        # KilluaBase.Garras
GS_CARGA,GS_ENTRADA=14,30                                                 # KilluaGodspeed
RP_PREPARO,RP_DISPARO=7,4                                                 # KilluaRelampagoPerfurante
WH_ACERTO,WH_REAPARECE,WH_GOLPE=7,9,12                                    # KilluaWhirlpool (contra: relativo ao contra-ataque)
SO_CARGA,SO_IMPACTO=8,11                                                  # KilluaSocoRelampago
PU_CARGA,PU_EXPANSAO=16,9                                                 # KilluaPulsoEletromagnetico
TD_PRENDER,TD_GOLPES,TD_INTERVALO=8,12,3                                  # KilluaTempestadeDivina
TD_FIM_GOLPES=TD_PRENDER+TD_GOLPES*TD_INTERVALO;TD_IMPACTO=TD_FIM_GOLPES+10+4

SONS={
 'killua_narukami':(NK_FIM*T+.6,28,'Narukami: palma relâmpago','Narukami: lightning palm'),
 'killua_echo':(EC_FIM*T,18,'Ritmo de eco: passos borrados','Rhythm Echo: blurred steps'),
 'killua_yoyo':((YO_LANCA+YO_GIRO+YO_RECOLHE)*T+.4,20,'Yo-yos de aço giram','Steel yo-yos spin'),
 'killua_garras':((GA_SOME+12)*T,16,'Killua some nas sombras','Killua vanishes'),
 'killua_garras_golpe':(1.6,18,'Garras de assassino','Assassin claws'),
 'killua_godspeed':(GS_ENTRADA*T+1.2,40,'Godspeed: o corpo vira relâmpago','Godspeed: the body turns to lightning'),
 'killua_relampago':(2.0,32,'Relâmpago perfurante','Piercing lightning'),
 'killua_whirlpool':(.9,18,'Guarda elétrica (Whirlpool)','Electric guard (Whirlpool)'),
 'killua_contra':(1.4,24,'Contra-ataque elétrico','Electric counter'),
 'killua_soco':(1.6,28,'Soco relâmpago','Lightning punch'),
 'killua_pulso':(2.4,36,'Pulso eletromagnético','Electromagnetic pulse'),
 'killua_tempestade':(TD_IMPACTO*T+2.4,64,'Tempestade divina','Divine storm'),
}

LUFADAS=['swishes/swish-3.wav','swishes/swish-4.wav','swishes/swish-7.wav','swishes/swish-8.wav','swishes/swish-9.wav']
TROVAO='rubberduck_v2/sfx100v2_thunder_01.ogg'
CAMPO=[f'kenney_scifi/forceField_00{i}.ogg' for i in range(4)]
METAL='kenney_scifi/impactMetal_002.ogg'

def zap(s,ini,fim,forca=1.,brilho=9000):
    """Electricity (v2, louder per user): two crackle layers (bright snaps + denser mid fizz), a hard-clipped detuned
    buzz that stutters, and a high sizzle gated in random bursts, like a live arc."""
    buzz=(s.tom(118,harmonicos=(1,2,3,5,7,9),pesos=[1,.7,.55,.4,.3,.2])+s.tom(121.5,harmonicos=(1,3,5,7)))/2
    gate=np.repeat(s.rng.random(s.n//480+1)>.35,480)[:s.n].astype(float)                     # stutter every 10 ms
    gate=np.convolve(gate,np.ones(48)/48,'same')
    chiado=s.ruido(3500,14000,0.)*gate
    x=1.2*s.estalos(ini,fim,170,brilho)+.6*s.estalos(ini,fim,70,3500)
    x+=(.45*np.tanh(6*s.girar(buzz,hz=13,prof=.8))*(.5+.5*gate)+.35*chiado)*s.janela(ini,fim,.015)
    return forca*x
def estalo(s,ini,forca=1.):
    """One sharp discharge: transient + loud crackle burst + a short sizzle tail."""
    return forca*(1.1*s.transiente(ini,10000,.008)+1.0*s.estalos(ini,ini+.12,320,10000)+.5*s.estalos(ini,ini+.35,90,4000)*s.env(ini,.002,.35))
def trovao(s,ini,forca=1.,tom=1.):
    return .75*forca*(s.passa_baixa(s.amostra(TROVAO,ini,1,tom,corte=s.dur-ini),2200)+.6*s.sub(ini,80,30,2.2,3))
def whoosh(s,ini,tom=1.,forca=1.):return forca*s.amostra(LUFADAS,ini,1,tom)

def sintese(nome,s):
    if nome=='killua_narukami':
        d,i,p=NK_DESCARGA*T,NK_IMPACTO*T,NK_POUSO*T
        x=.6*whoosh(s,NK_SALTO*T*.3,.8)+zap(s,NK_SALTO*T*.5,d,.7)*s.crescer(NK_SALTO*T*.5,d,1.5)   # the star forms in the raised hand
        x+=estalo(s,d,1.2)+.9*trovao(s,i,1.0,1.2)+.5*s.amostra(CAMPO,i,1,1.6,corte=.6)                   # column of lightning strikes
        x+=zap(s,i,i+1.0,.5)*s.env(i,.01,1.2)+.4*s.sub(p,70,40,8,2)                                     # paralysis crackle, landing
        return s.reverb(x,1.4,.25,6000)
    if nome=='killua_echo':
        x=np.zeros(s.n)
        for k in range(5):x+=.45*whoosh(s,(EC_PREPARO+k*2)*T,s.rng.uniform(1.2,1.6))                      # after-images
        x+=.7*s.transiente(EC_TELEPORTE*T,5000,.01)+.6*whoosh(s,EC_TELEPORTE*T,.7)
        return s.reverb(x,1.0,.2,7000)
    if nome=='killua_yoyo':
        l,g=YO_LANCA*T,(YO_LANCA+YO_GIRO)*T
        x=.7*whoosh(s,0,.9)+.6*s.amostra(METAL,l,1,1.1)
        for k in range(8):x+=.5*whoosh(s,l+k*(g-l)/8,s.rng.uniform(.5,.65))                              # 50 kg yo-yos humming around
        x+=.25*s.passa_baixa(s.tom(90,harmonicos=(1,2,3)),900)*s.janela(l,g,.05)+.6*s.amostra(METAL,g+YO_RECOLHE*T,1,1.5)
        return s.reverb(x,1.1,.2,5000)
    if nome=='killua_garras':
        so=GA_SOME*T
        x=.5*s.passa_baixa(s.amostra('rubberduck_v2/sfx100v2_air_01.ogg',0,1,1.3,corte=so+.3),3000)        # vanishes
        x+=.25*s.passa_baixa(s.ruido(60,800,.8),400)*s.env(so*.5,.1,.5)
        return s.reverb(x,.9,.2,6000)
    if nome=='killua_garras_golpe':
        g=.15                                                                                               # the cut sits on the packet
        for k in range(3):x=(x if k else np.zeros(s.n))+.7*whoosh(s,k*.07,s.rng.uniform(1.5,1.9))       # three claw slashes
        x+=.6*s.amostra(METAL,g,1,2.2)+.35*s.amostra('kenney_scifi/slime_000.ogg',g+.07,1,1.2,corte=.4)+.4*s.sub(g,90,40,8,2)
        return s.reverb(x,.9,.2,6000)
    if nome=='killua_godspeed':
        c,e=GS_CARGA*T,GS_ENTRADA*T
        x=zap(s,0,c,.8)*s.crescer(0,c,1.8)+.5*s.amostra(CAMPO,0,1,.7,corte=c+.2)*s.crescer(0,c,2)
        x+=1.1*estalo(s,c)+1.0*trovao(s,c,1.0,1.0)+.7*s.amostra('kenney_scifi/lowFrequency_explosion_000.ogg',c,1,.9)
        x+=zap(s,c,s.dur,.55)*s.env(c,.02,2.5,.3)                                                           # the aura keeps crackling
        return s.reverb(x,2.0,.3,7000)
    if nome=='killua_relampago':
        p,f=RP_PREPARO*T,(RP_PREPARO+RP_DISPARO)*T
        x=zap(s,0,p,.7)*s.crescer(0,p,1.5)+1.2*estalo(s,p)+.9*whoosh(s,p,1.3)+.9*trovao(s,p+.05,.9,1.3)
        x+=zap(s,f,f+.8,.4)*s.env(f,.01,1.0)                                                                 # burnt mark sizzling
        return s.reverb(x,1.6,.3,6000)
    if nome=='killua_whirlpool':
        x=.6*s.amostra(CAMPO,0,1,1.3)+zap(s,0,s.dur,.5)*s.env(0,.03,.8,.4)
        return s.reverb(x,1.0,.25,8000)
    if nome=='killua_contra':
        r,g=(WH_REAPARECE-WH_ACERTO)*T,(WH_GOLPE-WH_ACERTO)*T
        x=.6*s.transiente(0,6000,.01)+.6*whoosh(s,r,1.4)+1.1*estalo(s,g)+.8*s.amostra(METAL,g,1,.9)+.6*trovao(s,g,.7,1.5)
        return s.reverb(x,1.2,.25,6000)
    if nome=='killua_soco':
        c,i=SO_CARGA*T,SO_IMPACTO*T
        x=zap(s,0,c,.8)*s.crescer(0,c,1.6)+.6*whoosh(s,c,1.1)
        x+=1.2*estalo(s,i)+.9*s.amostra('kenney_scifi/explosionCrunch_004.ogg',i,1,1.2)+.7*trovao(s,i,.8,1.6)+.6*s.sub(i,90,35,6,3)
        return s.reverb(x,1.3,.25,6000)
    if nome=='killua_pulso':
        c=PU_CARGA*T
        x=zap(s,0,c,.7)*s.crescer(0,c,2)+.35*s.tom(300,1800,1.5,0,c,(1,2))*s.crescer(0,c,2)*s.janela(0,c+.01,.01)
        x+=1.2*estalo(s,c)+.8*s.amostra(CAMPO,c,1,.6)+.9*trovao(s,c+.02,1.0,1.1)+.8*s.sub(c,60,25,2,3)
        x+=zap(s,c,c+PU_EXPANSAO*T+.6,.5)*s.env(c,.01,1.2)
        return s.reverb(x,2.0,.3,6000)
    if nome=='killua_tempestade':
        pr,fg,im=TD_PRENDER*T,TD_FIM_GOLPES*T,TD_IMPACTO*T
        x=.9*estalo(s,0)+zap(s,0,pr,.6)
        for k in range(TD_GOLPES):a=(TD_PRENDER+k*TD_INTERVALO)*T;x+=.55*estalo(s,a,s.rng.uniform(.7,1))+.4*whoosh(s,a,s.rng.uniform(1.2,1.8))+.25*s.amostra(METAL,a,1,s.rng.uniform(1.3,1.9))
        x+=zap(s,fg,im,.8)*s.crescer(fg,im,2)+.5*s.amostra(CAMPO,fg,1,.5,corte=im-fg+.1)                   # sky charge
        x+=1.3*estalo(s,im)+1.3*trovao(s,im,1.2,.8)+1.0*s.amostra('kenney_scifi/explosionCrunch_004.ogg',im,1,.8)+1.0*s.sub(im,90,22,1.6,4)
        x+=zap(s,im,s.dur,.45)*s.env(im,.02,1.8)
        return s.reverb(x,2.6,.35,6000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Killua SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
