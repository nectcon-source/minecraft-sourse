package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/HorizontalDirectionalBlock.class */
public abstract class HorizontalDirectionalBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected HorizontalDirectionalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }
}
