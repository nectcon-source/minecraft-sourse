package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityRidingToPassengersFix extends DataFix {
   public EntityRidingToPassengersFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Schema var1 = this.getInputSchema();
      Schema var2 = this.getOutputSchema();
      Type<?> var3 = var1.getTypeRaw(References.ENTITY_TREE);
      Type<?> var4 = var2.getTypeRaw(References.ENTITY_TREE);
      Type<?> var5 = var1.getTypeRaw(References.ENTITY);
      return this.cap(var1, var2, var3, var4, var5);
   }

   private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(
      Schema var1, Schema var2, Type<OldEntityTree> var3, Type<NewEntityTree> var4, Type<Entity> var5
   ) {
      Type<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> var6 = DSL.named(
         References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", var3)), var5)
      );
      Type<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var7x = DSL.named(
         References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(var4))), var5)
      );
      Type<?> var8xx = var1.getType(References.ENTITY_TREE);
      Type<?> var9xxx = var2.getType(References.ENTITY_TREE);
      if (!Objects.equals(var8xx, var6)) {
         throw new IllegalStateException("Old entity type is not what was expected.");
      } else if (!var9xxx.equals(var7x, true, true)) {
         throw new IllegalStateException("New entity type is not what was expected.");
      } else {
         OpticFinder<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> var10 = DSL.typeFinder(var6);
         OpticFinder<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var11x = DSL.typeFinder(var7x);
         OpticFinder<NewEntityTree> var12xx = DSL.typeFinder(var4);
         Type<?> var13xxx = var1.getType(References.PLAYER);
         Type<?> var14xxxx = var2.getType(References.PLAYER);
         return TypeRewriteRule.seq(
            this.fixTypeEverywhere(
               "EntityRidingToPassengerFix",var6,var7x,
               var5x -> var6x -> {
                     Optional<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> var7x_ = Optional.empty();
                     Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>> var8x = var6x;
      
                     while(true) {
                        Either<List<NewEntityTree>, Unit> var9xx = DataFixUtils.orElse(
                           var7x_.map(
                              var4xxx -> {
                                 Typed<NewEntityTree> var5xxx = var4.pointTyped(var5x)
                                    .orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                                 NewEntityTree var6x_ = var5xxx.set(var11x, var4xxx)
                                    .getOptional(var12xx)
                                    .orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                                 return Either.left(ImmutableList.of(var6x_));
                              }
                           ),
                           Either.right(DSL.unit())
                        );
                        var7x_ = Optional.of(Pair.of(References.ENTITY_TREE.typeName(), Pair.of(var9xx, (var8x.getSecond()).getSecond())));
                        Optional<OldEntityTree> var10xxxx = ((Either)((Pair)var8x.getSecond()).getFirst()).left();
                        if (!var10xxxx.isPresent()) {
                           return (Pair)var7x_.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                        }

                         try {
                             var8x = (Pair)(new Typed(var3, var5x, var10xxxx.get())
                                .getOptional(var10)
                                .orElseThrow(() -> new IllegalStateException("Should always have an entity here")));
                         } catch (Throwable e) {
                             throw new RuntimeException(e);
                         }
                     }
                  }
            ),
            this.writeAndRead("player RootVehicle injecter", var13xxx, var14xxxx)
         );
      }
   }
}
