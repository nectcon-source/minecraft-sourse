package net.minecraft.world.item;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ArmorStandItem.class */
public class ArmorStandItem extends Item {
    public ArmorStandItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (useOnContext.getClickedFace() == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = new BlockPlaceContext(useOnContext).getClickedPos();
        ItemStack itemInHand = useOnContext.getItemInHand();
        Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(clickedPos);
        AABB makeBoundingBox = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(atBottomCenterOf.x(), atBottomCenterOf.y(), atBottomCenterOf.z());
        if (!level.noCollision(null, makeBoundingBox, entity -> {
            return true;
        }) || !level.getEntities(null, makeBoundingBox).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            ArmorStand create = EntityType.ARMOR_STAND.create(serverLevel, itemInHand.getTag(), null, useOnContext.getPlayer(), clickedPos, MobSpawnType.SPAWN_EGG, true, true);
            if (create == null) {
                return InteractionResult.FAIL;
            }
            serverLevel.addFreshEntityWithPassengers(create);
            create.moveTo(create.getX(), create.getY(), create.getZ(), Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0f) + 22.5f) / 45.0f) * 45.0f, 0.0f);
            randomizePose(create, level.random);
            level.addFreshEntity(create);
            level.playSound(null, create.getX(), create.getY(), create.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75f, 0.8f);
        }
        itemInHand.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void randomizePose(ArmorStand armorStand, Random random) {
        Rotations headPose = armorStand.getHeadPose();
        armorStand.setHeadPose(new Rotations(headPose.getX() + (random.nextFloat() * 5.0f), headPose.getY() + ((random.nextFloat() * 20.0f) - 10.0f), headPose.getZ()));
        Rotations bodyPose = armorStand.getBodyPose();
        armorStand.setBodyPose(new Rotations(bodyPose.getX(), bodyPose.getY() + ((random.nextFloat() * 10.0f) - 5.0f), bodyPose.getZ()));
    }
}
