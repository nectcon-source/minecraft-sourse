package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BaseFireBlock.class */
public abstract class BaseFireBlock extends Block {
    private final float fireDamage;
    protected static final VoxelShape DOWN_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 1.0d, 16.0d);

    protected abstract boolean canBurn(BlockState blockState);

    public BaseFireBlock(BlockBehaviour.Properties properties, float f) {
        super(properties);
        this.fireDamage = f;
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return getState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    public static BlockState getState(BlockGetter blockGetter, BlockPos blockPos) {
        if (SoulFireBlock.canSurviveOnBlock(blockGetter.getBlockState(blockPos.below()).getBlock())) {
            return Blocks.SOUL_FIRE.defaultBlockState();
        }
        return ((FireBlock) Blocks.FIRE).getStateForPlacement(blockGetter, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return DOWN_AABB;
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0f + random.nextFloat(), (random.nextFloat() * 0.7f) + 0.3f, false);
        }
        BlockPos below = blockPos.below();
        BlockState blockState2 = level.getBlockState(below);
        if (canBurn(blockState2) || blockState2.isFaceSturdy(level, below, Direction.UP)) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + random.nextDouble(), blockPos.getY() + (random.nextDouble() * 0.5d) + 0.5d, blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
            }
            return;
        }
        if (canBurn(level.getBlockState(blockPos.west()))) {
            for (int i2 = 0; i2 < 2; i2++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + (random.nextDouble() * 0.10000000149011612d), blockPos.getY() + random.nextDouble(), blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
            }
        }
        if (canBurn(level.getBlockState(blockPos.east()))) {
            for (int i3 = 0; i3 < 2; i3++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (blockPos.getX() + 1) - (random.nextDouble() * 0.10000000149011612d), blockPos.getY() + random.nextDouble(), blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
            }
        }
        if (canBurn(level.getBlockState(blockPos.north()))) {
            for (int i4 = 0; i4 < 2; i4++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + random.nextDouble(), blockPos.getY() + random.nextDouble(), blockPos.getZ() + (random.nextDouble() * 0.10000000149011612d), 0.0d, 0.0d, 0.0d);
            }
        }
        if (canBurn(level.getBlockState(blockPos.south()))) {
            for (int i5 = 0; i5 < 2; i5++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + random.nextDouble(), blockPos.getY() + random.nextDouble(), (blockPos.getZ() + 1) - (random.nextDouble() * 0.10000000149011612d), 0.0d, 0.0d, 0.0d);
            }
        }
        if (canBurn(level.getBlockState(blockPos.above()))) {
            for (int i6 = 0; i6 < 2; i6++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + random.nextDouble(), (blockPos.getY() + 1) - (random.nextDouble() * 0.10000000149011612d), blockPos.getZ() + random.nextDouble(), 0.0d, 0.0d, 0.0d);
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.setSecondsOnFire(8);
            }
            entity.hurt(DamageSource.IN_FIRE, this.fireDamage);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (inPortalDimension(level)) {
            Optional<PortalShape> findEmptyPortalShape = PortalShape.findEmptyPortalShape(level, blockPos, Direction.Axis.X);
            if (findEmptyPortalShape.isPresent()) {
                findEmptyPortalShape.get().createPortalBlocks();
                return;
            }
        }
        if (!blockState.canSurvive(level, blockPos)) {
            level.removeBlock(blockPos, false);
        }
    }

    private static boolean inPortalDimension(Level level) {
        return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide()) {
            level.levelEvent(null, 1009, blockPos, 0);
        }
    }

    public static boolean canBePlacedAt(Level level, BlockPos blockPos, Direction direction) {
        if (level.getBlockState(blockPos).isAir()) {
            return getState(level, blockPos).canSurvive(level, blockPos) || isPortal(level, blockPos, direction);
        }
        return false;
    }

    private static boolean isPortal(Level level, BlockPos blockPos, Direction direction) {
        if (!inPortalDimension(level)) {
            return false;
        }
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        boolean z = false;
        Direction[] values = Direction.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            if (!level.getBlockState(mutable.set(blockPos).move(values[i])).is(Blocks.OBSIDIAN)) {
                i++;
            } else {
                z = true;
                break;
            }
        }
        if (!z) {
            return false;
        }
        return PortalShape.findEmptyPortalShape(level, blockPos, direction.getAxis().isHorizontal() ? direction.getCounterClockWise().getAxis() : Direction.Plane.HORIZONTAL.getRandomAxis(level.random)).isPresent();
    }
}
