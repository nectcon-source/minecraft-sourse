package net.minecraft.world.entity.monster;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/ZombieVillager.class */
public class ZombieVillager extends Zombie implements VillagerDataHolder {
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(ZombieVillager.class, EntityDataSerializers.VILLAGER_DATA);
    private int villagerConversionTime;
    private UUID conversionStarter;
    private Tag gossips;
    private CompoundTag tradeOffers;
    private int villagerXp;

    public ZombieVillager(EntityType<? extends ZombieVillager> entityType, Level level) {
        super(entityType, level);
        setVillagerData(getVillagerData().setProfession(Registry.VILLAGER_PROFESSION.getRandom(this.random)));
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CONVERTING_ID, false);
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        DataResult encodeStart = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, getVillagerData());
        Logger logger = LOGGER;
        logger.getClass();
        encodeStart.resultOrPartial(logger::error).ifPresent(tag -> {
            compoundTag.put("VillagerData", (Tag) tag);
        });
        if (this.tradeOffers != null) {
            compoundTag.put("Offers", this.tradeOffers);
        }
        if (this.gossips != null) {
            compoundTag.put("Gossips", this.gossips);
        }
        compoundTag.putInt("ConversionTime", isConverting() ? this.villagerConversionTime : -1);
        if (this.conversionStarter != null) {
            compoundTag.putUUID("ConversionPlayer", this.conversionStarter);
        }
        compoundTag.putInt("Xp", this.villagerXp);
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("VillagerData", 10)) {
            DataResult<VillagerData> parse = VillagerData.CODEC.parse(new Dynamic(NbtOps.INSTANCE, compoundTag.get("VillagerData")));
            Logger logger = LOGGER;
            logger.getClass();
            parse.resultOrPartial(logger::error).ifPresent(this::setVillagerData);
        }
        if (compoundTag.contains("Offers", 10)) {
            this.tradeOffers = compoundTag.getCompound("Offers");
        }
        if (compoundTag.contains("Gossips", 10)) {
            this.gossips = compoundTag.getList("Gossips", 10);
        }
        if (compoundTag.contains("ConversionTime", 99) && compoundTag.getInt("ConversionTime") > -1) {
            startConverting(compoundTag.hasUUID("ConversionPlayer") ? compoundTag.getUUID("ConversionPlayer") : null, compoundTag.getInt("ConversionTime"));
        }
        if (compoundTag.contains("Xp", 3)) {
            this.villagerXp = compoundTag.getInt("Xp");
        }
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        if (!this.level.isClientSide && isAlive() && isConverting()) {
            this.villagerConversionTime -= getConversionProgress();
            if (this.villagerConversionTime <= 0) {
                finishConversion((ServerLevel) this.level);
            }
        }
        super.tick();
    }

    @Override // net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.GOLDEN_APPLE) {
            if (hasEffect(MobEffects.WEAKNESS)) {
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
                if (!this.level.isClientSide) {
                    startConverting(player.getUUID(), this.random.nextInt(2401) + 3600);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected boolean convertsInWater() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !isConverting() && this.villagerXp == 0;
    }

    public boolean isConverting() {
        return ((Boolean) getEntityData().get(DATA_CONVERTING_ID)).booleanValue();
    }

    private void startConverting(@Nullable UUID uuid, int i) {
        this.conversionStarter = uuid;
        this.villagerConversionTime = i;
        getEntityData().set(DATA_CONVERTING_ID, true);
        removeEffect(MobEffects.WEAKNESS);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, i, Math.min(this.level.getDifficulty().getId() - 1, 0)));
        this.level.broadcastEntityEvent(this, (byte) 16);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 16) {
            if (!isSilent()) {
                this.level.playLocalSound(getX(), getEyeY(), getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, getSoundSource(), 1.0f + this.random.nextFloat(), (this.random.nextFloat() * 0.7f) + 0.3f, false);
                return;
            }
            return;
        }
        super.handleEntityEvent(b);
    }

    private void finishConversion(ServerLevel serverLevel) {
        Villager villager = (Villager) convertTo(EntityType.VILLAGER, false);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemBySlot = getItemBySlot(equipmentSlot);
            if (!itemBySlot.isEmpty()) {
                if (EnchantmentHelper.hasBindingCurse(itemBySlot)) {
                    villager.setSlot(equipmentSlot.getIndex() + 300, itemBySlot);
                } else if (getEquipmentDropChance(equipmentSlot) > 1.0d) {
                    spawnAtLocation(itemBySlot);
                }
            }
        }
        villager.setVillagerData(getVillagerData());
        if (this.gossips != null) {
            villager.setGossips(this.gossips);
        }
        if (this.tradeOffers != null) {
            villager.setOffers(new MerchantOffers(this.tradeOffers));
        }
        villager.setVillagerXp(this.villagerXp);
        villager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.CONVERSION, null, null);
        if (this.conversionStarter != null) {
            Player playerByUUID = serverLevel.getPlayerByUUID(this.conversionStarter);
            if (playerByUUID instanceof ServerPlayer) {
                CriteriaTriggers.CURED_ZOMBIE_VILLAGER.trigger((ServerPlayer) playerByUUID, this, villager);
                serverLevel.onReputationEvent(ReputationEventType.ZOMBIE_VILLAGER_CURED, playerByUUID, villager);
            }
        }
        villager.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        if (!isSilent()) {
            serverLevel.levelEvent(null, 1027, blockPosition(), 0);
        }
    }

    private int getConversionProgress() {
        int i = 1;
        if (this.random.nextFloat() < 0.01f) {
            int i2 = 0;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int x = ((int) getX()) - 4; x < ((int) getX()) + 4 && i2 < 14; x++) {
                for (int y = ((int) getY()) - 4; y < ((int) getY()) + 4 && i2 < 14; y++) {
                    for (int z = ((int) getZ()) - 4; z < ((int) getZ()) + 4 && i2 < 14; z++) {
                        Block block = this.level.getBlockState(mutableBlockPos.set(x, y, z)).getBlock();
                        if (block == Blocks.IRON_BARS || (block instanceof BedBlock)) {
                            if (this.random.nextFloat() < 0.3f) {
                                i++;
                            }
                            i2++;
                        }
                    }
                }
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.LivingEntity
    public float getVoicePitch() {
        if (isBaby()) {
            return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 2.0f;
        }
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f) + 1.0f;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    public SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    public SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_VILLAGER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    public SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_VILLAGER_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    public SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_VILLAGER_STEP;
    }

    @Override // net.minecraft.world.entity.monster.Zombie
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    public void setTradeOffers(CompoundTag compoundTag) {
        this.tradeOffers = compoundTag;
    }

    public void setGossips(Tag tag) {
        this.gossips = tag;
    }

    @Override // net.minecraft.world.entity.monster.Zombie, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setVillagerData(getVillagerData().setType(VillagerType.byBiome(serverLevelAccessor.getBiomeName(blockPosition()))));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public void setVillagerData(VillagerData villagerData) {
        if (getVillagerData().getProfession() != villagerData.getProfession()) {
            this.tradeOffers = null;
        }
        this.entityData.set(DATA_VILLAGER_DATA, villagerData);
    }

    @Override // net.minecraft.world.entity.npc.VillagerDataHolder
    public VillagerData getVillagerData() {
        return (VillagerData) this.entityData.get(DATA_VILLAGER_DATA);
    }

    public void setVillagerXp(int i) {
        this.villagerXp = i;
    }
}
