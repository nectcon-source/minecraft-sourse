package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix extends DataFix {
   public NewVillageFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      CompoundListType<String, ?> var1 = DSL.compoundList(DSL.string(), this.getInputSchema().getType(References.STRUCTURE_FEATURE));
      OpticFinder<? extends List<? extends Pair<String, ?>>> var2x = var1.finder();
      return this.cap(var1);
   }

   private <SF> TypeRewriteRule cap(CompoundListType<String, SF> var1) {
      Type<?> var2 = this.getInputSchema().getType(References.CHUNK);
      Type<?> var3x = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
      OpticFinder<?> var4xx = var2.findField("Level");
      OpticFinder<?> var5xxx = var4xx.type().findField("Structures");
      OpticFinder<?> var6xxxx = var5xxx.type().findField("Starts");
      OpticFinder<List<Pair<String, SF>>> var7xxxxx = var1.finder();
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(
            "NewVillageFix", var2,
            var4x -> var4x.updateTyped(var4xx,
                  var3xx -> var3xx.updateTyped(var5xxx,
                        var2xxx -> var2xxx.updateTyped(var6xxxx,
                                 var1xxxx -> var1xxxx.update(var7xxxxx,
                                       var0xxxx -> var0xxxx.stream()
                                             .filter(var0xxxxx -> !Objects.equals(var0xxxxx.getFirst(), "Village"))
                                             .map(var0xxxxx -> var0xxxxx.mapFirst(var0xxxxxx -> var0xxxxxx.equals("New_Village") ? "Village" : var0xxxxxx))
                                             .collect(Collectors.toList())
                                    )
                              )
                              .update(
                                 DSL.remainderFinder(),
                                 var0xxx -> var0xxx.update(
                                       "References",
                                       var0xxxx -> {
                                          Optional<? extends Dynamic<?>> var1xxxx = var0xxxx.get("New_Village").result();
                                          return ((Dynamic)DataFixUtils.orElse(
                                                var1xxxx.map(var1xxxxx -> var0xxxx.remove("New_Village").set("Village", var1xxxxx)), var0xxxx
                                             ))
                                             .remove("Village");
                                       }
                                    )
                              )
                     )
               )
         ),
         this.fixTypeEverywhereTyped(
            "NewVillageStartFix",
                 var3x,
            var0 -> var0.update(
                  DSL.remainderFinder(),
                  var0x -> var0x.update(
                        "id",
                        var0xx -> Objects.equals(NamespacedSchema.ensureNamespaced(var0xx.asString("")), "minecraft:new_village")
                              ? var0xx.createString("minecraft:village")
                              : var0xx
                     )
               )
         )
      );
   }
}
