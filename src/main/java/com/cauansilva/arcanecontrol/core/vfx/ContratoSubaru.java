package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Contrato (G do Subaru): o circulo do contrato acende nos pes dele, a porta da Biblioteca Proibida se ergue ao lado,
 * abre com luz por dentro, a Beatrice sai flutuando ate o ombro dele e o contrato fecha: um fio de mana liga os dois,
 * o circulo da um clarao e fica uma aura violeta leve na forma. Tempo t a partir do cast.
 * Referencial: origem nos pes do Subaru, x lateral (+x = lado esquerdo dele, onde a Beatrice fica), y para cima,
 * z para frente. Classe pura para a previa e os testes.
 */
public final class ContratoSubaru {
    public record P(double x,double y,double z){}
    public static final int PORTA=12,ABRE=20,SAI=34,FECHA=44,FIM=70;
    public static final int PONTOS_CIRCULO=72,ANEIS=3,RUNAS=12,PONTOS_FIO=16,ESTRELAS=6;
    /** Onde a porta fica (ao lado e um pouco a frente) e onde a Beatrice para (ao lado do ombro). */
    public static final P PORTA_POS=new P(2.4,0,1.2),LUGAR=new P(1.15,.55,.45);
    public static final double RAIO=1.6;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}

    // ---------- circulo do contrato ----------
    public static double alphaCirculo(double t){return t<0?0:Math.min(1,t/6)*(t<FECHA?1:1-.65*s((t-FECHA)/10));}
    public static double raioCirculo(int a,double t){return RAIO*(.55+.25*a)*(.6+.4*s(t/8))*(1+.12*clarao(t));}
    public static P circulo(int a,int i,double t){double g=2*Math.PI*i/PONTOS_CIRCULO+t*(.03+.02*a)*(a%2==0?1:-1),r=raioCirculo(a,t);return new P(Math.cos(g)*r,.03+.01*a,Math.sin(g)*r);}
    /** Estrela de seis pontas inscrita no anel do meio: vertice k (ligado a k+2). */
    public static P estrela(int k,double t){double g=2*Math.PI*k/ESTRELAS-t*.025,r=raioCirculo(1,t);return new P(Math.cos(g)*r,.035,Math.sin(g)*r);}
    /** Runa k entre o anel de fora e o do meio: posicao e quanto ja acendeu (acendem uma a uma). */
    public static P runa(int k,double t){double g=2*Math.PI*(k+.5)/RUNAS+t*.04,r=(raioCirculo(1,t)+raioCirculo(2,t))/2;return new P(Math.cos(g)*r,.04,Math.sin(g)*r);}
    public static double acendeRuna(int k,double t){return s((t-2-k*.6)/3);}
    public static double clarao(double t){double a=t-FECHA;return a<0||a>8?0:Math.sin(Math.PI*a/8);}

    // ---------- porta ----------
    /** Altura da porta (0..1): sobe do chao. */
    public static double ergue(double t){return s((t-2)/(PORTA-2));}
    /** Abertura das folhas (0..1 = 0..100 graus). */
    public static double abertura(double t){return s((t-PORTA)/(ABRE-PORTA))*(1-s((t-SAI-2)/6));}
    public static double luzPorta(double t){return Math.min(abertura(t)*1.4,1);}
    /** A porta some depois que a Beatrice sai (afunda de volta). */
    public static double alphaPorta(double t){return t<2?0:Math.min(1,(t-2)/3)*(1-s((t-SAI-4)/6));}

    // ---------- Beatrice ----------
    /** Caminho da Beatrice: aparece no vao da porta, sai flutuando em arco e para ao lado do ombro do Subaru. */
    public static P beatrice(double t){
        // suave nas duas pontas (sem tranco na chegada) e o balanco ocioso entra aos poucos depois de chegar
        double u=s(s((t-ABRE+2)/(SAI-ABRE+2)));double arco=Math.sin(Math.PI*u)*.6,bob=.08*Math.sin(t*.2)*s((t-SAI+4)/10);
        return new P(PORTA_POS.x()+(LUGAR.x()-PORTA_POS.x())*u,PORTA_POS.y()+.15+(LUGAR.y()-.15)*u+arco+bob,PORTA_POS.z()+(LUGAR.z()-PORTA_POS.z())*u);
    }
    /** Escala da Beatrice (cresce ao atravessar a porta) e giro em y (sai de frente e vira para o mesmo lado dele). */
    public static double escalaBeatrice(double t){return t<ABRE-3?0:s((t-ABRE+3)/6);}
    public static double giroBeatrice(double t){return -100*(1-s((t-ABRE)/(SAI-ABRE+4)))-12;}
    /** Inclinacao (graus em x) da Beatrice no voo: inclina para frente ao acelerar e volta ao parar. */
    public static double inclinaBeatrice(double t){double u=(t-ABRE+2)/(SAI-ABRE+2);return u<0||u>1.3?0:14*Math.sin(Math.PI*Math.min(1,u))*(u>1?1-(u-1)/.3:1);}

    // ---------- fio do contrato ----------
    /** Ponto u do fio de mana entre o peito do Subaru e a mao da Beatrice; desenha-se do Subaru para ela. */
    public static P fio(double u,double t){
        var b=beatrice(t);double ax=0,ay=1.2,az=.2,bx=b.x()-.25,by=b.y()+.75,bz=b.z();double curva=Math.sin(Math.PI*u)*.25;
        return new P(ax+(bx-ax)*u,ay+(by-ay)*u+curva+.03*Math.sin(u*12-t*.6),az+(bz-az)*u);
    }
    public static double alcanceFio(double t){return s((t-SAI+4)/6);}
    public static double alphaFio(double t){return t<SAI-4?0:Math.min(1,(t-SAI+4)/3)*(t>FECHA+10?.35:1);}
    /** Aura da forma em volta do Subaru (fica fraca depois do clarao). */
    public static double aura(double t){return t<FECHA-2?0:Math.min(1,(t-FECHA+2)/4)*(.45+.55*clarao(t));}

    private ContratoSubaru(){}
}
