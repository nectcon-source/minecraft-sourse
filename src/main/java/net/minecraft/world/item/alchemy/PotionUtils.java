package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/alchemy/PotionUtils.class */
public class PotionUtils {
    private static final MutableComponent NO_EFFECT = new TranslatableComponent("effect.none").withStyle(ChatFormatting.GRAY);

    public static List<MobEffectInstance> getMobEffects(ItemStack itemStack) {
        return getAllEffects(itemStack.getTag());
    }

    public static List<MobEffectInstance> getAllEffects(Potion potion, Collection<MobEffectInstance> collection) {
        List<MobEffectInstance> newArrayList = Lists.newArrayList();
        newArrayList.addAll(potion.getEffects());
        newArrayList.addAll(collection);
        return newArrayList;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compoundTag) {
        List<MobEffectInstance> newArrayList = Lists.newArrayList();
        newArrayList.addAll(getPotion(compoundTag).getEffects());
        getCustomEffects(compoundTag, newArrayList);
        return newArrayList;
    }

    public static List<MobEffectInstance> getCustomEffects(ItemStack itemStack) {
        return getCustomEffects(itemStack.getTag());
    }

    public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag compoundTag) {
        List<MobEffectInstance> newArrayList = Lists.newArrayList();
        getCustomEffects(compoundTag, newArrayList);
        return newArrayList;
    }

    public static void getCustomEffects(@Nullable CompoundTag compoundTag, List<MobEffectInstance> list) {
        if (compoundTag != null && compoundTag.contains("CustomPotionEffects", 9)) {
            ListTag list2 = compoundTag.getList("CustomPotionEffects", 10);
            for (int i = 0; i < list2.size(); i++) {
                MobEffectInstance load = MobEffectInstance.load(list2.getCompound(i));
                if (load != null) {
                    list.add(load);
                }
            }
        }
    }

    public static int getColor(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", 99)) {
            return tag.getInt("CustomPotionColor");
        }
        if (getPotion(itemStack) == Potions.EMPTY) {
            return 16253176;
        }
        return getColor(getMobEffects(itemStack));
    }

    public static int getColor(Potion potion) {
        if (potion == Potions.EMPTY) {
            return 16253176;
        }
        return getColor(potion.getEffects());
    }

    public static int getColor(Collection<MobEffectInstance> collection) {
        if (collection.isEmpty()) {
            return 3694022;
        }
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        int i = 0;
        for (MobEffectInstance mobEffectInstance : collection) {
            if (mobEffectInstance.isVisible()) {
                int i_ = mobEffectInstance.getAmplifier() + 1;
                int color = mobEffectInstance.getEffect().getColor();
                f += (i_ * ((color >> 16) & 255)) / 255.0f;
                f2 += (i_ * ((color >> 8) & 255)) / 255.0f;
                f3 += (i_ * ((color >> 0) & 255)) / 255.0f;
                i += i_;
            }
        }
        if (i == 0) {
            return 0;
        }
        return (((int) ((f / i) * 255.0f)) << 16) | (((int) ((f2 / i) * 255.0f)) << 8) | ((int) ((f3 / i) * 255.0f));
    }

    public static Potion getPotion(ItemStack itemStack) {
        return getPotion(itemStack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag compoundTag) {
        if (compoundTag == null) {
            return Potions.EMPTY;
        }
        return Potion.byName(compoundTag.getString("Potion"));
    }

    public static ItemStack setPotion(ItemStack itemStack, Potion potion) {
        ResourceLocation key = Registry.POTION.getKey(potion);
        if (potion == Potions.EMPTY) {
            itemStack.removeTagKey("Potion");
        } else {
            itemStack.getOrCreateTag().putString("Potion", key.toString());
        }
        return itemStack;
    }

    public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> collection) {
        if (collection.isEmpty()) {
            return itemStack;
        }
        CompoundTag orCreateTag = itemStack.getOrCreateTag();
        ListTag list = orCreateTag.getList("CustomPotionEffects", 9);
        Iterator<MobEffectInstance> it = collection.iterator();
        while (it.hasNext()) {
            list.add(it.next().save(new CompoundTag()));
        }
        orCreateTag.put("CustomPotionEffects", list);
        return itemStack;
    }

    public static void addPotionTooltip(ItemStack itemStack, List<Component> list, float f) {
        double amount;
        List<MobEffectInstance> mobEffects = getMobEffects(itemStack);
        List<Pair<Attribute, AttributeModifier>> newArrayList = Lists.newArrayList();
        if (mobEffects.isEmpty()) {
            list.add(NO_EFFECT);
        } else {
            for (MobEffectInstance mobEffectInstance : mobEffects) {
                MutableComponent translatableComponent = new TranslatableComponent(mobEffectInstance.getDescriptionId());
                MobEffect effect = mobEffectInstance.getEffect();
                Map<Attribute, AttributeModifier> attributeModifiers = effect.getAttributeModifiers();
                if (!attributeModifiers.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entrySet()) {
                        AttributeModifier value = entry.getValue();
                        newArrayList.add(new Pair<>(entry.getKey(), new AttributeModifier(value.getName(), effect.getAttributeModifierValue(mobEffectInstance.getAmplifier(), value), value.getOperation())));
                    }
                }
                if (mobEffectInstance.getAmplifier() > 0) {
                    translatableComponent = new TranslatableComponent("potion.withAmplifier", translatableComponent, new TranslatableComponent("potion.potency." + mobEffectInstance.getAmplifier()));
                }
                if (mobEffectInstance.getDuration() > 20) {
                    translatableComponent = new TranslatableComponent("potion.withDuration", translatableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f));
                }
                list.add(translatableComponent.withStyle(effect.getCategory().getTooltipFormatting()));
            }
        }
        if (!newArrayList.isEmpty()) {
            list.add(TextComponent.EMPTY);
            list.add(new TranslatableComponent("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair<Attribute, AttributeModifier> pair : newArrayList) {
                AttributeModifier attributeModifier = (AttributeModifier) pair.getSecond();
                double amount2 = attributeModifier.getAmount();
                if (attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    amount = attributeModifier.getAmount() * 100.0d;
                } else {
                    amount = attributeModifier.getAmount();
                }
                if (amount2 > 0.0d) {
                    list.add(new TranslatableComponent("attribute.modifier.plus." + attributeModifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount), new TranslatableComponent(((Attribute) pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (amount2 < 0.0d) {
                    list.add(new TranslatableComponent("attribute.modifier.take." + attributeModifier.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount * (-1.0d)), new TranslatableComponent(((Attribute) pair.getFirst()).getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
