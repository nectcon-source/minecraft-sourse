package net.minecraft.world.entity.animal;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/TropicalFish.class */
public class TropicalFish extends AbstractSchoolingFish {
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(TropicalFish.class, EntityDataSerializers.INT);
    private static final ResourceLocation[] BASE_TEXTURE_LOCATIONS = {new ResourceLocation("textures/entity/fish/tropical_a.png"), new ResourceLocation("textures/entity/fish/tropical_b.png")};
    private static final ResourceLocation[] PATTERN_A_TEXTURE_LOCATIONS = {new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png"), new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png"), new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png"), new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png"), new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png"), new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png")};
    private static final ResourceLocation[] PATTERN_B_TEXTURE_LOCATIONS = {new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png"), new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png"), new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png"), new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png"), new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png"), new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png")};
    public static final int[] COMMON_VARIANTS = {calculateVariant(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), calculateVariant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), calculateVariant(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), calculateVariant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), calculateVariant(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), calculateVariant(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), calculateVariant(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), calculateVariant(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), calculateVariant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), calculateVariant(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), calculateVariant(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), calculateVariant(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), calculateVariant(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), calculateVariant(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), calculateVariant(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), calculateVariant(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), calculateVariant(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), calculateVariant(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), calculateVariant(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), calculateVariant(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), calculateVariant(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), calculateVariant(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)};
    private boolean isSchool;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/TropicalFish$Pattern.class */
    enum Pattern {
        KOB(0, 0),
        SUNSTREAK(0, 1),
        SNOOPER(0, 2),
        DASHER(0, 3),
        BRINELY(0, 4),
        SPOTTY(0, 5),
        FLOPPER(1, 0),
        STRIPEY(1, 1),
        GLITTER(1, 2),
        BLOCKFISH(1, 3),
        BETTY(1, 4),
        CLAYFISH(1, 5);

        private final int base;
        private final int index;
        private static final Pattern[] VALUES = values();

        Pattern(int i, int i2) {
            this.base = i;
            this.index = i2;
        }

        public int getBase() {
            return this.base;
        }

        public int getIndex() {
            return this.index;
        }

        public static String getPatternName(int i, int i2) {
            return VALUES[i2 + (6 * i)].getName();
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static int calculateVariant(Pattern pattern, DyeColor dyeColor, DyeColor dyeColor2) {
        return (pattern.getBase() & 255) | ((pattern.getIndex() & 255) << 8) | ((dyeColor.getId() & 255) << 16) | ((dyeColor2.getId() & 255) << 24);
    }

    public TropicalFish(EntityType<? extends TropicalFish> entityType, Level level) {
        super(entityType, level);
        this.isSchool = true;
    }

    public static String getPredefinedName(int i) {
        return "entity.minecraft.tropical_fish.predefined." + i;
    }

    public static DyeColor getBaseColor(int i) {
        return DyeColor.byId(getBaseColorIdx(i));
    }

    public static DyeColor getPatternColor(int i) {
        return DyeColor.byId(getPatternColorIdx(i));
    }

    public static String getFishTypeName(int i) {
        return "entity.minecraft.tropical_fish.type." + Pattern.getPatternName(getBaseVariant(i), getPatternVariant(i));
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Variant", getVariant());
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setVariant(compoundTag.getInt("Variant"));
    }

    public void setVariant(int i) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean isMaxGroupSizeReached(int i) {
        return !this.isSchool;
    }

    public int getVariant() {
        return ((Integer) this.entityData.get(DATA_ID_TYPE_VARIANT)).intValue();
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected void saveToBucketTag(ItemStack itemStack) {
        super.saveToBucketTag(itemStack);
        itemStack.getOrCreateTag().putInt("BucketVariantTag", getVariant());
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected ItemStack getBucketItemStack() {
        return new ItemStack(Items.TROPICAL_FISH_BUCKET);
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TROPICAL_FISH_AMBIENT;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.TROPICAL_FISH_DEATH;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.TROPICAL_FISH_HURT;
    }

    @Override // net.minecraft.world.entity.animal.AbstractFish
    protected SoundEvent getFlopSound() {
        return SoundEvents.TROPICAL_FISH_FLOP;
    }

    private static int getBaseColorIdx(int i) {
        return (i & 16711680) >> 16;
    }

    public float[] getBaseColor() {
        return DyeColor.byId(getBaseColorIdx(getVariant())).getTextureDiffuseColors();
    }

    private static int getPatternColorIdx(int i) {
        return (i & (-16777216)) >> 24;
    }

    public float[] getPatternColor() {
        return DyeColor.byId(getPatternColorIdx(getVariant())).getTextureDiffuseColors();
    }

    public static int getBaseVariant(int i) {
        return Math.min(i & 255, 1);
    }

    public int getBaseVariant() {
        return getBaseVariant(getVariant());
    }

    private static int getPatternVariant(int i) {
        return Math.min((i & 65280) >> 8, 5);
    }

    public ResourceLocation getPatternTextureLocation() {
        if (getBaseVariant(getVariant()) == 0) {
            return PATTERN_A_TEXTURE_LOCATIONS[getPatternVariant(getVariant())];
        }
        return PATTERN_B_TEXTURE_LOCATIONS[getPatternVariant(getVariant())];
    }

    public ResourceLocation getBaseTextureLocation() {
        return BASE_TEXTURE_LOCATIONS[getBaseVariant(getVariant())];
    }

    @Override // net.minecraft.world.entity.animal.AbstractSchoolingFish, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        if (compoundTag != null && compoundTag.contains("BucketVariantTag", 3)) {
            this.setVariant(compoundTag.getInt("BucketVariantTag"));
            return spawnGroupData;
        } else {
            int var6;
            int var7x;
            int var8xx;
            int var9xxx;
            if (spawnGroupData instanceof TropicalFish.TropicalFishGroupData) {
                TropicalFish.TropicalFishGroupData var10xxxx = (TropicalFish.TropicalFishGroupData)spawnGroupData;
                var6 = var10xxxx.base;
                var7x = var10xxxx.pattern;
                var8xx = var10xxxx.baseColor;
                var9xxx = var10xxxx.patternColor;
            } else if ((double)this.random.nextFloat() < 0.9) {
                int var12 = Util.getRandom(COMMON_VARIANTS, this.random);
                var6 = var12 & 0xFF;
                var7x = (var12 & 0xFF00) >> 8;
                var8xx = (var12 & 0xFF0000) >> 16;
                var9xxx = (var12 & 0xFF000000) >> 24;
                spawnGroupData = new TropicalFish.TropicalFishGroupData(this, var6, var7x, var8xx, var9xxx);
            } else {
                this.isSchool = false;
                var6 = this.random.nextInt(2);
                var7x = this.random.nextInt(6);
                var8xx = this.random.nextInt(15);
                var9xxx = this.random.nextInt(15);
            }

            this.setVariant(var6 | var7x << 8 | var8xx << 16 | var9xxx << 24);
            return spawnGroupData;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/TropicalFish$TropicalFishGroupData.class */
    static class TropicalFishGroupData extends AbstractSchoolingFish.SchoolSpawnGroupData {
        private final int base;
        private final int pattern;
        private final int baseColor;
        private final int patternColor;

        private TropicalFishGroupData(TropicalFish tropicalFish, int i, int i2, int i3, int i4) {
            super(tropicalFish);
            this.base = i;
            this.pattern = i2;
            this.baseColor = i3;
            this.patternColor = i4;
        }
    }
}
