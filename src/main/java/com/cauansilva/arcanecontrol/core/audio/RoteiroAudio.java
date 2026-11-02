package com.cauansilva.arcanecontrol.core.audio;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.*;
public final class RoteiroAudio {
    public record Cue(String nome,int atraso,int vida,float volume,boolean seguir){}
    public static List<Cue> atomic(){return List.of(new Cue("atomic_charge",0,90,.75f,false),new Cue("atomic_release",PerfilAtomic.IMPACTO,75,.9f,false));}
    public static List<Cue> divider(){return List.of(new Cue("divider_charge",0,18,.65f,true),new Cue("divider_release",18,24,.85f,true));}
    public static List<Cue> singularidade(){return List.of(new Cue("singularity_charge",0,70,.65f,false),new Cue("singularity_collapse",PerfilFrieren.COLAPSO,55,.85f,false));}
    public static List<Cue> imediato(String nome,int vida){return List.of(new Cue(nome,0,vida,.7f,false));}
}
