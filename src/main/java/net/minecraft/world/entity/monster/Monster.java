package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Monster.class */
public abstract class Monster extends PathfinderMob implements Enemy {
    protected Monster(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        updateSwingTime();
        updateNoActionTime();
        super.aiStep();
    }

    protected void updateNoActionTime() {
        if (getBrightness() > 0.5f) {
            this.noActionTime += 2;
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    @Override // net.minecraft.world.entity.Entity
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HOSTILE_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOSTILE_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getFallDamageSound(int i) {
        if (i > 4) {
            return SoundEvents.HOSTILE_BIG_FALL;
        }
        return SoundEvents.HOSTILE_SMALL_FALL;
    }

    @Override // net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.5f - levelReader.getBrightness(blockPos);
    }

    public static boolean isDarkEnoughToSpawn(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, Random random) {
        if (serverLevelAccessor.getBrightness(LightLayer.SKY, blockPos) > random.nextInt(32)) {
            return false;
        }
        return (serverLevelAccessor.getLevel().isThundering() ? serverLevelAccessor.getMaxLocalRawBrightness(blockPos, 10) : serverLevelAccessor.getMaxLocalRawBrightness(blockPos)) <= random.nextInt(8);
    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> entityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return serverLevelAccessor.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(serverLevelAccessor, blockPos, random) && checkMobSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, random);
    }

    public static boolean checkAnyLightMonsterSpawnRules(EntityType<? extends Monster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
    }

    public static AttributeSupplier.Builder createMonsterAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean shouldDropExperience() {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected boolean shouldDropLoot() {
        return true;
    }

    public boolean isPreventingPlayerRest(Player player) {
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ItemStack getProjectile(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ProjectileWeaponItem) {
            ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(this, ((ProjectileWeaponItem) itemStack.getItem()).getSupportedHeldProjectiles());
            return heldProjectile.isEmpty() ? new ItemStack(Items.ARROW) : heldProjectile;
        }
        return ItemStack.EMPTY;
    }
}
