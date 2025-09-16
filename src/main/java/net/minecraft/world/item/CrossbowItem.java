package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/CrossbowItem.class */
public class CrossbowItem extends ProjectileWeaponItem implements Vanishable {
    private boolean startSoundPlayed;
    private boolean midLoadSoundPlayed;

    public CrossbowItem(Item.Properties properties) {
        super(properties);
        this.startSoundPlayed = false;
        this.midLoadSoundPlayed = false;
    }

    @Override // net.minecraft.world.item.ProjectileWeaponItem
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override // net.minecraft.world.item.ProjectileWeaponItem
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (isCharged(itemInHand)) {
            performShooting(level, player, interactionHand, itemInHand, getShootingPower(itemInHand), 1.0f);
            setCharged(itemInHand, false);
            return InteractionResultHolder.consume(itemInHand);
        }
        if (!player.getProjectile(itemInHand).isEmpty()) {
            if (!isCharged(itemInHand)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                player.startUsingItem(interactionHand);
            }
            return InteractionResultHolder.consume(itemInHand);
        }
        return InteractionResultHolder.fail(itemInHand);
    }

    @Override // net.minecraft.world.item.Item
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (getPowerForTime(getUseDuration(itemStack) - i, itemStack) >= 1.0f && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
            setCharged(itemStack, true);
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_LOADING_END, livingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1.0f, (1.0f / ((random.nextFloat() * 0.5f) + 1.0f)) + 0.2f);
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemStack) == 0 ? 1 : 3;
        boolean z = (livingEntity instanceof Player) && ((Player) livingEntity).abilities.instabuild;
        ItemStack projectile = livingEntity.getProjectile(itemStack);
        ItemStack copy = projectile.copy();
        int i2 = 0;
        while (i2 < i) {
            if (i2 > 0) {
                projectile = copy.copy();
            }
            if (projectile.isEmpty() && z) {
                projectile = new ItemStack(Items.ARROW);
                copy = projectile.copy();
            }
            if (loadProjectile(livingEntity, itemStack, projectile, i2 > 0, z)) {
                i2++;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean loadProjectile(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean z, boolean z2) {
        ItemStack copy;
        if (itemStack2.isEmpty()) {
            return false;
        }
        if (!(z2 && (itemStack2.getItem() instanceof ArrowItem)) && !z2 && !z) {
            copy = itemStack2.split(1);
            if (itemStack2.isEmpty() && (livingEntity instanceof Player)) {
                ((Player) livingEntity).inventory.removeItem(itemStack2);
            }
        } else {
            copy = itemStack2.copy();
        }
        addChargedProjectile(itemStack, copy);
        return true;
    }

    public static boolean isCharged(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        return tag != null && tag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack itemStack, boolean z) {
        itemStack.getOrCreateTag().putBoolean("Charged", z);
    }

    private static void addChargedProjectile(ItemStack itemStack, ItemStack itemStack2) {
        ListTag listTag;
        CompoundTag orCreateTag = itemStack.getOrCreateTag();
        if (orCreateTag.contains("ChargedProjectiles", 9)) {
            listTag = orCreateTag.getList("ChargedProjectiles", 10);
        } else {
            listTag = new ListTag();
        }
        CompoundTag compoundTag = new CompoundTag();
        itemStack2.save(compoundTag);
        listTag.add(compoundTag);
        orCreateTag.put("ChargedProjectiles", listTag);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack itemStack) {
        ListTag list;
        List<ItemStack> newArrayList = Lists.newArrayList();
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("ChargedProjectiles", 9) && (list = tag.getList("ChargedProjectiles", 10)) != null) {
            for (int i = 0; i < list.size(); i++) {
                newArrayList.add(ItemStack.of(list.getCompound(i)));
            }
        }
        return newArrayList;
    }

    private static void clearChargedProjectiles(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            ListTag list = tag.getList("ChargedProjectiles", 9);
            list.clear();
            tag.put("ChargedProjectiles", list);
        }
    }

    public static boolean containsChargedProjectile(ItemStack itemStack, Item item) {
        return getChargedProjectiles(itemStack).stream().anyMatch(itemStack2 -> {
            return itemStack2.getItem() == item;
        });
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static void shootProjectile(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, ItemStack itemStack2, float f, boolean z, float f2, float f3, float f4) {
        Projectile arrow;
        if (level.isClientSide) {
            return;
        }
        boolean z2 = itemStack2.getItem() == Items.FIREWORK_ROCKET;
        if (z2) {
            arrow = new FireworkRocketEntity(level, itemStack2, livingEntity, livingEntity.getX(), livingEntity.getEyeY() - 0.15000000596046448d, livingEntity.getZ(), true);
        } else {
            arrow = getArrow(level, livingEntity, itemStack, itemStack2);
            if (z || f4 != 0.0f) {
                ((AbstractArrow) arrow).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
        }
        if (livingEntity instanceof CrossbowAttackMob) {
            CrossbowAttackMob crossbowAttackMob = (CrossbowAttackMob) livingEntity;
            crossbowAttackMob.shootCrossbowProjectile(crossbowAttackMob.getTarget(), itemStack, arrow, f4);
        } else {
            Quaternion quaternion = new Quaternion(new Vector3f(livingEntity.getUpVector(1.0f)), f4, true);
            Vec3 xx = livingEntity.getViewVector(1.0F);
            Vector3f r0 = new  Vector3f(xx);
            r0.transform(quaternion);
            arrow.shoot(r0.x(), r0.y(), r0.z(), f2, f3);
        }
        itemStack.hurtAndBreak(z2 ? 3 : 1, livingEntity, livingEntity2 -> {
            livingEntity2.broadcastBreakEvent(interactionHand);
        });
        level.addFreshEntity(arrow);
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, f);
    }

    private static AbstractArrow getArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2) {
        AbstractArrow createArrow = ((ArrowItem) (itemStack2.getItem() instanceof ArrowItem ? itemStack2.getItem() : Items.ARROW)).createArrow(level, itemStack2, livingEntity);
        if (livingEntity instanceof Player) {
            createArrow.setCritArrow(true);
        }
        createArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        createArrow.setShotFromCrossbow(true);
        int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, itemStack);
        if (itemEnchantmentLevel > 0) {
            createArrow.setPierceLevel((byte) itemEnchantmentLevel);
        }
        return createArrow;
    }

    public static void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float f2) {
        List<ItemStack> chargedProjectiles = getChargedProjectiles(itemStack);
        float[] shotPitches = getShotPitches(livingEntity.getRandom());
        for (int i = 0; i < chargedProjectiles.size(); i++) {
            ItemStack itemStack2 = chargedProjectiles.get(i);
            boolean z = (livingEntity instanceof Player) && ((Player) livingEntity).abilities.instabuild;
            if (!itemStack2.isEmpty()) {
                if (i == 0) {
                    shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, shotPitches[i], z, f, f2, 0.0f);
                } else if (i == 1) {
                    shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, shotPitches[i], z, f, f2, -10.0f);
                } else if (i == 2) {
                    shootProjectile(level, livingEntity, interactionHand, itemStack, itemStack2, shotPitches[i], z, f, f2, 10.0f);
                }
            }
        }
        onCrossbowShot(level, livingEntity, itemStack);
    }

    private static float[] getShotPitches(Random random) {
        boolean nextBoolean = random.nextBoolean();
        float[] fArr = new float[3];
        fArr[0] = 1.0f;
        fArr[1] = getRandomShotPitch(nextBoolean);
        fArr[2] = getRandomShotPitch(!nextBoolean);
        return fArr;
    }

    private static float getRandomShotPitch(boolean z) {
        return (1.0f / ((random.nextFloat() * 0.5f) + 1.8f)) + (z ? 0.63f : 0.43f);
    }

    private static void onCrossbowShot(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) livingEntity;
            if (!level.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        clearChargedProjectiles(itemStack);
    }

    @Override // net.minecraft.world.item.Item
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (!level.isClientSide) {
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
            SoundEvent startSound = getStartSound(itemEnchantmentLevel);
            SoundEvent soundEvent = itemEnchantmentLevel == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float useDuration = (itemStack.getUseDuration() - i) / getChargeDuration(itemStack);
            if (useDuration < 0.2f) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }
            if (useDuration >= 0.2f && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), startSound, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
            if (useDuration >= 0.5f && soundEvent != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
        }
    }

    @Override // net.minecraft.world.item.Item
    public int getUseDuration(ItemStack itemStack) {
        return getChargeDuration(itemStack) + 3;
    }

    public static int getChargeDuration(ItemStack itemStack) {
        int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
        if (itemEnchantmentLevel == 0) {
            return 25;
        }
        return 25 - (5 * itemEnchantmentLevel);
    }

    @Override // net.minecraft.world.item.Item
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.CROSSBOW;
    }

    private SoundEvent getStartSound(int i) {
        switch (i) {
            case 1:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            case 2:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            case 3:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            default:
                return SoundEvents.CROSSBOW_LOADING_START;
        }
    }

    private static float getPowerForTime(int i, ItemStack itemStack) {
        float chargeDuration = i / getChargeDuration(itemStack);
        if (chargeDuration > 1.0f) {
            chargeDuration = 1.0f;
        }
        return chargeDuration;
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        List<ItemStack> chargedProjectiles = getChargedProjectiles(itemStack);
        if (!isCharged(itemStack) || chargedProjectiles.isEmpty()) {
            return;
        }
        ItemStack itemStack2 = chargedProjectiles.get(0);
        list.add(new TranslatableComponent("item.minecraft.crossbow.projectile").append(" ").append(itemStack2.getDisplayName()));
        if (tooltipFlag.isAdvanced() && itemStack2.getItem() == Items.FIREWORK_ROCKET) {
            List<Component> newArrayList = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendHoverText(itemStack2, level, newArrayList, tooltipFlag);
            if (!newArrayList.isEmpty()) {
                for (int i = 0; i < newArrayList.size(); i++) {
                    newArrayList.set(i, new TextComponent("  ").append(newArrayList.get(i)).withStyle(ChatFormatting.GRAY));
                }
                list.addAll(newArrayList);
            }
        }
    }

    private static float getShootingPower(ItemStack itemStack) {
        if (itemStack.getItem() == Items.CROSSBOW && containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override // net.minecraft.world.item.ProjectileWeaponItem
    public int getDefaultProjectileRange() {
        return 8;
    }
}
