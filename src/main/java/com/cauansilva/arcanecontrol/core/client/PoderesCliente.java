package com.cauansilva.arcanecontrol.core.client;
import com.cauansilva.arcanecontrol.core.poderes.*;
import com.cauansilva.arcanecontrol.core.rede.PacoteEstadoPoderes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="arcanecontrol",value=Dist.CLIENT)
public final class PoderesCliente {
    public static EstadoPoderes estado=new EstadoPoderes();
    public static boolean recebido,criativo;
    public static void receber(PacoteEstadoPoderes p){estado=new EstadoPoderes(p.ativo(),p.kits(),p.conhecimento(),p.xp(),p.usos());criativo=p.criativo();recebido=true;}
    public static PoderAnime ativo(){return estado.ativo();}
    @SubscribeEvent public static void sair(ClientPlayerNetworkEvent.LoggingOut e){estado=new EstadoPoderes();recebido=false;criativo=false;}
}
