package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/StructureTemplatePool.class */
public class StructureTemplatePool {
    private static final Logger LOGGER = LogManager.getLogger();
//    public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create(instance -> {
//        RecordCodecBuilder forGetter = ResourceLocation.CODEC.fieldOf("name").forGetter((v0) -> {
//            return v0.getName();
//        });
//        RecordCodecBuilder forGetter2 = ResourceLocation.CODEC.fieldOf("fallback").forGetter((v0) -> {
//            return v0.getFallback();
//        });
//        Codec listOf = Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.INT.fieldOf("weight")).codec().listOf();
//        Logger logger = LOGGER;
//        logger.getClass();
//        return instance.group(forGetter, forGetter2, listOf.promotePartial(Util.prefix("Pool element: ", logger::error)).fieldOf("elements").forGetter(structureTemplatePool -> {
//            return structureTemplatePool.rawTemplates;
//        })).apply(instance, StructureTemplatePool::new);
//    });
public static final Codec<StructureTemplatePool> DIRECT_CODEC = RecordCodecBuilder.create((var0) -> {

    return var0.group(ResourceLocation.CODEC.fieldOf("name").forGetter(StructureTemplatePool::getName), ResourceLocation.CODEC.fieldOf("fallback").forGetter(StructureTemplatePool::getFallback), Codec.mapPair(StructurePoolElement.CODEC.fieldOf("element"), Codec.INT.fieldOf("weight")).codec().listOf().promotePartial(Util.prefix("Pool element: ", LOGGER::error)).fieldOf("elements").forGetter((var0x) -> var0x.rawTemplates)).apply(var0, StructureTemplatePool::new);
});
    public static final Codec<Supplier<StructureTemplatePool>> CODEC = RegistryFileCodec.create(Registry.TEMPLATE_POOL_REGISTRY, DIRECT_CODEC);
    private final ResourceLocation name;
    private final List<Pair<StructurePoolElement, Integer>> rawTemplates;
    private final List<StructurePoolElement> templates;
    private final ResourceLocation fallback;
    private int maxSize;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/StructureTemplatePool$Projection.class */
    public enum Projection implements StringRepresentable {
        TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))),
        RIGID("rigid", ImmutableList.of());

        public static final Codec<Projection> CODEC = StringRepresentable.fromEnum(Projection::values, Projection::byName);
        private static final Map<String, Projection> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, projection -> {
            return projection;
        }));
        private final String name;
        private final ImmutableList<StructureProcessor> processors;

        Projection(String str, ImmutableList immutableList) {
            this.name = str;
            this.processors = immutableList;
        }

        public String getName() {
            return this.name;
        }

        public static Projection byName(String str) {
            return BY_NAME.get(str);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }

    public StructureTemplatePool(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<Pair<StructurePoolElement, Integer>> list) {
        this.maxSize = Integer.MIN_VALUE;
        this.name = resourceLocation;
        this.rawTemplates = list;
        this.templates = Lists.newArrayList();
        for (Pair<StructurePoolElement, Integer> pair : list) {
            StructurePoolElement structurePoolElement = (StructurePoolElement) pair.getFirst();
            for (int i = 0; i < ((Integer) pair.getSecond()).intValue(); i++) {
                this.templates.add(structurePoolElement);
            }
        }
        this.fallback = resourceLocation2;
    }

    public StructureTemplatePool(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> list, Projection projection) {
        this.maxSize = Integer.MIN_VALUE;
        this.name = resourceLocation;
        this.rawTemplates = Lists.newArrayList();
        this.templates = Lists.newArrayList();
        for (Pair<Function<Projection, ? extends StructurePoolElement>, Integer> pair : list) {
            StructurePoolElement structurePoolElement = (StructurePoolElement) ((Function) pair.getFirst()).apply(projection);
            this.rawTemplates.add(Pair.of(structurePoolElement, pair.getSecond()));
            for (int i = 0; i < ((Integer) pair.getSecond()).intValue(); i++) {
                this.templates.add(structurePoolElement);
            }
        }
        this.fallback = resourceLocation2;
    }

    public int getMaxSize(StructureManager structureManager) {
        if (this.maxSize == Integer.MIN_VALUE) {
            this.maxSize = this.templates.stream().mapToInt(structurePoolElement -> {
                return structurePoolElement.getBoundingBox(structureManager, BlockPos.ZERO, Rotation.NONE).getYSpan();
            }).max().orElse(0);
        }
        return this.maxSize;
    }

    public ResourceLocation getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomTemplate(Random random) {
        return this.templates.get(random.nextInt(this.templates.size()));
    }

    public List<StructurePoolElement> getShuffledTemplates(Random random) {
        return ImmutableList.copyOf(ObjectArrays.shuffle(this.templates.toArray(new StructurePoolElement[0]), random));
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public int size() {
        return this.templates.size();
    }
}
