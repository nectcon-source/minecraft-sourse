package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/MoveBackToVillageGoal.class */
public class MoveBackToVillageGoal extends RandomStrollGoal {
    public MoveBackToVillageGoal(PathfinderMob pathfinderMob, double d, boolean z) {
        super(pathfinderMob, d, 10, z);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (((ServerLevel) this.mob.level).isVillage(this.mob.blockPosition())) {
            return false;
        }
        return super.canUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal
    @Nullable
    protected Vec3 getPosition() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level;
        SectionPos m36of = SectionPos.of(this.mob.blockPosition());
        SectionPos findSectionClosestToVillage = BehaviorUtils.findSectionClosestToVillage(serverLevel, m36of, 2);
        if (findSectionClosestToVillage != m36of) {
            return RandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(findSectionClosestToVillage.center()));
        }
        return null;
    }
}
