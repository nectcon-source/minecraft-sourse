package net.minecraft.world.entity.ai.goal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RemoveBlockGoal.class */
public class RemoveBlockGoal extends MoveToBlockGoal {
    private final Block blockToRemove;
    private final Mob removerMob;
    private int ticksSinceReachedGoal;

    public RemoveBlockGoal(Block block, PathfinderMob pathfinderMob, double d, int i) {
        super(pathfinderMob, d, 24, i);
        this.blockToRemove = block;
        this.removerMob = pathfinderMob;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (!this.removerMob.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        }
        if (tryFindBlock()) {
            this.nextStartTick = 20;
            return true;
        }
        this.nextStartTick = nextStartTick(this.mob);
        return false;
    }

    private boolean tryFindBlock() {
        if (this.blockPos != null && isValidTarget(this.mob.level, this.blockPos)) {
            return true;
        }
        return findNearestBlock();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        super.stop();
        this.removerMob.fallDistance = 1.0f;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos blockPos) {
    }

    public void playBreakSound(Level level, BlockPos blockPos) {
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        super.tick();
        Level level = this.removerMob.level;
        BlockPos posWithBlock = getPosWithBlock(this.removerMob.blockPosition(), level);
        Random random = this.removerMob.getRandom();
        if (isReachedTarget() && posWithBlock != null) {
            if (this.ticksSinceReachedGoal > 0) {
                Vec3 deltaMovement = this.removerMob.getDeltaMovement();
                this.removerMob.setDeltaMovement(deltaMovement.x, 0.3d, deltaMovement.z);
                if (!level.isClientSide) {
                    ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)), posWithBlock.getX() + 0.5d, posWithBlock.getY() + 0.7d, posWithBlock.getZ() + 0.5d, 3, (random.nextFloat() - 0.5d) * 0.08d, (random.nextFloat() - 0.5d) * 0.08d, (random.nextFloat() - 0.5d) * 0.08d, 0.15000000596046448d);
                }
            }
            if (this.ticksSinceReachedGoal % 2 == 0) {
                Vec3 deltaMovement2 = this.removerMob.getDeltaMovement();
                this.removerMob.setDeltaMovement(deltaMovement2.x, -0.3d, deltaMovement2.z);
                if (this.ticksSinceReachedGoal % 6 == 0) {
                    playDestroyProgressSound(level, this.blockPos);
                }
            }
            if (this.ticksSinceReachedGoal > 60) {
                level.removeBlock(posWithBlock, false);
                if (!level.isClientSide) {
                    for (int i = 0; i < 20; i++) {
                        ((ServerLevel) level).sendParticles(ParticleTypes.POOF, posWithBlock.getX() + 0.5d, posWithBlock.getY(), posWithBlock.getZ() + 0.5d, 1, random.nextGaussian() * 0.02d, random.nextGaussian() * 0.02d, random.nextGaussian() * 0.02d, 0.15000000596046448d);
                    }
                    playBreakSound(level, posWithBlock);
                }
            }
            this.ticksSinceReachedGoal++;
        }
    }

    @Nullable
    private BlockPos getPosWithBlock(BlockPos blockPos, BlockGetter blockGetter) {
        if (blockGetter.getBlockState(blockPos).is(this.blockToRemove)) {
            return blockPos;
        }
        for (BlockPos blockPos2 : new BlockPos[]{blockPos.below(), blockPos.west(), blockPos.east(), blockPos.north(), blockPos.south(), blockPos.below().below()}) {
            if (blockGetter.getBlockState(blockPos2).is(this.blockToRemove)) {
                return blockPos2;
            }
        }
        return null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.MoveToBlockGoal
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        ChunkAccess chunk = levelReader.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk != null && chunk.getBlockState(blockPos).is(this.blockToRemove) && chunk.getBlockState(blockPos.above()).isAir() && chunk.getBlockState(blockPos.above(2)).isAir();
    }
}
