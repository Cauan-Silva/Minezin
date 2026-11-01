package com.cauansilva.arcanecontrol.core.client.render;
import com.cauansilva.arcanecontrol.core.rede.PacoteEfeitoDespertar;
import com.cauansilva.arcanecontrol.core.client.AudioAnime;
import com.cauansilva.arcanecontrol.core.audio.RoteiroAudio;
import com.cauansilva.arcanecontrol.core.vfx.RoteiroUltimate;
/** Presentation cues use accepted server effects; no attack timing or damage changes. */
public final class UltimatesTransformadas {
    public static void receber(PacoteEfeitoDespertar p){
        var c=RoteiroUltimate.para(p.kit(),p.tipo(),p.ticks());if(c==null)return;
        ImpactosAnime.programar(p.dono(),p.a(),c.tick(),c.cor(),c.forca(),c.raio(),p.kit());
        if(c.som()!=null)AudioAnime.programar(p.dono(),"ultimate_"+p.tipo(),p.a(),java.util.List.of(new RoteiroAudio.Cue(c.som(),c.tick(),32,.7f,false)));
    }
    private UltimatesTransformadas(){}
}
