package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface TagCollection<T> {
   Map<ResourceLocation, Tag<T>> getAllTags();

   @Nullable
   default Tag<T> getTag(ResourceLocation var1) {
      return this.getAllTags().get(var1);
   }

   Tag<T> getTagOrEmpty(ResourceLocation var1);

   @Nullable
   ResourceLocation getId(Tag<T> var1);

   default ResourceLocation getIdOrThrow(Tag<T> var1) {
      ResourceLocation var2 = this.getId(var1);
      if (var2 == null) {
         throw new IllegalStateException("Unrecognized tag");
      } else {
         return var2;
      }
   }

   default Collection<ResourceLocation> getAvailableTags() {
      return this.getAllTags().keySet();
   }

   default Collection<ResourceLocation> getMatchingTags(T var1) {
      List<ResourceLocation> var2 = Lists.newArrayList();

      for(Map.Entry<ResourceLocation, Tag<T>> var4 : this.getAllTags().entrySet()) {
         if ((var4.getValue()).contains(var1)) {
            var2.add(var4.getKey());
         }
      }

      return var2;
   }

   default void serializeToNetwork(FriendlyByteBuf var1, DefaultedRegistry<T> var2) {
      Map<ResourceLocation, Tag<T>> var3 = this.getAllTags();
      var1.writeVarInt(var3.size());

      for(Map.Entry<ResourceLocation, Tag<T>> var5 : var3.entrySet()) {
         var1.writeResourceLocation(var5.getKey());
         var1.writeVarInt((var5.getValue()).getValues().size());

         for(T var7 : (var5.getValue()).getValues()) {
            var1.writeVarInt(var2.getId(var7));
         }
      }
   }

   static <T> TagCollection<T> loadFromNetwork(FriendlyByteBuf var0, Registry<T> var1) {
      Map<ResourceLocation, Tag<T>> var2 = Maps.newHashMap();
      int var3 = var0.readVarInt();

      for(int var4 = 0; var4 < var3; ++var4) {
         ResourceLocation var5 = var0.readResourceLocation();
         int var6 = var0.readVarInt();
         ImmutableSet.Builder<T> var7 = ImmutableSet.builder();

         for(int var8 = 0; var8 < var6; ++var8) {
            var7.add(var1.byId(var0.readVarInt()));
         }

         var2.put(var5, Tag.fromSet(var7.build()));
      }

      return of(var2);
   }

   static <T> TagCollection<T> empty() {
      return of(ImmutableBiMap.of());
   }

   static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> var0) {
      final BiMap<ResourceLocation, Tag<T>> var1 = ImmutableBiMap.copyOf(var0);
      return new TagCollection<T>() {
         private final Tag<T> empty = SetTag.empty();

         @Override
         public Tag<T> getTagOrEmpty(ResourceLocation var1x) {
            return (Tag<T>)var1.getOrDefault(var1x, this.empty);
         }

         @Nullable
         @Override
         public ResourceLocation getId(Tag<T> var1x) {
            return var1x instanceof Tag.Named ? ((Tag.Named)var1x).getName() : (ResourceLocation)var1.inverse().get(var1x);
         }

         @Override
         public Map<ResourceLocation, Tag<T>> getAllTags() {
            return var1;
         }
      };
   }
}
