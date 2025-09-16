package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends NamedEntityFix {
   public VillagerTradeFix(Schema var1, boolean var2) {
      super(var1, var2, "Villager trade fix", References.ENTITY, "minecraft:villager");
   }

   @Override
   protected Typed<?> fix(Typed<?> var1) {
      OpticFinder<?> var2 = var1.getType().findField("Offers");
      OpticFinder<?> var3x = var2.type().findField("Recipes");
      Type<?> var4xx = var3x.type();
      if (!(var4xx instanceof ListType)) {
         throw new IllegalStateException("Recipes are expected to be a list.");
      } else {
         ListType<?> var5 = (ListType)var4xx;
         Type<?> var6x = var5.getElement();
         OpticFinder<?> var7xx = DSL.typeFinder(var6x);
         OpticFinder<?> var8xxx = var6x.findField("buy");
         OpticFinder<?> var9xxxx = var6x.findField("buyB");
         OpticFinder<?> var10xxxxx = var6x.findField("sell");
         OpticFinder<Pair<String, String>> var11xxxxxx = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
         Function<Typed<?>, Typed<?>> var12xxxxxxx = var2x -> this.updateItemStack(var11xxxxxx, var2x);
         return var1.updateTyped(
                 var2, var6x_ -> var6x_.updateTyped(var3x, var5xx -> var5xx.updateTyped(var7xx, var4xxx -> var4xxx.updateTyped(var8xxx, var12xxxxxxx).updateTyped(var9xxxx, var12xxxxxxx).updateTyped(var10xxxxx, var12xxxxxxx)))
         );
      }
   }

   private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> var1, Typed<?> var2) {
      return var2.update(var1, (var0) -> var0.mapSecond((var0x) -> Objects.equals(var0x, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : var0x));
   }
}
