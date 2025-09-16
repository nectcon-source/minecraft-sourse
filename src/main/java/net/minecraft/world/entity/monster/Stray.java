package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Stray.class */
public class Stray extends AbstractSkeleton {
    public Stray(EntityType<? extends Stray> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkStraySpawnRules(EntityType<Stray> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return checkMonsterSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, random) && (mobSpawnType == MobSpawnType.SPAWNER || serverLevelAccessor.canSeeSky(blockPos));
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRAY_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton
    SoundEvent getStepSound() {
        return SoundEvents.STRAY_STEP;
    }

    @Override // net.minecraft.world.entity.monster.AbstractSkeleton
    protected AbstractArrow getArrow(ItemStack itemStack, float f) {
        AbstractArrow arrow = super.getArrow(itemStack, f);
        if (arrow instanceof Arrow) {
            ((Arrow) arrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
        }
        return arrow;
    }
}
