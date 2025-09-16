package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ThrownPotion.class */
public class ThrownPotion extends ThrowableItemProjectile implements ItemSupplier {
    public static final Predicate<LivingEntity> WATER_SENSITIVE = (v0) -> {
        return v0.isSensitiveToWater();
    };

    public ThrownPotion(EntityType<? extends ThrownPotion> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownPotion(Level level, LivingEntity livingEntity) {
        super(EntityType.POTION, livingEntity, level);
    }

    public ThrownPotion(Level level, double d, double d2, double d3) {
        super(EntityType.POTION, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableItemProjectile
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override // net.minecraft.world.entity.projectile.ThrowableProjectile
    protected float getGravity() {
        return 0.05f;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level.isClientSide) {
            return;
        }
        ItemStack item = getItem();
        boolean z = PotionUtils.getPotion(item) == Potions.WATER && PotionUtils.getMobEffects(item).isEmpty();
        Direction direction = blockHitResult.getDirection();
        BlockPos relative = blockHitResult.getBlockPos().relative(direction);
        if (z) {
            dowseFire(relative, direction);
            dowseFire(relative.relative(direction.getOpposite()), direction);
            Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
            while (it.hasNext()) {
                Direction next = it.next();
                dowseFire(relative.relative(next), next);
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level.isClientSide) {
            return;
        }
        ItemStack item = getItem();
        Potion potion = PotionUtils.getPotion(item);
        List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(item);
        if (potion == Potions.WATER && mobEffects.isEmpty()) {
            applyWater();
        } else if (!mobEffects.isEmpty()) {
            if (isLingering()) {
                makeAreaOfEffectCloud(item, potion);
            } else {
                applySplash(mobEffects, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) hitResult).getEntity() : null);
            }
        }
        this.level.levelEvent(potion.hasInstantEffects() ? 2007 : 2002, blockPosition(), PotionUtils.getColor(item));
        remove();
    }

    private void applyWater() {
        List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0d, 2.0d, 4.0d), WATER_SENSITIVE);
        if (!entitiesOfClass.isEmpty()) {
            for (LivingEntity livingEntity : entitiesOfClass) {
                if (distanceToSqr(livingEntity) < 16.0d && livingEntity.isSensitiveToWater()) {
                    livingEntity.hurt(DamageSource.indirectMagic(livingEntity, getOwner()), 1.0f);
                }
            }
        }
    }

    private void applySplash(List<MobEffectInstance> list, @Nullable Entity entity) {
        List<LivingEntity> entitiesOfClass = this.level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0d, 2.0d, 4.0d));
        if (!entitiesOfClass.isEmpty()) {
            for (LivingEntity livingEntity : entitiesOfClass) {
                if (livingEntity.isAffectedByPotions()) {
                    double distanceToSqr = distanceToSqr(livingEntity);
                    if (distanceToSqr < 16.0d) {
                        double sqrt = 1.0d - (Math.sqrt(distanceToSqr) / 4.0d);
                        if (livingEntity == entity) {
                            sqrt = 1.0d;
                        }
                        for (MobEffectInstance mobEffectInstance : list) {
                            MobEffect effect = mobEffectInstance.getEffect();
                            if (effect.isInstantenous()) {
                                effect.applyInstantenousEffect(this, getOwner(), livingEntity, mobEffectInstance.getAmplifier(), sqrt);
                            } else {
                                int duration = (int) ((sqrt * mobEffectInstance.getDuration()) + 0.5d);
                                if (duration > 20) {
                                    livingEntity.addEffect(new MobEffectInstance(effect, duration, mobEffectInstance.getAmplifier(), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void makeAreaOfEffectCloud(ItemStack itemStack, Potion potion) {
        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level, getX(), getY(), getZ());
        Entity owner = getOwner();
        if (owner instanceof LivingEntity) {
            areaEffectCloud.setOwner((LivingEntity) owner);
        }
        areaEffectCloud.setRadius(3.0f);
        areaEffectCloud.setRadiusOnUse(-0.5f);
        areaEffectCloud.setWaitTime(10);
        areaEffectCloud.setRadiusPerTick((-areaEffectCloud.getRadius()) / areaEffectCloud.getDuration());
        areaEffectCloud.setPotion(potion);
        Iterator<MobEffectInstance> it = PotionUtils.getCustomEffects(itemStack).iterator();
        while (it.hasNext()) {
            areaEffectCloud.addEffect(new MobEffectInstance(it.next()));
        }
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", 99)) {
            areaEffectCloud.setFixedColor(tag.getInt("CustomPotionColor"));
        }
        this.level.addFreshEntity(areaEffectCloud);
    }

    private boolean isLingering() {
        return getItem().getItem() == Items.LINGERING_POTION;
    }

    private void dowseFire(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.is(BlockTags.FIRE)) {
            this.level.removeBlock(blockPos, false);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.level.levelEvent(null, 1009, blockPos, 0);
            CampfireBlock.dowse(this.level, blockPos, blockState);
            this.level.setBlockAndUpdate(blockPos, (BlockState) blockState.setValue(CampfireBlock.LIT, false));
        }
    }
}
