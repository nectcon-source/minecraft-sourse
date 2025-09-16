//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedBakedModel implements BakedModel {
   private final int totalWeight;
   private final List<WeightedModel> list;
   private final BakedModel wrapped;

   public WeightedBakedModel(List<WeightedModel> var1) {
      this.list = var1;
      this.totalWeight = WeighedRandom.getTotalWeight(var1);
      this.wrapped = ((WeightedModel)var1.get(0)).model;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState var1, @Nullable Direction var2, Random var3) {
      return ((WeightedModel)WeighedRandom.getWeightedItem(this.list, Math.abs((int)var3.nextLong()) % this.totalWeight)).model.getQuads(var1, var2, var3);
   }

   public boolean useAmbientOcclusion() {
      return this.wrapped.useAmbientOcclusion();
   }

   public boolean isGui3d() {
      return this.wrapped.isGui3d();
   }

   public boolean usesBlockLight() {
      return this.wrapped.usesBlockLight();
   }

   public boolean isCustomRenderer() {
      return this.wrapped.isCustomRenderer();
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.wrapped.getParticleIcon();
   }

   public ItemTransforms getTransforms() {
      return this.wrapped.getTransforms();
   }

   public ItemOverrides getOverrides() {
      return this.wrapped.getOverrides();
   }

   public static class Builder {
      private final List<WeightedModel> list = Lists.newArrayList();

      public Builder() {
      }

      public Builder add(@Nullable BakedModel var1, int var2) {
         if (var1 != null) {
            this.list.add(new WeightedModel(var1, var2));
         }

         return this;
      }

      @Nullable
      public BakedModel build() {
         if (this.list.isEmpty()) {
            return null;
         } else {
            return (BakedModel)(this.list.size() == 1 ? ((WeightedModel)this.list.get(0)).model : new WeightedBakedModel(this.list));
         }
      }
   }

   static class WeightedModel extends WeighedRandom.WeighedRandomItem {
      protected final BakedModel model;

      public WeightedModel(BakedModel var1, int var2) {
         super(var2);
         this.model = var1;
      }
   }
}
