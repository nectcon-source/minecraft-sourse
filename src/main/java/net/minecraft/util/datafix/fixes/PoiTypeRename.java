package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;

public abstract class PoiTypeRename extends DataFix {
   public PoiTypeRename(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> var1 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
      if (!Objects.equals(var1, this.getInputSchema().getType(References.POI_CHUNK))) {
         throw new IllegalStateException("Poi type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("POI rename", var1, var1x -> var1xx -> var1xx.mapSecond(this::cap));
      }
   }

   private <T> Dynamic<T> cap(Dynamic<T> var1) {
      return var1.update(
         "Sections",
         var1x -> var1x.updateMapValues(
               var1xx -> var1xx.mapSecond(
                     var1xxx -> var1xxx.update("Records", var1xxxx -> (Dynamic)DataFixUtils.orElse(this.renameRecords(var1xxxx), var1xxxx))
                  )
            )
      );
   }

   private <T> Optional<Dynamic<T>> renameRecords(Dynamic<T> var1) {
      return var1.asStreamOpt()
         .map(
            var2 -> var1.createList(
                  var2.map(
                     var1xx -> var1xx.update(
                           "type", var1xxx -> (Dynamic)DataFixUtils.orElse(var1xxx.asString().map(this::rename).map(var1xxx::createString).result(), var1xxx)
                        )
                  )
               )
         )
         .result();
   }

   protected abstract String rename(String var1);
}
