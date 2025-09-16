package net.minecraft.world.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/BreakDoorGoal.class */
public class BreakDoorGoal extends DoorInteractGoal {
    private final Predicate<Difficulty> validDifficulties;
    protected int breakTime;
    protected int lastBreakProgress;
    protected int doorBreakTime;

    public BreakDoorGoal(Mob mob, Predicate<Difficulty> predicate) {
        super(mob);
        this.lastBreakProgress = -1;
        this.doorBreakTime = -1;
        this.validDifficulties = predicate;
    }

    public BreakDoorGoal(Mob mob, int i, Predicate<Difficulty> predicate) {
        this(mob, predicate);
        this.doorBreakTime = i;
    }

    protected int getDoorBreakTime() {
        return Math.max(240, this.doorBreakTime);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return super.canUse() && this.mob.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && isValidDifficulty(this.mob.level.getDifficulty()) && !isOpen();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        super.start();
        this.breakTime = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.breakTime <= getDoorBreakTime() && !isOpen() && this.doorPos.closerThan(this.mob.position(), 2.0d) && isValidDifficulty(this.mob.level.getDifficulty());
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        super.stop();
        this.mob.level.destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.DoorInteractGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        super.tick();
        if (this.mob.getRandom().nextInt(20) == 0) {
            this.mob.level.levelEvent(1019, this.doorPos, 0);
            if (!this.mob.swinging) {
                this.mob.swing(this.mob.getUsedItemHand());
            }
        }
        this.breakTime++;
        int doorBreakTime = (int) ((this.breakTime / getDoorBreakTime()) * 10.0f);
        if (doorBreakTime != this.lastBreakProgress) {
            this.mob.level.destroyBlockProgress(this.mob.getId(), this.doorPos, doorBreakTime);
            this.lastBreakProgress = doorBreakTime;
        }
        if (this.breakTime == getDoorBreakTime() && isValidDifficulty(this.mob.level.getDifficulty())) {
            this.mob.level.removeBlock(this.doorPos, false);
            this.mob.level.levelEvent(1021, this.doorPos, 0);
            this.mob.level.levelEvent(2001, this.doorPos, Block.getId(this.mob.level.getBlockState(this.doorPos)));
        }
    }

    private boolean isValidDifficulty(Difficulty difficulty) {
        return this.validDifficulties.test(difficulty);
    }
}
