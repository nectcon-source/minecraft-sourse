package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TwistingVinesPlant.class */
public class TwistingVinesPlant extends GrowingPlantBodyBlock {
    public static final VoxelShape SHAPE = Block.box(4.0d, 0.0d, 4.0d, 12.0d, 16.0d, 12.0d);

    public TwistingVinesPlant(BlockBehaviour.Properties properties) {
        super(properties, Direction.UP, SHAPE, false);
    }

    @Override // net.minecraft.world.level.block.GrowingPlantBlock
    protected GrowingPlantHeadBlock getHeadBlock() {
        return (GrowingPlantHeadBlock) Blocks.TWISTING_VINES;
    }
}
