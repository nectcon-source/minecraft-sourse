package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BlastFurnaceBlock.class */
public class BlastFurnaceBlock extends AbstractFurnaceBlock {
    protected BlastFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BlastFurnaceBlockEntity();
    }

    @Override // net.minecraft.world.level.block.AbstractFurnaceBlock
    protected void openContainer(Level level, BlockPos blockPos, Player player) {
        Object blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BlastFurnaceBlockEntity) {
            player.openMenu((MenuProvider) blockEntity);
            player.awardStat(Stats.INTERACT_WITH_BLAST_FURNACE);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if ((Boolean)blockState.getValue(LIT)) {
            double var5 = (double)blockPos.getX() + (double)0.5F;
            double var7 = (double)blockPos.getY();
            double var9 = (double)blockPos.getZ() + (double)0.5F;
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(var5, var7, var9, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction var11 = (Direction)blockState.getValue(FACING);
            Direction.Axis var12 = var11.getAxis();
            double var13 = 0.52;
            double var15 = random.nextDouble() * 0.6 - 0.3;
            double var17 = var12 == Direction.Axis.X ? (double)var11.getStepX() * 0.52 : var15;
            double var19 = random.nextDouble() * (double)9.0F / (double)16.0F;
            double var21 = var12 == Direction.Axis.Z ? (double)var11.getStepZ() * 0.52 : var15;
            level.addParticle(ParticleTypes.SMOKE, var5 + var17, var7 + var19, var9 + var21, (double)0.0F, (double)0.0F, (double)0.0F);
        }
    }
}
