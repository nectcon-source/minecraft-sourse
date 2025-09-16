package net.minecraft.world.item;

import net.minecraft.ChatFormatting;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/Rarity.class */
public enum Rarity {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.YELLOW),
    RARE(ChatFormatting.AQUA),
    EPIC(ChatFormatting.LIGHT_PURPLE);

    public final ChatFormatting color;

    Rarity(ChatFormatting chatFormatting) {
        this.color = chatFormatting;
    }
}
