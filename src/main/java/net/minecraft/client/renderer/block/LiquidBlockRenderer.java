package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlockRenderer {
   private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
   private TextureAtlasSprite waterOverlay;

   protected void setupSprites() {
      this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
      this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
      this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
      this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
      this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
   }

   private static boolean isNeighborSameFluid(BlockGetter var0, BlockPos var1, Direction var2, FluidState var3) {
      BlockPos var4 = var1.relative(var2);
      FluidState var5 = var0.getFluidState(var4);
      return var5.getType().isSame(var3.getType());
   }

   private static boolean isFaceOccludedByState(BlockGetter var0, Direction var1, float var2, BlockPos var3, BlockState var4) {
      if (var4.canOcclude()) {
         VoxelShape var5 = Shapes.box((double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F, (double)var2, (double)1.0F);
         VoxelShape var6 = var4.getOcclusionShape(var0, var3);
         return Shapes.blockOccudes(var5, var6, var1);
      } else {
         return false;
      }
   }

   private static boolean isFaceOccludedByNeighbor(BlockGetter var0, BlockPos var1, Direction var2, float var3) {
      BlockPos var4 = var1.relative(var2);
      BlockState var5 = var0.getBlockState(var4);
      return isFaceOccludedByState(var0, var2, var3, var4, var5);
   }

   private static boolean isFaceOccludedBySelf(BlockGetter var0, BlockPos var1, BlockState var2, Direction var3) {
      return isFaceOccludedByState(var0, var3.getOpposite(), 1.0F, var1, var2);
   }

   public static boolean shouldRenderFace(BlockAndTintGetter var0, BlockPos var1, FluidState var2, BlockState var3, Direction var4) {
      return !isFaceOccludedBySelf(var0, var1, var3, var4) && !isNeighborSameFluid(var0, var1, var4, var2);
   }

   public boolean tesselate(BlockAndTintGetter var1, BlockPos var2, VertexConsumer var3, FluidState var4) {
      boolean var5 = var4.is(FluidTags.LAVA);
      TextureAtlasSprite[] var6 = var5 ? this.lavaIcons : this.waterIcons;
      BlockState var7 = var1.getBlockState(var2);
      int var8 = var5 ? 16777215 : BiomeColors.getAverageWaterColor(var1, var2);
      float var9 = (float)(var8 >> 16 & 255) / 255.0F;
      float var10 = (float)(var8 >> 8 & 255) / 255.0F;
      float var11 = (float)(var8 & 255) / 255.0F;
      boolean var12 = !isNeighborSameFluid(var1, var2, Direction.UP, var4);
      boolean var13 = shouldRenderFace(var1, var2, var4, var7, Direction.DOWN) && !isFaceOccludedByNeighbor(var1, var2, Direction.DOWN, 0.8888889F);
      boolean var14 = shouldRenderFace(var1, var2, var4, var7, Direction.NORTH);
      boolean var15 = shouldRenderFace(var1, var2, var4, var7, Direction.SOUTH);
      boolean var16 = shouldRenderFace(var1, var2, var4, var7, Direction.WEST);
      boolean var17 = shouldRenderFace(var1, var2, var4, var7, Direction.EAST);
      if (!var12 && !var13 && !var17 && !var16 && !var14 && !var15) {
         return false;
      } else {
         boolean var18 = false;
         float var19 = var1.getShade(Direction.DOWN, true);
         float var20 = var1.getShade(Direction.UP, true);
         float var21 = var1.getShade(Direction.NORTH, true);
         float var22 = var1.getShade(Direction.WEST, true);
         float var23 = this.getWaterHeight(var1, var2, var4.getType());
         float var24 = this.getWaterHeight(var1, var2.south(), var4.getType());
         float var25 = this.getWaterHeight(var1, var2.east().south(), var4.getType());
         float var26 = this.getWaterHeight(var1, var2.east(), var4.getType());
         double var27 = (double)(var2.getX() & 15);
         double var29 = (double)(var2.getY() & 15);
         double var31 = (double)(var2.getZ() & 15);
         float var33 = 0.001F;
         float var34 = var13 ? 0.001F : 0.0F;
         if (var12 && !isFaceOccludedByNeighbor(var1, var2, Direction.UP, Math.min(Math.min(var23, var24), Math.min(var25, var26)))) {
            var18 = true;
            var23 -= 0.001F;
            var24 -= 0.001F;
            var25 -= 0.001F;
            var26 -= 0.001F;
            Vec3 var43 = var4.getFlow(var1, var2);
            float var35;
            float var36;
            float var37;
            float var38;
            float var39;
            float var40;
            float var41;
            float var42;
            if (var43.x == (double)0.0F && var43.z == (double)0.0F) {
               TextureAtlasSprite var82 = var6[0];
               var35 = var82.getU((double)0.0F);
               var39 = var82.getV((double)0.0F);
               var36 = var35;
               var40 = var82.getV((double)16.0F);
               var37 = var82.getU((double)16.0F);
               var41 = var40;
               var38 = var37;
               var42 = var39;
            } else {
               TextureAtlasSprite var44 = var6[1];
               float var45 = (float)Mth.atan2(var43.z, var43.x) - ((float)Math.PI / 2F);
               float var46 = Mth.sin(var45) * 0.25F;
               float var47 = Mth.cos(var45) * 0.25F;
               float var48 = 8.0F;
               var35 = var44.getU((8.0F + (-var47 - var46) * 16.0F));
               var39 = var44.getV((8.0F + (-var47 + var46) * 16.0F));
               var36 = var44.getU((8.0F + (-var47 + var46) * 16.0F));
               var40 = var44.getV((8.0F + (var47 + var46) * 16.0F));
               var37 = var44.getU((8.0F + (var47 + var46) * 16.0F));
               var41 = var44.getV((8.0F + (var47 - var46) * 16.0F));
               var38 = var44.getU((8.0F + (var47 - var46) * 16.0F));
               var42 = var44.getV((8.0F + (-var47 - var46) * 16.0F));
            }

            float var83 = (var35 + var36 + var37 + var38) / 4.0F;
            float var85 = (var39 + var40 + var41 + var42) / 4.0F;
            float var86 = (float)var6[0].getWidth() / (var6[0].getU1() - var6[0].getU0());
            float var88 = (float)var6[0].getHeight() / (var6[0].getV1() - var6[0].getV0());
            float var90 = 4.0F / Math.max(var88, var86);
            var35 = Mth.lerp(var90, var35, var83);
            var36 = Mth.lerp(var90, var36, var83);
            var37 = Mth.lerp(var90, var37, var83);
            var38 = Mth.lerp(var90, var38, var83);
            var39 = Mth.lerp(var90, var39, var85);
            var40 = Mth.lerp(var90, var40, var85);
            var41 = Mth.lerp(var90, var41, var85);
            var42 = Mth.lerp(var90, var42, var85);
            int var49 = this.getLightColor(var1, var2);
            float var50 = var20 * var9;
            float var51 = var20 * var10;
            float var52 = var20 * var11;
            this.vertex(var3, var27 + (double)0.0F, var29 + (double)var23, var31 + (double)0.0F, var50, var51, var52, var35, var39, var49);
            this.vertex(var3, var27 + (double)0.0F, var29 + (double)var24, var31 + (double)1.0F, var50, var51, var52, var36, var40, var49);
            this.vertex(var3, var27 + (double)1.0F, var29 + (double)var25, var31 + (double)1.0F, var50, var51, var52, var37, var41, var49);
            this.vertex(var3, var27 + (double)1.0F, var29 + (double)var26, var31 + (double)0.0F, var50, var51, var52, var38, var42, var49);
            if (var4.shouldRenderBackwardUpFace(var1, var2.above())) {
               this.vertex(var3, var27 + (double)0.0F, var29 + (double)var23, var31 + (double)0.0F, var50, var51, var52, var35, var39, var49);
               this.vertex(var3, var27 + (double)1.0F, var29 + (double)var26, var31 + (double)0.0F, var50, var51, var52, var38, var42, var49);
               this.vertex(var3, var27 + (double)1.0F, var29 + (double)var25, var31 + (double)1.0F, var50, var51, var52, var37, var41, var49);
               this.vertex(var3, var27 + (double)0.0F, var29 + (double)var24, var31 + (double)1.0F, var50, var51, var52, var36, var40, var49);
            }
         }

         if (var13) {
            float var61 = var6[0].getU0();
            float var64 = var6[0].getU1();
            float var67 = var6[0].getV0();
            float var70 = var6[0].getV1();
            int var73 = this.getLightColor(var1, var2.below());
            float var75 = var19 * var9;
            float var78 = var19 * var10;
            float var80 = var19 * var11;
            this.vertex(var3, var27, var29 + (double)var34, var31 + (double)1.0F, var75, var78, var80, var61, var70, var73);
            this.vertex(var3, var27, var29 + (double)var34, var31, var75, var78, var80, var61, var67, var73);
            this.vertex(var3, var27 + (double)1.0F, var29 + (double)var34, var31, var75, var78, var80, var64, var67, var73);
            this.vertex(var3, var27 + (double)1.0F, var29 + (double)var34, var31 + (double)1.0F, var75, var78, var80, var64, var70, var73);
            var18 = true;
         }

         for(int var62 = 0; var62 < 4; ++var62) {
            float var65;
            float var68;
            double var71;
            double var76;
            double var81;
            double var84;
            Direction var87;
            boolean var89;
            if (var62 == 0) {
               var65 = var23;
               var68 = var26;
               var71 = var27;
               var81 = var27 + (double)1.0F;
               var76 = var31 + (double)0.001F;
               var84 = var31 + (double)0.001F;
               var87 = Direction.NORTH;
               var89 = var14;
            } else if (var62 == 1) {
               var65 = var25;
               var68 = var24;
               var71 = var27 + (double)1.0F;
               var81 = var27;
               var76 = var31 + (double)1.0F - (double)0.001F;
               var84 = var31 + (double)1.0F - (double)0.001F;
               var87 = Direction.SOUTH;
               var89 = var15;
            } else if (var62 == 2) {
               var65 = var24;
               var68 = var23;
               var71 = var27 + (double)0.001F;
               var81 = var27 + (double)0.001F;
               var76 = var31 + (double)1.0F;
               var84 = var31;
               var87 = Direction.WEST;
               var89 = var16;
            } else {
               var65 = var26;
               var68 = var25;
               var71 = var27 + (double)1.0F - (double)0.001F;
               var81 = var27 + (double)1.0F - (double)0.001F;
               var76 = var31;
               var84 = var31 + (double)1.0F;
               var87 = Direction.EAST;
               var89 = var17;
            }

            if (var89 && !isFaceOccludedByNeighbor(var1, var2, var87, Math.max(var65, var68))) {
               var18 = true;
               BlockPos var91 = var2.relative(var87);
               TextureAtlasSprite var92 = var6[1];
               if (!var5) {
                  Block var93 = var1.getBlockState(var91).getBlock();
                  if (var93 instanceof HalfTransparentBlock || var93 instanceof LeavesBlock) {
                     var92 = this.waterOverlay;
                  }
               }

               float var94 = var92.getU(0.0F);
               float var95 = var92.getU(8.0F);
               float var96 = var92.getV(((1.0F - var65) * 16.0F * 0.5F));
               float var53 = var92.getV(((1.0F - var68) * 16.0F * 0.5F));
               float var54 = var92.getV(8.0F);
               int var55 = this.getLightColor(var1, var91);
               float var56 = var62 < 2 ? var21 : var22;
               float var57 = var20 * var56 * var9;
               float var58 = var20 * var56 * var10;
               float var59 = var20 * var56 * var11;
               this.vertex(var3, var71, var29 + (double)var65, var76, var57, var58, var59, var94, var96, var55);
               this.vertex(var3, var81, var29 + (double)var68, var84, var57, var58, var59, var95, var53, var55);
               this.vertex(var3, var81, var29 + (double)var34, var84, var57, var58, var59, var95, var54, var55);
               this.vertex(var3, var71, var29 + (double)var34, var76, var57, var58, var59, var94, var54, var55);
               if (var92 != this.waterOverlay) {
                  this.vertex(var3, var71, var29 + (double)var34, var76, var57, var58, var59, var94, var54, var55);
                  this.vertex(var3, var81, var29 + (double)var34, var84, var57, var58, var59, var95, var54, var55);
                  this.vertex(var3, var81, var29 + (double)var68, var84, var57, var58, var59, var95, var53, var55);
                  this.vertex(var3, var71, var29 + (double)var65, var76, var57, var58, var59, var94, var96, var55);
               }
            }
         }

         return var18;
      }
   }

   private void vertex(VertexConsumer var1, double var2, double var4, double var6, float var8, float var9, float var10, float var11, float var12, int var13) {
      var1.vertex(var2, var4, var6).color(var8, var9, var10, 1.0F).uv(var11, var12).uv2(var13).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private int getLightColor(BlockAndTintGetter var1, BlockPos var2) {
      int var3 = LevelRenderer.getLightColor(var1, var2);
      int var4 = LevelRenderer.getLightColor(var1, var2.above());
      int var5 = var3 & 255;
      int var6 = var4 & 255;
      int var7 = var3 >> 16 & 255;
      int var8 = var4 >> 16 & 255;
      return (var5 > var6 ? var5 : var6) | (var7 > var8 ? var7 : var8) << 16;
   }

   private float getWaterHeight(BlockGetter var1, BlockPos var2, Fluid var3) {
      int var4 = 0;
      float var5 = 0.0F;

      for(int var6 = 0; var6 < 4; ++var6) {
         BlockPos var7 = var2.offset(-(var6 & 1), 0, -(var6 >> 1 & 1));
         if (var1.getFluidState(var7.above()).getType().isSame(var3)) {
            return 1.0F;
         }

         FluidState var8 = var1.getFluidState(var7);
         if (var8.getType().isSame(var3)) {
            float var9 = var8.getHeight(var1, var7);
            if (var9 >= 0.8F) {
               var5 += var9 * 10.0F;
               var4 += 10;
            } else {
               var5 += var9;
               ++var4;
            }
         } else if (!var1.getBlockState(var7).getMaterial().isSolid()) {
            ++var4;
         }
      }

      return var5 / (float)var4;
   }
}
