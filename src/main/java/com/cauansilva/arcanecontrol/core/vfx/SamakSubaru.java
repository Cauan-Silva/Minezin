package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Shamak (Z do Subaru, magia Yin de curto alcance): a mao junta bruma violeta escura que converge numa palma, o orbe
 * de sombra e lancado em linha reta e, ao acertar, estoura numa nevoa que engole a cabeca do alvo (cegueira e
 * confusao por 3 s) com aneis de tontura girando. Tempo t a partir do cast (tick 0 = tecla); o disparo sai no tick
 * CARGA e o acerto no tick IMPACTO, que o servidor usara para o dano e os efeitos.
 * Referencial: origem nos olhos de quem lanca, x lateral (a mao direita, que lanca, fica em x negativo, como o braco
 * direito do modelo), y para cima, z para frente. A pose do corpo e player_animation/subaru.json (subaru_shamak): a palma
 * chega em MAO no fim da subida e estica para frente no disparo (tick CARGA).
 * Classe pura (sem Minecraft), no mesmo padrao de GetsugaIchigo, para a previa e os testes.
 */
public final class SamakSubaru {
    public record P(double x,double y,double z){}
    public static final int CARGA=8,VIAGEM=6,IMPACTO=CARGA+VIAGEM,NEVOA=60,FIM=IMPACTO+NEVOA;
    public static final double ALCANCE=8,RAIO_ORBE=.3,RAIO_NEVOA=1.15;
    public static final int LAT=16,LON=24,FIOS=14,PONTOS_FIO=12,BOLHAS=56,ANEIS=3,PONTOS_ANEL=36,RASTROS=5,PONTOS_RASTRO=12,
            TENTACULOS=7,PONTOS_TENTACULO=10,PARTICULAS=40,ENLACES=5,PONTOS_ENLACE=24;
    /** Palma direita de quem lanca na pose de carga (subaru_shamak, 0,35 s). */
    public static final P MAO=new P(-.38,-.18,.78);
    /** Onde o orbe acerta: a altura dos olhos, no alcance. */
    public static final P ALVO=new P(0,0,ALCANCE);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}

    // ---------- orbe ----------
    /** Escala do orbe: nasce pequeno na palma durante a carga, cheio na viagem, some no acerto. */
    public static double escalaOrbe(double t){return t<0||t>=IMPACTO?0:t<CARGA?.12+.88*s(t/CARGA):1;}
    /** 0..1 da viagem: acelera como um disparo (sai devagar da mao, chega rapido). */
    public static double voo(double t){return Math.pow(lim((t-CARGA)/VIAGEM),1.5);}
    public static P centro(double t){
        double v=voo(t);double tremor=t<CARGA?.012*Math.sin(t*2.1):0;
        return new P(MAO.x()*(1-v)+tremor,MAO.y()*(1-v)+tremor,MAO.z()+(ALCANCE-MAO.z())*v);
    }
    /** Alongamento do orbe na direcao do voo (esticado quando rapido). */
    public static double estica(double t){return t<CARGA||t>=IMPACTO?0:.9*Math.sin(Math.PI*lim((t-CARGA)/VIAGEM))*s((t-CARGA)/2);}
    /** Superficie deformada: lat i (0..LAT), lon j (0..LON-1); a casca ondula como sombra viva. */
    public static P orbe(int i,int j,double t){
        double th=Math.PI*i/LAT,ph=2*Math.PI*j/LON,e=escalaOrbe(t);
        return superficie(th,ph,t,RAIO_ORBE*e*(1+ondula(th,ph,t,1)));
    }
    /** Ruido da casca em tres oitavas: bolhas grandes lentas, medias e um fervilhar fino. */
    private static double ondula(double th,double ph,double t,double forca){
        return forca*(.14*Math.sin(3*th+ph*2+t*.9+hash(1)*6)+.09*Math.sin(ph*3-t*1.3)*Math.sin(th)
               +.05*Math.sin(7*th-ph*5+t*2.3)+.025*Math.sin(11*ph+13*th-t*3.7));
    }
    private static P superficie(double th,double ph,double t,double r){
        var c=centro(t);
        return new P(c.x()+r*Math.sin(th)*Math.cos(ph),c.y()+r*Math.cos(th),c.z()+r*Math.sin(th)*Math.sin(ph)*(1+estica(t)));
    }
    /** Casca externa de fumaca: maior, translucida e mais agitada que o nucleo, gira devagar em volta dele. */
    public static P casca(int i,int j,double t){
        double th=Math.PI*i/LAT,ph=2*Math.PI*j/LON+t*.12;
        return superficie(th,ph,t,RAIO_ORBE*1.45*escalaOrbe(t)*(1+ondula(th,ph+1.7,t*1.4,2.2)));
    }
    /** Tentaculo k (u = 0 na casca, 1 = ponta): fumaca que se contorce para fora do orbe e fica para tras no voo. */
    public static P tentaculo(int k,double u,double t){
        double a=2*Math.PI*k/TENTACULOS+hash(k*11)*.9+t*.25,el=(hash(k*13+4)-.5)*1.6,e=escalaOrbe(t);
        double comp=(.35+.3*hash(k*17))*e*(1+.8*estica(t)),ondaU=Math.sin(u*5-t*1.1+k)*.12*u;
        double r=RAIO_ORBE*e*1.2+comp*u;var c=centro(t);
        double dx=Math.cos(a)*Math.cos(el),dy=Math.sin(el),dz=Math.sin(a)*Math.cos(el);
        // no voo os tentaculos varrem para tras (z negativo em relacao ao orbe)
        double atras=estica(t)*.6*u*u;
        return new P(c.x()+dx*r+ondaU*-dz,c.y()+dy*r+ondaU,c.z()+dz*r-atras);
    }
    public static double alphaTentaculos(double t){return t<2||t>=IMPACTO?0:Math.min(1,(t-2)/3);}
    /** Particula k: grao de sombra que orbita a palma na carga e depois escorre do rastro; posicao e tamanho. */
    public static P particula(int k,double t){
        double fase=hash(k*19+3),r0=.5+.5*hash(k*23),a=2*Math.PI*hash(k*29)+t*(.35+.3*fase)*(k%2==0?1:-1);
        if(t<CARGA){double puxa=s(t/CARGA);double r=r0*(1-.75*puxa);var m=MAO;
            return new P(m.x()+Math.cos(a)*r,m.y()+Math.sin(a)*r*.8+(fase-.5)*.3,m.z()+(hash(k*31)-.5)*.5*(1-puxa));}
        double tt=Math.max(CARGA,Math.min(IMPACTO,t-fase*3));var c=centro(tt);double cai=Math.max(0,t-tt)*.025;
        return new P(c.x()+Math.cos(a)*.18*r0,c.y()+Math.sin(a)*.18*r0-cai,c.z());
    }
    public static double tamanhoParticula(int k,double t){
        double base=.03+.03*hash(k*37);if(t<0||t>IMPACTO+3)return 0;
        return base*Math.min(1,(t+1)/3)*(t>IMPACTO?1-(t-IMPACTO)/3:1);
    }
    /** Normal da superficie (direcao a partir do centro), para a luz e o brilho de borda. */
    public static P normal(int i,int j){double th=Math.PI*i/LAT,ph=2*Math.PI*j/LON;return new P(Math.sin(th)*Math.cos(ph),Math.cos(th),Math.sin(th)*Math.sin(ph));}
    public static double alphaOrbe(double t){return t<0||t>=IMPACTO?0:Math.min(1,(t+1)/2);}

    // ---------- carga: fios de bruma convergindo na palma ----------
    /** Ponto u (0 = cauda, 1 = cabeca) do fio k: espirala para a palma enquanto a carga avanca. */
    public static P fio(int k,double u,double t){
        double tau=lim(t/CARGA-.3*(1-u)),r0=.95+.4*hash(k*7+1),a0=2*Math.PI*k/FIOS+hash(k*3)*.8;
        double r=r0*(1-s(tau))+.1,a=a0+2.6*s(tau)*(k%2==0?1:-1),dz=(hash(k*5+2)-.5)*.9*(1-s(tau));
        return new P(MAO.x()+Math.cos(a)*r,MAO.y()+Math.sin(a)*r,MAO.z()+dz);
    }
    public static double alphaFios(double t){return t<0?0:Math.min(1,(t+1)/2)*(1-s((t-CARGA+3)/3));}

    // ---------- voo: rastro de sombra ----------
    /** Fita k do rastro (u = 0 no orbe, 1 = ponta): segue o caminho atras do orbe, espiralando para fora. */
    public static P rastro(int k,double u,double t){
        var c=centro(Math.max(CARGA,t-u*4));double a=2*Math.PI*k/RASTROS+u*5+t*.5,r=.16*(.4+u);
        return new P(c.x()+Math.cos(a)*r,c.y()+Math.sin(a)*r,c.z()-estica(t)*.25*u);
    }
    public static double alphaRastro(double t){return t<CARGA?0:t>=IMPACTO+1?0:Math.min(1,(t-CARGA+1)/2)*(1-s(t-IMPACTO));}

    // ---------- acerto: nevoa e tontura ----------
    private static double idade(double t){return t-IMPACTO;}
    private static double expande(double t){return 1-Math.exp(-Math.max(0,idade(t))/2.2);}
    /** 0..1 da contracao final, quando a nevoa se recolhe antes de sumir. */
    private static double recolhe(double t){return s((idade(t)-NEVOA*.7)/(NEVOA*.3));}
    /** Bolha k da nevoa: pontos de uma esfera (espiral de Fibonacci) que se abre e gira em volta do alvo. */
    public static P bolha(int k,double t){
        double y=1-2*(k+.5)/BOLHAS,rr=Math.sqrt(1-y*y),ph=k*2.399963+idade(t)*.06*(.6+hash(k)),
               r=RAIO_NEVOA*(.45+.55*hash(k*2+1))*expande(t)*(1-.4*recolhe(t));
        return new P(ALVO.x()+rr*Math.cos(ph)*r,ALVO.y()+y*r+.1*Math.sin(idade(t)*.15+k),ALVO.z()+rr*Math.sin(ph)*r);
    }
    public static double tamanhoBolha(int k,double t){return (.28+.22*hash(k*3+2))*(1+.5*expande(t))*(1-.5*recolhe(t))*alphaNevoa(t);}
    public static double alphaNevoa(double t){double a=idade(t);return a<0?0:Math.min(1,(a+1)/3)*(1-s((a-(NEVOA-14))/14));}
    /** Anel de choque do estouro: raio e opacidade nos primeiros ticks. */
    public static double raioOnda(double t){return 2.2*s(idade(t)/4);}
    public static double alphaOnda(double t){double a=idade(t);return a<0||a>4?0:1-s(a/4);}
    /** Aneis de tontura: o anel a roda em volta da cabeca do alvo, cada um num sentido e numa altura. */
    public static P anel(int a,int i,double t){
        double ang=2*Math.PI*i/PONTOS_ANEL+Math.max(0,idade(t))*(.18+.05*a)*(a%2==0?1:-1),r=(.62+.1*a)*(.7+.3*expande(t));
        return new P(ALVO.x()+Math.cos(ang)*r,ALVO.y()+.5+.2*a+.05*Math.sin(ang*2+idade(t)*.3),ALVO.z()+Math.sin(ang)*r);
    }
    /** Enlace k: faixa de fumaca que se enrola na cabeca do alvo (venda de sombra), apertando e girando. */
    public static P enlace(int k,int i,double t){
        double u=(double)i/(PONTOS_ENLACE-1),ang=2*Math.PI*(u*1.4+hash(k*41))+Math.max(0,idade(t))*(.12+.03*k)*(k%2==0?1:-1);
        double aperto=.42+.25*(1-expande(t))+.06*Math.sin(idade(t)*.2+k);double incl=(hash(k*43)-.5)*.9;
        double y=.05+Math.sin(ang)*incl*aperto+(k-ENLACES/2.0)*.06;
        return new P(ALVO.x()+Math.cos(ang)*aperto,ALVO.y()+y,ALVO.z()+Math.sin(ang)*aperto);
    }
    public static double alphaEnlaces(double t){double a=idade(t);return a<0?0:Math.min(1,a/3)*(1-s((a-(NEVOA-12))/12));}
    /** Rodo de nevoa no chao em volta do alvo (raio e opacidade): a fumaca densa escorre para os pes. */
    public static double raioChao(double t){return 1.6*expande(t)*(1-.3*recolhe(t));}
    public static double alphaChao(double t){double a=idade(t);return a<2?0:Math.min(1,(a-2)/6)*(1-s((a-(NEVOA-16))/16))*.6;}
    public static double alphaAneis(double t){double a=idade(t);return a<1?0:Math.min(1,(a-1)/4)*(1-s((a-(NEVOA-10))/10));}

    private SamakSubaru(){}
}
