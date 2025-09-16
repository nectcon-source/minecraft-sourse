package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BlastFurnaceBlockEntity.class */
public class BlastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public BlastFurnaceBlockEntity() {
        super(BlockEntityType.BLAST_FURNACE, RecipeType.BLASTING);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.blast_furnace");
    }

    @Override // net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
    protected int getBurnDuration(ItemStack itemStack) {
        return super.getBurnDuration(itemStack) / 2;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new BlastFurnaceMenu(i, inventory, this, this.dataAccess);
    }
}
