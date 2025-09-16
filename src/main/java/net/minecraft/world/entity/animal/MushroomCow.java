package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/MushroomCow.class */
public class MushroomCow extends Cow implements Shearable {
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.STRING);
    private MobEffect effect;
    private int effectDuration;
    private UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.PathfinderMob
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos.below()).is(Blocks.MYCELIUM)) {
            return 10.0f;
        }
        return levelReader.getBrightness(blockPos) - 0.5f;
    }

    public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        return levelAccessor.getBlockState(blockPos.below()).is(Blocks.MYCELIUM) && levelAccessor.getRawBrightness(blockPos, 0) > 8;
    }

    @Override // net.minecraft.world.entity.Entity
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        UUID uuid = lightningBolt.getUUID();
        if (!uuid.equals(this.lastLightningBoltUUID)) {
            setMushroomType(getMushroomType() == MushroomType.RED ? MushroomType.BROWN : MushroomType.RED);
            this.lastLightningBoltUUID = uuid;
            playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override // net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE, MushroomType.RED.type);
    }

    @Override // net.minecraft.world.entity.animal.Cow, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.Mob
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack;
        SoundEvent soundEvent;
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.BOWL && !isBaby()) {
            boolean z = false;
            if (this.effect != null) {
                z = true;
                itemStack = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(itemStack, this.effect, this.effectDuration);
                this.effect = null;
                this.effectDuration = 0;
            } else {
                itemStack = new ItemStack(Items.MUSHROOM_STEW);
            }
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemInHand, player, itemStack, false));
            if (z) {
                soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            } else {
                soundEvent = SoundEvents.MOOSHROOM_MILK;
            }
            playSound(soundEvent, 1.0f, 1.0f);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (itemInHand.getItem() == Items.SHEARS && readyForShearing()) {
            shear(SoundSource.PLAYERS);
            if (!this.level.isClientSide) {
                itemInHand.hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (getMushroomType() == MushroomType.BROWN && itemInHand.getItem().is(ItemTags.SMALL_FLOWERS)) {
            if (this.effect != null) {
                for (int i = 0; i < 2; i++) {
                    this.level.addParticle(ParticleTypes.SMOKE, getX() + (this.random.nextDouble() / 2.0d), getY(0.5d), getZ() + (this.random.nextDouble() / 2.0d), 0.0d, this.random.nextDouble() / 5.0d, 0.0d);
                }
            } else {
                Optional<Pair<MobEffect, Integer>> effectFromItemStack = getEffectFromItemStack(itemInHand);
                if (!effectFromItemStack.isPresent()) {
                    return InteractionResult.PASS;
                }
                Pair<MobEffect, Integer> pair = effectFromItemStack.get();
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
                for (int i2 = 0; i2 < 4; i2++) {
                    this.level.addParticle(ParticleTypes.EFFECT, getX() + (this.random.nextDouble() / 2.0d), getY(0.5d), getZ() + (this.random.nextDouble() / 2.0d), 0.0d, this.random.nextDouble() / 5.0d, 0.0d);
                }
                this.effect = (MobEffect) pair.getLeft();
                this.effectDuration = ((Integer) pair.getRight()).intValue();
                playSound(SoundEvents.MOOSHROOM_EAT, 2.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override // net.minecraft.world.entity.Shearable
    public void shear(SoundSource soundSource) {
        this.level.playSound((Player) null, this, SoundEvents.MOOSHROOM_SHEAR, soundSource, 1.0f, 1.0f);
        if (!this.level.isClientSide()) {
            ((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION, getX(), getY(0.5d), getZ(), 1, 0.0d, 0.0d, 0.0d, 0.0d);
            remove();
            Cow create = EntityType.COW.create(this.level);
            create.moveTo(getX(), getY(), getZ(), this.yRot, this.xRot);
            create.setHealth(getHealth());
            create.yBodyRot = this.yBodyRot;
            if (hasCustomName()) {
                create.setCustomName(getCustomName());
                create.setCustomNameVisible(isCustomNameVisible());
            }
            if (isPersistenceRequired()) {
                create.setPersistenceRequired();
            }
            create.setInvulnerable(isInvulnerable());
            this.level.addFreshEntity(create);
            for (int i = 0; i < 5; i++) {
                this.level.addFreshEntity(new ItemEntity(this.level, getX(), getY(1.0d), getZ(), new ItemStack(getMushroomType().blockState.getBlock())));
            }
        }
    }

    @Override // net.minecraft.world.entity.Shearable
    public boolean readyForShearing() {
        return isAlive() && !isBaby();
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("Type", getMushroomType().type);
        if (this.effect != null) {
            compoundTag.putByte("EffectId", (byte) MobEffect.getId(this.effect));
            compoundTag.putInt("EffectDuration", this.effectDuration);
        }
    }

    @Override // net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setMushroomType(MushroomType.byType(compoundTag.getString("Type")));
        if (compoundTag.contains("EffectId", 1)) {
            this.effect = MobEffect.byId(compoundTag.getByte("EffectId"));
        }
        if (compoundTag.contains("EffectDuration", 3)) {
            this.effectDuration = compoundTag.getInt("EffectDuration");
        }
    }

    private Optional<Pair<MobEffect, Integer>> getEffectFromItemStack(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof FlowerBlock) {
                FlowerBlock flowerBlock = (FlowerBlock) block;
                return Optional.of(Pair.of(flowerBlock.getSuspiciousStewEffect(), Integer.valueOf(flowerBlock.getEffectDuration())));
            }
        }
        return Optional.empty();
    }

    private void setMushroomType(MushroomType mushroomType) {
        this.entityData.set(DATA_TYPE, mushroomType.type);
    }

    public MushroomType getMushroomType() {
        return MushroomType.byType((String) this.entityData.get(DATA_TYPE));
    }

    @Override // net.minecraft.world.entity.animal.Cow, net.minecraft.world.entity.AgableMob
    public MushroomCow getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
        MushroomCow create = EntityType.MOOSHROOM.create(serverLevel);
        create.setMushroomType(getOffspringType((MushroomCow) agableMob));
        return create;
    }

    private MushroomType getOffspringType(MushroomCow mushroomCow) {
        MushroomType mushroomType;
        MushroomType mushroomType2 = getMushroomType();
        MushroomType mushroomType3 = mushroomCow.getMushroomType();
        if (mushroomType2 == mushroomType3 && this.random.nextInt(1024) == 0) {
            mushroomType = mushroomType2 == MushroomType.BROWN ? MushroomType.RED : MushroomType.BROWN;
        } else {
            mushroomType = this.random.nextBoolean() ? mushroomType2 : mushroomType3;
        }
        return mushroomType;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/MushroomCow$MushroomType.class */
    public enum MushroomType {
        RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

        private final String type;
        private final BlockState blockState;

        MushroomType(String str, BlockState blockState) {
            this.type = str;
            this.blockState = blockState;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static MushroomType byType(String str) {
            for (MushroomType mushroomType : values()) {
                if (mushroomType.type.equals(str)) {
                    return mushroomType;
                }
            }
            return RED;
        }
    }
}
