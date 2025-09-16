package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/MoveToBlockGoal.class */
public abstract class MoveToBlockGoal extends Goal {
    protected final PathfinderMob mob;
    public final double speedModifier;
    protected int nextStartTick;
    protected int tryTicks;
    private int maxStayTicks;
    protected BlockPos blockPos;
    private boolean reachedTarget;
    private final int searchRange;
    private final int verticalSearchRange;
    protected int verticalSearchStart;

    protected abstract boolean isValidTarget(LevelReader levelReader, BlockPos blockPos);

    public MoveToBlockGoal(PathfinderMob pathfinderMob, double d, int i) {
        this(pathfinderMob, d, i, 1);
    }

    public MoveToBlockGoal(PathfinderMob pathfinderMob, double d, int i, int i2) {
        this.blockPos = BlockPos.ZERO;
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.searchRange = i;
        this.verticalSearchStart = 0;
        this.verticalSearchRange = i2;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        }
        this.nextStartTick = nextStartTick(this.mob);
        return findNearestBlock();
    }

    protected int nextStartTick(PathfinderMob pathfinderMob) {
        return 200 + pathfinderMob.getRandom().nextInt(200);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.tryTicks >= (-this.maxStayTicks) && this.tryTicks <= 1200 && isValidTarget(this.mob.level, this.blockPos);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        moveMobToBlock();
        this.tryTicks = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo(this.blockPos.getX() + 0.5d, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5d, this.speedModifier);
    }

    public double acceptedDistance() {
        return 1.0d;
    }

    protected BlockPos getMoveToTarget() {
        return this.blockPos.above();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        BlockPos var1 = this.getMoveToTarget();
        if (!var1.closerThan(this.mob.position(), this.acceptedDistance())) {
            this.reachedTarget = false;
            ++this.tryTicks;
            if (this.shouldRecalculatePath()) {
                this.mob.getNavigation().moveTo((double)((float)var1.getX()) + (double)0.5F, (double)var1.getY(), (double)((float)var1.getZ()) + (double)0.5F, this.speedModifier);
            }
        } else {
            this.reachedTarget = true;
            --this.tryTicks;
        }
    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    protected boolean isReachedTarget() {
        return this.reachedTarget;
    }


    protected boolean findNearestBlock() {
        int var1 = this.searchRange;
        int var2 = this.verticalSearchRange;
        BlockPos var3 = this.mob.blockPosition();
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = this.verticalSearchStart; var5 <= var2; var5 = var5 > 0 ? -var5 : 1 - var5) {
            for(int var6 = 0; var6 < var1; ++var6) {
                for(int var7 = 0; var7 <= var6; var7 = var7 > 0 ? -var7 : 1 - var7) {
                    for(int var8 = var7 < var6 && var7 > -var6 ? var6 : 0; var8 <= var6; var8 = var8 > 0 ? -var8 : 1 - var8) {
                        var4.setWithOffset(var3, var7, var5 - 1, var8);
                        if (this.mob.isWithinRestriction(var4) && this.isValidTarget(this.mob.level, var4)) {
                            this.blockPos = var4;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
