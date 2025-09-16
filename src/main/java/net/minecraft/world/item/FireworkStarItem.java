package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FireworkStarItem.class */
public class FireworkStarItem extends Item {
    public FireworkStarItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tagElement = itemStack.getTagElement("Explosion");
        if (tagElement != null) {
            appendHoverText(tagElement, list);
        }
    }

    public static void appendHoverText(CompoundTag compoundTag, List<Component> list) {
        list.add(new TranslatableComponent("item.minecraft.firework_star.shape." + FireworkRocketItem.Shape.byId(compoundTag.getByte("Type")).getName()).withStyle(ChatFormatting.GRAY));
        int[] intArray = compoundTag.getIntArray("Colors");
        if (intArray.length > 0) {
            list.add(appendColors(new TextComponent("").withStyle(ChatFormatting.GRAY), intArray));
        }
        int[] intArray2 = compoundTag.getIntArray("FadeColors");
        if (intArray2.length > 0) {
            list.add(appendColors(new TranslatableComponent("item.minecraft.firework_star.fade_to").append(" ").withStyle(ChatFormatting.GRAY), intArray2));
        }
        if (compoundTag.getBoolean("Trail")) {
            list.add(new TranslatableComponent("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
        }
        if (compoundTag.getBoolean("Flicker")) {
            list.add(new TranslatableComponent("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component appendColors(MutableComponent mutableComponent, int[] iArr) {
        for (int i = 0; i < iArr.length; i++) {
            if (i > 0) {
                mutableComponent.append(", ");
            }
            mutableComponent.append(getColorName(iArr[i]));
        }
        return mutableComponent;
    }

    private static Component getColorName(int i) {
        DyeColor byFireworkColor = DyeColor.byFireworkColor(i);
        if (byFireworkColor == null) {
            return new TranslatableComponent("item.minecraft.firework_star.custom_color");
        }
        return new TranslatableComponent("item.minecraft.firework_star." + byFireworkColor.getName());
    }
}
