package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/EnderEyeItem.class */
public class EnderEyeItem extends Item {
    public EnderEyeItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (!blockState.is(Blocks.END_PORTAL_FRAME) || ((Boolean) blockState.getValue(EndPortalFrameBlock.HAS_EYE)).booleanValue()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState blockState2 = (BlockState) blockState.setValue(EndPortalFrameBlock.HAS_EYE, true);
        Block.pushEntitiesUp(blockState, blockState2, level, clickedPos);
        level.setBlock(clickedPos, blockState2, 2);
        level.updateNeighbourForOutputSignal(clickedPos, Blocks.END_PORTAL_FRAME);
        useOnContext.getItemInHand().shrink(1);
        level.levelEvent(1503, clickedPos, 0);
        BlockPattern.BlockPatternMatch find = EndPortalFrameBlock.getOrCreatePortalShape().find(level, clickedPos);
        if (find != null) {
            BlockPos offset = find.getFrontTopLeft().offset(-3, 0, -3);
            for (int i = 0; i < 3; i++) {
                for (int i2 = 0; i2 < 3; i2++) {
                    level.setBlock(offset.offset(i, 0, i2), Blocks.END_PORTAL.defaultBlockState(), 2);
                }
            }
            level.globalLevelEvent(1038, offset.offset(1, 0, 1), 0);
        }
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        BlockPos findNearestMapFeature;
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        HitResult playerPOVHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (playerPOVHitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(((BlockHitResult) playerPOVHitResult).getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
            return InteractionResultHolder.pass(itemInHand);
        }
        player.startUsingItem(interactionHand);
        if ((level instanceof ServerLevel) && (findNearestMapFeature = ((ServerLevel) level).getChunkSource().getGenerator().findNearestMapFeature((ServerLevel) level, StructureFeature.STRONGHOLD, player.blockPosition(), 100, false)) != null) {
            EyeOfEnder eyeOfEnder = new EyeOfEnder(level, player.getX(), player.getY(0.5d), player.getZ());
            eyeOfEnder.setItem(itemInHand);
            eyeOfEnder.signalTo(findNearestMapFeature);
            level.addFreshEntity(eyeOfEnder);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer) player, findNearestMapFeature);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5f, 0.4f / ((random.nextFloat() * 0.4f) + 0.8f));
            level.levelEvent(null, 1003, player.blockPosition(), 0);
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            player.swing(interactionHand, true);
            return InteractionResultHolder.success(itemInHand);
        }
        return InteractionResultHolder.consume(itemInHand);
    }
}
