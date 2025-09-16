package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/PiglinSpecificSensor.class */
public class PiglinSpecificSensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, (Optional) findNearestRepellent(serverLevel, livingEntity));
        Optional<Mob> empty = Optional.empty();
        Optional<Hoglin> empty2 = Optional.empty();
        Optional<Hoglin> empty3 = Optional.empty();
        Optional<Piglin> empty4 = Optional.empty();
        Optional<LivingEntity> empty5 = Optional.empty();
        Optional<Player> empty6 = Optional.empty();
        Optional<Player> empty7 = Optional.empty();
        int i = 0;
        ArrayList newArrayList = Lists.newArrayList();
        ArrayList newArrayList2 = Lists.newArrayList();
        for (LivingEntity livingEntity2 :  brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (livingEntity2 instanceof Hoglin) {
                Hoglin hoglin = (Hoglin) livingEntity2;
                if (hoglin.isBaby() && !empty3.isPresent()) {
                    empty3 = Optional.of(hoglin);
                } else if (hoglin.isAdult()) {
                    i++;
                    if (!empty2.isPresent() && hoglin.canBeHunted()) {
                        empty2 = Optional.of(hoglin);
                    }
                }
            } else if (livingEntity2 instanceof PiglinBrute) {
                newArrayList.add((PiglinBrute) livingEntity2);
            } else if (livingEntity2 instanceof Piglin) {
                Piglin piglin = (Piglin) livingEntity2;
                if (piglin.isBaby() && !empty4.isPresent()) {
                    empty4 = Optional.of(piglin);
                } else if (piglin.isAdult()) {
                    newArrayList.add(piglin);
                }
            } else if (livingEntity2 instanceof Player) {
                Player player = (Player) livingEntity2;
                if (!empty6.isPresent() && EntitySelector.ATTACK_ALLOWED.test(livingEntity2) && !PiglinAi.isWearingGold(player)) {
                    empty6 = Optional.of(player);
                }
                if (!empty7.isPresent() && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player)) {
                    empty7 = Optional.of(player);
                }
            } else if (!empty.isPresent() && ((livingEntity2 instanceof WitherSkeleton) || (livingEntity2 instanceof WitherBoss))) {
                empty = Optional.of((Mob) livingEntity2);
            } else if (!empty5.isPresent() && PiglinAi.isZombified(livingEntity2.getType())) {
                empty5 = Optional.of(livingEntity2);
            }
        }
        for (LivingEntity livingEntity3 :  brain.getMemory(MemoryModuleType.LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if ((livingEntity3 instanceof AbstractPiglin) && ((AbstractPiglin) livingEntity3).isAdult()) {
                newArrayList2.add((AbstractPiglin) livingEntity3);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (Optional) empty);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, (Optional) empty2);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, (Optional) empty3);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, (Optional) empty5);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, (Optional) empty6);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, (Optional) empty7);
        brain.setMemory( MemoryModuleType.NEARBY_ADULT_PIGLINS,  newArrayList2);
        brain.setMemory( MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,  newArrayList);
        brain.setMemory( MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,  Integer.valueOf(newArrayList.size()));
        brain.setMemory( MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,  Integer.valueOf(i));
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, LivingEntity livingEntity) {
        return BlockPos.findClosestMatch(livingEntity.blockPosition(), 8, 4, blockPos -> {
            return isValidRepellent(serverLevel, blockPos);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isValidRepellent(ServerLevel serverLevel, BlockPos blockPos) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        boolean is = blockState.is(BlockTags.PIGLIN_REPELLENTS);
        if (is && blockState.is(Blocks.SOUL_CAMPFIRE)) {
            return CampfireBlock.isLitCampfire(blockState);
        }
        return is;
    }
}
