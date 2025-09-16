package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BoatItem.class */
public class BoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and((v0) -> {
        return v0.isPickable();
    });
    private final Boat.Type type;

    public BoatItem(Boat.Type type, Item.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack var4 = player.getItemInHand(interactionHand);
        HitResult var5 = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (var5.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(var4);
        } else {
            Vec3 var6 = player.getViewVector(1.0F);
            double var7 = (double)5.0F;
            List<Entity> var9 = level.getEntities(player, player.getBoundingBox().expandTowards(var6.scale((double)5.0F)).inflate((double)1.0F), ENTITY_PREDICATE);
            if (!var9.isEmpty()) {
                Vec3 var10 = player.getEyePosition(1.0F);

                for(Entity var12 : var9) {
                    AABB var13 = var12.getBoundingBox().inflate((double)var12.getPickRadius());
                    if (var13.contains(var10)) {
                        return InteractionResultHolder.pass(var4);
                    }
                }
            }

            if (var5.getType() == HitResult.Type.BLOCK) {
                Boat var14 = new Boat(level, var5.getLocation().x, var5.getLocation().y, var5.getLocation().z);
                var14.setType(this.type);
                var14.yRot = player.yRot;
                if (!level.noCollision(var14, var14.getBoundingBox().inflate(-0.1))) {
                    return InteractionResultHolder.fail(var4);
                } else {
                    if (!level.isClientSide) {
                        level.addFreshEntity(var14);
                        if (!player.abilities.instabuild) {
                            var4.shrink(1);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(var4, level.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(var4);
            }
        }
    }
}
