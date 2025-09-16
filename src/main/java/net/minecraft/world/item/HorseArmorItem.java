package net.minecraft.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/HorseArmorItem.class */
public class HorseArmorItem extends Item {
    private final int protection;
    private final String texture;

    public HorseArmorItem(int i, String str, Item.Properties properties) {
        super(properties);
        this.protection = i;
        this.texture = "textures/entity/horse/armor/horse_armor_" + str + ".png";
    }

    public ResourceLocation getTexture() {
        return new ResourceLocation(this.texture);
    }

    public int getProtection() {
        return this.protection;
    }
}
