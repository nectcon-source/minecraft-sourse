package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DirectionalBlock.class */
public abstract class DirectionalBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected DirectionalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
