package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NetherWartBlock.class */
public class NetherWartBlock extends BushBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape[] SHAPE_BY_AGE = {Block.box(0.0d, 0.0d, 0.0d, 16.0d, 5.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 11.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 14.0d, 16.0d)};

    protected NetherWartBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[((Integer) blockState.getValue(AGE)).intValue()];
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(Blocks.SOUL_SAND);
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 3;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        if (intValue < 3 && random.nextInt(10) == 0) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(intValue + 1)), 2);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(Items.NETHER_WART);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
