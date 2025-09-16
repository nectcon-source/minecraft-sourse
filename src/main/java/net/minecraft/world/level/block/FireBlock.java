package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FireBlock.class */
public class FireBlock extends BaseFireBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;

    /* renamed from: UP */
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map) PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter(entry -> {
        return entry.getKey() != Direction.DOWN;
    }).collect(Util.toMap());
    private static final VoxelShape UP_AABB = Block.box(0.0d, 15.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape WEST_AABB = Block.box(0.0d, 0.0d, 0.0d, 1.0d, 16.0d, 16.0d);
    private static final VoxelShape EAST_AABB = Block.box(15.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape NORTH_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 1.0d);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0d, 0.0d, 15.0d, 16.0d, 16.0d, 16.0d);
    private final Map<BlockState, VoxelShape> shapesCache;
    private final Object2IntMap<Block> flameOdds;
    private final Object2IntMap<Block> burnOdds;

    public FireBlock(BlockBehaviour.Properties properties) {
        super(properties, 1.0f);
        this.flameOdds = new Object2IntOpenHashMap();
        this.burnOdds = new Object2IntOpenHashMap();
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(AGE, 0)).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(UP, false));
        this.shapesCache = ImmutableMap.copyOf((Map) this.stateDefinition.getPossibleStates().stream().filter(blockState -> {
            return ((Integer) blockState.getValue(AGE)).intValue() == 0;
        }).collect(Collectors.toMap(Function.identity(), FireBlock::calculateShape)));
    }

    private static VoxelShape calculateShape(BlockState blockState) {
        VoxelShape empty = Shapes.empty();
        if (((Boolean) blockState.getValue(UP)).booleanValue()) {
            empty = UP_AABB;
        }
        if (((Boolean) blockState.getValue(NORTH)).booleanValue()) {
            empty = Shapes.or(empty, NORTH_AABB);
        }
        if (((Boolean) blockState.getValue(SOUTH)).booleanValue()) {
            empty = Shapes.or(empty, SOUTH_AABB);
        }
        if (((Boolean) blockState.getValue(EAST)).booleanValue()) {
            empty = Shapes.or(empty, EAST_AABB);
        }
        if (((Boolean) blockState.getValue(WEST)).booleanValue()) {
            empty = Shapes.or(empty, WEST_AABB);
        }
        return empty.isEmpty() ? DOWN_AABB : empty;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (canSurvive(blockState, levelAccessor, blockPos)) {
            return getStateWithAge(levelAccessor, blockPos, ((Integer) blockState.getValue(AGE)).intValue());
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.block.BaseFireBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapesCache.get(blockState.setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.BaseFireBlock, net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getStateForPlacement(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    protected BlockState getStateForPlacement(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        BlockState blockState = blockGetter.getBlockState(below);
        if (canBurn(blockState) || blockState.isFaceSturdy(blockGetter, below, Direction.UP)) {
            return defaultBlockState();
        }
        BlockState defaultBlockState = defaultBlockState();
        for (Direction direction : Direction.values()) {
            BooleanProperty booleanProperty = PROPERTY_BY_DIRECTION.get(direction);
            if (booleanProperty != null) {
                defaultBlockState = (BlockState) defaultBlockState.setValue(booleanProperty, Boolean.valueOf(canBurn(blockGetter.getBlockState(blockPos.relative(direction)))));
            }
        }
        return defaultBlockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos below = blockPos.below();
        return levelReader.getBlockState(below).isFaceSturdy(levelReader, below, Direction.UP) || isValidFireLocation(levelReader, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        serverLevel.getBlockTicks().scheduleTick(blockPos, this, getFireTickDelay(serverLevel.random));
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.removeBlock(blockPos, false);
        }
        boolean is = serverLevel.getBlockState(blockPos.below()).is(serverLevel.dimensionType().infiniburn());
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        if (!is && serverLevel.isRaining() && isNearRain(serverLevel, blockPos) && random.nextFloat() < 0.2f + (intValue * 0.03f)) {
            serverLevel.removeBlock(blockPos, false);
            return;
        }
        int min = Math.min(15, intValue + (random.nextInt(3) / 2));
        if (intValue != min) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(min)), 4);
        }
        if (!is) {
            if (!isValidFireLocation(serverLevel, blockPos)) {
                BlockPos below = blockPos.below();
                if (!serverLevel.getBlockState(below).isFaceSturdy(serverLevel, below, Direction.UP) || intValue > 3) {
                    serverLevel.removeBlock(blockPos, false);
                    return;
                }
                return;
            }
            if (intValue == 15 && random.nextInt(4) == 0 && !canBurn(serverLevel.getBlockState(blockPos.below()))) {
                serverLevel.removeBlock(blockPos, false);
                return;
            }
        }
        boolean isHumidAt = serverLevel.isHumidAt(blockPos);
        int i = isHumidAt ? -50 : 0;
        checkBurnOut(serverLevel, blockPos.east(), 300 + i, random, intValue);
        checkBurnOut(serverLevel, blockPos.west(), 300 + i, random, intValue);
        checkBurnOut(serverLevel, blockPos.below(), 250 + i, random, intValue);
        checkBurnOut(serverLevel, blockPos.above(), 250 + i, random, intValue);
        checkBurnOut(serverLevel, blockPos.north(), 300 + i, random, intValue);
        checkBurnOut(serverLevel, blockPos.south(), 300 + i, random, intValue);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i2 = -1; i2 <= 1; i2++) {
            for (int i3 = -1; i3 <= 1; i3++) {
                for (int i4 = -1; i4 <= 4; i4++) {
                    if (i2 != 0 || i4 != 0 || i3 != 0) {
                        int i5 = 100;
                        if (i4 > 1) {
                            i5 = 100 + ((i4 - 1) * 100);
                        }
                        mutableBlockPos.setWithOffset(blockPos, i2, i4, i3);
                        int fireOdds = getFireOdds(serverLevel, mutableBlockPos);
                        if (fireOdds > 0) {
                            int id = ((fireOdds + 40) + (serverLevel.getDifficulty().getId() * 7)) / (intValue + 30);
                            if (isHumidAt) {
                                id /= 2;
                            }
                            if (id > 0 && random.nextInt(i5) <= id && (!serverLevel.isRaining() || !isNearRain(serverLevel, mutableBlockPos))) {
                                serverLevel.setBlock(mutableBlockPos, getStateWithAge(serverLevel, mutableBlockPos, Math.min(15, intValue + (random.nextInt(5) / 4))), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isNearRain(Level level, BlockPos blockPos) {
        return level.isRainingAt(blockPos) || level.isRainingAt(blockPos.west()) || level.isRainingAt(blockPos.east()) || level.isRainingAt(blockPos.north()) || level.isRainingAt(blockPos.south());
    }

    private int getBurnOdd(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && ((Boolean) blockState.getValue(BlockStateProperties.WATERLOGGED)).booleanValue()) {
            return 0;
        }
        return this.burnOdds.getInt(blockState.getBlock());
    }

    private int getFlameOdds(BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && ((Boolean) blockState.getValue(BlockStateProperties.WATERLOGGED)).booleanValue()) {
            return 0;
        }
        return this.flameOdds.getInt(blockState.getBlock());
    }

    private void checkBurnOut(Level level, BlockPos blockPos, int i, Random random, int i2) {
        if (random.nextInt(i) < getBurnOdd(level.getBlockState(blockPos))) {
            BlockState blockState = level.getBlockState(blockPos);
            if (random.nextInt(i2 + 10) < 5 && !level.isRainingAt(blockPos)) {
                level.setBlock(blockPos, getStateWithAge(level, blockPos, Math.min(i2 + (random.nextInt(5) / 4), 15)), 3);
            } else {
                level.removeBlock(blockPos, false);
            }
            Block block = blockState.getBlock();
            if (block instanceof TntBlock) {
                TntBlock.explode(level, blockPos);
            }
        }
    }

    private BlockState getStateWithAge(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        BlockState state = getState(levelAccessor, blockPos);
        if (state.is(Blocks.FIRE)) {
            return (BlockState) state.setValue(AGE, Integer.valueOf(i));
        }
        return state;
    }

    private boolean isValidFireLocation(BlockGetter blockGetter, BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (canBurn(blockGetter.getBlockState(blockPos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private int getFireOdds(LevelReader levelReader, BlockPos blockPos) {
        if (!levelReader.isEmptyBlock(blockPos)) {
            return 0;
        }
        int i = 0;
        for (Direction direction : Direction.values()) {
            i = Math.max(getFlameOdds(levelReader.getBlockState(blockPos.relative(direction))), i);
        }
        return i;
    }

    @Override // net.minecraft.world.level.block.BaseFireBlock
    protected boolean canBurn(BlockState blockState) {
        return getFlameOdds(blockState) > 0;
    }

    @Override // net.minecraft.world.level.block.BaseFireBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        super.onPlace(blockState, level, blockPos, blockState2, z);
        level.getBlockTicks().scheduleTick(blockPos, this, getFireTickDelay(level.random));
    }

    private static int getFireTickDelay(Random random) {
        return 30 + random.nextInt(10);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    private void setFlammable(Block block, int i, int i2) {
        this.flameOdds.put(block, i);
        this.burnOdds.put(block, i2);
    }

    public static void bootStrap() {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        fireBlock.setFlammable(Blocks.OAK_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.BIRCH_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.ACACIA_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
        fireBlock.setFlammable(Blocks.OAK_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.SPRUCE_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.BIRCH_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.JUNGLE_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.ACACIA_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
        fireBlock.setFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
        fireBlock.setFlammable(Blocks.OAK_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.SPRUCE_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.BIRCH_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.JUNGLE_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.ACACIA_FENCE, 5, 20);
        fireBlock.setFlammable(Blocks.OAK_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.BIRCH_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.ACACIA_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
        fireBlock.setFlammable(Blocks.OAK_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.SPRUCE_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.BIRCH_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.JUNGLE_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.ACACIA_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.DARK_OAK_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.OAK_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.SPRUCE_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.BIRCH_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.JUNGLE_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.ACACIA_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
        fireBlock.setFlammable(Blocks.OAK_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.BIRCH_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.ACACIA_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
        fireBlock.setFlammable(Blocks.BOOKSHELF, 30, 20);
        fireBlock.setFlammable(Blocks.TNT, 15, 100);
        fireBlock.setFlammable(Blocks.GRASS, 60, 100);
        fireBlock.setFlammable(Blocks.FERN, 60, 100);
        fireBlock.setFlammable(Blocks.DEAD_BUSH, 60, 100);
        fireBlock.setFlammable(Blocks.SUNFLOWER, 60, 100);
        fireBlock.setFlammable(Blocks.LILAC, 60, 100);
        fireBlock.setFlammable(Blocks.ROSE_BUSH, 60, 100);
        fireBlock.setFlammable(Blocks.PEONY, 60, 100);
        fireBlock.setFlammable(Blocks.TALL_GRASS, 60, 100);
        fireBlock.setFlammable(Blocks.LARGE_FERN, 60, 100);
        fireBlock.setFlammable(Blocks.DANDELION, 60, 100);
        fireBlock.setFlammable(Blocks.POPPY, 60, 100);
        fireBlock.setFlammable(Blocks.BLUE_ORCHID, 60, 100);
        fireBlock.setFlammable(Blocks.ALLIUM, 60, 100);
        fireBlock.setFlammable(Blocks.AZURE_BLUET, 60, 100);
        fireBlock.setFlammable(Blocks.RED_TULIP, 60, 100);
        fireBlock.setFlammable(Blocks.ORANGE_TULIP, 60, 100);
        fireBlock.setFlammable(Blocks.WHITE_TULIP, 60, 100);
        fireBlock.setFlammable(Blocks.PINK_TULIP, 60, 100);
        fireBlock.setFlammable(Blocks.OXEYE_DAISY, 60, 100);
        fireBlock.setFlammable(Blocks.CORNFLOWER, 60, 100);
        fireBlock.setFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
        fireBlock.setFlammable(Blocks.WITHER_ROSE, 60, 100);
        fireBlock.setFlammable(Blocks.WHITE_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.ORANGE_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.MAGENTA_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.YELLOW_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.LIME_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.PINK_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.GRAY_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.CYAN_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.PURPLE_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.BLUE_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.BROWN_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.GREEN_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.RED_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.BLACK_WOOL, 30, 60);
        fireBlock.setFlammable(Blocks.VINE, 15, 100);
        fireBlock.setFlammable(Blocks.COAL_BLOCK, 5, 5);
        fireBlock.setFlammable(Blocks.HAY_BLOCK, 60, 20);
        fireBlock.setFlammable(Blocks.TARGET, 15, 20);
        fireBlock.setFlammable(Blocks.WHITE_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.ORANGE_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.MAGENTA_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.YELLOW_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.LIME_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.PINK_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.GRAY_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.CYAN_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.PURPLE_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.BLUE_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.BROWN_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.GREEN_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.RED_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.BLACK_CARPET, 60, 20);
        fireBlock.setFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
        fireBlock.setFlammable(Blocks.BAMBOO, 60, 60);
        fireBlock.setFlammable(Blocks.SCAFFOLDING, 60, 60);
        fireBlock.setFlammable(Blocks.LECTERN, 30, 20);
        fireBlock.setFlammable(Blocks.COMPOSTER, 5, 20);
        fireBlock.setFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
        fireBlock.setFlammable(Blocks.BEEHIVE, 5, 20);
        fireBlock.setFlammable(Blocks.BEE_NEST, 30, 20);
    }
}
