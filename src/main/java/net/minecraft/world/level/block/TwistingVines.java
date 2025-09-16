package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TwistingVines.class */
public class TwistingVines extends GrowingPlantHeadBlock {
    public static final VoxelShape SHAPE = Block.box(4.0d, 0.0d, 4.0d, 12.0d, 15.0d, 12.0d);

    public TwistingVines(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, false, 0.1d);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected int getBlocksToGrowWhenBonemealed(Random random) {
        return NetherVines.getBlocksToGrowWhenBonemealed(random);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected Block getBodyBlock() {
        return Blocks.TWISTING_VINES_PLANT;
    }

    @Override // net.minecraft.world.level.block.GrowingPlantHeadBlock
    protected boolean canGrowInto(BlockState blockState) {
        return NetherVines.isValidGrowthState(blockState);
    }
}
