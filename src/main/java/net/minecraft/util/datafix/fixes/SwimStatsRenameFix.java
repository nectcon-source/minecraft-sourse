package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SwimStatsRenameFix extends DataFix {
   public SwimStatsRenameFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> var1 = this.getOutputSchema().getType(References.STATS);
      Type<?> var2x = this.getInputSchema().getType(References.STATS);
      OpticFinder<?> var3xx = var2x.findField("stats");
      OpticFinder<?> var4xxx = var3xx.type().findField("minecraft:custom");
      OpticFinder<String> var5xxxx = NamespacedSchema.namespacedString().finder();
      return this.fixTypeEverywhereTyped(
         "SwimStatsRenameFix", var2x, var1, var3x -> var3x.updateTyped(var3xx, var2xx -> var2xx.updateTyped(var4xxx, var1xxx -> var1xxx.update(var5xxxx, var0xxx -> {
                     if (var0xxx.equals("minecraft:swim_one_cm")) {
                        return "minecraft:walk_on_water_one_cm";
                     } else {
                        return var0xxx.equals("minecraft:dive_one_cm") ? "minecraft:walk_under_water_one_cm" : var0xxx;
                     }
                  })))
      );
   }
}
