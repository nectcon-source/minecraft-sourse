package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/AbstractIllager.class */
public abstract class AbstractIllager extends Raider {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/AbstractIllager$IllagerArmPose.class */
    public enum IllagerArmPose {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING,
        NEUTRAL
    }

    protected AbstractIllager(EntityType<? extends AbstractIllager> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public MobType getMobType() {
        return MobType.ILLAGER;
    }

    public IllagerArmPose getArmPose() {
        return IllagerArmPose.CROSSED;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/AbstractIllager$RaiderOpenDoorGoal.class */
    public class RaiderOpenDoorGoal extends OpenDoorGoal {
        public RaiderOpenDoorGoal(Raider raider) {
            super(raider, false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }
}
