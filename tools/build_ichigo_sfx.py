"""Ichigo's SFX on the layered engine (tools/sfx_motor.py), replacing the first procedural set. Reiatsu is bright and
roaring (blue in Shikai, black/red and heavier in Bankai); Zangetsu is a huge blade with a long ring. Each moment is
its own sound: the wind-up plays on the cast packet (500+slot / 510+slot) and the release on the packet the server
sends on the impact tick (520/523/524/525/526...), so the hit lands exactly on its VFX. RoteiroAudio.ichigo.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK
SONS={
 'ichigo_preparo':(.45,24,'Ichigo ergue a Zangetsu','Ichigo raises Zangetsu'),
 'ichigo_preparo_pesado':(.65,32,'Reiatsu se acumula na lâmina','Reiatsu gathers on the blade'),
 'ichigo_investida':(.5,24,'Ichigo avança','Ichigo lunges'),
 'ichigo_shunpo':(.4,20,'Passo flash','Flash step'),
 'ichigo_getsuga':(1.4,48,'Getsuga Tenshou corta o ar','Getsuga Tenshou cuts the air'),
 'ichigo_getsuga_bankai':(1.5,56,'Getsuga negro ruge','Black Getsuga roars'),
 'ichigo_jujisho':(1.8,64,'Getsuga Jūjishō','Getsuga Jujisho'),
 'ichigo_acerto':(.5,32,'Getsuga acerta','Getsuga hits'),
 'ichigo_golpe_pesado':(1.6,48,'Golpe pesado racha o chão','Heavy blow cracks the ground'),
 'ichigo_corte_ascendente':(.6,32,'Corte ascendente','Rising slash'),
 'ichigo_lancamento':(.8,32,'O alvo é lançado para o alto','The target is launched up'),
 'ichigo_mascara':(1.4,32,'Máscara Hollow se forma','Hollow mask forms'),
 'ichigo_bankai_pressao':(14*T+.05,48,'A pressão espiritual cresce','Spiritual pressure rises'),
 'ichigo_bankai_grito':(16*T+.05,64,'Bankai!','Bankai!'),
 'ichigo_bankai_revela':(1.6,64,'Tensa Zangetsu é revelada','Tensa Zangetsu is revealed'),
 'ichigo_mugetsu_forma':(50*T+.05,64,'As trevas do Mugetsu se reúnem','Mugetsu darkness gathers'),
 'ichigo_mugetsu_corte':(4.0,64,'Mugetsu','Mugetsu'),
}

def reiatsu(s,ini,fim,f=220,escuro=False,forca=1.):
    """Roaring spiritual pressure: a bright buzzing chord over rushing air; Bankai is darker and grittier."""
    acorde=(s.tom(f,harmonicos=(1,2,3,4),vibrato=.01,vib_hz=6)+s.tom(f*1.498,harmonicos=(1,2)))/2
    ar=s.ruido(300 if escuro else 800,7000 if escuro else 12000,.4 if escuro else .1)
    x=(.45*(np.tanh(3*acorde) if escuro else acorde)+.4*s.girar(ar,hz=5,prof=.5))*s.janela(ini,fim,.05)
    return forca*(s.passa_baixa(x,3500) if escuro else x)
def lamina(s,ini,dur=.3,f0=1400,f1=300):
    """Zangetsu's swing: a wide whoosh and the ring of the huge blade."""
    return .55*s.passa_baixa(s.ruido(200,8000,.3),3500)*s.env(ini,dur*.45,8)+.22*s.tom(f0,f1,.5,ini,ini+dur,(1,2.3,3.7))*s.env(ini,.01,6)
def crescente(s,ini,escuro=False):
    """A Getsuga crescent flying away: a strong whoosh-hum that falls in pitch (doppler) as it travels."""
    f0,f1=(700,160) if escuro else (1100,330)
    voo=s.tom(f0,f1,.7,ini,ini+1.0,(1,2,3))*s.env(ini,.02,2.2);ar=s.ruido(400,10000,.2)*s.env(ini,.02,3)
    return .4*(np.tanh(3*voo) if escuro else voo)+.35*ar

def sintese(nome,s):
    if nome=='ichigo_preparo':return lamina(s,0,.3,900,500)+.4*reiatsu(s,.05,.45,247)*s.crescer(0,.4,1.5)
    if nome=='ichigo_preparo_pesado':return .4*lamina(s,0,.4,700,400)+.7*reiatsu(s,0,.65,196)*s.crescer(0,.6,1.4)
    if nome=='ichigo_investida':return .6*s.passa_baixa(s.ruido(80,6000,.4),2500)*s.env(0,.12,6)+.3*s.sub(0,50,32,5,2)
    if nome=='ichigo_shunpo':return .7*s.ruido(1500,15000)*s.env(0,.003,28)+.35*s.tom(3200,500,.4,0,.25)*s.env(0,.002,18)+.4*s.transiente(0,8000,.004)
    if nome=='ichigo_getsuga':
        x=lamina(s,0,.25)+.8*s.transiente(.03,7000,.008)+crescente(s,.04)+.45*reiatsu(s,.04,1.2,262)*s.env(.04,.05,2)
        return s.reverb(x,1.6,.3,8000)
    if nome=='ichigo_getsuga_bankai':
        x=lamina(s,0,.22,1200,250)+1.0*s.transiente(.03,5000,.01)+.6*s.sub(.03,70,30,3,3)+crescente(s,.04,True)+.5*reiatsu(s,.04,1.3,147,True)*s.env(.04,.05,1.8)
        return s.reverb(x,1.8,.32,4500)
    if nome=='ichigo_jujisho':
        # Two crossed crescents a beat apart, the cross roaring away.
        x=lamina(s,0,.2,1300,260)+lamina(s,.12,.2,1200,240)+1.0*s.transiente(.15,5000,.01)+.8*s.sub(.15,80,26,2.4,3.5)
        x+=crescente(s,.15,True)+crescente(s,.2,True)*.7+.55*reiatsu(s,.15,1.6,131,True)*s.env(.15,.05,1.5)
        return s.reverb(x,2.2,.35,4500)
    if nome=='ichigo_acerto':return s.reverb(.9*s.transiente(0,5000,.01)+.7*s.sub(0,75,35,6,3)+.3*s.ruido(500,6000)*s.env(0,.005,10),1.0,.25,5000)
    if nome=='ichigo_golpe_pesado':
        x=1.0*s.transiente(0,4000,.012)+1.0*s.sub(0,80,26,2.2,4)
        for i in range(24):
            a=s.rng.uniform(.04,1.2);x+=.11*s.transiente(a,s.rng.uniform(1500,5000),.006,s.rng.uniform(.3,1))
        x+=.35*s.passa_baixa(s.ruido(40,2500,.8),900)*s.env(.02,.05,1.6)+.3*reiatsu(s,0,.8,196)*s.env(0,.01,4)
        return s.reverb(x,1.8,.3,3500)
    if nome=='ichigo_corte_ascendente':
        x=.6*s.ruido(500,10000,.2)*s.crescer(0,.15,1.2)*s.env(.12,.01,7)+.3*s.tom(400,1600,.8,0,.3,(1,2.3))*s.env(0,.05,6)+.7*s.transiente(.13,6000,.008)
        return s.reverb(x,1.0,.25,7000)
    if nome=='ichigo_lancamento':return .55*s.ruido(300,8000,.3)*s.env(0,.03,3.5)*(1-s.crescer(.2,.8,1))+.4*s.tom(200,900,1,0,.7)*s.env(0,.02,3)+.5*s.sub(0,60,40,6)
    if nome=='ichigo_mascara':
        # The Hollow mask cracking over the face, a guttural distorted screech when it sets (tick 14 of CeroIchigo).
        st=CERO_FORMA=14*T;x=s.estalos(0,st,140,3000)*s.crescer(0,st,1.2)
        grito=np.tanh(6*(s.tom(520,380,.6,st,st+.9,(1,1.48,2.1,3.3),vibrato=.05,vib_hz=13)+.5*s.ruido(800,6000)))*s.env(st,.03,2.4)
        x+=.8*s.transiente(st,5000,.01)+.45*grito+.4*s.sub(st,60,35,4)
        return s.reverb(x,1.4,.3,5000)
    if nome=='ichigo_bankai_pressao':
        g=s.crescer(0,s.dur,2);return (.6*reiatsu(s,0,s.dur,110,True)+.4*s.passa_baixa(s.ruido(30,1500,.8),500))*g
    if nome=='ichigo_bankai_grito':
        # "BANKAI": a pillar of black reiatsu erupting; the coat reveal comes on its own cue (REVELA).
        x=1.0*s.transiente(0,4000,.015)+1.0*s.sub(0,90,24,1.6,4)+.7*reiatsu(s,0,s.dur,98,True)*s.env(0,.05,.6,.5)
        return s.reverb(x+.4*s.passa_baixa(s.ruido(40,3000,.7),1200)*s.env(0,.05,1.2),2.0,.3,3500)
    if nome=='ichigo_bankai_revela':
        x=lamina(s,0,.3,1500,350)+.9*s.transiente(.04,7000,.008)+.4*s.ruido(1000,12000)*s.env(.04,.01,3)+.35*s.tom(1760,1320,.5,.04,1.2,(1,2.7))*s.env(.04,.01,1.6)
        return s.reverb(x,2.0,.35,8000)
    if nome=='ichigo_mugetsu_forma':
        # Darkness gathering until the cut (GOLPE 50): a deep drone, the black flames licking, near silence at the end.
        g=s.crescer(0,s.dur-.4,1.5);x=(.5*s.tom(36,55,1,0,s.dur,(1,2,3))+.4*s.passa_baixa(s.ruido(30,1200,.8),400)+.3*s.estalos(0,s.dur,40,1500))*g
        return x*(1-.9*s.crescer(s.dur-.45,s.dur-.05,2))
    if nome=='ichigo_mugetsu_corte':
        # The hair-thin line: one high, pure "shing", a breath, then the colossal boom and a long fall of darkness.
        x=.5*(s.tom(5200)+.5*s.tom(7800))*s.env(0,.002,9)+1.0*s.transiente(.2,4000,.02)+1.0*s.sub(.2,80,20,.9,4)
        x+=.5*s.passa_baixa(s.ruido(30,2500,.8),800)*s.env(.22,.05,.7)
        return s.reverb(x,3.5,.4,3000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Ichigo SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
