package net.minecraft.world.entity.vehicle;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/AbstractMinecartContainer.class */
public abstract class AbstractMinecartContainer extends AbstractMinecart implements Container, MenuProvider {
    private NonNullList<ItemStack> itemStacks;
    private boolean dropEquipment;

    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    protected abstract AbstractContainerMenu createMenu(int i, Inventory inventory);

    protected AbstractMinecartContainer(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
        this.dropEquipment = true;
    }

    protected AbstractMinecartContainer(EntityType<?> entityType, double d, double d2, double d3, Level level) {
        super(entityType, level, d, d2, d3);
        this.itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
        this.dropEquipment = true;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void destroy(DamageSource damageSource) {
        Entity directEntity;
        super.destroy(damageSource);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            Containers.dropContents(this.level, this, this);
            if (!this.level.isClientSide && (directEntity = damageSource.getDirectEntity()) != null && directEntity.getType() == EntityType.PLAYER) {
                PiglinAi.angerNearbyPiglins((Player) directEntity, true);
            }
        }
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.itemStacks.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        unpackLootTable(null);
        return this.itemStacks.get(i);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        unpackLootTable(null);
        return ContainerHelper.removeItem(this.itemStacks, i, i2);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        unpackLootTable(null);
        ItemStack itemStack = this.itemStacks.get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.itemStacks.set(i, ItemStack.EMPTY);
        return itemStack;
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        unpackLootTable(null);
        this.itemStacks.set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        if (i >= 0 && i < getContainerSize()) {
            setItem(i, itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.Container
    public void setChanged() {
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        if (this.removed || player.distanceToSqr(this) > 64.0d) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        this.dropEquipment = false;
        return super.changeDimension(serverLevel);
    }

    @Override // net.minecraft.world.entity.Entity
    public void remove() {
        if (!this.level.isClientSide && this.dropEquipment) {
            Containers.dropContents(this.level, this, this);
        }
        super.remove();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.lootTable != null) {
            compoundTag.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0) {
                compoundTag.putLong("LootTableSeed", this.lootTableSeed);
                return;
            }
            return;
        }
        ContainerHelper.saveAllItems(compoundTag, this.itemStacks);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.itemStacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (compoundTag.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(compoundTag.getString("LootTable"));
            this.lootTableSeed = compoundTag.getLong("LootTableSeed");
        } else {
            ContainerHelper.loadAllItems(compoundTag, this.itemStacks);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        player.openMenu(this);
        if (!player.level.isClientSide) {
            PiglinAi.angerNearbyPiglins(player, true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    protected void applyNaturalSlowdown() {
        float f = 0.98f;
        if (this.lootTable == null) {
            f = 0.98f + ((15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this)) * 0.001f);
        }
        setDeltaMovement(getDeltaMovement().multiply(f, 0.0d, f));
    }

    public void unpackLootTable(@Nullable Player player) {
        if (this.lootTable != null && this.level.getServer() != null) {
            LootTable lootTable = this.level.getServer().getLootTables().get(this.lootTable);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer) player, this.lootTable);
            }
            this.lootTable = null;
            LootContext.Builder withOptionalRandomSeed = new LootContext.Builder((ServerLevel) this.level).withParameter(LootContextParams.ORIGIN, position()).withOptionalRandomSeed(this.lootTableSeed);
            if (player != null) {
                withOptionalRandomSeed.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, withOptionalRandomSeed.create(LootContextParamSets.CHEST));
        }
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        unpackLootTable(null);
        this.itemStacks.clear();
    }

    public void setLootTable(ResourceLocation resourceLocation, long j) {
        this.lootTable = resourceLocation;
        this.lootTableSeed = j;
    }

    @Override // net.minecraft.world.inventory.MenuConstructor
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (this.lootTable == null || !player.isSpectator()) {
            unpackLootTable(inventory.player);
            return createMenu(i, inventory);
        }
        return null;
    }
}
