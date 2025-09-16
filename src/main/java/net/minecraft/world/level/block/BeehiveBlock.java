package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BeehiveBlock.class */
public class BeehiveBlock extends BaseEntityBlock {
    private static final Direction[] SPAWN_DIRECTIONS = {Direction.WEST, Direction.EAST, Direction.SOUTH};
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;

    public BeehiveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(  this.stateDefinition.any().setValue(HONEY_LEVEL, 0).setValue(FACING, Direction.NORTH));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return ((Integer) blockState.getValue(HONEY_LEVEL)).intValue();
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        if (!level.isClientSide && (blockEntity instanceof BeehiveBlockEntity)) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) blockEntity;
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
                beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                level.updateNeighbourForOutputSignal(blockPos, this);
                angerNearbyBees(level, blockPos);
            }
            CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer) player, blockState.getBlock(), itemStack, beehiveBlockEntity.getOccupantCount());
        }
    }

    private void angerNearbyBees(Level level, BlockPos blockPos) {
        List<Bee> entitiesOfClass = level.getEntitiesOfClass(Bee.class, new AABB(blockPos).inflate(8.0d, 6.0d, 8.0d));
        if (!entitiesOfClass.isEmpty()) {
            List<Player> entitiesOfClass2 = level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(8.0d, 6.0d, 8.0d));
            int size = entitiesOfClass2.size();
            for (Bee bee : entitiesOfClass) {
                if (bee.getTarget() == null) {
                    bee.setTarget(entitiesOfClass2.get(level.random.nextInt(size)));
                }
            }
        }
    }

    public static void dropHoneycomb(Level level, BlockPos blockPos) {
        popResource(level, blockPos, new ItemStack(Items.HONEYCOMB, 3));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        boolean z = false;
        if (((Integer) blockState.getValue(HONEY_LEVEL)).intValue() >= 5) {
            if (itemInHand.getItem() == Items.SHEARS) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0f, 1.0f);
                dropHoneycomb(level, blockPos);
                itemInHand.hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
                z = true;
            } else if (itemInHand.getItem() == Items.GLASS_BOTTLE) {
                itemInHand.shrink(1);
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                if (itemInHand.isEmpty()) {
                    player.setItemInHand(interactionHand, new ItemStack(Items.HONEY_BOTTLE));
                } else if (!player.inventory.add(new ItemStack(Items.HONEY_BOTTLE))) {
                    player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
                }
                z = true;
            }
        }
        if (z) {
            if (!CampfireBlock.isSmokeyPos(level, blockPos)) {
                if (hiveContainsBees(level, blockPos)) {
                    angerNearbyBees(level, blockPos);
                }
                releaseBeesAndResetHoneyLevel(level, blockState, blockPos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            } else {
                resetHoneyLevel(level, blockState, blockPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private boolean hiveContainsBees(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return (blockEntity instanceof BeehiveBlockEntity) && !((BeehiveBlockEntity) blockEntity).isEmpty();
    }

    public void releaseBeesAndResetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos, @Nullable Player player, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus) {
        resetHoneyLevel(level, blockState, blockPos);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            ((BeehiveBlockEntity) blockEntity).emptyAllLivingFromHive(player, blockState, beeReleaseStatus);
        }
    }

    public void resetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos) {
        level.setBlock(blockPos, (BlockState) blockState.setValue(HONEY_LEVEL, 0), 3);
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (((Integer) blockState.getValue(HONEY_LEVEL)).intValue() >= 5) {
            for (int i = 0; i < random.nextInt(1) + 1; i++) {
                trySpawnDripParticles(level, blockPos, blockState);
            }
        }
    }

    private void trySpawnDripParticles(Level level, BlockPos blockPos, BlockState blockState) {
        if (!blockState.getFluidState().isEmpty() || level.random.nextFloat() < 0.3f) {
            return;
        }
        VoxelShape collisionShape = blockState.getCollisionShape(level, blockPos);
        if (collisionShape.max(Direction.Axis.Y) >= 1.0d && !blockState.is(BlockTags.IMPERMEABLE)) {
            double min = collisionShape.min(Direction.Axis.Y);
            if (min > 0.0d) {
                spawnParticle(level, blockPos, collisionShape, (blockPos.getY() + min) - 0.05d);
                return;
            }
            BlockPos below = blockPos.below();
            BlockState blockState2 = level.getBlockState(below);
            if ((blockState2.getCollisionShape(level, below).max(Direction.Axis.Y) < 1.0d || !blockState2.isCollisionShapeFullBlock(level, below)) && blockState2.getFluidState().isEmpty()) {
                spawnParticle(level, blockPos, collisionShape, blockPos.getY() - 0.05d);
            }
        }
    }

    private void spawnParticle(Level level, BlockPos blockPos, VoxelShape voxelShape, double d) {
        spawnFluidParticle(level, blockPos.getX() + voxelShape.min(Direction.Axis.X), blockPos.getX() + voxelShape.max(Direction.Axis.X), blockPos.getZ() + voxelShape.min(Direction.Axis.Z), blockPos.getZ() + voxelShape.max(Direction.Axis.Z), d);
    }

    private void spawnFluidParticle(Level level, double d, double d2, double d3, double d4, double d5) {
        level.addParticle(ParticleTypes.DRIPPING_HONEY, Mth.lerp(level.random.nextDouble(), d, d2), d5, Mth.lerp(level.random.nextDouble(), d3, d4), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HONEY_LEVEL, FACING);
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BeehiveBlockEntity();
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && player.isCreative() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof BeehiveBlockEntity) {
                BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity) blockEntity;
                ItemStack itemStack = new ItemStack(this);
                int intValue = ((Integer) blockState.getValue(HONEY_LEVEL)).intValue();
                boolean z = !beehiveBlockEntity.isEmpty();
                if (!z && intValue == 0) {
                    return;
                }
                if (z) {
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.put("Bees", beehiveBlockEntity.writeBees());
                    itemStack.addTagElement("BlockEntityTag", compoundTag);
                }
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putInt("honey_level", intValue);
                itemStack.addTagElement("BlockStateTag", compoundTag2);
                ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
        Entity entity = (Entity) builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if ((entity instanceof PrimedTnt) || (entity instanceof Creeper) || (entity instanceof WitherSkull) || (entity instanceof WitherBoss) || (entity instanceof MinecartTNT)) {
            BlockEntity blockEntity = (BlockEntity) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof BeehiveBlockEntity) {
                ((BeehiveBlockEntity) blockEntity).emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
        }
        return super.getDrops(blockState, builder);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (levelAccessor.getBlockState(blockPos2).getBlock() instanceof FireBlock) {
            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
            if (blockEntity instanceof BeehiveBlockEntity) {
                ((BeehiveBlockEntity) blockEntity).emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public static Direction getRandomOffset(Random random) {
        return (Direction) Util.getRandom(SPAWN_DIRECTIONS, random);
    }
}
