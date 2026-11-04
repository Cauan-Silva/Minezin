package com.cauansilva.arcanecontrol.core.vfx;
/** Balance 3.11 (user: the ult took too long): 9 s until release, 27 s total; the release keeps its length.
 *  Visual landmark clock remains 0..240. */
public final class RelogioAtomic {
    public static final int IMPACTO=180,FIM=540,OCUPADO=204;
    /** User: the ult takes a while, so it hits many times: a burst at the impact, then a hit every TICK_DANO ticks
     *  while the pillar stands (until PILAR_FIM, the start of its fade), for anyone inside the pillar's radius. */
    public static final int PILAR_FIM=460,TICK_DANO=10;public static final float DANO_IMPACTO=20,DANO_TICK=2;
    public static boolean tickDeDano(long t){return t>IMPACTO&&t<=PILAR_FIM&&(t-IMPACTO)%TICK_DANO==0;}
    /** Total damage for a target that stays inside the pillar the whole time. */
    public static float danoTotal(){return DANO_IMPACTO+DANO_TICK*((PILAR_FIM-IMPACTO)/TICK_DANO);}
    private static final double[] REAL={0,51,86,137,163,180,186,460,540};
    private static final double[] VISUAL={0,30,50,78,88,90,96,170,240};
    private static double map(double t,double[] from,double[] to){
        if(!Double.isFinite(t))return -1;if(t<0)return t;if(t>=from[from.length-1])return to[to.length-1]+t-from[from.length-1];
        for(int i=1;i<from.length;i++)if(t<=from[i])return to[i-1]+(to[i]-to[i-1])*(t-from[i-1])/(from[i]-from[i-1]);return 0;
    }
    public static double visual(double ticks){return map(ticks,REAL,VISUAL);}
    public static double real(double ticks){return map(ticks,VISUAL,REAL);}
    public static String letreiro(double t){return t>=78&&t<86?"I AM":t>=86&&t<88?"ATOMIC":"";}
    public static boolean detonar(long anterior,long atual){return anterior<IMPACTO&&atual>=IMPACTO;}
    private RelogioAtomic(){}
}
