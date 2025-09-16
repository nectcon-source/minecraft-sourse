package net.minecraft.world.entity.monster.piglin;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/AbstractPiglin.class */
public abstract class AbstractPiglin extends Monster {
    protected static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(AbstractPiglin.class, EntityDataSerializers.BOOLEAN);
    protected int timeInOverworld;

    protected abstract boolean canHunt();

    public abstract PiglinArmPose getArmPose();

    protected abstract void playConvertedSound();

    public AbstractPiglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
        this.timeInOverworld = 0;
        setCanPickUpLoot(true);
        applyOpenDoorsAbility();
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0f);
    }

    private void applyOpenDoorsAbility() {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        }
    }

    public void setImmuneToZombification(boolean z) {
        getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, Boolean.valueOf(z));
    }

    protected boolean isImmuneToZombification() {
        return ((Boolean) getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION)).booleanValue();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (isImmuneToZombification()) {
            compoundTag.putBoolean("IsImmuneToZombification", true);
        }
        compoundTag.putInt("TimeInOverworld", this.timeInOverworld);
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return isBaby() ? -0.05d : -0.45d;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setImmuneToZombification(compoundTag.getBoolean("IsImmuneToZombification"));
        this.timeInOverworld = compoundTag.getInt("TimeInOverworld");
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isConverting()) {
            this.timeInOverworld++;
        } else {
            this.timeInOverworld = 0;
        }
        if (this.timeInOverworld > 300) {
            playConvertedSound();
            finishConversion((ServerLevel) this.level);
        }
    }

    public boolean isConverting() {
        return (this.level.dimensionType().piglinSafe() || isImmuneToZombification() || isNoAi()) ? false : true;
    }

    protected void finishConversion(ServerLevel serverLevel) {
        ZombifiedPiglin zombifiedPiglin = (ZombifiedPiglin) convertTo(EntityType.ZOMBIFIED_PIGLIN, true);
        if (zombifiedPiglin != null) {
            zombifiedPiglin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }
    }

    public boolean isAdult() {
        return !isBaby();
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public LivingEntity getTarget() {
        return (LivingEntity) this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    protected boolean isHoldingMeleeWeapon() {
        return getMainHandItem().getItem() instanceof TieredItem;
    }

    @Override // net.minecraft.world.entity.Mob
    public void playAmbientSound() {
        if (PiglinAi.isIdle(this)) {
            super.playAmbientSound();
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }
}
