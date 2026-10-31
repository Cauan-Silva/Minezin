package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record PacoteAcaoPoder(int acao,int kit){
    public static void encode(PacoteAcaoPoder p,FriendlyByteBuf b){b.writeVarInt(p.acao);b.writeVarInt(p.kit);}
    public static PacoteAcaoPoder decode(FriendlyByteBuf b){return new PacoteAcaoPoder(b.readVarInt(),b.readVarInt());}
    public static void handle(PacoteAcaoPoder p,Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{var jogador=ctx.get().getSender();if(jogador!=null)
            com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes.acao(jogador,p.acao,p.kit);});
        ctx.get().setPacketHandled(true);
    }
}
