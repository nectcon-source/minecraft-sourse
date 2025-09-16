package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/TradeWithPlayerGoal.class */
public class TradeWithPlayerGoal extends Goal {
    private final AbstractVillager mob;

    public TradeWithPlayerGoal(AbstractVillager abstractVillager) {
        this.mob = abstractVillager;
        setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Player tradingPlayer;
        return (!this.mob.isAlive() || this.mob.isInWater() || !this.mob.isOnGround() || this.mob.hurtMarked || (tradingPlayer = this.mob.getTradingPlayer()) == null || this.mob.distanceToSqr(tradingPlayer) > 16.0d || tradingPlayer.containerMenu == null) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.mob.setTradingPlayer(null);
    }
}
