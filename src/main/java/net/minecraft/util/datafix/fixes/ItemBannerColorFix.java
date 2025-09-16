package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
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
import java.util.stream.Stream;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix extends DataFix {
   public ItemBannerColorFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> var2x = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> var3xx = var1.findField("tag");
      OpticFinder<?> var4xxx = var3xx.type().findField("BlockEntityTag");
      return this.fixTypeEverywhereTyped(
         "ItemBannerColorFix", var1,
         var3x -> {
            Optional<Pair<String, String>> var4x = var3x.getOptional(var2x);
            if (var4x.isPresent() && Objects.equals(((Pair)var4x.get()).getSecond(), "minecraft:banner")) {
               Dynamic<?> var5x = (Dynamic)var3x.get(DSL.remainderFinder());
               Optional<? extends Typed<?>> var6xx = var3x.getOptionalTyped(var3xx);
               if (var6xx.isPresent()) {
                  Typed<?> var7xxx = (Typed)var6xx.get();
                  Optional<? extends Typed<?>> var8xxxx = var7xxx.getOptionalTyped(var4xxx);
                  if (var8xxxx.isPresent()) {
                     Typed<?> var9xxxxx = (Typed)var8xxxx.get();
                     Dynamic<?> var10xxxxxx = (Dynamic)var7xxx.get(DSL.remainderFinder());
                     Dynamic<?> var11xxxxxxx = (Dynamic)var9xxxxx.getOrCreate(DSL.remainderFinder());
                     if (var11xxxxxxx.get("Base").asNumber().result().isPresent()) {
                        var5x = var5x.set("Damage", var5x.createShort((short)(var11xxxxxxx.get("Base").asInt(0) & 15)));
                        Optional<? extends Dynamic<?>> var12xxxxxxxx = var10xxxxxx.get("display").result();
                        if (var12xxxxxxxx.isPresent()) {
                           Dynamic<?> var13xxxxxxxxx = (Dynamic)var12xxxxxxxx.get();
                           Dynamic<?> var14xxxxxxxxxx = var13xxxxxxxxx.createMap(
                              ImmutableMap.of(var13xxxxxxxxx.createString("Lore"), var13xxxxxxxxx.createList(Stream.of(var13xxxxxxxxx.createString("(+NBT"))))
                           );
                           if (Objects.equals(var13xxxxxxxxx, var14xxxxxxxxxx)) {
                              return var3x.set(DSL.remainderFinder(), var5x);
                           }
                        }

                        var11xxxxxxx.remove("Base");
                        return var3x.set(DSL.remainderFinder(), var5x).set(var3xx, var7xxx.set(var4xxx, var9xxxxx.set(DSL.remainderFinder(), var11xxxxxxx)));
                     }
                  }
               }
   
               return var3x.set(DSL.remainderFinder(), var5x);
            } else {
               return var3x;
            }
         }
      );
   }
}
