package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/stateproviders/WeightedStateProvider.class */
public class WeightedStateProvider extends BlockStateProvider {
    public static final Codec<WeightedStateProvider> CODEC = WeightedList.codec(BlockState.CODEC).comapFlatMap(WeightedStateProvider::create, weightedStateProvider -> {
        return weightedStateProvider.weightedList;
    }).fieldOf("entries").codec();
    private final WeightedList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(WeightedList<BlockState> weightedList) {
        if (weightedList.isEmpty()) {
            return DataResult.error("WeightedStateProvider with no states");
        }
        return DataResult.success(new WeightedStateProvider(weightedList));
    }

    private WeightedStateProvider(WeightedList<BlockState> weightedList) {
        this.weightedList = weightedList;
    }

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    public WeightedStateProvider() {
        this(new WeightedList());
    }

    public WeightedStateProvider add(BlockState blockState, int i) {
        this.weightedList.add(blockState, i);
        return this;
    }

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    public BlockState getState(Random random, BlockPos blockPos) {
        return this.weightedList.getOne(random);
    }
}
