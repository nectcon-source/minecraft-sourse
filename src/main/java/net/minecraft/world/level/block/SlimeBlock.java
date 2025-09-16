package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SlimeBlock.class */
public class SlimeBlock extends HalfTransparentBlock {
    public SlimeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(level, blockPos, entity, f);
        } else {
            entity.causeFallDamage(f, 0.0f);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(blockGetter, entity);
        } else {
            bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (deltaMovement.y < 0.0d) {
            entity.setDeltaMovement(deltaMovement.x, (-deltaMovement.y) * (entity instanceof LivingEntity ? 1.0d : 0.8d), deltaMovement.z);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        double abs = Math.abs(entity.getDeltaMovement().y);
        if (abs < 0.1d && !entity.isSteppingCarefully()) {
            double d = 0.4d + (abs * 0.2d);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(d, 1.0d, d));
        }
        super.stepOn(level, blockPos, entity);
    }
}
