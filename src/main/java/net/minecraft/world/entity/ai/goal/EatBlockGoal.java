package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/EatBlockGoal.class */
public class EatBlockGoal extends Goal {
    private static final Predicate<BlockState> IS_TALL_GRASS = BlockStatePredicate.forBlock(Blocks.GRASS);
    private final Mob mob;
    private final Level level;
    private int eatAnimationTick;

    public EatBlockGoal(Mob mob) {
        this.mob = mob;
        this.level = mob.level;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            return false;
        }
        BlockPos blockPosition = this.mob.blockPosition();
        if (IS_TALL_GRASS.test(this.level.getBlockState(blockPosition)) || this.level.getBlockState(blockPosition.below()).is(Blocks.GRASS_BLOCK)) {
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.eatAnimationTick = 40;
        this.level.broadcastEntityEvent(this.mob, (byte) 10);
        this.mob.getNavigation().stop();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.eatAnimationTick = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.eatAnimationTick > 0;
    }

    public int getEatAnimationTick() {
        return this.eatAnimationTick;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick != 4) {
            return;
        }
        BlockPos blockPosition = this.mob.blockPosition();
        if (IS_TALL_GRASS.test(this.level.getBlockState(blockPosition))) {
            if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level.destroyBlock(blockPosition, false);
            }
            this.mob.ate();
            return;
        }
        BlockPos below = blockPosition.below();
        if (this.level.getBlockState(below).is(Blocks.GRASS_BLOCK)) {
            if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level.levelEvent(2001, below, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                this.level.setBlock(below, Blocks.DIRT.defaultBlockState(), 2);
            }
            this.mob.ate();
        }
    }
}
