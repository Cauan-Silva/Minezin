package com.cauansilva.arcanecontrol.core.vfx;

import com.cauansilva.arcanecontrol.core.ichigo.PerfilIchigo;

/**
 * Getsuga Tensho (efeitos 520 Shikai e 524 Bankai) conforme o anime: o reiatsu sobe pelo corpo antes do golpe,
 * o corte diagonal solta uma meia-lua que cresce em 3 ticks, viaja reta com pos-imagens e rasga o chao; ao parar
 * (alcance maximo ou parede) se parte em lascas. Shikai: azul-branco luminoso; Bankai: nucleo preto com borda vermelha
 * e chamas mais irregulares. Tempo t a partir da liberacao (tick do dano do servidor, PerfilIchigo.impacto(0,forma)).
 * Coordenadas no referencial do golpe: x lateral (direita), y altura, z para frente; origem nos olhos.
 */
public final class GetsugaIchigo {
    public record P(double x,double y,double z){}
    public static final int CRESCE=3,VIAGEM=20,DISSIPA=8,FIM=VIAGEM+DISSIPA,PONTOS=24,CHAMAS=18,LASCAS=14,ECOS=2,LINGUAS=16;
    /** Inclinacao do corte diagonal (de cima a direita para baixo a esquerda), graus. */
    public static final double INCLINACAO=35,ABERTURA=Math.toRadians(76);

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    public static double raio(boolean bankai){return bankai?2.9:2.4;}
    public static double espessura(boolean bankai){return bankai?1.15:.9;}

    /** Distancia do centro da meia-lua: a mesma frente de colisao do servidor. */
    public static double distancia(double t){
        t=Math.max(0,t);int i=(int)Math.floor(t);double a=PerfilIchigo.alcance(i);return a+(PerfilIchigo.alcance(i+1)-a)*(t-i);
    }
    /** Tick em que a meia-lua parou: alcance maximo, ou antes se bateu num bloco a 'parada' blocos. */
    public static double parada(double bloqueio){return Math.min(VIAGEM,Math.max(0,bloqueio)/1.4);}
    /** 0..1: a meia-lua nasce pequena na lamina e cresce ate o tamanho cheio. */
    public static double escala(double t){return .35+.65*s(t/CRESCE);}
    /** 0..1 enquanto se desfaz depois de parar no tick 'fim'. */
    public static double dissolve(double t,double fim){return s((t-fim)/DISSIPA);}
    public static double alpha(double t,double fim){return t<0?0:Math.min(1,(t+1)/1.5)*(1-dissolve(t,fim));}

    /** Ponto do arco de raio rr: o arco fica no plano (eixo do corte, frente), com o meio bojudo apontado para o
     *  alvo e as pontas recurvadas para tras; a espessura 3D fica na perpendicular, que e o que se ve de tras. */
    private static P eixo(double th,double r,double rr,double recuo){
        double a=Math.toRadians(INCLINACAO),u=Math.sin(th)*rr;
        return new P(Math.sin(a)*u,Math.cos(a)*u,Math.cos(th)*rr-r-recuo);
    }
    /** Direcao da espessura (perpendicular ao plano do arco). */
    public static P perpendicular(){double a=Math.toRadians(INCLINACAO);return new P(Math.cos(a),-Math.sin(a),0);}
    /** Borda da frente (afiada, por fora) no ponto i, relativa ao centro da meia-lua. */
    public static P frente(int i,boolean bankai){double r=raio(bankai);return eixo(th(i),r,r,0);}
    /** Borda de dentro: grossa no meio, afinando nas pontas, com recortes de chama que mudam a cada 2 ticks. */
    public static P tras(int i,double t,boolean bankai){
        double th=th(i),r=raio(bankai),k=Math.pow(Math.cos(th),1.5),v=Math.floor(t/2),rasgo=(bankai?.45:.25)*hash(i*3+v*11)*k;
        return eixo(th,r,r-espessura(bankai)*k-rasgo,.3*k+rasgo*.3);
    }
    public static final int SECAO=12;
    public static double profundidade(boolean bankai){return bankai?1.3:1.1;}
    /** Volume 3D: secao em cunha (fio afiado por fora em v=0, dorso grosso por dentro em v=pi) varrida pelo arco;
     *  a profundidade vai para frente/tras do plano e some nas pontas. */
    public static P volume(int i,int j,double t,boolean bankai){
        var o=frente(i,bankai);var n=tras(i,t,bankai);double v=j*Math.PI*2/SECAO,k=Math.pow(Math.cos(th(i)),1.5);
        double cx=(o.x()+n.x())/2,cy=(o.y()+n.y())/2,cz=(o.z()+n.z())/2,rx=(o.x()-n.x())/2,ry=(o.y()-n.y())/2,rz=(o.z()-n.z())/2;
        double d=profundidade(bankai)*k*Math.sin(v)*(.3+.7*(1-Math.cos(v))/2);var pp=perpendicular();
        return new P(cx+rx*Math.cos(v)+pp.x()*d,cy+ry*Math.cos(v)+pp.y()*d,cz+rz*Math.cos(v));
    }
    /** Luz direcional (de cima, da direita e de tras do jogador) no referencial do golpe. */
    public static final double LX=.45,LY=.8,LZ=-.4;
    /** Normal da superficie no vertice (i,j), por diferencas no proprio volume. */
    public static P normal(int i,int j,double t,boolean bankai){
        var a=volume(Math.min(PONTOS-1,i+1),j,t,bankai);var b=volume(Math.max(0,i-1),j,t,bankai);
        var c=volume(i,(j+1)%SECAO,t,bankai);var d=volume(i,(j+SECAO-1)%SECAO,t,bankai);
        double ux=a.x()-b.x(),uy=a.y()-b.y(),uz=a.z()-b.z(),vx=c.x()-d.x(),vy=c.y()-d.y(),vz=c.z()-d.z();
        double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx,l=Math.sqrt(nx*nx+ny*ny+nz*nz);if(l<1e-9)return new P(0,0,-1);
        return new P(nx/l,ny/l,nz/l);
    }
    /** Brilho do vertice: difusa (dois lados, a secao e fechada) + borda que acende de raspao para a camera. */
    public static double borda(P n,double vx,double vy,double vz){
        double vl=Math.sqrt(vx*vx+vy*vy+vz*vz);return vl<1e-9?0:Math.pow(1-Math.abs((n.x()*vx+n.y()*vy+n.z()*vz)/vl),3);
    }
    public static double luz(P n,double vx,double vy,double vz){
        double l=Math.sqrt(LX*LX+LY*LY+LZ*LZ),dif=Math.abs(n.x()*LX+n.y()*LY+n.z()*LZ)/l,vl=Math.sqrt(vx*vx+vy*vy+vz*vz);
        double fr=vl<1e-9?0:Math.pow(1-Math.abs((n.x()*vx+n.y()*vy+n.z()*vz)/vl),3);
        return Math.min(1.25,.28+.72*dif+.55*fr);
    }
    private static double th(int i){return -ABERTURA+2*ABERTURA*i/(PONTOS-1.0);}

    /** Chama j saindo da borda de tras para tras e para fora, tremulando. */
    public static P chama(int j,double u,double t,boolean bankai){
        int i=1+(int)(hash(j)*(PONTOS-2));var b=tras(i,t,bankai);double th=th(i),a=Math.toRadians(INCLINACAO),comp=(.5+.9*hash(j+7+Math.floor(t/2)))*(bankai?1.35:1);
        double fora=Math.sin(th)*.35*u*comp,ond=Math.sin(u*5+t*.9+j)*.12*u;
        return new P(b.x()+Math.sin(a)*fora+ond,b.y()+Math.cos(a)*fora,b.z()-u*comp);
    }

    /** Pos-imagem k (1..ECOS) atras da meia-lua: distancia e alpha relativo. */
    public static double distanciaEco(int k,double t){return Math.max(0,distancia(t-k*1.5));}
    public static double alphaEco(int k,double t,double fim){return t>=fim?0:(k==1?.38:.16)*s((t-k*1.5)/1.5);}

    /** Rasgo no chao em z (distancia ao longo do caminho): abre quando a meia-lua passa e some devagar. */
    public static double alphaSulco(double z,double t){
        double idade=t-z/1.4;return idade<0?0:s(idade/1.5)*(1-s((idade-8)/12.0));
    }
    public static double larguraSulco(double z,boolean bankai){return (bankai?.75:.55)*(.85+.15*Math.sin(z*1.3)+.1*Math.sin(z*3.7));}

    /** Lasca k depois de parar: sai de um ponto da meia-lua, abre para fora e para frente. */
    public static P lasca(int k,double t,double fim,boolean bankai){
        int i=(int)Math.round(k*(PONTOS-1.0)/(LASCAS-1));var f=frente(i,bankai);double d=dissolve(t,fim)*(1.2+hash(k+3)*1.6);
        double n=Math.hypot(f.x(),f.y());n=n<1e-6?1:n;
        return new P(f.x()+f.x()/n*d,f.y()+f.y()/n*d+(hash(k+9)-.5)*d*.6,f.z()+d*.8);
    }

    /** Reiatsu antes do golpe (efeitos 500/510): intensidade 0..1 ate o tick do impacto. */
    public static double carga(double t,int impacto){return t>=impacto+1?0:s(t/Math.max(1,impacto*.8));}
    /** Lingua de reiatsu j subindo pelo corpo: altura u 0..1, relativa aos pes. */
    public static P lingua(int j,double u,double t,boolean bankai){
        double a=j*Math.PI*2/LINGUAS+hash(j)*.6,r=.55+.12*Math.sin(t*.8+j)+(bankai?.1:0),alt=(1.9+.9*hash(j+5+Math.floor(t/2)))*(bankai?1.15:1);
        return new P(Math.cos(a)*r*(1-u*.35)+Math.sin(u*6+t+j)*.08,u*alt,Math.sin(a)*r*(1-u*.35));
    }
}
