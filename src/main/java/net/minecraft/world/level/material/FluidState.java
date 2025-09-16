package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/FluidState.class */
public final class FluidState extends StateHolder<Fluid, FluidState> {
    public static final Codec<FluidState> CODEC = codec(Registry.FLUID, (v0) -> {
        return v0.defaultFluidState();
    }).stable();

    public FluidState(Fluid fluid, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<FluidState> mapCodec) {
        super(fluid, immutableMap, mapCodec);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Fluid getType() {
        return  this.owner;
    }

    public boolean isSource() {
        return getType().isSource(this);
    }

    public boolean isEmpty() {
        return getType().isEmpty();
    }

    public float getHeight(BlockGetter blockGetter, BlockPos blockPos) {
        return getType().getHeight(this, blockGetter, blockPos);
    }

    public float getOwnHeight() {
        return getType().getOwnHeight(this);
    }

    public int getAmount() {
        return getType().getAmount(this);
    }

    public boolean shouldRenderBackwardUpFace(BlockGetter blockGetter, BlockPos blockPos) {
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                BlockPos offset = blockPos.offset(i, 0, i2);
                if (!blockGetter.getFluidState(offset).getType().isSame(getType()) && !blockGetter.getBlockState(offset).isSolidRender(blockGetter, offset)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void tick(Level level, BlockPos blockPos) {
        getType().tick(level, blockPos, this);
    }

    public void animateTick(Level level, BlockPos blockPos, Random random) {
        getType().animateTick(level, blockPos, this, random);
    }

    public boolean isRandomlyTicking() {
        return getType().isRandomlyTicking();
    }

    public void randomTick(Level level, BlockPos blockPos, Random random) {
        getType().randomTick(level, blockPos, this, random);
    }

    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos) {
        return getType().getFlow(blockGetter, blockPos, this);
    }

    public BlockState createLegacyBlock() {
        return getType().createLegacyBlock(this);
    }

    @Nullable
    public ParticleOptions getDripParticle() {
        return getType().getDripParticle();
    }

    /* renamed from: is */
    public boolean is(Tag<Fluid> tag) {
        return getType().is(tag);
    }

    public float getExplosionResistance() {
        return getType().getExplosionResistance();
    }

    public boolean canBeReplacedWith(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return getType().canBeReplacedWith(this, blockGetter, blockPos, fluid, direction);
    }

    public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
        return getType().getShape(this, blockGetter, blockPos);
    }
}
