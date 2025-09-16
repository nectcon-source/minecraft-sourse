package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/EndGatewayFeature.class */
public class EndGatewayFeature extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Codec<EndGatewayConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, EndGatewayConfiguration endGatewayConfiguration) {
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
            boolean z = blockPos2.getX() == blockPos.getX();
            boolean z2 = blockPos2.getY() == blockPos.getY();
            boolean z3 = blockPos2.getZ() == blockPos.getZ();
            boolean z4 = Math.abs(blockPos2.getY() - blockPos.getY()) == 2;
            if (z && z2 && z3) {
                BlockPos immutable = blockPos2.immutable();
                setBlock(worldGenLevel, immutable, Blocks.END_GATEWAY.defaultBlockState());
                endGatewayConfiguration.getExit().ifPresent(blockPos3 -> {
                    BlockEntity blockEntity = worldGenLevel.getBlockEntity(immutable);
                    if (blockEntity instanceof TheEndGatewayBlockEntity) {
                        ((TheEndGatewayBlockEntity) blockEntity).setExitPosition(blockPos3, endGatewayConfiguration.isExitExact());
                        blockEntity.setChanged();
                    }
                });
            } else if (z2) {
                setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
            } else if (z4 && z && z3) {
                setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
            } else if ((!z && !z3) || z4) {
                setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
            } else {
                setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
            }
        }
        return true;
    }
}
