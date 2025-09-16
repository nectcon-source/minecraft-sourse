//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
   private static final ResourceLocation ID = new ResourceLocation("effects_changed");

   @Override
   public ResourceLocation getId() {
      return ID;
   }

   public TriggerInstance createInstance(JsonObject var1, EntityPredicate.Composite var2, DeserializationContext var3) {
      MobEffectsPredicate var4 = MobEffectsPredicate.fromJson(var1.get("effects"));
      return new TriggerInstance(var2, var4);
   }

   public void trigger(ServerPlayer var1) {
      this.trigger(var1, (var1x) -> var1x.matches(var1));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MobEffectsPredicate effects;

      public TriggerInstance(EntityPredicate.Composite var1, MobEffectsPredicate var2) {
         super(EffectsChangedTrigger.ID, var1);
         this.effects = var2;
      }

      public static TriggerInstance hasEffects(MobEffectsPredicate var0) {
         return new TriggerInstance(Composite.ANY, var0);
      }

      public boolean matches(ServerPlayer var1) {
         return this.effects.matches(var1);
      }

      public JsonObject serializeToJson(SerializationContext var1) {
         JsonObject var2 = super.serializeToJson(var1);
         var2.add("effects", this.effects.serializeToJson());
         return var2;
      }
   }
}
