package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class EntityPaintingItemFrameDirectionFix extends DataFix {
   private static final int[][] DIRECTIONS = new int[][]{{0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {1, 0, 0}};

   public EntityPaintingItemFrameDirectionFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   private Dynamic<?> doFix(Dynamic<?> var1, boolean var2, boolean var3) {
      if ((var2 || var3) && !var1.get("Facing").asNumber().result().isPresent()) {
         int var4;
         if (var1.get("Direction").asNumber().result().isPresent()) {
            var4 = var1.get("Direction").asByte((byte)0) % DIRECTIONS.length;
            int[] var5x = DIRECTIONS[var4];
            var1 = var1.set("TileX", var1.createInt(var1.get("TileX").asInt(0) + var5x[0]));
            var1 = var1.set("TileY", var1.createInt(var1.get("TileY").asInt(0) + var5x[1]));
            var1 = var1.set("TileZ", var1.createInt(var1.get("TileZ").asInt(0) + var5x[2]));
            var1 = var1.remove("Direction");
            if (var3 && var1.get("ItemRotation").asNumber().result().isPresent()) {
               var1 = var1.set("ItemRotation", var1.createByte((byte)(var1.get("ItemRotation").asByte((byte)0) * 2)));
            }
         } else {
            var4 = var1.get("Dir").asByte((byte)0) % DIRECTIONS.length;
            var1 = var1.remove("Dir");
         }

         var1 = var1.set("Facing", var1.createByte((byte)var4));
      }

      return var1;
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getChoiceType(References.ENTITY, "Painting");
      OpticFinder<?> var2x = DSL.namedChoice("Painting", var1);
      Type<?> var3xx = this.getInputSchema().getChoiceType(References.ENTITY, "ItemFrame");
      OpticFinder<?> var4xxx = DSL.namedChoice("ItemFrame", var3xx);
      Type<?> var5xxxx = this.getInputSchema().getType(References.ENTITY);
      TypeRewriteRule var6xxxxx = this.fixTypeEverywhereTyped(
         "EntityPaintingFix", var5xxxx,
         var3x -> var3x.updateTyped(var2x, var1, var1xx -> var1xx.update(DSL.remainderFinder(), var1xxx -> this.doFix(var1xxx, true, false)))
      );
      TypeRewriteRule var7xxxxxx = this.fixTypeEverywhereTyped(
         "EntityItemFrameFix", var5xxxx,
         var3x -> var3x.updateTyped(var4xxx, var3xx, var1xx -> var1xx.update(DSL.remainderFinder(), var1xxx -> this.doFix(var1xxx, false, true)))
      );
      return TypeRewriteRule.seq(var6xxxxx, var7xxxxxx);
   }
}
