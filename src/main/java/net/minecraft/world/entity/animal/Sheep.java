package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/Sheep.class */
public class Sheep extends Animal implements Shearable {
    private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
    private static final Map<DyeColor, ItemLike> ITEM_BY_DYE =  Util.make(Maps.newEnumMap(DyeColor.class), enumMap -> {
        enumMap.put( DyeColor.WHITE,  Blocks.WHITE_WOOL);
        enumMap.put( DyeColor.ORANGE,  Blocks.ORANGE_WOOL);
        enumMap.put( DyeColor.MAGENTA,  Blocks.MAGENTA_WOOL);
        enumMap.put( DyeColor.LIGHT_BLUE,  Blocks.LIGHT_BLUE_WOOL);
        enumMap.put( DyeColor.YELLOW,  Blocks.YELLOW_WOOL);
        enumMap.put( DyeColor.LIME,  Blocks.LIME_WOOL);
        enumMap.put( DyeColor.PINK,  Blocks.PINK_WOOL);
        enumMap.put( DyeColor.GRAY,  Blocks.GRAY_WOOL);
        enumMap.put( DyeColor.LIGHT_GRAY,  Blocks.LIGHT_GRAY_WOOL);
        enumMap.put( DyeColor.CYAN,  Blocks.CYAN_WOOL);
        enumMap.put( DyeColor.PURPLE,  Blocks.PURPLE_WOOL);
        enumMap.put( DyeColor.BLUE,  Blocks.BLUE_WOOL);
        enumMap.put( DyeColor.BROWN,  Blocks.BROWN_WOOL);
        enumMap.put( DyeColor.GREEN,  Blocks.GREEN_WOOL);
        enumMap.put( DyeColor.RED,  Blocks.RED_WOOL);
        enumMap.put( DyeColor.BLACK,  Blocks.BLACK_WOOL);
    });
    private static final Map<DyeColor, float[]> COLORARRAY_BY_COLOR = Maps.newEnumMap((Map) Arrays.stream(DyeColor.values()).collect(Collectors.toMap(dyeColor -> {
        return dyeColor;
    }, Sheep::createSheepColor)));
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    private static float[] createSheepColor(DyeColor dyeColor) {
        if (dyeColor == DyeColor.WHITE) {
            return new float[]{0.9019608f, 0.9019608f, 0.9019608f};
        }
        float[] textureDiffuseColors = dyeColor.getTextureDiffuseColors();
        return new float[]{textureDiffuseColors[0] * 0.75f, textureDiffuseColors[1] * 0.75f, textureDiffuseColors[2] * 0.75f};
    }

    public static float[] getColorArray(DyeColor dyeColor) {
        return COLORARRAY_BY_COLOR.get(dyeColor);
    }

    public Sheep(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25d));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0d));
        this.goalSelector.addGoal(3, new TemptGoal((PathfinderMob) this, 1.1d, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1d));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0d));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        if (this.level.isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }
        super.aiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0d).add(Attributes.MOVEMENT_SPEED, 0.23000000417232513d);
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WOOL_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.Mob
    public ResourceLocation getDefaultLootTable() {
        if (isSheared()) {
            return getType().getDefaultLootTable();
        }
        switch (getColor()) {
            case WHITE:
            default:
                return BuiltInLootTables.SHEEP_WHITE;
            case ORANGE:
                return BuiltInLootTables.SHEEP_ORANGE;
            case MAGENTA:
                return BuiltInLootTables.SHEEP_MAGENTA;
            case LIGHT_BLUE:
                return BuiltInLootTables.SHEEP_LIGHT_BLUE;
            case YELLOW:
                return BuiltInLootTables.SHEEP_YELLOW;
            case LIME:
                return BuiltInLootTables.SHEEP_LIME;
            case PINK:
                return BuiltInLootTables.SHEEP_PINK;
            case GRAY:
                return BuiltInLootTables.SHEEP_GRAY;
            case LIGHT_GRAY:
                return BuiltInLootTables.SHEEP_LIGHT_GRAY;
            case CYAN:
                return BuiltInLootTables.SHEEP_CYAN;
            case PURPLE:
                return BuiltInLootTables.SHEEP_PURPLE;
            case BLUE:
                return BuiltInLootTables.SHEEP_BLUE;
            case BROWN:
                return BuiltInLootTables.SHEEP_BROWN;
            case GREEN:
                return BuiltInLootTables.SHEEP_GREEN;
            case RED:
                return BuiltInLootTables.SHEEP_RED;
            case BLACK:
                return BuiltInLootTables.SHEEP_BLACK;
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(b);
        }
    }

    public float getHeadEatPositionScale(float f) {
        if (this.eatAnimationTick <= 0) {
            return 0.0f;
        }
        if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0f;
        }
        if (this.eatAnimationTick < 4) {
            return (this.eatAnimationTick - f) / 4.0f;
        }
        return (-((this.eatAnimationTick - 40) - f)) / 4.0f;
    }

    public float getHeadEatAngleScale(float f) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            return 0.62831855f + (0.21991149f * Mth.sin((((this.eatAnimationTick - 4) - f) / 32.0f) * 28.7f));
        }
        if (this.eatAnimationTick > 0) {
            return 0.62831855f;
        }
        return this.xRot * 0.017453292f;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.SHEARS) {
            if (!this.level.isClientSide && readyForShearing()) {
                shear(SoundSource.PLAYERS);
                itemInHand.hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.Shearable
    public void shear(SoundSource soundSource) {
        this.level.playSound((Player) null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1.0f, 1.0f);
        setSheared(true);
        int nextInt = 1 + this.random.nextInt(3);
        for (int i = 0; i < nextInt; i++) {
            ItemEntity spawnAtLocation = spawnAtLocation(ITEM_BY_DYE.get(getColor()), 1);
            if (spawnAtLocation != null) {
                spawnAtLocation.setDeltaMovement(spawnAtLocation.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1f, this.random.nextFloat() * 0.05f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f));
            }
        }
    }

    @Override // net.minecraft.world.entity.Shearable
    public boolean readyForShearing() {
        return (!isAlive() || isSheared() || isBaby()) ? false : true;
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Sheared", isSheared());
        compoundTag.putByte("Color", (byte) getColor().getId());
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setSheared(compoundTag.getBoolean("Sheared"));
        setColor(DyeColor.byId(compoundTag.getByte("Color")));
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        playSound(SoundEvents.SHEEP_STEP, 0.15f, 1.0f);
    }

    public DyeColor getColor() {
        return DyeColor.byId(((Byte) this.entityData.get(DATA_WOOL_ID)).byteValue() & 15);
    }

    public void setColor(DyeColor dyeColor) {
        this.entityData.set(DATA_WOOL_ID, Byte.valueOf((byte) ((((Byte) this.entityData.get(DATA_WOOL_ID)).byteValue() & 240) | (dyeColor.getId() & 15))));
    }

    public boolean isSheared() {
        return (((Byte) this.entityData.get(DATA_WOOL_ID)).byteValue() & 16) != 0;
    }

    public void setSheared(boolean z) {
        byte byteValue = ((Byte) this.entityData.get(DATA_WOOL_ID)).byteValue();
        if (z) {
            this.entityData.set(DATA_WOOL_ID, Byte.valueOf((byte) (byteValue | 16)));
        } else {
            this.entityData.set(DATA_WOOL_ID, Byte.valueOf((byte) (byteValue & (-17))));
        }
    }

    public static DyeColor getRandomSheepColor(Random random) {
        int nextInt = random.nextInt(100);
        if (nextInt < 5) {
            return DyeColor.BLACK;
        }
        if (nextInt < 10) {
            return DyeColor.GRAY;
        }
        if (nextInt < 15) {
            return DyeColor.LIGHT_GRAY;
        }
        if (nextInt < 18) {
            return DyeColor.BROWN;
        }
        if (random.nextInt(500) == 0) {
            return DyeColor.PINK;
        }
        return DyeColor.WHITE;
    }

    @Override // net.minecraft.world.entity.AgableMob
    public Sheep getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        Sheep create = EntityType.SHEEP.create(serverLevel);
        create.setColor(getOffspringColor(this, (Sheep) agableMob));
        return create;
    }

    @Override // net.minecraft.world.entity.Mob
    public void ate() {
        setSheared(false);
        if (isBaby()) {
            ageUp(60);
        }
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setColor(getRandomSheepColor(serverLevelAccessor.getRandom()));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    private DyeColor getOffspringColor(Animal var1, Animal var2) {
        DyeColor var3 = ((Sheep)var1).getColor();
        DyeColor var4x = ((Sheep)var2).getColor();
        CraftingContainer var5xx = makeContainer(var3, var4x);
        return this.level
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, var5xx, this.level)
                .map(var1x -> var1x.assemble(var5xx))
                .map(ItemStack::getItem)
                .filter(DyeItem.class::isInstance)
                .map(DyeItem.class::cast)
                .map(DyeItem::getDyeColor)
                .orElseGet(() -> this.level.random.nextBoolean() ? var3 : var4x);
    }

    private static CraftingContainer makeContainer(DyeColor dyeColor, DyeColor dyeColor2) {
        CraftingContainer craftingContainer = new CraftingContainer(new AbstractContainerMenu(null, -1) { // from class: net.minecraft.world.entity.animal.Sheep.1
            @Override // net.minecraft.world.inventory.AbstractContainerMenu
            public boolean stillValid(Player player) {
                return false;
            }
        }, 2, 1);
        craftingContainer.setItem(0, new ItemStack(DyeItem.byColor(dyeColor)));
        craftingContainer.setItem(1, new ItemStack(DyeItem.byColor(dyeColor2)));
        return craftingContainer;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.95f * entityDimensions.height;
    }
}
