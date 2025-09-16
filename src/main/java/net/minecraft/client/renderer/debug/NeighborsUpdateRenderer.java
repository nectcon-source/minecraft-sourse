package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

   NeighborsUpdateRenderer(Minecraft var1) {
      this.minecraft = var1;
   }

   public void addUpdate(long var1, BlockPos var3) {
      Map<BlockPos, Integer> var4 = this.lastUpdate.computeIfAbsent(var1, (var0) -> Maps.newHashMap());
      int var5 = var4.getOrDefault(var3, 0);
      var4.put(var3, var5 + 1);
   }

   @Override
   public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7) {
      long var9 = this.minecraft.level.getGameTime();
      int var11 = 200;
      double var12 = 0.0025;
      Set<BlockPos> var14 = Sets.newHashSet();
      Map<BlockPos, Integer> var15 = Maps.newHashMap();
      VertexConsumer var16 = var2.getBuffer(RenderType.lines());
      Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> var17 = this.lastUpdate.entrySet().iterator();

      while(var17.hasNext()) {
         Map.Entry<Long, Map<BlockPos, Integer>> var18 = var17.next();
         Long var19 = var18.getKey();
         Map<BlockPos, Integer> var20 = var18.getValue();
         long var21 = var9 - var19;
         if (var21 > 200L) {
            var17.remove();
         } else {
            for(Map.Entry<BlockPos, Integer> var24 : var20.entrySet()) {
               BlockPos var25 = var24.getKey();
               Integer var26 = var24.getValue();
               if (var14.add(var25)) {
                  AABB var27 = (new AABB(BlockPos.ZERO)).inflate(0.002).deflate(0.0025 * (double)var21).move(var25.getX(), var25.getY(), var25.getZ()).move(-var3, -var5, -var7);
                  LevelRenderer.renderLineBox(var1, var16, var27.minX, var27.minY, var27.minZ, var27.maxX, var27.maxY, var27.maxZ, 1.0F, 1.0F, 1.0F, 1.0F);
                  var15.put(var25, var26);
               }
            }
         }
      }

      for(Map.Entry<BlockPos, Integer> var29 : var15.entrySet()) {
         BlockPos var30 = var29.getKey();
         Integer var31 = var29.getValue();
         DebugRenderer.renderFloatingText(String.valueOf(var31), var30.getX(), var30.getY(), var30.getZ(), -1);
      }
   }
}
