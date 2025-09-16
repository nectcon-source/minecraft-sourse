package net.minecraft.world.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ExperienceOrb.class */
public class ExperienceOrb extends Entity {
    public int tickCount;
    public int age;
    public int throwTime;
    private int health;
    private int value;
    private Player followingPlayer;
    private int followingTime;

    public ExperienceOrb(Level level, double d, double d2, double d3, int i) {
        this(EntityType.EXPERIENCE_ORB, level);
        setPos(d, d2, d3);
        this.yRot = (float) (this.random.nextDouble() * 360.0d);
        setDeltaMovement(((this.random.nextDouble() * 0.20000000298023224d) - 0.10000000149011612d) * 2.0d, this.random.nextDouble() * 0.2d * 2.0d, ((this.random.nextDouble() * 0.20000000298023224d) - 0.10000000149011612d) * 2.0d);
        this.value = i;
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> entityType, Level level) {
        super(entityType, level);
        this.health = 5;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.throwTime > 0) {
            this.throwTime--;
        }
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        if (isEyeInFluid(FluidTags.WATER)) {
            setUnderwaterMovement();
        } else if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.03d, 0.0d));
        }
        if (this.level.getFluidState(blockPosition()).is(FluidTags.LAVA)) {
            setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.20000000298023224d, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + (this.random.nextFloat() * 0.4f));
        }
        if (!this.level.noCollision(getBoundingBox())) {
            moveTowardsClosestSpace(getX(), (getBoundingBox().minY + getBoundingBox().maxY) / 2.0d, getZ());
        }
        if (this.followingTime < (this.tickCount - 20) + (getId() % 100)) {
            if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0d) {
                this.followingPlayer = this.level.getNearestPlayer(this, 8.0d);
            }
            this.followingTime = this.tickCount;
        }
        if (this.followingPlayer != null && this.followingPlayer.isSpectator()) {
            this.followingPlayer = null;
        }
        if (this.followingPlayer != null) {
            Vec3 vec3 = new Vec3(this.followingPlayer.getX() - getX(), (this.followingPlayer.getY() + (this.followingPlayer.getEyeHeight() / 2.0d)) - getY(), this.followingPlayer.getZ() - getZ());
            double lengthSqr = vec3.lengthSqr();
            if (lengthSqr < 64.0d) {
                double sqrt = 1.0d - (Math.sqrt(lengthSqr) / 8.0d);
                setDeltaMovement(getDeltaMovement().add(vec3.normalize().scale(sqrt * sqrt * 0.1d)));
            }
        }
        move(MoverType.SELF, getDeltaMovement());
        float f = 0.98f;
        if (this.onGround) {
            f = this.level.getBlockState(new BlockPos(getX(), getY() - 1.0d, getZ())).getBlock().getFriction() * 0.98f;
        }
        setDeltaMovement(getDeltaMovement().multiply(f, 0.98d, f));
        if (this.onGround) {
            setDeltaMovement(getDeltaMovement().multiply(1.0d, -0.9d, 1.0d));
        }
        this.tickCount++;
        this.age++;
        if (this.age >= 6000) {
            remove();
        }
    }

    private void setUnderwaterMovement() {
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x * 0.9900000095367432d, Math.min(deltaMovement.y + 5.000000237487257E-4d, 0.05999999865889549d), deltaMovement.z * 0.9900000095367432d);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void doWaterSplashEffect() {
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        markHurt();
        this.health = (int) (this.health - f);
        if (this.health <= 0) {
            remove();
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Health", (short) this.health);
        compoundTag.putShort("Age", (short) this.age);
        compoundTag.putShort("Value", (short) this.value);
    }

    @Override // net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.health = compoundTag.getShort("Health");
        this.age = compoundTag.getShort("Age");
        this.value = compoundTag.getShort("Value");
    }

    @Override // net.minecraft.world.entity.Entity
    public void playerTouch(Player player) {
        if (!this.level.isClientSide && this.throwTime == 0 && player.takeXpDelay == 0) {
            player.takeXpDelay = 2;
            player.take(this, 1);
            Map.Entry<EquipmentSlot, ItemStack> randomItemWith = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player, (v0) -> {
                return v0.isDamaged();
            });
            if (randomItemWith != null) {
                ItemStack value = randomItemWith.getValue();
                if (!value.isEmpty() && value.isDamaged()) {
                    int min = Math.min(xpToDurability(this.value), value.getDamageValue());
                    this.value -= durabilityToXp(min);
                    value.setDamageValue(value.getDamageValue() - min);
                }
            }
            if (this.value > 0) {
                player.giveExperiencePoints(this.value);
            }
            remove();
        }
    }

    private int durabilityToXp(int i) {
        return i / 2;
    }

    private int xpToDurability(int i) {
        return i * 2;
    }

    public int getValue() {
        return this.value;
    }

    public int getIcon() {
        if (this.value >= 2477) {
            return 10;
        }
        if (this.value >= 1237) {
            return 9;
        }
        if (this.value >= 617) {
            return 8;
        }
        if (this.value >= 307) {
            return 7;
        }
        if (this.value >= 149) {
            return 6;
        }
        if (this.value >= 73) {
            return 5;
        }
        if (this.value >= 37) {
            return 4;
        }
        if (this.value >= 17) {
            return 3;
        }
        if (this.value >= 7) {
            return 2;
        }
        if (this.value >= 3) {
            return 1;
        }
        return 0;
    }

    public static int getExperienceValue(int i) {
        if (i >= 2477) {
            return 2477;
        }
        if (i >= 1237) {
            return 1237;
        }
        if (i >= 617) {
            return 617;
        }
        if (i >= 307) {
            return 307;
        }
        if (i >= 149) {
            return 149;
        }
        if (i >= 73) {
            return 73;
        }
        if (i >= 37) {
            return 37;
        }
        if (i >= 17) {
            return 17;
        }
        if (i >= 7) {
            return 7;
        }
        if (i >= 3) {
            return 3;
        }
        return 1;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddExperienceOrbPacket(this);
    }
}
