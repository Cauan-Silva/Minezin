package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Hollow kit of Ichigo's Bankai (wave 5): mask formation (effect 522, first FORMA ticks; the mask is a damage buff only,
 * the Cero was dropped by the user) and the chained Shunpo window. Pure geometry; local frame x lateral, y up, z forward.
 */
public final class CeroIchigo {
    public record P(double x,double y,double z){}
    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    // ---------- Shunpo em sequencia (X no Bankai) ----------
    public static final int SHUNPOS=3,JANELA_SHUNPO=30;

    // ---------- Formacao da mascara ----------
    public static final int FORMA=14,ESTILHACOS=22;
    /** Estilhaco k de reiatsu branco convergindo para o rosto (origem nos olhos) e encaixando na mascara. */
    public static P estilhaco(int k,double t){
        double a=k*2.39996,el=(hash(k)-.5)*2.4,u=s(t/(FORMA-2.0)),r=1.6*(1-u)+.28,gira=(1-u)*2.2*(hash(k+1)-.5);
        return new P(Math.cos(a+gira)*Math.cos(el)*r,Math.sin(el)*r*.8,.15+Math.sin(a+gira)*Math.cos(el)*r*(1-u*.7));
    }
    public static double tamanhoEstilhaco(int k,double t){return t>=FORMA?0:(.06+.05*hash(k+2))*(1-s((t-FORMA+3)/3.0));}
    /** Rachadura vermelha que corre pela mascara assim que ela fecha. */
    public static double rachadura(double t){return t<FORMA-4?0:s((t-FORMA+4)/4.0)*(1-s((t-FORMA-6)/6.0));}

}
