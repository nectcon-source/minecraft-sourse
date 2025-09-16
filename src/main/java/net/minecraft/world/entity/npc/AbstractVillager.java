package net.minecraft.world.entity.npc;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/AbstractVillager.class */
public abstract class AbstractVillager extends AgableMob implements Npc, Merchant {
    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);

    @Nullable
    private Player tradingPlayer;

    @Nullable
    protected MerchantOffers offers;
    private final SimpleContainer inventory;

    protected abstract void rewardTradeXp(MerchantOffer merchantOffer);

    protected abstract void updateTrades();

    public AbstractVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(8);
        setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0f);
        setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0f);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(false);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public int getUnhappyCounter() {
        return ((Integer) this.entityData.get(DATA_UNHAPPY_COUNTER)).intValue();
    }

    public void setUnhappyCounter(int i) {
        this.entityData.set(DATA_UNHAPPY_COUNTER, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public int getVillagerXp() {
        return 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        if (isBaby()) {
            return 0.81f;
        }
        return 1.62f;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    @Nullable
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            updateTrades();
        }
        return this.offers;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void overrideOffers(@Nullable MerchantOffers merchantOffers) {
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void overrideXp(int i) {
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void notifyTrade(MerchantOffer merchantOffer) {
        merchantOffer.increaseUses();
        this.ambientSoundTime = -getAmbientSoundInterval();
        rewardTradeXp(merchantOffer);
        if (this.tradingPlayer instanceof ServerPlayer) {
            CriteriaTriggers.TRADE.trigger((ServerPlayer) this.tradingPlayer, this, merchantOffer.getResult());
        }
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public boolean showProgressBar() {
        return true;
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public void notifyTradeUpdated(ItemStack itemStack) {
        if (!this.level.isClientSide && this.ambientSoundTime > (-getAmbientSoundInterval()) + 20) {
            this.ambientSoundTime = -getAmbientSoundInterval();
            playSound(getTradeUpdatedSound(!itemStack.isEmpty()), getSoundVolume(), getVoicePitch());
        }
    }

    @Override // net.minecraft.world.item.trading.Merchant
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean z) {
        return z ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    public void playCelebrateSound() {
        playSound(SoundEvents.VILLAGER_CELEBRATE, getSoundVolume(), getVoicePitch());
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        MerchantOffers offers = getOffers();
        if (!offers.isEmpty()) {
            compoundTag.put("Offers", offers.createTag());
        }
        compoundTag.put("Inventory", this.inventory.createTag());
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Offers", 10)) {
            this.offers = new MerchantOffers(compoundTag.getCompound("Offers"));
        }
        this.inventory.fromTag(compoundTag.getList("Inventory", 10));
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        stopTrading();
        return super.changeDimension(serverLevel);
    }

    protected void stopTrading() {
        setTradingPlayer(null);
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        stopTrading();
    }

    protected void addParticlesAroundSelf(ParticleOptions particleOptions) {
        for (int i = 0; i < 5; i++) {
            this.level.addParticle(particleOptions, getRandomX(1.0d), getRandomY() + 1.0d, getRandomZ(1.0d), this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d, this.random.nextGaussian() * 0.02d);
        }
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean canBeLeashed(Player player) {
        return false;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
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

    @Override // net.minecraft.world.item.trading.Merchant
    public Level getLevel() {
        return this.level;
    }

    protected void addOffersFromItemListings(MerchantOffers merchantOffers, VillagerTrades.ItemListing[] itemListingArr, int i) {
        Set<Integer> newHashSet = Sets.newHashSet();
        if (itemListingArr.length > i) {
            while (newHashSet.size() < i) {
                newHashSet.add(Integer.valueOf(this.random.nextInt(itemListingArr.length)));
            }
        } else {
            for (int i2 = 0; i2 < itemListingArr.length; i2++) {
                newHashSet.add(Integer.valueOf(i2));
            }
        }
        Iterator<Integer> it = newHashSet.iterator();
        while (it.hasNext()) {
            MerchantOffer offer = itemListingArr[it.next().intValue()].getOffer(this, this.random);
            if (offer != null) {
                merchantOffers.add(offer);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getRopeHoldPosition(float f) {
        float lerp = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * 0.017453292f;
        return getPosition(f).add(new Vec3(0.0d, getBoundingBox().getYsize() - 1.0d, 0.2d).yRot(-lerp));
    }
}
