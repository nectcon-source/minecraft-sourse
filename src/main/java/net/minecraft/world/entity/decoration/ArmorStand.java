package net.minecraft.world.entity.decoration;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/decoration/ArmorStand.class */
public class ArmorStand extends LivingEntity {
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0f, 0.0f, -10.0f);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0f, 0.0f, 10.0f);
    private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0f, 0.0f, -1.0f);
    private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0f, 0.0f, 1.0f);
    private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0f, 0.0f, true);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5f);
    public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
    private static final Predicate<Entity> RIDABLE_MINECARTS = entity -> {
        return (entity instanceof AbstractMinecart) && ((AbstractMinecart) entity).getMinecartType() == AbstractMinecart.Type.RIDEABLE;
    };
    private final NonNullList<ItemStack> handItems;
    private final NonNullList<ItemStack> armorItems;
    private boolean invisible;
    public long lastHit;
    private int disabledSlots;
    private Rotations headPose;
    private Rotations bodyPose;
    private Rotations leftArmPose;
    private Rotations rightArmPose;
    private Rotations leftLegPose;
    private Rotations rightLegPose;

    public ArmorStand(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
        this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        this.armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        this.headPose = DEFAULT_HEAD_POSE;
        this.bodyPose = DEFAULT_BODY_POSE;
        this.leftArmPose = DEFAULT_LEFT_ARM_POSE;
        this.rightArmPose = DEFAULT_RIGHT_ARM_POSE;
        this.leftLegPose = DEFAULT_LEFT_LEG_POSE;
        this.rightLegPose = DEFAULT_RIGHT_LEG_POSE;
        this.maxUpStep = 0.0f;
    }

    public ArmorStand(Level level, double d, double d2, double d3) {
        this(EntityType.ARMOR_STAND, level);
        setPos(d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions();
        setPos(x, y, z);
    }

    private boolean hasPhysics() {
        return (isMarker() || isNoGravity()) ? false : true;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && hasPhysics();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CLIENT_FLAGS, (byte) 0);
        this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
        this.entityData.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
        this.entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        this.entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        this.entityData.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
        this.entityData.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
    }

    @Override // net.minecraft.world.entity.Entity
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND:
                return this.handItems.get(equipmentSlot.getIndex());
            case ARMOR:
                return this.armorItems.get(equipmentSlot.getIndex());
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        switch (equipmentSlot.getType()) {
            case HAND:
                playEquipSound(itemStack);
                this.handItems.set(equipmentSlot.getIndex(), itemStack);
                break;
            case ARMOR:
                playEquipSound(itemStack);
                this.armorItems.set(equipmentSlot.getIndex(), itemStack);
                break;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        EquipmentSlot equipmentSlot;
        if (i == 98) {
            equipmentSlot = EquipmentSlot.MAINHAND;
        } else if (i == 99) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        } else if (i == 100 + EquipmentSlot.HEAD.getIndex()) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (i == 100 + EquipmentSlot.CHEST.getIndex()) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (i == 100 + EquipmentSlot.LEGS.getIndex()) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (i == 100 + EquipmentSlot.FEET.getIndex()) {
            equipmentSlot = EquipmentSlot.FEET;
        } else {
            return false;
        }
        if (itemStack.isEmpty() || Mob.isValidSlotForItem(equipmentSlot, itemStack) || equipmentSlot == EquipmentSlot.HEAD) {
            setItemSlot(equipmentSlot, itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canTakeItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemStack);
        return getItemBySlot(equipmentSlotForItem).isEmpty() && !isDisabled(equipmentSlotForItem);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ListTag listTag = new ListTag();
        Iterator<ItemStack> it = this.armorItems.iterator();
        while (it.hasNext()) {
            ItemStack next = it.next();
            CompoundTag compoundTag2 = new CompoundTag();
            if (!next.isEmpty()) {
                next.save(compoundTag2);
            }
            listTag.add(compoundTag2);
        }
        compoundTag.put("ArmorItems", listTag);
        ListTag listTag2 = new ListTag();
        Iterator<ItemStack> it2 = this.handItems.iterator();
        while (it2.hasNext()) {
            ItemStack next2 = it2.next();
            CompoundTag compoundTag3 = new CompoundTag();
            if (!next2.isEmpty()) {
                next2.save(compoundTag3);
            }
            listTag2.add(compoundTag3);
        }
        compoundTag.put("HandItems", listTag2);
        compoundTag.putBoolean("Invisible", isInvisible());
        compoundTag.putBoolean("Small", isSmall());
        compoundTag.putBoolean("ShowArms", isShowArms());
        compoundTag.putInt("DisabledSlots", this.disabledSlots);
        compoundTag.putBoolean("NoBasePlate", isNoBasePlate());
        if (isMarker()) {
            compoundTag.putBoolean("Marker", isMarker());
        }
        compoundTag.put("Pose", writePose());
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("ArmorItems", 9)) {
            ListTag list = compoundTag.getList("ArmorItems", 10);
            for (int i = 0; i < this.armorItems.size(); i++) {
                this.armorItems.set(i, ItemStack.of(list.getCompound(i)));
            }
        }
        if (compoundTag.contains("HandItems", 9)) {
            ListTag list2 = compoundTag.getList("HandItems", 10);
            for (int i2 = 0; i2 < this.handItems.size(); i2++) {
                this.handItems.set(i2, ItemStack.of(list2.getCompound(i2)));
            }
        }
        setInvisible(compoundTag.getBoolean("Invisible"));
        setSmall(compoundTag.getBoolean("Small"));
        setShowArms(compoundTag.getBoolean("ShowArms"));
        this.disabledSlots = compoundTag.getInt("DisabledSlots");
        setNoBasePlate(compoundTag.getBoolean("NoBasePlate"));
        setMarker(compoundTag.getBoolean("Marker"));
        this.noPhysics = !hasPhysics();
        readPose(compoundTag.getCompound("Pose"));
    }

    private void readPose(CompoundTag compoundTag) {
        ListTag list = compoundTag.getList("Head", 5);
        setHeadPose(list.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(list));
        ListTag list2 = compoundTag.getList("Body", 5);
        setBodyPose(list2.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(list2));
        ListTag list3 = compoundTag.getList("LeftArm", 5);
        setLeftArmPose(list3.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(list3));
        ListTag list4 = compoundTag.getList("RightArm", 5);
        setRightArmPose(list4.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(list4));
        ListTag list5 = compoundTag.getList("LeftLeg", 5);
        setLeftLegPose(list5.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(list5));
        ListTag list6 = compoundTag.getList("RightLeg", 5);
        setRightLegPose(list6.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(list6));
    }

    private CompoundTag writePose() {
        CompoundTag compoundTag = new CompoundTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            compoundTag.put("Head", this.headPose.save());
        }
        if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
            compoundTag.put("Body", this.bodyPose.save());
        }
        if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
            compoundTag.put("LeftArm", this.leftArmPose.save());
        }
        if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
            compoundTag.put("RightArm", this.rightArmPose.save());
        }
        if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
            compoundTag.put("LeftLeg", this.leftLegPose.save());
        }
        if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
            compoundTag.put("RightLeg", this.rightLegPose.save());
        }
        return compoundTag;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPushable() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void doPush(Entity entity) {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void pushEntities() {
        List<Entity> entities = this.level.getEntities(this, getBoundingBox(), RIDABLE_MINECARTS);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (distanceToSqr(entity) <= 0.2d) {
                entity.push(this);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (isMarker() || itemInHand.getItem() == Items.NAME_TAG) {
            return InteractionResult.PASS;
        }
        if (player.isSpectator()) {
            return InteractionResult.SUCCESS;
        }
        if (player.level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        EquipmentSlot equipmentSlotForItem = Mob.getEquipmentSlotForItem(itemInHand);
        if (itemInHand.isEmpty()) {
            EquipmentSlot clickedSlot = getClickedSlot(vec3);
            EquipmentSlot equipmentSlot = isDisabled(clickedSlot) ? equipmentSlotForItem : clickedSlot;
            if (hasItemInSlot(equipmentSlot) && swapItem(player, equipmentSlot, itemInHand, interactionHand)) {
                return InteractionResult.SUCCESS;
            }
        } else {
            if (isDisabled(equipmentSlotForItem)) {
                return InteractionResult.FAIL;
            }
            if (equipmentSlotForItem.getType() == EquipmentSlot.Type.HAND && !isShowArms()) {
                return InteractionResult.FAIL;
            }
            if (swapItem(player, equipmentSlotForItem, itemInHand, interactionHand)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private EquipmentSlot getClickedSlot(Vec3 vec3) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        boolean isSmall = isSmall();
        double d = isSmall ? vec3.y * 2.0d : vec3.y;
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1d) {
            if (d < 0.1d + (isSmall ? 0.8d : 0.45d) && hasItemInSlot(equipmentSlot2)) {
                equipmentSlot = EquipmentSlot.FEET;
                return equipmentSlot;
            }
        }
        if (d >= 0.9d + (isSmall ? 0.3d : 0.0d)) {
            if (d < 0.9d + (isSmall ? 1.0d : 0.7d) && hasItemInSlot(EquipmentSlot.CHEST)) {
                equipmentSlot = EquipmentSlot.CHEST;
                return equipmentSlot;
            }
        }
        if (d >= 0.4d) {
            if (d < 0.4d + (isSmall ? 1.0d : 0.8d) && hasItemInSlot(EquipmentSlot.LEGS)) {
                equipmentSlot = EquipmentSlot.LEGS;
                return equipmentSlot;
            }
        }
        if (d >= 1.6d && hasItemInSlot(EquipmentSlot.HEAD)) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (!hasItemInSlot(EquipmentSlot.MAINHAND) && hasItemInSlot(EquipmentSlot.OFFHAND)) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        }
        return equipmentSlot;
    }

    private boolean isDisabled(EquipmentSlot equipmentSlot) {
        return (this.disabledSlots & (1 << equipmentSlot.getFilterFlag())) != 0 || (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !isShowArms());
    }

    private boolean swapItem(Player player, EquipmentSlot equipmentSlot, ItemStack itemStack, InteractionHand interactionHand) {
        ItemStack itemBySlot = getItemBySlot(equipmentSlot);
        if (!itemBySlot.isEmpty() && (this.disabledSlots & (1 << (equipmentSlot.getFilterFlag() + 8))) != 0) {
            return false;
        }
        if (itemBySlot.isEmpty() && (this.disabledSlots & (1 << (equipmentSlot.getFilterFlag() + 16))) != 0) {
            return false;
        }
        if (player.abilities.instabuild && itemBySlot.isEmpty() && !itemStack.isEmpty()) {
            ItemStack copy = itemStack.copy();
            copy.setCount(1);
            setItemSlot(equipmentSlot, copy);
            return true;
        }
        if (!itemStack.isEmpty() && itemStack.getCount() > 1) {
            if (!itemBySlot.isEmpty()) {
                return false;
            }
            ItemStack copy2 = itemStack.copy();
            copy2.setCount(1);
            setItemSlot(equipmentSlot, copy2);
            itemStack.shrink(1);
            return true;
        }
        setItemSlot(equipmentSlot, itemStack);
        player.setItemInHand(interactionHand, itemBySlot);
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.level.isClientSide || this.removed) {
            return false;
        }
        if (DamageSource.OUT_OF_WORLD.equals(damageSource)) {
            remove();
            return false;
        }
        if (isInvulnerableTo(damageSource) || this.invisible || isMarker()) {
            return false;
        }
        if (damageSource.isExplosion()) {
            brokenByAnything(damageSource);
            remove();
            return false;
        }
        if (DamageSource.IN_FIRE.equals(damageSource)) {
            if (isOnFire()) {
                causeDamage(damageSource, 0.15f);
                return false;
            }
            setSecondsOnFire(5);
            return false;
        }
        if (DamageSource.ON_FIRE.equals(damageSource) && getHealth() > 0.5f) {
            causeDamage(damageSource, 4.0f);
            return false;
        }
        boolean z = damageSource.getDirectEntity() instanceof AbstractArrow;
        boolean z2 = z && ((AbstractArrow) damageSource.getDirectEntity()).getPierceLevel() > 0;
        if (!"player".equals(damageSource.getMsgId()) && !z) {
            return false;
        }
        if ((damageSource.getEntity() instanceof Player) && !((Player) damageSource.getEntity()).abilities.mayBuild) {
            return false;
        }
        if (damageSource.isCreativePlayer()) {
            playBrokenSound();
            showBreakingParticles();
            remove();
            return z2;
        }
        long gameTime = this.level.getGameTime();
        if (gameTime - this.lastHit <= 5 || z) {
            brokenByPlayer(damageSource);
            showBreakingParticles();
            remove();
            return true;
        }
        this.level.broadcastEntityEvent(this, (byte) 32);
        this.lastHit = gameTime;
        return true;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 32) {
            if (this.level.isClientSide) {
                this.level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ARMOR_STAND_HIT, getSoundSource(), 0.3f, 1.0f, false);
                this.lastHit = this.level.getGameTime();
                return;
            }
            return;
        }
        super.handleEntityEvent(b);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double size = getBoundingBox().getSize() * 4.0d;
        if (Double.isNaN(size) || size == 0.0d) {
            size = 4.0d;
        }
        double d2 = size * 64.0d;
        return d < d2 * d2;
    }

    private void showBreakingParticles() {
        if (this.level instanceof ServerLevel) {
            ((ServerLevel) this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), getX(), getY(0.6666666666666666d), getZ(), 10, getBbWidth() / 4.0f, getBbHeight() / 4.0f, getBbWidth() / 4.0f, 0.05d);
        }
    }

    private void causeDamage(DamageSource damageSource, float f) {
        float health = getHealth() - f;
        if (health <= 0.5f) {
            brokenByAnything(damageSource);
            remove();
        } else {
            setHealth(health);
        }
    }

    private void brokenByPlayer(DamageSource damageSource) {
        Block.popResource(this.level, blockPosition(), new ItemStack(Items.ARMOR_STAND));
        brokenByAnything(damageSource);
    }

    private void brokenByAnything(DamageSource damageSource) {
        playBrokenSound();
        dropAllDeathLoot(damageSource);
        for (int i = 0; i < this.handItems.size(); i++) {
            ItemStack itemStack = this.handItems.get(i);
            if (!itemStack.isEmpty()) {
                Block.popResource(this.level, blockPosition().above(), itemStack);
                this.handItems.set(i, ItemStack.EMPTY);
            }
        }
        for (int i2 = 0; i2 < this.armorItems.size(); i2++) {
            ItemStack itemStack2 = this.armorItems.get(i2);
            if (!itemStack2.isEmpty()) {
                Block.popResource(this.level, blockPosition().above(), itemStack2);
                this.armorItems.set(i2, ItemStack.EMPTY);
            }
        }
    }

    private void playBrokenSound() {
        this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.ARMOR_STAND_BREAK, getSoundSource(), 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float tickHeadTurn(float f, float f2) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.yRot;
        return 0.0f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * (isBaby() ? 0.5f : 0.9f);
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return isMarker() ? 0.0d : 0.10000000149011612d;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void travel(Vec3 vec3) {
        if (!hasPhysics()) {
            return;
        }
        super.travel(vec3);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void setYBodyRot(float f) {
        this.yRotO = f;
        this.yBodyRotO = f;
        this.yHeadRot = f;
        this.yHeadRotO = f;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void setYHeadRot(float f) {
        this.yRotO = f;
        this.yBodyRotO = f;
        this.yHeadRot = f;
        this.yHeadRotO = f;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        Rotations rotations = (Rotations) this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(rotations)) {
            setHeadPose(rotations);
        }
        Rotations rotations2 = (Rotations) this.entityData.get(DATA_BODY_POSE);
        if (!this.bodyPose.equals(rotations2)) {
            setBodyPose(rotations2);
        }
        Rotations rotations3 = (Rotations) this.entityData.get(DATA_LEFT_ARM_POSE);
        if (!this.leftArmPose.equals(rotations3)) {
            setLeftArmPose(rotations3);
        }
        Rotations rotations4 = (Rotations) this.entityData.get(DATA_RIGHT_ARM_POSE);
        if (!this.rightArmPose.equals(rotations4)) {
            setRightArmPose(rotations4);
        }
        Rotations rotations5 = (Rotations) this.entityData.get(DATA_LEFT_LEG_POSE);
        if (!this.leftLegPose.equals(rotations5)) {
            setLeftLegPose(rotations5);
        }
        Rotations rotations6 = (Rotations) this.entityData.get(DATA_RIGHT_LEG_POSE);
        if (!this.rightLegPose.equals(rotations6)) {
            setRightLegPose(rotations6);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void updateInvisibilityStatus() {
        setInvisible(this.invisible);
    }

    @Override // net.minecraft.world.entity.Entity
    public void setInvisible(boolean z) {
        this.invisible = z;
        super.setInvisible(z);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return isSmall();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void kill() {
        remove();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean ignoreExplosion() {
        return isInvisible();
    }

    @Override // net.minecraft.world.entity.Entity
    public PushReaction getPistonPushReaction() {
        if (isMarker()) {
            return PushReaction.IGNORE;
        }
        return super.getPistonPushReaction();
    }

    private void setSmall(boolean z) {
        this.entityData.set(DATA_CLIENT_FLAGS, Byte.valueOf(setBit(((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue(), 1, z)));
    }

    public boolean isSmall() {
        return (((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue() & 1) != 0;
    }

    private void setShowArms(boolean z) {
        this.entityData.set(DATA_CLIENT_FLAGS, Byte.valueOf(setBit(((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue(), 4, z)));
    }

    public boolean isShowArms() {
        return (((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue() & 4) != 0;
    }

    private void setNoBasePlate(boolean z) {
        this.entityData.set(DATA_CLIENT_FLAGS, Byte.valueOf(setBit(((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue(), 8, z)));
    }

    public boolean isNoBasePlate() {
        return (((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue() & 8) != 0;
    }

    private void setMarker(boolean z) {
        this.entityData.set(DATA_CLIENT_FLAGS, Byte.valueOf(setBit(((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue(), 16, z)));
    }

    public boolean isMarker() {
        return (((Byte) this.entityData.get(DATA_CLIENT_FLAGS)).byteValue() & 16) != 0;
    }

    private byte setBit(byte b, int i, boolean z) {
        byte b2;
        if (z) {
            b2 = (byte) (b | i);
        } else {
            b2 = (byte) (b & (i ^ (-1)));
        }
        return b2;
    }

    public void setHeadPose(Rotations rotations) {
        this.headPose = rotations;
        this.entityData.set(DATA_HEAD_POSE, rotations);
    }

    public void setBodyPose(Rotations rotations) {
        this.bodyPose = rotations;
        this.entityData.set(DATA_BODY_POSE, rotations);
    }

    public void setLeftArmPose(Rotations rotations) {
        this.leftArmPose = rotations;
        this.entityData.set(DATA_LEFT_ARM_POSE, rotations);
    }

    public void setRightArmPose(Rotations rotations) {
        this.rightArmPose = rotations;
        this.entityData.set(DATA_RIGHT_ARM_POSE, rotations);
    }

    public void setLeftLegPose(Rotations rotations) {
        this.leftLegPose = rotations;
        this.entityData.set(DATA_LEFT_LEG_POSE, rotations);
    }

    public void setRightLegPose(Rotations rotations) {
        this.rightLegPose = rotations;
        this.entityData.set(DATA_RIGHT_LEG_POSE, rotations);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    public Rotations getBodyPose() {
        return this.bodyPose;
    }

    public Rotations getLeftArmPose() {
        return this.leftArmPose;
    }

    public Rotations getRightArmPose() {
        return this.rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return this.leftLegPose;
    }

    public Rotations getRightLegPose() {
        return this.rightLegPose;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return super.isPickable() && !isMarker();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean skipAttackInteraction(Entity entity) {
        return (entity instanceof Player) && !this.level.mayInteract((Player) entity, blockPosition());
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getFallDamageSound(int i) {
        return SoundEvents.ARMOR_STAND_FALL;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override // net.minecraft.world.entity.Entity
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_CLIENT_FLAGS.equals(entityDataAccessor)) {
            refreshDimensions();
            this.blocksBuilding = !isMarker();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean attackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return getDimensionsMarker(isMarker());
    }

    private EntityDimensions getDimensionsMarker(boolean z) {
        if (z) {
            return MARKER_DIMENSIONS;
        }
        return isBaby() ? BABY_DIMENSIONS : getType().getDimensions();
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getLightProbePosition(float f) {
        if (isMarker()) {
            AABB makeBoundingBox = getDimensionsMarker(false).makeBoundingBox(position());
            BlockPos blockPosition = blockPosition();
            int i = Integer.MIN_VALUE;
            for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(makeBoundingBox.minX, makeBoundingBox.minY, makeBoundingBox.minZ), new BlockPos(makeBoundingBox.maxX, makeBoundingBox.maxY, makeBoundingBox.maxZ))) {
                int max = Math.max(this.level.getBrightness(LightLayer.BLOCK, blockPos), this.level.getBrightness(LightLayer.SKY, blockPos));
                if (max == 15) {
                    return Vec3.atCenterOf(blockPos);
                }
                if (max > i) {
                    i = max;
                    blockPosition = blockPos.immutable();
                }
            }
            return Vec3.atCenterOf(blockPosition);
        }
        return super.getLightProbePosition(f);
    }
}
