package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BaseEntityBlock.class */
public abstract class BaseEntityBlock extends Block implements EntityBlock {
    protected BaseEntityBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int i2) {
        super.triggerEvent(blockState, level, blockPos, i, i2);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        Object blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof MenuProvider) {
            return (MenuProvider) blockEntity;
        }
        return null;
    }
}
