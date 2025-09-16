package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DispenserBlock.class */
public class DispenserBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = (Map) Util.make(new Object2ObjectOpenHashMap(), object2ObjectOpenHashMap -> {
        object2ObjectOpenHashMap.defaultReturnValue(new DefaultDispenseItemBehavior());
    });

    public static void registerBehavior(ItemLike itemLike, DispenseItemBehavior dispenseItemBehavior) {
        DISPENSER_REGISTRY.put(itemLike.asItem(), dispenseItemBehavior);
    }

    protected DispenserBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(TRIGGERED, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof DispenserBlockEntity) {
            player.openMenu((DispenserBlockEntity) blockEntity);
            if (blockEntity instanceof DropperBlockEntity) {
                player.awardStat(Stats.INSPECT_DROPPER);
            } else {
                player.awardStat(Stats.INSPECT_DISPENSER);
            }
        }
        return InteractionResult.CONSUME;
    }

    protected void dispenseFrom(ServerLevel serverLevel, BlockPos blockPos) {
        BlockSourceImpl blockSourceImpl = new BlockSourceImpl(serverLevel, blockPos);
        DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity) blockSourceImpl.getEntity();
        int randomSlot = dispenserBlockEntity.getRandomSlot();
        if (randomSlot < 0) {
            serverLevel.levelEvent(1001, blockPos, 0);
            return;
        }
        ItemStack item = dispenserBlockEntity.getItem(randomSlot);
        DispenseItemBehavior dispenseMethod = getDispenseMethod(item);
        if (dispenseMethod != DispenseItemBehavior.NOOP) {
            dispenserBlockEntity.setItem(randomSlot, dispenseMethod.dispense(blockSourceImpl, item));
        }
    }

    protected DispenseItemBehavior getDispenseMethod(ItemStack itemStack) {
        return DISPENSER_REGISTRY.get(itemStack.getItem());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean z2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
        boolean booleanValue = ((Boolean) blockState.getValue(TRIGGERED)).booleanValue();
        if (z2 && !booleanValue) {
            level.getBlockTicks().scheduleTick(blockPos, this, 4);
            level.setBlock(blockPos, (BlockState) blockState.setValue(TRIGGERED, true), 4);
        } else if (!z2 && booleanValue) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(TRIGGERED, false), 4);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        dispenseFrom(serverLevel, blockPos);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new DispenserBlockEntity();
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (itemStack.hasCustomHoverName()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof DispenserBlockEntity) {
                ((DispenserBlockEntity) blockEntity).setCustomName(itemStack.getHoverName());
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof DispenserBlockEntity) {
            Containers.dropContents(level, blockPos, (DispenserBlockEntity) blockEntity);
            level.updateNeighbourForOutputSignal(blockPos, this);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    public static Position getDispensePosition(BlockSource blockSource) {
        Direction direction = (Direction) blockSource.getBlockState().getValue(FACING);
        return new PositionImpl(blockSource.x() + (0.7d * direction.getStepX()), blockSource.y() + (0.7d * direction.getStepY()), blockSource.z() + (0.7d * direction.getStepZ()));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
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
        builder.add(FACING, TRIGGERED);
    }
}
