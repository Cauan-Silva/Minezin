package com.cauansilva.arcanecontrol.core.audio;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.*;
public final class RoteiroAudio {
    public record Cue(String nome,int atraso,int vida,float volume,boolean seguir){}
    public static List<Cue> atomic(){return List.of(new Cue("atomic_charge",0,420,.75f,false),new Cue("atomic_release",RelogioAtomic.IMPACTO,360,.9f,false));}
    public static List<Cue> roxoMaximo(){return List.of(new Cue("purple_maximum_charge",0,90,.65f,false),new Cue("purple_maximum_release",RoxoMaximoGojo.COLAPSO,48,.95f,false));}
    public static List<Cue> divider(){return List.of(new Cue("divider_charge",0,18,.65f,true),new Cue("divider_release",18,24,.85f,true));}
    // Formation in hand and launch until ABERTURA, pull with tension until COLAPSO, burst and sparkles until FIM.
    public static List<Cue> singularidade(){return List.of(new Cue("frieren_buraco_carga",0,PerfilFrieren.ABERTURA,.7f,false),
            new Cue("frieren_buraco_atracao",PerfilFrieren.ABERTURA,PerfilFrieren.COLAPSO-PerfilFrieren.ABERTURA,.75f,false),
            new Cue("frieren_buraco_colapso",PerfilFrieren.COLAPSO,PerfilFrieren.FIM-PerfilFrieren.COLAPSO,.9f,false));}
    /** Crackle while the warning ring closes, thunder on the server's damage tick. */
    public static List<Cue> julgamento(){return List.of(new Cue("frieren_julgamento_carga",0,FormaFrieren.QUEDA,.6f,false),new Cue("frieren_julgamento_trovao",FormaFrieren.QUEDA,40,.9f,false));}
    public static List<Cue> imediato(String nome,int vida){return List.of(new Cue(nome,0,vida,.7f,false));}
}
