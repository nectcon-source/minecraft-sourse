package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/DefendVillageTargetGoal.class */
public class DefendVillageTargetGoal extends TargetGoal {
    private final IronGolem golem;
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting;

    public DefendVillageTargetGoal(IronGolem ironGolem) {
        super(ironGolem, false, true);
        this.attackTargeting = new TargetingConditions().range(64.0d);
        this.golem = ironGolem;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        AABB inflate = this.golem.getBoundingBox().inflate(10.0d, 8.0d, 10.0d);
        List<LivingEntity> nearbyEntities = this.golem.level.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, inflate);
        List<Player> nearbyPlayers = this.golem.level.getNearbyPlayers(this.attackTargeting, this.golem, inflate);
        Iterator<LivingEntity> it = nearbyEntities.iterator();
        while (it.hasNext()) {
            Villager villager = (Villager) it.next();
            for (Player player : nearbyPlayers) {
                if (villager.getPlayerReputation(player) <= -100) {
                    this.potentialTarget = player;
                }
            }
        }
        if (this.potentialTarget == null) {
            return false;
        }
        if (!(this.potentialTarget instanceof Player)) {
            return true;
        }
        if (this.potentialTarget.isSpectator() || ((Player) this.potentialTarget).isCreative()) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.golem.setTarget(this.potentialTarget);
        super.start();
    }
}
