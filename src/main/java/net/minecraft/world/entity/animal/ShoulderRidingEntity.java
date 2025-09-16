package net.minecraft.world.entity.animal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/ShoulderRidingEntity.class */
public abstract class ShoulderRidingEntity extends TamableAnimal {
    private int rideCooldownCounter;

    protected ShoulderRidingEntity(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public boolean setEntityOnShoulder(ServerPlayer serverPlayer) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("id", getEncodeId());
        saveWithoutId(compoundTag);
        if (serverPlayer.setEntityOnShoulder(compoundTag)) {
            remove();
            return true;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        this.rideCooldownCounter++;
        super.tick();
    }

    public boolean canSitOnShoulder() {
        return this.rideCooldownCounter > 100;
    }
}
