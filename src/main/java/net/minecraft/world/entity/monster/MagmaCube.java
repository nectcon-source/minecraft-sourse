package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/MagmaCube.class */
public class MagmaCube extends Slime {
    public MagmaCube(EntityType<? extends MagmaCube> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.20000000298023224d);
    }

    public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this) && !levelReader.containsAnyLiquid(getBoundingBox());
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected void setSize(int i, boolean z) {
        super.setSize(i, z);
        getAttribute(Attributes.ARMOR).setBaseValue(i * 3);
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected ParticleOptions getParticleType() {
        return ParticleTypes.FLAME;
    }

    @Override // net.minecraft.world.entity.monster.Slime, net.minecraft.world.entity.Mob
    protected ResourceLocation getDefaultLootTable() {
        return isTiny() ? BuiltInLootTables.EMPTY : getType().getDefaultLootTable();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isOnFire() {
        return false;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected void decreaseSquish() {
        this.targetSquish *= 0.9f;
    }

    @Override // net.minecraft.world.entity.monster.Slime, net.minecraft.world.entity.LivingEntity
    protected void jumpFromGround() {
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x, getJumpPower() + (getSize() * 0.1f), deltaMovement.z);
        this.hasImpulse = true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    protected void jumpInLiquid(Tag<Fluid> tag) {
        if (tag == FluidTags.LAVA) {
            Vec3 deltaMovement = getDeltaMovement();
            setDeltaMovement(deltaMovement.x, 0.22f + (getSize() * 0.05f), deltaMovement.z);
            this.hasImpulse = true;
            return;
        }
        super.jumpInLiquid(tag);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        return false;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected float getAttackDamage() {
        return super.getAttackDamage() + 2.0f;
    }

    @Override // net.minecraft.world.entity.monster.Slime, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (isTiny()) {
            return SoundEvents.MAGMA_CUBE_HURT_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Slime, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        if (isTiny()) {
            return SoundEvents.MAGMA_CUBE_DEATH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected SoundEvent getSquishSound() {
        if (isTiny()) {
            return SoundEvents.MAGMA_CUBE_SQUISH_SMALL;
        }
        return SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override // net.minecraft.world.entity.monster.Slime
    protected SoundEvent getJumpSound() {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }
}
