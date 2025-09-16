package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/RuinedPortalPiece.class */
public class RuinedPortalPiece extends TemplateStructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation templateLocation;
    private final Rotation rotation;
    private final Mirror mirror;
    private final VerticalPlacement verticalPlacement;
    private final Properties properties;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/RuinedPortalPiece$Properties.class */
    public static class Properties {
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(Codec.BOOL.fieldOf("cold").forGetter(properties -> {
                return Boolean.valueOf(properties.cold);
            }), Codec.FLOAT.fieldOf("mossiness").forGetter(properties2 -> {
                return Float.valueOf(properties2.mossiness);
            }), Codec.BOOL.fieldOf("air_pocket").forGetter(properties3 -> {
                return Boolean.valueOf(properties3.airPocket);
            }), Codec.BOOL.fieldOf("overgrown").forGetter(properties4 -> {
                return Boolean.valueOf(properties4.overgrown);
            }), Codec.BOOL.fieldOf("vines").forGetter(properties5 -> {
                return Boolean.valueOf(properties5.vines);
            }), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(properties6 -> {
                return Boolean.valueOf(properties6.replaceWithBlackstone);
            })).apply(instance, (v1, v2, v3, v4, v5, v6) -> {
                return new Properties(v1, v2, v3, v4, v5, v6);
            });
        });
        public boolean cold;
        public float mossiness;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public Properties() {
            this.mossiness = 0.2f;
        }

        public <T> Properties(boolean z, float f, boolean z2, boolean z3, boolean z4, boolean z5) {
            this.mossiness = 0.2f;
            this.cold = z;
            this.mossiness = f;
            this.airPocket = z2;
            this.overgrown = z3;
            this.vines = z4;
            this.replaceWithBlackstone = z5;
        }
    }

    public RuinedPortalPiece(BlockPos blockPos, VerticalPlacement verticalPlacement, Properties properties, ResourceLocation resourceLocation, StructureTemplate structureTemplate, Rotation rotation, Mirror mirror, BlockPos blockPos2) {
        super(StructurePieceType.RUINED_PORTAL, 0);
        this.templatePosition = blockPos;
        this.templateLocation = resourceLocation;
        this.rotation = rotation;
        this.mirror = mirror;
        this.verticalPlacement = verticalPlacement;
        this.properties = properties;
        loadTemplate(structureTemplate, blockPos2);
    }

    public RuinedPortalPiece(StructureManager structureManager, CompoundTag compoundTag) {
        super(StructurePieceType.RUINED_PORTAL, compoundTag);
        this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
        this.rotation = Rotation.valueOf(compoundTag.getString("Rotation"));
        this.mirror = Mirror.valueOf(compoundTag.getString("Mirror"));
        this.verticalPlacement = VerticalPlacement.byName(compoundTag.getString("VerticalPlacement"));
        DataResult parse = Properties.CODEC.parse(new Dynamic(NbtOps.INSTANCE, compoundTag.get("Properties")));
        Logger logger = LOGGER;
        logger.getClass();
        this.properties = (Properties) parse.getOrThrow(true, logger::error);
        StructureTemplate orCreate = structureManager.getOrCreate(this.templateLocation);
        loadTemplate(orCreate, new BlockPos(orCreate.getSize().getX() / 2, 0, orCreate.getSize().getZ() / 2));
    }

    @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putString("Template", this.templateLocation.toString());
        compoundTag.putString("Rotation", this.rotation.name());
        compoundTag.putString("Mirror", this.mirror.name());
        compoundTag.putString("VerticalPlacement", this.verticalPlacement.getName());
        DataResult encodeStart = Properties.CODEC.encodeStart(NbtOps.INSTANCE, this.properties);
        Logger logger = LOGGER;
        logger.getClass();
        encodeStart.resultOrPartial(logger::error).ifPresent(tag -> {
            compoundTag.put("Properties", (Tag) tag);
        });
    }

    private void loadTemplate(StructureTemplate structureTemplate, BlockPos blockPos) {
        BlockIgnoreProcessor blockIgnoreProcessor = this.properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
        List<ProcessorRule> newArrayList = Lists.newArrayList();
        newArrayList.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3f, Blocks.AIR));
        newArrayList.add(getLavaProcessorRule());
        if (!this.properties.cold) {
            newArrayList.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07f, Blocks.MAGMA_BLOCK));
        }
        StructurePlaceSettings addProcessor = new StructurePlaceSettings().setRotation(this.rotation).setMirror(this.mirror).setRotationPivot(blockPos).addProcessor(blockIgnoreProcessor).addProcessor(new RuleProcessor(newArrayList)).addProcessor(new BlockAgeProcessor(this.properties.mossiness)).addProcessor(new LavaSubmergedBlockProcessor());
        if (this.properties.replaceWithBlackstone) {
            addProcessor.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
        }
        setup(structureTemplate, this.templatePosition, addProcessor);
    }

    private ProcessorRule getLavaProcessorRule() {
        if (this.verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR) {
            return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
        }
        if (this.properties.cold) {
            return getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK);
        }
        return getBlockReplaceRule(Blocks.LAVA, 0.2f, Blocks.MAGMA_BLOCK);
    }

    @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        if (!boundingBox.isInside(this.templatePosition)) {
            return true;
        }
        boundingBox.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
        boolean postProcess = super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
        spreadNetherrack(random, worldGenLevel);
        addNetherrackDripColumnsBelowPortal(random, worldGenLevel);
        if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(getBoundingBox()).forEach(blockPos2 -> {
                if (this.properties.vines) {
                    maybeAddVines(random, worldGenLevel, blockPos2);
                }
                if (this.properties.overgrown) {
                    maybeAddLeavesAbove(random, worldGenLevel, blockPos2);
                }
            });
        }
        return postProcess;
    }

    @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
    protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
    }

    private void maybeAddVines(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.isAir() || blockState.is(Blocks.VINE)) {
            return;
        }
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos relative = blockPos.relative(randomDirection);
        if (!levelAccessor.getBlockState(relative).isAir() || !Block.isFaceFull(blockState.getCollisionShape(levelAccessor, blockPos), randomDirection)) {
            return;
        }
        levelAccessor.setBlock(relative, (BlockState) Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(randomDirection.getOpposite()), true), 3);
    }

    private void maybeAddLeavesAbove(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (random.nextFloat() < 0.5f && levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK) && levelAccessor.getBlockState(blockPos.above()).isAir()) {
            levelAccessor.setBlock(blockPos.above(), (BlockState) Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 3);
        }
    }

    private void addNetherrackDripColumnsBelowPortal(Random random, LevelAccessor levelAccessor) {
        for (int i = this.boundingBox.x0 + 1; i < this.boundingBox.x1; i++) {
            for (int i2 = this.boundingBox.z0 + 1; i2 < this.boundingBox.z1; i2++) {
                BlockPos blockPos = new BlockPos(i, this.boundingBox.y0, i2);
                if (levelAccessor.getBlockState(blockPos).is(Blocks.NETHERRACK)) {
                    addNetherrackDripColumn(random, levelAccessor, blockPos.below());
                }
            }
        }
    }

    private void addNetherrackDripColumn(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        placeNetherrackOrMagma(random, levelAccessor, mutable);
        int i = 8;
        while (i > 0 && random.nextFloat() < 0.5f) {
            mutable.move(Direction.DOWN);
            i--;
            placeNetherrackOrMagma(random, levelAccessor, mutable);
        }
    }

    private void spreadNetherrack(Random random, LevelAccessor levelAccessor) {
        boolean var3 = this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
        Vec3i var4 = this.boundingBox.getCenter();
        int var5 = var4.getX();
        int var6 = var4.getZ();
        float[] var7 = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
        int var8 = var7.length;
        int var9 = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int var10 = random.nextInt(Math.max(1, 8 - var9 / 2));
        int var11 = 3;
        BlockPos.MutableBlockPos var12 = BlockPos.ZERO.mutable();

        for(int var13 = var5 - var8; var13 <= var5 + var8; ++var13) {
            for(int var14 = var6 - var8; var14 <= var6 + var8; ++var14) {
                int var15 = Math.abs(var13 - var5) + Math.abs(var14 - var6);
                int var16 = Math.max(0, var15 + var10);
                if (var16 < var8) {
                    float var17 = var7[var16];
                    if (random.nextDouble() < (double)var17) {
                        int var18 = getSurfaceY(levelAccessor, var13, var14, this.verticalPlacement);
                        int var19 = var3 ? var18 : Math.min(this.boundingBox.y0, var18);
                        var12.set(var13, var19, var14);
                        if (Math.abs(var19 - this.boundingBox.y0) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(levelAccessor, var12)) {
                            this.placeNetherrackOrMagma(random, levelAccessor, var12);
                            if (this.properties.overgrown) {
                                this.maybeAddLeavesAbove(random, levelAccessor, var12);
                            }

                            this.addNetherrackDripColumn(random, levelAccessor, var12.below());
                        }
                    }
                }
            }
        }
    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        return (blockState.is(Blocks.AIR) || blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.CHEST) || (this.verticalPlacement != VerticalPlacement.IN_NETHER && blockState.is(Blocks.LAVA))) ? false : true;
    }

    private void placeNetherrackOrMagma(Random random, LevelAccessor levelAccessor, BlockPos blockPos) {
        if (!this.properties.cold && random.nextFloat() < 0.07f) {
            levelAccessor.setBlock(blockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            levelAccessor.setBlock(blockPos, Blocks.NETHERRACK.defaultBlockState(), 3);
        }
    }

    private static int getSurfaceY(LevelAccessor levelAccessor, int i, int i2, VerticalPlacement verticalPlacement) {
        return levelAccessor.getHeight(getHeightMapType(verticalPlacement), i, i2) - 1;
    }

    public static Heightmap.Types getHeightMapType(VerticalPlacement verticalPlacement) {
        return verticalPlacement == VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
    }

    private static ProcessorRule getBlockReplaceRule(Block block, float f, Block block2) {
        return new ProcessorRule(new RandomBlockMatchTest(block, f), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    private static ProcessorRule getBlockReplaceRule(Block block, Block block2) {
        return new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, block2.defaultBlockState());
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/RuinedPortalPiece$VerticalPlacement.class */
    public enum VerticalPlacement {
        ON_LAND_SURFACE("on_land_surface"),
        PARTLY_BURIED("partly_buried"),
        ON_OCEAN_FLOOR("on_ocean_floor"),
        IN_MOUNTAIN("in_mountain"),
        UNDERGROUND("underground"),
        IN_NETHER("in_nether");

        private static final Map<String, VerticalPlacement> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, verticalPlacement -> {
            return verticalPlacement;
        }));
        private final String name;

        VerticalPlacement(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        public static VerticalPlacement byName(String str) {
            return BY_NAME.get(str);
        }
    }
}
