package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ChestBlock.class */
public class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape NORTH_AABB = Block.box(1.0d, 0.0d, 0.0d, 15.0d, 14.0d, 15.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 14.0d, 16.0d);
    protected static final VoxelShape WEST_AABB = Block.box(0.0d, 0.0d, 1.0d, 15.0d, 14.0d, 15.0d);
    protected static final VoxelShape EAST_AABB = Block.box(1.0d, 0.0d, 1.0d, 16.0d, 14.0d, 15.0d);
    protected static final VoxelShape AABB = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 14.0d, 15.0d);
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>() { // from class: net.minecraft.world.level.block.ChestBlock.1
        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<Container> acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
            return Optional.of(new CompoundContainer(chestBlockEntity, chestBlockEntity2));
        }

        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<Container> acceptSingle(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<Container> acceptNone() {
            return Optional.empty();
        }
    };
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>() { // from class: net.minecraft.world.level.block.ChestBlock.2
        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<MenuProvider> acceptDouble(final ChestBlockEntity chestBlockEntity, final ChestBlockEntity chestBlockEntity2) {
            final Container compoundContainer = new CompoundContainer(chestBlockEntity, chestBlockEntity2);
            return Optional.of(new MenuProvider() { // from class: net.minecraft.world.level.block.ChestBlock.2.1
                @Override // net.minecraft.world.inventory.MenuConstructor
                @Nullable
                public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                    if (chestBlockEntity.canOpen(player) && chestBlockEntity2.canOpen(player)) {
                        chestBlockEntity.unpackLootTable(inventory.player);
                        chestBlockEntity2.unpackLootTable(inventory.player);
                        return ChestMenu.sixRows(i, inventory, compoundContainer);
                    }
                    return null;
                }

                @Override // net.minecraft.world.MenuProvider
                public Component getDisplayName() {
                    if (chestBlockEntity.hasCustomName()) {
                        return chestBlockEntity.getDisplayName();
                    }
                    if (chestBlockEntity2.hasCustomName()) {
                        return chestBlockEntity2.getDisplayName();
                    }
                    return new TranslatableComponent("container.chestDouble");
                }
            });
        }

        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<MenuProvider> acceptSingle(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };

    protected ChestBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(properties, supplier);
        registerDefaultState( this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, false));
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
        ChestType chestType = (ChestType) blockState.getValue(TYPE);
        if (chestType == ChestType.SINGLE) {
            return DoubleBlockCombiner.BlockType.SINGLE;
        }
        if (chestType == ChestType.RIGHT) {
            return DoubleBlockCombiner.BlockType.FIRST;
        }
        return DoubleBlockCombiner.BlockType.SECOND;
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (blockState2.is(this) && direction.getAxis().isHorizontal()) {
            ChestType chestType = (ChestType) blockState2.getValue(TYPE);
            if (blockState.getValue(TYPE) == ChestType.SINGLE && chestType != ChestType.SINGLE && blockState.getValue(FACING) == blockState2.getValue(FACING) && getConnectedDirection(blockState2) == direction.getOpposite()) {
                return (BlockState) blockState.setValue(TYPE, chestType.getOpposite());
            }
        } else if (getConnectedDirection(blockState) == direction) {
            return (BlockState) blockState.setValue(TYPE, ChestType.SINGLE);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(TYPE) == ChestType.SINGLE) {
            return AABB;
        }
        switch (getConnectedDirection(blockState)) {
            case NORTH:
            default:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
                return EAST_AABB;
        }
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        return blockState.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction candidatePartnerFacing;
        ChestType chestType = ChestType.SINGLE;
        Direction opposite = blockPlaceContext.getHorizontalDirection().getOpposite();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean isSecondaryUseActive = blockPlaceContext.isSecondaryUseActive();
        Direction clickedFace = blockPlaceContext.getClickedFace();
        if (clickedFace.getAxis().isHorizontal() && isSecondaryUseActive && (candidatePartnerFacing = candidatePartnerFacing(blockPlaceContext, clickedFace.getOpposite())) != null && candidatePartnerFacing.getAxis() != clickedFace.getAxis()) {
            opposite = candidatePartnerFacing;
            chestType = opposite.getCounterClockWise() == clickedFace.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
        }
        if (chestType == ChestType.SINGLE && !isSecondaryUseActive) {
            if (opposite == candidatePartnerFacing(blockPlaceContext, opposite.getClockWise())) {
                chestType = ChestType.LEFT;
            } else if (opposite == candidatePartnerFacing(blockPlaceContext, opposite.getCounterClockWise())) {
                chestType = ChestType.RIGHT;
            }
        }
        return (BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(FACING, opposite)).setValue(TYPE, chestType)).setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Nullable
    private Direction candidatePartnerFacing(BlockPlaceContext blockPlaceContext, Direction direction) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction));
        if (blockState.is(this) && blockState.getValue(TYPE) == ChestType.SINGLE) {
            return (Direction) blockState.getValue(FACING);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (itemStack.hasCustomHoverName()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof ChestBlockEntity) {
                ((ChestBlockEntity) blockEntity).setCustomName(itemStack.getHoverName());
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        Object blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof Container) {
            Containers.dropContents(level, blockPos, (Container) blockEntity);
            level.updateNeighbourForOutputSignal(blockPos, this);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = getMenuProvider(blockState, level, blockPos);
        if (menuProvider != null) {
            player.openMenu(menuProvider);
            player.awardStat(getOpenChestStat());
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return InteractionResult.CONSUME;
    }

    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    @Nullable
    public static Container getContainer(ChestBlock chestBlock, BlockState blockState, Level level, BlockPos blockPos, boolean z) {
        return (Container) ((Optional) chestBlock.combine(blockState, level, blockPos, z).apply(CHEST_COMBINER)).orElse(null);
    }

    @Override // net.minecraft.world.level.block.AbstractChestBlock
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockState, Level level, BlockPos blockPos, boolean z) {
        BiPredicate<LevelAccessor, BlockPos> biPredicate;
        if (z) {
            biPredicate = (levelAccessor, blockPos2) -> {
                return false;
            };
        } else {
            biPredicate = ChestBlock::isChestBlockedAt;
        }
        return DoubleBlockCombiner.combineWithNeigbour((BlockEntityType) this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, blockState, level, blockPos, biPredicate);
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return (MenuProvider) ((Optional) combine(blockState, level, blockPos, false).apply(MENU_PROVIDER_COMBINER)).orElse(null);
    }

    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity lidBlockEntity) {
        return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() { // from class: net.minecraft.world.level.block.ChestBlock.3
            @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
            public Float2FloatFunction acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
                return f -> {
                    return Math.max(chestBlockEntity.getOpenNess(f), chestBlockEntity2.getOpenNess(f));
                };
            }

            @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
            public Float2FloatFunction acceptSingle(ChestBlockEntity chestBlockEntity) {
                chestBlockEntity.getClass();
                return chestBlockEntity::getOpenNess;
            }

            /* JADX WARN: Can't rename method to resolve collision */
//            @Override // net.minecraft.world.level.block.DoubleBlockCombiner.Combiner
//            public Float2FloatFunction acceptNone() {
//                LidBlockEntity lidBlockEntity2 = LidBlockEntity.this;
//                lidBlockEntity2.getClass();
//                return lidBlockEntity2::getOpenNess;
//            }
            @Override
            public Float2FloatFunction acceptNone() {
                return lidBlockEntity::getOpenNess;  // Use the parameter instead of LidBlockEntity.this
            }
        };
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new ChestBlockEntity();
    }

    public static boolean isChestBlockedAt(LevelAccessor levelAccessor, BlockPos blockPos) {
        return isBlockedChestByBlock(levelAccessor, blockPos) || isCatSittingOnChest(levelAccessor, blockPos);
    }

    private static boolean isBlockedChestByBlock(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos above = blockPos.above();
        return blockGetter.getBlockState(above).isRedstoneConductor(blockGetter, above);
    }

    private static boolean isCatSittingOnChest(LevelAccessor levelAccessor, BlockPos blockPos) {
        List<Cat> entitiesOfClass = levelAccessor.getEntitiesOfClass(Cat.class, new AABB(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 2, blockPos.getZ() + 1));
        if (!entitiesOfClass.isEmpty()) {
            Iterator<Cat> it = entitiesOfClass.iterator();
            while (it.hasNext()) {
                if (it.next().isInSittingPose()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, blockState, level, blockPos, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, WATERLOGGED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
