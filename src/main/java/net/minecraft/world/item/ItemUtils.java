package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemUtils.class */
public class ItemUtils {
    public static InteractionResultHolder<ItemStack> useDrink(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
    }

    public static ItemStack createFilledResult(ItemStack itemStack, Player player, ItemStack itemStack2, boolean z) {
        boolean z2 = player.abilities.instabuild;
        if (z && z2) {
            if (!player.inventory.contains(itemStack2)) {
                player.inventory.add(itemStack2);
            }
            return itemStack;
        }
        if (!z2) {
            itemStack.shrink(1);
        }
        if (itemStack.isEmpty()) {
            return itemStack2;
        }
        if (!player.inventory.add(itemStack2)) {
            player.drop(itemStack2, false);
        }
        return itemStack;
    }

    public static ItemStack createFilledResult(ItemStack itemStack, Player player, ItemStack itemStack2) {
        return createFilledResult(itemStack, player, itemStack2, true);
    }
}
