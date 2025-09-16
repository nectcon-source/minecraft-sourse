package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TargetBlock.class */
public class TargetBlock extends Block {
    private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

    public TargetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(OUTPUT_POWER, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        int updateRedstoneOutput = updateRedstoneOutput(level, blockState, blockHitResult, projectile);
        Entity owner = projectile.getOwner();
        if (owner instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) owner;
            serverPlayer.awardStat(Stats.TARGET_HIT);
            CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, projectile, blockHitResult.getLocation(), updateRedstoneOutput);
        }
    }

    private static int updateRedstoneOutput(LevelAccessor levelAccessor, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
        int redstoneStrength = getRedstoneStrength(blockHitResult, blockHitResult.getLocation());
        int i = entity instanceof AbstractArrow ? 20 : 8;
        if (!levelAccessor.getBlockTicks().hasScheduledTick(blockHitResult.getBlockPos(), blockState.getBlock())) {
            setOutputPower(levelAccessor, blockState, redstoneStrength, blockHitResult.getBlockPos(), i);
        }
        return redstoneStrength;
    }

    private static int getRedstoneStrength(BlockHitResult blockHitResult, Vec3 vec3) {
        double max;
        Direction direction = blockHitResult.getDirection();
        double abs = Math.abs(Mth.frac(vec3.x) - 0.5d);
        double abs2 = Math.abs(Mth.frac(vec3.y) - 0.5d);
        double abs3 = Math.abs(Mth.frac(vec3.z) - 0.5d);
        Direction.Axis axis = direction.getAxis();
        if (axis == Direction.Axis.Y) {
            max = Math.max(abs, abs3);
        } else if (axis == Direction.Axis.Z) {
            max = Math.max(abs, abs2);
        } else {
            max = Math.max(abs2, abs3);
        }
        return Math.max(1, Mth.ceil(15.0d * Mth.clamp((0.5d - max) / 0.5d, 0.0d, 1.0d)));
    }

    private static void setOutputPower(LevelAccessor levelAccessor, BlockState blockState, int i, BlockPos blockPos, int i2) {
        levelAccessor.setBlock(blockPos, (BlockState) blockState.setValue(OUTPUT_POWER, Integer.valueOf(i)), 3);
        levelAccessor.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), i2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (((Integer) blockState.getValue(OUTPUT_POWER)).intValue() != 0) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(OUTPUT_POWER, 0), 3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Integer) blockState.getValue(OUTPUT_POWER)).intValue();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OUTPUT_POWER);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!level.isClientSide() && !blockState.is(blockState2.getBlock()) && ((Integer) blockState.getValue(OUTPUT_POWER)).intValue() > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(OUTPUT_POWER, 0), 18);
        }
    }
}
