package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RandomPatchConfiguration.class */
public class RandomPatchConfiguration implements FeatureConfiguration {
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(randomPatchConfiguration -> {
            return randomPatchConfiguration.stateProvider;
        }), BlockPlacer.CODEC.fieldOf("block_placer").forGetter(randomPatchConfiguration2 -> {
            return randomPatchConfiguration2.blockPlacer;
        }), BlockState.CODEC.listOf().fieldOf("whitelist").forGetter(randomPatchConfiguration3 -> {
            return (List) randomPatchConfiguration3.whitelist.stream().map((v0) -> {
                return v0.defaultBlockState();
            }).collect(Collectors.toList());
        }), BlockState.CODEC.listOf().fieldOf("blacklist").forGetter(randomPatchConfiguration4 -> {
            return ImmutableList.copyOf(randomPatchConfiguration4.blacklist);
        }), Codec.INT.fieldOf("tries").orElse(128).forGetter(randomPatchConfiguration5 -> {
            return Integer.valueOf(randomPatchConfiguration5.tries);
        }), Codec.INT.fieldOf("xspread").orElse(7).forGetter(randomPatchConfiguration6 -> {
            return Integer.valueOf(randomPatchConfiguration6.xspread);
        }), Codec.INT.fieldOf("yspread").orElse(3).forGetter(randomPatchConfiguration7 -> {
            return Integer.valueOf(randomPatchConfiguration7.yspread);
        }), Codec.INT.fieldOf("zspread").orElse(7).forGetter(randomPatchConfiguration8 -> {
            return Integer.valueOf(randomPatchConfiguration8.zspread);
        }), Codec.BOOL.fieldOf("can_replace").orElse(false).forGetter(randomPatchConfiguration9 -> {
            return Boolean.valueOf(randomPatchConfiguration9.canReplace);
        }), Codec.BOOL.fieldOf("project").orElse(true).forGetter(randomPatchConfiguration10 -> {
            return Boolean.valueOf(randomPatchConfiguration10.project);
        }), Codec.BOOL.fieldOf("need_water").orElse(false).forGetter(randomPatchConfiguration11 -> {
            return Boolean.valueOf(randomPatchConfiguration11.needWater);
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11) -> {
            return new RandomPatchConfiguration(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
        });
    });
    public final BlockStateProvider stateProvider;
    public final BlockPlacer blockPlacer;
    public final Set<Block> whitelist;
    public final Set<BlockState> blacklist;
    public final int tries;
    public final int xspread;
    public final int yspread;
    public final int zspread;
    public final boolean canReplace;
    public final boolean project;
    public final boolean needWater;

    private RandomPatchConfiguration(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer, List<BlockState> list, List<BlockState> list2, int i, int i2, int i3, int i4, boolean z, boolean z2, boolean z3) {
        this(blockStateProvider, blockPlacer, (Set<Block>) list.stream().map((v0) -> {
            return v0.getBlock();
        }).collect(Collectors.toSet()), (Set<BlockState>) ImmutableSet.copyOf(list2), i, i2, i3, i4, z, z2, z3);
    }

    private RandomPatchConfiguration(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer, Set<Block> set, Set<BlockState> set2, int i, int i2, int i3, int i4, boolean z, boolean z2, boolean z3) {
        this.stateProvider = blockStateProvider;
        this.blockPlacer = blockPlacer;
        this.whitelist = set;
        this.blacklist = set2;
        this.tries = i;
        this.xspread = i2;
        this.yspread = i3;
        this.zspread = i4;
        this.canReplace = z;
        this.project = z2;
        this.needWater = z3;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RandomPatchConfiguration$GrassConfigurationBuilder.class */
    public static class GrassConfigurationBuilder {
        private final BlockStateProvider stateProvider;
        private final BlockPlacer blockPlacer;
        private boolean canReplace;
        private Set<Block> whitelist = ImmutableSet.of();
        private Set<BlockState> blacklist = ImmutableSet.of();
        private int tries = 64;
        private int xspread = 7;
        private int yspread = 3;
        private int zspread = 7;
        private boolean project = true;
        private boolean needWater = false;

        public GrassConfigurationBuilder(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer) {
            this.stateProvider = blockStateProvider;
            this.blockPlacer = blockPlacer;
        }

        public GrassConfigurationBuilder whitelist(Set<Block> set) {
            this.whitelist = set;
            return this;
        }

        public GrassConfigurationBuilder blacklist(Set<BlockState> set) {
            this.blacklist = set;
            return this;
        }

        public GrassConfigurationBuilder tries(int i) {
            this.tries = i;
            return this;
        }

        public GrassConfigurationBuilder xspread(int i) {
            this.xspread = i;
            return this;
        }

        public GrassConfigurationBuilder yspread(int i) {
            this.yspread = i;
            return this;
        }

        public GrassConfigurationBuilder zspread(int i) {
            this.zspread = i;
            return this;
        }

        public GrassConfigurationBuilder canReplace() {
            this.canReplace = true;
            return this;
        }

        public GrassConfigurationBuilder noProjection() {
            this.project = false;
            return this;
        }

        public GrassConfigurationBuilder needWater() {
            this.needWater = true;
            return this;
        }

        public RandomPatchConfiguration build() {
            return new RandomPatchConfiguration(this.stateProvider, this.blockPlacer, this.whitelist, this.blacklist, this.tries, this.xspread, this.yspread, this.zspread, this.canReplace, this.project, this.needWater);
        }
    }
}
