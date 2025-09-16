package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/SuspiciousStewItem.class */
public class SuspiciousStewItem extends Item {
    public SuspiciousStewItem(Item.Properties properties) {
        super(properties);
    }

    public static void saveMobEffect(ItemStack itemStack, MobEffect mobEffect, int i) {
        CompoundTag orCreateTag = itemStack.getOrCreateTag();
        ListTag list = orCreateTag.getList("Effects", 9);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putByte("EffectId", (byte) MobEffect.getId(mobEffect));
        compoundTag.putInt("EffectDuration", i);
        list.add(compoundTag);
        orCreateTag.put("Effects", list);
    }

    @Override // net.minecraft.world.item.Item
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack finishUsingItem = super.finishUsingItem(itemStack, level, livingEntity);
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("Effects", 9)) {
            ListTag list = tag.getList("Effects", 10);
            for (int i = 0; i < list.size(); i++) {
                int i2 = 160;
                CompoundTag compound = list.getCompound(i);
                if (compound.contains("EffectDuration", 3)) {
                    i2 = compound.getInt("EffectDuration");
                }
                MobEffect byId = MobEffect.byId(compound.getByte("EffectId"));
                if (byId != null) {
                    livingEntity.addEffect(new MobEffectInstance(byId, i2));
                }
            }
        }
        if ((livingEntity instanceof Player) && ((Player) livingEntity).abilities.instabuild) {
            return finishUsingItem;
        }
        return new ItemStack(Items.BOWL);
    }
}
