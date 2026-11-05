package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;
public record PacoteEstadoPoderes(int ativo,int kits,int conhecimento,int[] xp,int[] usos,boolean criativo){
    public PacoteEstadoPoderes{xp=java.util.Arrays.copyOf(xp,com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS);usos=java.util.Arrays.copyOf(usos,com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS*5);}
    public static void encode(PacoteEstadoPoderes p,FriendlyByteBuf b){
        b.writeVarInt(p.ativo);b.writeVarInt(p.kits);b.writeVarInt(p.conhecimento);
        for(int i=0;i<com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS;i++)b.writeVarInt(p.xp[i]);for(int i=0;i<com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS*5;i++)b.writeVarInt(p.usos[i]);b.writeBoolean(p.criativo);
    }
    public static PacoteEstadoPoderes decode(FriendlyByteBuf b){
        int a=b.readVarInt(),k=b.readVarInt(),c=b.readVarInt();int[] xp=new int[com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS],u=new int[com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS*5];
        for(int i=0;i<com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS;i++)xp[i]=b.readVarInt();for(int i=0;i<com.cauansilva.arcanecontrol.core.poderes.PoderAnime.KITS*5;i++)u[i]=b.readVarInt();
        return new PacoteEstadoPoderes(a,k,c,xp,u,b.readBoolean());
    }
    public static void handle(PacoteEstadoPoderes p,Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->com.cauansilva.arcanecontrol.core.client.PoderesCliente.receber(p)));
        ctx.get().setPacketHandled(true);
    }
}
