package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/ChestBlockEntity.class */
public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, TickableBlockEntity {
    private NonNullList<ItemStack> items;
    protected float openness;
    protected float oOpenness;
    protected int openCount;
    private int tickInterval;

    protected ChestBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public ChestBlockEntity() {
        this(BlockEntityType.CHEST);
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 27;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.chest");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        this.tickInterval++;
        this.openCount = getOpenCount(this.level, this, this.tickInterval, x, y, z, this.openCount);
        this.oOpenness = this.openness;
        if (this.openCount > 0 && this.openness == 0.0f) {
            playSound(SoundEvents.CHEST_OPEN);
        }
        if ((this.openCount == 0 && this.openness > 0.0f) || (this.openCount > 0 && this.openness < 1.0f)) {
            float f = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1f;
            } else {
                this.openness -= 0.1f;
            }
            if (this.openness > 1.0f) {
                this.openness = 1.0f;
            }
            if (this.openness < 0.5f && f >= 0.5f) {
                playSound(SoundEvents.CHEST_CLOSE);
            }
            if (this.openness < 0.0f) {
                this.openness = 0.0f;
            }
        }
    }

    public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int i2, int i3, int i4, int i5) {
        if (!level.isClientSide && i5 != 0 && (((i + i2) + i3) + i4) % 200 == 0) {
            i5 = getOpenCount(level, baseContainerBlockEntity, i2, i3, i4);
        }
        return i5;
    }

    public static int getOpenCount(Level level, BaseContainerBlockEntity baseContainerBlockEntity, int i, int i2, int i3) {
        Container container;
        int i4 = 0;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(i - 5.0f, i2 - 5.0f, i3 - 5.0f, i + 1 + 5.0f, i2 + 1 + 5.0f, i3 + 1 + 5.0f))) {
            if ((player.containerMenu instanceof ChestMenu) && ((container = ((ChestMenu) player.containerMenu).getContainer()) == baseContainerBlockEntity || ((container instanceof CompoundContainer) && ((CompoundContainer) container).contains(baseContainerBlockEntity)))) {
                i4++;
            }
        }
        return i4;
    }

    private void playSound(SoundEvent soundEvent) {
        ChestType chestType = (ChestType) getBlockState().getValue(ChestBlock.TYPE);
        if (chestType == ChestType.LEFT) {
            return;
        }
        double x = this.worldPosition.getX() + 0.5d;
        double y = this.worldPosition.getY() + 0.5d;
        double z = this.worldPosition.getZ() + 0.5d;
        if (chestType == ChestType.RIGHT) {
            Direction connectedDirection = ChestBlock.getConnectedDirection(getBlockState());
            x += connectedDirection.getStepX() * 0.5d;
            z += connectedDirection.getStepZ() * 0.5d;
        }
        this.level.playSound(null, x, y, z, soundEvent, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (i == 1) {
            this.openCount = i2;
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    @Override // net.minecraft.world.Container
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            this.openCount++;
            signalOpenCount();
        }
    }

    @Override // net.minecraft.world.Container
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            this.openCount--;
            signalOpenCount();
        }
    }

    protected void signalOpenCount() {
        Block block = getBlockState().getBlock();
        if (block instanceof ChestBlock) {
            this.level.blockEvent(this.worldPosition, block, 1, this.openCount);
            this.level.updateNeighborsAt(this.worldPosition, block);
        }
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override // net.minecraft.world.level.block.entity.LidBlockEntity
    public float getOpenNess(float f) {
        return Mth.lerp(f, this.oOpenness, this.openness);
    }

    public static int getOpenCount(BlockGetter blockGetter, BlockPos blockPos) {
        if (blockGetter.getBlockState(blockPos).getBlock().isEntityBlock()) {
            BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
            if (blockEntity instanceof ChestBlockEntity) {
                return ((ChestBlockEntity) blockEntity).openCount;
            }
            return 0;
        }
        return 0;
    }

    public static void swapContents(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
        NonNullList<ItemStack> items = chestBlockEntity.getItems();
        chestBlockEntity.setItems(chestBlockEntity2.getItems());
        chestBlockEntity2.setItems(items);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return ChestMenu.threeRows(i, inventory, this);
    }
}
