package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour.class */
public abstract class BlockBehaviour {
    protected static final Direction[] UPDATE_SHAPE_ORDER = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    protected final Material material;
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final Properties properties;

    @Nullable
    protected ResourceLocation drops;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$OffsetType.class */
    public enum OffsetType {
        NONE,
        XZ,
        XYZ
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$StateArgumentPredicate.class */
    public interface StateArgumentPredicate<A> {
        boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, A a);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$StatePredicate.class */
    public interface StatePredicate {
        boolean test(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos);
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public BlockBehaviour(Properties properties) {
        this.material = properties.material;
        this.hasCollision = properties.hasCollision;
        this.drops = properties.drops;
        this.explosionResistance = properties.explosionResistance;
        this.isRandomlyTicking = properties.isRandomlyTicking;
        this.soundType = properties.soundType;
        this.friction = properties.friction;
        this.speedFactor = properties.speedFactor;
        this.jumpFactor = properties.jumpFactor;
        this.dynamicShape = properties.dynamicShape;
        this.properties = properties;
    }

    @Deprecated
    public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2) {
    }

    @Deprecated
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND:
                return !blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
            case WATER:
                return blockGetter.getFluidState(blockPos).is(FluidTags.WATER);
            case AIR:
                return !blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
            default:
                return false;
        }
    }

    @Deprecated
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState;
    }

    @Deprecated
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return false;
    }

    @Deprecated
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        DebugPackets.sendNeighborsUpdatePacket(level, blockPos);
    }

    @Deprecated
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
    }

    @Deprecated
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (isEntityBlock() && !blockState.is(blockState2.getBlock())) {
            level.removeBlockEntity(blockPos);
        }
    }

    @Deprecated
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return InteractionResult.PASS;
    }

    @Deprecated
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int i2) {
        return false;
    }

    @Deprecated
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return false;
    }

    @Deprecated
    public boolean isSignalSource(BlockState blockState) {
        return false;
    }

    @Deprecated
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return this.material.getPushReaction();
    }

    @Deprecated
    public FluidState getFluidState(BlockState blockState) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return false;
    }

    public OffsetType getOffsetType() {
        return OffsetType.NONE;
    }

    @Deprecated
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState;
    }

    @Deprecated
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState;
    }

    @Deprecated
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return this.material.isReplaceable() && (blockPlaceContext.getItemInHand().isEmpty() || blockPlaceContext.getItemInHand().getItem() != asItem());
    }

    @Deprecated
    public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return this.material.isReplaceable() || !this.material.isSolid();
    }

    @Deprecated
    public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
        ResourceLocation lootTable = getLootTable();
        if (lootTable == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        }
        LootContext create = builder.withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.BLOCK);
        return create.getLevel().getServer().getLootTables().get(lootTable).getRandomItems(create);
    }

    @Deprecated
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        return Mth.getSeed(blockPos);
    }

    @Deprecated
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getShape(blockGetter, blockPos);
    }

    @Deprecated
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return getCollisionShape(blockState, blockGetter, blockPos, CollisionContext.empty());
    }

    @Deprecated
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Deprecated
    public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        if (blockState.isSolidRender(blockGetter, blockPos)) {
            return blockGetter.getMaxLightLevel();
        }
        return blockState.propagatesSkylightDown(blockGetter, blockPos) ? 0 : 1;
    }

    @Nullable
    @Deprecated
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return null;
    }

    @Deprecated
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    @Deprecated
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) ? 0.2f : 1.0f;
    }

    @Deprecated
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return 0;
    }

    @Deprecated
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    @Deprecated
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.hasCollision ? blockState.getShape(blockGetter, blockPos) : Shapes.empty();
    }

    @Deprecated
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Deprecated
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        tick(blockState, serverLevel, blockPos, random);
    }

    @Deprecated
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
    }

    @Deprecated
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        float destroySpeed = blockState.getDestroySpeed(blockGetter, blockPos);
        if (destroySpeed == -1.0f) {
            return 0.0f;
        }
        return (player.getDestroySpeed(blockState) / destroySpeed) / (player.hasCorrectToolForDrops(blockState) ? 30 : 100);
    }

    @Deprecated
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
    }

    @Deprecated
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
    }

    @Deprecated
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    @Deprecated
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    }

    @Deprecated
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    public final boolean isEntityBlock() {
        return this instanceof EntityBlock;
    }

    public final ResourceLocation getLootTable() {
        if (this.drops == null) {
            ResourceLocation key = Registry.BLOCK.getKey(asBlock());
            this.drops = new ResourceLocation(key.getNamespace(), "blocks/" + key.getPath());
        }
        return this.drops;
    }

    @Deprecated
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
    }

    public MaterialColor defaultMaterialColor() {
        return (MaterialColor) this.properties.materialColor.apply(asBlock().defaultBlockState());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$Properties.class */
    public static class Properties {
        private Material material;
        private Function<BlockState, MaterialColor> materialColor;
        private boolean hasCollision;
        private SoundType soundType;
        private ToIntFunction<BlockState> lightEmission;
        private float explosionResistance;
        private float destroyTime;
        private boolean requiresCorrectToolForDrops;
        private boolean isRandomlyTicking;
        private float friction;
        private float speedFactor;
        private float jumpFactor;
        private ResourceLocation drops;
        private boolean canOcclude;
        private boolean isAir;
        private StateArgumentPredicate<EntityType<?>> isValidSpawn;
        private StatePredicate isRedstoneConductor;
        private StatePredicate isSuffocating;
        private StatePredicate isViewBlocking;
        private StatePredicate hasPostProcess;
        private StatePredicate emissiveRendering;
        private boolean dynamicShape;

        private Properties(Material material, MaterialColor materialColor) {
            this(material, (Function<BlockState, MaterialColor>) blockState -> {
                return materialColor;
            });
        }

        private Properties(Material material, Function<BlockState, MaterialColor> function) {
            this.hasCollision = true;
            this.soundType = SoundType.STONE;
            this.lightEmission = blockState -> {
                return 0;
            };
            this.friction = 0.6f;
            this.speedFactor = 1.0f;
            this.jumpFactor = 1.0f;
            this.canOcclude = true;
            this.isValidSpawn = (blockState2, blockGetter, blockPos, entityType) -> {
                return blockState2.isFaceSturdy(blockGetter, blockPos, Direction.UP) && blockState2.getLightEmission() < 14;
            };
            this.isRedstoneConductor = (blockState3, blockGetter2, blockPos2) -> {
                return blockState3.getMaterial().isSolidBlocking() && blockState3.isCollisionShapeFullBlock(blockGetter2, blockPos2);
            };
            this.isSuffocating = (blockState4, blockGetter3, blockPos3) -> {
                return this.material.blocksMotion() && blockState4.isCollisionShapeFullBlock(blockGetter3, blockPos3);
            };
            this.isViewBlocking = this.isSuffocating;
            this.hasPostProcess = (blockState5, blockGetter4, blockPos4) -> {
                return false;
            };
            this.emissiveRendering = (blockState6, blockGetter5, blockPos5) -> {
                return false;
            };
            this.material = material;
            this.materialColor = function;
        }

        /* renamed from: of */
        public static Properties of(Material material) {
            return of(material, material.getColor());
        }

        /* renamed from: of */
        public static Properties of(Material material, DyeColor dyeColor) {
            return of(material, dyeColor.getMaterialColor());
        }

        /* renamed from: of */
        public static Properties of(Material material, MaterialColor materialColor) {
            return new Properties(material, materialColor);
        }

        /* renamed from: of */
        public static Properties of(Material material, Function<BlockState, MaterialColor> function) {
            return new Properties(material, function);
        }

        public static Properties copy(BlockBehaviour blockBehaviour) {
            Properties properties = new Properties(blockBehaviour.material, blockBehaviour.properties.materialColor);
            properties.material = blockBehaviour.properties.material;
            properties.destroyTime = blockBehaviour.properties.destroyTime;
            properties.explosionResistance = blockBehaviour.properties.explosionResistance;
            properties.hasCollision = blockBehaviour.properties.hasCollision;
            properties.isRandomlyTicking = blockBehaviour.properties.isRandomlyTicking;
            properties.lightEmission = blockBehaviour.properties.lightEmission;
            properties.materialColor = blockBehaviour.properties.materialColor;
            properties.soundType = blockBehaviour.properties.soundType;
            properties.friction = blockBehaviour.properties.friction;
            properties.speedFactor = blockBehaviour.properties.speedFactor;
            properties.dynamicShape = blockBehaviour.properties.dynamicShape;
            properties.canOcclude = blockBehaviour.properties.canOcclude;
            properties.isAir = blockBehaviour.properties.isAir;
            properties.requiresCorrectToolForDrops = blockBehaviour.properties.requiresCorrectToolForDrops;
            return properties;
        }

        public Properties noCollission() {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public Properties noOcclusion() {
            this.canOcclude = false;
            return this;
        }

        public Properties friction(float f) {
            this.friction = f;
            return this;
        }

        public Properties speedFactor(float f) {
            this.speedFactor = f;
            return this;
        }

        public Properties jumpFactor(float f) {
            this.jumpFactor = f;
            return this;
        }

        public Properties sound(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

        public Properties lightLevel(ToIntFunction<BlockState> toIntFunction) {
            this.lightEmission = toIntFunction;
            return this;
        }

        public Properties strength(float f, float f2) {
            this.destroyTime = f;
            this.explosionResistance = Math.max(0.0f, f2);
            return this;
        }

        public Properties instabreak() {
            return strength(0.0f);
        }

        public Properties strength(float f) {
            strength(f, f);
            return this;
        }

        public Properties randomTicks() {
            this.isRandomlyTicking = true;
            return this;
        }

        public Properties dynamicShape() {
            this.dynamicShape = true;
            return this;
        }

        public Properties noDrops() {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public Properties dropsLike(Block block) {
            this.drops = block.getLootTable();
            return this;
        }

        public Properties air() {
            this.isAir = true;
            return this;
        }

        public Properties isValidSpawn(StateArgumentPredicate<EntityType<?>> stateArgumentPredicate) {
            this.isValidSpawn = stateArgumentPredicate;
            return this;
        }

        public Properties isRedstoneConductor(StatePredicate statePredicate) {
            this.isRedstoneConductor = statePredicate;
            return this;
        }

        public Properties isSuffocating(StatePredicate statePredicate) {
            this.isSuffocating = statePredicate;
            return this;
        }

        public Properties isViewBlocking(StatePredicate statePredicate) {
            this.isViewBlocking = statePredicate;
            return this;
        }

        public Properties hasPostProcess(StatePredicate statePredicate) {
            this.hasPostProcess = statePredicate;
            return this;
        }

        public Properties emissiveRendering(StatePredicate statePredicate) {
            this.emissiveRendering = statePredicate;
            return this;
        }

        public Properties requiresCorrectToolForDrops() {
            this.requiresCorrectToolForDrops = true;
            return this;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase.class */
    public static abstract class BlockStateBase extends StateHolder<Block, BlockState> {
        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final Material material;
        private final MaterialColor materialColor;
        private final float destroySpeed;
        private final boolean requiresCorrectToolForDrops;
        private final boolean canOcclude;
        private final StatePredicate isRedstoneConductor;
        private final StatePredicate isSuffocating;
        private final StatePredicate isViewBlocking;
        private final StatePredicate hasPostProcess;
        private final StatePredicate emissiveRendering;

        @Nullable
        protected Cache cache;

        protected abstract BlockState asState();

        protected BlockStateBase(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
            super(block, immutableMap, mapCodec);
            Properties properties = block.properties;
            this.lightEmission = properties.lightEmission.applyAsInt(asState());
            this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(asState());
            this.isAir = properties.isAir;
            this.material = properties.material;
            this.materialColor = (MaterialColor) properties.materialColor.apply(asState());
            this.destroySpeed = properties.destroyTime;
            this.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops;
            this.canOcclude = properties.canOcclude;
            this.isRedstoneConductor = properties.isRedstoneConductor;
            this.isSuffocating = properties.isSuffocating;
            this.isViewBlocking = properties.isViewBlocking;
            this.hasPostProcess = properties.hasPostProcess;
            this.emissiveRendering = properties.emissiveRendering;
        }

        public void initCache() {
            if (!getBlock().hasDynamicShape()) {
                this.cache = new Cache(asState());
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        public Block getBlock() {
            return (Block) this.owner;
        }

        public Material getMaterial() {
            return this.material;
        }

        public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
            return getBlock().properties.isValidSpawn.test(asState(), blockGetter, blockPos, entityType);
        }

        public boolean propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache == null) {
                return getBlock().propagatesSkylightDown(asState(), blockGetter, blockPos);
            }
            return this.cache.propagatesSkylightDown;
        }

        public int getLightBlock(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache == null) {
                return getBlock().getLightBlock(asState(), blockGetter, blockPos);
            }
            return this.cache.lightBlock;
        }

        public VoxelShape getFaceOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            if (this.cache == null || this.cache.occlusionShapes == null) {
                return Shapes.getFaceShape(getOcclusionShape(blockGetter, blockPos), direction);
            }
            return this.cache.occlusionShapes[direction.ordinal()];
        }

        public VoxelShape getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos) {
            return getBlock().getOcclusionShape(asState(), blockGetter, blockPos);
        }

        public boolean hasLargeCollisionShape() {
            return this.cache == null || this.cache.largeCollisionShape;
        }

        public boolean useShapeForLightOcclusion() {
            return this.useShapeForLightOcclusion;
        }

        public int getLightEmission() {
            return this.lightEmission;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public MaterialColor getMapColor(BlockGetter blockGetter, BlockPos blockPos) {
            return this.materialColor;
        }

        public BlockState rotate(Rotation rotation) {
            return getBlock().rotate(asState(), rotation);
        }

        public BlockState mirror(Mirror mirror) {
            return getBlock().mirror(asState(), mirror);
        }

        public RenderShape getRenderShape() {
            return getBlock().getRenderShape(asState());
        }

        public boolean emissiveRendering(BlockGetter blockGetter, BlockPos blockPos) {
            return this.emissiveRendering.test(asState(), blockGetter, blockPos);
        }

        public float getShadeBrightness(BlockGetter blockGetter, BlockPos blockPos) {
            return getBlock().getShadeBrightness(asState(), blockGetter, blockPos);
        }

        public boolean isRedstoneConductor(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isRedstoneConductor.test(asState(), blockGetter, blockPos);
        }

        public boolean isSignalSource() {
            return getBlock().isSignalSource(asState());
        }

        public int getSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return getBlock().getSignal(asState(), blockGetter, blockPos, direction);
        }

        public boolean hasAnalogOutputSignal() {
            return getBlock().hasAnalogOutputSignal(asState());
        }

        public int getAnalogOutputSignal(Level level, BlockPos blockPos) {
            return getBlock().getAnalogOutputSignal(asState(), level, blockPos);
        }

        public float getDestroySpeed(BlockGetter blockGetter, BlockPos blockPos) {
            return this.destroySpeed;
        }

        public float getDestroyProgress(Player player, BlockGetter blockGetter, BlockPos blockPos) {
            return getBlock().getDestroyProgress(asState(), player, blockGetter, blockPos);
        }

        public int getDirectSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return getBlock().getDirectSignal(asState(), blockGetter, blockPos, direction);
        }

        public PushReaction getPistonPushReaction() {
            return getBlock().getPistonPushReaction(asState());
        }

        public boolean isSolidRender(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.solidRender;
            }
            BlockState asState = asState();
            if (asState.canOcclude()) {
                return Block.isShapeFullBlock(asState.getOcclusionShape(blockGetter, blockPos));
            }
            return false;
        }

        public boolean canOcclude() {
            return this.canOcclude;
        }

        public boolean skipRendering(BlockState blockState, Direction direction) {
            return getBlock().skipRendering(asState(), blockState, direction);
        }

        public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
            return getShape(blockGetter, blockPos, CollisionContext.empty());
        }

        public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return getBlock().getShape(asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.collisionShape;
            }
            return getCollisionShape(blockGetter, blockPos, CollisionContext.empty());
        }

        public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return getBlock().getCollisionShape(asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getBlockSupportShape(BlockGetter blockGetter, BlockPos blockPos) {
            return getBlock().getBlockSupportShape(asState(), blockGetter, blockPos);
        }

        public VoxelShape getVisualShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return getBlock().getVisualShape(asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getInteractionShape(BlockGetter blockGetter, BlockPos blockPos) {
            return getBlock().getInteractionShape(asState(), blockGetter, blockPos);
        }

        public final boolean entityCanStandOn(BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
            return entityCanStandOnFace(blockGetter, blockPos, entity, Direction.UP);
        }

        public final boolean entityCanStandOnFace(BlockGetter blockGetter, BlockPos blockPos, Entity entity, Direction direction) {
            return Block.isFaceFull(getCollisionShape(blockGetter, blockPos, CollisionContext.of(entity)), direction);
        }

        public Vec3 getOffset(BlockGetter blockGetter, BlockPos blockPos) {
            OffsetType offsetType = getBlock().getOffsetType();
            if (offsetType == OffsetType.NONE) {
                return Vec3.ZERO;
            }
            long seed = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
            return new Vec3((((seed & 15) / 15.0f) - 0.5d) * 0.5d, offsetType == OffsetType.XYZ ? ((((seed >> 4) & 15) / 15.0f) - 1.0d) * 0.2d : 0.0d, ((((seed >> 8) & 15) / 15.0f) - 0.5d) * 0.5d);
        }

        public boolean triggerEvent(Level level, BlockPos blockPos, int i, int i2) {
            return getBlock().triggerEvent(asState(), level, blockPos, i, i2);
        }

        public void neighborChanged(Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
            getBlock().neighborChanged(asState(), level, blockPos, block, blockPos2, z);
        }

        public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
            updateNeighbourShapes(levelAccessor, blockPos, i, 512);
        }

        public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2) {
            getBlock();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : BlockBehaviour.UPDATE_SHAPE_ORDER) {
                mutableBlockPos.setWithOffset(blockPos, direction);
                BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
                Block.updateOrDestroy(blockState, blockState.updateShape(direction.getOpposite(), asState(), levelAccessor, mutableBlockPos, blockPos), levelAccessor, mutableBlockPos, i, i2);
            }
        }

        public final void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
            updateIndirectNeighbourShapes(levelAccessor, blockPos, i, 512);
        }

        public void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int i, int i2) {
            getBlock().updateIndirectNeighbourShapes(asState(), levelAccessor, blockPos, i, i2);
        }

        public void onPlace(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
            getBlock().onPlace(asState(), level, blockPos, blockState, z);
        }

        public void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
            getBlock().onRemove(asState(), level, blockPos, blockState, z);
        }

        public void tick(ServerLevel serverLevel, BlockPos blockPos, Random random) {
            getBlock().tick(asState(), serverLevel, blockPos, random);
        }

        public void randomTick(ServerLevel serverLevel, BlockPos blockPos, Random random) {
            getBlock().randomTick(asState(), serverLevel, blockPos, random);
        }

        public void entityInside(Level level, BlockPos blockPos, Entity entity) {
            getBlock().entityInside(asState(), level, blockPos, entity);
        }

        public void spawnAfterBreak(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
            getBlock().spawnAfterBreak(asState(), serverLevel, blockPos, itemStack);
        }

        public List<ItemStack> getDrops(LootContext.Builder builder) {
            return getBlock().getDrops(asState(), builder);
        }

        public InteractionResult use(Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
            return getBlock().use(asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
        }

        public void attack(Level level, BlockPos blockPos, Player player) {
            getBlock().attack(asState(), level, blockPos, player);
        }

        public boolean isSuffocating(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isSuffocating.test(asState(), blockGetter, blockPos);
        }

        public boolean isViewBlocking(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isViewBlocking.test(asState(), blockGetter, blockPos);
        }

        public BlockState updateShape(Direction direction, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
            return getBlock().updateShape(asState(), direction, blockState, levelAccessor, blockPos, blockPos2);
        }

        public boolean isPathfindable(BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
            return getBlock().isPathfindable(asState(), blockGetter, blockPos, pathComputationType);
        }

        public boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
            return getBlock().canBeReplaced(asState(), blockPlaceContext);
        }

        public boolean canBeReplaced(Fluid fluid) {
            return getBlock().canBeReplaced(asState(), fluid);
        }

        public boolean canSurvive(LevelReader levelReader, BlockPos blockPos) {
            return getBlock().canSurvive(asState(), levelReader, blockPos);
        }

        public boolean hasPostProcess(BlockGetter blockGetter, BlockPos blockPos) {
            return this.hasPostProcess.test(asState(), blockGetter, blockPos);
        }

        @Nullable
        public MenuProvider getMenuProvider(Level level, BlockPos blockPos) {
            return getBlock().getMenuProvider(asState(), level, blockPos);
        }

        /* renamed from: is */
        public boolean is(Tag<Block> tag) {
            return getBlock().is(tag);
        }

        /* renamed from: is */
        public boolean is(Tag<Block> tag, Predicate<BlockStateBase> predicate) {
            return getBlock().is(tag) && predicate.test(this);
        }

        /* renamed from: is */
        public boolean is(Block block) {
            return getBlock().is(block);
        }

        public FluidState getFluidState() {
            return getBlock().getFluidState(asState());
        }

        public boolean isRandomlyTicking() {
            return getBlock().isRandomlyTicking(asState());
        }

        public long getSeed(BlockPos blockPos) {
            return getBlock().getSeed(asState(), blockPos);
        }

        public SoundType getSoundType() {
            return getBlock().getSoundType(asState());
        }

        public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
            getBlock().onProjectileHit(level, blockState, blockHitResult, projectile);
        }

        public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return isFaceSturdy(blockGetter, blockPos, direction, SupportType.FULL);
        }

        public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction, SupportType supportType) {
            if (this.cache != null) {
                return this.cache.isFaceSturdy(direction, supportType);
            }
            return supportType.isSupporting(asState(), blockGetter, blockPos, direction);
        }

        public boolean isCollisionShapeFullBlock(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.isCollisionShapeFullBlock;
            }
            return Block.isShapeFullBlock(getCollisionShape(blockGetter, blockPos));
        }

        public boolean requiresCorrectToolForDrops() {
            return this.requiresCorrectToolForDrops;
        }

        /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase$Cache.class */
        static final class Cache {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
            protected final boolean solidRender;
            private final boolean propagatesSkylightDown;
            private final int lightBlock;

            @Nullable
            private final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            private final boolean[] faceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            private Cache(BlockState blockState) {
                Block block = blockState.getBlock();
                this.solidRender = blockState.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.propagatesSkylightDown = block.propagatesSkylightDown(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.lightBlock = block.getLightBlock(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (!blockState.canOcclude()) {
                    this.occlusionShapes = null;
                } else {
                    this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
                    VoxelShape occlusionShape = block.getOcclusionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                    for (Direction direction : DIRECTIONS) {
                        this.occlusionShapes[direction.ordinal()] = Shapes.getFaceShape(occlusionShape, direction);
                    }
                }
                this.collisionShape = block.getCollisionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                this.largeCollisionShape = Arrays.stream(Direction.Axis.values()).anyMatch(axis -> {
                    return this.collisionShape.min(axis) < 0.0d || this.collisionShape.max(axis) > 1.0d;
                });
                this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];
                for (Direction direction2 : DIRECTIONS) {
                    for (SupportType supportType : SupportType.values()) {
                        this.faceSturdy[getFaceSupportIndex(direction2, supportType)] = supportType.isSupporting(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction2);
                    }
                }
                this.isCollisionShapeFullBlock = Block.isShapeFullBlock(blockState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
            }

            public boolean isFaceSturdy(Direction direction, SupportType supportType) {
                return this.faceSturdy[getFaceSupportIndex(direction, supportType)];
            }

            private static int getFaceSupportIndex(Direction direction, SupportType supportType) {
                return (direction.ordinal() * SUPPORT_TYPE_COUNT) + supportType.ordinal();
            }
        }
    }
}
