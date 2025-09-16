package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WoodButtonBlock.class */
public class WoodButtonBlock extends ButtonBlock {
    protected WoodButtonBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
    }

    @Override // net.minecraft.world.level.block.ButtonBlock
    protected SoundEvent getSound(boolean z) {
        return z ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    }
}
