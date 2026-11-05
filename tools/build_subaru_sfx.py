"""Subaru's and Beatrice's SFX on the layered engine (tools/sfx_motor.py): CC0 recordings (art/audio/cc0, sources and
licences in art/audio/cc0/fontes.json) as the main layers, procedural synthesis for weight, glue and the tails. Each take
picks a different recording of the same family, so repeated casts never sound alike. Yin magic is dark and wet
(Shamak, E.M.M.), the spirits are glass and bells, Beatrice's crystal magic is real glass and stone. Every internal hit
sits on the tick of its VFX phase in the pure classes (core/vfx/*Subaru, *Beatrice); the client schedules them in
EfeitosSubaru on the packet of each moment.
"""
import argparse
import numpy as np
import sfx_motor as m

T=m.TICK
# --- ticks copied from the pure classes (checked by SomSubaruTest) ---
SH_CARGA,SH_NEVOA=8,60
ESC_INVOCA=10
INS_ORDEM,INS_PULSO=6,10
REC_INSPIRA,REC_LIMPA,REC_CURA=6,18,40
CON_PORTA,CON_ABRE,CON_SAI,CON_FECHA,CON_FIM=12,20,34,44,70
MI_FORMA,MI_DISPARO,MI_VOO,MI_CRISTAL,MI_QUEBRA,MI_ESTACAS=10,14,4,22,10,7
EMM_FECHA,EMM_ABRE,EMM_SOLTA=8,38,10
UL_QUEDA,UL_ABRE,UL_CHUVA=7,16,26
UL_PRISAO=UL_ABRE+UL_CHUVA+UL_QUEDA+2;UL_QUEBRA=UL_PRISAO+8+12

def mi_ordem(k):c=MI_ESTACAS//2;d=k-c;return 0 if d==0 else abs(d)*2-(1 if d<0 else 0)
MI_ACERTO_MAX=MI_DISPARO+(MI_ESTACAS-1)+MI_VOO

SONS={
 'subaru_shamak_carga':((SH_CARGA+4)*T,20,'Sombra se junta na mão','Shadow gathers in the hand'),
 'subaru_shamak_disparo':(.6,24,'Shamak é lançado','Shamak is cast'),
 'subaru_shamak_nevoa':(2.4,20,'Névoa de sombra envolve o alvo','Shadow mist engulfs the target'),
 'subaru_escudo':(1.0,18,'Espíritos formam um escudo','Spirits form a shield'),
 'subaru_escudo_estouro':(.8,20,'O escudo espiritual estoura','The spirit shield bursts'),
 'subaru_instrucao':(1.2,22,'Instrução tática','Tactical instruction'),
 'subaru_recuperacao':((REC_LIMPA+REC_CURA)*T,16,'Subaru respira fundo e se recupera','Subaru breathes deep and recovers'),
 'subaru_contrato':(CON_FIM*T,32,'Contrato com a Beatrice','Contract with Beatrice'),
 'beatrice_minya':((MI_ACERTO_MAX+MI_CRISTAL+MI_QUEBRA)*T,32,'Minya: estacas de cristal','Minya: crystal stakes'),
 'beatrice_emm':((EMM_ABRE+EMM_SOLTA+6)*T,18,'E.M.M.: o espaço se fecha','E.M.M.: space closes in'),
 'beatrice_emm_golpe':(.6,16,'O golpe para na casca','The blow stops on the shell'),
 'beatrice_murak':(1.3,18,'Murak: o peso some','Murak: weight vanishes'),
 'beatrice_ul_minya':((UL_QUEBRA+20)*T,64,'Ul Minya: chuva de cristal','Ul Minya: crystal rain'),
}

VIDROS=['rubberduck_v1/glass_01.ogg','rubberduck_v1/glass_02.ogg','rubberduck_v1/glass_03.ogg']+[f'rubberduck_v2/sfx100v2_glass_0{i}.ogg' for i in range(1,7)]
QUEBRAS=['rubberduck_v1/glass_04.ogg','rubberduck_v1/glass_05.ogg']
PEDRAS=[f'rubberduck_v2/sfx100v2_stones_0{i}.ogg' for i in range(1,4)]
LUFADAS=['swishes/swish-3.wav','swishes/swish-4.wav','swishes/swish-7.wav','swishes/swish-8.wav','swishes/swish-9.wav']
SINOS=['rubberduck_v1/bell_01.ogg','rubberduck_v1/bell_02.ogg','rubberduck_v1/bell_03.ogg']
CAMPO=[f'kenney_scifi/forceField_00{i}.ogg' for i in range(4)]
GOSMA=['kenney_scifi/slime_000.ogg','kenney_scifi/slime_001.ogg']

def sombra(s,ini,fim,forca=1.):
    """Yin darkness: a wet recording pitched down under breathy noise and a faint detuned drone."""
    ar=s.passa_baixa(s.ruido(60,3000,.7),1400);drone=(s.tom(73,harmonicos=(1,2,3))+s.tom(77.5,harmonicos=(1,2)))/2
    gosma=s.passa_baixa(s.amostra('kenney_scifi/slime_001.ogg',ini,1,.55,corte=fim-ini+.2),1800)
    return forca*(.45*s.girar(ar,hz=1.6,prof=.6)+.18*np.tanh(2*drone)+.55*gosma)*s.janela(ini,fim,.08)
def vidro(s,ini,f=1800,queda=7.,forca=1.):
    """A synthetic glass ring (sparkle filler)."""
    return forca*s.tom(f,harmonicos=(1,2.76,5.4,8.9),pesos=[1,.5,.3,.15])*s.env(ini,.002,queda)
def cristal_crava(s,ini,forca=1.):
    """A crystal stake driving into stone: a real glass hit over a real stone knock, plus weight."""
    return forca*(.75*s.amostra(VIDROS,ini,1,s.rng.uniform(.8,1.15))+.5*s.amostra(PEDRAS,ini,1,s.rng.uniform(.7,1))+.35*s.sub(ini,90,40,8,2.5))
def whoosh(s,ini,tom=1.,forca=1.):
    return forca*s.amostra(LUFADAS,ini,1,tom)
def brilho(s,ini,fim,densidade=30,forca=1.):
    """Magic sparkle: tiny real glass ticks and synthetic bells scattered in time."""
    x=np.zeros(s.n)
    for a in s.rng.uniform(ini,fim,max(1,int(densidade*(fim-ini)))):
        x+=.18*vidro(s,a,s.rng.uniform(2500,5200),18)
        if s.rng.random()<.35:x+=.12*s.amostra(['kenney_ui/glass_001.ogg','kenney_ui/glass_004.ogg'],a,1,s.rng.uniform(1.4,2.2))
    return forca*x

def sintese(nome,s):
    if nome=='subaru_shamak_carga':
        x=sombra(s,0,s.dur)*s.crescer(0,s.dur,1.6)+.45*s.amostra(LUFADAS,0,1,.6,inverter=True,corte=s.dur)*s.crescer(0,s.dur,2)
        return s.reverb(x,1.0,.25,3000)
    if nome=='subaru_shamak_disparo':
        x=.6*s.transiente(0,2500,.012)+.55*s.passa_baixa(s.amostra('kenney_scifi/lowFrequency_explosion_001.ogg',0,1,1.3,corte=.5),2500)
        x+=.8*whoosh(s,0,.65)+.35*s.amostra(GOSMA,0,1,.7,corte=.5)+.3*s.sub(0,70,35,6,2)
        return s.reverb(x,1.2,.25,2500)
    if nome=='subaru_shamak_nevoa':
        x=.7*s.amostra('kenney_scifi/slime_000.ogg',0,1,.6)+.6*s.sub(0,60,30,3,2)+.8*sombra(s,0,s.dur)*s.env(0,.05,.9,.4)
        x+=.4*s.passa_baixa(s.amostra('rubberduck_v2/sfx100v2_air_01.ogg',.05,1,.8,corte=s.dur),2500)*s.env(.05,.2,1.2)
        x+=.3*s.amostra(CAMPO,.15,1,.5,corte=1.6)                          # dizzy rings
        return s.reverb(x,1.6,.3,2500)
    if nome=='subaru_escudo':
        e=ESC_INVOCA*T
        x=brilho(s,0,e,40)+.55*s.amostra('kenney_ui/glass_004.ogg',0,1,1.2)+.55*s.amostra(CAMPO,e-.12,1,1.15,corte=.9)
        x+=.5*s.amostra(SINOS,e,1,1.5,corte=.8)
        return s.reverb(x,1.4,.35,9000)
    if nome=='subaru_escudo_estouro':
        x=.9*s.amostra(QUEBRAS,0,1,1.25)+.5*s.amostra(VIDROS,.02,1,1.4)+brilho(s,0,.3,60,1.0)+.3*s.sub(0,70,40,9)
        return s.reverb(x,1.2,.3,10000)
    if nome=='subaru_instrucao':
        o=INS_ORDEM*T
        x=.7*s.amostra(['kenney_ui/pluck_001.ogg','kenney_ui/pluck_002.ogg'],0,1,.9)+.4*s.amostra('kenney_ui/tick_004.ogg',0,1,1)
        x+=.75*s.amostra('rubberduck_v1/gong_02.ogg',o,1,2.0)+.4*s.amostra(SINOS,o,1,1.3,corte=.9)+.55*whoosh(s,o,.45)+.4*s.sub(o,80,50,5,1.5)
        return s.reverb(x,1.5,.3,8000)
    if nome=='subaru_recuperacao':
        i,l=REC_INSPIRA*T,REC_LIMPA*T
        x=.6*s.passa_baixa(s.amostra('rubberduck_v2/sfx100v2_air_02.ogg',0,1,1.0,inverter=True,corte=i+.15),3500)*s.crescer(0,i,1.2)   # deep inhale
        x+=.45*s.passa_baixa(s.amostra(GOSMA,i,1,.8,corte=l-i),2200)                                                          # toxins out
        x+=.7*s.amostra('rubberduck_v1/bell_03.ogg',l,1,1.0)+.35*vidro(s,l,1320,2.4)
        for b in range(3):x+=.35*s.sub(l+.25+b*.55,55,40,14,1.5)                                                               # heartbeat of light
        return s.reverb(x,1.6,.3,7000)
    if nome=='subaru_contrato':
        p,a,sa,f=CON_PORTA*T,CON_ABRE*T,CON_SAI*T,CON_FECHA*T
        x=.35*s.passa_baixa(s.amostra('kenney_scifi/engineCircular_000.ogg',0,1,.5,corte=f+.5),1500)*s.janela(0,f+.4,.2)  # circle hum
        x+=.35*s.passa_baixa(s.ruido(60,1500,.7),700)*s.janela(.1,p,.05)+.4*s.amostra(PEDRAS,.15,1,.6)                          # door rising
        x+=.6*s.amostra('kenney_rpg/doorOpen_2.ogg',p,1,.85)+.45*s.amostra(['kenney_rpg/creak1.ogg','kenney_rpg/creak2.ogg'],p+.15,1,.9)
        x+=.45*s.amostra(CAMPO,a-.1,1,.8)+brilho(s,a,sa,35)                                                                     # light pours out, Beatrice
        x+=.8*s.amostra('rubberduck_v1/gong_01.ogg',f,1,1.1)+.5*s.amostra(SINOS,f,1,1.5)+.5*s.sub(f,70,40,3,2)                  # the contract seals
        return s.reverb(x,2.2,.35,7000)
    if nome=='beatrice_minya':
        x=brilho(s,0,MI_FORMA*T,50)+.5*s.amostra('kenney_ui/glass_004.ogg',0,1,.8,inverter=True)
        x+=.15*s.tom(2400,harmonicos=(1,2.76),vibrato=.03,vib_hz=14)*s.janela(MI_FORMA*T,MI_DISPARO*T,.03)   # stakes trembling
        for k in range(MI_ESTACAS):
            d=(MI_DISPARO+mi_ordem(k))*T;x+=.5*whoosh(s,d,s.rng.uniform(1.1,1.4))
            x+=cristal_crava(s,d+MI_VOO*T,.85)
        q=(MI_ACERTO_MAX+MI_CRISTAL)*T;x+=.8*s.amostra(QUEBRAS,q-.05,1,1.1)+.5*brilho(s,q,q+.35,70)   # the crystals shatter
        return s.reverb(x,1.6,.3,9000)
    if nome=='beatrice_emm':
        f,a=EMM_FECHA*T,EMM_ABRE*T
        x=.7*s.amostra(CAMPO,0,1,.5,corte=f+.3)*s.crescer(0,f,1.5)+.6*s.sub(f,50,30,4,2)                                      # it closes
        x+=.45*s.passa_baixa(s.amostra('kenney_scifi/computerNoise_000.ogg',f,1,.45,corte=a-f+.1),500)*s.janela(f,a,.1)   # muffled stillness
        x+=.25*s.passa_baixa(s.tom(55,harmonicos=(1,2,3),vibrato=.004,vib_hz=.7),400)*s.janela(f,a,.1)
        x+=.55*s.amostra('kenney_ui/scratch_004.ogg',a,1,.7)+brilho(s,a+.2,a+.9,30,.7)                                  # peels into dust
        return s.reverb(x,1.3,.25,4000)
    if nome=='beatrice_emm_golpe':
        x=.9*s.passa_baixa(s.amostra('kenney_scifi/impactMetal_002.ogg',0,1,.7),800)+.7*s.sub(0,65,35,7,2.5)
        return s.reverb(x,.9,.2,1500)
    if nome=='beatrice_murak':
        x=.8*s.amostra('kenney_ui/tick_004.ogg',3*T,1,.8)+.5*s.amostra('kenney_ui/pluck_001.ogg',3*T,1,1.6)   # the finger snap
        x+=.5*s.amostra(['rubberduck_v1/spring_01.ogg','rubberduck_v1/spring_03.ogg'],6*T,1,1.5)+.45*s.amostra(LUFADAS,6*T,1,.4)
        x+=brilho(s,6*T,.9,25,.8)
        return s.reverb(x,1.6,.35,9000)
    if nome=='beatrice_ul_minya':
        ab,pr,qb=UL_ABRE*T,UL_PRISAO*T,UL_QUEBRA*T
        x=.4*s.passa_baixa(s.amostra('kenney_scifi/engineCircular_002.ogg',0,1,.5,corte=qb),1800)*s.janela(0,qb,.1)*s.crescer(0,ab,1)
        x+=.35*s.amostra(CAMPO,0,1,.6,corte=ab+.3)+.3*brilho(s,0,ab,30)                                     # the seal opens in the sky
        for _ in range(60):                                                                                 # the rain of stakes
            a=ab+UL_CHUVA*T*s.rng.random()**1.25+UL_QUEDA*T;x+=.24*cristal_crava(s,a,s.rng.uniform(.5,1))
        x+=.5*s.amostra(['kenney_rpg/creak1.ogg','kenney_rpg/creak2.ogg'],pr,1,.5)+.4*s.amostra(PEDRAS,pr+.1,1,.55)   # the giant crystal grows
        x+=.35*s.tom(600,2400,1.6,pr,qb,(1,2.76))*s.crescer(pr,qb,2.2)*s.janela(pr,qb+.02,.01)                # charge before the break
        x+=1.0*s.amostra(QUEBRAS,qb,1,.8)+.8*s.amostra('kenney_scifi/explosionCrunch_004.ogg',qb,1,.9)+.9*s.sub(qb,90,25,1.6,4)
        x+=.5*s.passa_baixa(s.amostra('rubberduck_v2/sfx100v2_thunder_01.ogg',qb+.05,1,1.0,corte=s.dur-qb),1500)+1.0*brilho(s,qb,qb+.8,120)
        return s.reverb(x,2.6,.35,8000)
    raise ValueError(nome)

def build(preview=None):m.escrever(SONS,sintese,'Subaru SFX:',preview)
if __name__=='__main__':
    a=argparse.ArgumentParser();a.add_argument('--preview');build(a.parse_args().preview)
