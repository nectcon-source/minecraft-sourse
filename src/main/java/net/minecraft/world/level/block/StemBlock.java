package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StemBlock.class */
public class StemBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    protected static final VoxelShape[] SHAPE_BY_AGE = {Block.box(7.0d, 0.0d, 7.0d, 9.0d, 2.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 4.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 6.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 8.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 10.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 12.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 14.0d, 9.0d), Block.box(7.0d, 0.0d, 7.0d, 9.0d, 16.0d, 9.0d)};
    private final StemGrownBlock fruit;

    protected StemBlock(StemGrownBlock stemGrownBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.fruit = stemGrownBlock;
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[((Integer) blockState.getValue(AGE)).intValue()];
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(Blocks.FARMLAND);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (serverLevel.getRawBrightness(blockPos, 0) >= 9 && random.nextInt(((int) (25.0f / CropBlock.getGrowthSpeed(this, serverLevel, blockPos))) + 1) == 0) {
            int intValue = ((Integer) blockState.getValue(AGE)).intValue();
            if (intValue < 7) {
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(intValue + 1)), 2);
                return;
            }
            Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos relative = blockPos.relative(randomDirection);
            BlockState blockState2 = serverLevel.getBlockState(relative.below());
            if (serverLevel.getBlockState(relative).isAir()) {
                if (blockState2.is(Blocks.FARMLAND) || blockState2.is(Blocks.DIRT) || blockState2.is(Blocks.COARSE_DIRT) || blockState2.is(Blocks.PODZOL) || blockState2.is(Blocks.GRASS_BLOCK)) {
                    serverLevel.setBlockAndUpdate(relative, this.fruit.defaultBlockState());
                    serverLevel.setBlockAndUpdate(blockPos, (BlockState) this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, randomDirection));
                }
            }
        }
    }

    @Nullable
    protected Item getSeedItem() {
        if (this.fruit == Blocks.PUMPKIN) {
            return Items.PUMPKIN_SEEDS;
        }
        if (this.fruit == Blocks.MELON) {
            return Items.MELON_SEEDS;
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Item seedItem = getSeedItem();
        return seedItem == null ? ItemStack.EMPTY : new ItemStack(seedItem);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return ((Integer) blockState.getValue(AGE)).intValue() != 7;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        int min = Math.min(7, ((Integer) blockState.getValue(AGE)).intValue() + Mth.nextInt(serverLevel.random, 2, 5));
        BlockState blockState2 = (BlockState) blockState.setValue(AGE, Integer.valueOf(min));
        serverLevel.setBlock(blockPos, blockState2, 2);
        if (min == 7) {
            blockState2.randomTick(serverLevel, blockPos, serverLevel.random);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public StemGrownBlock getFruit() {
        return this.fruit;
    }
}
