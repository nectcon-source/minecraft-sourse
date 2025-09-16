package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WeepingVines.class */
public class WeepingVines extends GrowingPlantHeadBlock {
    protected static final VoxelShape SHAPE = Block.box(4.0d, 9.0d, 4.0d, 12.0d, 16.0d, 12.0d);

    public WeepingVines(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.1d);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected int getBlocksToGrowWhenBonemealed(Random random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected Block getBodyBlock() {
        return Blocks.WEEPING_VINES_PLANT;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected boolean canGrowInto(BlockState blockState) {
        return NetherVines.isValidGrowthState(blockState);
    }
}
