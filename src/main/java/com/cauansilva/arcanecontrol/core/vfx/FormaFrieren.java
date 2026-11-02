package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Forma desperta da Frieren conforme o anime: Liberar Mana como coluna translucida azul-palida
 * (mana revelada, ep. 10), Julgamento como relampagos violeta do Judradjim (ep. 25) e Escudo
 * Celestial como cupula de paineis hexagonais ciano da magia defensiva (ep. 3).
 */
public final class FormaFrieren {
    public record P(double x,double y,double z){}
    public static final int SURTO=30,PARTICULAS=36,ESTRIAS=10;
    public static final int JULGAMENTO=40,QUEDA=25,RAIOS_JULGAMENTO=5,PONTOS_RAIO=9,RAMOS=8;
    public static final int ESCUDO=120,DISSOLVE=12;
    public static final double RAIO_ESCUDO=8,PAINEL=.95;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    // --- Liberar Mana ---
    /** Raio da coluna: explode larga na liberacao e assenta mais fina pelo resto da forma. */
    public static double raioColuna(double idade){return 1.25+1.6*Math.exp(-Math.max(0,idade-6)/10.0)*s(idade/6);}
    public static double alturaColuna(double idade){return 4+30*s(idade/14);}
    public static double alphaColuna(double idade,double restante){
        double surto=.55*s(idade/4)*Math.exp(-Math.max(0,idade-8)/12.0);
        return Math.min(1,(.3+surto)*s(idade/3)*s(restante/12));
    }
    /** Estria vertical j: angulo ao redor da coluna e ondulacao que sobe. */
    public static double anguloEstria(int j,double idade){return j*Math.PI*2/ESTRIAS+Math.sin(idade*.03+j)*.25;}
    public static double alphaEstria(int j,double idade){return .35+.35*Math.pow(.5+.5*Math.sin(idade*.11+j*1.9),3);}
    public static P particula(int i,double idade){
        double ciclo=60+hash(i)*50,fase=((idade+hash(i+3)*ciclo)%ciclo)/ciclo;
        double a=hash(i+7)*Math.PI*2+idade*.01,r=.6+hash(i+11)*2.4;
        return new P(Math.cos(a)*r,fase*(6+hash(i+13)*8),Math.sin(a)*r);
    }
    public static double alphaParticula(int i,double idade){
        double ciclo=60+hash(i)*50,fase=((idade+hash(i+3)*ciclo)%ciclo)/ciclo;
        return Math.sin(fase*Math.PI)*(.5+.5*hash(i+17))*s(idade/10);
    }

    // --- Julgamento (Judradjim) ---
    public static double alphaAviso(double t){return t>=QUEDA+2?0:s(t/4);}
    /** Anel de aviso no chao que fecha exatamente no tick do impacto do servidor. */
    public static double fechamentoAviso(double t){return Math.max(0,1-t/QUEDA);}
    public static double cargaCeu(double t){return s(t/QUEDA);}
    /** Ponto i (0 no ceu, PONTOS_RAIO-1 no chao) do raio j, de altura h ate o centro, em zigue-zague. */
    public static P raio(int j,int i,double h,double variacao){
        double u=i/(double)(PONTOS_RAIO-1);
        double amp=(i==0||i==PONTOS_RAIO-1)?0:(1.2+j*.25)*Math.sin(u*Math.PI);
        double a=hash(j*31+i*7+Math.floor(variacao))*Math.PI*2,base=j==0?0:.9+j*.35;
        double ang0=j*Math.PI*2/RAIOS_JULGAMENTO;
        return new P(Math.cos(a)*amp+Math.cos(ang0)*base*u,h*(1-u),Math.sin(a)*amp+Math.sin(ang0)*base*u);
    }
    /** O desenho do raio troca de forma a cada 2 ticks, como relampago animado. */
    public static double variacaoRaio(double t){return Math.floor(t/2);}
    public static double alphaRaio(double t){return t<QUEDA?0:1-s((t-QUEDA-3)/(JULGAMENTO-QUEDA-3.0));}
    public static P ramo(int k,int i,double t){
        double a=k*Math.PI*2/RAMOS+hash(k+50)*.6,passo=.9;double x=0,z=0,y=0;
        for(int n=1;n<=i;n++){a+=(hash(k*13+n+Math.floor(t/2)*3)-.5)*1.1;x+=Math.cos(a)*passo;z+=Math.sin(a)*passo;y=Math.max(0,y+(hash(k*5+n)-.6)*.4);}
        return new P(x,y+.1,z);
    }
    public static double alcanceRamo(double t){return t<QUEDA?0:6*s((t-QUEDA)/5.0);}
    public static double clarao(double t){return t<QUEDA?0:1-s((t-QUEDA)/8.0);}

    // --- Escudo Celestial ---
    public record Painel(P centro,P normal,int indice){}
    /** Paineis hexagonais em fileiras de latitude sobre a cupula (ligeiramente abaixo do equador). */
    public static java.util.List<Painel> paineis(){
        var lista=new java.util.ArrayList<Painel>();int n=0;
        for(int fila=-1;fila<=6;fila++){
            double lat=fila*Math.PI/14,anel=Math.cos(lat)*RAIO_ESCUDO;
            int quantos=Math.max(1,(int)Math.round(anel*Math.PI*2/(PAINEL*1.85)));
            for(int k=0;k<quantos;k++){
                double lon=k*Math.PI*2/quantos+(fila%2==0?0:Math.PI/quantos);
                var nrm=new P(Math.cos(lat)*Math.cos(lon),Math.sin(lat),Math.cos(lat)*Math.sin(lon));
                lista.add(new Painel(new P(nrm.x()*RAIO_ESCUDO,nrm.y()*RAIO_ESCUDO,nrm.z()*RAIO_ESCUDO),nrm,n++));
            }
        }
        lista.add(new Painel(new P(0,RAIO_ESCUDO,0),new P(0,1,0),n));
        return lista;
    }
    /** Paineis acendem de cima para baixo e dissolvem nos ultimos ticks. */
    public static double escalaPainel(Painel p,double t){
        double altura=p.normal().y(),atraso=(1-altura)*6;
        return s((t-atraso)/5)*(1-s((t-(ESCUDO-DISSOLVE)-hash(p.indice())*4)/8.0));
    }
    public static double brilhoPainel(Painel p,double t){return .55+.45*Math.pow(Math.max(0,Math.sin(t*.09+p.indice()*2.3)),6);}
}
