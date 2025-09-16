package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RunAroundLikeCrazyGoal.class */
public class RunAroundLikeCrazyGoal extends Goal {
    private final AbstractHorse horse;
    private final double speedModifier;
    private double posX;
    private double posY;
    private double posZ;

    public RunAroundLikeCrazyGoal(AbstractHorse abstractHorse, double d) {
        this.horse = abstractHorse;
        this.speedModifier = d;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Vec3 pos;
        if (this.horse.isTamed() || !this.horse.isVehicle() || (pos = RandomPos.getPos(this.horse, 5, 4)) == null) {
            return false;
        }
        this.posX = pos.x;
        this.posY = pos.y;
        this.posZ = pos.z;
        return true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.horse.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (this.horse.isTamed() || this.horse.getNavigation().isDone() || !this.horse.isVehicle()) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        Entity entity;
        if (this.horse.isTamed() || this.horse.getRandom().nextInt(50) != 0 || (entity = this.horse.getPassengers().get(0)) == null) {
            return;
        }
        if (entity instanceof Player) {
            int temper = this.horse.getTemper();
            int maxTemper = this.horse.getMaxTemper();
            if (maxTemper > 0 && this.horse.getRandom().nextInt(maxTemper) < temper) {
                this.horse.tameWithName((Player) entity);
                return;
            }
            this.horse.modifyTemper(5);
        }
        this.horse.ejectPassengers();
        this.horse.makeMad();
        this.horse.level.broadcastEntityEvent(this.horse, (byte) 6);
    }
}
