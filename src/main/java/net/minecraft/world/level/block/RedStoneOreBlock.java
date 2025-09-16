package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RedStoneOreBlock.class */
public class RedStoneOreBlock extends Block {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RedStoneOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) defaultBlockState().setValue(LIT, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        interact(blockState, level, blockPos);
        super.attack(blockState, level, blockPos, player);
    }

    @Override // net.minecraft.world.level.block.Block
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        interact(level.getBlockState(blockPos), level, blockPos);
        super.stepOn(level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            spawnParticles(level, blockPos);
        } else {
            interact(blockState, level, blockPos);
        }
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if ((itemInHand.getItem() instanceof BlockItem) && new BlockPlaceContext(player, interactionHand, itemInHand, blockHitResult).canPlace()) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private static void interact(BlockState blockState, Level level, BlockPos blockPos) {
        spawnParticles(level, blockPos);
        if (!((Boolean) blockState.getValue(LIT)).booleanValue()) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(LIT, true), 3);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Boolean) blockState.getValue(LIT)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (((Boolean) blockState.getValue(LIT)).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(LIT, false), 3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
            popExperience(serverLevel, blockPos, 1 + serverLevel.random.nextInt(5));
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (((Boolean) blockState.getValue(LIT)).booleanValue()) {
            spawnParticles(level, blockPos);
        }
    }

    private static void spawnParticles(Level level, BlockPos blockPos) {
        Random random = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos relative = blockPos.relative(direction);
            if (!level.getBlockState(relative).isSolidRender(level, relative)) {
                Direction.Axis axis = direction.getAxis();
                level.addParticle(DustParticleOptions.REDSTONE, blockPos.getX() + (axis == Direction.Axis.X ? 0.5d + (0.5625d * direction.getStepX()) : random.nextFloat()), blockPos.getY() + (axis == Direction.Axis.Y ? 0.5d + (0.5625d * direction.getStepY()) : random.nextFloat()), blockPos.getZ() + (axis == Direction.Axis.Z ? 0.5d + (0.5625d * direction.getStepZ()) : random.nextFloat()), 0.0d, 0.0d, 0.0d);
            }
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
