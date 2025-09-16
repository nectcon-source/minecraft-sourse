package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonSittingScanningPhase.class */
public class DragonSittingScanningPhase extends AbstractDragonSittingPhase {
    private static final TargetingConditions CHARGE_TARGETING = new TargetingConditions().range(150.0d);
    private final TargetingConditions scanTargeting;
    private int scanningTime;

    public DragonSittingScanningPhase(EnderDragon enderDragon) {
        super(enderDragon);
        this.scanTargeting = new TargetingConditions().range(20.0d).selector(livingEntity -> {
            return Math.abs(livingEntity.getY() - enderDragon.getY()) <= 10.0d;
        });
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        ++this.scanningTime;
        LivingEntity var1 = this.dragon.level.getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (var1 != null) {
            if (this.scanningTime > 25) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
            } else {
                Vec3 var2 = (new Vec3(var1.getX() - this.dragon.getX(), (double)0.0F, var1.getZ() - this.dragon.getZ())).normalize();
                Vec3 var3 = (new Vec3((double)Mth.sin(this.dragon.yRot * ((float)Math.PI / 180F)), (double)0.0F, (double)(-Mth.cos(this.dragon.yRot * ((float)Math.PI / 180F))))).normalize();
                float var4 = (float)var3.dot(var2);
                float var5 = (float)(Math.acos((double)var4) * (double)(180F / (float)Math.PI)) + 0.5F;
                if (var5 < 0.0F || var5 > 10.0F) {
                    double var6 = var1.getX() - this.dragon.head.getX();
                    double var8 = var1.getZ() - this.dragon.head.getZ();
                    double var10 = Mth.clamp(Mth.wrapDegrees((double)180.0F - Mth.atan2(var6, var8) * (double)(180F / (float)Math.PI) - (double)this.dragon.yRot), (double)-100.0F, (double)100.0F);
                    this.dragon.yRotA *= 0.8F;
                    float var12 = Mth.sqrt(var6 * var6 + var8 * var8) + 1.0F;
                    float var13 = var12;
                    if (var12 > 40.0F) {
                        var12 = 40.0F;
                    }

//                    this.dragon = this.dragon;
                    this.dragon.yRotA = (float)((double)this.dragon.yRotA + var10 * (double)(0.7F / var12 / var13));
//                    this.dragon = this.dragon;
                    this.dragon.yRot += this.dragon.yRotA;
                }
            }
        } else if (this.scanningTime >= 100) {
            LivingEntity var14 = this.dragon.level.getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            if (var14 != null) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                (this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER)).setTarget(new Vec3(((LivingEntity)var14).getX(), ((LivingEntity)var14).getY(), ((LivingEntity)var14).getZ()));
            }
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.scanningTime = 0;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
        return EnderDragonPhase.SITTING_SCANNING;
    }
}
