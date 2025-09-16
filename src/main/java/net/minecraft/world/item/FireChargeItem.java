package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FireChargeItem.class */
public class FireChargeItem extends Item {
    public FireChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        boolean z = false;
        if (CampfireBlock.canLight(blockState)) {
            playSound(level, clickedPos);
            level.setBlockAndUpdate(clickedPos, (BlockState) blockState.setValue(CampfireBlock.LIT, true));
            z = true;
        } else {
            BlockPos relative = clickedPos.relative(useOnContext.getClickedFace());
            if (BaseFireBlock.canBePlacedAt(level, relative, useOnContext.getHorizontalDirection())) {
                playSound(level, relative);
                level.setBlockAndUpdate(relative, BaseFireBlock.getState(level, relative));
                z = true;
            }
        }
        if (z) {
            useOnContext.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    private void playSound(Level level, BlockPos blockPos) {
        level.playSound((Player) null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, ((random.nextFloat() - random.nextFloat()) * 0.2f) + 1.0f);
    }
}
