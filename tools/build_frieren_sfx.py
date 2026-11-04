"""Frieren's and Qual's SFX on the layered engine (tools/sfx_motor.py), replacing the first procedural set. Same
names, durations and cue timing as before (RoteiroAudio.singularidade / julgamento, EfeitosFrieren,
UltimatesTransformadas, RenderBossVfx), so sync is unchanged. Her magic is precise and ancient: crystalline chimes,
clean focused beams, soft wind; Qual's Zoltraak is the same spell, darker and colder.
Also the barrier absorbing a hit (frieren_barreira_golpe), which used a vanilla amethyst sound.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK
SONS={
 'frieren_cast':(.9,32,'Zoltraak dispara','Zoltraak fires'),
 'frieren_barrier':(.8,32,'Barreira mágica se forma','Magic barrier forms'),
 'frieren_barrier_break':(.9,32,'Barreira mágica se estilhaça','Magic barrier shatters'),
 'frieren_barreira_golpe':(.45,24,'A barreira absorve o golpe','The barrier absorbs the blow'),
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

def sino(s,ini,f,queda=3.,forca=.2):
    """A crystalline chime: inharmonic partials of a small bell."""
    return forca*s.tom(f,harmonicos=(1,2.76,5.4,8.93),pesos=[1,.5,.25,.12])*s.env(ini,.003,queda)
def feixe(s,ini,dur,f=880,frio=False):
    """A focused beam: a tight bright tone with a hiss core and a fast tremble."""
    x=s.tom(f,f*.92,1,ini,ini+dur,(1,2,3),vibrato=.006,vib_hz=31)*.5+.5*s.ruido(2500 if not frio else 1500,15000 if not frio else 9000)*.4
    return (np.tanh(2*x) if frio else x)*s.janela(ini,ini+dur,.02)

def sintese(nome,s):
    if nome=='frieren_cast':
        x=.6*s.ruido(1500,14000)*s.crescer(0,.08,2)*s.janela(0,.1,.01)+.9*s.transiente(.08,8000,.006)+feixe(s,.08,.6)*s.env(.08,.02,3)+.4*s.sub(.08,90,50,8,1.5)
        return s.reverb(x+sino(s,.08,1760,4,.15),1.4,.3,10000)
    if nome=='frieren_barrier':
        x=sum(sino(s,.04*i,f,4,.2) for i,f in enumerate([660,990,1320,1980]))+.25*s.ruido(3000,14000)*s.env(0,.15,5)
        return s.reverb(x,1.6,.4,10000)
    if nome=='frieren_barrier_break':
        x=.9*s.transiente(0,9000,.006)+sum(sino(s,s.rng.uniform(0,.25),s.rng.uniform(1500,5000),s.rng.uniform(4,9),.12) for _ in range(14))
        return s.reverb(x+.3*s.ruido(3000,16000)*s.env(0,.005,7),1.3,.35,11000)
    if nome=='frieren_barreira_golpe':
        return s.reverb(.7*s.transiente(0,6000,.008)+sino(s,0,1240,7,.3)+sino(s,0,1860,9,.15)+.3*s.sub(0,90,60,12,1.5),.9,.3,9000)
    if nome=='frieren_impulso':
        return .6*s.passa_baixa(s.ruido(150,9000,.2),4000)*s.env(0,.08,4.5)+.3*s.tom(400,900,.8,0,.5)*s.env(0,.05,5)+sino(s,0,1320,8,.1)
    if nome=='frieren_buraco_carga':
        # Formation in hand until the launch at 2.5 s (tick 50), a rising whoosh into the flight up to ABERTURA (70).
        g=s.crescer(0,2.5,1.6)
        x=(.4*s.tom(80,160,1.4,0,2.5,(1,2,3))+.3*s.girar(s.passa_baixa(s.ruido(40,1500,.8),600),hz=2,prof=.6)+.2*s.estalos(0,2.5,30,4000))*g*s.janela(0,2.55,.05)
        x+=.6*s.ruido(200,9000,.3)*s.crescer(2.4,3.4,1.5)*s.janela(2.4,3.5,.05)+.5*s.transiente(2.5,4000,.015)
        return s.reverb(x,1.6,.3,5000)
    if nome=='frieren_buraco_atracao':
        # Arrival, steady pull with debris dragged in, tension rising in the last 1.2 s before COLAPSO.
        x=(.45*s.girar(s.passa_baixa(s.ruido(30,1200,.9),500),hz=1.2,prof=.6)+.4*s.tom(55,harmonicos=(1,2,3),vibrato=.01,vib_hz=.4)+.15*s.estalos(0,s.dur,25,3000))*s.janela(0,s.dur,.2)
        x+=.4*s.tom(110,330,2,s.dur-1.2,s.dur,(1,2))*s.janela(s.dur-1.2,s.dur,.05)*s.crescer(s.dur-1.2,s.dur,2)
        return s.reverb(x,2.0,.3,4000)
    if nome=='frieren_buraco_colapso':
        x=1.0*s.transiente(0,5000,.012)+1.0*s.sub(0,85,22,1.4,4)+.5*s.passa_baixa(s.ruido(40,3000,.7),1200)*s.env(0,.02,1.5)
        x+=sum(sino(s,.1+.07*i,f,2.2,.1) for i,f in enumerate([1320,1760,2217,2637,3520]))
        return s.reverb(x,2.8,.4,6000)
    if nome=='frieren_mana':
        # Releasing suppressed mana: a vast breath of pressure and an ancient chord blooming.
        x=1.0*s.sub(0,70,30,1.2,3)+.6*s.transiente(0,3000,.02)+.4*s.ruido(200,10000,.3)*s.env(0,.3,1.2)
        x+=sum(.15*s.tom(f,harmonicos=(1,2),vibrato=.003,vib_hz=.5+.2*i)*s.env(.1+.1*i,.6,.8) for i,f in enumerate([220,277,330,440,554]))
        return s.reverb(x,3.0,.4,8000)
    if nome=='frieren_julgamento_carga':
        # Electric crackle tightening as the warning ring closes (until QUEDA, tick 25).
        g=s.crescer(0,s.dur,1.5);x=(.5*s.estalos(0,s.dur,160,3000)+.3*s.tom(150,600,1.5,0,s.dur,(1,3,5))+.2*s.ruido(2000,12000))*g
        return x*(1-.8*s.crescer(s.dur-.15,s.dur,2))
    if nome=='frieren_julgamento_trovao':
        x=1.0*s.transiente(0,9000,.004)+.7*s.estalos(0,.25,800,4000)+1.0*s.sub(.02,90,26,1.6,4)
        x+=.5*s.passa_baixa(s.ruido(30,3000,.8),900)*s.env(.05,.1,1.4)
        return s.reverb(x,3.0,.4,5000)
    if nome=='frieren_escudo':
        x=sum(sino(s,.06*i,f,2.5,.2) for i,f in enumerate([523,784,1047,1568,2093]))+.3*s.tom(262,harmonicos=(1,2,3))*s.env(0,.3,2)
        return s.reverb(x,2.0,.45,10000)
    if nome=='frieren_gaiola':
        # The wind cage: a howling vortex that never sits still, gusts and a soft whistle on top.
        x=(.55*s.girar(s.passa_baixa(s.ruido(80,6000,.3),2500),hz=1.7,prof=.8)+.2*s.tom(700,harmonicos=(1,2),vibrato=.06,vib_hz=.9)+.25*s.girar(s.ruido(1500,12000),hz=3.1,prof=.9))*s.janela(0,s.dur,.3)
        return s.reverb(x,1.4,.25,8000)
    if nome=='qual_zoltraak_carga':
        g=s.crescer(0,s.dur,1.6);x=(.45*np.tanh(2*s.tom(110,440,1.3,0,s.dur,(1,2,3)))+.3*s.passa_baixa(s.ruido(100,4000,.5),1500)+.2*s.estalos(0,s.dur,40,2500))*g
        return s.reverb(x,1.4,.25,4000)
    if nome=='qual_zoltraak':
        x=.9*s.transiente(0,6000,.008)+feixe(s,0,.8,440,True)*s.env(0,.02,2.6)+.7*s.sub(0,75,35,3,3)
        return s.reverb(x,1.8,.32,5000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Frieren/Qual SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
