package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
   private static final Splitter SPLITTER = Splitter.on(';').limit(5);
   private static final Splitter LAYER_SPLITTER = Splitter.on(',');
   private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
   private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
   private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

   public LevelFlatGeneratorInfoFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), var1 -> var1.update(DSL.remainderFinder(), this::fix)
      );
   }

   private Dynamic<?> fix(Dynamic<?> var1) {
      return var1.get("generatorName").asString("").equalsIgnoreCase("flat")
         ? var1.update("generatorOptions", var1x -> (Dynamic)DataFixUtils.orElse(var1x.asString().map(this::fixString).map(var1x::createString).result(), var1x))
         : var1;
   }

   @VisibleForTesting
   String fixString(String var1) {
      if (var1.isEmpty()) {
         return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
      } else {
         Iterator<String> var2xx = SPLITTER.split(var1).iterator();
         String var3xxx = var2xx.next();
         int var4;
         String var5x;
         if (var2xx.hasNext()) {
            var4 = NumberUtils.toInt(var3xxx, 0);
            var5x = var2xx.next();
         } else {
            var4 = 0;
            var5x = var3xxx;
         }

         if (var4 >= 0 && var4 <= 3) {
            StringBuilder var6 = new StringBuilder();
            Splitter var7x = var4 < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
            var6.append(StreamSupport.<String>stream(LAYER_SPLITTER.split(var5x).spliterator(), false).map(var2x -> {
               List<String> var5xx = var7x.splitToList(var2x);
               int var3x;
               String var4xx;
               if (var5xx.size() == 2) {
                  var3x = NumberUtils.toInt((String)var5xx.get(0));
                  var4xx = (String)var5xx.get(1);
               } else {
                  var3x = 1;
                  var4xx = (String)var5xx.get(0);
               }

               List<String> var6x = BLOCK_SPLITTER.splitToList(var4xx);
               int var7xx = ((String)var6x.get(0)).equals("minecraft") ? 1 : 0;
               String var8xx = (String)var6x.get(var7xx);
               int var9xxx = var4 == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + var8xx) : NumberUtils.toInt(var8xx, 0);
               int var10xxxx = var7xx + 1;
               int var11xxxxx = var6x.size() > var10xxxx ? NumberUtils.toInt((String)var6x.get(var10xxxx), 0) : 0;
               return (var3x == 1 ? "" : var3x + "*") + BlockStateData.getTag(var9xxx << 4 | var11xxxxx).get("Name").asString("");
            }).collect(Collectors.joining(",")));

            while(var2xx.hasNext()) {
               var6.append(';').append(var2xx.next());
            }

            return var6.toString();
         } else {
            return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
         }
      }
   }
}
