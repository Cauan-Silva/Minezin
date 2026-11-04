"""Gojo's own SFX on the layered engine (tools/sfx_motor.py), replacing the vanilla beacon/warden/explosion sounds.
Times follow PerfilVfxGojo (ticks / 20); the release sounds are separate events played by the server on the exact
release tick (VERMELHO_DISPARO, ROXO_DISPARO), so each hit lands with its VFX.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK
AZUL,VERMELHO_DISPARO,ROXO_DISPARO,DOMINIO_ABERTURA=60*T,13*T,30*T,35*T
SONS={
 'gojo_azul':(AZUL+.4,48,'Azul puxa tudo para o centro','Blue pulls everything in'),
 'gojo_vermelho_carga':(VERMELHO_DISPARO+.05,32,'Vermelho se concentra na ponta do dedo','Red gathers at the fingertip'),
 'gojo_vermelho_disparo':(1.6,64,'Vermelho explode para frente','Red blasts forward'),
 'gojo_infinito_ativa':(1.1,24,'Infinito se ergue','Infinity rises'),
 'gojo_infinito_desativa':(.8,24,'Infinito se desfaz','Infinity fades'),
 'gojo_roxo_carga':(ROXO_DISPARO+.05,48,'Azul e Vermelho se fundem','Blue and Red fuse'),
 'gojo_roxo_disparo':(3.2,64,'Vazio Roxo apaga o caminho','Hollow Purple erases its path'),
 'gojo_dominio':(DOMINIO_ABERTURA+3.5,64,'Expansão de Domínio: Vazio Infinito','Domain Expansion: Infinite Void'),
}

def sintese(nome,s):
    t=s.t
    if nome=='gojo_azul':
        # Implosion: a reversed swell sucks the air in, then a deep swirling vortex with sparks being dragged in.
        suga=s.ruido(300,9000,.3)*s.crescer(0,.35,3)*s.janela(0,.42,.05)
        vortice=s.girar(s.ruido(80,2600,.6),hz=4.5,prof=.8)*s.janela(.3,AZUL,.25)
        hum=s.tom(62,74,harmonicos=(1,2,3),vibrato=.03,vib_hz=4.5)*s.janela(.25,AZUL,.3)
        x=.55*suga+.34*vortice+.30*hum+.16*s.estalos(.4,AZUL,35,4500)+.6*s.transiente(.36,3000,.02)*.5+.35*s.sub(.36,70,35,2)
        return s.reverb(x,1.4,.3,4000)
    if nome=='gojo_vermelho_carga':
        # Tight, rising buzz at the fingertip, getting angrier until the release tick.
        g=s.crescer(0,VERMELHO_DISPARO,1.6)
        x=(.4*np.tanh(3*s.tom(180,880,1.3,0,VERMELHO_DISPARO,(1,2,3,5)))+.25*s.ruido(1500,9000)+.2*s.estalos(0,VERMELHO_DISPARO,60,6000))*g
        return x
    if nome=='gojo_vermelho_disparo':
        # The blast: crack, heavy sub, a forward rushing shock of air, debris, big outdoor tail.
        x=1.0*s.transiente(0,7000,.01)+.9*s.sub(0,85,30,2.6,3)+.55*s.ruido(200,7000,.4)*s.env(0,.01,4.5)
        x+=.35*s.tom(900,140,.5,0,.6)*s.env(0,.005,5)+.22*s.estalos(.05,.9,50,3500)
        return s.reverb(x,2.2,.35,3500)
    if nome=='gojo_infinito_ativa':
        # Glassy chord blooming upward, a soft air pad: the barrier forming.
        x=sum(.18*s.tom(f,f*1.5,.6,0,.8,(1,2.76))*s.env(.02*i,.25,2.2) for i,f in enumerate([520,780,1170,1560]))
        x+=.25*s.ruido(3000,14000)*s.env(0,.3,3)
        return s.reverb(x,1.6,.4,9000)
    if nome=='gojo_infinito_desativa':
        x=sum(.18*s.tom(f*1.5,f,.6,0,.6,(1,2.76))*s.env(.02*i,.02,3.5) for i,f in enumerate([1560,1170,780]))
        x+=.2*s.ruido(2000,12000)*s.env(0,.05,5)
        return s.reverb(x,1.2,.35,8000)
    if nome=='gojo_roxo_carga':
        # Blue (high) and Red (low) glide into each other; their beating speeds up as they fuse, pressure builds.
        g=s.crescer(0,ROXO_DISPARO,1.4)
        azul=s.tom(1250,520,1.2,0,ROXO_DISPARO,(1,2));vermelho=s.tom(140,520,1.2,0,ROXO_DISPARO,(1,2,3))
        x=(.32*azul+.38*np.tanh(2*vermelho)+.25*s.ruido(60,1800,.8)*g+.12*s.estalos(.5,ROXO_DISPARO,70,5000))*(.2+.8*g)
        return s.reverb(x,1.1,.25,5000)
    if nome=='gojo_roxo_disparo':
        # The release: a sharp tear, a colossal sub drop, the sphere tearing through space (long low roar), echo.
        x=1.0*s.transiente(0,8000,.008)+1.0*s.sub(0,95,24,1.4,3.5)
        rasgo=s.ruido(100,6000,.5)*s.env(0,.02,1.1);x+=.55*np.tanh(2*rasgo)
        x+=.30*s.girar(s.ruido(40,900,1.),hz=7,prof=.5)*s.env(.05,.2,.7)+.25*s.tom(320,60,.6,0,1.5,(1,1.5,2))*s.env(0,.05,1.4)
        return s.reverb(x,3.0,.4,3000)
    if nome=='gojo_dominio':
        # Low, uncanny cluster swelling as the domain opens (until DOMINIO_ABERTURA), a deep "whomp" when it
        # closes around everyone, then a vast shimmering void that slowly fades.
        g=s.crescer(0,DOMINIO_ABERTURA,2.2)*s.janela(0,DOMINIO_ABERTURA+.05,.02)
        cluster=sum(s.tom(f,harmonicos=(1,2,3),vibrato=.004,vib_hz=.7+i*.3) for i,f in enumerate([55,58.3,82.4,110,116.5]))/5
        x=(.45*cluster+.3*s.ruido(40,3000,.7))*g
        x+=1.0*s.sub(DOMINIO_ABERTURA,70,22,1.0,3)+.6*s.transiente(DOMINIO_ABERTURA,4000,.02)
        brilho=sum(s.tom(f,vibrato=.01,vib_hz=.2+i*.13) for i,f in enumerate([1318,1760,2349,2637]))/4
        x+=(.16*brilho+.14*s.ruido(4000,15000))*s.env(DOMINIO_ABERTURA+.1,.6,.55)
        return s.reverb(x,3.5,.45,6000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Gojo SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
