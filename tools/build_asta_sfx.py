"""Asta's own SFX on the layered engine (tools/sfx_motor.py). Antimagic is gritty and dark: distorted low crackle,
heavy greatsword whooshes and clangs, no shimmer. Each skill is split into the sounds of its moments; the client
schedules them on the VFX ticks (RoteiroAudio.asta, from PerfilAsta / BlackMeteoriteAsta / BlackHurricaneAsta /
UltAsta / PerfilCinematico), so every hit lands with its picture.
"""
import argparse
import numpy as np
import sfx_motor as m

SONS={
 'asta_brandir':(.45,24,'Espada gigante corta o ar','Giant sword cuts the air'),
 'asta_impacto':(.9,32,'Golpe pesado de antimagia','Heavy antimagic blow'),
 'asta_corte':(1.0,40,'Corte de antimagia avança','Antimagic slash flies'),
 'asta_investida':(.6,32,'Asta avança como um touro','Asta charges like a bull'),
 'asta_barreira':(5.0,32,'Barreira de antimagia','Antimagic barrier'),
 'asta_reflexo':(.5,32,'Magia rebatida','Magic deflected'),
 'asta_salto':(.6,32,'Asta salta sobre o alvo','Asta leaps at the target'),
 'asta_meteoro':(1.8,64,'Black Meteorite esmaga o chão','Black Meteorite smashes the ground'),
 'asta_furacao':(.55,40,'Antimagia gira em um furacão','Antimagic spins into a hurricane'),
 'asta_estouro':(1.4,48,'Black Hurricane explode','Black Hurricane bursts'),
 'asta_ult_espadas':(.95,48,'As espadas se libertam','The swords break free'),
 'asta_ult_corte':(.5,48,'Corte da ultimate','Ultimate slash'),
 'asta_ult_final':(2.4,64,'Golpe final em X','Final X strike'),
 'asta_forma_carga':(2.4,48,'O demônio desperta','The devil awakens'),
 'asta_forma_rugido':(2.2,64,'Assimilação Demoníaca','Devil Union'),
}

def antimagia(s,ini,fim,forca=1.):
    """The signature: black crackle with a gritty low growl (distorted, rough, never clean)."""
    rosnado=np.tanh(4*s.passa_baixa(s.ruido(30,900,.8),300))*s.janela(ini,fim,.04)
    return forca*(.45*rosnado+.35*s.estalos(ini,fim,90,2500)+.15*np.tanh(3*s.ruido(400,4000,.3))*s.janela(ini,fim,.04))

def brandir(s,ini=0.,dur=.3,f0=900,f1=180):
    """Whoosh of a greatsword: a band of noise sweeping down, shaped by a quick swell."""
    w=s.passa_baixa(s.ruido(150,6000,.4),2500)*s.env(ini,dur*.45,9)
    return .55*w+.2*s.tom(f0,f1,.6,ini,ini+dur)*s.env(ini,dur*.4,10)

def sintese(nome,s):
    if nome=='asta_brandir':return brandir(s)+.25*antimagia(s,0,.35,.6)
    if nome=='asta_impacto':
        x=1.0*s.transiente(0,5000,.01)+.95*s.sub(0,75,30,4,3.5)+.45*s.tom(420,160,.5,0,.15,(1,2.4,3.9))*s.env(0,.002,14)
        return s.reverb(x+antimagia(s,0,.5,.8),1.1,.25,3500)
    if nome=='asta_corte':
        x=brandir(s,0,.35,1200,220)+.7*s.transiente(.05,6000,.008)+.5*s.sub(.05,60,30,5)
        return s.reverb(x+antimagia(s,.05,.8,.9),1.2,.25,3500)
    if nome=='asta_investida':
        x=.6*s.passa_baixa(s.ruido(60,4000,.6),1200)*s.env(0,.15,5)+.4*s.sub(0,45,30,3,2)+.3*antimagia(s,0,.5,.7)
        return x
    if nome=='asta_barreira':
        # A dark dome: a low throb at ~2 Hz with antimagic crackle all over, opening hit, soft close.
        g=s.janela(0,s.dur,.35);pulso=.6+.4*np.sin(2*np.pi*2.1*s.t)**2
        x=(.4*np.tanh(2*s.tom(48,harmonicos=(1,2,3)))*pulso+antimagia(s,.05,s.dur,.7))*g+.7*s.transiente(0,3500,.015)+.6*s.sub(0,60,35,3)
        return s.reverb(x,1.0,.2,3000)
    if nome=='asta_reflexo':
        x=.9*s.transiente(0,7000,.006)+.4*s.tom(1800,900,.7,0,.3,(1,2.7,4.1))*s.env(0,.002,9)+.3*s.estalos(0,.25,120,4000)
        return s.reverb(x,.8,.25,7000)
    if nome=='asta_salto':
        return .55*s.passa_baixa(s.ruido(100,5000,.4),2000)*s.crescer(0,.45,1.5)*s.janela(0,.58,.04)+.3*antimagia(s,.1,.58,.6)
    if nome=='asta_meteoro':
        # Slam: crack, enormous sub, cracking ground (rocks), dust roar.
        x=1.0*s.transiente(0,4500,.012)+1.0*s.sub(0,80,24,2,4)
        for i in range(26):
            a=s.rng.uniform(.04,1.2);x+=.12*s.transiente(a,s.rng.uniform(1500,5000),.006,s.rng.uniform(.3,1))
        x+=.35*s.passa_baixa(s.ruido(40,2500,.8),900)*s.env(.02,.05,1.8)+antimagia(s,0,1.2,.9)
        return s.reverb(x,2.0,.3,3000)
    if nome=='asta_furacao':
        return s.girar(s.passa_baixa(s.ruido(100,5000,.4),2200),hz=7,prof=.9)*s.crescer(0,.5,1.4)*s.janela(0,.53,.03)+.4*antimagia(s,0,.53,.8)
    if nome=='asta_estouro':
        x=1.0*s.transiente(0,5000,.01)+.85*s.sub(0,70,28,3,3)
        x+=.6*s.girar(s.ruido(150,6000,.5),hz=9,prof=.7)*s.env(0,.01,3.2)+antimagia(s,0,1.0,.9)
        return s.reverb(x,1.6,.3,3500)
    if nome=='asta_ult_espadas':
        # Five swords torn out of the grimoire: metallic scrapes rising, then a dark chord when they're out.
        x=sum(.25*s.tom(f,f*1.6,.7,.08*i,.08*i+.5,(1,2.4,3.6))*s.env(.08*i,.05,4) for i,f in enumerate([180,220,260,310,370]))
        x+=antimagia(s,0,.9,.8)+.5*s.sub(.6,55,30,3)
        return s.reverb(x,1.4,.3,4000)
    if nome=='asta_ult_corte':
        x=brandir(s,0,.18,1500,300)+.8*s.transiente(.03,6000,.006)+.55*s.sub(.03,70,35,6)+.4*antimagia(s,.03,.35,.7)
        return s.reverb(x,.9,.22,4000)
    if nome=='asta_ult_final':
        # The X: two slashes crossing a beat apart, then the huge black detonation and a long rumble.
        x=brandir(s,0,.2,1600,300)+brandir(s,.1,.2,1500,260)+1.0*s.transiente(.18,4500,.012)+1.0*s.sub(.18,90,22,1.4,4)
        x+=.4*s.passa_baixa(s.ruido(40,2500,.8),900)*s.env(.2,.05,1.2)+antimagia(s,.18,2.0,1.0)
        return s.reverb(x,2.6,.35,3000)
    if nome=='asta_forma_carga':
        # Devil Union building: a heartbeat that speeds up under a rising growl and crackle.
        x=np.zeros(s.n)
        for b in [0,.55,1.0,1.38,1.68,1.92,2.12,2.28]:x+=.55*s.sub(b,55,38,9,2)
        x+=(.5*antimagia(s,0,s.dur,1.)+.25*np.tanh(3*s.tom(40,95,1.6)))*s.crescer(0,s.dur,1.8)
        return s.reverb(x,1.2,.25,3000)
    if nome=='asta_forma_rugido':
        # The release on FORMA_IMPACTO: a monstrous distorted roar, big hit, ground and air shaking after.
        x=1.0*s.transiente(0,4000,.015)+1.0*s.sub(0,85,26,1.5,4)
        rugido=np.tanh(5*(s.tom(110,70,.7,0,1.6,(1,1.5,2,3))+.6*s.passa_baixa(s.ruido(60,2000,.6),700)))*s.env(0,.06,1.4)
        x+=.55*rugido+antimagia(s,0,2.0,1.0)
        return s.reverb(x,2.4,.35,3000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Asta SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
