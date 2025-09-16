package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BannerItem.class */
public class BannerItem extends StandingAndWallBlockItem {
    public BannerItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties);
        Validate.isInstanceOf(AbstractBannerBlock.class, block);
        Validate.isInstanceOf(AbstractBannerBlock.class, block2);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack itemStack, List<Component> list) {
        CompoundTag tagElement = itemStack.getTagElement("BlockEntityTag");
        if (tagElement == null || !tagElement.contains("Patterns")) {
            return;
        }
        ListTag list2 = tagElement.getList("Patterns", 10);
        for (int i = 0; i < list2.size() && i < 6; i++) {
            CompoundTag compound = list2.getCompound(i);
            DyeColor byId = DyeColor.byId(compound.getInt("Color"));
            BannerPattern byHash = BannerPattern.byHash(compound.getString("Pattern"));
            if (byHash != null) {
                list.add(new TranslatableComponent("block.minecraft.banner." + byHash.getFilename() + '.' + byId.getName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock) getBlock()).getColor();
    }

    @Override // net.minecraft.world.item.BlockItem, net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        appendHoverTextFromBannerBlockEntityTag(itemStack, list);
    }
}
