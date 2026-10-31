package com.cauansilva.arcanecontrol.core.poderes;

import com.cauansilva.arcanecontrol.core.rede.*;
import com.cauansilva.arcanecontrol.core.progressao.ProgressaoGojo;
import com.cauansilva.arcanecontrol.core.magia.GerenciadorMagiasGojo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class ProgressaoPoderes {
    private static final String CHAVE="minezin_poderes";
    public static CompoundTag tag(ServerPlayer p){
        CompoundTag raiz=p.getPersistentData();
        if(!raiz.contains(Player.PERSISTED_NBT_TAG))raiz.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        CompoundTag persistido=raiz.getCompound(Player.PERSISTED_NBT_TAG);
        if(!persistido.contains(CHAVE)){
            var inicial=new EstadoPoderes();
            if(persistido.contains("cid")&&!persistido.getCompound("cid").isEmpty()
                    ||p.getInventory().items.stream().anyMatch(s->s.is(com.cauansilva.arcanecontrol.core.cid.EspadaCid.ESPADA.get())))inicial.migrarCid();
            var t=new CompoundTag();escrever(t,inicial);persistido.put(CHAVE,t);
        }
        return persistido.getCompound(CHAVE);
    }
    public static EstadoPoderes ler(ServerPlayer p){
        var t=tag(p);return new EstadoPoderes(t.getInt("ativo"),t.getInt("kits"),t.getInt("conhecimento"),t.getIntArray("xp"),t.getIntArray("usos"));
    }
    private static void escrever(CompoundTag t,EstadoPoderes e){
        t.putInt("versao",1);t.putInt("ativo",e.ativo().id);t.putInt("kits",e.desbloqueados());
        t.putInt("conhecimento",e.conhecimento());t.putIntArray("xp",e.xps());t.putIntArray("usos",e.usos());
    }
    public static long agora(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean ocupado(ServerPlayer p){return tag(p).getLong("ocupado")>agora(p)
            ||com.cauansilva.arcanecontrol.core.cid.PoderesCid.dados(p).getLong("cid_ocupado")>agora(p);}
    public static void bloquear(ServerPlayer p,int ticks){tag(p).putLong("ocupado",Math.max(tag(p).getLong("ocupado"),agora(p)+ticks));}
    public static boolean podeUsar(ServerPlayer p,int tecnica){
        PoderAnime kit=PoderAnime.daTecnica(tecnica);
        if(kit==null||!p.isAlive()||p.isSpectator())return false;
        int slot=tecnica-(kit.id-1)*10-1;
        if(!ler(p).podeUsar(kit,slot)){
            p.displayClientMessage(Component.literal("Selecione um kit desbloqueado em P. Técnicas avançadas exigem nível no kit."),true);return false;
        }
        return true;
    }
    public static int recarga(ServerPlayer p,PoderAnime kit,int slot,int base){return ler(p).recarga(kit,slot,base);}
    public static void registrarUso(ServerPlayer p,PoderAnime kit,int slot,int duracao){
        var e=ler(p);e.registrarUso(kit,slot);escrever(tag(p),e);bloquear(p,duracao);sincronizar(p);
    }
    public static void sincronizar(ServerPlayer p){
        var e=ler(p);int[] xp=e.xps();xp[0]=ProgressaoGojo.ler(p).xp();
        RegistoRede.ENVIAR.send(PacketDistributor.PLAYER.with(()->p),
                new PacoteEstadoPoderes(e.ativo().id,e.desbloqueados(),e.conhecimento(),xp,e.usos(),p.isCreative()));
    }
    public static void acao(ServerPlayer p,int acao,int id){
        if(!p.isAlive()||p.isSpectator())return;
        long now=agora(p),ultimo=p.getPersistentData().getLong("minezin_menu_tick");
        if(now-ultimo<5 && ultimo>0)return;
        p.getPersistentData().putLong("minezin_menu_tick",now);
        if(acao==0){sincronizar(p);return;}
        PoderAnime kit=PoderAnime.porId(id);if(kit==null)return;
        var e=ler(p);
        if(acao==1){
            if(ocupado(p)||p.hurtTime>0){mensagem(p,"Aguarde a técnica terminar antes de trocar de kit.");return;}
            if(!e.selecionar(kit)){mensagem(p,"Esse kit ainda está bloqueado.");return;}
            GerenciadorMagiasGojo.desativarInfinito(p);
            escrever(tag(p),e);sincronizar(p);return;
        }
        if(acao!=2||e.tem(kit)){sincronizar(p);return;}
        Item recurso=kit==PoderAnime.CID?Items.AMETHYST_SHARD:Items.LAPIS_LAZULI;
        Item secundario=kit==PoderAnime.CID?Items.OBSIDIAN:Items.AMETHYST_SHARD;
        if(!p.isCreative()&&(!e.podeDesbloquear(kit)||contar(p,recurso)<8||contar(p,secundario)<4)){
            mensagem(p,"Faltam conhecimento ou materiais para pesquisar esse kit.");return;
        }
        if(e.desbloquear(kit,p.isCreative())){
            if(!p.isCreative()){consumir(p,recurso,8);consumir(p,secundario,4);}
            escrever(tag(p),e);sincronizar(p);
        }
    }
    private static int contar(ServerPlayer p,Item item){return p.getInventory().items.stream().filter(s->s.is(item)).mapToInt(s->s.getCount()).sum();}
    private static void consumir(ServerPlayer p,Item item,int quantidade){
        for(var s:p.getInventory().items)if(s.is(item)){int n=Math.min(s.getCount(),quantidade);s.shrink(n);quantidade-=n;if(quantidade==0)break;}
        p.getInventory().setChanged();
    }
    private static void mensagem(ServerPlayer p,String texto){p.displayClientMessage(Component.literal(texto),true);sincronizar(p);}
    @SubscribeEvent public static void entrar(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sincronizar(p);}
    @SubscribeEvent public static void renascer(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)sincronizar(p);}
    @SubscribeEvent public static void dimensao(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){
        tag(p).putLong("ocupado",0);tag(p).putLong("fri_barreira_ate",0);
        com.cauansilva.arcanecontrol.core.cid.PoderesCid.dados(p).putLong("cid_ocupado",0);sincronizar(p);
    }}
    @SubscribeEvent public static void clonar(PlayerEvent.Clone e){
        if(e.getEntity() instanceof ServerPlayer destino&&e.getOriginal() instanceof ServerPlayer origem){
            var copia=tag(origem).copy();copia.putLong("ocupado",0);copia.putLong("fri_barreira_ate",0);tag(destino).merge(copia);
        }
    }
    @SubscribeEvent public static void derrotar(LivingDeathEvent event){
        if(event.getEntity() instanceof Enemy&&event.getSource().getEntity() instanceof ServerPlayer p){
            var e=ler(p);e.ganharConhecimento(10);
            if(e.ativo()!=PoderAnime.GOJO)e.ganharXp(e.ativo(),25);
            escrever(tag(p),e);sincronizar(p);
        }
    }
}
