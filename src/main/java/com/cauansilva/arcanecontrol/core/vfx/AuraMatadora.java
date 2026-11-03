package com.cauansilva.arcanecontrol.core.vfx;

/**
 * Aura of the Demon-Slayer Sword in the Devil Union (user's screenshot): black flame tongues with a reddish rim
 * rising off both edges and the tip of the blade, each one growing, licking toward the tip and dying, on a loop.
 * Coordinates in the sword model's pixels (16 = one block): x across the blade, y along it from the grip
 * (blade from BASE to PONTA, same outline as build_asta_assets.espada()), z through it.
 */
public final class AuraMatadora {
    public record P(double x,double y,double z){}
    public static final int LINGUAS=22,PONTOS=8;
    public static final double BASE=6.6,RETA=30.2,PONTA=40.0,MEIA_LARGURA=4.5,CICLO=26;

    private static double s(double t){return PerfilVfxGojo.suave(t);}
    private static double hash(double n){double x=Math.sin(n*127.1+311.7)*43758.5453;return x-Math.floor(x);}

    /** Half-width of the blade at height y: straight up to RETA, then the stepped tip narrows to a point. */
    public static double meiaLargura(double y){return y<=RETA?MEIA_LARGURA:MEIA_LARGURA*Math.max(.1,(PONTA-y)/(PONTA-RETA));}
    /** Where tongue k sits along the blade (0 = guard, 1 = tip) and on which edge. */
    public static double posicao(int k){return Math.min(.97,(k/2+hash(k)*.8)/(LINGUAS/2.0));}
    public static int lado(int k){return k%2==0?-1:1;}
    /** Life of tongue k at tick t, 0..1, looping every CICLO ticks with its own offset. */
    public static double vida(int k,double t){double f=(t/CICLO+hash(k+7))%1.0;return f<0?f+1:f;}
    /** Length in pixels: grows over the first 40% of the life, then shrinks while it detaches. */
    public static double comprimento(int k,double t){double v=vida(k,t);return (3.5+5.5*hash(k+3))*(v<.4?s(v/.4):1-s((v-.4)/.6)*.75);}
    /** Point s (0 = on the edge, 1 = tip of the flame) of tongue k, licking outward and toward the sword tip. */
    public static P lingua(int k,double s,double t){
        double y0=BASE+posicao(k)*(PONTA-BASE),l=comprimento(k,t),v=vida(k,t),solta=v>.55?(v-.55)*6:0;
        double onda=Math.sin(s*4.5-t*.55+k*1.3)*1.1*s,lad=lado(k);
        double x=lad*(meiaLargura(y0)+s*l*.75+solta*.6)+onda*.4,y=y0+s*l*.95+solta+onda*.5;
        return new P(x,y,Math.sin(s*3+k)*.5*s);
    }
    public static double largura(int k,double s,double t){return (1.0+.6*hash(k+11))*Math.pow(1-s,.7)*Math.min(1,comprimento(k,t)/3);}
}
