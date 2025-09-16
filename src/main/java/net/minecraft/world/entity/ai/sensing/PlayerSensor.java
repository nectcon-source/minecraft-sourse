package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/PlayerSensor.class */
public class PlayerSensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.getClass();
        List<Player> var3 = serverLevel.players().stream().filter(EntitySelector.NO_SPECTATORS).filter((var1x) -> livingEntity.closerThan(var1x, (double)16.0F)).sorted(Comparator.comparingDouble(livingEntity::distanceToSqr)).collect(Collectors.toList());
        Brain<?> var4 = livingEntity.getBrain();
        var4.setMemory(MemoryModuleType.NEAREST_PLAYERS, var3);
        List<Player> var5 = var3.stream().filter((var1x) -> isEntityTargetable(livingEntity, var1x)).collect(Collectors.toList());
        var4.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, var5.isEmpty() ? null : (Player)var5.get(0));
        Optional<Player> var6 = var5.stream().filter(EntitySelector.ATTACK_ALLOWED).findFirst();
        var4.setMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, var6);
    }
}
