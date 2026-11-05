package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Minya (G-Z do Subaru, lancado pela Beatrice no contrato): ela ergue as maos, um circulo violeta abre na frente dela e
 * estacas de cristal violeta se formam em arco em volta das maos, tremem apontadas e disparam uma a uma em leque; cada
 * uma crava no chao ou no alvo e o ponto do acerto cristaliza em espinhos, que depois se partem em cacos.
 * Tempo t a partir do cast. Referencial: origem nos pes do Subaru, x lateral (+x = lado da Beatrice), y para cima,
 * z para frente. Classe pura para a previa e os testes; o servidor usa acerto(k) e pontoAcerto(k) para dano e lentidao.
 */
public final class MinyaBeatrice {
    public record P(double x,double y,double z){}
    public static final int FORMA=10,MIRA=4,DISPARO=FORMA+MIRA,INTERVALO=1,VOO=4,CRISTAL=22,QUEBRA=10;
    public static final int ESTACAS=7,ESPINHOS=6,CACOS=10,PONTOS_CIRCULO=48;
    public static final double ALCANCE=10,LEQUE=Math.toRadians(32),COMPRIMENTO=1.7,RAIO=.24;
    /** Maos da Beatrice (ela flutua em ContratoSubaru.LUGAR). */
    public static final P MAOS=new P(ContratoSubaru.LUGAR.x()-.05,ContratoSubaru.LUGAR.y()+.85,ContratoSubaru.LUGAR.z()+.35);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}
    /** Saida com leve passada do alvo (easeOutBack suave): da vida ao surgir dos cristais. */
    private static double volta(double t){double x=lim(t)-1,c=1.1;return 1+(c+1)*x*x*x+c*x*x;}
    /** Recuo antes do disparo, em blocos, ao longo do eixo da mira. */
    public static final double RECUO=.22;
    public static int disparo(int k){return DISPARO+INTERVALO*ordem(k);}
    /** Ordem de disparo: do centro para as pontas, alternando os lados. */
    private static int ordem(int k){int c=ESTACAS/2,d=k-c;return d==0?0:Math.abs(d)*2-(d<0?1:0);}
    public static int acerto(int k){return disparo(k)+VOO;}
    public static int fim(){int f=0;for(int k=0;k<ESTACAS;k++)f=Math.max(f,acerto(k)+CRISTAL+QUEBRA);return f;}

    /** Angulo da estaca k no leque (0 = frente). */
    private static double angulo(int k){return LEQUE*(2.0*k/(ESTACAS-1)-1);}
    /** Onde a estaca k crava: no chao, ao fim do leque, com um pouco de variacao. */
    public static P pontoAcerto(int k){
        double a=angulo(k),d=ALCANCE*(.82+.18*hash(k*7));return new P(MAOS.x()+Math.sin(a)*d,0,MAOS.z()+Math.cos(a)*d);
    }
    /** Lugar da estaca k durante a formacao: arco em volta das maos, atras do circulo. */
    private static P lugar(int k){double a=Math.PI*(.15+.7*k/(ESTACAS-1));return new P(MAOS.x()+Math.cos(a)*.75,MAOS.y()+Math.sin(a)*.55-.15,MAOS.z()+.1);}

    /** Centro da estaca k. */
    public static P centro(int k,double t){
        var l=lugar(k);int d=disparo(k);
        var a=pontoAcerto(k);double ax=a.x()-l.x(),az=a.z()-l.z(),an=Math.sqrt(ax*ax+az*az);ax/=an;az/=an;
        if(t<d){
            // flutua em arco, sobe com passada e treme cada vez mais na mira; puxa para tras antes de sair
            double flut=.05*Math.sin(t*.35+k*.9),deriva=.03*Math.sin(t*.22+k*1.7);
            double tremor=(.012+.03*lim((t-FORMA)/MIRA))*lim(t-FORMA+1);
            double sobe=(1-volta(t/FORMA))*.35,rc=RECUO*s((t-(d-2.5))/2.5);
            return new P(l.x()+deriva+tremor*Math.sin(t*3.1+k)-ax*rc,l.y()-sobe+flut+tremor*Math.cos(t*2.7+k*2),l.z()+tremor*Math.sin(t*2.3+k)-az*rc);
        }
        var ini=new P(l.x()+.03*Math.sin(d*.22+k*1.7)-ax*RECUO,l.y()+.05*Math.sin(d*.35+k*.9),l.z()-az*RECUO);
        var fimVoo=new P(a.x(),a.y()+COMPRIMENTO*.25,a.z());
        double v=lim((t-d)/VOO),u=Math.pow(v,1.6),arco=.55*Math.sin(Math.PI*v);
        return new P(ini.x()+(fimVoo.x()-ini.x())*u,ini.y()+(fimVoo.y()-ini.y())*u+arco,ini.z()+(fimVoo.z()-ini.z())*u);
    }
    /** Direcao (unitaria) da ponta: antes do disparo vira do "para cima" para o alvo; no voo aponta para o acerto. */
    public static P direcao(int k,double t){
        var l=lugar(k);var a=pontoAcerto(k);double dx=a.x()-l.x(),dy=a.y()-l.y(),dz=a.z()-l.z(),n=Math.sqrt(dx*dx+dy*dy+dz*dz);
        double m=s((t-FORMA*.4)/(FORMA*.6+MIRA*.5));double x=dx/n*m,y=dy/n*m+(1-m),z=dz/n*m;
        // depois de cravar a estaca vibra e assenta
        double c=t-acerto(k);if(c>0){double w=.14*Math.exp(-c/2.2)*Math.sin(c*2.6);x+=-dz/n*w;z+=dx/n*w;}
        double nn=Math.sqrt(x*x+y*y+z*z);return new P(x/nn,y/nn,z/nn);
    }
    /** Escala da estaca: cresce na formacao, some quando o cristal do acerto quebra. */
    public static double escala(int k,double t){
        double e=t<ordem(k)*.5?0:volta((t-ordem(k)*.5)/(FORMA-2));int a=acerto(k);return t>=a+CRISTAL?0:e;
    }
    /** Rastro (0..1) no voo. */
    public static double alphaRastro(int k,double t){int d=disparo(k);return t<d||t>acerto(k)+1?0:1-s((t-acerto(k))/1);}

    // ---------- circulo na frente dela ----------
    public static double alphaCirculo(double t){return t<0?0:Math.min(1,t/4)*(1-s((t-DISPARO-ESTACAS*INTERVALO)/6));}
    public static P circulo(int a,int i,double t){
        double g=2*Math.PI*i/PONTOS_CIRCULO+t*.08*(a==0?1:-1),r=(.55+.22*a)*s(t/6);
        return new P(MAOS.x()+Math.cos(g)*r,MAOS.y()+Math.sin(g)*r,MAOS.z()+.25);
    }

    // ---------- cristalizacao no acerto ----------
    /** Espinho j do acerto k: direcao (semiesfera para cima) e comprimento atual. */
    public static P espinho(int k,int j){
        double a=2*Math.PI*(j+hash(k*3+j))/ESPINHOS,el=Math.toRadians(25+45*hash(k*11+j*5));
        return new P(Math.cos(a)*Math.cos(el),Math.sin(el),Math.sin(a)*Math.cos(el));
    }
    public static double tamanhoEspinho(int k,int j,double t){
        double a=t-acerto(k);if(a<0||a>=CRISTAL)return 0;return (.55+.5*hash(k*13+j))*volta((a-j*.25)/3.5);
    }
    /** Clarao do impacto (0..1). */
    public static double impacto(int k,double t){double a=t-acerto(k);return a<0||a>4?0:1-a/4;}
    /** Caco c do acerto k depois que o cristal quebra: posicao e tamanho. */
    public static P caco(int k,int c,double t){
        var p=pontoAcerto(k);double a=Math.max(0,t-acerto(k)-CRISTAL),g=2*Math.PI*hash(k*17+c),v=.06+.05*hash(k*19+c),up=.12+.08*hash(c*23+k);
        return new P(p.x()+Math.cos(g)*v*a,Math.max(.03,.3+up*a-.02*a*a),p.z()+Math.sin(g)*v*a);
    }
    public static double tamanhoCaco(int k,int c,double t){double a=t-acerto(k)-CRISTAL;return a<0||a>=QUEBRA?0:(.06+.05*hash(k*29+c))*(1-a/QUEBRA);}

    private MinyaBeatrice(){}
}
