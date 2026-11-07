package com.cauansilva.arcanecontrol.core.reliquias;

import com.cauansilva.arcanecontrol.core.reliquias.Reliquias.*;
import com.cauansilva.arcanecontrol.core.rpg.RpgServidor;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Foco de Caca no servidor: guardado nos dados RPG ("foco_tipo"/"foco_afixo", vazio = sem foco). Comando aberto a
 * todos ({@code /foco}) com opcoes clicaveis. A Essencia e cobrada do inventario quando o bau e gerado; sem Essencia
 * suficiente o bau sai normal (e o foco continua salvo).
 */
@Mod.EventBusSubscriber(modid="arcanecontrol")
public final class FocoServidor {
    private static final String TIPO="foco_tipo",AFIXO="foco_afixo";

    public static Tipo tipo(ServerPlayer p){return FocoCaca.tipo(RpgServidor.dados(p).getString(TIPO));}
    public static Afixo afixo(ServerPlayer p){return FocoCaca.afixo(RpgServidor.dados(p).getString(AFIXO));}

    @SubscribeEvent public static void comandos(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("foco")
            .executes(c->{menu(c.getSource().getPlayerOrException());return 1;})
            .then(Commands.literal("limpar").executes(c->{var p=c.getSource().getPlayerOrException();RpgServidor.dados(p).remove(TIPO);RpgServidor.dados(p).remove(AFIXO);menu(p);return 1;}))
            .then(Commands.literal("tipo").then(Commands.argument("v",StringArgumentType.word()).executes(c->{
                var p=c.getSource().getPlayerOrException();var v=StringArgumentType.getString(c,"v");
                if(v.equalsIgnoreCase("nenhum"))RpgServidor.dados(p).remove(TIPO);else if(FocoCaca.tipo(v)!=null)RpgServidor.dados(p).putString(TIPO,FocoCaca.tipo(v).name());
                menu(p);return 1;})))
            .then(Commands.literal("afixo").then(Commands.argument("v",StringArgumentType.word()).executes(c->{
                var p=c.getSource().getPlayerOrException();var v=StringArgumentType.getString(c,"v");
                if(v.equalsIgnoreCase("nenhum"))RpgServidor.dados(p).remove(AFIXO);else if(FocoCaca.afixo(v)!=null)RpgServidor.dados(p).putString(AFIXO,FocoCaca.afixo(v).name());
                menu(p);return 1;}))));
    }
    private static MutableComponent botao(String txt,String cmd,boolean ativo){
        return Component.literal("["+txt+"]").withStyle(s->s.withColor(ativo?ChatFormatting.GREEN:ChatFormatting.GRAY).withBold(ativo)
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,cmd)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal(cmd))));
    }
    static void menu(ServerPlayer p){
        var t=tipo(p);var a=afixo(p);
        p.sendSystemMessage(Component.literal("— Foco de Caça (próximo baú do Vazio) —").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
        var lt=Component.literal("Tipo: ").withStyle(ChatFormatting.YELLOW);lt.append(botao("Nenhum","/foco tipo nenhum",t==null));
        for(var x:Tipo.values())lt.append(" ").append(botao(x.nome,"/foco tipo "+x.name().toLowerCase(),x==t));
        p.sendSystemMessage(lt);
        var la=Component.literal("Afixo: ").withStyle(ChatFormatting.YELLOW);la.append(botao("Nenhum","/foco afixo nenhum",a==null));
        for(var x:Afixo.values())la.append(" ").append(botao(x.bonus,"/foco afixo "+x.name().toLowerCase(),x==a));
        p.sendSystemMessage(la);
        int c=FocoCaca.custo(t,a);
        p.sendSystemMessage(Component.literal(c==0?"Sem foco: baú normal.":"Custo: "+c+" Essência do Vazio por baú. 1ª relíquia garantida no foco; as outras têm chance maior. Raridade não muda.").withStyle(ChatFormatting.GRAY));
    }
    /** Cobra o foco do inventario; devolve true se o foco vale para este bau. */
    static boolean cobrar(ServerPlayer p){
        var t=tipo(p);var a=afixo(p);int c=FocoCaca.custo(t,a);if(c==0)return false;
        var inv=p.getInventory();var ess=RegistroReliquias.ESSENCIA.get();
        if(inv.countItem(ess)<c){p.sendSystemMessage(Component.literal("Foco de Caça ignorado: precisa de "+c+" Essência do Vazio no inventário.").withStyle(ChatFormatting.RED));return false;}
        int falta=c;for(int i=0;i<inv.getContainerSize()&&falta>0;i++){var s=inv.getItem(i);if(s.is(ess)){int n=Math.min(falta,s.getCount());s.shrink(n);falta-=n;}}
        p.sendSystemMessage(Component.literal("Foco de Caça aplicado (-"+c+" Essência): "+(t==null?"":t.nome)+(t!=null&&a!=null?" + ":"")+(a==null?"":a.bonus)).withStyle(ChatFormatting.LIGHT_PURPLE));
        return true;
    }
    private FocoServidor(){}
}
