package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public class RaidDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private Collection<BlockPos> raidCenters = Lists.newArrayList();

   public RaidDebugRenderer(Minecraft var1) {
      this.minecraft = var1;
   }

   public void setRaidCenters(Collection<BlockPos> var1) {
      this.raidCenters = var1;
   }

   @Override
   public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7) {
      BlockPos var9 = this.getCamera().getBlockPosition();

      for(BlockPos var11 : this.raidCenters) {
         if (var9.closerThan(var11, (double)160.0F)) {
            highlightRaidCenter(var11);
         }
      }
   }

   private static void highlightRaidCenter(BlockPos var0) {
      DebugRenderer.renderFilledBox(var0.offset((double)-0.5F, (double)-0.5F, (double)-0.5F), var0.offset((double)1.5F, (double)1.5F, (double)1.5F), 1.0F, 0.0F, 0.0F, 0.15F);
      int var1 = -65536;
      renderTextOverBlock("Raid center", var0, -65536);
   }

   private static void renderTextOverBlock(String var0, BlockPos var1, int var2) {
      double var3 = (double)var1.getX() + (double)0.5F;
      double var5 = (double)var1.getY() + 1.3;
      double var7 = (double)var1.getZ() + (double)0.5F;
      DebugRenderer.renderFloatingText(var0, var3, var5, var7, var2, 0.04F, true, 0.0F, true);
   }

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }
}
