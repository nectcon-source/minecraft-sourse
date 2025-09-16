package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/MapItem.class */
public class MapItem extends ComplexItem {
    public MapItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int i, int i2, byte b, boolean z, boolean z2) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        createAndStoreSavedData(itemStack, level, i, i2, b, z, z2, level.dimension());
        return itemStack;
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        return level.getMapData(makeKey(getMapId(itemStack)));
    }

    @Nullable
    public static MapItemSavedData getOrCreateSavedData(ItemStack itemStack, Level level) {
        MapItemSavedData savedData = getSavedData(itemStack, level);
        if (savedData == null && (level instanceof ServerLevel)) {
            savedData = createAndStoreSavedData(itemStack, level, level.getLevelData().getXSpawn(), level.getLevelData().getZSpawn(), 3, false, false, level.dimension());
        }
        return savedData;
    }

    public static int getMapId(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains("map", 99)) {
            return 0;
        }
        return tag.getInt("map");
    }

    private static MapItemSavedData createAndStoreSavedData(ItemStack itemStack, Level level, int i, int i2, int i3, boolean z, boolean z2, ResourceKey<Level> resourceKey) {
        int freeMapId = level.getFreeMapId();
        MapItemSavedData mapItemSavedData = new MapItemSavedData(makeKey(freeMapId));
        mapItemSavedData.setProperties(i, i2, i3, z, z2, resourceKey);
        level.setMapData(mapItemSavedData);
        itemStack.getOrCreateTag().putInt("map", freeMapId);
        return mapItemSavedData;
    }

    public static String makeKey(int i) {
        return "map_" + i;
    }

    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        BlockState defaultBlockState;
        BlockState blockState;
        if (level.dimension() != mapItemSavedData.dimension || !(entity instanceof Player)) {
            return;
        }
        int i = 1 << mapItemSavedData.scale;
        int i2 = mapItemSavedData.x;
        int i3 = mapItemSavedData.z;
        int floor = (Mth.floor(entity.getX() - i2) / i) + 64;
        int floor2 = (Mth.floor(entity.getZ() - i3) / i) + 64;
        int i4 = 128 / i;
        if (level.dimensionType().hasCeiling()) {
            i4 /= 2;
        }
        MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player) entity);
        holdingPlayer.step++;
        boolean z = false;
        for (int i5 = (floor - i4) + 1; i5 < floor + i4; i5++) {
            if ((i5 & 15) == (holdingPlayer.step & 15) || z) {
                z = false;
                double d = 0.0d;
                for (int i6 = (floor2 - i4) - 1; i6 < floor2 + i4; i6++) {
                    if (i5 >= 0 && i6 >= -1 && i5 < 128 && i6 < 128) {
                        int i7 = i5 - floor;
                        int i8 = i6 - floor2;
                        boolean z2 = (i7 * i7) + (i8 * i8) > (i4 - 2) * (i4 - 2);
                        int i9 = (((i2 / i) + i5) - 64) * i;
                        int i10 = (((i3 / i) + i6) - 64) * i;
                        LinkedHashMultiset create = LinkedHashMultiset.create();
                        LevelChunk chunkAt = level.getChunkAt(new BlockPos(i9, 0, i10));
                        if (!chunkAt.isEmpty()) {
                            ChunkPos pos = chunkAt.getPos();
                            int i11 = i9 & 15;
                            int i12 = i10 & 15;
                            int i13 = 0;
                            double d2 = 0.0d;
                            if (level.dimensionType().hasCeiling()) {
                                int i14 = i9 + (i10 * 231871);
                                if ((((((i14 * i14) * 31287121) + (i14 * 11)) >> 20) & 1) == 0) {
                                    create.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                                } else {
                                    create.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                                }
                                d2 = 100.0d;
                            } else {
                                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                                BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
                                for (int i15 = 0; i15 < i; i15++) {
                                    for (int i16 = 0; i16 < i; i16++) {
                                        int height = chunkAt.getHeight(Heightmap.Types.WORLD_SURFACE, i15 + i11, i16 + i12) + 1;
                                        if (height > 1) {
                                            do {
                                                height--;
                                                mutableBlockPos.set(pos.getMinBlockX() + i15 + i11, height, pos.getMinBlockZ() + i16 + i12);
                                                defaultBlockState = chunkAt.getBlockState(mutableBlockPos);
                                                if (defaultBlockState.getMapColor(level, mutableBlockPos) != MaterialColor.NONE) {
                                                    break;
                                                }
                                            } while (height > 0);
                                            if (height > 0 && !defaultBlockState.getFluidState().isEmpty()) {
                                                int i17 = height - 1;
                                                mutableBlockPos2.set(mutableBlockPos);
                                                do {
                                                    int i18 = i17;
                                                    i17--;
                                                    mutableBlockPos2.setY(i18);
                                                    blockState = chunkAt.getBlockState(mutableBlockPos2);
                                                    i13++;
                                                    if (i17 <= 0) {
                                                        break;
                                                    }
                                                } while (!blockState.getFluidState().isEmpty());
                                                defaultBlockState = getCorrectStateForFluidBlock(level, defaultBlockState, mutableBlockPos);
                                            }
                                        } else {
                                            defaultBlockState = Blocks.BEDROCK.defaultBlockState();
                                        }
                                        mapItemSavedData.checkBanners(level, pos.getMinBlockX() + i15 + i11, pos.getMinBlockZ() + i16 + i12);
                                        d2 += height / (i * i);
                                        create.add(defaultBlockState.getMapColor(level, mutableBlockPos));
                                    }
                                }
                            }
                            int i19 = i13 / (i * i);
                            double d3 = (((d2 - d) * 4.0d) / (i + 4)) + ((((i5 + i6) & 1) - 0.5d) * 0.4d);
                            int i20 = 1;
                            if (d3 > 0.6d) {
                                i20 = 2;
                            }
                            if (d3 < -0.6d) {
                                i20 = 0;
                            }
                            MaterialColor materialColor = (MaterialColor) Iterables.getFirst(Multisets.copyHighestCountFirst(create), MaterialColor.NONE);
                            if (materialColor == MaterialColor.WATER) {
                                double d4 = (i19 * 0.1d) + (((i5 + i6) & 1) * 0.2d);
                                i20 = 1;
                                if (d4 < 0.5d) {
                                    i20 = 2;
                                }
                                if (d4 > 0.9d) {
                                    i20 = 0;
                                }
                            }
                            d = d2;
                            if (i6 >= 0 && (i7 * i7) + (i8 * i8) < i4 * i4 && (!z2 || ((i5 + i6) & 1) != 0)) {
                                byte b = mapItemSavedData.colors[i5 + (i6 * 128)];
                                byte b2 = (byte) ((materialColor.id * 4) + i20);
                                if (b != b2) {
                                    mapItemSavedData.colors[i5 + (i6 * 128)] = b2;
                                    mapItemSavedData.setDirty(i5, i6);
                                    z = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP)) {
            return fluidState.createLegacyBlock();
        }
        return blockState;
    }

    private static boolean isLand(Biome[] biomeArr, int i, int i2, int i3) {
        return biomeArr[(i2 * i) + (((i3 * i) * 128) * i)].getDepth() >= 0.0f;
    }

    public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
        MapItemSavedData orCreateSavedData = getOrCreateSavedData(itemStack, serverLevel);
        if (orCreateSavedData == null || serverLevel.dimension() != orCreateSavedData.dimension) {
            return;
        }
        int i = 1 << orCreateSavedData.scale;
        int i2 = orCreateSavedData.x;
        int i3 = orCreateSavedData.z;
        Biome[] biomeArr = new Biome[128 * i * 128 * i];
        for (int i4 = 0; i4 < 128 * i; i4++) {
            for (int i5 = 0; i5 < 128 * i; i5++) {
                biomeArr[(i4 * 128 * i) + i5] = serverLevel.getBiome(new BlockPos((((i2 / i) - 64) * i) + i5, 0, (((i3 / i) - 64) * i) + i4));
            }
        }
        for (int i6 = 0; i6 < 128; i6++) {
            for (int i7 = 0; i7 < 128; i7++) {
                if (i6 > 0 && i7 > 0 && i6 < 127 && i7 < 127) {
                    Biome biome = biomeArr[(i6 * i) + (i7 * i * 128 * i)];
                    int i8 = isLand(biomeArr, i, i6 - 1, i7 - 1) ? 8 - 1 : 8;
                    if (isLand(biomeArr, i, i6 - 1, i7 + 1)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6 - 1, i7)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6 + 1, i7 - 1)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6 + 1, i7 + 1)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6 + 1, i7)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6, i7 - 1)) {
                        i8--;
                    }
                    if (isLand(biomeArr, i, i6, i7 + 1)) {
                        i8--;
                    }
                    int i9 = 3;
                    MaterialColor materialColor = MaterialColor.NONE;
                    if (biome.getDepth() < 0.0f) {
                        materialColor = MaterialColor.COLOR_ORANGE;
                        if (i8 > 7 && i7 % 2 == 0) {
                            i9 = ((i6 + ((int) (Mth.sin(i7 + 0.0f) * 7.0f))) / 8) % 5;
                            if (i9 == 3) {
                                i9 = 1;
                            } else if (i9 == 4) {
                                i9 = 0;
                            }
                        } else if (i8 > 7) {
                            materialColor = MaterialColor.NONE;
                        } else if (i8 > 5) {
                            i9 = 1;
                        } else if (i8 > 3) {
                            i9 = 0;
                        } else if (i8 > 1) {
                            i9 = 0;
                        }
                    } else if (i8 > 0) {
                        materialColor = MaterialColor.COLOR_BROWN;
                        i9 = i8 > 3 ? 1 : 3;
                    }
                    if (materialColor != MaterialColor.NONE) {
                        orCreateSavedData.colors[i6 + (i7 * 128)] = (byte) ((materialColor.id * 4) + i9);
                        orCreateSavedData.setDirty(i6, i7);
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.item.Item
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean z) {
        MapItemSavedData orCreateSavedData;
        if (level.isClientSide || (orCreateSavedData = getOrCreateSavedData(itemStack, level)) == null) {
            return;
        }
        if (entity instanceof Player) {
            orCreateSavedData.tickCarriedBy((Player) entity, itemStack);
        }
        if (orCreateSavedData.locked) {
            return;
        }
        if (z || ((entity instanceof Player) && ((Player) entity).getOffhandItem() == itemStack)) {
            update(level, entity, orCreateSavedData);
        }
    }

    @Override // net.minecraft.world.item.ComplexItem
    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
        return getOrCreateSavedData(itemStack, level).getUpdatePacket(itemStack, level, player);
    }

    @Override // net.minecraft.world.item.Item
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("map_scale_direction", 99)) {
            scaleMap(itemStack, level, tag.getInt("map_scale_direction"));
            tag.remove("map_scale_direction");
        } else if (tag != null && tag.contains("map_to_lock", 1) && tag.getBoolean("map_to_lock")) {
            lockMap(level, itemStack);
            tag.remove("map_to_lock");
        }
    }

    protected static void scaleMap(ItemStack itemStack, Level level, int i) {
        MapItemSavedData orCreateSavedData = getOrCreateSavedData(itemStack, level);
        if (orCreateSavedData != null) {
            createAndStoreSavedData(itemStack, level, orCreateSavedData.x, orCreateSavedData.z, Mth.clamp(orCreateSavedData.scale + i, 0, 4), orCreateSavedData.trackingPosition, orCreateSavedData.unlimitedTracking, orCreateSavedData.dimension);
        }
    }

    public static void lockMap(Level level, ItemStack itemStack) {
        MapItemSavedData orCreateSavedData = getOrCreateSavedData(itemStack, level);
        if (orCreateSavedData != null) {
            createAndStoreSavedData(itemStack, level, 0, 0, orCreateSavedData.scale, orCreateSavedData.trackingPosition, orCreateSavedData.unlimitedTracking, orCreateSavedData.dimension).lockData(orCreateSavedData);
        }
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        MapItemSavedData orCreateSavedData = level == null ? null : getOrCreateSavedData(itemStack, level);
        if (orCreateSavedData != null && orCreateSavedData.locked) {
            list.add(new TranslatableComponent("filled_map.locked", Integer.valueOf(getMapId(itemStack))).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipFlag.isAdvanced()) {
            if (orCreateSavedData != null) {
                list.add(new TranslatableComponent("filled_map.id", Integer.valueOf(getMapId(itemStack))).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.scale", Integer.valueOf(1 << orCreateSavedData.scale)).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.level", Byte.valueOf(orCreateSavedData.scale), 4).withStyle(ChatFormatting.GRAY));
                return;
            }
            list.add(new TranslatableComponent("filled_map.unknown").withStyle(ChatFormatting.GRAY));
        }
    }

    public static int getColor(ItemStack itemStack) {
        CompoundTag tagElement = itemStack.getTagElement("display");
        if (tagElement != null && tagElement.contains("MapColor", 99)) {
            return (-16777216) | (tagElement.getInt("MapColor") & 16777215);
        }
        return -12173266;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (useOnContext.getLevel().getBlockState(useOnContext.getClickedPos()).is(BlockTags.BANNERS)) {
            if (!useOnContext.getLevel().isClientSide) {
                getOrCreateSavedData(useOnContext.getItemInHand(), useOnContext.getLevel()).toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos());
            }
            return InteractionResult.sidedSuccess(useOnContext.getLevel().isClientSide);
        }
        return super.useOn(useOnContext);
    }
}
