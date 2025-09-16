package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/WalkNodeEvaluator.class */
public class WalkNodeEvaluator extends NodeEvaluator {
    protected float oldWaterCost;
    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap();

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypesByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public Node getStart() {
        BlockPos blockPos;
        int y;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int floor = Mth.floor(this.mob.getY());
        BlockState blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), floor, this.mob.getZ()));
        if (this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
            while (this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
                floor++;
                blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), floor, this.mob.getZ()));
            }
            y = floor - 1;
        } else if (canFloat() && this.mob.isInWater()) {
            while (true) {
                if (blockState.getBlock() != Blocks.WATER && blockState.getFluidState() != Fluids.WATER.getSource(false)) {
                    break;
                }
                floor++;
                blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), floor, this.mob.getZ()));
            }
            y = floor - 1;
        } else if (this.mob.isOnGround()) {
            y = Mth.floor(this.mob.getY() + 0.5d);
        } else {
            BlockPos blockPosition = this.mob.blockPosition();
            while (true) {
                blockPos = blockPosition;
                if ((this.level.getBlockState(blockPos).isAir() || this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND)) && blockPos.getY() > 0) {
                    blockPosition = blockPos.below();
                }
            }
//            y = blockPos.above().getY();
        }
        BlockPos blockPosition2 = this.mob.blockPosition();
        if (this.mob.getPathfindingMalus(getCachedBlockType(this.mob, blockPosition2.getX(), y, blockPosition2.getZ())) < 0.0f) {
            AABB boundingBox = this.mob.getBoundingBox();
            if (hasPositiveMalus(mutableBlockPos.set(boundingBox.minX, y, boundingBox.minZ)) || hasPositiveMalus(mutableBlockPos.set(boundingBox.minX, y, boundingBox.maxZ)) || hasPositiveMalus(mutableBlockPos.set(boundingBox.maxX, y, boundingBox.minZ)) || hasPositiveMalus(mutableBlockPos.set(boundingBox.maxX, y, boundingBox.maxZ))) {
                Node node = getNode(mutableBlockPos);
                node.type = getBlockPathType(this.mob, node.asBlockPos());
                node.costMalus = this.mob.getPathfindingMalus(node.type);
                return node;
            }
        }
        Node node2 = getNode(blockPosition2.getX(), y, blockPosition2.getZ());
        node2.type = getBlockPathType(this.mob, node2.asBlockPos());
        node2.costMalus = this.mob.getPathfindingMalus(node2.type);
        return node2;
    }

    private boolean hasPositiveMalus(BlockPos blockPos) {
        return this.mob.getPathfindingMalus(getBlockPathType(this.mob, blockPos)) >= 0.0f;
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public Target getGoal(double d, double d2, double d3) {
        return new Target(getNode(Mth.floor(d), Mth.floor(d2), Mth.floor(d3)));
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public int getNeighbors(Node[] nodeArr, Node node) {
        int i = 0;
        int i2 = 0;
        BlockPathTypes cachedBlockType = getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
        BlockPathTypes cachedBlockType2 = getCachedBlockType(this.mob, node.x, node.y, node.z);
        if (this.mob.getPathfindingMalus(cachedBlockType) >= 0.0f && cachedBlockType2 != BlockPathTypes.STICKY_HONEY) {
            i2 = Mth.floor(Math.max(1.0f, this.mob.maxUpStep));
        }
        double floorLevel = getFloorLevel(this.level, new BlockPos(node.x, node.y, node.z));
        Node landNode = getLandNode(node.x, node.y, node.z + 1, i2, floorLevel, Direction.SOUTH, cachedBlockType2);
        if (isNeighborValid(landNode, node)) {
            i = 0 + 1;
            nodeArr[0] = landNode;
        }
        Node landNode2 = getLandNode(node.x - 1, node.y, node.z, i2, floorLevel, Direction.WEST, cachedBlockType2);
        if (isNeighborValid(landNode2, node)) {
            int i3 = i;
            i++;
            nodeArr[i3] = landNode2;
        }
        Node landNode3 = getLandNode(node.x + 1, node.y, node.z, i2, floorLevel, Direction.EAST, cachedBlockType2);
        if (isNeighborValid(landNode3, node)) {
            int i4 = i;
            i++;
            nodeArr[i4] = landNode3;
        }
        Node landNode4 = getLandNode(node.x, node.y, node.z - 1, i2, floorLevel, Direction.NORTH, cachedBlockType2);
        if (isNeighborValid(landNode4, node)) {
            int i5 = i;
            i++;
            nodeArr[i5] = landNode4;
        }
        Node landNode5 = getLandNode(node.x - 1, node.y, node.z - 1, i2, floorLevel, Direction.NORTH, cachedBlockType2);
        if (isDiagonalValid(node, landNode2, landNode4, landNode5)) {
            int i6 = i;
            i++;
            nodeArr[i6] = landNode5;
        }
        Node landNode6 = getLandNode(node.x + 1, node.y, node.z - 1, i2, floorLevel, Direction.NORTH, cachedBlockType2);
        if (isDiagonalValid(node, landNode3, landNode4, landNode6)) {
            int i7 = i;
            i++;
            nodeArr[i7] = landNode6;
        }
        Node landNode7 = getLandNode(node.x - 1, node.y, node.z + 1, i2, floorLevel, Direction.SOUTH, cachedBlockType2);
        if (isDiagonalValid(node, landNode2, landNode, landNode7)) {
            int i8 = i;
            i++;
            nodeArr[i8] = landNode7;
        }
        Node landNode8 = getLandNode(node.x + 1, node.y, node.z + 1, i2, floorLevel, Direction.SOUTH, cachedBlockType2);
        if (isDiagonalValid(node, landNode3, landNode, landNode8)) {
            int i9 = i;
            i++;
            nodeArr[i9] = landNode8;
        }
        return i;
    }

    private boolean isNeighborValid(Node node, Node node2) {
        return (node == null || node.closed || (node.costMalus < 0.0f && node2.costMalus >= 0.0f)) ? false : true;
    }

    private boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3, @Nullable Node node4) {
        if (node4 == null || node3 == null || node2 == null || node4.closed || node3.y > node.y || node2.y > node.y || node2.type == BlockPathTypes.WALKABLE_DOOR || node3.type == BlockPathTypes.WALKABLE_DOOR || node4.type == BlockPathTypes.WALKABLE_DOOR) {
            return false;
        }
        boolean z = node3.type == BlockPathTypes.FENCE && node2.type == BlockPathTypes.FENCE && ((double) this.mob.getBbWidth()) < 0.5d;
        return node4.costMalus >= 0.0f && (node3.y < node.y || node3.costMalus >= 0.0f || z) && (node2.y < node.y || node2.costMalus >= 0.0f || z);
    }

    private boolean canReachWithoutCollision(Node node) {
        Vec3 vec3 = new Vec3(node.x - this.mob.getX(), node.y - this.mob.getY(), node.z - this.mob.getZ());
        AABB boundingBox = this.mob.getBoundingBox();
        int ceil = Mth.ceil(vec3.length() / boundingBox.getSize());
        Vec3 scale = vec3.scale(1.0f / ceil);
        for (int i = 1; i <= ceil; i++) {
            boundingBox = boundingBox.move(scale);
            if (hasCollisions(boundingBox)) {
                return false;
            }
        }
        return true;
    }

    public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        VoxelShape collisionShape = blockGetter.getBlockState(below).getCollisionShape(blockGetter, below);
        return below.getY() + (collisionShape.isEmpty() ? 0.0d : collisionShape.max(Direction.Axis.Y));
    }

    @Nullable
    private Node getLandNode(int i, int i2, int i3, int i4, double d, Direction direction, BlockPathTypes blockPathTypes) {
        float pathfindingMalus = 0;
        Node node = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        if (getFloorLevel(this.level, mutableBlockPos.set(i, i2, i3)) - d > 1.125d) {
            return null;
        }
        BlockPathTypes cachedBlockType = getCachedBlockType(this.mob, i, i2, i3);
        float pathfindingMalus2 = this.mob.getPathfindingMalus(cachedBlockType);
        double bbWidth = this.mob.getBbWidth() / 2.0d;
        if (pathfindingMalus2 >= 0.0f) {
            node = getNode(i, i2, i3);
            node.type = cachedBlockType;
            node.costMalus = Math.max(node.costMalus, pathfindingMalus2);
        }
        if (blockPathTypes == BlockPathTypes.FENCE && node != null && node.costMalus >= 0.0f && !canReachWithoutCollision(node)) {
            node = null;
        }
        if (cachedBlockType == BlockPathTypes.WALKABLE) {
            return node;
        }
        if ((node == null || node.costMalus < 0.0f) && i4 > 0 && cachedBlockType != BlockPathTypes.FENCE && cachedBlockType != BlockPathTypes.UNPASSABLE_RAIL && cachedBlockType != BlockPathTypes.TRAPDOOR) {
            node = getLandNode(i, i2 + 1, i3, i4 - 1, d, direction, blockPathTypes);
            if (node != null && ((node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0f)) {
                double stepX = (i - direction.getStepX()) + 0.5d;
                double stepZ = (i3 - direction.getStepZ()) + 0.5d;
                if (hasCollisions(new AABB(stepX - bbWidth, getFloorLevel(this.level, mutableBlockPos.set(stepX, i2 + 1, stepZ)) + 0.001d, stepZ - bbWidth, stepX + bbWidth, (this.mob.getBbHeight() + getFloorLevel(this.level, mutableBlockPos.set(node.x, node.y, node.z))) - 0.002d, stepZ + bbWidth))) {
                    node = null;
                }
            }
        }
        if (cachedBlockType == BlockPathTypes.WATER && !canFloat()) {
            if (getCachedBlockType(this.mob, i, i2 - 1, i3) != BlockPathTypes.WATER) {
                return node;
            }
            while (i2 > 0) {
                i2--;
                cachedBlockType = getCachedBlockType(this.mob, i, i2, i3);
                if (cachedBlockType == BlockPathTypes.WATER) {
                    node = getNode(i, i2, i3);
                    node.type = cachedBlockType;
                    node.costMalus = Math.max(node.costMalus, this.mob.getPathfindingMalus(cachedBlockType));
                } else {
                    return node;
                }
            }
        }
        if (cachedBlockType == BlockPathTypes.OPEN) {
            int i5 = 0;
            int i6 = i2;
            do {
                if (cachedBlockType == BlockPathTypes.OPEN) {
                    i2--;
                    if (i2 < 0) {
                        Node node2 = getNode(i, i6, i3);
                        node2.type = BlockPathTypes.BLOCKED;
                        node2.costMalus = -1.0f;
                        return node2;
                    }
                    int i7 = i5;
                    i5++;
                    if (i7 >= this.mob.getMaxFallDistance()) {
                        Node node3 = getNode(i, i2, i3);
                        node3.type = BlockPathTypes.BLOCKED;
                        node3.costMalus = -1.0f;
                        return node3;
                    }
                    cachedBlockType = getCachedBlockType(this.mob, i, i2, i3);
                    pathfindingMalus = this.mob.getPathfindingMalus(cachedBlockType);
                    if (cachedBlockType != BlockPathTypes.OPEN && pathfindingMalus >= 0.0f) {
                        node = getNode(i, i2, i3);
                        node.type = cachedBlockType;
                        node.costMalus = Math.max(node.costMalus, pathfindingMalus);
                    }
                }
            } while (pathfindingMalus >= 0.0f);
            Node node4 = getNode(i, i2, i3);
            node4.type = BlockPathTypes.BLOCKED;
            node4.costMalus = -1.0f;
            return node4;
        }
        if (cachedBlockType == BlockPathTypes.FENCE) {
            node = getNode(i, i2, i3);
            node.closed = true;
            node.type = cachedBlockType;
            node.costMalus = cachedBlockType.getMalus();
        }
        return node;
    }

    private boolean hasCollisions(AABB aabb) {
        return ((Boolean) this.collisionCache.computeIfAbsent(aabb, aabb2 -> {
            return Boolean.valueOf(!this.level.noCollision(this.mob, aabb));
        })).booleanValue();
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3, Mob mob, int i4, int i5, int i6, boolean z, boolean z2) {
        EnumSet<BlockPathTypes> noneOf = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes blockPathTypes = getBlockPathTypes(blockGetter, i, i2, i3, i4, i5, i6, z, z2, noneOf, BlockPathTypes.BLOCKED, mob.blockPosition());
        if (noneOf.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        }
        if (noneOf.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
            return BlockPathTypes.UNPASSABLE_RAIL;
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
        if (blockPathTypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockPathTypes2) == 0.0f && i4 <= 1) {
            return BlockPathTypes.OPEN;
        }
        return blockPathTypes2;
    }

    public BlockPathTypes getBlockPathTypes(BlockGetter blockGetter, int i, int i2, int i3, int i4, int i5, int i6, boolean z, boolean z2, EnumSet<BlockPathTypes> enumSet, BlockPathTypes blockPathTypes, BlockPos blockPos) {
        for (int i7 = 0; i7 < i4; i7++) {
            for (int i8 = 0; i8 < i5; i8++) {
                for (int i9 = 0; i9 < i6; i9++) {
                    BlockPathTypes evaluateBlockPathType = evaluateBlockPathType(blockGetter, z, z2, blockPos, getBlockPathType(blockGetter, i7 + i, i8 + i2, i9 + i3));
                    if (i7 == 0 && i8 == 0 && i9 == 0) {
                        blockPathTypes = evaluateBlockPathType;
                    }
                    enumSet.add(evaluateBlockPathType);
                }
            }
        }
        return blockPathTypes;
    }

    protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean z, boolean z2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
        if (blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED && z && z2) {
            blockPathTypes = BlockPathTypes.WALKABLE_DOOR;
        }
        if (blockPathTypes == BlockPathTypes.DOOR_OPEN && !z2) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        if (blockPathTypes == BlockPathTypes.RAIL && !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock) && !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
            blockPathTypes = BlockPathTypes.UNPASSABLE_RAIL;
        }
        if (blockPathTypes == BlockPathTypes.LEAVES) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        return blockPathTypes;
    }

    private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
        return getCachedBlockType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private BlockPathTypes getCachedBlockType(Mob mob, int i, int i2, int i3) {
        return (BlockPathTypes) this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(i, i2, i3), j -> {
            return getBlockPathType(this.level, i, i2, i3, mob, this.entityWidth, this.entityHeight, this.entityDepth, canOpenDoors(), canPassDoors());
        });
    }

    @Override // net.minecraft.world.level.pathfinder.NodeEvaluator
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int i2, int i3) {
        return getBlockPathTypeStatic(blockGetter, new BlockPos.MutableBlockPos(i, i2, i3));
    }

    public static BlockPathTypes getBlockPathTypeStatic(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos) {
        int x = mutableBlockPos.getX();
        int y = mutableBlockPos.getY();
        int z = mutableBlockPos.getZ();
        BlockPathTypes blockPathTypeRaw = getBlockPathTypeRaw(blockGetter, mutableBlockPos);
        if (blockPathTypeRaw == BlockPathTypes.OPEN && y >= 1) {
            BlockPathTypes blockPathTypeRaw2 = getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(x, y - 1, z));
            blockPathTypeRaw = (blockPathTypeRaw2 == BlockPathTypes.WALKABLE || blockPathTypeRaw2 == BlockPathTypes.OPEN || blockPathTypeRaw2 == BlockPathTypes.WATER || blockPathTypeRaw2 == BlockPathTypes.LAVA) ? BlockPathTypes.OPEN : BlockPathTypes.WALKABLE;
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_FIRE) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_FIRE;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_CACTUS) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_CACTUS;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.DAMAGE_OTHER) {
                blockPathTypeRaw = BlockPathTypes.DAMAGE_OTHER;
            }
            if (blockPathTypeRaw2 == BlockPathTypes.STICKY_HONEY) {
                blockPathTypeRaw = BlockPathTypes.STICKY_HONEY;
            }
        }
        if (blockPathTypeRaw == BlockPathTypes.WALKABLE) {
            blockPathTypeRaw = checkNeighbourBlocks(blockGetter, mutableBlockPos.set(x, y, z), blockPathTypeRaw);
        }
        return blockPathTypeRaw;
    }

    public static BlockPathTypes checkNeighbourBlocks(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos, BlockPathTypes blockPathTypes) {
        int x = mutableBlockPos.getX();
        int y = mutableBlockPos.getY();
        int z = mutableBlockPos.getZ();
        for (int i = -1; i <= 1; i++) {
            for (int i2 = -1; i2 <= 1; i2++) {
                for (int i3 = -1; i3 <= 1; i3++) {
                    if (i != 0 || i3 != 0) {
                        mutableBlockPos.set(x + i, y + i2, z + i3);
                        BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                        if (blockState.is(Blocks.CACTUS)) {
                            return BlockPathTypes.DANGER_CACTUS;
                        }
                        if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
                            return BlockPathTypes.DANGER_OTHER;
                        }
                        if (isBurningBlock(blockState)) {
                            return BlockPathTypes.DANGER_FIRE;
                        }
                        if (blockGetter.getFluidState(mutableBlockPos).is(FluidTags.WATER)) {
                            return BlockPathTypes.WATER_BORDER;
                        }
                    }
                }
            }
        }
        return blockPathTypes;
    }

    protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        if (blockState.isAir()) {
            return BlockPathTypes.OPEN;
        }
        if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD)) {
            return BlockPathTypes.TRAPDOOR;
        }
        if (blockState.is(Blocks.CACTUS)) {
            return BlockPathTypes.DAMAGE_CACTUS;
        }
        if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DAMAGE_OTHER;
        }
        if (blockState.is(Blocks.HONEY_BLOCK)) {
            return BlockPathTypes.STICKY_HONEY;
        }
        if (blockState.is(Blocks.COCOA)) {
            return BlockPathTypes.COCOA;
        }
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.is(FluidTags.WATER)) {
            return BlockPathTypes.WATER;
        }
        if (fluidState.is(FluidTags.LAVA)) {
            return BlockPathTypes.LAVA;
        }
        if (isBurningBlock(blockState)) {
            return BlockPathTypes.DAMAGE_FIRE;
        }
        if (DoorBlock.isWoodenDoor(blockState) && !((Boolean) blockState.getValue(DoorBlock.OPEN)).booleanValue()) {
            return BlockPathTypes.DOOR_WOOD_CLOSED;
        }
        if ((block instanceof DoorBlock) && material == Material.METAL && !((Boolean) blockState.getValue(DoorBlock.OPEN)).booleanValue()) {
            return BlockPathTypes.DOOR_IRON_CLOSED;
        }
        if ((block instanceof DoorBlock) && ((Boolean) blockState.getValue(DoorBlock.OPEN)).booleanValue()) {
            return BlockPathTypes.DOOR_OPEN;
        }
        if (block instanceof BaseRailBlock) {
            return BlockPathTypes.RAIL;
        }
        if (block instanceof LeavesBlock) {
            return BlockPathTypes.LEAVES;
        }
        if (block.is(BlockTags.FENCES) || block.is(BlockTags.WALLS) || ((block instanceof FenceGateBlock) && !((Boolean) blockState.getValue(FenceGateBlock.OPEN)).booleanValue())) {
            return BlockPathTypes.FENCE;
        }
        if (!blockState.isPathfindable(blockGetter, blockPos, PathComputationType.LAND)) {
            return BlockPathTypes.BLOCKED;
        }
        return BlockPathTypes.OPEN;
    }

    private static boolean isBurningBlock(BlockState blockState) {
        return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }
}
