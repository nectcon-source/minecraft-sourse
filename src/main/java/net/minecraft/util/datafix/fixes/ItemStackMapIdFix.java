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
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackMapIdFix extends DataFix {
   public ItemStackMapIdFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> var2 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> var3xx = var1.findField("tag");
      return this.fixTypeEverywhereTyped("ItemInstanceMapIdFix", var1, var2x -> {
         Optional<Pair<String, String>> var3x = var2x.getOptional(var2);
         if (var3x.isPresent() && Objects.equals(((Pair)var3x.get()).getSecond(), "minecraft:filled_map")) {
            Dynamic<?> var4x = (Dynamic)var2x.get(DSL.remainderFinder());
            Typed<?> var5xx = var2x.getOrCreateTyped(var3xx);
            Dynamic<?> var6xxx = (Dynamic)var5xx.get(DSL.remainderFinder());
            var6xxx = var6xxx.set("map", var6xxx.createInt(var4x.get("Damage").asInt(0)));
            return var2x.set(var3xx, var5xx.set(DSL.remainderFinder(), var6xxx));
         } else {
            return var2x;
         }
      });
   }
}
