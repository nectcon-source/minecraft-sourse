package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemWaterPotionFix extends DataFix {
   public ItemWaterPotionFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> var2 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> var3xx = var1.findField("tag");
      return this.fixTypeEverywhereTyped(
         "ItemWaterPotionFix", var1,
         var2x -> {
            Optional<Pair<String, String>> var3x = var2x.getOptional(var2);
            if (var3x.isPresent()) {
               String var4x = (String)((Pair)var3x.get()).getSecond();
               if ("minecraft:potion".equals(var4x)
                  || "minecraft:splash_potion".equals(var4x)
                  || "minecraft:lingering_potion".equals(var4x)
                  || "minecraft:tipped_arrow".equals(var4x)) {
                  Typed<?> var5xx = var2x.getOrCreateTyped(var3xx);
                  Dynamic<?> var6xxx = (Dynamic)var5xx.get(DSL.remainderFinder());
                  if (!var6xxx.get("Potion").asString().result().isPresent()) {
                     var6xxx = var6xxx.set("Potion", var6xxx.createString("minecraft:water"));
                  }
   
                  return var2x.set(var3xx, var5xx.set(DSL.remainderFinder(), var6xxx));
               }
            }
   
            return var2x;
         }
      );
   }
}
