package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WitherRoseBlock.class */
public class WitherRoseBlock extends FlowerBlock {
    public WitherRoseBlock(MobEffect mobEffect, BlockBehaviour.Properties properties) {
        super(mobEffect, 8, properties);
    }

    @Override // net.minecraft.world.level.block.BushBlock
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return super.mayPlaceOn(blockState, blockGetter, blockPos) || blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.SOUL_SAND) || blockState.is(Blocks.SOUL_SOIL);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        Vec3 center = getShape(blockState, level, blockPos, CollisionContext.empty()).bounds().getCenter();
        double x = blockPos.getX() + center.x;
        double z = blockPos.getZ() + center.z;
        for (int i = 0; i < 3; i++) {
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() / 5.0d), blockPos.getY() + (0.5d - random.nextDouble()), z + (random.nextDouble() / 5.0d), 0.0d, 0.0d, 0.0d);
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!level.isClientSide && level.getDifficulty() != Difficulty.PEACEFUL && (entity instanceof LivingEntity)) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (!livingEntity.isInvulnerableTo(DamageSource.WITHER)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
            }
        }
    }
}
