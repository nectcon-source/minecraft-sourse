package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/JigsawBlock.class */
public class JigsawBlock extends Block implements EntityBlock {
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    protected JigsawBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(ORIENTATION, rotation.rotation().rotate((FrontAndTop) blockState.getValue(ORIENTATION)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState) blockState.setValue(ORIENTATION, mirror.rotation().rotate((FrontAndTop) blockState.getValue(ORIENTATION)));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction;
        Direction clickedFace = blockPlaceContext.getClickedFace();
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            direction = blockPlaceContext.getHorizontalDirection().getOpposite();
        } else {
            direction = Direction.UP;
        }
        return (BlockState) defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(clickedFace, direction));
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new JigsawBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if ((blockEntity instanceof JigsawBlockEntity) && player.canUseGameMasterBlocks()) {
            player.openJigsawBlock((JigsawBlockEntity) blockEntity);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2) {
        Direction frontFacing = getFrontFacing(structureBlockInfo.state);
        return frontFacing == getFrontFacing(structureBlockInfo2.state).getOpposite() && ((JigsawBlockEntity.JointType.byName(structureBlockInfo.nbt.getString("joint")).orElseGet(() -> {
            return frontFacing.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
        }) == JigsawBlockEntity.JointType.ROLLABLE) || getTopFacing(structureBlockInfo.state) == getTopFacing(structureBlockInfo2.state)) && structureBlockInfo.nbt.getString("target").equals(structureBlockInfo2.nbt.getString("name"));
    }

    public static Direction getFrontFacing(BlockState blockState) {
        return ((FrontAndTop) blockState.getValue(ORIENTATION)).front();
    }

    public static Direction getTopFacing(BlockState blockState) {
        return ((FrontAndTop) blockState.getValue(ORIENTATION)).top();
    }
}
