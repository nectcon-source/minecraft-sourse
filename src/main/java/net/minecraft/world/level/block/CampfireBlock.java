package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CampfireBlock.class */
public class CampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 7.0d, 16.0d);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape VIRTUAL_FENCE_POST = Block.box(6.0d, 0.0d, 6.0d, 10.0d, 16.0d, 10.0d);
    private final boolean spawnParticles;
    private final int fireDamage;

    public CampfireBlock(boolean z, int i, BlockBehaviour.Properties properties) {
        super(properties);
        this.spawnParticles = z;
        this.fireDamage = i;
        registerDefaultState( this.stateDefinition.any().setValue(LIT, true).setValue(SIGNAL_FIRE, false).setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CampfireBlockEntity) {
            CampfireBlockEntity campfireBlockEntity = (CampfireBlockEntity) blockEntity;
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            Optional<CampfireCookingRecipe> cookableRecipe = campfireBlockEntity.getCookableRecipe(itemInHand);
            if (cookableRecipe.isPresent()) {
                if (!level.isClientSide) {
                    if (campfireBlockEntity.placeFood(player.abilities.instabuild ? itemInHand.copy() : itemInHand, cookableRecipe.get().getCookingTime())) {
                        player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!entity.fireImmune() && ((Boolean) blockState.getValue(LIT)).booleanValue() && (entity instanceof LivingEntity) && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.hurt(DamageSource.IN_FIRE, this.fireDamage);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CampfireBlockEntity) {
            Containers.dropContents(level, blockPos, ((CampfireBlockEntity) blockEntity).getItems());
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        LevelAccessor level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        boolean z = level.getFluidState(clickedPos).getType() == Fluids.WATER;
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(z))).setValue(SIGNAL_FIRE, Boolean.valueOf(isSmokeSource(level.getBlockState(clickedPos.below()))))).setValue(LIT, Boolean.valueOf(!z))).setValue(FACING, blockPlaceContext.getHorizontalDirection());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction == Direction.DOWN) {
            return (BlockState) blockState.setValue(SIGNAL_FIRE, Boolean.valueOf(isSmokeSource(blockState2)));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private boolean isSmokeSource(BlockState blockState) {
        return blockState.is(Blocks.HAY_BLOCK);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(LIT)).booleanValue()) {
            return;
        }
        if (random.nextInt(10) == 0) {
            level.playLocalSound(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f + random.nextFloat(), (random.nextFloat() * 0.7f) + 0.6f, false);
        }
        if (this.spawnParticles && random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; i++) {
                level.addParticle(ParticleTypes.LAVA, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, random.nextFloat() / 2.0f, 5.0E-5d, random.nextFloat() / 2.0f);
            }
        }
    }

    public static void dowse(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (levelAccessor.isClientSide()) {
            for (int i = 0; i < 20; i++) {
                makeParticles((Level) levelAccessor, blockPos, ((Boolean) blockState.getValue(SIGNAL_FIRE)).booleanValue(), true);
            }
        }
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (blockEntity instanceof CampfireBlockEntity) {
            ((CampfireBlockEntity) blockEntity).dowse();
        }
    }

    @Override // net.minecraft.world.level.block.SimpleWaterloggedBlock, net.minecraft.world.level.block.LiquidBlockContainer
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!((Boolean) blockState.getValue(BlockStateProperties.WATERLOGGED)).booleanValue() && fluidState.getType() == Fluids.WATER) {
            if (((Boolean) blockState.getValue(LIT)).booleanValue()) {
                if (!levelAccessor.isClientSide()) {
                    levelAccessor.playSound(null, blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                dowse(levelAccessor, blockPos, blockState);
            }
            levelAccessor.setBlock(blockPos, (BlockState) ((BlockState) blockState.setValue(WATERLOGGED, true)).setValue(LIT, false), 3);
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            Entity owner = projectile.getOwner();
            if ((owner == null || (owner instanceof Player) || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) && !((Boolean) blockState.getValue(LIT)).booleanValue() && !((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
                level.setBlock(blockHitResult.getBlockPos(), (BlockState) blockState.setValue(BlockStateProperties.LIT, true), 11);
            }
        }
    }

    public static void makeParticles(Level level, BlockPos blockPos, boolean z, boolean z2) {
        Random random = level.getRandom();
        level.addAlwaysVisibleParticle(z ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE, true, blockPos.getX() + 0.5d + ((random.nextDouble() / 3.0d) * (random.nextBoolean() ? 1 : -1)), blockPos.getY() + random.nextDouble() + random.nextDouble(), blockPos.getZ() + 0.5d + ((random.nextDouble() / 3.0d) * (random.nextBoolean() ? 1 : -1)), 0.0d, 0.07d, 0.0d);
        if (z2) {
            level.addParticle(ParticleTypes.SMOKE, blockPos.getX() + 0.25d + ((random.nextDouble() / 2.0d) * (random.nextBoolean() ? 1 : -1)), blockPos.getY() + 0.4d, blockPos.getZ() + 0.25d + ((random.nextDouble() / 2.0d) * (random.nextBoolean() ? 1 : -1)), 0.0d, 0.005d, 0.0d);
        }
    }

    public static boolean isSmokeyPos(Level level, BlockPos blockPos) {
        for (int i = 1; i <= 5; i++) {
            BlockPos below = blockPos.below(i);
            BlockState blockState = level.getBlockState(below);
            if (isLitCampfire(blockState)) {
                return true;
            }
            if (Shapes.joinIsNotEmpty(VIRTUAL_FENCE_POST, blockState.getCollisionShape(level, blockPos, CollisionContext.empty()), BooleanOp.AND)) {
                return isLitCampfire(level.getBlockState(below.below()));
            }
        }
        return false;
    }

    public static boolean isLitCampfire(BlockState blockState) {
        return blockState.hasProperty(LIT) && blockState.is(BlockTags.CAMPFIRES) && ((Boolean) blockState.getValue(LIT)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new CampfireBlockEntity();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    public static boolean canLight(BlockState blockState) {
        return (!blockState.is(BlockTags.CAMPFIRES, blockStateBase -> {
            return blockStateBase.hasProperty(BlockStateProperties.WATERLOGGED) && blockStateBase.hasProperty(BlockStateProperties.LIT);
        }) || ((Boolean) blockState.getValue(BlockStateProperties.WATERLOGGED)).booleanValue() || ((Boolean) blockState.getValue(BlockStateProperties.LIT)).booleanValue()) ? false : true;
    }
}
