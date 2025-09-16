package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockNameFlatteningFix extends DataFix {
   public BlockNameFlatteningFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.BLOCK_NAME);
      Type<?> var2x = this.getOutputSchema().getType(References.BLOCK_NAME);
      Type<Pair<String, Either<Integer, String>>> var3xx = DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString()));
      Type<Pair<String, String>> var4xxx = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
      if (Objects.equals(var1, var3xx) && Objects.equals(var2x, var4xxx)) {
         return this.fixTypeEverywhere(
            "BlockNameFlatteningFix",
                 var3xx,
                 var4xxx,
            var0 -> var0x -> var0x.mapSecond(
                     var0xx -> (String)var0xx.map(
                           BlockStateData::upgradeBlock, var0xxx -> BlockStateData.upgradeBlock(NamespacedSchema.ensureNamespaced(var0xxx))
                        )
                  )
         );
      } else {
         throw new IllegalStateException("Expected and actual types don't match.");
      }
   }
}
