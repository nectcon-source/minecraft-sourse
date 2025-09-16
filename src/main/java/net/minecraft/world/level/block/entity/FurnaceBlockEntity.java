package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/FurnaceBlockEntity.class */
public class FurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public FurnaceBlockEntity() {
        super(BlockEntityType.FURNACE, RecipeType.SMELTING);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.furnace");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new FurnaceMenu(i, inventory, this, this.dataAccess);
    }
}
