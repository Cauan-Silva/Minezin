package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Recuperacao Psicologica (V do Subaru): mao no peito e respiracao funda; fios de veneno e lentidao (verde-escuro e
 * cinza) sao puxados para fora do corpo e se desfazem no ar, um pulso claro sai do peito e, durante a Regeneracao I
 * (2 s), um coracao de luz bate no peito, aneis sobem dos pes a cabeca e particulas verdes sobem em volta.
 * Tempo t a partir do cast; origem nos pes, x lateral, y para cima, z para frente. Classe pura para a previa e os testes.
 */
public final class RecuperacaoSubaru {
    public record P(double x,double y,double z){}
    public static final int INSPIRA=6,LIMPA=18,CURA=40,FIM=LIMPA+CURA;
    public static final int TOXINAS=9,PONTOS_TOXINA=10,PONTOS_ANEL=40,PARTICULAS=30;
    public static final P PEITO=new P(0,1.15,.2);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}

    // ---------- limpeza ----------
    /** 0..1 da extracao do fio k (cada um sai num tempo levemente diferente). */
    private static double extrai(int k,double t){return s((t-INSPIRA-hash(k*3)*4)/8);}
    /** Ponto u (0 = raiz no corpo, 1 = ponta) do fio de toxina k: sai do corpo para fora e para cima, ondulando. */
    public static P toxina(int k,double u,double t){
        double a=2*Math.PI*k/TOXINAS+hash(k)*.6,y0=.4+1.2*hash(k*7),e=extrai(k,t),solta=lim((e-.6)/.4);
        double chicote=.18*Math.sin(t*.8+k*1.7)*u*u*u;
        double r=.32+(.15+.7*e)*u+solta*.45,ond=(.2*Math.sin(u*7-t*.9+k)+.08*Math.sin(u*13+t*1.3+k*2))*u+chicote;
        double y=y0+(.25+.75*e)*u+solta*.7+.12*Math.sin(u*5-t*1.1+k*.7)*u;
        return new P(Math.cos(a)*r-Math.sin(a)*ond,y,Math.sin(a)*r+Math.cos(a)*ond);
    }
    public static final int FUMACAS=8;
    /** Fase 0..1 da baforada n do fio k: nasce na pele, corre pelo caminho do fio e se desfaz na ponta (loop). */
    private static double ciclo(int k,int n,double t){return ((t*.09+n/(double)FUMACAS+hash(k*17))%1+1)%1;}
    /** Baforada n do fio k: a toxina sai como fumaca densa em bolos que escorrem para fora e sobem, girando. */
    public static P fumaca(int k,int n,double t){
        double c=ciclo(k,n,t);var p=toxina(k,c,t);double g=t*.3+n*2.1+k,sp=.06*c;
        return new P(p.x()+Math.cos(g)*sp,p.y()+c*c*.35,p.z()+Math.sin(g)*sp);
    }
    /** Tamanho da baforada: pequena e densa na pele, incha e some no ar. */
    public static double tamanhoFumaca(int k,int n,double t){
        double c=ciclo(k,n,t);return (.06+.16*c)*Math.pow(Math.sin(Math.PI*Math.min(1,c*1.1)),.7)*(.3+.7*extrai(k,t));
    }
    /** Opacidade da baforada (densa no comeco, rala no fim). */
    public static double densidadeFumaca(int k,int n,double t){return 1-.75*ciclo(k,n,t);}
    public static double alphaToxinas(double t){return t<INSPIRA-2?0:Math.min(1,(t-INSPIRA+2)/3)*(1-s((t-LIMPA+4)/5));}
    /** Pulso de luz que sai do peito ao fim da limpeza (raio, opacidade). */
    public static double raioPulso(double t){return 2.4*s((t-LIMPA+2)/6);}
    public static double alphaPulso(double t){double a=t-LIMPA+2;return a<0||a>6?0:1-a/6;}

    // ---------- cura ----------
    private static double idade(double t){return t-LIMPA;}
    public static double alphaCura(double t){double a=idade(t);return a<-2?0:Math.min(1,(a+2)/4)*(1-s((a-(CURA-8))/8));}
    /** Batida do coracao: duas batidas (tum-tum) a cada 16 ticks; escala do brilho no peito. */
    public static double coracao(double t){
        double c=((idade(t)%16)+16)%16;return .16+.1*Math.exp(-Math.pow((c-1)/1.2,2))+.07*Math.exp(-Math.pow((c-4)/1.2,2));
    }
    /** Anel de cura n (0 ou 1): sobe dos pes a cabeca em loop, deslocado meio ciclo um do outro. */
    public static double alturaAnel(int n,double t){double c=((idade(t)/20+n*.5)%1+1)%1;return 2.0*s(c);}
    public static double alphaAnel(int n,double t){double c=((idade(t)/20+n*.5)%1+1)%1;return Math.sin(Math.PI*c)*alphaCura(t);}
    public static P anel(int n,int i,double t){double a=2*Math.PI*i/PONTOS_ANEL+t*.1,h=alturaAnel(n,t),r=.62-.1*Math.sin(Math.PI*h/2);return new P(Math.cos(a)*r,h,Math.sin(a)*r);}
    /** Particula k: sobe em volta do corpo girando devagar (loop de 24 ticks), com tamanho que pulsa. */
    public static P particula(int k,double t){
        double c=((idade(t)/24+hash(k*5))%1+1)%1,a=2*Math.PI*hash(k*9)+c*2,r=.55+.35*hash(k*11);
        return new P(Math.cos(a)*r,.1+2.1*c,Math.sin(a)*r);
    }
    public static double tamanhoParticula(int k,double t){double c=((idade(t)/24+hash(k*5))%1+1)%1;return (.035+.025*hash(k*13))*Math.sin(Math.PI*c)*alphaCura(t);}

    private RecuperacaoSubaru(){}
}
