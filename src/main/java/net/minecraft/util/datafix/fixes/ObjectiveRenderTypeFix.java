package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveRenderTypeFix extends DataFix {
   public ObjectiveRenderTypeFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   private static ObjectiveCriteria.RenderType getRenderType(String var0) {
      return var0.equals("health") ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER;
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> var1 = DSL.named(References.OBJECTIVE.typeName(), DSL.remainderType());
      if (!Objects.equals(var1, this.getInputSchema().getType(References.OBJECTIVE))) {
         throw new IllegalStateException("Objective type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("ObjectiveRenderTypeFix", var1, var0 -> var0x -> var0x.mapSecond(var0xx -> {
                  Optional<String> var1x = var0xx.get("RenderType").asString().result();
                  if (!var1x.isPresent()) {
                     String var2x = var0xx.get("CriteriaName").asString("");
                     ObjectiveCriteria.RenderType var3xx = getRenderType(var2x);
                     return var0xx.set("RenderType", var0xx.createString(var3xx.getId()));
                  } else {
                     return var0xx;
                  }
               }));
      }
   }
}
