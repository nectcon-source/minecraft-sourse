package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CarrotBlock.class */
public class CarrotBlock extends CropBlock {
    private static final VoxelShape[] SHAPE_BY_AGE = {Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 3.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 4.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 5.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 6.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 7.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 9.0d, 16.0d)};

    public CarrotBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.CropBlock
    protected ItemLike getBaseSeedId() {
        return Items.CARROT;
    }

    @Override // net.minecraft.world.level.block.CropBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[((Integer) blockState.getValue(getAgeProperty())).intValue()];
    }
}
