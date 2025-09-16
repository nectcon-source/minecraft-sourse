package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class FurnaceRecipeFix extends DataFix {
   public FurnaceRecipeFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
   }

   private <R> TypeRewriteRule cap(Type<R> var1) {
      Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> var2 = DSL.and(
         DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(var1, DSL.intType()), DSL.remainderType()))), DSL.remainderType()
      );
      OpticFinder<?> var3x = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
      OpticFinder<?> var4xx = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace"));
      OpticFinder<?> var5xxx = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
      Type<?> var6xxxx = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
      Type<?> var7xxxxx = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
      Type<?> var8xxxxxx = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
      Type<?> var9xxxxxxx = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> var10xxxxxxxx = this.getOutputSchema().getType(References.BLOCK_ENTITY);
      return this.fixTypeEverywhereTyped(
         "FurnaceRecipesFix",
              var9xxxxxxx,
              var10xxxxxxxx,
         var9x -> var9x.updateTyped(var3x, var6xxxx, var3xx -> this.updateFurnaceContents(var1, var2, var3xx))
               .updateTyped(var4xx, var7xxxxx, var3xx -> this.updateFurnaceContents(var1, var2, var3xx))
               .updateTyped(var5xxx, var8xxxxxx, var3xx -> this.updateFurnaceContents(var1, var2, var3xx))
      );
   }

   private <R> Typed<?> updateFurnaceContents(Type<R> var1, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> var2, Typed<?> var3) {
      Dynamic<?> var4 = (Dynamic)var3.getOrCreate(DSL.remainderFinder());
      int var5x = var4.get("RecipesUsedSize").asInt(0);
      var4 = var4.remove("RecipesUsedSize");
      List<Pair<R, Integer>> var6xx = Lists.newArrayList();

      for(int var7xxx = 0; var7xxx < var5x; ++var7xxx) {
         String var8xxxx = "RecipeLocation" + var7xxx;
         String var9xxxxx = "RecipeAmount" + var7xxx;
         Optional<? extends Dynamic<?>> var10xxxxxx = var4.get(var8xxxx).result();
         int var11xxxxxxx = var4.get(var9xxxxx).asInt(0);
         if (var11xxxxxxx > 0) {
            var10xxxxxx.ifPresent(var3x -> {
               Optional<? extends Pair<R, ? extends Dynamic<?>>> var4x = var1.read(var3x).result();
               var4x.ifPresent(var2xx -> var6xx.add(Pair.of(var2xx.getFirst(), var11xxxxxxx)));
            });
         }

         var4 = var4.remove(var8xxxx).remove(var9xxxxx);
      }

      return var3.set(DSL.remainderFinder(), var2, Pair.of(Either.left(Pair.of(var6xx, var4.emptyMap())), var4));
   }
}
