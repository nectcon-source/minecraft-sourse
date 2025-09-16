package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/RuinedPortalFeature.class */
public class RuinedPortalFeature extends StructureFeature<RuinedPortalConfiguration> {
    private static final String[] STRUCTURE_LOCATION_PORTALS = {"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
    private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = {"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};

    public RuinedPortalFeature(Codec<RuinedPortalConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<RuinedPortalConfiguration> getStartFactory() {
        return FeatureStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/RuinedPortalFeature$FeatureStart.class */
    public static class FeatureStart extends StructureStart<RuinedPortalConfiguration> {
        protected FeatureStart(StructureFeature<RuinedPortalConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, RuinedPortalConfiguration ruinedPortalConfiguration) {
            RuinedPortalPiece.VerticalPlacement verticalPlacement;
            ResourceLocation resourceLocation;
            RuinedPortalPiece.Properties properties = new RuinedPortalPiece.Properties();
            if (ruinedPortalConfiguration.portalType == Type.DESERT) {
                verticalPlacement = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
                properties.airPocket = false;
                properties.mossiness = 0.0f;
            } else if (ruinedPortalConfiguration.portalType == Type.JUNGLE) {
                verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                properties.airPocket = this.random.nextFloat() < 0.5f;
                properties.mossiness = 0.8f;
                properties.overgrown = true;
                properties.vines = true;
            } else if (ruinedPortalConfiguration.portalType == Type.SWAMP) {
                verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                properties.airPocket = false;
                properties.mossiness = 0.5f;
                properties.vines = true;
            } else if (ruinedPortalConfiguration.portalType == Type.MOUNTAIN) {
                boolean z = this.random.nextFloat() < 0.5f;
                verticalPlacement = z ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                properties.airPocket = z || this.random.nextFloat() < 0.5f;
            } else if (ruinedPortalConfiguration.portalType == Type.OCEAN) {
                verticalPlacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
                properties.airPocket = false;
                properties.mossiness = 0.8f;
            } else if (ruinedPortalConfiguration.portalType == Type.NETHER) {
                verticalPlacement = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
                properties.airPocket = this.random.nextFloat() < 0.5f;
                properties.mossiness = 0.0f;
                properties.replaceWithBlackstone = true;
            } else {
                boolean z2 = this.random.nextFloat() < 0.5f;
                verticalPlacement = z2 ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
                properties.airPocket = z2 || this.random.nextFloat() < 0.5f;
            }
            if (this.random.nextFloat() < 0.05f) {
                resourceLocation = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
            } else {
                resourceLocation = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]);
            }
            StructureTemplate orCreate = structureManager.getOrCreate(resourceLocation);
            Rotation rotation = (Rotation) Util.getRandom(Rotation.values(), this.random);
            Mirror mirror = this.random.nextFloat() < 0.5f ? Mirror.NONE : Mirror.FRONT_BACK;
            BlockPos blockPos = new BlockPos(orCreate.getSize().getX() / 2, 0, orCreate.getSize().getZ() / 2);
            BlockPos worldPosition = new ChunkPos(i, i2).getWorldPosition();
            BoundingBox boundingBox = orCreate.getBoundingBox(worldPosition, rotation, blockPos, mirror);
            Vec3i center = boundingBox.getCenter();
            BlockPos blockPos2 = new BlockPos(worldPosition.getX(), RuinedPortalFeature.findSuitableY(this.random, chunkGenerator, verticalPlacement, properties.airPocket, chunkGenerator.getBaseHeight(center.getX(), center.getZ(), RuinedPortalPiece.getHeightMapType(verticalPlacement)) - 1, boundingBox.getYSpan(), boundingBox), worldPosition.getZ());
            if (ruinedPortalConfiguration.portalType == Type.MOUNTAIN || ruinedPortalConfiguration.portalType == Type.OCEAN || ruinedPortalConfiguration.portalType == Type.STANDARD) {
                properties.cold = RuinedPortalFeature.isCold(blockPos2, biome);
            }
            this.pieces.add(new RuinedPortalPiece(blockPos2, verticalPlacement, properties, resourceLocation, orCreate, rotation, mirror, blockPos));
            calculateBoundingBox();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isCold(BlockPos blockPos, Biome biome) {
        return biome.getTemperature(blockPos) < 0.15f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int findSuitableY(Random random, ChunkGenerator chunkGenerator, RuinedPortalPiece.VerticalPlacement verticalPlacement, boolean z, int i, int i2, BoundingBox boundingBox) {
        int i3;
        if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
            if (z) {
                i3 = randomIntInclusive(random, 32, 100);
            } else if (random.nextFloat() < 0.5f) {
                i3 = randomIntInclusive(random, 27, 29);
            } else {
                i3 = randomIntInclusive(random, 29, 100);
            }
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            i3 = getRandomWithinInterval(random, 70, i - i2);
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            i3 = getRandomWithinInterval(random, 15, i - i2);
        } else if (verticalPlacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
            i3 = (i - i2) + randomIntInclusive(random, 2, 8);
        } else {
            i3 = i;
        }
        List<BlockGetter> list = (List) ImmutableList.of(new BlockPos(boundingBox.x0, 0, boundingBox.z0), new BlockPos(boundingBox.x1, 0, boundingBox.z0), new BlockPos(boundingBox.x0, 0, boundingBox.z1), new BlockPos(boundingBox.x1, 0, boundingBox.z1)).stream().map(blockPos -> {
            return chunkGenerator.getBaseColumn(blockPos.getX(), blockPos.getZ());
        }).collect(Collectors.toList());
        Heightmap.Types types = verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i4 = i3;
        loop0: while (i4 > 15) {
            int i5 = 0;
            mutableBlockPos.set(0, i4, 0);
            Iterator<BlockGetter> it = list.iterator();
            while (it.hasNext()) {
                BlockState blockState = it.next().getBlockState(mutableBlockPos);
                if (blockState != null && types.isOpaque().test(blockState)) {
                    i5++;
                    if (i5 == 3) {
                        break loop0;
                    }
                }
            }
            i4--;
        }
        return i4;
    }

    private static int randomIntInclusive(Random random, int i, int i2) {
        return random.nextInt((i2 - i) + 1) + i;
    }

    private static int getRandomWithinInterval(Random random, int i, int i2) {
        if (i < i2) {
            return randomIntInclusive(random, i, i2);
        }
        return i2;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/RuinedPortalFeature$Type.class */
    public enum Type implements StringRepresentable {
        STANDARD("standard"),
        DESERT("desert"),
        JUNGLE("jungle"),
        SWAMP("swamp"),
        MOUNTAIN("mountain"),
        OCEAN("ocean"),
        NETHER("nether");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values, Type::byName);
        private static final Map<String, Type> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, type -> {
            return type;
        }));
        private final String name;

        Type(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        public static Type byName(String str) {
            return BY_NAME.get(str);
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }
}
