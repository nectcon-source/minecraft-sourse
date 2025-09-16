package net.minecraft.world.entity.monster;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/CrossbowAttackMob.class */
public interface CrossbowAttackMob extends RangedAttackMob {
    void setChargingCrossbow(boolean z);

    void shootCrossbowProjectile(LivingEntity livingEntity, ItemStack itemStack, Projectile projectile, float f);

    @Nullable
    LivingEntity getTarget();

    void onCrossbowAttackPerformed();

    default void performCrossbowAttack(LivingEntity livingEntity, float f) {
        InteractionHand weaponHoldingHand = ProjectileUtil.getWeaponHoldingHand(livingEntity, Items.CROSSBOW);
        ItemStack itemInHand = livingEntity.getItemInHand(weaponHoldingHand);
        if (livingEntity.isHolding(Items.CROSSBOW)) {
            CrossbowItem.performShooting(livingEntity.level, livingEntity, weaponHoldingHand, itemInHand, f, 14 - (livingEntity.level.getDifficulty().getId() * 4));
        }
        onCrossbowAttackPerformed();
    }

    default void shootCrossbowProjectile(LivingEntity livingEntity, LivingEntity livingEntity2, Projectile projectile, float f, float f2) {
        double var7 = livingEntity2.getX() - livingEntity.getX();
        double var9 = livingEntity2.getZ() - livingEntity.getZ();
        double var11 = (double)Mth.sqrt(var7 * var7 + var9 * var9);
        double var13 = livingEntity2.getY(0.3333333333333333) - ((Entity)projectile).getY() + var11 * (double)0.2F;
        Vector3f var15 = this.getProjectileShotVector(livingEntity, new Vec3(var7, var13, var9), f);
        projectile.shoot((double)var15.x(), (double)var15.y(), (double)var15.z(), f2, (float)(14 - livingEntity.level.getDifficulty().getId() * 4));
        livingEntity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (livingEntity.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    default Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
        Vec3 normalize = vec3.normalize();
        Vec3 cross = normalize.cross(new Vec3(0.0d, 1.0d, 0.0d));
        if (cross.lengthSqr() <= 1.0E-7d) {
            cross = normalize.cross(livingEntity.getUpVector(1.0f));
        }
        Quaternion quaternion = new Quaternion(new Vector3f(cross), 90.0f, true);
        Vector3f vector3f = new Vector3f(normalize);
        vector3f.transform(quaternion);
        Quaternion quaternion2 = new Quaternion(vector3f, f, true);
        Vector3f vector3f2 = new Vector3f(normalize);
        vector3f2.transform(quaternion2);
        return vector3f2;
    }
}
