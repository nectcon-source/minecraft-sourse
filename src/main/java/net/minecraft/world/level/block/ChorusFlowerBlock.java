package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ChorusFlowerBlock.class */
public class ChorusFlowerBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    private final ChorusPlantBlock plant;

    protected ChorusFlowerBlock(ChorusPlantBlock chorusPlantBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.plant = chorusPlantBlock;
        registerDefaultState( this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 5;
    }


    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(net.minecraft.world.level.block.state.BlockState var1, net.minecraft.server.level.ServerLevel var2, net.minecraft.core.BlockPos var3, java.util.Random var4) {
        BlockPos var5 = var3.above();
        if (var2.isEmptyBlock(var5) && var5.getY() < 256) {
            int var6x = var1.getValue(AGE);
            if (var6x < 5) {
                boolean var7xx = false;
                boolean var8xxx = false;
                BlockState var9xxxx = var2.getBlockState(var3.below());
                Block var10xxxxx = var9xxxx.getBlock();
                if (var10xxxxx == Blocks.END_STONE) {
                    var7xx = true;
                } else if (var10xxxxx == this.plant) {
                    int var11xx = 1;

                    for(int var12xxx = 0; var12xxx < 4; ++var12xxx) {
                        Block var13xxxx = var2.getBlockState(var3.below(var11xx + 1)).getBlock();
                        if (var13xxxx != this.plant) {
                            if (var13xxxx == Blocks.END_STONE) {
                                var8xxx = true;
                            }
                            break;
                        }

                        ++var11xx;
                    }

                    if (var11xx < 2 || var11xx <= var4.nextInt(var8xxx ? 5 : 4)) {
                        var7xx = true;
                    }
                } else if (var9xxxx.isAir()) {
                    var7xx = true;
                }

                if (var7xx && allNeighborsEmpty(var2, var5, null) && var2.isEmptyBlock(var3.above(2))) {
                    var2.setBlock(var3, this.plant.getStateForPlacement(var2,var3), 2);
                    this.placeGrownFlower(var2, var5, var6x);
                } else if (var6x < 4) {
                    int var16xx = var4.nextInt(4);
                    if (var8xxx) {
                        ++var16xx;
                    }

                    boolean var17xx = false;

                    for(int var18xxx = 0; var18xxx < var16xx; ++var18xxx) {
                        Direction var14xxxx = Direction.Plane.HORIZONTAL.getRandomDirection(var4);
                        BlockPos var15xxxxx = var3.relative(var14xxxx);
                        if (var2.isEmptyBlock(var15xxxxx) && var2.isEmptyBlock(var15xxxxx.below()) && allNeighborsEmpty(var2, var15xxxxx, var14xxxx.getOpposite())) {
                            this.placeGrownFlower(var2, var15xxxxx, var6x + 1);
                            var17xx = true;
                        }
                    }

                    if (var17xx) {
                        var2.setBlock(var3, this.plant.getStateForPlacement(var2, var3), 2);
                    } else {
                        this.placeDeadFlower(var2, var3);
                    }
                } else {
                    this.placeDeadFlower(var2, var3);
                }
            }
        }
    }

    private void placeGrownFlower(Level level, BlockPos blockPos, int i) {
        level.setBlock(blockPos, (BlockState) defaultBlockState().setValue(AGE, Integer.valueOf(i)), 2);
        level.levelEvent(1033, blockPos, 0);
    }

    private void placeDeadFlower(Level level, BlockPos blockPos) {
        level.setBlock(blockPos, (BlockState) defaultBlockState().setValue(AGE, 5), 2);
        level.levelEvent(1034, blockPos, 0);
    }

    private static boolean allNeighborsEmpty(LevelReader levelReader, BlockPos blockPos, @Nullable Direction direction) {
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (next != direction && !levelReader.isEmptyBlock(blockPos.relative(next))) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction != Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        if (blockState2.getBlock() == this.plant || blockState2.is(Blocks.END_STONE)) {
            return true;
        }
        if (!blockState2.isAir()) {
            return false;
        }
        boolean z = false;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            BlockState blockState3 = levelReader.getBlockState(blockPos.relative(it.next()));
            if (blockState3.is(this.plant)) {
                if (z) {
                    return false;
                }
                z = true;
            } else if (!blockState3.isAir()) {
                return false;
            }
        }
        return z;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public static void generatePlant(LevelAccessor levelAccessor, BlockPos blockPos, Random random, int i) {
        levelAccessor.setBlock(blockPos, ((ChorusPlantBlock) Blocks.CHORUS_PLANT).getStateForPlacement(levelAccessor, blockPos), 2);
        growTreeRecursive(levelAccessor, blockPos, random, blockPos, i, 0);
    }

    private static void growTreeRecursive(LevelAccessor levelAccessor, BlockPos blockPos, Random random, BlockPos blockPos2, int i, int i2) {
        ChorusPlantBlock chorusPlantBlock = (ChorusPlantBlock) Blocks.CHORUS_PLANT;
        int nextInt = random.nextInt(4) + 1;
        if (i2 == 0) {
            nextInt++;
        }
        for (int i3 = 0; i3 < nextInt; i3++) {
            BlockPos above = blockPos.above(i3 + 1);
            if (!allNeighborsEmpty(levelAccessor, above, null)) {
                return;
            }
            levelAccessor.setBlock(above, chorusPlantBlock.getStateForPlacement(levelAccessor, above), 2);
            levelAccessor.setBlock(above.below(), chorusPlantBlock.getStateForPlacement(levelAccessor, above.below()), 2);
        }
        boolean z = false;
        if (i2 < 4) {
            int nextInt2 = random.nextInt(4);
            if (i2 == 0) {
                nextInt2++;
            }
            for (int i4 = 0; i4 < nextInt2; i4++) {
                Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos relative = blockPos.above(nextInt).relative(randomDirection);
                if (Math.abs(relative.getX() - blockPos2.getX()) < i && Math.abs(relative.getZ() - blockPos2.getZ()) < i && levelAccessor.isEmptyBlock(relative) && levelAccessor.isEmptyBlock(relative.below()) && allNeighborsEmpty(levelAccessor, relative, randomDirection.getOpposite())) {
                    z = true;
                    levelAccessor.setBlock(relative, chorusPlantBlock.getStateForPlacement(levelAccessor, relative), 2);
                    levelAccessor.setBlock(relative.relative(randomDirection.getOpposite()), chorusPlantBlock.getStateForPlacement(levelAccessor, relative.relative(randomDirection.getOpposite())), 2);
                    growTreeRecursive(levelAccessor, relative, random, blockPos2, i, i2 + 1);
                }
            }
        }
        if (!z) {
            levelAccessor.setBlock(blockPos.above(nextInt), (BlockState) Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, 5), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (projectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
            level.destroyBlock(blockHitResult.getBlockPos(), true, projectile);
        }
    }
}
