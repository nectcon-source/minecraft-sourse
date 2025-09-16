package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SandBlock.class */
public class SandBlock extends FallingBlock {
    private final int dustColor;

    public SandBlock(int i, BlockBehaviour.Properties properties) {
        super(properties);
        this.dustColor = i;
    }

    @Override // net.minecraft.world.level.block.FallingBlock
    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.dustColor;
    }
}
