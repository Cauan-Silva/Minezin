package com.cauansilva.arcanecontrol.core.rpg;
import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Ferramentas administrativas para exercitar nivel, custos e UI sem grind de teste. */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class ComandosRpg {
    @SubscribeEvent public static void registrar(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("minezin").requires(s->s.hasPermission(2))
            .then(Commands.literal("rpg")
                .then(Commands.literal("adicionar_xp").then(Commands.argument("quantidade",IntegerArgumentType.integer(0,9900)).executes(c->{
                    var p=c.getSource().getPlayerOrException();RpgServidor.ganharXp(p,ProgressaoPoderes.ler(p).ativo(),IntegerArgumentType.getInteger(c,"quantidade"));
                    c.getSource().sendSuccess(()->Component.literal("XP aplicado ao kit ativo."),false);return 1;
                })))
                .then(Commands.literal("mana").then(Commands.argument("valor",DoubleArgumentType.doubleArg(0,100)).executes(c->{
                    var p=c.getSource().getPlayerOrException();RpgServidor.dados(p).putDouble("mana",new RecursoMana(DoubleArgumentType.getDouble(c,"valor")).atual());RpgServidor.sincronizar(p);
                    c.getSource().sendSuccess(()->Component.literal("Mana ajustada."),false);return 1;
                })))));
    }
}
