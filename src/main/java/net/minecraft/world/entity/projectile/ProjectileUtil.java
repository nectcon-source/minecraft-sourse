package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ProjectileUtil.class */
public final class ProjectileUtil {
    public static HitResult getHitResult(Entity entity, Predicate<Entity> predicate) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        Level level = entity.level;
        Vec3 position = entity.position();
        Vec3 add = position.add(deltaMovement);
        HitResult clip = level.clip(new ClipContext(position, add, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (clip.getType() != HitResult.Type.MISS) {
            add = clip.getLocation();
        }
        HitResult entityHitResult = getEntityHitResult(level, entity, position, add, entity.getBoundingBox().expandTowards(entity.getDeltaMovement()).inflate(1.0d), predicate);
        if (entityHitResult != null) {
            clip = entityHitResult;
        }
        return clip;
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity entity, Vec3 vec3, Vec3 vec32, AABB aabb, Predicate<Entity> predicate, double d) {
        double d2 = d;
        Entity entity2 = null;
        Vec3 vec33 = null;
        for (Entity entity3 : entity.level.getEntities(entity, aabb, predicate)) {
            AABB inflate = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> clip = inflate.clip(vec3, vec32);
            if (inflate.contains(vec3)) {
                if (d2 >= 0.0d) {
                    entity2 = entity3;
                    vec33 = clip.orElse(vec3);
                    d2 = 0.0d;
                }
            } else if (clip.isPresent()) {
                Vec3 vec34 = clip.get();
                double distanceToSqr = vec3.distanceToSqr(vec34);
                if (distanceToSqr < d2 || d2 == 0.0d) {
                    if (entity3.getRootVehicle() != entity.getRootVehicle()) {
                        entity2 = entity3;
                        vec33 = vec34;
                        d2 = distanceToSqr;
                    } else if (d2 == 0.0d) {
                        entity2 = entity3;
                        vec33 = vec34;
                    }
                }
            }
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec33);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aabb, Predicate<Entity> predicate) {
        double d = Double.MAX_VALUE;
        Entity entity2 = null;
        for (Entity entity3 : level.getEntities(entity, aabb, predicate)) {
            Optional<Vec3> clip = entity3.getBoundingBox().inflate(0.30000001192092896d).clip(vec3, vec32);
            if (clip.isPresent()) {
                double distanceToSqr = vec3.distanceToSqr(clip.get());
                if (distanceToSqr < d) {
                    entity2 = entity3;
                    d = distanceToSqr;
                }
            }
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2);
    }

    public static final void rotateTowardsMovement(Entity entity, float f) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        if (deltaMovement.lengthSqr() == 0.0d) {
            return;
        }
        float sqrt = Mth.sqrt(Entity.getHorizontalDistanceSqr(deltaMovement));
        entity.yRot = ((float) (Mth.atan2(deltaMovement.z, deltaMovement.x) * 57.2957763671875d)) + 90.0f;
        entity.xRot = ((float) (Mth.atan2(sqrt, deltaMovement.y) * 57.2957763671875d)) - 90.0f;
        while (entity.xRot - entity.xRotO < -180.0f) {
            entity.xRotO -= 360.0f;
        }
        while (entity.xRot - entity.xRotO >= 180.0f) {
            entity.xRotO += 360.0f;
        }
        while (entity.yRot - entity.yRotO < -180.0f) {
            entity.yRotO -= 360.0f;
        }
        while (entity.yRot - entity.yRotO >= 180.0f) {
            entity.yRotO += 360.0f;
        }
        entity.xRot = Mth.lerp(f, entity.xRotO, entity.xRot);
        entity.yRot = Mth.lerp(f, entity.yRotO, entity.yRot);
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Item item) {
        return livingEntity.getMainHandItem().getItem() == item ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity livingEntity, ItemStack itemStack, float f) {
        AbstractArrow createArrow = ((ArrowItem) (itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW)).createArrow(livingEntity.level, itemStack, livingEntity);
        createArrow.setEnchantmentEffectsFromEntity(livingEntity, f);
        if (itemStack.getItem() == Items.TIPPED_ARROW && (createArrow instanceof Arrow)) {
            ((Arrow) createArrow).setEffectsFromItem(itemStack);
        }
        return createArrow;
    }
}
