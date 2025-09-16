package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WetSpongeBlock.class */
public class WetSpongeBlock extends Block {
    protected WetSpongeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (level.dimensionType().ultraWarm()) {
            level.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 3);
            level.levelEvent(2009, blockPos, 0);
            level.playSound((Player) null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, (1.0f + (level.getRandom().nextFloat() * 0.2f)) * 0.7f);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        double nextDouble;
        double nextDouble2;
        double d;
        Direction random2 = Direction.getRandom(random);
        if (random2 == Direction.UP) {
            return;
        }
        BlockPos relative = blockPos.relative(random2);
        BlockState blockState2 = level.getBlockState(relative);
        if (blockState.canOcclude() && blockState2.isFaceSturdy(level, relative, random2.getOpposite())) {
            return;
        }
        double x = blockPos.getX();
        double y = blockPos.getY();
        double z = blockPos.getZ();
        if (random2 == Direction.DOWN) {
            nextDouble = y - 0.05d;
            nextDouble2 = x + random.nextDouble();
            d = z + random.nextDouble();
        } else {
            nextDouble = y + (random.nextDouble() * 0.8d);
            if (random2.getAxis() == Direction.Axis.X) {
                d = z + random.nextDouble();
                nextDouble2 = random2 == Direction.EAST ? x + 1.1d : x + 0.05d;
            } else {
                nextDouble2 = x + random.nextDouble();
                d = random2 == Direction.SOUTH ? z + 1.1d : z + 0.05d;
            }
        }
        level.addParticle(ParticleTypes.DRIPPING_WATER, nextDouble2, nextDouble, d, 0.0d, 0.0d, 0.0d);
    }
}
