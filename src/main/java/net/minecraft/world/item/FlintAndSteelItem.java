package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FlintAndSteelItem.class */
public class FlintAndSteelItem extends Item {
    public FlintAndSteelItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (CampfireBlock.canLight(blockState)) {
            level.playSound(player, clickedPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, (random.nextFloat() * 0.4f) + 0.8f);
            level.setBlock(clickedPos, (BlockState) blockState.setValue(BlockStateProperties.LIT, true), 11);
            if (player != null) {
                useOnContext.getItemInHand().hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(useOnContext.getHand());
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        BlockPos relative = clickedPos.relative(useOnContext.getClickedFace());
        if (BaseFireBlock.canBePlacedAt(level, relative, useOnContext.getHorizontalDirection())) {
            level.playSound(player, relative, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, (random.nextFloat() * 0.4f) + 0.8f);
            level.setBlock(relative, BaseFireBlock.getState(level, relative), 11);
            ItemStack itemInHand = useOnContext.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, relative, itemInHand);
                itemInHand.hurtAndBreak(1, player, player3 -> {
                    player3.broadcastBreakEvent(useOnContext.getHand());
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.FAIL;
    }
}
