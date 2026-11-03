package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Mugetsu (C no Bankai, efeito 530) em volume 3D. Ticks a partir do cast:
 * 0-20 Getsuga final: reiatsu escuro baixo em volta do corpo ate a forma Mugetsu (cabelo longo, faixas);
 * 30-50 chamas negras envolvem a Tensa Zangetsu erguida; 50 golpe (dano do servidor): um corte fino, reto e altissimo
 * sobe do chao ao ceu a frente e as trevas vazam dele;
 * 80 a forma se desfaz, o Bankai acaba e os poderes ficam BLOQUEIO ticks bloqueados.
 * Origem nos pes; x lateral (direita), y altura, z frente.
 */
public final class MugetsuIchigo {
    public record P(double x,double y,double z){}
    public static final int FORMA=20,LAMINA=30,GOLPE=50,DESTRANSFORMA=200,FIM=GOLPE+DESTRANSFORMA,BLOQUEIO=1200; // forma segue 10 s (200 ticks) depois do golpe
    public static final int COLUNA=10,VOLTAS=10;
    public static final double ALCANCE=50,ALTURA_DANO=14,DANO=60;
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    public static String fase(double t){return t<FORMA?"GETSUGA FINAL":t<LAMINA?"FORMA MUGETSU":t<GOLPE?"LAMINA DE TREVAS":t<GOLPE+3?"MUGETSU: A LINHA SOBE":t<FIM?"TREVAS NA LINHA":"FIM";}
    /** Forma Mugetsu visivel (cabelo, faixas, corpo escuro) entre o estouro do casulo e o fim. */
    public static boolean forma(double t){return t>=FORMA&&t<FIM;}

    /** Meia-largura do cone de dano a z blocos a frente (o servidor usa a mesma funcao). */
    public static double largura(double z){return 4+z*.3;}
    public static boolean atinge(double x,double y,double z){return z>=0&&z<=ALCANCE&&Math.abs(x)<=largura(z)&&y>=-2&&y<=ALTURA_DANO;}

    // ---------- Chamas negras em volta do corpo (referencia: labaredas grossas de borda macia) ----------
    public static final int LABAREDAS=14,ANEL_CHAMA=12,TUBO_CHAMA=8,FRAGMENTOS=18;
    /** Intensidade da aura: acende ate a forma (tick 20), forte ate o golpe e se apaga logo depois dele (pedido do usuario). */
    public static double aura(double t){return s(t/12.0)*(1-s((t-GOLPE-2)/8.0));}
    /** Altura da labareda k: tremula devagar (duas senoides) e cresce com a aura. */
    public static double alturaLabareda(int k,double t){return (1.9+1.3*hash(k)+.5*Math.sin(t*.31+k*1.7)+.3*Math.sin(t*.77+k))*aura(t);}
    /** Eixo da labareda: nasce num anel nos pes, sobe abrindo para fora e lambe para dentro na ponta, balancando. */
    public static P eixoLabareda(int k,double u,double t){
        double a=k*Math.PI*2/LABAREDAS+hash(k+1)*.4,r=.55+.35*Math.sin(Math.PI*u*.9)-.25*u*u,bal=Math.sin(t*.4+k*2.1+u*3)*.22*u;
        return new P(Math.cos(a)*r+Math.cos(a+1.57)*bal,u*alturaLabareda(k,t),Math.sin(a)*r+Math.sin(a+1.57)*bal);
    }
    /** Superficie da labareda (anel i ao longo, j em volta): gorda na base, ponta afiada, bolhas que sobem. */
    public static P labareda(int k,int i,int j,double t){
        double u=i/(ANEL_CHAMA-1.0),q=j*Math.PI*2/TUBO_CHAMA;var c=eixoLabareda(k,u,t);
        double r=(.34*Math.pow(1-u,.75)+.02)*(1+.22*Math.sin(q*2+u*9-t*.9+k)+.12*Math.sin(q*3-u*14+t*1.3));
        return new P(c.x()+Math.cos(q)*r,c.y(),c.z()+Math.sin(q)*r);
    }
    /** Fragmento k de trevas que se solta das pontas e sobe encolhendo (ciclo de 14 ticks). */
    public static P fragmento(int k,double t){
        double ciclo=(t+hash(k)*14)%14,a=k*2.39996+Math.floor((t+hash(k)*14)/14),r=.6+.5*hash(k+2);
        return new P(Math.cos(a)*r*(1+ciclo*.03),2.1+ciclo*.16+hash(k+3)*.8,Math.sin(a)*r*(1+ciclo*.03));
    }
    public static double tamanhoFragmento(int k,double t){double ciclo=(t+hash(k)*14)%14;return (.13+.08*hash(k+4))*(1-ciclo/14)*aura(t);}

    // ---------- Golpe: um corte fino, reto e altissimo (referencia do usuario) ----------
    public static final double LINHA_Z=2,LINHA_ALTURA=72,LINHA_ESPESSURA=.14;
    /** Ponta superior da linha: sobe do chao ao alto em 3 ticks a partir do golpe (correcao do usuario). */
    public static double topoLinha(double t){return t<GOLPE?0:LINHA_ALTURA*s((t-GOLPE)/3.0);}
    /** Frente do corte: avanca em linha reta de LINHA_Z ate o alcance em 6 ticks, sem mudar espessura nem altura
     *  (correcao do usuario: "um corte que vai em linha reta se expandindo para frente na mesma finura e largura"). */
    public static double frenteLinha(double t){return t<GOLPE?LINHA_Z:LINHA_Z+(ALCANCE-LINHA_Z)*s((t-GOLPE)/6.0);}
    /** Espessura constante: fio finissimo enquanto existe. */
    public static double espessuraLinha(double t){return t<GOLPE?0:LINHA_ESPESSURA;}
    public static double alphaLinha(double t){return t<GOLPE?0:1-s((t-GOLPE-16)/6.0);}
    /** Halo escuro em volta do fio (largura relativa) enquanto as trevas vazam dele. */
    public static double halo(double t){return t<GOLPE+1?0:2.0*s((t-GOLPE-1)/5.0)*(1-s((t-GOLPE-10)/8.0));}

    // ---------- Lamina de trevas ----------
    /** Comprimento da lamina negra que cresce da mao erguida. */
    public static double comprimento(double t){return t<LAMINA?0:3.1*s((t-LAMINA)/10.0);}
    /** Giro do pulso que alinha a Tensa Zangetsu ao braco erguido (como no preparo do Bankai): a lamina aponta
     *  para cima e, no golpe, para a frente; as chamas negras envolvem a propria espada. */
    public static double pulso(double t){return 90*s((t-22)/4.0)*(1-s((t-GOLPE-8)/4.0)); /* volta ao normal logo apos o corte, quando o braco desce */}
    public static final int LINGUAS=16;
    /** Lingua de chama negra k saindo da lamina (u 0 = na lamina, 1 = ponta), subindo e tremulando. */
    public static P linguaLamina(int k,double u,double t){
        double a=inclinacao(t),v=(k+.5)/LINGUAS,L=comprimento(t),bx=MAO.x(),by=MAO.y()+Math.cos(a)*v*L,bz=MAO.z()+Math.sin(a)*v*L;
        double alt=(.5+.6*hash(k+Math.floor(t/2)))*u,lado=(hash(k+7)-.5)*.5*u+Math.sin(t*.9+k+u*4)*.12*u;
        return new P(bx+lado,by+alt,bz-alt*.3*Math.sin(a));
    }
    /** Angulo da lamina: 0 = para cima, desce para a frente (120 graus) em 3 ticks no golpe. */
    public static double inclinacao(double t){return Math.toRadians(125*s((t-GOLPE)/3.0));}
    public static double alphaLamina(double t){return t<LAMINA?0:s((t-LAMINA)/3.0)*(1-s((t-GOLPE-3)/5.0));}
    public static final P MAO=new P(.38,2.45,.15);
    /** Superficie da lamina (i ao longo, j em volta): tubo de chamas, mais grosso no meio, ponta afiada. */
    public static P lamina(int i,int j,double t){
        double u=i/(COLUNA-1.0),L=comprimento(t),a=inclinacao(t),q=j*Math.PI*2/VOLTAS,r=(.22+.12*Math.sin(u*Math.PI))*(1-u*.85)*(1+.25*Math.sin(q*3+u*9-t*1.2));
        double ax=0,ay=Math.cos(a),az=Math.sin(a),px=1,pz=0,py=0,qx=0,qy=-Math.sin(a),qz=Math.cos(a);
        double cx=MAO.x()+ax*u*L,cy=MAO.y()+ay*u*L,cz=MAO.z()+az*u*L;
        return new P(cx+(px*Math.cos(q)+qx*Math.sin(q))*r,cy+(py*Math.cos(q)+qy*Math.sin(q))*r,cz+(pz*Math.cos(q)+qz*Math.sin(q))*r);
    }

}
