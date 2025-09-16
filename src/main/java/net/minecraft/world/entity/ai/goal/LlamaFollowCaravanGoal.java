package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/LlamaFollowCaravanGoal.class */
public class LlamaFollowCaravanGoal extends Goal {
    public final Llama llama;
    private double speedModifier;
    private int distCheckCounter;

    public LlamaFollowCaravanGoal(Llama llama, double d) {
        this.llama = llama;
        this.speedModifier = d;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.llama.isLeashed() || this.llama.inCaravan()) {
            return false;
        }
        List<Entity> entities = this.llama.level.getEntities(this.llama, this.llama.getBoundingBox().inflate(9.0d, 4.0d, 9.0d), entity -> {
            EntityType<?> type = entity.getType();
            return type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA;
        });
        Llama llama = null;
        double d = Double.MAX_VALUE;
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Llama llama2 = (Llama) it.next();
            if (llama2.inCaravan() && !llama2.hasCaravanTail()) {
                double distanceToSqr = this.llama.distanceToSqr(llama2);
                if (distanceToSqr <= d) {
                    d = distanceToSqr;
                    llama = llama2;
                }
            }
        }
        if (llama == null) {
            Iterator<Entity> it2 = entities.iterator();
            while (it2.hasNext()) {
                Llama llama3 = (Llama) it2.next();
                if (llama3.isLeashed() && !llama3.hasCaravanTail()) {
                    double distanceToSqr2 = this.llama.distanceToSqr(llama3);
                    if (distanceToSqr2 <= d) {
                        d = distanceToSqr2;
                        llama = llama3;
                    }
                }
            }
        }
        if (llama == null || d < 4.0d) {
            return false;
        }
        if (!llama.isLeashed() && !firstIsLeashed(llama, 1)) {
            return false;
        }
        this.llama.joinCaravan(llama);
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (!this.llama.inCaravan() || !this.llama.getCaravanHead().isAlive() || !firstIsLeashed(this.llama, 0)) {
            return false;
        }
        if (this.llama.distanceToSqr(this.llama.getCaravanHead()) > 676.0d) {
            if (this.speedModifier <= 3.0d) {
                this.speedModifier *= 1.2d;
                this.distCheckCounter = 40;
                return true;
            }
            if (this.distCheckCounter == 0) {
                return false;
            }
        }
        if (this.distCheckCounter > 0) {
            this.distCheckCounter--;
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.llama.leaveCaravan();
        this.speedModifier = 2.1d;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (!this.llama.inCaravan() || (this.llama.getLeashHolder() instanceof LeashFenceKnotEntity)) {
            return;
        }
        Llama caravanHead = this.llama.getCaravanHead();
        Vec3 scale = new Vec3(caravanHead.getX() - this.llama.getX(), caravanHead.getY() - this.llama.getY(), caravanHead.getZ() - this.llama.getZ()).normalize().scale(Math.max(this.llama.distanceTo(caravanHead) - 2.0d, 0.0d));
        this.llama.getNavigation().moveTo(this.llama.getX() + scale.x, this.llama.getY() + scale.y, this.llama.getZ() + scale.z, this.speedModifier);
    }

    private boolean firstIsLeashed(Llama llama, int i) {
        if (i <= 8 && llama.inCaravan()) {
            if (llama.getCaravanHead().isLeashed()) {
                return true;
            }
            return firstIsLeashed(llama.getCaravanHead(), i + 1);
        }
        return false;
    }
}
