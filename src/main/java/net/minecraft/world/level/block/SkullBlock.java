package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SkullBlock.class */
public class SkullBlock extends AbstractSkullBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Block.box(4.0d, 0.0d, 4.0d, 12.0d, 8.0d, 12.0d);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SkullBlock$Type.class */
    public interface Type {
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SkullBlock$Types.class */
    public enum Types implements Type {
        SKELETON,
        WITHER_SKELETON,
        PLAYER,
        ZOMBIE,
        CREEPER,
        DRAGON
    }

    protected SkullBlock(Type type, BlockBehaviour.Properties properties) {
        super(type, properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(ROTATION, Integer.valueOf(Mth.floor(((blockPlaceContext.getRotation() * 16.0f) / 360.0f) + 0.5d) & 15));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(ROTATION, Integer.valueOf(rotation.rotate(((Integer) blockState.getValue(ROTATION)).intValue(), 16)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState) blockState.setValue(ROTATION, Integer.valueOf(mirror.mirror(((Integer) blockState.getValue(ROTATION)).intValue(), 16)));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }
}
