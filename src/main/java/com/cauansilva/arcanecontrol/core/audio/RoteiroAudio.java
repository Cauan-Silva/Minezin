package com.cauansilva.arcanecontrol.core.audio;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
public final class RoteiroAudio {
    public record Cue(String nome,int atraso,int vida,float volume,boolean seguir){}
    public static List<Cue> atomic(){return List.of(new Cue("atomic_charge",0,RelogioAtomic.IMPACTO,.75f,false),new Cue("atomic_release",RelogioAtomic.IMPACTO,360,.9f,false));}
    public static List<Cue> roxoMaximo(){return List.of(new Cue("purple_maximum_charge",0,90,.65f,false),new Cue("purple_maximum_release",RoxoMaximoGojo.COLAPSO,48,.95f,false));}
    public static List<Cue> divider(){return List.of(new Cue("divider_charge",0,18,.65f,true),new Cue("divider_release",18,24,.85f,true));}
    // Formation in hand and launch until ABERTURA, pull with tension until COLAPSO, burst and sparkles until FIM.
    public static List<Cue> singularidade(){return List.of(new Cue("frieren_buraco_carga",0,PerfilFrieren.ABERTURA,.7f,false),
            new Cue("frieren_buraco_atracao",PerfilFrieren.ABERTURA,PerfilFrieren.COLAPSO-PerfilFrieren.ABERTURA,.75f,false),
            new Cue("frieren_buraco_colapso",PerfilFrieren.COLAPSO,PerfilFrieren.FIM-PerfilFrieren.COLAPSO,.9f,false));}
    /** Crackle while the warning ring closes, thunder on the server's damage tick. */
    public static List<Cue> julgamento(){return List.of(new Cue("frieren_julgamento_carga",0,FormaFrieren.QUEDA,.6f,false),new Cue("frieren_julgamento_trovao",FormaFrieren.QUEDA,40,.9f,false));}
    public static List<Cue> imediato(String nome,int vida){return List.of(new Cue(nome,0,vida,.7f,false));}
    /** Asta (tools/build_asta_sfx.py): each skill's moments on its VFX ticks. 300-303 base Z/X/C/V, 304-307 Devil
     *  Union Z/X/C/V, 308 deflection, 200 the awakening. Null when the type has no sound of its own. */
    public static List<Cue> asta(int tipo){
        return switch(tipo){
            case 300->List.of(new Cue("asta_brandir",0,10,.8f,true),new Cue("asta_impacto",PerfilAsta.impacto(0,false),18,.9f,false));
            case 301->List.of(new Cue("asta_brandir",0,10,.8f,true),new Cue("asta_corte",PerfilAsta.impacto(1,false),20,.9f,false));
            case 302->List.of(new Cue("asta_investida",0,12,.85f,true),new Cue("asta_impacto",PerfilAsta.impacto(2,false),18,.9f,false));
            case 303->List.of(new Cue("asta_barreira",0,PerfilAsta.BARREIRA,.7f,true));
            case 304->List.of(new Cue("asta_salto",0,BlackMeteoriteAsta.IMPACTO,.85f,true),new Cue("asta_meteoro",BlackMeteoriteAsta.IMPACTO,36,1f,false));
            case 305->List.of(new Cue("asta_furacao",0,BlackHurricaneAsta.ESTOURO,.85f,true),new Cue("asta_estouro",BlackHurricaneAsta.ESTOURO,28,1f,false));
            case 306->divider();
            case 307->ultAsta();
            case 308->List.of(new Cue("asta_reflexo",0,10,.85f,false));
            default->null;
        };
    }
    /** Ultimate: swords out, one slash on each cut's tick, then the final X. */
    public static List<Cue> ultAsta(){
        var l=new java.util.ArrayList<Cue>();l.add(new Cue("asta_ult_espadas",0,UltAsta.SAIDA_ESPADA+2,.85f,true));
        for(int j=0;j<UltAsta.CORTES;j++)l.add(new Cue("asta_ult_corte",UltAsta.tempoCorte(j),10,.9f,false));
        l.add(new Cue("asta_ult_final",UltAsta.FINAL,48,1f,false));return List.copyOf(l);
    }
    /** Devil Union: the heartbeat until the release on FORMA_IMPACTO, then the roar. */
    public static List<Cue> formaAsta(){return List.of(new Cue("asta_forma_carga",0,PerfilCinematico.FORMA_IMPACTO,.85f,true),new Cue("asta_forma_rugido",PerfilCinematico.FORMA_IMPACTO,44,1f,false));}
}
