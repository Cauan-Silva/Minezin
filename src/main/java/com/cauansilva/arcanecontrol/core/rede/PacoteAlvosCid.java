package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import java.util.*;
import java.util.function.Supplier;
public record PacoteAlvosCid(UUID jogador,List<UUID> alvos){
    public static void encode(PacoteAlvosCid p,FriendlyByteBuf b){b.writeUUID(p.jogador);int n=Math.min(8,p.alvos.size());b.writeVarInt(n);for(int i=0;i<n;i++)b.writeUUID(p.alvos.get(i));}
    public static PacoteAlvosCid decode(FriendlyByteBuf b){
        UUID id=b.readUUID();int n=b.readVarInt();if(n<0||n>8)throw new io.netty.handler.codec.DecoderException("Alvos Cid invalidos");
        List<UUID> ids=new ArrayList<>();for(int i=0;i<n;i++)ids.add(b.readUUID());return new PacoteAlvosCid(id,List.copyOf(ids));
    }
    public static void handle(PacoteAlvosCid p,Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.cauansilva.arcanecontrol.core.cid.EfeitosCid.vincularAlvos(p.jogador,p.alvos)));
        ctx.get().setPacketHandled(true);
    }
}
