package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CropBlock.class */
public class CropBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[] SHAPE_BY_AGE = {Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 4.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 6.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 10.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 12.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 14.0d, 16.0d), Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d)};

    protected CropBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(getAgeProperty(), 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[((Integer) blockState.getValue(getAgeProperty())).intValue()];
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(Blocks.FARMLAND);
    }

    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 7;
    }

    protected int getAge(BlockState blockState) {
        return ((Integer) blockState.getValue(getAgeProperty())).intValue();
    }

    public BlockState getStateForAge(int i) {
        return (BlockState) defaultBlockState().setValue(getAgeProperty(), Integer.valueOf(i));
    }

    public boolean isMaxAge(BlockState blockState) {
        return ((Integer) blockState.getValue(getAgeProperty())).intValue() >= getMaxAge();
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return !isMaxAge(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int age;
        if (serverLevel.getRawBrightness(blockPos, 0) >= 9 && (age = getAge(blockState)) < getMaxAge() && random.nextInt(((int) (25.0f / getGrowthSpeed(this, serverLevel, blockPos))) + 1) == 0) {
            serverLevel.setBlock(blockPos, getStateForAge(age + 1), 2);
        }
    }

    public void growCrops(Level level, BlockPos blockPos, BlockState blockState) {
        int age = getAge(blockState) + getBonemealAgeIncrease(level);
        int maxAge = getMaxAge();
        if (age > maxAge) {
            age = maxAge;
        }
        level.setBlock(blockPos, getStateForAge(age), 2);
    }

    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 2, 5);
    }

    protected static float getGrowthSpeed(Block block, BlockGetter blockGetter, BlockPos blockPos) {
        float f = 1.0f;
        BlockPos below = blockPos.below();
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                float f2 = 0.0f;
                BlockState blockState = blockGetter.getBlockState(below.offset(i, 0, i2));
                if (blockState.is(Blocks.FARMLAND)) {
                    f2 = 1.0f;
                    if (((Integer) blockState.getValue(FarmBlock.MOISTURE)).intValue() > 0) {
                        f2 = 3.0f;
                    }
                }
                if (i != 0 || i2 != 0) {
                    f2 /= 4.0f;
                }
                f += f2;
            }
        }
        BlockPos north = blockPos.north();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos east = blockPos.east();
        boolean z = block == blockGetter.getBlockState(west).getBlock() || block == blockGetter.getBlockState(east).getBlock();
        boolean z2 = block == blockGetter.getBlockState(north).getBlock() || block == blockGetter.getBlockState(south).getBlock();
        if (z && z2) {
            f /= 2.0f;
        } else if (block == blockGetter.getBlockState(west.north()).getBlock() || block == blockGetter.getBlockState(east.north()).getBlock() || block == blockGetter.getBlockState(east.south()).getBlock() || block == blockGetter.getBlockState(west.south()).getBlock()) {
            f /= 2.0f;
        }
        return f;
    }

    @Override // net.minecraft.world.level.block.BushBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return (levelReader.getRawBrightness(blockPos, 0) >= 8 || levelReader.canSeeSky(blockPos)) && super.canSurvive(blockState, levelReader, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if ((entity instanceof Ravager) && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            level.destroyBlock(blockPos, true, entity);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }

    protected ItemLike getBaseSeedId() {
        return Items.WHEAT_SEEDS;
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(getBaseSeedId());
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return !isMaxAge(blockState);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        growCrops(serverLevel, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
