package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/MenuConstructor.class */
public interface MenuConstructor {
    @Nullable
    AbstractContainerMenu createMenu(int i, Inventory inventory, Player player);
}
