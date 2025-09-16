package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/HoneyBlock.class */
public class HoneyBlock extends HalfTransparentBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 15.0d, 15.0d);

    public HoneyBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static boolean doesEntityDoHoneyBlockSlideEffects(Entity entity) {
        return (entity instanceof LivingEntity) || (entity instanceof AbstractMinecart) || (entity instanceof PrimedTnt) || (entity instanceof Boat);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0f, 1.0f);
        if (!level.isClientSide) {
            level.broadcastEntityEvent(entity, (byte) 54);
        }
        if (entity.causeFallDamage(f, 0.2f)) {
            entity.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5f, this.soundType.getPitch() * 0.75f);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (isSlidingDown(blockPos, entity)) {
            maybeDoSlideAchievement(entity, blockPos);
            doSlideMovement(entity);
            maybeDoSlideEffects(level, entity);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }

    private boolean isSlidingDown(BlockPos blockPos, Entity entity) {
        if (entity.isOnGround() || entity.getY() > (blockPos.getY() + 0.9375d) - 1.0E-7d || entity.getDeltaMovement().y >= -0.08d) {
            return false;
        }
        double bbWidth = 0.4375d + (entity.getBbWidth() / 2.0f);
        return Math.abs((blockPos.getX() + 0.5d) - entity.getX()) + 1.0E-7d > bbWidth || Math.abs((blockPos.getZ() + 0.5d) - entity.getZ()) + 1.0E-7d > bbWidth;
    }

    private void maybeDoSlideAchievement(Entity entity, BlockPos blockPos) {
        if ((entity instanceof ServerPlayer) && entity.level.getGameTime() % 20 == 0) {
            CriteriaTriggers.HONEY_BLOCK_SLIDE.trigger((ServerPlayer) entity, entity.level.getBlockState(blockPos));
        }
    }

    private void doSlideMovement(Entity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (deltaMovement.y < -0.13d) {
            double d = (-0.05d) / deltaMovement.y;
            entity.setDeltaMovement(new Vec3(deltaMovement.x * d, -0.05d, deltaMovement.z * d));
        } else {
            entity.setDeltaMovement(new Vec3(deltaMovement.x, -0.05d, deltaMovement.z));
        }
        entity.fallDistance = 0.0f;
    }

    private void maybeDoSlideEffects(Level level, Entity entity) {
        if (doesEntityDoHoneyBlockSlideEffects(entity)) {
            if (level.random.nextInt(5) == 0) {
                entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0f, 1.0f);
            }
            if (!level.isClientSide && level.random.nextInt(5) == 0) {
                level.broadcastEntityEvent(entity, (byte) 53);
            }
        }
    }

    public static void showSlideParticles(Entity entity) {
        showParticles(entity, 5);
    }

    public static void showJumpParticles(Entity entity) {
        showParticles(entity, 10);
    }

    private static void showParticles(Entity entity, int i) {
        if (!entity.level.isClientSide) {
            return;
        }
        BlockState defaultBlockState = Blocks.HONEY_BLOCK.defaultBlockState();
        for (int i2 = 0; i2 < i; i2++) {
            entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState), entity.getX(), entity.getY(), entity.getZ(), 0.0d, 0.0d, 0.0d);
        }
    }
}
