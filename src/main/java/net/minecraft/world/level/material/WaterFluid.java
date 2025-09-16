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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/WaterFluid.class */
public abstract class WaterFluid extends FlowingFluid {
    @Override // net.minecraft.world.level.material.FlowingFluid
    public Fluid getFlowing() {
        return Fluids.FLOWING_WATER;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public Fluid getSource() {
        return Fluids.WATER;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public Item getBucket() {
        return Items.WATER_BUCKET;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
        if (!fluidState.isSource() && !((Boolean) fluidState.getValue(FALLING)).booleanValue()) {
            if (random.nextInt(64) == 0) {
                level.playLocalSound(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, (random.nextFloat() * 0.25f) + 0.75f, random.nextFloat() + 0.5f, false);
            }
        } else if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.UNDERWATER, blockPos.getX() + random.nextDouble(), blockPos.getY() + random.nextDouble(), blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
        }
    }

    @Override // net.minecraft.world.level.material.Fluid
    @Nullable
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_WATER;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    protected boolean canConvertToSource() {
        return true;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        Block.dropResources(blockState, levelAccessor, blockPos, blockState.getBlock().isEntityBlock() ? levelAccessor.getBlockEntity(blockPos) : null);
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public int getSlopeFindDistance(LevelReader levelReader) {
        return 4;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public BlockState createLegacyBlock(FluidState fluidState) {
        return (BlockState) Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(fluidState)));
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean isSame(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    @Override // net.minecraft.world.level.material.FlowingFluid
    public int getDropOff(LevelReader levelReader) {
        return 1;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public int getTickDelay(LevelReader levelReader) {
        return 5;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected float getExplosionResistance() {
        return 100.0f;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/WaterFluid$Source.class */
    public static class Source extends WaterFluid {
        @Override // net.minecraft.world.level.material.Fluid
        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override // net.minecraft.world.level.material.Fluid
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/WaterFluid$Flowing.class */
    public static class Flowing extends WaterFluid {
        @Override // net.minecraft.world.level.material.FlowingFluid, net.minecraft.world.level.material.Fluid
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override // net.minecraft.world.level.material.Fluid
        public int getAmount(FluidState fluidState) {
            return ((Integer) fluidState.getValue(LEVEL)).intValue();
        }

        @Override // net.minecraft.world.level.material.Fluid
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }
}
