package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemStack.class */
public final class ItemStack {
    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Registry.ITEM.fieldOf("id").forGetter(itemStack -> {
            return itemStack.item;
        }), Codec.INT.fieldOf("Count").forGetter(itemStack2 -> {
            return Integer.valueOf(itemStack2.count);
        }), CompoundTag.CODEC.optionalFieldOf("tag").forGetter(itemStack3 -> {
            return Optional.ofNullable(itemStack3.tag);
        })).apply(instance, (v1, v2, v3) -> {
            return new ItemStack(v1, v2, v3);
        });
    });
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ItemStack EMPTY = new ItemStack((ItemLike) null);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT =  Util.make(new DecimalFormat("#.##"), decimalFormat -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private int count;
    private int popTime;

    @Deprecated
    private final Item item;
    private CompoundTag tag;
    private boolean emptyCacheFlag;
    private Entity entityRepresentation;
    private BlockInWorld cachedBreakBlock;
    private boolean cachedBreakBlockResult;
    private BlockInWorld cachedPlaceBlock;
    private boolean cachedPlaceBlockResult;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemStack$TooltipPart.class */
    public enum TooltipPart {
        ENCHANTMENTS,
        MODIFIERS,
        UNBREAKABLE,
        CAN_DESTROY,
        CAN_PLACE,
        ADDITIONAL,
        DYE;

        private int mask = 1 << ordinal();

        TooltipPart() {
        }

        public int getMask() {
            return this.mask;
        }
    }

    public ItemStack(ItemLike itemLike) {
        this(itemLike, 1);
    }

    private ItemStack(ItemLike itemLike, int i, Optional<CompoundTag> optional) {
        this(itemLike, i);
        optional.ifPresent(this::setTag);
    }

    public ItemStack(ItemLike itemLike, int i) {
        this.item = itemLike == null ? null : itemLike.asItem();
        this.count = i;
        if (this.item != null && this.item.canBeDepleted()) {
            setDamageValue(getDamageValue());
        }
        updateEmptyCacheFlag();
    }

    private void updateEmptyCacheFlag() {
        this.emptyCacheFlag = false;
        this.emptyCacheFlag = isEmpty();
    }

    private ItemStack(CompoundTag compoundTag) {
        this.item = Registry.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
        this.count = compoundTag.getByte("Count");
        if (compoundTag.contains("tag", 10)) {
            this.tag = compoundTag.getCompound("tag");
            getItem().verifyTagAfterLoad(compoundTag);
        }
        if (getItem().canBeDepleted()) {
            setDamageValue(getDamageValue());
        }
        updateEmptyCacheFlag();
    }

    /* renamed from: of */
    public static ItemStack of(CompoundTag compoundTag) {
        try {
            return new ItemStack(compoundTag);
        } catch (RuntimeException e) {
            LOGGER.debug("Tried to load invalid item: {}", compoundTag, e);
            return EMPTY;
        }
    }

    public boolean isEmpty() {
        if (this == EMPTY || getItem() == null || getItem() == Items.AIR || this.count <= 0) {
            return true;
        }
        return false;
    }

    public ItemStack split(int i) {
        int min = Math.min(i, this.count);
        ItemStack copy = copy();
        copy.setCount(min);
        shrink(min);
        return copy;
    }

    public Item getItem() {
        return this.emptyCacheFlag ? Items.AIR : this.item;
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        BlockInWorld blockInWorld = new BlockInWorld(useOnContext.getLevel(), useOnContext.getClickedPos(), false);
        if (player != null && !player.abilities.mayBuild && !hasAdventureModePlaceTagForBlock(useOnContext.getLevel().getTagManager(), blockInWorld)) {
            return InteractionResult.PASS;
        }
        Item item = getItem();
        InteractionResult useOn = item.useOn(useOnContext);
        if (player != null && useOn.consumesAction()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return useOn;
    }

    public float getDestroySpeed(BlockState blockState) {
        return getItem().getDestroySpeed(this, blockState);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return getItem().use(level, player, interactionHand);
    }

    public ItemStack finishUsingItem(Level level, LivingEntity livingEntity) {
        return getItem().finishUsingItem(this, level, livingEntity);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        ResourceLocation key = Registry.ITEM.getKey(getItem());
        compoundTag.putString("id", key == null ? "minecraft:air" : key.toString());
        compoundTag.putByte("Count", (byte) this.count);
        if (this.tag != null) {
            compoundTag.put("tag", this.tag.copy());
        }
        return compoundTag;
    }

    public int getMaxStackSize() {
        return getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return getMaxStackSize() > 1 && !(isDamageableItem() && isDamaged());
    }

    public boolean isDamageableItem() {
        if (this.emptyCacheFlag || getItem().getMaxDamage() <= 0) {
            return false;
        }
        CompoundTag tag = getTag();
        return tag == null || !tag.getBoolean("Unbreakable");
    }

    public boolean isDamaged() {
        return isDamageableItem() && getDamageValue() > 0;
    }

    public int getDamageValue() {
        if (this.tag == null) {
            return 0;
        }
        return this.tag.getInt("Damage");
    }

    public void setDamageValue(int i) {
        getOrCreateTag().putInt("Damage", Math.max(0, i));
    }

    public int getMaxDamage() {
        return getItem().getMaxDamage();
    }

    public boolean hurt(int i, Random random, @Nullable ServerPlayer serverPlayer) {
        if (!isDamageableItem()) {
            return false;
        }
        if (i > 0) {
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
            int i2 = 0;
            for (int i3 = 0; itemEnchantmentLevel > 0 && i3 < i; i3++) {
                if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, itemEnchantmentLevel, random)) {
                    i2++;
                }
            }
            i -= i2;
            if (i <= 0) {
                return false;
            }
        }
        if (serverPlayer != null && i != 0) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, this, getDamageValue() + i);
        }
        int damageValue = getDamageValue() + i;
        setDamageValue(damageValue);
        return damageValue >= getMaxDamage();
    }

    public <T extends LivingEntity> void hurtAndBreak(int i, T t, Consumer<T> consumer) {
        if (t.level.isClientSide) {
            return;
        }
        if (((t instanceof Player) && ((Player) t).abilities.instabuild) || !isDamageableItem()) {
            return;
        }
        if (hurt(i, t.getRandom(), t instanceof ServerPlayer ? (ServerPlayer) t : null)) {
            consumer.accept(t);
            Item item = getItem();
            shrink(1);
            if (t instanceof Player) {
                ((Player) t).awardStat(Stats.ITEM_BROKEN.get(item));
            }
            setDamageValue(0);
        }
    }

    public void hurtEnemy(LivingEntity livingEntity, Player player) {
        Item item = getItem();
        if (item.hurtEnemy(this, livingEntity, player)) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public void mineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player) {
        Item item = getItem();
        if (item.mineBlock(this, level, blockState, blockPos, player)) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState blockState) {
        return getItem().isCorrectToolForDrops(blockState);
    }

    public InteractionResult interactLivingEntity(Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        return getItem().interactLivingEntity(this, player, livingEntity, interactionHand);
    }

    public ItemStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = new ItemStack(getItem(), this.count);
        itemStack.setPopTime(getPopTime());
        if (this.tag != null) {
            itemStack.tag = this.tag.copy();
        }
        return itemStack;
    }

    public static boolean tagMatches(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            return false;
        }
        if (itemStack.tag == null && itemStack2.tag != null) {
            return false;
        }
        if (itemStack.tag != null && !itemStack.tag.equals(itemStack2.tag)) {
            return false;
        }
        return true;
    }

    public static boolean matches(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            return false;
        }
        return itemStack.matches(itemStack2);
    }

    private boolean matches(ItemStack itemStack) {
        if (this.count != itemStack.count || getItem() != itemStack.getItem()) {
            return false;
        }
        if (this.tag == null && itemStack.tag != null) {
            return false;
        }
        if (this.tag != null && !this.tag.equals(itemStack.tag)) {
            return false;
        }
        return true;
    }

    public static boolean isSame(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) {
            return true;
        }
        if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            return itemStack.sameItem(itemStack2);
        }
        return false;
    }

    public static boolean isSameIgnoreDurability(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) {
            return true;
        }
        if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            return itemStack.sameItemStackIgnoreDurability(itemStack2);
        }
        return false;
    }

    public boolean sameItem(ItemStack itemStack) {
        return !itemStack.isEmpty() && getItem() == itemStack.getItem();
    }

    public boolean sameItemStackIgnoreDurability(ItemStack itemStack) {
        if (isDamageableItem()) {
            return !itemStack.isEmpty() && getItem() == itemStack.getItem();
        }
        return sameItem(itemStack);
    }

    public String getDescriptionId() {
        return getItem().getDescriptionId(this);
    }

    public String toString() {
        return this.count + " " + getItem();
    }

    public void inventoryTick(Level level, Entity entity, int i, boolean z) {
        if (this.popTime > 0) {
            this.popTime--;
        }
        if (getItem() != null) {
            getItem().inventoryTick(this, level, entity, i, z);
        }
    }

    public void onCraftedBy(Level level, Player player, int i) {
        player.awardStat(Stats.ITEM_CRAFTED.get(getItem()), i);
        getItem().onCraftedBy(this, level, player);
    }

    public int getUseDuration() {
        return getItem().getUseDuration(this);
    }

    public UseAnim getUseAnimation() {
        return getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level level, LivingEntity livingEntity, int i) {
        getItem().releaseUsing(this, level, livingEntity, i);
    }

    public boolean useOnRelease() {
        return getItem().useOnRelease(this);
    }

    public boolean hasTag() {
        return (this.emptyCacheFlag || this.tag == null || this.tag.isEmpty()) ? false : true;
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    public CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            setTag(new CompoundTag());
        }
        return this.tag;
    }

    public CompoundTag getOrCreateTagElement(String str) {
        if (this.tag == null || !this.tag.contains(str, 10)) {
            CompoundTag compoundTag = new CompoundTag();
            addTagElement(str, compoundTag);
            return compoundTag;
        }
        return this.tag.getCompound(str);
    }

    @Nullable
    public CompoundTag getTagElement(String str) {
        if (this.tag == null || !this.tag.contains(str, 10)) {
            return null;
        }
        return this.tag.getCompound(str);
    }

    public void removeTagKey(String str) {
        if (this.tag != null && this.tag.contains(str)) {
            this.tag.remove(str);
            if (this.tag.isEmpty()) {
                this.tag = null;
            }
        }
    }

    public ListTag getEnchantmentTags() {
        if (this.tag != null) {
            return this.tag.getList("Enchantments", 10);
        }
        return new ListTag();
    }

    public void setTag(@Nullable CompoundTag compoundTag) {
        this.tag = compoundTag;
        if (getItem().canBeDepleted()) {
            setDamageValue(getDamageValue());
        }
    }

    public Component getHoverName() {
        CompoundTag tagElement = getTagElement("display");
        if (tagElement != null && tagElement.contains("Name", 8)) {
            try {
                Component fromJson = Component.Serializer.fromJson(tagElement.getString("Name"));
                if (fromJson != null) {
                    return fromJson;
                }
                tagElement.remove("Name");
            } catch (JsonParseException e) {
                tagElement.remove("Name");
            }
        }
        return getItem().getName(this);
    }

    public ItemStack setHoverName(@Nullable Component component) {
        CompoundTag orCreateTagElement = getOrCreateTagElement("display");
        if (component != null) {
            orCreateTagElement.putString("Name", Component.Serializer.toJson(component));
        } else {
            orCreateTagElement.remove("Name");
        }
        return this;
    }

    public void resetHoverName() {
        CompoundTag tagElement = getTagElement("display");
        if (tagElement != null) {
            tagElement.remove("Name");
            if (tagElement.isEmpty()) {
                removeTagKey("display");
            }
        }
        if (this.tag != null && this.tag.isEmpty()) {
            this.tag = null;
        }
    }

    public boolean hasCustomHoverName() {
        CompoundTag tagElement = getTagElement("display");
        return tagElement != null && tagElement.contains("Name", 8);
    }

    public List<Component> getTooltipLines(@Nullable Player player, TooltipFlag tooltipFlag)  {
        double d;
        List<Component> newArrayList = Lists.newArrayList();
        MutableComponent withStyle = new TextComponent("").append(getHoverName()).withStyle(getRarity().color);
        if (hasCustomHoverName()) {
            withStyle.withStyle(ChatFormatting.ITALIC);
        }
        newArrayList.add(withStyle);
        if (!tooltipFlag.isAdvanced() && !hasCustomHoverName() && getItem() == Items.FILLED_MAP) {
            newArrayList.add(new TextComponent("#" + MapItem.getMapId(this)).withStyle(ChatFormatting.GRAY));
        }
        int hideFlags = getHideFlags();
        if (shouldShowInTooltip(hideFlags, TooltipPart.ADDITIONAL)) {
            getItem().appendHoverText(this, player == null ? null : player.level, newArrayList, tooltipFlag);
        }
        if (hasTag()) {
            if (shouldShowInTooltip(hideFlags, TooltipPart.ENCHANTMENTS)) {
                appendEnchantmentNames(newArrayList, getEnchantmentTags());
            }
            if (this.tag.contains("display", 10)) {
                CompoundTag compound = this.tag.getCompound("display");
                if (shouldShowInTooltip(hideFlags, TooltipPart.DYE) && compound.contains("color", 99)) {
                    if (tooltipFlag.isAdvanced()) {
                        newArrayList.add(new TranslatableComponent("item.color", String.format("#%06X", Integer.valueOf(compound.getInt("color")))).withStyle(ChatFormatting.GRAY));
                    } else {
                        newArrayList.add(new TranslatableComponent("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    }
                }
                if (compound.getTagType("Lore") == 9) {
                    ListTag list = compound.getList("Lore", 8);
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            MutableComponent fromJson = Component.Serializer.fromJson(list.getString(i));
                            if (fromJson != null) {
                                newArrayList.add(ComponentUtils.mergeStyles(fromJson, LORE_STYLE));
                            }
                        } catch (JsonParseException e) {
                            compound.remove("Lore");
                        }
                    }
                }
            }
        }
        if (shouldShowInTooltip(hideFlags, TooltipPart.MODIFIERS)) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                Multimap<Attribute, AttributeModifier> attributeModifiers = getAttributeModifiers(equipmentSlot);
                if (!attributeModifiers.isEmpty()) {
                    newArrayList.add(TextComponent.EMPTY);
                    newArrayList.add(new TranslatableComponent("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));
                    for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries()) {
                        AttributeModifier value = entry.getValue();
                        double amount = value.getAmount();
                        boolean z = false;
                        if (player != null) {
                            if (value.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                                amount = amount + player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                                z = true;
                            } else if (value.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                                amount += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                                z = true;
                            }
                        }
                        if (value.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || value.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            d = amount * 100.0d;
                        } else if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                            d = amount * 10.0d;
                        } else {
                            d = amount;
                        }
                        if (z) {
                            newArrayList.add(new TextComponent(" ").append(new TranslatableComponent("attribute.modifier.equals." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                        } else if (amount > 0.0d) {
                            newArrayList.add(new TranslatableComponent("attribute.modifier.plus." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d), new TranslatableComponent(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        } else if (amount < 0.0d) {
                            newArrayList.add(new TranslatableComponent("attribute.modifier.take." + value.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d * (-1.0d)), new TranslatableComponent(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }
        if (hasTag()) {
            if (shouldShowInTooltip(hideFlags, TooltipPart.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
                newArrayList.add(new TranslatableComponent("item.unbreakable").withStyle(ChatFormatting.BLUE));
            }
            if (shouldShowInTooltip(hideFlags, TooltipPart.CAN_DESTROY) && this.tag.contains("CanDestroy", 9)) {
                ListTag list2 = this.tag.getList("CanDestroy", 8);
                if (!list2.isEmpty()) {
                    newArrayList.add(TextComponent.EMPTY);
                    newArrayList.add(new TranslatableComponent("item.canBreak").withStyle(ChatFormatting.GRAY));
                    for (int i2 = 0; i2 < list2.size(); i2++) {
                        newArrayList.addAll(expandBlockState(list2.getString(i2)));
                    }
                }
            }
            if (shouldShowInTooltip(hideFlags, TooltipPart.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9)) {
                ListTag list3 = this.tag.getList("CanPlaceOn", 8);
                if (!list3.isEmpty()) {
                    newArrayList.add(TextComponent.EMPTY);
                    newArrayList.add(new TranslatableComponent("item.canPlace").withStyle(ChatFormatting.GRAY));
                    for (int i3 = 0; i3 < list3.size(); i3++) {
                        newArrayList.addAll(expandBlockState(list3.getString(i3)));
                    }
                }
            }
        }
        if (tooltipFlag.isAdvanced()) {
            if (isDamaged()) {
                newArrayList.add(new TranslatableComponent("item.durability", Integer.valueOf(getMaxDamage() - getDamageValue()), Integer.valueOf(getMaxDamage())));
            }
            newArrayList.add(new TextComponent(Registry.ITEM.getKey(getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (hasTag()) {
                newArrayList.add(new TranslatableComponent("item.nbt_tags", Integer.valueOf(this.tag.getAllKeys().size())).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return newArrayList;
    }

    private static boolean shouldShowInTooltip(int i, TooltipPart tooltipPart) {
        return (i & tooltipPart.getMask()) == 0;
    }

    private int getHideFlags() {
        if (hasTag() && this.tag.contains("HideFlags", 99)) {
            return this.tag.getInt("HideFlags");
        }
        return 0;
    }

    public void hideTooltipPart(TooltipPart tooltipPart) {
        CompoundTag orCreateTag = getOrCreateTag();
        orCreateTag.putInt("HideFlags", orCreateTag.getInt("HideFlags") | tooltipPart.getMask());
    }

    public static void appendEnchantmentNames(List<Component> list, ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compound.getString("id"))).ifPresent(enchantment -> {
                list.add(enchantment.getFullname(compound.getInt("lvl")));
            });
        }
    }

    private static Collection<Component> expandBlockState(String str) {
        try {
            BlockStateParser var1 = new BlockStateParser(new StringReader(str), true).parse(true);
            BlockState var2x = var1.getState();
            ResourceLocation var3xx = var1.getTag();
            boolean var4xxx = var2x != null;
            boolean var5xxxx = var3xx != null;
            if (var4xxx || var5xxxx) {
                if (var4xxx) {
                    return Lists.newArrayList(new Component[]{var2x.getBlock().getName().withStyle(ChatFormatting.DARK_GRAY)});
                }

                Tag<Block> var6xxxxx = BlockTags.getAllTags().getTag(var3xx);
                if (var6xxxxx != null) {
                    Collection<Block> var7xxxxxx = var6xxxxx.getValues();
                    if (!var7xxxxxx.isEmpty()) {
                        return var7xxxxxx.stream().map(Block::getName).map(var0x -> var0x.withStyle(ChatFormatting.DARK_GRAY)).collect(Collectors.toList());
                    }
                }
            }
        } catch (CommandSyntaxException var8) {
        }

        return Lists.newArrayList(new Component[]{new TextComponent("missingno").withStyle(ChatFormatting.DARK_GRAY)});
    }

    public boolean hasFoil() {
        return getItem().isFoil(this);
    }

    public Rarity getRarity() {
        return getItem().getRarity(this);
    }

    public boolean isEnchantable() {
        if (!getItem().isEnchantable(this) || isEnchanted()) {
            return false;
        }
        return true;
    }

    public void enchant(Enchantment enchantment, int i) {
        getOrCreateTag();
        if (!this.tag.contains("Enchantments", 9)) {
            this.tag.put("Enchantments", new ListTag());
        }
        ListTag list = this.tag.getList("Enchantments", 10);
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(enchantment)));
        compoundTag.putShort("lvl", (byte) i);
        list.add(compoundTag);
    }

    public boolean isEnchanted() {
        return (this.tag == null || !this.tag.contains("Enchantments", 9) || this.tag.getList("Enchantments", 10).isEmpty()) ? false : true;
    }

    public void addTagElement(String str, net.minecraft.nbt.Tag tag) {
        getOrCreateTag().put(str, tag);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity entity) {
        this.entityRepresentation = entity;
    }

    @Nullable
    public ItemFrame getFrame() {
        if (this.entityRepresentation instanceof ItemFrame) {
            return (ItemFrame) getEntityRepresentation();
        }
        return null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        if (this.emptyCacheFlag) {
            return null;
        }
        return this.entityRepresentation;
    }

    public int getBaseRepairCost() {
        if (hasTag() && this.tag.contains("RepairCost", 3)) {
            return this.tag.getInt("RepairCost");
        }
        return 0;
    }

    public void setRepairCost(int i) {
        getOrCreateTag().putInt("RepairCost", i);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> defaultAttributeModifiers;
        AttributeModifier load;
        if (hasTag() && this.tag.contains("AttributeModifiers", 9)) {
            defaultAttributeModifiers = HashMultimap.create();
            ListTag list = this.tag.getList("AttributeModifiers", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                if (!compound.contains("Slot", 8) || compound.getString("Slot").equals(equipmentSlot.getName())) {
                    Optional<Attribute> optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(compound.getString("AttributeName")));
                    if (optional.isPresent() && (load = AttributeModifier.load(compound)) != null && load.getId().getLeastSignificantBits() != 0 && load.getId().getMostSignificantBits() != 0) {
                        defaultAttributeModifiers.put(optional.get(), load);
                    }
                }
            }
        } else {
            defaultAttributeModifiers = getItem().getDefaultAttributeModifiers(equipmentSlot);
        }
        return defaultAttributeModifiers;
    }

    public void addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier, @Nullable EquipmentSlot equipmentSlot) {
        getOrCreateTag();
        if (!this.tag.contains("AttributeModifiers", 9)) {
            this.tag.put("AttributeModifiers", new ListTag());
        }
        ListTag list = this.tag.getList("AttributeModifiers", 10);
        CompoundTag save = attributeModifier.save();
        save.putString("AttributeName", Registry.ATTRIBUTE.getKey(attribute).toString());
        if (equipmentSlot != null) {
            save.putString("Slot", equipmentSlot.getName());
        }
        list.add(save);
    }

    public Component getDisplayName() {
        MutableComponent append = new TextComponent("").append(getHoverName());
        if (hasCustomHoverName()) {
            append.withStyle(ChatFormatting.ITALIC);
        }
        MutableComponent wrapInSquareBrackets = ComponentUtils.wrapInSquareBrackets(append);
        if (!this.emptyCacheFlag) {
            wrapInSquareBrackets.withStyle(getRarity().color).withStyle(style -> {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this)));
            });
        }
        return wrapInSquareBrackets;
    }

    private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2) {
        if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
            return false;
        }
        if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
            return true;
        }
        if (blockInWorld.getEntity() == null || blockInWorld2.getEntity() == null) {
            return false;
        }
        return Objects.equals(blockInWorld.getEntity().save(new CompoundTag()), blockInWorld2.getEntity().save(new CompoundTag()));
    }

    public boolean hasAdventureModeBreakTagForBlock(TagContainer tagContainer, BlockInWorld blockInWorld) {
        if (areSameBlocks(blockInWorld, this.cachedBreakBlock)) {
            return this.cachedBreakBlockResult;
        } else {
            this.cachedBreakBlock = blockInWorld;
            if (this.hasTag() && this.tag.contains("CanDestroy", 9)) {
                ListTag var3 = this.tag.getList("CanDestroy", 8);

                for(int var4 = 0; var4 < var3.size(); ++var4) {
                    String var5 = var3.getString(var4);

                    try {
                        Predicate<BlockInWorld> var6 = BlockPredicateArgument.blockPredicate().parse(new StringReader(var5)).create(tagContainer);
                        if (var6.test(blockInWorld)) {
                            this.cachedBreakBlockResult = true;
                            return true;
                        }
                    } catch (CommandSyntaxException var7) {
                    }
                }
            }

            this.cachedBreakBlockResult = false;
            return false;
        }
    }

    public boolean hasAdventureModePlaceTagForBlock(TagContainer tagContainer, BlockInWorld blockInWorld)  {
        if (areSameBlocks(blockInWorld, this.cachedPlaceBlock)) {
            return this.cachedPlaceBlockResult;
        } else {
            this.cachedPlaceBlock = blockInWorld;
            if (this.hasTag() && this.tag.contains("CanPlaceOn", 9)) {
                ListTag var3 = this.tag.getList("CanPlaceOn", 8);

                for(int var4x = 0; var4x < var3.size(); ++var4x) {
                    String var5xx = var3.getString(var4x);

                    try {
                        Predicate<BlockInWorld> var6xxx = BlockPredicateArgument.blockPredicate().parse(new StringReader(var5xx)).create(tagContainer);
                        if (var6xxx.test(blockInWorld)) {
                            this.cachedPlaceBlockResult = true;
                            return true;
                        }
                    } catch (CommandSyntaxException var7) {
                    }
                }
            }

            this.cachedPlaceBlockResult = false;
            return false;
        }
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int i) {
        this.popTime = i;
    }

    public int getCount() {
        if (this.emptyCacheFlag) {
            return 0;
        }
        return this.count;
    }

    public void setCount(int i) {
        this.count = i;
        updateEmptyCacheFlag();
    }

    public void grow(int i) {
        setCount(this.count + i);
    }

    public void shrink(int i) {
        grow(-i);
    }

    public void onUseTick(Level level, LivingEntity livingEntity, int i) {
        getItem().onUseTick(level, livingEntity, this, i);
    }

    public boolean isEdible() {
        return getItem().isEdible();
    }

    public SoundEvent getDrinkingSound() {
        return getItem().getDrinkingSound();
    }

    public SoundEvent getEatingSound() {
        return getItem().getEatingSound();
    }
}
