package com.cauansilva.arcanecontrol.core.vazio;

import com.cauansilva.arcanecontrol.core.rpg.NemesisEntity;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import com.cauansilva.arcanecontrol.core.vazio.RegrasNemesis.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/**
 * Memoria do Nemesis por jogador e kit ("nem_<kit>_nv"/"nem_<kit>_tr" nos dados RPG do dono). Durante a luta o chefe
 * conta o dano corpo a corpo e o de magia/projetil que recebe; no wipe isso decide o traco aprendido.
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class NemesisMemoria {
    private static final String CORPO="mem_corpo",MAGIA="mem_magia",TRACOS="mem_tracos",NIVEL="mem_nivel",APANHOU="mem_apanhou";
    private static final UUID MOD_VEL=UUID.fromString("7b4a3f8e-2c1d-4e5f-9a6b-1c2d3e4f5a61");
    private static final Random RNG=new Random();

    private static String chave(int kit,String s){return "nem_"+kit+"_"+s;}
    public static Estado estado(ServerPlayer p,int kit){var d=RpgServidor.dados(p);return new Estado(d.getInt(chave(kit,"nv")),d.getInt(chave(kit,"tr")));}
    private static void salvar(ServerPlayer p,int kit,Estado e){var d=RpgServidor.dados(p);d.putInt(chave(kit,"nv"),e.nivel());d.putInt(chave(kit,"tr"),e.tracos());}

    /** Na entrada: aplica nivel e tracos ao chefe recem-preparado e provoca o grupo. */
    public static void aplicar(NemesisEntity chefe,ServerPlayer dono,int kit){
        var e=estado(dono,kit);var d=chefe.getPersistentData();d.putInt(NIVEL,e.nivel());d.putInt(TRACOS,e.tracos());
        if(e.nivel()<=0)return;
        var vida=chefe.getAttribute(Attributes.MAX_HEALTH);vida.setBaseValue(vida.getBaseValue()*RegrasNemesis.vida(e.nivel()));chefe.setHealth(chefe.getMaxHealth());
        if(Traco.CACADOR.em(e.tracos())){var v=chefe.getAttribute(Attributes.MOVEMENT_SPEED);if(v!=null)v.addPermanentModifier(new AttributeModifier(MOD_VEL,"nemesis_cacador",RegrasNemesis.CACADOR,AttributeModifier.Operation.MULTIPLY_TOTAL));}
        chefe.setCustomName(Component.literal(RegrasNemesis.titulo(chefe.getCustomName()==null?"Nêmesis":chefe.getCustomName().getString(),e)));
        var txt=Component.literal("Ele se lembra de você... ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.ITALIC)
            .append(Component.literal("Nv."+e.nivel()+" — "+String.join(", ",e.lista().stream().map(t->t.nome+": "+t.efeito).toList())).withStyle(ChatFormatting.RED));
        dono.displayClientMessage(txt,false);
    }
    /** Wipe: o Nemesis do dono evolui com base no estilo usado contra ele. */
    public static void wipe(ServerPlayer dono,NemesisEntity chefe,int kit){
        if(dono==null)return;var antes=estado(dono,kit);
        double corpo=chefe==null?0:chefe.getPersistentData().getDouble(CORPO),magia=chefe==null?0:chefe.getPersistentData().getDouble(MAGIA);
        var depois=RegrasNemesis.wipe(antes,corpo,magia,RNG);salvar(dono,kit,depois);
        int novo=depois.tracos()&~antes.tracos();Traco aprendido=null;for(var t:Traco.values())if(t.bit()==novo)aprendido=t;
        dono.displayClientMessage(Component.literal("O Nêmesis ficou mais forte (Nv."+depois.nivel()+")"+(aprendido==null?".":" e aprendeu "+aprendido.nome+": "+aprendido.efeito+"."))
            .withStyle(ChatFormatting.DARK_RED),false);
    }
    /** Vitoria: memoria zerada e vinganca paga em Essencia do Vazio. */
    public static void vencido(ServerPlayer dono,int kit){
        if(dono==null)return;var e=estado(dono,kit);if(e.nivel()<=0)return;
        salvar(dono,kit,new Estado(0,0));int n=RegrasNemesis.vinganca(e.nivel());
        var item=new net.minecraft.world.item.ItemStack(com.cauansilva.arcanecontrol.core.reliquias.RegistroReliquias.ESSENCIA.get(),n);
        if(!dono.getInventory().add(item))dono.drop(item,false);
        dono.displayClientMessage(Component.literal("Vingança! Nêmesis Nv."+e.nivel()+" derrotado: +"+n+" Essência do Vazio.").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD),false);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void apanhou(LivingHurtEvent e){
        if(e.getSource().getEntity() instanceof NemesisEntity n&&e.getEntity()!=n){var d=n.getPersistentData();
            if(d.getInt(NIVEL)>0||d.getInt(TRACOS)!=0)e.setAmount((float)(e.getAmount()*RegrasNemesis.dano(d.getInt(NIVEL),d.getInt(TRACOS))));return;}
        if(!(e.getEntity() instanceof NemesisEntity n)||!(e.getSource().getEntity() instanceof Player p))return;
        var d=n.getPersistentData();boolean corpo=e.getSource().getDirectEntity()==p;
        d.putDouble(corpo?CORPO:MAGIA,d.getDouble(corpo?CORPO:MAGIA)+e.getAmount());
        d.putLong(APANHOU,n.level().getGameTime());
        e.setAmount((float)(e.getAmount()*RegrasNemesis.recebido(d.getInt(TRACOS),corpo)));
    }
    @SubscribeEvent public static void tick(LivingEvent.LivingTickEvent e){
        if(!(e.getEntity() instanceof NemesisEntity n)||n.level().isClientSide||n.tickCount%20!=0)return;
        var d=n.getPersistentData();if(!Traco.IMPLACAVEL.em(d.getInt(TRACOS))||n.getHealth()>=n.getMaxHealth())return;
        if(n.level().getGameTime()-d.getLong(APANHOU)<RegrasNemesis.REGEN_ATRASO)return;
        n.heal((float)(n.getMaxHealth()*RegrasNemesis.REGEN));
        ((ServerLevel)n.level()).sendParticles(ParticleTypes.HEART,n.getX(),n.getY()+n.getBbHeight()+.2,n.getZ(),1,.3,.1,.3,0);
    }
    private NemesisMemoria(){}
}
