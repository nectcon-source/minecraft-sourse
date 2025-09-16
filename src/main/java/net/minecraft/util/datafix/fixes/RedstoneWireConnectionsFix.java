package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RedstoneWireConnectionsFix extends DataFix {
   public RedstoneWireConnectionsFix(Schema var1) {
      super(var1, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema var1 = this.getInputSchema();
      return this.fixTypeEverywhereTyped(
         "RedstoneConnectionsFix", var1.getType(References.BLOCK_STATE), var1x -> var1x.update(DSL.remainderFinder(), this::updateRedstoneConnections)
      );
   }

   private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> var1) {
      boolean var2 = var1.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
      return !var2 ? var1 : var1.update(
            "Properties",
            var0 -> {
               String var1x = var0.get("east").asString("none");
               String var2x = var0.get("west").asString("none");
               String var3xx = var0.get("north").asString("none");
               String var4xxx = var0.get("south").asString("none");
               boolean var5xxxx = isConnected(var1x) || isConnected(var2x);
               boolean var6xxxxx = isConnected(var3xx) || isConnected(var4xxx);
               String var7xxxxxx = !isConnected(var1x) && !var6xxxxx ? "side" : var1x;
               String var8xxxxxxx = !isConnected(var2x) && !var6xxxxx ? "side" : var2x;
               String var9xxxxxxxx = !isConnected(var3xx) && !var5xxxx ? "side" : var3xx;
               String var10xxxxxxxxx = !isConnected(var4xxx) && !var5xxxx ? "side" : var4xxx;
               return var0.update("east", var1xx -> var1xx.createString(var7xxxxxx))
                  .update("west", var1xx -> var1xx.createString(var8xxxxxxx))
                  .update("north", var1xx -> var1xx.createString(var9xxxxxxxx))
                  .update("south", var1xx -> var1xx.createString(var10xxxxxxxxx));
            }
         );
   }

   private static boolean isConnected(String var0) {
      return !"none".equals(var0);
   }
}
