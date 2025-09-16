package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FallingBlock.class */
public class FallingBlock extends Block {
    public FallingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        level.getBlockTicks().scheduleTick(blockPos, this, getDelayAfterPlace());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        levelAccessor.getBlockTicks().scheduleTick(blockPos, this, getDelayAfterPlace());
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!isFree(serverLevel.getBlockState(blockPos.below())) || blockPos.getY() < 0) {
            return;
        }
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(serverLevel, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, serverLevel.getBlockState(blockPos));
        falling(fallingBlockEntity);
        serverLevel.addFreshEntity(fallingBlockEntity);
    }

    protected void falling(FallingBlockEntity fallingBlockEntity) {
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(BlockState blockState) {
        Material material = blockState.getMaterial();
        return blockState.isAir() || blockState.is(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable();
    }

    public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
    }

    public void onBroken(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (random.nextInt(16) == 0 && isFree(level.getBlockState(blockPos.below()))) {
            level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, blockState), blockPos.getX() + random.nextDouble(), blockPos.getY() - 0.05d, blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
        }
    }

    public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return -16777216;
    }
}
