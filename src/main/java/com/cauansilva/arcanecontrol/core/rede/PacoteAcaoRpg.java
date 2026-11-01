package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
/** Cliente envia apenas pedido/kit/no; nunca XP, pontos ou mana. */
public record PacoteAcaoRpg(int acao,int kit,int no){
    public static void encode(PacoteAcaoRpg p,FriendlyByteBuf b){b.writeVarInt(p.acao);b.writeVarInt(p.kit);b.writeVarInt(p.no);}
    public static PacoteAcaoRpg decode(FriendlyByteBuf b){return new PacoteAcaoRpg(b.readVarInt(),b.readVarInt(),b.readVarInt());}
    public static void handle(PacoteAcaoRpg p,Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            var jogador=ctx.get().getSender();if(jogador==null)return;
            if(p.acao==1)com.cauansilva.arcanecontrol.core.rpg.RpgServidor.comprar(jogador,p.kit,p.no);
            else if(p.acao==0){
                long now=com.cauansilva.arcanecontrol.core.rpg.RpgServidor.agora(jogador),ultimo=jogador.getPersistentData().getLong("rpg_snapshot_tick");
                if(ultimo<=0||now-ultimo>=10){jogador.getPersistentData().putLong("rpg_snapshot_tick",now);com.cauansilva.arcanecontrol.core.rpg.RpgServidor.sincronizar(jogador);}
            }
        });ctx.get().setPacketHandled(true);
    }
}
