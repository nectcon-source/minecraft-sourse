package net.minecraft.world.entity.npc;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/WanderingTrader.class */
public class WanderingTrader extends AbstractVillager {

    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    public WanderingTrader(EntityType<? extends WanderingTrader> entityType, Level level) {
        super(entityType, level);
        this.forcedLoading = true;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
//        this.goalSelector.addGoal(0, new UseItemGoal(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, wanderingTrader -> {
//            return this.level.isNight() && !wanderingTrader.isInvisible();
//        }));
//        this.goalSelector.addGoal(0, new UseItemGoal(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, wanderingTrader2 -> {
//            return this.level.isDay() && wanderingTrader2.isInvisible();
//        }));
        // Явно указываем тип WanderingTrader для лямбда-параметра
        this.goalSelector.addGoal(0, new UseItemGoal<>(this,
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY),
                SoundEvents.WANDERING_TRADER_DISAPPEARED,
                (WanderingTrader trader) -> this.level.isNight() && !trader.isInvisible()
        ));

        this.goalSelector.addGoal(0, new UseItemGoal<>(this,
                new ItemStack(Items.MILK_BUCKET),
                SoundEvents.WANDERING_TRADER_REAPPEARED,
                (WanderingTrader trader) -> this.level.isDay() && trader.isInvisible()
        ));//
//
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Zombie.class, 8.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Evoker.class, 12.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Vindicator.class, 8.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Vex.class, 8.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Pillager.class, 15.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Illusioner.class, 12.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new AvoidEntityGoal(this, Zoglin.class, 10.0f, 0.5d, 0.5d));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5d));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderToPositionGoal(this, 2.0d, 0.35d));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35d));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35d));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
    }

    @Override // net.minecraft.world.entity.AgableMob
    @Nullable
    public AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        return null;
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.item.trading.Merchant
    public boolean showProgressBar() {
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (player.getItemInHand(interactionHand).getItem() != Items.VILLAGER_SPAWN_EGG && isAlive() && !isTrading() && !isBaby()) {
            if (interactionHand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (getOffers().isEmpty()) {
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (!this.level.isClientSide) {
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected void updateTrades() {
        VillagerTrades.ItemListing[] itemListingArr = (VillagerTrades.ItemListing[]) VillagerTrades.WANDERING_TRADER_TRADES.get(1);
        VillagerTrades.ItemListing[] itemListingArr2 = (VillagerTrades.ItemListing[]) VillagerTrades.WANDERING_TRADER_TRADES.get(2);
        if (itemListingArr == null || itemListingArr2 == null) {
            return;
        }
        MerchantOffers offers = getOffers();
        addOffersFromItemListings(offers, itemListingArr, 5);
        MerchantOffer offer = itemListingArr2[this.random.nextInt(itemListingArr2.length)].getOffer(this, this.random);
        if (offer != null) {
            offers.add(offer);
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DespawnDelay", this.despawnDelay);
        if (this.wanderTarget != null) {
            compoundTag.put("WanderTarget", NbtUtils.writeBlockPos(this.wanderTarget));
        }
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("DespawnDelay", 99)) {
            this.despawnDelay = compoundTag.getInt("DespawnDelay");
        }
        if (compoundTag.contains("WanderTarget")) {
            this.wanderTarget = NbtUtils.readBlockPos(compoundTag.getCompound("WanderTarget"));
        }
        setAge(Math.max(0, getAge()));
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected void rewardTradeXp(MerchantOffer merchantOffer) {
        if (merchantOffer.shouldRewardExp()) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, getX(), getY() + 0.5d, getZ(), 3 + this.random.nextInt(4)));
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        if (isTrading()) {
            return SoundEvents.WANDERING_TRADER_TRADE;
        }
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDrinkingSound(ItemStack itemStack) {
        if (itemStack.getItem() == Items.MILK_BUCKET) {
            return SoundEvents.WANDERING_TRADER_DRINK_MILK;
        }
        return SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager
    protected SoundEvent getTradeUpdatedSound(boolean z) {
        return z ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override // net.minecraft.world.entity.npc.AbstractVillager, net.minecraft.world.item.trading.Merchant
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int i) {
        this.despawnDelay = i;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay <= 0 || isTrading()) {
            return;
        }
        int i = this.despawnDelay - 1;
        this.despawnDelay = i;
        if (i == 0) {
            remove();
        }
    }

    public void setWanderTarget(@Nullable BlockPos blockPos) {
        this.wanderTarget = blockPos;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/npc/WanderingTrader$WanderToPositionGoal.class */
    class WanderToPositionGoal extends Goal {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(WanderingTrader wanderingTrader, double d, double d2) {
            this.trader = wanderingTrader;
            this.stopDistance = d;
            this.speedModifier = d2;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.trader.setWanderTarget(null);
            WanderingTrader.this.navigation.stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            BlockPos wanderTarget = this.trader.getWanderTarget();
            return wanderTarget != null && isTooFarAway(wanderTarget, this.stopDistance);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            BlockPos wanderTarget = this.trader.getWanderTarget();
            if (wanderTarget != null && WanderingTrader.this.navigation.isDone()) {
                if (!isTooFarAway(wanderTarget, 10.0d)) {
                    WanderingTrader.this.navigation.moveTo(wanderTarget.getX(), wanderTarget.getY(), wanderTarget.getZ(), this.speedModifier);
                } else {
                    Vec3 add = new Vec3(wanderTarget.getX() - this.trader.getX(), wanderTarget.getY() - this.trader.getY(), wanderTarget.getZ() - this.trader.getZ()).normalize().scale(10.0d).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo(add.x, add.y, add.z, this.speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos blockPos, double d) {
            return !blockPos.closerThan(this.trader.position(), d);
        }
    }
}
