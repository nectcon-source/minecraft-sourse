package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/LookAtTradingPlayerGoal.class */
public class LookAtTradingPlayerGoal extends LookAtPlayerGoal {
    private final AbstractVillager villager;

    public LookAtTradingPlayerGoal(AbstractVillager abstractVillager) {
        super(abstractVillager, Player.class, 8.0f);
        this.villager = abstractVillager;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.LookAtPlayerGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.villager.isTrading()) {
            this.lookAt = this.villager.getTradingPlayer();
            return true;
        }
        return false;
    }
}
