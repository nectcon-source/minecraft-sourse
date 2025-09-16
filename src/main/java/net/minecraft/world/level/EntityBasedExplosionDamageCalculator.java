package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/EntityBasedExplosionDamageCalculator.class */
public class EntityBasedExplosionDamageCalculator extends ExplosionDamageCalculator {
    private final Entity source;

    public EntityBasedExplosionDamageCalculator(Entity entity) {
        this.source = entity;
    }

    @Override // net.minecraft.world.level.ExplosionDamageCalculator
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState).map(f -> {
            return Float.valueOf(this.source.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState, f.floatValue()));
        });
    }

    @Override // net.minecraft.world.level.ExplosionDamageCalculator
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        return this.source.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
    }
}
