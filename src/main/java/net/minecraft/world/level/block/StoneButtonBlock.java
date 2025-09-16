package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StoneButtonBlock.class */
public class StoneButtonBlock extends ButtonBlock {
    protected StoneButtonBlock(BlockBehaviour.Properties properties) {
        super(false, properties);
    }

    @Override // net.minecraft.world.level.block.ButtonBlock
    protected SoundEvent getSound(boolean z) {
        return z ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
    }
}
