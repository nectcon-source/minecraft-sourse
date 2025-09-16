package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/WaterAvoidingRandomStrollGoal.class */
public class WaterAvoidingRandomStrollGoal extends RandomStrollGoal {
    protected final float probability;

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d) {
        this(pathfinderMob, d, 0.001f);
    }

    public WaterAvoidingRandomStrollGoal(PathfinderMob pathfinderMob, double d, float f) {
        super(pathfinderMob, d);
        this.probability = f;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.RandomStrollGoal
    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 landPos = RandomPos.getLandPos(this.mob, 15, 7);
            return landPos == null ? super.getPosition() : landPos;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return RandomPos.getLandPos(this.mob, 10, 7);
        }
        return super.getPosition();
    }
}
