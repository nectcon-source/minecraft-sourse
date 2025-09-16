package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/navigation/WallClimberNavigation.class */
public class WallClimberNavigation extends GroundPathNavigation {
    private BlockPos pathToPosition;

    public WallClimberNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.GroundPathNavigation, net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public Path createPath(BlockPos blockPos, int i) {
        this.pathToPosition = blockPos;
        return super.createPath(blockPos, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.GroundPathNavigation, net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public Path createPath(Entity entity, int i) {
        this.pathToPosition = entity.blockPosition();
        return super.createPath(entity, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public boolean moveTo(Entity entity, double d) {
        Path createPath = createPath(entity, 0);
        if (createPath != null) {
            return moveTo(createPath, d);
        }
        this.pathToPosition = entity.blockPosition();
        this.speedModifier = d;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public void tick() {
        if (isDone()) {
            if (this.pathToPosition != null) {
                if (this.pathToPosition.closerThan(this.mob.position(), this.mob.getBbWidth()) || (this.mob.getY() > this.pathToPosition.getY() && new BlockPos(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ()).closerThan(this.mob.position(), this.mob.getBbWidth()))) {
                    this.pathToPosition = null;
                    return;
                } else {
                    this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
                    return;
                }
            }
            return;
        }
        super.tick();
    }
}
