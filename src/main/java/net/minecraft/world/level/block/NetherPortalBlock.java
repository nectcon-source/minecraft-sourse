package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NetherPortalBlock.class */
public class NetherPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0d, 0.0d, 6.0d, 16.0d, 16.0d, 10.0d);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0d, 0.0d, 0.0d, 10.0d, 16.0d, 16.0d);

    public NetherPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch ((Direction.Axis) blockState.getValue(AXIS)) {
            case Z:
                return Z_AXIS_AABB;
            case X:
            default:
                return X_AXIS_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        Entity spawn;
        if (serverLevel.dimensionType().natural() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && random.nextInt(2000) < serverLevel.getDifficulty().getId()) {
            while (serverLevel.getBlockState(blockPos).is(this)) {
                blockPos = blockPos.below();
            }
            if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIFIED_PIGLIN) && (spawn = EntityType.ZOMBIFIED_PIGLIN.spawn(serverLevel, null, null, null, blockPos.above(), MobSpawnType.STRUCTURE, false, false)) != null) {
                spawn.setPortalCooldown();
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = (Direction.Axis) blockState.getValue(AXIS);
        if ((axis2 != axis && axis.isHorizontal()) || blockState2.is(this) || new PortalShape(levelAccessor, blockPos, axis2).isComplete()) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
            entity.handleInsidePortal(blockPos);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f, (random.nextFloat() * 0.4f) + 0.8f, false);
        }
        for (int i = 0; i < 4; i++) {
            double x = blockPos.getX() + random.nextDouble();
            double y = blockPos.getY() + random.nextDouble();
            double z = blockPos.getZ() + random.nextDouble();
            double nextFloat = (random.nextFloat() - 0.5d) * 0.5d;
            double nextFloat2 = (random.nextFloat() - 0.5d) * 0.5d;
            double nextFloat3 = (random.nextFloat() - 0.5d) * 0.5d;
            int nextInt = (random.nextInt(2) * 2) - 1;
            if (level.getBlockState(blockPos.west()).is(this) || level.getBlockState(blockPos.east()).is(this)) {
                z = blockPos.getZ() + 0.5d + (0.25d * nextInt);
                nextFloat3 = random.nextFloat() * 2.0f * nextInt;
            } else {
                x = blockPos.getX() + 0.5d + (0.25d * nextInt);
                nextFloat = random.nextFloat() * 2.0f * nextInt;
            }
            level.addParticle(ParticleTypes.PORTAL, x, y, z, nextFloat, nextFloat2, nextFloat3);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch ((Direction.Axis) blockState.getValue(AXIS)) {
                    case Z:
                        return (BlockState) blockState.setValue(AXIS, Direction.Axis.X);
                    case X:
                        return (BlockState) blockState.setValue(AXIS, Direction.Axis.Z);
                    default:
                        return blockState;
                }
            default:
                return blockState;
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}
