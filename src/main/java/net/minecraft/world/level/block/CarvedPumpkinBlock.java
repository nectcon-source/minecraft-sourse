package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CarvedPumpkinBlock.class */
public class CarvedPumpkinBlock extends HorizontalDirectionalBlock implements Wearable {

    @Nullable
    private BlockPattern snowGolemBase;

    @Nullable
    private BlockPattern snowGolemFull;

    @Nullable
    private BlockPattern ironGolemBase;

    @Nullable
    private BlockPattern ironGolemFull;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Predicate<BlockState> PUMPKINS_PREDICATE = blockState -> {
        return blockState != null && (blockState.is(Blocks.CARVED_PUMPKIN) || blockState.is(Blocks.JACK_O_LANTERN));
    };

    protected CarvedPumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState( this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        trySpawnGolem(level, blockPos);
    }

    public boolean canSpawnGolem(LevelReader levelReader, BlockPos blockPos) {
        return (getOrCreateSnowGolemBase().find(levelReader, blockPos) == null && getOrCreateIronGolemBase().find(levelReader, blockPos) == null) ? false : true;
    }

    private void trySpawnGolem(Level level, BlockPos blockPos) {
        BlockPattern.BlockPatternMatch find = getOrCreateSnowGolemFull().find(level, blockPos);
        if (find != null) {
            for (int i = 0; i < getOrCreateSnowGolemFull().getHeight(); i++) {
                BlockInWorld block = find.getBlock(0, i, 0);
                level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, block.getPos(), Block.getId(block.getState()));
            }
            SnowGolem create = EntityType.SNOW_GOLEM.create(level);
            BlockPos pos = find.getBlock(0, 2, 0).getPos();
            create.moveTo(pos.getX() + 0.5d, pos.getY() + 0.05d, pos.getZ() + 0.5d, 0.0f, 0.0f);
            level.addFreshEntity(create);
            Iterator it = level.getEntitiesOfClass(ServerPlayer.class, create.getBoundingBox().inflate(5.0d)).iterator();
            while (it.hasNext()) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) it.next(), create);
            }
            for (int i2 = 0; i2 < getOrCreateSnowGolemFull().getHeight(); i2++) {
                level.blockUpdated(find.getBlock(0, i2, 0).getPos(), Blocks.AIR);
            }
            return;
        }
        BlockPattern.BlockPatternMatch find2 = getOrCreateIronGolemFull().find(level, blockPos);
        if (find2 != null) {
            for (int i3 = 0; i3 < getOrCreateIronGolemFull().getWidth(); i3++) {
                for (int i4 = 0; i4 < getOrCreateIronGolemFull().getHeight(); i4++) {
                    BlockInWorld block2 = find2.getBlock(i3, i4, 0);
                    level.setBlock(block2.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    level.levelEvent(2001, block2.getPos(), Block.getId(block2.getState()));
                }
            }
            BlockPos pos2 = find2.getBlock(1, 2, 0).getPos();
            IronGolem create2 = EntityType.IRON_GOLEM.create(level);
            create2.setPlayerCreated(true);
            create2.moveTo(pos2.getX() + 0.5d, pos2.getY() + 0.05d, pos2.getZ() + 0.5d, 0.0f, 0.0f);
            level.addFreshEntity(create2);
            Iterator it2 = level.getEntitiesOfClass(ServerPlayer.class, create2.getBoundingBox().inflate(5.0d)).iterator();
            while (it2.hasNext()) {
                CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) it2.next(), create2);
            }
            for (int i5 = 0; i5 < getOrCreateIronGolemFull().getWidth(); i5++) {
                for (int i6 = 0; i6 < getOrCreateIronGolemFull().getHeight(); i6++) {
                    level.blockUpdated(find2.getBlock(i5, i6, 0).getPos(), Blocks.AIR);
                }
            }
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private BlockPattern getOrCreateSnowGolemBase() {
        if (this.snowGolemBase == null) {
            this.snowGolemBase = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemBase;
    }

    private BlockPattern getOrCreateSnowGolemFull() {
        if (this.snowGolemFull == null) {
            this.snowGolemFull = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
        }
        return this.snowGolemFull;
    }

    private BlockPattern getOrCreateIronGolemBase() {
        if (this.ironGolemBase == null) {
            this.ironGolemBase = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return this.ironGolemBase;
    }

    private BlockPattern getOrCreateIronGolemFull() {
        if (this.ironGolemFull == null) {
            this.ironGolemFull = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return this.ironGolemFull;
    }
}
