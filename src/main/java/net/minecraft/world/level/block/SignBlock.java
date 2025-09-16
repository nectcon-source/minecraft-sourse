package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SignBlock.class */
public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(4.0d, 0.0d, 4.0d, 12.0d, 16.0d, 12.0d);
    private final WoodType type;

    protected SignBlock(BlockBehaviour.Properties properties, WoodType woodType) {
        super(properties);
        this.type = woodType;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new SignBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        boolean z = (itemInHand.getItem() instanceof DyeItem) && player.abilities.mayBuild;
        if (level.isClientSide) {
            return z ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity) blockEntity;
            if (z && signBlockEntity.setColor(((DyeItem) itemInHand.getItem()).getDyeColor()) && !player.isCreative()) {
                itemInHand.shrink(1);
            }
            return signBlockEntity.executeClickCommands(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    public WoodType type() {
        return this.type;
    }
}
