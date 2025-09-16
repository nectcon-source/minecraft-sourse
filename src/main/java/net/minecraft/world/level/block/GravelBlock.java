package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GravelBlock.class */
public class GravelBlock extends FallingBlock {
    public GravelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.FallingBlock
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return -8356741;
    }
}
