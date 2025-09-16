package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/HopperBlockEntity.class */
public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper, TickableBlockEntity {
    private NonNullList<ItemStack> items;
    private int cooldownTime;
    private long tickedGameTime;

    public HopperBlockEntity() {
        super(BlockEntityType.HOPPER);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
        this.cooldownTime = -1;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
        this.cooldownTime = compoundTag.getInt("TransferCooldown");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        compoundTag.putInt("TransferCooldown", this.cooldownTime);
        return compoundTag;
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.items.size();
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity, net.minecraft.world.Container
    public ItemStack removeItem(int i, int i2) {
        unpackLootTable(null);
        return ContainerHelper.removeItem(getItems(), i, i2);
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity, net.minecraft.world.Container
    public void setItem(int i, ItemStack itemStack) {
        unpackLootTable(null);
        getItems().set(i, itemStack);
        if (itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.hopper");
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.cooldownTime--;
        this.tickedGameTime = this.level.getGameTime();
        if (!isOnCooldown()) {
            setCooldown(0);
            tryMoveItems(() -> {
                return Boolean.valueOf(suckInItems(this));
            });
        }
    }

    private boolean tryMoveItems(Supplier<Boolean> supplier) {
        if (this.level != null && !this.level.isClientSide && !isOnCooldown() && ((Boolean) getBlockState().getValue(HopperBlock.ENABLED)).booleanValue()) {
            boolean z = false;
            if (!isEmpty()) {
                z = ejectItems();
            }
            if (!inventoryFull()) {
                z |= supplier.get().booleanValue();
            }
            if (z) {
                setCooldown(8);
                setChanged();
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean inventoryFull() {
        Iterator<ItemStack> it = this.items.iterator();
        while (it.hasNext()) {
            ItemStack next = it.next();
            if (next.isEmpty() || next.getCount() != next.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    private boolean ejectItems() {
        Container attachedContainer = getAttachedContainer();
        if (attachedContainer == null) {
            return false;
        }
        Direction opposite = ((Direction) getBlockState().getValue(HopperBlock.FACING)).getOpposite();
        if (isFullContainer(attachedContainer, opposite)) {
            return false;
        }
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                ItemStack copy = getItem(i).copy();
                if (addItem(this, attachedContainer, removeItem(i, 1), opposite).isEmpty()) {
                    attachedContainer.setChanged();
                    return true;
                }
                setItem(i, copy);
            }
        }
        return false;
    }

    private static IntStream getSlots(Container container, Direction direction) {
        if (container instanceof WorldlyContainer) {
            return IntStream.of(((WorldlyContainer) container).getSlotsForFace(direction));
        }
        return IntStream.range(0, container.getContainerSize());
    }

    private boolean isFullContainer(Container container, Direction direction) {
        return getSlots(container, direction).allMatch(i -> {
            ItemStack item = container.getItem(i);
            return item.getCount() >= item.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container container, Direction direction) {
        return getSlots(container, direction).allMatch(i -> {
            return container.getItem(i).isEmpty();
        });
    }

    public static boolean suckInItems(Hopper hopper) {
        Container sourceContainer = getSourceContainer(hopper);
        if (sourceContainer != null) {
            Direction direction = Direction.DOWN;
            if (isEmptyContainer(sourceContainer, direction)) {
                return false;
            }
            return getSlots(sourceContainer, direction).anyMatch(i -> {
                return tryTakeInItemFromSlot(hopper, sourceContainer, i, direction);
            });
        }
        Iterator<ItemEntity> it = getItemsAtAndAbove(hopper).iterator();
        while (it.hasNext()) {
            if (addItem(hopper, it.next())) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
        ItemStack item = container.getItem(i);
        if (!item.isEmpty() && canTakeItemFromContainer(container, item, i, direction)) {
            ItemStack copy = item.copy();
            if (addItem(container, hopper, container.removeItem(i, 1), null).isEmpty()) {
                container.setChanged();
                return true;
            }
            container.setItem(i, copy);
            return false;
        }
        return false;
    }

    public static boolean addItem(Container container, ItemEntity itemEntity) {
        boolean z = false;
        ItemStack addItem = addItem(null, container, itemEntity.getItem().copy(), null);
        if (addItem.isEmpty()) {
            z = true;
            itemEntity.remove();
        } else {
            itemEntity.setItem(addItem);
        }
        return z;
    }

    public static ItemStack addItem(@Nullable Container container, Container container2, ItemStack itemStack, @Nullable Direction direction) {
        if ((container2 instanceof WorldlyContainer) && direction != null) {
            int[] slotsForFace = ((WorldlyContainer) container2).getSlotsForFace(direction);
            for (int i = 0; i < slotsForFace.length && !itemStack.isEmpty(); i++) {
                itemStack = tryMoveInItem(container, container2, itemStack, slotsForFace[i], direction);
            }
        } else {
            int containerSize = container2.getContainerSize();
            for (int i2 = 0; i2 < containerSize && !itemStack.isEmpty(); i2++) {
                itemStack = tryMoveInItem(container, container2, itemStack, i2, direction);
            }
        }
        return itemStack;
    }

    private static boolean canPlaceItemInContainer(Container container, ItemStack itemStack, int i, @Nullable Direction direction) {
        if (!container.canPlaceItem(i, itemStack)) {
            return false;
        }
        if ((container instanceof WorldlyContainer) && !((WorldlyContainer) container).canPlaceItemThroughFace(i, itemStack, direction)) {
            return false;
        }
        return true;
    }

    private static boolean canTakeItemFromContainer(Container container, ItemStack itemStack, int i, Direction direction) {
        if ((container instanceof WorldlyContainer) && !((WorldlyContainer) container).canTakeItemThroughFace(i, itemStack, direction)) {
            return false;
        }
        return true;
    }

    private static ItemStack tryMoveInItem(@Nullable Container container, Container container2, ItemStack itemStack, int i, @Nullable Direction direction) {
        ItemStack item = container2.getItem(i);
        if (canPlaceItemInContainer(container2, itemStack, i, direction)) {
            boolean z = false;
            boolean isEmpty = container2.isEmpty();
            if (item.isEmpty()) {
                container2.setItem(i, itemStack);
                itemStack = ItemStack.EMPTY;
                z = true;
            } else if (canMergeItems(item, itemStack)) {
                int min = Math.min(itemStack.getCount(), itemStack.getMaxStackSize() - item.getCount());
                itemStack.shrink(min);
                item.grow(min);
                z = min > 0;
            }
            if (z) {
                if (isEmpty && (container2 instanceof HopperBlockEntity)) {
                    HopperBlockEntity hopperBlockEntity = (HopperBlockEntity) container2;
                    if (!hopperBlockEntity.isOnCustomCooldown()) {
                        int i2 = 0;
                        if ((container instanceof HopperBlockEntity) && hopperBlockEntity.tickedGameTime >= ((HopperBlockEntity) container).tickedGameTime) {
                            i2 = 1;
                        }
                        hopperBlockEntity.setCooldown(8 - i2);
                    }
                }
                container2.setChanged();
            }
        }
        return itemStack;
    }

    @Nullable
    private Container getAttachedContainer() {
        return getContainerAt(getLevel(), this.worldPosition.relative((Direction) getBlockState().getValue(HopperBlock.FACING)));
    }

    @Nullable
    public static Container getSourceContainer(Hopper hopper) {
        return getContainerAt(hopper.getLevel(), hopper.getLevelX(), hopper.getLevelY() + 1.0d, hopper.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Hopper hopper) {
        return (List) hopper.getSuckShape().toAabbs().stream().flatMap(aabb -> {
            return hopper.getLevel().getEntitiesOfClass(ItemEntity.class, aabb.move(hopper.getLevelX() - 0.5d, hopper.getLevelY() - 0.5d, hopper.getLevelZ() - 0.5d), EntitySelector.ENTITY_STILL_ALIVE).stream();
        }).collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos blockPos) {
        return getContainerAt(level, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Nullable
    public static Container getContainerAt(Level level, double d, double d2, double d3) {
        Container container = null;
        BlockPos blockPos = new BlockPos(d, d2, d3);
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder) block).getContainer(blockState, level, blockPos);
        } else if (block.isEntityBlock()) {
            Object blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof Container) {
                container = (Container) blockEntity;
                if ((container instanceof ChestBlockEntity) && (block instanceof ChestBlock)) {
                    container = ChestBlock.getContainer((ChestBlock) block, blockState, level, blockPos, true);
                }
            }
        }
        if (container == null) {
            List<Entity> entities = level.getEntities((Entity) null, new AABB(d - 0.5d, d2 - 0.5d, d3 - 0.5d, d + 0.5d, d2 + 0.5d, d3 + 0.5d), EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!entities.isEmpty()) {
                container = (Container) entities.get(level.random.nextInt(entities.size()));
            }
        }
        return container;
    }

    private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.getItem() != itemStack2.getItem() || itemStack.getDamageValue() != itemStack2.getDamageValue() || itemStack.getCount() > itemStack.getMaxStackSize() || !ItemStack.tagMatches(itemStack, itemStack2)) {
            return false;
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelX() {
        return this.worldPosition.getX() + 0.5d;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelY() {
        return this.worldPosition.getY() + 0.5d;
    }

    @Override // net.minecraft.world.level.block.entity.Hopper
    public double getLevelZ() {
        return this.worldPosition.getZ() + 0.5d;
    }

    private void setCooldown(int i) {
        this.cooldownTime = i;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public void entityInside(Entity entity) {
        if (entity instanceof ItemEntity) {
            BlockPos blockPos = getBlockPos();
            if (Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ())), getSuckShape(), BooleanOp.AND)) {
                tryMoveItems(() -> {
                    return Boolean.valueOf(addItem(this, (ItemEntity) entity));
                });
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new HopperMenu(i, inventory, this);
    }
}
