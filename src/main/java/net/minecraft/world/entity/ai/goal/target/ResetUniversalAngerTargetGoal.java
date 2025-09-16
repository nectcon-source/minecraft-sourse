package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/ResetUniversalAngerTargetGoal.class */
public class ResetUniversalAngerTargetGoal<T extends Mob & NeutralMob> extends Goal {
    private final T mob;
    private final boolean alertOthersOfSameType;
    private int lastHurtByPlayerTimestamp;

    public ResetUniversalAngerTargetGoal(T t, boolean z) {
        this.mob = t;
        this.alertOthersOfSameType = z;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && wasHurtByPlayer();
    }

    private boolean wasHurtByPlayer() {
        return this.mob.getLastHurtByMob() != null && this.mob.getLastHurtByMob().getType() == EntityType.PLAYER && this.mob.getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.lastHurtByPlayerTimestamp = this.mob.getLastHurtByMobTimestamp();
        this.mob.forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.alertOthersOfSameType) {
            getNearbyMobsOfSameType().stream().filter(mob -> {
                return mob != this.mob;
            }).map(mob2 -> {
                return (NeutralMob) mob2;
            }).forEach((v0) -> {
                v0.forgetCurrentTargetAndRefreshUniversalAnger();
            });
        }
        super.start();
    }

    private List<Mob> getNearbyMobsOfSameType() {
        double attributeValue = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        return this.mob.level.getLoadedEntitiesOfClass(this.mob.getClass(), AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(attributeValue, 10.0d, attributeValue));
    }
}
