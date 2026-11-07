package com.cauansilva.arcanecontrol.core.familiar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.UUID;

/**
 * Familiar: espirito sem IA de mob (nao usa pathfinding nem colisao, entao nao trava no Vazio nem em teleportes).
 * Move-se suavemente ate o ombro do dono e teleporta se ficar longe. Nao e salvo no mundo: o servidor recria a
 * partir do NBT do dono ({@link FamiliarServidor}).
 */
public final class FamiliarEntity extends Entity {
    static final EntityDataAccessor<Integer> TIPO=SynchedEntityData.defineId(FamiliarEntity.class,EntityDataSerializers.INT);
    static final EntityDataAccessor<Optional<UUID>> DONO=SynchedEntityData.defineId(FamiliarEntity.class,EntityDataSerializers.OPTIONAL_UUID);
    public FamiliarEntity(EntityType<? extends FamiliarEntity> t,Level l){super(t,l);noPhysics=true;setNoGravity(true);}
    @Override protected void defineSynchedData(){entityData.define(TIPO,0);entityData.define(DONO,Optional.empty());}
    public RegrasFamiliar.Tipo tipo(){var t=RegrasFamiliar.Tipo.porId(entityData.get(TIPO));return t==null?RegrasFamiliar.Tipo.RAIO:t;}
    public UUID dono(){return entityData.get(DONO).orElse(null);}
    void configurar(UUID dono,RegrasFamiliar.Tipo t){entityData.set(DONO,Optional.of(dono));entityData.set(TIPO,t.ordinal());}

    @Override public void tick(){
        super.tick();
        var id=dono();var p=id==null?null:level().getPlayerByUUID(id);
        if(p==null){if(!level().isClientSide&&tickCount>40)discard();return;}
        double[] a=RegrasFamiliar.posicao(p.getX(),p.getY(),p.getZ(),p.getYRot(),tickCount);
        Vec3 alvo=new Vec3(a[0],a[1],a[2]),d=alvo.subtract(position());
        if(d.length()>RegrasFamiliar.TELEPORTA){setPos(alvo);setDeltaMovement(Vec3.ZERO);}
        else{setDeltaMovement(d.scale(.22));setPos(position().add(getDeltaMovement()));}
        setYRot(p.getYRot());
    }
    @Override public boolean isPickable(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public boolean isInvulnerable(){return true;}
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float v){return false;}
    @Override protected void readAdditionalSaveData(CompoundTag t){}
    @Override protected void addAdditionalSaveData(CompoundTag t){}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return new net.minecraft.network.protocol.game.ClientboundAddEntityPacket(this);}
}
