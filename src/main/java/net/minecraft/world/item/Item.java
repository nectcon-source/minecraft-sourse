package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/Item.class */
public class Item implements ItemLike {
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    protected static final Random random = new Random();
    protected final CreativeModeTab category;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int maxDamage;
    private final boolean isFireResistant;
    private final Item craftingRemainingItem;

    @Nullable
    private String descriptionId;

    @Nullable
    private final FoodProperties foodProperties;

    public static int getId(Item item) {
        if (item == null) {
            return 0;
        }
        return Registry.ITEM.getId(item);
    }

    public static Item byId(int i) {
        return Registry.ITEM.byId(i);
    }

    @Deprecated
    public static Item byBlock(Block block) {
        return BY_BLOCK.getOrDefault(block, Items.AIR);
    }

    public Item(Properties properties) {
        this.category = properties.category;
        this.rarity = properties.rarity;
        this.craftingRemainingItem = properties.craftingRemainingItem;
        this.maxDamage = properties.maxDamage;
        this.maxStackSize = properties.maxStackSize;
        this.foodProperties = properties.foodProperties;
        this.isFireResistant = properties.isFireResistant;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/Item$Properties.class */
    public static class Properties {
        private int maxDamage;
        private Item craftingRemainingItem;
        private CreativeModeTab category;
        private FoodProperties foodProperties;
        private boolean isFireResistant;
        private int maxStackSize = 64;
        private Rarity rarity = Rarity.COMMON;

        public Properties food(FoodProperties foodProperties) {
            this.foodProperties = foodProperties;
            return this;
        }

        public Properties stacksTo(int i) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Unable to have damage AND stack.");
            }
            this.maxStackSize = i;
            return this;
        }

        public Properties defaultDurability(int i) {
            return this.maxDamage == 0 ? durability(i) : this;
        }

        public Properties durability(int i) {
            this.maxDamage = i;
            this.maxStackSize = 1;
            return this;
        }

        public Properties craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Properties tab(CreativeModeTab creativeModeTab) {
            this.category = creativeModeTab;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Properties fireResistant() {
            this.isFireResistant = true;
            return this;
        }
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
    }

    public boolean verifyTagAfterLoad(CompoundTag compoundTag) {
        return false;
    }

    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return true;
    }

    @Override // net.minecraft.world.level.ItemLike
    public Item asItem() {
        return this;
    }

    public InteractionResult useOn(UseOnContext useOnContext){
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        return 1.0f;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (isEdible()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if (player.canEat(getFoodProperties().canAlwaysEat())) {
                player.startUsingItem(interactionHand);
                return InteractionResultHolder.consume(itemInHand);
            }
            return InteractionResultHolder.fail(itemInHand);
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (isEdible()) {
            return livingEntity.eat(level, itemStack);
        }
        return itemStack;
    }

    public final int getMaxStackSize() {
        return this.maxStackSize;
    }

    public final int getMaxDamage() {
        return this.maxDamage;
    }

    public boolean canBeDepleted() {
        return this.maxDamage > 0;
    }

    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        return false;
    }

    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        return false;
    }

    public boolean isCorrectToolForDrops(BlockState blockState) {
        return false;
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public Component getDescription() {
        return new TranslatableComponent(getDescriptionId());
    }

    public String toString() {
        return Registry.ITEM.getKey(this).getPath();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack itemStack) {
        return getDescriptionId();
    }

    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }

    @Nullable
    public final Item getCraftingRemainingItem() {
        return this.craftingRemainingItem;
    }

    public boolean hasCraftingRemainingItem() {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean z) {
    }

    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
    }

    public boolean isComplex() {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack itemStack) {
        return itemStack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
    }

    public int getUseDuration(ItemStack itemStack) {
        if (itemStack.getItem().isEdible()) {
            return getFoodProperties().isFastFood() ? 16 : 32;
        }
        return 0;
    }

    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
    }

    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
    }

    public Component getName(ItemStack itemStack) {
        return new TranslatableComponent(getDescriptionId(itemStack));
    }

    public boolean isFoil(ItemStack itemStack) {
        return itemStack.isEnchanted();
    }

    public Rarity getRarity(ItemStack itemStack) {
        if (!itemStack.isEnchanted()) {
            return this.rarity;
        }
        switch (this.rarity) {
            case COMMON:
            case UNCOMMON:
                return Rarity.RARE;
            case RARE:
                return Rarity.EPIC;
            case EPIC:
            default:
                return this.rarity;
        }
    }

    public boolean isEnchantable(ItemStack itemStack) {
        return getMaxStackSize() == 1 && canBeDepleted();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float f = player.xRot;
        float f2 = player.yRot;
        Vec3 eyePosition = player.getEyePosition(1.0f);
        float cos = Mth.cos(((-f2) * 0.017453292f) - 3.1415927f);
        float sin = Mth.sin(((-f2) * 0.017453292f) - 3.1415927f);
        float f3 = -Mth.cos((-f) * 0.017453292f);
        return level.clip(new ClipContext(eyePosition, eyePosition.add(sin * f3 * 5.0d, Mth.sin((-f) * 0.017453292f) * 5.0d, cos * f3 * 5.0d), ClipContext.Block.OUTLINE, fluid, player));
    }

    public int getEnchantmentValue() {
        return 0;
    }

    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (allowdedIn(creativeModeTab)) {
            nonNullList.add(new ItemStack(this));
        }
    }

    protected boolean allowdedIn(CreativeModeTab creativeModeTab) {
        CreativeModeTab itemCategory = getItemCategory();
        return itemCategory != null && (creativeModeTab == CreativeModeTab.TAB_SEARCH || creativeModeTab == itemCategory);
    }

    @Nullable
    public final CreativeModeTab getItemCategory() {
        return this.category;
    }

    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return false;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return ImmutableMultimap.of();
    }

    public boolean useOnRelease(ItemStack itemStack) {
        return itemStack.getItem() == Items.CROSSBOW;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    /* renamed from: is */
    public boolean is(Tag<Item> tag) {
        return tag.contains(this);
    }

    public boolean isEdible() {
        return this.foodProperties != null;
    }

    @Nullable
    public FoodProperties getFoodProperties() {
        return this.foodProperties;
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }

    public boolean isFireResistant() {
        return this.isFireResistant;
    }

    public boolean canBeHurtBy(DamageSource damageSource) {
        return (this.isFireResistant && damageSource.isFire()) ? false : true;
    }
}
