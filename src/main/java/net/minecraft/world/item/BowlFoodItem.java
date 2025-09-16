package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BowlFoodItem.class */
public class BowlFoodItem extends Item {
    public BowlFoodItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack finishUsingItem = super.finishUsingItem(itemStack, level, livingEntity);
        if ((livingEntity instanceof Player) && ((Player) livingEntity).abilities.instabuild) {
            return finishUsingItem;
        }
        return new ItemStack(Items.BOWL);
    }
}
