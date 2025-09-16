package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;

public class EntityMinecartIdentifiersFix extends DataFix {
   private static final List<String> MINECART_BY_ID = Lists.newArrayList(new String[]{"MinecartRideable", "MinecartChest", "MinecartFurnace"});

   public EntityMinecartIdentifiersFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      TaggedChoiceType<String> var1 = (TaggedChoiceType<String>) this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoiceType<String> var2 = (TaggedChoiceType<String>) this.getOutputSchema().findChoiceType(References.ENTITY);
      return this.fixTypeEverywhere(
         "EntityMinecartIdentifiersFix",
              var1,
              var2,
         var2x -> var3 -> {
               if (!Objects.equals(var3.getFirst(), "Minecart")) {
                  return var3;
               } else {
                  Typed<? extends Pair<String, ?>> var4x = (Typed)var2.point(var2x, "Minecart", var3.getSecond()).orElseThrow(IllegalStateException::new);
                  Dynamic<?> var5xx = (Dynamic)var4x.getOrCreate(DSL.remainderFinder());
                  int var7xxx = var5xx.get("Type").asInt(0);
                  String var6;
                  if (var7xxx > 0 && var7xxx < MINECART_BY_ID.size()) {
                     var6 = MINECART_BY_ID.get(var7xxx);
                  } else {
                     var6 = "MinecartRideable";
                  }
   
                  return Pair.of(
                          var6,
                          var4x.write()
                        .map(var2xxx -> ((Type)var2.types().get(var6)).read(var2xxx))
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Could not read the new minecart."))
                  );
               }
            }
      );
   }
}
