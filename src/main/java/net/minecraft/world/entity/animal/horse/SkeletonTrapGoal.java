package net.minecraft.world.entity.animal.horse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/SkeletonTrapGoal.class */
public class SkeletonTrapGoal extends Goal {
    private final SkeletonHorse horse;

    public SkeletonTrapGoal(SkeletonHorse skeletonHorse) {
        this.horse = skeletonHorse;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.horse.level.hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0d);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        ServerLevel serverLevel = (ServerLevel) this.horse.level;
        DifficultyInstance currentDifficultyAt = serverLevel.getCurrentDifficultyAt(this.horse.blockPosition());
        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        LightningBolt create = EntityType.LIGHTNING_BOLT.create(serverLevel);
        create.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        create.setVisualOnly(true);
        serverLevel.addFreshEntity(create);
        Skeleton createSkeleton = createSkeleton(currentDifficultyAt, this.horse);
        createSkeleton.startRiding(this.horse);
        serverLevel.addFreshEntityWithPassengers(createSkeleton);
        for (int i = 0; i < 3; i++) {
            AbstractHorse createHorse = createHorse(currentDifficultyAt);
            createSkeleton(currentDifficultyAt, createHorse).startRiding(createHorse);
            createHorse.push(this.horse.getRandom().nextGaussian() * 0.5d, 0.0d, this.horse.getRandom().nextGaussian() * 0.5d);
            serverLevel.addFreshEntityWithPassengers(createHorse);
        }
    }

    private AbstractHorse createHorse(DifficultyInstance difficultyInstance) {
        SkeletonHorse create = EntityType.SKELETON_HORSE.create(this.horse.level);
        create.finalizeSpawn((ServerLevel) this.horse.level, difficultyInstance, MobSpawnType.TRIGGERED, null, null);
        create.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        create.invulnerableTime = 60;
        create.setPersistenceRequired();
        create.setTamed(true);
        create.setAge(0);
        return create;
    }

    private Skeleton createSkeleton(DifficultyInstance difficultyInstance, AbstractHorse abstractHorse) {
        Skeleton create = EntityType.SKELETON.create(abstractHorse.level);
        create.finalizeSpawn((ServerLevel) abstractHorse.level, difficultyInstance, MobSpawnType.TRIGGERED, null, null);
        create.setPos(abstractHorse.getX(), abstractHorse.getY(), abstractHorse.getZ());
        create.invulnerableTime = 60;
        create.setPersistenceRequired();
        if (create.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            create.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        }
        create.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(create.getRandom(), disenchant(create.getMainHandItem()), (int) (5.0f + (difficultyInstance.getSpecialMultiplier() * create.getRandom().nextInt(18))), false));
        create.setItemSlot(EquipmentSlot.HEAD, EnchantmentHelper.enchantItem(create.getRandom(), disenchant(create.getItemBySlot(EquipmentSlot.HEAD)), (int) (5.0f + (difficultyInstance.getSpecialMultiplier() * create.getRandom().nextInt(18))), false));
        return create;
    }

    private ItemStack disenchant(ItemStack itemStack) {
        itemStack.removeTagKey("Enchantments");
        return itemStack;
    }
}
