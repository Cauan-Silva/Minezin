package com.cauansilva.arcanecontrol.core.rpg;
import com.cauansilva.arcanecontrol.core.rede.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import java.util.UUID;
public final class EfeitosDespertar {
    public static void enviar(ServerLevel l,UUID dono,int tipo,Vec3 a,Vec3 b,int ticks,int kit){
        RegistoRede.ENVIAR.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(a.x,a.y,a.z,128,l.dimension())),new PacoteEfeitoDespertar(dono,tipo,a,b,ticks,kit));
    }
}
