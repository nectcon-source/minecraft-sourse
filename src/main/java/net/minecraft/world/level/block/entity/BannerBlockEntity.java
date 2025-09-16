package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BannerBlockEntity.class */
public class BannerBlockEntity extends BlockEntity implements Nameable {

    @Nullable
    private Component name;

    @Nullable
    private DyeColor baseColor;

    @Nullable
    private ListTag itemPatterns;
    private boolean receivedData;

    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public BannerBlockEntity() {
        super(BlockEntityType.BANNER);
        this.baseColor = DyeColor.WHITE;
    }

    public BannerBlockEntity(DyeColor dyeColor) {
        this();
        this.baseColor = dyeColor;
    }

    @Nullable
    public static ListTag getItemPatterns(ItemStack itemStack) {
        ListTag listTag = null;
        CompoundTag tagElement = itemStack.getTagElement("BlockEntityTag");
        if (tagElement != null && tagElement.contains("Patterns", 9)) {
            listTag = tagElement.getList("Patterns", 10).copy();
        }
        return listTag;
    }

    public void fromItem(ItemStack itemStack, DyeColor dyeColor) {
        this.itemPatterns = getItemPatterns(itemStack);
        this.baseColor = dyeColor;
        this.patterns = null;
        this.receivedData = true;
        this.name = itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
    }

    @Override // net.minecraft.world.Nameable
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return new TranslatableComponent("block.minecraft.banner");
    }

    @Override // net.minecraft.world.Nameable
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.itemPatterns != null) {
            compoundTag.put("Patterns", this.itemPatterns);
        }
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
        if (hasLevel()) {
            this.baseColor = ((AbstractBannerBlock) getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }
        this.itemPatterns = compoundTag.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public static int getPatternCount(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("BlockEntityTag");
        if (tagElement != null && tagElement.contains("Patterns")) {
            return tagElement.getList("Patterns", 10).size();
        }
        return 0;
    }

    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = createPatterns(getBaseColor(this::getBlockState), this.itemPatterns);
        }
        return this.patterns;
    }

    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor dyeColor, @Nullable ListTag listTag) {
        List<Pair<BannerPattern, DyeColor>> newArrayList = Lists.newArrayList();
        newArrayList.add(Pair.of(BannerPattern.BASE, dyeColor));
        if (listTag != null) {
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag compound = listTag.getCompound(i);
                BannerPattern byHash = BannerPattern.byHash(compound.getString("Pattern"));
                if (byHash != null) {
                    newArrayList.add(Pair.of(byHash, DyeColor.byId(compound.getInt("Color"))));
                }
            }
        }
        return newArrayList;
    }

    public static void removeLastPattern(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("BlockEntityTag");
        if (tagElement == null || !tagElement.contains("Patterns", 9)) {
            return;
        }
        ListTag list = tagElement.getList("Patterns", 10);
        if (list.isEmpty()) {
            return;
        }
        list.remove(list.size() - 1);
        if (list.isEmpty()) {
            itemStack.removeTagKey("BlockEntityTag");
        }
    }

    public ItemStack getItem(BlockState blockState) {
        ItemStack itemStack = new ItemStack(BannerBlock.byColor(getBaseColor(() -> {
            return blockState;
        })));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemStack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }
        if (this.name != null) {
            itemStack.setHoverName(this.name);
        }
        return itemStack;
    }

    public DyeColor getBaseColor(Supplier<BlockState> supplier) {
        if (this.baseColor == null) {
            this.baseColor = ((AbstractBannerBlock) supplier.get().getBlock()).getColor();
        }
        return this.baseColor;
    }
}
