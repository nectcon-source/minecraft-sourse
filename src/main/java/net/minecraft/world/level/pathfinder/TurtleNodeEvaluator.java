package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/TurtleNodeEvaluator.class */
public class TurtleNodeEvaluator extends WalkNodeEvaluator {
    private float oldWalkableCost;
    private float oldWaterBorderCost;

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
        this.oldWalkableCost = mob.getPathfindingMalus(BlockPathTypes.WALKABLE);
        mob.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0f);
        this.oldWaterBorderCost = mob.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
        mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0f);
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
        super.done();
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public Node getStart() {
        return getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5d), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public Target getGoal(double d, double d2, double d3) {
        return new Target(getNode(Mth.floor(d), Mth.floor(d2 + 0.5d), Mth.floor(d3)));
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public int getNeighbors(Node[] nodeArr, Node node) {
        Node acceptedNode;
        Node acceptedNode2;
        Node acceptedNode3;
        Node acceptedNode4;
        int i = 0;
        double inWaterDependentPosHeight = inWaterDependentPosHeight(new BlockPos(node.x, node.y, node.z));
        Node acceptedNode5 = getAcceptedNode(node.x, node.y, node.z + 1, 1, inWaterDependentPosHeight);
        Node acceptedNode6 = getAcceptedNode(node.x - 1, node.y, node.z, 1, inWaterDependentPosHeight);
        Node acceptedNode7 = getAcceptedNode(node.x + 1, node.y, node.z, 1, inWaterDependentPosHeight);
        Node acceptedNode8 = getAcceptedNode(node.x, node.y, node.z - 1, 1, inWaterDependentPosHeight);
        Node acceptedNode9 = getAcceptedNode(node.x, node.y + 1, node.z, 0, inWaterDependentPosHeight);
        Node acceptedNode10 = getAcceptedNode(node.x, node.y - 1, node.z, 1, inWaterDependentPosHeight);
        if (acceptedNode5 != null && !acceptedNode5.closed) {
            i = 0 + 1;
            nodeArr[0] = acceptedNode5;
        }
        if (acceptedNode6 != null && !acceptedNode6.closed) {
            int i2 = i;
            i++;
            nodeArr[i2] = acceptedNode6;
        }
        if (acceptedNode7 != null && !acceptedNode7.closed) {
            int i3 = i;
            i++;
            nodeArr[i3] = acceptedNode7;
        }
        if (acceptedNode8 != null && !acceptedNode8.closed) {
            int i4 = i;
            i++;
            nodeArr[i4] = acceptedNode8;
        }
        if (acceptedNode9 != null && !acceptedNode9.closed) {
            int i5 = i;
            i++;
            nodeArr[i5] = acceptedNode9;
        }
        if (acceptedNode10 != null && !acceptedNode10.closed) {
            int i6 = i;
            i++;
            nodeArr[i6] = acceptedNode10;
        }
        boolean z = acceptedNode8 == null || acceptedNode8.type == BlockPathTypes.OPEN || acceptedNode8.costMalus != 0.0f;
        boolean z2 = acceptedNode5 == null || acceptedNode5.type == BlockPathTypes.OPEN || acceptedNode5.costMalus != 0.0f;
        boolean z3 = acceptedNode7 == null || acceptedNode7.type == BlockPathTypes.OPEN || acceptedNode7.costMalus != 0.0f;
        boolean z4 = acceptedNode6 == null || acceptedNode6.type == BlockPathTypes.OPEN || acceptedNode6.costMalus != 0.0f;
        if (z && z4 && (acceptedNode4 = getAcceptedNode(node.x - 1, node.y, node.z - 1, 1, inWaterDependentPosHeight)) != null && !acceptedNode4.closed) {
            int i7 = i;
            i++;
            nodeArr[i7] = acceptedNode4;
        }
        if (z && z3 && (acceptedNode3 = getAcceptedNode(node.x + 1, node.y, node.z - 1, 1, inWaterDependentPosHeight)) != null && !acceptedNode3.closed) {
            int i8 = i;
            i++;
            nodeArr[i8] = acceptedNode3;
        }
        if (z2 && z4 && (acceptedNode2 = getAcceptedNode(node.x - 1, node.y, node.z + 1, 1, inWaterDependentPosHeight)) != null && !acceptedNode2.closed) {
            int i9 = i;
            i++;
            nodeArr[i9] = acceptedNode2;
        }
        if (z2 && z3 && (acceptedNode = getAcceptedNode(node.x + 1, node.y, node.z + 1, 1, inWaterDependentPosHeight)) != null && !acceptedNode.closed) {
            int i10 = i;
            i++;
            nodeArr[i10] = acceptedNode;
        }
        return i;
    }

    private double inWaterDependentPosHeight(BlockPos blockPos) {
        if (!this.mob.isInWater()) {
            BlockPos below = blockPos.below();
            VoxelShape collisionShape = this.level.getBlockState(below).getCollisionShape(this.level, below);
            return below.getY() + (collisionShape.isEmpty() ? 0.0d : collisionShape.max(Direction.Axis.Y));
        }
        return blockPos.getY() + 0.5d;
    }

    @Nullable
    private Node getAcceptedNode(int var1, int var2, int var3, int var4, double var5) {
        Node var7 = null;
        BlockPos var8 = new BlockPos(var1, var2, var3);
        double var9 = this.inWaterDependentPosHeight(var8);
        if (var9 - var5 > (double)1.125F) {
            return null;
        } else {
            BlockPathTypes var11 = this.getBlockPathType(this.level, var1, var2, var3, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false);
            float var12 = this.mob.getPathfindingMalus(var11);
            double var13 = (double)this.mob.getBbWidth() / (double)2.0F;
            if (var12 >= 0.0F) {
                var7 = this.getNode(var1, var2, var3);
                var7.type = var11;
                var7.costMalus = Math.max(var7.costMalus, var12);
            }

            if (var11 != BlockPathTypes.WATER && var11 != BlockPathTypes.WALKABLE) {
                if (var7 == null && var4 > 0 && var11 != BlockPathTypes.FENCE && var11 != BlockPathTypes.UNPASSABLE_RAIL && var11 != BlockPathTypes.TRAPDOOR) {
                    var7 = this.getAcceptedNode(var1, var2 + 1, var3, var4 - 1, var5);
                }

                if (var11 == BlockPathTypes.OPEN) {
                    AABB var15 = new AABB((double)var1 - var13 + (double)0.5F, (double)var2 + 0.001, (double)var3 - var13 + (double)0.5F, (double)var1 + var13 + (double)0.5F, (double)((float)var2 + this.mob.getBbHeight()), (double)var3 + var13 + (double)0.5F);
                    if (!this.mob.level.noCollision(this.mob, var15)) {
                        return null;
                    }

                    BlockPathTypes var16 = this.getBlockPathType(this.level, var1, var2 - 1, var3, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false);
                    if (var16 == BlockPathTypes.BLOCKED) {
                        var7 = this.getNode(var1, var2, var3);
                        var7.type = BlockPathTypes.WALKABLE;
                        var7.costMalus = Math.max(var7.costMalus, var12);
                        return var7;
                    }

                    if (var16 == BlockPathTypes.WATER) {
                        var7 = this.getNode(var1, var2, var3);
                        var7.type = BlockPathTypes.WATER;
                        var7.costMalus = Math.max(var7.costMalus, var12);
                        return var7;
                    }

                    int var17 = 0;

                    while(var2 > 0 && var11 == BlockPathTypes.OPEN) {
                        --var2;
                        if (var17++ >= this.mob.getMaxFallDistance()) {
                            return null;
                        }

                        var11 = this.getBlockPathType(this.level, var1, var2, var3, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false);
                        var12 = this.mob.getPathfindingMalus(var11);
                        if (var11 != BlockPathTypes.OPEN && var12 >= 0.0F) {
                            var7 = this.getNode(var1, var2, var3);
                            var7.type = var11;
                            var7.costMalus = Math.max(var7.costMalus, var12);
                            break;
                        }

                        if (var12 < 0.0F) {
                            return null;
                        }
                    }
                }

                return var7;
            } else {
                if (var2 < this.mob.level.getSeaLevel() - 10 && var7 != null) {
                    ++var7.costMalus;
                }

                return var7;
            }
        }
    }
    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator
    protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean z, boolean z2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
        if (blockPathTypes == BlockPathTypes.RAIL && !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock) && !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
            blockPathTypes = BlockPathTypes.UNPASSABLE_RAIL;
        }
        if (blockPathTypes == BlockPathTypes.DOOR_OPEN || blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED || blockPathTypes == BlockPathTypes.DOOR_IRON_CLOSED) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        if (blockPathTypes == BlockPathTypes.LEAVES) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        return blockPathTypes;
    }

    @Override // net.minecraft.world.level.pathfinder.WalkNodeEvaluator, net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPathTypes blockPathTypeRaw = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, i2, i3));
        if (blockPathTypeRaw == BlockPathTypes.WATER) {
            for (Direction direction : Direction.values()) {
                if (getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, i2, i3).move(direction)) == BlockPathTypes.BLOCKED) {
                    return BlockPathTypes.WATER_BORDER;
                }
            }
            return BlockPathTypes.WATER;
        }
        if (blockPathTypeRaw == BlockPathTypes.OPEN && i2 >= 1) {
            BlockState blockState = blockGetter.getBlockState(new BlockPos(i, i2 - 1, i3));
            BlockPathTypes blockPathTypeRaw2 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, i2 - 1, i3));
            if (blockPathTypeRaw2 == BlockPathTypes.WALKABLE || blockPathTypeRaw2 == BlockPathTypes.OPEN || blockPathTypeRaw2 == BlockPathTypes.LAVA) {
                blockPathTypeRaw = BlockPathTypes.OPEN;
            } else {
                blockPathTypeRaw = BlockPathTypes.WALKABLE;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_FIRE || blockState.is(Blocks.MAGMA_BLOCK) || blockState.is(BlockTags.CAMPFIRES)) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_FIRE;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_CACTUS) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_CACTUS;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_OTHER) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_OTHER;
            }
        }
        if (blockPathTypeRaw == BlockPathTypes.WALKABLE) {
            blockPathTypeRaw = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, i2, i3), blockPathTypeRaw);
        }
        return blockPathTypeRaw;
    }
}
