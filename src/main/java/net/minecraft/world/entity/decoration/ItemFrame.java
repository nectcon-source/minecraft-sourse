package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/decoration/ItemFrame.class */
public class ItemFrame extends HangingEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    private float dropChance;
    private boolean fixed;

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level) {
        super(entityType, level);
        this.dropChance = 1.0f;
    }

    public ItemFrame(Level level, BlockPos blockPos, Direction direction) {
        super(EntityType.ITEM_FRAME, level, blockPos);
        this.dropChance = 1.0f;
        setDirection(direction);
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.0f;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
        getEntityData().define(DATA_ROTATION, 0);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    protected void setDirection(Direction direction) {
        Validate.notNull(direction);
        this.direction = direction;
        if (direction.getAxis().isHorizontal()) {
            this.xRot = 0.0f;
            this.yRot = this.direction.get2DDataValue() * 90;
        } else {
            this.xRot = (-90) * direction.getAxisDirection().getStep();
            this.yRot = 0.0f;
        }
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        recalculateBoundingBox();
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    protected void recalculateBoundingBox() {
        if (this.direction == null) {
            return;
        }
        double x = (this.pos.getX() + 0.5d) - (this.direction.getStepX() * 0.46875d);
        double y = (this.pos.getY() + 0.5d) - (this.direction.getStepY() * 0.46875d);
        double z = (this.pos.getZ() + 0.5d) - (this.direction.getStepZ() * 0.46875d);
        setPosRaw(x, y, z);
        double width = getWidth();
        double height = getHeight();
        double width2 = getWidth();
        switch (this.direction.getAxis()) {
            case X:
                width = 1.0d;
                break;
            case Y:
                height = 1.0d;
                break;
            case Z:
                width2 = 1.0d;
                break;
        }
        double d = width / 32.0d;
        double d2 = height / 32.0d;
        double d3 = width2 / 32.0d;
        setBoundingBox(new AABB(x - d, y - d2, z - d3, x + d, y + d2, z + d3));
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public boolean survives() {
        if (this.fixed) {
            return true;
        }
        if (!this.level.noCollision(this)) {
            return false;
        }
        BlockState blockState = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
        if (!blockState.getMaterial().isSolid() && (!this.direction.getAxis().isHorizontal() || !DiodeBlock.isDiode(blockState))) {
            return false;
        }
        return this.level.getEntities(this, getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.fixed) {
            super.move(moverType, vec3);
        }
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void push(double d, double d2, double d3) {
        if (!this.fixed) {
            super.push(d, d2, d3);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public float getPickRadius() {
        return 0.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public void kill() {
        removeFramedMap(getItem());
        super.kill();
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.fixed) {
            if (damageSource == DamageSource.OUT_OF_WORLD || damageSource.isCreativePlayer()) {
                return super.hurt(damageSource, f);
            }
            return false;
        }
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!damageSource.isExplosion() && !getItem().isEmpty()) {
            if (!this.level.isClientSide) {
                dropItem(damageSource.getEntity(), false);
                playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
                return true;
            }
            return true;
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getWidth() {
        return 12;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getHeight() {
        return 12;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double viewScale = 16.0d * 64.0d * getViewScale();
        return d < viewScale * viewScale;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void dropItem(@Nullable Entity entity) {
        playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0f, 1.0f);
        dropItem(entity, true);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void playPlacementSound() {
        playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0f, 1.0f);
    }

    private void dropItem(@Nullable Entity entity, boolean z) {
        if (this.fixed) {
            return;
        }
        ItemStack item = getItem();
        setItem(ItemStack.EMPTY);
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (entity == null) {
                removeFramedMap(item);
            }
        } else {
            if ((entity instanceof Player) && ((Player) entity).abilities.instabuild) {
                removeFramedMap(item);
                return;
            }
            if (z) {
                spawnAtLocation(Items.ITEM_FRAME);
            }
            if (!item.isEmpty()) {
                ItemStack copy = item.copy();
                removeFramedMap(copy);
                if (this.random.nextFloat() < this.dropChance) {
                    spawnAtLocation(copy);
                }
            }
        }
    }

    private void removeFramedMap(ItemStack itemStack) {
        if (itemStack.getItem() == Items.FILLED_MAP) {
            MapItemSavedData orCreateSavedData = MapItem.getOrCreateSavedData(itemStack, this.level);
            orCreateSavedData.removedFromFrame(this.pos, getId());
            orCreateSavedData.setDirty(true);
        }
        itemStack.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return (ItemStack) getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack itemStack) {
        setItem(itemStack, true);
    }

    public void setItem(ItemStack itemStack, boolean z) {
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            itemStack.setCount(1);
            itemStack.setEntityRepresentation(this);
        }
        getEntityData().set(DATA_ITEM, itemStack);
        if (!itemStack.isEmpty()) {
            playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
        }
        if (z && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        if (i == 0) {
            setItem(itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (entityDataAccessor.equals(DATA_ITEM)) {
            ItemStack item = getItem();
            if (!item.isEmpty() && item.getFrame() != this) {
                item.setEntityRepresentation(this);
            }
        }
    }

    public int getRotation() {
        return ((Integer) getEntityData().get(DATA_ROTATION)).intValue();
    }

    public void setRotation(int i) {
        setRotation(i, true);
    }

    private void setRotation(int i, boolean z) {
        getEntityData().set(DATA_ROTATION, Integer.valueOf(i % 8));
        if (z && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!getItem().isEmpty()) {
            compoundTag.put("Item", getItem().save(new CompoundTag()));
            compoundTag.putByte("ItemRotation", (byte) getRotation());
            compoundTag.putFloat("ItemDropChance", this.dropChance);
        }
        compoundTag.putByte("Facing", (byte) this.direction.get3DDataValue());
        compoundTag.putBoolean("Invisible", isInvisible());
        compoundTag.putBoolean("Fixed", this.fixed);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        CompoundTag compound = compoundTag.getCompound("Item");
        if (compound != null && !compound.isEmpty()) {
            ItemStack m66of = ItemStack.of(compound);
            if (m66of.isEmpty()) {
                LOGGER.warn("Unable to load item from: {}", compound);
            }
            ItemStack item = getItem();
            if (!item.isEmpty() && !ItemStack.matches(m66of, item)) {
                removeFramedMap(item);
            }
            setItem(m66of, false);
            setRotation(compoundTag.getByte("ItemRotation"), false);
            if (compoundTag.contains("ItemDropChance", 99)) {
                this.dropChance = compoundTag.getFloat("ItemDropChance");
            }
        }
        setDirection(Direction.from3DDataValue(compoundTag.getByte("Facing")));
        setInvisible(compoundTag.getBoolean("Invisible"));
        this.fixed = compoundTag.getBoolean("Fixed");
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        boolean z = !getItem().isEmpty();
        boolean z2 = !itemInHand.isEmpty();
        if (this.fixed) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide) {
            return (z || z2) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!z) {
            if (z2 && !this.removed) {
                setItem(itemInHand);
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
            }
        } else {
            playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0f, 1.0f);
            setRotation(getRotation() + 1);
        }
        return InteractionResult.CONSUME;
    }

    public int getAnalogOutput() {
        if (getItem().isEmpty()) {
            return 0;
        }
        return (getRotation() % 8) + 1;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, getType(), this.direction.get3DDataValue(), getPos());
    }
}
