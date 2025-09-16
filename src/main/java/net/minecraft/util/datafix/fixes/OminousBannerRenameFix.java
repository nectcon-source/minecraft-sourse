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

public class OminousBannerRenameFix extends DataFix {
   public OminousBannerRenameFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   private Dynamic<?> fixTag(Dynamic<?> var1) {
      Optional<? extends Dynamic<?>> var2 = var1.get("display").result();
      if (var2.isPresent()) {
         Dynamic<?> var3x = (Dynamic)var2.get();
         Optional<String> var4xx = var3x.get("Name").asString().result();
         if (var4xx.isPresent()) {
            String var5xxx = var4xx.get();
            var5xxx = var5xxx.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
            var3x = var3x.set("Name", var3x.createString(var5xxx));
         }

         return var1.set("display", var3x);
      } else {
         return var1;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> var2x = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> var3xx = var1.findField("tag");
      return this.fixTypeEverywhereTyped("OminousBannerRenameFix", var1, var3x -> {
         Optional<Pair<String, String>> var4 = var3x.getOptional(var2x);
         if (var4.isPresent() && Objects.equals(((Pair)var4.get()).getSecond(), "minecraft:white_banner")) {
            Optional<? extends Typed<?>> var5x = var3x.getOptionalTyped(var3xx);
            if (var5x.isPresent()) {
               Typed<?> var6xx = (Typed)var5x.get();
               Dynamic<?> var7xxx = (Dynamic)var6xx.get(DSL.remainderFinder());
               return var3x.set(var3xx, var6xx.set(DSL.remainderFinder(), this.fixTag(var7xxx)));
            }
         }

         return var3x;
      });
   }
}
