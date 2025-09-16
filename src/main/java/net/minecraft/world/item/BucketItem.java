package net.minecraft.world.item;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BucketItem.class */
public class BucketItem extends Item {
    private final Fluid content;

    public BucketItem(Fluid fluid, Item.Properties properties) {
        super(properties);
        this.content = fluid;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        Fluid takeLiquid;
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        HitResult playerPOVHitResult = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (playerPOVHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemInHand);
        }
        if (playerPOVHitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) playerPOVHitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos relative = blockPos.relative(direction);
            if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(relative, direction, itemInHand)) {
                return InteractionResultHolder.fail(itemInHand);
            }
            if (this.content == Fluids.EMPTY) {
                BlockState blockState = level.getBlockState(blockPos);
                if ((blockState.getBlock() instanceof BucketPickup) && (takeLiquid = ((BucketPickup) blockState.getBlock()).takeLiquid(level, blockPos, blockState)) != Fluids.EMPTY) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    player.playSound(takeLiquid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL, 1.0f, 1.0f);
                    ItemStack createFilledResult = ItemUtils.createFilledResult(itemInHand, player, new ItemStack(takeLiquid.getBucket()));
                    if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, new ItemStack(takeLiquid.getBucket()));
                    }
                    return InteractionResultHolder.sidedSuccess(createFilledResult, level.isClientSide());
                }
                return InteractionResultHolder.fail(itemInHand);
            }
            BlockPos blockPos2 = ((level.getBlockState(blockPos).getBlock() instanceof LiquidBlockContainer) && this.content == Fluids.WATER) ? blockPos : relative;
            if (emptyBucket(player, level, blockPos2, blockHitResult)) {
                checkExtraContent(level, itemInHand, blockPos2);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockPos2, itemInHand);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemInHand, player), level.isClientSide());
            }
            return InteractionResultHolder.fail(itemInHand);
        }
        return InteractionResultHolder.pass(itemInHand);
    }

    protected ItemStack getEmptySuccessItem(ItemStack itemStack, Player player) {
        if (!player.abilities.instabuild) {
            return new ItemStack(Items.BUCKET);
        }
        return itemStack;
    }

    public void checkExtraContent(Level level, ItemStack itemStack, BlockPos blockPos) {
    }

    public boolean emptyBucket(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        if (!(this.content instanceof FlowingFluid)) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        ItemLike block = blockState.getBlock();
        Material material = blockState.getMaterial();
        boolean canBeReplaced = blockState.canBeReplaced(this.content);
        if (!(blockState.isAir() || canBeReplaced || ((block instanceof LiquidBlockContainer) && ((LiquidBlockContainer) block).canPlaceLiquid(level, blockPos, blockState, this.content)))) {
            return blockHitResult != null && emptyBucket(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
        }
        if (level.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
            level.playSound(player, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + ((level.random.nextFloat() - level.random.nextFloat()) * 0.8f));
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0d, 0.0d, 0.0d);
            }
            return true;
        }
        if ((block instanceof LiquidBlockContainer) && this.content == Fluids.WATER) {
            ((LiquidBlockContainer) block).placeLiquid(level, blockPos, blockState, ((FlowingFluid) this.content).getSource(false));
            playEmptySound(player, level, blockPos);
            return true;
        }
        if (!level.isClientSide && canBeReplaced && !material.isLiquid()) {
            level.destroyBlock(blockPos, true);
        }
        if (level.setBlock(blockPos, this.content.defaultFluidState().createLegacyBlock(), 11) || blockState.getFluidState().isSource()) {
            playEmptySound(player, level, blockPos);
            return true;
        }
        return false;
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(player, blockPos, this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
