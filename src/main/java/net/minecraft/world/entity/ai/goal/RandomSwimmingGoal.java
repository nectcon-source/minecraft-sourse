package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RandomSwimmingGoal.class */
public class RandomSwimmingGoal extends RandomStrollGoal {
    public RandomSwimmingGoal(PathfinderMob pathfinderMob, double d, int i) {
        super(pathfinderMob, d, i);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal
    @Nullable
    protected Vec3 getPosition() {
        Vec3 pos = RandomPos.getPos(this.mob, 10, 7);
        int i = 0;
        while (pos != null && !this.mob.level.getBlockState(new BlockPos(pos)).isPathfindable(this.mob.level, new BlockPos(pos), PathComputationType.WATER)) {
            int i2 = i;
            i++;
            if (i2 >= 10) {
                break;
            }
            pos = RandomPos.getPos(this.mob, 10, 7);
        }
        return pos;
    }
}
