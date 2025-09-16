package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/MagmaBlock.class */
public class MagmaBlock extends Block {
    public MagmaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.Block
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        if (!entity.fireImmune() && (entity instanceof LivingEntity) && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.hurt(DamageSource.HOT_FLOOR, 1.0f);
        }
        super.stepOn(level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BubbleColumnBlock.growColumn(serverLevel, blockPos.above(), true);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.UP && blockState2.is(Blocks.WATER)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 20);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        BlockPos above = blockPos.above();
        if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
            serverLevel.playSound((Player) null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + ((serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.8f));
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, above.getX() + 0.5d, above.getY() + 0.25d, above.getZ() + 0.5d, 8, 0.5d, 0.25d, 0.5d, 0.0d);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        level.getBlockTicks().scheduleTick(blockPos, this, 20);
    }
}
