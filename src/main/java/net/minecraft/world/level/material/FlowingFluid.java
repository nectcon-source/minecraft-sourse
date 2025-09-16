package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/FlowingFluid.class */
public abstract class FlowingFluid extends Fluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) { // from class: net.minecraft.world.level.material.FlowingFluid.1
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue(Byte.MAX_VALUE);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    public abstract Fluid getFlowing();

    public abstract Fluid getSource();

    protected abstract boolean canConvertToSource();

    protected abstract void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState);

    protected abstract int getSlopeFindDistance(LevelReader levelReader);

    protected abstract int getDropOff(LevelReader levelReader);

    @Override // net.minecraft.world.level.material.Fluid
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override // net.minecraft.world.level.material.Fluid
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        double var4 = (double)0.0F;
        double var6 = (double)0.0F;
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

        for(Direction var10 : Direction.Plane.HORIZONTAL) {
            var8.setWithOffset(blockPos, var10);
            FluidState var11 = blockGetter.getFluidState(var8);
            if (this.affectsFlow(var11)) {
                float var12 = var11.getOwnHeight();
                float var13 = 0.0F;
                if (var12 == 0.0F) {
                    if (!blockGetter.getBlockState(var8).getMaterial().blocksMotion()) {
                        BlockPos var14 = var8.below();
                        FluidState var15 = blockGetter.getFluidState(var14);
                        if (this.affectsFlow(var15)) {
                            var12 = var15.getOwnHeight();
                            if (var12 > 0.0F) {
                                var13 = fluidState.getOwnHeight() - (var12 - 0.8888889F);
                            }
                        }
                    }
                } else if (var12 > 0.0F) {
                    var13 = fluidState.getOwnHeight() - var12;
                }

                if (var13 != 0.0F) {
                    var4 += (double)((float)var10.getStepX() * var13);
                    var6 += (double)((float)var10.getStepZ() * var13);
                }
            }
        }

        Vec3 var16 = new Vec3(var4, (double)0.0F, var6);
        if ((Boolean)fluidState.getValue(FALLING)) {
            for(Direction var18 : Direction.Plane.HORIZONTAL) {
                var8.setWithOffset(blockPos, var18);
                if (this.isSolidFace(blockGetter, var8, var18) || this.isSolidFace(blockGetter, var8.above(), var18)) {
                    var16 = var16.normalize().add((double)0.0F, (double)-6.0F, (double)0.0F);
                    break;
                }
            }
        }

        return var16.normalize();
    }

    private boolean affectsFlow(FluidState fluidState) {
        return fluidState.isEmpty() || fluidState.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockGetter.getFluidState(blockPos).getType().isSame(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (blockState.getMaterial() == Material.ICE) {
            return false;
        }
        return blockState.isFaceSturdy(blockGetter, blockPos, direction);
    }

    protected void spread(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        BlockPos below = blockPos.below();
        BlockState blockState2 = levelAccessor.getBlockState(below);
        FluidState newLiquid = getNewLiquid(levelAccessor, below, blockState2);
        if (canSpreadTo(levelAccessor, blockPos, blockState, Direction.DOWN, below, blockState2, levelAccessor.getFluidState(below), newLiquid.getType())) {
            spreadTo(levelAccessor, below, blockState2, Direction.DOWN, newLiquid);
            if (sourceNeighborCount(levelAccessor, blockPos) >= 3) {
                spreadToSides(levelAccessor, blockPos, fluidState, blockState);
                return;
            }
            return;
        }
        if (fluidState.isSource() || !isWaterHole(levelAccessor, newLiquid.getType(), blockPos, blockState, below, blockState2)) {
            spreadToSides(levelAccessor, blockPos, fluidState, blockState);
        }
    }

    private void spreadToSides(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
        int amount = fluidState.getAmount() - getDropOff(levelAccessor);
        if (((Boolean) fluidState.getValue(FALLING)).booleanValue()) {
            amount = 7;
        }
        if (amount <= 0) {
            return;
        }
        for (Map.Entry<Direction, FluidState> entry : getSpread(levelAccessor, blockPos, blockState).entrySet()) {
            Direction key = entry.getKey();
            FluidState value = entry.getValue();
            BlockPos relative = blockPos.relative(key);
            BlockState blockState2 = levelAccessor.getBlockState(relative);
            if (canSpreadTo(levelAccessor, blockPos, blockState, key, relative, blockState2, levelAccessor.getFluidState(relative), value.getType())) {
                spreadTo(levelAccessor, relative, blockState2, key, value);
            }
        }
    }

    protected FluidState getNewLiquid(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        int i = 0;
        int i2 = 0;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            BlockPos relative = blockPos.relative(next);
            BlockState blockState2 = levelReader.getBlockState(relative);
            FluidState fluidState = blockState2.getFluidState();
            if (fluidState.getType().isSame(this) && canPassThroughWall(next, levelReader, blockPos, blockState, relative, blockState2)) {
                if (fluidState.isSource()) {
                    i2++;
                }
                i = Math.max(i, fluidState.getAmount());
            }
        }
        if (canConvertToSource() && i2 >= 2) {
            BlockState blockState3 = levelReader.getBlockState(blockPos.below());
            FluidState fluidState2 = blockState3.getFluidState();
            if (blockState3.getMaterial().isSolid() || isSourceBlockOfThisType(fluidState2)) {
                return getSource(false);
            }
        }
        BlockPos above = blockPos.above();
        BlockState blockState4 = levelReader.getBlockState(above);
        FluidState fluidState3 = blockState4.getFluidState();
        if (!fluidState3.isEmpty() && fluidState3.getType().isSame(this) && canPassThroughWall(Direction.UP, levelReader, blockPos, blockState, above, blockState4)) {
            return getFlowing(8, true);
        }
        int dropOff = i - getDropOff(levelReader);
        if (dropOff <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return getFlowing(dropOff, false);
    }

    private boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap;
        Block.BlockStatePairKey blockStatePairKey;
        if (blockState.getBlock().hasDynamicShape() || blockState2.getBlock().hasDynamicShape()) {
            object2ByteLinkedOpenHashMap = null;
        } else {
            object2ByteLinkedOpenHashMap = OCCLUSION_CACHE.get();
        }
        if (object2ByteLinkedOpenHashMap != null) {
            blockStatePairKey = new Block.BlockStatePairKey(blockState, blockState2, direction);
            byte andMoveToFirst = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
            if (andMoveToFirst != Byte.MAX_VALUE) {
                return andMoveToFirst != 0;
            }
        } else {
            blockStatePairKey = null;
        }
        boolean z = !Shapes.mergedFaceOccludes(blockState.getCollisionShape(blockGetter, blockPos), blockState2.getCollisionShape(blockGetter, blockPos2), direction);
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte) (z ? 1 : 0));
        }
        return z;
    }

    public FluidState getFlowing(int i, boolean z) {
        return   getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(i)).setValue(FALLING, Boolean.valueOf(z));
    }

    public FluidState getSource(boolean z) {
        return  getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(z));
    }

    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (blockState.getBlock() instanceof LiquidBlockContainer) {
            ((LiquidBlockContainer) blockState.getBlock()).placeLiquid(levelAccessor, blockPos, blockState, fluidState);
            return;
        }
        if (!blockState.isAir()) {
            beforeDestroyingBlock(levelAccessor, blockPos, blockState);
        }
        levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
    }

    private static short getCacheKey(BlockPos blockPos, BlockPos blockPos2) {
        return (short) (((((blockPos2.getX() - blockPos.getX()) + 128) & 255) << 8) | (((blockPos2.getZ() - blockPos.getZ()) + 128) & 255));
    }

    protected int getSlopeDistance(LevelReader levelReader, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int slopeDistance;
        int i2 = 1000;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            if (next != direction) {
                BlockPos relative = blockPos.relative(next);
                short cacheKey = getCacheKey(blockPos2, relative);
                Pair<BlockState, FluidState> pair =  short2ObjectMap.computeIfAbsent(cacheKey, i3 -> {
                    BlockState blockState2 = levelReader.getBlockState(relative);
                    return Pair.of(blockState2, blockState2.getFluidState());
                });
                BlockState blockState2 =  pair.getFirst();
                if (!canPassThrough(levelReader, getFlowing(), blockPos, blockState, next, relative, blockState2, (FluidState) pair.getSecond())) {
                    continue;
                } else {
                    if (short2BooleanMap.computeIfAbsent(cacheKey, i4 -> {
                        BlockPos below = relative.below();
                        return isWaterHole(levelReader, getFlowing(), relative, blockState2, below, levelReader.getBlockState(below));
                    })) {
                        return i;
                    }
                    if (i < getSlopeFindDistance(levelReader) && (slopeDistance = getSlopeDistance(levelReader, relative, i + 1, next.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) < i2) {
                        i2 = slopeDistance;
                    }
                }
            }
        }
        return i2;
    }

    private boolean isWaterHole(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        if (!canPassThroughWall(Direction.DOWN, blockGetter, blockPos, blockState, blockPos2, blockState2)) {
            return false;
        }
        if (blockState2.getFluidState().getType().isSame(this)) {
            return true;
        }
        return canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return !isSourceBlockOfThisType(fluidState) && canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2) && canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean isSourceBlockOfThisType(FluidState fluidState) {
        return fluidState.getType().isSame(this) && fluidState.isSource();
    }

    private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
        int i = 0;
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            if (isSourceBlockOfThisType(levelReader.getFluidState(blockPos.relative(it.next())))) {
                i++;
            }
        }
        return i;
    }

    protected Map<Direction, FluidState> getSpread(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        int slopeDistance;
        int i = 1000;
        Map<Direction, FluidState> newEnumMap = Maps.newEnumMap(Direction.class);
        Short2ObjectOpenHashMap short2ObjectOpenHashMap = new Short2ObjectOpenHashMap();
        Short2BooleanOpenHashMap short2BooleanOpenHashMap = new Short2BooleanOpenHashMap();
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            BlockPos relative = blockPos.relative(next);
            short cacheKey = getCacheKey(blockPos, relative);
            Pair<BlockState, FluidState> pair = (Pair) short2ObjectOpenHashMap.computeIfAbsent(cacheKey, i2 -> {
                BlockState blockState2 = levelReader.getBlockState(relative);
                return Pair.of(blockState2, blockState2.getFluidState());
            });
            BlockState blockState2 =  pair.getFirst();
            FluidState fluidState =  pair.getSecond();
            FluidState newLiquid = getNewLiquid(levelReader, relative, blockState2);
            if (canPassThrough(levelReader, newLiquid.getType(), blockPos, blockState, next, relative, blockState2, fluidState)) {
                BlockPos below = relative.below();
                if (short2BooleanOpenHashMap.computeIfAbsent(cacheKey, i3 -> {
                    return isWaterHole(levelReader, getFlowing(), relative, blockState2, below, levelReader.getBlockState(below));
                })) {
                    slopeDistance = 0;
                } else {
                    slopeDistance = getSlopeDistance(levelReader, relative, 1, next.getOpposite(), blockState2, blockPos, short2ObjectOpenHashMap, short2BooleanOpenHashMap);
                }
                if (slopeDistance < i) {
                    newEnumMap.clear();
                }
                if (slopeDistance <= i) {
                    newEnumMap.put(next, newLiquid);
                    i = slopeDistance;
                }
            }
        }
        return newEnumMap;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        Material material;
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(blockGetter, blockPos, blockState, fluid);
        }
        return ((block instanceof DoorBlock) || block.is(BlockTags.SIGNS) || block == Blocks.LADDER || block == Blocks.SUGAR_CANE || block == Blocks.BUBBLE_COLUMN || (material = blockState.getMaterial()) == Material.PORTAL || material == Material.STRUCTURAL_AIR || material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT || material.blocksMotion()) ? false : true;
    }

    protected boolean canSpreadTo(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, Fluid fluid) {
        return fluidState.canBeReplacedWith(blockGetter, blockPos2, fluid, direction) && canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2) && canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    protected int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        return getTickDelay(level);
    }

    @Override // net.minecraft.world.level.material.Fluid
    public void tick(Level level, BlockPos blockPos, FluidState fluidState) {
        if (!fluidState.isSource()) {
            FluidState newLiquid = getNewLiquid(level, blockPos, level.getBlockState(blockPos));
            int spreadDelay = getSpreadDelay(level, blockPos, fluidState, newLiquid);
            if (newLiquid.isEmpty()) {
                fluidState = newLiquid;
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            } else if (!newLiquid.equals(fluidState)) {
                fluidState = newLiquid;
                BlockState createLegacyBlock = fluidState.createLegacyBlock();
                level.setBlock(blockPos, createLegacyBlock, 2);
                level.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), spreadDelay);
                level.updateNeighborsAt(blockPos, createLegacyBlock.getBlock());
            }
        }
        spread(level, blockPos, fluidState);
    }

    protected static int getLegacyLevel(FluidState fluidState) {
        if (fluidState.isSource()) {
            return 0;
        }
        return (8 - Math.min(fluidState.getAmount(), 8)) + (((Boolean) fluidState.getValue(FALLING)).booleanValue() ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return fluidState.getType().isSame(blockGetter.getFluidState(blockPos.above()).getType());
    }

    @Override // net.minecraft.world.level.material.Fluid
    public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (hasSameAbove(fluidState, blockGetter, blockPos)) {
            return 1.0f;
        }
        return fluidState.getOwnHeight();
    }

    @Override // net.minecraft.world.level.material.Fluid
    public float getOwnHeight(FluidState fluidState) {
        return fluidState.getAmount() / 9.0f;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public VoxelShape getShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (fluidState.getAmount() == 9 && hasSameAbove(fluidState, blockGetter, blockPos)) {
            return Shapes.block();
        }
        return this.shapes.computeIfAbsent(fluidState, fluidState2 -> {
            return Shapes.box(0.0d, 0.0d, 0.0d, 1.0d, fluidState2.getHeight(blockGetter, blockPos), 1.0d);
        });
    }
}
