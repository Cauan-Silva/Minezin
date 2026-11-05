package com.cauansilva.arcanecontrol.core.audio;
import java.util.List;
import com.cauansilva.arcanecontrol.core.vfx.*;
import com.cauansilva.arcanecontrol.core.asta.PerfilAsta;
public final class RoteiroAudio {
    public record Cue(String nome,int atraso,int vida,float volume,boolean seguir){}
    /** Cid's Atomic (tools/build_cid_sfx.py): the 9 s charge with its heartbeat, then the release and pillar roar. */
    public static List<Cue> atomic(){return List.of(new Cue("cid_atomic_carga",0,RelogioAtomic.IMPACTO,.8f,false),new Cue("cid_atomic_explosao",RelogioAtomic.IMPACTO,RelogioAtomic.FIM-RelogioAtomic.IMPACTO,1f,false));}
    public static List<Cue> roxoMaximo(){return List.of(new Cue("purple_maximum_charge",0,90,.65f,false),new Cue("purple_maximum_release",RoxoMaximoGojo.COLAPSO,48,.95f,false));}
    public static List<Cue> divider(){return List.of(new Cue("divider_charge",0,18,.65f,true),new Cue("divider_release",18,24,.85f,true));}
    // Formation in hand and launch until ABERTURA, pull with tension until COLAPSO, burst and sparkles until FIM.
    public static List<Cue> singularidade(){return List.of(new Cue("frieren_buraco_carga",0,PerfilFrieren.ABERTURA,.7f,false),
            new Cue("frieren_buraco_atracao",PerfilFrieren.ABERTURA,PerfilFrieren.COLAPSO-PerfilFrieren.ABERTURA,.75f,false),
            new Cue("frieren_buraco_colapso",PerfilFrieren.COLAPSO,PerfilFrieren.FIM-PerfilFrieren.COLAPSO,.9f,false));}
    /** Crackle while the warning ring closes, thunder on the server's damage tick. */
    public static List<Cue> julgamento(){return List.of(new Cue("frieren_julgamento_carga",0,FormaFrieren.QUEDA,.6f,false),new Cue("frieren_julgamento_trovao",FormaFrieren.QUEDA,40,.9f,false));}
    public static List<Cue> imediato(String nome,int vida){return List.of(new Cue(nome,0,vida,.7f,false));}
    /** Subaru e Beatrice (tools/build_subaru_sfx.py, gravacoes CC0 + sintese): um roteiro por pacote 600-614 de
     *  PoderesSubaru; os acertos internos ja estao nos ticks das classes puras. Lista vazia quando o tipo nao tem som. */
    public static List<Cue> subaru(int tipo){return switch(tipo){
        case 600->List.of(new Cue("subaru_shamak_carga",0,com.cauansilva.arcanecontrol.core.vfx.SamakSubaru.CARGA+4,.75f,true),
                new Cue("subaru_shamak_disparo",com.cauansilva.arcanecontrol.core.vfx.SamakSubaru.CARGA,14,.8f,false));
        case 601->List.of(new Cue("subaru_shamak_nevoa",0,50,.75f,false));
        case 602->List.of(new Cue("subaru_escudo",0,22,.7f,true));
        case 603->List.of(new Cue("subaru_escudo_estouro",0,18,.8f,true));
        case 604->List.of(new Cue("subaru_instrucao",0,26,.75f,false));
        case 605->List.of(new Cue("subaru_recuperacao",0,com.cauansilva.arcanecontrol.core.vfx.RecuperacaoSubaru.FIM+2,.7f,true));
        case 610->List.of(new Cue("beatrice_minya",0,60,.8f,false));
        case 611->List.of(new Cue("beatrice_emm",0,56,.75f,true));
        case 612->List.of(new Cue("beatrice_emm_golpe",0,14,.8f,true));
        case 613->List.of(new Cue("beatrice_murak",0,28,.75f,true));
        case 614->List.of(new Cue("beatrice_ul_minya",0,com.cauansilva.arcanecontrol.core.vfx.UlMinyaBeatrice.FIM+2,.9f,false));
        default->List.of();};}
    public static List<Cue> subaruContrato(){return List.of(new Cue("subaru_contrato",0,com.cauansilva.arcanecontrol.core.vfx.ContratoSubaru.FIM+2,.85f,false));}
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
    /** Cid base kit (EfeitosCid types): 11 Slime Cut, 12 Shadow Step (16 = arrival), 13 Slime Threads, 17 awakening;
     *  awakened moveset (kit 2 effects): 31 hidden blade strike, 34 blade rain, 35 singularity. Null otherwise. */
    public static List<Cue> cid(int tipo){
        return switch(tipo){
            case 11->List.of(new Cue("cid_corte_ar",0,8,.8f,true),new Cue("cid_corte_impacto",PerfilCid.CORTE_IMPACTO,16,.9f,false));
            case 12->List.of(new Cue("cid_passo",0,10,.8f,false));
            case 16->List.of(new Cue("cid_passo_chegada",0,10,.85f,false));
            case 13->List.of(new Cue("cid_fios_lanca",0,10,.8f,true),new Cue("cid_fios_prende",PerfilCid.FIOS_IMPACTO,14,.85f,false),new Cue("cid_fios_dreno",PerfilCid.FIOS_IMPACTO+20,12,.8f,false));
            case 17->List.of(new Cue("cid_despertar",0,34,.9f,true));
            case 31->List.of(new Cue("cid_lamina",0,8,.8f,false));
            case 34->chuvaCid();
            case 35->List.of(new Cue("cid_singularidade",0,100,.85f,false));
            default->null;
        };
    }
    /** Blade rain (MovesetsServidor case 34): blades rise, then one falls every 4 ticks from tick 24 to the end (64). */
    public static List<Cue> chuvaCid(){var l=new java.util.ArrayList<Cue>();l.add(new Cue("cid_chuva",0,24,.85f,false));for(int t=24;t<64;t+=4)l.add(new Cue("cid_lamina_queda",t,11,.8f,false));return List.copyOf(l);}
    /** Ichigo (tools/build_ichigo_sfx.py): wind-ups on the cast packets (500+slot base, 510+slot Bankai, 512 Jujisho
     *  with the mask), releases on the packets the server sends on each impact tick. Null when it has no sound. */
    public static List<Cue> ichigo(int tipo){
        String n=switch(tipo){
            case 500,510->"ichigo_preparo";case 502,512->"ichigo_preparo_pesado";case 503,513->"ichigo_investida";case 501,511->"ichigo_shunpo";
            case 520->"ichigo_getsuga";case 524->"ichigo_getsuga_bankai";case 523->"ichigo_jujisho";case 521->"ichigo_acerto";
            case 525->"ichigo_golpe_pesado";case 526->"ichigo_corte_ascendente";case 527->"ichigo_lancamento";case 522->"ichigo_mascara";
            default->null;};
        if(n==null)return null;
        int vida=switch(tipo){case 520,524,521->30;case 523->36;case 525->32;case 522->28;case 527->16;default->14;};
        return List.of(new Cue(n,0,vida,.85f,tipo>=500&&tipo<=513));
    }
    /** Bankai release (EntradaBankai): pressure until the shout (GRITO), the coat reveal on REVELA. */
    public static List<Cue> bankai(){return List.of(new Cue("ichigo_bankai_pressao",0,EntradaBankai.GRITO,.8f,true),new Cue("ichigo_bankai_grito",EntradaBankai.GRITO,EntradaBankai.REVELA-EntradaBankai.GRITO+1,1f,false),new Cue("ichigo_bankai_revela",EntradaBankai.REVELA,32,.9f,false));}
    /** Mugetsu: darkness gathers until the cut (GOLPE), then the line and the boom. */
    public static List<Cue> mugetsu(){return List.of(new Cue("ichigo_mugetsu_forma",0,MugetsuIchigo.GOLPE,.85f,false),new Cue("ichigo_mugetsu_corte",MugetsuIchigo.GOLPE,80,1f,false));}
    /** Devil Union: the heartbeat until the release on FORMA_IMPACTO, then the roar. */
    public static List<Cue> formaAsta(){return List.of(new Cue("asta_forma_carga",0,PerfilCinematico.FORMA_IMPACTO,.85f,true),new Cue("asta_forma_rugido",PerfilCinematico.FORMA_IMPACTO,44,1f,false));}
}
