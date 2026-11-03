package com.cauansilva.arcanecontrol.core.asta;

import com.cauansilva.arcanecontrol.core.cid.RoteiroTecidoCid;

/**
 * Pure visual pushes on Asta's robe from his base strikes, in ticks since the effect (like RoteiroTecidoCid); no
 * movement, damage or network change. Units are the cloth input (blocks per tick of apparent motion, radians of turn).
 * Each push is timed on that strike (PerfilAsta.impacto(slot,false)): the cuts peak on the hit, the dash during it.
 */
public final class RoteiroTecidoAsta {
    private static double pulso(double t,double centro,double largura){double u=Math.abs(t-centro)/largura;return u>=1?0:Math.pow(1-u*u,2);}
    public static RoteiroTecidoCid.Impulso amostrar(int tipo,double t){
        if(!Double.isFinite(t)||t<0)return new RoteiroTecidoCid.Impulso(0,0,0);
        return switch(tipo){
            // Z: wide horizontal fan, the robe swings round with the cut.
            case 300 -> new RoteiroTecidoCid.Impulso(.06*pulso(t,6,5),.10*pulso(t,6,5),.20*pulso(t,6,5));
            // X: overhead cut, the robe whips back on the hit and settles forward.
            case 301 -> new RoteiroTecidoCid.Impulso(.26*pulso(t,8,4)-.07*pulso(t,14,6),0,.04*pulso(t,8,4));
            // C: forward dash, the robe streams straight back until the hit.
            case 302 -> new RoteiroTecidoCid.Impulso(.30*pulso(t,6,7),0,0);
            // V: anti-magic guard, a small lift then a steady flutter while it holds.
            case 303 -> new RoteiroTecidoCid.Impulso(.06*pulso(t,3,4)+.02*pulso(t,50,55),.03*Math.sin(t*.45)*pulso(t,50,55),0);
            default -> new RoteiroTecidoCid.Impulso(0,0,0);
        };
    }
    private RoteiroTecidoAsta(){}
}
