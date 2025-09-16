package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TurtleEggBlock.class */
public class TurtleEggBlock extends Block {
    private static final VoxelShape ONE_EGG_AABB = Block.box(3.0d, 0.0d, 3.0d, 12.0d, 7.0d, 12.0d);
    private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 7.0d, 15.0d);
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

    public TurtleEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(HATCH, 0)).setValue(EGGS, 1));
    }

    @Override // net.minecraft.world.level.block.Block
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        destroyEgg(level, blockPos, entity, 100);
        super.stepOn(level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.Block
    public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
        if (!(entity instanceof Zombie)) {
            destroyEgg(level, blockPos, entity, 3);
        }
        super.fallOn(level, blockPos, entity, f);
    }

    private void destroyEgg(Level level, BlockPos blockPos, Entity entity, int i) {
        if (canDestroyEgg(level, entity) && !level.isClientSide && level.random.nextInt(i) == 0) {
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.is(Blocks.TURTLE_EGG)) {
                decreaseEggs(level, blockPos, blockState);
            }
        }
    }

    private void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState) {
        level.playSound((Player) null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7f, 0.9f + (level.random.nextFloat() * 0.2f));
        int intValue = ((Integer) blockState.getValue(EGGS)).intValue();
        if (intValue <= 1) {
            level.destroyBlock(blockPos, false);
        } else {
            level.setBlock(blockPos, (BlockState) blockState.setValue(EGGS, Integer.valueOf(intValue - 1)), 2);
            level.levelEvent(2001, blockPos, Block.getId(blockState));
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (shouldUpdateHatchLevel(serverLevel) && onSand(serverLevel, blockPos)) {
            int intValue = ((Integer) blockState.getValue(HATCH)).intValue();
            if (intValue < 2) {
                serverLevel.playSound((Player) null, blockPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7f, 0.9f + (random.nextFloat() * 0.2f));
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(HATCH, Integer.valueOf(intValue + 1)), 2);
                return;
            }
            serverLevel.playSound((Player) null, blockPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7f, 0.9f + (random.nextFloat() * 0.2f));
            serverLevel.removeBlock(blockPos, false);
            for (int i = 0; i < ((Integer) blockState.getValue(EGGS)).intValue(); i++) {
                serverLevel.levelEvent(2001, blockPos, Block.getId(blockState));
                Turtle create = EntityType.TURTLE.create(serverLevel);
                create.setAge(-24000);
                create.setHomePos(blockPos);
                create.moveTo(blockPos.getX() + 0.3d + (i * 0.2d), blockPos.getY(), blockPos.getZ() + 0.3d, 0.0f, 0.0f);
                serverLevel.addFreshEntity(create);
            }
        }
    }

    public static boolean onSand(BlockGetter blockGetter, BlockPos blockPos) {
        return isSand(blockGetter, blockPos.below());
    }

    public static boolean isSand(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos).is(BlockTags.SAND);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (onSand(level, blockPos) && !level.isClientSide) {
            level.levelEvent(2005, blockPos, 0);
        }
    }

    private boolean shouldUpdateHatchLevel(Level level) {
        float timeOfDay = level.getTimeOfDay(1.0f);
        return (((double) timeOfDay) < 0.69d && ((double) timeOfDay) > 0.65d) || level.random.nextInt(500) == 0;
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        decreaseEggs(level, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        if (blockPlaceContext.getItemInHand().getItem() == asItem() && ((Integer) blockState.getValue(EGGS)).intValue() < 4) {
            return true;
        }
        return super.canBeReplaced(blockState, blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
        if (blockState.is(this)) {
            return (BlockState) blockState.setValue(EGGS, Integer.valueOf(Math.min(4, ((Integer) blockState.getValue(EGGS)).intValue() + 1)));
        }
        return super.getStateForPlacement(blockPlaceContext);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (((Integer) blockState.getValue(EGGS)).intValue() > 1) {
            return MULTIPLE_EGGS_AABB;
        }
        return ONE_EGG_AABB;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    private boolean canDestroyEgg(Level level, Entity entity) {
        if ((entity instanceof Turtle) || (entity instanceof Bat) || !(entity instanceof LivingEntity)) {
            return false;
        }
        return (entity instanceof Player) || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }
}
