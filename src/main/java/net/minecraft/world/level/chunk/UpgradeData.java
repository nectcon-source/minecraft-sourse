package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/UpgradeData.class */
public class UpgradeData {
    private final EnumSet<Direction8> sides;
    private final int[][] index;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final UpgradeData EMPTY = new UpgradeData();
    private static final Direction8[] DIRECTIONS = Direction8.values();
    private static final Map<Block, BlockFixer> MAP = new IdentityHashMap();
    private static final Set<BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

    /* JADX WARN: Type inference failed for: r1v3, types: [int[], int[][]] */
    private UpgradeData() {
        this.sides = EnumSet.noneOf(Direction8.class);
        this.index = new int[][]{new int[16]};
    }

    public UpgradeData(CompoundTag compoundTag) {
        this();
        if (compoundTag.contains("Indices", 10)) {
            CompoundTag compound = compoundTag.getCompound("Indices");
            for (int i = 0; i < this.index.length; i++) {
                String valueOf = String.valueOf(i);
                if (compound.contains(valueOf, 11)) {
                    this.index[i] = compound.getIntArray(valueOf);
                }
            }
        }
        int i2 = compoundTag.getInt("Sides");
        for (Direction8 direction8 : Direction8.values()) {
            if ((i2 & (1 << direction8.ordinal())) != 0) {
                this.sides.add(direction8);
            }
        }
    }

    public void upgrade(LevelChunk levelChunk) {
        upgradeInside(levelChunk);
        for (Direction8 direction8 : DIRECTIONS) {
            upgradeSides(levelChunk, direction8);
        }
        Level level = levelChunk.getLevel();
        CHUNKY_FIXERS.forEach(blockFixer -> {
            blockFixer.processChunk(level);
        });
    }

    private static void upgradeSides(LevelChunk levelChunk, Direction8 direction8) {
        Level level = levelChunk.getLevel();
        if (!levelChunk.getUpgradeData().sides.remove(direction8)) {
            return;
        }
        Set<Direction> directions = direction8.getDirections();
        boolean contains = directions.contains(Direction.EAST);
        boolean contains2 = directions.contains(Direction.WEST);
        boolean contains3 = directions.contains(Direction.SOUTH);
        boolean contains4 = directions.contains(Direction.NORTH);
        boolean z = directions.size() == 1;
        ChunkPos pos = levelChunk.getPos();
        int minBlockX = pos.getMinBlockX() + ((z && (contains4 || contains3)) ? 1 : contains2 ? 0 : 15);
        int minBlockX2 = pos.getMinBlockX() + ((z && (contains4 || contains3)) ? 14 : contains2 ? 0 : 15);
        int minBlockZ = pos.getMinBlockZ() + ((z && (contains || contains2)) ? 1 : contains4 ? 0 : 15);
        int minBlockZ2 = pos.getMinBlockZ() + ((z && (contains || contains2)) ? 14 : contains4 ? 0 : 15);
        Direction[] values = Direction.values();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : BlockPos.betweenClosed(minBlockX, 0, minBlockZ, minBlockX2, level.getMaxBuildHeight() - 1, minBlockZ2)) {
            BlockState blockState = level.getBlockState(blockPos);
            BlockState blockState2 = blockState;
            for (Direction direction : values) {
                mutableBlockPos.setWithOffset(blockPos, direction);
                blockState2 = updateState(blockState2, direction, level, blockPos, mutableBlockPos);
            }
            Block.updateOrDestroy(blockState, blockState2, level, blockPos, 18);
        }
    }

    private static BlockState updateState(BlockState blockState, Direction direction, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return MAP.getOrDefault(blockState.getBlock(), BlockFixers.DEFAULT).updateShape(blockState, direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
    }

    private void upgradeInside(LevelChunk levelChunk) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        ChunkPos pos = levelChunk.getPos();
        LevelAccessor level = levelChunk.getLevel();
        for (int i = 0; i < 16; i++) {
            LevelChunkSection levelChunkSection = levelChunk.getSections()[i];
            int[] iArr = this.index[i];
            this.index[i] = null;
            if (levelChunkSection != null && iArr != null && iArr.length > 0) {
                Direction[] values = Direction.values();
                PalettedContainer<BlockState> states = levelChunkSection.getStates();
                for (int i2 : iArr) {
                    mutableBlockPos.set(pos.getMinBlockX() + (i2 & 15), (i << 4) + ((i2 >> 8) & 15), pos.getMinBlockZ() + ((i2 >> 4) & 15));
                    BlockState blockState = states.get(i2);
                    BlockState blockState2 = blockState;
                    for (Direction direction : values) {
                        mutableBlockPos2.setWithOffset(mutableBlockPos, direction);
                        if ((mutableBlockPos.getX() >> 4) == pos.x && (mutableBlockPos.getZ() >> 4) == pos.z) {
                            blockState2 = updateState(blockState2, direction, level, mutableBlockPos, mutableBlockPos2);
                        }
                    }
                    Block.updateOrDestroy(blockState, blockState2, level, mutableBlockPos, 18);
                }
            }
        }
        for (int i3 = 0; i3 < this.index.length; i3++) {
            if (this.index[i3] != null) {
                LOGGER.warn("Discarding update data for section {} for chunk ({} {})", Integer.valueOf(i3), Integer.valueOf(pos.x), Integer.valueOf(pos.z));
            }
            this.index[i3] = null;
        }
    }

    public boolean isEmpty() {
        for (int[] iArr : this.index) {
            if (iArr != null) {
                return false;
            }
        }
        return this.sides.isEmpty();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/UpgradeData$BlockFixer.class */
    public interface BlockFixer {
        BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2);

        default void processChunk(LevelAccessor levelAccessor) {
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/UpgradeData$BlockFixers.class */
    enum BlockFixers implements BlockFixer {
        BLACKLIST(Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN) { // from class: net.minecraft.world.level.chunk.UpgradeData.BlockFixers.1
            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState;
            }
        },
        DEFAULT(new Block[0]) { // from class: net.minecraft.world.level.chunk.UpgradeData.BlockFixers.2
            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                return blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
            }
        },
        CHEST(Blocks.CHEST, Blocks.TRAPPED_CHEST) { // from class: net.minecraft.world.level.chunk.UpgradeData.BlockFixers.3
            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                if (blockState2.is(blockState.getBlock()) && direction.getAxis().isHorizontal() && blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE && blockState2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                    Direction direction2 = (Direction) blockState.getValue(ChestBlock.FACING);
                    if (direction.getAxis() != direction2.getAxis() && direction2 == blockState2.getValue(ChestBlock.FACING)) {
                        ChestType chestType = direction == direction2.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                        levelAccessor.setBlock(blockPos2, (BlockState) blockState2.setValue(ChestBlock.TYPE, chestType.getOpposite()), 18);
                        if (direction2 == Direction.NORTH || direction2 == Direction.EAST) {
                            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
                            BlockEntity blockEntity2 = levelAccessor.getBlockEntity(blockPos2);
                            if ((blockEntity instanceof ChestBlockEntity) && (blockEntity2 instanceof ChestBlockEntity)) {
                                ChestBlockEntity.swapContents((ChestBlockEntity) blockEntity, (ChestBlockEntity) blockEntity2);
                            }
                        }
                        return (BlockState) blockState.setValue(ChestBlock.TYPE, chestType);
                    }
                }
                return blockState;
            }
        },
        LEAVES(true, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES) { // from class: net.minecraft.world.level.chunk.UpgradeData.BlockFixers.4
            private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> {
                return Lists.newArrayListWithCapacity(7);
            });

            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                BlockState updateShape = blockState.updateShape(direction, levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos, blockPos2);
                if (blockState != updateShape) {
                    int intValue = ((Integer) updateShape.getValue(BlockStateProperties.DISTANCE)).intValue();
                    List<ObjectSet<BlockPos>> list = this.queue.get();
                    if (list.isEmpty()) {
                        for (int i = 0; i < 7; i++) {
                            list.add(new ObjectOpenHashSet());
                        }
                    }
                    list.get(intValue).add(blockPos.immutable());
                }
                return blockState;
            }

            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public void processChunk(LevelAccessor levelAccessor) {
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                List<ObjectSet<BlockPos>> list = this.queue.get();
                for (int i = 2; i < list.size(); i++) {
                    int i2 = i - 1;
                    ObjectSet<BlockPos> objectSet = list.get(i2);
                    ObjectSet<BlockPos> objectSet2 = list.get(i);
                    ObjectIterator it = objectSet.iterator();
                    while (it.hasNext()) {
                        BlockPos blockPos = (BlockPos) it.next();
                        BlockState blockState = levelAccessor.getBlockState(blockPos);
                        if (((Integer) blockState.getValue(BlockStateProperties.DISTANCE)).intValue() >= i2) {
                            levelAccessor.setBlock(blockPos, (BlockState) blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(i2)), 18);
                            if (i != 7) {
                                for (Direction direction : DIRECTIONS) {
                                    mutableBlockPos.setWithOffset(blockPos, direction);
                                    if (levelAccessor.getBlockState(mutableBlockPos).hasProperty(BlockStateProperties.DISTANCE) && ((Integer) blockState.getValue(BlockStateProperties.DISTANCE)).intValue() > i) {
                                        objectSet2.add(mutableBlockPos.immutable());
                                    }
                                }
                            }
                        }
                    }
                }
                list.clear();
            }
        },
        STEM_BLOCK(Blocks.MELON_STEM, Blocks.PUMPKIN_STEM) { // from class: net.minecraft.world.level.chunk.UpgradeData.BlockFixers.5
            @Override // net.minecraft.world.level.chunk.UpgradeData.BlockFixer
            public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
                if (((Integer) blockState.getValue(StemBlock.AGE)).intValue() == 7) {
                    StemGrownBlock fruit = ((StemBlock) blockState.getBlock()).getFruit();
                    if (blockState2.is(fruit)) {
                        return (BlockState) fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
                    }
                }
                return blockState;
            }
        };

        public static final Direction[] DIRECTIONS = Direction.values();

        BlockFixers(Block... blockArr) {
            this(false, blockArr);
        }

        BlockFixers(boolean z, Block... blockArr) {
            for (Block block : blockArr) {
                UpgradeData.MAP.put(block, this);
            }
            if (z) {
                UpgradeData.CHUNKY_FIXERS.add(this);
            }
        }
    }

    public CompoundTag write() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        for (int i = 0; i < this.index.length; i++) {
            String valueOf = String.valueOf(i);
            if (this.index[i] != null && this.index[i].length != 0) {
                compoundTag2.putIntArray(valueOf, this.index[i]);
            }
        }
        if (!compoundTag2.isEmpty()) {
            compoundTag.put("Indices", compoundTag2);
        }
        int i2 = 0;
        Iterator it = this.sides.iterator();
        while (it.hasNext()) {
            i2 |= 1 << ((Direction8) it.next()).ordinal();
        }
        compoundTag.putByte("Sides", (byte) i2);
        return compoundTag;
    }
}
