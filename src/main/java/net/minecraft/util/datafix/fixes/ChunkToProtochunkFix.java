package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkToProtochunkFix extends DataFix {
   public ChunkToProtochunkFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.CHUNK);
      Type<?> var2x = this.getOutputSchema().getType(References.CHUNK);
      Type<?> var3xx = var1.findFieldType("Level");
      Type<?> var4xxx = var2x.findFieldType("Level");
      Type<?> var5xxxx = var3xx.findFieldType("TileTicks");
      OpticFinder<?> var6xxxxx = DSL.fieldFinder("Level", var3xx);
      OpticFinder<?> var7xxxxxx = DSL.fieldFinder("TileTicks", var5xxxx);
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(
            "ChunkToProtoChunkFix",var1,
            this.getOutputSchema().getType(References.CHUNK),
            var3x -> var3x.updateTyped(var6xxxxx,var4xxx,
                  var2xx -> {
                     Optional<? extends Stream<? extends Dynamic<?>>> var3x_ = var2xx.getOptionalTyped(var7xxxxxx)
                        .flatMap(var0xx -> var0xx.write().result())
                        .flatMap(var0xx -> var0xx.asStreamOpt().result());
                     Dynamic<?> var8_4xx = (Dynamic)var2xx.get(DSL.remainderFinder());
                     boolean var5xxx = var8_4xx.get("TerrainPopulated").asBoolean(false)
                        && (!var8_4xx.get("LightPopulated").asNumber().result().isPresent() || var8_4xx.get("LightPopulated").asBoolean(false));
                      var8_4xx = var8_4xx.set("Status", var8_4xx.createString(var5xxx ? "mobs_spawned" : "empty"));
                      var8_4xx = var8_4xx.set("hasLegacyStructureData", var8_4xx.createBoolean(true));
                     Dynamic var6x;
                     if (var5xxx) {
                        Optional<ByteBuffer> var7xxxxx = var8_4xx.get("Biomes").asByteBufferOpt().result();
                        if (var7xxxxx.isPresent()) {
                           ByteBuffer var8xxxxx = (ByteBuffer)var7xxxxx.get();
                           int[] var9xxxxxx = new int[256];

                           for(int var10xxxxxxx = 0; var10xxxxxxx < var9xxxxxx.length; ++var10xxxxxxx) {
                              if (var10xxxxxxx < var8xxxxx.capacity()) {
                                 var9xxxxxx[var10xxxxxxx] = var8xxxxx.get(var10xxxxxxx) & 255;
                              }
                           }

                            var8_4xx = var8_4xx.set("Biomes", var8_4xx.createIntList(Arrays.stream(var9xxxxxx)));
                        }

//                        Dynamic<?> ☃xxxx = ☃xx;
                         Dynamic<?> finalVar8_4xx = var8_4xx;
                        List<ShortList> var13xxxxx = IntStream.range(0, 16).mapToObj(var0xx -> new ShortArrayList()).collect(Collectors.toList());
                        if (var3x_.isPresent()) {
                           (var3x_.get()).forEach(var1xxx -> {
                              int var2xxx = var1xxx.get("x").asInt(0);
                              int var3x__ = var1xxx.get("y").asInt(0);
                              int var4xx = var1xxx.get("z").asInt(0);
                              short var5xxx_ = packOffsetCoordinates(var2xxx, var3x__, var4xx);
                              ((ShortList)var13xxxxx.get(var3x__ >> 4)).add(var5xxx_);
                           });

                            var8_4xx = var8_4xx.set("ToBeTicked", var8_4xx.createList(var13xxxxx.stream().map(var1xxx -> finalVar8_4xx.createList(var1xxx.stream().map(finalVar8_4xx::createShort)))));
                        }

                        var6x = (Dynamic)DataFixUtils.orElse(var2xx.set(DSL.remainderFinder(), var8_4xx).write().result(), var8_4xx);
                     } else {
                        var6x = var8_4xx;
                     }

                      try {
                          return (Typed)((Pair)var4xxx.readTyped(var6x).result().orElseThrow(() -> new IllegalStateException("Could not read the new chunk"))).getFirst();
                      } catch (Throwable e) {
                          throw new RuntimeException(e);
                      }
                  }
               )
         ),
         this.writeAndRead(
            "Structure biome inject", this.getInputSchema().getType(References.STRUCTURE_FEATURE), this.getOutputSchema().getType(References.STRUCTURE_FEATURE)
         )
      );
   }

   private static short packOffsetCoordinates(int var0, int var1, int var2) {
       return (short)(var0 & 15 | (var1 & 15) << 4 | (var2 & 15) << 8);
   }
}
