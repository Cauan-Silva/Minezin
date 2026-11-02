package com.cauansilva.arcanecontrol.core.vfx;
/** Tick anchors from MovesetsServidor / PerfilAsta, not arbitrary camera timings. */
public final class RoteiroUltimate {
    public record Cue(int tick,int cor,double forca,double raio,String som){}
    public static Cue para(int kit,int tipo,int duracao){
        return switch(tipo){
            case 32->kit==1?new Cue(90,0xAD71FF,1,30,"purple_release"):null;
            case 33->kit==2?new Cue(9,0xBE72FF,.85,20,"divider_release"):null;
            case 34->kit==2&&duracao>10?new Cue(24,0xB66BFF,.9,20,"divider_release"):null;
            case 35->kit==2?new Cue(0,0x8C38DA,.86,24,"singularity_charge"):null;
            case 38->kit==3?new Cue(25,0xFFF0C6,.95,24,"frieren_cast"):null;
            case 39->kit==3?new Cue(0,0xFFE5A0,.82,20,"frieren_barrier"):null;
            case 304->kit==4?new Cue(12,0xD42B4C,.88,20,"divider_release"):null;
            case 305->kit==4?new Cue(10,0xD42B4C,.88,20,"divider_release"):null;
            case 306->kit==4?new Cue(18,0xD42B4C,1,24,null):null;
            default->null;
        };
    }
    private RoteiroUltimate(){}
}
