package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/player/Abilities.class */
public class Abilities {
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    private float flyingSpeed = 0.05f;
    private float walkingSpeed = 0.1f;

    public void addSaveData(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.putBoolean("invulnerable", this.invulnerable);
        compoundTag2.putBoolean("flying", this.flying);
        compoundTag2.putBoolean("mayfly", this.mayfly);
        compoundTag2.putBoolean("instabuild", this.instabuild);
        compoundTag2.putBoolean("mayBuild", this.mayBuild);
        compoundTag2.putFloat("flySpeed", this.flyingSpeed);
        compoundTag2.putFloat("walkSpeed", this.walkingSpeed);
        compoundTag.put("abilities", compoundTag2);
    }

    public void loadSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("abilities", 10)) {
            CompoundTag compound = compoundTag.getCompound("abilities");
            this.invulnerable = compound.getBoolean("invulnerable");
            this.flying = compound.getBoolean("flying");
            this.mayfly = compound.getBoolean("mayfly");
            this.instabuild = compound.getBoolean("instabuild");
            if (compound.contains("flySpeed", 99)) {
                this.flyingSpeed = compound.getFloat("flySpeed");
                this.walkingSpeed = compound.getFloat("walkSpeed");
            }
            if (compound.contains("mayBuild", 1)) {
                this.mayBuild = compound.getBoolean("mayBuild");
            }
        }
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float f) {
        this.flyingSpeed = f;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float f) {
        this.walkingSpeed = f;
    }
}
