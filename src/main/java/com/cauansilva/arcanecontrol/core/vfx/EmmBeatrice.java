package com.cauansilva.arcanecontrol.core.vfx;

/**
 * E.M.M. (G-X do Subaru, lancado pela Beatrice no contrato): a defesa absoluta Yin. A Beatrice estende a mao, um fio
 * violeta liga a mao dela ao peito do Subaru e uma casca escura, justa ao corpo, fecha de cima e de baixo ate a cintura.
 * Enquanto a casca existe o espaco em volta fica "parado": as particulas proximas congelam e o que acerta (na previa,
 * dois projeteis) para na superficie, fica suspenso e se esfarela; a casca so afunda um pouco, engolindo o golpe.
 * Ao fim, a casca descasca de cima para baixo e vira poeira violeta que volta para as maos da Beatrice.
 * Tempo t a partir do cast. Referencial: origem nos pes do Subaru, x lateral (+x = lado da Beatrice), y para cima,
 * z para frente. Classe pura para a previa e os testes; o servidor usa invulneravel(t).
 */
public final class EmmBeatrice {
    public record P(double x,double y,double z){}
    public static final int FECHA=8,DURACAO=30,ABRE=FECHA+DURACAO,SOLTA=10,VOLTA=12,FIM=ABRE+SOLTA+VOLTA;
    public static final int LAT=18,LON=28,VEIAS=9,PONTOS_VEIA=20,PONTOS_ANEL=40,PONTOS_CIRCULO=48,PONTOS_FIO=16;
    public static final int CONGELADAS=16,POEIRA=40,GOLPES=2,CONGELA=8,ESTILHACOS=8;
    public static final double RX=.78,RY=1.2,RZ=.78;
    public static final P CENTRO=new P(0,1,0);
    /** Maos da Beatrice (mesmo lugar da Minya). */
    public static final P MAOS=new P(MinyaBeatrice.MAOS.x(),MinyaBeatrice.MAOS.y(),MinyaBeatrice.MAOS.z());
    /** Projeteis da previa: de onde saem e em que tick tocam a casca. */
    public static final P[] ORIGEM={new P(0,1.3,6),new P(-3.5,1.4,4.5)};
    public static final int[] CHEGADA={16,26};
    public static final int VOO=5;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}
    private static P mais(P a,P b,double k){return new P(a.x()+b.x()*k,a.y()+b.y()*k,a.z()+b.z()*k);}
    private static P un(P a){double n=Math.sqrt(a.x()*a.x()+a.y()*a.y()+a.z()*a.z());return new P(a.x()/n,a.y()/n,a.z()/n);}

    /** O Subaru nao recebe dano nem efeito enquanto a casca esta fechada (1,5 s). */
    public static boolean invulneravel(double t){return t>=FECHA-1&&t<ABRE;}
    /** Tempo "do mundo" em volta: corre normal, para com a casca fechada e volta a correr depois. */
    public static double tempoMundo(double t){return t<FECHA?t:t<ABRE?FECHA+(t-FECHA)*.02:FECHA+DURACAO*.02+(t-ABRE);}

    // ---------- casca ----------
    public static P esfera(int i,int j){double th=Math.PI*i/LAT,ph=2*Math.PI*j/LON;return new P(Math.sin(th)*Math.cos(ph),Math.cos(th),Math.sin(th)*Math.sin(ph));}
    /** Ponto da elipsoide na direcao unitaria n. */
    public static P naCasca(P n,double k){return new P(CENTRO.x()+n.x()*RX*k,CENTRO.y()+n.y()*RY*k,CENTRO.z()+n.z()*RZ*k);}
    public static P normal(int i,int j){var n=esfera(i,j);return un(new P(n.x()/RX,n.y()/RY,n.z()/RZ));}
    /** Direcao (unitaria) do ponto em que o golpe g toca a casca. */
    public static P direcaoGolpe(int g){var o=ORIGEM[g];return un(new P(o.x()-CENTRO.x(),(o.y()-CENTRO.y())*.4,o.z()-CENTRO.z()));}
    public static P pontoGolpe(int g){return naCasca(direcaoGolpe(g),1.02);}
    /** Afundamento (0..1) do golpe g: a casca engole o impacto e volta. */
    public static double afunda(int g,double t){double a=t-CHEGADA[g];return a<0||a>7?0:Math.sin(Math.PI*a/7)*Math.exp(-a/5);}

    /**
     * Quanto do anel de latitude i esta visivel (0..1): fecha das duas pontas ate a cintura e, na soltura,
     * descasca de cima para baixo. Borda suave para o fechamento parecer liquido.
     */
    public static double cobertura(int i,double t){
        double th=Math.PI*i/LAT,ponta=Math.min(th,Math.PI-th)/(Math.PI/2),f=s(t/FECHA)*1.08;
        double fecha=lim((f-ponta)/.12+1);
        double d=s((t-ABRE)/SOLTA)*1.1,descasca=t<ABRE?1:lim((th/Math.PI-d)/.08+1);
        return t<0?0:fecha*descasca;
    }
    public static P casca(int i,int j,double t){
        var n=esfera(i,j);double k=1+.012*Math.sin(t*.2+i*.7)*(t<ABRE&&t>=FECHA?.3:1)+.06*(1-s(t/FECHA))*Math.sin(j*.9+t);
        for(int g=0;g<GOLPES;g++){var dg=direcaoGolpe(g);double c=n.x()*dg.x()+n.y()*dg.y()+n.z()*dg.z(),ang=Math.acos(Math.max(-1,Math.min(1,c)));
            k-=.09*afunda(g,t)*Math.exp(-ang*ang/.08);}
        return naCasca(n,k);
    }
    /** Escuridao da casca: some de fora no fechamento, fica densa no meio e pulsa devagar. */
    public static double alphaCasca(double t){return t<0||t>=ABRE+SOLTA?0:.55+.3*s(t/FECHA)-.05*Math.cos(t*.3);}

    // ---------- veias Yin na superficie ----------
    public static P veia(int k,double u,double t){
        double th=Math.PI*(.08+.84*u),ph=2*Math.PI*k/VEIAS+.9*Math.sin(u*Math.PI*1.5+k*1.3)+.15*Math.sin(tempoMundo(t)*.05);
        return naCasca(new P(Math.sin(th)*Math.cos(ph),Math.cos(th),Math.sin(th)*Math.sin(ph)),1.015);
    }
    /** Brilho no ponto u da veia k: um pulso que corre a veia, mais forte quando um golpe acaba de ser engolido. */
    public static double brilhoVeia(int k,double u,double t){
        double fase=(t*.045+hash(k))%1,b=Math.exp(-Math.pow((u-fase)*5,2))*.8+.2;
        double golpe=0;for(int g=0;g<GOLPES;g++)golpe=Math.max(golpe,afunda(g,t));
        return b*(.6+.8*golpe);
    }

    // ---------- anel de distorcao ----------
    public static P anel(int i,double t){
        double th=Math.PI*(.5+.32*Math.sin(t*.13)),g=2*Math.PI*i/PONTOS_ANEL+t*.04;
        return naCasca(new P(Math.sin(th)*Math.cos(g),Math.cos(th),Math.sin(th)*Math.sin(g)),1.14);
    }
    public static double alphaAnel(double t){return t<FECHA-2||t>ABRE+2?0:lim((t-FECHA+2)/4)*lim((ABRE+2-t)/4);}

    // ---------- circulo Yin no chao (congela junto com o tempo) ----------
    public static P circulo(int a,int i,double t){
        double g=2*Math.PI*i/PONTOS_CIRCULO+tempoMundo(t)*(.06+.03*a)*(a==0?1:-1),r=(.85+.35*a)*(.7+.3*s(t/5));
        return new P(Math.cos(g)*r,.03+.01*a,Math.sin(g)*r);
    }
    public static double alphaCirculo(double t){return t<0||t>=FIM?0:Math.min(1,t/3)*(t<FECHA+4?1:.45)*(1-s((t-ABRE)/SOLTA));}

    // ---------- fio da mao da Beatrice ao peito ----------
    public static P fio(int i,double t){
        double u=(double)i/(PONTOS_FIO-1);var peito=new P(0,1.25,.2);
        double onda=.12*Math.sin(u*Math.PI)*Math.sin(t*.9-u*6)*(1-lim(t/FECHA));
        return new P(MAOS.x()+(peito.x()-MAOS.x())*u,MAOS.y()+(peito.y()-MAOS.y())*u+onda+.25*Math.sin(u*Math.PI),MAOS.z()+(peito.z()-MAOS.z())*u);
    }
    /** O fio "corre" da mao ao peito (0..1 do comprimento) e some quando a casca fecha. */
    public static double alcanceFio(double t){return s(t/4);}
    public static double alphaFio(double t){return t<0?0:1-s((t-FECHA+1)/4);}

    // ---------- particulas congeladas ----------
    public static P congelada(int k,double t){
        double a=2*Math.PI*hash(k*3),r=1.5+.7*hash(k*5),y=.3+1.8*hash(k*7),tm=tempoMundo(t);
        return new P(Math.cos(a+tm*.03)*r,y+.25*Math.sin(tm*.2+k),Math.sin(a+tm*.03)*r);
    }
    public static double alphaCongelada(double t){return t<0||t>=ABRE+SOLTA?0:Math.min(1,t/4)*(1-s((t-ABRE)/SOLTA));}

    // ---------- projeteis da previa ----------
    /** Posicao do projetil g: voa ate a casca e para ali, suspenso, ate se esfarelar. */
    public static P projetil(int g,double t){
        var o=ORIGEM[g];var p=naCasca(direcaoGolpe(g),1.12);double u=Math.pow(lim((t-CHEGADA[g]+VOO)/VOO),1.15);
        double treme=t>CHEGADA[g]?.01*Math.sin(t*4.1+g):0;
        return new P(o.x()+(p.x()-o.x())*u+treme,o.y()+(p.y()-o.y())*u,o.z()+(p.z()-o.z())*u);
    }
    public static double alphaProjetil(int g,double t){double a=t-CHEGADA[g];return a<-VOO||a>=CONGELA?0:a<CONGELA-2?1:(CONGELA-a)/2;}
    /** Estilhaco e do projetil g depois que ele se esfarela: cai devagar. */
    public static P estilhaco(int g,int e,double t){
        var p=naCasca(direcaoGolpe(g),1.12);var d=direcaoGolpe(g);double a=Math.max(0,t-CHEGADA[g]-CONGELA+2),ang=2*Math.PI*e/ESTILHACOS;
        return new P(p.x()+d.x()*.03*a+Math.cos(ang)*.035*a,Math.max(.03,p.y()+Math.sin(ang)*.02*a-.008*a*a),p.z()+d.z()*.03*a+Math.sin(ang)*.035*a);
    }
    public static double tamanhoEstilhaco(int g,int e,double t){double a=t-CHEGADA[g]-CONGELA+2;return a<0||a>=10?0:(.04+.03*hash(g*7+e))*(1-a/10);}

    // ---------- poeira que volta para a Beatrice ----------
    /** Grao k: nasce na casca quando a borda da descascada passa por ele e voa em curva ate as maos dela. */
    public static P poeira(int k,double t){
        double y=1-2*(k+.5)/POEIRA,rr=Math.sqrt(1-y*y),ph=k*2.399963;var n=new P(rr*Math.cos(ph),y,rr*Math.sin(ph));
        var ini=naCasca(n,1);double u=s((t-nasce(k))/VOLTA);
        double arco=.6*Math.sin(Math.PI*u);
        return new P(ini.x()+(MAOS.x()-ini.x())*u+n.x()*arco*.5,ini.y()+(MAOS.y()-ini.y())*u+arco,ini.z()+(MAOS.z()-ini.z())*u+n.z()*arco*.5);
    }
    public static double nasce(int k){double y=1-2*(k+.5)/POEIRA;return ABRE+SOLTA*Math.acos(y)/Math.PI;}
    public static double tamanhoPoeira(int k,double t){double a=t-nasce(k);return a<0||a>=VOLTA?0:(.05+.03*hash(k*11))*Math.sin(Math.PI*Math.min(1,a/VOLTA*1.1));}

    private EmmBeatrice(){}
}
