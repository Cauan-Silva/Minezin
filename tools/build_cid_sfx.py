"""Cid / Shadow's own SFX on the layered engine (tools/sfx_motor.py). His sound is elegant and dark: a katana's clean
edge on top of wet, viscous slime and a low violet hum of mana. Each skill is split into its moments, scheduled on
the VFX ticks (RoteiroAudio.cid*, from PerfilCid / RelogioAtomic / MovesetsServidor).
The Atomic replaces the old 21 s charge (the 3.11 Atomic charges in 9 s) and the vanilla warden heartbeat/boom.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK;IMPACTO=180*T;FIM=540*T
SONS={
 'cid_corte_ar':(.35,24,'Lâmina de slime corta o ar','Slime blade cuts the air'),
 'cid_corte_impacto':(.8,32,'Corte de Slime acerta','Slime Cut lands'),
 'cid_passo':(.45,24,'Cid some nas sombras','Cid vanishes into shadow'),
 'cid_passo_chegada':(.5,24,'Cid surge das sombras','Cid emerges from shadow'),
 'cid_fios_lanca':(.45,32,'Fios de slime disparam','Slime threads shoot out'),
 'cid_fios_prende':(.7,32,'Fios de slime prendem','Slime threads bind'),
 'cid_fios_dreno':(.6,24,'Os fios drenam vida','The threads drain life'),
 'cid_lamina':(.4,32,'Lâmina oculta','Hidden blade'),
 'cid_despertar':(1.7,48,'Shadow desperta','Shadow awakens'),
 'cid_chuva':(1.2,48,'Lâminas se erguem no céu','Blades rise into the sky'),
 'cid_lamina_queda':(.55,48,'Lâmina cai do céu','Blade falls from the sky'),
 'cid_singularidade':(5.1,48,'Singularidade de slime','Slime singularity'),
 'cid_atomic_carga':(IMPACTO/1+.05,64,'Energia Atomic se concentra','Atomic energy gathers'),
 'cid_atomic_explosao':(FIM-IMPACTO,64,'I Am Atomic','I Am Atomic'),
}

def slime(s,ini,fim,forca=1.):
    """Wet viscous slime: low gurgling noise with slow random blobs and a resonant squelch."""
    bolhas=s.passa_baixa(s.ruido(60,1400,.8),600)*(0.6+0.4*np.sin(2*np.pi*(5+3*np.sin(2*np.pi*.7*s.t))*s.t))
    return forca*(.6*bolhas+.25*s.tom(180,120,1,ini,fim,(1,2.1))*.5)*s.janela(ini,fim,.04)
def violeta(s,ini,fim,f=110,forca=1.):
    """Low violet mana hum (two detuned voices and a soft fifth)."""
    return forca*(s.tom(f,harmonicos=(1,2,3),vibrato=.004)+s.tom(f*1.005,harmonicos=(1,2))+.4*s.tom(f*1.5))/2.4*s.janela(ini,fim,.08)
def katana(s,ini,dur=.18,f0=2600,f1=700):
    """A clean, sharp edge: thin bright noise swish plus a short ringing."""
    return .5*s.ruido(1500,14000)*s.env(ini,dur*.4,14)+.25*s.tom(f0,f1,.5,ini,ini+dur,(1,2.7))*s.env(ini,.003,12)

def sintese(nome,s):
    if nome=='cid_corte_ar':return katana(s,0,.25)+.35*slime(s,0,.3,.7)
    if nome=='cid_corte_impacto':
        x=.9*s.transiente(0,6000,.008)+.7*s.sub(0,70,32,4.5,3)+.4*slime(s,0,.6)+.25*violeta(s,0,.7,98)*s.env(0,.01,3)
        return s.reverb(x,1.1,.25,5000)
    if nome=='cid_passo':return .6*s.passa_baixa(s.ruido(80,4000,.6),1500)*s.env(0,.05,7)+.35*s.tom(600,120,.6,0,.35)*s.env(0,.02,8)+.3*slime(s,0,.4,.6)
    if nome=='cid_passo_chegada':
        x=.6*s.ruido(200,6000,.5)*s.crescer(0,.12,2)*s.janela(0,.16,.02)+.6*s.transiente(.12,3500,.01)+.5*s.sub(.12,55,32,6)
        return s.reverb(x,.9,.25,5000)
    if nome=='cid_fios_lanca':
        x=sum(.2*s.tom(f,f*.6,.7,.03*i,.3+.03*i,(1,3))*s.env(.03*i,.005,9) for i,f in enumerate([700,860,1040,1250]))
        return x+.3*s.ruido(1200,9000)*s.env(0,.01,12)+.3*slime(s,0,.4,.6)
    if nome=='cid_fios_prende':
        x=.6*s.transiente(0,4000,.01)+.6*slime(s,0,.6,1.)+.4*s.tom(140,90,1,0,.6,(1,2))*s.env(0,.02,4)
        return s.reverb(x,.8,.2,4000)
    if nome=='cid_fios_dreno':return .5*slime(s,0,.55,.9)+.4*s.tom(220,90,1.4,0,.55,(1,2,3))*s.env(0,.05,4)+.3*violeta(s,0,.55,82)
    if nome=='cid_lamina':return s.reverb(katana(s,0,.16,3200,900)+.8*s.transiente(.05,7000,.006)+.4*s.sub(.05,80,40,8),.8,.2,7000)
    if nome=='cid_despertar':
        # Shadow's transformation (32 ticks): slime surging up his body, mana rising, a sharp final set.
        g=s.crescer(0,1.2,1.6)
        x=(.6*slime(s,0,1.3,1.)+.6*violeta(s,0,1.6,73)+.3*s.ruido(300,6000,.5)*.5)*g
        x+=.8*s.transiente(1.25,5000,.01)+.7*s.sub(1.25,65,30,3)+katana(s,1.25,.25)
        return s.reverb(x,1.6,.3,5000)
    if nome=='cid_chuva':
        x=sum(.18*s.tom(f,f*1.8,.8,.06*i,.5+.06*i,(1,2.7))*s.env(.06*i,.1,3) for i,f in enumerate([300,370,450,540,650]))
        return s.reverb(x+.5*violeta(s,0,1.2,98),1.8,.35,6000)
    if nome=='cid_lamina_queda':
        x=.45*s.ruido(800,10000)*s.crescer(0,.12,2)*s.janela(0,.14,.01)+.9*s.transiente(.12,5000,.008)+.6*s.sub(.12,70,30,6)
        return s.reverb(x,1.0,.22,5000)
    if nome=='cid_singularidade':
        # 100 ticks of pull: a deep violet vortex that throbs on every 20-tick damage pulse.
        x=(.5*s.girar(s.passa_baixa(s.ruido(40,1800,.8),700),hz=3,prof=.7)+.5*violeta(s,0,s.dur,55))*s.janela(0,s.dur,.3)
        for k in (1,2,3,4):x+=.6*s.sub(k*20*T,60,35,6,2.5)+.3*s.transiente(k*20*T,2500,.015)
        return s.reverb(x,1.6,.3,4000)
    if nome=='cid_atomic_carga':
        # 9 s: mana sucked into one point. A heartbeat speeding up (the old warden beats, now his own), a violet
        # hum rising an octave, slime and air drawn inward; near silence on the last beat before the release.
        g=s.crescer(0,IMPACTO-.4,1.7);x=np.zeros(s.n)
        bate=0.;intervalo=1.5
        while bate<IMPACTO-.8:
            x+=.55*(.4+.6*bate/IMPACTO)*(s.sub(bate,58,36,10,2)+.6*s.sub(bate+.18,54,34,12,2));bate+=intervalo;intervalo=max(.38,intervalo*.86)
        x+=(.45*s.tom(55,110,1.6,0,IMPACTO,(1,2,3))+.3*s.ruido(200,8000,.4)*s.crescer(0,IMPACTO,3))*g
        x*=1-s.crescer(IMPACTO-.5,IMPACTO-.05,2)*.85
        return s.reverb(x,1.8,.25,4000)
    if nome=='cid_atomic_explosao':
        # The release: a blinding crack, a colossal sub, then the pillar of light roaring (a vast violet drone with
        # a slow surge) for as long as it stands, fading with it.
        x=1.0*s.transiente(0,5000,.015)+1.0*s.sub(0,95,20,.6,4)
        x+=.5*s.passa_baixa(s.ruido(30,3000,.8),1100)*s.env(0,.02,.25,.35)
        pilar=(.5*violeta(s,.2,s.dur,49)+.3*s.girar(s.ruido(100,5000,.5),hz=.6,prof=.5))*s.env(.2,1.5,.08,.6)
        x+=pilar*(1-s.crescer(s.dur-4,s.dur,1.2))
        return s.reverb(x,3.2,.35,3500)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Cid SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
