package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/ShulkerBoxBlockEntity.class */
public class ShulkerBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, TickableBlockEntity {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks;
    private int openCount;
    private AnimationStatus animationStatus;
    private float progress;
    private float progressOld;

    @Nullable
    private DyeColor color;
    private boolean loadColorFromBlock;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/ShulkerBoxBlockEntity$AnimationStatus.class */
    public enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING
    }

    public ShulkerBoxBlockEntity(@Nullable DyeColor dyeColor) {
        super(BlockEntityType.SHULKER_BOX);
        this.itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
        this.animationStatus = AnimationStatus.CLOSED;
        this.color = dyeColor;
    }

    public ShulkerBoxBlockEntity() {
        this(null);
        this.loadColorFromBlock = true;
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        updateAnimation();
        if (this.animationStatus == AnimationStatus.OPENING || this.animationStatus == AnimationStatus.CLOSING) {
            moveCollidedEntities();
        }
    }

    protected void updateAnimation() {
        this.progressOld = this.progress;
        switch (this.animationStatus) {
            case CLOSED:
                this.progress = 0.0f;
                break;
            case OPENING:
                this.progress += 0.1f;
                if (this.progress >= 1.0f) {
                    moveCollidedEntities();
                    this.animationStatus = AnimationStatus.OPENED;
                    this.progress = 1.0f;
                    doNeighborUpdates();
                    break;
                }
                break;
            case CLOSING:
                this.progress -= 0.1f;
                if (this.progress <= 0.0f) {
                    this.animationStatus = AnimationStatus.CLOSED;
                    this.progress = 0.0f;
                    doNeighborUpdates();
                    break;
                }
                break;
            case OPENED:
                this.progress = 1.0f;
                break;
        }
    }

    public AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public AABB getBoundingBox(BlockState blockState) {
        return getBoundingBox((Direction) blockState.getValue(ShulkerBoxBlock.FACING));
    }

    public AABB getBoundingBox(Direction direction) {
        float progress = getProgress(1.0f);
        return Shapes.block().bounds().expandTowards(0.5f * progress * direction.getStepX(), 0.5f * progress * direction.getStepY(), 0.5f * progress * direction.getStepZ());
    }

    private AABB getTopBoundingBox(Direction direction) {
        Direction opposite = direction.getOpposite();
        return getBoundingBox(direction).contract(opposite.getStepX(), opposite.getStepY(), opposite.getStepZ());
    }

    private void moveCollidedEntities() {
        double d;
        double d2;
        double d3;
        BlockState blockState = this.level.getBlockState(getBlockPos());
        if (!(blockState.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        Direction direction = (Direction) blockState.getValue(ShulkerBoxBlock.FACING);
        AABB move = getTopBoundingBox(direction).move(this.worldPosition);
        List<Entity> entities = this.level.getEntities(null, move);
        if (entities.isEmpty()) {
            return;
        }
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                double d4 = 0.0d;
                double d5 = 0.0d;
                double d6 = 0.0d;
                AABB boundingBox = entity.getBoundingBox();
                switch (direction.getAxis()) {
                    case X:
                        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                            d3 = move.maxX - boundingBox.minX;
                        } else {
                            d3 = boundingBox.maxX - move.minX;
                        }
                        d4 = d3 + 0.01d;
                        break;
                    case Y:
                        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                            d2 = move.maxY - boundingBox.minY;
                        } else {
                            d2 = boundingBox.maxY - move.minY;
                        }
                        d5 = d2 + 0.01d;
                        break;
                    case Z:
                        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                            d = move.maxZ - boundingBox.minZ;
                        } else {
                            d = boundingBox.maxZ - move.minZ;
                        }
                        d6 = d + 0.01d;
                        break;
                }
                entity.move(MoverType.SHULKER_BOX, new Vec3(d4 * direction.getStepX(), d5 * direction.getStepY(), d6 * direction.getStepZ()));
            }
        }
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean triggerEvent(int i, int i2) {
        if (i == 1) {
            this.openCount = i2;
            if (i2 == 0) {
                this.animationStatus = AnimationStatus.CLOSING;
                doNeighborUpdates();
            }
            if (i2 == 1) {
                this.animationStatus = AnimationStatus.OPENING;
                doNeighborUpdates();
                return true;
            }
            return true;
        }
        return super.triggerEvent(i, i2);
    }

    private void doNeighborUpdates() {
        getBlockState().updateNeighbourShapes(getLevel(), getBlockPos(), 3);
    }

    @Override // net.minecraft.world.Container
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            this.openCount++;
            this.level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.level.playSound((Player) null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
            }
        }
    }

    @Override // net.minecraft.world.Container
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            this.openCount--;
            this.level.blockEvent(this.worldPosition, getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.level.playSound((Player) null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5f, (this.level.random.nextFloat() * 0.1f) + 0.9f);
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected Component getDefaultName() {
        return new TranslatableComponent("container.shulkerBox");
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        loadFromTag(compoundTag);
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity, net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        return saveToTag(compoundTag);
    }

    public void loadFromTag(CompoundTag compoundTag) {
        this.itemStacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compoundTag) && compoundTag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(compoundTag, this.itemStacks);
        }
    }

    public CompoundTag saveToTag(CompoundTag compoundTag) {
        if (!trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.itemStacks, false);
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override // net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.itemStacks = nonNullList;
    }

    @Override // net.minecraft.world.WorldlyContainer
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return !(Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override // net.minecraft.world.WorldlyContainer
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return true;
    }

    public float getProgress(float f) {
        return Mth.lerp(f, this.progressOld, this.progress);
    }

    @Nullable
    public DyeColor getColor() {
        if (this.loadColorFromBlock) {
            this.color = ShulkerBoxBlock.getColorFromBlock(getBlockState().getBlock());
            this.loadColorFromBlock = false;
        }
        return this.color;
    }

    @Override // net.minecraft.world.level.block.entity.BaseContainerBlockEntity
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ShulkerBoxMenu(i, inventory, this);
    }

    public boolean isClosed() {
        return this.animationStatus == AnimationStatus.CLOSED;
    }
}
