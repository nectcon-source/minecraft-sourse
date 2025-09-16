package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CommandBlock.class */
public class CommandBlock extends BaseEntityBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty CONDITIONAL = BlockStateProperties.CONDITIONAL;

    public CommandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(CONDITIONAL, false));
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        CommandBlockEntity commandBlockEntity = new CommandBlockEntity();
        commandBlockEntity.setAutomatic(this == Blocks.CHAIN_COMMAND_BLOCK);
        return commandBlockEntity;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof CommandBlockEntity)) {
            return;
        }
        CommandBlockEntity commandBlockEntity = (CommandBlockEntity) blockEntity;
        boolean hasNeighborSignal = level.hasNeighborSignal(blockPos);
        boolean isPowered = commandBlockEntity.isPowered();
        commandBlockEntity.setPowered(hasNeighborSignal);
        if (!isPowered && !commandBlockEntity.isAutomatic() && commandBlockEntity.getMode() != CommandBlockEntity.Mode.SEQUENCE && hasNeighborSignal) {
            commandBlockEntity.markConditionMet();
            level.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            CommandBlockEntity commandBlockEntity = (CommandBlockEntity) blockEntity;
            BaseCommandBlock commandBlock = commandBlockEntity.getCommandBlock();
            boolean z = !StringUtil.isNullOrEmpty(commandBlock.getCommand());
            CommandBlockEntity.Mode mode = commandBlockEntity.getMode();
            boolean wasConditionMet = commandBlockEntity.wasConditionMet();
            if (mode == CommandBlockEntity.Mode.AUTO) {
                commandBlockEntity.markConditionMet();
                if (wasConditionMet) {
                    execute(blockState, serverLevel, blockPos, commandBlock, z);
                } else if (commandBlockEntity.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
                if (commandBlockEntity.isPowered() || commandBlockEntity.isAutomatic()) {
                    serverLevel.getBlockTicks().scheduleTick(blockPos, this, 1);
                }
            } else if (mode == CommandBlockEntity.Mode.REDSTONE) {
                if (wasConditionMet) {
                    execute(blockState, serverLevel, blockPos, commandBlock, z);
                } else if (commandBlockEntity.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
            }
            serverLevel.updateNeighbourForOutputSignal(blockPos, this);
        }
    }

    private void execute(BlockState blockState, Level level, BlockPos blockPos, BaseCommandBlock baseCommandBlock, boolean z) {
        if (z) {
            baseCommandBlock.performCommand(level);
        } else {
            baseCommandBlock.setSuccessCount(0);
        }
        executeChain(level, blockPos, (Direction) blockState.getValue(FACING));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if ((blockEntity instanceof CommandBlockEntity) && player.canUseGameMasterBlocks()) {
            player.openCommandBlock((CommandBlockEntity) blockEntity);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            return ((CommandBlockEntity) blockEntity).getCommandBlock().getSuccessCount();
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof CommandBlockEntity)) {
            return;
        }
        CommandBlockEntity commandBlockEntity = (CommandBlockEntity) blockEntity;
        BaseCommandBlock commandBlock = commandBlockEntity.getCommandBlock();
        if (itemStack.hasCustomHoverName()) {
            commandBlock.setName(itemStack.getHoverName());
        }
        if (!level.isClientSide) {
            if (itemStack.getTagElement("BlockEntityTag") == null) {
                commandBlock.setTrackOutput(level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK));
                commandBlockEntity.setAutomatic(this == Blocks.CHAIN_COMMAND_BLOCK);
            }
            if (commandBlockEntity.getMode() == CommandBlockEntity.Mode.SEQUENCE) {
                commandBlockEntity.setPowered(level.hasNeighborSignal(blockPos));
            }
        }
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
        builder.add(FACING, CONDITIONAL);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
    }

    private static void executeChain(Level level, BlockPos blockPos, Direction direction) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        GameRules gameRules = level.getGameRules();
        int i = gameRules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
        while (true) {
            int i2 = i;
            i--;
            if (i2 <= 0) {
                break;
            }
            mutable.move(direction);
            BlockState blockState = level.getBlockState(mutable);
            Block block = blockState.getBlock();
            if (!blockState.is(Blocks.CHAIN_COMMAND_BLOCK)) {
                break;
            }
            BlockEntity blockEntity = level.getBlockEntity(mutable);
            if (!(blockEntity instanceof CommandBlockEntity)) {
                break;
            }
            CommandBlockEntity commandBlockEntity = (CommandBlockEntity) blockEntity;
            if (commandBlockEntity.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
                break;
            }
            if (commandBlockEntity.isPowered() || commandBlockEntity.isAutomatic()) {
                BaseCommandBlock commandBlock = commandBlockEntity.getCommandBlock();
                if (commandBlockEntity.markConditionMet()) {
                    if (!commandBlock.performCommand(level)) {
                        break;
                    } else {
                        level.updateNeighbourForOutputSignal(mutable, block);
                    }
                } else if (commandBlockEntity.isConditional()) {
                    commandBlock.setSuccessCount(0);
                }
            }
            direction = (Direction) blockState.getValue(FACING);
        }
        if (i <= 0) {
            LOGGER.warn("Command Block chain tried to execute more than {} steps!", Integer.valueOf(Math.max(gameRules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH), 0)));
        }
    }
}
