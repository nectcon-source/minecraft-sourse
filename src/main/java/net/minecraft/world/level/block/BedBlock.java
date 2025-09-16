package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BedBlock.class */
public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    protected static final VoxelShape BASE = Block.box(0.0d, 3.0d, 0.0d, 16.0d, 9.0d, 16.0d);
    protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0d, 0.0d, 0.0d, 3.0d, 3.0d, 3.0d);
    protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0d, 0.0d, 13.0d, 3.0d, 3.0d, 16.0d);
    protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0d, 0.0d, 0.0d, 16.0d, 3.0d, 3.0d);
    protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0d, 0.0d, 13.0d, 16.0d, 3.0d, 16.0d);
    protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
    protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
    protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
    protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
    private final DyeColor color;

    public BedBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
        registerDefaultState( this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, false));
    }

    @Nullable
    public static Direction getBedOrientation(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BedBlock) {
            return (Direction) blockState.getValue(FACING);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (blockState.getValue(PART) != BedPart.HEAD) {
            blockPos = blockPos.relative((Direction) blockState.getValue(FACING));
            blockState = level.getBlockState(blockPos);
            if (!blockState.is(this)) {
                return InteractionResult.CONSUME;
            }
        }
        if (!canSetSpawn(level)) {
            level.removeBlock(blockPos, false);
            BlockPos relative = blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite());
            if (level.getBlockState(relative).is(this)) {
                level.removeBlock(relative, false);
            }
            level.explode(null, DamageSource.badRespawnPointExplosion(), null, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 5.0f, true, Explosion.BlockInteraction.DESTROY);
            return InteractionResult.SUCCESS;
        }
        if (((Boolean) blockState.getValue(OCCUPIED)).booleanValue()) {
            if (!kickVillagerOutOfBed(level, blockPos)) {
                player.displayClientMessage(new TranslatableComponent("block.minecraft.bed.occupied"), true);
            }
            return InteractionResult.SUCCESS;
        }
        player.startSleepInBed(blockPos).ifLeft(bedSleepingProblem -> {
            if (bedSleepingProblem != null) {
                player.displayClientMessage(bedSleepingProblem.getMessage(), true);
            }
        });
        return InteractionResult.SUCCESS;
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().bedWorks();
    }

    private boolean kickVillagerOutOfBed(Level level, BlockPos blockPos) {
        List<Villager> entitiesOfClass = level.getEntitiesOfClass(Villager.class, new AABB(blockPos), (v0) -> {
            return v0.isSleeping();
        });
        if (entitiesOfClass.isEmpty()) {
            return false;
        }
        entitiesOfClass.get(0).stopSleeping();
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        super.fallOn(level, blockPos, entity, f * 0.5f);
    }

    @Override // net.minecraft.world.level.block.Block
    public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(blockGetter, entity);
        } else {
            bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (deltaMovement.y < 0.0d) {
            entity.setDeltaMovement(deltaMovement.x, (-deltaMovement.y) * 0.6600000262260437d * (entity instanceof LivingEntity ? 1.0d : 0.8d), deltaMovement.z);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == getNeighbourDirection((BedPart) blockState.getValue(PART), (Direction) blockState.getValue(FACING))) {
            if (blockState2.is(this) && blockState2.getValue(PART) != blockState.getValue(PART)) {
                return (BlockState) blockState.setValue(OCCUPIED, blockState2.getValue(OCCUPIED));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
        return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        BedPart bedPart;
        if (!level.isClientSide && player.isCreative() && (bedPart = (BedPart) blockState.getValue(PART)) == BedPart.FOOT) {
            BlockPos relative = blockPos.relative(getNeighbourDirection(bedPart, (Direction) blockState.getValue(FACING)));
            BlockState blockState2 = level.getBlockState(relative);
            if (blockState2.getBlock() == this && blockState2.getValue(PART) == BedPart.HEAD) {
                level.setBlock(relative, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, relative, Block.getId(blockState2));
            }
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction horizontalDirection = blockPlaceContext.getHorizontalDirection();
        if (blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(horizontalDirection)).canBeReplaced(blockPlaceContext)) {
            return (BlockState) defaultBlockState().setValue(FACING, horizontalDirection);
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (getConnectedDirection(blockState).getOpposite()) {
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            default:
                return EAST_SHAPE;
        }
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(FACING);
        return blockState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
        if (((BedPart) blockState.getValue(PART)) == BedPart.HEAD) {
            return DoubleBlockCombiner.BlockType.FIRST;
        }
        return DoubleBlockCombiner.BlockType.SECOND;
    }

    private static boolean isBunkBed(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BedBlock;
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, float f) {
        Direction direction = (Direction) collisionGetter.getBlockState(blockPos).getValue(FACING);
        Direction clockWise = direction.getClockWise();
        Direction opposite = clockWise.isFacingAngle(f) ? clockWise.getOpposite() : clockWise;
        if (isBunkBed(collisionGetter, blockPos)) {
            return findBunkBedStandUpPosition(entityType, collisionGetter, blockPos, direction, opposite);
        }
        int[][] bedStandUpOffsets = bedStandUpOffsets(direction, opposite);
        Optional<Vec3> findStandUpPositionAtOffset = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedStandUpOffsets, true);
        if (findStandUpPositionAtOffset.isPresent()) {
            return findStandUpPositionAtOffset;
        }
        return findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedStandUpOffsets, false);
    }

    private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, Direction direction, Direction direction2) {
        int[][] bedSurroundStandUpOffsets = bedSurroundStandUpOffsets(direction, direction2);
        Optional<Vec3> findStandUpPositionAtOffset = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedSurroundStandUpOffsets, true);
        if (findStandUpPositionAtOffset.isPresent()) {
            return findStandUpPositionAtOffset;
        }
        BlockPos below = blockPos.below();
        Optional<Vec3> findStandUpPositionAtOffset2 = findStandUpPositionAtOffset(entityType, collisionGetter, below, bedSurroundStandUpOffsets, true);
        if (findStandUpPositionAtOffset2.isPresent()) {
            return findStandUpPositionAtOffset2;
        }
        int[][] bedAboveStandUpOffsets = bedAboveStandUpOffsets(direction);
        Optional<Vec3> findStandUpPositionAtOffset3 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedAboveStandUpOffsets, true);
        if (findStandUpPositionAtOffset3.isPresent()) {
            return findStandUpPositionAtOffset3;
        }
        Optional<Vec3> findStandUpPositionAtOffset4 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedSurroundStandUpOffsets, false);
        if (findStandUpPositionAtOffset4.isPresent()) {
            return findStandUpPositionAtOffset4;
        }
        Optional<Vec3> findStandUpPositionAtOffset5 = findStandUpPositionAtOffset(entityType, collisionGetter, below, bedSurroundStandUpOffsets, false);
        if (findStandUpPositionAtOffset5.isPresent()) {
            return findStandUpPositionAtOffset5;
        }
        return findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, bedAboveStandUpOffsets, false);
    }

    private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, int[][] iArr, boolean z) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int[] iArr2 : iArr) {
            mutableBlockPos.set(blockPos.getX() + iArr2[0], blockPos.getY(), blockPos.getZ() + iArr2[1]);
            Vec3 findSafeDismountLocation = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, z);
            if (findSafeDismountLocation != null) {
                return Optional.of(findSafeDismountLocation);
            }
        }
        return Optional.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BedBlockEntity(this.color);
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        if (!level.isClientSide) {
            level.setBlock(blockPos.relative((Direction) blockState.getValue(FACING)), (BlockState) blockState.setValue(PART, BedPart.HEAD), 3);
            level.blockUpdated(blockPos, Blocks.AIR);
            blockState.updateNeighbourShapes(level, blockPos, 3);
        }
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public long getSeed(BlockState blockState, BlockPos blockPos) {
        BlockPos relative = blockPos.relative((Direction) blockState.getValue(FACING), blockState.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(relative.getX(), blockPos.getY(), relative.getZ());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    private static int[][] bedStandUpOffsets(Direction direction, Direction direction2) {
        return (int[][]) ArrayUtils.addAll(bedSurroundStandUpOffsets(direction, direction2), bedAboveStandUpOffsets(direction));
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [int[], int[][]] */
    private static int[][] bedSurroundStandUpOffsets(Direction direction, Direction direction2) {
        return new int[][]{{direction2.getStepX(), direction2.getStepZ()}, {direction2.getStepX() - direction.getStepX(), direction2.getStepZ() - direction.getStepZ()}, {direction2.getStepX() - direction.getStepX() * 2, direction2.getStepZ() - direction.getStepZ() * 2}, {-direction.getStepX() * 2, -direction.getStepZ() * 2}, {-direction2.getStepX() - direction.getStepX() * 2, -direction2.getStepZ() - direction.getStepZ() * 2}, {-direction2.getStepX() - direction.getStepX(), -direction2.getStepZ() - direction.getStepZ()}, {-direction2.getStepX(), -direction2.getStepZ()}, {-direction2.getStepX() + direction.getStepX(), -direction2.getStepZ() + direction.getStepZ()}, {direction.getStepX(), direction.getStepZ()}, {direction2.getStepX() + direction.getStepX(), direction2.getStepZ() + direction.getStepZ()}};
    }
    /* JADX WARN: Type inference failed for: r0v1, types: [int[], int[][]] */
    private static int[][] bedAboveStandUpOffsets(Direction direction) {
        return new int[][]{{0, 0}, {-direction.getStepX(), -direction.getStepZ()}};
    }
}
