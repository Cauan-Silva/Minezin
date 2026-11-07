package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/**
 * Reliquias no servidor: Bolsa (NBT do jogador), bonus no combate, sets, Soulbound e o bau do chefe do Vazio
 * (loot individual por jogador, pity de Mitico na 11a abertura).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class ReliquiasServidor {
    private record Bonus(Map<Afixo,Double> v,Set<Integer> sets){double get(Afixo a){return v.getOrDefault(a,0.0);}}
    private static final Map<UUID,Bonus> CACHE=new HashMap<>();
    private static final UUID VIDA=UUID.fromString("7b6c3c1e-2a51-4c0e-9a3f-5d1e0f2a7c11"),VELOCIDADE=UUID.fromString("0d4f8e2a-9b13-4f6a-8c55-6e2b1a9d3f22");
    private static final Random RNG=new Random();

    // ---------------- Bolsa ----------------
    private static ListTag lista(ServerPlayer p){return RpgServidor.dados(p).getList("rel_bolsa",Tag.TAG_COMPOUND);}
    static SimpleContainer carregar(ServerPlayer p){
        var c=new SimpleContainer(MenuBolsa.SLOTS);var l=lista(p);
        for(int i=0;i<l.size()&&i<MenuBolsa.SLOTS;i++)c.setItem(i,ItemStack.of(l.getCompound(i)));
        return c;
    }
    public static void abrir(ServerPlayer p){
        var c=carregar(p);
        net.minecraftforge.network.NetworkHooks.openScreen(p,new SimpleMenuProvider((id,inv,pl)->new MenuBolsa(id,inv,c),Component.literal("Bolsa de Relíquias")));
    }
    static void fechou(ServerPlayer p,Container c){
        var l=new ListTag();
        for(int i=0;i<MenuBolsa.SLOTS;i++){var s=c.getItem(i);
            if(!s.isEmpty()&&!MenuBolsa.aceita(i,s)){if(!p.getInventory().add(s))p.drop(s,false);s=ItemStack.EMPTY;}
            l.add(s.save(new CompoundTag()));}
        RpgServidor.dados(p).put("rel_bolsa",l);recalcular(p);
    }
    private static Bonus bonus(ServerPlayer p){return CACHE.computeIfAbsent(p.getUUID(),k->calcular(p));}
    private static Bonus calcular(ServerPlayer p){
        var rel=new ArrayList<Reliquia>();var l=lista(p);
        for(int i=0;i<l.size()&&i<MenuBolsa.SLOTS;i++){var s=ItemStack.of(l.getCompound(i));if(MenuBolsa.aceita(i,s))rel.add(ItemReliquia.ler(s));}
        var v=new EnumMap<Afixo,Double>(Reliquias.somar(rel));var sets=Reliquias.sets(rel);
        for(int k:sets)Reliquias.bonusSet(k).forEach((a,x)->v.merge(a,x,Double::sum));
        return new Bonus(v,sets);
    }
    static void recalcular(ServerPlayer p){
        var antes=CACHE.remove(p.getUUID());var b=bonus(p);
        modificador(p.getAttribute(Attributes.MAX_HEALTH),VIDA,"minezin_reliquia_vida",b.get(Afixo.VIDA)*2,AttributeModifier.Operation.ADDITION);
        modificador(p.getAttribute(Attributes.MOVEMENT_SPEED),VELOCIDADE,"minezin_reliquia_velocidade",b.get(Afixo.VELOCIDADE)/100,AttributeModifier.Operation.MULTIPLY_BASE);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
        if(antes!=null)for(int k:b.sets)if(!antes.sets.contains(k))p.displayClientMessage(Component.literal(Reliquias.set(k)).withStyle(ChatFormatting.GREEN),true);
    }
    private static void modificador(AttributeInstance a,UUID id,String nome,double valor,AttributeModifier.Operation op){
        if(a==null)return;if(a.getModifier(id)!=null)a.removeModifier(id);if(valor!=0)a.addPermanentModifier(new AttributeModifier(id,nome,valor,op));
    }

    // ---------------- ganchos do RPG ----------------
    /** Recarga base depois do afixo RECARGA. */
    public static int recarga(ServerPlayer p,int base){return (int)Math.max(1,Math.round(base*(1-bonus(p).get(Afixo.RECARGA)/100)));}
    /** Multiplicador da regeneracao de mana. */
    public static double regeneracao(ServerPlayer p){return 1+bonus(p).get(Afixo.MANA)/100;}

    // ---------------- combate ----------------
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void dano(LivingHurtEvent e){
        if(e.getEntity().level().isClientSide)return;
        if(e.getSource().getEntity() instanceof ServerPlayer p&&p!=e.getEntity()){
            var b=bonus(p);float v=e.getAmount()*(float)(1+b.get(Afixo.DANO)/100);
            if(b.get(Afixo.CRITICO)>0&&RNG.nextDouble()*100<b.get(Afixo.CRITICO)){v*=1.5f;p.level().playSound(null,e.getEntity().blockPosition(),SoundEvents.PLAYER_ATTACK_CRIT,SoundSource.PLAYERS,.8f,1.2f);}
            e.setAmount(v);
            if(b.get(Afixo.ROUBO)>0)p.heal(v*(float)(b.get(Afixo.ROUBO)/100));
        }
        if(e.getEntity() instanceof ServerPlayer p){
            var b=bonus(p);double red=b.get(Afixo.RESISTENCIA);
            if(b.sets.contains(4)&&e.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO))red+=15;
            if(red>0)e.setAmount(e.getAmount()*(float)(1-Math.min(60,red)/100));
        }
    }
    /** Sets: Aurora imune a Wither; Raio Absoluto imune a lentidao. */
    @SubscribeEvent public static void efeito(MobEffectEvent.Applicable e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var s=bonus(p).sets;var ef=e.getEffectInstance().getEffect();
        if(s.contains(2)&&ef==MobEffects.WITHER||s.contains(12)&&ef==MobEffects.MOVEMENT_SLOWDOWN)e.setResult(Event.Result.DENY);
    }

    // ---------------- Soulbound ----------------
    @SubscribeEvent public static void jogar(ItemTossEvent e){
        if(!ItemReliquia.soulbound(e.getEntity().getItem())||e.getPlayer().isCreative())return;
        e.setCanceled(true);e.getPlayer().getInventory().add(e.getEntity().getItem());
        e.getPlayer().displayClientMessage(Component.literal("Relíquia Soulbound: não pode ser jogada.").withStyle(ChatFormatting.RED),true);
    }
    /** Ao morrer a Soulbound nao cai: guarda e devolve no renascimento. */
    @SubscribeEvent public static void morrer(LivingDropsEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var guardadas=new ListTag();
        e.getDrops().removeIf(d->{if(!ItemReliquia.soulbound(d.getItem()))return false;guardadas.add(d.getItem().save(new CompoundTag()));return true;});
        if(!guardadas.isEmpty())RpgServidor.dados(p).put("rel_alma",guardadas);
    }
    @SubscribeEvent public static void renascer(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var t=RpgServidor.dados(p);
        for(var x:t.getList("rel_alma",Tag.TAG_COMPOUND)){var s=ItemStack.of((CompoundTag)x);if(!p.getInventory().add(s))p.drop(s,false);}
        t.remove("rel_alma");recalcular(p);
    }
    @SubscribeEvent public static void entrar(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)recalcular(p);}
    @SubscribeEvent public static void sair(PlayerEvent.PlayerLoggedOutEvent e){CACHE.remove(e.getEntity().getUUID());}

    // ---------------- bau do chefe ----------------
    /** Loot individual de um jogador para o chefe `kit` (aplica e atualiza o pity). */
    public static List<ItemStack> sortear(ServerPlayer p,int kit){
        var t=RpgServidor.dados(p);int[] pt=Reliquias.pity(t.getInt("rel_pity"));
        var bau=Reliquias.bau(RNG,kit,pt[0]==1);boolean mitico=bau.stream().anyMatch(Reliquia::mitica);
        t.putInt("rel_pity",Reliquias.depois(pt[1],mitico));
        var out=new ArrayList<>(bau.stream().map(ItemReliquia::criar).toList());
        out.add(new ItemStack(RegistroReliquias.ESSENCIA.get(),Reliquias.essenciaBau(RNG)));return out;
    }
    /** Abre o bau so com o loot deste jogador; o que sobrar volta ao inventario quando ele fechar. */
    public static void abrirBau(ServerPlayer p,List<ItemStack> itens,String chefe,Runnable aoFechar){
        var c=new SimpleContainer(27);for(int i=0;i<itens.size()&&i<9;i++)c.setItem(12+i-itens.size()/2,itens.get(i));
        p.level().playSound(null,p.blockPosition(),SoundEvents.ENDER_CHEST_OPEN,SoundSource.BLOCKS,.8f,.8f);
        net.minecraftforge.network.NetworkHooks.openScreen(p,new SimpleMenuProvider((id,inv,pl)->new ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x3,id,inv,c,3){
            @Override public void removed(net.minecraft.world.entity.player.Player pl2){
                super.removed(pl2);for(int i=0;i<c.getContainerSize();i++){var s=c.removeItemNoUpdate(i);if(!s.isEmpty()&&!pl2.getInventory().add(s))pl2.drop(s,false);}
                aoFechar.run();}
        },Component.literal("Espólio: "+chefe)));
    }
    private ReliquiasServidor(){}
}
