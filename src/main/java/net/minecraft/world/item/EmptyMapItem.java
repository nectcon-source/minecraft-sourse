package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/EmptyMapItem.class */
public class EmptyMapItem extends ComplexItem {
    public EmptyMapItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack create = MapItem.create(level, Mth.floor(player.getX()), Mth.floor(player.getZ()), (byte) 0, true, false);
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!player.abilities.instabuild) {
            itemInHand.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0f, 1.0f);
        if (itemInHand.isEmpty()) {
            return InteractionResultHolder.sidedSuccess(create, level.isClientSide());
        }
        if (!player.inventory.add(create.copy())) {
            player.drop(create, false);
        }
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }
}
