package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.*;

/**
 * Reliquia (anel, colar, amuleto, brinco). Tudo o que importa fica no NBT: tier, chefe de origem, afixos e Soulbound
 * (Miticas). Sem NBT (criativo, receita) o item e um "molde" sem bonus.
 */
public final class ItemReliquia extends Item {
    public final Tipo tipo;
    public ItemReliquia(Tipo tipo){super(new Item.Properties().stacksTo(1).fireResistant());this.tipo=tipo;}

    static final String TIER="rel_tier",KIT="rel_kit",AFIXOS="rel_afixos",ALMA="rel_soulbound";
    public static ItemStack criar(Reliquia r){
        var it=switch(r.tipo()){case ANEL->RegistroReliquias.ANEL.get();case COLAR->RegistroReliquias.COLAR.get();case AMULETO->RegistroReliquias.AMULETO.get();case BRINCO->RegistroReliquias.BRINCO.get();};
        var s=new ItemStack(it);var t=s.getOrCreateTag();t.putInt(TIER,r.tier());t.putInt(KIT,r.kit());
        var l=new ListTag();for(var a:r.afixos()){var c=new CompoundTag();c.putString("id",a.afixo().name());c.putDouble("v",a.valor());l.add(c);}
        t.put(AFIXOS,l);if(r.mitica())t.putBoolean(ALMA,true);
        s.setHoverName(Component.literal(r.nome()).withStyle(cor(r.tier())).withStyle(s2->s2.withItalic(false)));
        return s;
    }
    /** Le a reliquia do item (null se nao for uma reliquia gerada). */
    public static Reliquia ler(ItemStack s){
        if(!(s.getItem() instanceof ItemReliquia ir)||!s.hasTag()||!s.getTag().contains(TIER))return null;
        var t=s.getTag();var rol=new ArrayList<Rolagem>();
        for(var e:t.getList(AFIXOS,Tag.TAG_COMPOUND)){var c=(CompoundTag)e;try{rol.add(new Rolagem(Afixo.valueOf(c.getString("id")),c.getDouble("v")));}catch(IllegalArgumentException ignorado){}}
        if(rol.isEmpty())return null;
        return new Reliquia(ir.tipo,Math.max(1,Math.min(Reliquias.TIERS,t.getInt(TIER))),t.getInt(KIT),List.copyOf(rol));
    }
    public static boolean soulbound(ItemStack s){return s.hasTag()&&s.getTag().getBoolean(ALMA);}
    static ChatFormatting cor(int tier){return switch(tier){case 2->ChatFormatting.BLUE;case 3->ChatFormatting.DARK_PURPLE;case 4->ChatFormatting.GOLD;case 5->ChatFormatting.RED;default->ChatFormatting.WHITE;};}

    @Override public boolean isFoil(ItemStack s){var r=ler(s);return r!=null&&r.tier()>=4;}
    @Override public void appendHoverText(ItemStack s,Level l,List<Component> texto,TooltipFlag f){
        var r=ler(s);
        if(r==null){texto.add(Component.literal("Relíquia sem afixos (molde).").withStyle(ChatFormatting.GRAY));return;}
        texto.add(Component.literal(Reliquias.TIER_NOME[r.tier()]+" • "+r.tipo().nome).withStyle(cor(r.tier())));
        for(var a:r.afixos())texto.add(Component.literal(" "+a.afixo().formatar(a.valor())).withStyle(ChatFormatting.AQUA));
        var rpg=com.cauansilva.arcanecontrol.core.rpg.PersonagemRpg.porId(r.kit());
        texto.add(Component.literal("Origem: "+(rpg==null?"Vazio":rpg.nemesis)).withStyle(ChatFormatting.DARK_GRAY));
        texto.add(Component.literal(Reliquias.set(r.kit())+" ("+Reliquias.PECAS_SET+" peças; Mítica conta 2)").withStyle(ChatFormatting.DARK_GREEN));
        if(soulbound(s))texto.add(Component.literal("Soulbound: não pode ser jogada nem trocada").withStyle(ChatFormatting.RED));
        texto.add(Component.literal("Equipe na Bolsa de Relíquias [B]").withStyle(ChatFormatting.GRAY));
    }
}
