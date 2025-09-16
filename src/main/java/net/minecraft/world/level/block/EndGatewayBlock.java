package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/EndGatewayBlock.class */
public class EndGatewayBlock extends BaseEntityBlock {
    protected EndGatewayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new TheEndGatewayBlockEntity();
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TheEndGatewayBlockEntity)) {
            return;
        }
        int particleAmount = ((TheEndGatewayBlockEntity) blockEntity).getParticleAmount();
        for (int i = 0; i < particleAmount; i++) {
            double x = blockPos.getX() + random.nextDouble();
            double y = blockPos.getY() + random.nextDouble();
            double z = blockPos.getZ() + random.nextDouble();
            double nextDouble = (random.nextDouble() - 0.5d) * 0.5d;
            double nextDouble2 = (random.nextDouble() - 0.5d) * 0.5d;
            double nextDouble3 = (random.nextDouble() - 0.5d) * 0.5d;
            int nextInt = (random.nextInt(2) * 2) - 1;
            if (random.nextBoolean()) {
                z = blockPos.getZ() + 0.5d + (0.25d * nextInt);
                nextDouble3 = random.nextFloat() * 2.0f * nextInt;
            } else {
                x = blockPos.getX() + 0.5d + (0.25d * nextInt);
                nextDouble = random.nextFloat() * 2.0f * nextInt;
            }
            level.addParticle(ParticleTypes.PORTAL, x, y, z, nextDouble, nextDouble2, nextDouble3);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return false;
    }
}
