package com.cauansilva.arcanecontrol.core.rpg;
import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import com.cauansilva.arcanecontrol.core.poderes.PoderAnime;
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
                .then(comandoMaestria())
                .then(Commands.literal("restaurar_recursos").executes(c->{
                    var p=c.getSource().getPlayerOrException();
                    RpgServidor.dados(p).putDouble("mana",100);
                    PerfilRpgNbt.salvarRecargas(RpgServidor.dados(p),new RecargasRpg());
                    RpgServidor.sincronizar(p);
                    c.getSource().sendSuccess(()->Component.literal("Mana restaurada e recargas RPG de todos os kits zeradas. Técnicas em andamento continuam."),true);return 1;
                }))
                .then(Commands.literal("adicionar_xp").then(Commands.argument("quantidade",IntegerArgumentType.integer(0,9900)).executes(c->{
                    var p=c.getSource().getPlayerOrException();RpgServidor.ganharXp(p,ProgressaoPoderes.ler(p).ativo(),IntegerArgumentType.getInteger(c,"quantidade"));
                    c.getSource().sendSuccess(()->Component.literal("XP aplicado ao kit ativo."),false);return 1;
                })))
                .then(Commands.literal("mana").then(Commands.argument("valor",DoubleArgumentType.doubleArg(0,100)).executes(c->{
                    var p=c.getSource().getPlayerOrException();RpgServidor.dados(p).putDouble("mana",new RecursoMana(DoubleArgumentType.getDouble(c,"valor")).atual());RpgServidor.sincronizar(p);
                    c.getSource().sendSuccess(()->Component.literal("Mana ajustada."),false);return 1;
                })))));
    }
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> comandoMaestria(){
        var maxima=Commands.literal("maxima").executes(c->maximizar(c.getSource(),null,false));
        maxima.then(Commands.literal("todos").executes(c->maximizar(c.getSource(),null,true)));
        for(var kit:PoderAnime.values())maxima.then(Commands.literal(kit.name().toLowerCase(java.util.Locale.ROOT))
            .executes(c->maximizar(c.getSource(),kit,false)));
        return Commands.literal("maestria").then(maxima);
    }
    private static int maximizar(CommandSourceStack fonte,PoderAnime escolhido,boolean todos)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=fonte.getPlayerOrException();var estado=ProgressaoPoderes.ler(p);
        var kit=escolhido==null?estado.ativo():escolhido;
        // Initialize migration before changing unlocks; retain the selected kit and other progression.
        var dados=RpgServidor.dados(p);int desbloqueados=estado.desbloqueados();
        for(var atual:PoderAnime.values())if(todos||atual==kit){
            desbloqueados|=atual.bit();
            RpgServidor.salvarMestria(dados,atual.id,MestriaRpg.maximaParaTeste());
        }
        ProgressaoPoderes.tag(p).putInt("kits",desbloqueados);
        RpgServidor.aplicarAtributos(p);ProgressaoPoderes.sincronizar(p);
        fonte.sendSuccess(()->Component.literal("Maestria nível 100, árvore completa e despertar liberados: "+(todos?"todos os kits":kit.name())+". Progresso salvo; kit ativo mantido."),true);
        return todos?PoderAnime.values().length:1;
    }

}
