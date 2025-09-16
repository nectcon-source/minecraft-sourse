package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/BrewingStandBlockEntity.class */
public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, TickableBlockEntity {
    private static final int[] SLOTS_FOR_UP = {3};
    private static final int[] SLOTS_FOR_DOWN = {0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = {0, 1, 2, 4};
    private NonNullList<ItemStack> items;
    private int brewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    private int fuel;
    protected final ContainerData dataAccess;

    public BrewingStandBlockEntity() {
        super(BlockEntityType.BREWING_STAND);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.dataAccess = new ContainerData() { // from class: net.minecraft.world.level.block.entity.BrewingStandBlockEntity.1
            @Override // net.minecraft.world.inventory.ContainerData
            public int get(int i) {
                switch (i) {
                    case 0:
                        return BrewingStandBlockEntity.this.brewTime;
                    case 1:
                        return BrewingStandBlockEntity.this.fuel;
                    default:
                        return 0;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public void set(int i, int i2) {
                switch (i) {
                    case 0:
                        BrewingStandBlockEntity.this.brewTime = i2;
                        break;
                    case 1:
                        BrewingStandBlockEntity.this.fuel = i2;
                        break;
                }
            }

            @Override // net.minecraft.world.inventory.ContainerData
            public int getCount() {
                return 2;
            }
        };
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.brewing");
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.items.size();
    }

    @Override // net.minecraft.world.Container
    public boolean isEmpty() {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        ItemStack itemStack = this.items.get(4);
        if (this.fuel <= 0 && itemStack.getItem() == Items.BLAZE_POWDER) {
            this.fuel = 20;
            itemStack.shrink(1);
            setChanged();
        }
        boolean isBrewable = isBrewable();
        boolean z = this.brewTime > 0;
        ItemStack itemStack2 = this.items.get(3);
        if (z) {
            this.brewTime--;
            if ((this.brewTime == 0) && isBrewable) {
                doBrew();
                setChanged();
            } else if (!isBrewable) {
                this.brewTime = 0;
                setChanged();
            } else if (this.ingredient != itemStack2.getItem()) {
                this.brewTime = 0;
                setChanged();
            }
        } else if (isBrewable && this.fuel > 0) {
            this.fuel--;
            this.brewTime = 400;
            this.ingredient = itemStack2.getItem();
            setChanged();
        }
        if (!this.level.isClientSide) {
            boolean[] potionBits = getPotionBits();
            if (!Arrays.equals(potionBits, this.lastPotionCount)) {
                this.lastPotionCount = potionBits;
                BlockState blockState = this.level.getBlockState(getBlockPos());
                if (!(blockState.getBlock() instanceof BrewingStandBlock)) {
                    return;
                }
                for (int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; i++) {
                    blockState = (BlockState) blockState.setValue(BrewingStandBlock.HAS_BOTTLE[i], Boolean.valueOf(potionBits[i]));
                }
                this.level.setBlock(this.worldPosition, blockState, 2);
            }
        }
    }

    public boolean[] getPotionBits() {
        boolean[] zArr = new boolean[3];
        for (int i = 0; i < 3; i++) {
            if (!this.items.get(i).isEmpty()) {
                zArr[i] = true;
            }
        }
        return zArr;
    }

    private boolean isBrewable() {
        ItemStack itemStack = this.items.get(3);
        if (itemStack.isEmpty() || !PotionBrewing.isIngredient(itemStack)) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            ItemStack itemStack2 = this.items.get(i);
            if (!itemStack2.isEmpty() && PotionBrewing.hasMix(itemStack2, itemStack)) {
                return true;
            }
        }
        return false;
    }

    private void doBrew() {
        ItemStack itemStack = this.items.get(3);
        for (int i = 0; i < 3; i++) {
            this.items.set(i, PotionBrewing.mix(itemStack, this.items.get(i)));
        }
        itemStack.shrink(1);
        BlockPos blockPos = getBlockPos();
        if (itemStack.getItem().hasCraftingRemainingItem()) {
            ItemStack itemStack2 = new ItemStack(itemStack.getItem().getCraftingRemainingItem());
            if (itemStack.isEmpty()) {
                itemStack = itemStack2;
            } else if (!this.level.isClientSide) {
                Containers.dropItemStack(this.level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack2);
            }
        }
        this.items.set(3, itemStack);
        this.level.levelEvent(1035, blockPos, 0);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
        this.brewTime = compoundTag.getShort("BrewTime");
        this.fuel = compoundTag.getByte("Fuel");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.putShort("BrewTime", (short) this.brewTime);
        ContainerHelper.saveAllItems(compoundTag, this.items);
        compoundTag.putByte("Fuel", (byte) this.fuel);
        return compoundTag;
    }

    @Override // net.minecraft.world.Container
    public ItemStack getItem(int i) {
        if (i >= 0 && i < this.items.size()) {
            return this.items.get(i);
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        return ContainerHelper.removeItem(this.items, i, i2);
    }

    @Override // net.minecraft.world.Container
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.items, i);
    }

    @Override // net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        if (i >= 0 && i < this.items.size()) {
            this.items.set(i, itemStack);
        }
    }

    @Override // net.minecraft.world.Container
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this || player.distanceToSqr(this.worldPosition.getX() + 0.5d, this.worldPosition.getY() + 0.5d, this.worldPosition.getZ() + 0.5d) > 64.0d) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.Container
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i == 3) {
            return PotionBrewing.isIngredient(itemStack);
        }
        Item item = itemStack.getItem();
        return i == 4 ? item == Items.BLAZE_POWDER : (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) && getItem(i).isEmpty();
    }

    @Override // net.minecraft.world.WorldlyContainer
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(i, itemStack);
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return i != 3 || itemStack.getItem() == Items.GLASS_BOTTLE;
    }

    @Override // net.minecraft.world.Clearable
    public void clearContent() {
        this.items.clear();
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new BrewingStandMenu(i, inventory, this, this.dataAccess);
    }
}
