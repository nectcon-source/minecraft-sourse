package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CryingObsidianBlock.class */
public class CryingObsidianBlock extends Block {
    public CryingObsidianBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        Direction random2;
        if (random.nextInt(5) != 0 || (random2 = Direction.getRandom(random)) == Direction.UP) {
            return;
        }
        BlockPos relative = blockPos.relative(random2);
        BlockState blockState2 = level.getBlockState(relative);
        if (blockState.canOcclude() && blockState2.isFaceSturdy(level, relative, random2.getOpposite())) {
            return;
        }
        level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, blockPos.getX() + (random2.getStepX() == 0 ? random.nextDouble() : 0.5d + (random2.getStepX() * 0.6d)), blockPos.getY() + (random2.getStepY() == 0 ? random.nextDouble() : 0.5d + (random2.getStepY() * 0.6d)), blockPos.getZ() + (random2.getStepZ() == 0 ? random.nextDouble() : 0.5d + (random2.getStepZ() * 0.6d)), 0.0d, 0.0d, 0.0d);
    }
}
