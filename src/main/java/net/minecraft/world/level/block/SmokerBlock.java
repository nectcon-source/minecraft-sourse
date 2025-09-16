package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SmokerBlock.class */
public class SmokerBlock extends AbstractFurnaceBlock {
    protected SmokerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new SmokerBlockEntity();
    }

    @Override // net.minecraft.world.level.block.AbstractFurnaceBlock
    protected void openContainer(Level level, BlockPos blockPos, Player player) {
        Object blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SmokerBlockEntity) {
            player.openMenu((MenuProvider) blockEntity);
            player.awardStat(Stats.INTERACT_WITH_SMOKER);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(LIT)).booleanValue()) {
            return;
        }
        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY();
        double z = blockPos.getZ() + 0.5d;
        if (random.nextDouble() < 0.1d) {
            level.playLocalSound(x, y, z, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        level.addParticle(ParticleTypes.SMOKE, x, y + 1.1d, z, 0.0d, 0.0d, 0.0d);
    }
}
