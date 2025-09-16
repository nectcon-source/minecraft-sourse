package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/DyeableLeatherItem.class */
public interface DyeableLeatherItem {
    default boolean hasCustomColor(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("display");
        return tagElement != null && tagElement.contains("color", 99);
    }

    default int getColor(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("display");
        if (tagElement != null && tagElement.contains("color", 99)) {
            return tagElement.getInt("color");
        }
        return 10511680;
    }

    default void clearColor(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("display");
        if (tagElement != null && tagElement.contains("color")) {
            tagElement.remove("color");
        }
    }

    default void setColor(ItemStack itemStack, int i) {
        itemStack.getOrCreateTagElement("display").putInt("color", i);
    }

    static ItemStack dyeArmor(ItemStack itemStack, List<DyeItem> list) {
        ItemStack itemStack2 = ItemStack.EMPTY;
        int[] iArr = new int[3];
        int i = 0;
        int i2 = 0;
        DyeableLeatherItem dyeableLeatherItem = null;
        ItemLike item = itemStack.getItem();
        if (item instanceof DyeableLeatherItem) {
            dyeableLeatherItem = (DyeableLeatherItem) item;
            itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            if (dyeableLeatherItem.hasCustomColor(itemStack)) {
                int color = dyeableLeatherItem.getColor(itemStack2);
                float f = ((color >> 16) & 255) / 255.0f;
                float f2 = ((color >> 8) & 255) / 255.0f;
                float f3 = (color & 255) / 255.0f;
                i = (int) (0 + (Math.max(f, Math.max(f2, f3)) * 255.0f));
                iArr[0] = (int) (iArr[0] + (f * 255.0f));
                iArr[1] = (int) (iArr[1] + (f2 * 255.0f));
                iArr[2] = (int) (iArr[2] + (f3 * 255.0f));
                i2 = 0 + 1;
            }
            Iterator<DyeItem> it = list.iterator();
            while (it.hasNext()) {
                float[] textureDiffuseColors = it.next().getDyeColor().getTextureDiffuseColors();
                int i3 = (int) (textureDiffuseColors[0] * 255.0f);
                int i4 = (int) (textureDiffuseColors[1] * 255.0f);
                int i5 = (int) (textureDiffuseColors[2] * 255.0f);
                i += Math.max(i3, Math.max(i4, i5));
                iArr[0] = iArr[0] + i3;
                iArr[1] = iArr[1] + i4;
                iArr[2] = iArr[2] + i5;
                i2++;
            }
        }
        if (dyeableLeatherItem == null) {
            return ItemStack.EMPTY;
        }
        int i6 = iArr[0] / i2;
        int i7 = iArr[1] / i2;
        int i8 = iArr[2] / i2;
        float f4 = i / i2;
        float max = Math.max(i6, Math.max(i7, i8));
        int i9 = (int) ((i6 * f4) / max);
        dyeableLeatherItem.setColor(itemStack2, (((i9 << 8) + ((int) ((i7 * f4) / max))) << 8) + ((int) ((i8 * f4) / max)));
        return itemStack2;
    }
}
