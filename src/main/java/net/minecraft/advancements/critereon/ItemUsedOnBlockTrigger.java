package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemUsedOnBlockTrigger extends SimpleCriterionTrigger<ItemUsedOnBlockTrigger.TriggerInstance> {
   private static final ResourceLocation ID = new ResourceLocation("item_used_on_block");

   @Override
   public ResourceLocation getId() {
      return ID;
   }

   public ItemUsedOnBlockTrigger.TriggerInstance createInstance(JsonObject var1, EntityPredicate.Composite var2, DeserializationContext var3) {
      LocationPredicate var4 = LocationPredicate.fromJson(var1.get("location"));
      ItemPredicate var5 = ItemPredicate.fromJson(var1.get("item"));
      return new TriggerInstance(var2, var4, var5);
   }

   public void trigger(ServerPlayer var1, BlockPos var2, ItemStack var3) {
      BlockState var4 = var1.getLevel().getBlockState(var2);
      this.trigger(var1, (var4x) -> var4x.matches(var4, var1.getLevel(), var2, var3));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final LocationPredicate location;
      private final ItemPredicate item;

      public TriggerInstance(EntityPredicate.Composite var1, LocationPredicate var2, ItemPredicate var3) {
         super(ItemUsedOnBlockTrigger.ID, var1);
         this.location = var2;
         this.item = var3;
      }

      public static ItemUsedOnBlockTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder var0, ItemPredicate.Builder var1) {
         return new TriggerInstance(EntityPredicate.Composite.ANY, var0.build(), var1.build());
      }

      public boolean matches(BlockState var1, ServerLevel var2, BlockPos var3, ItemStack var4) {
         return !this.location.matches(var2, (double)var3.getX() + (double)0.5F, (double)var3.getY() + (double)0.5F, (double)var3.getZ() + (double)0.5F) ? false : this.item.matches(var4);
      }

      @Override
      public JsonObject serializeToJson(SerializationContext var1) {
         JsonObject var2 = super.serializeToJson(var1);
         var2.add("location", this.location.serializeToJson());
         var2.add("item", this.item.serializeToJson());
         return var2;
      }
   }
}
