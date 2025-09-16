package net.minecraft.world.item;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BoneMealItem.class */
public class BoneMealItem extends Item {
    public BoneMealItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockPos relative = clickedPos.relative(useOnContext.getClickedFace());
        if (growCrop(useOnContext.getItemInHand(), level, clickedPos)) {
            if (!level.isClientSide) {
                level.levelEvent(2005, clickedPos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockState(clickedPos).isFaceSturdy(level, clickedPos, useOnContext.getClickedFace()) && growWaterPlant(useOnContext.getItemInHand(), level, relative, useOnContext.getClickedFace())) {
            if (!level.isClientSide) {
                level.levelEvent(2005, relative, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BonemealableBlock) {
            BonemealableBlock bonemealableBlock = (BonemealableBlock) blockState.getBlock();
            if (bonemealableBlock.isValidBonemealTarget(level, blockPos, blockState, level.isClientSide)) {
                if (level instanceof ServerLevel) {
                    if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
                        bonemealableBlock.performBonemeal((ServerLevel) level, level.random, blockPos, blockState);
                    }
                    itemStack.shrink(1);
                    return true;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (!level.getBlockState(blockPos).is(Blocks.WATER) || level.getFluidState(blockPos).getAmount() != 8) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        for (int i = 0; i < 128; i++) {
            BlockPos blockPos2 = blockPos;
            BlockState defaultBlockState = Blocks.SEAGRASS.defaultBlockState();
            int i2 = 0;
            while (true) {
                if (i2 < i / 16) {
                    blockPos2 = blockPos2.offset(random.nextInt(3) - 1, ((random.nextInt(3) - 1) * random.nextInt(3)) / 2, random.nextInt(3) - 1);
                    if (level.getBlockState(blockPos2).isCollisionShapeFullBlock(level, blockPos2)) {
                        break;
                    }
                    i2++;
                } else {
                    Optional<ResourceKey<Biome>> biomeName = level.getBiomeName(blockPos2);
                    if (Objects.equals(biomeName, Optional.of(Biomes.WARM_OCEAN)) || Objects.equals(biomeName, Optional.of(Biomes.DEEP_WARM_OCEAN))) {
                        if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
                            defaultBlockState = (BlockState) BlockTags.WALL_CORALS.getRandomElement(level.random).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, direction);
                        } else if (random.nextInt(4) == 0) {
                            defaultBlockState = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
                        }
                    }
                    if (defaultBlockState.getBlock().is(BlockTags.WALL_CORALS)) {
                        for (int i3 = 0; !defaultBlockState.canSurvive(level, blockPos2) && i3 < 4; i3++) {
                            defaultBlockState = (BlockState) defaultBlockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                        }
                    }
                    if (defaultBlockState.canSurvive(level, blockPos2)) {
                        BlockState blockState = level.getBlockState(blockPos2);
                        if (blockState.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
                            level.setBlock(blockPos2, defaultBlockState, 3);
                        } else if (blockState.is(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
                            ((BonemealableBlock) Blocks.SEAGRASS).performBonemeal((ServerLevel) level, random, blockPos2, blockState);
                        }
                    }
                }
            }
        }
        itemStack.shrink(1);
        return true;
    }

    public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        double max;
        if (i == 0) {
            i = 15;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.isAir()) {
            return;
        }
        double d = 0.5d;
        if (blockState.is(Blocks.WATER)) {
            i *= 3;
            max = 1.0d;
            d = 3.0d;
        } else if (blockState.isSolidRender(levelAccessor, blockPos)) {
            blockPos = blockPos.above();
            i *= 3;
            d = 3.0d;
            max = 1.0d;
        } else {
            max = blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
        }
        levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 0.0d, 0.0d, 0.0d);
        for (int i2 = 0; i2 < i; i2++) {
            double nextGaussian = random.nextGaussian() * 0.02d;
            double nextGaussian2 = random.nextGaussian() * 0.02d;
            double nextGaussian3 = random.nextGaussian() * 0.02d;
            double d2 = 0.5d - d;
            double x = blockPos.getX() + d2 + (random.nextDouble() * d * 2.0d);
            double y = blockPos.getY() + (random.nextDouble() * max);
            double z = blockPos.getZ() + d2 + (random.nextDouble() * d * 2.0d);
            if (!levelAccessor.getBlockState(new BlockPos(x, y, z).below()).isAir()) {
                levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, nextGaussian, nextGaussian2, nextGaussian3);
            }
        }
    }
}
