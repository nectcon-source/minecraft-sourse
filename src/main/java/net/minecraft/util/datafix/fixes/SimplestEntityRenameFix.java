package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class SimplestEntityRenameFix extends DataFix {
   private final String name;

   public SimplestEntityRenameFix(String var1, Schema var2, boolean var3) {
      super(var2,var3 );
      this.name = var1;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoiceType<String> var1 = (TaggedChoiceType<String>) this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoiceType<String> var2x = (TaggedChoiceType<String>) this.getOutputSchema().findChoiceType(References.ENTITY);
      Type<Pair<String, String>> var3xx = DSL.named(References.ENTITY_NAME.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(this.getOutputSchema().getType(References.ENTITY_NAME), var3xx)) {
         throw new IllegalStateException("Entity name type is not what was expected.");
      } else {
         return TypeRewriteRule.seq(this.fixTypeEverywhere(this.name, var1, var2x, var3x -> var3xx_ -> var3xx_.mapFirst(var3xxx -> {
                  String var4 = this.rename(var3xxx);
                  Type<?> var5x = (Type)var1.types().get(var3xxx);
                  Type<?> var6xx = (Type)var2x.types().get(var4);
                  if (!var6xx.equals(var5x, true, true)) {
                     throw new IllegalStateException(String.format("Dynamic type check failed: %s not equal to %s", var6xx, var5x));
                  } else {
                     return var4;
                  }
               })), this.fixTypeEverywhere(this.name + " for entity name", var3xx, var1x -> var1xx -> var1xx.mapSecond(this::rename)));
      }
   }

   protected abstract String rename(String var1);
}
