package net.minecraft.world.level.material;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/LavaFluid.class */
public abstract class LavaFluid extends FlowingFluid {
    @Override // net.minecraft.world.level.material.FlowingFluid
    public Fluid getFlowing() {
        return Fluids.FLOWING_LAVA;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public Fluid getSource() {
        return Fluids.LAVA;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public Item getBucket() {
        return Items.LAVA_BUCKET;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        BlockPos above = blockPos.above();
        if (level.getBlockState(above).isAir() && !level.getBlockState(above).isSolidRender(level, above)) {
            if (random.nextInt(100) == 0) {
                double x = blockPos.getX() + random.nextDouble();
                double y = blockPos.getY() + 1.0d;
                double z = blockPos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0d, 0.0d, 0.0d);
                level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2f + (random.nextFloat() * 0.2f), 0.9f + (random.nextFloat() * 0.15f), false);
            }
            if (random.nextInt(200) == 0) {
                level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2f + (random.nextFloat() * 0.2f), 0.9f + (random.nextFloat() * 0.15f), false);
            }
        }
    }

    @Override // net.minecraft.world.level.material.Fluid
    public void randomTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        int nextInt = random.nextInt(3);
        if (nextInt > 0) {
            BlockPos blockPos2 = blockPos;
            for (int i = 0; i < nextInt; i++) {
                blockPos2 = blockPos2.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                if (!level.isLoaded(blockPos2)) {
                    return;
                }
                BlockState blockState = level.getBlockState(blockPos2);
                if (blockState.isAir()) {
                    if (hasFlammableNeighbours(level, blockPos2)) {
                        level.setBlockAndUpdate(blockPos2, BaseFireBlock.getState(level, blockPos2));
                        return;
                    }
                } else if (blockState.getMaterial().blocksMotion()) {
                    return;
                }
            }
            return;
        }
        for (int i2 = 0; i2 < 3; i2++) {
            BlockPos offset = blockPos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (!level.isLoaded(offset)) {
                return;
            }
            if (level.isEmptyBlock(offset.above()) && isFlammable(level, offset)) {
                level.setBlockAndUpdate(offset.above(), BaseFireBlock.getState(level, offset));
            }
        }
    }

    private boolean hasFlammableNeighbours(LevelReader levelReader, BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (isFlammable(levelReader, blockPos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlammable(LevelReader levelReader, BlockPos blockPos) {
        if (blockPos.getY() >= 0 && blockPos.getY() < 256 && !levelReader.hasChunkAt(blockPos)) {
            return false;
        }
        return levelReader.getBlockState(blockPos).getMaterial().isFlammable();
    }

    @Override // net.minecraft.world.level.material.Fluid
    @Nullable
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_LAVA;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        fizz(levelAccessor, blockPos);
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public int getSlopeFindDistance(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 4 : 2;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public BlockState createLegacyBlock(FluidState fluidState) {
        return (BlockState) Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(fluidState)));
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean isSame(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public int getDropOff(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 1 : 2;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return fluidState.getHeight(blockGetter, blockPos) >= 0.44444445f && fluid.is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.level.material.Fluid
    public int getTickDelay(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 10 : 30;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        int tickDelay = getTickDelay(level);
        if (!fluidState.isEmpty() && !fluidState2.isEmpty() && !((Boolean) fluidState.getValue(FALLING)).booleanValue() && !((Boolean) fluidState2.getValue(FALLING)).booleanValue() && fluidState2.getHeight(level, blockPos) > fluidState.getHeight(level, blockPos) && level.getRandom().nextInt(4) != 0) {
            tickDelay *= 4;
        }
        return tickDelay;
    }

    private void fizz(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.levelEvent(1501, blockPos, 0);
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    protected boolean canConvertToSource() {
        return false;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState fluidState2 = levelAccessor.getFluidState(blockPos);
            if (is(FluidTags.LAVA) && fluidState2.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    levelAccessor.setBlock(blockPos, Blocks.STONE.defaultBlockState(), 3);
                }
                fizz(levelAccessor, blockPos);
                return;
            }
        }
        super.spreadTo(levelAccessor, blockPos, blockState, direction, fluidState);
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected float getExplosionResistance() {
        return 100.0f;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/LavaFluid$Source.class */
    public static class Source extends LavaFluid {
        @Override // net.minecraft.world.level.material.Fluid
        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override // net.minecraft.world.level.material.Fluid
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/LavaFluid$Flowing.class */
    public static class Flowing extends LavaFluid {
        @Override // net.minecraft.world.level.material.FlowingFluid, net.minecraft.world.level.material.Fluid
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override // net.minecraft.world.level.material.Fluid
        public int getAmount(FluidState fluidState) {
            return  fluidState.getValue(LEVEL).intValue();
        }

        @Override // net.minecraft.world.level.material.Fluid
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }
}
