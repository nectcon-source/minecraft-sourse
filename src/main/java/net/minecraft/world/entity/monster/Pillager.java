package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Pillager.class */
public class Pillager extends AbstractIllager implements CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
    private final SimpleContainer inventory;

    public Pillager(EntityType<? extends Pillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(5);
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0f));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal(this, 1.0d, 8.0f));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6d));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.3499999940395355d).add(Attributes.MAX_HEALTH, 24.0d).add(Attributes.ATTACK_DAMAGE, 5.0d).add(Attributes.FOLLOW_RANGE, 32.0d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return projectileWeaponItem == Items.CROSSBOW;
    }

    public boolean isChargingCrossbow() {
        return ((Boolean) this.entityData.get(IS_CHARGING_CROSSBOW)).booleanValue();
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void setChargingCrossbow(boolean z) {
        this.entityData.set(IS_CHARGING_CROSSBOW, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (!item.isEmpty()) {
                listTag.add(item.save(new CompoundTag()));
            }
        }
        compoundTag.put("Inventory", listTag);
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (isChargingCrossbow()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        }
        if (isHolding(Items.CROSSBOW)) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        }
        if (isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        }
        return AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        ListTag list = compoundTag.getList("Inventory", 10);
        for (int i = 0; i < list.size(); i++) {
            ItemStack m66of = ItemStack.of(list.getCompound(i));
            if (!m66of.isEmpty()) {
                this.inventory.addItem(m66of);
            }
        }
        setCanPickUpLoot(true);
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        BlockState blockState = levelReader.getBlockState(blockPos.below());
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.SAND)) {
            return 10.0f;
        }
        return 0.5f - levelReader.getBrightness(blockPos);
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        populateDefaultEquipmentSlots(difficultyInstance);
        populateDefaultEquipmentEnchantments(difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override // net.minecraft.world.entity.Mob
    protected void enchantSpawnedWeapon(float f) {
        super.enchantSpawnedWeapon(f);
        if (this.random.nextInt(300) == 0) {
            ItemStack mainHandItem = getMainHandItem();
            if (mainHandItem.getItem() == Items.CROSSBOW) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(mainHandItem);
                enchantments.putIfAbsent(Enchantments.PIERCING, 1);
                EnchantmentHelper.setEnchantments(enchantments, mainHandItem);
                setItemSlot(EquipmentSlot.MAINHAND, mainHandItem);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }
        return (entity instanceof LivingEntity) && ((LivingEntity) entity).getMobType() == MobType.ILLAGER && getTeam() == null && entity.getTeam() == null;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        performCrossbowAttack(this, 1.6f);
    }

    @Override // net.minecraft.world.entity.monster.CrossbowAttackMob
    public void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f) {
        shootCrossbowProjectile(this, livingEntity, projectile, f, 1.6f);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (item.getItem() instanceof BannerItem) {
            super.pickUpItem(itemEntity);
            return;
        }
        if (wantsItem(item.getItem())) {
            onItemPickup(itemEntity);
            ItemStack addItem = this.inventory.addItem(item);
            if (addItem.isEmpty()) {
                itemEntity.remove();
            } else {
                item.setCount(addItem.getCount());
            }
        }
    }

    private boolean wantsItem(Item item) {
        return hasActiveRaid() && item == Items.WHITE_BANNER;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.Entity
    public boolean setSlot(int i, ItemStack itemStack) {
        if (super.setSlot(i, itemStack)) {
            return true;
        }
        int i2 = i - 300;
        if (i2 >= 0 && i2 < this.inventory.getContainerSize()) {
            this.inventory.setItem(i2, itemStack);
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
        Raid currentRaid = getCurrentRaid();
        if (this.random.nextFloat() <= currentRaid.getEnchantOdds()) {
            ItemStack itemStack = new ItemStack(Items.CROSSBOW);
            Map<Enchantment, Integer> newHashMap = Maps.newHashMap();
            if (i > currentRaid.getNumGroups(Difficulty.NORMAL)) {
                newHashMap.put(Enchantments.QUICK_CHARGE, 2);
            } else if (i > currentRaid.getNumGroups(Difficulty.EASY)) {
                newHashMap.put(Enchantments.QUICK_CHARGE, 1);
            }
            newHashMap.put(Enchantments.MULTISHOT, 1);
            EnchantmentHelper.setEnchantments(newHashMap, itemStack);
            setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        }
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}
