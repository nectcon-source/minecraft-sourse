package net.minecraft.world.level.block;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WitherSkullBlock.class */
public class WitherSkullBlock extends SkullBlock {

    @Nullable
    private static BlockPattern witherPatternFull;

    @Nullable
    private static BlockPattern witherPatternBase;

    protected WitherSkullBlock(BlockBehaviour.Properties properties) {
        super(SkullBlock.Types.WITHER_SKELETON, properties);
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SkullBlockEntity) {
            checkSpawn(level, blockPos, (SkullBlockEntity) blockEntity);
        }
    }

    public static void checkSpawn(Level level, BlockPos blockPos, SkullBlockEntity skullBlockEntity) {
        BlockPattern orCreateWitherFull;
        BlockPattern.BlockPatternMatch find;
        if (level.isClientSide) {
            return;
        }
        BlockState blockState = skullBlockEntity.getBlockState();
        if (!(blockState.is(Blocks.WITHER_SKELETON_SKULL) || blockState.is(Blocks.WITHER_SKELETON_WALL_SKULL)) || blockPos.getY() < 0 || level.getDifficulty() == Difficulty.PEACEFUL || (find = (orCreateWitherFull = getOrCreateWitherFull()).find(level, blockPos)) == null) {
            return;
        }
        for (int i = 0; i < orCreateWitherFull.getWidth(); i++) {
            for (int i2 = 0; i2 < orCreateWitherFull.getHeight(); i2++) {
                BlockInWorld block = find.getBlock(i, i2, 0);
                level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, block.getPos(), Block.getId(block.getState()));
            }
        }
        WitherBoss create = EntityType.WITHER.create(level);
        BlockPos pos = find.getBlock(1, 2, 0).getPos();
        create.moveTo(pos.getX() + 0.5d, pos.getY() + 0.55d, pos.getZ() + 0.5d, find.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f, 0.0f);
        create.yBodyRot = find.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f;
        create.makeInvulnerable();
        Iterator it = level.getEntitiesOfClass(ServerPlayer.class, create.getBoundingBox().inflate(50.0d)).iterator();
        while (it.hasNext()) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) it.next(), create);
        }
        level.addFreshEntity(create);
        for (int i3 = 0; i3 < orCreateWitherFull.getWidth(); i3++) {
            for (int i4 = 0; i4 < orCreateWitherFull.getHeight(); i4++) {
                level.blockUpdated(find.getBlock(i3, i4, 0).getPos(), Blocks.AIR);
            }
        }
    }

    public static boolean canSpawnMob(Level level, BlockPos blockPos, ItemStack itemStack) {
        return (itemStack.getItem() != Items.WITHER_SKELETON_SKULL || blockPos.getY() < 2 || level.getDifficulty() == Difficulty.PEACEFUL || level.isClientSide || getOrCreateWitherBase().find(level, blockPos) == null) ? false : true;
    }

    private static BlockPattern getOrCreateWitherFull() {
        if (witherPatternFull == null) {
            witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', blockInWorld -> {
                return blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
            }).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return witherPatternFull;
    }

    private static BlockPattern getOrCreateWitherBase() {
        if (witherPatternBase == null) {
            witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', blockInWorld -> {
                return blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
            }).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return witherPatternBase;
    }
}
