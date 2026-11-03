package com.cauansilva.arcanecontrol.core.client.render;
import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AudioAnime;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.cauansilva.arcanecontrol.core.vfx.RoteiroUltimate;
/** Presentation cues use accepted server effects; no attack timing or damage changes. */
public final class UltimatesTransformadas {
    public static void receber(PacoteEfeitoDespertar p){
        if(p.kit()==3){
            // Frieren cues without impact frames: each of the twenty Zoltraak, mana release, wind cage and Judradjim.
            if(p.tipo()==113)AudioAnime.programar(p.dono(),"vinte_zoltraak",p.a(),java.util.List.of(new RoteiroAudio.Cue("frieren_cast",0,14,.4f,false)));
            if(p.tipo()==200&&p.ticks()==900)AudioAnime.programar(p.dono(),"liberar_mana",p.a(),RoteiroAudio.imediato("frieren_mana",60));
            if(p.tipo()==30)AudioAnime.programar(p.dono(),"gaiola",p.a(),RoteiroAudio.imediato("frieren_gaiola",p.ticks()));
            if(p.tipo()==38)AudioAnime.programar(p.dono(),"julgamento",p.a(),RoteiroAudio.julgamento());
        }
        var c=RoteiroUltimate.para(p.kit(),p.tipo(),p.ticks());if(c==null)return;
        if(p.kit()==1&&p.tipo()==32)GerenciadorCutscene3D.iniciarRoxoMaximo(p.dono(),p.a(),p.ticks());
        if(p.kit()==4&&p.tipo()==307)GerenciadorCutscene3D.iniciarUltAsta(p.dono(),p.a(),p.b(),p.ticks());
        ImpactosAnime.programar(p.dono(),p.a(),c.tick(),c.cor(),c.forca(),c.raio(),p.kit());
        if(c.som()!=null)AudioAnime.programar(p.dono(),"ultimate_"+p.tipo(),p.a(),p.kit()==1&&p.tipo()==32?RoteiroAudio.roxoMaximo():java.util.List.of(new RoteiroAudio.Cue(c.som(),c.tick(),32,.7f,false)));
    }
    private UltimatesTransformadas(){}
}
