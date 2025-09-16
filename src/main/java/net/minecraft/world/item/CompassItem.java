package net.minecraft.world.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/CompassItem.class */
public class CompassItem extends Item implements Vanishable {
    private static final Logger LOGGER = LogManager.getLogger();

    public CompassItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isLodestoneCompass(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        return tag != null && (tag.contains("LodestoneDimension") || tag.contains("LodestonePos"));
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return isLodestoneCompass(itemStack) || super.isFoil(itemStack);
    }

    public static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag compoundTag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get("LodestoneDimension")).result();
    }

    @Override // net.minecraft.world.item.Item
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean z) {
        if (!level.isClientSide && isLodestoneCompass(itemStack)) {
            CompoundTag orCreateTag = itemStack.getOrCreateTag();
            if (orCreateTag.contains("LodestoneTracked") && !orCreateTag.getBoolean("LodestoneTracked")) {
                return;
            }
            Optional<ResourceKey<Level>> lodestoneDimension = getLodestoneDimension(orCreateTag);
            if (lodestoneDimension.isPresent() && lodestoneDimension.get() == level.dimension() && orCreateTag.contains("LodestonePos") && !((ServerLevel) level).getPoiManager().existsAtPosition(PoiType.LODESTONE, NbtUtils.readBlockPos(orCreateTag.getCompound("LodestonePos")))) {
                orCreateTag.remove("LodestonePos");
            }
        }
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos clickedPos = useOnContext.getClickedPos();
        Level level = useOnContext.getLevel();
        if (level.getBlockState(clickedPos).is(Blocks.LODESTONE)) {
            level.playSound( null, clickedPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
            Player player = useOnContext.getPlayer();
            ItemStack itemInHand = useOnContext.getItemInHand();
            if (!player.abilities.instabuild && itemInHand.getCount() == 1) {
                addLodestoneTags(level.dimension(), clickedPos, itemInHand.getOrCreateTag());
            } else {
                ItemStack itemStack = new ItemStack(Items.COMPASS, 1);
                CompoundTag copy = itemInHand.hasTag() ? itemInHand.getTag().copy() : new CompoundTag();
                itemStack.setTag(copy);
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
                addLodestoneTags(level.dimension(), clickedPos, copy);
                if (!player.inventory.add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(useOnContext);
    }

    private void addLodestoneTags(ResourceKey<Level> resourceKey, BlockPos blockPos, CompoundTag compoundTag) {
        compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
        DataResult encodeStart = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, resourceKey);
        encodeStart.resultOrPartial(LOGGER::error).ifPresent(tag -> {
            compoundTag.put("LodestoneDimension", (Tag) tag);
        });
        compoundTag.putBoolean("LodestoneTracked", true);
    }

    @Override // net.minecraft.world.item.Item
    public String getDescriptionId(ItemStack itemStack) {
        return isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
    }
}
