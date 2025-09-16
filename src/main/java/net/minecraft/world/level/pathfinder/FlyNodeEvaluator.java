package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/FlyNodeEvaluator.class */
public class FlyNodeEvaluator extends WalkNodeEvaluator {
    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        super.done();
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public Node getStart() {
        int floor;
        if (canFloat() && this.mob.isInWater()) {
            floor = Mth.floor(this.mob.getY());
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.getX(), floor, this.mob.getZ());
            Block block = this.level.getBlockState(mutableBlockPos).getBlock();
            while (block == Blocks.WATER) {
                floor++;
                mutableBlockPos.set(this.mob.getX(), floor, this.mob.getZ());
                block = this.level.getBlockState(mutableBlockPos).getBlock();
            }
        } else {
            floor = Mth.floor(this.mob.getY() + 0.5d);
        }
        BlockPos blockPosition = this.mob.blockPosition();
        if (this.mob.getPathfindingMalus(getBlockPathType(this.mob, blockPosition.getX(), floor, blockPosition.getZ())) < 0.0f) {
            Set<BlockPos> newHashSet = Sets.newHashSet();
            newHashSet.add(new BlockPos(this.mob.getBoundingBox().minX, floor, this.mob.getBoundingBox().minZ));
            newHashSet.add(new BlockPos(this.mob.getBoundingBox().minX, floor, this.mob.getBoundingBox().maxZ));
            newHashSet.add(new BlockPos(this.mob.getBoundingBox().maxX, floor, this.mob.getBoundingBox().minZ));
            newHashSet.add(new BlockPos(this.mob.getBoundingBox().maxX, floor, this.mob.getBoundingBox().maxZ));
            for (BlockPos blockPos : newHashSet) {
                if (this.mob.getPathfindingMalus(getBlockPathType(this.mob, blockPos)) >= 0.0f) {
                    return super.getNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                }
            }
        }
        return super.getNode(blockPosition.getX(), floor, blockPosition.getZ());
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public Target getGoal(double d, double d2, double d3) {
        return new Target(super.getNode(Mth.floor(d), Mth.floor(d2), Mth.floor(d3)));
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public int getNeighbors(Node[] nodeArr, Node node) {
        int i = 0;
        Node node2 = getNode(node.x, node.y, node.z + 1);
        if (isOpen(node2)) {
            i = 0 + 1;
            nodeArr[0] = node2;
        }
        Node node3 = getNode(node.x - 1, node.y, node.z);
        if (isOpen(node3)) {
            int i2 = i;
            i++;
            nodeArr[i2] = node3;
        }
        Node node4 = getNode(node.x + 1, node.y, node.z);
        if (isOpen(node4)) {
            int i3 = i;
            i++;
            nodeArr[i3] = node4;
        }
        Node node5 = getNode(node.x, node.y, node.z - 1);
        if (isOpen(node5)) {
            int i4 = i;
            i++;
            nodeArr[i4] = node5;
        }
        Node node6 = getNode(node.x, node.y + 1, node.z);
        if (isOpen(node6)) {
            int i5 = i;
            i++;
            nodeArr[i5] = node6;
        }
        Node node7 = getNode(node.x, node.y - 1, node.z);
        if (isOpen(node7)) {
            int i6 = i;
            i++;
            nodeArr[i6] = node7;
        }
        Node node8 = getNode(node.x, node.y + 1, node.z + 1);
        if (isOpen(node8) && hasMalus(node2) && hasMalus(node6)) {
            int i7 = i;
            i++;
            nodeArr[i7] = node8;
        }
        Node node9 = getNode(node.x - 1, node.y + 1, node.z);
        if (isOpen(node9) && hasMalus(node3) && hasMalus(node6)) {
            int i8 = i;
            i++;
            nodeArr[i8] = node9;
        }
        Node node10 = getNode(node.x + 1, node.y + 1, node.z);
        if (isOpen(node10) && hasMalus(node4) && hasMalus(node6)) {
            int i9 = i;
            i++;
            nodeArr[i9] = node10;
        }
        Node node11 = getNode(node.x, node.y + 1, node.z - 1);
        if (isOpen(node11) && hasMalus(node5) && hasMalus(node6)) {
            int i10 = i;
            i++;
            nodeArr[i10] = node11;
        }
        Node node12 = getNode(node.x, node.y - 1, node.z + 1);
        if (isOpen(node12) && hasMalus(node2) && hasMalus(node7)) {
            int i11 = i;
            i++;
            nodeArr[i11] = node12;
        }
        Node node13 = getNode(node.x - 1, node.y - 1, node.z);
        if (isOpen(node13) && hasMalus(node3) && hasMalus(node7)) {
            int i12 = i;
            i++;
            nodeArr[i12] = node13;
        }
        Node node14 = getNode(node.x + 1, node.y - 1, node.z);
        if (isOpen(node14) && hasMalus(node4) && hasMalus(node7)) {
            int i13 = i;
            i++;
            nodeArr[i13] = node14;
        }
        Node node15 = getNode(node.x, node.y - 1, node.z - 1);
        if (isOpen(node15) && hasMalus(node5) && hasMalus(node7)) {
            int i14 = i;
            i++;
            nodeArr[i14] = node15;
        }
        Node node16 = getNode(node.x + 1, node.y, node.z - 1);
        if (isOpen(node16) && hasMalus(node5) && hasMalus(node4)) {
            int i15 = i;
            i++;
            nodeArr[i15] = node16;
        }
        Node node17 = getNode(node.x + 1, node.y, node.z + 1);
        if (isOpen(node17) && hasMalus(node2) && hasMalus(node4)) {
            int i16 = i;
            i++;
            nodeArr[i16] = node17;
        }
        Node node18 = getNode(node.x - 1, node.y, node.z - 1);
        if (isOpen(node18) && hasMalus(node5) && hasMalus(node3)) {
            int i17 = i;
            i++;
            nodeArr[i17] = node18;
        }
        Node node19 = getNode(node.x - 1, node.y, node.z + 1);
        if (isOpen(node19) && hasMalus(node2) && hasMalus(node3)) {
            int i18 = i;
            i++;
            nodeArr[i18] = node19;
        }
        Node node20 = getNode(node.x + 1, node.y + 1, node.z - 1);
        if (isOpen(node20) && hasMalus(node16) && hasMalus(node5) && hasMalus(node4) && hasMalus(node6) && hasMalus(node11) && hasMalus(node10)) {
            int i19 = i;
            i++;
            nodeArr[i19] = node20;
        }
        Node node21 = getNode(node.x + 1, node.y + 1, node.z + 1);
        if (isOpen(node21) && hasMalus(node17) && hasMalus(node2) && hasMalus(node4) && hasMalus(node6) && hasMalus(node8) && hasMalus(node10)) {
            int i20 = i;
            i++;
            nodeArr[i20] = node21;
        }
        Node node22 = getNode(node.x - 1, node.y + 1, node.z - 1);
        if (isOpen(node22) && hasMalus(node18) && hasMalus(node5) && (hasMalus(node3) & hasMalus(node6)) && hasMalus(node11) && hasMalus(node9)) {
            int i21 = i;
            i++;
            nodeArr[i21] = node22;
        }
        Node node23 = getNode(node.x - 1, node.y + 1, node.z + 1);
        if (isOpen(node23) && hasMalus(node19) && hasMalus(node2) && (hasMalus(node3) & hasMalus(node6)) && hasMalus(node8) && hasMalus(node9)) {
            int i22 = i;
            i++;
            nodeArr[i22] = node23;
        }
        Node node24 = getNode(node.x + 1, node.y - 1, node.z - 1);
        if (isOpen(node24) && hasMalus(node16) && hasMalus(node5) && hasMalus(node4) && hasMalus(node7) && hasMalus(node15) && hasMalus(node14)) {
            int i23 = i;
            i++;
            nodeArr[i23] = node24;
        }
        Node node25 = getNode(node.x + 1, node.y - 1, node.z + 1);
        if (isOpen(node25) && hasMalus(node17) && hasMalus(node2) && hasMalus(node4) && hasMalus(node7) && hasMalus(node12) && hasMalus(node14)) {
            int i24 = i;
            i++;
            nodeArr[i24] = node25;
        }
        Node node26 = getNode(node.x - 1, node.y - 1, node.z - 1);
        if (isOpen(node26) && hasMalus(node18) && hasMalus(node5) && hasMalus(node3) && hasMalus(node7) && hasMalus(node15) && hasMalus(node13)) {
            int i25 = i;
            i++;
            nodeArr[i25] = node26;
        }
        Node node27 = getNode(node.x - 1, node.y - 1, node.z + 1);
        if (isOpen(node27) && hasMalus(node19) && hasMalus(node2) && hasMalus(node3) && hasMalus(node7) && hasMalus(node12) && hasMalus(node13)) {
            int i26 = i;
            i++;
            nodeArr[i26] = node27;
        }
        return i;
    }

    private boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    private boolean isOpen(@Nullable Node node) {
        return (node == null || node.closed) ? false : true;
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    @Nullable
    protected Node getNode(int i, int i2, int i3) {
        Node node = null;
        BlockPathTypes blockPathType = getBlockPathType(this.mob, i, i2, i3);
        float pathfindingMalus = this.mob.getPathfindingMalus(blockPathType);
        if (pathfindingMalus >= 0.0f) {
            node = super.getNode(i, i2, i3);
            node.type = blockPathType;
            node.costMalus = Math.max(node.costMalus, pathfindingMalus);
            if (blockPathType == BlockPathTypes.WALKABLE) {
                node.costMalus += 1.0f;
            }
        }
        if (blockPathType == BlockPathTypes.OPEN || blockPathType == BlockPathTypes.WALKABLE) {
            return node;
        }
        return node;
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3, Mob mob, int i4, int i5, int i6, boolean z, boolean z2) {
        EnumSet<BlockPathTypes> noneOf = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes blockPathTypes = getBlockPathTypes(blockGetter, i, i2, i3, i4, i5, i6, z, z2, noneOf, BlockPathTypes.BLOCKED, mob.blockPosition());
        if (noneOf.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        }
        BlockPathTypes blockPathTypes2 = BlockPathTypes.BLOCKED;
        Iterator it = noneOf.iterator();
        while (it.hasNext()) {
            BlockPathTypes blockPathTypes3 = (BlockPathTypes) it.next();
            if (mob.getPathfindingMalus(blockPathTypes3) < 0.0f) {
                return blockPathTypes3;
            }
            if (mob.getPathfindingMalus(blockPathTypes3) >= mob.getPathfindingMalus(blockPathTypes2)) {
                blockPathTypes2 = blockPathTypes3;
            }
        }
        if (blockPathTypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockPathTypes2) == 0.0f) {
            return BlockPathTypes.OPEN;
        }
        return blockPathTypes2;
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPathTypes blockPathTypeRaw = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, i2, i3));
        if (blockPathTypeRaw == BlockPathTypes.OPEN && i2 >= 1) {
            BlockState blockState = blockGetter.getBlockState(mutableBlockPos.set(i, i2 - 1, i3));
            BlockPathTypes blockPathTypeRaw2 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, i2 - 1, i3));
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_FIRE || blockState.is(Blocks.MAGMA_BLOCK) || blockPathTypeRaw2 == BlockPathTypes.LAVA || blockState.is(BlockTags.CAMPFIRES)) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_FIRE;
            } else if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_CACTUS) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_CACTUS;
            } else if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_OTHER) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_OTHER;
            } else if (blockPathTypeRaw2 == BlockPathTypes.COCOA) {
                blockPathTypeRaw = BlockPathTypes.COCOA;
            } else if (blockPathTypeRaw2 == BlockPathTypes.FENCE) {
                blockPathTypeRaw = BlockPathTypes.FENCE;
            } else {
                blockPathTypeRaw = (blockPathTypeRaw2 == BlockPathTypes.WALKABLE || blockPathTypeRaw2 == BlockPathTypes.OPEN || blockPathTypeRaw2 == BlockPathTypes.WATER) ? BlockPathTypes.OPEN : BlockPathTypes.WALKABLE;
            }
        }
        if (blockPathTypeRaw == BlockPathTypes.WALKABLE || blockPathTypeRaw == BlockPathTypes.OPEN) {
            blockPathTypeRaw = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, i2, i3), blockPathTypeRaw);
        }
        return blockPathTypeRaw;
    }

    private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
        return getBlockPathType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private BlockPathTypes getBlockPathType(Mob mob, int i, int i2, int i3) {
        return getBlockPathType(this.level, i, i2, i3, mob, this.entityWidth, this.entityHeight, this.entityDepth, canOpenDoors(), canPassDoors());
    }
}
