package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ReorganizePoi extends DataFix {
   public ReorganizePoi(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> var1 = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
      if (!Objects.equals(var1, this.getInputSchema().getType(References.POI_CHUNK))) {
         throw new IllegalStateException("Poi type is not what was expected.");
      } else {
         return this.fixTypeEverywhere("POI reorganization", var1, var0 -> var0x -> var0x.mapSecond(ReorganizePoi::cap));
      }
   }

   private static <T> Dynamic<T> cap(Dynamic<T> var0) {
      Map<Dynamic<T>, Dynamic<T>> var1 = Maps.newHashMap();

      for(int var2x = 0; var2x < 16; ++var2x) {
         String var3xx = String.valueOf(var2x);
         Optional<Dynamic<T>> var4xxx = var0.get(var3xx).result();
         if (var4xxx.isPresent()) {
            Dynamic<T> var5xxxx = (Dynamic)var4xxx.get();
            Dynamic<T> var6xxxxx = var0.createMap(ImmutableMap.of(var0.createString("Records"), var5xxxx));
            var1.put(var0.createInt(var2x), var6xxxxx);
            var0 = var0.remove(var3xx);
         }
      }

      return var0.set("Sections", var0.createMap(var1));
   }
}
