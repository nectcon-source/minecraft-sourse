package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SocializeAtBell extends Behavior<LivingEntity> {
   public SocializeAtBell() {
      super(
         ImmutableMap.of(
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.MEETING_POINT,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.VISIBLE_LIVING_ENTITIES,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryStatus.VALUE_ABSENT
         )
      );
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel var1, LivingEntity var2) {
      Brain<?> var3 = var2.getBrain();
      Optional<GlobalPos> var4 = var3.getMemory(MemoryModuleType.MEETING_POINT);
      return var1.getRandom().nextInt(100) == 0 && var4.isPresent() && var1.dimension() == ((GlobalPos)var4.get()).dimension() && ((GlobalPos)var4.get()).pos().closerThan(var2.position(), 4.0F) && (var3.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get()).stream().anyMatch((var0) -> EntityType.VILLAGER.equals(var0.getType()));
   }

   @Override
   protected void start(ServerLevel var1, LivingEntity var2, long var3) {
      Brain<?> var5 = var2.getBrain();
      var5.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
         .ifPresent(
            var2x -> var2x.stream()
                  .filter(var0x -> EntityType.VILLAGER.equals(var0x.getType()))
                  .filter(var1xx -> var1xx.distanceToSqr(var2) <= 32.0)
                  .findFirst()
                  .ifPresent(var1xx -> {
                     var5.setMemory(MemoryModuleType.INTERACTION_TARGET, var1xx);
                     var5.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(var1xx, true));
                     var5.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(var1xx, false), 0.3F, 1));
                  })
         );
   }
}
