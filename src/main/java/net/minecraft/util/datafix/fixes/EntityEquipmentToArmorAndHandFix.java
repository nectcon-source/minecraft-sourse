package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
   public EntityEquipmentToArmorAndHandFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK));
   }

   private <IS> TypeRewriteRule cap(Type<IS> var1) {
      Type<Pair<Either<List<IS>, Unit>, Dynamic<?>>> var2 = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(var1))), DSL.remainderType());
      Type<Pair<Either<List<IS>, Unit>, Pair<Either<List<IS>, Unit>, Dynamic<?>>>> var3x = DSL.and(
         DSL.optional(DSL.field("ArmorItems", DSL.list(var1))), DSL.optional(DSL.field("HandItems", DSL.list(var1))), DSL.remainderType()
      );
      OpticFinder<Pair<Either<List<IS>, Unit>, Dynamic<?>>> var4xx = DSL.typeFinder(var2);
      OpticFinder<List<IS>> var5xxx = DSL.fieldFinder("Equipment", DSL.list(var1));
      return this.fixTypeEverywhereTyped(
         "EntityEquipmentToArmorAndHandFix",
         this.getInputSchema().getType(References.ENTITY),
         this.getOutputSchema().getType(References.ENTITY),
         var4x -> {
            Either<List<IS>, Unit> var5x = Either.right(DSL.unit());
            Either<List<IS>, Unit> var6x = Either.right(DSL.unit());
            Dynamic<?> var9_9xx = (Dynamic)var4x.getOrCreate(DSL.remainderFinder());
            Optional<List<IS>> var8xxx = var4x.getOptional(var5xxx);
            if (var8xxx.isPresent()) {
               List<IS> var9xxxx = var8xxx.get();
               IS var10xxxxx = (IS)((Pair)var1.read(var9_9xx.emptyMap())
                     .result()
                     .orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")))
                  .getFirst();
               if (!var9xxxx.isEmpty()) {
                  var5x = Either.left((List<IS>) Lists.newArrayList(new Object[]{var9xxxx.get(0), var10xxxxx}));
               }
   
               if (var9xxxx.size() > 1) {
                  List<IS> var11xxxx = (List<IS>) Lists.newArrayList(new Object[]{var10xxxxx, var10xxxxx, var10xxxxx, var10xxxxx});
   
                  for(int var12xxxxx = 1; var12xxxxx < Math.min(var9xxxx.size(), 5); ++var12xxxxx) {
                      var11xxxx.set(var12xxxxx - 1, var9xxxx.get(var12xxxxx));
                  }

                   var6x = Either.left(var11xxxx);
               }
            }
   
            Dynamic<?> var9_9 = var9_9xx;
            Optional<? extends Stream<? extends Dynamic<?>>> var14x = var9_9xx.get("DropChances").asStreamOpt().result();
            if (var14x.isPresent()) {
               Iterator<? extends Dynamic<?>> var15xx = Stream.concat(var14x.get(), Stream.generate(() -> var9_9.createInt(0))).iterator();
               float var16xxx = ((Dynamic)var15xx.next()).asFloat(0.0F);
               if (!var9_9xx.get("HandDropChances").result().isPresent()) {
                  Dynamic<?> var13xxxx = var9_9xx.createList(Stream.of(var16xxx, 0.0F).map(var9_9xx::createFloat));
                   var9_9xx = var9_9xx.set("HandDropChances", var13xxxx);
               }
   
               if (!var9_9xx.get("ArmorDropChances").result().isPresent()) {
                  Dynamic<?> var17xx = var9_9xx.createList(
                     Stream.of(
                           ((Dynamic)var15xx.next()).asFloat(0.0F),
                           ((Dynamic)var15xx.next()).asFloat(0.0F),
                           ((Dynamic)var15xx.next()).asFloat(0.0F),
                           ((Dynamic)var15xx.next()).asFloat(0.0F)
                        )
                        .map(var9_9xx::createFloat)
                  );
                   var9_9xx = var9_9xx.set("ArmorDropChances", var17xx);
               }

                var9_9xx = var9_9xx.remove("DropChances");
            }
   
            return var4x.set(var4xx, var3x, Pair.of(var5x, Pair.of(var6x, var9_9xx)));
         }
      );
   }
}
