package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.rpg.*;
import com.cauansilva.arcanecontrol.core.rede.PacoteEstadoRpg;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class RpgCliente {
    private static PacoteEstadoRpg estado;
    private static long recebidoEm;
    public static boolean recebido(){return estado!=null;}
    public static void receber(PacoteEstadoRpg p){estado=p;var mc=Minecraft.getInstance();recebidoEm=mc.level==null?0:mc.level.getGameTime();}
    public static int ativo(){return estado==null?1:estado.ativo();}
    public static boolean tem(int kit){return estado!=null&&kit>=1&&kit<=15&&(estado.kits()&(1<<(kit-1)))!=0;}
    public static boolean criativo(){return estado!=null&&estado.criativo();}
    public static int formaTicks(){var mc=Minecraft.getInstance();return estado==null||mc.level==null?0:(int)Math.max(0,estado.formaTicks()-Math.max(0,mc.level.getGameTime()-recebidoEm));}
    public static boolean forma(){return formaTicks()>0;}
    public static boolean exclusivoUsado(){return estado!=null&&estado.exclusivoUsado();}
    public static boolean dominioLegado(){return estado!=null&&estado.dominioLegado();}
    public static MestriaRpg mestria(int kit){return estado==null||kit<1||kit>15?new MestriaRpg():new MestriaRpg(estado.xp()[kit-1],estado.nos()[kit-1],estado.despertar()[kit-1]);}
    public static double mana(){return estado==null?100:new RecursoMana(estado.mana()).atual();}
    public static double custo(int slot){return criativo()?0:RecursoMana.custo(ConjuracaoRpg.custoBase(slot),estado!=null&&estado.conjuntoCompleto());}
    public static int recarga(int slot){
        var mc=Minecraft.getInstance();if(estado==null||mc.level==null||slot<0||slot>4)return 0;
        return (int)Math.max(0,Math.min(12000,estado.recargas()[slot]-Math.max(0,mc.level.getGameTime()-recebidoEm)));
    }
    public static double fracao(int slot){return estado==null||estado.duracoes()[slot]<=0?0:Math.min(1,recarga(slot)/(double)estado.duracoes()[slot]);}
    public static boolean tecnica(int kit,int slot){return tem(kit)&&(criativo()&&slot<4||mestria(kit).podeUsarSlot(slot));}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){estado=null;recebidoEm=0;}
}
