package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ChunkBiomeFix extends DataFix {
   public ChunkBiomeFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> var2 = var1.findField("Level");
      return this.fixTypeEverywhereTyped("Leaves fix", var1, var1x -> var1x.updateTyped(var2, var0x -> var0x.update(DSL.remainderFinder(), var0xx -> {
               Optional<IntStream> var1xx = var0xx.get("Biomes").asIntStreamOpt().result();
               if (!var1xx.isPresent()) {
                  return var0xx;
               } else {
                  int[] var2x = ((IntStream)var1xx.get()).toArray();
                  int[] var3x = new int[1024];

                  for(int var4xx = 0; var4xx < 4; ++var4xx) {
                     for(int var5xxx = 0; var5xxx < 4; ++var5xxx) {
                        int var6xxxx = (var5xxx << 2) + 2;
                        int var7xxxxx = (var4xx << 2) + 2;
                        int var8xxxxxx = var7xxxxx << 4 | var6xxxx;
                        var3x[var4xx << 2 | var5xxx] = var8xxxxxx < var2x.length ? var2x[var8xxxxxx] : -1;
                     }
                  }

                  for(int var9xx = 1; var9xx < 64; ++var9xx) {
                     System.arraycopy(var3x, 0, var3x, var9xx * 16, 16);
                  }

                  return var0xx.set("Biomes", var0xx.createIntList(Arrays.stream(var3x)));
               }
            })));
   }
}
