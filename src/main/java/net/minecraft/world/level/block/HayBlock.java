package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/HayBlock.class */
public class HayBlock extends RotatedPillarBlock {
    public HayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        entity.causeFallDamage(f, 0.2f);
    }
}
