package com.cauansilva.arcanecontrol.core.rpg;

import com.cauansilva.arcanecontrol.core.poderes.ProgressaoPoderes;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.network.chat.Component;

public final class AltarDespertar extends Block {
    private static final VoxelShape FORMA=Shapes.or(box(0,0,0,16,3,16),box(2,3,2,14,6,14),box(5,6,5,11,11,11));
    public AltarDespertar(Properties p){super(p);}
    @Override public VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,CollisionContext c){return FORMA;}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player jogador,InteractionHand mao,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(!(jogador instanceof ServerPlayer p)||mao!=InteractionHand.MAIN_HAND)return InteractionResult.CONSUME;
        var tag=RpgServidor.dados(p);
        if(p.isShiftKeyDown()){
            if(tag.hasUUID("desafio")){var e=((ServerLevel)l).getEntity(tag.getUUID("desafio"));if(e instanceof NemesisEntity)e.discard();tag.remove("desafio");}
            p.displayClientMessage(Component.literal("Desafio cancelado. Nenhuma alma foi concedida."),true);return InteractionResult.CONSUME;
        }
        var kit=ProgressaoPoderes.ler(p).ativo();var m=RpgServidor.mestria(p,kit.id);
        String erro=!p.isAlive()||p.isSpectator()?"Você precisa estar vivo.":tag.hasUUID("desafio")?"Você já tem um desafio. Agache e use o altar para cancelar.":!m.tem(NoRpg.DESPERTAR)?"Compre o nó Despertar em H (nível 50).":m.despertarLiberado()?"Este kit já despertou.":null;
        if(erro!=null){p.displayClientMessage(Component.literal(erro),true);return InteractionResult.CONSUME;}
        Vec3 direcao=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize();if(direcao.lengthSqr()<.01)direcao=new Vec3(0,0,1);
        Vec3 centro=Vec3.atBottomCenterOf(pos);var level=(ServerLevel)l;NemesisEntity chefe=null;
        for(int altura=0;altura<5&&chefe==null;altura++){
            var destino=centro.add(direcao.scale(5)).add(0,altura,0);var bp=BlockPos.containing(destino);
            if(!level.hasChunkAt(bp)||!level.getWorldBorder().isWithinBounds(bp))continue;
            var candidato=RegistroDespertar.NEMESIS.get().create(level);if(candidato==null)break;candidato.moveTo(destino.x,destino.y,destino.z,p.getYRot()+180,0);
            if(!level.noCollision(candidato,candidato.getBoundingBox().inflate(1,.1,1))||level.containsAnyLiquid(candidato.getBoundingBox())||!level.getBlockState(bp.below()).isFaceSturdy(level,bp.below(),Direction.UP))continue;
            chefe=candidato;
        }
        if(chefe==null){p.displayClientMessage(Component.literal("Libere uma área com chão firme, a cinco blocos à frente do altar."),true);return InteractionResult.CONSUME;}
        chefe.preparar(p,kit.id,centro);tag.putUUID("desafio",chefe.getUUID());
        if(!level.addFreshEntity(chefe)){tag.remove("desafio");return InteractionResult.FAIL;}
        p.displayClientMessage(Component.literal("Desafio: "+PersonagemRpg.porId(kit.id).nemesis+". Permaneça próximo do altar."),false);
        return InteractionResult.CONSUME;
    }
}
