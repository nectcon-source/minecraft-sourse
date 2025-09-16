package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/piston/PistonStructureResolver.class */
public class PistonStructureResolver {
    private final Level level;
    private final BlockPos pistonPos;
    private final boolean extending;
    private final BlockPos startPos;
    private final Direction pushDirection;
    private final List<BlockPos> toPush = Lists.newArrayList();
    private final List<BlockPos> toDestroy = Lists.newArrayList();
    private final Direction pistonDirection;

    public PistonStructureResolver(Level level, BlockPos blockPos, Direction direction, boolean z) {
        this.level = level;
        this.pistonPos = blockPos;
        this.pistonDirection = direction;
        this.extending = z;
        if (z) {
            this.pushDirection = direction;
            this.startPos = blockPos.relative(direction);
        } else {
            this.pushDirection = direction.getOpposite();
            this.startPos = blockPos.relative(direction, 2);
        }
    }

    public boolean resolve() {
        this.toPush.clear();
        this.toDestroy.clear();
        BlockState blockState = this.level.getBlockState(this.startPos);
        if (!PistonBaseBlock.isPushable(blockState, this.level, this.startPos, this.pushDirection, false, this.pistonDirection)) {
            if (this.extending && blockState.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(this.startPos);
                return true;
            }
            return false;
        }
        if (!addBlockLine(this.startPos, this.pushDirection)) {
            return false;
        }
        for (int i = 0; i < this.toPush.size(); i++) {
            BlockPos blockPos = this.toPush.get(i);
            if (isSticky(this.level.getBlockState(blockPos).getBlock()) && !addBranchingBlocks(blockPos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSticky(Block block) {
        return block == Blocks.SLIME_BLOCK || block == Blocks.HONEY_BLOCK;
    }

    private static boolean canStickToEachOther(Block block, Block block2) {
        if (block == Blocks.HONEY_BLOCK && block2 == Blocks.SLIME_BLOCK) {
            return false;
        }
        if (block == Blocks.SLIME_BLOCK && block2 == Blocks.HONEY_BLOCK) {
            return false;
        }
        return isSticky(block) || isSticky(block2);
    }

    private boolean addBlockLine(BlockPos blockPos, Direction direction) {
        BlockState blockState = this.level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (blockState.isAir() || !PistonBaseBlock.isPushable(blockState, this.level, blockPos, this.pushDirection, false, direction) || blockPos.equals(this.pistonPos) || this.toPush.contains(blockPos)) {
            return true;
        }
        int i = 1;
        if (1 + this.toPush.size() > 12) {
            return false;
        }
        while (isSticky(block)) {
            BlockPos relative = blockPos.relative(this.pushDirection.getOpposite(), i);
            Block block2 = block;
            BlockState blockState2 = this.level.getBlockState(relative);
            block = blockState2.getBlock();
            if (blockState2.isAir() || !canStickToEachOther(block2, block) || !PistonBaseBlock.isPushable(blockState2, this.level, relative, this.pushDirection, false, this.pushDirection.getOpposite()) || relative.equals(this.pistonPos)) {
                break;
            }
            i++;
            if (i + this.toPush.size() > 12) {
                return false;
            }
        }
        int i2 = 0;
        for (int i3 = i - 1; i3 >= 0; i3--) {
            this.toPush.add(blockPos.relative(this.pushDirection.getOpposite(), i3));
            i2++;
        }
        int i4 = 1;
        while (true) {
            BlockPos relative2 = blockPos.relative(this.pushDirection, i4);
            int indexOf = this.toPush.indexOf(relative2);
            if (indexOf > -1) {
                reorderListAtCollision(i2, indexOf);
                for (int i5 = 0; i5 <= indexOf + i2; i5++) {
                    BlockPos blockPos2 = this.toPush.get(i5);
                    if (isSticky(this.level.getBlockState(blockPos2).getBlock()) && !addBranchingBlocks(blockPos2)) {
                        return false;
                    }
                }
                return true;
            }
            BlockState blockState3 = this.level.getBlockState(relative2);
            if (blockState3.isAir()) {
                return true;
            }
            if (!PistonBaseBlock.isPushable(blockState3, this.level, relative2, this.pushDirection, true, this.pushDirection) || relative2.equals(this.pistonPos)) {
                return false;
            }
            if (blockState3.getPistonPushReaction() == PushReaction.DESTROY) {
                this.toDestroy.add(relative2);
                return true;
            }
            if (this.toPush.size() >= 12) {
                return false;
            }
            this.toPush.add(relative2);
            i2++;
            i4++;
        }
    }

    private void reorderListAtCollision(int i, int i2) {
        List<BlockPos> newArrayList = Lists.newArrayList();
        List<BlockPos> newArrayList2 = Lists.newArrayList();
        List<BlockPos> newArrayList3 = Lists.newArrayList();
        newArrayList.addAll(this.toPush.subList(0, i2));
        newArrayList2.addAll(this.toPush.subList(this.toPush.size() - i, this.toPush.size()));
        newArrayList3.addAll(this.toPush.subList(i2, this.toPush.size() - i));
        this.toPush.clear();
        this.toPush.addAll(newArrayList);
        this.toPush.addAll(newArrayList2);
        this.toPush.addAll(newArrayList3);
    }

    private boolean addBranchingBlocks(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != this.pushDirection.getAxis()) {
                BlockPos relative = blockPos.relative(direction);
                if (canStickToEachOther(this.level.getBlockState(relative).getBlock(), blockState.getBlock()) && !addBlockLine(relative, direction)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<BlockPos> getToPush() {
        return this.toPush;
    }

    public List<BlockPos> getToDestroy() {
        return this.toDestroy;
    }
}
