package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/SnowGolem.class */
public class SnowGolem extends AbstractGolem implements Shearable, RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);

    public SnowGolem(EntityType<? extends SnowGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25d, 20, 10.0f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0d, 1.0000001E-5f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Mob.class, 10, true, false, livingEntity -> {
            return livingEntity instanceof Enemy;
        }));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0d).add(Attributes.MOVEMENT_SPEED, 0.20000000298023224d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PUMPKIN_ID, (byte) 16);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Pumpkin", hasPumpkin());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Pumpkin")) {
            setPumpkin(compoundTag.getBoolean("Pumpkin"));
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            int floor = Mth.floor(getX());
            int floor2 = Mth.floor(getY());
            int floor3 = Mth.floor(getZ());
            if (this.level.getBiome(new BlockPos(floor, 0, floor3)).getTemperature(new BlockPos(floor, floor2, floor3)) > 1.0f) {
                hurt(DamageSource.ON_FIRE, 1.0f);
            }
            if (!this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return;
            }
            BlockState defaultBlockState = Blocks.SNOW.defaultBlockState();
            for (int i = 0; i < 4; i++) {
                BlockPos blockPos = new BlockPos(Mth.floor(getX() + ((((i % 2) * 2) - 1) * 0.25f)), Mth.floor(getY()), Mth.floor(getZ() + (((((i / 2) % 2) * 2) - 1) * 0.25f)));
                if (this.level.getBlockState(blockPos).isAir() && this.level.getBiome(blockPos).getTemperature(blockPos) < 0.8f && defaultBlockState.canSurvive(this.level, blockPos)) {
                    this.level.setBlockAndUpdate(blockPos, defaultBlockState);
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        Snowball var3 = new Snowball(this.level, this);
        double var4x = livingEntity.getEyeY() - 1.1F;
        double var6xx = livingEntity.getX() - this.getX();
        double var8xxx = var4x - var3.getY();
        double var10xxxx = livingEntity.getZ() - this.getZ();
        float var12xxxxx = Mth.sqrt(var6xx * var6xx + var10xxxx * var10xxxx) * 0.2F;
        var3.shoot(var6xx, var8xxx + (double)var12xxxxx, var10xxxx, 1.6F, 12.0F);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var3);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 1.7f;
    }

    @Override // net.minecraft.world.entity.Mob
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.SHEARS && readyForShearing()) {
            shear(SoundSource.PLAYERS);
            if (!this.level.isClientSide) {
                itemInHand.hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.entity.Shearable
    public void shear(SoundSource soundSource) {
        this.level.playSound((Player) null, this, SoundEvents.SNOW_GOLEM_SHEAR, soundSource, 1.0f, 1.0f);
        if (!this.level.isClientSide()) {
            setPumpkin(false);
            spawnAtLocation(new ItemStack(Items.CARVED_PUMPKIN), 1.7f);
        }
    }

    @Override // net.minecraft.world.entity.Shearable
    public boolean readyForShearing() {
        return isAlive() && hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (((Byte) this.entityData.get(DATA_PUMPKIN_ID)).byteValue() & 16) != 0;
    }

    public void setPumpkin(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_PUMPKIN_ID)).byteValue();
        if (z) {
            this.entityData.set(DATA_PUMPKIN_ID, Byte.valueOf((byte) (byteValue | 16)));
        } else {
            this.entityData.set(DATA_PUMPKIN_ID, Byte.valueOf((byte) (byteValue & (-17))));
        }
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.Mob
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLeashOffset() {
        return new Vec3(0.0d, 0.75f * getEyeHeight(), getBbWidth() * 0.4f);
    }
}
