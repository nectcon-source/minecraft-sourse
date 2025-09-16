package net.minecraft.world.effect;

import net.minecraft.ChatFormatting;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/MobEffectCategory.class */
public enum MobEffectCategory {
    BENEFICIAL(ChatFormatting.BLUE),
    HARMFUL(ChatFormatting.RED),
    NEUTRAL(ChatFormatting.BLUE);

    private final ChatFormatting tooltipFormatting;

    MobEffectCategory(ChatFormatting chatFormatting) {
        this.tooltipFormatting = chatFormatting;
    }

    public ChatFormatting getTooltipFormatting() {
        return this.tooltipFormatting;
    }
}
