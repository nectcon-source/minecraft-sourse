package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/Block.class */
public class Block extends BlockBehaviour implements ItemLike {
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;

    @Nullable
    private String descriptionId;

    @Nullable
    private Item item;
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() { // from class: net.minecraft.world.level.block.Block.1
        public Boolean load(VoxelShape voxelShape) {
            return Boolean.valueOf(!Shapes.joinIsNotEmpty(Shapes.block(), voxelShape, BooleanOp.NOT_SAME));
        }
    });
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<BlockStatePairKey>(2048, 0.25f) { // from class: net.minecraft.world.level.block.Block.2
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue(Byte.MAX_VALUE);
        return object2ByteLinkedOpenHashMap;
    });

    public static int getId(@Nullable BlockState blockState) {
        int id;
        if (blockState == null || (id = BLOCK_STATE_REGISTRY.getId(blockState)) == -1) {
            return 0;
        }
        return id;
    }

    public static BlockState stateById(int i) {
        BlockState byId = BLOCK_STATE_REGISTRY.byId(i);
        return byId == null ? Blocks.AIR.defaultBlockState() : byId;
    }

    public static Block byItem(@Nullable Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem) item).getBlock();
        }
        return Blocks.AIR;
    }

    public static BlockState pushEntitiesUp(BlockState blockState, BlockState blockState2, Level level, BlockPos blockPos) {
        VoxelShape move = Shapes.joinUnoptimized(blockState.getCollisionShape(level, blockPos), blockState2.getCollisionShape(level, blockPos), BooleanOp.ONLY_SECOND).move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (Entity entity : level.getEntities(null, move.bounds())) {
            entity.teleportTo(entity.getX(), entity.getY() + 1.0d + Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0d, 1.0d, 0.0d), Stream.of(move), -1.0d), entity.getZ());
        }
        return blockState2;
    }

    public static VoxelShape box(double d, double d2, double d3, double d4, double d5, double d6) {
        return Shapes.box(d / 16.0d, d2 / 16.0d, d3 / 16.0d, d4 / 16.0d, d5 / 16.0d, d6 / 16.0d);
    }

    /* renamed from: is */
    public boolean is(Tag<Block> tag) {
        return tag.contains(this);
    }

    /* renamed from: is */
    public boolean is(Block block) {
        return this == block;
    }

    public static BlockState updateFromNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = blockState;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_SHAPE_ORDER) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            blockState2 = blockState2.updateShape(direction, levelAccessor.getBlockState(mutableBlockPos), levelAccessor, blockPos, mutableBlockPos);
        }
        return blockState2;
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        updateOrDestroy(blockState, blockState2, levelAccessor, blockPos, i, 512);
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2) {
        if (blockState2 != blockState) {
            if (blockState2.isAir()) {
                if (!levelAccessor.isClientSide()) {
                    levelAccessor.destroyBlock(blockPos, (i & 32) == 0, null, i2);
                    return;
                }
                return;
            }
            levelAccessor.setBlock(blockPos, blockState2, i & (-33), i2);
        }
    }

    public Block(BlockBehaviour.Properties properties) {
        super(properties);
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        registerDefaultState(this.stateDefinition.any());
    }

    public static boolean isExceptionForConnection(Block block) {
        return (block instanceof LeavesBlock) || block == Blocks.BARRIER || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN || block == Blocks.MELON || block == Blocks.PUMPKIN || block.is(BlockTags.SHULKER_BOXES);
    }

    public boolean isRandomlyTicking(BlockState blockState) {
        return this.isRandomlyTicking;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/Block$BlockStatePairKey.class */
    public static final class BlockStatePairKey {
        private final BlockState first;
        private final BlockState second;
        private final Direction direction;

        public BlockStatePairKey(BlockState blockState, BlockState blockState2, Direction direction) {
            this.first = blockState;
            this.second = blockState2;
            this.direction = direction;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BlockStatePairKey)) {
                return false;
            }
            BlockStatePairKey blockStatePairKey = (BlockStatePairKey) obj;
            return this.first == blockStatePairKey.first && this.second == blockStatePairKey.second && this.direction == blockStatePairKey.direction;
        }

        public int hashCode() {
            return (31 * ((31 * this.first.hashCode()) + this.second.hashCode())) + this.direction.hashCode();
        }
    }

    public static boolean shouldRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos relative = blockPos.relative(direction);
        BlockState blockState2 = blockGetter.getBlockState(relative);
        if (blockState.skipRendering(blockState2, direction)) {
            return false;
        }
        if (blockState2.canOcclude()) {
            BlockStatePairKey blockStatePairKey = new BlockStatePairKey(blockState, blockState2, direction);
            Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = OCCLUSION_CACHE.get();
            byte andMoveToFirst = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
            if (andMoveToFirst != Byte.MAX_VALUE) {
                return andMoveToFirst != 0;
            }
            boolean joinIsNotEmpty = Shapes.joinIsNotEmpty(blockState.getFaceOcclusionShape(blockGetter, blockPos, direction), blockState2.getFaceOcclusionShape(blockGetter, relative, direction.getOpposite()), BooleanOp.ONLY_FIRST);
            if (object2ByteLinkedOpenHashMap.size() == 2048) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte) (joinIsNotEmpty ? 1 : 0));
            return joinIsNotEmpty;
        }
        return true;
    }

    public static boolean canSupportRigidBlock(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos).isFaceSturdy(blockGetter, blockPos, Direction.UP, SupportType.RIGID);
    }

    public static boolean canSupportCenter(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        if (direction == Direction.DOWN && blockState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            return false;
        }
        return blockState.isFaceSturdy(levelReader, blockPos, direction, SupportType.CENTER);
    }

    public static boolean isFaceFull(VoxelShape voxelShape, Direction direction) {
        return isShapeFullBlock(voxelShape.getFaceShape(direction));
    }

    public static boolean isShapeFullBlock(VoxelShape voxelShape) {
        return ( SHAPE_FULL_BLOCK_CACHE.getUnchecked(voxelShape)).booleanValue();
    }

    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !isShapeFullBlock(blockState.getShape(blockGetter, blockPos)) && blockState.getFluidState().isEmpty();
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
    }

    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        return blockState.getDrops(new LootContext.Builder(serverLevel).withRandom(serverLevel.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity));
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack) {
        return blockState.getDrops(new LootContext.Builder(serverLevel).withRandom(serverLevel.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, itemStack).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity));
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            getDrops(blockState, (ServerLevel) level, blockPos, null).forEach(itemStack -> {
                popResource(level, blockPos, itemStack);
            });
            blockState.spawnAfterBreak((ServerLevel) level, blockPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        if (levelAccessor instanceof ServerLevel) {
            getDrops(blockState, (ServerLevel) levelAccessor, blockPos, blockEntity).forEach(itemStack -> {
                popResource((ServerLevel) levelAccessor, blockPos, itemStack);
            });
            blockState.spawnAfterBreak((ServerLevel) levelAccessor, blockPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack itemStack) {
        if (level instanceof ServerLevel) {
            getDrops(blockState, (ServerLevel) level, blockPos, blockEntity, entity, itemStack).forEach(itemStack2 -> {
                popResource(level, blockPos, itemStack2);
            });
            blockState.spawnAfterBreak((ServerLevel) level, blockPos, itemStack);
        }
    }

    public static void popResource(Level level, BlockPos blockPos, ItemStack itemStack) {
        if (level.isClientSide || itemStack.isEmpty() || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + (level.random.nextFloat() * 0.5f) + 0.25d, blockPos.getY() + (level.random.nextFloat() * 0.5f) + 0.25d, blockPos.getZ() + (level.random.nextFloat() * 0.5f) + 0.25d, itemStack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    protected void popExperience(ServerLevel serverLevel, BlockPos blockPos, int i) {
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            while (i > 0) {
                int experienceValue = ExperienceOrb.getExperienceValue(i);
                i -= experienceValue;
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, experienceValue));
            }
        }
    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
    }

    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState();
    }

    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005f);
        dropResources(blockState, level, blockPos, blockEntity, player, itemStack);
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
    }

    public boolean isPossibleToRespawnInThis() {
        return (this.material.isSolid() || this.material.isLiquid()) ? false : true;
    }

    public MutableComponent getName() {
        return new TranslatableComponent(getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }
        return this.descriptionId;
    }

    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        entity.causeFallDamage(f, 1.0f);
    }

    public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0d, 0.0d, 1.0d));
    }

    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(this);
    }

    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        nonNullList.add(new ItemStack(this));
    }

    public float getFriction() {
        return this.friction;
    }

    public float getSpeedFactor() {
        return this.speedFactor;
    }

    public float getJumpFactor() {
        return this.jumpFactor;
    }

    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        level.levelEvent(player, 2001, blockPos, getId(blockState));
        if (is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(player, false);
        }
    }

    public void handleRain(Level level, BlockPos blockPos) {
    }

    public boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(BlockState blockState) {
        this.defaultBlockState = blockState;
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public SoundType getSoundType(BlockState blockState) {
        return this.soundType;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour, net.minecraft.world.level.ItemLike
    public Item asItem() {
        if (this.item == null) {
            this.item = Item.byBlock(this);
        }
        return this.item;
    }

    public boolean hasDynamicShape() {
        return this.dynamicShape;
    }

    public String toString() {
        return "Block{" + Registry.BLOCK.getKey(this) + "}";
    }

    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    protected Block asBlock() {
        return this;
    }
}
