package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;

   public LightDebugRenderer(Minecraft var1) {
      this.minecraft = var1;
   }

   @Override
   public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7) {
      Level var9 = this.minecraft.level;
      RenderSystem.pushMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      BlockPos var10 = new BlockPos(var3, var5, var7);
      LongSet var11 = new LongOpenHashSet();

      for(BlockPos var13 : BlockPos.betweenClosed(var10.offset(-10, -10, -10), var10.offset(10, 10, 10))) {
         int var14 = var9.getBrightness(LightLayer.SKY, var13);
         float var15 = (float)(15 - var14) / 15.0F * 0.5F + 0.16F;
         int var16 = Mth.hsvToRgb(var15, 0.9F, 0.9F);
         long var17 = SectionPos.blockToSection(var13.asLong());
         if (var11.add(var17)) {
            DebugRenderer.renderFloatingText(var9.getChunkSource().getLightEngine().getDebugData(LightLayer.SKY, SectionPos.of(var17)), (SectionPos.x(var17) * 16 + 8), (SectionPos.y(var17) * 16 + 8), (SectionPos.z(var17) * 16 + 8), 16711680, 0.3F);
         }

         if (var14 != 15) {
            DebugRenderer.renderFloatingText(String.valueOf(var14), (double)var13.getX() + (double)0.5F, (double)var13.getY() + (double)0.25F, (double)var13.getZ() + (double)0.5F, var16);
         }
      }

      RenderSystem.enableTexture();
      RenderSystem.popMatrix();
   }
}
