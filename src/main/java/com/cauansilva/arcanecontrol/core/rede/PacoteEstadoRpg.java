package com.cauansilva.arcanecontrol.core.rede;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;

public record PacoteEstadoRpg(int ativo,int kits,int[] xp,int[] nos,boolean[] despertar,float mana,int[] recargas,int[] duracoes,boolean criativo,boolean dominioLegado,boolean conjuntoCompleto){
    public PacoteEstadoRpg{
        if(xp.length!=15||nos.length!=15||despertar.length!=15||recargas.length!=5||duracoes.length!=5)throw new IllegalArgumentException("Snapshot RPG invalido");
        xp=xp.clone();nos=nos.clone();despertar=despertar.clone();recargas=recargas.clone();duracoes=duracoes.clone();
    }
    public static void encode(PacoteEstadoRpg p,FriendlyByteBuf b){
        b.writeVarInt(p.ativo);b.writeVarInt(p.kits);
        for(int i=0;i<15;i++){b.writeVarInt(p.xp[i]);b.writeVarInt(p.nos[i]);b.writeBoolean(p.despertar[i]);}
        b.writeFloat(p.mana);for(int i=0;i<5;i++){b.writeVarInt(p.recargas[i]);b.writeVarInt(p.duracoes[i]);}
        b.writeBoolean(p.criativo);b.writeBoolean(p.dominioLegado);b.writeBoolean(p.conjuntoCompleto);
    }
    public static PacoteEstadoRpg decode(FriendlyByteBuf b){
        int ativo=b.readVarInt(),kits=b.readVarInt();int[] xp=new int[15],nos=new int[15],cd=new int[5],dur=new int[5];boolean[] g=new boolean[15];
        for(int i=0;i<15;i++){xp[i]=b.readVarInt();nos[i]=b.readVarInt();g[i]=b.readBoolean();}
        float mana=b.readFloat();for(int i=0;i<5;i++){cd[i]=b.readVarInt();dur[i]=b.readVarInt();}
        return new PacoteEstadoRpg(ativo,kits,xp,nos,g,mana,cd,dur,b.readBoolean(),b.readBoolean(),b.readBoolean());
    }
    public static void handle(PacoteEstadoRpg p,Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.cauansilva.arcanecontrol.core.client.RpgCliente.receber(p)));
        ctx.get().setPacketHandled(true);
    }
}
