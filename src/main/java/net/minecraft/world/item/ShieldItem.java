package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ShieldItem.class */
public class ShieldItem extends Item {
    public ShieldItem(Item.Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override // net.minecraft.world.item.Item
    public String getDescriptionId(ItemStack itemStack) {
        if (itemStack.getTagElement("BlockEntityTag") != null) {
            return getDescriptionId() + '.' + getColor(itemStack).getName();
        }
        return super.getDescriptionId(itemStack);
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
    }

    @Override // net.minecraft.world.item.Item
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BLOCK;
    }

    @Override // net.minecraft.world.item.Item
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemInHand);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return ItemTags.PLANKS.contains(itemStack2.getItem()) || super.isValidRepairItem(itemStack, itemStack2);
    }

    public static DyeColor getColor(ItemStack itemStack) {
        return DyeColor.byId(itemStack.getOrCreateTagElement("BlockEntityTag").getInt("Base"));
    }
}
