package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/SpawnEggItem.class */
public class SpawnEggItem extends Item {
    private static final Map<EntityType<?>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
    private final int color1;
    private final int color2;
    private final EntityType<?> defaultType;

    public SpawnEggItem(EntityType<?> entityType, int i, int i2, Item.Properties properties) {
        super(properties);
        this.defaultType = entityType;
        this.color1 = i;
        this.color2 = i2;
        BY_ID.put(entityType, this);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos relative;
        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack itemInHand = useOnContext.getItemInHand();
        BlockPos clickedPos = useOnContext.getClickedPos();
        Direction clickedFace = useOnContext.getClickedFace();
        BlockState blockState = level.getBlockState(clickedPos);
        if (blockState.is(Blocks.SPAWNER)) {
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            if (blockEntity instanceof SpawnerBlockEntity) {
                ((SpawnerBlockEntity) blockEntity).getSpawner().setEntityId(getType(itemInHand.getTag()));
                blockEntity.setChanged();
                level.sendBlockUpdated(clickedPos, blockState, blockState, 3);
                itemInHand.shrink(1);
                return InteractionResult.CONSUME;
            }
        }
        if (blockState.getCollisionShape(level, clickedPos).isEmpty()) {
            relative = clickedPos;
        } else {
            relative = clickedPos.relative(clickedFace);
        }
        if (getType(itemInHand.getTag()).spawn((ServerLevel) level, itemInHand, useOnContext.getPlayer(), relative, MobSpawnType.SPAWN_EGG, true, !Objects.equals(clickedPos, relative) && clickedFace == Direction.UP) != null) {
            itemInHand.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        HitResult playerPOVHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (playerPOVHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemInHand);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.success(itemInHand);
        }
        BlockHitResult blockHitResult = (BlockHitResult) playerPOVHitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemInHand);
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, blockHitResult.getDirection(), itemInHand)) {
            return InteractionResultHolder.fail(itemInHand);
        }
        if (getType(itemInHand.getTag()).spawn((ServerLevel) level, itemInHand, player, blockPos, MobSpawnType.SPAWN_EGG, false, false) == null) {
            return InteractionResultHolder.pass(itemInHand);
        }
        if (!player.abilities.instabuild) {
            itemInHand.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.consume(itemInHand);
    }

    public boolean spawnsEntity(@Nullable CompoundTag compoundTag, EntityType<?> entityType) {
        return Objects.equals(getType(compoundTag), entityType);
    }

    public int getColor(int i) {
        return i == 0 ? this.color1 : this.color2;
    }

    @Nullable
    public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
        return BY_ID.get(entityType);
    }

    public static Iterable<SpawnEggItem> eggs() {
        return Iterables.unmodifiableIterable(BY_ID.values());
    }

    public EntityType<?> getType(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null && compoundTag.contains("EntityTag", 10)) {
            CompoundTag compound = compoundTag.getCompound("EntityTag");
            if (compound.contains("id", 8)) {
                return EntityType.byString(compound.getString("id")).orElse(this.defaultType);
            }
        }
        return this.defaultType;
    }

    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 vec3, ItemStack itemStack) {
        Mob create;
        if (!spawnsEntity(itemStack.getTag(), entityType)) {
            return Optional.empty();
        }
        if (mob instanceof AgableMob) {
            create = ((AgableMob) mob).getBreedOffspring(serverLevel, (AgableMob) mob);
        } else {
            create = entityType.create(serverLevel);
        }
        if (create == null) {
            return Optional.empty();
        }
        create.setBaby(true);
        if (!create.isBaby()) {
            return Optional.empty();
        }
        create.moveTo(vec3.x(), vec3.y(), vec3.z(), 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(create);
        if (itemStack.hasCustomHoverName()) {
            create.setCustomName(itemStack.getHoverName());
        }
        if (!player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        return Optional.of(create);
    }
}
