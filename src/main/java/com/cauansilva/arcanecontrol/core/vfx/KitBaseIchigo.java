package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Kit base (Shikai) do Ichigo em volume 3D: X Shunpo (efeito 501), C golpe pesado com onda de choque (525) e
 * V investida com corte ascendente (526). Geometria pura; o render e a previa desenham estes pontos.
 * Referenciais: Shunpo e onda com origem nos pes (x lateral, y altura, z frente); corte com origem nos olhos.
 */
public final class KitBaseIchigo {
    public record P(double x,double y,double z){}
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    // ---------- X: Shunpo ----------
    public static final int SHUNPO_FIM=16,ECOS=4,RISCOS=10,POEIRA=10;
    /** Posicao (0..1 do caminho) e alpha da pos-imagem k: as mais antigas ficam perto da partida e somem antes. */
    public static double ecoU(int k){return .12+.22*k;}
    public static double alphaEco(int k,double t){return (.6-.1*k)*(1-s((t-k)/(5.0+k)));}
    /** Risco de velocidade j: deslocamento no plano do corpo e trecho visivel (u0..u1) correndo pelo caminho. */
    public static P riscoOffset(int j){double a=j*2.4+hash(j)*.8,r=.25+.35*hash(j+3);return new P(Math.cos(a)*r,1+Math.sin(a)*r*1.5,0);}
    public static double riscoU1(int j,double t){return Math.min(1,s(t/3.0)+.1*hash(j+5));}
    public static double riscoU0(int j,double t){return Math.max(0,riscoU1(j,t)-.45-.3*hash(j+7)+s((t-2)/6.0));}
    public static double alphaRisco(double t){return 1-s((t-2)/7.0);}
    /** Poeira chutada (cubos) na partida (fim=false) e na chegada. */
    public static P poeira(int k,double t,boolean chegada){
        double a=k*Math.PI*2/POEIRA+hash(k+(chegada?20:0)),v=.1+.1*hash(k+30),idade=Math.max(0,t-(chegada?1:0));
        return new P(Math.cos(a)*(.35+idade*v),Math.max(0,idade*(.16+.1*hash(k+40))-idade*idade*.012),Math.sin(a)*(.35+idade*v));
    }
    public static double tamanhoPoeira(int k,double t){return (.1+.08*hash(k+50))*(1-s(t/SHUNPO_FIM));}
    public static final int ANEL_SHUNPO=32,TUBO=6,HELICES=3,PONTOS_HELICE=40,FLOCOS=6;
    /** Anel de deslocamento de ar (volume em toro achatado) na partida e na chegada, abrindo rente ao chao. */
    public static P anelAr(int i,int j,double t,boolean chegada){
        double idade=Math.max(0,t-(chegada?1:0)),R=.3+1.7*s(idade/4.0),r=.13*(1-s(idade/8.0))+.02,phi=i*Math.PI*2/ANEL_SHUNPO,v=j*Math.PI*2/TUBO;
        return new P(Math.cos(phi)*(R+r*Math.cos(v)),.12+idade*.03+r*.5*Math.sin(v),Math.sin(phi)*(R+r*Math.cos(v)));
    }
    public static double alphaAnelAr(double t,boolean chegada){double idade=t-(chegada?1:0);return idade<0?0:1-s((idade-2)/6.0);}
    /** Helice de vento h enrolando o caminho (u 0..1), visivel entre riscoU0 e riscoU1 do risco h. */
    public static P helice(int h,double u){double a=u*16+h*Math.PI*2/HELICES,r=.55+.1*Math.sin(u*9);return new P(Math.cos(a)*r,1+Math.sin(a)*r*1.3,0);}
    /** Floco f desprendendo da pos-imagem k: sobe e recua enquanto ela some (a imagem se desfaz em cubos). */
    public static P floco(int k,int f,double t){
        double idade=Math.max(0,t-k),a=hash(k*7+f)*Math.PI*2;
        return new P(Math.cos(a)*(.25+idade*.04),.3+1.5*hash(k*3+f+1)+idade*(.05+.04*hash(f)),Math.sin(a)*(.15+idade*.03)-idade*.05);
    }
    public static double tamanhoFloco(int k,int f,double t){double idade=t-k;return idade<0?0:(.07+.05*hash(k+f*5))*(1-s(idade/(7.0+k)));}

    // ---------- C: golpe pesado + onda de choque ----------
    public static final int ONDA_FIM=36,ANEL=48,SECAO_ANEL=8,RACHADURAS=10,PONTOS_RACHADURA=8,DETRITOS=18;
    public static final double RAIO_DANO=8;
    /** Raio da parede da onda: chega exatamente ao raio de dano do servidor (8) em 5 ticks. */
    public static double raioOnda(double t){return RAIO_DANO*s(t/5.0);}
    public static double alturaOnda(double t){return 1.4*s(t/1.5)*(1-s((t-3)/12.0));}
    public static double alphaOnda(double t){return 1-s((t-6)/10.0);}
    /** Parede da onda em volume: anel varrido por uma secao em cunha (crista fina em cima, base larga). */
    public static P onda(int i,int j,double t){
        double phi=i*Math.PI*2/ANEL,v=j*Math.PI*2/SECAO_ANEL,R=raioOnda(t),h=alturaOnda(t),ond=1+.12*Math.sin(phi*5+t);
        double rad=R+.4*Math.cos(v)*(1-.5*Math.max(0,Math.sin(v))),y=Math.max(0,h*ond*(.5+.5*Math.sin(v)));
        return new P(Math.cos(phi)*rad,y,Math.sin(phi)*rad);
    }
    /** Rachadura radial j no chao: ponto i (0 = centro), comprimento abre em 3 ticks ate 7 blocos. */
    public static P rachadura(int j,int i,double t){
        double u=i/(PONTOS_RACHADURA-1.0),comp=7*s(t/3.0)*(.7+.3*hash(j)),a=j*Math.PI*2/RACHADURAS+hash(j+1)*.4+(hash(j*9+i)-.5)*.25;
        double r=.5+u*comp;return new P(Math.cos(a)*r,.02,Math.sin(a)*r);
    }
    public static double larguraRachadura(int i){return .16*(1-i/(PONTOS_RACHADURA-1.0))+.03;}
    public static double alphaRachadura(double t){return 1-s((t-14)/10.0);}
    /** Detrito k: sai do chao quando a onda passa por ele, sobe e cai. */
    public static P detrito(int k,double t){
        double r=1+5.5*hash(k),a=hash(k+2)*Math.PI*2,saida=5*Math.sqrt(r/RAIO_DANO),idade=Math.max(0,t-saida);
        double y=Math.max(0,idade*(.32+.18*hash(k+3))-idade*idade*.035);return new P(Math.cos(a)*(r+idade*.05),y,Math.sin(a)*(r+idade*.05));
    }
    public static double tamanhoDetrito(int k,double t){double r=1+5.5*hash(k),saida=5*Math.sqrt(r/RAIO_DANO);return t<saida?0:(.14+.16*hash(k+4))*(1-s((t-saida-6)/10.0));}
    // Fumaca: nuvens de poeira em volume que a onda levanta, rolando para fora e subindo; coluna no centro.
    public static final int FUMACAS=30,LAT=7,LON=12;
    /** Centro da nuvem k: anel externo nasce quando a frente da onda passa (atrasado), interno sobe do impacto. */
    public static P fumaca(int k,double t){
        boolean centro=k<6;double a=k*2.39996+hash(k)*.5,r0=centro?.3+.5*hash(k+1):2+5.5*hash(k+1),saida=centro?0:5*Math.sqrt(r0/RAIO_DANO)+.5,idade=Math.max(0,t-saida);
        double r=r0+(centro?idade*.05:idade*(.06+.04*hash(k+2))),y=(centro?.6:.25)+idade*(centro?.11:.035)*(1-s(idade/22.0));
        return new P(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    public static double raioFumaca(int k,double t){
        boolean centro=k<6;double r0=centro?.3+.5*hash(k+1):2+5.5*hash(k+1),saida=centro?0:5*Math.sqrt(r0/RAIO_DANO)+.5,idade=t-saida;
        return idade<0?0:(centro?.9:.55+.35*hash(k+3))*(.35+.65*s(idade/6.0))*(1+idade*.025);
    }
    public static double alphaFumaca(int k,double t){
        double r0=k<6?0:2+5.5*hash(k+1),saida=k<6?0:5*Math.sqrt(r0/RAIO_DANO)+.5,idade=t-saida;return idade<0?0:s(idade/2.0)*(1-s((idade-8)/(ONDA_FIM-9.0-saida)));
    }
    /** Superficie encaroçada da nuvem k (lat i, lon j): esfera com bolhas que giram devagar. */
    public static P bolha(int k,int i,int j,double t){
        double la=-Math.PI/2+Math.PI*i/(LAT-1),lo=Math.PI*2*j/LON,c=fumaca(k,t).y(),R=raioFumaca(k,t);
        double ond=1+.18*Math.sin(lo*3+la*2+k+t*.15)+.12*Math.sin(lo*5-la*3+k*2);var f=fumaca(k,t);
        return new P(f.x()+Math.cos(la)*Math.cos(lo)*R*ond,Math.max(0,f.y()+Math.sin(la)*R*ond*.8),f.z()+Math.cos(la)*Math.sin(lo)*R*ond);
    }
    public static double clarao(double t){return 1.6*s(t/2.0);}
    public static double alphaClarao(double t){return 1-s(t/5.0);}

    // ---------- V: investida + corte ascendente ----------
    public static final int CORTE_FIM=16,ARCO=18,SECAO=10,ECOS_CORTE=2;
    public static final double INVESTIDA=3,ALCANCE_CORTE=5,RAIO_CORTE=1.7;
    /** Angulo da ponta do corte: sobe de baixo-frente (-60 graus) ate o alto (+85 graus) em 3 ticks. */
    public static double pontaCorte(double t){return Math.toRadians(-60+145*s(t/3.0));}
    public static double alphaCorte(double t){return 1-s((t-4)/7.0);}
    /** Volume do corte: arco vertical no plano (altura, frente) atras da ponta, secao em cunha na lateral. */
    public static P corte(int i,int j,double t){
        double ponta=pontaCorte(t),cauda=Math.max(Math.toRadians(-60),ponta-Math.toRadians(95)),u=i/(ARCO-1.0),a=cauda+(ponta-cauda)*u;
        double v=j*Math.PI*2/SECAO,k=Math.sin(Math.PI*Math.min(1,u*1.05)),larg=.95*k,esp=.45*k;
        double r=RAIO_CORTE-larg/2+larg/2*Math.cos(v);
        return new P(esp*Math.sin(v)*(.35+.65*(1-Math.cos(v))/2),-.1+Math.sin(a)*r,.8+Math.cos(a)*r);
    }
    public static double ecoCorte(int k,double t){return t-k*.8;}
    public static double alphaEcoCorte(int k){return k==1?.35:.15;}

    // ---------- Lancamento do alvo do V (efeito 527, na posicao do alvo) ----------
    public static final int LANCA_FIM=20,ESPIRAIS=3,PONTOS_ESPIRAL=24,ESTILHACOS=12;
    public static final double IMPULSO=1.3;
    /** Coluna de reiatsu em espiral que sobe junto com o alvo lancado. */
    public static double alturaEspiral(double t){return 4.5*s(t/6.0);}
    public static P espiral(int e,double u,double t){double a=u*9+e*Math.PI*2/ESPIRAIS+t*.6,r=.75*(1-u*.5);return new P(Math.cos(a)*r,u*alturaEspiral(t),Math.sin(a)*r);}
    public static double alphaEspiral(double t){return 1-s((t-6)/12.0);}
    /** Estilhaco k do impacto: sai do peito do alvo para fora e para cima, cai depois. */
    public static P estilhaco(int k,double t){
        double a=k*Math.PI*2/ESTILHACOS+hash(k),v=.18+.12*hash(k+1);
        return new P(Math.cos(a)*v*t,1+t*(.25+.15*hash(k+2))-t*t*.03,Math.sin(a)*v*t);
    }
    public static double tamanhoEstilhaco(int k,double t){return (.1+.07*hash(k+3))*(1-s(t/12.0));}
}
