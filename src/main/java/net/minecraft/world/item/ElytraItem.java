package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ElytraItem.class */
public class ElytraItem extends Item implements Wearable {
    public ElytraItem(Item.Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static boolean isFlyEnabled(ItemStack itemStack) {
        return itemStack.getDamageValue() < itemStack.getMaxDamage() - 1;
    }

    @Override // net.minecraft.world.item.Item
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.getItem() == Items.PHANTOM_MEMBRANE;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemInHand);
        if (player.getItemBySlot(equipmentSlotForItem).isEmpty()) {
            player.setItemSlot(equipmentSlotForItem, itemInHand.copy());
            itemInHand.setCount(0);
            return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
        }
        return InteractionResultHolder.fail(itemInHand);
    }
}
