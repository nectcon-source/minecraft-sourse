package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix extends DataFix {
   private final String name;

   public BlockRenameFix(Schema var1, String var2) {
      super(var1, false);
      this.name = var2;
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.BLOCK_NAME);
      Type<Pair<String, String>> var2x = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(var1, var2x)) {
         throw new IllegalStateException("block type is not what was expected.");
      } else {
         TypeRewriteRule var3 = this.fixTypeEverywhere(this.name + " for block", var2x, var1x -> var1xx -> var1xx.mapSecond(this::fixBlock));
         TypeRewriteRule var4x = this.fixTypeEverywhereTyped(
            this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), var1x -> var1x.update(DSL.remainderFinder(), var1xx -> {
                  Optional<String> var2x_ = var1xx.get("Name").asString().result();
                  return var2x_.isPresent() ? var1xx.set("Name", var1xx.createString(this.fixBlock((String)var2x_.get()))) : var1xx;
               })
         );
         return TypeRewriteRule.seq(var3, var4x);
      }
   }

   protected abstract String fixBlock(String var1);

   public static DataFix create(Schema var0, String var1, final Function<String, String> var2) {
      return new BlockRenameFix(var0, var1) {
         @Override
         protected String fixBlock(String var1) {
            return var2.apply(var1);
         }
      };
   }
}
