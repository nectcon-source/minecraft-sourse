package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/BegGoal.class */
public class BegGoal extends Goal {
    private final Wolf wolf;
    private Player player;
    private final Level level;
    private final float lookDistance;
    private int lookTime;
    private final TargetingConditions begTargeting;

    public BegGoal(Wolf wolf, float f) {
        this.wolf = wolf;
        this.level = wolf.level;
        this.lookDistance = f;
        this.begTargeting = new TargetingConditions().range(f).allowInvulnerable().allowSameTeam().allowNonAttackable();
        setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        this.player = this.level.getNearestPlayer(this.begTargeting, this.wolf);
        if (this.player == null) {
            return false;
        }
        return playerHoldingInteresting(this.player);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.player.isAlive() && this.wolf.distanceToSqr(this.player) <= ((double) (this.lookDistance * this.lookDistance)) && this.lookTime > 0 && playerHoldingInteresting(this.player);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.wolf.setIsInterested(true);
        this.lookTime = 40 + this.wolf.getRandom().nextInt(40);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.wolf.setIsInterested(false);
        this.player = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.wolf.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0f, this.wolf.getMaxHeadXRot());
        this.lookTime--;
    }

    private boolean playerHoldingInteresting(Player player) {
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if ((this.wolf.isTame() && itemInHand.getItem() == Items.BONE) || this.wolf.isFood(itemInHand)) {
                return true;
            }
        }
        return false;
    }
}
