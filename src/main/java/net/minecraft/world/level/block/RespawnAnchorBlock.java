package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RespawnAnchorBlock.class */
public class RespawnAnchorBlock extends Block {
    public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
    private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = new ImmutableList.Builder().addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map((v0) -> {
        return v0.below();
    }).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map((v0) -> {
        return v0.above();
    }).iterator()).add(new Vec3i(0, 1, 0)).build();

    public RespawnAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (interactionHand == InteractionHand.MAIN_HAND && !isRespawnFuel(itemInHand) && isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        }
        if (isRespawnFuel(itemInHand) && canBeCharged(blockState)) {
            charge(level, blockPos, blockState);
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (((Integer) blockState.getValue(CHARGE)).intValue() == 0) {
            return InteractionResult.PASS;
        }
        if (canSetSpawn(level)) {
            if (!level.isClientSide) {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                if (serverPlayer.getRespawnDimension() != level.dimension() || !serverPlayer.getRespawnPosition().equals(blockPos)) {
                    serverPlayer.setRespawnPosition(level.dimension(), blockPos, 0.0f, false, true);
                    level.playSound(null, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            explode(blockState, level, blockPos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isRespawnFuel(ItemStack itemStack) {
        return itemStack.getItem() == Items.GLOWSTONE;
    }

    private static boolean canBeCharged(BlockState blockState) {
        return ((Integer) blockState.getValue(CHARGE)).intValue() < 4;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isWaterThatWouldFlow(BlockPos blockPos, Level level) {
        FluidState fluidState = level.getFluidState(blockPos);
        if (!fluidState.is(FluidTags.WATER)) {
            return false;
        }
        if (fluidState.isSource()) {
            return true;
        }
        return ((float) fluidState.getAmount()) >= 2.0f && !level.getFluidState(blockPos.below()).is(FluidTags.WATER);
    }

    private void explode(BlockState blockState, Level level, final BlockPos blockPos) {
        level.removeBlock(blockPos, false);
        Stream<Direction> stream = Direction.Plane.HORIZONTAL.stream();
        blockPos.getClass();
        final boolean z = stream.map(blockPos::relative).anyMatch(blockPos2 -> {
            return isWaterThatWouldFlow(blockPos2, level);
        }) || level.getFluidState(blockPos.above()).is(FluidTags.WATER);
        level.explode(null, DamageSource.badRespawnPointExplosion(), new ExplosionDamageCalculator() { // from class: net.minecraft.world.level.block.RespawnAnchorBlock.1
            @Override // net.minecraft.world.level.ExplosionDamageCalculator
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos3, BlockState blockState2, FluidState fluidState) {
                if (blockPos3.equals(blockPos) && z) {
                    return Optional.of(Float.valueOf(Blocks.WATER.getExplosionResistance()));
                }
                return super.getBlockExplosionResistance(explosion, blockGetter, blockPos3, blockState2, fluidState);
            }
        }, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 5.0f, true, Explosion.BlockInteraction.DESTROY);
    }

    public static boolean canSetSpawn(Level level) {
        return level.dimensionType().respawnAnchorWorks();
    }

    public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
        level.setBlock(blockPos, (BlockState) blockState.setValue(CHARGE, Integer.valueOf(((Integer) blockState.getValue(CHARGE)).intValue() + 1)), 3);
        level.playSound(null, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (((Integer) blockState.getValue(CHARGE)).intValue() == 0) {
            return;
        }
        if (random.nextInt(100) == 0) {
            level.playSound(null, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        level.addParticle(ParticleTypes.REVERSE_PORTAL, blockPos.getX() + 0.5d + (0.5d - random.nextDouble()), blockPos.getY() + 1.0d, blockPos.getZ() + 0.5d + (0.5d - random.nextDouble()), 0.0d, random.nextFloat() * 0.04d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    public static int getScaledChargeLevel(BlockState blockState, int i) {
        return Mth.floor(((((Integer) blockState.getValue(CHARGE)).intValue() - 0) / 4.0f) * i);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return getScaledChargeLevel(blockState, 15);
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos) {
        Optional<Vec3> findStandUpPosition = findStandUpPosition(entityType, collisionGetter, blockPos, true);
        if (findStandUpPosition.isPresent()) {
            return findStandUpPosition;
        }
        return findStandUpPosition(entityType, collisionGetter, blockPos, false);
    }

    private static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean z) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        UnmodifiableIterator it = RESPAWN_OFFSETS.iterator();
        while (it.hasNext()) {
            mutableBlockPos.set(blockPos).move((Vec3i) it.next());
            Vec3 findSafeDismountLocation = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, z);
            if (findSafeDismountLocation != null) {
                return Optional.of(findSafeDismountLocation);
            }
        }
        return Optional.empty();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
