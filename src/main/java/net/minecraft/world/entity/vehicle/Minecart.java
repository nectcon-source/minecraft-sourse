package net.minecraft.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/Minecart.class */
public class Minecart extends AbstractMinecart {
    public Minecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public Minecart(Level level, double d, double d2, double d3) {
        super(EntityType.MINECART, level, d, d2, d3);
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (isVehicle()) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void activateMinecart(int i, int i2, int i3, boolean z) {
        if (z) {
            if (isVehicle()) {
                ejectPassengers();
            }
            if (getHurtTime() == 0) {
                setHurtDir(-getHurtDir());
                setHurtTime(10);
                setDamage(50.0f);
                markHurt();
            }
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }
}
