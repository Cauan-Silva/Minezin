package com.cauansilva.arcanecontrol.core.vfx;
/** Balance 3.11 (user: the ult took too long): 9 s until release, 27 s total; the release keeps its length.
 *  Visual landmark clock remains 0..240. */
public final class RelogioAtomic {
    public static final int IMPACTO=180,FIM=540,OCUPADO=204;
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
