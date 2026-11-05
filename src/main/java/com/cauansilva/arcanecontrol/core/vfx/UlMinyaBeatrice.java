package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Ul Minya (G-V do Subaru, ultimate da forma de contrato, lancada pela Beatrice): a versao maxima da Minya. Ela sobe,
 * ergue os bracos e um circulo violeta enorme abre no ceu sobre a area na frente do Subaru, com uma zona de aviso no
 * chao. Dezenas de estacas de cristal chovem aceleradas, cravam espalhadas pela area e cristalizam em espinhos; depois
 * um cristal gigante cresce no centro e prende tudo. Quando ela baixa os bracos, todos os cristais se partem juntos
 * num clarao e os cacos voam para fora.
 * Tempo t a partir do cast. Referencial: origem nos pes do Subaru, x lateral (+x = lado da Beatrice), y para cima,
 * z para frente. Classe pura para a previa e os testes; o servidor usa pontoAcerto(k)/acerto(k) para o dano de cada
 * estaca, prende (atordoa) quem esta em RAIO_AREA de CENTRO_AREA entre PRISAO e QUEBRA e da o dano final na QUEBRA.
 */
public final class UlMinyaBeatrice {
    public record P(double x,double y,double z){}
    public static final int QUEDA=7,ABRE=16,CHUVA=26,PRISAO=ABRE+CHUVA+QUEDA+2,CRESCE=8,QUEBRA=PRISAO+CRESCE+12,VOO_CACO=16,FIM=QUEBRA+VOO_CACO+4;
    public static final int ESTACAS=90,ESPINHOS=4,CACOS=4,PONTOS_CIRCULO=160,ANEIS=3,RAIOS_CIRCULO=16,PONTOS_ZONA=120;
    public static final int GIGANTES=15,CACOS_GIGANTE=12,SATELITES=6;
    public static final double RAIO_AREA=12,ALTURA_CEU=15,COMPRIMENTO=2.2,RAIO=.3;
    public static final P CENTRO_AREA=new P(0,0,10);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    private static double lim(double v){return Math.max(0,Math.min(1,v));}
    private static double volta(double t){double x=lim(t)-1,c=1.1;return 1+(c+1)*x*x*x+c*x*x;}

    // ---------- circulo no ceu e zona no chao ----------
    public static double aberturaCirculo(double t){return volta(t/ABRE);}
    public static double alphaCirculo(double t){return t<0||t>=QUEBRA+8?0:Math.min(1,t/4)*(t<QUEBRA?1:1-(t-QUEBRA)/8);}
    public static P circulo(int a,int i,double t){
        double g=2*Math.PI*i/PONTOS_CIRCULO+t*(.02+.012*a)*(a%2==0?1:-1),r=RAIO_AREA*(.6+.2*a)*aberturaCirculo(t);
        return new P(CENTRO_AREA.x()+Math.cos(g)*r,ALTURA_CEU+.05*a,CENTRO_AREA.z()+Math.sin(g)*r);
    }
    /** Raio k do circulo: liga o anel de dentro ao de fora (as "pétalas" do selo). */
    public static P raioCirculo(int k,double u,double t){
        double g=2*Math.PI*k/RAIOS_CIRCULO-t*.015,r=RAIO_AREA*(.6+.4*u)*aberturaCirculo(t);
        return new P(CENTRO_AREA.x()+Math.cos(g)*r,ALTURA_CEU,CENTRO_AREA.z()+Math.sin(g)*r);
    }
    public static P zona(int i,double t){double g=2*Math.PI*i/PONTOS_ZONA,r=RAIO_AREA*s(t/ABRE);return new P(CENTRO_AREA.x()+Math.cos(g)*r,.03,CENTRO_AREA.z()+Math.sin(g)*r);}
    public static double alphaZona(double t){return t<2||t>=QUEBRA+4?0:Math.min(1,(t-2)/4)*(.55+.25*Math.sin(t*.6))*(t<QUEBRA?1:1-(t-QUEBRA)/4);}

    // ---------- chuva de estacas ----------
    /** Tick em que a estaca k sai do circulo: ordem embaralhada (37 e primo com 90) para cair espalhada pela area toda. */
    public static double sai(int k){int ordem=(k*37)%ESTACAS;return ABRE+CHUVA*Math.pow((ordem+hash(k*5)*.8)/ESTACAS,1.25);}
    public static double acerto(int k){return sai(k)+QUEDA;}
    /** Onde a estaca k crava: pontos de Fibonacci no disco da area. */
    public static P pontoAcerto(int k){double r=RAIO_AREA*.92*Math.sqrt((k+.5)/ESTACAS),g=k*2.399963;return new P(CENTRO_AREA.x()+Math.cos(g)*r,0,CENTRO_AREA.z()+Math.sin(g)*r);}
    private static P saida(int k){var a=pontoAcerto(k);double g=2*Math.PI*hash(k*3);return new P(a.x()+Math.cos(g)*1.6,ALTURA_CEU-.3,a.z()+Math.sin(g)*1.6);}
    public static P centro(int k,double t){
        var o=saida(k);var a=pontoAcerto(k);var f=new P(a.x(),COMPRIMENTO*.25,a.z());double u=Math.pow(lim((t-sai(k))/QUEDA),1.8);
        return new P(o.x()+(f.x()-o.x())*u,o.y()+(f.y()-o.y())*u,o.z()+(f.z()-o.z())*u);
    }
    /** Direcao da ponta (unitaria): para baixo, ao longo da queda. */
    public static P direcao(int k){var o=saida(k);var a=pontoAcerto(k);double dx=a.x()-o.x(),dy=COMPRIMENTO*.25-o.y(),dz=a.z()-o.z(),n=Math.sqrt(dx*dx+dy*dy+dz*dz);return new P(dx/n,dy/n,dz/n);}
    /** Escala da estaca: surge saindo do circulo e some quando tudo quebra. */
    public static double escala(int k,double t){return t<sai(k)-2||t>=QUEBRA?0:volta((t-sai(k)+2)/3);}
    public static double alphaRastro(int k,double t){double a=t-sai(k);return a<0||a>QUEDA+1?0:1-s((a-QUEDA)/1);}
    public static double impacto(int k,double t){double a=t-acerto(k);return a<0||a>4?0:1-a/4;}
    public static P espinho(int k,int j){
        double a=2*Math.PI*(j+hash(k*3+j))/ESPINHOS,el=Math.toRadians(30+40*hash(k*11+j*5));
        return new P(Math.cos(a)*Math.cos(el),Math.sin(el),Math.sin(a)*Math.cos(el));
    }
    public static double tamanhoEspinho(int k,int j,double t){double a=t-acerto(k);return a<0||t>=QUEBRA?0:(.65+.55*hash(k*13+j))*volta((a-j*.3)/3.5);}

    // ---------- cristal gigante (prisao) ----------
    /**
     * Cristal g da prisao: g=0 e o do meio (vertical, o mais alto), 1..8 saem inclinados em volta dele e os
     * SATELITES ultimos sao aglomerados menores espalhados a meio caminho da borda, para a prisao cobrir a area toda.
     */
    public static boolean satelite(int g){return g>=GIGANTES-SATELITES;}
    public static P baseGigante(int g){
        if(!satelite(g))return CENTRO_AREA;double a=2*Math.PI*(g-(GIGANTES-SATELITES))/SATELITES+.5,r=RAIO_AREA*(.5+.08*hash(g*13));
        return new P(CENTRO_AREA.x()+Math.cos(a)*r,0,CENTRO_AREA.z()+Math.sin(a)*r);
    }
    public static P eixoGigante(int g){
        if(g==0)return new P(0,1,0);
        if(satelite(g)){double a=2*Math.PI*hash(g*17),el=Math.toRadians(70+12*hash(g*7));return new P(Math.cos(a)*Math.cos(el),Math.sin(el),Math.sin(a)*Math.cos(el));}
        double a=2*Math.PI*(g-1)/(GIGANTES-SATELITES-1)+.3,el=Math.toRadians(55+12*hash(g*7));
        return new P(Math.cos(a)*Math.cos(el),Math.sin(el),Math.sin(a)*Math.cos(el));
    }
    private static double tamanhoGigante(int g){return g==0?7:satelite(g)?3+1.2*hash(g*3):3.6+1.6*hash(g*3);}
    /** Os do meio crescem primeiro; os satelites logo depois, como se a prisao se espalhasse. */
    private static double atrasoGigante(int g){return g==0?0:satelite(g)?2.5+.5*(g-(GIGANTES-SATELITES)):1+.3*g;}
    public static double comprimentoGigante(int g,double t){return t>=QUEBRA?0:tamanhoGigante(g)*volta((t-PRISAO-atrasoGigante(g))/CRESCE);}
    public static double raioGigante(int g){return g==0?1.25:satelite(g)?.5+.15*hash(g*5):.62+.18*hash(g*5);}
    /** Brilho que sobe pelos cristais antes de quebrar (aviso de 1 s). */
    public static double carga(double t){return t<QUEBRA-20?0:t>=QUEBRA?0:s((t-QUEBRA+20)/20);}

    // ---------- quebra ----------
    public static double clarao(double t){double a=t-QUEBRA;return a<0||a>10?0:Math.exp(-a/3)*(1-a/10);}
    /** Caco c da estaca k, nasce no ponto do acerto e voa para fora do centro, caindo. */
    public static P caco(int k,int c,double t){
        var p=pontoAcerto(k);double a=Math.max(0,t-QUEBRA),dx=p.x()-CENTRO_AREA.x(),dz=p.z()-CENTRO_AREA.z(),n=Math.max(.3,Math.sqrt(dx*dx+dz*dz));
        double g=2*Math.PI*hash(k*17+c),v=.12+.08*hash(k*19+c),sobe=.2+.12*hash(c*23+k);
        return new P(p.x()+(dx/n*.6+Math.cos(g)*.5)*v*a,Math.max(.03,.35+sobe*a-.018*a*a),p.z()+(dz/n*.6+Math.sin(g)*.5)*v*a);
    }
    public static double tamanhoCaco(int k,int c,double t){double a=t-QUEBRA;return a<0||a>=VOO_CACO?0:(.1+.07*hash(k*29+c))*(1-a/VOO_CACO);}
    /** Caco c do cristal gigante g: nasce ao longo do cristal e explode para fora. */
    public static P cacoGigante(int g,int c,double t){
        var e=eixoGigante(g);var b=baseGigante(g);double u=(c+.5)/CACOS_GIGANTE,L=tamanhoGigante(g),a=Math.max(0,t-QUEBRA);
        double px=b.x()+e.x()*L*u,py=e.y()*L*u,pz=b.z()+e.z()*L*u,ang=2*Math.PI*hash(g*31+c),v=.18+.14*hash(g*37+c);
        return new P(px+Math.cos(ang)*v*a,Math.max(.03,py+(.14+.1*hash(c+g))*a-.025*a*a),pz+Math.sin(ang)*v*a);
    }
    public static double tamanhoCacoGigante(int g,int c,double t){double a=t-QUEBRA;return a<0||a>=VOO_CACO?0:(.18+.12*hash(g*41+c))*(1-a/VOO_CACO);}

    /** Dentro da area (servidor). */
    public static boolean naArea(double x,double z){double dx=x-CENTRO_AREA.x(),dz=z-CENTRO_AREA.z();return dx*dx+dz*dz<=RAIO_AREA*RAIO_AREA;}

    private UlMinyaBeatrice(){}
}
