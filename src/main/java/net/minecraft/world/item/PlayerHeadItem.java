package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/PlayerHeadItem.class */
public class PlayerHeadItem extends StandingAndWallBlockItem {
    public PlayerHeadItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties);
    }

    @Override // net.minecraft.world.item.Item
    public Component getName(ItemStack itemStack) {
        if (itemStack.getItem() == Items.PLAYER_HEAD && itemStack.hasTag()) {
            String str = null;
            CompoundTag tag = itemStack.getTag();
            if (tag.contains("SkullOwner", 8)) {
                str = tag.getString("SkullOwner");
            } else if (tag.contains("SkullOwner", 10)) {
                CompoundTag compound = tag.getCompound("SkullOwner");
                if (compound.contains("Name", 8)) {
                    str = compound.getString("Name");
                }
            }
            if (str != null) {
                return new TranslatableComponent(getDescriptionId() + ".named", str);
            }
        }
        return super.getName(itemStack);
    }

    @Override // net.minecraft.world.item.Item
    public boolean verifyTagAfterLoad(CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
            compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), SkullBlockEntity.updateGameprofile(new GameProfile((UUID) null, compoundTag.getString("SkullOwner")))));
            return true;
        }
        return false;
    }
}
