package com.cauansilva.arcanecontrol.core.reliquias;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Ferreiro do Vazio: abre o menu de desmanche (Essencia) e reforja de reliquias. */
public final class BlocoFerreiro extends Block {
    private static final VoxelShape FORMA=Shapes.or(box(2,0,2,14,4,14),box(4,4,5,12,9,11),box(0,9,3,16,15,13));
    public BlocoFerreiro(Properties p){super(p);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return FORMA;}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player jogador,InteractionHand mao,BlockHitResult hit){
        if(l.isClientSide)return InteractionResult.SUCCESS;
        if(jogador instanceof ServerPlayer p)net.minecraftforge.network.NetworkHooks.openScreen(p,new SimpleMenuProvider((id,inv,pl)->new MenuFerreiro(id,inv,ContainerLevelAccess.create(l,pos)),Component.literal("Ferreiro do Vazio")));
        return InteractionResult.CONSUME;
    }
}
