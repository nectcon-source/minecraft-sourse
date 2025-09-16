package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FoodOnAStickItem.class */
public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item {
    private final EntityType<T> canInteractWith;
    private final int consumeItemDamage;

    public FoodOnAStickItem(Item.Properties properties, EntityType<T> entityType, int i) {
        super(properties);
        this.canInteractWith = entityType;
        this.consumeItemDamage = i;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(itemInHand);
        }
        Entity vehicle = player.getVehicle();
        if (player.isPassenger() && (vehicle instanceof ItemSteerable) && vehicle.getType() == this.canInteractWith && ((ItemSteerable) vehicle).boost()) {
            itemInHand.hurtAndBreak(this.consumeItemDamage, player, player2 -> {
                player2.broadcastBreakEvent(interactionHand);
            });
            if (itemInHand.isEmpty()) {
                ItemStack itemStack = new ItemStack(Items.FISHING_ROD);
                itemStack.setTag(itemInHand.getTag());
                return InteractionResultHolder.success(itemStack);
            }
            return InteractionResultHolder.success(itemInHand);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.pass(itemInHand);
    }
}
