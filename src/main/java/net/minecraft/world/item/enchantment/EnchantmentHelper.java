package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/EnchantmentHelper.class */
public class EnchantmentHelper {

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor.class */
    interface EnchantmentVisitor {
        void accept(Enchantment enchantment, int i);
    }

    public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        ResourceLocation key = Registry.ENCHANTMENT.getKey(enchantment);
        ListTag enchantmentTags = itemStack.getEnchantmentTags();
        for (int i = 0; i < enchantmentTags.size(); i++) {
            CompoundTag compound = enchantmentTags.getCompound(i);
            ResourceLocation tryParse = ResourceLocation.tryParse(compound.getString("id"));
            if (tryParse != null && tryParse.equals(key)) {
                return Mth.clamp(compound.getInt("lvl"), 0, 255);
            }
        }
        return 0;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        return deserializeEnchantments(itemStack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantments(itemStack) : itemStack.getEnchantmentTags());
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(ListTag listTag) {
        Map<Enchantment, Integer> newLinkedHashMap = Maps.newLinkedHashMap();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compound.getString("id"))).ifPresent(enchantment -> {
            });
        }
        return newLinkedHashMap;
    }

    public static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemStack) {
        ListTag listTag = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment key = entry.getKey();
            if (key != null) {
                int intValue = entry.getValue().intValue();
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(key)));
                compoundTag.putShort("lvl", (short) intValue);
                listTag.add(compoundTag);
                if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
                    EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(key, intValue));
                }
            }
        }
        if (listTag.isEmpty()) {
            itemStack.removeTagKey("Enchantments");
        } else if (itemStack.getItem() != Items.ENCHANTED_BOOK) {
            itemStack.addTagElement("Enchantments", listTag);
        }
    }

    private static void runIterationOnItem(EnchantmentVisitor enchantmentVisitor, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }
        ListTag enchantmentTags = itemStack.getEnchantmentTags();
        for (int i = 0; i < enchantmentTags.size(); i++) {
            String string = enchantmentTags.getCompound(i).getString("id");
            int i2 = enchantmentTags.getCompound(i).getInt("lvl");
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(string)).ifPresent(enchantment -> {
                enchantmentVisitor.accept(enchantment, i2);
            });
        }
    }

    private static void runIterationOnInventory(EnchantmentVisitor enchantmentVisitor, Iterable<ItemStack> iterable) {
        Iterator<ItemStack> it = iterable.iterator();
        while (it.hasNext()) {
            runIterationOnItem(enchantmentVisitor, it.next());
        }
    }

    public static int getDamageProtection(Iterable<ItemStack> iterable, DamageSource damageSource) {
        MutableInt mutableInt = new MutableInt();
        runIterationOnInventory((enchantment, i) -> {
            mutableInt.add(enchantment.getDamageProtection(i, damageSource));
        }, iterable);
        return mutableInt.intValue();
    }

    public static float getDamageBonus(ItemStack itemStack, MobType mobType) {
        MutableFloat mutableFloat = new MutableFloat();
        runIterationOnItem((enchantment, i) -> {
            mutableFloat.add(enchantment.getDamageBonus(i, mobType));
        }, itemStack);
        return mutableFloat.floatValue();
    }

    public static float getSweepingDamageRatio(LivingEntity livingEntity) {
        int enchantmentLevel = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, livingEntity);
        if (enchantmentLevel > 0) {
            return SweepingEdgeEnchantment.getSweepingDamageRatio(enchantmentLevel);
        }
        return 0.0f;
    }

    public static void doPostHurtEffects(LivingEntity livingEntity, Entity entity) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> {
            enchantment.doPostHurt(livingEntity, entity, i);
        };
        if (livingEntity != null) {
            runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }
        if (entity instanceof Player) {
            runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
        }
    }

    public static void doPostDamageEffects(LivingEntity livingEntity, Entity entity) {
        EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> {
            enchantment.doPostAttack(livingEntity, entity, i);
        };
        if (livingEntity != null) {
            runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
        }
        if (livingEntity instanceof Player) {
            runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
        }
    }

    public static int getEnchantmentLevel(Enchantment enchantment, LivingEntity livingEntity) {
        Iterable<ItemStack> values = enchantment.getSlotItems(livingEntity).values();
        if (values == null) {
            return 0;
        }
        int i = 0;
        Iterator<ItemStack> it = values.iterator();
        while (it.hasNext()) {
            int itemEnchantmentLevel = getItemEnchantmentLevel(enchantment, it.next());
            if (itemEnchantmentLevel > i) {
                i = itemEnchantmentLevel;
            }
        }
        return i;
    }

    public static int getKnockbackBonus(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.KNOCKBACK, livingEntity);
    }

    public static int getFireAspect(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.FIRE_ASPECT, livingEntity);
    }

    public static int getRespiration(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.RESPIRATION, livingEntity);
    }

    public static int getDepthStrider(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, livingEntity);
    }

    public static int getBlockEfficiency(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, livingEntity);
    }

    public static int getFishingLuckBonus(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, itemStack);
    }

    public static int getFishingSpeedBonus(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, itemStack);
    }

    public static int getMobLooting(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.MOB_LOOTING, livingEntity);
    }

    public static boolean hasAquaAffinity(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, livingEntity) > 0;
    }

    public static boolean hasFrostWalker(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.FROST_WALKER, livingEntity) > 0;
    }

    public static boolean hasSoulSpeed(LivingEntity livingEntity) {
        return getEnchantmentLevel(Enchantments.SOUL_SPEED, livingEntity) > 0;
    }

    public static boolean hasBindingCurse(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemStack) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemStack) > 0;
    }

    public static int getLoyalty(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.LOYALTY, itemStack);
    }

    public static int getRiptide(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.RIPTIDE, itemStack);
    }

    public static boolean hasChanneling(ItemStack itemStack) {
        return getItemEnchantmentLevel(Enchantments.CHANNELING, itemStack) > 0;
    }

    @Nullable
    public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity) {
        return getRandomItemWith(enchantment, livingEntity, itemStack -> {
            return true;
        });
    }

    @Nullable
    public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        Map<EquipmentSlot, ItemStack> slotItems = enchantment.getSlotItems(livingEntity);
        if (slotItems.isEmpty()) {
            return null;
        }
        List<Map.Entry<EquipmentSlot, ItemStack>> newArrayList = Lists.newArrayList();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : slotItems.entrySet()) {
            ItemStack value = entry.getValue();
            if (!value.isEmpty() && getItemEnchantmentLevel(enchantment, value) > 0 && predicate.test(value)) {
                newArrayList.add(entry);
            }
        }
        if (newArrayList.isEmpty()) {
            return null;
        }
        return newArrayList.get(livingEntity.getRandom().nextInt(newArrayList.size()));
    }

    public static int getEnchantmentCost(Random random, int i, int i2, ItemStack itemStack) {
        if (itemStack.getItem().getEnchantmentValue() <= 0) {
            return 0;
        }
        if (i2 > 15) {
            i2 = 15;
        }
        int nextInt = random.nextInt(8) + 1 + (i2 >> 1) + random.nextInt(i2 + 1);
        if (i == 0) {
            return Math.max(nextInt / 3, 1);
        }
        if (i == 1) {
            return ((nextInt * 2) / 3) + 1;
        }
        return Math.max(nextInt, i2 * 2);
    }

    public static ItemStack enchantItem(Random random, ItemStack itemStack, int i, boolean z) {
        List<EnchantmentInstance> selectEnchantment = selectEnchantment(random, itemStack, i, z);
        boolean z2 = itemStack.getItem() == Items.BOOK;
        if (z2) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentInstance enchantmentInstance : selectEnchantment) {
            if (z2) {
                EnchantedBookItem.addEnchantment(itemStack, enchantmentInstance);
            } else {
                itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
            }
        }
        return itemStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(Random random, ItemStack itemStack, int i, boolean z) {
        ArrayList newArrayList = Lists.newArrayList();
        int enchantmentValue = itemStack.getItem().getEnchantmentValue();
        if (enchantmentValue <= 0) {
            return newArrayList;
        }
        int nextInt = i + 1 + random.nextInt((enchantmentValue / 4) + 1) + random.nextInt((enchantmentValue / 4) + 1);
        int clamp = Mth.clamp(Math.round(nextInt + (nextInt * ((random.nextFloat() + random.nextFloat()) - 1.0f) * 0.15f)), 1, Integer.MAX_VALUE);
        List<EnchantmentInstance> availableEnchantmentResults = getAvailableEnchantmentResults(clamp, itemStack, z);
        if (!availableEnchantmentResults.isEmpty()) {
            newArrayList.add(WeighedRandom.getRandomItem(random, availableEnchantmentResults));
            while (random.nextInt(50) <= clamp) {
                filterCompatibleEnchantments(availableEnchantmentResults, (EnchantmentInstance) Util.lastOf(newArrayList));
                if (availableEnchantmentResults.isEmpty()) {
                    break;
                }
                newArrayList.add(WeighedRandom.getRandomItem(random, availableEnchantmentResults));
                clamp /= 2;
            }
        }
        return newArrayList;
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentInstance) {
        Iterator<EnchantmentInstance> it = list.iterator();
        while (it.hasNext()) {
            if (!enchantmentInstance.enchantment.isCompatibleWith(it.next().enchantment)) {
                it.remove();
            }
        }
    }

    public static boolean isEnchantmentCompatible(Collection<Enchantment> collection, Enchantment enchantment) {
        Iterator<Enchantment> it = collection.iterator();
        while (it.hasNext()) {
            if (!it.next().isCompatibleWith(enchantment)) {
                return false;
            }
        }
        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemStack, boolean z) {
        List<EnchantmentInstance> newArrayList = Lists.newArrayList();
        Item item = itemStack.getItem();
        boolean z2 = itemStack.getItem() == Items.BOOK;
        Iterator it = Registry.ENCHANTMENT.iterator();
        while (it.hasNext()) {
            Enchantment enchantment = (Enchantment) it.next();
            if (!enchantment.isTreasureOnly() || z) {
                if (enchantment.isDiscoverable() && (enchantment.category.canEnchant(item) || z2)) {
                    int maxLevel = enchantment.getMaxLevel();
                    while (true) {
                        if (maxLevel > enchantment.getMinLevel() - 1) {
                            if (i < enchantment.getMinCost(maxLevel) || i > enchantment.getMaxCost(maxLevel)) {
                                maxLevel--;
                            } else {
                                newArrayList.add(new EnchantmentInstance(enchantment, maxLevel));
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return newArrayList;
    }
}
