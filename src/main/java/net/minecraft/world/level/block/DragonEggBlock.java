package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/DragonEggBlock.class */
public class DragonEggBlock extends FallingBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 16.0d, 15.0d);

    public DragonEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        teleport(blockState, level, blockPos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        teleport(blockState, level, blockPos);
    }

    private void teleport(BlockState blockState, Level level, BlockPos blockPos) {
        for (int i = 0; i < 1000; i++) {
            BlockPos offset = blockPos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
            if (level.getBlockState(offset).isAir()) {
                if (level.isClientSide) {
                    for (int i2 = 0; i2 < 128; i2++) {
                        double nextDouble = level.random.nextDouble();
                        level.addParticle(ParticleTypes.PORTAL, Mth.lerp(nextDouble, offset.getX(), blockPos.getX()) + (level.random.nextDouble() - 0.5d) + 0.5d, (Mth.lerp(nextDouble, offset.getY(), blockPos.getY()) + level.random.nextDouble()) - 0.5d, Mth.lerp(nextDouble, offset.getZ(), blockPos.getZ()) + (level.random.nextDouble() - 0.5d) + 0.5d, (level.random.nextFloat() - 0.5f) * 0.2f, (level.random.nextFloat() - 0.5f) * 0.2f, (level.random.nextFloat() - 0.5f) * 0.2f);
                    }
                    return;
                }
                level.setBlock(offset, blockState, 2);
                level.removeBlock(blockPos, false);
                return;
            }
        }
    }

    @Override // net.minecraft.world.level.block.FallingBlock
    protected int getDelayAfterPlace() {
        return 5;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
