package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StructurePiece.class */
public abstract class StructurePiece {
    protected BoundingBox boundingBox;

    @Nullable
    private Direction orientation;
    private Mirror mirror;
    private Rotation rotation;
    protected int genDepth;
    private final StructurePieceType type;
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();

    protected abstract void addAdditionalSaveData(CompoundTag compoundTag);

    public abstract boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos);

    protected StructurePiece(StructurePieceType structurePieceType, int i) {
        this.type = structurePieceType;
        this.genDepth = i;
    }

    public StructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
        this(structurePieceType, compoundTag.getInt("GD"));
        if (compoundTag.contains("BB")) {
            this.boundingBox = new BoundingBox(compoundTag.getIntArray("BB"));
        }
        int i = compoundTag.getInt("O");
        setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
    }

    public final CompoundTag createTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("id", Registry.STRUCTURE_PIECE.getKey(getType()).toString());
        compoundTag.put("BB", this.boundingBox.createTag());
        Direction orientation = getOrientation();
        compoundTag.putInt("O", orientation == null ? -1 : orientation.get2DDataValue());
        compoundTag.putInt("GD", this.genDepth);
        addAdditionalSaveData(compoundTag);
        return compoundTag;
    }

    public void addChildren(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public int getGenDepth() {
        return this.genDepth;
    }

    public boolean isCloseToChunk(ChunkPos chunkPos, int i) {
        int i2 = chunkPos.x << 4;
        int i3 = chunkPos.z << 4;
        return this.boundingBox.intersects(i2 - i, i3 - i, i2 + 15 + i, i3 + 15 + i);
    }

    public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingBox) {
        for (StructurePiece structurePiece : list) {
            if (structurePiece.getBoundingBox() != null && structurePiece.getBoundingBox().intersects(boundingBox)) {
                return structurePiece;
            }
        }
        return null;
    }

    protected boolean edgesLiquid(BlockGetter blockGetter, BoundingBox boundingBox) {
        int max = Math.max(this.boundingBox.x0 - 1, boundingBox.x0);
        int max2 = Math.max(this.boundingBox.y0 - 1, boundingBox.y0);
        int max3 = Math.max(this.boundingBox.z0 - 1, boundingBox.z0);
        int min = Math.min(this.boundingBox.x1 + 1, boundingBox.x1);
        int min2 = Math.min(this.boundingBox.y1 + 1, boundingBox.y1);
        int min3 = Math.min(this.boundingBox.z1 + 1, boundingBox.z1);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = max; i <= min; i++) {
            for (int i2 = max3; i2 <= min3; i2++) {
                if (blockGetter.getBlockState(mutableBlockPos.set(i, max2, i2)).getMaterial().isLiquid() || blockGetter.getBlockState(mutableBlockPos.set(i, min2, i2)).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        for (int i3 = max; i3 <= min; i3++) {
            for (int i4 = max2; i4 <= min2; i4++) {
                if (blockGetter.getBlockState(mutableBlockPos.set(i3, i4, max3)).getMaterial().isLiquid() || blockGetter.getBlockState(mutableBlockPos.set(i3, i4, min3)).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        for (int i5 = max3; i5 <= min3; i5++) {
            for (int i6 = max2; i6 <= min2; i6++) {
                if (blockGetter.getBlockState(mutableBlockPos.set(max, i6, i5)).getMaterial().isLiquid() || blockGetter.getBlockState(mutableBlockPos.set(min, i6, i5)).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected int getWorldX(int i, int i2) {
        Direction orientation = getOrientation();
        if (orientation == null) {
            return i;
        }
        switch (orientation) {
            case NORTH:
            case SOUTH:
                return this.boundingBox.x0 + i;
            case WEST:
                return this.boundingBox.x1 - i2;
            case EAST:
                return this.boundingBox.x0 + i2;
            default:
                return i;
        }
    }

    protected int getWorldY(int i) {
        if (getOrientation() == null) {
            return i;
        }
        return i + this.boundingBox.y0;
    }

    protected int getWorldZ(int i, int i2) {
        Direction orientation = getOrientation();
        if (orientation == null) {
            return i2;
        }
        switch (orientation) {
            case NORTH:
                return this.boundingBox.z1 - i2;
            case SOUTH:
                return this.boundingBox.z0 + i2;
            case WEST:
            case EAST:
                return this.boundingBox.z0 + i;
            default:
                return i2;
        }
    }

    protected void placeBlock(WorldGenLevel worldGenLevel, BlockState blockState, int i, int i2, int i3, BoundingBox boundingBox) {
        BlockPos blockPos = new BlockPos(getWorldX(i, i3), getWorldY(i2), getWorldZ(i, i3));
        if (!boundingBox.isInside(blockPos)) {
            return;
        }
        if (this.mirror != Mirror.NONE) {
            blockState = blockState.mirror(this.mirror);
        }
        if (this.rotation != Rotation.NONE) {
            blockState = blockState.rotate(this.rotation);
        }
        worldGenLevel.setBlock(blockPos, blockState, 2);
        FluidState fluidState = worldGenLevel.getFluidState(blockPos);
        if (!fluidState.isEmpty()) {
            worldGenLevel.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), 0);
        }
        if (SHAPE_CHECK_BLOCKS.contains(blockState.getBlock())) {
            worldGenLevel.getChunk(blockPos).markPosForPostprocessing(blockPos);
        }
    }

    protected BlockState getBlock(BlockGetter blockGetter, int i, int i2, int i3, BoundingBox boundingBox) {
        BlockPos blockPos = new BlockPos(getWorldX(i, i3), getWorldY(i2), getWorldZ(i, i3));
        if (!boundingBox.isInside(blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockGetter.getBlockState(blockPos);
    }

    protected boolean isInterior(LevelReader levelReader, int i, int i2, int i3, BoundingBox boundingBox) {
        int worldX = getWorldX(i, i3);
        int worldY = getWorldY(i2 + 1);
        int worldZ = getWorldZ(i, i3);
        return boundingBox.isInside(new BlockPos(worldX, worldY, worldZ)) && worldY < levelReader.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, worldX, worldZ);
    }

    protected void generateAirBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6) {
        for (int i7 = i2; i7 <= i5; i7++) {
            for (int i8 = i; i8 <= i4; i8++) {
                for (int i9 = i3; i9 <= i6; i9++) {
                    placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), i8, i7, i9, boundingBox);
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6, BlockState blockState, BlockState blockState2, boolean z) {
        for (int i7 = i2; i7 <= i5; i7++) {
            for (int i8 = i; i8 <= i4; i8++) {
                for (int i9 = i3; i9 <= i6; i9++) {
                    if (!z || !getBlock(worldGenLevel, i8, i7, i9, boundingBox).isAir()) {
                        if (i7 == i2 || i7 == i5 || i8 == i || i8 == i4 || i9 == i3 || i9 == i6) {
                            placeBlock(worldGenLevel, blockState, i8, i7, i9, boundingBox);
                        } else {
                            placeBlock(worldGenLevel, blockState2, i8, i7, i9, boundingBox);
                        }
                    }
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6, boolean z, Random random, BlockSelector blockSelector) {
        int i7 = i2;
        while (i7 <= i5) {
            int i8 = i;
            while (i8 <= i4) {
                int i9 = i3;
                while (i9 <= i6) {
                    if (!z || !getBlock(worldGenLevel, i8, i7, i9, boundingBox).isAir()) {
                        blockSelector.next(random, i8, i7, i9, i7 == i2 || i7 == i5 || i8 == i || i8 == i4 || i9 == i3 || i9 == i6);
                        placeBlock(worldGenLevel, blockSelector.getNext(), i8, i7, i9, boundingBox);
                    }
                    i9++;
                }
                i8++;
            }
            i7++;
        }
    }

    protected void generateMaybeBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, float f, int i, int i2, int i3, int i4, int i5, int i6, BlockState blockState, BlockState blockState2, boolean z, boolean z2) {
        for (int i7 = i2; i7 <= i5; i7++) {
            for (int i8 = i; i8 <= i4; i8++) {
                for (int i9 = i3; i9 <= i6; i9++) {
                    if (random.nextFloat() <= f && ((!z || !getBlock(worldGenLevel, i8, i7, i9, boundingBox).isAir()) && (!z2 || isInterior(worldGenLevel, i8, i7, i9, boundingBox)))) {
                        if (i7 == i2 || i7 == i5 || i8 == i || i8 == i4 || i9 == i3 || i9 == i6) {
                            placeBlock(worldGenLevel, blockState, i8, i7, i9, boundingBox);
                        } else {
                            placeBlock(worldGenLevel, blockState2, i8, i7, i9, boundingBox);
                        }
                    }
                }
            }
        }
    }

    protected void maybeGenerateBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, float f, int i, int i2, int i3, BlockState blockState) {
        if (random.nextFloat() < f) {
            placeBlock(worldGenLevel, blockState, i, i2, i3, boundingBox);
        }
    }

    protected void generateUpperHalfSphere(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int i, int i2, int i3, int i4, int i5, int i6, BlockState blockState, boolean z) {
        float f = (i4 - i) + 1;
        float f2 = (i5 - i2) + 1;
        float f3 = (i6 - i3) + 1;
        float f4 = i + (f / 2.0f);
        float f5 = i3 + (f3 / 2.0f);
        for (int i7 = i2; i7 <= i5; i7++) {
            float f6 = (i7 - i2) / f2;
            for (int i8 = i; i8 <= i4; i8++) {
                float f7 = (i8 - f4) / (f * 0.5f);
                for (int i9 = i3; i9 <= i6; i9++) {
                    float f8 = (i9 - f5) / (f3 * 0.5f);
                    if ((!z || !getBlock(worldGenLevel, i8, i7, i9, boundingBox).isAir()) && (f7 * f7) + (f6 * f6) + (f8 * f8) <= 1.05f) {
                        placeBlock(worldGenLevel, blockState, i8, i7, i9, boundingBox);
                    }
                }
            }
        }
    }

    protected void fillColumnDown(WorldGenLevel worldGenLevel, BlockState blockState, int i, int i2, int i3, BoundingBox boundingBox) {
        int worldX = getWorldX(i, i3);
        int worldY = getWorldY(i2);
        int worldZ = getWorldZ(i, i3);
        if (!boundingBox.isInside(new BlockPos(worldX, worldY, worldZ))) {
            return;
        }
        while (true) {
            if ((worldGenLevel.isEmptyBlock(new BlockPos(worldX, worldY, worldZ)) || worldGenLevel.getBlockState(new BlockPos(worldX, worldY, worldZ)).getMaterial().isLiquid()) && worldY > 1) {
                worldGenLevel.setBlock(new BlockPos(worldX, worldY, worldZ), blockState, 2);
                worldY--;
            } else {
                return;
            }
        }
    }

    protected boolean createChest(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, int i, int i2, int i3, ResourceLocation resourceLocation) {
        return createChest(worldGenLevel, boundingBox, random, new BlockPos(getWorldX(i, i3), getWorldY(i2), getWorldZ(i, i3)), resourceLocation, null);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x005d, code lost:
    
        if (r7 == null) goto L20;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x006e, code lost:
    
        return (net.minecraft.world.level.block.state.BlockState) r6.setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, r7.getOpposite());
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x006f, code lost:
    
        r8 = (net.minecraft.core.Direction) r6.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
        r9 = r5.relative(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x0091, code lost:
    
        if (r4.getBlockState(r9).isSolidRender(r4, r9) == false) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x0094, code lost:
    
        r8 = r8.getOpposite();
        r9 = r5.relative(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x00b1, code lost:
    
        if (r4.getBlockState(r9).isSolidRender(r4, r9) == false) goto L26;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x00b4, code lost:
    
        r8 = r8.getClockWise();
        r9 = r5.relative(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x00d1, code lost:
    
        if (r4.getBlockState(r9).isSolidRender(r4, r9) == false) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:29:0x00d4, code lost:
    
        r8 = r8.getOpposite();
        r0 = r5.relative(r8);
     */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x00ef, code lost:
    
        return (net.minecraft.world.level.block.state.BlockState) r6.setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, r8);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static net.minecraft.world.level.block.state.BlockState reorient(net.minecraft.world.level.BlockGetter var0, net.minecraft.core.BlockPos var1, net.minecraft.world.level.block.state.BlockState var2) {
        Direction var3 = null;

        for(Direction var5 : Direction.Plane.HORIZONTAL) {
            BlockPos var6x = var1.relative(var5);
            BlockState var7xx = var0.getBlockState(var6x);
            if (var7xx.is(Blocks.CHEST)) {
                return var2;
            }

            if (var7xx.isSolidRender(var0, var6x)) {
                if (var3 != null) {
                    var3 = null;
                    break;
                }

                var3 = var5;
            }
        }

        if (var3 != null) {
            return var2.setValue(HorizontalDirectionalBlock.FACING, var3.getOpposite());
        } else {
            Direction var8x = var2.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos var9xx = var1.relative(var8x);
            if (var0.getBlockState(var9xx).isSolidRender(var0, var9xx)) {
                var8x = var8x.getOpposite();
                var9xx = var1.relative(var8x);
            }

            if (var0.getBlockState(var9xx).isSolidRender(var0, var9xx)) {
                var8x = var8x.getClockWise();
                var9xx = var1.relative(var8x);
            }

            if (var0.getBlockState(var9xx).isSolidRender(var0, var9xx)) {
                var8x = var8x.getOpposite();
            var9xx = var1.relative(var8x);
            }

            return var2.setValue(HorizontalDirectionalBlock.FACING, var8x);
        }
    }

    protected boolean createChest(ServerLevelAccessor serverLevelAccessor, BoundingBox boundingBox, Random random, BlockPos blockPos, ResourceLocation resourceLocation, @Nullable BlockState blockState) {
        if (!boundingBox.isInside(blockPos) || serverLevelAccessor.getBlockState(blockPos).is(Blocks.CHEST)) {
            return false;
        }
        if (blockState == null) {
            blockState = reorient(serverLevelAccessor, blockPos, Blocks.CHEST.defaultBlockState());
        }
        serverLevelAccessor.setBlock(blockPos, blockState, 2);
        BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
        if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity) blockEntity).setLootTable(resourceLocation, random.nextLong());
            return true;
        }
        return true;
    }

    protected boolean createDispenser(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, int i, int i2, int i3, Direction direction, ResourceLocation resourceLocation) {
        BlockPos blockPos = new BlockPos(getWorldX(i, i3), getWorldY(i2), getWorldZ(i, i3));
        if (boundingBox.isInside(blockPos) && !worldGenLevel.getBlockState(blockPos).is(Blocks.DISPENSER)) {
            placeBlock(worldGenLevel, (BlockState) Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction), i, i2, i3, boundingBox);
            BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);
            if (blockEntity instanceof DispenserBlockEntity) {
                ((DispenserBlockEntity) blockEntity).setLootTable(resourceLocation, random.nextLong());
                return true;
            }
            return true;
        }
        return false;
    }

    public void move(int i, int i2, int i3) {
        this.boundingBox.move(i, i2, i3);
    }

    @Nullable
    public Direction getOrientation() {
        return this.orientation;
    }

    public void setOrientation(@Nullable Direction direction) {
        this.orientation = direction;
        if (direction == null) {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
            return;
        }
        switch (direction) {
            case SOUTH:
                this.mirror = Mirror.LEFT_RIGHT;
                this.rotation = Rotation.NONE;
                break;
            case WEST:
                this.mirror = Mirror.LEFT_RIGHT;
                this.rotation = Rotation.CLOCKWISE_90;
                break;
            case EAST:
                this.mirror = Mirror.NONE;
                this.rotation = Rotation.CLOCKWISE_90;
                break;
            default:
                this.mirror = Mirror.NONE;
                this.rotation = Rotation.NONE;
                break;
        }
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public StructurePieceType getType() {
        return this.type;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StructurePiece$BlockSelector.class */
    public static abstract class BlockSelector {
        protected BlockState next = Blocks.AIR.defaultBlockState();

        public abstract void next(Random random, int i, int i2, int i3, boolean z);

        protected BlockSelector() {
        }

        public BlockState getNext() {
            return this.next;
        }
    }
}
