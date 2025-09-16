package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BeetrootBlock.class */
public class BeetrootBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape[] SHAPE_BY_AGE = {Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 4.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 6.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d)};

    public BeetrootBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.CropBlock
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override // net.minecraft.world.level.block.CropBlock
    public int getMaxAge() {
        return 3;
    }

    @Override // net.minecraft.world.level.block.CropBlock
    protected ItemLike getBaseSeedId() {
        return Items.BEETROOT_SEEDS;
    }

    @Override // net.minecraft.world.level.block.CropBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (random.nextInt(3) != 0) {
            super.randomTick(blockState, serverLevel, blockPos, random);
        }
    }

    @Override // net.minecraft.world.level.block.CropBlock
    protected int getBonemealAgeIncrease(Level level) {
        return super.getBonemealAgeIncrease(level) / 3;
    }

    @Override // net.minecraft.world.level.block.CropBlock, net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override // net.minecraft.world.level.block.CropBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[((Integer) blockState.getValue(getAgeProperty())).intValue()];
    }
}
