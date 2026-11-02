package com.cauansilva.arcanecontrol.core.vfx;
/** Canonical visual phases. RelogioAtomic maps the approved long server clock to these landmarks. */
public final class RoteiroCinemaAtomic {
    public enum Fase{INATIVA,CARGA,CAMPO,TITULO,SILENCIO,CLARAO,DESCARGA,RESCALDO}
    public static Fase fase(double t,boolean frames){
        if(!Double.isFinite(t)||t<0||t>=PerfilAtomic.FIM)return Fase.INATIVA;
        if(t<30)return Fase.CARGA;if(t<78)return Fase.CAMPO;if(t<88)return Fase.TITULO;if(t<90)return Fase.SILENCIO;
        if(t<91.25&&frames)return Fase.CLARAO;return t<130?Fase.DESCARGA:Fase.RESCALDO;
    }
    public static double peso(double t,double distancia,boolean local){
        if(!Double.isFinite(t)||!Double.isFinite(distancia)||t<0||t>=240||distancia<0)return 0;
        double entrada=suave(t/12),saida=1-suave((t-170)/70);
        double alcance=local?1:1-suave((distancia-20)/60);
        return entrada*saida*alcance;
    }
    public static double suave(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    private RoteiroCinemaAtomic(){}
}
