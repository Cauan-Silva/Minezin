package com.cauansilva.arcanecontrol.core.vfx;

/** Local visual geometry for the domain; no changes to its radius or lifetime. */
public final class VazioInfinitoGojo {
    public static final int FAIXAS=64,PASSOS=14,FAIXAS_REDUZIDAS=24,PASSOS_REDUZIDOS=8;
    public record Ponto(double x,double y,double z){}
    private static final int[] PALETA={0x0EE7FF,0x2454FF,0x8230FF,0xE920FF,0xFF238E,0x7542FF};
    public static int cor(int faixa,double s,double tick){
        double fase=Math.floorMod(faixa,PALETA.length)+s*.85+.1*Math.sin(tick*.035+faixa);
        int base=(int)Math.floor(fase);double mistura=fase-base;
        int a=PALETA[Math.floorMod(base,PALETA.length)],b=PALETA[Math.floorMod(base+1,PALETA.length)],resultado=0;
        for(int shift=16;shift>=0;shift-=8){
            int canal=(int)Math.round(((a>>shift)&255)*(1-mistura)+((b>>shift)&255)*mistura);
            resultado|=canal<<shift;
        }
        return resultado;
    }
    public static boolean interior(double distancia,double raio){return raio>.1&&distancia<raio-.5;}
    public static Ponto informacao(int faixa,double s,double tick){
        double a=faixa*2.399963+tick*.003;
        double r=17+(faixa%5)*1.1;
        double inicio=.3+(faixa%7)*1.5;
        return new Ponto(Math.cos(a)*r*(1-s),inicio+(8-inicio)*s+Math.sin(s*Math.PI),
                Math.sin(a)*r*(1-s)+22*s);
    }
    public static double pulso(int faixa,double s,double tick){
        double onda=.5+.5*Math.cos((s*2-tick*.025-faixa*.13)*Math.PI*2);
        double abertura=.2+.8*(1-PerfilVfxGojo.suave((tick-36)/32));
        return (.18+.82*Math.pow(onda,12))*Math.sin(s*Math.PI)*abertura;
    }
}
