package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import java.util.UUID;
import java.util.function.Supplier;
public record PacoteEfeitoDespertar(UUID dono,int tipo,Vec3 a,Vec3 b,int ticks,int kit){
    public static void encode(PacoteEfeitoDespertar p,FriendlyByteBuf b){b.writeUUID(p.dono);b.writeVarInt(p.tipo);b.writeDouble(p.a.x);b.writeDouble(p.a.y);b.writeDouble(p.a.z);b.writeDouble(p.b.x);b.writeDouble(p.b.y);b.writeDouble(p.b.z);b.writeVarInt(p.ticks);b.writeVarInt(p.kit);}
    public static PacoteEfeitoDespertar decode(FriendlyByteBuf b){return new PacoteEfeitoDespertar(b.readUUID(),b.readVarInt(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readVarInt(),b.readVarInt());}
    public static void handle(PacoteEfeitoDespertar p,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.cauansilva.arcanecontrol.core.client.render.RenderDespertar.receber(p)));c.get().setPacketHandled(true);}
}
