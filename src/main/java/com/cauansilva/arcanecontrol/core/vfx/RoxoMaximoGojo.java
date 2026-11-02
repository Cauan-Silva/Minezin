package com.cauansilva.arcanecontrol.core.vfx;

/** Local camera/VFX choreography around the fixed focus of existing effect 32. */
public final class RoxoMaximoGojo {
    public static final int COLAPSO=90,DURACAO=110;
    public record V(double x,double y,double z){
        public V add(V b){return new V(x+b.x,y+b.y,z+b.z);}
        public V sub(V b){return new V(x-b.x,y-b.y,z-b.z);}
        public V escala(double s){return new V(x*s,y*s,z*s);}
        public double dot(V b){return x*b.x+y*b.y+z*b.z;}
        public double tamanho(){return Math.sqrt(x*x+y*y+z*z);}
        public V normal(){double n=tamanho();return n<1e-8?new V(0,0,-1):escala(1/n);}
    }
    public record Quadro(V camera,V alvo,double fov){}
    private static final double[][] CHAVES={
        {0,2.6,.4,3.6,.90},{20,3.6,1.5,-4,.83},{35,8,3,-8,.95},
        {55,-11,5,-11,1.02},{80,13,5,-14,.90},{87,13,5,-14,.80},{90,17,8,-18,1.22},
        {100,20,10,-20,1.12},{110,12,5,-16,1}
    };
    private static double[] chave(double tick){
        if(tick<=0)return CHAVES[0].clone();
        for(int i=1;i<CHAVES.length;i++)if(tick<=CHAVES[i][0]){
            var a=CHAVES[i-1];var b=CHAVES[i];double u=PerfilVfxGojo.suave((tick-a[0])/(b[0]-a[0]));
            return new double[]{tick,a[1]+(b[1]-a[1])*u,a[2]+(b[2]-a[2])*u,a[3]+(b[3]-a[3])*u,a[4]+(b[4]-a[4])*u};
        }
        return CHAVES[CHAVES.length-1].clone();
    }
    public static Quadro quadro(double tick,V olho,V dominio){
        var k=chave(tick);double transicao=PerfilVfxGojo.suave((tick-8)/28);
        V pos=olho.escala(1-transicao).add(new V(k[1],k[2],k[3]));
        double minimo=raio(tick)+1.2;
        if(pos.tamanho()<minimo)pos=pos.normal().escala(minimo);
        V relativo=pos.sub(dominio);
        if(relativo.tamanho()>24.8)pos=dominio.add(relativo.normal().escala(24.8));
        if(pos.tamanho()<minimo){
            V dir=pos.normal();double d=dominio.tamanho();
            if(d>1e-8){
                V eixo=dominio.normal();double cos=dir.dot(eixo);
                double limite=Math.max(-1,Math.min(1,(minimo*minimo+d*d-24.8*24.8)/(2*minimo*d)));
                if(cos<limite){
                    V tangente=dir.sub(eixo.escala(cos));
                    if(tangente.tamanho()<1e-8)tangente=Math.abs(eixo.y())<.99?new V(eixo.z(),0,-eixo.x()):new V(1,0,0);
                    dir=eixo.escala(limite).add(tangente.normal().escala(Math.sqrt(1-limite*limite)));
                }
            }
            pos=dir.escala(minimo);
        }
        V alvo=olho.escala(1-PerfilVfxGojo.suave((tick-8)/24));
        return new Quadro(pos,alvo,k[4]);
    }
    public static double fov(double tick){return chave(tick)[4];}
    public static double raio(double tick){return 1+Math.max(0,tick)*.1;}
    public static double alpha(double tick){return Math.max(0,Math.min(1,Math.min(tick/3,(DURACAO-tick)/6)));}
    public static double impacto(double tick){return tick<COLAPSO?0:1.15*Math.max(0,1-(tick-COLAPSO)/14);}
    public static double raioChoque(double tick){return tick<COLAPSO?0:raio(tick)+20*PerfilVfxGojo.suave((tick-COLAPSO)/12);}
    public static double alphaChoque(double tick){return tick<COLAPSO?0:1-PerfilVfxGojo.suave((tick-COLAPSO)/20);}
    public static double pesoCinema(double tick){return PerfilVfxGojo.suave((tick-44)/8)*PerfilVfxGojo.suave((DURACAO-tick)/8);}
    public static double raioExplosao(double tick){return tick<COLAPSO?0:raio(tick)+12*PerfilVfxGojo.suave((tick-COLAPSO)/10);}
    public static double alphaExplosao(double tick){return tick<COLAPSO?0:.9*(1-PerfilVfxGojo.suave((tick-COLAPSO)/20));}
    public static V anel(int plano,double s,double tick){
        double a=s*Math.PI*2,r=raioChoque(tick);
        return plano==0?new V(Math.cos(a)*r,Math.sin(a*3)*.2,Math.sin(a)*r):plano==1?new V(Math.cos(a)*r,Math.sin(a)*r,Math.sin(a*3)*.2):new V(Math.sin(a*3)*.2,Math.cos(a)*r,Math.sin(a)*r);
    }
}
