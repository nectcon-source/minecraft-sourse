package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/Mob.class */
public abstract class Mob extends LivingEntity {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    private LivingEntity target;
    private final Sensing sensing;
    private final NonNullList<ItemStack> handItems;
    protected final float[] handDropChances;
    private final NonNullList<ItemStack> armorItems;
    protected final float[] armorDropChances;
    private boolean canPickUpLoot;
    private boolean persistenceRequired;
    private final Map<BlockPathTypes, Float> pathfindingMalus;
    private ResourceLocation lootTable;
    private long lootTableSeed;

    @Nullable
    private Entity leashHolder;
    private int delayedLeashHolderId;

    @Nullable
    private CompoundTag leashInfoTag;
    private BlockPos restrictCenter;
    private float restrictRadius;

    protected Mob(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        this.handDropChances = new float[2];
        this.armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
        this.armorDropChances = new float[4];
        this.pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
        this.restrictCenter = BlockPos.ZERO;
        this.restrictRadius = -1.0f;
        this.goalSelector = new GoalSelector(level.getProfilerSupplier());
        this.targetSelector = new GoalSelector(level.getProfilerSupplier());
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = createBodyControl();
        this.navigation = createNavigation(level);
        this.sensing = new Sensing(this);
        Arrays.fill(this.armorDropChances, 0.085f);
        Arrays.fill(this.handDropChances, 0.085f);
        if (level != null && !level.isClientSide) {
            registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0d).add(Attributes.ATTACK_KNOCKBACK);
    }

    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(BlockPathTypes blockPathTypes) {
        Mob mob;
        if ((getVehicle() instanceof Mob) && ((Mob) getVehicle()).shouldPassengersInheritMalus()) {
            mob = (Mob) getVehicle();
        } else {
            mob = this;
        }
        Float f = mob.pathfindingMalus.get(blockPathTypes);
        return f == null ? blockPathTypes.getMalus() : f.floatValue();
    }

    public void setPathfindingMalus(BlockPathTypes blockPathTypes, float f) {
        this.pathfindingMalus.put(blockPathTypes, Float.valueOf(f));
    }

    public boolean canCutCorner(BlockPathTypes blockPathTypes) {
        return (blockPathTypes == BlockPathTypes.DANGER_FIRE || blockPathTypes == BlockPathTypes.DANGER_CACTUS || blockPathTypes == BlockPathTypes.DANGER_OTHER || blockPathTypes == BlockPathTypes.WALKABLE_DOOR) ? false : true;
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        if (isPassenger() && (getVehicle() instanceof Mob)) {
            return ((Mob) getVehicle()).getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        if (isPassenger() && (getVehicle() instanceof Mob)) {
            return ((Mob) getVehicle()).getNavigation();
        }
        return this.navigation;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Nullable
    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canAttackType(EntityType<?> entityType) {
        return entityType != EntityType.GHAST;
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return false;
    }

    public void ate() {
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MOB_FLAGS_ID, (byte) 0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        SoundEvent ambientSound = getAmbientSound();
        if (ambientSound != null) {
            playSound(ambientSound, getSoundVolume(), getVoicePitch());
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void baseTick() {
        super.baseTick();
        this.level.getProfiler().push("mobBaseTick");
        if (isAlive()) {
            int nextInt = this.random.nextInt(1000);
            int i = this.ambientSoundTime;
            this.ambientSoundTime = i + 1;
            if (nextInt < i) {
                resetAmbientSoundTime();
                playAmbientSound();
            }
        }
        this.level.getProfiler().pop();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void playHurtSound(DamageSource damageSource) {
        resetAmbientSoundTime();
        super.playHurtSound(damageSource);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -getAmbientSoundInterval();
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected int getExperienceReward(Player player) {
        if (this.xpReward > 0) {
            int i = this.xpReward;
            for (int i2 = 0; i2 < this.armorItems.size(); i2++) {
                if (!this.armorItems.get(i2).isEmpty() && this.armorDropChances[i2] <= 1.0f) {
                    i += 1 + this.random.nextInt(3);
                }
            }
            for (int i3 = 0; i3 < this.handItems.size(); i3++) {
                if (!this.handItems.get(i3).isEmpty() && this.handDropChances[i3] <= 1.0f) {
                    i += 1 + this.random.nextInt(3);
                }
            }
            return i;
        }
        return this.xpReward;
    }

    public void spawnAnim() {
        if (this.level.isClientSide) {
            for (int i = 0; i < 20; i++) {
                double nextGaussian = this.random.nextGaussian() * 0.02d;
                double nextGaussian2 = this.random.nextGaussian() * 0.02d;
                double nextGaussian3 = this.random.nextGaussian() * 0.02d;
                this.level.addParticle(ParticleTypes.POOF, getX(1.0d) - (nextGaussian * 10.0d), getRandomY() - (nextGaussian2 * 10.0d), getRandomZ(1.0d) - (nextGaussian3 * 10.0d), nextGaussian, nextGaussian2, nextGaussian3);
            }
            return;
        }
        this.level.broadcastEntityEvent(this, (byte) 20);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 20) {
            spawnAnim();
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            tickLeash();
            if (this.tickCount % 5 == 0) {
                updateControlFlags();
            }
        }
    }

    protected void updateControlFlags() {
        boolean z = !(getControllingPassenger() instanceof Mob);
        boolean z2 = !(getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, z);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, z && z2);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, z);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float tickHeadTurn(float f, float f2) {
        this.bodyRotationControl.clientTick();
        return f2;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("CanPickUpLoot", canPickUpLoot());
        compoundTag.putBoolean("PersistenceRequired", this.persistenceRequired);
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
        ListTag listTag3 = new ListTag();
        for (float f : this.armorDropChances) {
            listTag3.add(FloatTag.valueOf(f));
        }
        compoundTag.put("ArmorDropChances", listTag3);
        ListTag listTag4 = new ListTag();
        for (float f2 : this.handDropChances) {
            listTag4.add(FloatTag.valueOf(f2));
        }
        compoundTag.put("HandDropChances", listTag4);
        if (this.leashHolder != null) {
            CompoundTag compoundTag4 = new CompoundTag();
            if (this.leashHolder instanceof LivingEntity) {
                compoundTag4.putUUID("UUID", this.leashHolder.getUUID());
            } else if (this.leashHolder instanceof HangingEntity) {
                BlockPos pos = ((HangingEntity) this.leashHolder).getPos();
                compoundTag4.putInt("X", pos.getX());
                compoundTag4.putInt("Y", pos.getY());
                compoundTag4.putInt("Z", pos.getZ());
            }
            compoundTag.put("Leash", compoundTag4);
        } else if (this.leashInfoTag != null) {
            compoundTag.put("Leash", this.leashInfoTag.copy());
        }
        compoundTag.putBoolean("LeftHanded", isLeftHanded());
        if (this.lootTable != null) {
            compoundTag.putString("DeathLootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0) {
                compoundTag.putLong("DeathLootTableSeed", this.lootTableSeed);
            }
        }
        if (isNoAi()) {
            compoundTag.putBoolean("NoAI", isNoAi());
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CanPickUpLoot", 1)) {
            setCanPickUpLoot(compoundTag.getBoolean("CanPickUpLoot"));
        }
        this.persistenceRequired = compoundTag.getBoolean("PersistenceRequired");
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
        if (compoundTag.contains("ArmorDropChances", 9)) {
            ListTag list3 = compoundTag.getList("ArmorDropChances", 5);
            for (int i3 = 0; i3 < list3.size(); i3++) {
                this.armorDropChances[i3] = list3.getFloat(i3);
            }
        }
        if (compoundTag.contains("HandDropChances", 9)) {
            ListTag list4 = compoundTag.getList("HandDropChances", 5);
            for (int i4 = 0; i4 < list4.size(); i4++) {
                this.handDropChances[i4] = list4.getFloat(i4);
            }
        }
        if (compoundTag.contains("Leash", 10)) {
            this.leashInfoTag = compoundTag.getCompound("Leash");
        }
        setLeftHanded(compoundTag.getBoolean("LeftHanded"));
        if (compoundTag.contains("DeathLootTable", 8)) {
            this.lootTable = new ResourceLocation(compoundTag.getString("DeathLootTable"));
            this.lootTableSeed = compoundTag.getLong("DeathLootTableSeed");
        }
        setNoAi(compoundTag.getBoolean("NoAI"));
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropFromLootTable(DamageSource damageSource, boolean z) {
        super.dropFromLootTable(damageSource, z);
        this.lootTable = null;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected LootContext.Builder createLootContext(boolean z, DamageSource damageSource) {
        return super.createLootContext(z, damageSource).withOptionalRandomSeed(this.lootTableSeed, this.random);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public final ResourceLocation getLootTable() {
        return this.lootTable == null ? getDefaultLootTable() : this.lootTable;
    }

    protected ResourceLocation getDefaultLootTable() {
        return super.getLootTable();
    }

    public void setZza(float f) {
        this.zza = f;
    }

    public void setYya(float f) {
        this.yya = f;
    }

    public void setXxa(float f) {
        this.xxa = f;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void setSpeed(float f) {
        super.setSpeed(f);
        setZza(f);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        this.level.getProfiler().push("looting");
        if (!this.level.isClientSide && canPickUpLoot() && isAlive() && !this.dead && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (ItemEntity itemEntity : this.level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1.0d, 0.0d, 1.0d))) {
                if (!itemEntity.removed && !itemEntity.getItem().isEmpty() && !itemEntity.hasPickUpDelay() && wantsToPickUp(itemEntity.getItem())) {
                    pickUpItem(itemEntity);
                }
            }
        }
        this.level.getProfiler().pop();
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (equipItemIfPossible(item)) {
            onItemPickup(itemEntity);
            take(itemEntity, item.getCount());
            itemEntity.remove();
        }
    }

    public boolean equipItemIfPossible(ItemStack itemStack) {
        EquipmentSlot equipmentSlotForItem = getEquipmentSlotForItem(itemStack);
        ItemStack itemBySlot = getItemBySlot(equipmentSlotForItem);
        if (canReplaceCurrentItem(itemStack, itemBySlot) && canHoldItem(itemStack)) {
            double equipmentDropChance = getEquipmentDropChance(equipmentSlotForItem);
            if (!itemBySlot.isEmpty() && Math.max(this.random.nextFloat() - 0.1f, 0.0f) < equipmentDropChance) {
                spawnAtLocation(itemBySlot);
            }
            setItemSlotAndDropWhenKilled(equipmentSlotForItem, itemStack);
            playEquipSound(itemStack);
            return true;
        }
        return false;
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        setItemSlot(equipmentSlot, itemStack);
        setGuaranteedDrop(equipmentSlot);
        this.persistenceRequired = true;
    }

    public void setGuaranteedDrop(EquipmentSlot equipmentSlot) {
        switch (equipmentSlot.getType()) {
            case HAND:
                this.handDropChances[equipmentSlot.getIndex()] = 2.0f;
                break;
            case ARMOR:
                this.armorDropChances[equipmentSlot.getIndex()] = 2.0f;
                break;
        }
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.getItem() instanceof SwordItem) {
            if (!(itemStack2.getItem() instanceof SwordItem)) {
                return true;
            }
            SwordItem swordItem = (SwordItem) itemStack.getItem();
            SwordItem swordItem2 = (SwordItem) itemStack2.getItem();
            if (swordItem.getDamage() != swordItem2.getDamage()) {
                return swordItem.getDamage() > swordItem2.getDamage();
            }
            return canReplaceEqualItem(itemStack, itemStack2);
        }
        if ((itemStack.getItem() instanceof BowItem) && (itemStack2.getItem() instanceof BowItem)) {
            return canReplaceEqualItem(itemStack, itemStack2);
        }
        if ((itemStack.getItem() instanceof CrossbowItem) && (itemStack2.getItem() instanceof CrossbowItem)) {
            return canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof ArmorItem) {
            if (EnchantmentHelper.hasBindingCurse(itemStack2)) {
                return false;
            }
            if (!(itemStack2.getItem() instanceof ArmorItem)) {
                return true;
            }
            ArmorItem armorItem = (ArmorItem) itemStack.getItem();
            ArmorItem armorItem2 = (ArmorItem) itemStack2.getItem();
            if (armorItem.getDefense() != armorItem2.getDefense()) {
                return armorItem.getDefense() > armorItem2.getDefense();
            }
            if (armorItem.getToughness() != armorItem2.getToughness()) {
                return armorItem.getToughness() > armorItem2.getToughness();
            }
            return canReplaceEqualItem(itemStack, itemStack2);
        }
        if (itemStack.getItem() instanceof DiggerItem) {
            if (itemStack2.getItem() instanceof BlockItem) {
                return true;
            }
            if (itemStack2.getItem() instanceof DiggerItem) {
                DiggerItem diggerItem = (DiggerItem) itemStack.getItem();
                DiggerItem diggerItem2 = (DiggerItem) itemStack2.getItem();
                if (diggerItem.getAttackDamage() != diggerItem2.getAttackDamage()) {
                    return diggerItem.getAttackDamage() > diggerItem2.getAttackDamage();
                }
                return canReplaceEqualItem(itemStack, itemStack2);
            }
            return false;
        }
        return false;
    }

    public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.getDamageValue() < itemStack2.getDamageValue()) {
            return true;
        }
        if (!itemStack.hasTag() || itemStack2.hasTag()) {
            return itemStack.hasTag() && itemStack2.hasTag() && itemStack.getTag().getAllKeys().stream().anyMatch(str -> {
                return !str.equals("Damage");
            }) && !itemStack2.getTag().getAllKeys().stream().anyMatch(str2 -> {
                return !str2.equals("Damage");
            });
        }
        return true;
    }

    public boolean canHoldItem(ItemStack itemStack) {
        return true;
    }

    public boolean wantsToPickUp(ItemStack itemStack) {
        return canHoldItem(itemStack);
    }

    public boolean removeWhenFarAway(double d) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return isPassenger();
    }

    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && shouldDespawnInPeaceful()) {
            remove();
            return;
        }
        if (isPersistenceRequired() || requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Entity nearestPlayer = this.level.getNearestPlayer(this, -1.0d);
        if (nearestPlayer != null) {
            double distanceToSqr = nearestPlayer.distanceToSqr(this);
            int despawnDistance = getType().getCategory().getDespawnDistance();
            if (distanceToSqr > despawnDistance * despawnDistance && removeWhenFarAway(distanceToSqr)) {
                remove();
            }
            int noDespawnDistance = getType().getCategory().getNoDespawnDistance();
            int i = noDespawnDistance * noDespawnDistance;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && distanceToSqr > i && removeWhenFarAway(distanceToSqr)) {
                remove();
            } else if (distanceToSqr < i) {
                this.noActionTime = 0;
            }
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected final void serverAiStep() {
        this.noActionTime++;
        this.level.getProfiler().push("sensing");
        this.sensing.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("targetSelector");
        this.targetSelector.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("goalSelector");
        this.goalSelector.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("navigation");
        this.navigation.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("mob tick");
        customServerAiStep();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("controls");
        this.level.getProfiler().push("move");
        this.moveControl.tick();
        this.level.getProfiler().popPush("look");
        this.lookControl.tick();
        this.level.getProfiler().popPush("jump");
        this.jumpControl.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().pop();
        sendDebugPackets();
    }

    protected void sendDebugPackets() {
        DebugPackets.sendGoalSelector(this.level, this, this.goalSelector);
    }

    protected void customServerAiStep() {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity entity, float f, float f2) {
        double eyeY;
        double x = entity.getX() - getX();
        double z = entity.getZ() - getZ();
        if (entity instanceof LivingEntity) {
            eyeY = ((LivingEntity) entity).getEyeY() - getEyeY();
        } else {
            eyeY = ((entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0d) - getEyeY();
        }
        double sqrt = Mth.sqrt((x * x) + (z * z));
        float atan2 = ((float) (Mth.atan2(z, x) * 57.2957763671875d)) - 90.0f;
        this.xRot = rotlerp(this.xRot, (float) (-(Mth.atan2(eyeY, sqrt) * 57.2957763671875d)), f2);
        this.yRot = rotlerp(this.yRot, atan2, f);
    }

    private float rotlerp(float f, float f2, float f3) {
        float wrapDegrees = Mth.wrapDegrees(f2 - f);
        if (wrapDegrees > f3) {
            wrapDegrees = f3;
        }
        if (wrapDegrees < (-f3)) {
            wrapDegrees = -f3;
        }
        return f + wrapDegrees;
    }

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        BlockPos below = blockPos.below();
        return mobSpawnType == MobSpawnType.SPAWNER || levelAccessor.getBlockState(below).isValidSpawn(levelAccessor, below, entityType);
    }

    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType mobSpawnType) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return !levelReader.containsAnyLiquid(getBoundingBox()) && levelReader.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int i) {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public int getMaxFallDistance() {
        if (getTarget() == null) {
            return 3;
        }
        int health = ((int) (getHealth() - (getMaxHealth() * 0.33f))) - ((3 - this.level.getDifficulty().getId()) * 4);
        if (health < 0) {
            health = 0;
        }
        return health + 3;
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
                this.handItems.set(equipmentSlot.getIndex(), itemStack);
                break;
            case ARMOR:
                this.armorItems.set(equipmentSlot.getIndex(), itemStack);
                break;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean z) {
        super.dropCustomDeathLoot(damageSource, i, z);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemBySlot = getItemBySlot(equipmentSlot);
            float equipmentDropChance = getEquipmentDropChance(equipmentSlot);
            boolean z2 = equipmentDropChance > 1.0f;
            if (!itemBySlot.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemBySlot) && ((z || z2) && Math.max(this.random.nextFloat() - (i * 0.01f), 0.0f) < equipmentDropChance)) {
                if (!z2 && itemBySlot.isDamageableItem()) {
                    itemBySlot.setDamageValue(itemBySlot.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemBySlot.getMaxDamage() - 3, 1))));
                }
                spawnAtLocation(itemBySlot);
                setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
    }

    protected float getEquipmentDropChance(EquipmentSlot equipmentSlot) {
        float f;
        switch (equipmentSlot.getType()) {
            case HAND:
                f = this.handDropChances[equipmentSlot.getIndex()];
                break;
            case ARMOR:
                f = this.armorDropChances[equipmentSlot.getIndex()];
                break;
            default:
                f = 0.0f;
                break;
        }
        return f;
    }

    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        Item equipmentForSlot;
        if (this.random.nextFloat() < 0.15f * difficultyInstance.getSpecialMultiplier()) {
            int nextInt = this.random.nextInt(2);
            float f = this.level.getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            if (this.random.nextFloat() < 0.095f) {
                nextInt++;
            }
            if (this.random.nextFloat() < 0.095f) {
                nextInt++;
            }
            if (this.random.nextFloat() < 0.095f) {
                nextInt++;
            }
            boolean z = true;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack itemBySlot = getItemBySlot(equipmentSlot);
                    if (z || this.random.nextFloat() >= f) {
                        z = false;
                        if (itemBySlot.isEmpty() && (equipmentForSlot = getEquipmentForSlot(equipmentSlot, nextInt)) != null) {
                            setItemSlot(equipmentSlot, new ItemStack(equipmentForSlot));
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public static EquipmentSlot getEquipmentSlotForItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == Blocks.CARVED_PUMPKIN.asItem() || ((item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock))) {
            return EquipmentSlot.HEAD;
        }
        if (item instanceof ArmorItem) {
            return ((ArmorItem) item).getSlot();
        }
        if (item == Items.ELYTRA) {
            return EquipmentSlot.CHEST;
        }
        if (item == Items.SHIELD) {
            return EquipmentSlot.OFFHAND;
        }
        return EquipmentSlot.MAINHAND;
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int i) {
        switch (equipmentSlot) {
            case HEAD:
                if (i == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (i == 1) {
                    return Items.GOLDEN_HELMET;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (i == 3) {
                    return Items.IRON_HELMET;
                }
                if (i == 4) {
                    return Items.DIAMOND_HELMET;
                }
            case CHEST:
                if (i == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (i == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (i == 3) {
                    return Items.IRON_CHESTPLATE;
                }
                if (i == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            case LEGS:
                if (i == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (i == 1) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (i == 3) {
                    return Items.IRON_LEGGINGS;
                }
                if (i == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            case FEET:
                if (i == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (i == 1) {
                    return Items.GOLDEN_BOOTS;
                }
                if (i == 2) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (i == 3) {
                    return Items.IRON_BOOTS;
                }
                if (i == 4) {
                    return Items.DIAMOND_BOOTS;
                }
                return null;
            default:
                return null;
        }
    }

    protected void populateDefaultEquipmentEnchantments(DifficultyInstance difficultyInstance) {
        float specialMultiplier = difficultyInstance.getSpecialMultiplier();
        enchantSpawnedWeapon(specialMultiplier);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
                enchantSpawnedArmor(specialMultiplier, equipmentSlot);
            }
        }
    }

    protected void enchantSpawnedWeapon(float f) {
        if (!getMainHandItem().isEmpty() && this.random.nextFloat() < 0.25f * f) {
            setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(this.random, getMainHandItem(), (int) (5.0f + (f * this.random.nextInt(18))), false));
        }
    }

    protected void enchantSpawnedArmor(float f, EquipmentSlot equipmentSlot) {
        ItemStack itemBySlot = getItemBySlot(equipmentSlot);
        if (!itemBySlot.isEmpty() && this.random.nextFloat() < 0.5f * f) {
            setItemSlot(equipmentSlot, EnchantmentHelper.enchantItem(this.random, itemBySlot, (int) (5.0f + (f * this.random.nextInt(18))), false));
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05d, AttributeModifier.Operation.MULTIPLY_BASE));
        if (this.random.nextFloat() < 0.05f) {
            setLeftHanded(true);
        } else {
            setLeftHanded(false);
        }
        return spawnGroupData;
    }

    public boolean canBeControlledByRider() {
        return false;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    public void setDropChance(EquipmentSlot equipmentSlot, float f) {
        switch (equipmentSlot.getType()) {
            case HAND:
                this.handDropChances[equipmentSlot.getIndex()] = f;
                break;
            case ARMOR:
                this.armorDropChances[equipmentSlot.getIndex()] = f;
                break;
        }
    }

    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean z) {
        this.canPickUpLoot = z;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canTakeItem(ItemStack itemStack) {
        return getItemBySlot(getEquipmentSlotForItem(itemStack)).isEmpty() && canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override // net.minecraft.world.entity.Entity
    public final InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!isAlive()) {
            return InteractionResult.PASS;
        }
        if (getLeashHolder() == player) {
            dropLeash(true, !player.abilities.instabuild);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        InteractionResult checkAndHandleImportantInteractions = checkAndHandleImportantInteractions(player, interactionHand);
        if (checkAndHandleImportantInteractions.consumesAction()) {
            return checkAndHandleImportantInteractions;
        }
        InteractionResult mobInteract = mobInteract(player, interactionHand);
        if (mobInteract.consumesAction()) {
            return mobInteract;
        }
        return super.interact(player, interactionHand);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.LEAD && canBeLeashed(player)) {
            setLeashedTo(player, true);
            itemInHand.shrink(1);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (itemInHand.getItem() == Items.NAME_TAG) {
            InteractionResult interactLivingEntity = itemInHand.interactLivingEntity(player, this, interactionHand);
            if (interactLivingEntity.consumesAction()) {
                return interactLivingEntity;
            }
        }
        if (itemInHand.getItem() instanceof SpawnEggItem) {
            if (this.level instanceof ServerLevel) {
                Optional<Mob> spawnOffspringFromSpawnEgg = ((SpawnEggItem) itemInHand.getItem()).spawnOffspringFromSpawnEgg(player, this, (EntityType<? extends Mob>) getType(), (ServerLevel) this.level, position(), itemInHand);
                spawnOffspringFromSpawnEgg.ifPresent(mob -> {
                    onOffspringSpawnedFromEgg(player, mob);
                });
                return spawnOffspringFromSpawnEgg.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
    }

    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public boolean isWithinRestriction() {
        return isWithinRestriction(blockPosition());
    }

    public boolean isWithinRestriction(BlockPos blockPos) {
        return this.restrictRadius == -1.0f || this.restrictCenter.distSqr(blockPos) < ((double) (this.restrictRadius * this.restrictRadius));
    }

    public void restrictTo(BlockPos blockPos, int i) {
        this.restrictCenter = blockPos;
        this.restrictRadius = i;
    }

    public BlockPos getRestrictCenter() {
        return this.restrictCenter;
    }

    public float getRestrictRadius() {
        return this.restrictRadius;
    }

    public boolean hasRestriction() {
        return this.restrictRadius != -1.0f;
    }

    @Nullable
    public <T extends Mob> T convertTo(EntityType<T> entityType, boolean z) {
        if (this.removed) {
            return null;
        }
        T create = entityType.create(this.level);
        create.copyPosition(this);
        create.setBaby(isBaby());
        create.setNoAi(isNoAi());
        if (hasCustomName()) {
            create.setCustomName(getCustomName());
            create.setCustomNameVisible(isCustomNameVisible());
        }
        if (isPersistenceRequired()) {
            create.setPersistenceRequired();
        }
        create.setInvulnerable(isInvulnerable());
        if (z) {
            create.setCanPickUpLoot(canPickUpLoot());
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemBySlot = getItemBySlot(equipmentSlot);
                if (!itemBySlot.isEmpty()) {
                    create.setItemSlot(equipmentSlot, itemBySlot.copy());
                    create.setDropChance(equipmentSlot, getEquipmentDropChance(equipmentSlot));
                    itemBySlot.setCount(0);
                }
            }
        }
        this.level.addFreshEntity(create);
        if (isPassenger()) {
            Entity vehicle = getVehicle();
            stopRiding();
            create.startRiding(vehicle, true);
        }
        remove();
        return create;
    }

    protected void tickLeash() {
        if (this.leashInfoTag != null) {
            restoreLeashFromSave();
        }
        if (this.leashHolder == null) {
            return;
        }
        if (!isAlive() || !this.leashHolder.isAlive()) {
            dropLeash(true, true);
        }
    }

    public void dropLeash(boolean z, boolean z2) {
        if (this.leashHolder != null) {
            this.forcedLoading = false;
            if (!(this.leashHolder instanceof Player)) {
                this.leashHolder.forcedLoading = false;
            }
            this.leashHolder = null;
            this.leashInfoTag = null;
            if (!this.level.isClientSide && z2) {
                spawnAtLocation(Items.LEAD);
            }
            if (!this.level.isClientSide && z && (this.level instanceof ServerLevel)) {
                ((ServerLevel) this.level).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, null));
            }
        }
    }

    public boolean canBeLeashed(Player player) {
        return (isLeashed() || (this instanceof Enemy)) ? false : true;
    }

    public boolean isLeashed() {
        return this.leashHolder != null;
    }

    @Nullable
    public Entity getLeashHolder() {
        if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level.isClientSide) {
            this.leashHolder = this.level.getEntity(this.delayedLeashHolderId);
        }
        return this.leashHolder;
    }

    public void setLeashedTo(Entity entity, boolean z) {
        this.leashHolder = entity;
        this.leashInfoTag = null;
        this.forcedLoading = true;
        if (!(this.leashHolder instanceof Player)) {
            this.leashHolder.forcedLoading = true;
        }
        if (!this.level.isClientSide && z && (this.level instanceof ServerLevel)) {
            ((ServerLevel) this.level).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
        }
        if (isPassenger()) {
            stopRiding();
        }
    }

    public void setDelayedLeashHolderId(int i) {
        this.delayedLeashHolderId = i;
        dropLeash(false, false);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean startRiding(Entity entity, boolean z) {
        boolean startRiding = super.startRiding(entity, z);
        if (startRiding && isLeashed()) {
            dropLeash(true, true);
        }
        return startRiding;
    }

    private void restoreLeashFromSave() {
        if (this.leashInfoTag != null && (this.level instanceof ServerLevel)) {
            if (this.leashInfoTag.hasUUID("UUID")) {
                Entity entity = ((ServerLevel) this.level).getEntity(this.leashInfoTag.getUUID("UUID"));
                if (entity != null) {
                    setLeashedTo(entity, true);
                    return;
                }
            } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
                setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(this.level, new BlockPos(this.leashInfoTag.getInt("X"), this.leashInfoTag.getInt("Y"), this.leashInfoTag.getInt("Z"))), true);
                return;
            }
            if (this.tickCount > 100) {
                spawnAtLocation(Items.LEAD);
                this.leashInfoTag = null;
            }
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
        if (itemStack.isEmpty() || isValidSlotForItem(equipmentSlot, itemStack) || equipmentSlot == EquipmentSlot.HEAD) {
            setItemSlot(equipmentSlot, itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isControlledByLocalInstance() {
        return canBeControlledByRider() && super.isControlledByLocalInstance();
    }

    public static boolean isValidSlotForItem(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        EquipmentSlot equipmentSlotForItem = getEquipmentSlotForItem(itemStack);
        return equipmentSlotForItem == equipmentSlot || (equipmentSlotForItem == EquipmentSlot.MAINHAND && equipmentSlot == EquipmentSlot.OFFHAND) || (equipmentSlotForItem == EquipmentSlot.OFFHAND && equipmentSlot == EquipmentSlot.MAINHAND);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !isNoAi();
    }

    public void setNoAi(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue();
        this.entityData.set(DATA_MOB_FLAGS_ID, Byte.valueOf(z ? (byte) (byteValue | 1) : (byte) (byteValue & (-2))));
    }

    public void setLeftHanded(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue();
        this.entityData.set(DATA_MOB_FLAGS_ID, Byte.valueOf(z ? (byte) (byteValue | 2) : (byte) (byteValue & (-3))));
    }

    public void setAggressive(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue();
        this.entityData.set(DATA_MOB_FLAGS_ID, Byte.valueOf(z ? (byte) (byteValue | 4) : (byte) (byteValue & (-5))));
    }

    public boolean isNoAi() {
        return (((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue() & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue() & 2) != 0;
    }

    public boolean isAggressive() {
        return (((Byte) this.entityData.get(DATA_MOB_FLAGS_ID)).byteValue() & 4) != 0;
    }

    public void setBaby(boolean z) {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public HumanoidArm getMainArm() {
        return isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean canAttack(LivingEntity livingEntity) {
        if (livingEntity.getType() == EntityType.PLAYER && ((Player) livingEntity).abilities.invulnerable) {
            return false;
        }
        return super.canAttack(livingEntity);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean doHurtTarget(Entity entity) {
        float attributeValue = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        float attributeValue2 = (float) getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (entity instanceof LivingEntity) {
            attributeValue += EnchantmentHelper.getDamageBonus(getMainHandItem(), ((LivingEntity) entity).getMobType());
            attributeValue2 += EnchantmentHelper.getKnockbackBonus(this);
        }
        int fireAspect = EnchantmentHelper.getFireAspect(this);
        if (fireAspect > 0) {
            entity.setSecondsOnFire(fireAspect * 4);
        }
        boolean hurt = entity.hurt(DamageSource.mobAttack(this), attributeValue);
        if (hurt) {
            if (attributeValue2 > 0.0f && (entity instanceof LivingEntity)) {
                ((LivingEntity) entity).knockback(attributeValue2 * 0.5f, Mth.sin(this.yRot * 0.017453292f), -Mth.cos(this.yRot * 0.017453292f));
                setDeltaMovement(getDeltaMovement().multiply(0.6d, 1.0d, 0.6d));
            }
            if (entity instanceof Player) {
                Player player = (Player) entity;
                maybeDisableShield(player, getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
            }
            doEnchantDamageEffects(this, entity);
            setLastHurtMob(entity);
        }
        return hurt;
    }

    private void maybeDisableShield(Player player, ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack.isEmpty() && !itemStack2.isEmpty() && (itemStack.getItem() instanceof AxeItem) && itemStack2.getItem() == Items.SHIELD) {
            if (this.random.nextFloat() < 0.25f + (EnchantmentHelper.getBlockEfficiency(this) * 0.05f)) {
                player.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level.broadcastEntityEvent(player, (byte) 30);
            }
        }
    }

    protected boolean isSunBurnTick() {
        if (this.level.isDay() && !this.level.isClientSide) {
            float brightness = getBrightness();
            BlockPos above = getVehicle() instanceof Boat ? new BlockPos(getX(), Math.round(getY()), getZ()).above() : new BlockPos(getX(), Math.round(getY()), getZ());
            if (brightness > 0.5f && this.random.nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f && this.level.canSeeSky(above)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected void jumpInLiquid(Tag<Fluid> tag) {
        if (getNavigation().canFloat()) {
            super.jumpInLiquid(tag);
        } else {
            setDeltaMovement(getDeltaMovement().add(0.0d, 0.3d, 0.0d));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        dropLeash(true, false);
    }
}
