package com.cauansilva.arcanecontrol.core.rpg;
import net.minecraft.nbt.CompoundTag;

/** Schema persistente separado dos eventos para round-trip real de NBT nos testes. */
public final class PerfilRpgNbt {
    private static String chave(int kit){if(PersonagemRpg.porId(kit)==null)throw new IllegalArgumentException("Kit invalido");return "kit_"+kit;}
    public static MestriaRpg ler(CompoundTag raiz,int kit){var t=raiz.getCompound(chave(kit));return new MestriaRpg(t.getInt("xp"),t.getInt("nos"),t.getBoolean("despertar"),t.getInt("asc"),t.getBoolean("desp_mem"));}
    public static void salvar(CompoundTag raiz,int kit,MestriaRpg m){
        var t=new CompoundTag();t.putInt("xp",m.xp());t.putInt("nos",m.nos());t.putBoolean("despertar",m.despertarLiberado());if(m.ascensoes()>0)t.putInt("asc",m.ascensoes());if(m.memoriaDespertar())t.putBoolean("desp_mem",true);raiz.put(chave(kit),t);
    }
    public static RecursoMana mana(CompoundTag raiz){return new RecursoMana(raiz.getDouble("mana"));}
    public static RecargasRpg recargas(CompoundTag raiz){return new RecargasRpg(raiz.getLongArray("prazos"),raiz.getIntArray("duracoes"));}
    public static void salvarRecargas(CompoundTag raiz,RecargasRpg recargas){raiz.putLongArray("prazos",recargas.prazos());raiz.putIntArray("duracoes",recargas.duracoes());}
}
