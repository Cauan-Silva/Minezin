package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Geometria visual do Buraco Negro da Frieren, relativa ao centro da singularidade e
 * baseada na cena do anime: forma-se na mao, voa como cometa e chega ao centro em ABERTURA,
 * quando a atracao do servidor comeca. Na tensao os detritos viram uma casca rachada, que
 * explode em espinhos violeta e rachaduras ciano no COLAPSO; faiscas e estilhacos ate FIM.
 */
public final class BuracoNegroFrieren {
    public record Ponto(double x,double y,double z){}
    public static final int LANCAMENTO=PerfilFrieren.ABERTURA-20,TENSAO=PerfilFrieren.COLAPSO-24;
    public static final int LINHAS=8,RACHADURAS=9,PONTOS_RACHADURA=10,ESPINHOS=8,RELAMPAGOS=10,FAISCAS=40,ESTILHACOS=18;
    public static final double RAIO_CENTRO=2.2,RAIO_MAO=.42,RAIO_CASCA=3.8;
    private static final int A=PerfilFrieren.ABERTURA,C=PerfilFrieren.COLAPSO,F=PerfilFrieren.FIM;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}
    public static double tensao(double t){return t>=C?1:s((t-TENSAO)/24.0);}

    /** Mao ate LANCAMENTO, trajeto acelerado com arco curto, centro a partir de ABERTURA. */
    public static Ponto posicao(Ponto mao,double t){
        if(t<=LANCAMENTO)return mao;if(t>=A)return new Ponto(0,0,0);
        double k=Math.pow((t-LANCAMENTO)/(double)(A-LANCAMENTO),2),arco=Math.sin(k*Math.PI)*1.5;
        return new Ponto(mao.x()*(1-k),mao.y()*(1-k)+arco,mao.z()*(1-k));
    }
    public static boolean emVoo(double t){return t>LANCAMENTO&&t<A;}

    public static double nucleo(double t){
        if(t<A)return RAIO_MAO*s(t/12)*(1+.08*Math.sin(t*.9));
        if(t>=C)return RAIO_CENTRO*.6*(1-s((t-C)/4.0));
        double abre=s((t-A)/8.0),aperto=tensao(t);
        double r=RAIO_MAO+(RAIO_CENTRO-RAIO_MAO)*abre*(1+.12*Math.sin(Math.min(1,(t-A)/8.0)*Math.PI));
        return r*(1+.04*Math.sin(t*.35))*(1-.4*aperto);
    }
    public static double halo(double t){return nucleo(t)*(t<A?3.4:3.6);}
    /** Meio comprimento do traco horizontal que atravessa a singularidade. */
    public static double estria(double t){
        if(t<A)return 2.4*s(t/16);
        return (14+.8*Math.sin(t*.21))*s((t-A)/10.0)*(1-.5*tensao(t))*(t>=C?0:1)+(t<A+10?2.4*(1-s((t-A)/10.0)):0);
    }
    public static double larguraEstria(double t){return t<A?.16:.42*(1-.4*tensao(t));}

    /** Aneis de ar distorcido que nascem na borda e se afastam. */
    public static double raioDistorcao(int k,double t){
        double fase=t*.035+k/3.0;return nucleo(t)*(1.4+(fase-Math.floor(fase))*3.6);
    }
    public static double alphaDistorcao(int k,double t){
        double fase=t*.035+k/3.0;fase-=Math.floor(fase);
        return Math.sin(fase*Math.PI)*.35*s(t/14)*(t>=C?0:1);
    }

    /** s percorre a linha de vento de fora (0) para o nucleo (1). */
    public static Ponto linhaVento(int j,double s,double t){
        double r=nucleo(t)*1.2+(4+7*hash(j))*(1-s);
        double a=j*2.399963+s*(3.2+2*tensao(t))-t*.07;
        double inclinacao=(hash(j+9)-.5)*1.4;
        return new Ponto(Math.cos(a)*r,Math.sin(a+j)*r*inclinacao*.5,Math.sin(a)*r);
    }
    public static double alphaVento(int j,double s,double t){
        double frente=((t*.045+hash(j+3))%1.0);
        double crista=Math.exp(-Math.pow((s-frente)*4.5,2));
        return crista*Math.sin(Math.min(1,s)*Math.PI)*s((t-A+6)/12.0)*(1-s((t-C+4)/4.0));
    }
    public static double orbitaDetritos(double t){return 5.5+(RAIO_CASCA*.95-5.5)*tensao(t);}

    public static double presencaCasca(double t){return t>=C?1-s((t-C)/3.0):s((t-TENSAO-4)/12.0);}
    /** Ponto i da rachadura j na superficie unitaria da casca, alongada na vertical. */
    public static Ponto rachadura(int j,int i){
        double lon=hash(j)*Math.PI*2,lat=(hash(j+31)-.5)*2.2;
        for(int k=1;k<=i;k++){lon+=(hash(j*17+k)-.5)*.7;lat+=(hash(j*23+k*5)-.5)*.55;}
        lat=Math.max(-1.4,Math.min(1.4,lat));
        return new Ponto(Math.cos(lat)*Math.cos(lon),Math.sin(lat)*1.2,Math.cos(lat)*Math.sin(lon));
    }
    /** Quantos segmentos de cada rachadura ja se abriram. */
    public static double aberturaRachaduras(double t){return (PONTOS_RACHADURA-1)*s((t-TENSAO-8)/16.0);}

    public static Ponto direcaoEspinho(int j){
        double a=j*Math.PI*2/ESPINHOS+(hash(j)-.5)*.5,y=(hash(j+40)-.5)*1.1;
        double r=Math.sqrt(Math.max(0,1-y*y*.6));return new Ponto(Math.cos(a)*r,y,Math.sin(a)*r);
    }
    public static double comprimentoEspinho(int j,double t){return t<C?0:(16+8*hash(j+7))*s((t-C)/5.0);}
    public static double alphaEspinho(double t){return t<C?0:1-s((t-C-4)/16.0);}

    public static Ponto relampago(int j,int i){
        double a=hash(j+60)*Math.PI*2,y=(hash(j+70)-.5)*.9,passo=1.6;
        double x=0,yy=0,z=0;
        for(int k=1;k<=i;k++){
            a+=(hash(j*13+k)-.5)*.9;double h=y+(hash(j*7+k*3)-.5)*.8;
            x+=Math.cos(a)*passo;yy+=h*passo*.6;z+=Math.sin(a)*passo;
        }
        return new Ponto(x,yy,z);
    }
    public static double alcanceRelampago(double t){return t<C+4?0:8*s((t-C-4)/8.0);}
    public static double alphaRelampago(double t){return t<C+4?0:1-s((t-C-12)/18.0);}

    public static Ponto faisca(int i,double t){
        double idade=Math.max(0,t-C-8),a=hash(i)*Math.PI*2,r=2+hash(i+5)*9+idade*.03;
        return new Ponto(Math.cos(a)*r,(hash(i+11)-.3)*6+idade*(.02+hash(i+17)*.04),Math.sin(a)*r);
    }
    public static double alphaFaisca(int i,double t){
        double idade=t-C-8;if(idade<0)return 0;
        double brilho=.55+.45*Math.sin(idade*(.3+hash(i+23)*.5)+i);
        return s(idade/5)*(1-s((idade-(F-C-8)*.45)/((F-C-8)*.55)))*brilho;
    }
    public static Ponto estilhaco(int i,double t){
        double idade=Math.max(0,t-C-2),a=hash(i+80)*Math.PI*2,v=.25+hash(i+81)*.35;
        double subida=.35+hash(i+82)*.3,y=1+idade*subida-idade*idade*.012;
        return new Ponto(Math.cos(a)*v*idade,Math.max(-4,y),Math.sin(a)*v*idade);
    }
    public static double alphaEstilhaco(int i,double t){
        double idade=t-C-2;if(idade<0)return 0;return s(idade/3)*(1-s((idade-24)/20.0));
    }
}
