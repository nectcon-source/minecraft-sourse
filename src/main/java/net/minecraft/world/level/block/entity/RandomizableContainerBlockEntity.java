package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/RandomizableContainerBlockEntity.class */
public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity {

    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> nonNullList);

    protected RandomizableContainerBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public static void setLootTable(BlockGetter blockGetter, Random random, BlockPos blockPos, ResourceLocation resourceLocation) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof RandomizableContainerBlockEntity) {
            ((RandomizableContainerBlockEntity) blockEntity).setLootTable(resourceLocation, random.nextLong());
        }
    }

    protected boolean tryLoadLootTable(CompoundTag compoundTag) {
        if (compoundTag.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(compoundTag.getString("LootTable"));
            this.lootTableSeed = compoundTag.getLong("LootTableSeed");
            return true;
        }
        return false;
    }

    protected boolean trySaveLootTable(CompoundTag compoundTag) {
        if (this.lootTable == null) {
            return false;
        }
        compoundTag.putString("LootTable", this.lootTable.toString());
        if (this.lootTableSeed != 0) {
            compoundTag.putLong("LootTableSeed", this.lootTableSeed);
            return true;
        }
        return true;
    }

    public void unpackLootTable(@Nullable Player player) {
        if (this.lootTable != null && this.level.getServer() != null) {
            LootTable lootTable = this.level.getServer().getLootTables().get(this.lootTable);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, this.lootTable);
            }
            this.lootTable = null;
            LootContext.Builder withOptionalRandomSeed = new LootContext.Builder((ServerLevel) this.level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withOptionalRandomSeed(this.lootTableSeed);
            if (player != null) {
                withOptionalRandomSeed.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, withOptionalRandomSeed.create(LootContextParamSets.CHEST));
        }
    }

    public void setLootTable(ResourceLocation resourceLocation, long j) {
        this.lootTable = resourceLocation;
        this.lootTableSeed = j;
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        unpackLootTable(null);
        return getItems().stream().allMatch((v0) -> {
            return v0.isEmpty();
        });
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        unpackLootTable(null);
        return getItems().get(i);
    }

    public ItemStack removeItem(int i, int i2) {
        unpackLootTable(null);
        ItemStack removeItem = ContainerHelper.removeItem(getItems(), i, i2);
        if (!removeItem.isEmpty()) {
            setChanged();
        }
        return removeItem;
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        unpackLootTable(null);
        return ContainerHelper.takeItem(getItems(), i);
    }

    public void setItem(int i, ItemStack itemStack) {
        unpackLootTable(null);
        getItems().set(i, itemStack);
        if (itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this || player.distanceToSqr(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d) > 64.0d) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        getItems().clear();
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    public boolean canOpen(Player player) {
        return super.canOpen(player) && (this.lootTable == null || !player.isSpectator());
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.inventory.MenuConstructor
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (canOpen(player)) {
            unpackLootTable(inventory.player);
            return createMenu(i, inventory);
        }
        return null;
    }
}
